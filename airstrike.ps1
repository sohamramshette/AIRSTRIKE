# AIRSTRIKE: Master Unified Launcher (PowerShell Edition)
# Connects all 4 subject modules and 20 enemy demonstrations.
param(
    [Parameter(Position=0)]
    [string]$Option
)

$projectRoot = $PSScriptRoot
if (Test-Path (Join-Path $projectRoot "bin")) {
    Set-Location $projectRoot
}

function Pause-IfInteractive {
    if (-not $Option) {
        Read-Host "`nPress Enter to return to master menu"
    }
}

function Show-Menu {
    Clear-Host
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "                     AIRSTRIKE - UNIFIED MASTER LAUNCHER                      " -ForegroundColor Cyan
    Write-Host "                 Academic Demonstration & Verification System                 " -ForegroundColor Cyan
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "   Four Integrated Subjects - 19 Course Outcomes - 20 Academic Enemy Types    " -ForegroundColor White
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "  [1]  Play AIRSTRIKE — Java Desktop (Swing / AWT Space Combat Game)" -ForegroundColor Green
    Write-Host "  [2]  Launch AIRSTRIKE — Browser (HTML5 Canvas + TypeScript Web Client)" -ForegroundColor Green
    Write-Host "  [3]  PSOOP Enemy Demonstrations (Java Space Shooter & Enemy Fleet)" -ForegroundColor Green
    Write-Host "  [4]  PL Enemy Demonstrations (C++17 Data Structures & Tactical Fleet)" -ForegroundColor Green
    Write-Host "  [5]  COA Enemy Demonstrations (32-bit Intel 80386 Assembly Suite)" -ForegroundColor Green
    Write-Host "  [6]  CGL Enemy Demonstrations (OpenGL / GLUT Graphics — Screen 7)" -ForegroundColor Green
    Write-Host "  [7]  Build All Modules (build_all.ps1)" -ForegroundColor Yellow
    Write-Host "  [8]  Run All Tests (scripts\test_all.ps1 — 418 Assertions)" -ForegroundColor Yellow
    Write-Host "  [9]  View 20-Enemy Academic Roster (CO1 to CO19 Full Mapping)" -ForegroundColor Magenta
    Write-Host "  [10] Exit Application" -ForegroundColor Red
    Write-Host "==============================================================================" -ForegroundColor Cyan
}

function Show-Roster {
    Clear-Host
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "                AIRSTRIKE — 20-ENEMY ACADEMIC ROSTER & CO MAPPING             " -ForegroundColor Cyan
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "  19 Course Outcomes (COs) Across 4 Core Computer Science Subjects (100% Covered)" -ForegroundColor White
    Write-Host "==============================================================================" -ForegroundColor Cyan

    Write-Host "`n[1] PSOOP — Object-Oriented Programming (Java Desktop & TypeScript Web)" -ForegroundColor Yellow
    Write-Host "  1. Armoured Vanguard        | PSOOP-CO1 | Encapsulation, Private Fields, Armor Damage Reduction" -ForegroundColor White
    Write-Host "  2. Interceptor Fighter      | PSOOP-CO1 | Polymorphism, Dynamic Dispatch, Alignment & High-Speed Dash" -ForegroundColor White
    Write-Host "  3. Adaptive Morphing Drone  | PSOOP-CO2 | State Pattern FSM (Patrol / Chase / Overcharge / Evade)" -ForegroundColor White
    Write-Host "  4. Fail-Safe Sentry         | PSOOP-CO3 | Defensive Programming, Boundary Clamping, Emergency Barrier" -ForegroundColor White
    Write-Host "  5. Kinetic Battering Ram    | PSOOP-CO4 | Dynamic Binding, Non-Uniform Acceleration, Heavy Prow Impact" -ForegroundColor White

    Write-Host "`n[2] PL — Programming Laboratory (Standard C++17 Data Structures & Algorithms)" -ForegroundColor Yellow
    Write-Host "  6. Spatial Grid Swarm       | PL-CO1    | 2D Spatial Partitioning, Row-Major Indexing, Nearby Scanning" -ForegroundColor White
    Write-Host "  7. Formation Squadron Chain | PL-CO2    | Doubly Linked List, Bidirectional Linkage, O(1) Node Split" -ForegroundColor White
    Write-Host "  8. Queue Wave Echelon       | PL-CO3    | Circular Queue Array, Index Wraparound Arithmetic, FIFO Dispatch" -ForegroundColor White
    Write-Host "  9. Target-Seeking Hunter    | PL-CO4    | QuickSort In-Place Partitioning, Monotonic Check, O(log N) Search" -ForegroundColor White
    Write-Host " 10. Hash-Shielded Matrix Craft|PL-CO5   | Open Addressing Hash Table, Polynomial Hash, Linear Probing" -ForegroundColor White

    Write-Host "`n[3] COA — Computer Organization & Architecture (32-bit Intel 80386 Assembly)" -ForegroundColor Yellow
    Write-Host " 11. Sub-Register Aegis Sentry| COA-CO1   | Register Partitioning (EAX/AX/AH/AL), EFLAGS, SIB Addressing" -ForegroundColor White
    Write-Host " 12. Fibonacci Skimmer        | COA-CO2   | Iterative Assembly Fibonacci Series, Flight Weave, Salvo Cooldown" -ForegroundColor White
    Write-Host " 13. Booth Multiplier Gunship | COA-CO3   | 8-Cycle Booth's Signed Multiplication, 4 Sign Quadrants, Saturation" -ForegroundColor White
    Write-Host " 14. Pipelined Railgun Dreadnought|COA-CO4| 5-Stage RISC Pipeline Model, RAW Hazard Stalls, Bypass Forwarding" -ForegroundColor White

    Write-Host "`n[4] CGL — Computer Graphics Laboratory (C++17 / OpenGL / GLUT Visual Engine)" -ForegroundColor Yellow
    Write-Host " 15. Geometric Polygon Cruiser| CGL-CO1   | CCW Polygon Fuselage, Delta Wings, Positive Shoelace Area" -ForegroundColor White
    Write-Host " 16. Orbital Rotor Drone      | CGL-CO2   | Hierarchical Matrix Transforms, Non-Commutative T*R, Orbit Pods" -ForegroundColor White
    Write-Host " 17. Bresenham Shield Sentinel| CGL-CO3   | Midpoint Circle Algorithm, 8-Way Symmetry, Bresenham Line" -ForegroundColor White
    Write-Host " 18. Depth-Diving Warp Cruiser| CGL-CO4   | Perspective 1/Z Foreshortening vs Orthographic Invariance" -ForegroundColor White
    Write-Host " 19. Boundary Phase Skimmer   | CGL-CO5   | Cohen-Sutherland Line Clipping, Sutherland-Hodgman Polygon Clip" -ForegroundColor White
    Write-Host " 20. Specular Phong Cruiser   | CGL-CO6   | Blinn-Phong Specular Highlight, Reflection Vector, 24-bit BMP Texture" -ForegroundColor White

    Write-Host "`n==============================================================================" -ForegroundColor Cyan
    Write-Host "Total: Exactly 19 Course Outcomes (COs) | Exactly 20 Distinct Enemy Classes" -ForegroundColor Green
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Pause-IfInteractive
}

function Invoke-LauncherOption([string]$choice) {
    switch ($choice) {
        "1" {
            Write-Host "`n==============================================================================" -ForegroundColor Cyan
            Write-Host "  [1] PLAY AIRSTRIKE — JAVA DESKTOP SPACE COMBAT CLIENT                       " -ForegroundColor Cyan
            Write-Host "==============================================================================" -ForegroundColor Cyan
            $javaCmd = Get-Command "java" -ErrorAction SilentlyContinue
            if (-not $javaCmd) {
                Write-Host "[ERROR] Java Runtime (java.exe) was not found in PATH!" -ForegroundColor Red
                Write-Host "Please install Java 11 or higher and add it to your system PATH." -ForegroundColor Yellow
                Pause-IfInteractive
                return
            }
            if (-not (Test-Path "bin\java\main\Main.class")) {
                Write-Host "[INFO] Java classes not found in bin\java. Compiling module..." -ForegroundColor Yellow
                & ".\scripts\build_java.ps1"
                if ($LASTEXITCODE -ne 0) {
                    Write-Host "[ERROR] Java build failed!" -ForegroundColor Red
                    Pause-IfInteractive
                    return
                }
            }
            Write-Host "[INFO] Launching Java Desktop Game Client in an independent GUI window..." -ForegroundColor Green
            Start-Process -FilePath "java" -ArgumentList "-cp", "bin\java", "main.Main"
            Write-Host "[INFO] Java application started. Returning to master menu..." -ForegroundColor Gray
            Start-Sleep -Seconds 1
        }
        "2" {
            Write-Host "`n==============================================================================" -ForegroundColor Cyan
            Write-Host "  [2] LAUNCH AIRSTRIKE — BROWSER EDITION (TYPESCRIPT & HTML5 CANVAS)          " -ForegroundColor Cyan
            Write-Host "==============================================================================" -ForegroundColor Cyan
            $nodeCmd = Get-Command "node" -ErrorAction SilentlyContinue
            $npmCmd = Get-Command "npm" -ErrorAction SilentlyContinue
            if (-not $nodeCmd -or -not $npmCmd) {
                Write-Host "[ERROR] Node.js and npm were not found in PATH!" -ForegroundColor Red
                Write-Host "Please install Node.js 18+ and ensure 'node' and 'npm' are in your PATH." -ForegroundColor Yellow
                Pause-IfInteractive
                return
            }
            if (-not (Test-Path "web\node_modules")) {
                Write-Host "[INFO] web/node_modules not found. Installing web dependencies..." -ForegroundColor Yellow
                npm --prefix web install
                if ($LASTEXITCODE -ne 0) {
                    Write-Host "[ERROR] npm install failed!" -ForegroundColor Red
                    Pause-IfInteractive
                    return
                }
            }
            Write-Host "[INFO] Starting local Vite development server for Web client..." -ForegroundColor Green
            Start-Process -FilePath "cmd.exe" -ArgumentList "/c cd /d `"$projectRoot\web`" && npm run dev"
            Start-Sleep -Seconds 2
            Write-Host "[INFO] Opening browser at http://localhost:3000..." -ForegroundColor Green
            Start-Process "http://localhost:3000"
            Write-Host "[INFO] Browser client running in background. Returning to master menu..." -ForegroundColor Gray
            Start-Sleep -Seconds 1
        }
        "3" {
            Write-Host "`n==============================================================================" -ForegroundColor Cyan
            Write-Host "  [3] PSOOP ENEMY DEMONSTRATIONS (JAVA DESKTOP & COMBAT FLEET)                " -ForegroundColor Cyan
            Write-Host "==============================================================================" -ForegroundColor Cyan
            Write-Host "  Course Outcomes Covered: PSOOP-CO1, PSOOP-CO2, PSOOP-CO3, PSOOP-CO4" -ForegroundColor White
            Write-Host "  Enemies Verified in Java Engine:" -ForegroundColor Gray
            Write-Host "    1. Armoured Vanguard       (CO1: Encapsulation & Armor Mitigation)" -ForegroundColor Gray
            Write-Host "    2. Interceptor Fighter     (CO1: Polymorphism, Dynamic Dispatch & Alignment Dive)" -ForegroundColor Gray
            Write-Host "    3. Adaptive Morphing Drone (CO2: State Pattern FSM & Algorithmic Complexity)" -ForegroundColor Gray
            Write-Host "    4. Fail-Safe Sentry        (CO3: Defensive Boundary Clamping & Emergency Barrier)" -ForegroundColor Gray
            Write-Host "    5. Kinetic Battering Ram   (CO4: Non-Uniform Acceleration & Heavy Prow Impact)" -ForegroundColor Gray
            Write-Host "------------------------------------------------------------------------------" -ForegroundColor Gray
            if (-not (Test-Path "bin\java\test\GameUnitTest.class")) {
                & ".\scripts\build_java.ps1"
            }
            Write-Host "[INFO] Executing live programmatic verification for all 5 PSOOP enemy types..." -ForegroundColor Yellow
            java -cp bin\java test.GameUnitTest
            Write-Host "`n[NOTE] In normal gameplay, all 5 enemies spawn across progressive combat sectors (Waves 1 to 5)." -ForegroundColor Cyan
            if ($Option) {
                Start-Process -FilePath "java" -ArgumentList "-cp", "bin\java", "main.Main", "--demo"
                Write-Host "[INFO] Java 5-Enemy Demonstration arena launched." -ForegroundColor Green
            } else {
                $launchGame = Read-Host "Launch active Java 5-Enemy Academic Showcase GUI arena now? [Y/N, default=Y]"
                if ($launchGame -eq "" -or $launchGame -eq "Y" -or $launchGame -eq "y") {
                    Start-Process -FilePath "java" -ArgumentList "-cp", "bin\java", "main.Main", "--demo"
                    Write-Host "[INFO] Java 5-Enemy Demonstration arena launched." -ForegroundColor Green
                }
            }
            Pause-IfInteractive
        }
        "4" {
            Write-Host "`n==============================================================================" -ForegroundColor Cyan
            Write-Host "  [4] PL ENEMY DEMONSTRATIONS (C++17 DATA STRUCTURES & ALGORITHMS)            " -ForegroundColor Cyan
            Write-Host "==============================================================================\n" -ForegroundColor Cyan
            Write-Host "  [TIP] In the PL interactive menu, select Option [8] for the dedicated:" -ForegroundColor Yellow
            Write-Host "        'PL Enemy Systems & Tactical Fleet Demonstrator' submenu." -ForegroundColor Yellow
            Write-Host "        Contains live interactive demos for PL-CO1 through PL-CO5.\n" -ForegroundColor Yellow
            if (-not (Test-Path "bin\pl\pl_test_runner.exe")) {
                Write-Host "[INFO] Compiling PL C++17 module..." -ForegroundColor Yellow
                & ".\scripts\build_pl.ps1"
            }
            & "bin\pl\pl_test_runner.exe"
            Pause-IfInteractive
        }
        "5" {
            Write-Host "`n==============================================================================" -ForegroundColor Cyan
            Write-Host "  [5] COA ENEMY DEMONSTRATIONS (32-BIT INTEL 80386 ASSEMBLY SUITE)             " -ForegroundColor Cyan
            Write-Host "==============================================================================\n" -ForegroundColor Cyan
            Write-Host "  [TIP] In the COA menu, access each enemy under its respective CO submenu:" -ForegroundColor Yellow
            Write-Host "        - Select [1] -> [6] for Sub-Register Aegis Sentry (COA-CO1: EAX/AX/AH/AL)" -ForegroundColor White
            Write-Host "        - Select [2] -> [6] for Fibonacci Skimmer (COA-CO2: Assembly Math Series)" -ForegroundColor White
            Write-Host "        - Select [3] -> [4] for Booth Multiplier Gunship (COA-CO3: Signed Multiply)" -ForegroundColor White
            Write-Host "        - Select [4] -> [3] for Pipelined Railgun (COA-CO4: 5-Stage Pipeline Simulation)" -ForegroundColor White
            Write-Host "        - Or select [5] to run all demonstrations sequentially.\n" -ForegroundColor White
            if (-not (Test-Path "bin\coa\coa_runner.exe")) {
                Write-Host "[INFO] Assembling COA 80386 module..." -ForegroundColor Yellow
                & ".\scripts\build_coa.ps1"
            }
            & "bin\coa\coa_runner.exe"
            Pause-IfInteractive
        }
        "6" {
            Write-Host "`n==============================================================================" -ForegroundColor Cyan
            Write-Host "  [6] CGL ENEMY DEMONSTRATIONS (OPENGL / GLUT GRAPHICS ENGINE)                 " -ForegroundColor Cyan
            Write-Host "==============================================================================\n" -ForegroundColor Cyan
            Write-Host "  [TIP] In the CGL GLUT window:" -ForegroundColor Yellow
            Write-Host "        - Press [7] or [E] to switch to Screen 7 (Enemy Demonstrations Scene)" -ForegroundColor White
            Write-Host "        - On Screen 7, press [1-6] to switch between all six CGL enemies:" -ForegroundColor White
            Write-Host "            [1] Geometric Polygon Cruiser (CGL-CO1: CCW Polygon Fuselage & Wings)" -ForegroundColor Gray
            Write-Host "            [2] Orbital Rotor Drone       (CGL-CO2: Hierarchical Matrix Transforms)" -ForegroundColor Gray
            Write-Host "            [3] Bresenham Shield Sentinel (CGL-CO3: Midpoint Circle & Bresenham Line)" -ForegroundColor Gray
            Write-Host "            [4] Depth-Diving Warp Cruiser (CGL-CO4: Perspective 1/Z Foreshortening)" -ForegroundColor Gray
            Write-Host "            [5] Boundary Phase Skimmer    (CGL-CO5: Cohen-Sutherland Window Clipping)" -ForegroundColor Gray
            Write-Host "            [6] Specular Phong Cruiser    (CGL-CO6: Blinn-Phong Specular & Texturing)" -ForegroundColor Gray
            Write-Host "        - Press [ESC] to exit GLUT window.\n" -ForegroundColor Yellow
            if (-not (Test-Path "bin\cgl\cgl_runner.exe")) {
                Write-Host "[INFO] Compiling CGL OpenGL module..." -ForegroundColor Yellow
                & ".\scripts\build_cgl.ps1"
            }
            Write-Host "[INFO] Launching CGL OpenGL Visual Demonstration Window..." -ForegroundColor Green
            Start-Process -FilePath "bin\cgl\cgl_runner.exe"
            Pause-IfInteractive
        }
        "7" {
            Write-Host "`n[BUILD] Building all four subject modules..." -ForegroundColor Cyan
            & ".\build_all.ps1"
            Pause-IfInteractive
        }
        "8" {
            Write-Host "`n[TEST] Running master automated test suite across all 4 modules..." -ForegroundColor Cyan
            & ".\scripts\test_all.ps1"
            Pause-IfInteractive
        }
        "9" {
            Show-Roster
        }
        "roster" {
            Show-Roster
        }
        "10" {
            Write-Host "`nThank you for evaluating AIRSTRIKE. Systems safely powered down." -ForegroundColor Green
            exit 0
        }
        "exit" {
            Write-Host "`nThank you for evaluating AIRSTRIKE. Systems safely powered down." -ForegroundColor Green
            exit 0
        }
        Default {
            Write-Host "`n[ERROR] Invalid option '$choice'. Please enter a number between 1 and 10." -ForegroundColor Red
            Start-Sleep -Seconds 1
        }
    }
}

if ($Option) {
    Invoke-LauncherOption -choice $Option
    exit 0
}

while ($true) {
    Show-Menu
    $choice = Read-Host "Select an option [1-10]"
    if ([string]::IsNullOrWhiteSpace($choice)) { break }
    Invoke-LauncherOption -choice $choice
}
