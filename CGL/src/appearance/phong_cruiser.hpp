#ifndef PHONG_CRUISER_HPP
#define PHONG_CRUISER_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

/**
 * @brief Specular Phong Cruiser Enemy (CGL-CO6).
 *
 * Demonstrates:
 * - Phong & Blinn-Phong Illumination Models (Ambient, Diffuse, Specular)
 * - Lambert's Cosine Law for Diffuse Reflection: Id = kd * Ld * max(0, N . L)
 * - Specular Highlights: Is = ks * Ls * max(0, R . V)^n (Phong) and (N . H)^n (Blinn-Phong)
 * - Clear Distinction: Per-vertex Gouraud Interpolation vs High-frequency Specular Highlights
 * - Local 24-bit Uncompressed BMP Texture Mapping (space_hull.bmp)
 */
class PhongCruiser {
public:
    PhongCruiser();
    ~PhongCruiser() = default;

    void init();
    void update(float dt);
    void render();

    // Illumination Mathematics Helpers (Pure C++ for direct testability)
    static Vec3 computeReflectionVector(const Vec3& normal, const Vec3& lightDir);
    static Vec3 computeHalfAngleVector(const Vec3& lightDir, const Vec3& viewDir);

    static Color4 evaluatePhongModel(const Vec3& normal, const Vec3& lightDir, const Vec3& viewDir,
                                     float ka, float kd, float ks, float shininess,
                                     Color4 lightColor = Color4(1.0f, 1.0f, 1.0f, 1.0f));

    static Color4 evaluateBlinnPhongModel(const Vec3& normal, const Vec3& lightDir, const Vec3& viewDir,
                                          float ka, float kd, float ks, float shininess,
                                          Color4 lightColor = Color4(1.0f, 1.0f, 1.0f, 1.0f));

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
