// ==============================================================================
// File      : CGL/src/projection/warp_cruiser.hpp
// Subject   : CGL-CO4 | WarpCruiser Depth-Diving Enemy
// Topics    : [TOPIC: Orthographic (glOrtho)] [TOPIC: Perspective (gluPerspective, 1/Z foreshortening)] [TOPIC: Camera Viewing] [TOPIC: WarpCruiser]
// Game Role : Declares the depth-diving WarpCruiser enemy showing 1/Z perspective scaling vs ortho invariance.
// ==============================================================================

#ifndef WARP_CRUISER_HPP
#define WARP_CRUISER_HPP

#include "common/gl_common.h"
#include "viewing_demo.hpp"

namespace CGL {

// [TOPIC: WarpCruiser / Perspective (gluPerspective, 1/Z foreshortening) / Orthographic (glOrtho)]
// Purpose: Animates a 3D wireframe cruiser diving between Z = -50 and Z = -10 under selectable projection modes.
// Game Role: Represents CGL-CO4 enemy type [4] in AIRSTRIKE's Enemy Demonstrator and CO4 test suite.
class WarpCruiser {
public:
    WarpCruiser();
    ~WarpCruiser() = default;

    void init();
    void update(float dt);
    void render(bool usePerspective);

    // [TOPIC: Perspective & Orthographic NDC Projection]
    // Purpose: Projects world points to NDC [-1, 1]^3 via ViewingDemo perspective or ortho matrices.
    // Game Role: Provides context-independent projection math for CO4 WarpCruiser assertions.
    Vec3 projectPerspective(const Vec3& worldPoint, float fovYDeg, float aspect,
                            float zNear, float zFar) const;
    Vec3 projectOrthographic(const Vec3& worldPoint, float left, float right,
                             float bottom, float top, float zNear, float zFar) const;

    // [TOPIC: 1/Z Foreshortening Ratio]
    // Purpose: Computes apparent NDC width ratio between zNearDist and zFarDist under perspective vs ortho.
    // Game Role: Proves perspective scales by |zFar|/|zNear| while orthographic remains 1.0x invariant.
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
