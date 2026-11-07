#ifndef ORBITAL_ROTOR_HPP
#define ORBITAL_ROTOR_HPP

#include "common/gl_common.h"
#include <vector>

namespace CGL {

/**
 * @brief Orbital Rotor Drone Enemy (CGL-CO2).
 *
 * Demonstrates:
 * - Affine Transformations in Homogeneous Coordinates: T(tx,ty,tz), R(theta,ux,uy,uz), S(sx,sy,sz)
 * - Matrix Stack Architecture: Isolated hierarchical pods via glPushMatrix() and glPopMatrix()
 * - Non-commutative Transformation Sequencing: T * R != R * T
 * - Dynamic Orbital Kinematics: Revolving satellite pods inheriting parent translation
 */
class OrbitalRotorDrone {
public:
    OrbitalRotorDrone();
    ~OrbitalRotorDrone() = default;

    void init();
    void update(float dt);
    void render(float posX, float posY, float baseScale = 1.0f);

    // Matrix mathematics helpers (Pure C++ for automated unit testing)
    static void getTranslationMatrix(float tx, float ty, float tz, float m[16]);
    static void getRotationZMatrix(float angleDeg, float m[16]);
    static void getScaleMatrix(float sx, float sy, float sz, float m[16]);
    static void multiplyMatrices(const float a[16], const float b[16], float out[16]);
    static Vec3 transformPoint(const float m[16], const Vec3& p);

    // Kinematic & Position queries
    Vec3 computeRotorWorldPosition(int rotorIndex, float parentX, float parentY,
                                   float orbitAngleDeg, float orbitRadius) const;

    float getOrbitAngle() const { return orbitAngle; }
    float getSpinAngle() const { return spinAngle; }
    float getPulseScale() const { return pulseScale; }

    bool verifyNonCommutativity(float tx, float ty, float angleDeg) const;

private:
    float orbitAngle;    // Revolution around parent center
    float spinAngle;     // Local rotation of rotor blades
    float pulseTime;     // Breathing scale timer
    float pulseScale;    // Current scale factor S(t)
    float orbitRadius;   // Distance of rotors from central core

    void renderCoreHub(float scale);
    void renderRotorPod(float podRadius, float spinDeg);
};

} // namespace CGL

#endif // ORBITAL_ROTOR_HPP
