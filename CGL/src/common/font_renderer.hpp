#ifndef FONT_RENDERER_HPP
#define FONT_RENDERER_HPP

#include "gl_common.h"
#include <string>

namespace CGL {

/**
 * @brief Renders 2D bitmap text at world/screen coordinates (x, y)
 * @param x X coordinate
 * @param y Y coordinate
 * @param font GLUT bitmap font pointer (e.g. GLUT_BITMAP_HELVETICA_12, GLUT_BITMAP_HELVETICA_18)
 * @param text String to display
 */
void renderBitmapString(float x, float y, void* font, const std::string& text);

/**
 * @brief Sets up 2D orthographic HUD overlay projection for text and 2D UI
 * @param width Viewport width in pixels
 * @param height Viewport height in pixels
 */
void beginHUD(int width, int height);

/**
 * @brief Restores projection and modelview matrices after HUD rendering
 */
void endHUD();

} // namespace CGL

#endif // FONT_RENDERER_HPP

