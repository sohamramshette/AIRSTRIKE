#include "multiplayer_battle_scene.hpp"
#include "common/image_loader.hpp"
#include "common/font_renderer.hpp"
#include "algorithms/raster_algorithms.hpp"
#include "clipping/clipping_algorithms.hpp"

#include <cmath>
#include <iostream>

namespace CGL {

MultiplayerBattleScene::MultiplayerBattleScene()
    : viewMode(MPViewMode::ARENA_OVERVIEW),
      textureId(0),
      textureLoaded(false),
      textureBytes(0),
      lightingEnabled(true),
      wireframe(false),
      animTimer(0.0f) {
    // Player 1: Agile Interceptor (Left side)
    player1.x = -1.6f;
    player1.y = 0.0f;
    player1.z = 0.0f;
    player1.rotation = 15.0f;
    player1.bank = 0.0f;
    player1.scale = 0.75f;
    player1.shieldAlpha = 0.6f;
    player1.health = 100;
    player1.score = 4800;
    player1.shieldActive = true;

    // Player 2: Heavy Assault Cruiser (Right side)
    player2.x = 1.6f;
    player2.y = 0.0f;
    player2.z = 0.0f;
    player2.rotation = 195.0f;
    player2.bank = 0.0f;
    player2.scale = 0.85f;
    player2.shieldAlpha = 0.6f;
    player2.health = 85;
    player2.score = 3200;
    player2.shieldActive = true;
}

MultiplayerBattleScene::~MultiplayerBattleScene() {
    if (textureId != 0) {
        glDeleteTextures(1, &textureId);
        textureId = 0;
    }
}

void MultiplayerBattleScene::init() {
    int texW = 0, texH = 0;
    std::vector<unsigned char> texData;

    // Attempt loading space_hull texture from standard paths
    bool loaded = loadBMP("CGL/assets/textures/space_hull.bmp", texW, texH, texData);
    if (!loaded) {
        loaded = loadBMP("assets/textures/space_hull.bmp", texW, texH, texData);
    }

    if (loaded && !texData.empty()) {
        textureLoaded = true;
        textureBytes = texData.size();
        glGenTextures(1, &textureId);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texW, texH, 0, GL_RGB, GL_UNSIGNED_BYTE, texData.data());
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}

void MultiplayerBattleScene::setupLighting() {
    if (!lightingEnabled) {
        glDisable(GL_LIGHTING);
        glDisable(GL_LIGHT0);
        return;
    }

    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glShadeModel(GL_SMOOTH); // CGL-CO6: Gouraud smooth vertex color shading

    GLfloat lightPos[] = {0.0f, 2.0f, 4.0f, 1.0f};
    GLfloat lightAmbient[] = {0.2f, 0.2f, 0.25f, 1.0f};
    GLfloat lightDiffuse[] = {0.8f, 0.85f, 0.95f, 1.0f};
    GLfloat lightSpecular[] = {1.0f, 1.0f, 1.0f, 1.0f};

    glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
    glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbient);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, lightDiffuse);
    glLightfv(GL_LIGHT0, GL_SPECULAR, lightSpecular);
}

void MultiplayerBattleScene::setupProjection(int w, int h, int cameraPerspective) {
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    float aspect = static_cast<float>(w) / static_cast<float>(h > 0 ? h : 1);

    if (cameraPerspective == 0) {
        // Orthographic Wide-Angle Overview (CGL-CO4)
        float orthoSize = 2.8f;
        glOrtho(-orthoSize * aspect, orthoSize * aspect, -orthoSize, orthoSize, -10.0, 10.0);
    } else {
        // Perspective Split Camera View (CGL-CO4)
        gluPerspective(45.0, aspect, 0.1, 100.0);
    }

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
}

void MultiplayerBattleScene::renderArenaBoundary() {
    // Combat Envelope Boundary (CO5)
    glDisable(GL_LIGHTING);
    glLineWidth(2.0f);
    glColor4f(0.2f, 0.8f, 0.3f, 0.7f); // Neon combat grid green

    glBegin(GL_LINE_LOOP);
    glVertex3f(-2.5f, -1.8f, 0.0f);
    glVertex3f(2.5f, -1.8f, 0.0f);
    glVertex3f(2.5f, 1.8f, 0.0f);
    glVertex3f(-2.5f, 1.8f, 0.0f);
    glEnd();

    // Corner targeting marks
    glColor4f(1.0f, 0.8f, 0.0f, 0.8f);
    float mLen = 0.25f;
    glBegin(GL_LINES);
    // Top-left
    glVertex3f(-2.5f, 1.8f - mLen, 0.0f); glVertex3f(-2.5f, 1.8f, 0.0f);
    glVertex3f(-2.5f, 1.8f, 0.0f); glVertex3f(-2.5f + mLen, 1.8f, 0.0f);
    // Top-right
    glVertex3f(2.5f - mLen, 1.8f, 0.0f); glVertex3f(2.5f, 1.8f, 0.0f);
    glVertex3f(2.5f, 1.8f, 0.0f); glVertex3f(2.5f, 1.8f - mLen, 0.0f);
    // Bottom-left
    glVertex3f(-2.5f, -1.8f + mLen, 0.0f); glVertex3f(-2.5f, -1.8f, 0.0f);
    glVertex3f(-2.5f, -1.8f, 0.0f); glVertex3f(-2.5f + mLen, -1.8f, 0.0f);
    // Bottom-right
    glVertex3f(2.5f - mLen, -1.8f, 0.0f); glVertex3f(2.5f, -1.8f, 0.0f);
    glVertex3f(2.5f, -1.8f, 0.0f); glVertex3f(2.5f, -1.8f + mLen, 0.0f);
    glEnd();
}

void MultiplayerBattleScene::renderPlayer1Ship() {
    // CGL-CO1 & CO2: Cyan Agile Interceptor with delta wings
    glPushMatrix();
    glTranslatef(player1.x, player1.y, player1.z);
    glRotatef(player1.rotation, 0.0f, 0.0f, 1.0f);
    glRotatef(player1.bank, 1.0f, 0.0f, 0.0f);
    glScalef(player1.scale, player1.scale, player1.scale);

    if (textureId != 0 && !wireframe) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    // Material for Interceptor (CGL-CO6)
    GLfloat p1Ambient[] = {0.05f, 0.25f, 0.35f, 1.0f};
    GLfloat p1Diffuse[] = {0.0f, 0.85f, 1.0f, 1.0f};
    GLfloat p1Specular[] = {0.8f, 0.95f, 1.0f, 1.0f};
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, p1Ambient);
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, p1Diffuse);
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, p1Specular);
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 64.0f);

    glColor3f(0.0f, 0.85f, 1.0f);

    // Delta-wing hull (CCW winding)
    glBegin(GL_TRIANGLES);
    // Nose to left wingtip
    glNormal3f(0.0f, 0.0f, 1.0f);
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.65f, 0.05f);
    glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.45f, -0.45f, 0.0f);
    glTexCoord2f(0.5f, 0.2f); glVertex3f(0.0f, -0.25f, 0.08f);

    // Nose to right wingtip
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.65f, 0.05f);
    glTexCoord2f(0.5f, 0.2f); glVertex3f(0.0f, -0.25f, 0.08f);
    glTexCoord2f(1.0f, 0.0f); glVertex3f(0.45f, -0.45f, 0.0f);
    glEnd();

    // Twin Needle Cannons
    glDisable(GL_TEXTURE_2D);
    glColor3f(0.2f, 0.9f, 1.0f);
    glLineWidth(2.5f);
    glBegin(GL_LINES);
    glVertex3f(-0.25f, -0.1f, 0.02f); glVertex3f(-0.25f, 0.55f, 0.02f);
    glVertex3f(0.25f, -0.1f, 0.02f);  glVertex3f(0.25f, 0.55f, 0.02f);
    glEnd();

    // Cockpit Canopy (Gouraud shaded)
    glColor4f(0.9f, 1.0f, 1.0f, 0.9f);
    glBegin(GL_TRIANGLES);
    glVertex3f(0.0f, 0.35f, 0.12f);
    glVertex3f(-0.08f, 0.0f, 0.08f);
    glVertex3f(0.08f, 0.0f, 0.08f);
    glEnd();

    glPopMatrix();
}

void MultiplayerBattleScene::renderPlayer2Ship() {
    // CGL-CO1 & CO2: Purple/Magenta Heavy Assault Cruiser
    glPushMatrix();
    glTranslatef(player2.x, player2.y, player2.z);
    glRotatef(player2.rotation, 0.0f, 0.0f, 1.0f);
    glRotatef(player2.bank, 1.0f, 0.0f, 0.0f);
    glScalef(player2.scale, player2.scale, player2.scale);

    if (textureId != 0 && !wireframe) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    // Material for Heavy Cruiser (CGL-CO6)
    GLfloat p2Ambient[] = {0.3f, 0.05f, 0.35f, 1.0f};
    GLfloat p2Diffuse[] = {0.85f, 0.2f, 0.95f, 1.0f};
    GLfloat p2Specular[] = {1.0f, 0.6f, 1.0f, 1.0f};
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, p2Ambient);
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, p2Diffuse);
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, p2Specular);
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 32.0f);

    glColor3f(0.85f, 0.2f, 0.95f);

    // Heavy Diamond Chassis Hull
    glBegin(GL_QUADS);
    glNormal3f(0.0f, 0.0f, 1.0f);
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.7f, 0.06f);
    glTexCoord2f(0.0f, 0.5f); glVertex3f(-0.55f, 0.05f, 0.02f);
    glTexCoord2f(0.2f, 0.0f); glVertex3f(-0.35f, -0.55f, 0.02f);
    glTexCoord2f(0.5f, 0.2f); glVertex3f(0.0f, -0.35f, 0.08f);

    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.7f, 0.06f);
    glTexCoord2f(0.5f, 0.2f); glVertex3f(0.0f, -0.35f, 0.08f);
    glTexCoord2f(0.8f, 0.0f); glVertex3f(0.35f, -0.55f, 0.02f);
    glTexCoord2f(1.0f, 0.5f); glVertex3f(0.55f, 0.05f, 0.02f);
    glEnd();

    // Twin Plasma Nacelles
    glDisable(GL_TEXTURE_2D);
    glColor3f(1.0f, 0.3f, 0.8f);
    glBegin(GL_QUADS);
    glVertex3f(-0.5f, -0.45f, 0.04f); glVertex3f(-0.35f, -0.45f, 0.04f);
    glVertex3f(-0.35f, 0.15f, 0.04f);  glVertex3f(-0.5f, 0.15f, 0.04f);

    glVertex3f(0.35f, -0.45f, 0.04f); glVertex3f(0.5f, -0.45f, 0.04f);
    glVertex3f(0.5f, 0.15f, 0.04f);  glVertex3f(0.35f, 0.15f, 0.04f);
    glEnd();

    glPopMatrix();
}

void MultiplayerBattleScene::renderBresenhamBeams() {
    // CGL-CO3: Integer Bresenham Laser Trajectory from P1 towards P2
    glDisable(GL_LIGHTING);
    glPointSize(3.0f);
    glColor4f(0.0f, 1.0f, 0.9f, 0.85f);

    // Map float coordinates to integer screen/grid units for Bresenham demonstration
    int x1 = static_cast<int>(player1.x * 100.0f);
    int y1 = static_cast<int>(player1.y * 100.0f);
    int x2 = static_cast<int>(player2.x * 100.0f);
    int y2 = static_cast<int>(player2.y * 100.0f);

    std::vector<Point2D> laserPoints = RasterAlgorithmsDemo::bresenhamLine(x1, y1, x2, y2);

    glBegin(GL_POINTS);
    for (size_t i = 0; i < laserPoints.size(); i += 2) { // Alternate for pulsed laser effect
        glVertex3f(laserPoints[i].x / 100.0f, laserPoints[i].y / 100.0f, 0.05f);
    }
    glEnd();
}

void MultiplayerBattleScene::renderMidpointShields() {
    // CGL-CO3: Midpoint Circle Algorithm for Shield Barriers
    glDisable(GL_LIGHTING);
    glPointSize(2.0f);

    // Player 1 Shield (Cyan)
    if (player1.shieldActive) {
        glColor4f(0.0f, 0.8f, 1.0f, player1.shieldAlpha);
        int r1 = static_cast<int>(55 + 5 * std::sin(animTimer * 4.0f));
        std::vector<Point2D> circle1 = RasterAlgorithmsDemo::midpointCircle(0, 0, r1);

        glPushMatrix();
        glTranslatef(player1.x, player1.y, 0.05f);
        glBegin(GL_POINTS);
        for (size_t i = 0; i < circle1.size(); ++i) {
            glVertex3f(circle1[i].x / 100.0f, circle1[i].y / 100.0f, 0.0f);
        }
        glEnd();
        glPopMatrix();
    }

    // Player 2 Shield (Purple)
    if (player2.shieldActive) {
        glColor4f(0.9f, 0.3f, 1.0f, player2.shieldAlpha);
        int r2 = static_cast<int>(60 + 5 * std::cos(animTimer * 4.0f));
        std::vector<Point2D> circle2 = RasterAlgorithmsDemo::midpointCircle(0, 0, r2);

        glPushMatrix();
        glTranslatef(player2.x, player2.y, 0.05f);
        glBegin(GL_POINTS);
        for (size_t i = 0; i < circle2.size(); ++i) {
            glVertex3f(circle2[i].x / 100.0f, circle2[i].y / 100.0f, 0.0f);
        }
        glEnd();
        glPopMatrix();
    }
}

void MultiplayerBattleScene::renderClippedCombatBeams() {
    // CGL-CO5: Cohen-Sutherland Line Clipping against Arena Window
    RectWindow arenaWin(-2.5f, -1.8f, 2.5f, 1.8f);

    // Simulated high-energy sniper shot extending beyond boundary
    LineSegment incomingBeam(player1.x, player1.y, player1.x + 4.5f * std::cos(player1.rotation * 0.017453f),
                                                 player1.y + 4.5f * std::sin(player1.rotation * 0.017453f));

    LineSegment clippedBeam;
    int code1 = 0, code2 = 0;
    bool visible = ClippingAlgorithmsDemo::cohenSutherlandClip(incomingBeam, arenaWin, clippedBeam, code1, code2);

    glDisable(GL_LIGHTING);
    glLineWidth(2.0f);

    if (visible) {
        // Render inside (accepted) segment in high-intensity laser color
        glColor4f(1.0f, 0.9f, 0.1f, 0.9f);
        glBegin(GL_LINES);
        glVertex3f(clippedBeam.p1.x, clippedBeam.p1.y, 0.05f);
        glVertex3f(clippedBeam.p2.x, clippedBeam.p2.y, 0.05f);
        glEnd();

        // Render clipped-away exterior section in dashed/dim red (proving clipping math)
        glColor4f(1.0f, 0.2f, 0.2f, 0.35f);
        glBegin(GL_LINES);
        glVertex3f(clippedBeam.p2.x, clippedBeam.p2.y, 0.05f);
        glVertex3f(incomingBeam.p2.x, incomingBeam.p2.y, 0.05f);
        glEnd();
    }
}

void MultiplayerBattleScene::renderHUD(int screenWidth, int screenHeight, int viewportIndex) {
    (void)viewportIndex;
    beginHUD(screenWidth, screenHeight);

    // HUD Header Banner
    glColor4f(0.0f, 0.9f, 1.0f, 1.0f);
    renderBitmapString(15.0f, screenHeight - 25.0f, GLUT_BITMAP_HELVETICA_12, "AIRSTRIKE MULTIPLAYER COMBAT VISUALIZER (CGL-CO1 to CO6)");

    // Player 1 Stats
    glColor4f(0.2f, 0.9f, 1.0f, 1.0f);
    renderBitmapString(15.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12, "P1 [INTERCEPTOR]: SCORE 4800 | HP 100% | SHIELD: ACTIVE");

    // Player 2 Stats
    glColor4f(1.0f, 0.4f, 1.0f, 1.0f);
    renderBitmapString(15.0f, screenHeight - 75.0f, GLUT_BITMAP_HELVETICA_12, "P2 [CRUISER]:     SCORE 3200 | HP  85% | SHIELD: ACTIVE");

    // Controls Legend
    glColor4f(0.8f, 0.8f, 0.8f, 0.8f);
    renderBitmapString(15.0f, 20.0f, GLUT_BITMAP_HELVETICA_12, "Modes: [V] Toggle Arena/Split View | [L] Toggle Lighting | [W] Wireframe | [ESC] Main Menu");

    endHUD();
}

void MultiplayerBattleScene::render(int screenWidth, int screenHeight) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    if (viewMode == MPViewMode::ARENA_OVERVIEW) {
        // CGL-CO4 Mode A: Single wide-angle arena orthographic projection
        glViewport(0, 0, screenWidth, screenHeight);
        setupProjection(screenWidth, screenHeight, 0);
        setupLighting();

        renderArenaBoundary();
        renderPlayer1Ship();
        renderPlayer2Ship();
        renderBresenhamBeams();
        renderMidpointShields();
        renderClippedCombatBeams();
        renderHUD(screenWidth, screenHeight, 0);

    } else {
        // CGL-CO4 Mode B: Dual Split-Screen Viewports
        int halfW = screenWidth / 2;

        // Viewport 1: Player 1 Camera (Left)
        glViewport(0, 0, halfW, screenHeight);
        setupProjection(halfW, screenHeight, 1);
        gluLookAt(player1.x, player1.y - 2.5f, 3.2f,
                  player1.x, player1.y + 1.0f, 0.0f,
                  0.0f, 0.0f, 1.0f);
        setupLighting();
        renderArenaBoundary();
        renderPlayer1Ship();
        renderPlayer2Ship();
        renderBresenhamBeams();
        renderMidpointShields();

        // Viewport 2: Player 2 Camera (Right)
        glViewport(halfW, 0, halfW, screenHeight);
        setupProjection(halfW, screenHeight, 1);
        gluLookAt(player2.x, player2.y + 2.5f, 3.2f,
                  player2.x, player2.y - 1.0f, 0.0f,
                  0.0f, 0.0f, 1.0f);
        setupLighting();
        renderArenaBoundary();
        renderPlayer1Ship();
        renderPlayer2Ship();
        renderBresenhamBeams();
        renderMidpointShields();

        // Viewport divider bar
        glViewport(0, 0, screenWidth, screenHeight);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, screenWidth, 0, screenHeight, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glDisable(GL_LIGHTING);
        glColor4f(0.0f, 0.8f, 1.0f, 0.7f);
        glLineWidth(3.0f);
        glBegin(GL_LINES);
        glVertex2i(halfW, 0);
        glVertex2i(halfW, screenHeight);
        glEnd();

        renderHUD(screenWidth, screenHeight, 1);
    }
}

void MultiplayerBattleScene::update(float dt) {
    animTimer += dt;

    // Subtle combat maneuvering & banking oscillation (CGL-CO2)
    player1.y = 0.35f * std::sin(animTimer * 1.5f);
    player1.bank = 12.0f * std::sin(animTimer * 2.0f);

    player2.y = 0.30f * std::cos(animTimer * 1.2f);
    player2.bank = -10.0f * std::cos(animTimer * 1.8f);

    // Shield pulsating effect (CGL-CO3 & CO6)
    player1.shieldAlpha = 0.45f + 0.25f * std::sin(animTimer * 3.0f);
    player2.shieldAlpha = 0.45f + 0.25f * std::cos(animTimer * 3.0f);
}

void MultiplayerBattleScene::handleKey(unsigned char key) {
    switch (key) {
        case 'v': case 'V':
            toggleViewMode();
            break;
        case 'l': case 'L':
            lightingEnabled = !lightingEnabled;
            break;
        case 'w': case 'W':
            wireframe = !wireframe;
            glPolygonMode(GL_FRONT_AND_BACK, wireframe ? GL_LINE : GL_FILL);
            break;
        default:
            break;
    }
}

void MultiplayerBattleScene::toggleViewMode() {
    if (viewMode == MPViewMode::ARENA_OVERVIEW) {
        viewMode = MPViewMode::SPLIT_SCREEN;
        std::cout << "[CGL] Switched to Split-Screen Dual Viewports Mode" << std::endl;
    } else {
        viewMode = MPViewMode::ARENA_OVERVIEW;
        std::cout << "[CGL] Switched to Arena Wide-Angle Overview Mode" << std::endl;
    }
}

} // namespace CGL
