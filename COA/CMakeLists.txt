cmake_minimum_required(VERSION 3.10)
project(Airstrike_COA LANGUAGES C ASM)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../bin/coa)

file(GLOB_RECURSE COA_ASM_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/src/*.s
    ${CMAKE_CURRENT_SOURCE_DIR}/tests/*.s
)

if(NOT COA_ASM_SOURCES)
    message(STATUS "No COA assembly source files detected.")
else()
    add_executable(coa_runner ${COA_ASM_SOURCES})
endif()
