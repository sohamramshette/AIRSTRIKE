// ==============================================================================
// File      : CGL/src/common/font_renderer.cpp
// Subject   : Common | HUD Font Renderer
// Topics    : [TOPIC: HUD Font Renderer] [TOPIC: glRasterPos2f] [TOPIC: gluOrtho2D] [TOPIC: Matrix Stack]
// Game Role : Renders crisp pixel-space GLUT bitmap text and isolates 2D HUD overlays in AIRSTRIKE.
// ==============================================================================

#include "font_renderer.hpp"

namespace CGL {

// [TOPIC: HUD Font Renderer]
// Purpose: Positions the OpenGL raster cursor and emits each character via glutBitmapCharacter.
// Game Role: Prints telemetry readouts, labels, and control prompts on AIRSTRIKE HUDs.
void renderBitmapString(float x, float y, void* font, const std::string& text) {
    // WHY: Bitmap glyphs use the current transformed raster position in screen coordinates
    glRasterPos2f(x, y);
    for (char c : text) {
        glutBitmapCharacter(font, c);
    }
}

// [TOPIC: 2D Orthographic Overlay / glPushMatrix]
// Purpose: Saves PROJECTION and MODELVIEW stacks, sets [0, width]x[0, height] ortho, and disables 3D states.
// Game Role: Lets AIRSTRIKE draw flat HUD bars and text atop 2D or 3D scenes without state leakage.
void beginHUD(int width, int height) {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    // WHY: Map pixel coordinates 1:1 with origin at bottom-left to match OpenGL viewport conventions
    gluOrtho2D(0.0, static_cast<double>(width), 0.0, static_cast<double>(height));

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // WHY: Lighting, depth testing, and texturing would tint or occlude 2D HUD text
    glDisable(GL_LIGHTING);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_TEXTURE_2D);
}

// [TOPIC: Matrix Stack / glPopMatrix]
// Purpose: Pops saved PROJECTION and MODELVIEW matrices and leaves MODELVIEW active.
// Game Role: Restores pre-HUD camera and projection transforms after overlay drawing.
void endHUD() {
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
    glPopMatrix();
}

} // namespace CGL

