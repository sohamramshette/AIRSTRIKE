// ==============================================================================
// File      : CGL/src/clipping/clipping_algorithms.hpp
// Subject   : CGL-CO5 | 2D Line & Polygon Clipping
// Topics    : [TOPIC: Cohen-Sutherland 4-bit TBRL Outcodes] [TOPIC: Liang-Barsky] [TOPIC: Sutherland-Hodgman Polygon Clipping]
// Game Role : Declares 2D clipping types and exact Cohen-Sutherland, Liang-Barsky, and Sutherland-Hodgman clippers.
// ==============================================================================

#ifndef CLIPPING_ALGORITHMS_HPP
#define CLIPPING_ALGORITHMS_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

// [TOPIC: Cohen-Sutherland 4-bit TBRL Outcodes]
// Purpose: Bitmask flags encoding Top (8), Bottom (4), Right (2), Left (1) half-plane violations.
// Game Role: Classifies endpoints for trivial accept/reject and boundary intersection in CO5 scenes.
enum OutCode {
    CODE_INSIDE = 0, // 0000
    CODE_LEFT   = 1, // 0001
    CODE_RIGHT  = 2, // 0010
    CODE_BOTTOM = 4, // 0100
    CODE_TOP    = 8  // 1000
};

// [TOPIC: Rectangular Clipping Window]
// Purpose: Defines an axis-aligned 2D clipping viewport [xmin, xmax] x [ymin, ymax].
// Game Role: Bounds laser beams and starfighter polygons inside the combat sector.
struct RectWindow {
    float xmin, ymin, xmax, ymax;
    RectWindow(float _xmin = -0.5f, float _ymin = -0.5f, float _xmax = 0.5f, float _ymax = 0.5f)
        : xmin(_xmin), ymin(_ymin), xmax(_xmax), ymax(_ymax) {}
};

// [TOPIC: 2D Line Segment]
// Purpose: Pairs endpoints p1 and p2 for parametric or outcode clipping.
// Game Role: Represents incoming laser rays before and after boundary clipping.
struct LineSegment {
    Vec2 p1;
    Vec2 p2;
    LineSegment(Vec2 _p1 = Vec2(), Vec2 _p2 = Vec2()) : p1(_p1), p2(_p2) {}
    LineSegment(float x1, float y1, float x2, float y2) : p1(x1, y1), p2(x2, y2) {}
};

// [TOPIC: Cohen-Sutherland 4-bit TBRL Outcodes / Liang-Barsky / Sutherland-Hodgman Polygon Clipping]
// Purpose: Provides pure C++ 2D clippers plus an interactive 3-mode CO5 OpenGL visualization.
// Game Role: Powers BoundarySkimmer, SpaceCombatScene, MultiplayerBattleScene, and CO5 unit tests.
class ClippingAlgorithmsDemo {
public:
    ClippingAlgorithmsDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // [TOPIC: Cohen-Sutherland 4-bit TBRL Outcodes]
    // Purpose: Computes the 4-bit TBRL region code and clips a line segment iteratively to win.
    // Game Role: Truncates space lasers at combat sector borders in real time.
    static int computeOutCode(float x, float y, const RectWindow& win);
    static bool cohenSutherlandClip(const LineSegment& in, const RectWindow& win, LineSegment& out, int& outCode1, int& outCode2);

    // [TOPIC: Liang-Barsky]
    // Purpose: Clips a line segment via parametric inequalities u * p_k <= q_k over [u1, u2] in [0, 1].
    // Game Role: Solves line intersections parametrically without iterative outcode refinement.
    static bool liangBarskyClip(const LineSegment& in, const RectWindow& win, LineSegment& out, float& u1, float& u2);

    // [TOPIC: Sutherland-Hodgman Polygon Clipping]
    // Purpose: Clips a subject polygon sequentially against Left, Right, Bottom, and Top half-planes.
    // Game Role: Trims starfighter silhouettes cleanly against rectangular arena boundaries.
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

