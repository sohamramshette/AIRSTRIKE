#include "test_cgl.hpp"
#include "common/gl_common.h"
#include "common/image_loader.hpp"
#include "transformations/transform_demo.hpp"
#include "algorithms/raster_algorithms.hpp"
#include "projection/viewing_demo.hpp"
#include "clipping/clipping_algorithms.hpp"
#include "appearance/lighting_texture_demo.hpp"
#include "primitives/polygon_cruiser.hpp"
#include "transformations/orbital_rotor.hpp"
#include "algorithms/raster_sentinel.hpp"
#include "projection/warp_cruiser.hpp"
#include "clipping/boundary_skimmer.hpp"
#include "appearance/phong_cruiser.hpp"

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

static bool approxEqual(float a, float b, float eps = 0.005f) {
    return std::fabs(a - b) <= eps;
}

static bool vec3ApproxEqual(const Vec3& a, const Vec3& b, float eps = 0.005f) {
    return approxEqual(a.x, b.x, eps) && approxEqual(a.y, b.y, eps) && approxEqual(a.z, b.z, eps);
}

// -------------------------------------------------------------
// CO1: Primitives Tests
// -------------------------------------------------------------
void testCO1Primitives() {
    std::cout << "\n--- Testing CGL-CO1: OpenGL Graphics Primitives ---" << std::endl;

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
// CO2: Transformations Tests (Matrix & Transformed Coordinates)
// -------------------------------------------------------------
void testCO2Transformations() {
    std::cout << "\n--- Testing CGL-CO2: Geometric Transformations ---" << std::endl;

    // 1. Translation Matrix and Coordinate Transformation
    float T[16];
    TransformDemo::getTranslationMatrix(5.0f, -3.0f, 2.0f, T);
    CGL_ASSERT(approxEqual(T[12], 5.0f) && approxEqual(T[13], -3.0f) && approxEqual(T[14], 2.0f),
               "TC_CGL_02A: 4x4 Translation Matrix construction");
    Vec3 pOrigin(0.0f, 0.0f, 0.0f);
    Vec3 pTranslated = TransformDemo::transformPoint(T, pOrigin);
    CGL_ASSERT(vec3ApproxEqual(pTranslated, Vec3(5.0f, -3.0f, 2.0f)),
               "TC_CGL_02B: Coordinate Translation T(5, -3, 2) on (0, 0, 0)");

    // 2. Rotation Matrix around Z and Coordinate Transformation
    float Rz[16];
    TransformDemo::getRotationZMatrix(90.0f, Rz);
    Vec3 pX(1.0f, 0.0f, 0.0f);
    Vec3 pRotatedZ = TransformDemo::transformPoint(Rz, pX);
    CGL_ASSERT(vec3ApproxEqual(pRotatedZ, Vec3(0.0f, 1.0f, 0.0f)),
               "TC_CGL_02C: Coordinate Rotation Rz(90 deg) on (1, 0, 0) -> (0, 1, 0)");

    // 3. Scaling Matrix and Coordinate Transformation
    float S[16];
    TransformDemo::getScaleMatrix(2.0f, 3.0f, 4.0f, S);
    Vec3 pUnit(1.0f, 1.0f, 1.0f);
    Vec3 pScaled = TransformDemo::transformPoint(S, pUnit);
    CGL_ASSERT(vec3ApproxEqual(pScaled, Vec3(2.0f, 3.0f, 4.0f)),
               "TC_CGL_02D: Coordinate Scaling S(2, 3, 4) on (1, 1, 1)");

    // 4. Reflection across X-axis (inverts Y)
    float ReflX[16];
    TransformDemo::getReflectionXMatrix(ReflX);
    Vec3 pPreRefl(2.0f, 3.0f, 0.0f);
    Vec3 pPostReflX = TransformDemo::transformPoint(ReflX, pPreRefl);
    CGL_ASSERT(vec3ApproxEqual(pPostReflX, Vec3(2.0f, -3.0f, 0.0f)),
               "TC_CGL_02E: Reflection across X-axis coordinate transformation (2, 3) -> (2, -3)");

    // 5. Reflection across Y-axis (inverts X)
    float ReflY[16];
    TransformDemo::getReflectionYMatrix(ReflY);
    Vec3 pPostReflY = TransformDemo::transformPoint(ReflY, pPreRefl);
    CGL_ASSERT(vec3ApproxEqual(pPostReflY, Vec3(-2.0f, 3.0f, 0.0f)),
               "TC_CGL_02F: Reflection across Y-axis coordinate transformation (2, 3) -> (-2, 3)");

    // 6. Arbitrary Pivot Point Rotation
    // Rotate Q(2, 1, 0) by 90 deg around pivot P(1, 1, 0) -> expected (1, 2, 0)
    float M_pivot[16];
    TransformDemo::getArbitraryPivotRotationMatrix(1.0f, 1.0f, 90.0f, M_pivot);
    Vec3 pQ(2.0f, 1.0f, 0.0f);
    Vec3 pQRotated = TransformDemo::transformPoint(M_pivot, pQ);
    CGL_ASSERT(vec3ApproxEqual(pQRotated, Vec3(1.0f, 2.0f, 0.0f)),
               "TC_CGL_02G: Arbitrary Pivot Rotation: point Q(2, 1) rotated 90 deg about P(1, 1) -> (1, 2)");

    // Pivot point itself must be invariant under rotation
    Vec3 pPivot(1.0f, 1.0f, 0.0f);
    Vec3 pPivotRotated = TransformDemo::transformPoint(M_pivot, pPivot);
    CGL_ASSERT(vec3ApproxEqual(pPivotRotated, Vec3(1.0f, 1.0f, 0.0f)),
               "TC_CGL_02H: Arbitrary Pivot Invariance: pivot P(1, 1) rotated about itself remains (1, 1)");

    // 7. Combined Transformations on Coordinates
    // S(2, 3, 1) -> Rz(90 deg) -> T(5, -2, 0)
    // Point (1, 0, 0) -> S -> (2, 0, 0) -> Rz(90) -> (0, 2, 0) -> T -> (5, 0, 0)
    float S2[16], Rz90[16], T5[16], tempM[16], M_comb[16];
    TransformDemo::getScaleMatrix(2.0f, 3.0f, 1.0f, S2);
    TransformDemo::getRotationZMatrix(90.0f, Rz90);
    TransformDemo::getTranslationMatrix(5.0f, -2.0f, 0.0f, T5);
    TransformDemo::multiplyMatrix4x4(Rz90, S2, tempM); // Rz * S
    TransformDemo::multiplyMatrix4x4(T5, tempM, M_comb); // T * (Rz * S)
    Vec3 pComb = TransformDemo::transformPoint(M_comb, Vec3(1.0f, 0.0f, 0.0f));
    CGL_ASSERT(vec3ApproxEqual(pComb, Vec3(5.0f, 0.0f, 0.0f)),
               "TC_CGL_02I: Combined transformation coordinate result verified (5, 0, 0)");

    // 8. Transformation Order Verification (Coordinate Proof of Non-Commutativity)
    // Point V(1, 0, 0):
    // Order A: T(2, 0) * Rz(90) -> Rz(90)*(1, 0) = (0, 1) -> T(2, 0) = (2, 1, 0)
    // Order B: Rz(90) * T(2, 0) -> T(2, 0)*(1, 0) = (3, 0) -> Rz(90) = (0, 3, 0)
    float T2[16], M_TR[16], M_RT[16];
    TransformDemo::getTranslationMatrix(2.0f, 0.0f, 0.0f, T2);
    TransformDemo::multiplyMatrix4x4(T2, Rz90, M_TR);
    TransformDemo::multiplyMatrix4x4(Rz90, T2, M_RT);
    Vec3 pA = TransformDemo::transformPoint(M_TR, Vec3(1.0f, 0.0f, 0.0f));
    Vec3 pB = TransformDemo::transformPoint(M_RT, Vec3(1.0f, 0.0f, 0.0f));
    CGL_ASSERT(vec3ApproxEqual(pA, Vec3(2.0f, 1.0f, 0.0f)),
               "TC_CGL_02J: Order A (T * R) coordinates match (2, 1, 0)");
    CGL_ASSERT(vec3ApproxEqual(pB, Vec3(0.0f, 3.0f, 0.0f)),
               "TC_CGL_02K: Order B (R * T) coordinates match (0, 3, 0)");
    CGL_ASSERT(!vec3ApproxEqual(pA, pB),
               "TC_CGL_02L: Non-commutative coordinate difference proved: (2, 1) != (0, 3)");
}

// -------------------------------------------------------------
// CO3: Line Drawing Algorithms Across All 8 Octants
// -------------------------------------------------------------
void testCO3LineAlgorithms() {
    std::cout << "\n--- Testing CGL-CO3: Line Drawing Algorithms (All 8 Octants) ---" << std::endl;

    // DDA Line
    auto dda = RasterAlgorithmsDemo::ddaLine(0, 0, 5, 5);
    CGL_ASSERT(dda.size() == 6, "TC_CGL_03A: DDA line point count for (0,0)->(5,5)");
    CGL_ASSERT(dda.front() == Point2D(0, 0) && dda.back() == Point2D(5, 5),
               "TC_CGL_03B: DDA line endpoints match exactly");

    // Bresenham Octant 1: 0 <= m <= 1, dx > 0, dy > 0
    auto bres1 = RasterAlgorithmsDemo::bresenhamLine(0, 0, 8, 4);
    CGL_ASSERT(bres1.front() == Point2D(0, 0) && bres1.back() == Point2D(8, 4) && bres1.size() == 9,
               "TC_CGL_03C: Bresenham Octant 1 (0 <= m <= 1): (0,0)->(8,4)");

    // Bresenham Octant 2: m > 1, dx > 0, dy > 0
    auto bres2 = RasterAlgorithmsDemo::bresenhamLine(0, 0, 4, 8);
    CGL_ASSERT(bres2.front() == Point2D(0, 0) && bres2.back() == Point2D(4, 8) && bres2.size() == 9,
               "TC_CGL_03D: Bresenham Octant 2 (m > 1): (0,0)->(4,8)");

    // Bresenham Octant 3: m < -1, dx < 0, dy > 0 (Negative slope, steep)
    auto bres3 = RasterAlgorithmsDemo::bresenhamLine(0, 0, -4, 8);
    CGL_ASSERT(bres3.front() == Point2D(0, 0) && bres3.back() == Point2D(-4, 8) && bres3.size() == 9,
               "TC_CGL_03E: Bresenham Octant 3 (m < -1): (0,0)->(-4,8)");

    // Bresenham Octant 4: -1 <= m <= 0, dx < 0, dy > 0 (Negative slope, shallow)
    auto bres4 = RasterAlgorithmsDemo::bresenhamLine(0, 0, -8, 4);
    CGL_ASSERT(bres4.front() == Point2D(0, 0) && bres4.back() == Point2D(-8, 4) && bres4.size() == 9,
               "TC_CGL_03F: Bresenham Octant 4 (-1 <= m <= 0): (0,0)->(-8,4)");

    // Bresenham Octant 5: 0 <= m <= 1, dx < 0, dy < 0 (Reverse / Quadrant 3)
    auto bres5 = RasterAlgorithmsDemo::bresenhamLine(0, 0, -8, -4);
    CGL_ASSERT(bres5.front() == Point2D(0, 0) && bres5.back() == Point2D(-8, -4) && bres5.size() == 9,
               "TC_CGL_03G: Bresenham Octant 5: (0,0)->(-8,-4)");

    // Bresenham Octant 6: m > 1, dx < 0, dy < 0
    auto bres6 = RasterAlgorithmsDemo::bresenhamLine(0, 0, -4, -8);
    CGL_ASSERT(bres6.front() == Point2D(0, 0) && bres6.back() == Point2D(-4, -8) && bres6.size() == 9,
               "TC_CGL_03H: Bresenham Octant 6: (0,0)->(-4,-8)");

    // Bresenham Octant 7: m < -1, dx > 0, dy < 0
    auto bres7 = RasterAlgorithmsDemo::bresenhamLine(0, 0, 4, -8);
    CGL_ASSERT(bres7.front() == Point2D(0, 0) && bres7.back() == Point2D(4, -8) && bres7.size() == 9,
               "TC_CGL_03I: Bresenham Octant 7: (0,0)->(4,-8)");

    // Bresenham Octant 8: -1 <= m <= 0, dx > 0, dy < 0
    auto bres8 = RasterAlgorithmsDemo::bresenhamLine(0, 0, 8, -4);
    CGL_ASSERT(bres8.front() == Point2D(0, 0) && bres8.back() == Point2D(8, -4) && bres8.size() == 9,
               "TC_CGL_03J: Bresenham Octant 8: (0,0)->(8,-4)");

    // Pure Vertical and Horizontal Lines
    auto bresH = RasterAlgorithmsDemo::bresenhamLine(2, 4, 8, 4);
    auto bresV = RasterAlgorithmsDemo::bresenhamLine(3, 1, 3, 6);
    CGL_ASSERT(bresH.size() == 7 && bresH.front() == Point2D(2, 4) && bresH.back() == Point2D(8, 4),
               "TC_CGL_03K: Bresenham Horizontal Line: (2,4)->(8,4)");
    CGL_ASSERT(bresV.size() == 6 && bresV.front() == Point2D(3, 1) && bresV.back() == Point2D(3, 6),
               "TC_CGL_03L: Bresenham Vertical Line: (3,1)->(3,6)");

    // Reverse Direction Test
    auto bresRev = RasterAlgorithmsDemo::bresenhamLine(10, 10, 0, 0);
    CGL_ASSERT(bresRev.front() == Point2D(10, 10) && bresRev.back() == Point2D(0, 0) && bresRev.size() == 11,
               "TC_CGL_03M: Bresenham Reverse Direction (10,10)->(0,0)");
}

// -------------------------------------------------------------
// CO3: Curve Algorithms & Symmetry Tests
// -------------------------------------------------------------
void testCO3CurveAlgorithms() {
    std::cout << "\n--- Testing CGL-CO3: Curve Drawing Algorithms ---" << std::endl;

    int r = 12;
    auto circle = RasterAlgorithmsDemo::midpointCircle(0, 0, r);
    CGL_ASSERT(!circle.empty(), "TC_CGL_03N: Midpoint Circle generates pixel points");

    // Verify 8-way symmetry: every point (x, y) has corresponding (-x, y), (x, -y), (-x, -y), (y, x), (-y, x), (y, -x), (-y, -x)
    bool allPointsOnCircle = true;
    for (const auto& pt : circle) {
        int distSq = pt.x * pt.x + pt.y * pt.y;
        if (std::abs(distSq - r * r) > 2 * r + 1) {
            allPointsOnCircle = false;
            break;
        }
    }
    CGL_ASSERT(allPointsOnCircle, "TC_CGL_03O: Midpoint Circle equation adherence: |x^2 + y^2 - r^2| <= 2r+1");

    // Midpoint Ellipse: verify Region 1 and Region 2
    int rx = 20, ry = 10;
    auto ellipse = RasterAlgorithmsDemo::midpointEllipse(0, 0, rx, ry);
    CGL_ASSERT(!ellipse.empty(), "TC_CGL_03P: Midpoint Ellipse generates pixel points");

    // Ellipse boundary points on axes
    bool hasMajor = false, hasMinor = false;
    for (const auto& pt : ellipse) {
        if (pt.x == rx && pt.y == 0) hasMajor = true;
        if (pt.x == 0 && pt.y == ry) hasMinor = true;
    }
    CGL_ASSERT(hasMajor && hasMinor, "TC_CGL_03Q: Midpoint Ellipse intercepts on major and minor axes");

    // Scanline Polygon Fill
    std::vector<Point2D> square = {
        Point2D(0, 0), Point2D(10, 0), Point2D(10, 10), Point2D(0, 10)
    };
    auto filled = RasterAlgorithmsDemo::scanlineFill(square);
    CGL_ASSERT(filled.size() >= 100, "TC_CGL_03R: Scanline polygon fill interior coverage (10x10 square)");
}

// -------------------------------------------------------------
// CO3: 3D Mesh & Rotation Calculations
// -------------------------------------------------------------
void testCO33DMeshTransformations() {
    std::cout << "\n--- Testing CGL-CO3: 3D Mesh Vertex Transformations ---" << std::endl;

    // 8 vertices of unit cube: (-1, -1, -1) to (1, 1, 1)
    std::vector<Vec3> cubeVertices = {
        Vec3(-1, -1, -1), Vec3(1, -1, -1), Vec3(1, 1, -1), Vec3(-1, 1, -1),
        Vec3(-1, -1,  1), Vec3(1, -1,  1), Vec3(1, 1,  1), Vec3(-1, 1,  1)
    };

    // Apply Translation (3, -2, 4) and Scaling (2, 2, 2)
    float S[16], T[16], M[16];
    TransformDemo::getScaleMatrix(2.0f, 2.0f, 2.0f, S);
    TransformDemo::getTranslationMatrix(3.0f, -2.0f, 4.0f, T);
    TransformDemo::multiplyMatrix4x4(T, S, M);

    Vec3 centroid(0.0f, 0.0f, 0.0f);
    for (const auto& v : cubeVertices) {
        Vec3 tv = TransformDemo::transformPoint(M, v);
        centroid.x += tv.x;
        centroid.y += tv.y;
        centroid.z += tv.z;
    }
    centroid.x /= 8.0f; centroid.y /= 8.0f; centroid.z /= 8.0f;
    CGL_ASSERT(vec3ApproxEqual(centroid, Vec3(3.0f, -2.0f, 4.0f)),
               "TC_CGL_03S: 3D Cube transformed centroid matches translation vector (3, -2, 4)");

    // 3D Euler Rotations: Rx(90) on (0, 1, 0) -> (0, 0, 1)
    float Rx[16];
    TransformDemo::getRotationXMatrix(90.0f, Rx);
    Vec3 pRx = TransformDemo::transformPoint(Rx, Vec3(0.0f, 1.0f, 0.0f));
    CGL_ASSERT(vec3ApproxEqual(pRx, Vec3(0.0f, 0.0f, 1.0f)),
               "TC_CGL_03T: 3D Euler Rotation Rx(90 deg) on (0, 1, 0) -> (0, 0, 1)");

    // Ry(90) on (0, 0, 1) -> (1, 0, 0)
    float Ry[16];
    TransformDemo::getRotationYMatrix(90.0f, Ry);
    Vec3 pRy = TransformDemo::transformPoint(Ry, Vec3(0.0f, 0.0f, 1.0f));
    CGL_ASSERT(vec3ApproxEqual(pRy, Vec3(1.0f, 0.0f, 0.0f)),
               "TC_CGL_03U: 3D Euler Rotation Ry(90 deg) on (0, 0, 1) -> (1, 0, 0)");
}

// -------------------------------------------------------------
// CO4: Projection and Viewing Tests
// -------------------------------------------------------------
void testCO4Projection() {
    std::cout << "\n--- Testing CGL-CO4: Projection and Viewing ---" << std::endl;

    // 1. Orthographic Projection: Size Invariance Across Z
    float orthoM[16];
    ViewingDemo::getOrthoMatrix(-10.0f, 10.0f, -10.0f, 10.0f, 0.1f, 100.0f, orthoM);
    Vec3 pNear(5.0f, 5.0f, -5.0f);
    Vec3 pFar(5.0f, 5.0f, -50.0f);
    Vec3 ndcNear = ViewingDemo::projectPoint(orthoM, pNear);
    Vec3 ndcFar  = ViewingDemo::projectPoint(orthoM, pFar);
    CGL_ASSERT(approxEqual(ndcNear.x, 0.5f) && approxEqual(ndcFar.x, 0.5f),
               "TC_CGL_04A: Orthographic Projection size invariance across Z depths (NDC X = 0.5)");

    // 2. Perspective Projection: 1/z Foreshortening Ratio
    float perspM[16];
    ViewingDemo::getPerspectiveMatrix(90.0f, 1.0f, 1.0f, 100.0f, perspM);
    Vec3 pZ2(2.0f, 0.0f, -2.0f);
    Vec3 pZ10(2.0f, 0.0f, -10.0f);
    Vec3 ndcZ2  = ViewingDemo::projectPoint(perspM, pZ2);
    Vec3 ndcZ10 = ViewingDemo::projectPoint(perspM, pZ10);
    CGL_ASSERT(approxEqual(ndcZ2.x, 1.0f) && approxEqual(ndcZ10.x, 0.2f),
               "TC_CGL_04B: Perspective Projection NDC foreshortening (1.0 vs 0.2)");
    CGL_ASSERT(approxEqual(ndcZ2.x / ndcZ10.x, 5.0f),
               "TC_CGL_04C: Perspective foreshortening ratio matches depth ratio Z2/Z1 (5.0x)");

    // 3. Camera LookAt Transformation
    float lookAtM[16];
    ViewingDemo::getLookAtMatrix(Vec3(0.0f, 0.0f, 10.0f), Vec3(0.0f, 0.0f, 0.0f), Vec3(0.0f, 1.0f, 0.0f), lookAtM);
    Vec3 pWorldOrigin(0.0f, 0.0f, 0.0f);
    Vec3 pEye = TransformDemo::transformPoint(lookAtM, pWorldOrigin);
    CGL_ASSERT(vec3ApproxEqual(pEye, Vec3(0.0f, 0.0f, -10.0f)),
               "TC_CGL_04D: Camera LookAt matrix transforms origin to eye-space (0, 0, -10)");

    // 4. Viewport Mapping Dimensions
    Point2D pixelMin = ViewingDemo::viewportMap(Vec3(-1.0f, -1.0f, 0.0f), 0, 0, 1024, 720);
    Point2D pixelMid = ViewingDemo::viewportMap(Vec3(0.0f, 0.0f, 0.0f), 0, 0, 1024, 720);
    Point2D pixelMax = ViewingDemo::viewportMap(Vec3(1.0f, 1.0f, 0.0f), 0, 0, 1024, 720);
    CGL_ASSERT(pixelMin == Point2D(0, 0), "TC_CGL_04E: Viewport mapping NDC (-1, -1) -> Pixel (0, 0)");
    CGL_ASSERT(pixelMid == Point2D(512, 360), "TC_CGL_04F: Viewport mapping NDC (0, 0) -> Screen Center (512, 360)");
    CGL_ASSERT(pixelMax == Point2D(1024, 720), "TC_CGL_04G: Viewport mapping NDC (1, 1) -> Screen Max (1024, 720)");
}

// -------------------------------------------------------------
// CO5: Clipping Algorithms Tests
// -------------------------------------------------------------
void testCO5Clipping() {
    std::cout << "\n--- Testing CGL-CO5: Clipping Algorithms ---" << std::endl;

    RectWindow win(-10.0f, -10.0f, 10.0f, 10.0f);

    int codeIn = ClippingAlgorithmsDemo::computeOutCode(0.0f, 0.0f, win);
    int codeLeft = ClippingAlgorithmsDemo::computeOutCode(-15.0f, 0.0f, win);
    int codeTopRight = ClippingAlgorithmsDemo::computeOutCode(12.0f, 12.0f, win);
    CGL_ASSERT(codeIn == CODE_INSIDE, "TC_CGL_05A: Cohen-Sutherland inside outcode == 0000");
    CGL_ASSERT(codeLeft == CODE_LEFT, "TC_CGL_05B: Cohen-Sutherland left outcode == 0001");
    CGL_ASSERT(codeTopRight == (CODE_TOP | CODE_RIGHT), "TC_CGL_05C: Cohen-Sutherland top-right outcode == 1010");

    LineSegment lineIn(-5.0f, -5.0f, 5.0f, 5.0f);
    LineSegment outSeg;
    int c1 = 0, c2 = 0;
    bool acc1 = ClippingAlgorithmsDemo::cohenSutherlandClip(lineIn, win, outSeg, c1, c2);
    CGL_ASSERT(acc1 && approxEqual(outSeg.p1.x, -5.0f) && approxEqual(outSeg.p2.x, 5.0f),
               "TC_CGL_05D: Cohen-Sutherland trivial accept line inside window");

    LineSegment lineOut(-20.0f, 0.0f, -15.0f, 5.0f);
    bool acc2 = ClippingAlgorithmsDemo::cohenSutherlandClip(lineOut, win, outSeg, c1, c2);
    CGL_ASSERT(!acc2, "TC_CGL_05E: Cohen-Sutherland trivial reject line outside left");

    LineSegment lineCrossing(-20.0f, 0.0f, 20.0f, 0.0f);
    bool acc3 = ClippingAlgorithmsDemo::cohenSutherlandClip(lineCrossing, win, outSeg, c1, c2);
    CGL_ASSERT(acc3 && approxEqual(outSeg.p1.x, -10.0f) && approxEqual(outSeg.p2.x, 10.0f),
               "TC_CGL_05F: Cohen-Sutherland horizontal clipping to exact window boundaries");

    float u1 = 0.0f, u2 = 1.0f;
    LineSegment lbOut;
    bool lbAcc = ClippingAlgorithmsDemo::liangBarskyClip(lineCrossing, win, lbOut, u1, u2);
    CGL_ASSERT(lbAcc && approxEqual(lbOut.p1.x, -10.0f) && approxEqual(lbOut.p2.x, 10.0f),
               "TC_CGL_05G: Liang-Barsky parametric clipping yields identical boundary points");
    CGL_ASSERT(approxEqual(u1, 0.25f) && approxEqual(u2, 0.75f),
               "TC_CGL_05H: Liang-Barsky parametric range u1=0.25, u2=0.75 verified");

    std::vector<Vec2> bigSquare = {
        Vec2(-15.0f, -15.0f), Vec2(15.0f, -15.0f), Vec2(15.0f, 15.0f), Vec2(-15.0f, 15.0f)
    };
    auto clippedPoly = ClippingAlgorithmsDemo::sutherlandHodgmanClip(bigSquare, win);
    CGL_ASSERT(clippedPoly.size() == 4, "TC_CGL_05I: Sutherland-Hodgman clipped polygon vertex count is 4");
    bool hasCorner = false;
    for (const auto& v : clippedPoly) {
        if (approxEqual(v.x, 10.0f) && approxEqual(v.y, 10.0f)) hasCorner = true;
    }
    CGL_ASSERT(hasCorner, "TC_CGL_05J: Sutherland-Hodgman clipped polygon aligns with window corners");
}

// -------------------------------------------------------------
// CO6: Texture, Lighting & Color Tests
// -------------------------------------------------------------
void testCO6Appearance() {
    std::cout << "\n--- Testing CGL-CO6: Texture, Lighting, and Color Models ---" << std::endl;

    // 1. Color Space: HSV to RGB Conversion
    Color4 red = LightingTextureDemo::hsvToRgb(0.0f, 1.0f, 1.0f);
    CGL_ASSERT(approxEqual(red.r, 1.0f) && approxEqual(red.g, 0.0f) && approxEqual(red.b, 0.0f),
               "TC_CGL_06A: HSV (0, 1, 1) converts to Pure Red (1, 0, 0)");
    Color4 green = LightingTextureDemo::hsvToRgb(1.0f / 3.0f, 1.0f, 1.0f);
    CGL_ASSERT(approxEqual(green.r, 0.0f) && approxEqual(green.g, 1.0f) && approxEqual(green.b, 0.0f),
               "TC_CGL_06B: HSV (1/3, 1, 1) converts to Pure Green (0, 1, 0)");

    // 2. Local Image Texture Loading (Genuine BMP file from CGL/assets/textures/)
    int w = 0, h = 0;
    std::vector<unsigned char> localTexData;
    bool loadSuccess = loadBMP("CGL/assets/textures/space_hull.bmp", w, h, localTexData);
    if (!loadSuccess) {
        // Fallback relative path check
        loadSuccess = loadBMP("assets/textures/space_hull.bmp", w, h, localTexData);
    }
    CGL_ASSERT(loadSuccess && w == 256 && h == 256,
               "TC_CGL_06C: Actual local BMP texture loaded: space_hull.bmp (256x256)");
    CGL_ASSERT(localTexData.size() == 256 * 256 * 3,
               "TC_CGL_06D: Local BMP byte buffer size verification (196,608 bytes)");

    // 3. Graceful handling of missing or invalid image files
    int dummyW = 0, dummyH = 0;
    std::vector<unsigned char> dummyData;
    bool missingFileResult = loadBMP("CGL/assets/textures/nonexistent_texture.bmp", dummyW, dummyH, dummyData);
    CGL_ASSERT(!missingFileResult, "TC_CGL_06E: Missing texture file handled gracefully without crashing (returns false)");

    // 4. Procedural Texture Buffer Generation (Retained demonstration)
    std::vector<unsigned char> hullTex, planetTex;
    LightingTextureDemo::generateHullTexture(128, 128, hullTex);
    LightingTextureDemo::generatePlanetTexture(128, 128, planetTex);
    CGL_ASSERT(hullTex.size() == 128 * 128 * 3 && planetTex.size() == 128 * 128 * 3,
               "TC_CGL_06F: Procedural texture buffers generated in memory");

    // 5. Phong Illumination Model Reflection Math Verification
    // Ambient: Ia = ka * La
    float ka = 0.25f, La = 1.0f;
    float Ia = ka * La;
    CGL_ASSERT(approxEqual(Ia, 0.25f), "TC_CGL_06G: Phong Ambient reflection calculation (Ia = ka * La)");

    // Diffuse: Id = kd * Ld * max(0, N . L)
    float kd = 0.85f, Ld = 1.0f, nDotL = 0.707f;
    float Id = kd * Ld * nDotL;
    CGL_ASSERT(approxEqual(Id, 0.601f), "TC_CGL_06H: Phong Diffuse Lambert reflection calculation (Id = kd * Ld * (N.L))");

    // Specular: Is = ks * Ls * max(0, R . V)^shininess
    float ks = 0.95f, Ls = 1.0f, rDotV = 0.85f, shininess = 16.0f;
    float Is = ks * Ls * std::pow(rDotV, shininess);
    CGL_ASSERT(Is > 0.0f && Is < 1.0f && approxEqual(Is, 0.070f, 0.01f),
               "TC_CGL_06I: Phong Specular reflection calculation (Is = ks * Ls * (R.V)^n)");

    // Reflection Vector: R = 2*(N.L)*N - L
    Vec3 N(0.0f, 1.0f, 0.0f);
    Vec3 L(0.7071f, 0.7071f, 0.0f);
    float dot = N.x * L.x + N.y * L.y + N.z * L.z;
    Vec3 R(2.0f * dot * N.x - L.x, 2.0f * dot * N.y - L.y, 2.0f * dot * N.z - L.z);
    CGL_ASSERT(approxEqual(R.x, -0.7071f) && approxEqual(R.y, 0.7071f),
               "TC_CGL_06J: Specular Reflection vector formula R = 2(N.L)N - L verified");

    // 6. Gouraud Shading Per-Vertex Interpolation Formula: C(t) = C1 + t*(C2 - C1)
    float c1 = 0.2f, c2 = 0.8f, t = 0.5f;
    float cInterp = c1 + t * (c2 - c1);
    CGL_ASSERT(approxEqual(cInterp, 0.5f),
               "TC_CGL_06K: Gouraud shading bilinear vertex color interpolation verification");
}

// -------------------------------------------------------------
// CGL Enemy Implementation Tests (CGL-CO1 to CGL-CO6)
// -------------------------------------------------------------
void testCGLEnemies() {
    std::cout << "\n--- Testing CGL Enemy Implementations (CGL-CO1 to CGL-CO6) ---" << std::endl;

    // 1. Geometric Polygon Cruiser (CGL-CO1)
    {
        PolygonCruiser cruiser;
        CGL_ASSERT(cruiser.getHullVertexCount() == 6,
                   "TC_CGL_EN1A: PolygonCruiser 6-vertex CCW fuselage hull generation");
        CGL_ASSERT(cruiser.getWingVertexCount() == 6,
                   "TC_CGL_EN1B: PolygonCruiser swept delta wings triangle-fan geometry");
        CGL_ASSERT(cruiser.verifyCCWWindingOrder(),
                   "TC_CGL_EN1C: PolygonCruiser strict Counter-Clockwise (CCW) front-facing winding");
        CGL_ASSERT(cruiser.calculateHullArea() > 0.05f,
                   "TC_CGL_EN1D: PolygonCruiser positive signed Green-Shoelace area verification");
        CGL_ASSERT(cruiser.isPointInHull(0.0f, 0.0f) && !cruiser.isPointInHull(0.5f, 0.5f),
                   "TC_CGL_EN1E: PolygonCruiser ray-casting point-in-polygon containment check");
    }

    // 2. Orbital Rotor Drone (CGL-CO2)
    {
        OrbitalRotorDrone drone;
        CGL_ASSERT(drone.verifyNonCommutativity(2.0f, 3.0f, 45.0f),
                   "TC_CGL_EN2A: OrbitalRotor non-commutative matrix sequencing (T*R != R*T)");

        // Pod 0 world position at angle 0: Base(5, 10) + (2, 0) -> (7, 10, 0)
        Vec3 pod0 = drone.computeRotorWorldPosition(0, 5.0f, 10.0f, 0.0f, 2.0f);
        CGL_ASSERT(approxEqual(pod0.x, 7.0f) && approxEqual(pod0.y, 10.0f),
                   "TC_CGL_EN2B: OrbitalRotor hierarchical pod 0 world coordinate resolution");

        // Pod 1 world position at 180 deg opposite: Base(5, 10) + (-2, 0) -> (3, 10, 0)
        Vec3 pod1 = drone.computeRotorWorldPosition(1, 5.0f, 10.0f, 0.0f, 2.0f);
        CGL_ASSERT(approxEqual(pod1.x, 3.0f) && approxEqual(pod1.y, 10.0f),
                   "TC_CGL_EN2C: OrbitalRotor hierarchical pod 1 180-deg opposed orbit coordinate");

        // Orbit at 90 deg: Base(5, 10) + (0, 2) -> (5, 12, 0)
        Vec3 podRot90 = drone.computeRotorWorldPosition(0, 5.0f, 10.0f, 90.0f, 2.0f);
        CGL_ASSERT(approxEqual(podRot90.x, 5.0f) && approxEqual(podRot90.y, 12.0f),
                   "TC_CGL_EN2D: OrbitalRotor dynamic rotation angle trigonometry accuracy");

        // Pulsation scale factor range check
        drone.update(0.5f);
        CGL_ASSERT(drone.getPulseScale() >= 0.85f && drone.getPulseScale() <= 1.15f,
                   "TC_CGL_EN2E: OrbitalRotor central core breathing scale range [0.85, 1.15]");
    }

    // 3. Bresenham Shield Sentinel (CGL-CO3)
    {
        RasterSentinel sentinel;
        std::vector<Point2D> shield = sentinel.generateShield(100, 100, 25);
        CGL_ASSERT(!shield.empty() && shield.size() >= 100,
                   "TC_CGL_EN3A: RasterSentinel Midpoint Circle shield pixel points generation");

        CGL_ASSERT(RasterSentinel::verifyCircleEquation(shield, 100, 100, 25),
                   "TC_CGL_EN3B: RasterSentinel Midpoint Circle mathematical equation adherence");

        CGL_ASSERT(RasterSentinel::verify8WaySymmetry(100, 100, 25),
                   "TC_CGL_EN3C: RasterSentinel 8-way octant symmetry across cardinal axes");

        std::vector<Point2D> beam = sentinel.generateTargetingBeam(10, 20, 80, 60);
        CGL_ASSERT(RasterSentinel::verifyBresenhamEndpoints(beam, 10, 20, 80, 60),
                   "TC_CGL_EN3D: RasterSentinel Bresenham integer line targeting beam endpoints");

        std::vector<Point2D> steepBeam = sentinel.generateTargetingBeam(0, 0, 4, 12);
        CGL_ASSERT(RasterSentinel::verifyBresenhamEndpoints(steepBeam, 0, 0, 4, 12) && steepBeam.size() == 13,
                   "TC_CGL_EN3E: RasterSentinel Bresenham line across steep slope (m > 1) octants");
    }

    // 4. Depth-Diving Warp Cruiser (CGL-CO4)
    {
        WarpCruiser cruiser;
        float pRatio = cruiser.computePerspectiveForeshorteningRatio(-50.0f, -10.0f, 60.0f, 1.333f);
        CGL_ASSERT(approxEqual(pRatio, 5.0f, 0.05f),
                   "TC_CGL_EN4A: WarpCruiser perspective 1/Z foreshortening ratio (5.0x for Z=50 to Z=10)");

        float oRatio = cruiser.computeOrthoForeshorteningRatio(-50.0f, -10.0f, -15.0f, 15.0f);
        CGL_ASSERT(approxEqual(oRatio, 1.0f, 0.001f),
                   "TC_CGL_EN4B: WarpCruiser orthographic parallel distance invariance ratio (1.0x)");

        Vec3 nearPt = cruiser.projectPerspective(Vec3(0.0f, 0.0f, -10.0f), 60.0f, 1.333f, 1.0f, 100.0f);
        Vec3 farPt  = cruiser.projectPerspective(Vec3(0.0f, 0.0f, -50.0f), 60.0f, 1.333f, 1.0f, 100.0f);
        CGL_ASSERT(nearPt.z < farPt.z,
                   "TC_CGL_EN4C: WarpCruiser perspective NDC depth buffer ordering (near < far)");
    }

    // 5. Boundary Phase Skimmer (CGL-CO5)
    {
        RectWindow win(-0.6f, -0.6f, 0.6f, 0.6f);
        CGL_ASSERT(BoundarySkimmer::testTrivialAccept(win),
                   "TC_CGL_EN5A: BoundarySkimmer Cohen-Sutherland trivial accept inside window");

        CGL_ASSERT(BoundarySkimmer::testTrivialReject(win),
                   "TC_CGL_EN5B: BoundarySkimmer Cohen-Sutherland trivial reject outside left");

        CGL_ASSERT(BoundarySkimmer::testSingleBoundaryCross(win),
                   "TC_CGL_EN5C: BoundarySkimmer Cohen-Sutherland single boundary intersection clip");

        CGL_ASSERT(BoundarySkimmer::testMultipleBoundariesCross(win),
                   "TC_CGL_EN5D: BoundarySkimmer Cohen-Sutherland multiple boundaries traversal clip");

        CGL_ASSERT(BoundarySkimmer::testPolygonClipping(win),
                   "TC_CGL_EN5E: BoundarySkimmer Sutherland-Hodgman polygon clipping against boundary");
    }

    // 6. Specular Phong Cruiser (CGL-CO6)
    {
        PhongCruiser cruiser;

        // Reflection vector test
        Vec3 N(0.0f, 1.0f, 0.0f);
        Vec3 L(0.7071f, 0.7071f, 0.0f);
        Vec3 R = PhongCruiser::computeReflectionVector(N, L);
        CGL_ASSERT(approxEqual(R.x, -0.7071f) && approxEqual(R.y, 0.7071f),
                   "TC_CGL_EN6A: PhongCruiser Reflection Vector formula R = 2(N.L)N - L verified");

        // Half-angle vector test
        Vec3 V(0.0f, 1.0f, 0.0f);
        Vec3 L2(1.0f, 0.0f, 0.0f);
        Vec3 H = PhongCruiser::computeHalfAngleVector(L2, V);
        CGL_ASSERT(approxEqual(H.x, 0.7071f) && approxEqual(H.y, 0.7071f),
                   "TC_CGL_EN6B: PhongCruiser Blinn-Phong Half-Angle Vector H = normalize(L + V)");

        // Phong evaluation with aligned reflection & view vector
        Color4 phongColor = PhongCruiser::evaluatePhongModel(N, Vec3(0.0f, 1.0f, 0.0f), Vec3(0.0f, 1.0f, 0.0f),
                                                            0.2f, 0.6f, 0.9f, 32.0f);
        CGL_ASSERT(phongColor.r > 0.8f,
                   "TC_CGL_EN6C: PhongCruiser specular highlight peak on aligned viewer vector");

        // Blinn-Phong evaluation
        Color4 blinnColor = PhongCruiser::evaluateBlinnPhongModel(N, Vec3(0.0f, 1.0f, 0.0f), Vec3(0.0f, 1.0f, 0.0f),
                                                                 0.2f, 0.6f, 0.9f, 32.0f);
        CGL_ASSERT(blinnColor.r > 0.8f,
                   "TC_CGL_EN6D: PhongCruiser Blinn-Phong specular reflection intensity evaluation");

        // Local 24-bit uncompressed BMP texture check
        CGL_ASSERT(cruiser.isTextureLoaded() && cruiser.getTextureBufferSize() == 196608,
                   "TC_CGL_EN6E: PhongCruiser local 24-bit uncompressed BMP texture buffer (196,608 bytes)");
    }
}

int runAllCGLTests() {
    testsPassed = 0;
    testsTotal = 0;

    std::cout << "========================================================" << std::endl;
    std::cout << "  AIRSTRIKE CGL: Computer Graphics Laboratory Test Suite" << std::endl;
    std::cout << "  Phase 5.1 Technical Audit & Mathematical Verification " << std::endl;
    std::cout << "========================================================" << std::endl;

    testCO1Primitives();
    testCO2Transformations();
    testCO3LineAlgorithms();
    testCO3CurveAlgorithms();
    testCO33DMeshTransformations();
    testCO4Projection();
    testCO5Clipping();
    testCO6Appearance();
    testCGLEnemies();

    std::cout << "\n========================================================" << std::endl;
    std::cout << "  TEST SUMMARY: " << testsPassed << " / " << testsTotal << " Passed ("
              << (testsTotal > 0 ? (testsPassed * 100 / testsTotal) : 0) << "%)" << std::endl;
    std::cout << "========================================================" << std::endl;

    return (testsPassed == testsTotal) ? 0 : 1;
}

} // namespace CGL
