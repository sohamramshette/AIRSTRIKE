#include "boundary_skimmer.hpp"
#include <cmath>

namespace CGL {

BoundarySkimmer::BoundarySkimmer()
    : arenaWindow(-0.6f, -0.6f, 0.6f, 0.6f), skimmerPos(-0.7f, 0.5f),
      skimSpeed(0.5f), animTimer(0.0f) {
    init();
}

void BoundarySkimmer::init() {
    arenaWindow = RectWindow(-0.6f, -0.6f, 0.6f, 0.6f);
    skimmerPos = Vec2(-0.7f, 0.5f);

    // Triangular wedge hull crossing boundary
    rawHullVertices = {
        Vec2(-0.15f, 0.15f),
        Vec2(-0.15f, -0.15f),
        Vec2(0.25f, 0.0f)
    };

    targetingSensor = LineSegment(Vec2(-0.8f, 0.2f), Vec2(0.3f, 0.2f));
}

void BoundarySkimmer::update(float dt) {
    animTimer += dt;
    // Skim along perimeter of arena window
    skimmerPos.x = 0.65f * std::cos(animTimer * 0.8f);
    skimmerPos.y = 0.65f * std::sin(animTimer * 0.8f);

    // Sensor sweeps towards screen center
    targetingSensor = LineSegment(skimmerPos, Vec2(0.0f, 0.0f));
}

bool BoundarySkimmer::clipLaserSight(const LineSegment& in, const RectWindow& win,
                                     LineSegment& out, int& outCode1, int& outCode2) const {
    return ClippingAlgorithmsDemo::cohenSutherlandClip(in, win, out, outCode1, outCode2);
}

std::vector<Vec2> BoundarySkimmer::clipHullPolygon(const std::vector<Vec2>& subjectPoly,
                                                   const RectWindow& win) const {
    return ClippingAlgorithmsDemo::sutherlandHodgmanClip(subjectPoly, win);
}

bool BoundarySkimmer::testTrivialAccept(const RectWindow& win) {
    // Line completely inside: (-0.2, -0.2) to (0.2, 0.2)
    LineSegment in(Vec2(-0.2f, -0.2f), Vec2(0.2f, 0.2f));
    LineSegment out;
    int c1 = 0, c2 = 0;
    bool visible = ClippingAlgorithmsDemo::cohenSutherlandClip(in, win, out, c1, c2);
    return visible && (c1 == 0) && (c2 == 0) && (in.p1 == out.p1) && (in.p2 == out.p2);
}

bool BoundarySkimmer::testTrivialReject(const RectWindow& win) {
    // Line completely outside to the left: (-0.9, -0.2) to (-0.7, 0.4)
    LineSegment in(Vec2(-0.9f, -0.2f), Vec2(-0.7f, 0.4f));
    LineSegment out;
    int c1 = 0, c2 = 0;
    bool visible = ClippingAlgorithmsDemo::cohenSutherlandClip(in, win, out, c1, c2);
    // Both points outside left: c1 & c2 == CODE_LEFT (1) != 0
    return (!visible) && ((c1 & c2) != 0);
}

bool BoundarySkimmer::testSingleBoundaryCross(const RectWindow& win) {
    // Line crosses left boundary (x = -0.6): from (-0.8, 0.0) to (0.0, 0.0)
    LineSegment in(Vec2(-0.8f, 0.0f), Vec2(0.0f, 0.0f));
    LineSegment out;
    int c1 = 0, c2 = 0;
    bool visible = ClippingAlgorithmsDemo::cohenSutherlandClip(in, win, out, c1, c2);
    // Clipped point out.p1.x must be exactly win.xmin (-0.6)
    return visible && std::fabs(out.p1.x - win.xmin) < 0.005f && std::fabs(out.p2.x - 0.0f) < 0.005f;
}

bool BoundarySkimmer::testMultipleBoundariesCross(const RectWindow& win) {
    // Line crosses left boundary (x = -0.6) and right boundary (x = +0.6): (-0.8, 0.0) to (+0.8, 0.0)
    LineSegment in(Vec2(-0.8f, 0.0f), Vec2(0.8f, 0.0f));
    LineSegment out;
    int c1 = 0, c2 = 0;
    bool visible = ClippingAlgorithmsDemo::cohenSutherlandClip(in, win, out, c1, c2);
    return visible && std::fabs(out.p1.x - win.xmin) < 0.005f && std::fabs(out.p2.x - win.xmax) < 0.005f;
}

bool BoundarySkimmer::testPolygonClipping(const RectWindow& win) {
    // Convex polygon overlapping top boundary of window (ymin=-0.6, ymax=+0.6, xmin=-0.6, xmax=+0.6)
    // Square: (-0.3, 0.3) to (0.3, 0.9)
    std::vector<Vec2> subject = {
        Vec2(-0.3f, 0.3f),
        Vec2(0.3f, 0.3f),
        Vec2(0.3f, 0.9f),
        Vec2(-0.3f, 0.9f)
    };
    std::vector<Vec2> clipped = ClippingAlgorithmsDemo::sutherlandHodgmanClip(subject, win);
    // Clipped against ymax = 0.6 produces 4 vertices with top vertices at y = 0.6
    if (clipped.size() != 4) return false;
    float maxClippedY = -1.0f;
    for (const auto& v : clipped) {
        if (v.y > maxClippedY) maxClippedY = v.y;
    }
    return std::fabs(maxClippedY - win.ymax) < 0.005f;
}

void BoundarySkimmer::render(int screenW, int screenH) {
    (void)screenW;
    (void)screenH;

    // 1. Draw Viewport Clipping Window
    glColor3f(0.8f, 0.8f, 0.2f); // Yellow boundary frame
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    glVertex2f(arenaWindow.xmin, arenaWindow.ymin);
    glVertex2f(arenaWindow.xmax, arenaWindow.ymin);
    glVertex2f(arenaWindow.xmax, arenaWindow.ymax);
    glVertex2f(arenaWindow.xmin, arenaWindow.ymax);
    glEnd();

    // 2. Clip and Render Targeting Sensor Beam
    LineSegment clippedBeam;
    int c1 = 0, c2 = 0;
    bool beamVisible = clipLaserSight(targetingSensor, arenaWindow, clippedBeam, c1, c2);

    // Draw unclipped segment in faint red
    glColor4f(0.8f, 0.2f, 0.2f, 0.3f);
    glLineWidth(1.0f);
    glBegin(GL_LINES);
    glVertex2f(targetingSensor.p1.x, targetingSensor.p1.y);
    glVertex2f(targetingSensor.p2.x, targetingSensor.p2.y);
    glEnd();

    // Draw clipped visible beam segment in bright green
    if (beamVisible) {
        glColor3f(0.2f, 1.0f, 0.3f);
        glLineWidth(3.0f);
        glBegin(GL_LINES);
        glVertex2f(clippedBeam.p1.x, clippedBeam.p1.y);
        glVertex2f(clippedBeam.p2.x, clippedBeam.p2.y);
        glEnd();
    }

    // 3. Render Skimmer Hull
    glPushMatrix();
    glTranslatef(skimmerPos.x, skimmerPos.y, 0.0f);
    glColor3f(1.0f, 0.5f, 0.1f);
    glBegin(GL_POLYGON);
    for (const auto& v : rawHullVertices) {
        glVertex2f(v.x, v.y);
    }
    glEnd();
    glPopMatrix();
}

} // namespace CGL
