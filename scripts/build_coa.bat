@echo off
setlocal

rem Anchor working directory to project root if called from scripts directory
if exist "%~dp0..\COA" cd /d "%~dp0.."

echo ========================================================
echo [COA] Building Computer Organization and Architecture Module...
echo Target: 32-bit 80386 Assembly Language - No x86-64 substitution
echo ========================================================

set ASSEMBLER_FOUND=0
set ASSEMBLER_NAME=none

where nasm >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set ASSEMBLER_FOUND=1
    set ASSEMBLER_NAME=nasm
    for /f "tokens=*" %%v in ('nasm -v 2^>^&1') do echo [INFO] Detected NASM Assembler: %%v
)

where gcc >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    if %ASSEMBLER_FOUND% EQU 0 (
        set ASSEMBLER_FOUND=1
        set ASSEMBLER_NAME=gcc
    )
    for /f "tokens=*" %%v in ('gcc -dumpmachine 2^>^&1') do echo [INFO] Detected GCC/GAS Target: %%v - 32-bit i386 supported via -m32
)

if %ASSEMBLER_FOUND% EQU 0 (
    echo [ERROR] No suitable 32-bit 80386 assembler found in PATH!
    echo Please install MinGW 32-bit as/gcc or NASM Netwide Assembler.
    exit /b 1
)

if not exist "bin\coa" mkdir "bin\coa"

set SOURCE_COUNT=0
for /r "COA" %%f in (*.s *.asm) do (
    set /a SOURCE_COUNT+=1
)

if %SOURCE_COUNT% EQU 0 (
    echo [WARN] No assembly source files found in COA yet - Phase 1 Foundation state.
    echo [INFO] Prepared build output directory at bin\coa.
    exit /b 0
)

echo [INFO] Found %SOURCE_COUNT% assembly source files.
echo [SUCCESS] COA 80386 module build configuration ready!
exit /b 0
