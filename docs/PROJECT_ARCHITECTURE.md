# AIRSTRIKE: Comprehensive Project Architecture Specification

**Project**: AIRSTRIKE – 2D Space Shooter & Academic Demonstration System  
**Document**: Architectural Blueprint (Phase 6.3 Final Integration Edition)  
**Target Audience**: Academic Reviewers, Evaluators, Engineering Faculty  
**Status**: Production / Presentation-Ready  

---

## 1. Architectural Philosophy & Principle of Autonomy

AIRSTRIKE was engineered as an integrated multi-subject computer science academic system. Rather than implementing isolated, trivial toy programs for each subject, AIRSTRIKE connects all four foundational subjects to a cohesive, high-performance space shooter theme.

### The Autonomy Rule
> [!IMPORTANT]
> **Strict Independent Execution**:
> There is **no JNI (Java Native Interface)**, **no TCP/UDP socket bridging**, and **no shared-memory IPC** between Java and C++.
> Each subject module is completely autonomous, compiled by its own verified toolchain, and executable independently.

This design guarantees that:
1. An evaluator reviewing **Programming Laboratory (PL)** can compile and inspect standard C++17 data structures without requiring a Java Virtual Machine.
2. A reviewer evaluating **Computer Organization and Architecture (COA)** can inspect and execute 32-bit Intel 80386 assembly programs directly on hardware without C++ or Java runtime dependencies.
3. The **Computer Graphics Laboratory (CGL)** module runs native hardware-accelerated OpenGL/GLUT demonstrations directly on the GPU/display server.
4. The **Object-Oriented Programming (PSOOP)** module runs as a pure Java SE application communicating with the **Supabase PostgreSQL** cloud backend.

---

## 2. Global System Architecture Diagram

```mermaid
graph TD
    subgraph Master Launcher
        ML["airstrike.bat / airstrike.ps1<br/>Master Project Launcher & Diagnostics"]
    end

    subgraph Module 1: PSOOP (Java 26 / Swing / AWT)
        J_UI["Swing Desktop UI<br/>(MainWindow, MenuPanel, HUD, Dialogs)"]
        J_ENG["GameEngine & 60 FPS Loop<br/>(Entities, Collisions, WaveManager)"]
        J_AUTH["AuthService & MatchService<br/>(java.net.http.HttpClient + SimpleJson)"]
        J_UI --> J_ENG
        J_ENG --> J_AUTH
    end

    subgraph Module 2: PL (C++17 Data Structures)
        PL_CLI["Interactive CLI Runner<br/>(main.cpp / pl_test_runner.exe)"]
        PL_DS["Custom Data Structures<br/>(DynamicArray, BulletPool, LinkedLists,<br/>Stacks, Queues, Sorts, HashTables)"]
        PL_CLI --> PL_DS
    end

    subgraph Module 3: COA (32-bit 80386 Assembly)
        COA_CLI["Master Assembly Dispatcher<br/>(main.s / coa_runner.exe)"]
        COA_CORE["80386 Low-Level Routines<br/>(GPR & Flags, Booth Multiply,<br/>Restoring Division, 5-Stage Pipeline Model)"]
        COA_CLI --> COA_CORE
    end

    subgraph Module 4: CGL (C++17 + OpenGL + GLUT)
        CGL_WIN["GLUT Window & Event Loop<br/>(cgl_runner.exe)"]
        CGL_GFX["Graphics Algorithms & Math<br/>(8 Primitives, Affine Transforms, Bresenham,<br/>Circles, Projections, Clipping, Phong & BMP)"]
        CGL_WIN --> CGL_GFX
    end

    subgraph Cloud Backend (Supabase + PostgreSQL)
        SB_AUTH["GoTrue Auth (/auth/v1/)<br/>(Email/Password Sign-up & Login)"]
        SB_REST["PostgREST API (/rest/v1/)<br/>(public.matches, public.profiles)"]
        SB_DB["PostgreSQL Database Engine<br/>(Row Level Security & Leaderboard View)"]
        SB_REST --> SB_DB
        SB_AUTH --> SB_DB
    end

    ML -->|Option 1| J_UI
    ML -->|Option 2| PL_CLI
    ML -->|Option 3| COA_CLI
    ML -->|Option 4| CGL_WIN
    J_AUTH -->|HTTPS / TLS 1.3| SB_AUTH
    J_AUTH -->|HTTPS / TLS 1.3| SB_REST
```

---

## 3. Subsystem Architecture Specifications

### 3.1 Principles of Object-Oriented Programming (PSOOP)
* **Runtime**: Java SE 11+ (verified on Java 26.0.2).
* **GUI Engine**: Swing / AWT operating on the Event Dispatch Thread (EDT).
* **Architectural Highlights**:
  - **Inheritance Hierarchy**: Abstract `Entity` base class extended by `Player` and `Enemy` (`DroneEnemy`, `FighterEnemy`, `BossEnemy`).
  - **Complexity Reduction**: Subsystem services (`CollisionManager`, `ScoreCalculator`, `WaveManager`, `AssetManager`) completely decouple physics, scoring, spawning, and starfield rendering from the main canvas.
  - **Thread Synchronization**: Dedicated background `GameLoop` timer thread maintaining fixed 60 FPS physics updates, safely synchronizing canvas redraws via `SwingUtilities.invokeLater()`.
  - **State Machine**: Clean transitions across `MENU`, `PLAYING`, `PAUSED`, and `GAME_OVER` with validation against illegal paths via `InvalidGameStateException`.

### 3.2 Programming Laboratory (PL)
* **Language Standard**: ISO C++17 (MinGW GCC 6.3.0).
* **Memory Management**: 100% manual dynamic allocation (`new[]`, `delete[]`, raw pointer manipulation).
* **Architectural Highlights**:
  - **Zero STL Container Shortcuts**: Custom implementations of dynamic arrays, linked lists, stacks, queues, and hash tables.
  - **Rule of Five**: Explicit copy constructors, copy assignment operators, move constructors, move assignment operators, and destructors preventing memory leaks.
  - **Bullet Pool Optimization**: Contiguous memory pool eliminating heap fragmentation during intense space combat firing sequences.
  - **Spatial Collision Grid**: $O(1)$ cell hashing reducing broadphase collision queries from $O(N^2)$ to $O(N \cdot k)$.

### 3.3 Computer Organization and Architecture (COA)
* **Architecture**: Intel 80386 Protected Mode (32-bit x86).
* **Assembler & Linker**: GNU Assembler (GAS) using Intel Syntax (`.intel_syntax noprefix`) compiled with MinGW `gcc -m32`.
* **Architectural Highlights**:
  - **Native Hardware Execution**: All instructions are genuine 32-bit 80386 opcodes (`EAX`, `EBX`, `ECX`, `EDX`, `ESI`, `EDI`, `EBP`, `ESP`, `EFLAGS`). No 64-bit register substitution.
  - **Calling Conventions**: Adheres strictly to standard 32-bit `cdecl` calling convention (arguments pushed right-to-left, caller cleans up stack, return value in `EAX`).
  - **Arithmetic Rigor**: Full Booth's Multiplication Algorithm for signed two's complement integers and Restoring Division Algorithm for unsigned integers with complete register traces.
  - **Pipeline Hazard Simulation**: Educational five-stage pipeline model (IF, ID, EX, MEM, WB) evaluating RAW data hazards and branch penalty flushes.

### 3.4 Computer Graphics Laboratory (CGL)
* **Graphics Stack**: OpenGL 1.x/2.x compatibility context with GLUT and GLU.
* **Architectural Highlights**:
  - **Direct GPU Rendering**: Uses native OpenGL primitives (`GL_POINTS` through `GL_QUADS`) and matrix stack manipulation (`glPushMatrix`, `glPopMatrix`).
  - **Software Pixel Rasterization**: Pixel-level implementations of Bresenham's Line Algorithm (across all 8 octants), Midpoint Circle Algorithm (8-way symmetry), Midpoint Ellipse Algorithm, and Scanline Polygon Fill.
  - **Dual Camera Projection**: Dynamic switching between 2D orthographic tactical radar view (`glOrtho`) and 3D perspective chase view (`gluPerspective`).
  - **Local Texture Pipeline**: Custom uncompressed 24-bit RGB BMP loader (`image_loader.cpp`) streaming bitmap data from disk into GPU texture memory with bilinear filtering and wrapping.
  - **Phong Illumination**: Ambient, Lambertian diffuse, and specular reflection highlights calculated per vertex/fragment.

---

## 4. Cloud Database & Security Architecture

* **Engine**: Supabase PostgreSQL with PostgREST Gateway.
* **Network Protocol**: Secure HTTPS (TLS 1.3) via Java `java.net.http.HttpClient`.
* **Security Constraints**:
  - Client utilizes **publishable / anonymous API key only**.
  - `SupabaseConfig.java` actively rejects administrative `service_role` keys via `SecurityException`.
  - Credentials reside in external, git-ignored `supabase.properties`.
  - Raw tokens, passwords, and API keys are redacted from logs and `toString()` outputs.
  - Database Row-Level Security (RLS) ensures pilots only query and insert their own records (`auth.uid() = user_id`).
  - Transparent fallback to local Guest Pilot mode during network disconnections.

---

## 5. Unified Directory Layout

```
AIRSTRIKE/
├── bin/                          # Output binaries for all 4 modules
│   ├── java/                     # Compiled .class files for PSOOP
│   ├── pl/                       # pl_test_runner.exe for PL
│   ├── coa/                      # coa_runner.exe for COA
│   └── cgl/                      # cgl_runner.exe for CGL
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
│   ├── src/
│   │   ├── model/                # Entity, Player, Enemy, Projectile, PowerUp
│   │   ├── game/                 # GameEngine, GameLoop, GameCanvas
│   │   ├── ui/                   # MainWindow, MenuPanel, Dialogs
│   │   ├── service/              # CollisionManager, WaveManager, AuthService, MatchService
│   │   ├── database/             # SupabaseConfig, SimpleJson, SessionManager
│   │   └── test/                 # 143 automated Java unit and integration tests
├── docs/                         # Comprehensive academic documentation and reports
│   ├── screenshots/FINAL/        # High-resolution visual demonstration evidence
│   └── SUPABASE/                 # Cloud database and security audits
├── scripts/                      # Autonomous build and test batch/PowerShell scripts
├── airstrike.bat / .ps1          # Master Unified Project Launcher
├── build_all.bat / .ps1          # Master multi-module compiler script
└── test_all.bat / .ps1           # Master automated test harness across all 19 COs
```
