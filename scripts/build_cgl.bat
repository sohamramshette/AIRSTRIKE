@echo off
setlocal

rem Anchor working directory to project root if called from scripts directory
if exist "%~dp0..\CGL" cd /d "%~dp0.."

echo ========================================================
echo [CGL] Building Computer Graphics Lab OpenGL/GLUT Module...
echo ========================================================

where g++ >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] g++ compiler was not found in PATH!
    echo Please install MinGW or GCC with C++17 and OpenGL support.
    exit /b 1
)

for /f "tokens=*" %%v in ('g++ --version 2^>^&1') do (
    set GPP_VER=%%v
    goto :found_cgl_gpp
)
:found_cgl_gpp
echo [INFO] Detected C++ Compiler: %GPP_VER%

if not exist "bin\cgl" mkdir "bin\cgl"

set SOURCE_COUNT=0
for /r "CGL" %%f in (*.cpp) do (
    set /a SOURCE_COUNT+=1
)

if %SOURCE_COUNT% EQU 0 (
    echo [WARN] No .cpp source files found in CGL yet - Phase 1 Foundation state.
    echo [INFO] Prepared build output directory at bin\cgl.
    exit /b 0
)

echo [INFO] Compiling CGL OpenGL/GLUT sources with MinGW g++ C++17...
g++ -std=c++17 -Wall -Wextra ^
    -ICGL/src ^
    CGL/src/common/*.cpp ^
    CGL/src/primitives/*.cpp ^
    CGL/src/transformations/*.cpp ^
    CGL/src/algorithms/*.cpp ^
    CGL/src/projection/*.cpp ^
    CGL/src/clipping/*.cpp ^
    CGL/src/appearance/*.cpp ^
    CGL/src/objects/*.cpp ^
    CGL/src/main.cpp ^
    CGL/tests/*.cpp ^
    -lglut32 -lglu32 -lopengl32 ^
    -o bin\cgl\cgl_runner.exe

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] OpenGL / GLUT compilation failed!
    exit /b 1
)

copy /y bin\cgl\cgl_runner.exe bin\cgl\cgl_demo.exe >nul 2>&1

echo [SUCCESS] CGL OpenGL module built successfully: bin\cgl\cgl_runner.exe
exit /b 0
