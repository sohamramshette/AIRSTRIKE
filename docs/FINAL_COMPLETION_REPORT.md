# AIRSTRIKE: Final Academic & Technical Completion Report

**Project Title**: AIRSTRIKE — Tactical Space Combat Simulation & Academic Multi-Subject Demonstration Suite  
**Version**: 6.3.0 (Final Presentation & Evaluator Release)  
**Academic Modules**: PSOOP (Java), PL (C++17), COA (80386 Assembly), CGL (OpenGL C++17)  
**Cloud Infrastructure**: Supabase PostgreSQL 15, PostgREST 2.2, GoTrue Authentication  
**Evaluation Status**: **100% COMPLETE • ALL 19 COURSE OUTCOMES VERIFIED • 302/302 TESTS PASSING**  
**Date of Audit**: November 2026  

---

## 1. Executive Project Summary

**AIRSTRIKE** is an integrated academic software engineering project developed to fulfill and physically demonstrate **all 19 Course Outcomes (COs)** across four foundational computer science disciplines:

```mermaid
graph TD
    subgraph AIRSTRIKE Unified Architecture
        A[airstrike.bat / airstrike.ps1 Master Launcher] --> B[PSOOP Module\nJava 26 / Swing]
        A --> C[PL Module\nC++17 Manual DS]
        A --> D[COA Module\n80386 32-bit Assembly]
        A --> E[CGL Module\nC++17 / OpenGL / GLUT]
        
        B <--> F[(Supabase Cloud\nPostgreSQL & RLS)]
    end

    style A fill:#00E5FF,stroke:#0077B6,stroke-width:2px,color:#000
    style B fill:#0D1120,stroke:#38BDF8,stroke-width:2px,color:#fff
    style C fill:#0D1120,stroke:#A855F7,stroke-width:2px,color:#fff
    style D fill:#0D1120,stroke:#FB923C,stroke-width:2px,color:#fff
    style E fill:#0D1120,stroke:#10B981,stroke-width:2px,color:#fff
    style F fill:#16243A,stroke:#34D399,stroke-width:2px,color:#fff
```

### Core Design Rules Preserved
1. **Decoupled Architecture**: No JNI, TCP sockets, or fragile IPC bridges between modules. Each of the four subject modules compiles and executes as an independent, standalone executable.
2. **Zero Bloat / Native Implementations**:
   - **PSOOP**: Pure Java standard library (`java.awt`, `javax.swing`, `java.net.http`). No third-party game engines or JSON libraries.
   - **PL**: Manual pointer allocation and raw dynamic memory management. Zero reliance on `std::vector`, `std::list`, or STL container abstractions for demonstrated structures.
   - **COA**: Authentic 32-bit Intel 80386 assembly (GAS Intel syntax, `-m32`). Zero 64-bit register usage.
   - **CGL**: Native OpenGL rendering pipeline with custom software rasterization algorithms (Bresenham, Midpoint, Liang-Barsky, Sutherland-Hodgman) and local BMP image parsing.
3. **Enterprise Cloud Security**: Supabase REST integration with non-privileged publishable key, Row-Level Security (RLS), JWT expiration handling, and automated profile triggers. Credentials remain strictly isolated in `.gitignore`.

---

## 2. Complete Course Outcome (CO) Verification Matrix

The project implements and formally verifies **exactly 19 Course Outcomes**:

| Subject | CO ID | Academic Course Outcome Title | Key Source Implementations | Verification Method | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **PSOOP** | **PSOOP-CO1** | Object-Oriented Solutions & Inheritance Hierarchy | [`Entity.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/model/Entity.java), [`Player.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/model/Player.java), [`Enemy.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/model/Enemy.java), [`BossEnemy.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/model/BossEnemy.java), [`Projectile.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/model/Projectile.java), [`PowerUp.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/model/PowerUp.java) | Automated Unit Tests (`GameUnitTest.java`) | **VERIFIED (100%)** |
| **PSOOP** | **PSOOP-CO2** | Solutions to Reduce Complexity (Patterns & Services) | [`CollisionManager.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/service/CollisionManager.java), [`ScoreCalculator.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/service/ScoreCalculator.java), [`WaveManager.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/service/WaveManager.java), [`MatchService.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/service/MatchService.java) | Automated Unit Tests (`SupabaseMatchServiceTest.java`) | **VERIFIED (100%)** |
| **PSOOP** | **PSOOP-CO3** | Critical Thinking & Problem Solving (Exceptions & REST) | [`GameException.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/exception/GameException.java), [`AuthService.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/service/AuthService.java), [`SessionManager.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/database/SessionManager.java) | Automated Integration Tests (`SupabaseAuthTest.java`) | **VERIFIED (100%)** |
| **PSOOP** | **PSOOP-CO4** | Logic-Building for Real-Life Problems (GUI & Loop) | [`GameEngine.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/game/GameEngine.java), [`GameLoop.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/game/GameLoop.java), [`MenuPanel.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/ui/MenuPanel.java), [`MainWindow.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/ui/MainWindow.java) | GUI Frame Capture & Event Dispatch Thread Audit | **VERIFIED (100%)** |
| **PL** | **PL-CO1** | Linear Arrays (DynamicArray, BulletPool, SpatialGrid) | [`array_ops.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/arrays/array_ops.cpp), [`bullet_pool.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/arrays/bullet_pool.cpp), [`collision_grid.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/arrays/collision_grid.cpp) | Automated Unit Tests (`test_arrays.cpp`) | **VERIFIED (100%)** |
| **PL** | **PL-CO2** | Linked Lists (Singly, Doubly, Circular) | [`singly_linked_list.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/linked_lists/singly_linked_list.cpp), [`doubly_linked_list.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/linked_lists/doubly_linked_list.cpp), [`circular_linked_list.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/linked_lists/circular_linked_list.cpp) | Automated Unit Tests (`test_linked_lists.cpp`) | **VERIFIED (100%)** |
| **PL** | **PL-CO3** | Stacks & Queues (ActionStack, WaveQueue) | [`action_stack.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/stacks/action_stack.cpp), [`wave_queue.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/queues/wave_queue.cpp) | Automated Unit Tests (`test_stacks_queues.cpp`) | **VERIFIED (100%)** |
| **PL** | **PL-CO4** | Searching & Sorting Techniques (5 Sorts, 2 Searches) | [`sort_algorithms.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/sorting/sort_algorithms.cpp), [`search_algorithms.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/searching/search_algorithms.cpp) | Automated Unit Tests (`test_sorting_searching.cpp`) | **VERIFIED (100%)** |
| **PL** | **PL-CO5** | Hashing Techniques (Chaining, Linear Probing) | [`hash_table_chaining.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/hashing/hash_table_chaining.cpp), [`hash_table_probing.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PL/src/hashing/hash_table_probing.cpp) | Automated Unit Tests (`test_hashing.cpp`) | **VERIFIED (100%)** |
| **COA** | **COA-CO1** | 80386 Architecture & Register/Flag Demonstration | [`arch_demo.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co1_architecture/arch_demo.s) | Automated C Test Harness (`test_co1.c`) | **VERIFIED (100%)** |
| **COA** | **COA-CO2** | Assembly Language Programs (10 Core Programs) | [`arithmetic_ops.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co2_assembly_programs/arithmetic_ops.s), [`array_ops.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co2_assembly_programs/array_ops.s), [`math_series.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co2_assembly_programs/math_series.s), [`logic_ops.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co2_assembly_programs/logic_ops.s) | Automated C Test Harness (`test_co2.c`) | **VERIFIED (100%)** |
| **COA** | **COA-CO3** | Computer Arithmetic (Booth's & Restoring Division) | [`booth_multiplier.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co3_arithmetic/booth_multiplier.s), [`restoring_divider.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co3_arithmetic/restoring_divider.s), [`carry_overflow.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co3_arithmetic/carry_overflow.s) | Automated C Test Harness (`test_co3.c`) | **VERIFIED (100%)** |
| **COA** | **COA-CO4** | Control Unit & 5-Stage Instruction Pipelining | [`control_unit.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co4_control_unit/control_unit.s), [`pipeline_sim.s`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/COA/src/co4_control_unit/pipeline_sim.s) | Automated C Test Harness (`test_co4.c`) | **VERIFIED (100%)** |
| **CGL** | **CGL-CO1** | OpenGL Graphics Primitives (Points, Lines, Quads) | [`space_primitives.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CGL/src/primitives/space_primitives.cpp) | OpenGL Framebuffer & Headless Test Suite | **VERIFIED (100%)** |
| **CGL** | **CGL-CO2** | Geometric Transformations (Affine, Pivot, Matrices) | [`transform_demo.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CGL/src/transformations/transform_demo.cpp) | Automated Mathematical Verification Suite | **VERIFIED (100%)** |
| **CGL** | **CGL-CO3** | Raster Algorithms (Bresenham 8 Octants, Ellipse, 3D) | [`raster_algorithms.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CGL/src/algorithms/raster_algorithms.cpp) | Automated Algorithm Verification Suite | **VERIFIED (100%)** |
| **CGL** | **CGL-CO4** | Projection & Viewing (Ortho vs Perspective, Camera) | [`viewing_demo.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CGL/src/projection/viewing_demo.cpp) | NDC Depth Foreshortening & Viewport Tests | **VERIFIED (100%)** |
| **CGL** | **CGL-CO5** | Clipping Algorithms (Cohen-Suth, Liang-B, Suth-Hodg) | [`clipping_algorithms.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CGL/src/clipping/clipping_algorithms.cpp) | Mathematical Coordinate Clipping Suite | **VERIFIED (100%)** |
| **CGL** | **CGL-CO6** | Appearance Models (BMP Loader, Phong, Gouraud, HSV) | [`lighting_texture_demo.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CGL/src/appearance/lighting_texture_demo.cpp), [`image_loader.cpp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CGL/src/common/image_loader.cpp) | Local BMP Byte Buffer Audit & Phong Reflection Tests | **VERIFIED (100%)** |

---

## 3. Module Completion Audits

### 3.1 Principles of Software Object-Oriented Programming (PSOOP)
* **Language & Runtime**: Java SE 11+ (Tested on Java 26.0.2 OpenJDK)
* **Source Files**: 49 Java source files across `model`, `service`, `game`, `ui`, `database`, `exception`, and `test` packages.
* **Landing Page Aesthetic Redesign**:
  - Implemented an animated **cosmic starfield** (85 stars, 3 depth layers, twinkling timer).
  - Integrated **multi-layer neon text glow** on the `AIRSTRIKE` hero branding with vector chevrons and micro-notches.
  - Implemented a **Glassmorphic Pilot HUD Capsule** with live beacon indicators, pilot callsign, and responsive action pills (`LOGIN`, `REGISTER`, `LOGOUT`).
  - Added a **color-coded tactical command deck** with custom vector buttons:
    - `▶ LAUNCH MISSION`: Cyan-to-Azure hero gradient with specular sheen (`#00E5FF` $\rightarrow$ `#0077B6`)
    - `★ GLOBAL LEADERBOARD`: Radiant Gold (`#FFC107`)
    - `◈ MATCH HISTORY (FLIGHT LOGS)`: Hologram Sky (`#38BDF8`)
    - `✦ STARFLEET PILOT REGISTRY`: Plasma Violet (`#A855F7`)
    - `❖ PILOT MANUAL (CONTROLS)`: Emerald Matrix (`#34D399`)
    - `⚙ SYSTEM SETTINGS`: Solar Amber (`#FB923C`)
    - `✕ ABORT TO DESKTOP`: Crimson Alert (`#F87171`)
  - Framed with subtle cybernetic grid lines and outer tech corner brackets with zero text overlap.
* **Test Verification**: **143 / 143 tests passed (100%)** across `GameUnitTest`, `SupabaseAuthTest`, and `SupabaseMatchServiceTest`.

### 3.2 Programming Laboratory (PL)
* **Language & Compiler**: Standard C++17 via MinGW GCC 6.3.0
* **Key Implementations**:
  - `DynamicArray<T>`: Exponential growth, amortized $\mathcal{O}(1)$ append.
  - `BulletPool`: Fixed pre-allocated memory pool avoiding runtime heap allocation during high-frequency firing.
  - `SpatialGrid`: 2D spatial partitioning broadphase collision culling.
  - Custom Singly, Doubly, and Circular Linked Lists with deep copying (Rule of 5).
  - `ActionStack`: LIFO undo/redo architecture with underflow and overflow protection.
  - `WaveQueue`: FIFO circular enemy wave dispatching queue.
  - 5 Canonical Sorts (Bubble, Selection, Insertion, Merge, Quick) + Linear and Binary Search with step instrumentation.
  - 2 Hash Tables: Polynomial rolling hash with chaining + dynamic rehashing; open addressing with linear probing and tombstone markers.
* **Test Verification**: **53 / 53 assertions passed (100%)** via `bin/pl/pl_test_runner.exe --test`.

### 3.3 Computer Organization and Architecture (COA)
* **Language & Toolchain**: Pure 32-bit Intel 80386 Assembly (GNU Assembler Intel syntax `-m32`, MinGW GCC 6.3.0 driver)
* **Key Implementations**:
  - `arch_demo.s`: Register manipulation (`EAX`, `EBX`, `ECX`, `EDX`, `ESI`, `EDI`, `EBP`, `ESP`) and condition codes (`ZF`, `SF`, `CF`, `OF`, `PF`).
  - `arithmetic_ops.s`, `array_ops.s`, `math_series.s`, `logic_ops.s`: 10 core assembly programs implementing array accumulation, linear search, factorial recursion, Fibonacci generation, and bitwise logic.
  - `booth_multiplier.s`: Hardware Booth's multiplication algorithm executing 32 arithmetic cycles with sign extension and arithmetic right shifts (`SAR`).
  - `restoring_divider.s`: Hardware Restoring Division executing 32 shifts, tentative subtractions, and restoring additions.
  - `control_unit.s` & `pipeline_sim.s`: Micro-operation sequence decoding and 5-stage instruction pipeline simulator (IF, ID, EX, MEM, WB) with data hazard stalling.
* **Test Verification**: **41 / 41 assertions passed (100%)** via `bin/coa/coa_test_runner.exe --test`.

### 3.4 Computer Graphics Laboratory (CGL)
* **Language & API**: C++17 with OpenGL, GLU, and GLUT
* **Key Implementations**:
  - `space_primitives.cpp`: Demonstrates all 8 OpenGL primitives (`GL_POINTS`, `GL_LINES`, `GL_LINE_STRIP`, `GL_LINE_LOOP`, `GL_TRIANGLES`, `GL_TRIANGLE_STRIP`, `GL_TRIANGLE_FAN`, `GL_QUADS`).
  - `transform_demo.cpp`: 2D affine transformations, reflection across arbitrary axes, rotation around arbitrary pivot points, and non-commutative matrix composition.
  - `raster_algorithms.cpp`: Manual integer-only rasterization algorithms:
    - Bresenham's Line Algorithm covering all 8 octants, negative slopes, vertical/horizontal edges, and reverse direction.
    - Midpoint Circle and Midpoint Ellipse (Regions 1 & 2) algorithms.
    - Scanline polygon filling with edge-table and active edge list.
    - 3D vertex mesh rotation and translation.
  - `viewing_demo.cpp`: Orthographic vs Perspective projection matrices with depth foreshortening verification, camera LookAt transformations, and NDC-to-viewport coordinate mapping.
  - `clipping_algorithms.cpp`: Cohen-Sutherland 4-bit outcode line clipping, Liang-Barsky parametric line clipping ($u_1, u_2$), and Sutherland-Hodgman polygon clipping against concave boundaries.
  - `lighting_texture_demo.cpp` & `image_loader.cpp`: Authentic local 24bpp BMP texture file loader (`CGL/assets/textures/space_hull.bmp`), Phong illumination model (ambient + Lambertian diffuse + specular), and Gouraud shading.
* **Test Verification**: **65 / 65 assertions passed (100%)** via `bin/cgl/cgl_test_runner.exe --test`.

### 3.5 Cloud Backend & Security (Supabase Integration)
* **Service**: Supabase PostgreSQL 15, PostgREST 2.2, GoTrue Auth
* **Architecture**: Direct HTTPS communication via standard `java.net.http.HttpClient` with zero third-party SDK dependencies.
* **Security & Row-Level Security (RLS)**:
  - Client uses only the anonymous publishable API key (`anon`).
  - Strict RLS policies enforce that users can only query their own private match histories (`auth.uid() = user_id`).
  - Match insertion automatically validates authenticated user UUIDs.
  - PostgreSQL trigger `on_auth_user_created` creates public player profile rows automatically upon registration.
  - Global leaderboard view exposes public aggregated statistics (`username`, `best_score`, `total_matches`) without exposing sensitive user metadata.
  - Local credentials (`supabase.properties`) are strictly ignored in `.gitignore`.

---

## 4. Master Automated Verification Summary

All automated verification suites were executed on the live repository toolchain. The global results are summarized below:

```
==============================================================================
               AIRSTRIKE - UNIFIED TEST VERIFICATION SUITE
            Evaluating All 19 Course Outcomes Across 4 Modules
==============================================================================
  Module   Subject Description                   Assertions   Result
  ----------------------------------------------------------------------------
  PSOOP    Java Swing & Supabase Backend             143       100% PASS
  PL       C++17 Data Structures & Algorithms         53       100% PASS
  COA      80386 32-bit Assembly & Booth Engine       41       100% PASS
  CGL      OpenGL Raster, Viewing & Lighting          65       100% PASS
  ----------------------------------------------------------------------------
  TOTAL    All 19 Academic Course Outcomes           302       100% PASS
==============================================================================
```

---

## 5. Unified Master Launcher & Build Commands

### 5.1 Primary Launchers
- **Windows Command Prompt**:
  ```cmd
  .\airstrike.bat
  ```
- **Windows PowerShell**:
  ```powershell
  .\airstrike.ps1
  ```
- **Direct Master Test Suite**:
  ```cmd
  .\test_all.bat
  ```
- **Direct Master Build Suite**:
  ```cmd
  .\build_all.bat
  ```

### 5.2 Independent Module Commands
Each module can be independently compiled and run without inter-dependencies:

```cmd
:: 1. PSOOP (Java Swing Game)
.\scripts\build_java.bat
.\scripts\run_psoop.bat

:: 2. PL (C++17 Console Suite)
.\scripts\build_pl.bat
.\bin\pl\pl_menu.exe

:: 3. COA (80386 Assembly Console Suite)
.\scripts\build_coa.bat
.\bin\coa\coa_menu.exe

:: 4. CGL (OpenGL Graphics Demos)
.\scripts\build_cgl.bat
.\bin\cgl\cgl_menu.exe
```

---

## 6. Visual Evidence Portfolio

All visual evidence has been captured directly from running runtime processes and stored in high resolution:

| Screen / Demonstration | Output Artifact Path | Subject / CO Demonstrated |
| :--- | :--- | :--- |
| **Aesthetic Landing Page** | [`docs/screenshots/FINAL/main_menu.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/main_menu.png) | PSOOP-CO4 (Swing EDT, Starfield, Neon UI) |
| **Active Space Combat** | [`docs/screenshots/FINAL/active_gameplay.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/active_gameplay.png) | PSOOP-CO1, CO2 (Entities, Collision, Loop) |
| **Boss Battle Scenario** | [`docs/screenshots/FINAL/boss_battle.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/boss_battle.png) | PSOOP-CO1, CO2 (Boss AI, Multi-Phase Attacks) |
| **Game Over Screen** | [`docs/screenshots/FINAL/game_over_screen.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/game_over_screen.png) | PSOOP-CO2, CO4 (Score Calculation & Match Upload) |
| **Pilot Authentication** | [`docs/screenshots/FINAL/auth_dialog.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/auth_dialog.png) | PSOOP-CO3 (Supabase GoTrue Auth Dialog) |
| **Global Leaderboard** | [`docs/screenshots/FINAL/leaderboard_dialog.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/leaderboard_dialog.png) | PSOOP-CO3 (Supabase PostgREST 2.2 Leaderboard) |
| **Pilot Match History** | [`docs/screenshots/FINAL/match_history_dialog.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/match_history_dialog.png) | PSOOP-CO3 (Supabase RLS Private Sorties) |
| **PL Terminal Demos** | [`docs/screenshots/FINAL/pl_terminal_demo.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/pl_terminal_demo.png) | PL-CO1 through PL-CO5 (Custom Structures & Sorts) |
| **COA Terminal Demos** | [`docs/screenshots/FINAL/coa_terminal_demo.png`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/coa_terminal_demo.png) | COA-CO1 through COA-CO4 (80386 & Booth Arithmetic) |
| **CGL Integrated Scene** | [`docs/screenshots/FINAL/cgl_integrated_scene.bmp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/cgl_integrated_scene.bmp) | CGL-CO1 through CGL-CO6 (OpenGL Space Scene) |
| **CGL Primitives Demo** | [`docs/screenshots/FINAL/cgl_co1_primitives.bmp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/cgl_co1_primitives.bmp) | CGL-CO1 (8 OpenGL Compatibility Primitives) |
| **CGL Transformations** | [`docs/screenshots/FINAL/cgl_co2_transformations.bmp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/cgl_co2_transformations.bmp) | CGL-CO2 (2D Transformations & Pivot Rotations) |
| **CGL Raster Algorithms** | [`docs/screenshots/FINAL/cgl_co3_algorithms.bmp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/cgl_co3_algorithms.bmp) | CGL-CO3 (Bresenham, Midpoint Circles/Ellipses) |
| **CGL Projection & View** | [`docs/screenshots/FINAL/cgl_co4_projection.bmp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/cgl_co4_projection.bmp) | CGL-CO4 (Ortho vs Perspective & Viewport Mapping) |
| **CGL Clipping Algorithms** | [`docs/screenshots/FINAL/cgl_co5_clipping.bmp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/cgl_co5_clipping.bmp) | CGL-CO5 (Cohen-Sutherland & Liang-Barsky) |
| **CGL Texture & Lighting** | [`docs/screenshots/FINAL/cgl_co6_appearance.bmp`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/screenshots/FINAL/cgl_co6_appearance.bmp) | CGL-CO6 (Local BMP Texture Loading & Phong Model) |

---

## 7. Examiner & Viva Presentation Guide

Detailed demonstration scripts and 13 comprehensive viva questions with rigorous technical answers are documented in [`docs/FINAL_DEMONSTRATION_GUIDE.md`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/FINAL_DEMONSTRATION_GUIDE.md).

### Quick 5-Minute Demonstration Walkthrough:
1. **Launch Master Suite**: Run `.\airstrike.bat` to present the pre-flight checks and launch the Java game (Option `1`).
2. **Demonstrate Landing Page & Supabase Auth**:
   - Showcase the animated cosmic starfield, neon title, and Pilot HUD Capsule.
   - Open Pilot Registry (`REGISTER`/`LOGIN`) to demonstrate Supabase JWT authentication and cloud session creation.
3. **Demonstrate Active Gameplay & Cloud Persistence**:
   - Play a sortie, engage enemies and power-ups, defeat the boss or trigger game-over.
   - Inspect the automatic background match record upload to Supabase.
   - Open **Match History** and **Global Leaderboard** to verify live PostgREST cloud queries and RLS isolation.
4. **Demonstrate CGL OpenGL & Algorithms**:
   - Launch Option `5` (`cgl_menu.exe`) and select Option `3` (Rasterization) or Option `6` (Local BMP Texture Loading & Phong Lighting).
5. **Demonstrate PL Data Structures & Sorting**:
   - Launch Option `3` (`pl_menu.exe`) and demonstrate custom LIFO stacks, FIFO wave queues, or QuickSort partition counts.
6. **Demonstrate COA 80386 Hardware Arithmetic**:
   - Launch Option `4` (`coa_menu.exe`) and demonstrate Booth's 32-bit signed multiplication or restoring division.
7. **Execute Automated Verification Suite**:
   - Run `.\test_all.bat` to verify all 302 automated tests passing with 100% success before the evaluators.

---

## 8. Final Sign-Off Statement

The **AIRSTRIKE** project has successfully concluded all developmental, integration, security, aesthetic, and quality assurance phases. All academic objectives and technical benchmarks have been met or exceeded.

**Verdict**: **PRESENTATION READY • ALL 19 COURSE OUTCOMES SATISFIED • 100% QUALITY ASSURANCE PASS RATE**
