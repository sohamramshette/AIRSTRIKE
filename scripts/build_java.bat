@echo off
setlocal

rem Anchor working directory to project root if called from scripts directory
if exist "%~dp0..\PSOOP" cd /d "%~dp0.."

echo ========================================================
echo [PSOOP] Building Java Swing/AWT Module...
echo ========================================================

where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] javac compiler was not found in PATH!
    echo Please install Java JDK and add javac to PATH.
    exit /b 1
)

for /f "tokens=*" %%v in ('javac -version 2^>^&1') do set JAVAC_VER=%%v
echo [INFO] Detected Java Compiler: %JAVAC_VER%

if not exist "bin\java" mkdir "bin\java"

set SOURCE_COUNT=0
for /r "PSOOP\src" %%f in (*.java) do (
    set /a SOURCE_COUNT+=1
)

if %SOURCE_COUNT% EQU 0 (
    echo [WARN] No .java source files found in PSOOP\src yet - Phase 1 Foundation state.
    echo [INFO] Prepared build output directory at bin\java.
    exit /b 0
)

echo [INFO] Found %SOURCE_COUNT% Java source files. Compiling...
dir /s /B PSOOP\src\*.java > bin\java\sources.txt
javac -encoding UTF-8 -d bin\java @bin\java\sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java compilation failed!
    del bin\java\sources.txt 2>nul
    exit /b 1
)
del bin\java\sources.txt 2>nul

echo [SUCCESS] PSOOP Java module compiled successfully to bin\java!
exit /b 0
