// ==============================================================================
// File      : CGL/src/common/screenshot.hpp
// Subject   : Common | OpenGL Screenshot Capture
// Topics    : [TOPIC: glReadPixels] [TOPIC: 24-bit BMP Export] [TOPIC: Framebuffer Readback]
// Game Role : Declares framebuffer-to-BMP capture helpers for automated AIRSTRIKE scene verification.
// ==============================================================================

#ifndef SCREENSHOT_HPP
#define SCREENSHOT_HPP

#include "gl_common.h"
#include <string>

namespace CGL {

// [TOPIC: glReadPixels / Framebuffer Readback]
// Purpose: Reads the rendered OpenGL framebuffer pixels and saves an uncompressed 24-bit BMP image.
// Game Role: Captures all 15 automated AIRSTRIKE demonstration screenshots during --screenshot runs.
bool captureScreenshot(const std::string& filepath, int width, int height);

// [TOPIC: Filesystem Helper]
// Purpose: Recursively creates parent directories along a destination file path.
// Game Role: Ensures docs/screenshots/CGL exists before writing captured BMP snapshots.
void ensureDirectoryExists(const std::string& path);

} // namespace CGL

#endif // SCREENSHOT_HPP

