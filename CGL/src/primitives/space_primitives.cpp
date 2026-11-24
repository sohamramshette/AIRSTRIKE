// ==============================================================================
// File      : CGL/src/primitives/space_primitives.cpp
// Subject   : CGL-CO1 | OpenGL Graphics Primitives
// Topics    : [TOPIC: GL_POINTS] [TOPIC: GL_LINES] [TOPIC: GL_TRIANGLE_FAN] [TOPIC: CCW Winding] [TOPIC: Gouraud color interpolation]
// Game Role : Renders the 8-quadrant OpenGL primitive showcase (stars, lasers, paths, crafts, shields, blasts, panels).
// ==============================================================================

#include "space_primitives.hpp"
#include "common/font_renderer.hpp"

namespace CGL {

SpacePrimitivesDemo::SpacePrimitivesDemo()
    : wireframeMode(false), animTimer(0.0f) {}

// [TOPIC: Gouraud color interpolation / Alpha Blending]
// Purpose: Configures GL_BLEND and GL_SMOOTH vertex color shading in the OpenGL state machine.
// Game Role: Ensures smooth gradients across primitive beams, shields, and spacecraft hulls.
void SpacePrimitivesDemo::init() {
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    // WHY: GL_SMOOTH enables Gouraud color interpolation across vertices inside each primitive
    glShadeModel(GL_SMOOTH);
}

// [TOPIC: glPolygonMode]
// Purpose: Toggles solid fill vs wireframe polygon rendering state.
// Game Role: Allows interactive mesh inspection in the CO1 showcase.
void SpacePrimitivesDemo::toggleWireframe() {
    wireframeMode = !wireframeMode;
}

// [TOPIC: GL_POINTS]
// Purpose: Plots discrete 2D particle vertices around concentric polar rings.
// Game Role: Renders the starfield particle cluster in Quadrant 1.
void SpacePrimitivesDemo::drawPoints(float cx, float cy, float size) {
    // 1. GL_POINTS: Starfield cluster and sparkling particles
    glPointSize(3.0f);
    glBegin(GL_POINTS);
    for (int i = 0; i < 24; ++i) {
        float angle = i * 2.0f * static_cast<float>(M_PI) / 24.0f;
        float r = (i % 3 + 1) * (size * 0.25f);
        float px = cx + std::cos(angle) * r;
        float py = cy + std::sin(angle) * r;
        // Vertex color interpolation per particle
        if (i % 3 == 0) glColor4f(1.0f, 0.9f, 0.4f, 1.0f); // Gold star
        else if (i % 3 == 1) glColor4f(0.4f, 0.8f, 1.0f, 0.9f); // Cyan star
        else glColor4f(1.0f, 1.0f, 1.0f, 0.8f); // White star
        glVertex2f(px, py);
    }
    // High-energy center point
    glColor4f(1.0f, 0.3f, 0.2f, 1.0f);
    glVertex2f(cx, cy);
    glEnd();
    // WHY: Restore default point size so subsequent OpenGL draws are unaffected
    glPointSize(1.0f);
}

// [TOPIC: GL_LINES / Gouraud color interpolation]
// Purpose: Connects independent vertex pairs with linear per-vertex color gradients.
// Game Role: Renders twin laser beams and targeting crosshairs in Quadrant 2.
void SpacePrimitivesDemo::drawLines(float cx, float cy, float size) {
    // 2. GL_LINES: Twin laser beams and reticle crosshairs
    glLineWidth(2.5f);
    glBegin(GL_LINES);
    // Left laser beam with gradient
    glColor4f(1.0f, 0.1f, 0.1f, 1.0f);
    glVertex2f(cx - size * 0.5f, cy - size * 0.6f);
    // WHY: Varying endpoint colors triggers Gouraud color interpolation along the rasterized line
    glColor4f(1.0f, 0.8f, 0.2f, 1.0f);
    glVertex2f(cx - size * 0.5f, cy + size * 0.6f);

    // Right laser beam with gradient
    glColor4f(1.0f, 0.1f, 0.1f, 1.0f);
    glVertex2f(cx + size * 0.5f, cy - size * 0.6f);
    glColor4f(1.0f, 0.8f, 0.2f, 1.0f);
    glVertex2f(cx + size * 0.5f, cy + size * 0.6f);

    // Crosshair lines
    glColor4f(0.2f, 1.0f, 0.4f, 0.8f);
    glVertex2f(cx - size * 0.3f, cy);
    glVertex2f(cx + size * 0.3f, cy);
    glVertex2f(cx, cy - size * 0.3f);
    glVertex2f(cx, cy + size * 0.3f);
    glEnd();
    glLineWidth(1.0f);
}

// [TOPIC: GL_LINE_STRIP]
// Purpose: Draws an open polyline connecting sequential sampled points along a sine wave.
// Game Role: Renders a continuous missile flight trajectory in Quadrant 3.
void SpacePrimitivesDemo::drawLineStrip(float cx, float cy, float size) {
    // 3. GL_LINE_STRIP: Continuous flight trajectory / missile curve
    glLineWidth(2.0f);
    glBegin(GL_LINE_STRIP);
    for (int i = 0; i <= 20; ++i) {
        float t = i / 20.0f;
        float px = cx - size * 0.7f + t * (size * 1.4f);
        float py = cy + std::sin(t * 3.14159f * 2.0f) * (size * 0.45f);
        glColor4f(0.1f + t * 0.9f, 0.4f + (1.0f - t) * 0.5f, 1.0f - t * 0.7f, 1.0f);
        glVertex2f(px, py);
    }
    glEnd();
    glLineWidth(1.0f);
}

// [TOPIC: GL_LINE_LOOP / CCW Winding]
// Purpose: Automatically closes the final vertex back to the first vertex in CCW order.
// Game Role: Renders a jagged closed asteroid wireframe boundary in Quadrant 4.
void SpacePrimitivesDemo::drawLineLoop(float cx, float cy, float size) {
    // 4. GL_LINE_LOOP: Closed asteroid bounding polygon wireframe
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    float radii[] = {0.7f, 0.85f, 0.6f, 0.9f, 0.65f, 0.8f, 0.55f, 0.75f};
    int count = sizeof(radii) / sizeof(radii[0]);
    for (int i = 0; i < count; ++i) {
        float angle = i * 2.0f * static_cast<float>(M_PI) / count;
        float r = size * radii[i];
        float px = cx + std::cos(angle) * r;
        float py = cy + std::sin(angle) * r;
        glColor4f(0.8f, 0.8f, 0.2f + 0.1f * i, 1.0f);
        glVertex2f(px, py);
    }
    glEnd();
    glLineWidth(1.0f);
}

// [TOPIC: GL_TRIANGLES / CCW Winding / Gouraud color interpolation]
// Purpose: Assembles three independent triangles with CCW winding and shaded hull gradients.
// Game Role: Renders the starfighter fuselage and delta wings in Quadrant 5.
void SpacePrimitivesDemo::drawTriangles(float cx, float cy, float size) {
    // 5. GL_TRIANGLES: Space fighter spacecraft hull & delta wings
    glBegin(GL_TRIANGLES);
    // Fuselage / Center Hull
    // WHY: Vertices are ordered Top -> Bottom-Left -> Bottom-Right for standard CCW Winding
    glColor4f(0.2f, 0.6f, 1.0f, 1.0f); // Cockpit top cyan
    glVertex2f(cx, cy + size * 0.8f);
    glColor4f(0.05f, 0.15f, 0.4f, 1.0f); // Left rear dark blue
    glVertex2f(cx - size * 0.35f, cy - size * 0.6f);
    glColor4f(0.05f, 0.15f, 0.4f, 1.0f); // Right rear dark blue
    glVertex2f(cx + size * 0.35f, cy - size * 0.6f);

    // Left Wing
    glColor4f(0.1f, 0.4f, 0.8f, 1.0f);
    glVertex2f(cx - size * 0.2f, cy);
    glColor4f(0.0f, 0.2f, 0.5f, 1.0f);
    glVertex2f(cx - size * 0.8f, cy - size * 0.5f);
    glColor4f(0.1f, 0.5f, 0.9f, 1.0f);
    glVertex2f(cx - size * 0.2f, cy - size * 0.5f);

    // Right Wing
    glColor4f(0.1f, 0.4f, 0.8f, 1.0f);
    glVertex2f(cx + size * 0.2f, cy);
    glColor4f(0.1f, 0.5f, 0.9f, 1.0f);
    glVertex2f(cx + size * 0.2f, cy - size * 0.5f);
    glColor4f(0.0f, 0.2f, 0.5f, 1.0f);
    glVertex2f(cx + size * 0.8f, cy - size * 0.5f);
    glEnd();
}

// [TOPIC: GL_TRIANGLE_STRIP / Gouraud color interpolation]
// Purpose: Generates a connected arc mesh reusing consecutive vertices between inner/outer radii.
// Game Role: Renders the translucent energy deflector shield ribbon in Quadrant 6.
void SpacePrimitivesDemo::drawTriangleStrip(float cx, float cy, float size) {
    // 6. GL_TRIANGLE_STRIP: Deflector shield barrier / energy ribbon
    glBegin(GL_TRIANGLE_STRIP);
    int segments = 10;
    for (int i = 0; i <= segments; ++i) {
        float t = static_cast<float>(i) / segments;
        float angle = -static_cast<float>(M_PI) * 0.5f + t * static_cast<float>(M_PI);
        float rInner = size * 0.45f;
        float rOuter = size * 0.75f;

        // Inner vertex (bright core)
        // WHY: Alternating inner/outer vertices forms a seamless radial strip with Gouraud alpha fade
        glColor4f(0.3f, 1.0f, 0.8f, 0.85f);
        glVertex2f(cx + std::cos(angle) * rInner, cy + std::sin(angle) * rInner);

        // Outer vertex (faded aura)
        glColor4f(0.1f, 0.3f, 0.9f, 0.4f);
        glVertex2f(cx + std::cos(angle) * rOuter, cy + std::sin(angle) * rOuter);
    }
    glEnd();
}

// [TOPIC: GL_TRIANGLE_FAN / CCW Winding]
// Purpose: Shares one central apex vertex across all radial slices swept counterclockwise.
// Game Role: Renders the circular radial plasma blast wave in Quadrant 7.
void SpacePrimitivesDemo::drawTriangleFan(float cx, float cy, float size) {
    // 7. GL_TRIANGLE_FAN: Circular energy blast / alien scout saucer
    glBegin(GL_TRIANGLE_FAN);
    // Center vertex (hot white/yellow plasma)
    glColor4f(1.0f, 1.0f, 0.8f, 1.0f);
    glVertex2f(cx, cy);

    int slices = 16;
    // WHY: i <= slices closes the fan back at angle 0 while incrementing counterclockwise
    for (int i = 0; i <= slices; ++i) {
        float angle = i * 2.0f * static_cast<float>(M_PI) / slices;
        float r = size * 0.7f;
        // Outer perimeter (deep magenta / plasma edge)
        glColor4f(0.9f, 0.1f, 0.4f, 0.7f);
        glVertex2f(cx + std::cos(angle) * r, cy + std::sin(angle) * r);
    }
    glEnd();
}

// [TOPIC: GL_QUADS / Gouraud color interpolation]
// Purpose: Assembles four independent 4-vertex panels with per-corner color gradients.
// Game Role: Renders the Space Station solar panel array and mounting spar in Quadrant 8.
void SpacePrimitivesDemo::drawQuads(float cx, float cy, float size) {
    // 8. GL_QUADS: Space Station solar panel array with metallic seams
    glBegin(GL_QUADS);
    // Top-Left Panel
    glColor4f(0.1f, 0.25f, 0.6f, 1.0f);
    glVertex2f(cx - size * 0.7f, cy + size * 0.6f);
    glColor4f(0.15f, 0.35f, 0.8f, 1.0f);
    glVertex2f(cx - size * 0.1f, cy + size * 0.6f);
    glColor4f(0.05f, 0.15f, 0.4f, 1.0f);
    glVertex2f(cx - size * 0.1f, cy + size * 0.1f);
    glColor4f(0.08f, 0.2f, 0.5f, 1.0f);
    glVertex2f(cx - size * 0.7f, cy + size * 0.1f);

    // Top-Right Panel
    glColor4f(0.15f, 0.35f, 0.8f, 1.0f);
    glVertex2f(cx + size * 0.1f, cy + size * 0.6f);
    glColor4f(0.1f, 0.25f, 0.6f, 1.0f);
    glVertex2f(cx + size * 0.7f, cy + size * 0.6f);
    glColor4f(0.08f, 0.2f, 0.5f, 1.0f);
    glVertex2f(cx + size * 0.7f, cy + size * 0.1f);
    glColor4f(0.05f, 0.15f, 0.4f, 1.0f);
    glVertex2f(cx + size * 0.1f, cy + size * 0.1f);

    // Bottom-Left Panel
    glColor4f(0.08f, 0.2f, 0.5f, 1.0f);
    glVertex2f(cx - size * 0.7f, cy - size * 0.1f);
    glColor4f(0.05f, 0.15f, 0.4f, 1.0f);
    glVertex2f(cx - size * 0.1f, cy - size * 0.1f);
    glColor4f(0.1f, 0.25f, 0.6f, 1.0f);
    glVertex2f(cx - size * 0.1f, cy - size * 0.6f);
    glColor4f(0.15f, 0.35f, 0.8f, 1.0f);
    glVertex2f(cx - size * 0.7f, cy - size * 0.6f);

    // Bottom-Right Panel
    glColor4f(0.05f, 0.15f, 0.4f, 1.0f);
    glVertex2f(cx + size * 0.1f, cy - size * 0.1f);
    glColor4f(0.08f, 0.2f, 0.5f, 1.0f);
    glVertex2f(cx + size * 0.7f, cy - size * 0.1f);
    glColor4f(0.15f, 0.35f, 0.8f, 1.0f);
    glVertex2f(cx + size * 0.7f, cy - size * 0.6f);
    glColor4f(0.1f, 0.25f, 0.6f, 1.0f);
    glVertex2f(cx + size * 0.1f, cy - size * 0.6f);
    glEnd();

    // Central mounting spar
    glLineWidth(2.0f);
    glColor4f(0.8f, 0.8f, 0.9f, 1.0f);
    glBegin(GL_LINES);
    glVertex2f(cx - size * 0.8f, cy);
    glVertex2f(cx + size * 0.8f, cy);
    glEnd();
    glLineWidth(1.0f);
}

// [TOPIC: OpenGL Graphics Primitives / HUD Font Renderer]
// Purpose: Clears buffers, draws the 8 primitive quadrants in NDC [-1, 1], and overlays HUD labels.
// Game Role: Orchestrates the CO1 OpenGL Graphics Primitives screen in AIRSTRIKE.
void SpacePrimitivesDemo::render(int screenWidth, int screenHeight) {
    animTimer += 0.016f;

    glClearColor(0.03f, 0.04f, 0.08f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Apply polygon mode based on wireframe toggle
    if (wireframeMode) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    } else {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    // Set 2D orthographic coordinate system [-1.0, 1.0]
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Draw background boundary grid for the 8 primitive quadrants
    glColor4f(0.15f, 0.18f, 0.25f, 0.5f);
    glLineWidth(1.0f);
    glBegin(GL_LINES);
    // Vertical dividers (4 columns)
    glVertex2f(-0.5f, 0.85f); glVertex2f(-0.5f, -0.85f);
    glVertex2f( 0.0f, 0.85f); glVertex2f( 0.0f, -0.85f);
    glVertex2f( 0.5f, 0.85f); glVertex2f( 0.5f, -0.85f);
    // Horizontal divider (2 rows)
    glVertex2f(-0.95f, 0.0f); glVertex2f(0.95f, 0.0f);
    glEnd();

    // Define 8 cell centers: 4 columns x 2 rows
    float colX[4] = {-0.75f, -0.25f, 0.25f, 0.75f};
    float rowY[2] = {0.42f, -0.42f};
    float cellSize = 0.18f;

    // Row 0: Points, Lines, Line Strip, Line Loop
    drawPoints(colX[0], rowY[0], cellSize);
    drawLines(colX[1], rowY[0], cellSize);
    drawLineStrip(colX[2], rowY[0], cellSize);
    drawLineLoop(colX[3], rowY[0], cellSize);

    // Row 1: Triangles, Triangle Strip, Triangle Fan, Quads
    drawTriangles(colX[0], rowY[1], cellSize);
    drawTriangleStrip(colX[1], rowY[1], cellSize);
    drawTriangleFan(colX[2], rowY[1], cellSize);
    drawQuads(colX[3], rowY[1], cellSize);

    // Switch to HUD mode to render raster labels
    // WHY: Restore GL_FILL before HUD rendering so text quads/bitmaps are never left in wireframe mode
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Ensure text is always filled
    beginHUD(screenWidth, screenHeight);

    // Header Title
    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_18,
                       "AIRSTRIKE CGL-CO1: OpenGL Graphics Primitives Showcase");

    glColor3f(0.7f, 0.75f, 0.85f);
    renderBitmapString(20.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12,
                       "Client-Server State Machine | Double Buffering | CCW Vertex Winding | Gouraud Interpolation");

    std::string wfText = wireframeMode ? "Mode: WIREFRAME [W to toggle]" : "Mode: SOLID FILL [W to toggle]";
    glColor3f(1.0f, 0.85f, 0.2f);
    renderBitmapString(screenWidth - 260.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_12, wfText);

    // Cell Labels
    struct CellLabel {
        float normX, normY;
        const char* name;
        const char* sub;
    };
    CellLabel labels[8] = {
        {-0.75f, 0.12f, "1. GL_POINTS", "Starfield Particles"},
        {-0.25f, 0.12f, "2. GL_LINES", "Lasers & Reticle"},
        { 0.25f, 0.12f, "3. GL_LINE_STRIP", "Flight Trajectory"},
        { 0.75f, 0.12f, "4. GL_LINE_LOOP", "Asteroid Wireframe"},
        {-0.75f, -0.72f, "5. GL_TRIANGLES", "Fighter Spacecraft"},
        {-0.25f, -0.72f, "6. GL_TRIANGLE_STRIP", "Shield Barrier Mesh"},
        { 0.25f, -0.72f, "7. GL_TRIANGLE_FAN", "Radial Blast Wave"},
        { 0.75f, -0.72f, "8. GL_QUADS", "Station Solar Array"}
    };

    for (int i = 0; i < 8; ++i) {
        // WHY: Map normalized [-1, 1] cell coordinates to pixel HUD coordinates
        float px = (labels[i].normX + 1.0f) * 0.5f * screenWidth - 65.0f;
        float py = (labels[i].normY + 1.0f) * 0.5f * screenHeight;

        glColor3f(1.0f, 1.0f, 1.0f);
        renderBitmapString(px, py, GLUT_BITMAP_HELVETICA_12, labels[i].name);
        glColor3f(0.5f, 0.8f, 0.6f);
        renderBitmapString(px, py - 16.0f, GLUT_BITMAP_HELVETICA_10, labels[i].sub);
    }

    // Bottom Navigation Help
    glColor3f(0.6f, 0.6f, 0.7f);
    renderBitmapString(20.0f, 20.0f, GLUT_BITMAP_HELVETICA_12,
                       "[1-6] Switch COs | [0] Integrated Scene | [W] Toggle Wireframe | [ESC] Exit");

    endHUD();
}

} // namespace CGL

