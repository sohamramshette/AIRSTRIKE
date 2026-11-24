// ==============================================================================
// File      : CGL/src/transformations/orbital_rotor.hpp
// Subject   : CGL-CO2 | OrbitalRotor Hierarchical Drone
// Topics    : [TOPIC: Homogeneous Coordinates] [TOPIC: Affine Matrices (T*R != R*T)] [TOPIC: glPushMatrix/glPopMatrix] [TOPIC: OrbitalRotor]
// Game Role : Declares the OrbitalRotor enemy combining a pulsing core and two hierarchical satellite pods.
// ==============================================================================

#ifndef ORBITAL_ROTOR_HPP
#define ORBITAL_ROTOR_HPP

#include "common/gl_common.h"
#include <vector>

namespace CGL {

// [TOPIC: OrbitalRotor / Homogeneous Coordinates / glPushMatrix/glPopMatrix]
// Purpose: Demonstrates hierarchical matrix stacks and non-commutative affine transforms (T*R != R*T).
// Game Role: Represents CGL-CO2 enemy type [2] in AIRSTRIKE's Enemy Demonstrator and unit suite.
class OrbitalRotorDrone {
public:
    OrbitalRotorDrone();
    ~OrbitalRotorDrone() = default;

    void init();
    void update(float dt);
    void render(float posX, float posY, float baseScale = 1.0f);

    // [TOPIC: Homogeneous Coordinates / Affine Matrices]
    // Purpose: Column-major 4x4 translation, Z-rotation, scale, multiply, and point-transform utilities.
    // Game Role: Enables deterministic matrix math verification for OrbitalRotor unit tests.
    static void getTranslationMatrix(float tx, float ty, float tz, float m[16]);
    static void getRotationZMatrix(float angleDeg, float m[16]);
    static void getScaleMatrix(float sx, float sy, float sz, float m[16]);
    static void multiplyMatrices(const float a[16], const float b[16], float out[16]);
    static Vec3 transformPoint(const float m[16], const Vec3& p);

    // [TOPIC: Orbital Kinematics]
    // Purpose: Resolves world-space coordinates for opposing rotor pods along orbitAngleDeg.
    // Game Role: Validates parent-to-child hierarchical position transforms for Pod 0 and Pod 1.
    Vec3 computeRotorWorldPosition(int rotorIndex, float parentX, float parentY,
                                   float orbitAngleDeg, float orbitRadius) const;

    float getOrbitAngle() const { return orbitAngle; }
    float getSpinAngle() const { return spinAngle; }
    float getPulseScale() const { return pulseScale; }

    // [TOPIC: Affine Matrices (T*R != R*T)]
    // Purpose: Verifies that (T * R) * p differs from (R * T) * p for non-zero offsets and angles.
    // Game Role: Proves non-commutativity of homogeneous matrix composition in CO2 tests.
    bool verifyNonCommutativity(float tx, float ty, float angleDeg) const;

private:
    float orbitAngle;    // Revolution around parent center
    float spinAngle;     // Local rotation of rotor blades
    float pulseTime;     // Breathing scale timer
    float pulseScale;    // Current scale factor S(t)
    float orbitRadius;   // Distance of rotors from central core

    // [TOPIC: glPushMatrix/glPopMatrix]
    // Purpose: Isolated renderers for the pulsing octagonal core and spinning hexagonal rotor pods.
    // Game Role: Sub-parts composed inside OrbitalRotorDrone's hierarchical matrix tree.
    void renderCoreHub(float scale);
    void renderRotorPod(float podRadius, float spinDeg);
};

} // namespace CGL

#endif // ORBITAL_ROTOR_HPP
