// ==============================================================================
// File      : CGL/src/common/font_renderer.hpp
// Subject   : Common | HUD Font Renderer
// Topics    : [TOPIC: HUD Font Renderer] [TOPIC: 2D Orthographic Overlay] [TOPIC: Matrix Stack]
// Game Role : Declares screen-space 2D HUD projection switches and GLUT bitmap text drawing for AIRSTRIKE.
// ==============================================================================

#ifndef FONT_RENDERER_HPP
#define FONT_RENDERER_HPP

#include "gl_common.h"
#include <string>

namespace CGL {

// [TOPIC: HUD Font Renderer]
// Purpose: Draws a 2D GLUT bitmap string anchored at raster coordinates (x, y).
// Game Role: Displays live telemetry, controls, and CO annotations across all AIRSTRIKE screens.
void renderBitmapString(float x, float y, void* font, const std::string& text);

// [TOPIC: 2D Orthographic Overlay / Matrix Stack]
// Purpose: Pushes current matrices and configures a pixel-aligned 2D orthographic HUD projection.
// Game Role: Isolates 2D HUD overlays from underlying 3D perspective scenes without state corruption.
void beginHUD(int width, int height);

// [TOPIC: Matrix Stack Restoration]
// Purpose: Pops the projection and modelview matrices saved by beginHUD().
// Game Role: Restores scene transforms cleanly after AIRSTRIKE HUD rendering completes.
void endHUD();

} // namespace CGL

#endif // FONT_RENDERER_HPP

