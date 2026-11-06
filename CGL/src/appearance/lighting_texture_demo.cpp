#include "lighting_texture_demo.hpp"
#include "common/font_renderer.hpp"
#include <cmath>
#include <iomanip>
#include <sstream>

namespace CGL {

LightingTextureDemo::LightingTextureDemo()
    : lightingEnabled(true), textureEnabled(true), smoothShading(true),
      ambientOn(true), diffuseOn(true), specularOn(true), linearFilter(true),
      matShininess(32.0f), animAngle(0.0f), lightOrbitAngle(45.0f),
      hullTextureId(0), planetTextureId(0) {}

LightingTextureDemo::~LightingTextureDemo() {
    if (hullTextureId != 0) glDeleteTextures(1, &hullTextureId);
    if (planetTextureId != 0) glDeleteTextures(1, &planetTextureId);
}

Color4 LightingTextureDemo::hsvToRgb(float h, float s, float v, float a) {
    float r = 0.0f, g = 0.0f, b = 0.0f;
    int i = static_cast<int>(h * 6.0f);
    float f = h * 6.0f - i;
    float p = v * (1.0f - s);
    float q = v * (1.0f - f * s);
    float t = v * (1.0f - (1.0f - f) * s);

    switch (i % 6) {
        case 0: r = v; g = t; b = p; break;
        case 1: r = q; g = v; b = p; break;
        case 2: r = p; g = v; b = t; break;
        case 3: r = p; g = q; b = v; break;
        case 4: r = t; g = p; b = v; break;
        case 5: r = v; g = p; b = q; break;
    }
    return Color4(r, g, b, a);
}

void LightingTextureDemo::generateHullTexture(int width, int height, std::vector<unsigned char>& data) {
    data.resize(width * height * 3);
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            int idx = (y * width + x) * 3;
            // Base metallic gray
            unsigned char base = 140;

            // Seam lines every 32 pixels
            bool isSeam = (x % 32 == 0) || (y % 32 == 0);

            // Rivets at intersections
            bool isRivet = ((x % 32 == 2 || x % 32 == 30) && (y % 32 == 2 || y % 32 == 30));

            // Diagonal yellow/black hazard stripe in lower-left quadrant
            bool isHazardQuadrant = (x < width / 2 && y < height / 2);
            bool isYellowStripe = ((x + y) / 8) % 2 == 0;

            if (isHazardQuadrant) {
                if (isYellowStripe) {
                    data[idx + 0] = 220; // R
                    data[idx + 1] = 180; // G
                    data[idx + 2] = 20;  // B
                } else {
                    data[idx + 0] = 30;
                    data[idx + 1] = 30;
                    data[idx + 2] = 35;
                }
            } else if (isRivet) {
                data[idx + 0] = 230;
                data[idx + 1] = 235;
                data[idx + 2] = 245;
            } else if (isSeam) {
                data[idx + 0] = 60;
                data[idx + 1] = 65;
                data[idx + 2] = 75;
            } else {
                data[idx + 0] = base + (x % 7);
                data[idx + 1] = base + 5 + (y % 5);
                data[idx + 2] = base + 15 + ((x * y) % 9);
            }
        }
    }
}

void LightingTextureDemo::generatePlanetTexture(int width, int height, std::vector<unsigned char>& data) {
    data.resize(width * height * 3);
    for (int y = 0; y < height; ++y) {
        float ny = static_cast<float>(y) / height;
        for (int x = 0; x < width; ++x) {
            float nx = static_cast<float>(x) / width;
            int idx = (y * width + x) * 3;

            // Atmospheric bands and craters
            float band = std::sin(ny * 12.0f * static_cast<float>(M_PI)) * 0.2f;
            float swirl = std::sin((nx + ny) * 8.0f * static_cast<float>(M_PI)) * 0.15f;
            float val = 0.5f + band + swirl;
            if (val < 0.0f) val = 0.0f;
            if (val > 1.0f) val = 1.0f;

            // Deep ocean turquoise to deep royal blue
            data[idx + 0] = static_cast<unsigned char>(30 + val * 60);  // R
            data[idx + 1] = static_cast<unsigned char>(70 + val * 120); // G
            data[idx + 2] = static_cast<unsigned char>(160 + val * 85); // B
        }
    }
}

void LightingTextureDemo::createProceduralTextures() {
    int texDim = 128;
    std::vector<unsigned char> hullData;
    generateHullTexture(texDim, texDim, hullData);

    glGenTextures(1, &hullTextureId);
    glBindTexture(GL_TEXTURE_2D, hullTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texDim, texDim, 0, GL_RGB, GL_UNSIGNED_BYTE, hullData.data());
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, linearFilter ? GL_LINEAR : GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, linearFilter ? GL_LINEAR : GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    std::vector<unsigned char> planetData;
    generatePlanetTexture(texDim, texDim, planetData);

    glGenTextures(1, &planetTextureId);
    glBindTexture(GL_TEXTURE_2D, planetTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texDim, texDim, 0, GL_RGB, GL_UNSIGNED_BYTE, planetData.data());
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, linearFilter ? GL_LINEAR : GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, linearFilter ? GL_LINEAR : GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
}

void LightingTextureDemo::init() {
    glEnable(GL_DEPTH_TEST);
    createProceduralTextures();
}

void LightingTextureDemo::update(float dt) {
    animAngle += 35.0f * dt;
    if (animAngle >= 360.0f) animAngle -= 360.0f;

    lightOrbitAngle += 20.0f * dt;
    if (lightOrbitAngle >= 360.0f) lightOrbitAngle -= 360.0f;
}

void LightingTextureDemo::toggleLighting() { lightingEnabled = !lightingEnabled; }
void LightingTextureDemo::toggleTexture()  { textureEnabled = !textureEnabled; }
void LightingTextureDemo::toggleShading()  { smoothShading = !smoothShading; }
void LightingTextureDemo::toggleAmbient()  { ambientOn = !ambientOn; }
void LightingTextureDemo::toggleDiffuse()  { diffuseOn = !diffuseOn; }
void LightingTextureDemo::toggleSpecular() { specularOn = !specularOn; }

void LightingTextureDemo::toggleFiltering() {
    linearFilter = !linearFilter;
    GLint filter = linearFilter ? GL_LINEAR : GL_NEAREST;
    if (hullTextureId) {
        glBindTexture(GL_TEXTURE_2D, hullTextureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
    }
    if (planetTextureId) {
        glBindTexture(GL_TEXTURE_2D, planetTextureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
    }
}

void LightingTextureDemo::adjustShininess(float delta) {
    matShininess += delta;
    if (matShininess < 2.0f) matShininess = 2.0f;
    if (matShininess > 128.0f) matShininess = 128.0f;
}

void LightingTextureDemo::reset() {
    lightingEnabled = true;
    textureEnabled = true;
    smoothShading = true;
    ambientOn = true;
    diffuseOn = true;
    specularOn = true;
    linearFilter = true;
    matShininess = 32.0f;
}

void LightingTextureDemo::setupLighting() {
    if (!lightingEnabled) {
        glDisable(GL_LIGHTING);
        glDisable(GL_LIGHT0);
        return;
    }

    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);

    float lightRad = lightOrbitAngle * static_cast<float>(M_PI) / 180.0f;
    GLfloat lightPos[] = {
        std::cos(lightRad) * 5.0f,
        3.0f,
        std::sin(lightRad) * 5.0f,
        1.0f // Positional point light
    };
    glLightfv(GL_LIGHT0, GL_POSITION, lightPos);

    GLfloat amb[]  = {ambientOn ? 0.25f : 0.0f, ambientOn ? 0.25f : 0.0f, ambientOn ? 0.3f : 0.0f, 1.0f};
    GLfloat diff[] = {diffuseOn ? 0.85f : 0.0f, diffuseOn ? 0.85f : 0.0f, diffuseOn ? 0.85f : 0.0f, 1.0f};
    GLfloat spec[] = {specularOn ? 0.95f : 0.0f, specularOn ? 0.95f : 0.0f, specularOn ? 0.95f : 0.0f, 1.0f};

    glLightfv(GL_LIGHT0, GL_AMBIENT, amb);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, diff);
    glLightfv(GL_LIGHT0, GL_SPECULAR, spec);

    // Material properties
    GLfloat matAmb[]  = {0.8f, 0.8f, 0.8f, 1.0f};
    GLfloat matDiff[] = {0.9f, 0.9f, 0.9f, 1.0f};
    GLfloat matSpec[] = {1.0f, 1.0f, 1.0f, 1.0f};

    glMaterialfv(GL_FRONT, GL_AMBIENT, matAmb);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, matDiff);
    glMaterialfv(GL_FRONT, GL_SPECULAR, matSpec);
    glMaterialf(GL_FRONT, GL_SHININESS, matShininess);

    glShadeModel(smoothShading ? GL_SMOOTH : GL_FLAT);

    // Render a small glowing yellow orb at the light position
    glPushMatrix();
    glTranslatef(lightPos[0], lightPos[1], lightPos[2]);
    glDisable(GL_LIGHTING);
    glColor3f(1.0f, 1.0f, 0.2f);
    glutSolidSphere(0.15, 8, 8);
    if (lightingEnabled) glEnable(GL_LIGHTING);
    glPopMatrix();
}

void LightingTextureDemo::renderTexturedSphere(float radius, int slices, int stacks) {
    if (textureEnabled && planetTextureId != 0) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, planetTextureId);
    } else {
        glDisable(GL_TEXTURE_2D);
        glColor3f(0.3f, 0.6f, 0.9f);
    }

    for (int i = 0; i < stacks; ++i) {
        float phi1 = static_cast<float>(M_PI) * (static_cast<float>(i) / stacks);
        float phi2 = static_cast<float>(M_PI) * (static_cast<float>(i + 1) / stacks);
        float v1 = static_cast<float>(i) / stacks;
        float v2 = static_cast<float>(i + 1) / stacks;

        glBegin(GL_QUAD_STRIP);
        for (int j = 0; j <= slices; ++j) {
            float theta = 2.0f * static_cast<float>(M_PI) * (static_cast<float>(j) / slices);
            float u = static_cast<float>(j) / slices;

            // Vertex 1
            float nx1 = std::sin(phi1) * std::cos(theta);
            float ny1 = std::cos(phi1);
            float nz1 = std::sin(phi1) * std::sin(theta);
            glNormal3f(nx1, ny1, nz1);
            glTexCoord2f(u, v1);
            glVertex3f(radius * nx1, radius * ny1, radius * nz1);

            // Vertex 2
            float nx2 = std::sin(phi2) * std::cos(theta);
            float ny2 = std::cos(phi2);
            float nz2 = std::sin(phi2) * std::sin(theta);
            glNormal3f(nx2, ny2, nz2);
            glTexCoord2f(u, v2);
            glVertex3f(radius * nx2, radius * ny2, radius * nz2);
        }
        glEnd();
    }

    glDisable(GL_TEXTURE_2D);
}

void LightingTextureDemo::renderTexturedShip(float size) {
    if (textureEnabled && hullTextureId != 0) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, hullTextureId);
    } else {
        glDisable(GL_TEXTURE_2D);
        glColor3f(0.7f, 0.75f, 0.85f);
    }

    // Fuselage Top
    glBegin(GL_TRIANGLES);
    glNormal3f(0.0f, 0.8f, 0.4f);
    glTexCoord2f(0.5f, 1.0f); glVertex3f(0.0f, size * 0.4f, size * 1.2f);
    glTexCoord2f(0.0f, 0.0f); glVertex3f(-size * 0.6f, 0.0f, -size * 0.8f);
    glTexCoord2f(1.0f, 0.0f); glVertex3f(size * 0.6f, 0.0f, -size * 0.8f);

    // Left Wing
    glNormal3f(-0.3f, 0.9f, 0.0f);
    glTexCoord2f(0.3f, 0.7f); glVertex3f(-size * 0.3f, 0.0f, size * 0.3f);
    glTexCoord2f(0.0f, 0.0f); glVertex3f(-size * 1.5f, 0.0f, -size * 1.0f);
    glTexCoord2f(0.6f, 0.0f); glVertex3f(-size * 0.3f, 0.0f, -size * 0.7f);

    // Right Wing
    glNormal3f(0.3f, 0.9f, 0.0f);
    glTexCoord2f(0.3f, 0.7f); glVertex3f(size * 0.3f, 0.0f, size * 0.3f);
    glTexCoord2f(0.6f, 0.0f); glVertex3f(size * 0.3f, 0.0f, -size * 0.7f);
    glTexCoord2f(0.0f, 0.0f); glVertex3f(size * 1.5f, 0.0f, -size * 1.0f);
    glEnd();

    glDisable(GL_TEXTURE_2D);
}

void LightingTextureDemo::render(int screenWidth, int screenHeight) {
    glClearColor(0.02f, 0.03f, 0.05f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(45.0, static_cast<double>(screenWidth) / screenHeight, 0.1, 100.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    gluLookAt(0.0, 3.0, 7.5, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

    setupLighting();

    // 1. Render Rotating Textured Celestial Planet (Left)
    glPushMatrix();
    glTranslatef(-2.2f, 0.0f, 0.0f);
    glRotatef(animAngle * 0.4f, 0.2f, 1.0f, 0.1f);
    renderTexturedSphere(1.3f, 24, 20);
    glPopMatrix();

    // 2. Render Rotating Textured Spacecraft Hull (Right)
    glPushMatrix();
    glTranslatef(2.2f, 0.0f, 0.0f);
    glRotatef(animAngle, 0.0f, 1.0f, 0.3f);
    renderTexturedShip(1.0f);
    glPopMatrix();

    glDisable(GL_LIGHTING);

    // HUD Status and Metrics
    beginHUD(screenWidth, screenHeight);

    glColor3f(0.3f, 0.9f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 30.0f, GLUT_BITMAP_HELVETICA_18,
                       "AIRSTRIKE CGL-CO6: Texture, Lighting & Color Models");

    glColor3f(0.7f, 0.75f, 0.85f);
    renderBitmapString(20.0f, screenHeight - 50.0f, GLUT_BITMAP_HELVETICA_12,
                       "Phong Reflection Model (Ambient + Diffuse + Specular) | Procedural 2D Texture UV Mapping");

    // Component states
    std::string lightStr = lightingEnabled ? "Lighting: ENABLED [L]" : "Lighting: DISABLED [L]";
    glColor3f(lightingEnabled ? 0.3f : 0.8f, lightingEnabled ? 1.0f : 0.3f, 0.3f);
    renderBitmapString(20.0f, screenHeight - 80.0f, GLUT_BITMAP_HELVETICA_12, lightStr);

    std::string texStr = textureEnabled ? "Texture Mapping: ON [T]" : "Texture Mapping: OFF [T]";
    glColor3f(textureEnabled ? 0.3f : 0.8f, textureEnabled ? 1.0f : 0.3f, 0.3f);
    renderBitmapString(20.0f, screenHeight - 100.0f, GLUT_BITMAP_HELVETICA_12, texStr);

    std::string shadeStr = smoothShading ? "Shading: SMOOTH GOURAUD [S]" : "Shading: FLAT [S]";
    glColor3f(1.0f, 0.8f, 0.2f);
    renderBitmapString(20.0f, screenHeight - 120.0f, GLUT_BITMAP_HELVETICA_12, shadeStr);

    std::string filterStr = linearFilter ? "Filtering: GL_LINEAR (Bilinear) [F]" : "Filtering: GL_NEAREST (Pixelated) [F]";
    glColor3f(0.4f, 0.8f, 1.0f);
    renderBitmapString(20.0f, screenHeight - 140.0f, GLUT_BITMAP_HELVETICA_12, filterStr);

    std::stringstream ss;
    ss << "Ambient: [" << (ambientOn ? "ON" : "OFF") << "] (Key 7) | Diffuse: ["
       << (diffuseOn ? "ON" : "OFF") << "] (Key 8) | Specular: ["
       << (specularOn ? "ON" : "OFF") << "] (Key 9)";
    glColor3f(0.8f, 0.85f, 0.95f);
    renderBitmapString(20.0f, screenHeight - 160.0f, GLUT_BITMAP_HELVETICA_12, ss.str());

    ss.str("");
    ss << "Material Shininess Exponent: " << static_cast<int>(matShininess) << " [+/- to adjust]";
    glColor3f(1.0f, 0.5f, 0.8f);
    renderBitmapString(20.0f, screenHeight - 180.0f, GLUT_BITMAP_HELVETICA_12, ss.str());

    // Navigation Help
    glColor3f(0.6f, 0.6f, 0.7f);
    renderBitmapString(20.0f, 20.0f, GLUT_BITMAP_HELVETICA_12,
                       "[L] Light | [T] Texture | [S] Shading | [7/8/9] Light Components | [F] Filter | [+/-] Shininess | [R] Reset");

    endHUD();
}

} // namespace CGL
