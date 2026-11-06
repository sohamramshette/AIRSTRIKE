#ifndef GL_COMMON_H
#define GL_COMMON_H

#ifdef _WIN32
#ifndef WIN32_LEAN_AND_MEAN
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
#define M_PI 3.14159265358979323846
#endif

struct Point2D {
    int x;
    int y;
    Point2D() : x(0), y(0) {}
    Point2D(int _x, int _y) : x(_x), y(_y) {}
    bool operator==(const Point2D& other) const {
        return x == other.x && y == other.y;
    }
};

struct Vec2 {
    float x;
    float y;
    Vec2() : x(0.0f), y(0.0f) {}
    Vec2(float _x, float _y) : x(_x), y(_y) {}
    bool operator==(const Vec2& other) const {
        return std::fabs(x - other.x) < 0.001f && std::fabs(y - other.y) < 0.001f;
    }
};

struct Vec3 {
    float x;
    float y;
    float z;
    Vec3() : x(0.0f), y(0.0f), z(0.0f) {}
    Vec3(float _x, float _y, float _z) : x(_x), y(_y), z(_z) {}
};

struct Color4 {
    float r;
    float g;
    float b;
    float a;
    Color4() : r(1.0f), g(1.0f), b(1.0f), a(1.0f) {}
    Color4(float _r, float _g, float _b, float _a = 1.0f) : r(_r), g(_g), b(_b), a(_a) {}
};

#endif // GL_COMMON_H

