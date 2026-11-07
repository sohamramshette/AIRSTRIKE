#ifndef WARP_CRUISER_HPP
#define WARP_CRUISER_HPP

#include "common/gl_common.h"
#include "viewing_demo.hpp"

namespace CGL {

/**
 * @brief Depth-Diving Warp Cruiser Enemy (CGL-CO4).
 *
 * Demonstrates:
 * - Parallel / Orthographic Projection vs Perspective Projection
 * - Non-linear Perspective Foreshortening: NDC coordinate scaling proportional to 1 / Z
 * - Orthographic Distance Invariance: Constant NDC coordinates regardless of Z depth
 * - Viewing Transformation Pipeline: Camera LookAt, Frustum division, and Viewport mapping
 */
class WarpCruiser {
public:
    WarpCruiser();
    ~WarpCruiser() = default;

    void init();
    void update(float dt);
    void render(bool usePerspective);

    // Mathematical Projection Calculations (Pure C++ for automated unit testing)
    Vec3 projectPerspective(const Vec3& worldPoint, float fovYDeg, float aspect,
                            float zNear, float zFar) const;
    Vec3 projectOrthographic(const Vec3& worldPoint, float left, float right,
                             float bottom, float top, float zNear, float zFar) const;

    float computePerspectiveForeshorteningRatio(float zFarDist, float zNearDist,
                                                float fovYDeg, float aspect) const;
    float computeOrthoForeshorteningRatio(float zFarDist, float zNearDist,
                                          float left, float right) const;

    float getZDepth() const { return currentZ; }
    void setZDepth(float z) { currentZ = z; }

private:
    float currentZ;       // 3D depth in world space (-50.0 to -10.0)
    float diveDirection;  // +1 or -1 for warp dive oscillation
    float animRoll;

    void drawCruiserGeometry();
};

} // namespace CGL

#endif // WARP_CRUISER_HPP
