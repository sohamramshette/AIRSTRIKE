@echo off
setlocal enabledelayedexpansion

rem Anchor working directory to project root
if exist "%~dp0bin" cd /d "%~dp0"

:interactive_menu
cls
echo ==============================================================================
echo                     AIRSTRIKE - UNIFIED MASTER LAUNCHER
echo                 Academic Demonstration ^& Verification System
echo ==============================================================================
echo    Four Integrated Subjects - 19 Course Outcomes - 20 Academic Enemy Types
echo ==============================================================================
echo   [1]  Play AIRSTRIKE - Java Desktop (Swing / AWT Space Combat Game)
echo   [2]  Launch AIRSTRIKE - Browser (HTML5 Canvas + TypeScript Web Client)
echo   [3]  PSOOP Enemy Demonstrations (Java Space Shooter ^& Enemy Fleet)
echo   [4]  PL Enemy Demonstrations (C++17 Data Structures ^& Tactical Fleet)
echo   [5]  COA Enemy Demonstrations (32-bit Intel 80386 Assembly Suite)
echo   [6]  CGL Enemy Demonstrations (OpenGL / GLUT Graphics - Screen 7)
echo   [7]  Build All Modules (build_all.bat)
echo   [8]  Run All Tests (test_all.bat - 418 Assertions)
echo   [9]  View 20-Enemy Academic Roster (CO1 to CO19 Full Mapping)
echo   [10] Exit Application
echo ==============================================================================
set "CHOICE="
set /p CHOICE=Select an option [1-10]: 

if not defined CHOICE exit /b 0
if "!CHOICE!"=="1" goto :launch_psoop_play
if "!CHOICE!"=="2" goto :launch_browser
if "!CHOICE!"=="3" goto :launch_psoop_enemies
if "!CHOICE!"=="4" goto :launch_pl_enemies
if "!CHOICE!"=="5" goto :launch_coa_enemies
if "!CHOICE!"=="6" goto :launch_cgl_enemies
if "!CHOICE!"=="7" goto :launch_build
if "!CHOICE!"=="8" goto :launch_tests
if "!CHOICE!"=="9" goto :show_roster
if "!CHOICE!"=="10" goto :exit_launcher

echo.
echo [ERROR] Invalid choice "%CHOICE%". Please enter a number between 1 and 10.
timeout /t 2 >nul
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 1. Play AIRSTRIKE Java Desktop Client
rem --------------------------------------------------------------------------
:launch_psoop_play
echo.
echo ==============================================================================
echo [1] PLAY AIRSTRIKE - JAVA DESKTOP SPACE COMBAT CLIENT
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

echo [INFO] Launching AIRSTRIKE Java Space Shooter in an independent GUI window...
start "AIRSTRIKE - Java Combat Client" java -cp bin\java main.Main
echo [INFO] Java application started. Returning to master menu...
timeout /t 2 >nul
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 2. Launch Browser Client
rem --------------------------------------------------------------------------
:launch_browser
echo.
echo ==============================================================================
echo [2] LAUNCH AIRSTRIKE - BROWSER EDITION (TYPESCRIPT ^& HTML5 CANVAS)
echo ==============================================================================
where node >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Node.js (node.exe) was not found in PATH!
    echo Please install Node.js 18+ and ensure 'node' and 'npm' are in your PATH.
    pause
    goto :interactive_menu
)

if not exist "web\node_modules" (
    echo [INFO] web\node_modules not found. Installing web dependencies...
    npm --prefix web install
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] npm install failed!
        pause
        goto :interactive_menu
    )
)

echo [INFO] Starting local Vite development server for Web client...
start "AIRSTRIKE - Web Server" cmd /k "cd /d web && npm run dev"
timeout /t 2 >nul
echo [INFO] Opening default browser at http://localhost:5173...
start http://localhost:5173
echo [INFO] Browser client started. Returning to master menu...
timeout /t 2 >nul
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 3. PSOOP Enemy Demonstrations
rem --------------------------------------------------------------------------
:launch_psoop_enemies
echo.
echo ==============================================================================
echo [3] PSOOP ENEMY DEMONSTRATIONS (JAVA DESKTOP ^& COMBAT FLEET)
echo ==============================================================================
echo   Course Outcomes Covered: PSOOP-CO1, PSOOP-CO2, PSOOP-CO3, PSOOP-CO4
echo   Enemies Verified in Java Engine:
echo     1. Armoured Vanguard       (CO1: Encapsulation ^& Armor Mitigation)
echo     2. Interceptor Fighter     (CO1: Dynamic Dispatch Alignment ^& Dive)
echo     3. Adaptive Morphing Drone (CO2: State Pattern FSM ^& Algorithmic Complexity)
echo     4. Fail-Safe Sentry        (CO3: Defensive Clamping ^& Emergency Barrier)
echo     5. Kinetic Battering Ram   (CO4: Non-Uniform Acceleration ^& Heavy Prow Impact)
echo ------------------------------------------------------------------------------
if not exist "bin\java\test\GameUnitTest.class" (
    call scripts\build_java.bat
)
echo [INFO] Executing live programmatic verification for all 5 PSOOP enemy types...
java -cp bin\java test.GameUnitTest
echo.
echo [NOTE] In normal gameplay, all 5 enemies spawn across Waves 1 to 5.
set "LAUNCH_GAME=N"
set /p "LAUNCH_GAME=Launch active Java Space Shooter GUI now? (Y/N): "
if /i "%LAUNCH_GAME%"=="Y" (
    start "AIRSTRIKE - Java Combat Client" java -cp bin\java main.Main
    echo [INFO] Java combat window launched.
)
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 4. PL Enemy Demonstrations
rem --------------------------------------------------------------------------
:launch_pl_enemies
echo.
echo ==============================================================================
echo [4] PL ENEMY DEMONSTRATIONS (C++17 DATA STRUCTURES ^& ALGORITHMS)
echo ==============================================================================
echo   [TIP] In the PL interactive menu, select Option [8] for the dedicated:
echo         'PL Enemy Systems ^& Tactical Fleet Demonstrator' submenu.
echo         Contains live interactive demos for PL-CO1 through PL-CO5.
echo.
if not exist "bin\pl\pl_test_runner.exe" (
    echo [INFO] PL binary not found. Compiling with MinGW C++17...
    call scripts\build_pl.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] C++ compilation failed!
        pause
        goto :interactive_menu
    )
)
bin\pl\pl_test_runner.exe
echo.
echo [INFO] Returned from PL application.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 5. COA Enemy Demonstrations
rem --------------------------------------------------------------------------
:launch_coa_enemies
echo.
echo ==============================================================================
echo [5] COA ENEMY DEMONSTRATIONS (32-BIT INTEL 80386 ASSEMBLY SUITE)
echo ==============================================================================
echo   [TIP] In the COA menu, access each enemy under its respective CO submenu:
echo         - Select [1] -^> [6] for Sub-Register Aegis Sentry (COA-CO1: EAX/AX/AH/AL)
echo         - Select [2] -^> [6] for Fibonacci Skimmer (COA-CO2: Assembly Math Series)
echo         - Select [3] -^> [4] for Booth Multiplier Gunship (COA-CO3: Signed Multiply)
echo         - Select [4] -^> [3] for Pipelined Railgun (COA-CO4: 5-Stage Pipeline)
echo         - Or select [5] to run all demonstrations sequentially.
echo.
if not exist "bin\coa\coa_runner.exe" (
    echo [INFO] COA binary not found. Assembling 80386 programs...
    call scripts\build_coa.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Assembly compilation failed!
        pause
        goto :interactive_menu
    )
)
bin\coa\coa_runner.exe
echo.
echo [INFO] Returned from COA application.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 6. CGL Enemy Demonstrations
rem --------------------------------------------------------------------------
:launch_cgl_enemies
echo.
echo ==============================================================================
echo [6] CGL ENEMY DEMONSTRATIONS (OPENGL / GLUT GRAPHICS ENGINE)
echo ==============================================================================
echo   [TIP] In the CGL GLUT window:
echo         - Press [7] or [E] to switch to Screen 7 (Enemy Demonstrations Scene)
echo         - On Screen 7, press [1-6] to switch between all six CGL enemies:
echo             [1] Geometric Polygon Cruiser (CGL-CO1: CCW Polygon Fuselage ^& Wings)
echo             [2] Orbital Rotor Drone       (CGL-CO2: Hierarchical Matrix Transforms)
echo             [3] Bresenham Shield Sentinel (CGL-CO3: Midpoint Circle ^& Bresenham Line)
echo             [4] Depth-Diving Warp Cruiser (CGL-CO4: Perspective 1/Z Foreshortening)
echo             [5] Boundary Phase Skimmer    (CGL-CO5: Cohen-Sutherland Window Clipping)
echo             [6] Specular Phong Cruiser    (CGL-CO6: Blinn-Phong Specular ^& Texturing)
echo         - Press [ESC] to exit GLUT window.
echo.
if not exist "bin\cgl\cgl_runner.exe" (
    echo [INFO] CGL binary not found. Compiling OpenGL module...
    call scripts\build_cgl.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] OpenGL compilation failed!
        pause
        goto :interactive_menu
    )
)
echo [INFO] Launching CGL OpenGL Visual Demonstration Window...
start "AIRSTRIKE - Computer Graphics Lab (OpenGL)" bin\cgl\cgl_runner.exe
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 7. Build All Modules
rem --------------------------------------------------------------------------
:launch_build
echo.
call build_all.bat
echo.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 8. Run All Tests
rem --------------------------------------------------------------------------
:launch_tests
echo.
call test_all.bat
echo.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 9. View 20-Enemy Academic Roster
rem --------------------------------------------------------------------------
:show_roster
cls
echo ==============================================================================
echo                AIRSTRIKE - 20-ENEMY ACADEMIC ROSTER ^& CO MAPPING
echo ==============================================================================
echo   19 Course Outcomes (COs) Across 4 Core Computer Science Subjects (100%% Covered)
echo ==============================================================================
echo.
echo [1] PSOOP - Object-Oriented Programming (Java Desktop ^& TypeScript Web)
echo   1. Armoured Vanguard        ^| PSOOP-CO1 ^| Encapsulation, Private Fields, Armor Damage Reduction
echo   2. Interceptor Fighter      ^| PSOOP-CO1 ^| Polymorphism, Dynamic Dispatch, Alignment ^& Dive
echo   3. Adaptive Morphing Drone  ^| PSOOP-CO2 ^| State Pattern FSM (Patrol / Chase / Overcharge / Evade)
echo   4. Fail-Safe Sentry         ^| PSOOP-CO3 ^| Defensive Programming, Boundary Clamping, Barrier
echo   5. Kinetic Battering Ram    ^| PSOOP-CO4 ^| Dynamic Binding, Non-Uniform Acceleration, Prow Impact
echo.
echo [2] PL - Programming Laboratory (Standard C++17 Data Structures ^& Algorithms)
echo   6. Spatial Grid Swarm       ^| PL-CO1    ^| 2D Spatial Partitioning, Row-Major Indexing, Scanning
echo   7. Formation Squadron Chain ^| PL-CO2    ^| Doubly Linked List, Bidirectional Linkage, O(1) Split
echo   8. Queue Wave Echelon       ^| PL-CO3    ^| Circular Queue Array, Index Wraparound, FIFO Dispatch
echo   9. Target-Seeking Hunter    ^| PL-CO4    ^| QuickSort In-Place Partitioning, O(log N) Binary Search
echo  10. Hash-Shielded Matrix Craft^|PL-CO5   ^| Open Addressing Hash Table, Polynomial Hash, Linear Probing
echo.
echo [3] COA - Computer Organization ^& Architecture (32-bit Intel 80386 Assembly)
echo  11. Sub-Register Aegis Sentry^| COA-CO1   ^| Register Partitioning (EAX/AX/AH/AL), Flags, SIB Mode
echo  12. Fibonacci Skimmer        ^| COA-CO2   ^| Iterative Assembly Fibonacci Series, Flight Weave, Cooldown
echo  13. Booth Multiplier Gunship ^| COA-CO3   ^| 8-Cycle Booth's Signed Multiplication, Saturation
echo  14. Pipelined Railgun Dreadnought^|COA-CO4^| 5-Stage RISC Pipeline Model, RAW Hazard Stalls, Forwarding
echo.
echo [4] CGL - Computer Graphics Laboratory (C++17 / OpenGL / GLUT Visual Engine)
echo  15. Geometric Polygon Cruiser^| CGL-CO1   ^| CCW Polygon Fuselage, Delta Wings, Positive Shoelace Area
echo  16. Orbital Rotor Drone      ^| CGL-CO2   ^| Hierarchical Matrix Transforms, Non-Commutative T*R
echo  17. Bresenham Shield Sentinel^| CGL-CO3   ^| Midpoint Circle Algorithm, 8-Way Symmetry, Bresenham Line
echo  18. Depth-Diving Warp Cruiser^| CGL-CO4   ^| Perspective 1/Z Foreshortening vs Orthographic Invariance
echo  19. Boundary Phase Skimmer   ^| CGL-CO5   ^| Cohen-Sutherland Line Clipping, Sutherland-Hodgman Clip
echo  20. Specular Phong Cruiser   ^| CGL-CO6   ^| Blinn-Phong Specular Highlight, Reflection Vector, Texture
echo.
echo ==============================================================================
echo Total: Exactly 19 Course Outcomes (COs) ^| Exactly 20 Distinct Enemy Classes
echo ==============================================================================
echo.
pause
goto :interactive_menu

rem --------------------------------------------------------------------------
rem 10. Exit Application
rem --------------------------------------------------------------------------
:exit_launcher
echo.
echo Thank you for evaluating AIRSTRIKE. Systems safely powered down.
exit /b 0
