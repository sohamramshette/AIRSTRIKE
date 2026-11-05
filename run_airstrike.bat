@echo off
setlocal

if "%1"=="" goto :interactive_menu
if /i "%1"=="java" goto :run_java
if /i "%1"=="psoop" goto :run_java
if /i "%1"=="pl" goto :run_pl
if /i "%1"=="coa" goto :run_coa
if /i "%1"=="cgl" goto :run_cgl
if /i "%1"=="build" goto :run_build
if /i "%1"=="verify" goto :run_build
if /i "%1"=="env" goto :view_env

:interactive_menu
cls
echo ==============================================================================
echo                 AIRSTRIKE - 2D SPACE SHOOTER LAUNCHER
echo                 Academic Demonstration and Verification System
echo ==============================================================================
echo   [1] Launch PSOOP Java Game Client - Swing / AWT
echo   [2] Launch PL C++ Data Structures and Algorithms Suite
echo   [3] Launch COA 80386 32-bit Assembly Demonstrations
echo   [4] Launch CGL OpenGL and GLUT Graphics Demonstrations
echo   [5] Build and Verify All Modules - build_all.bat
echo   [6] View Environment Report
echo   [7] Exit
echo ==============================================================================
set /p CHOICE="Enter option [1-7]: "

if "%CHOICE%"=="1" goto :run_java
if "%CHOICE%"=="2" goto :run_pl
if "%CHOICE%"=="3" goto :run_coa
if "%CHOICE%"=="4" goto :run_cgl
if "%CHOICE%"=="5" goto :run_build
if "%CHOICE%"=="6" goto :view_env
if "%CHOICE%"=="7" goto :exit_launcher
echo [ERROR] Invalid choice.
pause
goto :interactive_menu

:run_java
echo.
echo [PSOOP] Launching Java Game Client...
if not exist "bin\java\main\Main.class" (
    echo [NOTICE] Java binary not found in bin\java.
    echo In Phase 1 Foundation, modules are scaffolded.
    echo Once Phase 2 implementation begins, Main.class will be compiled.
    if "%1"=="" pause
    if not "%1"=="" exit /b 0
    goto :interactive_menu
)
java -cp bin\java main.Main
if "%1"=="" pause
if not "%1"=="" exit /b 0
goto :interactive_menu

:run_pl
echo.
echo [PL] Launching C++ Data Structures Runner...
if not exist "bin\pl\pl_test_runner.exe" (
    echo [NOTICE] PL binary not found at bin\pl\pl_test_runner.exe.
    echo In Phase 1 Foundation, modules are scaffolded.
    if "%1"=="" pause
    if not "%1"=="" exit /b 0
    goto :interactive_menu
)
bin\pl\pl_test_runner.exe
if "%1"=="" pause
if not "%1"=="" exit /b 0
goto :interactive_menu

:run_coa
echo.
echo [COA] Launching 80386 Assembly Demonstrations...
if not exist "bin\coa\coa_runner.exe" (
    echo [NOTICE] COA binary not found at bin\coa\coa_runner.exe.
    echo In Phase 1 Foundation, modules are scaffolded.
    if "%1"=="" pause
    if not "%1"=="" exit /b 0
    goto :interactive_menu
)
bin\coa\coa_runner.exe
if "%1"=="" pause
if not "%1"=="" exit /b 0
goto :interactive_menu

:run_cgl
echo.
echo [CGL] Launching OpenGL and GLUT Demonstrations...
if not exist "bin\cgl\cgl_demo.exe" (
    echo [NOTICE] CGL binary not found at bin\cgl\cgl_demo.exe.
    echo In Phase 1 Foundation, modules are scaffolded.
    if "%1"=="" pause
    if not "%1"=="" exit /b 0
    goto :interactive_menu
)
bin\cgl\cgl_demo.exe
if "%1"=="" pause
if not "%1"=="" exit /b 0
goto :interactive_menu

:run_build
echo.
call build_all.bat
if "%1"=="" pause
if not "%1"=="" exit /b 0
goto :interactive_menu

:view_env
echo.
if exist "docs\setup\ENVIRONMENT_REPORT.md" (
    type docs\setup\ENVIRONMENT_REPORT.md
) else (
    echo [ERROR] docs\setup\ENVIRONMENT_REPORT.md not found.
)
if "%1"=="" pause
if not "%1"=="" exit /b 0
goto :interactive_menu

:exit_launcher
echo Exiting AIRSTRIKE launcher.
exit /b 0
