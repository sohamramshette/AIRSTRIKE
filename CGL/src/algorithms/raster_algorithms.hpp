// ==============================================================================
// File      : CGL/src/algorithms/raster_algorithms.hpp
// Subject   : CGL-CO3 | 2D Rasterization & 3D Mesh Algorithms
// Topics    : [TOPIC: DDA Line] [TOPIC: Bresenham Integer Line (8 Octants)] [TOPIC: Midpoint Circle (8-Way Symmetry)] [TOPIC: Scanline Polygon Fill]
// Game Role : Declares software rasterizers (DDA, Bresenham, Midpoint, Scanline ET/AET) and 3D mesh modes.
// ==============================================================================

#ifndef RASTER_ALGORITHMS_HPP
#define RASTER_ALGORITHMS_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

// [TOPIC: Scanline Polygon Fill]
// Purpose: Represents an active polygon edge with upper Y bound, current X crossing, and dx/dy step.
// Game Role: Data record stored in Scanline Edge Tables (ET) and Active Edge Tables (AET).
struct Edge {
    int yMax;
    float xCurrent;
    float invSlope; // dx / dy
};

// [TOPIC: DDA Line / Bresenham Integer Line (8 Octants) / Midpoint Circle (8-Way Symmetry) / Scanline Polygon Fill]
// Purpose: Implements pure C++ software rasterization algorithms alongside interactive OpenGL pixel/mesh demos.
// Game Role: Powers the CO3 Algorithm Workbench as well as RasterSentinel and MultiplayerBattleScene beams/shields.
class RasterAlgorithmsDemo {
public:
    RasterAlgorithmsDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // [TOPIC: DDA Line]
    // Purpose: Samples floating-point incremental line pixels over max(|dx|, |dy|) steps.
    // Game Role: Baseline comparison against Bresenham's integer line algorithm.
    static std::vector<Point2D> ddaLine(int x1, int y1, int x2, int y2);

    // [TOPIC: Bresenham Integer Line (8 Octants)]
    // Purpose: Rasterizes integer line pixels across all 8 octants without floating-point arithmetic.
    // Game Role: Drives RasterSentinel targeting beams and multiplayer pulse lasers.
    static std::vector<Point2D> bresenhamLine(int x1, int y1, int x2, int y2);

    // [TOPIC: Midpoint Circle (8-Way Symmetry)]
    // Purpose: Plots one octant using integer decision parameter pk = 1 - r and mirrors across 8 octants.
    // Game Role: Rasterizes energy force shields for RasterSentinel and MultiplayerBattleScene.
    static std::vector<Point2D> midpointCircle(int xc, int yc, int radius);

    // [TOPIC: Midpoint Ellipse]
    // Purpose: Rasterizes an axis-aligned ellipse across Region 1 (dx < dy) and Region 2 using 4-way symmetry.
    // Game Role: Renders elliptical sensor sweeps in the CO3 Curves mode.
    static std::vector<Point2D> midpointEllipse(int xc, int yc, int rx, int ry);

    // [TOPIC: Scanline Polygon Fill]
    // Purpose: Fills closed polygon interiors using Edge Table (ET), Active Edge Table (AET), and parity spans.
    // Game Role: Fills irregular asteroid silhouettes pixel-by-pixel in CO3 Scanline mode.
    static std::vector<Point2D> scanlineFill(const std::vector<Point2D>& vertices);

    // Interactive controls
    void cycleMode();
    void adjustParameter(int delta);
    void toggleGrid();

    int getMode() const { return currentMode; }
    int getParam() const { return paramValue; }

private:
    int currentMode; // 0: Lines (DDA vs Bresenham), 1: Circle & Ellipse, 2: Scanline Fill, 3: 3D Meshes
    int paramValue;  // radius / dimension adjustment
    bool showGrid;
    float animAngle;

    void renderLinesMode(int screenWidth, int screenHeight);
    void renderCurvesMode(int screenWidth, int screenHeight);
    void renderScanlineMode(int screenWidth, int screenHeight);
    void render3DMeshMode(int screenWidth, int screenHeight);

    void drawPixelPoints(const std::vector<Point2D>& points, float r, float g, float b);
};

} // namespace CGL

#endif // RASTER_ALGORITHMS_HPP

