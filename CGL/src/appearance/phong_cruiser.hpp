// ==============================================================================
// File      : CGL/src/appearance/phong_cruiser.hpp
// Subject   : CGL-CO6 | PhongCruiser Illuminated Enemy
// Topics    : [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular)] [TOPIC: Gouraud Smooth Shading] [TOPIC: 24-bit BMP Texture Mapping] [TOPIC: PhongCruiser]
// Game Role : Declares the PhongCruiser 3D enemy with pure-C++ Phong/Blinn-Phong/Gouraud illumination math.
// ==============================================================================

#ifndef PHONG_CRUISER_HPP
#define PHONG_CRUISER_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

// [TOPIC: PhongCruiser / Blinn-Phong Illumination (Ambient + Diffuse + Specular) / Gouraud Smooth Shading / 24-bit BMP Texture Mapping]
// Purpose: Combines a 24-bit BMP-textured 3D faceted hull with analytical Phong, Blinn-Phong, and Gouraud evaluation.
// Game Role: Represents CGL-CO6 enemy type [6] in AIRSTRIKE's Enemy Demonstrator and CO6 test suite.
class PhongCruiser {
public:
    PhongCruiser();
    ~PhongCruiser() = default;

    void init();
    void update(float dt);
    void render();

    // [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular)]
    // Purpose: Computes reflection vector R, Blinn-Phong half-angle vector H, and full illumination colors.
    // Game Role: Validates Ambient + Diffuse + Specular shading equations in CO6 automated tests.
    static Vec3 computeReflectionVector(const Vec3& normal, const Vec3& lightDir);
    static Vec3 computeHalfAngleVector(const Vec3& lightDir, const Vec3& viewDir);

    static Color4 evaluatePhongModel(const Vec3& normal, const Vec3& lightDir, const Vec3& viewDir,
                                     float ka, float kd, float ks, float shininess,
                                     Color4 lightColor = Color4(1.0f, 1.0f, 1.0f, 1.0f));

    static Color4 evaluateBlinnPhongModel(const Vec3& normal, const Vec3& lightDir, const Vec3& viewDir,
                                          float ka, float kd, float ks, float shininess,
                                          Color4 lightColor = Color4(1.0f, 1.0f, 1.0f, 1.0f));

    // [TOPIC: Gouraud Smooth Shading]
    // Purpose: Linearly interpolates vertex-evaluated colors across edge parameter t in [0, 1].
    // Game Role: Demonstrates per-vertex Gouraud shading interpolation analytically.
    static Color4 interpolateGouraudColor(const Color4& c1, const Color4& c2, float t);

    // Texture Inspection
    bool isTextureLoaded() const { return textureLoaded; }
    size_t getTextureBufferSize() const { return textureBytes; }

    float getShininess() const { return shininess; }
    void setShininess(float s) { shininess = (s > 1.0f) ? s : 1.0f; }

    bool isSmoothShading() const { return smoothShading; }
    void setSmoothShading(bool smooth) { smoothShading = smooth; }

    bool isTextureActive() const { return textureActive; }
    void setTextureActive(bool active) { textureActive = active; }

    float getLightAngle() const { return lightAngle; }
    void setLightAngle(float angle) { lightAngle = angle; }

private:
    float shininess;      // Material shininess exponent n
    float ka, kd, ks;     // Ambient, Diffuse, Specular reflection coefficients
    float animAngle;
    bool  smoothShading;  // true: GL_SMOOTH (Gouraud), false: GL_FLAT
    bool  textureActive;  // true: modulate with space_hull.bmp
    float lightAngle;     // Directional light orbital azimuth in degrees

    bool textureLoaded;
    size_t textureBytes;
    GLuint textureId;

    void loadTexture();
    void renderIlluminatedHull();
};

} // namespace CGL

#endif // PHONG_CRUISER_HPP
