#include "screenshot.hpp"
#include <fstream>
#include <vector>
#include <cstdint>
#include <sys/stat.h>

#ifdef _WIN32
#include <direct.h>
#endif

namespace CGL {

void ensureDirectoryExists(const std::string& path) {
    std::string currentPath = "";
    for (size_t i = 0; i < path.length(); ++i) {
        char c = path[i];
        if (c == '/' || c == '\\') {
            if (!currentPath.empty()) {
#ifdef _WIN32
                _mkdir(currentPath.c_str());
#else
                mkdir(currentPath.c_str(), 0755);
#endif
            }
        }
        currentPath += c;
    }
}

#pragma pack(push, 1)
struct BMPHeader {
    uint16_t bfType{0x4D42}; // 'BM'
    uint32_t bfSize{0};
    uint16_t bfReserved1{0};
    uint16_t bfReserved2{0};
    uint32_t bfOffBits{54};

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

bool captureScreenshot(const std::string& filepath, int width, int height) {
    if (width <= 0 || height <= 0) return false;

    ensureDirectoryExists(filepath);

    int rowPadding = (4 - (width * 3) % 4) % 4;
    int rowStride = width * 3 + rowPadding;
    uint32_t imageSize = static_cast<uint32_t>(rowStride * height);

    BMPHeader header;
    header.bfSize = 54 + imageSize;
    header.biWidth = width;
    header.biHeight = height;
    header.biSizeImage = imageSize;

    std::vector<uint8_t> rgbBuffer(width * height * 3);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);
    glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, rgbBuffer.data());

    std::ofstream out(filepath, std::ios::binary);
    if (!out.is_open()) {
        std::cerr << "[ERROR] Failed to open file for screenshot: " << filepath << std::endl;
        return false;
    }

    out.write(reinterpret_cast<const char*>(&header), sizeof(header));

    std::vector<uint8_t> rowBuffer(rowStride, 0);
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            int srcIndex = (y * width + x) * 3;
            // BMP expects BGR order
            rowBuffer[x * 3 + 0] = rgbBuffer[srcIndex + 2]; // Blue
            rowBuffer[x * 3 + 1] = rgbBuffer[srcIndex + 1]; // Green
            rowBuffer[x * 3 + 2] = rgbBuffer[srcIndex + 0]; // Red
        }
        for (int p = 0; p < rowPadding; ++p) {
            rowBuffer[width * 3 + p] = 0;
        }
        out.write(reinterpret_cast<const char*>(rowBuffer.data()), rowStride);
    }

    out.close();
    std::cout << "[INFO] Screenshot captured: " << filepath << " (" << width << "x" << height << ")" << std::endl;
    return true;
}

} // namespace CGL

