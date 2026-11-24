// ==============================================================================
// File      : CGL/src/appearance/phong_cruiser.cpp
// Subject   : CGL-CO6 | PhongCruiser Illuminated Enemy
// Topics    : [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular)] [TOPIC: Gouraud Smooth Shading] [TOPIC: 24-bit BMP Texture Mapping] [TOPIC: PhongCruiser]
// Game Role : Evaluates Phong/Blinn-Phong reflection math and renders a BMP-textured lit 3D PhongCruiser.
// ==============================================================================

#include "phong_cruiser.hpp"
#include "common/image_loader.hpp"
#include <cmath>
#include <algorithm>

namespace CGL {

PhongCruiser::PhongCruiser()
    : shininess(32.0f), ka(0.20f), kd(0.70f), ks(0.90f), animAngle(0.0f),
      smoothShading(true), textureActive(true), lightAngle(45.0f),
      textureLoaded(false), textureBytes(0), textureId(0) {
    init();
}

void PhongCruiser::init() {
    loadTexture();
}

void PhongCruiser::update(float dt) {
    animAngle += dt * 30.0f;
    if (animAngle >= 360.0f) animAngle -= 360.0f;
}

// [TOPIC: 24-bit BMP Texture Mapping]
// Purpose: Loads 256x256 space_hull.bmp and configures GL_LINEAR + GL_REPEAT texture state.
// Game Role: Applies realistic metallic hull plating onto PhongCruiser facets.
void PhongCruiser::loadTexture() {
    std::string path = "CGL/assets/textures/space_hull.bmp";
    int w = 0, h = 0;
    std::vector<unsigned char> rgb;
    if (loadBMP(path, w, h, rgb)) {
        textureLoaded = true;
        textureBytes = rgb.size();

        glGenTextures(1, &textureId);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w, h, 0, GL_RGB, GL_UNSIGNED_BYTE, rgb.data());
    }
}

// [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular)]
// Purpose: Mirrors incident light vector L across unit normal N via R = 2*(N·L)*N - L.
// Game Role: Computes classic Phong specular reflection direction for CO6 tests.
Vec3 PhongCruiser::computeReflectionVector(const Vec3& N, const Vec3& L) {
    // R = 2 * (N . L) * N - L
    float dotNL = N.x * L.x + N.y * L.y + N.z * L.z;
    // WHY: Project L onto N and double minus L reflects incoming ray symmetrically across N
    return Vec3(2.0f * dotNL * N.x - L.x,
                2.0f * dotNL * N.y - L.y,
                2.0f * dotNL * N.z - L.z);
}

// [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular)]
// Purpose: Computes the normalized halfway bisector H = normalize(L + V) between light and viewer.
// Game Role: Replaces expensive reflection-vector dot products in Blinn-Phong specular shading.
Vec3 PhongCruiser::computeHalfAngleVector(const Vec3& L, const Vec3& V) {
    // H = normalize(L + V)
    Vec3 H(L.x + V.x, L.y + V.y, L.z + V.z);
    float len = std::sqrt(H.x * H.x + H.y * H.y + H.z * H.z);
    if (len > 0.0001f) {
        // WHY: Normalization ensures N . H equals cos(alpha) directly for specular exponentiation
        H.x /= len;
        H.y /= len;
        H.z /= len;
    }
    return H;
}

// [TOPIC: Phong Illumination (Ambient + Diffuse + Specular)]
// Purpose: Evaluates clamped I = min(1, Ia + Id + Is) using max(0, R·V)^shininess.
// Game Role: Verifies classic Phong specular highlight peaks in CO6 unit tests.
Color4 PhongCruiser::evaluatePhongModel(const Vec3& N, const Vec3& L, const Vec3& V,
                                       float ka, float kd, float ks, float shininess,
                                       Color4 lightColor) {
    // 1. Ambient: Ia = ka * La
    float Ia = ka;

    // 2. Diffuse: Id = kd * Ld * max(0, N . L)
    float dotNL = std::max(0.0f, N.x * L.x + N.y * L.y + N.z * L.z);
    float Id = kd * dotNL;

    // 3. Specular (Classic Phong): Is = ks * Ls * max(0, R . V)^n
    Vec3 R = computeReflectionVector(N, L);
    float dotRV = std::max(0.0f, R.x * V.x + R.y * V.y + R.z * V.z);
    float Is = ks * std::pow(dotRV, shininess);

    float intensity = std::min(1.0f, Ia + Id + Is);
    return Color4(intensity * lightColor.r, intensity * lightColor.g, intensity * lightColor.b, 1.0f);
}

// [TOPIC: Blinn-Phong Illumination (Ambient + Diffuse + Specular)]
// Purpose: Evaluates clamped I = min(1, Ambient + Diffuse + Specular) using half-vector max(0, N·H)^shininess.
// Game Role: Verifies Blinn-Phong specular intensity evaluation in CO6 PhongCruiser tests.
Color4 PhongCruiser::evaluateBlinnPhongModel(const Vec3& N, const Vec3& L, const Vec3& V,
                                            float ka, float kd, float ks, float shininess,
                                            Color4 lightColor) {
    // 1. Ambient
    float Ia = ka;

    // 2. Diffuse
    float dotNL = std::max(0.0f, N.x * L.x + N.y * L.y + N.z * L.z);
    float Id = kd * dotNL;

    // 3. Specular (Blinn-Phong half-angle): Is = ks * Ls * max(0, N . H)^n
    Vec3 H = computeHalfAngleVector(L, V);
    float dotNH = std::max(0.0f, N.x * H.x + N.y * H.y + N.z * H.z);
    // WHY: Raising (N . H) to shininess concentrates highlight lobes tightly around aligned microfacets
    float Is = ks * std::pow(dotNH, shininess);

    float intensity = std::min(1.0f, Ia + Id + Is);
    return Color4(intensity * lightColor.r, intensity * lightColor.g, intensity * lightColor.b, 1.0f);
}

// [TOPIC: Gouraud Smooth Shading]
// Purpose: Linearly blends endpoint vertex illumination colors c1 and c2 by clamped factor t.
// Game Role: Models Gouraud intensity interpolation across polygon spans.
Color4 PhongCruiser::interpolateGouraudColor(const Color4& c1, const Color4& c2, float t) {
    t = std::max(0.0f, std::min(1.0f, t));
    return Color4(c1.r + t * (c2.r - c1.r),
                  c1.g + t * (c2.g - c1.g),
                  c1.b + t * (c2.b - c1.b),
                  c1.a + t * (c2.a - c1.a));
}

// [TOPIC: Blinn-Phong Material / 24-bit BMP Texture Mapping]
// Purpose: Binds metallic red Ambient/Diffuse/Specular material, optional BMP texture, and 4 angled hull triangles.
// Game Role: Draws the illuminated PhongCruiser wedge mesh under active OpenGL lights.
void PhongCruiser::renderIlluminatedHull() {
    // Set material properties for specular highlight demonstration
    GLfloat matAmbient[]  = { ka, ka, ka, 1.0f };
    GLfloat matDiffuse[]  = { kd * 0.8f, kd * 0.2f, kd * 0.2f, 1.0f }; // Metallic red hull
    GLfloat matSpecular[] = { ks, ks, ks, 1.0f };                      // Sharp white specular highlight
    GLfloat matShine[]    = { shininess };

    glMaterialfv(GL_FRONT, GL_AMBIENT, matAmbient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, matDiffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, matSpecular);
    glMaterialfv(GL_FRONT, GL_SHININESS, matShine);

    if (textureLoaded && textureActive) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
    } else {
        glDisable(GL_TEXTURE_2D);
    }

    // Render faceted hull with explicit surface normals
    glBegin(GL_TRIANGLES);

    // Nose facet 1 (Dorsal port)
    glNormal3f(-0.4f, 0.4f, 0.8f);
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.0f, 1.5f);
    glTexCoord2f(0.0f, 0.0f); glVertex3f(-1.0f, 0.5f, -0.5f);
    glTexCoord2f(0.5f, 0.0f); glVertex3f(0.0f, 0.8f, -0.5f);

    // Nose facet 2 (Dorsal starboard)
    glNormal3f(0.4f, 0.4f, 0.8f);
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.0f, 1.5f);
    glTexCoord2f(0.5f, 0.0f); glVertex3f(0.0f, 0.8f, -0.5f);
    glTexCoord2f(1.0f, 0.0f); glVertex3f(1.0f, 0.5f, -0.5f);

    // Ventral facet 1 (Port)
    glNormal3f(-0.4f, -0.4f, 0.8f);
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.0f, 1.5f);
    glTexCoord2f(0.5f, 0.0f); glVertex3f(0.0f, -0.8f, -0.5f);
    glTexCoord2f(0.0f, 0.0f); glVertex3f(-1.0f, -0.5f, -0.5f);

    // Ventral facet 2 (Starboard)
    glNormal3f(0.4f, -0.4f, 0.8f);
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, 0.0f, 1.5f);
    glTexCoord2f(1.0f, 0.0f); glVertex3f(1.0f, -0.5f, -0.5f);
    glTexCoord2f(0.5f, 0.0f); glVertex3f(0.0f, -0.8f, -0.5f);

    glEnd();

    if (textureLoaded) {
        glDisable(GL_TEXTURE_2D);
    }
}

// [TOPIC: Gouraud Smooth Shading / Directional Light / PhongCruiser]
// Purpose: Enables depth test and GL_LIGHT0 directional light, rotates PhongCruiser, and cleans up light state.
// Game Role: Renders CGL-CO6 PhongCruiser inside EnemyDemoScene.
void PhongCruiser::render() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);

    if (smoothShading) {
        glShadeModel(GL_SMOOTH); // Smooth Gouraud shading enabled
    } else {
        glShadeModel(GL_FLAT);   // Flat constant shading enabled
    }

    // Directional light orbiting at lightAngle
    float rad = lightAngle * 3.14159265f / 180.0f;
    // WHY: Homogeneous w = 0.0f defines an infinitely distant directional light (parallel rays)
    GLfloat lightPos[]     = { std::cos(rad), 1.0f, std::sin(rad), 0.0f }; // Directional
    GLfloat lightAmbient[] = { 0.2f, 0.2f, 0.2f, 1.0f };
    GLfloat lightDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    GLfloat lightSpec[]    = { 1.0f, 1.0f, 1.0f, 1.0f };

    glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
    glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbient);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, lightDiffuse);
    glLightfv(GL_LIGHT0, GL_SPECULAR, lightSpec);

    glPushMatrix();
    glRotatef(animAngle, 0.0f, 1.0f, 0.0f);
    renderIlluminatedHull();
    glPopMatrix();

    // WHY: Disable lighting on exit so subsequent unlit scenes or HUD overlays do not inherit GL_LIGHT0
    glDisable(GL_LIGHT0);
    glDisable(GL_LIGHTING);
}

} // namespace CGL
