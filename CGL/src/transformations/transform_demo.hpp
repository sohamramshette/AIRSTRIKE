#ifndef TRANSFORM_DEMO_HPP
#define TRANSFORM_DEMO_HPP

#include "common/gl_common.h"
#include <string>

namespace CGL {

/**
 * @brief Class demonstrating CGL-CO2: Geometric Transformations.
 * Covers Translation, Rotation, Scaling, Reflection, Shearing,
 * Composite Transformations (T*R vs R*T), Arbitrary Pivot Point,
 * and Hierarchical Matrix Stacks (glPushMatrix / glPopMatrix).
 */
class TransformDemo {
public:
    TransformDemo();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);

    // Interactive controls
    void translate(float dx, float dy);
    void rotate(float deltaAngle);
    void scale(float deltaScale);
    void toggleOrder();
    void togglePivotMode();
    void toggleHierarchical();
    void toggleShear();
    void toggleReflection();
    void reset();

    // Inspection getters for testing
    float getTx() const { return tx; }
    float getTy() const { return ty; }
    float getAngle() const { return angle; }
    float getScale() const { return scaleVal; }
    bool isTRMode() const { return orderTR; }
    bool isPivotMode() const { return pivotMode; }
    bool isHierarchical() const { return hierarchicalMode; }
    bool isShearMode() const { return shearMode; }
    int getReflectionMode() const { return reflectionMode; }

    // Math helpers for verification
    static void multiplyMatrix4x4(const float a[16], const float b[16], float result[16]);
    static void getTranslationMatrix(float tx, float ty, float tz, float m[16]);
    static void getRotationXMatrix(float angleDeg, float m[16]);
    static void getRotationYMatrix(float angleDeg, float m[16]);
    static void getRotationZMatrix(float angleDeg, float m[16]);
    static void getScaleMatrix(float sx, float sy, float sz, float m[16]);
    static void getShear2DMatrix(float shx, float shy, float m[16]);
    static void getReflectionXMatrix(float m[16]);
    static void getReflectionYMatrix(float m[16]);
    static void getArbitraryPivotRotationMatrix(float px, float py, float angleDeg, float m[16]);
    static Vec3 transformPoint(const float m[16], const Vec3& p);

private:
    float tx, ty;
    float angle;
    float scaleVal;
    float shearX;
    int reflectionMode; // 0: None, 1: Reflect X, 2: Reflect Y

    bool orderTR;          // true: Translate then Rotate; false: Rotate then Translate
    bool pivotMode;        // true: Rotate around arbitrary pivot point
    bool hierarchicalMode; // true: Display parent ship + orbiting escort drone
    bool shearMode;        // true: Apply affine shearing

    float pivotX, pivotY;
    float droneOrbitAngle;

    void drawFighter(float size);
    void drawDrone(float size);
};

} // namespace CGL

#endif // TRANSFORM_DEMO_HPP

