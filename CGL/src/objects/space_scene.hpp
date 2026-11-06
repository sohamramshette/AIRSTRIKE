#ifndef SPACE_SCENE_HPP
#define SPACE_SCENE_HPP

#include "common/gl_common.h"
#include "clipping/clipping_algorithms.hpp"
#include <vector>
#include <string>

namespace CGL {

struct AsteroidEntity {
    Vec3 pos;
    Vec3 rotAxis;
    float rotSpeed;
    float currentRot;
    float radius;
};

/**
 * @brief Integrated AirStrike 2D/3D Combat Scene.
 * Integrates all 6 CGL Course Outcomes:
 * - CO1: Complete suite of OpenGL primitives (Ship, Lasers, Shields, Station, Particles)
 * - CO2: Hierarchical transformations (Player ship, Escort drones, Matrix stack)
 * - CO3: Mathematical Bresenham laser trajectory & Midpoint circle shield
 * - CO4: Simultaneous 3D Perspective chase-cam and Orthographic PIP radar
 * - CO5: Cohen-Sutherland & Sutherland-Hodgman sector boundary clipping
 * - CO6: Dynamic Phong lighting and textured celestial planet
 */
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
