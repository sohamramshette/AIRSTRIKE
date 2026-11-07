#ifndef POLYGON_CRUISER_HPP
#define POLYGON_CRUISER_HPP

#include "common/gl_common.h"
#include <vector>

namespace CGL {

/**
 * @brief Geometric Polygon Cruiser Enemy (CGL-CO1).
 *
 * Demonstrates:
 * - Direct OpenGL Primitive Assembly: GL_POLYGON, GL_TRIANGLE_FAN, GL_LINES, GL_LINE_LOOP, GL_POINTS
 * - Vertex Winding Order: Strict Counter-Clockwise (CCW) front-facing geometry
 * - Normalized Device Coordinates & Screen Space Mapping
 * - Primitive Isolation: Pure geometric rendering without higher-level lighting shaders
 */
class PolygonCruiser {
public:
    PolygonCruiser();
    ~PolygonCruiser() = default;

    // Initialization and rendering
    void init();
    void render(float posX, float posY, float scale = 1.0f);

    // Mathematical and geometry verification queries (Context-independent)
    size_t getHullVertexCount() const { return hullVertices.size(); }
    size_t getWingVertexCount() const { return wingVertices.size(); }
    const std::vector<Vec2>& getHullVertices() const { return hullVertices; }
    const std::vector<Vec2>& getWingVertices() const { return wingVertices; }

    bool verifyCCWWindingOrder() const;
    bool isPointInHull(float px, float py) const;
    float calculateHullArea() const;

private:
    std::vector<Vec2> hullVertices;   // GL_POLYGON (Main fuselage)
    std::vector<Vec2> wingVertices;   // GL_TRIANGLE_FAN (Delta wings)
    std::vector<Vec2> canopyLoop;     // GL_LINE_LOOP (Cockpit canopy)
    std::vector<Vec2> weaponLines;    // GL_LINES (Twin plasma rail mounts)
    std::vector<Vec2> enginePoints;   // GL_POINTS (Thruster exhaust nozzles)

    void setupGeometry();
};

} // namespace CGL

#endif // POLYGON_CRUISER_HPP
