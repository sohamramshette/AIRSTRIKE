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

void RasterSentinel::update(float dt) {
    pulseAngle += dt * 2.0f;
    // Dynamic shield radius breathing: 40px to 52px
    shieldRadius = 45 + static_cast<int>(7.0f * std::sin(pulseAngle));
    rebuildRasterGeometry();
}

std::vector<Point2D> RasterSentinel::generateShield(int xc, int yc, int radius) const {
    // Invoke syllabus-approved Midpoint Circle Algorithm from RasterAlgorithmsDemo
    return RasterAlgorithmsDemo::midpointCircle(xc, yc, radius);
}

std::vector<Point2D> RasterSentinel::generateTargetingBeam(int x0, int y0, int x1, int y1) const {
    // Invoke syllabus-approved Bresenham's Integer Line Algorithm from RasterAlgorithmsDemo
    return RasterAlgorithmsDemo::bresenhamLine(x0, y0, x1, y1);
}

void RasterSentinel::rebuildRasterGeometry() {
    cachedShieldPixels = generateShield(sentinelX, sentinelY, shieldRadius);
    cachedBeamPixels = generateTargetingBeam(sentinelX, sentinelY, targetX, targetY);
}

bool RasterSentinel::verifyCircleEquation(const std::vector<Point2D>& points, int xc, int yc, int radius) {
    if (points.empty()) return false;
    int r2 = radius * radius;
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

bool RasterSentinel::verifyBresenhamEndpoints(const std::vector<Point2D>& points,
                                             int x0, int y0, int x1, int y1) {
    if (points.empty()) return false;
    bool startMatch = (points.front().x == x0 && points.front().y == y0);
    bool endMatch = (points.back().x == x1 && points.back().y == y1);
    return startMatch && endMatch;
}

void RasterSentinel::render(int screenW, int screenH) {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
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

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
}

} // namespace CGL
