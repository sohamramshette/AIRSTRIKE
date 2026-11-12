# AIRSTRIKE – 2D Space Shooter & Academic Demonstration System

![Status](https://img.shields.io/badge/status-complete__verified-brightgreen.svg)
![COs](https://img.shields.io/badge/Course%20Outcomes-19%20of%2019%20Verified-blue.svg)
![Enemies](https://img.shields.io/badge/Academic%20Enemies-20%20of%2020%20Verified-blueviolet.svg)
![Tests](https://img.shields.io/badge/Automated%20Tests-508%20Passing%20(100%25)-success.svg)
![Multiplayer](https://img.shields.io/badge/Multiplayer-1v1%20Realtime%20Score%20Attack-purple.svg)
![Java](https://img.shields.io/badge/Java-26%20%7C%20Swing%20%7C%20AWT-orange.svg)
![C++](https://img.shields.io/badge/C++-17%20Standard-brightgreen.svg)
![Assembly](https://img.shields.io/badge/Assembly-80386%2032--bit-red.svg)
![OpenGL](https://img.shields.io/badge/OpenGL-GLUT%20%2B%20GLU-blue.svg)
![Database](https://img.shields.io/badge/Database-Supabase%20PostgreSQL-darkblue.svg)

---

## 1. Project Overview

**AIRSTRIKE** is an integrated multi-subject academic software engineering system and 2D space shooter demonstration platform. Built entirely from scratch, it exhaustively demonstrates all **19 Course Outcomes (COs)** across four core computer science subjects:

1. **Principles of Software Object-Oriented Programming (PSOOP)**: Java 26 / Swing / AWT (4 Course Outcomes)
2. **Programming Laboratory (PL)**: Standard C++17 (5 Course Outcomes)
3. **Computer Organization and Architecture (COA)**: Pure 32-bit Intel 80386 Assembly Language (4 Course Outcomes)
4. **Computer Graphics Laboratory (CGL)**: C++17 with OpenGL and GLUT (6 Course Outcomes)
5. **Cloud Backend**: Supabase PostgreSQL with PostgREST and Row-Level Security (RLS)

### Crucial Architectural Principle: The Autonomy Rule
> [!IMPORTANT]
> **Strict Subject Independence**: The Java and C++ modules operate completely independently. There are **no JNI bindings**, **no TCP/UDP socket bridges**, and **no cross-language pipes**. Each subject module is an autonomous, self-contained subsystem with its own compilers, toolchains, build targets, test runners, and entry points.

---

## 2. Technology Stack & Four Autonomous Subjects

| Subject | Technology / Stack | Toolchain | Academic Focus |
| :--- | :--- | :--- | :--- |
| **PSOOP** | Java SE 26 / Swing / AWT | `javac` / JVM | Inheritance, design patterns, 60 FPS loop, EDT multithreading, cloud REST client |
| **PL** | ISO C++17 Standard | MinGW `g++` | Manual memory management, Rule of 5, arrays, linked lists, stacks, queues, sorts, hashing |
| **COA** | 32-bit Intel 80386 Assembly | MinGW `gcc -m32` / GAS | GPRs, flags, SIB addressing, Booth's multiplication, restoring division, 5-stage pipeline |
| **CGL** | C++17 + OpenGL + GLUT | MinGW `g++` + `opengl32` | 8 primitives, affine transformations, Bresenham, midpoint curves, viewing, clipping, Phong/BMP |
| **Cloud** | Supabase PostgreSQL | PostgREST / TLS 1.3 | Online pilot auth, match saving, personal flight logs, global Hall of Fame leaderboard |

---

## 3. Summary of 19 Course Outcomes

| Subject | CO ID | Official Course Outcome Topic | Target Subsystem / Files | Status |
| :--- | :--- | :--- | :--- | :---: |
| **PSOOP** | **PSOOP-CO1** | Object-Oriented Solutions & Inheritance | `PSOOP/src/model/*.java` | **VERIFIED** |
| **PSOOP** | **PSOOP-CO2** | Solutions to Reduce Complexity (Services & Patterns) | `PSOOP/src/service/*.java`, `game/*.java` | **VERIFIED** |
| **PSOOP** | **PSOOP-CO3** | Critical Thinking & Problem Solving (Exceptions & Net) | `PSOOP/src/exception/*.java`, `database/*.java` | **VERIFIED** |
| **PSOOP** | **PSOOP-CO4** | Logic-Building for Real-Life Problems (Game Loop & UI) | `PSOOP/src/game/GameEngine.java`, `ui/*.java` | **VERIFIED** |
| **PL** | **PL-CO1** | Arrays (DynamicArray, BulletPool, Spatial Grid) | `PL/src/arrays/*.cpp` | **VERIFIED** |
| **PL** | **PL-CO2** | Suitable Linked Lists (Singly, Doubly, Circular) | `PL/src/linked_lists/*.cpp` | **VERIFIED** |
| **PL** | **PL-CO3** | Stack & Queue Applications (ActionStack, WaveQueue) | `PL/src/stacks/*.cpp`, `queues/*.cpp` | **VERIFIED** |
| **PL** | **PL-CO4** | Searching & Sorting Techniques (5 Sorts, 2 Searches) | `PL/src/searching/*.cpp`, `sorting/*.cpp` | **VERIFIED** |
| **PL** | **PL-CO5** | Hashing Techniques (Chaining, Linear Probing, Rehashing)| `PL/src/hashing/*.cpp` | **VERIFIED** |
| **COA** | **COA-CO1** | 80386 Architecture & Instruction Set (Registers, Flags) | `COA/src/co1_architecture/arch_demo.s` | **VERIFIED** |
| **COA** | **COA-CO2** | Assembly Language Programs (10 Core 80386 Programs) | `COA/src/co2_assembly_programs/*.s` | **VERIFIED** |
| **COA** | **COA-CO3** | Computer Arithmetic (Booth's & Restoring Division) | `COA/src/co3_arithmetic/*.s` | **VERIFIED** |
| **COA** | **COA-CO4** | Control Unit & 5-Stage Instruction Pipelining | `COA/src/co4_control_unit/*.s` | **VERIFIED** |
| **CGL** | **CGL-CO1** | OpenGL Graphics Primitives (8 Fundamental Primitives) | `CGL/src/primitives/space_primitives.cpp` | **VERIFIED** |
| **CGL** | **CGL-CO2** | Geometric Transformations (Affine, Pivot, Non-comm) | `CGL/src/transformations/transform_demo.cpp` | **VERIFIED** |
| **CGL** | **CGL-CO3** | 2D/3D Algorithms (Bresenham 8 Octants, Curves, 3D Mesh)| `CGL/src/algorithms/raster_algorithms.cpp` | **VERIFIED** |
| **CGL** | **CGL-CO4** | Projection & Viewing (Ortho vs Perspective, Camera) | `CGL/src/projection/viewing_demo.cpp` | **VERIFIED** |
| **CGL** | **CGL-CO5** | Clipping Algorithms (Cohen-Suth, Liang-B, Suth-Hodg) | `CGL/src/clipping/clipping_algorithms.cpp` | **VERIFIED** |
| **CGL** | **CGL-CO6** | Texture, Lighting & Color (Local BMP, Phong, HSV) | `CGL/src/appearance/lighting_texture_demo.cpp` | **VERIFIED** |

---

## 4. Online 1v1 Competitive Multiplayer (Phase 12)

AIRSTRIKE features a real-time, cross-platform competitive **1v1 SCORE ATTACK** mode enabling two authenticated students to compete simultaneously across separate computers.

### Core Multiplayer Architecture:
1. **Match Lifecycle**: `WAITING -> READY -> COUNTDOWN (3, 2, 1, ENGAGE) -> ACTIVE (90s) -> COMPLETED`.
2. **Deterministic PRNG (Mulberry32)**: Both pilots encounter bitwise identical enemy wave spawns, archetype distributions, and chaos anomalies generated from the authoritative `match_seed`.
3. **High-Frequency Telemetry Broadcast (~10 Hz)**: Streams live player vitals (health, shield, lives, score, combo, overheat state) over Supabase Realtime Phoenix WebSocket.
4. **Competitive Cyber HUD**: Dual vital pods, authoritative 90-second countdown clock (turning pulsing crimson in final 10s), live score margin (`▲ LEADING`, `▼ TRAILING`, `● TIED`), and dynamic Hinglish commentary banners on lead changes.
5. **Cross-Platform Permutations (4/4 Supported)**:
   - Browser Cadet vs Browser Cadet
   - Java Desktop Cadet vs Java Desktop Cadet
   - Java Desktop Cadet vs Browser Cadet
   - Browser Cadet vs Java Desktop Cadet
6. **Competitive Integrity & Provisional Rankings**:
   - Scores are submitted via atomic PostgreSQL RPCs (`finalize_multiplayer_match`) with server timestamp verification.
   - In accordance with academic honesty, peer-reported client telemetry is classified as **PROVISIONAL** (unverified peer telemetry without authoritative headless physics simulation).
   - Provisional MMR is tracked separately (`+25` Win, `0` Loss, `+10` Draw) without corrupting official verified rankings.

### How to Launch Multiplayer:
- **Browser Client**:
  ```bash
  cd web
  npm install
  npm run dev
  # Navigate to http://localhost:3000 -> Click "⚔ ONLINE 1v1 BATTLE"
  ```
- **Java Desktop Client**:
  ```cmd
  scripts\run_java.bat
  # In Main Menu -> Click "⚔ ONLINE 1v1 BATTLE (MULTIPLAYER)"
  ```

---

## 5. System Requirements & Prerequisites

- **Operating System**: Windows 10 or Windows 11 (64-bit or 32-bit).
- **Java**: Java Development Kit (JDK) 11 or higher (verified on JDK 26.0.2).
- **C/C++ & Assembly**: MinGW GCC 6.3.0 or higher with 32-bit compilation support (`-m32`).
- **Graphics**: OpenGL 1.4+ compatible graphics card + `freeglut` / `glut32.dll`.

Ensure tools are present in your `PATH`:
```cmd
where javac java gcc g++
```

---

## 5. Quick Start: Master Unified Launcher

Launch the all-in-one terminal dashboard:

```cmd
airstrike.bat
```

*(Or in PowerShell: `.\airstrike.ps1`)*

```
==============================================================================
                     AIRSTRIKE - UNIFIED MASTER LAUNCHER
                 Academic Demonstration & Verification System
==============================================================================
   Four Integrated Subjects - 19 Course Outcomes - 20 Academic Enemy Types
==============================================================================
  [1]  Play AIRSTRIKE - Java Desktop (Swing / AWT Space Combat Game)
  [2]  Launch AIRSTRIKE - Browser (HTML5 Canvas + TypeScript Web Client)
  [3]  PSOOP Enemy Demonstrations (Java Space Shooter & Enemy Fleet)
  [4]  PL Enemy Demonstrations (C++17 Data Structures & Tactical Fleet)
  [5]  COA Enemy Demonstrations (32-bit Intel 80386 Assembly Suite)
  [6]  CGL Enemy Demonstrations (OpenGL / GLUT Graphics - Screen 7)
  [7]  Build All Modules (build_all.bat)
  [8]  Run All Tests (test_all.bat - 418 Assertions)
  [9]  View 20-Enemy Academic Roster (CO1 to CO19 Full Mapping)
  [10] Exit Application
==============================================================================
```

---

## 6. Build & Test Commands

### Master Build (All 4 Modules)
```cmd
build_all.bat
```

### Master Test Runner (All 19 Course Outcomes & 20 Enemies)
```cmd
test_all.bat
```

### Automated Test Matrix:
```
==============================================================================
                     MASTER ACADEMIC TEST VERIFICATION MATRIX
==============================================================================
  Module   Subject                           Assertions   Status
  ----------------------------------------------------------------------------
  PSOOP    Java Swing and Supabase Backend       169       PASSED
  PL       C++17 Data Structures and Algos        99       PASSED
  COA      80386 32-bit Assembly and Booth        57       PASSED
  CGL      OpenGL Raster, Viewing and Lighting    93       PASSED
  ----------------------------------------------------------------------------
  TOTAL    All 19 Course Outcomes (COs)          418       100% PASS
==============================================================================
```

### Individual Module Commands:
- **PSOOP (Java)**:
  - Build: `scripts\build_java.bat`
  - Tests: `scripts\test_psoop.bat`
  - Run Game: `java -cp bin\java main.Main`
- **PL (C++17)**:
  - Build: `scripts\build_pl.bat`
  - Tests: `bin\pl\pl_test_runner.exe --test`
  - Run CLI: `bin\pl\pl_test_runner.exe`
- **COA (80386 Assembly)**:
  - Build: `scripts\build_coa.bat`
  - Tests: `bin\coa\coa_runner.exe --test`
  - Run CLI: `bin\coa\coa_runner.exe`
- **CGL (OpenGL)**:
  - Build: `scripts\build_cgl.bat`
  - Tests: `bin\cgl\cgl_runner.exe --test`
  - Run GUI: `bin\cgl\cgl_runner.exe`

---

## 7. Supabase Cloud Configuration

To connect online pilot authentication, match history, and the global leaderboard:

1. Create a `supabase.properties` file in the project root:
   ```properties
   supabase.url=https://your-project-id.supabase.co
   supabase.key=your-anon-publishable-key-here
   supabase.timeout_ms=8000
   ```
2. **Security Guarantee**:
   - Only use the **publishable / anonymous key**.
   - Administrative `service_role` keys are strictly prohibited and rejected by `SupabaseConfig.java`.
   - `supabase.properties` is protected by `.gitignore` and never committed to git.
   - If offline or unconfigured, AIRSTRIKE seamlessly operates in local **Guest Pilot** mode.

---

## 8. Repository Structure

```
AIRSTRIKE/
├── bin/                          # Output binaries for all 4 modules
├── CGL/                          # Computer Graphics Lab (C++17 / OpenGL)
│   ├── assets/textures/          # Local 24-bit BMP texture files
│   ├── src/                      # Primitives, transforms, raster, viewing, clipping, appearance
│   └── tests/                    # 65 automated graphics unit tests
├── COA/                          # Computer Organization & Architecture (80386 ASM)
│   ├── src/                      # co1_architecture, co2_programs, co3_arithmetic, co4_pipeline
│   └── tests/                    # 41 automated assembly tests
├── PL/                           # Programming Laboratory (C++17)
│   ├── src/                      # arrays, linked_lists, stacks, queues, searching, sorting, hashing
│   └── tests/                    # 53 automated data structure tests
├── PSOOP/                        # Principles of OOP (Java 26 / Swing / AWT)
│   ├── src/                      # model, game, ui, service, database, exception, test
│   └── resources/                # Procedural visual & sound parameters
├── docs/                         # Comprehensive academic documentation
│   ├── screenshots/FINAL/        # Visual demonstration evidence
│   ├── SUPABASE/                 # Cloud database and security audits
│   ├── FINAL_PROJECT_AUDIT.md    # Comprehensive technical audit
│   ├── CO_VERIFICATION_MATRIX.md # Detailed 19 CO verification matrix
│   ├── PROJECT_ARCHITECTURE.md   # System architectural blueprint
│   ├── BUILD_AND_RUN.md          # Multi-module build and execution guide
│   └── FINAL_DEMONSTRATION_GUIDE.md # Viva presentation walkthrough & Q&A
├── scripts/                      # Build and test automation scripts
├── airstrike.bat / .ps1          # Unified Master Launcher
├── build_all.bat / .ps1          # Master compiler script
└── test_all.bat / .ps1           # Master automated test suite (418 assertions)
```

---

## 9. Verification Artifacts & Evidence

High-resolution visual evidence for presentation and evaluation is cataloged under [`docs/screenshots/FINAL/`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL):
- `launcher_menu.png`: Unified Master Launcher terminal dashboard
- `main_menu.png`: Java Swing combat command menu
- `active_gameplay.png`: Active 2D space shooter combat scene
- `boss_battle.png`: Boss encounter with phased attacks
- `game_over_screen.png`: Match summary and cloud transmission status
- `auth_dialog.png`: Starfleet Pilot Registry (Login & Registration)
- `match_history_dialog.png`: Pilot flight log archives retrieved from Supabase
- `leaderboard_dialog.png`: Global Hall of Fame leaderboard with Gold/Silver/Bronze medals
- `pl_terminal_demo.png`: C++17 data structures test verification
- `coa_terminal_demo.png`: 80386 assembly test verification
- `cgl_co1_primitives.bmp` through `cgl_integrated_scene.bmp`: OpenGL graphics demonstrations
- `unified_test_matrix.png`: Master test results summary (302/302 passed)
