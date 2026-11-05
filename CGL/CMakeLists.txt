cmake_minimum_required(VERSION 3.10)
project(Airstrike_CGL LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Set runtime output directory
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../bin/cgl)

# Include paths
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${CMAKE_CURRENT_SOURCE_DIR}/src/primitives
    ${CMAKE_CURRENT_SOURCE_DIR}/src/transformations
    ${CMAKE_CURRENT_SOURCE_DIR}/src/algorithms
    ${CMAKE_CURRENT_SOURCE_DIR}/src/objects
    ${CMAKE_CURRENT_SOURCE_DIR}/src/projection
    ${CMAKE_CURRENT_SOURCE_DIR}/src/clipping
    ${CMAKE_CURRENT_SOURCE_DIR}/src/appearance
)

# Required definitions for MinGW GLUT
add_definitions(-D_DLL -DGLUT_DISABLE_ATEXIT_HACK)

# Collect source files
file(GLOB_RECURSE CGL_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/tests/*.cpp
)

find_package(OpenGL REQUIRED)

if(NOT CGL_SOURCES)
    message(STATUS "No CGL C++ source files detected yet. Add .cpp files in src/ and tests/.")
else()
    add_executable(cgl_demo ${CGL_SOURCES})
    target_link_libraries(cgl_demo PRIVATE glut32 glu32 opengl32)
endif()

