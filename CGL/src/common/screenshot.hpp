#ifndef SCREENSHOT_HPP
#define SCREENSHOT_HPP

#include "gl_common.h"
#include <string>

namespace CGL {

/**
 * @brief Captures the current OpenGL back/front buffer and writes it to an uncompressed 24-bit BMP image.
 * @param filepath Full destination path for the BMP file.
 * @param width Viewport width in pixels.
 * @param height Viewport height in pixels.
 * @return true if successfully saved, false otherwise.
 */
bool captureScreenshot(const std::string& filepath, int width, int height);

/**
 * @brief Ensures directory path exists before writing files.
 * @param path Directory or file path.
 */
void ensureDirectoryExists(const std::string& path);

} // namespace CGL

#endif // SCREENSHOT_HPP
