@echo off
setlocal

echo ==============================================================================
echo                 AIRSTRIKE - MASTER BUILD SYSTEM - PHASE 1
echo ==============================================================================
echo Target Modules:
echo   1. PSOOP : Java + Swing + AWT
echo   2. PL    : C++17 Data Structures and Algorithms
echo   3. COA   : 80386 32-bit Assembly Language
echo   4. CGL   : C++17 + OpenGL + GLUT Graphics
echo ==============================================================================
echo.

set FAILURES=0
set STATUS_PSOOP=SUCCESS
set STATUS_PL=SUCCESS
set STATUS_COA=SUCCESS
set STATUS_CGL=SUCCESS

if not exist "bin\java" mkdir "bin\java"
if not exist "bin\pl"   mkdir "bin\pl"
if not exist "bin\coa"  mkdir "bin\coa"
if not exist "bin\cgl"  mkdir "bin\cgl"

echo --- [1/4] Building PSOOP Java ---
call scripts\build_java.bat
if %ERRORLEVEL% NEQ 0 (
    set STATUS_PSOOP=FAILED
    set /a FAILURES+=1
    echo [ERROR] PSOOP build failed or missing dependencies!
)
echo.

echo --- [2/4] Building PL C++17 ---
call scripts\build_pl.bat
if %ERRORLEVEL% NEQ 0 (
    set STATUS_PL=FAILED
    set /a FAILURES+=1
    echo [ERROR] PL build failed or missing dependencies!
)
echo.

echo --- [3/4] Building COA 80386 ASM ---
call scripts\build_coa.bat
if %ERRORLEVEL% NEQ 0 (
    set STATUS_COA=FAILED
    set /a FAILURES+=1
    echo [ERROR] COA build failed or missing dependencies!
)
echo.

echo --- [4/4] Building CGL OpenGL/GLUT ---
call scripts\build_cgl.bat
if %ERRORLEVEL% NEQ 0 (
    set STATUS_CGL=FAILED
    set /a FAILURES+=1
    echo [ERROR] CGL build failed or missing dependencies!
)
echo.

echo ==============================================================================
echo                           BUILD SUMMARY MATRIX
echo ==============================================================================
echo   PSOOP    : %STATUS_PSOOP%      Java Swing/AWT Subsystem
echo   PL       : %STATUS_PL%      C++17 Data Structures and Algorithms
echo   COA      : %STATUS_COA%      80386 32-bit Assembly Toolchain
echo   CGL      : %STATUS_CGL%      OpenGL and GLUT Graphics Subsystem
echo ==============================================================================

if %FAILURES% GTR 0 (
    echo [ERROR] Build completed with %FAILURES% failures. Check messages above.
    exit /b 1
)

echo [SUCCESS] All Airstrike modules verified and ready!
exit /b 0
