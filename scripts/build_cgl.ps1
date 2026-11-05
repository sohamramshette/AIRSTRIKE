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

$cmakeCmd = Get-Command "cmake" -ErrorAction SilentlyContinue
if ($cmakeCmd) {
    Write-Host "[INFO] CMake detected. Building via CMake..." -ForegroundColor White
    if (-not (Test-Path "build\cgl")) { New-Item -ItemType Directory -Path "build\cgl" -Force | Out-Null }
    & cmake -B build\cgl -S CGL -G "MinGW Makefiles"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] CMake configuration failed!" -ForegroundColor Red
        exit 1
    }
    & cmake --build build\cgl
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] CMake build failed!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[INFO] CMake not detected in PATH. Using direct MinGW g++ compilation fallback..." -ForegroundColor Yellow
    $includeArgs = @(
        "-std=c++17", "-Wall", "-Wextra",
        "-D_DLL", "-DGLUT_DISABLE_ATEXIT_HACK",
        "-ICGL/src", "-ICGL/src/primitives", "-ICGL/src/transformations",
        "-ICGL/src/algorithms", "-ICGL/src/objects", "-ICGL/src/projection",
        "-ICGL/src/clipping", "-ICGL/src/appearance"
    )
    $sourcePaths = $cppFiles.FullName
    $linkArgs = @("-lglut32", "-lglu32", "-lopengl32", "-o", "$binCgl\cgl_demo.exe")
    & g++ @includeArgs @sourcePaths @linkArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Direct OpenGL compilation failed!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "[SUCCESS] CGL OpenGL module built successfully!" -ForegroundColor Green
exit 0
