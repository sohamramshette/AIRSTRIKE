@echo off
setlocal

rem Anchor working directory to project root
if exist "%~dp0bin" cd /d "%~dp0"

if "%1"=="" goto :delegate_master
if /i "%1"=="java" goto :run_java
if /i "%1"=="psoop" goto :run_java
if /i "%1"=="browser" goto :run_browser
if /i "%1"=="web" goto :run_browser
if /i "%1"=="pl" goto :run_pl
if /i "%1"=="coa" goto :run_coa
if /i "%1"=="cgl" goto :run_cgl
if /i "%1"=="build" goto :run_build
if /i "%1"=="test" goto :run_tests
if /i "%1"=="verify" goto :run_tests
if /i "%1"=="roster" goto :run_roster
if /i "%1"=="docs" goto :open_docs

:delegate_master
call airstrike.bat
exit /b %ERRORLEVEL%

:run_java
echo.
echo [PSOOP] Launching Java Game Client...
if not exist "bin\java\main\Main.class" call scripts\build_java.bat
start "AIRSTRIKE - Java Combat Client" java -cp bin\java main.Main
exit /b 0

:run_browser
echo.
echo [WEB] Launching Browser Client development server...
start "AIRSTRIKE - Web Server" cmd /k "cd /d web && npm run dev"
timeout /t 2 >nul
start http://localhost:3000
exit /b 0

:run_pl
echo.
echo [PL] Launching C++ Data Structures Runner...
if not exist "bin\pl\pl_test_runner.exe" call scripts\build_pl.bat
bin\pl\pl_test_runner.exe
exit /b 0

:run_coa
echo.
echo [COA] Launching 80386 Assembly Demonstrations...
if not exist "bin\coa\coa_runner.exe" call scripts\build_coa.bat
bin\coa\coa_runner.exe
exit /b 0

:run_cgl
echo.
echo [CGL] Launching OpenGL and GLUT Demonstrations...
if not exist "bin\cgl\cgl_runner.exe" call scripts\build_cgl.bat
start "AIRSTRIKE - Computer Graphics Lab (OpenGL)" bin\cgl\cgl_runner.exe
exit /b 0

:run_build
call build_all.bat
exit /b %ERRORLEVEL%

:run_tests
call test_all.bat
exit /b %ERRORLEVEL%

:run_roster
call airstrike.bat 9
exit /b %ERRORLEVEL%

:open_docs
start "" "docs\FINAL_PROJECT_AUDIT.md"
exit /b 0
