// ==============================================================================
// File      : CGL/src/transformations/orbital_rotor.cpp
// Subject   : CGL-CO2 | OrbitalRotor Hierarchical Drone
// Topics    : [TOPIC: Homogeneous Coordinates] [TOPIC: Affine Matrices (T*R != R*T)] [TOPIC: glPushMatrix/glPopMatrix] [TOPIC: OrbitalRotor]
// Game Role : Implements OrbitalRotor kinematics, T*R != R*T verification, and hierarchical matrix stack drawing.
// ==============================================================================

#include "orbital_rotor.hpp"
#include <cmath>

namespace CGL {

OrbitalRotorDrone::OrbitalRotorDrone()
    : orbitAngle(0.0f), spinAngle(0.0f), pulseTime(0.0f), pulseScale(1.0f), orbitRadius(0.35f) {
    init();
}

void OrbitalRotorDrone::init() {
    orbitAngle = 0.0f;
    spinAngle = 0.0f;
    pulseTime = 0.0f;
    pulseScale = 1.0f;
    orbitRadius = 0.35f;
}

// [TOPIC: OrbitalRotor Kinematics]
// Purpose: Steps orbital revolution, local pod spin, and sinusoidal core scale S(t).
// Game Role: Animates the OrbitalRotor drone's multi-axis motion each frame.
void OrbitalRotorDrone::update(float dt) {
    orbitAngle += 90.0f * dt;      // 90 deg/sec orbit
    if (orbitAngle >= 360.0f) orbitAngle -= 360.0f;

    spinAngle += 360.0f * dt;       // 360 deg/sec local spin
    if (spinAngle >= 360.0f) spinAngle -= 360.0f;

    pulseTime += dt * 3.0f;         // Breathing pulsation
    // WHY: Restricts breathing scale factor to [0.85, 1.15] around unit core size
    pulseScale = 1.0f + 0.15f * std::sin(pulseTime);
}

// [TOPIC: Homogeneous Coordinates]
// Purpose: Builds a 4x4 column-major translation matrix T(tx, ty, tz).
// Game Role: Pure C++ helper used to verify non-commutative affine sequencing.
void OrbitalRotorDrone::getTranslationMatrix(float tx, float ty, float tz, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    m[12] = tx;
    m[13] = ty;
    m[14] = tz;
}

// [TOPIC: Affine Matrices]
// Purpose: Builds a 4x4 column-major Z-axis rotation matrix Rz(angleDeg).
// Game Role: Models orbital and blade rotations analytically.
void OrbitalRotorDrone::getRotationZMatrix(float angleDeg, float m[16]) {
    float rad = angleDeg * static_cast<float>(M_PI) / 180.0f;
    float c = std::cos(rad);
    float s = std::sin(rad);
    for (int i = 0; i < 16; ++i) m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    m[0] = c;  m[1] = s;
    m[4] = -s; m[5] = c;
}

void OrbitalRotorDrone::getScaleMatrix(float sx, float sy, float sz, float m[16]) {
    for (int i = 0; i < 16; ++i) m[i] = 0.0f;
    m[0] = sx;
    m[5] = sy;
    m[10] = sz;
    m[15] = 1.0f;
}

// [TOPIC: Affine Matrices (T*R != R*T)]
// Purpose: Multiplies two column-major 4x4 matrices into a temporary buffer before writing out.
// Game Role: Enables safe matrix composition even if out aliases an operand.
void OrbitalRotorDrone::multiplyMatrices(const float a[16], const float b[16], float out[16]) {
    float res[16];
    for (int c = 0; c < 4; ++c) {
        for (int r = 0; r < 4; ++r) {
            float sum = 0.0f;
            for (int k = 0; k < 4; ++k) {
                // WHY: Column-major indexing accesses row r of column k in a, row k of column c in b
                sum += a[k * 4 + r] * b[c * 4 + k];
            }
            res[c * 4 + r] = sum;
        }
    }
    for (int i = 0; i < 16; ++i) out[i] = res[i];
}

// [TOPIC: Homogeneous Coordinates]
// Purpose: Multiplies homogeneous 4x4 matrix m by affine point (p.x, p.y, p.z, 1).
// Game Role: Evaluates transformed probe coordinates in OrbitalRotor tests.
Vec3 OrbitalRotorDrone::transformPoint(const float m[16], const Vec3& p) {
    float x = m[0] * p.x + m[4] * p.y + m[8] * p.z + m[12];
    float y = m[1] * p.x + m[5] * p.y + m[9] * p.z + m[13];
    float z = m[2] * p.x + m[6] * p.y + m[10] * p.z + m[14];
    return Vec3(x, y, z);
}

// [TOPIC: OrbitalRotor Hierarchical Kinematics]
// Purpose: Evaluates world position of rotor pod 0 or 1 (180 deg offset) around (parentX, parentY).
// Game Role: Confirms hierarchical pod coordinates match the OpenGL matrix stack.
Vec3 OrbitalRotorDrone::computeRotorWorldPosition(int rotorIndex, float parentX, float parentY,
                                                 float curOrbitAngleDeg, float curOrbitRadius) const {
    float baseAngle = curOrbitAngleDeg + (rotorIndex == 0 ? 0.0f : 180.0f);
    float rad = baseAngle * static_cast<float>(M_PI) / 180.0f;

    // Parent Translation T(parentX, parentY) * Rotation R(baseAngle) * Translation along Arm T(orbitRadius, 0)
    float worldX = parentX + std::cos(rad) * curOrbitRadius;
    float worldY = parentY + std::sin(rad) * curOrbitRadius;
    return Vec3(worldX, worldY, 0.0f);
}

// [TOPIC: Affine Matrices (T*R != R*T)]
// Purpose: Compares transformed coordinates of (1, 0, 0) under T*R vs R*T.
// Game Role: Automated proof that translation and rotation do not commute.
bool OrbitalRotorDrone::verifyNonCommutativity(float tx, float ty, float angleDeg) const {
    float T[16], R[16], TR[16], RT[16];
    getTranslationMatrix(tx, ty, 0.0f, T);
    getRotationZMatrix(angleDeg, R);

    // Order 1: T * R
    // WHY: Applies rotation about origin first, then translates by (tx, ty)
    multiplyMatrices(T, R, TR);
    // Order 2: R * T
    // WHY: Translates first, then rotates the offset point around the world origin
    multiplyMatrices(R, T, RT);

    Vec3 testPoint(1.0f, 0.0f, 0.0f);
    Vec3 resTR = transformPoint(TR, testPoint);
    Vec3 resRT = transformPoint(RT, testPoint);

    // Assert that (T * R) and (R * T) produce distinct coordinates when angle != 0 and translation != 0
    float diffX = std::fabs(resTR.x - resRT.x);
    float diffY = std::fabs(resTR.y - resRT.y);
    return (diffX > 0.01f || diffY > 0.01f);
}

// [TOPIC: glPushMatrix/glPopMatrix]
// Purpose: Applies breathing pulse scale locally to the octagonal hub and energy eye.
// Game Role: Prevents core pulsation from scaling the orbiting rotor pods.
void OrbitalRotorDrone::renderCoreHub(float scale) {
    glPushMatrix();
    glScalef(scale, scale, 1.0f);

    // Center Core Hull (Octagon)
    glColor3f(0.20f, 0.70f, 0.40f); // Emerald core
    glBegin(GL_POLYGON);
    const int segments = 8;
    const float r = 0.18f;
    for (int i = 0; i < segments; ++i) {
        float theta = i * 2.0f * static_cast<float>(M_PI) / segments;
        glVertex2f(std::cos(theta) * r, std::sin(theta) * r);
    }
    glEnd();

    // Central Energy Eye
    glColor3f(1.0f, 1.0f, 0.20f); // Bright yellow energy eye
    glPointSize(8.0f);
    glBegin(GL_POINTS);
    glVertex2f(0.0f, 0.0f);
    glEnd();

    glPopMatrix();
}

// [TOPIC: glPushMatrix/glPopMatrix]
// Purpose: Renders a hexagonal rotor housing and locally spinning cross blades.
// Game Role: Child pod assembly attached to each orbiting arm of OrbitalRotor.
void OrbitalRotorDrone::renderRotorPod(float podRadius, float spinDeg) {
    glPushMatrix();

    // Rotor Pod Chassis
    glColor3f(0.80f, 0.30f, 0.10f); // Amber rotor frame
    glBegin(GL_POLYGON);
    const int segs = 6;
    for (int i = 0; i < segs; ++i) {
        float theta = i * 2.0f * static_cast<float>(M_PI) / segs;
        glVertex2f(std::cos(theta) * podRadius, std::sin(theta) * podRadius);
    }
    glEnd();

    // Spinning Rotor Blades: local rotation inside pod matrix
    // WHY: Rotating after drawing the pod spins the blades around the pod's own center
    glRotatef(spinDeg, 0.0f, 0.0f, 1.0f);
    glColor3f(0.30f, 0.85f, 1.0f); // Cyan spinning blades
    glLineWidth(2.5f);
    glBegin(GL_LINES);
    glVertex2f(-podRadius * 1.6f, 0.0f);
    glVertex2f(podRadius * 1.6f, 0.0f);
    glVertex2f(0.0f, -podRadius * 1.6f);
    glVertex2f(0.0f, podRadius * 1.6f);
    glEnd();

    glPopMatrix();
}

// [TOPIC: glPushMatrix/glPopMatrix / OrbitalRotor]
// Purpose: Builds the 2-level hierarchical transform tree (Parent Hub -> Pod 1 & Pod 2 branches).
// Game Role: Draws the CGL-CO2 OrbitalRotor enemy with isolated sibling pod matrices.
void OrbitalRotorDrone::render(float posX, float posY, float baseScale) {
    glPushMatrix(); // Root Level: Position Drone in World
    glTranslatef(posX, posY, 0.0f);
    glScalef(baseScale, baseScale, 1.0f);

    // 1. Render Pulsing Core Hub
    renderCoreHub(pulseScale);

    // 2. Render Orbital Rotor Pod 1 (Angle: orbitAngle)
    // WHY: Stack push/pop isolates Pod 1's R(orbitAngle) * T(orbitRadius) from Pod 2's branch
    glPushMatrix(); // Hierarchical Branch 1: Isolate Pod 1
    glRotatef(orbitAngle, 0.0f, 0.0f, 1.0f);
    glTranslatef(orbitRadius, 0.0f, 0.0f);

    // Connecting arm line to core
    glColor3f(0.5f, 0.5f, 0.5f);
    glLineWidth(1.5f);
    glBegin(GL_LINES);
    glVertex2f(-orbitRadius, 0.0f);
    glVertex2f(0.0f, 0.0f);
    glEnd();

    renderRotorPod(0.08f, spinAngle);
    glPopMatrix(); // Restore to Core Frame

    // 3. Render Orbital Rotor Pod 2 (Angle: orbitAngle + 180)
    glPushMatrix(); // Hierarchical Branch 2: Isolate Pod 2
    glRotatef(orbitAngle + 180.0f, 0.0f, 0.0f, 1.0f);
    glTranslatef(orbitRadius, 0.0f, 0.0f);

    // Connecting arm line to core
    glColor3f(0.5f, 0.5f, 0.5f);
    glLineWidth(1.5f);
    glBegin(GL_LINES);
    glVertex2f(-orbitRadius, 0.0f);
    glVertex2f(0.0f, 0.0f);
    glEnd();

    // WHY: Opposite sign spins Pod 2 counter-clockwise relative to Pod 1 for visual balance
    renderRotorPod(0.08f, -spinAngle);
    glPopMatrix(); // Restore to Core Frame

    glPopMatrix(); // Restore World Frame
}

} // namespace CGL
