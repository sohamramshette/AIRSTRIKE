@echo off
setlocal enabledelayedexpansion

rem Anchor working directory to project root
if exist "%~dp0bin" cd /d "%~dp0"

:interactive_menu
cls
echo ==============================================================================
echo                     AIRSTRIKE - UNIFIED MASTER LAUNCHER
echo                 Academic Demonstration & Verification System
echo ==============================================================================
echo   Four Integrated Subjects - 19 Course Outcomes - Supabase Cloud Database
echo ==============================================================================
echo   [1] PSOOP - Java Space Shooter (Swing / AWT GUI)
echo   [2] PL    - Data Structures and Algorithms (C++17 CLI)
echo   [3] COA   - 80386 Assembly Programs (32-bit Assembly CLI)
echo   [4] CGL   - Computer Graphics Laboratory (OpenGL / GLUT)
echo   [5] Build All Modules (build_all.bat)
echo   [6] Run All Tests (test_all.bat)
echo   [7] Open Project Documentation
echo   [8] Exit
echo ==============================================================================
set /p CHOICE="Select an option [1-8]: "

if "%CHOICE%"=="1" goto :launch_psoop
if "%CHOICE%"=="2" goto :launch_pl
if "%CHOICE%"=="3" goto :launch_coa
if "%CHOICE%"=="4" goto :launch_cgl
if "%CHOICE%"=="5" goto :launch_build
if "%CHOICE%"=="6" goto :launch_tests
if "%CHOICE%"=="7" goto :launch_docs
if "%CHOICE%"=="8" goto :exit_launcher

echo.
echo [ERROR] Invalid choice "%CHOICE%". Please enter a number between 1 and 8.
timeout /t 2 >nul
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 1. Launch PSOOP Java Game Client
rem --------------------------------------------------------------------------
:launch_psoop
echo.
echo ==============================================================================
echo [PSOOP] Validating Java Runtime Environment...
echo ==============================================================================
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java Runtime (java.exe) was not found in PATH!
    echo Please install Java 11 or higher and add it to your system PATH.
    pause
    goto :interactive_menu
)

if not exist "bin\java\main\Main.class" (
    echo [INFO] Java classes not found in bin\java. Compiling module...
    call scripts\build_java.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Java compilation failed! Please inspect compilation errors above.
        pause
        goto :interactive_menu
    )
)

echo [INFO] Launching AIRSTRIKE Java Space Shooter...
start "AIRSTRIKE - Java Combat Client" java -cp bin\java main.Main
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 2. Launch PL C++ Data Structures Runner
rem --------------------------------------------------------------------------
:launch_pl
echo.
echo ==============================================================================
echo [PL] Validating C++17 Runtime Binary...
echo ==============================================================================
if not exist "bin\pl\pl_test_runner.exe" (
    echo [INFO] PL binary not found. Compiling with MinGW C++17...
    call scripts\build_pl.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] C++ compilation failed! Please inspect compilation errors above.
        pause
        goto :interactive_menu
    )
)

echo [INFO] Launching PL Data Structures Interactive Demonstration...
echo.
bin\pl\pl_test_runner.exe
echo.
echo [INFO] PL demonstration completed.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 3. Launch COA 80386 Assembly Demonstrations
rem --------------------------------------------------------------------------
:launch_coa
echo.
echo ==============================================================================
echo [COA] Validating 80386 32-bit Assembly Binary...
echo ==============================================================================
if not exist "bin\coa\coa_runner.exe" (
    echo [INFO] COA binary not found. Assembling 80386 programs...
    call scripts\build_coa.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Assembly compilation failed! Please inspect errors above.
        pause
        goto :interactive_menu
    )
)

echo [INFO] Launching COA 80386 Architecture & Arithmetic Interactive CLI...
echo.
bin\coa\coa_runner.exe
echo.
echo [INFO] COA demonstration completed.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 4. Launch CGL OpenGL Visual Graphics Demonstrations
rem --------------------------------------------------------------------------
:launch_cgl
echo.
echo ==============================================================================
echo [CGL] Validating OpenGL & GLUT Graphics Binary...
echo ==============================================================================
if not exist "bin\cgl\cgl_runner.exe" (
    echo [INFO] CGL binary not found. Compiling OpenGL module...
    call scripts\build_cgl.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] OpenGL compilation failed! Please inspect errors above.
        pause
        goto :interactive_menu
    )
)

echo [INFO] Launching CGL OpenGL Visual Demonstration Window...
echo Controls: [1-6] CO Demos | [0] Integrated Combat Scene | [ESC] Exit Window
start "AIRSTRIKE - Computer Graphics Lab (OpenGL)" bin\cgl\cgl_runner.exe
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 5. Build All Modules
rem --------------------------------------------------------------------------
:launch_build
echo.
call build_all.bat
echo.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 6. Run All Tests
rem --------------------------------------------------------------------------
:launch_tests
echo.
call test_all.bat
echo.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 7. Open Project Documentation
rem --------------------------------------------------------------------------
:launch_docs
echo.
echo ==============================================================================
echo                       AIRSTRIKE PROJECT DOCUMENTATION
echo ==============================================================================
echo   [1] Final Project Audit & CO Verification (docs/FINAL_PROJECT_AUDIT.md)
echo   [2] Course Outcome Verification Matrix (docs/CO_VERIFICATION_MATRIX.md)
echo   [3] System Architecture Blueprint (docs/PROJECT_ARCHITECTURE.md)
echo   [4] Build & Run Guide (docs/BUILD_AND_RUN.md)
echo   [5] Supabase Cloud Database Audit (docs/SUPABASE/FINAL_DATABASE_AUDIT.md)
echo   [6] Final Demonstration & Viva Guide (docs/FINAL_DEMONSTRATION_GUIDE.md)
echo   [7] Project Readme (README.md)
echo   [8] Back to Main Menu
echo ==============================================================================
set /p DOC_CHOICE="Select a document [1-8]: "

if "%DOC_CHOICE%"=="1" start "" "docs\FINAL_PROJECT_AUDIT.md" & goto :interactive_menu
if "%DOC_CHOICE%"=="2" start "" "docs\CO_VERIFICATION_MATRIX.md" & goto :interactive_menu
if "%DOC_CHOICE%"=="3" start "" "docs\PROJECT_ARCHITECTURE.md" & goto :interactive_menu
if "%DOC_CHOICE%"=="4" start "" "docs\BUILD_AND_RUN.md" & goto :interactive_menu
if "%DOC_CHOICE%"=="5" start "" "docs\SUPABASE\FINAL_DATABASE_AUDIT.md" & goto :interactive_menu
if "%DOC_CHOICE%"=="6" start "" "docs\FINAL_DEMONSTRATION_GUIDE.md" & goto :interactive_menu
if "%DOC_CHOICE%"=="7" start "" "README.md" & goto :interactive_menu
if "%DOC_CHOICE%"=="8" goto :interactive_menu

echo [ERROR] Invalid selection.
timeout /t 2 >nul
goto :launch_docs

rem --------------------------------------------------------------------------
rem 8. Exit
rem --------------------------------------------------------------------------
:exit_launcher
echo.
echo Thank you for evaluating AIRSTRIKE. Systems safely powered down.
exit /b 0
