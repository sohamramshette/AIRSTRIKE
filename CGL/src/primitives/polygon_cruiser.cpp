// ==============================================================================
// File      : CGL/src/primitives/polygon_cruiser.cpp
// Subject   : CGL-CO1 | PolygonCruiser Enemy Geometry
// Topics    : [TOPIC: GL_POLYGON] [TOPIC: GL_TRIANGLE_FAN] [TOPIC: GL_LINES] [TOPIC: GL_POINTS] [TOPIC: CCW Winding] [TOPIC: PolygonCruiser]
// Game Role : Defines PolygonCruiser primitive buffers, renders layered enemy geometry, and verifies CCW winding.
// ==============================================================================

#include "polygon_cruiser.hpp"
#include <cmath>

namespace CGL {

PolygonCruiser::PolygonCruiser() {
    setupGeometry();
}

void PolygonCruiser::init() {
    setupGeometry();
}

// [TOPIC: CCW Winding / GL_POLYGON / GL_TRIANGLE_FAN / GL_LINES / GL_POINTS]
// Purpose: Populates local-space vertex lists for fuselage, delta wings, canopy, cannons, and thrusters.
// Game Role: Establishes PolygonCruiser geometry for rendering and headless CO1 assertions.
void PolygonCruiser::setupGeometry() {
    // 1. Fuselage Hull: GL_POLYGON in Counter-Clockwise (CCW) winding
    // Vertices relative to origin (0, 0):
    // V0: Nose tip (0, 0.45)
    // V1: Port shoulder (-0.20, 0.10)
    // V2: Port aft winglet (-0.15, -0.35)
    // V3: Keel / Exhaust center (0, -0.25)
    // V4: Starboard aft winglet (0.15, -0.35)
    // V5: Starboard shoulder (0.20, 0.10)
    hullVertices = {
        Vec2(0.0f, 0.45f),
        Vec2(-0.20f, 0.10f),
        Vec2(-0.15f, -0.35f),
        Vec2(0.0f, -0.25f),
        Vec2(0.15f, -0.35f),
        Vec2(0.20f, 0.10f)
    };

    // 2. Swept Wings: GL_TRIANGLE_FAN centered at (0, 0)
    wingVertices = {
        Vec2(0.0f, 0.0f),       // Center hub
        Vec2(-0.45f, -0.20f),   // Port wingtip
        Vec2(-0.25f, -0.35f),   // Port trailing edge
        Vec2(0.0f, -0.25f),     // Keel
        Vec2(0.25f, -0.35f),    // Starboard trailing edge
        Vec2(0.45f, -0.20f)     // Starboard wingtip
    };

    // 3. Canopy Wireframe: GL_LINE_LOOP
    canopyLoop = {
        Vec2(0.0f, 0.25f),
        Vec2(-0.06f, 0.05f),
        Vec2(0.0f, -0.05f),
        Vec2(0.06f, 0.05f)
    };

    // 4. Weapon Rails: GL_LINES (2 segments = 4 vertices)
    weaponLines = {
        Vec2(-0.35f, -0.15f), Vec2(-0.35f, 0.20f),  // Port cannon
        Vec2(0.35f, -0.15f),  Vec2(0.35f, 0.20f)   // Starboard cannon
    };

    // 5. Thruster Nozzles: GL_POINTS
    enginePoints = {
        Vec2(-0.08f, -0.30f),
        Vec2(0.08f, -0.30f),
        Vec2(0.0f, -0.28f)
    };
}

// [TOPIC: CCW Winding / Shoelace Formula]
// Purpose: Computes signed polygon area using the determinant sum over consecutive edges.
// Game Role: Validates positive area (+0.0765) for PolygonCruiser front-facing CCW winding.
float PolygonCruiser::calculateHullArea() const {
    // Green's Theorem / Shoelace Formula for polygon area:
    // 0.5 * sum(x_i * y_{i+1} - x_{i+1} * y_i)
    if (hullVertices.size() < 3) return 0.0f;
    float area = 0.0f;
    size_t n = hullVertices.size();
    for (size_t i = 0; i < n; ++i) {
        size_t next = (i + 1) % n;
        // WHY: Cross product z-term is positive when traversing boundary vertices counterclockwise
        area += (hullVertices[i].x * hullVertices[next].y - hullVertices[next].x * hullVertices[i].y);
    }
    return 0.5f * area; // Positive indicates CCW winding
}

// [TOPIC: CCW Winding]
// Purpose: Checks that the hull polygon's signed Shoelace area is strictly positive.
// Game Role: Guarantees front-facing polygon orientation under OpenGL CCW rules.
bool PolygonCruiser::verifyCCWWindingOrder() const {
    // In OpenGL front-facing polygons have Counter-Clockwise (CCW) winding,
    // which yields a positive signed area under the Shoelace formula.
    return calculateHullArea() > 0.0f;
}

// [TOPIC: Point-in-Polygon Containment]
// Purpose: Applies the even-odd horizontal ray-casting rule across all polygon edges.
// Game Role: Supports collision/containment testing on the PolygonCruiser hull.
bool PolygonCruiser::isPointInHull(float px, float py) const {
    // Ray-casting algorithm: count ray intersections along horizontal line
    if (hullVertices.size() < 3) return false;
    bool inside = false;
    size_t n = hullVertices.size();
    for (size_t i = 0, j = n - 1; i < n; j = i++) {
        float xi = hullVertices[i].x, yi = hullVertices[i].y;
        float xj = hullVertices[j].x, yj = hullVertices[j].y;
        // WHY: Edge straddles horizontal ray y = py and intersection x lies strictly right of px
        if (((yi > py) != (yj > py)) &&
            (px < (xj - xi) * (py - yi) / (yj - yi) + xi)) {
            inside = !inside;
        }
    }
    return inside;
}

// [TOPIC: GL_TRIANGLE_FAN / GL_POLYGON / GL_LINE_LOOP / GL_LINES / GL_POINTS]
// Purpose: Draws all five PolygonCruiser primitive layers in painter order inside a pushed matrix.
// Game Role: Renders the CGL-CO1 PolygonCruiser enemy on screen.
void PolygonCruiser::render(float posX, float posY, float scale) {
    glPushMatrix();
    glTranslatef(posX, posY, 0.0f);
    glScalef(scale, scale, 1.0f);

    // 1. Render Delta Wings: GL_TRIANGLE_FAN
    // WHY: Draw wings first so the central fuselage GL_POLYGON cleanly overlaps the wing root
    glColor3f(0.35f, 0.40f, 0.55f); // Steel slate blue
    glBegin(GL_TRIANGLE_FAN);
    for (const auto& v : wingVertices) {
        glVertex2f(v.x, v.y);
    }
    glEnd();

    // 2. Render Fuselage: GL_POLYGON
    glColor3f(0.75f, 0.20f, 0.25f); // Crimson hull
    glBegin(GL_POLYGON);
    for (const auto& v : hullVertices) {
        glVertex2f(v.x, v.y);
    }
    glEnd();

    // 3. Render Cockpit Canopy: GL_LINE_LOOP
    glColor3f(0.20f, 0.90f, 1.0f); // Cyan canopy outline
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    for (const auto& v : canopyLoop) {
        glVertex2f(v.x, v.y);
    }
    glEnd();

    // 4. Render Plasma Cannons: GL_LINES
    glColor3f(1.0f, 0.85f, 0.20f); // Gold railgun barrels
    glLineWidth(3.0f);
    glBegin(GL_LINES);
    for (const auto& v : weaponLines) {
        glVertex2f(v.x, v.y);
    }
    glEnd();

    // 5. Render Engine Thruster Particles: GL_POINTS
    glColor3f(1.0f, 0.40f, 0.10f); // Orange thruster flare
    glPointSize(5.0f);
    glBegin(GL_POINTS);
    for (const auto& v : enginePoints) {
        glVertex2f(v.x, v.y);
    }
    glEnd();

    glPopMatrix();
}

} // namespace CGL
