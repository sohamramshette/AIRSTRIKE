@echo off
setlocal

if exist "%~dp0..\CGL" cd /d "%~dp0.."
if exist "%~dp0src" cd /d "%~dp0.."

echo ========================================================
echo [CGL] Building Computer Graphics Lab Module...
echo ========================================================

where g++ >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] g++ compiler not found in PATH!
    exit /b 1
)

if not exist "bin\cgl" mkdir "bin\cgl"

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
    echo [ERROR] CGL Compilation failed!
    exit /b 1
)

copy /y bin\cgl\cgl_runner.exe bin\cgl\cgl_demo.exe >nul 2>&1

echo [SUCCESS] CGL Module built successfully at bin\cgl\cgl_runner.exe
exit /b 0
