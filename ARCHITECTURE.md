# AIRSTRIKE: System Architecture Specification

**Project**: AIRSTRIKE – 2D Space Shooter and Academic Demonstration System  
**Document**: Architectural Blueprint  
**Status**: Phase 1 Foundation  

---

## 1. Architectural Philosophy & Subject Independence

The AIRSTRIKE project is designed as an integrated multi-subject academic software engineering system. It demonstrates practical implementations of core computer science subjects through a common creative theme—a high-performance 2D Space Shooter game.

### Strict Independence Rule
> [!IMPORTANT]
> **Java and C++ operate completely independently.**
> There is **NO JNI (Java Native Interface)**, **NO TCP/UDP sockets**, **NO shared memory IPC**, and **NO pipe-based inter-process communication** between the Java and C++ modules.
> Each subject module is designed as an autonomous, self-contained subsystem with its own compilers, toolchains, build targets, test runners, and entry points.

This strict separation guarantees that:
1. An evaluator or professor reviewing the **Programming Laboratory (PL)** module can build and run C++ data structures in isolation without requiring Java.
2. A reviewer evaluating **Computer Organization and Architecture (COA)** can inspect and execute 32-bit 80386 assembly programs directly without C++ or Java runtime dependencies.
3. The **Computer Graphics (CGL)** module can demonstrate OpenGL/GLUT transformations and raster algorithms as dedicated native C++ executables.
4. The **Java OOP (PSOOP)** module operates as a standalone Swing/AWT desktop application representing the game's high-level interactive interface and gameplay engine.

---

## 2. High-Level Modular Overview

```
                               +-------------------------------------+
                               |           AIRSTRIKE SYSTEM          |
                               +-------------------------------------+
                                                  |
           +--------------------+-----------------+--------------------+--------------------+
           |                    |                                      |                    |
           v                    v                                      v                    v
+--------------------+ +--------------------+                +--------------------+ +--------------------+
|       PSOOP        | |         PL         |                |        COA         | |        CGL         |
|   Java + Swing     | |    C++17 Core      |                |   80386 Assembly   | |   OpenGL + GLUT    |
+--------------------+ +--------------------+                +--------------------+ +--------------------+
| - OOP Architecture | | - Custom Arrays    |                | - 32-bit Registers | | - Graphics Prims   |
| - Design Patterns  | | - Linked Lists     |                | - Control Flow     | | - Transformations  |
| - Robust Exception | | - Stacks & Queues  |                | - Booth Multiply   | | - Rasterization    |
| - 60 FPS Loop      | | - Search & Sort    |                | - Pipeline Hazard  | | - Viewing & Camera |
| - Swing GUI / EDT  | | - Custom Hash Tbl  |                |   Simulation       | | - Clipping & Phong |
+--------------------+ +--------------------+                +--------------------+ +--------------------+
           |                    |                                      |                    |
           | (Standalone)       | (Standalone)                         | (Standalone)       | (Standalone)
           v                    v                                      v                    v
       [JVM 26]             [MinGW g++]                            [MinGW as]           [MinGW g++ + GL]
```

---

## 3. Detailed Subject Module Responsibilities

### 3.1. PSOOP (Java + Swing + AWT)
- **Role**: High-level game client, interactive user interface, entity lifecycle management, and demonstration of Object-Oriented Programming principles.
- **Key Responsibilities**:
  - Encapsulate all combat entities (`PlayerShip`, `EnemyShip`, `BossShip`, `LaserProjectile`, `Asteroid`) in a well-structured inheritance hierarchy rooted at abstract class `Entity`.
  - Implement design patterns to minimize system complexity:
    - **Singleton**: Central `GameEngine` instance and resource coordinates.
    - **Factory**: `EnemyFactory` generating enemy variations dynamically based on difficulty.
    - **Observer**: `ScoreObserver` updating HUD statistics without coupling UI to physics.
    - **State**: `GameStateManager` controlling menu navigation, active gameplay, pauses, and game over states.
  - Implement critical-thinking exception handling with custom domain exceptions (`ResourceNotFoundException`, `CorruptedSaveException`) and defensive resource management (`try-with-resources`).
  - Maintain a rock-solid, fixed-timestep game loop (60 updates per second) with smooth interpolation on the Swing Event Dispatch Thread (EDT).

### 3.2. PL (Programming Laboratory – Standard C++17)
- **Role**: Pure data structures and algorithms foundation. Demonstrates manual memory management, pointer manipulation, and computational complexity without relying on STL shortcuts.
- **Key Responsibilities**:
  - Implement custom dynamic memory array `CustomArray<T>` with amortized geometric resizing.
  - Implement custom `DoublyLinkedList<T>` with $O(1)$ node insertion and deletion for particle simulation.
  - Implement LIFO `ActionStack<T>` for undo/redo game actions and FIFO `EventQueue<T>` for input buffering.
  - Implement sorting algorithms (`QuickSort`, `MergeSort`, `InsertionSort`) and searching algorithms (`BinarySearch`, `LinearSearch`) on player leaderboards.
  - Implement custom hash table `HashTable<K, V>` with open addressing (linear probing) and separate chaining collision resolution.
  - Zero reliance on `std::vector`, `std::list`, `std::stack`, `std::queue`, or `std::unordered_map` for core CO demonstrations.

### 3.3. COA (Computer Organization and Architecture – 80386 Assembly)
- **Role**: Low-level machine architecture exploration, hardware register manipulation, computer arithmetic, and CPU pipeline analysis.
- **Key Responsibilities**:
  - Native 32-bit 80386 assembly programs operating in protected mode using flat memory addressing.
  - Direct manipulation of general-purpose 32-bit registers (`EAX`, `EBX`, `ECX`, `EDX`, `ESI`, `EDI`, `ESP`, `EBP`) and status flags (`CF`, `ZF`, `SF`, `OF`).
  - Bitwise masking and status register manipulation for space combat systems (shields, powerups, damage states).
  - Procedural calling conventions (cdecl) managing stack frames, local variables, and return values.
  - Full implementation of Booth's Multiplication Algorithm for signed two's complement binary numbers with step-by-step register tracing.
  - Instruction pipelining hazard detection and control unit signal generation modeling the 80386 execution stages.

### 3.4. CGL (Computer Graphics Laboratory – C++ with OpenGL & GLUT)
- **Role**: Mathematical computer graphics, 2D/3D visual pipeline, coordinate transformations, and rasterization algorithms.
- **Key Responsibilities**:
  - OpenGL geometric primitives (`GL_POINTS`, `GL_LINES`, `GL_TRIANGLES`, `GL_QUADS`, `GL_TRIANGLE_FAN`) rendering vector space combat assets.
  - Matrix stack transformations (`glPushMatrix`, `glPopMatrix`, `glTranslatef`, `glRotatef`, `glScalef`) demonstrating 2D/3D affine coordinate manipulations and pivot rotations.
  - Software rasterization algorithms implemented at the pixel level:
    - Line drawing: Bresenham's Integer Line Algorithm across all octants.
    - Circle drawing: Midpoint Circle Algorithm utilizing 8-way symmetry.
    - Polygon filling: Scanline Polygon Fill with Edge Tables.
  - Dual camera projection system: Orthographic 2D tactical view (`glOrtho`) and 3D perspective chase view (`gluPerspective`).
  - Clipping algorithms: Cohen-Sutherland outcode line clipping and Sutherland-Hodgman polygon clipping against screen boundaries.
  - Lighting, color, and texture mapping: Phong reflection model (ambient, diffuse, specular) and 2D procedural texture coordinate mapping.

---

## 4. Main Integration Launcher Architecture

While Java and C++ run independently at runtime, the project provides a unified **Master Integration Launcher** in `integration/launcher/` and root execution scripts (`run_airstrike.bat`, `run_airstrike.sh`).

### Launcher Mechanism
1. **Interactive CLI / Shell Menu**:
   - The launcher presents a clean, user-friendly terminal interface.
   - Users can choose to launch:
     1. Complete Java Game Client (PSOOP Swing application)
     2. PL Data Structures Demonstration Suite (C++ native CLI runner)
     3. COA 80386 Assembly Demonstrations (32-bit native binary)
     4. CGL OpenGL Visual Graphics Demonstrations (OpenGL/GLUT window)
     5. Complete Test & Verification Runner across all modules
2. **Process Invocation**:
   - The launcher invokes the respective compiled binary located in `bin/`:
     - `bin/java/main.Main` (via `java`)
     - `bin/pl/pl_test_runner.exe`
     - `bin/coa/coa_arithmetic.exe`
     - `bin/cgl/cgl_demo.exe`
   - Each process runs natively in its own thread or console window without any cross-language runtime bridges.

---

## 5. Future Database Architecture (Supabase + PostgreSQL)

In future project phases, AIRSTRIKE will support cloud synchronization and centralized telemetry:

```
+-------------------------------------------------------------------------+
|                              LOCAL CLIENTS                              |
|                                                                         |
|  +--------------------+                     +--------------------+      |
|  |     Java Client    |                     |   C++ PL Runner    |      |
|  |   (Player Scores)  |                     |  (Leaderboard Data)|      |
|  +--------------------+                     +--------------------+      |
|            |                                           |                |
|    HTTPS / REST API                             HTTPS / REST API        |
|    (PostgREST / JSON)                           (PostgREST / JSON)      |
+------------|-------------------------------------------|----------------+
             |                                           |
             +---------------------+---------------------+
                                   |
                                   v
             +-------------------------------------------+
             |             SUPABASE BACKEND              |
             |                                           |
             |   +-----------------------------------+   |
             |   |        PostgREST Gateway          |   |
             |   +-----------------------------------+   |
             |                     |                     |
             |   +-----------------------------------+   |
             |   |      PostgreSQL Database Engine   |   |
             |   |  - high_scores table              |   |
             |   |  - player_telemetry table         |   |
             |   |  - audit_log table                |   |
             |   +-----------------------------------+   |
             +-------------------------------------------+
```

### Architectural Principles for Database Integration:
1. **Direct Database Decoupling**:
   - The database layer will be introduced without adding heavy dependencies to core computational units.
   - Database operations will run asynchronously to prevent blocking the real-time 60 FPS game loop.
2. **PostgreSQL Relational Schema**:
   - Tables for `players`, `high_scores`, `combat_sessions`, and `academic_verifications`.
   - Relational constraints, foreign keys, timestamps, and row-level security (RLS).
3. **RESTful PostgREST Protocol**:
   - Standard HTTP JSON endpoints provided natively by Supabase, enabling lightweight integration from Java HTTP client without heavy driver bloat.

---

## 6. Directory Layout & Binary Artifact Flow

```
AIRSTRIKE/
├── PSOOP/src/           -->  [javac]        -->  bin/java/
├── PL/src/ & tests/     -->  [g++ C++17]    -->  bin/pl/pl_test_runner.exe
├── COA/                 -->  [as / gcc]     -->  bin/coa/coa_arithmetic.exe
├── CGL/src/             -->  [g++ + GLUT]   -->  bin/cgl/cgl_demo.exe
├── integration/         -->  Launcher scripts coordinating binary execution
├── docs/                -->  Academic reports, architecture specs, viva guides
└── bin/                 -->  Target directory for all compiled executables
```

This clean architecture satisfies all educational requirements, ensures maximum stability during viva demonstrations, and guarantees that each subject can be reviewed and tested independently.

