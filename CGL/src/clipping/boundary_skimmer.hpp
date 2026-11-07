#ifndef BOUNDARY_SKIMMER_HPP
#define BOUNDARY_SKIMMER_HPP

#include "common/gl_common.h"
#include "clipping_algorithms.hpp"
#include <vector>

namespace CGL {

/**
 * @brief Boundary Phase Skimmer Enemy (CGL-CO5).
 *
 * Demonstrates:
 * - 2D Point and Line Clipping via Cohen-Sutherland Algorithm
 * - 4-bit Region Outcodes (Top, Bottom, Right, Left)
 * - Trivial Acceptance (code1 | code2 == 0) and Trivial Rejection (code1 & code2 != 0)
 * - Sutherland-Hodgman Polygon Clipping pipeline against 4 boundary edges
 */
class BoundarySkimmer {
public:
    BoundarySkimmer();
    ~BoundarySkimmer() = default;

    void init();
    void update(float dt);
    void render(int screenW, int screenH);

    // Mathematical Clipping Algorithms (Pure C++ for automated unit testing)
    bool clipLaserSight(const LineSegment& in, const RectWindow& win,
                        LineSegment& out, int& outCode1, int& outCode2) const;

    std::vector<Vec2> clipHullPolygon(const std::vector<Vec2>& subjectPoly,
                                      const RectWindow& win) const;

    // Test Scenarios
    static bool testTrivialAccept(const RectWindow& win);
    static bool testTrivialReject(const RectWindow& win);
    static bool testSingleBoundaryCross(const RectWindow& win);
    static bool testMultipleBoundariesCross(const RectWindow& win);
    static bool testPolygonClipping(const RectWindow& win);

    RectWindow getViewportWindow() const { return arenaWindow; }
    void setWindowSize(float size) { arenaWindow = RectWindow(-size, -size, size, size); }

    Vec2 getSkimmerPos() const { return skimmerPos; }
    void setSkimmerPos(float x, float y) {
        skimmerPos.x = x;
        skimmerPos.y = y;
        targetingSensor = LineSegment(skimmerPos, Vec2(0.0f, 0.0f));
    }

    LineSegment getTargetingSensor() const { return targetingSensor; }
    const std::vector<Vec2>& getRawHullVertices() const { return rawHullVertices; }

private:
    RectWindow arenaWindow;
    Vec2 skimmerPos;
    float skimSpeed;
    float animTimer;

    std::vector<Vec2> rawHullVertices;
    LineSegment targetingSensor;
};

} // namespace CGL

#endif // BOUNDARY_SKIMMER_HPP
