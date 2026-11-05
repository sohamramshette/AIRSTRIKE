# AIRSTRIKE – 2D Space Shooter & Academic Demonstration System

![Build Status](https://img.shields.io/badge/status-phase__1__foundation-blue.svg)
![Java](https://img.shields.io/badge/Java-26%20%7C%20Swing%20%7C%20AWT-orange.svg)
![C++](https://img.shields.io/badge/C++-17%20Standard-brightgreen.svg)
![Assembly](https://img.shields.io/badge/Assembly-80386%2032--bit-red.svg)
![OpenGL](https://img.shields.io/badge/OpenGL-GLUT%20%2B%20GLU-blue.svg)

---

## 1. Project Introduction

**AIRSTRIKE** is a multi-subject academic software engineering project and 2D space combat demonstration system. Built entirely from scratch, it rigorously satisfies all **19 Course Outcomes (COs)** across four core undergraduate computer science subjects:

1. **PSOOP (Principles of Object-Oriented Programming)**: Java + Swing + AWT
2. **PL (Programming Laboratory)**: Standard C++ (C++17)
3. **COA (Computer Organization and Architecture)**: 80386 32-bit Assembly Language
4. **CGL (Computer Graphics Laboratory)**: C++ + OpenGL + GLUT

### Crucial Architectural Principle
> [!IMPORTANT]
> **Zero Inter-Language Coupling**: The Java and C++ modules operate completely independently. There are **no JNI bindings**, **no TCP/UDP socket bridges**, and **no cross-language pipes**. Each subject is independently compiled, inspected, and verified with its own native toolchain.

---

## 2. Technology Stack

| Subject / Domain | Core Language | Frameworks / Libraries | Target Subsystem / Toolchain |
| :--- | :--- | :--- | :--- |
| **PSOOP** | Java (Java SE 26 / 17+) | Swing, AWT | JVM Desktop, Event Dispatch Thread (EDT) |
| **PL** | C++ (C++17 standard) | Custom Data Structures | MinGW GCC 6.3.0 (`-std=c++17`) |
| **COA** | 80386 Assembly | Protected Mode 32-bit Flat Model | MinGW GNU Assembler (`as` / `gcc -m32`) / NASM |
| **CGL** | C++ (C++17) | OpenGL 1.x-2.x, GLUT, GLU | MinGW GCC + `libglut32.a` + `glut32.dll` |
| **Database (Future Phase)** | SQL / HTTP | PostgreSQL + PostgREST | Supabase Cloud Backend |
| **Version Control** | Git | Git CLI | Local Repository |

---

## 3. Project Directory Structure

```
AIRSTRIKE/
├── PSOOP/                     # Principles of Object-Oriented Programming (Java)
│   ├── src/
│   │   ├── main/              # Application entry point
│   │   ├── model/             # Entity hierarchy (Ship, Player, Enemy, Projectile)
│   │   ├── game/              # GameEngine (Singleton), GameLoop, CollisionDetector
│   │   ├── ui/                # GameWindow, GamePanel, HUD, Canvas
│   │   ├── service/           # EnemyFactory, ScoreObserver, AssetManager
│   │   ├── database/          # Database client abstractions (future phase)
│   │   ├── exception/         # Custom exception hierarchy
│   │   └── test/              # Unit & verification tests
│   └── resources/
│       ├── images/            # Sprites and background textures
│       ├── sounds/            # Sound effects and audio
│       └── fonts/             # Custom typography
├── PL/                        # Programming Laboratory (C++17)
│   ├── src/
│   │   ├── arrays/            # Custom dynamic array (no std::vector)
│   │   ├── linked_lists/      # Custom doubly & circular linked lists
│   │   ├── stacks/            # Custom action undo/redo stack
│   │   ├── queues/            # Custom circular event queue
│   │   ├── searching/         # Binary & linear search implementations
│   │   ├── sorting/           # QuickSort, MergeSort, InsertionSort
│   │   └── hashing/           # Custom hash table (Linear Probing, Chaining)
│   ├── tests/                 # Unit & performance benchmarks
│   └── CMakeLists.txt         # PL CMake build definition
├── COA/                       # Computer Organization & Architecture (80386 ASM)
│   ├── architecture/          # 32-bit register and status flag operations
│   ├── instructions/          # Data transfer, arithmetic, logic, procedures
│   ├── arithmetic/            # Booth's multiplication, non-restoring division
│   ├── control_unit/          # Control word & signal generation
│   ├── pipelining/            # Hazard detection (RAW, stalls, branch flushes)
│   ├── simulator/             # Cycle-accurate instruction trace simulator
│   ├── tests/                 # Assembly verification test suite
│   ├── CMakeLists.txt         # COA CMake build definition
│   └── Makefile               # Direct MinGW 32-bit Makefile
├── CGL/                       # Computer Graphics Laboratory (OpenGL / GLUT)
│   ├── src/
│   │   ├── primitives/        # Points, lines, triangles, polygon primitives
│   │   ├── transformations/   # 2D/3D affine matrix transformations
│   │   ├── algorithms/        # Bresenham's line, Midpoint circle, Scanline fill
│   │   ├── objects/           # Spacecraft and asteroid geometric meshes
│   │   ├── projection/        # Orthographic and perspective camera viewing
│   │   ├── clipping/          # Cohen-Sutherland & Sutherland-Hodgman clipping
│   │   └── appearance/        # Phong reflection model, lighting, texturing
│   ├── resources/
│   │   ├── textures/          # Bitmap textures
│   │   └── shaders/           # Shader assets
│   ├── tests/                 # Graphics test harness
│   └── CMakeLists.txt         # CGL CMake build definition
├── database/                  # SQL schemas & migration scripts (future phase)
├── integration/
│   └── launcher/              # Integration launcher resources
├── docs/                      # Academic documentation & viva preparation
│   ├── architecture/          # System design specifications
│   ├── PL/                    # PL laboratory manuals & reports
│   ├── COA/                   # COA laboratory manuals & instruction sheets
│   ├── CGL/                   # CGL graphics theory & algorithm proofs
│   ├── PSOOP/                 # PSOOP UML class diagrams & design patterns
│   ├── viva/                  # Oral examination Q&A preparation
│   ├── screenshots/           # Verification screenshots
│   └── setup/
│       └── ENVIRONMENT_REPORT.md # Hardware/software inspection report
├── scripts/                   # Automated build & compilation scripts
│   ├── build_java.bat / .ps1
│   ├── build_pl.bat / .ps1
│   ├── build_coa.bat / .ps1
│   └── build_cgl.bat / .ps1
├── bin/                       # Compiled executables and binaries
│   ├── java/
│   ├── pl/
│   ├── coa/
│   └── cgl/
├── README.md                  # Project overview and instructions
├── ARCHITECTURE.md            # In-depth architectural blueprint
├── CO_MAPPING.md              # 19-outcome course mapping specification
├── .gitignore                 # Git ignore rules
├── build_all.bat              # Master Windows CMD build script
├── build_all.ps1              # Master Windows PowerShell build script
├── run_airstrike.bat          # Master Windows launcher
└── run_airstrike.sh           # Master POSIX/Bash launcher
```

---

## 4. Setup Instructions

### 4.1. Prerequisites Verified on System
The following tools are already detected on this host:
- **Java SE Development Kit**: Oracle JDK 26 (`javac`, `java`)
- **C++ Compiler**: MinGW GCC 6.3.0 (`g++`, `mingw32-make`)
- **OpenGL & GLUT**: MinGW `libglut32.a`, `libglu32.a`, `libopengl32.a`, `glut32.dll`
- **80386 Assembler**: MinGW GNU Assembler (`as.exe` / `gcc -m32`)
- **Git**: Git for Windows 2.55.0

See [docs/setup/ENVIRONMENT_REPORT.md](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/docs/setup/ENVIRONMENT_REPORT.md) for full environmental details.

### 4.2. Optional Tools Installation
If you wish to use CMake or NASM:
```powershell
# Using Chocolatey in an Administrator terminal:
choco install cmake --installargs 'ADD_CMAKE_TO_PATH=User' -y
choco install nasm -y
```

---

## 5. Build and Run Instructions

### 5.1. Master Build
To compile and verify all modules in one command:
```cmd
# Via Windows Command Prompt:
build_all.bat

# Via PowerShell:
.\build_all.ps1
```

### 5.2. Individual Module Compilation
Each module can be built independently without affecting other modules:
- **Java (PSOOP)**: `scripts\build_java.bat` (or `.\scripts\build_java.ps1`)
- **C++ Data Structures (PL)**: `scripts\build_pl.bat` (or `.\scripts\build_pl.ps1`)
- **80386 Assembly (COA)**: `scripts\build_coa.bat` (or `.\scripts\build_coa.ps1`)
- **OpenGL Graphics (CGL)**: `scripts\build_cgl.bat` (or `.\scripts\build_cgl.ps1`)

### 5.3. Running Airstrike
Launch the interactive master menu:
```cmd
run_airstrike.bat
```
Or run a specific module directly:
```cmd
# Run Java Game Client
run_airstrike.bat java

# Run C++ PL Data Structures Suite
run_airstrike.bat pl

# Run COA 80386 Assembly Demonstrations
run_airstrike.bat coa

# Run CGL OpenGL Demonstrations
run_airstrike.bat cgl
```

---

## 6. Academic Mapping (Course Outcomes)

For complete details on all 19 Course Outcomes, including theoretical concepts, source file locations, demonstration procedures, test cases, and viva Q&A explanations, refer to [CO_MAPPING.md](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/CO_MAPPING.md).

