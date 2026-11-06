#include "clipping_algorithms.hpp"
#include "common/font_renderer.hpp"
#include <iomanip>
#include <sstream>

namespace CGL {

ClippingAlgorithmsDemo::ClippingAlgorithmsDemo()
    : currentMode(0), clipWin(-0.5f, -0.4f, 0.5f, 0.4f),
      animate(true), animTime(0.0f) {}

void ClippingAlgorithmsDemo::init() {
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

void ClippingAlgorithmsDemo::update(float dt) {
    if (animate) {
        animTime += dt;
    }
}

void ClippingAlgorithmsDemo::cycleMode() {
    currentMode = (currentMode + 1) % 3;
}

void ClippingAlgorithmsDemo::moveClipWindow(float dx, float dy) {
    clipWin.xmin += dx;
    clipWin.xmax += dx;
    clipWin.ymin += dy;
    clipWin.ymax += dy;
}

void ClippingAlgorithmsDemo::resizeClipWindow(float delta) {
    clipWin.xmin -= delta;
    clipWin.xmax += delta;
    clipWin.ymin -= delta * 0.8f;
    clipWin.ymax += delta * 0.8f;
    if (clipWin.xmax - clipWin.xmin < 0.2f) {
        clipWin.xmin = -0.1f; clipWin.xmax = 0.1f;
        clipWin.ymin = -0.1f; clipWin.ymax = 0.1f;
    }
}

void ClippingAlgorithmsDemo::toggleAnimate() {
    animate = !animate;
}

void ClippingAlgorithmsDemo::reset() {
    clipWin = RectWindow(-0.5f, -0.4f, 0.5f, 0.4f);
    animTime = 0.0f;
    currentMode = 0;
}

// -------------------------------------------------------------
// Algorithm Implementations
// -------------------------------------------------------------

int ClippingAlgorithmsDemo::computeOutCode(float x, float y, const RectWindow& win) {
    int code = CODE_INSIDE;
    if (x < win.xmin)      code |= CODE_LEFT;
    else if (x > win.xmax) code |= CODE_RIGHT;
    if (y < win.ymin)      code |= CODE_BOTTOM;
    else if (y > win.ymax) code |= CODE_TOP;
    return code;
}

bool ClippingAlgorithmsDemo::cohenSutherlandClip(const LineSegment& in, const RectWindow& win,
                                                LineSegment& out, int& outCode1, int& outCode2) {
    float x1 = in.p1.x, y1 = in.p1.y;
    float x2 = in.p2.x, y2 = in.p2.y;

    int code1 = computeOutCode(x1, y1, win);
    int code2 = computeOutCode(x2, y2, win);
    outCode1 = code1;
    outCode2 = code2;

    bool accept = false;

    while (true) {
        if ((code1 | code2) == 0) {
            // Trivial accept: both endpoints inside
            accept = true;
            break;
        } else if ((code1 & code2) != 0) {
            // Trivial reject: both endpoints share outside region
            break;
        } else {
            // Calculate boundary intersection
            float x = 0.0f, y = 0.0f;
            int codeOut = code1 ? code1 : code2;

            if (codeOut & CODE_TOP) {
                x = x1 + (x2 - x1) * (win.ymax - y1) / (y2 - y1);
                y = win.ymax;
            } else if (codeOut & CODE_BOTTOM) {
                x = x1 + (x2 - x1) * (win.ymin - y1) / (y2 - y1);
                y = win.ymin;
            } else if (codeOut & CODE_RIGHT) {
                y = y1 + (y2 - y1) * (win.xmax - x1) / (x2 - x1);
                x = win.xmax;
            } else if (codeOut & CODE_LEFT) {
                y = y1 + (y2 - y1) * (win.xmin - x1) / (x2 - x1);
                x = win.xmin;
            }

            if (codeOut == code1) {
                x1 = x;
                y1 = y;
                code1 = computeOutCode(x1, y1, win);
            } else {
                x2 = x;
                y2 = y;
                code2 = computeOutCode(x2, y2, win);
            }
        }
    }

    if (accept) {
        out = LineSegment(x1, y1, x2, y2);
    }
    return accept;
}

bool ClippingAlgorithmsDemo::liangBarskyClip(const LineSegment& in, const RectWindow& win,
                                            LineSegment& out, float& u1, float& u2) {
    float dx = in.p2.x - in.p1.x;
    float dy = in.p2.y - in.p1.y;

    float p[4] = {-dx, dx, -dy, dy};
    float q[4] = {
        in.p1.x - win.xmin,
        win.xmax - in.p1.x,
        in.p1.y - win.ymin,
        win.ymax - in.p1.y
    };

    u1 = 0.0f;
    u2 = 1.0f;

    for (int i = 0; i < 4; ++i) {
        if (std::abs(p[i]) < 1e-7f) {
            // Line is parallel to boundary
            if (q[i] < 0.0f) return false; // Completely outside
        } else if (p[i] < 0.0f) {
            // Line directed outside to inside
            float r = q[i] / p[i];
            if (r > u1) u1 = r;
        } else {
            // Line directed inside to outside
            float r = q[i] / p[i];
            if (r < u2) u2 = r;
        }
    }

    if (u1 > u2) return false; // Rejected

    out.p1 = Vec2(in.p1.x + u1 * dx, in.p1.y + u1 * dy);
    out.p2 = Vec2(in.p1.x + u2 * dx, in.p1.y + u2 * dy);
    return true;
}

std::vector<Vec2> ClippingAlgorithmsDemo::sutherlandHodgmanClip(const std::vector<Vec2>& poly, const RectWindow& win) {
    std::vector<Vec2> outputList = poly;
    if (outputList.empty()) return outputList;

    // 4 clipping boundary passes: 0: Left, 1: Right, 2: Bottom, 3: Top
    for (int edge = 0; edge < 4; ++edge) {
        std::vector<Vec2> inputList = outputList;
        outputList.clear();
        if (inputList.empty()) break;

        auto isInside = [&](const Vec2& pt) -> bool {
            switch (edge) {
                case 0: return pt.x >= win.xmin; // Left
                case 1: return pt.x <= win.xmax; // Right
                case 2: return pt.y >= win.ymin; // Bottom
                case 3: return pt.y <= win.ymax; // Top
            }
            return false;
        };

        auto intersection = [&](const Vec2& p1, const Vec2& p2) -> Vec2 {
            float dx = p2.x - p1.x;
            float dy = p2.y - p1.y;
            Vec2 inter;
            switch (edge) {
                case 0: // Left (x = win.xmin)
                    inter.x = win.xmin;
                    inter.y = p1.y + dy * (win.xmin - p1.x) / dx;
                    break;
                case 1: // Right (x = win.xmax)
                    inter.x = win.xmax;
                    inter.y = p1.y + dy * (win.xmax - p1.x) / dx;
                    break;
                case 2: // Bottom (y = win.ymin)
                    inter.y = win.ymin;
                    inter.x = p1.x + dx * (win.ymin - p1.y) / dy;
                    break;
                case 3: // Top (y = win.ymax)
                    inter.y = win.ymax;
                    inter.x = p1.x + dx * (win.ymax - p1.y) / dy;
                    break;
            }
            return inter;
        };

        Vec2 prev = inputList.back();
        for (const auto& curr : inputList) {
            bool currIn = isInside(curr);
            bool prevIn = isInside(prev);

            if (currIn) {
                if (prevIn) {
                    outputList.push_back(curr); // In -> In
                } else {
                    outputList.push_back(intersection(prev, curr)); // Out -> In
                    outputList.push_back(curr);
                }
            } else if (prevIn) {
                outputList.push_back(intersection(prev, curr)); // In -> Out
            } // Out -> Out: emit nothing

            prev = curr;
        }
    }

    return outputList;
}

// -------------------------------------------------------------
// Visual Screen Rendering
// -------------------------------------------------------------

void ClippingAlgorithmsDemo::drawClipWindow() {
    // Shaded clip window background
    glColor4f(0.05f, 0.15f, 0.25f, 0.35f);
    glBegin(GL_QUADS);
    glVertex2f(clipWin.xmin, clipWin.ymin);
    glVertex2f(clipWin.xmax, clipWin.ymin);
    glVertex2f(clipWin.xmax, clipWin.ymax);
    glVertex2f(clipWin.xmin, clipWin.ymax);
    glEnd();

    // Clip boundary lines (Bright Cyan)
    glColor4f(0.2f, 0.9f, 1.0f, 0.9f);
    glLineWidth(2.5f);
    glBegin(GL_LINE_LOOP);
    glVertex2f(clipWin.xmin, clipWin.ymin);
    glVertex2f(clipWin.xmax, clipWin.ymin);
    glVertex2f(clipWin.xmax, clipWin.ymax);
    glVertex2f(clipWin.xmin, clipWin.ymax);
    glEnd();
    glLineWidth(1.0f);
}

void ClippingAlgorithmsDemo::renderCohenSutherland(int screenWidth, int screenHeight) {
    drawClipWindow();

    // Generate animated test lines across sector boundaries
    float t = animTime * 0.8f;
    std::vector<LineSegment> testLines = {
        LineSegment(-0.85f, -0.6f, 0.85f, 0.6f),                              // Diagonal cut
        LineSegment(0.0f, -0.8f + std::sin(t) * 0.2f, 0.0f, 0.8f),            // Vertical
        LineSegment(-0.8f, 0.1f + std::cos(t) * 0.2f, 0.8f, 0.1f),            // Horizontal
        LineSegment(-0.7f, 0.6f, -0.1f, 0.7f),                               // Outside Top-Left
        LineSegment(-0.3f, -0.2f, 0.3f, 0.2f)                                // Completely Inside
    };

    int acceptedCount = 0;

    for (const auto& line : testLines) {
        // Draw original line in dimmed red/gray
        glColor4f(0.8f, 0.2f, 0.2f, 0.45f);
        glLineWidth(1.5f);
        glBegin(GL_LINES);
        glVertex2f(line.p1.x, line.p1.y);
        glVertex2f(line.p2.x, line.p2.y);
        glEnd();

        // Perform Cohen-Sutherland Clipping
        LineSegment clipped;
        int code1 = 0, code2 = 0;
        bool accepted = cohenSutherlandClip(line, clipWin, clipped, code1, code2);

        if (accepted) {
            acceptedCount++;
            // Draw accepted segment in vibrant Neon Green
            glColor4f(0.2f, 1.0f, 0.4f, 1.0f);
            glLineWidth(3.0f);
            glBegin(GL_LINES);
            glVertex2f(clipped.p1.x, clipped.p1.y);
            glVertex2f(clipped.p2.x, clipped.p2.y);
            glEnd();

            // Highlight intersection vertices
            glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
            glPointSize(6.0f);
            glBegin(GL_POINTS);
            glVertex2f(clipped.p1.x, clipped.p1.y);
            glVertex2f(clipped.p2.x, clipped.p2.y);
            glEnd();
            glPointSize(1.0f);
        }
    }
    glLineWidth(1.0f);

    beginHUD(screenWidth, screenHeight);
    glColor3f(0.2f, 1.0f, 0.4f);
    renderBitmapString(30.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18,
                       "Cohen-Sutherland Line Clipping (4-bit TBRL Outcodes)");

    std::stringstream ss;
    ss << "Region Codes: TOP(1000) | BOTTOM(0100) | RIGHT(0010) | LEFT(0001) | Accepted: "
       << acceptedCount << "/" << testLines.size() << " lines";
    glColor3f(0.8f, 0.85f, 0.95f);
    renderBitmapString(30.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, ss.str());
    endHUD();
}

void ClippingAlgorithmsDemo::renderLiangBarsky(int screenWidth, int screenHeight) {
    drawClipWindow();

    float t = animTime * 0.7f;
    std::vector<LineSegment> testLines = {
        LineSegment(-0.8f, -0.7f, 0.75f, 0.65f),
        LineSegment(-0.6f + std::sin(t) * 0.3f, 0.7f, 0.6f, -0.7f),
        LineSegment(-0.9f, 0.0f, 0.9f, 0.0f),
        LineSegment(0.6f, 0.5f, 0.85f, 0.7f)
    };

    int acceptedCount = 0;

    for (const auto& line : testLines) {
        // Original line
        glColor4f(0.7f, 0.3f, 0.3f, 0.4f);
        glLineWidth(1.5f);
        glBegin(GL_LINES);
        glVertex2f(line.p1.x, line.p1.y);
        glVertex2f(line.p2.x, line.p2.y);
        glEnd();

        LineSegment clipped;
        float u1 = 0.0f, u2 = 1.0f;
        bool accepted = liangBarskyClip(line, clipWin, clipped, u1, u2);

        if (accepted) {
            acceptedCount++;
            glColor4f(1.0f, 0.8f, 0.2f, 1.0f); // Bright Gold
            glLineWidth(3.0f);
            glBegin(GL_LINES);
            glVertex2f(clipped.p1.x, clipped.p1.y);
            glVertex2f(clipped.p2.x, clipped.p2.y);
            glEnd();

            glColor4f(0.3f, 1.0f, 1.0f, 1.0f);
            glPointSize(6.0f);
            glBegin(GL_POINTS);
            glVertex2f(clipped.p1.x, clipped.p1.y);
            glVertex2f(clipped.p2.x, clipped.p2.y);
            glEnd();
            glPointSize(1.0f);
        }
    }
    glLineWidth(1.0f);

    beginHUD(screenWidth, screenHeight);
    glColor3f(1.0f, 0.8f, 0.2f);
    renderBitmapString(30.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18,
                       "Liang-Barsky Parametric Line Clipping Algorithm");
    glColor3f(0.8f, 0.85f, 0.95f);
    renderBitmapString(30.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12,
                       "P(u) = P1 + u*(P2 - P1), 0 <= u <= 1 | Parametric Inequalities u*pk <= qk");
    endHUD();
}

void ClippingAlgorithmsDemo::renderSutherlandHodgman(int screenWidth, int screenHeight) {
    drawClipWindow();

    float angle = animTime * 25.0f * static_cast<float>(M_PI) / 180.0f;
    float cosA = std::cos(angle);
    float sinA = std::sin(angle);

    // Starfighter polygon silhouette
    std::vector<Vec2> originalPoly = {
        Vec2( 0.0f,  0.55f),
        Vec2( 0.25f, 0.1f),
        Vec2( 0.65f, -0.45f),
        Vec2( 0.2f,  -0.3f),
        Vec2( 0.0f,  -0.2f),
        Vec2(-0.2f,  -0.3f),
        Vec2(-0.65f, -0.45f),
        Vec2(-0.25f, 0.1f)
    };

    // Apply rotation and offset
    for (auto& v : originalPoly) {
        float rx = v.x * cosA - v.y * sinA;
        float ry = v.x * sinA + v.y * cosA;
        v.x = rx + 0.1f;
        v.y = ry + 0.05f;
    }

    // Draw original unclipped polygon (dashed outline)
    glColor4f(0.8f, 0.25f, 0.25f, 0.4f);
    glLineWidth(1.5f);
    glBegin(GL_LINE_LOOP);
    for (const auto& v : originalPoly) {
        glVertex2f(v.x, v.y);
    }
    glEnd();

    // Clip Polygon using Sutherland-Hodgman
    auto clippedPoly = sutherlandHodgmanClip(originalPoly, clipWin);

    if (clippedPoly.size() >= 3) {
        // Shaded clipped interior
        glColor4f(0.2f, 0.8f, 0.5f, 0.5f);
        glBegin(GL_POLYGON);
        for (const auto& v : clippedPoly) {
            glVertex2f(v.x, v.y);
        }
        glEnd();

        // Bright border
        glColor4f(0.3f, 1.0f, 0.6f, 1.0f);
        glLineWidth(2.5f);
        glBegin(GL_LINE_LOOP);
        for (const auto& v : clippedPoly) {
            glVertex2f(v.x, v.y);
        }
        glEnd();

        // Highlight clipped vertices
        glColor4f(1.0f, 1.0f, 0.2f, 1.0f);
        glPointSize(6.0f);
        glBegin(GL_POINTS);
        for (const auto& v : clippedPoly) {
            glVertex2f(v.x, v.y);
        }
        glEnd();
        glPointSize(1.0f);
    }
    glLineWidth(1.0f);

    beginHUD(screenWidth, screenHeight);
    glColor3f(0.3f, 1.0f, 0.6f);
    renderBitmapString(30.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18,
                       "Sutherland-Hodgman Polygon Clipping Algorithm");
    std::stringstream ss;
    ss << "Sequential Edge Pipeline (Left->Right->Bottom->Top) | Original: "
       << originalPoly.size() << " verts | Clipped: " << clippedPoly.size() << " verts";
    glColor3f(0.8f, 0.85f, 0.95f);
    renderBitmapString(30.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, ss.str());
    endHUD();
}

void ClippingAlgorithmsDemo::render(int screenWidth, int screenHeight) {
    glClearColor(0.02f, 0.03f, 0.05f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    switch (currentMode) {
        case 0: renderCohenSutherland(screenWidth, screenHeight); break;
        case 1: renderLiangBarsky(screenWidth, screenHeight); break;
        case 2: renderSutherlandHodgman(screenWidth, screenHeight); break;
    }

    // Top Header & Bottom Help
    beginHUD(screenWidth, screenHeight);
    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_18,
                       "AIRSTRIKE CGL-CO5: 2D Line and Polygon Clipping Algorithms");

    glColor3f(0.7f, 0.75f, 0.85f);
    renderBitmapString(20.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12,
                       "Outcode Trivial Accept/Reject | Parametric Boundary Intersection | 4-Stage Vertex Pipeline");

    glColor3f(0.6f, 0.6f, 0.7f);
    renderBitmapString(20.0f, 20.0f, GLUT_BITMAP_HELVETICA_12,
                       "[M] Cycle Algorithm | [WASD] Move Clip Window | [+/-] Resize Window | [SPACE] Pause Animation | [R] Reset");

    endHUD();
}

} // namespace CGL

