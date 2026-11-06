#include "raster_algorithms.hpp"
#include "common/font_renderer.hpp"
#include <algorithm>
#include <cmath>
#include <sstream>

namespace CGL {

RasterAlgorithmsDemo::RasterAlgorithmsDemo()
    : currentMode(0), paramValue(40), showGrid(false), animAngle(0.0f) {}

void RasterAlgorithmsDemo::init() {
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

void RasterAlgorithmsDemo::update(float dt) {
    animAngle += 45.0f * dt;
    if (animAngle >= 360.0f) animAngle -= 360.0f;
}

void RasterAlgorithmsDemo::cycleMode() {
    currentMode = (currentMode + 1) % 4;
}

void RasterAlgorithmsDemo::adjustParameter(int delta) {
    paramValue += delta;
    if (paramValue < 10) paramValue = 10;
    if (paramValue > 150) paramValue = 150;
}

void RasterAlgorithmsDemo::toggleGrid() {
    showGrid = !showGrid;
}

// -------------------------------------------------------------
// Pure Mathematical Algorithm Implementations
// -------------------------------------------------------------

std::vector<Point2D> RasterAlgorithmsDemo::ddaLine(int x1, int y1, int x2, int y2) {
    std::vector<Point2D> points;
    int dx = x2 - x1;
    int dy = y2 - y1;
    int steps = std::max(std::abs(dx), std::abs(dy));

    if (steps == 0) {
        points.emplace_back(x1, y1);
        return points;
    }

    float xInc = static_cast<float>(dx) / static_cast<float>(steps);
    float yInc = static_cast<float>(dy) / static_cast<float>(steps);

    float x = static_cast<float>(x1);
    float y = static_cast<float>(y1);

    for (int i = 0; i <= steps; ++i) {
        points.emplace_back(static_cast<int>(std::round(x)), static_cast<int>(std::round(y)));
        x += xInc;
        y += yInc;
    }

    return points;
}

std::vector<Point2D> RasterAlgorithmsDemo::bresenhamLine(int x1, int y1, int x2, int y2) {
    std::vector<Point2D> points;
    int dx = std::abs(x2 - x1);
    int dy = std::abs(y2 - y1);
    int sx = (x1 < x2) ? 1 : -1;
    int sy = (y1 < y2) ? 1 : -1;

    int x = x1;
    int y = y1;

    if (dx >= dy) {
        // Octants with |m| <= 1
        int err = 2 * dy - dx;
        while (true) {
            points.emplace_back(x, y);
            if (x == x2 && y == y2) break;
            if (err >= 0) {
                y += sy;
                err -= 2 * dx;
            }
            err += 2 * dy;
            x += sx;
        }
    } else {
        // Octants with |m| > 1
        int err = 2 * dx - dy;
        while (true) {
            points.emplace_back(x, y);
            if (x == x2 && y == y2) break;
            if (err >= 0) {
                x += sx;
                err -= 2 * dy;
            }
            err += 2 * dx;
            y += sy;
        }
    }

    return points;
}

std::vector<Point2D> RasterAlgorithmsDemo::midpointCircle(int xc, int yc, int radius) {
    std::vector<Point2D> points;
    if (radius <= 0) {
        points.emplace_back(xc, yc);
        return points;
    }

    int x = 0;
    int y = radius;
    int p = 1 - radius;

    auto add8Points = [&](int cx, int cy, int px, int py) {
        points.emplace_back(cx + px, cy + py);
        points.emplace_back(cx - px, cy + py);
        points.emplace_back(cx + px, cy - py);
        points.emplace_back(cx - px, cy - py);
        points.emplace_back(cx + py, cy + px);
        points.emplace_back(cx - py, cy + px);
        points.emplace_back(cx + py, cy - px);
        points.emplace_back(cx - py, cy - px);
    };

    while (x <= y) {
        add8Points(xc, yc, x, y);
        x++;
        if (p < 0) {
            p += 2 * x + 1;
        } else {
            y--;
            p += 2 * x - 2 * y + 1;
        }
    }

    return points;
}

std::vector<Point2D> RasterAlgorithmsDemo::midpointEllipse(int xc, int yc, int rx, int ry) {
    std::vector<Point2D> points;
    if (rx <= 0 || ry <= 0) return points;

    long long rx2 = static_cast<long long>(rx) * rx;
    long long ry2 = static_cast<long long>(ry) * ry;

    auto add4Points = [&](int cx, int cy, int px, int py) {
        points.emplace_back(cx + px, cy + py);
        points.emplace_back(cx - px, cy + py);
        points.emplace_back(cx + px, cy - py);
        points.emplace_back(cx - px, cy - py);
    };

    // Region 1: dy/dx >= -1
    int x = 0;
    int y = ry;
    double p1 = ry2 - rx2 * ry + 0.25 * rx2;
    long long dx = 2 * ry2 * x;
    long long dy = 2 * rx2 * y;

    while (dx < dy) {
        add4Points(xc, yc, x, y);
        x++;
        dx += 2 * ry2;
        if (p1 < 0) {
            p1 += dx + ry2;
        } else {
            y--;
            dy -= 2 * rx2;
            p1 += dx - dy + ry2;
        }
    }

    // Region 2: dy/dx < -1
    double p2 = ry2 * (x + 0.5) * (x + 0.5) + rx2 * (y - 1.0) * (y - 1.0) - rx2 * ry2;
    while (y >= 0) {
        add4Points(xc, yc, x, y);
        y--;
        dy -= 2 * rx2;
        if (p2 > 0) {
            p2 += rx2 - dy;
        } else {
            x++;
            dx += 2 * ry2;
            p2 += dx - dy + rx2;
        }
    }

    return points;
}

std::vector<Point2D> RasterAlgorithmsDemo::scanlineFill(const std::vector<Point2D>& vertices) {
    std::vector<Point2D> filledPixels;
    if (vertices.size() < 3) return filledPixels;

    int yMin = vertices[0].y;
    int yMax = vertices[0].y;
    for (const auto& v : vertices) {
        if (v.y < yMin) yMin = v.y;
        if (v.y > yMax) yMax = v.y;
    }

    struct ScanEdge {
        int yUpper;
        float xIntersect;
        float invM;
    };

    std::vector<std::vector<ScanEdge>> edgeTable(yMax - yMin + 1);

    size_t numVerts = vertices.size();
    for (size_t i = 0; i < numVerts; ++i) {
        Point2D p1 = vertices[i];
        Point2D p2 = vertices[(i + 1) % numVerts];

        if (p1.y == p2.y) continue; // Ignore horizontal edges

        Point2D lower = (p1.y < p2.y) ? p1 : p2;
        Point2D upper = (p1.y < p2.y) ? p2 : p1;

        float invM = static_cast<float>(upper.x - lower.x) / static_cast<float>(upper.y - lower.y);
        ScanEdge edge{upper.y, static_cast<float>(lower.x), invM};
        edgeTable[lower.y - yMin].push_back(edge);
    }

    std::vector<ScanEdge> activeEdgeList;

    for (int y = yMin; y <= yMax; ++y) {
        // Add new edges from edge table bucket
        for (const auto& e : edgeTable[y - yMin]) {
            activeEdgeList.push_back(e);
        }

        // Remove edges that end at y
        activeEdgeList.erase(
            std::remove_if(activeEdgeList.begin(), activeEdgeList.end(),
                           [y](const ScanEdge& e) { return e.yUpper <= y; }),
            activeEdgeList.end());

        // Sort active edges by current X intersection
        std::sort(activeEdgeList.begin(), activeEdgeList.end(),
                  [](const ScanEdge& a, const ScanEdge& b) { return a.xIntersect < b.xIntersect; });

        // Fill horizontal spans between parity pairs
        for (size_t k = 0; k + 1 < activeEdgeList.size(); k += 2) {
            int xStart = static_cast<int>(std::ceil(activeEdgeList[k].xIntersect));
            int xEnd = static_cast<int>(std::floor(activeEdgeList[k + 1].xIntersect));
            for (int x = xStart; x <= xEnd; ++x) {
                filledPixels.emplace_back(x, y);
            }
        }

        // Advance X intersections
        for (auto& e : activeEdgeList) {
            e.xIntersect += e.invM;
        }
    }

    return filledPixels;
}

// -------------------------------------------------------------
// Screen Visualizations
// -------------------------------------------------------------

void RasterAlgorithmsDemo::drawPixelPoints(const std::vector<Point2D>& points, float r, float g, float b) {
    glColor3f(r, g, b);
    glBegin(GL_POINTS);
    for (const auto& pt : points) {
        glVertex2i(pt.x, pt.y);
    }
    glEnd();
}

void RasterAlgorithmsDemo::renderLinesMode(int screenWidth, int screenHeight) {
    // Left: DDA Line Drawing, Right: Bresenham's Integer Line Drawing
    int halfW = screenWidth / 2;

    // Divider line
    glColor3f(0.3f, 0.35f, 0.45f);
    glLineWidth(1.0f);
    glBegin(GL_LINES);
    glVertex2i(halfW, 40);
    glVertex2i(halfW, screenHeight - 60);
    glEnd();

    int cx1 = halfW / 2;
    int cy = screenHeight / 2;
    int cx2 = halfW + halfW / 2;

    glPointSize(2.0f);

    // Multi-octant fan lines
    int numRays = 16;
    int radius = paramValue * 2;
    int ddaTotal = 0, bresTotal = 0;

    for (int i = 0; i < numRays; ++i) {
        float angle = i * 2.0f * static_cast<float>(M_PI) / numRays;
        int targetX1 = cx1 + static_cast<int>(std::cos(angle) * radius);
        int targetY1 = cy + static_cast<int>(std::sin(angle) * radius);
        int targetX2 = cx2 + static_cast<int>(std::cos(angle) * radius);
        int targetY2 = cy + static_cast<int>(std::sin(angle) * radius);

        auto ddaPts = ddaLine(cx1, cy, targetX1, targetY1);
        auto bresPts = bresenhamLine(cx2, cy, targetX2, targetY2);

        ddaTotal += static_cast<int>(ddaPts.size());
        bresTotal += static_cast<int>(bresPts.size());

        drawPixelPoints(ddaPts, 0.2f, 0.9f, 1.0f); // DDA (Cyan)
        drawPixelPoints(bresPts, 1.0f, 0.6f, 0.2f); // Bresenham (Orange)
    }

    glPointSize(1.0f);

    // HUD Mode Labels
    beginHUD(screenWidth, screenHeight);
    glColor3f(0.2f, 0.9f, 1.0f);
    renderBitmapString(cx1 - 80.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "DDA Line Algorithm");
    glColor3f(0.8f, 0.8f, 0.9f);
    std::stringstream s1;
    s1 << "Floating-Point Incremental | " << ddaTotal << " pixels plotted";
    renderBitmapString(cx1 - 110.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, s1.str());

    glColor3f(1.0f, 0.6f, 0.2f);
    renderBitmapString(cx2 - 100.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Bresenham's Algorithm");
    glColor3f(0.8f, 0.8f, 0.9f);
    std::stringstream s2;
    s2 << "Integer Decision Param (pk) | " << bresTotal << " pixels plotted";
    renderBitmapString(cx2 - 110.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, s2.str());
    endHUD();
}

void RasterAlgorithmsDemo::renderCurvesMode(int screenWidth, int screenHeight) {
    int halfW = screenWidth / 2;
    int cx1 = halfW / 2;
    int cy = screenHeight / 2;
    int cx2 = halfW + halfW / 2;

    int radius = paramValue * 2;
    int rx = static_cast<int>(paramValue * 2.2f);
    int ry = static_cast<int>(paramValue * 1.3f);

    auto circlePts = midpointCircle(cx1, cy, radius);
    auto ellipsePts = midpointEllipse(cx2, cy, rx, ry);

    glPointSize(2.0f);
    drawPixelPoints(circlePts, 0.3f, 1.0f, 0.5f);  // Circle (Green)
    drawPixelPoints(ellipsePts, 1.0f, 0.3f, 0.8f); // Ellipse (Pink)
    glPointSize(1.0f);

    // Center markers
    glColor3f(1.0f, 1.0f, 1.0f);
    glPointSize(5.0f);
    glBegin(GL_POINTS);
    glVertex2i(cx1, cy);
    glVertex2i(cx2, cy);
    glEnd();
    glPointSize(1.0f);

    beginHUD(screenWidth, screenHeight);
    glColor3f(0.3f, 1.0f, 0.5f);
    renderBitmapString(cx1 - 100.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Midpoint Circle");
    std::stringstream sc;
    sc << "8-Way Symmetry | r = " << radius << " | " << circlePts.size() << " px";
    glColor3f(0.8f, 0.8f, 0.9f);
    renderBitmapString(cx1 - 100.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, sc.str());

    glColor3f(1.0f, 0.3f, 0.8f);
    renderBitmapString(cx2 - 100.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Midpoint Ellipse");
    std::stringstream se;
    se << "4-Way Symmetry | rx = " << rx << ", ry = " << ry << " | " << ellipsePts.size() << " px";
    glColor3f(0.8f, 0.8f, 0.9f);
    renderBitmapString(cx2 - 100.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, se.str());
    endHUD();
}

void RasterAlgorithmsDemo::renderScanlineMode(int screenWidth, int screenHeight) {
    int cx = screenWidth / 2;
    int cy = screenHeight / 2;

    // Build an irregular asteroid polygon
    float scale = paramValue * 2.5f;
    std::vector<Point2D> polyVerts = {
        Point2D(cx, static_cast<int>(cy + scale * 0.9f)),
        Point2D(static_cast<int>(cx + scale * 0.7f), static_cast<int>(cy + scale * 0.5f)),
        Point2D(static_cast<int>(cx + scale * 0.9f), static_cast<int>(cy - scale * 0.2f)),
        Point2D(static_cast<int>(cx + scale * 0.4f), static_cast<int>(cy - scale * 0.8f)),
        Point2D(static_cast<int>(cx - scale * 0.3f), static_cast<int>(cy - scale * 0.6f)),
        Point2D(static_cast<int>(cx - scale * 0.8f), static_cast<int>(cy - scale * 0.1f)),
        Point2D(static_cast<int>(cx - scale * 0.5f), static_cast<int>(cy + scale * 0.6f))
    };

    auto filledPixels = scanlineFill(polyVerts);

    // Draw filled interior pixels
    glPointSize(1.5f);
    drawPixelPoints(filledPixels, 0.25f, 0.5f, 0.8f);
    glPointSize(1.0f);

    // Draw outline wireframe
    glColor3f(1.0f, 0.9f, 0.3f);
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    for (const auto& v : polyVerts) {
        glVertex2i(v.x, v.y);
    }
    glEnd();
    glLineWidth(1.0f);

    beginHUD(screenWidth, screenHeight);
    glColor3f(1.0f, 0.9f, 0.3f);
    renderBitmapString(cx - 140.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Scanline Polygon Fill Algorithm");
    std::stringstream ss;
    ss << "Edge Table (ET) + Active Edge Table (AET) | " << filledPixels.size() << " filled pixels";
    glColor3f(0.8f, 0.85f, 0.95f);
    renderBitmapString(cx - 150.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, ss.str());
    endHUD();
}

void RasterAlgorithmsDemo::render3DMeshMode(int screenWidth, int screenHeight) {
    // 3D Wireframe Cube & UV Sphere Asteroid
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(45.0, static_cast<double>(screenWidth) / screenHeight, 0.1, 100.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    gluLookAt(0.0, 2.5, 6.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

    glEnable(GL_DEPTH_TEST);

    // 1. 3D Space Station Cube (Left)
    glPushMatrix();
    glTranslatef(-1.8f, 0.0f, 0.0f);
    glRotatef(animAngle, 1.0f, 1.0f, 0.5f);

    glColor3f(0.2f, 0.8f, 1.0f);
    glutWireCube(1.6);
    // Draw vertices
    glColor3f(1.0f, 0.9f, 0.2f);
    glPointSize(6.0f);
    glBegin(GL_POINTS);
    for (int x = -1; x <= 1; x += 2) {
        for (int y = -1; y <= 1; y += 2) {
            for (int z = -1; z <= 1; z += 2) {
                glVertex3f(x * 0.8f, y * 0.8f, z * 0.8f);
            }
        }
    }
    glEnd();
    glPointSize(1.0f);
    glPopMatrix();

    // 2. 3D UV Sphere Asteroid Mesh (Right)
    glPushMatrix();
    glTranslatef(1.8f, 0.0f, 0.0f);
    glRotatef(animAngle * 0.7f, 0.2f, 1.0f, 0.3f);

    glColor3f(0.9f, 0.6f, 0.3f);
    glutWireSphere(1.0, 16, 12);
    glPopMatrix();

    glDisable(GL_DEPTH_TEST);

    beginHUD(screenWidth, screenHeight);
    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(screenWidth / 2 - 130.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "3D Mesh Generation & Rotation");
    glColor3f(0.8f, 0.85f, 0.95f);
    renderBitmapString(screenWidth / 2 - 160.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12,
                       "Space Station Cube (8 Vertices, 12 Edges) | UV Sphere Mesh (Lat/Lon Grid)");
    endHUD();
}

void RasterAlgorithmsDemo::render(int screenWidth, int screenHeight) {
    glClearColor(0.02f, 0.03f, 0.05f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (currentMode != 3) {
        // Set 2D pixel coordinate space [0, width] x [0, height]
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluOrtho2D(0.0, static_cast<double>(screenWidth), 0.0, static_cast<double>(screenHeight));

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    switch (currentMode) {
        case 0: renderLinesMode(screenWidth, screenHeight); break;
        case 1: renderCurvesMode(screenWidth, screenHeight); break;
        case 2: renderScanlineMode(screenWidth, screenHeight); break;
        case 3: render3DMeshMode(screenWidth, screenHeight); break;
    }

    // Top Header
    beginHUD(screenWidth, screenHeight);
    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_18,
                       "AIRSTRIKE CGL-CO3: 2D & 3D Rasterization & Mesh Algorithms");

    glColor3f(0.7f, 0.75f, 0.85f);
    renderBitmapString(20.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12,
                       "Pure Integer Decision Parameter (pk) | DDA | 8-Way & 4-Way Symmetry | Scanline ET/AET");

    // Bottom Navigation
    glColor3f(0.6f, 0.6f, 0.7f);
    renderBitmapString(20.0f, 20.0f, GLUT_BITMAP_HELVETICA_12,
                       "[M] Cycle Mode (Lines / Curves / Scanline / 3D) | [+/-] Adjust Size | [SPACE] Pause | [1-6] Switch COs");

    endHUD();
}

} // namespace CGL
