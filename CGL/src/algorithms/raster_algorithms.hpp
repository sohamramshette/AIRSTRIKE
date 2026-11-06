#ifndef RASTER_ALGORITHMS_HPP
#define RASTER_ALGORITHMS_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

struct Edge {
    int yMax;
    float xCurrent;
    float invSlope; // dx / dy
};

/**
 * @brief Class demonstrating CGL-CO3: 2D and 3D Algorithms.
 * Implements pure mathematical rasterization:
 * - DDA Line Algorithm
 * - Bresenham's Integer Line Algorithm (All 8 octants)
 * - Midpoint Circle Algorithm (8-way symmetry)
 * - Midpoint Ellipse Algorithm (Region 1 & Region 2)
 * - Scanline Polygon Fill Algorithm (Edge Table + Active Edge Table)
 * - 3D Mesh Generation (Cube & UV Sphere Asteroid)
 */
class RasterAlgorithmsDemo {
public:
    RasterAlgorithmsDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // Algorithm math functions (Pure C++ for direct testing & verification)
    static std::vector<Point2D> ddaLine(int x1, int y1, int x2, int y2);
    static std::vector<Point2D> bresenhamLine(int x1, int y1, int x2, int y2);
    static std::vector<Point2D> midpointCircle(int xc, int yc, int radius);
    static std::vector<Point2D> midpointEllipse(int xc, int yc, int rx, int ry);
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
