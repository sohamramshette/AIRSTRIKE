#include "warp_cruiser.hpp"
#include <cmath>

namespace CGL {

WarpCruiser::WarpCruiser()
    : currentZ(-30.0f), diveDirection(-1.0f), animRoll(0.0f) {
    init();
}

void WarpCruiser::init() {
    currentZ = -30.0f;
    diveDirection = -1.0f;
    animRoll = 0.0f;
}

void WarpCruiser::update(float dt) {
    // Oscillate between deep background (Z = -50) and near combat arena (Z = -10)
    currentZ += diveDirection * dt * 15.0f;
    if (currentZ <= -50.0f) {
        currentZ = -50.0f;
        diveDirection = 1.0f;
    } else if (currentZ >= -10.0f) {
        currentZ = -10.0f;
        diveDirection = -1.0f;
    }

    animRoll += dt * 45.0f;
}

Vec3 WarpCruiser::projectPerspective(const Vec3& p, float fovYDeg, float aspect,
                                     float zNear, float zFar) const {
    float m[16];
    ViewingDemo::getPerspectiveMatrix(fovYDeg, aspect, zNear, zFar, m);
    return ViewingDemo::projectPoint(m, p);
}

Vec3 WarpCruiser::projectOrthographic(const Vec3& p, float left, float right,
                                      float bottom, float top, float zNear, float zFar) const {
    float m[16];
    ViewingDemo::getOrthoMatrix(left, right, bottom, top, zNear, zFar, m);
    return ViewingDemo::projectPoint(m, p);
}

float WarpCruiser::computePerspectiveForeshorteningRatio(float zFarDist, float zNearDist,
                                                         float fovYDeg, float aspect) const {
    // Points along wing: (-1.0, 0, Z) and (+1.0, 0, Z)
    Vec3 p1Far(-1.0f, 0.0f, zFarDist);
    Vec3 p2Far(1.0f, 0.0f, zFarDist);
    Vec3 ndc1Far = projectPerspective(p1Far, fovYDeg, aspect, 1.0f, 100.0f);
    Vec3 ndc2Far = projectPerspective(p2Far, fovYDeg, aspect, 1.0f, 100.0f);
    float widthFar = std::fabs(ndc2Far.x - ndc1Far.x);

    Vec3 p1Near(-1.0f, 0.0f, zNearDist);
    Vec3 p2Near(1.0f, 0.0f, zNearDist);
    Vec3 ndc1Near = projectPerspective(p1Near, fovYDeg, aspect, 1.0f, 100.0f);
    Vec3 ndc2Near = projectPerspective(p2Near, fovYDeg, aspect, 1.0f, 100.0f);
    float widthNear = std::fabs(ndc2Near.x - ndc1Near.x);

    if (widthFar <= 0.0001f) return 0.0f;
    return widthNear / widthFar; // Should equal |zFarDist| / |zNearDist|
}

float WarpCruiser::computeOrthoForeshorteningRatio(float zFarDist, float zNearDist,
                                                   float left, float right) const {
    Vec3 p1Far(-1.0f, 0.0f, zFarDist);
    Vec3 p2Far(1.0f, 0.0f, zFarDist);
    Vec3 ndc1Far = projectOrthographic(p1Far, left, right, -10.0f, 10.0f, 1.0f, 100.0f);
    Vec3 ndc2Far = projectOrthographic(p2Far, left, right, -10.0f, 10.0f, 1.0f, 100.0f);
    float widthFar = std::fabs(ndc2Far.x - ndc1Far.x);

    Vec3 p1Near(-1.0f, 0.0f, zNearDist);
    Vec3 p2Near(1.0f, 0.0f, zNearDist);
    Vec3 ndc1Near = projectOrthographic(p1Near, left, right, -10.0f, 10.0f, 1.0f, 100.0f);
    Vec3 ndc2Near = projectOrthographic(p2Near, left, right, -10.0f, 10.0f, 1.0f, 100.0f);
    float widthNear = std::fabs(ndc2Near.x - ndc1Near.x);

    if (widthFar <= 0.0001f) return 0.0f;
    return widthNear / widthFar; // In orthographic projection, ratio must be exactly 1.0
}

void WarpCruiser::drawCruiserGeometry() {
    // 3D Cruiser Wireframe Mesh
    glColor3f(0.30f, 0.75f, 1.0f); // Electric Blue Warp Cruiser
    glLineWidth(2.0f);

    // Front Nose to Wings
    glBegin(GL_LINE_LOOP);
    glVertex3f(0.0f, 0.0f, 2.0f);    // Nose
    glVertex3f(-1.5f, 0.0f, -1.0f);  // Port Wingtip
    glVertex3f(-0.5f, 0.5f, -1.5f);  // Port Dorsal
    glVertex3f(0.0f, 0.8f, -1.0f);   // Dorsal Fin
    glVertex3f(0.5f, 0.5f, -1.5f);   // Starboard Dorsal
    glVertex3f(1.5f, 0.0f, -1.0f);   // Starboard Wingtip
    glEnd();

    // Ventral Keel
    glBegin(GL_LINES);
    glVertex3f(0.0f, 0.0f, 2.0f);
    glVertex3f(0.0f, -0.6f, -1.0f);
    glVertex3f(-1.5f, 0.0f, -1.0f);
    glVertex3f(0.0f, -0.6f, -1.0f);
    glVertex3f(1.5f, 0.0f, -1.0f);
    glVertex3f(0.0f, -0.6f, -1.0f);
    glEnd();
}

void WarpCruiser::render(bool usePerspective) {
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    glLoadIdentity();

    if (usePerspective) {
        gluPerspective(60.0, 1.333, 1.0, 100.0);
    } else {
        glOrtho(-15.0, 15.0, -10.0, 10.0, 1.0, 100.0);
    }

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // Position camera at origin looking down -Z axis
    gluLookAt(0.0, 0.0, 0.0,
              0.0, 0.0, -1.0,
              0.0, 1.0, 0.0);

    // Position cruiser at current Z depth
    glTranslatef(0.0f, 0.0f, currentZ);
    glRotatef(animRoll, 0.0f, 0.0f, 1.0f);

    drawCruiserGeometry();

    glPopMatrix();
    glMatrixMode(GL_PROJECTION);
    glPopMatrix();
    glMatrixMode(GL_MODELVIEW);
}

} // namespace CGL
