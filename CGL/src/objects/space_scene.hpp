// ==============================================================================
// File      : CGL/src/objects/space_scene.hpp
// Subject   : Scene | Integrated CO1-CO6 Space Combat Scene
// Topics    : [TOPIC: Scene Orchestration] [TOPIC: CO1-CO6 Integration] [TOPIC: Orthographic Radar PIP] [TOPIC: HUD Font Renderer]
// Game Role : Declares the integrated AIRSTRIKE combat scene uniting all six CGL Course Outcomes.
// ==============================================================================

#ifndef SPACE_SCENE_HPP
#define SPACE_SCENE_HPP

#include "common/gl_common.h"
#include "clipping/clipping_algorithms.hpp"
#include <vector>
#include <string>

namespace CGL {

// [TOPIC: 3D Asteroid Entity]
// Purpose: Stores world position, rotation axis/speed, and radius for each spinning asteroid.
// Game Role: Populates the 3D lit asteroid belt in SpaceCombatScene.
struct AsteroidEntity {
    Vec3 pos;
    Vec3 rotAxis;
    float rotSpeed;
    float currentRot;
    float radius;
};

// [TOPIC: Scene Orchestration / CO1-CO6 Integration / Orthographic Radar PIP / HUD Font Renderer]
// Purpose: Integrates CO1 primitives, CO2 hierarchical drones, CO3 shields, CO4 PIP radar, CO5 clipped lasers, and CO6 lit/textured bodies.
// Game Role: Default Screen 0 interactive combat showcase for AIRSTRIKE CGL.
class SpaceCombatScene {
public:
    SpaceCombatScene();
    ~SpaceCombatScene();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // Interactive gameplay controls
    void movePlayer(float dx, float dy);
    void fireLaser();
    void toggleShield();
    void reset();

    // Inspection getters
    float getPlayerX() const { return playerX; }
    float getPlayerY() const { return playerY; }
    bool isShieldActive() const { return shieldActive; }
    int getAsteroidCount() const { return static_cast<int>(asteroids.size()); }

private:
    float playerX, playerY;
    float playerRoll;
    bool shieldActive;
    float shieldEnergy;
    float laserTimer;
    bool laserActive;

    float animTime;
    float droneOrbitAngle;

    GLuint planetTexId;
    std::vector<AsteroidEntity> asteroids;
    RectWindow combatBounds;

    // [TOPIC: Scene Subsystem Renderers]
    // Purpose: Helper methods separating procedural textures, skybox/planet, asteroids, player+drones, clipped lasers, and PIP radar.
    // Game Role: Structures the integrated CO1-CO6 render pass cleanly.
    void createProceduralTextures();
    void renderSkyboxStars();
    void renderAsteroids();
    void renderPlayerCraft();
    void renderEscortDrone(float offsetAngle);
    void renderLaserBeams();
    void renderEnergyShield();
    void renderTacticalRadarPIP(int screenWidth, int screenHeight);
};

} // namespace CGL

#endif // SPACE_SCENE_HPP

