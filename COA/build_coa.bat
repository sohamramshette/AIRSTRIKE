@echo off
setlocal

rem Anchor working directory to project root
if exist "%~dp0..\COA" cd /d "%~dp0.."
if exist "%~dp0src" cd /d "%~dp0.."

echo ========================================================
echo [COA] Building x86-64 Assembly Module...
echo Target: 64-bit x86-64 (Windows x64 ABI, GAS Intel Syntax)
echo ========================================================

set CC=
where x86_64-w64-mingw32-gcc >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set CC=x86_64-w64-mingw32-gcc
) else if exist "C:\msys64\ucrt64\bin\gcc.exe" (
    set CC=C:\msys64\ucrt64\bin\gcc.exe
) else (
    where gcc >nul 2>&1
    if %ERRORLEVEL% EQU 0 set CC=gcc -m64
)

if "%CC%"=="" (
    echo [ERROR] No 64-bit MinGW GCC compiler found in PATH!
    exit /b 1
)

if not exist "bin\coa" mkdir "bin\coa"

%CC% -Wall -O2 COA\src\common\*.s COA\src\co1_architecture\*.s COA\src\co2_assembly_programs\*.s COA\src\co3_arithmetic\*.s COA\src\co4_control_unit\*.s COA\tests\*.s COA\src\main.s -o bin\coa\coa_runner.exe
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] COA x86-64 assembly build failed!
    exit /b 1
)

echo [SUCCESS] COA x86-64 module built successfully at bin\coa\coa_runner.exe!
exit /b 0
