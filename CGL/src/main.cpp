#include "common/gl_common.h"
#include "common/font_renderer.hpp"
#include "common/screenshot.hpp"
#include "primitives/space_primitives.hpp"
#include "transformations/transform_demo.hpp"
#include "algorithms/raster_algorithms.hpp"
#include "projection/viewing_demo.hpp"
#include "clipping/clipping_algorithms.hpp"
#include "appearance/lighting_texture_demo.hpp"
#include "objects/space_scene.hpp"
#include "objects/enemy_demo_scene.hpp"
#include "../tests/test_cgl.hpp"

#include <iostream>
#include <string>
#include <memory>

namespace CGL {

// Active Window Dimensions
static int windowWidth = 1024;
static int windowHeight = 720;
static int currentScreen = 0; // 0: Integrated, 1-6: CO1-CO6, 7: Enemy Demonstrator
static bool screenshotMode = false;
static int screenshotStep = 0;

// Subsystem instances
static std::unique_ptr<SpacePrimitivesDemo> primitivesDemo;
static std::unique_ptr<TransformDemo> transformDemo;
static std::unique_ptr<RasterAlgorithmsDemo> rasterDemo;
static std::unique_ptr<ViewingDemo> viewingDemo;
static std::unique_ptr<ClippingAlgorithmsDemo> clippingDemo;
static std::unique_ptr<LightingTextureDemo> lightingDemo;
static std::unique_ptr<SpaceCombatScene> combatScene;
static std::unique_ptr<EnemyDemoScene> enemyDemoScene;

void initModules() {
    primitivesDemo = std::make_unique<SpacePrimitivesDemo>();
    primitivesDemo->init();

    transformDemo = std::make_unique<TransformDemo>();
    transformDemo->init();

    rasterDemo = std::make_unique<RasterAlgorithmsDemo>();
    rasterDemo->init();

    viewingDemo = std::make_unique<ViewingDemo>();
    viewingDemo->init();

    clippingDemo = std::make_unique<ClippingAlgorithmsDemo>();
    clippingDemo->init();

    lightingDemo = std::make_unique<LightingTextureDemo>();
    lightingDemo->init();

    combatScene = std::make_unique<SpaceCombatScene>();
    combatScene->init();

    enemyDemoScene = std::make_unique<EnemyDemoScene>();
    enemyDemoScene->init();
}

void displayCallback() {
    switch (currentScreen) {
        case 1: primitivesDemo->render(windowWidth, windowHeight); break;
        case 2: transformDemo->render(windowWidth, windowHeight); break;
        case 3: rasterDemo->render(windowWidth, windowHeight); break;
        case 4: viewingDemo->render(windowWidth, windowHeight); break;
        case 5: clippingDemo->render(windowWidth, windowHeight); break;
        case 6: lightingDemo->render(windowWidth, windowHeight); break;
        case 7: enemyDemoScene->render(windowWidth, windowHeight); break;
        case 0:
        default:
            combatScene->render(windowWidth, windowHeight);
            break;
    }

    glutSwapBuffers();
}

void reshapeCallback(int w, int h) {
    windowWidth = (w > 0) ? w : 1;
    windowHeight = (h > 0) ? h : 1;
    glViewport(0, 0, windowWidth, windowHeight);
}

void timerCallback(int value) {
    (void)value;
    float dt = 0.016f; // ~60 FPS

    if (screenshotMode) {
        // Step-by-step automatic capture of all screens
        switch (screenshotStep) {
            case 0:
                currentScreen = 1;
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_co1_primitives.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 1:
                currentScreen = 2;
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_co2_transformations.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 2:
                currentScreen = 3;
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_co3_algorithms.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 3:
                currentScreen = 4;
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_co4_projection.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 4:
                currentScreen = 5;
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_co5_clipping.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 5:
                currentScreen = 6;
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_co6_appearance.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 6:
                currentScreen = 0;
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_integrated_scene.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 7:
                currentScreen = 7;
                enemyDemoScene->selectEnemy(0);
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_enemy1_polygon_cruiser.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 8:
                currentScreen = 7;
                enemyDemoScene->selectEnemy(1);
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_enemy2_orbital_rotor.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 9:
                currentScreen = 7;
                enemyDemoScene->selectEnemy(2);
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_enemy3_raster_sentinel.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 10:
                currentScreen = 7;
                enemyDemoScene->selectEnemy(3);
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_enemy4_warp_cruiser.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 11:
                currentScreen = 7;
                enemyDemoScene->selectEnemy(4);
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_enemy5_boundary_skimmer.bmp", windowWidth, windowHeight);
                screenshotStep++;
                glutTimerFunc(100, timerCallback, 0);
                return;
            case 12:
                currentScreen = 7;
                enemyDemoScene->selectEnemy(5);
                displayCallback();
                captureScreenshot("docs/screenshots/CGL/cgl_enemy6_phong_cruiser.bmp", windowWidth, windowHeight);
                std::cout << "[SUCCESS] All 13 CGL screenshots (Base + 6 Enemies) successfully generated!" << std::endl;
                std::exit(0);
        }
    }

    // Normal interactive update
    switch (currentScreen) {
        case 2: transformDemo->update(dt); break;
        case 3: rasterDemo->update(dt); break;
        case 4: viewingDemo->update(dt); break;
        case 5: clippingDemo->update(dt); break;
        case 6: lightingDemo->update(dt); break;
        case 7: enemyDemoScene->update(dt); break;
        case 0: combatScene->update(dt); break;
        default: break;
    }

    glutPostRedisplay();
    glutTimerFunc(16, timerCallback, 0);
}

void keyboardCallback(unsigned char key, int x, int y) {
    (void)x; (void)y;

    // Dedicated input routing for Screen 7: Enemy Demonstrator
    if (currentScreen == 7) {
        if (key == 27 || key == '0') {
            currentScreen = 0; // Return to Main Menu / Integrated Scene
            return;
        }
        if (key >= '1' && key <= '6') {
            enemyDemoScene->selectEnemy(key - '1');
            return;
        }
        if (key == 'r' || key == 'R') {
            enemyDemoScene->resetCurrentDemonstration();
            return;
        }
        enemyDemoScene->handleKey(key);
        return;
    }

    switch (key) {
        case 27: // ESC
            std::cout << "[INFO] Exiting AIRSTRIKE CGL..." << std::endl;
            std::exit(0);
            break;

        // Screen selection keys
        case '0': currentScreen = 0; break;
        case '1': currentScreen = 1; break;
        case '2': currentScreen = 2; break;
        case '3': currentScreen = 3; break;
        case '4': currentScreen = 4; break;
        case '5': currentScreen = 5; break;
        case '6': currentScreen = 6; break;

        // Contextual Controls
        case 'w': case 'W':
            if (currentScreen == 1) primitivesDemo->toggleWireframe();
            else if (currentScreen == 2) transformDemo->translate(0.0f, 0.1f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(3.0f, 0.0f);
            else if (currentScreen == 5) clippingDemo->moveClipWindow(0.0f, 0.05f);
            else if (currentScreen == 0) combatScene->movePlayer(0.0f, 0.25f);
            break;

        case 's': case 'S':
            if (currentScreen == 2) transformDemo->translate(0.0f, -0.1f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(-3.0f, 0.0f);
            else if (currentScreen == 5) clippingDemo->moveClipWindow(0.0f, -0.05f);
            else if (currentScreen == 6) lightingDemo->toggleShading();
            else if (currentScreen == 0) combatScene->toggleShield();
            break;

        case 'a': case 'A':
            if (currentScreen == 2) transformDemo->translate(-0.1f, 0.0f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(0.0f, -4.0f);
            else if (currentScreen == 5) clippingDemo->moveClipWindow(-0.05f, 0.0f);
            else if (currentScreen == 0) combatScene->movePlayer(-0.25f, 0.0f);
            break;

        case 'd': case 'D':
            if (currentScreen == 2) transformDemo->translate(0.1f, 0.0f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(0.0f, 4.0f);
            else if (currentScreen == 5) clippingDemo->moveClipWindow(0.05f, 0.0f);
            else if (currentScreen == 0) combatScene->movePlayer(0.25f, 0.0f);
            break;

        case 'q': case 'Q':
            if (currentScreen == 2) transformDemo->rotate(5.0f);
            break;

        case 'e': case 'E':
            if (currentScreen == 2) transformDemo->rotate(-5.0f);
            else currentScreen = 7;
            break;

        case '+': case '=':
            if (currentScreen == 2) transformDemo->scale(0.1f);
            else if (currentScreen == 3) rasterDemo->adjustParameter(5);
            else if (currentScreen == 4) viewingDemo->zoomCamera(-1.0f);
            else if (currentScreen == 5) clippingDemo->resizeClipWindow(0.05f);
            else if (currentScreen == 6) lightingDemo->adjustShininess(8.0f);
            break;

        case '-': case '_':
            if (currentScreen == 2) transformDemo->scale(-0.1f);
            else if (currentScreen == 3) rasterDemo->adjustParameter(-5);
            else if (currentScreen == 4) viewingDemo->zoomCamera(1.0f);
            else if (currentScreen == 5) clippingDemo->resizeClipWindow(-0.05f);
            else if (currentScreen == 6) lightingDemo->adjustShininess(-8.0f);
            break;

        case 'm': case 'M':
            if (currentScreen == 3) rasterDemo->cycleMode();
            else if (currentScreen == 5) clippingDemo->cycleMode();
            break;

        case 'p': case 'P':
            if (currentScreen == 2) transformDemo->togglePivotMode();
            else if (currentScreen == 4) viewingDemo->toggleProjectionMode();
            break;

        case 'o': case 'O':
            if (currentScreen == 2) transformDemo->toggleOrder();
            break;

        case 'h': case 'H':
            if (currentScreen == 2) transformDemo->toggleHierarchical();
            break;

        case 'l': case 'L':
            if (currentScreen == 6) lightingDemo->toggleLighting();
            break;

        case 't': case 'T':
            if (currentScreen == 6) lightingDemo->toggleTexture();
            break;

        case 'u': case 'U':
            if (currentScreen == 6) lightingDemo->toggleTextureSource();
            break;

        case 'k': case 'K':
            if (currentScreen == 6) lightingDemo->toggleWrapping();
            break;

        case 'f': case 'F':
            if (currentScreen == 2) transformDemo->toggleReflection();
            else if (currentScreen == 6) lightingDemo->toggleFiltering();
            break;

        case '7':
            if (currentScreen == 6) lightingDemo->toggleAmbient();
            else currentScreen = 7;
            break;

        case '8':
            if (currentScreen == 6) lightingDemo->toggleDiffuse();
            break;

        case '9':
            if (currentScreen == 6) lightingDemo->toggleSpecular();
            break;

        case ' ': // SPACE
            if (currentScreen == 0) combatScene->fireLaser();
            else if (currentScreen == 5) clippingDemo->toggleAnimate();
            break;

        case 'r': case 'R':
            if (currentScreen == 2) transformDemo->reset();
            else if (currentScreen == 4) viewingDemo->reset();
            else if (currentScreen == 5) clippingDemo->reset();
            else if (currentScreen == 6) lightingDemo->reset();
            else if (currentScreen == 0) combatScene->reset();
            break;

        default:
            break;
    }
}

void specialKeyCallback(int key, int x, int y) {
    (void)x; (void)y;

    if (currentScreen == 7) {
        enemyDemoScene->handleSpecialKey(key);
        return;
    }

    switch (key) {
        case GLUT_KEY_UP:
            if (currentScreen == 0) combatScene->movePlayer(0.0f, 0.25f);
            else if (currentScreen == 2) transformDemo->translate(0.0f, 0.1f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(3.0f, 0.0f);
            break;
        case GLUT_KEY_DOWN:
            if (currentScreen == 0) combatScene->movePlayer(0.0f, -0.25f);
            else if (currentScreen == 2) transformDemo->translate(0.0f, -0.1f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(-3.0f, 0.0f);
            break;
        case GLUT_KEY_LEFT:
            if (currentScreen == 0) combatScene->movePlayer(-0.25f, 0.0f);
            else if (currentScreen == 2) transformDemo->translate(-0.1f, 0.0f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(0.0f, -4.0f);
            break;
        case GLUT_KEY_RIGHT:
            if (currentScreen == 0) combatScene->movePlayer(0.25f, 0.0f);
            else if (currentScreen == 2) transformDemo->translate(0.1f, 0.0f);
            else if (currentScreen == 4) viewingDemo->orbitCamera(0.0f, 4.0f);
            break;
    }
}

} // namespace CGL

int main(int argc, char** argv) {
    // Check command line arguments
    for (int i = 1; i < argc; ++i) {
        std::string arg = argv[i];
        if (arg == "--test" || arg == "-t" || arg == "test" || arg == "--all") {
            return CGL::runAllCGLTests();
        }
        if (arg == "--screenshot" || arg == "-s" || arg == "screenshot") {
            CGL::screenshotMode = true;
        }
    }

    std::cout << "==========================================================" << std::endl;
    std::cout << "  AIRSTRIKE – Computer Graphics Laboratory (CGL) [OpenGL] " << std::endl;
    std::cout << "==========================================================" << std::endl;
    std::cout << "Initializing GLUT Window..." << std::endl;

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(CGL::windowWidth, CGL::windowHeight);
    glutInitWindowPosition(100, 80);
    glutCreateWindow("AIRSTRIKE – Computer Graphics Laboratory (CGL) [OpenGL/GLUT]");

    CGL::initModules();

    glutDisplayFunc(CGL::displayCallback);
    glutReshapeFunc(CGL::reshapeCallback);
    glutKeyboardFunc(CGL::keyboardCallback);
    glutSpecialFunc(CGL::specialKeyCallback);
    glutTimerFunc(16, CGL::timerCallback, 0);

    if (CGL::screenshotMode) {
        std::cout << "[INFO] Screenshot capture mode engaged. Rendering all CO scenes and Enemy Demos..." << std::endl;
    } else {
        std::cout << "[INFO] Entering interactive OpenGL event loop." << std::endl;
        std::cout << "Controls: [1-6] CO Demos | [7 / E] CGL Enemy Demonstrator | [0] Combat Scene | [ESC] Exit" << std::endl;
    }

    glutMainLoop();
    return 0;
}
