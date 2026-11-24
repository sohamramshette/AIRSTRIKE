// ==============================================================================
// File      : CGL/src/projection/viewing_demo.hpp
// Subject   : CGL-CO4 | Projection & Viewing Pipeline
// Topics    : [TOPIC: Orthographic (glOrtho)] [TOPIC: Perspective (gluPerspective, 1/Z foreshortening)] [TOPIC: Camera Viewing]
// Game Role : Declares the CO4 split-screen Radar vs Cockpit view demo and pure C++ projection/LookAt matrices.
// ==============================================================================

#ifndef VIEWING_DEMO_HPP
#define VIEWING_DEMO_HPP

#include "common/gl_common.h"
#include <string>

namespace CGL {

// [TOPIC: Orthographic (glOrtho) / Perspective (gluPerspective, 1/Z foreshortening) / Camera Viewing]
// Purpose: Compares parallel orthographic and perspective viewing pipelines plus viewport scissor splitting.
// Game Role: Interactive CO4 showcase contrasting depth-invariant tactical radar with 1/Z cockpit depth.
class ViewingDemo {
public:
    ViewingDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // Interactive controls
    void toggleProjectionMode();
    void orbitCamera(float dPitch, float dYaw);
    void zoomCamera(float dZoom);
    void reset();

    // Getters for testing and inspection
    int getProjectionMode() const { return projectionMode; } // 0: Split View, 1: Ortho Only, 2: Perspective Only
    float getCameraDistance() const { return cameraDistance; }
    float getCameraPitch() const { return cameraPitch; }
    float getCameraYaw() const { return cameraYaw; }
    float getFov() const { return fov; }

    // [TOPIC: Viewing & Projection Matrices]
    // Purpose: Column-major Ortho, Perspective, LookAt, NDC projection, and viewport mapping functions.
    // Game Role: Drives CO4 and WarpCruiser automated mathematical projection tests.
    static void getOrthoMatrix(float l, float r, float b, float t, float n, float f, float m[16]);
    static void getPerspectiveMatrix(float fovyDeg, float aspect, float zNear, float zFar, float m[16]);
    static void getLookAtMatrix(const Vec3& eye, const Vec3& center, const Vec3& up, float m[16]);
    static Vec3 projectPoint(const float projMatrix[16], const Vec3& p);
    static Point2D viewportMap(const Vec3& ndc, int vpX, int vpY, int vpW, int vpH);

private:
    int projectionMode; // 0: Split, 1: Ortho, 2: Perspective
    float cameraPitch;
    float cameraYaw;
    float cameraDistance;
    float fov;
    float animAngle;

    void renderSceneObjects();
    void renderTacticalRadarView(int x, int y, int w, int h);
    void renderPerspectiveView(int x, int y, int w, int h);
};

} // namespace CGL

#endif // VIEWING_DEMO_HPP

