# AIRSTRIKE: Build Script for CGL (OpenGL / GLUT Module)
$projectRoot = Split-Path -Parent $PSScriptRoot
if (Test-Path (Join-Path $projectRoot "CGL")) {
    Set-Location $projectRoot
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "[CGL] Building Computer Graphics Lab (OpenGL/GLUT) Module..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$gppCmd = Get-Command "g++" -ErrorAction SilentlyContinue
if (-not $gppCmd) {
    Write-Host "[ERROR] 'g++' (C++ Compiler) was not found in PATH!" -ForegroundColor Red
    Write-Host "Please install MinGW or GCC with C++17 and OpenGL support." -ForegroundColor Yellow
    exit 1
}

$gppVersion = (& g++ --version 2>&1)[0]
Write-Host "[INFO] Detected C++ Compiler: $gppVersion" -ForegroundColor Green

$binCgl = "bin\cgl"
if (-not (Test-Path $binCgl)) {
    New-Item -ItemType Directory -Path $binCgl -Force | Out-Null
}

$cppFiles = Get-ChildItem -Path "CGL" -Filter "*.cpp" -Recurse -ErrorAction SilentlyContinue
if (-not $cppFiles -or $cppFiles.Count -eq 0) {
    Write-Host "[WARN] No .cpp source files found in CGL yet (Phase 1 Foundation state)." -ForegroundColor Yellow
    Write-Host "[INFO] Prepared build output directory at $binCgl." -ForegroundColor Gray
    exit 0
}

Write-Host "[INFO] Compiling CGL OpenGL/GLUT sources with MinGW g++ C++17..." -ForegroundColor White
$includeArgs = @(
    "-std=c++17", "-Wall", "-Wextra",
    "-ICGL/src"
)
$sourcePaths = $cppFiles.FullName
$targetExe = "$binCgl\cgl_runner.exe"
$linkArgs = @("-lglut32", "-lglu32", "-lopengl32", "-o", $targetExe)

& g++ @includeArgs @sourcePaths @linkArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Direct OpenGL compilation failed!" -ForegroundColor Red
    exit 1
}

Copy-Item $targetExe "$binCgl\cgl_demo.exe" -Force -ErrorAction SilentlyContinue

Write-Host "[SUCCESS] CGL OpenGL module built successfully: $targetExe" -ForegroundColor Green
exit 0
