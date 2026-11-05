# AIRSTRIKE: Build Script for PL (C++ Module)
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "[PL] Building Programming Laboratory (C++17) Module..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$gppCmd = Get-Command "g++" -ErrorAction SilentlyContinue
if (-not $gppCmd) {
    Write-Host "[ERROR] 'g++' (C++ Compiler) was not found in PATH!" -ForegroundColor Red
    Write-Host "Please install MinGW or GCC with C++17 support and add it to PATH." -ForegroundColor Yellow
    exit 1
}

$gppVersion = (& g++ --version 2>&1)[0]
Write-Host "[INFO] Detected C++ Compiler: $gppVersion" -ForegroundColor Green

$binPl = "bin\pl"
if (-not (Test-Path $binPl)) {
    New-Item -ItemType Directory -Path $binPl -Force | Out-Null
}

$cppFiles = Get-ChildItem -Path "PL" -Filter "*.cpp" -Recurse -ErrorAction SilentlyContinue
if (-not $cppFiles -or $cppFiles.Count -eq 0) {
    Write-Host "[WARN] No .cpp source files found in PL yet (Phase 1 Foundation state)." -ForegroundColor Yellow
    Write-Host "[INFO] Prepared build output directory at $binPl." -ForegroundColor Gray
    exit 0
}

$cmakeCmd = Get-Command "cmake" -ErrorAction SilentlyContinue
if ($cmakeCmd) {
    Write-Host "[INFO] CMake detected. Building via CMake..." -ForegroundColor White
    if (-not (Test-Path "build\pl")) { New-Item -ItemType Directory -Path "build\pl" -Force | Out-Null }
    & cmake -B build\pl -S PL -G "MinGW Makefiles"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] CMake configuration failed!" -ForegroundColor Red
        exit 1
    }
    & cmake --build build\pl
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] CMake build failed!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[INFO] CMake not detected in PATH. Using direct MinGW g++ compilation fallback..." -ForegroundColor Yellow
    $includeArgs = @(
        "-std=c++17", "-Wall", "-Wextra",
        "-IPL/src", "-IPL/src/arrays", "-IPL/src/linked_lists",
        "-IPL/src/stacks", "-IPL/src/queues", "-IPL/src/searching",
        "-IPL/src/sorting", "-IPL/src/hashing"
    )
    $sourcePaths = $cppFiles.FullName
    $outArgs = @("-o", "$binPl\pl_test_runner.exe")
    & g++ @includeArgs @sourcePaths @outArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Direct C++ compilation failed!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "[SUCCESS] PL C++ module built successfully!" -ForegroundColor Green
exit 0
