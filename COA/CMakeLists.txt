cmake_minimum_required(VERSION 3.10)
project(Airstrike_COA LANGUAGES C ASM)

# Ensure 32-bit architecture for 80386
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -m32")
set(CMAKE_ASM_FLAGS "${CMAKE_ASM_FLAGS} -m32")

# Set runtime output directory
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../bin/coa)

# Collect 80386 assembly and C driver sources
file(GLOB_RECURSE COA_ASM_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/architecture/*.s
    ${CMAKE_CURRENT_SOURCE_DIR}/instructions/*.s
    ${CMAKE_CURRENT_SOURCE_DIR}/arithmetic/*.s
    ${CMAKE_CURRENT_SOURCE_DIR}/control_unit/*.s
    ${CMAKE_CURRENT_SOURCE_DIR}/pipelining/*.s
    ${CMAKE_CURRENT_SOURCE_DIR}/simulator/*.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/tests/*.s
    ${CMAKE_CURRENT_SOURCE_DIR}/tests/*.cpp
)

if(NOT COA_ASM_SOURCES)
    message(STATUS "No COA 80386 assembly source files detected yet. Add .s files in COA subdirectories.")
else()
    add_executable(coa_runner ${COA_ASM_SOURCES})
endif()

