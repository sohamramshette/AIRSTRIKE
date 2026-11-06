# AIRSTRIKE: Build, Execution & Environment Guide

**Project**: AIRSTRIKE – 2D Space Shooter & Academic Demonstration System  
**Document**: Master Build & Runtime Guide (Phase 6.3 Final Integration Edition)  
**Date**: November 2026  

---

## 1. System Requirements

### Hardware & Operating System
- **Operating System**: Microsoft Windows 10 or Windows 11 (64-bit or 32-bit).
- **Processor**: Intel Core i3 / AMD Ryzen 3 or higher (x86/x64 architecture).
- **RAM**: Minimum 2 GB (4 GB recommended).
- **Graphics**: GPU with OpenGL 1.4+ compatibility context support (integrated graphics supported).

### Software Toolchains & Dependencies
| Module | Required Toolchain | Verified Version | Purpose |
| :--- | :--- | :--- | :--- |
| **PSOOP** | Java Development Kit (JDK) | Java 11+ (Verified on JDK 26.0.2) | Compiles and executes Java Swing client |
| **PL** | MinGW GCC (C++17) | GCC 6.3.0 or higher (`g++`) | Compiles C++17 data structures & algorithms |
| **COA** | MinGW GCC / GAS (`-m32`) | GCC 6.3.0 (i386 target support) | Assembles pure 32-bit 80386 assembly files |
| **CGL** | MinGW GCC + OpenGL + GLUT | GCC 6.3.0 + `freeglut` / `glut32` | Compiles hardware OpenGL graphics demos |
| **Cloud** | Supabase REST / HTTPS | PostgREST / GoTrue Auth | Online pilot authentication and leaderboard |

---

## 2. Environment Verification

Before building the project, ensure your command prompt recognizes the core compilers.

Run the diagnostic command:
```cmd
where javac java gcc g++
```

Expected output:
```
C:\Program Files\Java\jdk-26\bin\javac.exe
C:\Program Files\Java\jdk-26\bin\java.exe
C:\MinGW\bin\gcc.exe
C:\MinGW\bin\g++.exe
```

> [!TIP]
> If any compiler is missing, add its `bin` directory to your system `PATH` environment variable.

---

## 3. Quick Start: Unified Launcher

The easiest way to build, test, and launch AIRSTRIKE is using the **Unified Master Launcher**:

### In Windows Command Prompt:
```cmd
airstrike.bat
```

### In PowerShell:
```powershell
.\airstrike.ps1
```

### Launcher Interactive Menu:
```
==============================================================================
                     AIRSTRIKE - UNIFIED MASTER LAUNCHER
                 Academic Demonstration & Verification System
==============================================================================
  Four Integrated Subjects - 19 Course Outcomes - Supabase Cloud Database
==============================================================================
  [1] PSOOP - Java Space Shooter (Swing / AWT GUI)
  [2] PL    - Data Structures and Algorithms (C++17 CLI)
  [3] COA   - 80386 Assembly Programs (32-bit Assembly CLI)
  [4] CGL   - Computer Graphics Laboratory (OpenGL / GLUT)
  [5] Build All Modules (build_all.bat)
  [6] Run All Tests (test_all.bat)
  [7] Open Project Documentation
  [8] Exit
==============================================================================
```

---

## 4. Master Multi-Module Build

To compile all four autonomous modules in a single batch pass:

```cmd
build_all.bat
```

*(Or in PowerShell: `.\build_all.ps1`)*

### Output Matrix:
```
==============================================================================
                          BUILD SUMMARY MATRIX
==============================================================================
  PSOOP    : SUCCESS      Java Swing/AWT Subsystem
  PL       : SUCCESS      C++17 Data Structures and Algorithms
  COA      : SUCCESS      80386 32-bit Assembly Toolchain
  CGL      : SUCCESS      OpenGL and GLUT Graphics Subsystem
==============================================================================
[SUCCESS] All Airstrike modules verified and ready!
```

---

## 5. Master Automated Test Suite

To run all 302 automated verification tests across all four modules:

```cmd
test_all.bat
```

*(Or in PowerShell: `.\scripts\test_all.ps1`)*

### Test Results Breakdown:
- **PSOOP (Java)**: 143 / 143 Passed (Core game mechanics, Supabase auth, match persistence)
- **PL (C++17)**: 53 / 53 Passed (Arrays, linked lists, stacks, queues, sorting, searching, hashing)
- **COA (80386 ASM)**: 41 / 41 Passed (Architecture, assembly programs, Booth multiplication, pipeline model)
- **CGL (OpenGL)**: 65 / 65 Passed (Primitives, affine transformations, Bresenham, viewing, clipping, Phong/BMP)
- **TOTAL**: **302 / 302 Passed (100% Pass Rate)**

---

## 6. Autonomous Module Build & Run Instructions

Each subject can be built and run completely independently without cross-module dependencies:

### 6.1 PSOOP (Java Space Shooter)
* **Compile**:
  ```cmd
  scripts\build_java.bat
  ```
* **Run Unit Tests**:
  ```cmd
  scripts\test_psoop.bat
  ```
* **Run Interactive Game**:
  ```cmd
  java -cp bin\java main.Main
  ```

### 6.2 PL (C++17 Data Structures)
* **Compile**:
  ```cmd
  scripts\build_pl.bat
  ```
* **Run Headless Verification**:
  ```cmd
  bin\pl\pl_test_runner.exe --test
  ```
* **Run Interactive CLI Suite**:
  ```cmd
  bin\pl\pl_test_runner.exe
  ```

### 6.3 COA (80386 Assembly)
* **Assemble & Link**:
  ```cmd
  scripts\build_coa.bat
  ```
* **Run Headless Verification**:
  ```cmd
  bin\coa\coa_runner.exe --test
  ```
* **Run Interactive 80386 Suite**:
  ```cmd
  bin\coa\coa_runner.exe
  ```

### 6.4 CGL (OpenGL / GLUT Graphics)
* **Compile**:
  ```cmd
  scripts\build_cgl.bat
  ```
* **Run Headless Math Verification**:
  ```cmd
  bin\cgl\cgl_runner.exe --test
  ```
* **Run Interactive Graphical Window**:
  ```cmd
  bin\cgl\cgl_runner.exe
  ```

---

## 7. Supabase Cloud Configuration

AIRSTRIKE connects to Supabase for online pilot credentials, flight log persistence, and global leaderboard rankings.

### Configuration Precedence:
1. Java System Properties (`-Dsupabase.url=... -Dsupabase.key=...`)
2. Environment Variables (`SUPABASE_URL`, `SUPABASE_KEY`)
3. `config/supabase.properties`
4. `supabase.properties` (Project root)

### Template (`supabase.properties`):
```properties
supabase.url=https://your-project-id.supabase.co
supabase.key=your-anon-publishable-key-here
supabase.timeout_ms=8000
```

> [!WARNING]
> **Security Requirement**: Never use or commit the Supabase `service_role` secret key!
> `SupabaseConfig.java` actively throws a `SecurityException` if a `service_role` key is detected.

### Offline & Disconnected Play:
If no credentials are provided or network connectivity is unavailable, AIRSTRIKE automatically runs in **Guest Pilot** mode. The game loop, local high scores, and combat remain 100% playable offline without errors.

---

## 8. Troubleshooting

| Symptom | Cause | Solution |
| :--- | :--- | :--- |
| `'javac' is not recognized` | JDK not in system PATH | Install JDK 11+ and add `C:\Program Files\Java\jdk-<version>\bin` to PATH. |
| `'gcc' is not recognized` | MinGW not in system PATH | Install MinGW and add `C:\MinGW\bin` to PATH. |
| `skipping incompatible when searching for -lglut32` | 64-bit GCC used on 32-bit GLUT | Ensure MinGW 32-bit GCC is installed or pass `-m32` flags. |
| `glut32.dll was not found` | GLUT runtime library missing | Copy `glut32.dll` to `C:\Windows\System32` or the project root. |
| `SecurityException: Attempted to configure service_role key` | Administrative key detected | Replace key in `supabase.properties` with the client-safe `anon` publishable key. |

