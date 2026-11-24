// ==============================================================================
// File      : CGL/src/appearance/lighting_texture_demo.hpp
// Subject   : CGL-CO6 | Lighting, Shading, Color & Texture Mapping
// Topics    : [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular)] [TOPIC: Gouraud Smooth Shading] [TOPIC: 24-bit BMP Texture Mapping] [TOPIC: HSV-to-RGB]
// Game Role : Declares the CO6 appearance workbench for lighting components, Gouraud shading, BMP textures, and HSV math.
// ==============================================================================

#ifndef LIGHTING_TEXTURE_DEMO_HPP
#define LIGHTING_TEXTURE_DEMO_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

// [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular) / Gouraud Smooth Shading / 24-bit BMP Texture Mapping / HSV-to-RGB]
// Purpose: Demonstrates OpenGL fixed-function lighting, Flat vs Gouraud shading, BMP/procedural textures, filtering/wrapping, and HSV conversion.
// Game Role: Interactive CO6 Appearance screen rendering a lit, UV-mapped planet and spacecraft hull in AIRSTRIKE.
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

    // [TOPIC: HSV-to-RGB / Procedural Texture Synthesis]
    // Purpose: Converts HSV color space to RGBA and synthesizes 24-bit RGB hull and planet textures in memory.
    // Game Role: Provides fallback textures for SpaceCombatScene and verifies CO6 color/texture tests.
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
