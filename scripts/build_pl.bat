@echo off
setlocal

echo ========================================================
echo [PL] Building Programming Laboratory C++17 Module...
echo ========================================================

where g++ >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] g++ compiler was not found in PATH!
    echo Please install MinGW or GCC with C++17 support and add it to PATH.
    exit /b 1
)

for /f "tokens=*" %%v in ('g++ --version 2^>^&1') do (
    set GPP_VER=%%v
    goto :found_gpp
)
:found_gpp
echo [INFO] Detected C++ Compiler: %GPP_VER%

if not exist "bin\pl" mkdir "bin\pl"

set SOURCE_COUNT=0
for /r "PL" %%f in (*.cpp) do (
    set /a SOURCE_COUNT+=1
)

if %SOURCE_COUNT% EQU 0 (
    echo [WARN] No .cpp source files found in PL yet - Phase 1 Foundation state.
    echo [INFO] Prepared build output directory at bin\pl.
    exit /b 0
)

where cmake >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] CMake detected. Building via CMake...
    if not exist "build\pl" mkdir "build\pl"
    cmake -B build\pl -S PL -G "MinGW Makefiles"
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] CMake configuration failed!
        exit /b 1
    )
    cmake --build build\pl
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] CMake build failed!
        exit /b 1
    )
) else (
    echo [INFO] CMake not detected in PATH. Using direct MinGW g++ compilation fallback...
    g++ -std=c++17 -Wall -Wextra -IPL/src -IPL/src/arrays -IPL/src/linked_lists -IPL/src/stacks -IPL/src/queues -IPL/src/searching -IPL/src/sorting -IPL/src/hashing PL/src/*/*.cpp PL/tests/*.cpp -o bin\pl\pl_test_runner.exe
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] C++ compilation failed!
        exit /b 1
    )
)

echo [SUCCESS] PL C++ module built successfully!
exit /b 0
