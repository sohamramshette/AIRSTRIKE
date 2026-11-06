# AIRSTRIKE Standalone Desktop Packager (PowerShell)
# Packages Java game with minimal embedded JRE via jlink & jpackage

$ErrorActionPreference = "Stop"

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "                AIRSTRIKE - STANDALONE WINDOWS PACKAGER                      " -ForegroundColor Cyan
Write-Host "              Bundles Standalone Application with Embedded JRE                " -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan

$jdkBin = "C:\Program Files\Java\jdk-26.0.2\bin"
$jmods = "C:\Program Files\Java\jdk-26.0.2\jmods"

if (-not (Test-Path "$jdkBin\jpackage.exe")) {
    Write-Error "jpackage.exe not found in $jdkBin. Please verify JDK 26 installation."
}

Write-Host "`n[1/4] Compiling Java source files..." -ForegroundColor Yellow
& .\scripts\build_java.bat
if ($LASTEXITCODE -ne 0) { throw "Java build failed." }

Write-Host "`n[2/4] Assembling executable JAR dist\airstrike.jar..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path build, dist, release | Out-Null
& "$jdkBin\jar.exe" --create --file dist\airstrike.jar --main-class main.Main -C bin\java .
if ($LASTEXITCODE -ne 0) { throw "JAR creation failed." }
Write-Host "[SUCCESS] dist\airstrike.jar created." -ForegroundColor Green

Write-Host "`n[3/4] Building minimal custom JRE with jlink..." -ForegroundColor Yellow
if (Test-Path build\custom-runtime) { Remove-Item -Recurse -Force build\custom-runtime }
& "$jdkBin\jlink.exe" --module-path $jmods --add-modules java.base,java.desktop,java.net.http,java.logging,jdk.crypto.ec --strip-debug --no-man-pages --no-header-files --compress zip-6 --output build\custom-runtime
if ($LASTEXITCODE -ne 0) { throw "jlink runtime creation failed." }
Write-Host "[SUCCESS] Custom JRE built in build\custom-runtime." -ForegroundColor Green

Write-Host "`n[4/4] Generating standalone Windows app-image with jpackage..." -ForegroundColor Yellow
if (Test-Path release\AIRSTRIKE) { Remove-Item -Recurse -Force release\AIRSTRIKE }
& "$jdkBin\jpackage.exe" --type app-image --name AIRSTRIKE --input dist --main-jar airstrike.jar --main-class main.Main --runtime-image build\custom-runtime --dest release
if ($LASTEXITCODE -ne 0) { throw "jpackage bundling failed." }

if (Test-Path config\supabase.properties) {
    New-Item -ItemType Directory -Force -Path release\AIRSTRIKE\config | Out-Null
    Copy-Item config\supabase.properties release\AIRSTRIKE\config\supabase.properties -Force
    Write-Host "[INFO] Included config\supabase.properties in package." -ForegroundColor Cyan
}

Write-Host "`n[INFO] Compressing release\AIRSTRIKE_Windows_x64.zip..." -ForegroundColor Yellow
Compress-Archive -Path release\AIRSTRIKE -DestinationPath release\AIRSTRIKE_Windows_x64.zip -Force

$zip = Get-Item release\AIRSTRIKE_Windows_x64.zip
Write-Host "`n==============================================================================" -ForegroundColor Green
Write-Host "                   STANDALONE DESKTOP PACKAGE READY!                         " -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host "App Directory : release\AIRSTRIKE\"
Write-Host "Executable    : release\AIRSTRIKE\AIRSTRIKE.exe"
Write-Host "Zip Archive   : release\AIRSTRIKE_Windows_x64.zip ($([math]::Round($zip.Length/1MB, 2)) MB)"
Write-Host "Requirements  : Standalone! Zero Java installation required by students."
Write-Host "==============================================================================" -ForegroundColor Green

