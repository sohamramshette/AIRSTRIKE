// ==============================================================================
// File      : CGL/src/algorithms/raster_sentinel.cpp
// Subject   : CGL-CO3 | RasterSentinel Software-Rasterized Enemy
// Topics    : [TOPIC: Bresenham Integer Line (8 Octants)] [TOPIC: Midpoint Circle (8-Way Symmetry)] [TOPIC: RasterSentinel]
// Game Role : Generates, verifies, and plots RasterSentinel's pixel shield ring and targeting laser in screen space.
// ==============================================================================

#include "raster_sentinel.hpp"
#include <cmath>
#include <algorithm>

namespace CGL {

RasterSentinel::RasterSentinel()
    : sentinelX(200), sentinelY(200), targetX(400), targetY(300),
      shieldRadius(45), pulseAngle(0.0f) {
    init();
}

void RasterSentinel::init() {
    rebuildRasterGeometry();
}

// [TOPIC: RasterSentinel Animation]
// Purpose: Modulates shield radius in [38, 52] px and refreshes integer pixel buffers.
// Game Role: Creates a breathing Midpoint Circle energy barrier around the Sentinel.
void RasterSentinel::update(float dt) {
    pulseAngle += dt * 2.0f;
    // Dynamic shield radius breathing: 40px to 52px
    shieldRadius = 45 + static_cast<int>(7.0f * std::sin(pulseAngle));
    rebuildRasterGeometry();
}

// [TOPIC: Midpoint Circle (8-Way Symmetry)]
// Purpose: Delegates shield contour generation to RasterAlgorithmsDemo::midpointCircle.
// Game Role: Produces integer pixel points for RasterSentinel's cyan force field.
std::vector<Point2D> RasterSentinel::generateShield(int xc, int yc, int radius) const {
    // Invoke syllabus-approved Midpoint Circle Algorithm from RasterAlgorithmsDemo
    return RasterAlgorithmsDemo::midpointCircle(xc, yc, radius);
}

// [TOPIC: Bresenham Integer Line (8 Octants)]
// Purpose: Delegates targeting beam rasterization to RasterAlgorithmsDemo::bresenhamLine.
// Game Role: Produces integer pixel points from Sentinel center to target crosshair.
std::vector<Point2D> RasterSentinel::generateTargetingBeam(int x0, int y0, int x1, int y1) const {
    // Invoke syllabus-approved Bresenham's Integer Line Algorithm from RasterAlgorithmsDemo
    return RasterAlgorithmsDemo::bresenhamLine(x0, y0, x1, y1);
}

void RasterSentinel::rebuildRasterGeometry() {
    cachedShieldPixels = generateShield(sentinelX, sentinelY, shieldRadius);
    cachedBeamPixels = generateTargetingBeam(sentinelX, sentinelY, targetX, targetY);
}

// [TOPIC: Midpoint Circle (8-Way Symmetry)]
// Purpose: Checks every plotted pixel satisfies |(x-xc)^2 + (y-yc)^2 - r^2| <= 2r + 1.
// Game Role: Validates integer discretization accuracy for CO3 RasterSentinel tests.
bool RasterSentinel::verifyCircleEquation(const std::vector<Point2D>& points, int xc, int yc, int radius) {
    if (points.empty()) return false;
    int r2 = radius * radius;
    // WHY: Rounding to nearest integer pixel grid bounds squared-radius error by 2r + 1
    int maxError = 2 * radius + 1;

    for (const auto& pt : points) {
        int dx = pt.x - xc;
        int dy = pt.y - yc;
        int dist2 = dx * dx + dy * dy;
        int err = std::abs(dist2 - r2);
        if (err > maxError) {
            return false;
        }
    }
    return true;
}

// [TOPIC: Midpoint Circle (8-Way Symmetry)]
// Purpose: Verifies presence of all four cardinal axis intercepts produced by 8-way octant mirroring.
// Game Role: Ensures symmetric coverage across top, bottom, left, and right octant boundaries.
bool RasterSentinel::verify8WaySymmetry(int xc, int yc, int radius) {
    std::vector<Point2D> circlePts = RasterAlgorithmsDemo::midpointCircle(xc, yc, radius);
    if (circlePts.empty()) return false;

    // A valid midpoint circle at radius > 0 contains points in all 8 octants
    // Check for the 4 cardinal extreme intercepts: (xc + r, yc), (xc - r, yc), (xc, yc + r), (xc, yc - r)
    bool hasRight = false, hasLeft = false, hasTop = false, hasBottom = false;

    for (const auto& pt : circlePts) {
        if (pt.x == xc + radius && pt.y == yc) hasRight = true;
        if (pt.x == xc - radius && pt.y == yc) hasLeft = true;
        if (pt.x == xc && pt.y == yc + radius) hasTop = true;
        if (pt.x == xc && pt.y == yc - radius) hasBottom = true;
    }

    return (hasRight && hasLeft && hasTop && hasBottom);
}

// [TOPIC: Bresenham Integer Line (8 Octants)]
// Purpose: Asserts start and end pixels match exact integer coordinates (x0, y0) and (x1, y1).
// Game Role: Verifies endpoint fidelity for RasterSentinel targeting beam tests.
bool RasterSentinel::verifyBresenhamEndpoints(const std::vector<Point2D>& points,
                                             int x0, int y0, int x1, int y1) {
    if (points.empty()) return false;
    bool startMatch = (points.front().x == x0 && points.front().y == y0);
    bool endMatch = (points.back().x == x1 && points.back().y == y1);
    return startMatch && endMatch;
}

// [TOPIC: RasterSentinel Screen-Space Plotting]
// Purpose: Sets 1:1 pixel orthographic projection and plots core, Bresenham laser, and Midpoint shield points.
// Game Role: Renders CGL-CO3 RasterSentinel enemy inside EnemyDemoScene.
void RasterSentinel::render(int screenW, int screenH) {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    // WHY: Map 1:1 to pixel coordinates so cached integer pixels plot without sub-pixel distortion
    gluOrtho2D(0, screenW, 0, screenH);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // 1. Render Sentinel Core (Software scanline/diamond)
    glColor3f(0.85f, 0.20f, 0.85f); // Neon Magenta Sentinel Core
    glPointSize(4.0f);
    glBegin(GL_POINTS);
    for (int dy = -10; dy <= 10; ++dy) {
        for (int dx = -10 + std::abs(dy); dx <= 10 - std::abs(dy); ++dx) {
            glVertex2i(sentinelX + dx, sentinelY + dy);
        }
    }
    glEnd();

    // 2. Render Targeting Laser Beam (Software-rasterized Bresenham pixels)
    glColor3f(1.0f, 0.20f, 0.20f); // Bright Red Targeting Laser
    glPointSize(2.0f);
    glBegin(GL_POINTS);
    for (const auto& pt : cachedBeamPixels) {
        glVertex2i(pt.x, pt.y);
    }
    glEnd();

    // 3. Render Midpoint Circular Force Shield (Software-rasterized circle pixels)
    glColor3f(0.20f, 0.85f, 1.0f); // Cyan Force Field
    glPointSize(3.0f);
    glBegin(GL_POINTS);
    for (const auto& pt : cachedShieldPixels) {
        glVertex2i(pt.x, pt.y);
    }
    glEnd();

    // WHY: Pop modelview and projection matrices in reverse push order to leave caller state intact
    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
}

} // namespace CGL
