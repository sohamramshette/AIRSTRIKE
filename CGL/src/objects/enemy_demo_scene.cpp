#include "enemy_demo_scene.hpp"
#include "common/font_renderer.hpp"
#include <cmath>
#include <sstream>
#include <iomanip>

namespace CGL {

EnemyDemoScene::EnemyDemoScene()
    : selectedEnemy(0), animTime(0.0f),
      co1Wireframe(false), co1Scale(1.8f), co1RotZ(0.0f),
      co2OrbitSpeedMult(1.0f), co2Scale(1.5f), co2PulseActive(true),
      co3TargetX(720), co3TargetY(480), co3ShieldRadius(45),
      co4UsePerspective(true), co4CustomZ(-25.0f), co4AutoDive(true), co4CameraPitch(0.0f),
      co5SkimmerOffsetX(0.0f), co5SkimmerOffsetY(0.0f), co5PolygonMode(false), co5WindowSize(0.6f),
      co6GouraudShading(true), co6TextureActive(true), co6Shininess(48.0f),
      co6LightAngle(45.0f), co6ModelRotX(15.0f), co6ModelRotY(0.0f) {}

void EnemyDemoScene::init() {
    polygonCruiser.init();
    orbitalRotor.init();
    rasterSentinel.init();
    warpCruiser.init();
    boundarySkimmer.init();
    phongCruiser.init();

    resetCurrentDemonstration();
}

void EnemyDemoScene::selectEnemy(int index) {
    if (index >= 0 && index <= 5) {
        selectedEnemy = index;
    }
}

void EnemyDemoScene::resetCurrentDemonstration() {
    switch (selectedEnemy) {
        case 0:
            co1Wireframe = false;
            co1Scale = 1.8f;
            co1RotZ = 0.0f;
            break;
        case 1:
            co2OrbitSpeedMult = 1.0f;
            co2Scale = 1.5f;
            co2PulseActive = true;
            break;
        case 2:
            co3TargetX = 720;
            co3TargetY = 480;
            co3ShieldRadius = 45;
            rasterSentinel.setShieldRadius(co3ShieldRadius);
            rasterSentinel.setTarget(co3TargetX, co3TargetY);
            break;
        case 3:
            co4UsePerspective = true;
            co4CustomZ = -25.0f;
            co4AutoDive = true;
            co4CameraPitch = 0.0f;
            warpCruiser.setZDepth(co4CustomZ);
            break;
        case 4:
            co5SkimmerOffsetX = 0.0f;
            co5SkimmerOffsetY = 0.0f;
            co5PolygonMode = false;
            co5WindowSize = 0.6f;
            boundarySkimmer.setWindowSize(co5WindowSize);
            break;
        case 5:
            co6GouraudShading = true;
            co6TextureActive = true;
            co6Shininess = 48.0f;
            co6LightAngle = 45.0f;
            co6ModelRotX = 15.0f;
            co6ModelRotY = 0.0f;
            phongCruiser.setShininess(co6Shininess);
            phongCruiser.setSmoothShading(co6GouraudShading);
            phongCruiser.setTextureActive(co6TextureActive);
            phongCruiser.setLightAngle(co6LightAngle);
            break;
    }
}

void EnemyDemoScene::update(float dt) {
    animTime += dt;

    switch (selectedEnemy) {
        case 0:
            // Subtly hover
            break;
        case 1:
            orbitalRotor.update(dt * co2OrbitSpeedMult);
            break;
        case 2:
            rasterSentinel.update(dt);
            rasterSentinel.setShieldRadius(co3ShieldRadius);
            rasterSentinel.setTarget(co3TargetX, co3TargetY);
            break;
        case 3:
            if (co4AutoDive) {
                warpCruiser.update(dt);
                co4CustomZ = warpCruiser.getZDepth();
            } else {
                warpCruiser.setZDepth(co4CustomZ);
            }
            break;
        case 4:
            boundarySkimmer.update(dt);
            break;
        case 5:
            phongCruiser.update(dt);
            phongCruiser.setShininess(co6Shininess);
            phongCruiser.setSmoothShading(co6GouraudShading);
            phongCruiser.setTextureActive(co6TextureActive);
            phongCruiser.setLightAngle(co6LightAngle);
            break;
    }
}

void EnemyDemoScene::handleKey(unsigned char key) {
    if (key >= '1' && key <= '6') {
        selectEnemy(key - '1');
        return;
    }

    if (key == 'r' || key == 'R') {
        resetCurrentDemonstration();
        return;
    }

    switch (selectedEnemy) {
        case 0: // CO1: Polygon Cruiser
            if (key == 'w' || key == 'W') {
                co1Wireframe = !co1Wireframe;
            }
            break;

        case 1: // CO2: Orbital Rotor
            if (key == 'p' || key == 'P') {
                co2PulseActive = !co2PulseActive;
            }
            break;

        case 2: // CO3: Raster Sentinel
            if (key == '+' || key == '=') {
                co3ShieldRadius = std::min(85, co3ShieldRadius + 4);
                rasterSentinel.setShieldRadius(co3ShieldRadius);
            } else if (key == '-' || key == '_') {
                co3ShieldRadius = std::max(15, co3ShieldRadius - 4);
                rasterSentinel.setShieldRadius(co3ShieldRadius);
            }
            break;

        case 3: // CO4: Warp Cruiser
            if (key == 'p' || key == 'P') {
                co4UsePerspective = !co4UsePerspective;
            } else if (key == ' ') {
                co4AutoDive = !co4AutoDive;
            }
            break;

        case 4: // CO5: Boundary Skimmer
            if (key == 'm' || key == 'M') {
                co5PolygonMode = !co5PolygonMode;
            } else if (key == '+' || key == '=') {
                co5WindowSize = std::min(0.9f, co5WindowSize + 0.05f);
                boundarySkimmer.setWindowSize(co5WindowSize);
            } else if (key == '-' || key == '_') {
                co5WindowSize = std::max(0.3f, co5WindowSize - 0.05f);
                boundarySkimmer.setWindowSize(co5WindowSize);
            }
            break;

        case 5: // CO6: Phong Cruiser
            if (key == 's' || key == 'S') {
                co6GouraudShading = !co6GouraudShading;
                phongCruiser.setSmoothShading(co6GouraudShading);
            } else if (key == 't' || key == 'T') {
                co6TextureActive = !co6TextureActive;
                phongCruiser.setTextureActive(co6TextureActive);
            } else if (key == '+' || key == '=') {
                co6Shininess = std::min(128.0f, co6Shininess + 8.0f);
                phongCruiser.setShininess(co6Shininess);
            } else if (key == '-' || key == '_') {
                co6Shininess = std::max(4.0f, co6Shininess - 8.0f);
                phongCruiser.setShininess(co6Shininess);
            }
            break;
    }
}

void EnemyDemoScene::handleSpecialKey(int key) {
    switch (selectedEnemy) {
        case 0: // CO1: Polygon Cruiser
            if (key == GLUT_KEY_LEFT)  co1RotZ += 5.0f;
            if (key == GLUT_KEY_RIGHT) co1RotZ -= 5.0f;
            if (key == GLUT_KEY_UP)    co1Scale = std::min(3.0f, co1Scale + 0.1f);
            if (key == GLUT_KEY_DOWN)  co1Scale = std::max(0.6f, co1Scale - 0.1f);
            break;

        case 1: // CO2: Orbital Rotor
            if (key == GLUT_KEY_LEFT)  co2OrbitSpeedMult = std::max(0.0f, co2OrbitSpeedMult - 0.25f);
            if (key == GLUT_KEY_RIGHT) co2OrbitSpeedMult = std::min(4.0f, co2OrbitSpeedMult + 0.25f);
            if (key == GLUT_KEY_UP)    co2Scale = std::min(2.5f, co2Scale + 0.1f);
            if (key == GLUT_KEY_DOWN)  co2Scale = std::max(0.8f, co2Scale - 0.1f);
            break;

        case 2: // CO3: Raster Sentinel
            if (key == GLUT_KEY_LEFT)  co3TargetX = std::max(50, co3TargetX - 15);
            if (key == GLUT_KEY_RIGHT) co3TargetX = std::min(980, co3TargetX + 15);
            if (key == GLUT_KEY_UP)    co3TargetY = std::min(680, co3TargetY + 15);
            if (key == GLUT_KEY_DOWN)  co3TargetY = std::max(50, co3TargetY - 15);
            rasterSentinel.setTarget(co3TargetX, co3TargetY);
            break;

        case 3: // CO4: Warp Cruiser
            if (key == GLUT_KEY_UP) {
                co4AutoDive = false;
                co4CustomZ = std::min(-8.0f, co4CustomZ + 1.5f);
                warpCruiser.setZDepth(co4CustomZ);
            }
            if (key == GLUT_KEY_DOWN) {
                co4AutoDive = false;
                co4CustomZ = std::max(-55.0f, co4CustomZ - 1.5f);
                warpCruiser.setZDepth(co4CustomZ);
            }
            if (key == GLUT_KEY_LEFT)  co4CameraPitch -= 5.0f;
            if (key == GLUT_KEY_RIGHT) co4CameraPitch += 5.0f;
            break;

        case 4: // CO5: Boundary Skimmer
            if (key == GLUT_KEY_LEFT)  co5SkimmerOffsetX -= 0.05f;
            if (key == GLUT_KEY_RIGHT) co5SkimmerOffsetX += 0.05f;
            if (key == GLUT_KEY_UP)    co5SkimmerOffsetY += 0.05f;
            if (key == GLUT_KEY_DOWN)  co5SkimmerOffsetY -= 0.05f;
            break;

        case 5: // CO6: Phong Cruiser
            if (key == GLUT_KEY_LEFT)  co6LightAngle += 10.0f;
            if (key == GLUT_KEY_RIGHT) co6LightAngle -= 10.0f;
            if (key == GLUT_KEY_UP)    co6ModelRotX += 5.0f;
            if (key == GLUT_KEY_DOWN)  co6ModelRotX -= 5.0f;
            break;
    }
}

void EnemyDemoScene::render(int width, int height) {
    glViewport(0, 0, width, height);
    glClearColor(0.04f, 0.06f, 0.10f, 1.0f); // Deep slate space background
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Disable unwanted global states
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_DEPTH_TEST);

    switch (selectedEnemy) {
        case 0: renderCO1PolygonCruiser(width, height); break;
        case 1: renderCO2OrbitalRotor(width, height); break;
        case 2: renderCO3RasterSentinel(width, height); break;
        case 3: renderCO4WarpCruiser(width, height); break;
        case 4: renderCO5BoundarySkimmer(width, height); break;
        case 5: renderCO6PhongCruiser(width, height); break;
    }

    // Always clean up state before HUD
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_DEPTH_TEST);

    // Render Overlay HUD
    renderHUD(width, height);
}

// -------------------------------------------------------------
// 1. CGL-CO1: Geometric Polygon Cruiser
// -------------------------------------------------------------
void EnemyDemoScene::renderCO1PolygonCruiser(int width, int height) {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    float aspect = static_cast<float>(width) / static_cast<float>(height);
    glOrtho(-1.5 * aspect, 1.5 * aspect, -1.5, 1.5, -1.0, 1.0);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // Background tactical grid
    glColor4f(0.12f, 0.20f, 0.32f, 0.5f);
    glLineWidth(1.0f);
    glBegin(GL_LINES);
    for (float x = -3.0f; x <= 3.0f; x += 0.5f) {
        glVertex2f(x, -2.0f); glVertex2f(x, 2.0f);
    }
    for (float y = -2.0f; y <= 2.0f; y += 0.5f) {
        glVertex2f(-3.0f, y); glVertex2f(3.0f, y);
    }
    glEnd();

    // Wireframe or Solid Mode
    if (co1Wireframe) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    } else {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    glRotatef(co1RotZ, 0.0f, 0.0f, 1.0f);
    polygonCruiser.render(0.0f, 0.0f, co1Scale);

    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
}

// -------------------------------------------------------------
// 2. CGL-CO2: Orbital Rotor Drone
// -------------------------------------------------------------
void EnemyDemoScene::renderCO2OrbitalRotor(int width, int height) {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    float aspect = static_cast<float>(width) / static_cast<float>(height);
    glOrtho(-2.2 * aspect, 2.2 * aspect, -2.2, 2.2, -1.0, 1.0);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // Orbit trail circle
    glColor4f(0.2f, 0.7f, 1.0f, 0.35f);
    glLineWidth(1.2f);
    glBegin(GL_LINE_LOOP);
    for (int i = 0; i < 64; ++i) {
        float theta = i * 2.0f * 3.14159265f / 64.0f;
        glVertex2f(std::cos(theta) * 0.95f * co2Scale, std::sin(theta) * 0.95f * co2Scale);
    }
    glEnd();

    orbitalRotor.render(0.0f, 0.0f, co2Scale);

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
}

// -------------------------------------------------------------
// 3. CGL-CO3: Bresenham Shield Sentinel
// -------------------------------------------------------------
void EnemyDemoScene::renderCO3RasterSentinel(int width, int height) {
    // RasterSentinel handles its own screen-space pixel projection (0 to W, 0 to H)
    rasterSentinel.setCenter(width / 2, height / 2);
    rasterSentinel.render(width, height);
}

// -------------------------------------------------------------
// 4. CGL-CO4: Depth-Diving Warp Cruiser
// -------------------------------------------------------------
void EnemyDemoScene::renderCO4WarpCruiser(int width, int height) {
    glEnable(GL_DEPTH_TEST);

    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    float aspect = static_cast<float>(width) / static_cast<float>(height);

    if (co4UsePerspective) {
        gluPerspective(60.0, aspect, 1.0, 100.0);
    } else {
        glOrtho(-15.0 * aspect, 15.0 * aspect, -15.0, 15.0, 1.0, 100.0);
    }

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    gluLookAt(0.0, 0.0, 0.0,
              0.0, 0.0, -1.0,
              0.0, 1.0, 0.0);

    // Reference Warp Tunnel Depth Rings at Z=-50, -40, -30, -20, -10
    for (float rz = -50.0f; rz <= -10.0f; rz += 10.0f) {
        glPushMatrix();
        glTranslatef(0.0f, 0.0f, rz);
        float alpha = 0.15f + (rz + 50.0f) / 40.0f * 0.25f;
        glColor4f(0.2f, 0.5f, 0.9f, alpha);
        glLineWidth(1.0f);
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < 32; ++i) {
            float rad = i * 2.0f * 3.14159265f / 32.0f;
            glVertex2f(std::cos(rad) * 6.0f, std::sin(rad) * 4.0f);
        }
        glEnd();
        glPopMatrix();
    }

    glRotatef(co4CameraPitch, 1.0f, 0.0f, 0.0f);

    warpCruiser.render(co4UsePerspective);

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);

    glDisable(GL_DEPTH_TEST);
}

// -------------------------------------------------------------
// 5. CGL-CO5: Boundary Phase Skimmer
// -------------------------------------------------------------
void EnemyDemoScene::renderCO5BoundarySkimmer(int width, int height) {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    float aspect = static_cast<float>(width) / static_cast<float>(height);
    glOrtho(-1.3 * aspect, 1.3 * aspect, -1.3, 1.3, -1.0, 1.0);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // Arena background crosshairs
    glColor4f(0.2f, 0.25f, 0.35f, 0.4f);
    glLineWidth(1.0f);
    glBegin(GL_LINES);
    glVertex2f(-2.0f, 0.0f); glVertex2f(2.0f, 0.0f);
    glVertex2f(0.0f, -2.0f); glVertex2f(0.0f, 2.0f);
    glEnd();

    boundarySkimmer.render(width, height);

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
}

// -------------------------------------------------------------
// 6. CGL-CO6: Specular Phong Cruiser
// -------------------------------------------------------------
void EnemyDemoScene::renderCO6PhongCruiser(int width, int height) {
    glEnable(GL_DEPTH_TEST);

    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();
    float aspect = static_cast<float>(width) / static_cast<float>(height);
    gluPerspective(45.0, aspect, 0.5, 50.0);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    gluLookAt(0.0, 1.2, 3.8,
              0.0, 0.0, 0.0,
              0.0, 1.0, 0.0);

    // Apply interactive model pitch and yaw
    glRotatef(co6ModelRotX, 1.0f, 0.0f, 0.0f);
    glRotatef(co6ModelRotY, 0.0f, 1.0f, 0.0f);

    phongCruiser.render();

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);

    glDisable(GL_DEPTH_TEST);
}

// -------------------------------------------------------------
// HUD Overlay System
// -------------------------------------------------------------
void EnemyDemoScene::renderHUD(int width, int height) {
    beginHUD(width, height);

    // 1. Top Navigation Bar Container
    glColor4f(0.05f, 0.08f, 0.15f, 0.88f);
    glBegin(GL_QUADS);
    glVertex2f(0.0f, height - 105.0f);
    glVertex2f(width, height - 105.0f);
    glVertex2f(width, height);
    glVertex2f(0.0f, height);
    glEnd();

    // Top Bar Bottom Accent Line
    glColor3f(0.0f, 0.8f, 1.0f);
    glLineWidth(2.0f);
    glBegin(GL_LINES);
    glVertex2f(0.0f, height - 105.0f);
    glVertex2f(width, height - 105.0f);
    glEnd();

    // Tab Bar: 6 Enemy Tabs
    const char* tabs[6] = {
        "[1] CO1: Polygon Cruiser",
        "[2] CO2: Orbital Rotor",
        "[3] CO3: Raster Sentinel",
        "[4] CO4: Warp Cruiser",
        "[5] CO5: Boundary Skimmer",
        "[6] CO6: Phong Cruiser"
    };

    float tabWidth = static_cast<float>(width) / 6.0f;
    for (int i = 0; i < 6; ++i) {
        float tx = i * tabWidth;
        if (i == selectedEnemy) {
            // Highlight active tab
            glColor4f(0.10f, 0.35f, 0.65f, 0.9f);
            glBegin(GL_QUADS);
            glVertex2f(tx + 4.0f, height - 32.0f);
            glVertex2f(tx + tabWidth - 4.0f, height - 32.0f);
            glVertex2f(tx + tabWidth - 4.0f, height - 6.0f);
            glVertex2f(tx + 4.0f, height - 6.0f);
            glEnd();

            glColor3f(1.0f, 0.9f, 0.2f); // Gold active text
        } else {
            glColor3f(0.65f, 0.75f, 0.85f); // Soft gray inactive text
        }
        renderBitmapString(tx + 12.0f, height - 22.0f, GLUT_BITMAP_HELVETICA_12, tabs[i]);
    }

    // Dynamic Title & Syllabus Details per Enemy
    std::string title;
    std::string concept;
    std::string mathParams;
    std::string controlsGuide;

    switch (selectedEnemy) {
        case 0:
            title = "CGL-CO1: Geometric Polygon Cruiser";
            concept = "Syllabus Concept: OpenGL Graphics Primitives & Geometric Modeling";
            mathParams = co1Wireframe ? "Mode: WIREFRAME (glPolygonMode GL_LINE) | Winding: Strict CCW | Shoelace Area = +0.0765"
                                      : "Mode: SOLID FILL (glPolygonMode GL_FILL) | Winding: Strict CCW | Shoelace Area = +0.0765";
            controlsGuide = "[W] Toggle Wireframe/Solid | [Arrows] Rotate / Scale Cruiser | [R] Reset";
            break;

        case 1: {
            title = "CGL-CO2: Orbital Rotor Drone";
            concept = "Syllabus Concept: 2D/3D Geometric Transformations & glPushMatrix / glPopMatrix Stack";
            std::ostringstream ss;
            ss << std::fixed << std::setprecision(1);
            ss << "Orbit Angle: " << orbitalRotor.getOrbitAngle() << " deg | Pod Spin: "
               << orbitalRotor.getSpinAngle() << " deg | Breathing Scale S(t): " << orbitalRotor.getPulseScale();
            mathParams = ss.str();
            controlsGuide = "[Left/Right] Orbit Speed Multiplier | [Up/Down] Scale Drone | [P] Toggle Pulse | [R] Reset";
            break;
        }

        case 2: {
            title = "CGL-CO3: Bresenham Shield Sentinel";
            concept = "Syllabus Concept: 2D Software Rasterization (Midpoint Circle & Bresenham Line Algorithms)";
            std::ostringstream ss;
            ss << "Shield Radius: " << co3ShieldRadius << " px (" << rasterSentinel.getShieldPixelCount()
               << " points, pk = 1-r) | Laser Target: (" << co3TargetX << ", " << co3TargetY
               << ") (" << rasterSentinel.getBeamPixelCount() << " integer pixels)";
            mathParams = ss.str();
            controlsGuide = "[Arrow Keys] Move Targeting Laser | [+/-] Shield Radius | [R] Reset";
            break;
        }

        case 3: {
            title = "CGL-CO4: Depth-Diving Warp Cruiser";
            concept = "Syllabus Concept: Projection and Viewing (Perspective Foreshortening vs Orthographic Invariance)";
            std::ostringstream ss;
            ss << std::fixed << std::setprecision(1);
            float ratio = 50.0f / std::max(5.0f, std::abs(co4CustomZ));
            ss << "Projection: " << (co4UsePerspective ? "PERSPECTIVE (gluPerspective 1/Z)" : "ORTHOGRAPHIC (glOrtho Parallel)")
               << " | Depth Z: " << co4CustomZ << " | Foreshortening Factor: " << (co4UsePerspective ? ratio : 1.0f) << "x";
            mathParams = ss.str();
            controlsGuide = "[P] Toggle Perspective/Ortho | [Up/Down] Adjust Depth Z | [Space] Auto-Dive | [R] Reset";
            break;
        }

        case 4: {
            title = "CGL-CO5: Boundary Phase Skimmer";
            concept = "Syllabus Concept: Cohen-Sutherland 4-bit Outcodes & Sutherland-Hodgman Polygon Clipping";
            Vec2 p1 = boundarySkimmer.getSkimmerPos();
            int c1 = 0;
            if (p1.y > co5WindowSize) c1 |= 8;
            if (p1.y < -co5WindowSize) c1 |= 4;
            if (p1.x > co5WindowSize) c1 |= 2;
            if (p1.x < -co5WindowSize) c1 |= 1;

            std::ostringstream ss;
            ss << "Window: [-" << co5WindowSize << ", " << co5WindowSize << "] | Skimmer Outcode: [";
            for (int b = 3; b >= 0; --b) ss << ((c1 >> b) & 1);
            ss << "] -> " << (c1 == 0 ? "TRIVIAL ACCEPT (Inside)" : "CLIPPED BY BOUNDARY");
            mathParams = ss.str();
            controlsGuide = "[Arrow Keys] Move Skimmer | [+/-] Resize Window | [M] Toggle Poly/Line | [R] Reset";
            break;
        }

        case 5: {
            title = "CGL-CO6: Specular Phong Cruiser";
            concept = "Syllabus Concept: Texture Mapping, Lighting, and Color Models (Phong / Blinn-Phong & Gouraud)";
            std::ostringstream ss;
            ss << "Shading: " << (co6GouraudShading ? "SMOOTH (Gouraud Interpolation)" : "FLAT (Constant Facet)")
               << " | Texture: " << (co6TextureActive ? "space_hull.bmp (256x256 24bpp)" : "OFF")
               << " | Specular Shininess n = " << static_cast<int>(co6Shininess)
               << " | Light Orbit: " << static_cast<int>(co6LightAngle) << " deg";
            mathParams = ss.str();
            controlsGuide = "[S] Shading Smooth/Flat | [T] Toggle Texture | [+/-] Shininess | [Arrows] Orbit Light | [R] Reset";
            break;
        }
    }

    // Render Titles
    glColor3f(1.0f, 1.0f, 1.0f);
    renderBitmapString(24.0f, height - 56.0f, GLUT_BITMAP_HELVETICA_18, title);

    glColor3f(0.0f, 0.85f, 1.0f);
    renderBitmapString(24.0f, height - 76.0f, GLUT_BITMAP_HELVETICA_12, concept);

    glColor3f(1.0f, 0.88f, 0.35f);
    renderBitmapString(24.0f, height - 96.0f, GLUT_BITMAP_HELVETICA_12, mathParams);

    // 2. Bottom Navigation Bar Container
    glColor4f(0.05f, 0.08f, 0.15f, 0.88f);
    glBegin(GL_QUADS);
    glVertex2f(0.0f, 0.0f);
    glVertex2f(width, 0.0f);
    glVertex2f(width, 36.0f);
    glVertex2f(0.0f, 36.0f);
    glEnd();

    glColor3f(0.0f, 0.8f, 1.0f);
    glLineWidth(1.5f);
    glBegin(GL_LINES);
    glVertex2f(0.0f, 36.0f);
    glVertex2f(width, 36.0f);
    glEnd();

    // Bottom Help Text
    std::string help = "[1-6] Select Enemy | [0 / ESC] Return to Main Demos | " + controlsGuide;
    glColor3f(0.85f, 0.95f, 1.0f);
    renderBitmapString(20.0f, 12.0f, GLUT_BITMAP_HELVETICA_12, help);

    endHUD();
}

} // namespace CGL
