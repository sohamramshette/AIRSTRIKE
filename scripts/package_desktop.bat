@echo off
setlocal enabledelayedexpansion

echo ==============================================================================
echo                 AIRSTRIKE - STANDALONE WINDOWS PACKAGER
echo               Bundles Standalone Application with Embedded JRE
echo ==============================================================================
echo.

:: Detect JDK 26 location
set "JDK_BIN=C:\Program Files\Java\jdk-26.0.2\bin"
if not exist "%JDK_BIN%\jpackage.exe" (
    echo [ERROR] jpackage.exe not found in %JDK_BIN%!
    echo Please verify Java 26 JDK installation.
    exit /b 1
)

echo [1/4] Compiling Java source files...
call scripts\build_java.bat
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java compilation failed!
    exit /b %ERRORLEVEL%
)

echo.
echo [2/4] Assembling executable JAR dist\airstrike.jar...
if not exist "dist" mkdir dist
if not exist "build" mkdir build
if not exist "release" mkdir release

"%JDK_BIN%\jar.exe" --create --file dist\airstrike.jar --main-class main.Main -C bin\java .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] JAR assembly failed!
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] dist\airstrike.jar created successfully.

echo.
echo [3/4] Building minimal custom JRE with jlink...
if exist "build\custom-runtime" rmdir /s /q "build\custom-runtime"
"%JDK_BIN%\jlink.exe" --module-path "C:\Program Files\Java\jdk-26.0.2\jmods" --add-modules java.base,java.desktop,java.net.http,java.logging,jdk.crypto.ec --strip-debug --no-man-pages --no-header-files --compress zip-6 --output build\custom-runtime
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] jlink runtime generation failed!
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] Minimal JRE runtime built in build\custom-runtime.

echo.
echo [4/4] Generating standalone Windows app-image with jpackage...
if exist "release\AIRSTRIKE" rmdir /s /q "release\AIRSTRIKE"
"%JDK_BIN%\jpackage.exe" --type app-image --name AIRSTRIKE --input dist --main-jar airstrike.jar --main-class main.Main --runtime-image build\custom-runtime --dest release
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] jpackage packaging failed!
    exit /b %ERRORLEVEL%
)

:: Copy configuration template or local properties if available
if exist "config\supabase.properties" (
    if not exist "release\AIRSTRIKE\config" mkdir "release\AIRSTRIKE\config"
    copy /y "config\supabase.properties" "release\AIRSTRIKE\config\supabase.properties" >nul
    echo [INFO] Included config\supabase.properties in release package.
)

echo.
echo [INFO] Generating distributable ZIP archive release\AIRSTRIKE_Windows_x64.zip...
powershell -Command "Compress-Archive -Path release\AIRSTRIKE -DestinationPath release\AIRSTRIKE_Windows_x64.zip -Force"

echo.
echo ==============================================================================
echo                    STANDALONE DESKTOP PACKAGE READY!
echo ==============================================================================
echo App Directory : release\AIRSTRIKE\
echo Executable    : release\AIRSTRIKE\AIRSTRIKE.exe
echo Zip Archive   : release\AIRSTRIKE_Windows_x64.zip
echo Requirements  : Standalone! Zero Java installation required by students.
echo ==============================================================================
exit /b 0

