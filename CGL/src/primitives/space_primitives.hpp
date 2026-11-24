// ==============================================================================
// File      : CGL/src/primitives/space_primitives.hpp
// Subject   : CGL-CO1 | OpenGL Graphics Primitives
// Topics    : [TOPIC: GL_POINTS] [TOPIC: GL_LINES] [TOPIC: GL_TRIANGLE_FAN] [TOPIC: CCW Winding] [TOPIC: Gouraud color interpolation]
// Game Role : Declares the CO1 8-quadrant OpenGL primitive showcase using AIRSTRIKE space assets.
// ==============================================================================

#ifndef SPACE_PRIMITIVES_HPP
#define SPACE_PRIMITIVES_HPP

#include "common/gl_common.h"
#include <string>

namespace CGL {

// [TOPIC: OpenGL Graphics Primitives / State Machine]
// Purpose: Showcases GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP, GL_TRIANGLES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, and GL_QUADS.
// Game Role: Interactive gallery demonstrating primitive assembly, CCW winding, wireframe toggle, and Gouraud color interpolation.
class SpacePrimitivesDemo {
public:
    SpacePrimitivesDemo();

    // [TOPIC: OpenGL Blend & Shade State]
    // Purpose: Enables alpha blending and GL_SMOOTH Gouraud vertex color interpolation.
    // Game Role: Initializes CO1 rendering state before primitive draws.
    void init();

    // [TOPIC: Primitive Showcase Scene]
    // Purpose: Draws the 4x2 grid of primitive cells and screen-space HUD labels.
    // Game Role: Main CO1 interactive visualization screen in AIRSTRIKE.
    void render(int screenWidth, int screenHeight);

    // [TOPIC: glPolygonMode Wireframe Toggle]
    // Purpose: Switches polygon rasterization between GL_LINE wireframe and GL_FILL solid.
    // Game Role: Lets students inspect triangle/quad tessellation via [W].
    void toggleWireframe();
    bool isWireframe() const { return wireframeMode; }

    // [TOPIC: GL_POINTS / GL_LINES / GL_TRIANGLE_FAN / CCW Winding]
    // Purpose: Individual CO1 primitive renderers positioned at normalized cell centers (cx, cy).
    // Game Role: Renders stars, twin lasers, trajectories, asteroids, ships, shields, blasts, and solar panels.
    void drawPoints(float cx, float cy, float size);
    void drawLines(float cx, float cy, float size);
    void drawLineStrip(float cx, float cy, float size);
    void drawLineLoop(float cx, float cy, float size);
    void drawTriangles(float cx, float cy, float size);
    void drawTriangleStrip(float cx, float cy, float size);
    void drawTriangleFan(float cx, float cy, float size);
    void drawQuads(float cx, float cy, float size);

private:
    bool wireframeMode;
    float animTimer;
};

} // namespace CGL

#endif // SPACE_PRIMITIVES_HPP

