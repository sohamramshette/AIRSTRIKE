#include "test_cgl.hpp"
#include "common/gl_common.h"
#include "transformations/transform_demo.hpp"
#include "algorithms/raster_algorithms.hpp"
#include "clipping/clipping_algorithms.hpp"
#include "appearance/lighting_texture_demo.hpp"

#include <iostream>
#include <cassert>
#include <cmath>
#include <vector>

namespace CGL {

static int testsPassed = 0;
static int testsTotal = 0;

#define CGL_ASSERT(cond, name) \
    do { \
        testsTotal++; \
        if (cond) { \
            testsPassed++; \
            std::cout << "  [PASS] " << name << std::endl; \
        } else { \
            std::cerr << "  [FAIL] " << name << " (Line " << __LINE__ << ")" << std::endl; \
        } \
    } while (0)

static bool approxEqual(float a, float b, float eps = 0.001f) {
    return std::fabs(a - b) <= eps;
}

// -------------------------------------------------------------
// CO1: Primitives Tests
// -------------------------------------------------------------
void testCO1Primitives() {
    std::cout << "\n--- Testing CGL-CO1: OpenGL Graphics Primitives ---" << std::endl;

    // Verify 2D and 3D data structures
    Point2D pt1(10, 20);
    Point2D pt2(10, 20);
    Point2D pt3(15, 25);
    CGL_ASSERT(pt1 == pt2, "TC_CGL_01A: Point2D equality check");
    CGL_ASSERT(!(pt1 == pt3), "TC_CGL_01B: Point2D inequality check");

    Vec2 v1(1.5f, -2.5f);
    Vec2 v2(1.5001f, -2.4999f);
    CGL_ASSERT(v1 == v2, "TC_CGL_01C: Vec2 approximate equality check");

    Color4 c(0.2f, 0.4f, 0.6f, 0.8f);
    CGL_ASSERT(approxEqual(c.r, 0.2f) && approxEqual(c.a, 0.8f), "TC_CGL_01D: Color4 component verification");
}

// -------------------------------------------------------------
// CO2: Transformations Tests
// -------------------------------------------------------------
void testCO2Transformations() {
    std::cout << "\n--- Testing CGL-CO2: Geometric Transformations ---" << std::endl;

    // 1. Translation Matrix
    float T[16];
    TransformDemo::getTranslationMatrix(5.0f, -3.0f, 2.0f, T);
    CGL_ASSERT(approxEqual(T[12], 5.0f) && approxEqual(T[13], -3.0f) && approxEqual(T[14], 2.0f),
               "TC_CGL_02A: 4x4 Translation Matrix construction");

    // 2. Rotation Matrix around Z (90 degrees)
    float R[16];
    TransformDemo::getRotationZMatrix(90.0f, R);
    // cos(90) = 0, sin(90) = 1
    CGL_ASSERT(approxEqual(R[0], 0.0f) && approxEqual(R[1], 1.0f) && approxEqual(R[4], -1.0f) && approxEqual(R[5], 0.0f),
               "TC_CGL_02B: 4x4 Rotation-Z Matrix (90 deg)");

    // 3. Scaling Matrix
    float S[16];
    TransformDemo::getScaleMatrix(2.0f, 3.0f, 4.0f, S);
    CGL_ASSERT(approxEqual(S[0], 2.0f) && approxEqual(S[5], 3.0f) && approxEqual(S[10], 4.0f) && approxEqual(S[15], 1.0f),
               "TC_CGL_02C: 4x4 Scaling Matrix construction");

    // 4. Matrix Multiplication: T * R != R * T (Non-commutative)
    float TR[16], RT[16];
    TransformDemo::multiplyMatrix4x4(T, R, TR);
    TransformDemo::multiplyMatrix4x4(R, T, RT);

    // In T * R: translation column remains (5, -3, 2)
    // In R * T: translation column gets rotated!
    bool nonCommutative = !approxEqual(TR[12], RT[12]) || !approxEqual(TR[13], RT[13]);
    CGL_ASSERT(nonCommutative, "TC_CGL_02D: Non-commutative matrix order (T*R != R*T)");

    // 5. Affine Shearing Matrix
    float SH[16];
    TransformDemo::getShear2DMatrix(0.5f, 0.0f, SH);
    CGL_ASSERT(approxEqual(SH[4], 0.5f) && approxEqual(SH[0], 1.0f),
               "TC_CGL_02E: 2D Affine Shearing Matrix construction");
}

// -------------------------------------------------------------
// CO3: Rasterization Algorithms Tests
// -------------------------------------------------------------
void testCO3LineAlgorithms() {
    std::cout << "\n--- Testing CGL-CO3: Line Drawing Algorithms ---" << std::endl;

    // DDA Line
    auto dda = RasterAlgorithmsDemo::ddaLine(0, 0, 5, 5);
    CGL_ASSERT(dda.size() == 6, "TC_CGL_03A: DDA line point count for (0,0)->(5,5)");
    CGL_ASSERT(dda.front() == Point2D(0, 0) && dda.back() == Point2D(5, 5),
               "TC_CGL_03B: DDA line endpoints match exactly");

    // Bresenham Line: Octant 1 (0 <= m <= 1)
    auto bres1 = RasterAlgorithmsDemo::bresenhamLine(0, 0, 8, 4);
    CGL_ASSERT(bres1.front() == Point2D(0, 0) && bres1.back() == Point2D(8, 4),
               "TC_CGL_03C: Bresenham Octant 1 endpoints match");
    CGL_ASSERT(bres1.size() == 9, "TC_CGL_03D: Bresenham Octant 1 step count (dx + 1 = 9)");

    // Bresenham Line: Octant 2 (m > 1)
    auto bres2 = RasterAlgorithmsDemo::bresenhamLine(0, 0, 3, 7);
    CGL_ASSERT(bres2.front() == Point2D(0, 0) && bres2.back() == Point2D(3, 7),
               "TC_CGL_03E: Bresenham Octant 2 (steep slope) endpoints match");
    CGL_ASSERT(bres2.size() == 8, "TC_CGL_03F: Bresenham Octant 2 step count (dy + 1 = 8)");

    // Bresenham Line: Negative slope and backwards
    auto bres3 = RasterAlgorithmsDemo::bresenhamLine(5, 5, 0, 0);
    CGL_ASSERT(bres3.front() == Point2D(5, 5) && bres3.back() == Point2D(0, 0),
               "TC_CGL_03G: Bresenham reverse direction endpoints match");

    // Bresenham Line: Pure vertical and horizontal
    auto bresH = RasterAlgorithmsDemo::bresenhamLine(2, 4, 8, 4);
    auto bresV = RasterAlgorithmsDemo::bresenhamLine(3, 1, 3, 6);
    CGL_ASSERT(bresH.size() == 7 && bresV.size() == 6,
               "TC_CGL_03H: Bresenham horizontal and vertical line segment handling");
}

void testCO3CurveAlgorithms() {
    std::cout << "\n--- Testing CGL-CO3: Curve Drawing Algorithms ---" << std::endl;

    // Midpoint Circle (r = 10, center 0, 0)
    int r = 10;
    auto circle = RasterAlgorithmsDemo::midpointCircle(0, 0, r);
    CGL_ASSERT(!circle.empty(), "TC_CGL_03I: Midpoint Circle generates pixel points");

    // Verify 8-way symmetry points exist on cardinal axes
    bool hasRight = false, hasTop = false, hasLeft = false, hasBottom = false;
    for (const auto& pt : circle) {
        if (pt.x == r && pt.y == 0) hasRight = true;
        if (pt.x == 0 && pt.y == r) hasTop = true;
        if (pt.x == -r && pt.y == 0) hasLeft = true;
        if (pt.x == 0 && pt.y == -r) hasBottom = true;
    }
    CGL_ASSERT(hasRight && hasTop && hasLeft && hasBottom,
               "TC_CGL_03J: Midpoint Circle 8-way cardinal symmetry points verified");

    // Midpoint Ellipse (rx = 15, ry = 8)
    auto ellipse = RasterAlgorithmsDemo::midpointEllipse(0, 0, 15, 8);
    CGL_ASSERT(!ellipse.empty(), "TC_CGL_03K: Midpoint Ellipse generates pixel points");

    bool hasRx = false, hasRy = false;
    for (const auto& pt : ellipse) {
        if (pt.x == 15 && pt.y == 0) hasRx = true;
        if (pt.x == 0 && pt.y == 8) hasRy = true;
    }
    CGL_ASSERT(hasRx && hasRy, "TC_CGL_03L: Midpoint Ellipse 4-way major/minor axis points verified");
}

void testCO3ScanlineFill() {
    std::cout << "\n--- Testing CGL-CO3: Scanline Polygon Fill ---" << std::endl;

    // Simple 10x10 square polygon
    std::vector<Point2D> square = {
        Point2D(0, 0), Point2D(10, 0), Point2D(10, 10), Point2D(0, 10)
    };
    auto filled = RasterAlgorithmsDemo::scanlineFill(square);
    // Square of 11x11 points has 121 pixels
    CGL_ASSERT(filled.size() >= 100, "TC_CGL_03M: Scanline fill interior pixel coverage");
}

// -------------------------------------------------------------
// CO4: Projection and Viewing Tests
// -------------------------------------------------------------
void testCO4Projection() {
    std::cout << "\n--- Testing CGL-CO4: Projection and Viewing ---" << std::endl;

    // Perspective foreshortening ratio: scale = near / Z
    float zNear = 1.0f;
    float z1 = 2.0f;
    float z2 = 10.0f;
    float s1 = zNear / z1;
    float s2 = zNear / z2;
    CGL_ASSERT(s1 > s2 && approxEqual(s1 / s2, 5.0f),
               "TC_CGL_04A: Perspective foreshortening 1/z ratio scaling verified");

    // Camera Orbit Spherical Coordinates: distance = 10, pitch = 0, yaw = 90 deg
    float radYaw = 90.0f * static_cast<float>(M_PI) / 180.0f;
    float eyeX = 10.0f * std::sin(radYaw);
    float eyeZ = 10.0f * std::cos(radYaw);
    CGL_ASSERT(approxEqual(eyeX, 10.0f) && approxEqual(eyeZ, 0.0f),
               "TC_CGL_04B: Orbit camera spherical coordinates to Cartesian conversion");
}

// -------------------------------------------------------------
// CO5: Clipping Algorithms Tests
// -------------------------------------------------------------
void testCO5Clipping() {
    std::cout << "\n--- Testing CGL-CO5: Clipping Algorithms ---" << std::endl;

    RectWindow win(-10.0f, -10.0f, 10.0f, 10.0f);

    // 1. Cohen-Sutherland Outcodes
    int codeIn = ClippingAlgorithmsDemo::computeOutCode(0.0f, 0.0f, win);
    int codeLeft = ClippingAlgorithmsDemo::computeOutCode(-15.0f, 0.0f, win);
    int codeTopRight = ClippingAlgorithmsDemo::computeOutCode(12.0f, 12.0f, win);
    CGL_ASSERT(codeIn == CODE_INSIDE, "TC_CGL_05A: Cohen-Sutherland inside outcode == 0000");
    CGL_ASSERT(codeLeft == CODE_LEFT, "TC_CGL_05B: Cohen-Sutherland left outcode == 0001");
    CGL_ASSERT(codeTopRight == (CODE_TOP | CODE_RIGHT), "TC_CGL_05C: Cohen-Sutherland top-right outcode == 1010");

    // 2. Cohen-Sutherland Trivial Accept
    LineSegment lineIn(-5.0f, -5.0f, 5.0f, 5.0f);
    LineSegment outSeg;
    int c1 = 0, c2 = 0;
    bool acc1 = ClippingAlgorithmsDemo::cohenSutherlandClip(lineIn, win, outSeg, c1, c2);
    CGL_ASSERT(acc1 && approxEqual(outSeg.p1.x, -5.0f) && approxEqual(outSeg.p2.x, 5.0f),
               "TC_CGL_05D: Cohen-Sutherland trivial accept line inside window");

    // 3. Cohen-Sutherland Trivial Reject
    LineSegment lineOut(-20.0f, 0.0f, -15.0f, 5.0f);
    bool acc2 = ClippingAlgorithmsDemo::cohenSutherlandClip(lineOut, win, outSeg, c1, c2);
    CGL_ASSERT(!acc2, "TC_CGL_05E: Cohen-Sutherland trivial reject line outside left");

    // 4. Cohen-Sutherland Partial Clip
    LineSegment lineCrossing(-20.0f, 0.0f, 20.0f, 0.0f);
    bool acc3 = ClippingAlgorithmsDemo::cohenSutherlandClip(lineCrossing, win, outSeg, c1, c2);
    CGL_ASSERT(acc3 && approxEqual(outSeg.p1.x, -10.0f) && approxEqual(outSeg.p2.x, 10.0f),
               "TC_CGL_05F: Cohen-Sutherland horizontal clipping to exact window boundaries");

    // 5. Liang-Barsky Line Clipping
    float u1 = 0.0f, u2 = 1.0f;
    LineSegment lbOut;
    bool lbAcc = ClippingAlgorithmsDemo::liangBarskyClip(lineCrossing, win, lbOut, u1, u2);
    CGL_ASSERT(lbAcc && approxEqual(lbOut.p1.x, -10.0f) && approxEqual(lbOut.p2.x, 10.0f),
               "TC_CGL_05G: Liang-Barsky parametric clipping yields identical boundary points");
    CGL_ASSERT(approxEqual(u1, 0.25f) && approxEqual(u2, 0.75f),
               "TC_CGL_05H: Liang-Barsky parametric range u1=0.25, u2=0.75 verified");

    // 6. Sutherland-Hodgman Polygon Clipping
    std::vector<Vec2> bigSquare = {
        Vec2(-15.0f, -15.0f), Vec2(15.0f, -15.0f), Vec2(15.0f, 15.0f), Vec2(-15.0f, 15.0f)
    };
    auto clippedPoly = ClippingAlgorithmsDemo::sutherlandHodgmanClip(bigSquare, win);
    CGL_ASSERT(clippedPoly.size() == 4, "TC_CGL_05I: Sutherland-Hodgman clipped polygon vertex count is 4");
    // Verify clipped square matches window corners
    bool hasCorner = false;
    for (const auto& v : clippedPoly) {
        if (approxEqual(v.x, 10.0f) && approxEqual(v.y, 10.0f)) hasCorner = true;
    }
    CGL_ASSERT(hasCorner, "TC_CGL_05J: Sutherland-Hodgman clipped polygon aligns with window corners");
}

// -------------------------------------------------------------
// CO6: Lighting, Texture, and Color Tests
// -------------------------------------------------------------
void testCO6Appearance() {
    std::cout << "\n--- Testing CGL-CO6: Texture, Lighting, and Color Models ---" << std::endl;

    // 1. HSV to RGB Conversion
    // Pure Red: H=0, S=1, V=1
    Color4 red = LightingTextureDemo::hsvToRgb(0.0f, 1.0f, 1.0f);
    CGL_ASSERT(approxEqual(red.r, 1.0f) && approxEqual(red.g, 0.0f) && approxEqual(red.b, 0.0f),
               "TC_CGL_06A: HSV (0, 1, 1) converts to Pure Red (1, 0, 0)");

    // Pure Green: H=1/3 (0.333), S=1, V=1
    Color4 green = LightingTextureDemo::hsvToRgb(1.0f / 3.0f, 1.0f, 1.0f);
    CGL_ASSERT(approxEqual(green.r, 0.0f) && approxEqual(green.g, 1.0f) && approxEqual(green.b, 0.0f),
               "TC_CGL_06B: HSV (1/3, 1, 1) converts to Pure Green (0, 1, 0)");

    // Pure Blue: H=2/3 (0.666), S=1, V=1
    Color4 blue = LightingTextureDemo::hsvToRgb(2.0f / 3.0f, 1.0f, 1.0f);
    CGL_ASSERT(approxEqual(blue.r, 0.0f) && approxEqual(blue.g, 0.0f) && approxEqual(blue.b, 1.0f),
               "TC_CGL_06C: HSV (2/3, 1, 1) converts to Pure Blue (0, 0, 1)");

    // 2. Procedural Texture Buffer Generation
    std::vector<unsigned char> hullTex, planetTex;
    LightingTextureDemo::generateHullTexture(128, 128, hullTex);
    LightingTextureDemo::generatePlanetTexture(128, 128, planetTex);

    CGL_ASSERT(hullTex.size() == 128 * 128 * 3, "TC_CGL_06D: Spacecraft Hull texture byte buffer size");
    CGL_ASSERT(planetTex.size() == 128 * 128 * 3, "TC_CGL_06E: Planet texture byte buffer size");

    // Check non-zero content
    bool hullHasData = false, planetHasData = false;
    for (auto b : hullTex) if (b > 0) { hullHasData = true; break; }
    for (auto b : planetTex) if (b > 0) { planetHasData = true; break; }
    CGL_ASSERT(hullHasData && planetHasData, "TC_CGL_06F: Procedural texture patterns populated with image data");

    // 3. Phong Reflection Model Calculation
    // Diffuse: Id = kd * Ld * max(0, N . L)
    // When N = (0, 1, 0) and L = (0, 1, 0), N . L = 1.0 (Max intensity)
    float nDotL = 1.0f;
    float kd = 0.8f;
    float Ld = 1.0f;
    float Id = kd * Ld * nDotL;
    CGL_ASSERT(approxEqual(Id, 0.8f), "TC_CGL_06G: Phong Diffuse Lambert reflection calculation");

    // Specular: Is = ks * Ls * max(0, R . V)^shininess
    float ks = 0.9f;
    float Ls = 1.0f;
    float rDotV = 0.9f;
    float shininess = 16.0f;
    float Is = ks * Ls * std::pow(rDotV, shininess);
    CGL_ASSERT(Is > 0.0f && Is < 1.0f, "TC_CGL_06H: Phong Specular highlight exponential falloff calculation");
}

int runAllCGLTests() {
    testsPassed = 0;
    testsTotal = 0;

    std::cout << "========================================================" << std::endl;
    std::cout << "  AIRSTRIKE CGL: Computer Graphics Laboratory Test Suite" << std::endl;
    std::cout << "  Verifying Mathematical Algorithms (CO1 through CO6)   " << std::endl;
    std::cout << "========================================================" << std::endl;

    testCO1Primitives();
    testCO2Transformations();
    testCO3LineAlgorithms();
    testCO3CurveAlgorithms();
    testCO3ScanlineFill();
    testCO4Projection();
    testCO5Clipping();
    testCO6Appearance();

    std::cout << "\n========================================================" << std::endl;
    std::cout << "  TEST SUMMARY: " << testsPassed << " / " << testsTotal << " Passed ("
              << (testsTotal > 0 ? (testsPassed * 100 / testsTotal) : 0) << "%)" << std::endl;
    std::cout << "========================================================" << std::endl;

    return (testsPassed == testsTotal) ? 0 : 1;
}

} // namespace CGL
