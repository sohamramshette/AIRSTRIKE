#ifndef SPACE_PRIMITIVES_HPP
#define SPACE_PRIMITIVES_HPP

#include "common/gl_common.h"
#include <string>

namespace CGL {

/**
 * @brief Class demonstrating CGL-CO1: OpenGL Graphics Primitives.
 * Showcases GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP,
 * GL_TRIANGLES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, and GL_QUADS.
 */
class SpacePrimitivesDemo {
public:
    SpacePrimitivesDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void toggleWireframe();
    bool isWireframe() const { return wireframeMode; }

    // Primitive renderers
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
