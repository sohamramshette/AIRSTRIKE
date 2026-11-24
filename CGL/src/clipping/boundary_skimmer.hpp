// ==============================================================================
// File      : CGL/src/clipping/boundary_skimmer.hpp
// Subject   : CGL-CO5 | BoundarySkimmer Perimeter Enemy
// Topics    : [TOPIC: Cohen-Sutherland 4-bit TBRL Outcodes] [TOPIC: Sutherland-Hodgman Polygon Clipping] [TOPIC: BoundarySkimmer]
// Game Role : Declares the BoundarySkimmer enemy that skims the arena border to verify line/polygon clipping.
// ==============================================================================

#ifndef BOUNDARY_SKIMMER_HPP
#define BOUNDARY_SKIMMER_HPP

#include "common/gl_common.h"
#include "clipping_algorithms.hpp"
#include <vector>

namespace CGL {

// [TOPIC: BoundarySkimmer / Cohen-Sutherland 4-bit TBRL Outcodes / Sutherland-Hodgman Polygon Clipping]
// Purpose: Patrols the viewport border while clipping its laser sight and hull polygon against arenaWindow.
// Game Role: Represents CGL-CO5 enemy type [5] in AIRSTRIKE's Enemy Demonstrator and CO5 test suite.
class BoundarySkimmer {
public:
    BoundarySkimmer();
    ~BoundarySkimmer() = default;

    void init();
    void update(float dt);
    void render(int screenW, int screenH);

    // [TOPIC: Cohen-Sutherland 4-bit TBRL Outcodes / Sutherland-Hodgman Polygon Clipping]
    // Purpose: Clips the targeting ray and hull polygon against rectangular window win.
    // Game Role: Links BoundarySkimmer sensor geometry directly to CO5 clipping algorithms.
    bool clipLaserSight(const LineSegment& in, const RectWindow& win,
                        LineSegment& out, int& outCode1, int& outCode2) const;

    std::vector<Vec2> clipHullPolygon(const std::vector<Vec2>& subjectPoly,
                                      const RectWindow& win) const;

    // [TOPIC: Clipping Case Verification]
    // Purpose: Validates trivial accept, trivial reject, single/multi-edge line clipping, and polygon clipping.
    // Game Role: Headless test predicates invoked by the CO5 BoundarySkimmer test suite.
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
