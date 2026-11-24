@echo off
setlocal

rem Anchor working directory to project root if called from scripts directory
if exist "%~dp0..\COA" cd /d "%~dp0.."

echo ========================================================
echo [COA] Building Computer Organization and Architecture Module...
echo Target: 64-bit x86-64 Assembly Language (Windows x64 ABI)
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
    echo [ERROR] No suitable 64-bit GCC/GAS assembler found in PATH!
    exit /b 1
)

if not exist "bin\coa" mkdir "bin\coa"

set SOURCE_COUNT=0
for /r "COA" %%f in (*.s) do (
    set /a SOURCE_COUNT+=1
)

if %SOURCE_COUNT% EQU 0 (
    echo [WARN] No assembly source files found in COA.
    exit /b 0
)

echo [INFO] Found %SOURCE_COUNT% assembly source files. Assembling and linking...
%CC% -Wall -O2 COA\src\common\*.s COA\src\co1_architecture\*.s COA\src\co2_assembly_programs\*.s COA\src\co3_arithmetic\*.s COA\src\co4_control_unit\*.s COA\tests\*.s COA\src\main.s -o bin\coa\coa_runner.exe
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] COA x86-64 assembly build failed!
    exit /b 1
)

echo [SUCCESS] COA x86-64 module built successfully at bin\coa\coa_runner.exe!
exit /b 0
