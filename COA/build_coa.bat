@echo off
setlocal

rem Anchor working directory to project root
if exist "%~dp0..\COA" cd /d "%~dp0.."
if exist "%~dp0src" cd /d "%~dp0.."

echo ========================================================
echo [COA] Building 80386 Assembly Module...
echo Target: 32-bit Intel 80386 Protected Mode via MinGW GCC
echo ========================================================

where gcc >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] MinGW gcc compiler was not found in PATH!
    exit /b 1
)

if not exist "bin\coa" mkdir "bin\coa"

gcc -m32 -Wall -O2 COA\src\common\*.s COA\src\co1_architecture\*.s COA\src\co2_assembly_programs\*.s COA\src\co3_arithmetic\*.s COA\src\co4_control_unit\*.s COA\tests\*.s COA\src\main.s -o bin\coa\coa_runner.exe
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] 80386 assembly build failed!
    exit /b 1
)

echo [SUCCESS] COA 80386 module built successfully at bin\coa\coa_runner.exe!
exit /b 0
