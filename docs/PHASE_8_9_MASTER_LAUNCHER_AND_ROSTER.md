# AIRSTRIKE — Phase 8.9: Master Launcher & 20-Enemy Roster Integration Report

## 1. Executive Summary

Phase 8.9 delivers a unified, production-grade master launcher navigation system and establishes the complete 20-enemy academic roster across all four autonomous subject modules:
- **Unified Navigation Experience**: Both Windows Command Prompt (`airstrike.bat`, `run_airstrike.bat`) and PowerShell (`airstrike.ps1`) launchers now provide an identical, 10-option interactive menu.
- **Autonomous Module Execution**: Each subject module (Java Desktop, Browser Client, PL C++17, COA 80386 Assembly, and CGL OpenGL/GLUT) remains independently executable with its own build systems and test runners.
- **20-Enemy Academic Roster**: All 20 enemy types are cataloged and aligned with the 19 Course Outcomes (COs).
- **Test-Count Reconciliation**: The historical discrepancy between the legacy 302/328 verification matrix and the actual individual module totals has been resolved: **418 automated assertions** are verified across all modules (169 PSOOP + 99 PL + 57 COA + 93 CGL) with zero failures.

---

## 2. Master Launcher Architecture & Menu Options

The master launcher provides the following options across `airstrike.bat` and `airstrike.ps1`:

```
==============================================================================
                     AIRSTRIKE - UNIFIED MASTER LAUNCHER
                 Academic Demonstration & Verification System
==============================================================================
   Four Integrated Subjects - 19 Course Outcomes - 20 Academic Enemy Types
==============================================================================
  [1]  Play AIRSTRIKE — Java Desktop (Swing / AWT Space Combat Game)
  [2]  Launch AIRSTRIKE — Browser (HTML5 Canvas + TypeScript Web Client)
  [3]  PSOOP Enemy Demonstrations (Java Space Shooter & Enemy Fleet)
  [4]  PL Enemy Demonstrations (C++17 Data Structures & Tactical Fleet)
  [5]  COA Enemy Demonstrations (32-bit Intel 80386 Assembly Suite)
  [6]  CGL Enemy Demonstrations (OpenGL / GLUT Graphics — Screen 7)
  [7]  Build All Modules (build_all.bat / build_all.ps1)
  [8]  Run All Tests (test_all.bat / scripts\test_all.ps1 — 418 Assertions)
  [9]  View 20-Enemy Academic Roster (CO1 to CO19 Full Mapping)
  [10] Exit Application
==============================================================================
```

### 2.1 Invocation and Process Mapping

| Option | Displayed Name | Invoked Binary / Script | Process Execution Model | Returning Behavior |
|---|---|---|---|---|
| **[1]** | **Play AIRSTRIKE — Java Desktop** | `java -cp bin\java main.Main` | Detached GUI Window (Swing Event Dispatch Thread) | Launches independent GUI window; console returns immediately to master menu |
| **[2]** | **Launch AIRSTRIKE — Browser** | `cmd /k "cd web && npm run dev"` + `start http://localhost:5173` | Detached Background Node.js Process & Browser Window | Opens Vite dev server in terminal and launches browser; console returns to master menu |
| **[3]** | **PSOOP Enemy Demonstrations** | `java -cp bin\java test.GameUnitTest` + prompt for `main.Main` | Synchronous Console Execution + Optional GUI Launch | Displays live programmatic enemy test assertions; prompts to launch game GUI, then pauses and returns |
| **[4]** | **PL Enemy Demonstrations** | `bin\pl\pl_test_runner.exe` | Synchronous Console CLI Application | Interactive console session (Submenu `[8]`); exiting PL returns cleanly to master menu |
| **[5]** | **COA Enemy Demonstrations** | `bin\coa\coa_runner.exe` | Synchronous Console CLI Application | Interactive console session (Submenus `[1]`, `[2]`, `[3]`, `[4]`); exiting COA returns cleanly to master menu |
| **[6]** | **CGL Enemy Demonstrations** | `bin\cgl\cgl_runner.exe` | Detached OpenGL/GLUT Window | Launches GLUT window with Screen 7 instructions; pauses and returns cleanly on exit |
| **[7]** | **Build All Modules** | `build_all.bat` / `build_all.ps1` | Synchronous Batch / PowerShell Script | Executes 4 toolchains sequentially (`javac`, `g++`, `gcc -m32`); pauses and returns to menu |
| **[8]** | **Run All Tests** | `test_all.bat` / `scripts\test_all.ps1` | Synchronous Test Suite Harness | Executes all 418 assertions across 4 modules; pauses and returns to menu |
| **[9]** | **View 20-Enemy Academic Roster** | Built-in launcher function | In-Terminal Display | Renders formatted 20-enemy roster table; pauses and returns on Enter |
| **[10]** | **Exit Application** | Script exit command | Terminates launcher process | Clean exit with code 0 |

---

## 3. Navigation Flow for All Four Subject Modules

### 3.1 PSOOP Navigation Flow
- **Gameplay**: Selecting Option `[1]` launches the Java Swing GUI (`main.Main`). The main window provides full CardLayout navigation between the Main Menu, active Combat Canvas, Instructions Dialog, Settings Dialog, and Supabase Authentication/Leaderboard dialogs.
- **Enemy Demonstrations**: Selecting Option `[3]` displays the academic concepts of all 5 PSOOP enemies, runs the automated test assertions for each enemy class (`test.GameUnitTest`), and provides a prompt to launch the interactive game client where the enemies spawn progressively across Waves 1 to 5.

### 3.2 PL Navigation Flow
- Selecting Option `[4]` launches `bin\pl\pl_test_runner.exe`.
- The user is greeted by the master PL menu:
  - Select Option `[8]` for the **PL Enemy Systems & Tactical Fleet Demonstrator** submenu.
  - Inside the submenu, select:
    - `[1]` Spatial Grid Swarm (PL-CO1: 2D Spatial Partitioning & Row-Major Memory)
    - `[2] `Formation Squadron Chain (PL-CO2: Doubly-Linked Fleet & $O(1)$ Node Splitting)
    - `[3]` Queue Wave Echelon (PL-CO3: Circular Queue & FIFO Dispatch Wraparound)
    - `[4]` Target-Seeking Hunter (PL-CO4: QuickSort In-Place Partitioning & $O(\log N)$ Binary Search)
    - `[5]` Hash-Shielded Matrix Craft (PL-CO5: Open Addressing & Tombstone Markers)
    - `[6]` Return to Main Menu
  - Select Option `[10]` to exit PL and return to the AIRSTRIKE master launcher.

### 3.3 COA Navigation Flow
- Selecting Option `[5]` launches `bin\coa\coa_runner.exe`.
- The user is greeted by the master 80386 Assembly menu:
  - Select `[1]` -> `[6]` for **Sub-Register Aegis Sentry** (COA-CO1: `EAX/AX/AH/AL` Register Partitioning & SIB Mode)
  - Select `[2]` -> `[6]` for **Fibonacci Skimmer** (COA-CO2: Iterative Assembly Fibonacci Series & Weave)
  - Select `[3]` -> `[4]` for **Booth Multiplier Gunship** (COA-CO3: 8-Cycle Booth's Signed Multiplication)
  - Select `[4]` -> `[3]` for **Pipelined Railgun Dreadnought** (COA-CO4: 5-Stage RISC Pipeline Model & Forwarding)
  - Select `[5]` to run all architecture demonstrations, enemy simulations, and automated tests sequentially.
  - Select `[6]` to exit COA and return to the AIRSTRIKE master launcher.

### 3.4 CGL Navigation Flow
- Selecting Option `[6]` launches `bin\cgl\cgl_runner.exe` in an OpenGL/GLUT window.
- In the GLUT window:
  - Press `[7]` or `[E]` to switch directly to **Screen 7 (Enemy Demonstrations Scene)**.
  - On Screen 7, press `[1-6]` to inspect the six CGL enemies:
    - `[1]` Geometric Polygon Cruiser (CGL-CO1: CCW Polygon Fuselage & Delta Wings Geometry)
    - `[2]` Orbital Rotor Drone (CGL-CO2: Hierarchical Matrix Transforms & Animated Orbit Pods)
    - `[3]` Bresenham Shield Sentinel (CGL-CO3: Midpoint Circle Algorithm & Bresenham Line)
    - `[4]` Depth-Diving Warp Cruiser (CGL-CO4: Perspective 1/Z Foreshortening vs Orthographic Invariance)
    - `[5]` Boundary Phase Skimmer (CGL-CO5: Cohen-Sutherland Line & Sutherland-Hodgman Polygon Clipping)
    - `[6]` Specular Phong Cruiser (CGL-CO6: Blinn-Phong Specular Lighting & 24-bit BMP Texturing)
  - Press `[ESC]` to close the GLUT window and return to the master launcher.

---

## 4. Browser Client Workflow & Local Setup

The browser client is built with TypeScript, HTML5 Canvas 2D, and Vite, connecting to the Supabase PostgreSQL backend.

### 4.1 Prerequisites
- Node.js 18+ (tested on Node.js v24)
- npm 9+

### 4.2 Local Execution Workflow
```bash
# Navigate to web directory
cd web

# Install dependencies (only required once)
npm install

# Start Vite development server
npm run dev

# Run automated unit test suite (44 tests)
npm test

# Build production bundle
npm run build
```

### 4.3 Environment Configuration & Security
The browser client requires only public, publishable credentials in `web/.env` or `web/.env.local`:
```env
VITE_SUPABASE_URL=https://qbhotsqwpxbmtpqbgpjy.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=sb_publishable_45YsUzEdi6crovilqrubwg_Z6QgVVsM
```
> [!CAUTION]
> Under no circumstances is the Supabase `service_role` key exposed to or stored within the web client. The web client operates exclusively under Row-Level Security (RLS) policies using the publishable client key.

---

## 5. Twenty-Enemy Academic Roster & Course Outcome Mapping

The curriculum retains **exactly 19 Course Outcomes (COs)** and **exactly 20 Academic Enemy Classes** distributed across all four subjects:

```
==============================================================================
                AIRSTRIKE — 20-ENEMY ACADEMIC ROSTER & CO MAPPING             
==============================================================================
  19 Course Outcomes (COs) Across 4 Core Computer Science Subjects (100% Covered)
==============================================================================

[1] PSOOP — Object-Oriented Programming (Java Desktop & TypeScript Web)
  1. Armoured Vanguard        | PSOOP-CO1 | Encapsulation, Private Fields, Armor Damage Reduction
  2. Interceptor Fighter      | PSOOP-CO1 | Polymorphism, Dynamic Dispatch, Alignment & High-Speed Dash
  3. Adaptive Morphing Drone  | PSOOP-CO2 | State Pattern FSM (Patrol / Chase / Overcharge / Evade)
  4. Fail-Safe Sentry         | PSOOP-CO3 | Defensive Programming, Boundary Clamping, Emergency Barrier
  5. Kinetic Battering Ram    | PSOOP-CO4 | Dynamic Binding, Non-Uniform Acceleration, Heavy Prow Impact

[2] PL — Programming Laboratory (Standard C++17 Data Structures & Algorithms)
  6. Spatial Grid Swarm       | PL-CO1    | 2D Spatial Partitioning, Row-Major Indexing, Nearby Scanning
  7. Formation Squadron Chain | PL-CO2    | Doubly Linked List, Bidirectional Linkage, O(1) Node Split
  8. Queue Wave Echelon       | PL-CO3    | Circular Queue Array, Index Wraparound Arithmetic, FIFO Dispatch
  9. Target-Seeking Hunter    | PL-CO4    | QuickSort In-Place Partitioning, Monotonic Check, O(log N) Search
 10. Hash-Shielded Matrix Craft|PL-CO5   | Open Addressing Hash Table, Polynomial Hash, Linear Probing

[3] COA — Computer Organization & Architecture (32-bit Intel 80386 Assembly)
 11. Sub-Register Aegis Sentry| COA-CO1   | Register Partitioning (EAX/AX/AH/AL), EFLAGS, SIB Addressing
 12. Fibonacci Skimmer        | COA-CO2   | Iterative Assembly Fibonacci Series, Flight Weave, Salvo Cooldown
 13. Booth Multiplier Gunship | COA-CO3   | 8-Cycle Booth's Signed Multiplication, 4 Sign Quadrants, Saturation
 14. Pipelined Railgun Dreadnought|COA-CO4| 5-Stage RISC Pipeline Model, RAW Hazard Stalls, Bypass Forwarding

[4] CGL — Computer Graphics Laboratory (C++17 / OpenGL / GLUT Visual Engine)
 15. Geometric Polygon Cruiser| CGL-CO1   | CCW Polygon Fuselage, Delta Wings, Positive Shoelace Area
 16. Orbital Rotor Drone      | CGL-CO2   | Hierarchical Matrix Transforms, Non-Commutative T*R, Orbit Pods
 17. Bresenham Shield Sentinel| CGL-CO3   | Midpoint Circle Algorithm, 8-Way Symmetry, Bresenham Line
 18. Depth-Diving Warp Cruiser| CGL-CO4   | Perspective 1/Z Foreshortening vs Orthographic Invariance
 19. Boundary Phase Skimmer   | CGL-CO5   | Cohen-Sutherland Line Clipping, Sutherland-Hodgman Polygon Clip
 20. Specular Phong Cruiser   | CGL-CO6   | Blinn-Phong Specular Highlight, Reflection Vector, 24-bit BMP Texture
==============================================================================
Total: Exactly 19 Course Outcomes (COs) | Exactly 20 Distinct Enemy Classes
==============================================================================
```

---

## 6. Build and Test Commands

### 6.1 Master Build Commands
- **Windows Command Prompt**:
  ```cmd
  build_all.bat
  ```
- **PowerShell**:
  ```powershell
  .\build_all.ps1
  ```

### 6.2 Master Test Commands
- **Windows Command Prompt**:
  ```cmd
  test_all.bat
  ```
- **PowerShell**:
  ```powershell
  .\scripts\test_all.ps1
  ```

### 6.3 Individual Module Build & Test Commands
- **PSOOP (Java)**:
  - Build: `powershell -File scripts\build_java.ps1`
  - Test: `powershell -File scripts\test_psoop.ps1`
- **PL (C++17)**:
  - Build: `powershell -File scripts\build_pl.ps1`
  - Test: `.\bin\pl\pl_test_runner.exe --test`
- **COA (32-bit Assembly)**:
  - Build: `powershell -File scripts\build_coa.ps1`
  - Test: `.\bin\coa\coa_runner.exe --test`
- **CGL (OpenGL/GLUT)**:
  - Build: `powershell -File scripts\build_cgl.ps1`
  - Test: `.\bin\cgl\cgl_runner.exe --test`
- **Web (TypeScript)**:
  - Test: `npm --prefix web test`

---

## 7. Test-Count Reconciliation

### 7.1 Historical Context of Reported Numbers
In earlier phase reports, two master numbers were referenced:
- **302 Tests**: The original count of baseline tests prior to the enemy implementation phase (Phase 8).
- **328 Assertions**: The matrix count when 26 PSOOP integration checks were tallied alongside the baseline assertions ($169 + 53 + 41 + 65 = 328$).

### 7.2 Breakdown of Baseline vs. Enemy Implementation Assertions

| Subject Module | Pre-Phase 8 Baseline Assertions | Phase 8 New Enemy Assertions | Current Verified Total Assertions | Status |
|---|---|---|---|---|
| **PSOOP** | 143 (41 unit + 32 auth + 25 match + 45 integration) | **+26** (25 enemy unit + 1 enemy wave integration) | **169 Assertions** | **PASSED** |
| **PL** | 53 (Arrays 10, Lists 10, Stack 6, Queue 6, Search 6, Sort 7, Hash 8) | **+46** (Swarm 12, Squadron 9, Queue 8, Hunter 8, Hash 9) | **99 Assertions** | **PASSED** |
| **COA** | 41 (CO1 6, CO2 15, CO3 18, CO4 2) | **+16** (Aegis 6, Skimmer 2, Booth 5, Railgun 3) | **57 Assertions** | **PASSED** |
| **CGL** | 65 (CO1 10, CO2 10, CO3 10, CO4 12, CO5 12, CO6 11) | **+28** (Polygon 5, Rotor 5, Raster 5, Warp 3, Skimmer 5, Phong 5) | **93 Assertions** | **PASSED** |
| **TOTAL** | **302 Baseline Assertions** | **+116 New Enemy Assertions** | **418 Assertions Total** | **100% PASS** |

### 7.3 Mathematical Reconciliation
$$\text{Total Assertions} = 169\ (\text{PSOOP}) + 99\ (\text{PL}) + 57\ (\text{COA}) + 93\ (\text{CGL}) = \mathbf{418\ \text{Assertions}}$$

All 418 programmatic assertions are verified by `scripts/test_all.ps1` and `test_all.bat`.

---

## 8. Known Limitations & Boundaries

1. **Process Independence**: Java Desktop and CGL OpenGL/GLUT are desktop GUI applications requiring windowing system event loops. They run in independent processes and cannot render inside the text console; the master launcher returns cleanly after spawning their respective windows.
2. **Web Server Background Process**: Launching the browser client initiates a local Vite development server via `cmd.exe`. To terminate the web server, the student must close the associated terminal window or terminate the node process.
3. **MinGW 32-bit Compatibility**: Building the COA 80386 assembly module requires a toolchain capable of generating 32-bit PE binaries (`-m32`). On 64-bit MinGW-w64 without multilib support, prebuilt binaries in `bin/coa/` are utilized.
4. **GLUT Window Focus**: On certain Windows display configurations, the OpenGL/GLUT window may spawn beneath active windows; students should bring the window to the foreground to inspect Screen 7.
5. **No Cross-Module Bridging**: In compliance with academic evaluation rules, Java and C++ modules do not communicate via JNI, shared memory, or network sockets. Each subject stands strictly on its own genuine implementation.
