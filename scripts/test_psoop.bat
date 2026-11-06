@echo off
setlocal

rem Anchor working directory to project root
if exist "%~dp0..\PSOOP" cd /d "%~dp0.."

echo ========================================================
echo [PSOOP] Running Java Test Suites
echo ========================================================

if not exist "bin\java\test\GameUnitTest.class" (
    echo [INFO] Compiling Java module first...
    call scripts\build_java.bat
    if %ERRORLEVEL% NEQ 0 exit /b 1
)

echo.
echo [TEST 1/2] Running Core Game Unit Tests...
java -cp bin\java test.GameUnitTest
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] GameUnitTest failed!
    exit /b 1
)

echo.
echo [TEST 2/2] Running Supabase Authentication Test Suite...
java -cp bin\java test.SupabaseAuthTest
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] SupabaseAuthTest failed!
    exit /b 1
)

echo.
echo ========================================================
echo [SUCCESS] All PSOOP Java test suites passed!
echo ========================================================
exit /b 0
