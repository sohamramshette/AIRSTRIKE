#include "phong_cruiser.hpp"
#include "common/image_loader.hpp"
#include <cmath>
#include <algorithm>

namespace CGL {

PhongCruiser::PhongCruiser()
    : shininess(32.0f), ka(0.20f), kd(0.70f), ks(0.90f), animAngle(0.0f),
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

Vec3 PhongCruiser::computeReflectionVector(const Vec3& N, const Vec3& L) {
    // R = 2 * (N . L) * N - L
    float dotNL = N.x * L.x + N.y * L.y + N.z * L.z;
    return Vec3(2.0f * dotNL * N.x - L.x,
                2.0f * dotNL * N.y - L.y,
                2.0f * dotNL * N.z - L.z);
}

Vec3 PhongCruiser::computeHalfAngleVector(const Vec3& L, const Vec3& V) {
    // H = normalize(L + V)
    Vec3 H(L.x + V.x, L.y + V.y, L.z + V.z);
    float len = std::sqrt(H.x * H.x + H.y * H.y + H.z * H.z);
    if (len > 0.0001f) {
        H.x /= len;
        H.y /= len;
        H.z /= len;
    }
    return H;
}

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
    float Is = ks * std::pow(dotNH, shininess);

    float intensity = std::min(1.0f, Ia + Id + Is);
    return Color4(intensity * lightColor.r, intensity * lightColor.g, intensity * lightColor.b, 1.0f);
}

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

    if (textureLoaded) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
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

void PhongCruiser::render() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glShadeModel(GL_SMOOTH); // Smooth Gouraud shading enabled

    // Directional sunlight
    GLfloat lightPos[]     = { 1.0f, 1.0f, 1.0f, 0.0f }; // Directional
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

    glDisable(GL_LIGHT0);
    glDisable(GL_LIGHTING);
}

} // namespace CGL
