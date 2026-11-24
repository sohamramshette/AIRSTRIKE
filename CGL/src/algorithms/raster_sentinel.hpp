// ==============================================================================
// File      : CGL/src/algorithms/raster_sentinel.hpp
// Subject   : CGL-CO3 | RasterSentinel Software-Rasterized Enemy
// Topics    : [TOPIC: Bresenham Integer Line (8 Octants)] [TOPIC: Midpoint Circle (8-Way Symmetry)] [TOPIC: RasterSentinel]
// Game Role : Declares the RasterSentinel enemy whose pulsing shield and laser beam use pure integer rasterizers.
// ==============================================================================

#ifndef RASTER_SENTINEL_HPP
#define RASTER_SENTINEL_HPP

#include "common/gl_common.h"
#include "raster_algorithms.hpp"
#include <vector>

namespace CGL {

// [TOPIC: RasterSentinel / Bresenham Integer Line (8 Octants) / Midpoint Circle (8-Way Symmetry)]
// Purpose: Plots an enemy's force shield and targeting laser as exact integer pixel arrays without floats.
// Game Role: Represents CGL-CO3 enemy type [3] in AIRSTRIKE's Enemy Demonstrator and test suite.
class RasterSentinel {
public:
    RasterSentinel();
    ~RasterSentinel() = default;

    void init();
    void update(float dt);
    void render(int screenW, int screenH);

    // [TOPIC: Midpoint Circle (8-Way Symmetry) / Bresenham Integer Line (8 Octants)]
    // Purpose: Generates integer shield and targeting beam pixel buffers via RasterAlgorithmsDemo.
    // Game Role: Produces the live pixel geometry drawn for RasterSentinel.
    std::vector<Point2D> generateShield(int xc, int yc, int radius) const;
    std::vector<Point2D> generateTargetingBeam(int x0, int y0, int x1, int y1) const;

    // [TOPIC: Rasterization Verification]
    // Purpose: Asserts circle equation error bound, 8-way cardinal symmetry, and Bresenham endpoint accuracy.
    // Game Role: Automated correctness predicates used by CO3 enemy unit tests.
    static bool verifyCircleEquation(const std::vector<Point2D>& points, int xc, int yc, int radius);
    static bool verify8WaySymmetry(int xc, int yc, int radius);
    static bool verifyBresenhamEndpoints(const std::vector<Point2D>& points, int x0, int y0, int x1, int y1);

    int getShieldRadius() const { return shieldRadius; }
    void setShieldRadius(int r) { shieldRadius = (r > 5) ? r : 5; rebuildRasterGeometry(); }

    int getSentinelX() const { return sentinelX; }
    int getSentinelY() const { return sentinelY; }
    int getTargetX() const { return targetX; }
    int getTargetY() const { return targetY; }
    size_t getShieldPixelCount() const { return cachedShieldPixels.size(); }
    size_t getBeamPixelCount() const { return cachedBeamPixels.size(); }

    void setTarget(int tx, int ty) { targetX = tx; targetY = ty; rebuildRasterGeometry(); }
    void setCenter(int cx, int cy) { sentinelX = cx; sentinelY = cy; rebuildRasterGeometry(); }

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
