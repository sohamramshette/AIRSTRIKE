#ifndef IMAGE_LOADER_HPP
#define IMAGE_LOADER_HPP

#include <string>
#include <vector>

namespace CGL {

/**
 * @brief Robust loader for uncompressed 24-bit and 32-bit BMP image files.
 * Zero external dependencies. Converts BGR(A) to RGB for standard OpenGL upload.
 * 
 * @param filepath Path to the image file.
 * @param width Output parameter: image width in pixels.
 * @param height Output parameter: image height in pixels.
 * @param rgbData Output parameter: raw RGB byte buffer (width * height * 3).
 * @return true if successfully loaded, false on missing or invalid file.
 */
bool loadBMP(const std::string& filepath, int& width, int& height, std::vector<unsigned char>& rgbData);

/**
 * @brief Saves an RGB byte buffer as an uncompressed 24-bit Windows Bitmap (.bmp) file.
 * 
 * @param filepath Destination path.
 * @param width Image width in pixels.
 * @param height Image height in pixels.
 * @param rgbData RGB byte buffer (width * height * 3).
 * @return true if successfully saved, false otherwise.
 */
bool saveBMP(const std::string& filepath, int width, int height, const std::vector<unsigned char>& rgbData);

} // namespace CGL

#endif // IMAGE_LOADER_HPP
