#ifndef RASTER_SENTINEL_HPP
#define RASTER_SENTINEL_HPP

#include "common/gl_common.h"
#include "raster_algorithms.hpp"
#include <vector>

namespace CGL {

/**
 * @brief Bresenham Shield Sentinel Enemy (CGL-CO3).
 *
 * Demonstrates:
 * - Software Rasterization of Force Shield via Midpoint Circle Algorithm (8-way symmetry)
 * - Software Rasterization of Targeting Beam via Bresenham's Integer Line Algorithm (All 8 octants)
 * - Elimination of Floating-point Arithmetic: Pure integer decision parameter updates
 * - Verification of circle equation adherence: |(x - xc)^2 + (y - yc)^2 - r^2| <= 2r + 1
 */
class RasterSentinel {
public:
    RasterSentinel();
    ~RasterSentinel() = default;

    void init();
    void update(float dt);
    void render(int screenW, int screenH);

    // Mathematical Rasterization Generators (Direct link to CO3 algorithms)
    std::vector<Point2D> generateShield(int xc, int yc, int radius) const;
    std::vector<Point2D> generateTargetingBeam(int x0, int y0, int x1, int y1) const;

    // Mathematical Verification Assertions
    static bool verifyCircleEquation(const std::vector<Point2D>& points, int xc, int yc, int radius);
    static bool verify8WaySymmetry(int xc, int yc, int radius);
    static bool verifyBresenhamEndpoints(const std::vector<Point2D>& points, int x0, int y0, int x1, int y1);

    int getShieldRadius() const { return shieldRadius; }
    void setShieldRadius(int r) { shieldRadius = (r > 5) ? r : 5; }

private:
    int sentinelX;
    int sentinelY;
    int targetX;
    int targetY;
    int shieldRadius;
    float pulseAngle;

    std::vector<Point2D> cachedShieldPixels;
    std::vector<Point2D> cachedBeamPixels;

    void rebuildRasterGeometry();
};

} // namespace CGL

#endif // RASTER_SENTINEL_HPP
