#ifndef LIGHTING_TEXTURE_DEMO_HPP
#define LIGHTING_TEXTURE_DEMO_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

/**
 * @brief Class demonstrating CGL-CO6: Texture, Lighting, and Color.
 * Covers:
 * - Color Models: RGB, RGBA, HSV conversion
 * - Shading Models: Flat (GL_FLAT) vs Smooth Gouraud (GL_SMOOTH)
 * - Phong Illumination Model (Ambient, Diffuse, Specular reflection components)
 * - Positional vs Directional Light Sources
 * - Material Properties (Ambient, Diffuse, Specular, Shininess)
 * - Actual Local Image File Loading (.bmp from CGL/assets/textures/)
 * - Procedural 2D Texture Generation & UV Coordinate Mapping
 * - Texture Filtering (GL_LINEAR vs GL_NEAREST) and Wrapping (GL_REPEAT vs GL_CLAMP)
 */
class LightingTextureDemo {
public:
    LightingTextureDemo();
    ~LightingTextureDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // Interactive controls
    void toggleLighting();
    void toggleTexture();
    void toggleShading();
    void toggleAmbient();
    void toggleDiffuse();
    void toggleSpecular();
    void toggleFiltering();
    void toggleWrapping();
    void toggleTextureSource();
    void adjustShininess(float delta);
    void reset();

    // Inspection getters for automated testing
    bool isLightingEnabled() const { return lightingEnabled; }
    bool isTextureEnabled() const { return textureEnabled; }
    bool isSmoothShading() const { return smoothShading; }
    bool isAmbientOn() const { return ambientOn; }
    bool isDiffuseOn() const { return diffuseOn; }
    bool isSpecularOn() const { return specularOn; }
    bool isLinearFilter() const { return linearFilter; }
    bool isRepeatWrap() const { return repeatWrap; }
    bool isFileTextureMode() const { return useFileTexture; }
    bool isLocalTexLoaded() const { return localTexLoaded; }
    float getShininess() const { return matShininess; }
    std::string getLoadedFilePath() const { return loadedFilePath; }

    // Color space math helpers
    static Color4 hsvToRgb(float h, float s, float v, float a = 1.0f);
    static void generateHullTexture(int width, int height, std::vector<unsigned char>& data);
    static void generatePlanetTexture(int width, int height, std::vector<unsigned char>& data);

private:
    bool lightingEnabled;
    bool textureEnabled;
    bool smoothShading;
    bool ambientOn;
    bool diffuseOn;
    bool specularOn;
    bool linearFilter;
    bool repeatWrap;
    bool useFileTexture;
    bool localTexLoaded;

    float matShininess;
    float animAngle;
    float lightOrbitAngle;

    GLuint procHullTextureId;
    GLuint procPlanetTextureId;
    GLuint localHullTextureId;
    GLuint localAlienTextureId;

    std::string loadedFilePath;

    void loadLocalTextures();
    void createProceduralTextures();
    void applyTextureParameters(GLuint texId);
    void setupLighting();
    void renderTexturedSphere(float radius, int slices, int stacks);
    void renderTexturedShip(float size);
};

} // namespace CGL

#endif // LIGHTING_TEXTURE_DEMO_HPP
