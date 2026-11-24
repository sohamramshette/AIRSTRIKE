// ==============================================================================
// File      : CGL/src/objects/enemy_demo_scene.hpp
// Subject   : Scene | Interactive CO1-CO6 Enemy Showcase
// Topics    : [TOPIC: Scene Orchestration] [TOPIC: PolygonCruiser] [TOPIC: OrbitalRotor] [TOPIC: RasterSentinel] [TOPIC: WarpCruiser] [TOPIC: BoundarySkimmer] [TOPIC: PhongCruiser]
// Game Role : Declares Screen 7's tabbed inspector switching among all six CO-specific AIRSTRIKE enemy craft.
// ==============================================================================

#ifndef ENEMY_DEMO_SCENE_HPP
#define ENEMY_DEMO_SCENE_HPP

#include "common/gl_common.h"
#include "primitives/polygon_cruiser.hpp"
#include "transformations/orbital_rotor.hpp"
#include "algorithms/raster_sentinel.hpp"
#include "projection/warp_cruiser.hpp"
#include "clipping/boundary_skimmer.hpp"
#include "appearance/phong_cruiser.hpp"

namespace CGL {

// [TOPIC: Scene Orchestration / CO1-CO6 Enemy Demonstrator / HUD Font Renderer]
// Purpose: Hosts all six CGL enemy instances, switches tabs [1]-[6], prevents OpenGL state leakage, and draws HUD telemetry.
// Game Role: Interactive enemy inspection gallery (Screen 7) in AIRSTRIKE CGL.
class EnemyDemoScene {
public:
    EnemyDemoScene();
    ~EnemyDemoScene() = default;

    void init();
    void update(float dt);
    void render(int width, int height);

    void selectEnemy(int index);
    int getSelectedEnemy() const { return selectedEnemy; }

    void handleKey(unsigned char key);
    void handleSpecialKey(int key);
    void resetCurrentDemonstration();

private:
    int selectedEnemy; // 0 to 5 for CGL-CO1 through CGL-CO6
    float animTime;

    // Six CGL Enemy Instances
    PolygonCruiser    polygonCruiser;    // CGL-CO1: OpenGL Primitives
    OrbitalRotorDrone orbitalRotor;      // CGL-CO2: Geometric Transformations
    RasterSentinel    rasterSentinel;    // CGL-CO3: Software Rasterization
    WarpCruiser       warpCruiser;       // CGL-CO4: Projection & Viewing
    BoundarySkimmer   boundarySkimmer;   // CGL-CO5: Clipping Algorithms
    PhongCruiser      phongCruiser;      // CGL-CO6: Lighting, Texture & Color

    // Interactive Inspection Parameters per Enemy:
    // CO1 (Polygon Cruiser)
    bool  co1Wireframe;
    float co1Scale;
    float co1RotZ;

    // CO2 (Orbital Rotor)
    float co2OrbitSpeedMult;
    float co2Scale;
    bool  co2PulseActive;

    // CO3 (Raster Sentinel)
    int   co3TargetX;
    int   co3TargetY;
    int   co3ShieldRadius;

    // CO4 (Warp Cruiser)
    bool  co4UsePerspective;
    float co4CustomZ;
    bool  co4AutoDive;
    float co4CameraPitch;

    // CO5 (Boundary Skimmer)
    float co5SkimmerOffsetX;
    float co5SkimmerOffsetY;
    bool  co5PolygonMode;
    float co5WindowSize;

    // CO6 (Phong Cruiser)
    bool  co6GouraudShading;
    bool  co6TextureActive;
    float co6Shininess;
    float co6LightAngle;
    float co6ModelRotX;
    float co6ModelRotY;

    // [TOPIC: Isolated Per-CO Enemy Renderers]
    // Purpose: Configures dedicated projection, camera, and OpenGL state for each enemy tab.
    // Game Role: Prevents state leakage across 2D primitive, pixel-space, and 3D lit enemies.
    void renderCO1PolygonCruiser(int width, int height);
    void renderCO2OrbitalRotor(int width, int height);
    void renderCO3RasterSentinel(int width, int height);
    void renderCO4WarpCruiser(int width, int height);
    void renderCO5BoundarySkimmer(int width, int height);
    void renderCO6PhongCruiser(int width, int height);

    // [TOPIC: HUD Font Renderer]
    // Purpose: Draws top navigation tabs, syllabus telemetry bar, and bottom control hints.
    // Game Role: Displays live parameters and CO annotations for the active enemy.
    void renderHUD(int width, int height);
};

} // namespace CGL

#endif // ENEMY_DEMO_SCENE_HPP
