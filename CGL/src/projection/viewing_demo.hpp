#ifndef VIEWING_DEMO_HPP
#define VIEWING_DEMO_HPP

#include "common/gl_common.h"
#include <string>

namespace CGL {

/**
 * @brief Class demonstrating CGL-CO4: Projection and Viewing.
 * Covers the complete viewing pipeline:
 * - Parallel / Orthographic Projection (glOrtho)
 * - Perspective Projection (gluPerspective / glFrustum)
 * - Camera Positioning (gluLookAt)
 * - Viewport Mapping (glViewport)
 * - Split-screen Tactical Radar vs Cockpit Chase View
 */
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

    // Math helpers for testing and verification
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

