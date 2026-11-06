#include "viewing_demo.hpp"
#include "common/font_renderer.hpp"
#include <iomanip>
#include <sstream>

namespace CGL {

ViewingDemo::ViewingDemo()
    : projectionMode(0), cameraPitch(25.0f), cameraYaw(35.0f),
      cameraDistance(12.0f), fov(55.0f), animAngle(0.0f) {}

void ViewingDemo::init() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

void ViewingDemo::update(float dt) {
    animAngle += 30.0f * dt;
    if (animAngle >= 360.0f) animAngle -= 360.0f;
}

void ViewingDemo::toggleProjectionMode() {
    projectionMode = (projectionMode + 1) % 3;
}

void ViewingDemo::orbitCamera(float dPitch, float dYaw) {
    cameraPitch += dPitch;
    if (cameraPitch > 85.0f) cameraPitch = 85.0f;
    if (cameraPitch < -85.0f) cameraPitch = -85.0f;

    cameraYaw += dYaw;
    if (cameraYaw >= 360.0f) cameraYaw -= 360.0f;
    if (cameraYaw < 0.0f) cameraYaw += 360.0f;
}

void ViewingDemo::zoomCamera(float dZoom) {
    cameraDistance += dZoom;
    if (cameraDistance < 4.0f) cameraDistance = 4.0f;
    if (cameraDistance > 30.0f) cameraDistance = 30.0f;
}

void ViewingDemo::reset() {
    projectionMode = 0;
    cameraPitch = 25.0f;
    cameraYaw = 35.0f;
    cameraDistance = 12.0f;
    fov = 55.0f;
}

void ViewingDemo::renderSceneObjects() {
    // 1. Sector Floor Grid (XZ plane)
    glLineWidth(1.0f);
    glColor4f(0.15f, 0.25f, 0.4f, 0.4f);
    glBegin(GL_LINES);
    for (float i = -10.0f; i <= 10.0f; i += 2.0f) {
        glVertex3f(i, -2.0f, -15.0f); glVertex3f(i, -2.0f, 15.0f);
        glVertex3f(-10.0f, -2.0f, i); glVertex3f(10.0f, -2.0f, i);
    }
    glEnd();

    // 2. Central Player Starfighter
    glPushMatrix();
    glTranslatef(0.0f, 0.0f, 0.0f);
    glRotatef(animAngle * 0.5f, 0.0f, 1.0f, 0.0f);

    glBegin(GL_TRIANGLES);
    // Ship Cockpit / Fuselage
    glColor3f(0.2f, 0.8f, 1.0f);
    glVertex3f(0.0f, 0.4f, 1.5f);
    glColor3f(0.05f, 0.2f, 0.6f);
    glVertex3f(-0.8f, 0.0f, -1.0f);
    glColor3f(0.05f, 0.2f, 0.6f);
    glVertex3f(0.8f, 0.0f, -1.0f);

    // Left Wing
    glColor3f(0.1f, 0.5f, 0.9f);
    glVertex3f(-0.4f, 0.0f, 0.5f);
    glColor3f(0.0f, 0.15f, 0.4f);
    glVertex3f(-2.0f, 0.0f, -1.2f);
    glColor3f(0.1f, 0.6f, 1.0f);
    glVertex3f(-0.4f, 0.0f, -0.8f);

    // Right Wing
    glColor3f(0.1f, 0.5f, 0.9f);
    glVertex3f(0.4f, 0.0f, 0.5f);
    glColor3f(0.1f, 0.6f, 1.0f);
    glVertex3f(0.4f, 0.0f, -0.8f);
    glColor3f(0.0f, 0.15f, 0.4f);
    glVertex3f(2.0f, 0.0f, -1.2f);
    glEnd();
    glPopMatrix();

    // 3. Asteroid Field at Varying Z Depths (Z = 5, 0, -5, -10, -15)
    // In Orthographic view, identical radii look the SAME size.
    // In Perspective view, distant asteroids shrink realistically!
    float asteroidPositions[5][3] = {
        {-4.0f,  1.0f,   5.0f}, // Near
        { 4.0f,  0.5f,   0.0f}, // Mid
        {-3.5f, -0.5f,  -5.0f}, // Mid-Far
        { 3.5f,  1.5f, -10.0f}, // Far
        { 0.0f,  0.0f, -15.0f}  // Deep Space
    };

    float colors[5][3] = {
        {1.0f, 0.5f, 0.2f},
        {0.8f, 0.8f, 0.3f},
        {0.3f, 0.9f, 0.6f},
        {0.4f, 0.6f, 1.0f},
        {0.9f, 0.4f, 0.8f}
    };

    for (int i = 0; i < 5; ++i) {
        glPushMatrix();
        glTranslatef(asteroidPositions[i][0], asteroidPositions[i][1], asteroidPositions[i][2]);
        glRotatef(animAngle * (1.0f + i * 0.2f), 0.3f, 1.0f, 0.5f);
        glColor3fv(colors[i]);
        glutWireSphere(1.2, 10, 8);
        glPopMatrix();
    }
}

void ViewingDemo::renderTacticalRadarView(int x, int y, int w, int h) {
    glViewport(x, y, w, h);
    glScissor(x, y, w, h);
    glEnable(GL_SCISSOR_TEST);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    // Top-down Orthographic Projection (X and Z mapped to screen)
    glOrtho(-12.0, 12.0, -12.0, 12.0, -30.0, 30.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    // Look straight down from Y = 20 along negative Y axis
    gluLookAt(0.0, 20.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0);

    renderSceneObjects();

    // Radar scan sweep ring
    glLineWidth(1.5f);
    glBegin(GL_LINE_LOOP);
    glColor4f(0.2f, 1.0f, 0.4f, 0.6f);
    for (int i = 0; i < 36; ++i) {
        float r = i * 2.0f * static_cast<float>(M_PI) / 36.0f;
        glVertex3f(std::cos(r) * 10.0f, 0.0f, std::sin(r) * 10.0f);
    }
    glEnd();

    glDisable(GL_SCISSOR_TEST);
}

void ViewingDemo::renderPerspectiveView(int x, int y, int w, int h) {
    glViewport(x, y, w, h);
    glScissor(x, y, w, h);
    glEnable(GL_SCISSOR_TEST);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    double aspect = (h > 0) ? (static_cast<double>(w) / h) : 1.0;
    gluPerspective(fov, aspect, 0.5, 60.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Orbiting Camera Position
    float pitchRad = cameraPitch * static_cast<float>(M_PI) / 180.0f;
    float yawRad = cameraYaw * static_cast<float>(M_PI) / 180.0f;

    float eyeX = cameraDistance * std::cos(pitchRad) * std::sin(yawRad);
    float eyeY = cameraDistance * std::sin(pitchRad);
    float eyeZ = cameraDistance * std::cos(pitchRad) * std::cos(yawRad);

    gluLookAt(eyeX, eyeY, eyeZ, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

    renderSceneObjects();

    glDisable(GL_SCISSOR_TEST);
}

void ViewingDemo::render(int screenWidth, int screenHeight) {
    glClearColor(0.02f, 0.03f, 0.05f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (projectionMode == 0) {
        // Split View: Left half Orthographic Radar, Right half Perspective Cockpit
        int halfW = screenWidth / 2;
        renderTacticalRadarView(0, 0, halfW, screenHeight);
        renderPerspectiveView(halfW, 0, halfW, screenHeight);

        // Border divider
        glViewport(0, 0, screenWidth, screenHeight);
        beginHUD(screenWidth, screenHeight);
        glColor3f(0.3f, 0.4f, 0.6f);
        glLineWidth(2.0f);
        glBegin(GL_LINES);
        glVertex2i(halfW, 0); glVertex2i(halfW, screenHeight);
        glEnd();
        glLineWidth(1.0f);
        endHUD();
    } else if (projectionMode == 1) {
        // Fullscreen Orthographic
        renderTacticalRadarView(0, 0, screenWidth, screenHeight);
    } else {
        // Fullscreen Perspective
        renderPerspectiveView(0, 0, screenWidth, screenHeight);
    }

    // Reset full viewport for HUD
    glViewport(0, 0, screenWidth, screenHeight);

    beginHUD(screenWidth, screenHeight);
    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_18,
                       "AIRSTRIKE CGL-CO4: Projection & Viewing Pipeline");

    glColor3f(0.7f, 0.75f, 0.85f);
    renderBitmapString(20.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12,
                       "World -> Eye (gluLookAt) -> Clip (glOrtho / gluPerspective) -> NDC [-1,1]^3 -> Viewport (glViewport)");

    // Mode labels
    if (projectionMode == 0) {
        int halfW = screenWidth / 2;
        glColor3f(0.2f, 1.0f, 0.4f);
        renderBitmapString(30.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Tactical Radar (Orthographic)");
        glColor3f(0.8f, 0.85f, 0.9f);
        renderBitmapString(30.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12,
                           "glOrtho(-12, 12, -12, 12, -30, 30) | Constant Object Scale across Z");

        glColor3f(1.0f, 0.7f, 0.2f);
        renderBitmapString(halfW + 30.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Cockpit View (Perspective)");
        glColor3f(0.8f, 0.85f, 0.9f);
        renderBitmapString(halfW + 30.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12,
                           "gluPerspective(55 deg) | Foreshortening Proportional to 1/z");
    } else if (projectionMode == 1) {
        glColor3f(0.2f, 1.0f, 0.4f);
        renderBitmapString(30.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Mode: Fullscreen Orthographic Projection");
    } else {
        glColor3f(1.0f, 0.7f, 0.2f);
        renderBitmapString(30.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_18, "Mode: Fullscreen Perspective Projection");
    }

    // Camera Stats
    std::stringstream ss;
    ss << "Camera Distance: " << std::fixed << std::setprecision(1) << cameraDistance
       << " | Pitch: " << cameraPitch << " deg | Yaw: " << cameraYaw << " deg";
    glColor3f(0.8f, 0.9f, 1.0f);
    renderBitmapString(20.0f, 50.0f, GLUT_BITMAP_HELVETICA_12, ss.str());

    // Navigation Controls
    glColor3f(0.6f, 0.6f, 0.7f);
    renderBitmapString(20.0f, 20.0f, GLUT_BITMAP_HELVETICA_12,
                       "[P] Toggle View (Split / Ortho / Perspective) | [Arrows/WASD] Orbit Camera | [+/-] Zoom | [R] Reset");

    endHUD();
}

} // namespace CGL
