cmake_minimum_required(VERSION 3.10)
project(Airstrike_PL LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Set runtime output directory
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../bin/pl)

# Include paths
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${CMAKE_CURRENT_SOURCE_DIR}/src/arrays
    ${CMAKE_CURRENT_SOURCE_DIR}/src/linked_lists
    ${CMAKE_CURRENT_SOURCE_DIR}/src/stacks
    ${CMAKE_CURRENT_SOURCE_DIR}/src/queues
    ${CMAKE_CURRENT_SOURCE_DIR}/src/searching
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sorting
    ${CMAKE_CURRENT_SOURCE_DIR}/src/hashing
)

# Collect source files
file(GLOB_RECURSE PL_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/tests/*.cpp
)

# If no sources exist yet, create a dummy or report
if(NOT PL_SOURCES)
    message(STATUS "No PL C++ source files detected yet. Add .cpp files in src/ and tests/.")
else()
    add_executable(pl_test_runner ${PL_SOURCES})
    target_compile_options(pl_test_runner PRIVATE -Wall -Wextra)
endif()

