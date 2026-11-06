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
