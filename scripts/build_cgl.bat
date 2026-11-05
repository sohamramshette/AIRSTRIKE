@echo off
setlocal

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

where cmake >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] CMake detected. Building via CMake...
    if not exist "build\cgl" mkdir "build\cgl"
    cmake -B build\cgl -S CGL -G "MinGW Makefiles"
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] CMake CGL configuration failed!
        exit /b 1
    )
    cmake --build build\cgl
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] CMake CGL build failed!
        exit /b 1
    )
) else (
    echo [INFO] CMake not detected in PATH. Using direct MinGW g++ compilation fallback...
    g++ -std=c++17 -Wall -Wextra -D_DLL -DGLUT_DISABLE_ATEXIT_HACK -ICGL/src -ICGL/src/primitives -ICGL/src/transformations -ICGL/src/algorithms -ICGL/src/objects -ICGL/src/projection -ICGL/src/clipping -ICGL/src/appearance CGL/src/*/*.cpp CGL/tests/*.cpp -lglut32 -lglu32 -lopengl32 -o bin\cgl\cgl_demo.exe
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] OpenGL / GLUT compilation failed!
        exit /b 1
    )
)

echo [SUCCESS] CGL OpenGL module built successfully!
exit /b 0
