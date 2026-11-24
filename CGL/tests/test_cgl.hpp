// ==============================================================================
// File      : CGL/tests/test_cgl.hpp
// Subject   : Tests | Headless CGL Verification Entry Point
// Topics    : [TOPIC: Automated Assertions] [TOPIC: CO1-CO6 Mathematical Verification]
// Game Role : Exposes the headless unit test suite callable via --test in AIRSTRIKE CGL.
// ==============================================================================

#ifndef TEST_CGL_HPP
#define TEST_CGL_HPP

namespace CGL {

// [TOPIC: Automated Assertions]
// Purpose: Executes all headless unit tests validating CO1-CO6 algorithms, enemies, and scenes.
// Game Role: Verifies AIRSTRIKE CGL math and asset loading without requiring a live GLUT window.
int runAllCGLTests();

} // namespace CGL

#endif // TEST_CGL_HPP

