#include "space_scene.hpp"
#include "common/font_renderer.hpp"
#include "algorithms/raster_algorithms.hpp"
#include "appearance/lighting_texture_demo.hpp"
#include <cmath>
#include <iomanip>
#include <sstream>

namespace CGL {

SpaceCombatScene::SpaceCombatScene()
    : playerX(0.0f), playerY(0.0f), playerRoll(0.0f),
      shieldActive(true), shieldEnergy(100.0f),
      laserTimer(0.0f), laserActive(false),
      animTime(0.0f), droneOrbitAngle(0.0f),
      planetTexId(0),
      combatBounds(-6.0f, -4.5f, 6.0f, 4.5f) {}

SpaceCombatScene::~SpaceCombatScene() {
    if (planetTexId != 0) glDeleteTextures(1, &planetTexId);
}

void SpaceCombatScene::init() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    createProceduralTextures();

    // Populate asteroid belt
    asteroids.clear();
    float posData[6][3] = {
        {-3.5f,  2.0f, -6.0f},
        { 4.0f,  1.5f, -8.0f},
        {-2.0f, -2.5f, -10.0f},
        { 3.0f, -1.8f, -5.0f},
        {-5.0f,  0.5f, -12.0f},
        { 1.5f,  3.0f, -14.0f}
    };

    for (int i = 0; i < 6; ++i) {
        AsteroidEntity ast;
        ast.pos = Vec3(posData[i][0], posData[i][1], posData[i][2]);
        ast.rotAxis = Vec3(0.3f * (i + 1), 0.7f, 0.2f * (6 - i));
        ast.rotSpeed = 25.0f + i * 15.0f;
        ast.currentRot = static_cast<float>(i * 45);
        ast.radius = 0.8f + (i % 3) * 0.35f;
        asteroids.push_back(ast);
    }
}

void SpaceCombatScene::createProceduralTextures() {
    int texDim = 128;
    std::vector<unsigned char> texData;
    LightingTextureDemo::generatePlanetTexture(texDim, texDim, texData);

    glGenTextures(1, &planetTexId);
    glBindTexture(GL_TEXTURE_2D, planetTexId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texDim, texDim, 0, GL_RGB, GL_UNSIGNED_BYTE, texData.data());
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
}

void SpaceCombatScene::update(float dt) {
    animTime += dt;
    droneOrbitAngle += 100.0f * dt;
    if (droneOrbitAngle >= 360.0f) droneOrbitAngle -= 360.0f;

    // Decay player roll back towards 0
    playerRoll *= (1.0f - dt * 4.0f);

    // Update asteroids
    for (auto& ast : asteroids) {
        ast.currentRot += ast.rotSpeed * dt;
        if (ast.currentRot >= 360.0f) ast.currentRot -= 360.0f;
    }

    if (laserActive) {
        laserTimer += dt;
        if (laserTimer > 0.35f) {
            laserActive = false;
            laserTimer = 0.0f;
        }
    }
}

void SpaceCombatScene::movePlayer(float dx, float dy) {
    playerX += dx;
    playerY += dy;

    // Clamp inside combat bounds
    if (playerX < combatBounds.xmin + 0.8f) playerX = combatBounds.xmin + 0.8f;
    if (playerX > combatBounds.xmax - 0.8f) playerX = combatBounds.xmax - 0.8f;
    if (playerY < combatBounds.ymin + 0.8f) playerY = combatBounds.ymin + 0.8f;
    if (playerY > combatBounds.ymax - 0.8f) playerY = combatBounds.ymax - 0.8f;

    if (dx < -0.01f) playerRoll = 25.0f;
    else if (dx > 0.01f) playerRoll = -25.0f;
}

void SpaceCombatScene::fireLaser() {
    laserActive = true;
    laserTimer = 0.0f;
}

void SpaceCombatScene::toggleShield() {
    shieldActive = !shieldActive;
}

void SpaceCombatScene::reset() {
    playerX = 0.0f;
    playerY = 0.0f;
    playerRoll = 0.0f;
    shieldActive = true;
    shieldEnergy = 100.0f;
    laserActive = false;
    animTime = 0.0f;
}

void SpaceCombatScene::renderSkyboxStars() {
    // CO1: GL_POINTS starfield
    glPointSize(2.0f);
    glBegin(GL_POINTS);
    for (int i = 0; i < 80; ++i) {
        float angle = i * 2.0f * static_cast<float>(M_PI) / 80.0f;
        float r = 25.0f;
        float sx = std::cos(angle) * r + (i % 7) * 2.0f - 7.0f;
        float sy = std::sin(angle * 1.5f) * 15.0f;
        float sz = -20.0f - (i % 5) * 3.0f;

        if (i % 3 == 0) glColor3f(0.5f, 0.8f, 1.0f);
        else if (i % 3 == 1) glColor3f(1.0f, 0.9f, 0.4f);
        else glColor3f(1.0f, 1.0f, 1.0f);

        glVertex3f(sx, sy, sz);
    }
    glEnd();
    glPointSize(1.0f);

    // CO6: Background Textured Planet
    glPushMatrix();
    glTranslatef(12.0f, 6.0f, -22.0f);
    glRotatef(animTime * 5.0f, 0.1f, 1.0f, 0.0f);

    if (planetTexId != 0) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, planetTexId);
        int slices = 20, stacks = 16;
        float radius = 4.5f;
        for (int i = 0; i < stacks; ++i) {
            float phi1 = static_cast<float>(M_PI) * (static_cast<float>(i) / stacks);
            float phi2 = static_cast<float>(M_PI) * (static_cast<float>(i + 1) / stacks);
            float v1 = static_cast<float>(i) / stacks;
            float v2 = static_cast<float>(i + 1) / stacks;
            glBegin(GL_QUAD_STRIP);
            for (int j = 0; j <= slices; ++j) {
                float theta = 2.0f * static_cast<float>(M_PI) * (static_cast<float>(j) / slices);
                float u = static_cast<float>(j) / slices;
                float nx1 = std::sin(phi1) * std::cos(theta);
                float ny1 = std::cos(phi1);
                float nz1 = std::sin(phi1) * std::sin(theta);
                glTexCoord2f(u, v1);
                glVertex3f(radius * nx1, radius * ny1, radius * nz1);
                float nx2 = std::sin(phi2) * std::cos(theta);
                float ny2 = std::cos(phi2);
                float nz2 = std::sin(phi2) * std::sin(theta);
                glTexCoord2f(u, v2);
                glVertex3f(radius * nx2, radius * ny2, radius * nz2);
            }
            glEnd();
        }
        glDisable(GL_TEXTURE_2D);
    }
    glPopMatrix();
}

void SpaceCombatScene::renderAsteroids() {
    // CO3 & CO6: 3D Asteroid Meshes with Lighting & Rotation
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);

    GLfloat lightPos[] = {5.0f, 8.0f, 5.0f, 1.0f};
    glLightfv(GL_LIGHT0, GL_POSITION, lightPos);

    GLfloat amb[] = {0.2f, 0.2f, 0.25f, 1.0f};
    GLfloat diff[] = {0.8f, 0.7f, 0.6f, 1.0f};
    GLfloat spec[] = {0.5f, 0.5f, 0.5f, 1.0f};
    glLightfv(GL_LIGHT0, GL_AMBIENT, amb);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, diff);
    glLightfv(GL_LIGHT0, GL_SPECULAR, spec);

    for (size_t i = 0; i < asteroids.size(); ++i) {
        const auto& ast = asteroids[i];
        glPushMatrix();
        glTranslatef(ast.pos.x, ast.pos.y, ast.pos.z);
        glRotatef(ast.currentRot, ast.rotAxis.x, ast.rotAxis.y, ast.rotAxis.z);

        GLfloat matDiff[] = {0.6f + 0.05f * i, 0.45f + 0.03f * i, 0.35f, 1.0f};
        glMaterialfv(GL_FRONT, GL_DIFFUSE, matDiff);
        glMaterialf(GL_FRONT, GL_SHININESS, 16.0f);

        glutSolidSphere(ast.radius, 12, 10);
        glPopMatrix();
    }

    glDisable(GL_LIGHTING);
}

void SpaceCombatScene::renderEscortDrone(float offsetAngle) {
    // CO2: Hierarchical Matrix Stack for Escort Drones
    glPushMatrix();
    glRotatef(droneOrbitAngle + offsetAngle, 0.0f, 0.0f, 1.0f);
    glTranslatef(1.3f, 0.0f, 0.0f);
    glRotatef(droneOrbitAngle * 2.0f, 0.0f, 1.0f, 0.0f);

    // Drone geometry (CO1 Triangles & Line Loop)
    glBegin(GL_TRIANGLES);
    glColor3f(1.0f, 0.5f, 0.1f);
    glVertex3f(0.0f, 0.25f, 0.0f);
    glColor3f(0.6f, 0.1f, 0.0f);
    glVertex3f(-0.18f, -0.15f, 0.0f);
    glColor3f(0.6f, 0.1f, 0.0f);
    glVertex3f(0.18f, -0.15f, 0.0f);
    glEnd();

    glLineWidth(1.5f);
    glBegin(GL_LINE_LOOP);
    glColor4f(1.0f, 0.8f, 0.2f, 0.8f);
    for (int i = 0; i < 8; ++i) {
        float r = i * 2.0f * static_cast<float>(M_PI) / 8.0f;
        glVertex3f(std::cos(r) * 0.28f, std::sin(r) * 0.28f, 0.0f);
    }
    glEnd();
    glLineWidth(1.0f);

    glPopMatrix();
}

void SpaceCombatScene::renderPlayerCraft() {
    glPushMatrix();
    // CO2 Transformations: Translate and Rotate
    glTranslatef(playerX, playerY, 0.0f);
    glRotatef(playerRoll, 0.0f, 1.0f, 0.0f);

    // CO1: GL_TRIANGLES Player Spacefighter
    glBegin(GL_TRIANGLES);
    // Fuselage Core
    glColor4f(0.2f, 0.7f, 1.0f, 1.0f);
    glVertex3f(0.0f, 0.8f, 0.2f);
    glColor4f(0.05f, 0.2f, 0.5f, 1.0f);
    glVertex3f(-0.4f, -0.6f, 0.0f);
    glColor4f(0.05f, 0.2f, 0.5f, 1.0f);
    glVertex3f(0.4f, -0.6f, 0.0f);

    // Left Wing
    glColor4f(0.1f, 0.4f, 0.9f, 1.0f);
    glVertex3f(-0.2f, 0.0f, 0.0f);
    glColor4f(0.0f, 0.15f, 0.4f, 1.0f);
    glVertex3f(-1.0f, -0.5f, -0.1f);
    glColor4f(0.1f, 0.6f, 1.0f, 1.0f);
    glVertex3f(-0.2f, -0.5f, 0.0f);

    // Right Wing
    glColor4f(0.1f, 0.4f, 0.9f, 1.0f);
    glVertex3f(0.2f, 0.0f, 0.0f);
    glColor4f(0.1f, 0.6f, 1.0f, 1.0f);
    glVertex3f(0.2f, -0.5f, 0.0f);
    glColor4f(0.0f, 0.15f, 0.4f, 1.0f);
    glVertex3f(1.0f, -0.5f, -0.1f);
    glEnd();

    // CO1: GL_POINTS Thruster Particles
    glPointSize(4.0f);
    glBegin(GL_POINTS);
    for (int p = 0; p < 6; ++p) {
        float offset = (std::sin(animTime * 20.0f + p) * 0.08f);
        glColor4f(1.0f, 0.6f + p * 0.06f, 0.1f, 0.9f);
        glVertex3f(-0.25f + offset, -0.7f - p * 0.08f, 0.0f);
        glVertex3f( 0.25f + offset, -0.7f - p * 0.08f, 0.0f);
    }
    glEnd();
    glPointSize(1.0f);

    // Escort Drones orbiting player craft
    renderEscortDrone(0.0f);
    renderEscortDrone(180.0f);

    // Deflector Shield
    if (shieldActive) {
        renderEnergyShield();
    }

    glPopMatrix();
}

void SpaceCombatScene::renderEnergyShield() {
    // CO1 & CO3: GL_TRIANGLE_FAN Energy Bubble calculated using Midpoint Circle math
    glBegin(GL_TRIANGLE_FAN);
    glColor4f(0.3f, 0.8f, 1.0f, 0.35f); // Center translucent blue
    glVertex3f(0.0f, 0.0f, 0.2f);

    int slices = 24;
    float radius = 1.15f;
    for (int i = 0; i <= slices; ++i) {
        float angle = i * 2.0f * static_cast<float>(M_PI) / slices;
        float pulse = std::sin(animTime * 6.0f + i) * 0.05f;
        glColor4f(0.1f, 0.4f, 0.9f, 0.15f);
        glVertex3f(std::cos(angle) * (radius + pulse), std::sin(angle) * (radius + pulse), 0.0f);
    }
    glEnd();

    // Shield Wireframe Contour (GL_LINE_LOOP)
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    glColor4f(0.4f, 1.0f, 0.8f, 0.8f);
    for (int i = 0; i < 24; ++i) {
        float angle = i * 2.0f * static_cast<float>(M_PI) / 24.0f;
        glVertex3f(std::cos(angle) * 1.15f, std::sin(angle) * 1.15f, 0.0f);
    }
    glEnd();
    glLineWidth(1.0f);
}

void SpaceCombatScene::renderLaserBeams() {
    if (!laserActive) return;

    // CO5: Cohen-Sutherland Clipping applied to combat sector boundaries
    LineSegment leftLaser(playerX - 0.25f, playerY + 0.5f, playerX - 0.25f, playerY + 12.0f);
    LineSegment rightLaser(playerX + 0.25f, playerY + 0.5f, playerX + 0.25f, playerY + 12.0f);

    LineSegment clippedLeft, clippedRight;
    int c1 = 0, c2 = 0;
    bool leftOk = ClippingAlgorithmsDemo::cohenSutherlandClip(leftLaser, combatBounds, clippedLeft, c1, c2);
    bool rightOk = ClippingAlgorithmsDemo::cohenSutherlandClip(rightLaser, combatBounds, clippedRight, c1, c2);

    glLineWidth(3.0f);
    glBegin(GL_LINES);
    if (leftOk) {
        glColor4f(1.0f, 0.2f, 0.2f, 1.0f);
        glVertex3f(clippedLeft.p1.x, clippedLeft.p1.y, 0.0f);
        glColor4f(1.0f, 0.8f, 0.2f, 1.0f);
        glVertex3f(clippedLeft.p2.x, clippedLeft.p2.y, 0.0f);
    }
    if (rightOk) {
        glColor4f(1.0f, 0.2f, 0.2f, 1.0f);
        glVertex3f(clippedRight.p1.x, clippedRight.p1.y, 0.0f);
        glColor4f(1.0f, 0.8f, 0.2f, 1.0f);
        glVertex3f(clippedRight.p2.x, clippedRight.p2.y, 0.0f);
    }
    glEnd();
    glLineWidth(1.0f);
}

void SpaceCombatScene::renderTacticalRadarPIP(int screenWidth, int screenHeight) {
    // CO4: Viewport & Orthographic Tactical Radar Overlay in Top-Right Corner
    int radarW = 200;
    int radarH = 160;
    int radarX = screenWidth - radarW - 20;
    int radarY = screenHeight - radarH - 20;

    glViewport(radarX, radarY, radarW, radarH);
    glScissor(radarX, radarY, radarW, radarH);
    glEnable(GL_SCISSOR_TEST);

    // Clear radar background
    glClearColor(0.02f, 0.06f, 0.1f, 0.9f);
    glClear(GL_COLOR_BUFFER_BIT);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(-10.0, 10.0, -8.0, 8.0, -1.0, 1.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    // Radar grid
    glColor4f(0.1f, 0.3f, 0.5f, 0.5f);
    glLineWidth(1.0f);
    glBegin(GL_LINES);
    glVertex2f(-10.0f, 0.0f); glVertex2f(10.0f, 0.0f);
    glVertex2f(0.0f, -8.0f); glVertex2f(0.0f, 8.0f);
    glEnd();

    // Radar scan circle
    glBegin(GL_LINE_LOOP);
    glColor4f(0.2f, 0.6f, 0.8f, 0.6f);
    for (int i = 0; i < 20; ++i) {
        float r = i * 2.0f * static_cast<float>(M_PI) / 20.0f;
        glVertex2f(std::cos(r) * 6.0f, std::sin(r) * 6.0f);
    }
    glEnd();

    // Asteroid blips
    glPointSize(5.0f);
    glBegin(GL_POINTS);
    glColor3f(1.0f, 0.5f, 0.2f);
    for (const auto& ast : asteroids) {
        glVertex2f(ast.pos.x, ast.pos.y);
    }
    glEnd();

    // Player blip
    glPointSize(7.0f);
    glBegin(GL_POINTS);
    glColor3f(0.2f, 1.0f, 0.4f);
    glVertex2f(playerX, playerY);
    glEnd();
    glPointSize(1.0f);

    glDisable(GL_SCISSOR_TEST);

    // Border around radar
    glViewport(0, 0, screenWidth, screenHeight);
    beginHUD(screenWidth, screenHeight);
    glColor3f(0.3f, 0.7f, 1.0f);
    glLineWidth(2.0f);
    glBegin(GL_LINE_LOOP);
    glVertex2i(radarX, radarY);
    glVertex2i(radarX + radarW, radarY);
    glVertex2i(radarX + radarW, radarY + radarH);
    glVertex2i(radarX, radarY + radarH);
    glEnd();
    glLineWidth(1.0f);

    renderBitmapString(radarX + 15.0f, radarY + radarH - 20.0f, GLUT_BITMAP_HELVETICA_12, "TACTICAL RADAR (ORTHO)");
    endHUD();
}

void SpaceCombatScene::render(int screenWidth, int screenHeight) {
    glClearColor(0.015f, 0.02f, 0.04f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // CO4: Main Perspective View
    glViewport(0, 0, screenWidth, screenHeight);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(55.0, static_cast<double>(screenWidth) / screenHeight, 0.1, 100.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    gluLookAt(0.0, -1.5, 9.0, 0.0, 0.5, 0.0, 0.0, 1.0, 0.0);

    // 1. Sector Boundary Box (CO5 Clipping bounds in 3D space)
    glLineWidth(1.5f);
    glBegin(GL_LINE_LOOP);
    glColor4f(0.2f, 0.6f, 0.8f, 0.5f);
    glVertex3f(combatBounds.xmin, combatBounds.ymin, 0.0f);
    glVertex3f(combatBounds.xmax, combatBounds.ymin, 0.0f);
    glVertex3f(combatBounds.xmax, combatBounds.ymax, 0.0f);
    glVertex3f(combatBounds.xmin, combatBounds.ymax, 0.0f);
    glEnd();
    glLineWidth(1.0f);

    // 2. Background Skybox & Textured Planet
    renderSkyboxStars();

    // 3. Asteroid Field
    renderAsteroids();

    // 4. Lasers (clipped)
    renderLaserBeams();

    // 5. Player Starfighter with Escort Drones & Shield
    renderPlayerCraft();

    // 6. PIP Tactical Radar
    renderTacticalRadarPIP(screenWidth, screenHeight);

    // 7. Full Combat HUD
    beginHUD(screenWidth, screenHeight);

    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_18,
                       "AIRSTRIKE CGL: Integrated 2D/3D Space Combat System");

    glColor3f(0.7f, 0.75f, 0.85f);
    renderBitmapString(20.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12,
                       "All 6 Course Outcomes (CO1-CO6) Active in Real-Time Execution");

    // Flight Telemetry
    std::stringstream ss;
    ss << "Ship Pos: (" << std::fixed << std::setprecision(2) << playerX << ", " << playerY << ") | Roll: "
       << static_cast<int>(playerRoll) << " deg";
    glColor3f(1.0f, 1.0f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_12, ss.str());

    std::string sText = shieldActive ? "Deflector Shield: ONLINE [S to toggle]" : "Deflector Shield: OFFLINE [S to toggle]";
    glColor3f(shieldActive ? 0.3f : 0.8f, shieldActive ? 1.0f : 0.3f, 0.3f);
    renderBitmapString(20.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, sText);

    glColor3f(0.4f, 0.8f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 120.0f, GLUT_BITMAP_HELVETICA_12,
                       "Active Drones: 2 Orbiting Escorts (CO2 Hierarchical Stack)");

    glColor3f(0.8f, 0.85f, 0.95f);
    renderBitmapString(20.0f, screenHeight - 140.0f, GLUT_BITMAP_HELVETICA_12,
                       "Lasers: Cohen-Sutherland Boundary Clipped (CO5)");

    // Controls
    glColor3f(0.6f, 0.6f, 0.7f);
    renderBitmapString(20.0f, 20.0f, GLUT_BITMAP_HELVETICA_12,
                       "[Arrows/WASD] Maneuver Fighter | [SPACE] Fire Lasers | [S] Toggle Shield | [1-6] CO Demos | [R] Reset | [ESC] Exit");

    endHUD();
}

} // namespace CGL
