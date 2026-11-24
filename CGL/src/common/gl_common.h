// ==============================================================================
// File      : CGL/src/common/gl_common.h
// Subject   : Common | OpenGL/GLUT Core Headers & Math Primitives
// Topics    : [TOPIC: OpenGL/GLUT Setup] [TOPIC: 2D/3D Point & Vector Types] [TOPIC: RGBA Color Model]
// Game Role : Centralizes platform GL/GLUT headers and lightweight math/color structs across AIRSTRIKE CGL.
// ==============================================================================

#ifndef GL_COMMON_H
#define GL_COMMON_H

#ifdef _WIN32
#ifndef WIN32_LEAN_AND_MEAN
// WHY: Trim legacy Win32 APIs before <windows.h> to prevent macro conflicts with OpenGL headers
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#endif

#include <GL/gl.h>
#include <GL/glu.h>
#include <GL/glut.h>

#include <iostream>
#include <vector>
#include <string>
#include <cmath>
#include <cstring>
#include <cstdlib>

#ifndef M_PI
// WHY: MinGW/MSVC do not always expose M_PI without _USE_MATH_DEFINES
#define M_PI 3.14159265358979323846
#endif

// [TOPIC: Integer Raster Coordinates]
// Purpose: Stores discrete 2D screen/grid pixel coordinates.
// Game Role: Output element type for DDA, Bresenham, Midpoint, and Scanline rasterizers.
struct Point2D {
    int x;
    int y;
    Point2D() : x(0), y(0) {}
    Point2D(int _x, int _y) : x(_x), y(_y) {}
    bool operator==(const Point2D& other) const {
        return x == other.x && y == other.y;
    }
};

// [TOPIC: 2D Euclidean Vector]
// Purpose: Stores floating-point 2D coordinates with epsilon equality comparison.
// Game Role: Represents normalized ship vertices, clipping endpoints, and polygon hulls.
struct Vec2 {
    float x;
    float y;
    Vec2() : x(0.0f), y(0.0f) {}
    Vec2(float _x, float _y) : x(_x), y(_y) {}
    bool operator==(const Vec2& other) const {
        // WHY: Tolerance avoids false mismatches after floating-point clipping intersections
        return std::fabs(x - other.x) < 0.001f && std::fabs(y - other.y) < 0.001f;
    }
};

// [TOPIC: 3D Euclidean Vector]
// Purpose: Stores 3D world, eye, or NDC coordinates and surface normal/light vectors.
// Game Role: Represents spacecraft positions, 3D mesh vertices, and Phong lighting vectors.
struct Vec3 {
    float x;
    float y;
    float z;
    Vec3() : x(0.0f), y(0.0f), z(0.0f) {}
    Vec3(float _x, float _y, float _z) : x(_x), y(_y), z(_z) {}
};

// [TOPIC: RGBA Color Model]
// Purpose: Stores normalized [0, 1] red, green, blue, and alpha color channels.
// Game Role: Holds Gouraud vertex colors, HSV conversion outputs, and Phong illumination intensities.
struct Color4 {
    float r;
    float g;
    float b;
    float a;
    Color4() : r(1.0f), g(1.0f), b(1.0f), a(1.0f) {}
    Color4(float _r, float _g, float _b, float _a = 1.0f) : r(_r), g(_g), b(_b), a(_a) {}
};

#endif // GL_COMMON_H

