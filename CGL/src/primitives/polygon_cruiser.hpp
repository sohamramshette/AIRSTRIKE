// ==============================================================================
// File      : CGL/src/primitives/polygon_cruiser.hpp
// Subject   : CGL-CO1 | PolygonCruiser Enemy Geometry
// Topics    : [TOPIC: GL_POLYGON] [TOPIC: GL_TRIANGLE_FAN] [TOPIC: GL_LINES] [TOPIC: GL_POINTS] [TOPIC: CCW Winding] [TOPIC: PolygonCruiser]
// Game Role : Declares the PolygonCruiser enemy built from pure OpenGL primitives with CCW winding checks.
// ==============================================================================

#ifndef POLYGON_CRUISER_HPP
#define POLYGON_CRUISER_HPP

#include "common/gl_common.h"
#include <vector>

namespace CGL {

// [TOPIC: PolygonCruiser / GL_POLYGON / GL_TRIANGLE_FAN / CCW Winding]
// Purpose: Assembles a pure-primitive 2D enemy ship and exposes analytical winding/containment tests.
// Game Role: Represents CGL-CO1 enemy type [1] in the AIRSTRIKE Enemy Demonstrator and unit suite.
class PolygonCruiser {
public:
    PolygonCruiser();
    ~PolygonCruiser() = default;

    // [TOPIC: Primitive Assembly]
    // Purpose: Initializes local vertex arrays and renders the cruiser at (posX, posY, scale).
    // Game Role: Draws delta wings, crimson hull, cyan canopy, railguns, and thruster flares.
    void init();
    void render(float posX, float posY, float scale = 1.0f);

    // Geometry inspection accessors
    size_t getHullVertexCount() const { return hullVertices.size(); }
    size_t getWingVertexCount() const { return wingVertices.size(); }
    const std::vector<Vec2>& getHullVertices() const { return hullVertices; }
    const std::vector<Vec2>& getWingVertices() const { return wingVertices; }

    // [TOPIC: CCW Winding / Green-Shoelace Formula]
    // Purpose: Confirms strict Counter-Clockwise vertex order via positive signed polygon area.
    // Game Role: Ensures front-face orientation for the PolygonCruiser hull.
    bool verifyCCWWindingOrder() const;

    // [TOPIC: Ray-Casting Point-in-Polygon]
    // Purpose: Tests whether point (px, py) lies inside the closed fuselage polygon.
    // Game Role: Provides hit-test verification against the PolygonCruiser boundary.
    bool isPointInHull(float px, float py) const;

    // [TOPIC: Shoelace Signed Polygon Area]
    // Purpose: Computes signed 2D hull area via Green's Theorem.
    // Game Role: Quantifies winding direction and hull surface size for tests and HUD telemetry.
    float calculateHullArea() const;

private:
    std::vector<Vec2> hullVertices;   // GL_POLYGON (Main fuselage)
    std::vector<Vec2> wingVertices;   // GL_TRIANGLE_FAN (Delta wings)
    std::vector<Vec2> canopyLoop;     // GL_LINE_LOOP (Cockpit canopy)
    std::vector<Vec2> weaponLines;    // GL_LINES (Twin plasma rail mounts)
    std::vector<Vec2> enginePoints;   // GL_POINTS (Thruster exhaust nozzles)

    // [TOPIC: CCW Winding Vertex Definition]
    // Purpose: Populates local-space vertex buffers for all five primitive groups.
    // Game Role: Defines PolygonCruiser geometry around local origin (0, 0).
    void setupGeometry();
};

} // namespace CGL

#endif // POLYGON_CRUISER_HPP
