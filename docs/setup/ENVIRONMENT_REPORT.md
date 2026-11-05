# AIRSTRIKE: Academic System Environment Inspection Report

**Generated Date**: 2026-11-06  
**Host Operating System**: Microsoft Windows (64-bit with WoW64 32-bit execution subsystem)  
**Project Workspace**: `c:\Users\lenovo\OneDrive\Desktop\Projects\AirStrike11`  

---

## 1. Executive Summary

This report documents the availability, versions, toolchain capabilities, missing dependencies, and recommended setup procedures for the **AIRSTRIKE** project across its four core college subjects:
1. **PSOOP** (Java OOP with Swing & AWT)
2. **PL** (Programming Laboratory in C++17)
3. **COA** (Computer Organization & Architecture with 80386 32-bit Assembly)
4. **CGL** (Computer Graphics Laboratory in C++ with OpenGL & GLUT)

---

## 2. Installed Tools & Verified Status

| Component | Tool / Binary | Installed Version | Path | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Java Runtime** | `java.exe` | 26.0.2 (64-Bit Server VM) | `C:\Program Files\Common Files\Oracle\Java\javapath\java.exe` | **READY** |
| **Java Compiler** | `javac.exe` | 26.0.2 | `C:\Program Files\Common Files\Oracle\Java\javapath\javac.exe` | **READY** |
| **Swing & AWT** | Java Standard Desktop | Java SE 26 Desktop Module | Built-in (`javax.swing.*`, `java.awt.*`) | **READY** |
| **C++ Compiler** | `g++.exe` | MinGW.org GCC 6.3.0-1 | `C:\MinGW\bin\g++.exe` | **READY** (supports `-std=c++17`) |
| **C++ Target Arch**| `mingw32` | 32-bit x86 (`i686-pc-mingw32`) | `C:\MinGW\bin` | **READY** |
| **OpenGL Libs** | `libopengl32.a`, `libglu32.a` | Win32 OpenGL Driver | `C:\MinGW\lib` | **READY** |
| **GLUT Library** | `libglut32.a`, `glut32.dll` | Win32 GLUT 3.7.6 | `C:\MinGW\lib\libglut32.a`, `C:\MinGW\bin\glut32.dll` | **READY** |
| **OpenGL Headers** | `gl.h`, `glu.h`, `glut.h` | OpenGL Core / Utility | `C:\MinGW\include\GL\` | **READY** |
| **80386 Assembler**| `as.exe` / `gcc.exe` | GNU Binutils 2.28 (`mingw32`) | `C:\MinGW\bin\as.exe` | **READY** (`.arch i386`, Intel syntax) |
| **Build Make Tool**| `mingw32-make.exe`| GNU Make 3.82.90 | `C:\MinGW\bin\mingw32-make.exe` | **READY** |
| **Version Control**| `git.exe` | 2.55.0.windows.2 | `C:\Program Files\Git\cmd\git.exe` | **READY** |
| **Package Manager**| `choco.exe` | Chocolatey CLI | `C:\ProgramData\chocolatey\bin\choco.exe` | **READY** |

---

## 3. 80386 Assembly Toolchain Investigation (COA)

### Requirement
The COA subject strictly requires **32-bit 80386 assembly language**. Silent substitution of x86-64 assembly instructions or registers (`RAX`, `RBX`, `RSP`, 64-bit ABI) is prohibited.

### Toolchain Findings
1. **Available 32-bit Native Assembler**:
   - `as.exe` (GNU Binutils 2.28) is configured specifically for `mingw32` (32-bit x86 i386/i686 architecture).
   - Supports 32-bit 80386 instructions with `.arch i386` directive.
   - Supports Intel assembly syntax via `.intel_syntax noprefix`.
   - Outputs native 32-bit PE COFF object files (`.o`) and executes on Windows WoW64.
   - **Verification Passed**: A standalone 80386 assembly program utilizing 32-bit `mov eax, 42; ret` was assembled and executed natively, returning exit code 42.

2. **Optional / Industry Standard Assembler (NASM)**:
   - NASM is currently **not installed** in PATH.
   - While MinGW GNU Assembler (`as`) fully satisfies the 80386 requirement, NASM (`nasm -f win32`) is popular in college curricula for MASM/Intel syntax compatibility.
   - Build scripts are designed to support MinGW `as`/`gcc` out of the box, with automatic detection and support for NASM if installed by the user.

---

## 4. Missing Dependencies

| Missing Tool | Subject / Module | Impact | Workaround Available? |
| :--- | :--- | :--- | :--- |
| **CMake** | PL & CGL Modules | Cannot run `cmake -B build` from terminal unless installed. | **Yes**: Direct compilation scripts (`scripts/build_pl.bat`, `scripts/build_cgl.bat`, `scripts/build_pl.ps1`, `scripts/build_cgl.ps1`) use MinGW `g++` directly. |
| **NASM** | COA (Optional) | Intel syntax files formatted for NASM cannot be compiled via `nasm`. | **Yes**: MinGW GNU Assembler (`as` / `gcc -m32`) is installed and active with `.intel_syntax noprefix`. |

---

## 5. Recommended Installation Steps

To install the optional tools (CMake and NASM), the user may run the following commands in an elevated PowerShell terminal:

### Option A: Via Chocolatey (Recommended)
```powershell
# Install CMake and add to user PATH
choco install cmake --installargs 'ADD_CMAKE_TO_PATH=User' -y

# Install NASM (Netwide Assembler)
choco install nasm -y
```

### Option B: Manual Downloads
1. **CMake**: Download installer from [https://cmake.org/download/](https://cmake.org/download/) (Select `Windows x64 Installer` and check "Add CMake to system PATH").
2. **NASM**: Download zip or installer from [https://www.nasm.us/pub/nasm/releasebuilds/](https://www.nasm.us/pub/nasm/releasebuilds/) and add its folder (e.g. `C:\Program Files\NASM`) to PATH.

---

## 6. Build & Compilation Commands

### 6.1. Java PSOOP Module
- **Compiler**: `javac`
- **Source Root**: `PSOOP/src`
- **Output Directory**: `bin/java`
- **Direct Command**:
  ```cmd
  javac -d bin/java PSOOP/src/model/*.java PSOOP/src/game/*.java PSOOP/src/ui/*.java PSOOP/src/service/*.java PSOOP/src/exception/*.java PSOOP/src/main/*.java
  ```
- **Execution Command**:
  ```cmd
  java -cp bin/java main.Main
  ```

### 6.2. C++ PL Module
- **Compiler**: `g++ -std=c++17`
- **With CMake**:
  ```cmd
  cmake -B build/pl -S PL -G "MinGW Makefiles"
  cmake --build build/pl
  ```
- **Direct MinGW Compilation (when CMake is absent)**:
  ```cmd
  g++ -std=c++17 -Wall -Wextra PL/src/arrays/*.cpp PL/src/linked_lists/*.cpp PL/src/stacks/*.cpp PL/src/queues/*.cpp PL/src/searching/*.cpp PL/src/sorting/*.cpp PL/src/hashing/*.cpp PL/tests/*.cpp -o bin/pl/pl_test_runner.exe
  ```

### 6.3. COA 80386 Assembly Module
- **Assembler**: MinGW `gcc -m32` or `as --32`
- **Target**: 32-bit x86 PE executable
- **Direct Compilation**:
  ```cmd
  gcc -m32 -nostartfiles COA/architecture/*.s COA/instructions/*.s COA/arithmetic/*.s -o bin/coa/coa_arithmetic.exe
  ```
- **With NASM (if installed)**:
  ```cmd
  nasm -f win32 COA/arithmetic/booth_mult.asm -o bin/coa/booth_mult.obj
  gcc -m32 bin/coa/booth_mult.obj -o bin/coa/booth_mult.exe
  ```

### 6.4. CGL OpenGL Module
- **Compiler**: `g++ -std=c++17`
- **Headers & Flags**: `-D_DLL` (required for MinGW `glut32.dll` dynamic linking)
- **Linker Flags**: `-lglut32 -lglu32 -lopengl32`
- **With CMake**:
  ```cmd
  cmake -B build/cgl -S CGL -G "MinGW Makefiles"
  cmake --build build/cgl
  ```
- **Direct MinGW Compilation (when CMake is absent)**:
  ```cmd
  g++ -std=c++17 -D_DLL CGL/src/primitives/*.cpp CGL/src/transformations/*.cpp CGL/src/algorithms/*.cpp CGL/src/projection/*.cpp CGL/src/clipping/*.cpp CGL/src/appearance/*.cpp -lglut32 -lglu32 -lopengl32 -o bin/cgl/cgl_demo.exe
  ```

---

## 7. Verification Summary
- **No environment changes were made** without permission.
- **MinGW GCC 6.3.0 and MinGW as** provide an intact, functional 32-bit C++ and 80386 assembly pipeline.
- **Java 26.0.2** provides a complete desktop Java Swing & AWT development environment.
- **GLUT & OpenGL** are verified and operational.
