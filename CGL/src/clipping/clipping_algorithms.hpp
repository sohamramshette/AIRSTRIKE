#ifndef CLIPPING_ALGORITHMS_HPP
#define CLIPPING_ALGORITHMS_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

// 4-bit Cohen-Sutherland Region Codes
enum OutCode {
    CODE_INSIDE = 0, // 0000
    CODE_LEFT   = 1, // 0001
    CODE_RIGHT  = 2, // 0010
    CODE_BOTTOM = 4, // 0100
    CODE_TOP    = 8  // 1000
};

struct RectWindow {
    float xmin, ymin, xmax, ymax;
    RectWindow(float _xmin = -0.5f, float _ymin = -0.5f, float _xmax = 0.5f, float _ymax = 0.5f)
        : xmin(_xmin), ymin(_ymin), xmax(_xmax), ymax(_ymax) {}
};

struct LineSegment {
    Vec2 p1;
    Vec2 p2;
    LineSegment(Vec2 _p1 = Vec2(), Vec2 _p2 = Vec2()) : p1(_p1), p2(_p2) {}
    LineSegment(float x1, float y1, float x2, float y2) : p1(x1, y1), p2(x2, y2) {}
};

/**
 * @brief Class demonstrating CGL-CO5: Clipping Algorithms.
 * Implements:
 * - Cohen-Sutherland Line Clipping (4-bit TBRL Outcodes)
 * - Liang-Barsky Parametric Line Clipping (u1, u2 inequalities)
 * - Sutherland-Hodgman Polygon Clipping (4-boundary pipeline)
 */
class ClippingAlgorithmsDemo {
public:
    ClippingAlgorithmsDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // Algorithm math functions (Pure C++ for direct testability)
    static int computeOutCode(float x, float y, const RectWindow& win);
    static bool cohenSutherlandClip(const LineSegment& in, const RectWindow& win, LineSegment& out, int& outCode1, int& outCode2);
    static bool liangBarskyClip(const LineSegment& in, const RectWindow& win, LineSegment& out, float& u1, float& u2);
    static std::vector<Vec2> sutherlandHodgmanClip(const std::vector<Vec2>& poly, const RectWindow& win);

    // Interactive controls
    void cycleMode();
    void moveClipWindow(float dx, float dy);
    void resizeClipWindow(float delta);
    void toggleAnimate();
    void reset();

    int getMode() const { return currentMode; }
    RectWindow getWindow() const { return clipWin; }

private:
    int currentMode; // 0: Cohen-Sutherland, 1: Liang-Barsky, 2: Sutherland-Hodgman
    RectWindow clipWin;
    bool animate;
    float animTime;

    void renderCohenSutherland(int screenWidth, int screenHeight);
    void renderLiangBarsky(int screenWidth, int screenHeight);
    void renderSutherlandHodgman(int screenWidth, int screenHeight);

    void drawClipWindow();
};

} // namespace CGL

#endif // CLIPPING_ALGORITHMS_HPP

