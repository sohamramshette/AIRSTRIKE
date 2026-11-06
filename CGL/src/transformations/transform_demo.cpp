#include "transform_demo.hpp"
#include "common/font_renderer.hpp"
#include <iomanip>
#include <sstream>

namespace CGL {

TransformDemo::TransformDemo()
    : tx(0.0f), ty(0.0f), angle(0.0f), scaleVal(1.0f), shearX(0.0f),
      reflectionMode(0), orderTR(true), pivotMode(false),
      hierarchicalMode(true), shearMode(false),
      pivotX(0.3f), pivotY(0.3f), droneOrbitAngle(0.0f) {}

void TransformDemo::init() {
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

void TransformDemo::update(float dt) {
    droneOrbitAngle += 90.0f * dt;
    if (droneOrbitAngle >= 360.0f) droneOrbitAngle -= 360.0f;
}

void TransformDemo::translate(float dx, float dy) {
    tx += dx;
    ty += dy;
}

void TransformDemo::rotate(float deltaAngle) {
    angle += deltaAngle;
    if (angle >= 360.0f) angle -= 360.0f;
    if (angle < 0.0f) angle += 360.0f;
}

void TransformDemo::scale(float deltaScale) {
    scaleVal += deltaScale;
    if (scaleVal < 0.2f) scaleVal = 0.2f;
    if (scaleVal > 3.0f) scaleVal = 3.0f;
}

void TransformDemo::toggleOrder() {
    orderTR = !orderTR;
}

void TransformDemo::togglePivotMode() {
    pivotMode = !pivotMode;
}

void TransformDemo::toggleHierarchical() {
    hierarchicalMode = !hierarchicalMode;
}

void TransformDemo::toggleShear() {
    shearMode = !shearMode;
    shearX = shearMode ? 0.5f : 0.0f;
}

void TransformDemo::toggleReflection() {
    reflectionMode = (reflectionMode + 1) % 3; // 0: None, 1: X-axis, 2: Y-axis
}

void TransformDemo::reset() {
    tx = 0.0f;
    ty = 0.0f;
    angle = 0.0f;
    scaleVal = 1.0f;
    shearX = 0.0f;
    reflectionMode = 0;
    orderTR = true;
    pivotMode = false;
    shearMode = false;
}

void TransformDemo::multiplyMatrix4x4(const float a[16], const float b[16], float result[16]) {
    for (int col = 0; col < 4; ++col) {
        for (int row = 0; row < 4; ++row) {
            float sum = 0.0f;
            for (int k = 0; k < 4; ++k) {
                sum += a[k * 4 + row] * b[col * 4 + k];
            }
            result[col * 4 + row] = sum;
        }
    }
}

void TransformDemo::getTranslationMatrix(float tx, float ty, float tz, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    m[12] = tx;
    m[13] = ty;
    m[14] = tz;
}

void TransformDemo::getRotationXMatrix(float angleDeg, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    float rad = angleDeg * static_cast<float>(M_PI) / 180.0f;
    float c = std::cos(rad);
    float s = std::sin(rad);
    m[5] = c;
    m[6] = s;
    m[9] = -s;
    m[10] = c;
}

void TransformDemo::getRotationYMatrix(float angleDeg, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    float rad = angleDeg * static_cast<float>(M_PI) / 180.0f;
    float c = std::cos(rad);
    float s = std::sin(rad);
    m[0] = c;
    m[2] = -s;
    m[8] = s;
    m[10] = c;
}

void TransformDemo::getRotationZMatrix(float angleDeg, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    float rad = angleDeg * static_cast<float>(M_PI) / 180.0f;
    float c = std::cos(rad);
    float s = std::sin(rad);
    m[0] = c;
    m[1] = s;
    m[4] = -s;
    m[5] = c;
}

void TransformDemo::getScaleMatrix(float sx, float sy, float sz, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = 0.0f;
    m[0] = sx;
    m[5] = sy;
    m[10] = sz;
    m[15] = 1.0f;
}

void TransformDemo::getShear2DMatrix(float shx, float shy, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    m[4] = shx; // x' = x + shx * y
    m[1] = shy; // y' = y + shy * x
}

void TransformDemo::getReflectionXMatrix(float m[16]) {
    getScaleMatrix(1.0f, -1.0f, 1.0f, m); // Invert Y
}

void TransformDemo::getReflectionYMatrix(float m[16]) {
    getScaleMatrix(-1.0f, 1.0f, 1.0f, m); // Invert X
}

void TransformDemo::getArbitraryPivotRotationMatrix(float px, float py, float angleDeg, float m[16]) {
    float tNeg[16], rz[16], tPos[16], temp[16];
    getTranslationMatrix(-px, -py, 0.0f, tNeg);
    getRotationZMatrix(angleDeg, rz);
    getTranslationMatrix(px, py, 0.0f, tPos);

    multiplyMatrix4x4(rz, tNeg, temp);      // rz * tNeg
    multiplyMatrix4x4(tPos, temp, m);       // tPos * (rz * tNeg)
}

Vec3 TransformDemo::transformPoint(const float m[16], const Vec3& p) {
    float x = m[0] * p.x + m[4] * p.y + m[8]  * p.z + m[12];
    float y = m[1] * p.x + m[5] * p.y + m[9]  * p.z + m[13];
    float z = m[2] * p.x + m[6] * p.y + m[10] * p.z + m[14];
    float w = m[3] * p.x + m[7] * p.y + m[11] * p.z + m[15];

    if (std::fabs(w) > 1e-7f && std::fabs(w - 1.0f) > 1e-5f) {
        x /= w; y /= w; z /= w;
    }
    return Vec3(x, y, z);
}

void TransformDemo::drawFighter(float size) {
    // Fuselage
    glBegin(GL_TRIANGLES);
    glColor4f(0.2f, 0.7f, 1.0f, 1.0f);
    glVertex2f(0.0f, size * 0.9f);
    glColor4f(0.05f, 0.2f, 0.5f, 1.0f);
    glVertex2f(-size * 0.4f, -size * 0.7f);
    glColor4f(0.05f, 0.2f, 0.5f, 1.0f);
    glVertex2f(size * 0.4f, -size * 0.7f);

    // Left Wing
    glColor4f(0.1f, 0.4f, 0.9f, 1.0f);
    glVertex2f(-size * 0.2f, 0.0f);
    glColor4f(0.0f, 0.15f, 0.4f, 1.0f);
    glVertex2f(-size * 0.9f, -size * 0.6f);
    glColor4f(0.1f, 0.5f, 1.0f, 1.0f);
    glVertex2f(-size * 0.2f, -size * 0.6f);

    // Right Wing
    glColor4f(0.1f, 0.4f, 0.9f, 1.0f);
    glVertex2f(size * 0.2f, 0.0f);
    glColor4f(0.1f, 0.5f, 1.0f, 1.0f);
    glVertex2f(size * 0.2f, -size * 0.6f);
    glColor4f(0.0f, 0.15f, 0.4f, 1.0f);
    glVertex2f(size * 0.9f, -size * 0.6f);
    glEnd();

    // Twin Plasma Thrusters (animated)
    glBegin(GL_TRIANGLES);
    glColor4f(1.0f, 0.8f, 0.2f, 0.9f);
    glVertex2f(-size * 0.25f, -size * 0.7f);
    glColor4f(1.0f, 0.2f, 0.0f, 0.0f);
    glVertex2f(-size * 0.25f, -size * 1.1f);
    glColor4f(1.0f, 0.8f, 0.2f, 0.9f);
    glVertex2f(-size * 0.15f, -size * 0.7f);

    glVertex2f(size * 0.15f, -size * 0.7f);
    glColor4f(1.0f, 0.2f, 0.0f, 0.0f);
    glVertex2f(size * 0.25f, -size * 1.1f);
    glColor4f(1.0f, 0.8f, 0.2f, 0.9f);
    glVertex2f(size * 0.25f, -size * 0.7f);
    glEnd();
}

void TransformDemo::drawDrone(float size) {
    glBegin(GL_TRIANGLES);
    glColor4f(1.0f, 0.4f, 0.1f, 1.0f);
    glVertex2f(0.0f, size * 0.7f);
    glColor4f(0.6f, 0.1f, 0.0f, 1.0f);
    glVertex2f(-size * 0.5f, -size * 0.5f);
    glColor4f(0.6f, 0.1f, 0.0f, 1.0f);
    glVertex2f(size * 0.5f, -size * 0.5f);
    glEnd();

    // Drone energy shield ring
    glLineWidth(1.5f);
    glBegin(GL_LINE_LOOP);
    glColor4f(1.0f, 0.8f, 0.3f, 0.8f);
    for (int i = 0; i < 12; ++i) {
        float rad = i * 2.0f * static_cast<float>(M_PI) / 12.0f;
        glVertex2f(std::cos(rad) * size * 0.9f, std::sin(rad) * size * 0.9f);
    }
    glEnd();
    glLineWidth(1.0f);
}

void TransformDemo::render(int screenWidth, int screenHeight) {
    glClearColor(0.02f, 0.03f, 0.06f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(-2.0, 2.0, -2.0, 2.0, -1.0, 1.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // 1. Draw World Coordinate Axes & Grid
    glLineWidth(1.0f);
    glColor4f(0.2f, 0.25f, 0.35f, 0.4f);
    glBegin(GL_LINES);
    for (float g = -2.0f; g <= 2.0f; g += 0.5f) {
        glVertex2f(g, -2.0f); glVertex2f(g, 2.0f);
        glVertex2f(-2.0f, g); glVertex2f(2.0f, g);
    }
    glEnd();

    // Main X and Y axes
    glLineWidth(2.0f);
    glBegin(GL_LINES);
    glColor4f(1.0f, 0.3f, 0.3f, 0.9f); // X-axis (Red)
    glVertex2f(-2.0f, 0.0f); glVertex2f(2.0f, 0.0f);
    glColor4f(0.3f, 1.0f, 0.3f, 0.9f); // Y-axis (Green)
    glVertex2f(0.0f, -2.0f); glVertex2f(0.0f, 2.0f);
    glEnd();
    glLineWidth(1.0f);

    // 2. Draw Pivot Point Indicator if Pivot Mode is Active
    if (pivotMode) {
        glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
        glPointSize(6.0f);
        glBegin(GL_POINTS);
        glVertex2f(pivotX, pivotY);
        glEnd();
        glPointSize(1.0f);

        // Crosshairs at pivot
        glLineWidth(1.5f);
        glBegin(GL_LINES);
        glVertex2f(pivotX - 0.1f, pivotY); glVertex2f(pivotX + 0.1f, pivotY);
        glVertex2f(pivotX, pivotY - 0.1f); glVertex2f(pivotX, pivotY + 0.1f);
        glEnd();
        glLineWidth(1.0f);
    }

    // 3. Apply Transformations to Space Fighter
    glPushMatrix(); // Save identity world matrix

    if (pivotMode) {
        // Arbitrary Pivot Point: T(pivot) * R(angle) * T(-pivot)
        glTranslatef(pivotX, pivotY, 0.0f);
        glRotatef(angle, 0.0f, 0.0f, 1.0f);
        glTranslatef(-pivotX, -pivotY, 0.0f);
        glTranslatef(tx, ty, 0.0f);
    } else {
        if (orderTR) {
            // T * R: Translation applied, then rotation around local origin
            glTranslatef(tx, ty, 0.0f);
            glRotatef(angle, 0.0f, 0.0f, 1.0f);
        } else {
            // R * T: Rotation first, causing object to orbit world origin!
            glRotatef(angle, 0.0f, 0.0f, 1.0f);
            glTranslatef(tx, ty, 0.0f);
        }
    }

    // Scaling
    glScalef(scaleVal, scaleVal, 1.0f);

    // Reflection
    if (reflectionMode == 1) {
        glScalef(1.0f, -1.0f, 1.0f); // Reflect across X-axis
    } else if (reflectionMode == 2) {
        glScalef(-1.0f, 1.0f, 1.0f); // Reflect across Y-axis
    }

    // Shearing
    if (shearMode) {
        float shearMatrix[16] = {
            1.0f, 0.0f, 0.0f, 0.0f,
            shearX, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        };
        glMultMatrixf(shearMatrix);
    }

    // Read ModelView matrix to display to user
    float currentMatrix[16];
    glGetFloatv(GL_MODELVIEW_MATRIX, currentMatrix);

    // Render Primary Fighter
    drawFighter(0.4f);

    // 4. Hierarchical Transformation: Orbiting Escort Drone
    if (hierarchicalMode) {
        glPushMatrix(); // Push parent ship transform
        // Drone rotates around parent ship at distance 0.8
        glRotatef(droneOrbitAngle, 0.0f, 0.0f, 1.0f);
        glTranslatef(0.8f, 0.0f, 0.0f);
        // Self-spin of drone
        glRotatef(droneOrbitAngle * 2.0f, 0.0f, 0.0f, 1.0f);
        drawDrone(0.15f);
        glPopMatrix(); // Pop back to parent ship transform

        // Second symmetrical escort drone
        glPushMatrix();
        glRotatef(droneOrbitAngle + 180.0f, 0.0f, 0.0f, 1.0f);
        glTranslatef(0.8f, 0.0f, 0.0f);
        glRotatef(droneOrbitAngle * 2.0f, 0.0f, 0.0f, 1.0f);
        drawDrone(0.15f);
        glPopMatrix();
    }

    glPopMatrix(); // Restore world matrix

    // 5. HUD Information Overlay
    beginHUD(screenWidth, screenHeight);

    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_18,
                       "AIRSTRIKE CGL-CO2: 2D & 3D Geometric Transformations");

    glColor3f(0.7f, 0.75f, 0.85f);
    renderBitmapString(20.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12,
                       "Homogeneous Coordinates | Matrix Stack | Affine Mappings | Non-Commutative Composite Transforms");

    // Left Panel: Current Parameter State
    std::stringstream ss;
    ss << "Tx: " << std::fixed << std::setprecision(2) << tx << "  Ty: " << ty;
    glColor3f(1.0f, 1.0f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_12, ss.str());

    ss.str("");
    ss << "Angle: " << std::fixed << std::setprecision(1) << angle << " deg  |  Scale: " << scaleVal << "x";
    renderBitmapString(20.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, ss.str());

    std::string orderStr = orderTR ? "Order: [Translate * Rotate] (Local)" : "Order: [Rotate * Translate] (Orbital)";
    glColor3f(1.0f, 0.8f, 0.2f);
    renderBitmapString(20.0f, screenHeight - 120.0f, GLUT_BITMAP_HELVETICA_12, orderStr);

    std::string pivotStr = pivotMode ? "Pivot Mode: ON (0.3, 0.3) [P]" : "Pivot Mode: OFF (Origin 0,0) [P]";
    glColor3f(0.4f, 1.0f, 0.5f);
    renderBitmapString(20.0f, screenHeight - 140.0f, GLUT_BITMAP_HELVETICA_12, pivotStr);

    const char* reflStr = (reflectionMode == 1) ? "Reflection: X-Axis" : ((reflectionMode == 2) ? "Reflection: Y-Axis" : "Reflection: None");
    glColor3f(0.9f, 0.5f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 160.0f, GLUT_BITMAP_HELVETICA_12, reflStr);

    std::string shearStr = shearMode ? "Shear: ON (shx = 0.5) [S]" : "Shear: OFF [S]";
    glColor3f(1.0f, 0.4f, 0.4f);
    renderBitmapString(20.0f, screenHeight - 180.0f, GLUT_BITMAP_HELVETICA_12, shearStr);

    std::string hierStr = hierarchicalMode ? "Hierarchical Drones: ON [H]" : "Hierarchical Drones: OFF [H]";
    glColor3f(0.4f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 200.0f, GLUT_BITMAP_HELVETICA_12, hierStr);

    // Right Panel: 4x4 Current ModelView Transformation Matrix
    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(screenWidth - 320.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_12, "ModelView 4x4 Matrix:");
    glColor3f(0.8f, 0.85f, 0.95f);
    for (int r = 0; r < 4; ++r) {
        ss.str("");
        ss << "[ ";
        for (int c = 0; c < 4; ++c) {
            ss << std::setw(6) << std::fixed << std::setprecision(2) << currentMatrix[c * 4 + r] << " ";
        }
        ss << "]";
        renderBitmapString(screenWidth - 320.0f, screenHeight - 100.0f - r * 18.0f, GLUT_BITMAP_HELVETICA_12, ss.str());
    }

    // Bottom Controls Guide
    glColor3f(0.6f, 0.6f, 0.7f);
    renderBitmapString(20.0f, 20.0f, GLUT_BITMAP_HELVETICA_12,
                       "[Arrows/WASD] Translate | [Q/E] Rotate | [+/-] Scale | [O] Toggle T*R | [P] Pivot | [F] Reflect | [S] Shear | [R] Reset");

    endHUD();
}

} // namespace CGL

