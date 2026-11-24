// ==============================================================================
// File      : CGL/src/common/image_loader.cpp
// Subject   : Common | BMP Loader & Writer
// Topics    : [TOPIC: BMP Loader] [TOPIC: 24-bit BMP Texture Mapping] [TOPIC: 4-Byte Row Padding]
// Game Role : Parses binary BMP headers, strips DWORD row padding, and converts BGR pixels to RGB for OpenGL.
// ==============================================================================

#include "image_loader.hpp"
#include <fstream>
#include <iostream>
#include <cstdint>
#include <cmath>

namespace CGL {

// WHY: Pack structs to 1-byte boundaries so binary header fields match the exact on-disk BMP layout
#pragma pack(push, 1)
struct BMPFileHeader {
    uint16_t bfType{0x4D42}; // 'BM'
    uint32_t bfSize{0};
    uint16_t bfReserved1{0};
    uint16_t bfReserved2{0};
    uint32_t bfOffBits{54};
};

struct BMPInfoHeader {
    uint32_t biSize{40};
    int32_t  biWidth{0};
    int32_t  biHeight{0};
    uint16_t biPlanes{1};
    uint16_t biBitCount{24};
    uint32_t biCompression{0};
    uint32_t biSizeImage{0};
    int32_t  biXPelsPerMeter{2835};
    int32_t  biYPelsPerMeter{2835};
    uint32_t biClrUsed{0};
    uint32_t biClrImportant{0};
};
#pragma pack(pop)

// [TOPIC: BMP Loader / BGR-to-RGB Conversion]
// Purpose: Validates BMP headers, reads BI_RGB pixel rows with padding, and outputs bottom-up RGB bytes.
// Game Role: Feeds space_hull.bmp and alien_surface.bmp into glTexImage2D for CO6 and combat scenes.
bool loadBMP(const std::string& filepath, int& width, int& height, std::vector<unsigned char>& rgbData) {
    std::ifstream file(filepath, std::ios::binary);
    if (!file.is_open()) {
        std::cerr << "[WARN] Texture file not found or inaccessible: " << filepath << std::endl;
        return false;
    }

    BMPFileHeader fileHeader;
    file.read(reinterpret_cast<char*>(&fileHeader), sizeof(fileHeader));
    if (fileHeader.bfType != 0x4D42) {
        std::cerr << "[WARN] Invalid BMP header magic (not 'BM'): " << filepath << std::endl;
        return false;
    }

    BMPInfoHeader infoHeader;
    file.read(reinterpret_cast<char*>(&infoHeader), sizeof(infoHeader));

    if (infoHeader.biBitCount != 24 && infoHeader.biBitCount != 32) {
        std::cerr << "[WARN] Unsupported BMP bit depth (" << infoHeader.biBitCount
                  << " bits, must be 24 or 32): " << filepath << std::endl;
        return false;
    }

    if (infoHeader.biCompression != 0) {
        std::cerr << "[WARN] Compressed BMP formats not supported (BI_RGB only): " << filepath << std::endl;
        return false;
    }

    width = std::abs(infoHeader.biWidth);
    height = std::abs(infoHeader.biHeight);
    // WHY: Negative biHeight denotes top-down row storage; flip to match OpenGL bottom-up texcoords
    bool flipY = (infoHeader.biHeight < 0);

    int bytesPerPixel = infoHeader.biBitCount / 8;
    // WHY: Windows BMP scanlines are padded up to the next 4-byte (DWORD) multiple
    int rowPadding = (4 - (width * bytesPerPixel) % 4) % 4;
    int rowStride = width * bytesPerPixel + rowPadding;

    file.seekg(fileHeader.bfOffBits, std::ios::beg);

    rgbData.resize(width * height * 3);
    std::vector<uint8_t> rowBuffer(rowStride);

    for (int y = 0; y < height; ++y) {
        file.read(reinterpret_cast<char*>(rowBuffer.data()), rowStride);
        if (!file) {
            std::cerr << "[WARN] Unexpected EOF while reading BMP pixels: " << filepath << std::endl;
            return false;
        }

        int targetY = flipY ? (height - 1 - y) : y;

        for (int x = 0; x < width; ++x) {
            int srcIdx = x * bytesPerPixel;
            int dstIdx = (targetY * width + x) * 3;

            // WHY: BMP stores pixels in BGR(A) order while glTexImage2D expects GL_RGB order
            uint8_t b = rowBuffer[srcIdx + 0];
            uint8_t g = rowBuffer[srcIdx + 1];
            uint8_t r = rowBuffer[srcIdx + 2];

            rgbData[dstIdx + 0] = r;
            rgbData[dstIdx + 1] = g;
            rgbData[dstIdx + 2] = b;
        }
    }

    std::cout << "[INFO] Successfully loaded local BMP texture: " << filepath
              << " (" << width << "x" << height << ", " << infoHeader.biBitCount << "bpp)" << std::endl;
    return true;
}

// [TOPIC: 24-bit BMP Export]
// Purpose: Writes a 54-byte BMP header and DWORD-aligned BGR scanlines from an RGB buffer.
// Game Role: Exports raw 24-bit RGB image buffers to disk in standard Windows BMP format.
bool saveBMP(const std::string& filepath, int width, int height, const std::vector<unsigned char>& rgbData) {
    if (width <= 0 || height <= 0 || rgbData.size() < static_cast<size_t>(width * height * 3)) {
        return false;
    }

    // WHY: Pad each 24-bit scanline to a 4-byte boundary per BMP specification
    int rowPadding = (4 - (width * 3) % 4) % 4;
    int rowStride = width * 3 + rowPadding;
    uint32_t imageSize = static_cast<uint32_t>(rowStride * height);

    BMPFileHeader fileHeader;
    fileHeader.bfSize = sizeof(BMPFileHeader) + sizeof(BMPInfoHeader) + imageSize;
    fileHeader.bfOffBits = sizeof(BMPFileHeader) + sizeof(BMPInfoHeader);

    BMPInfoHeader infoHeader;
    infoHeader.biWidth = width;
    infoHeader.biHeight = height;
    infoHeader.biSizeImage = imageSize;

    std::ofstream file(filepath, std::ios::binary);
    if (!file.is_open()) {
        std::cerr << "[ERROR] Cannot open destination BMP file: " << filepath << std::endl;
        return false;
    }

    file.write(reinterpret_cast<const char*>(&fileHeader), sizeof(fileHeader));
    file.write(reinterpret_cast<const char*>(&infoHeader), sizeof(infoHeader));

    std::vector<uint8_t> rowBuffer(rowStride, 0);

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            int srcIdx = (y * width + x) * 3;
            // WHY: Swap RGB -> BGR because uncompressed 24-bit BMP stores Blue first
            rowBuffer[x * 3 + 0] = rgbData[srcIdx + 2]; // Blue
            rowBuffer[x * 3 + 1] = rgbData[srcIdx + 1]; // Green
            rowBuffer[x * 3 + 2] = rgbData[srcIdx + 0]; // Red
        }
        for (int p = 0; p < rowPadding; ++p) {
            rowBuffer[width * 3 + p] = 0;
        }
        file.write(reinterpret_cast<const char*>(rowBuffer.data()), rowStride);
    }

    return true;
}

} // namespace CGL
