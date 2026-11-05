# AIRSTRIKE: Build Script for PSOOP (Java Module)
$projectRoot = Split-Path -Parent $PSScriptRoot
if (Test-Path (Join-Path $projectRoot "PSOOP")) {
    Set-Location $projectRoot
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "[PSOOP] Building Java (Swing/AWT) Module..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$javacCmd = Get-Command "javac" -ErrorAction SilentlyContinue
if (-not $javacCmd) {
    Write-Host "[ERROR] 'javac' (Java Compiler) was not found in PATH!" -ForegroundColor Red
    Write-Host "Please install Java JDK (e.g., JDK 17 or higher) and add javac to PATH." -ForegroundColor Yellow
    exit 1
}

$javacVersion = & javac -version 2>&1
Write-Host "[INFO] Detected Java Compiler: $javacVersion" -ForegroundColor Green

$binJava = "bin\java"
if (-not (Test-Path $binJava)) {
    New-Item -ItemType Directory -Path $binJava -Force | Out-Null
}

$javaFiles = Get-ChildItem -Path "PSOOP\src" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
if (-not $javaFiles -or $javaFiles.Count -eq 0) {
    Write-Host "[WARN] No .java source files found in PSOOP\src yet (Phase 1 Foundation state)." -ForegroundColor Yellow
    Write-Host "[INFO] Prepared build output directory at $binJava." -ForegroundColor Gray
    exit 0
}

Write-Host "[INFO] Found $($javaFiles.Count) Java source file(s). Compiling..." -ForegroundColor White
$sourcesFile = "bin\java\sources.txt"
$javaFiles.FullName | Set-Content -Path $sourcesFile

& javac -encoding UTF-8 -d $binJava "@$sourcesFile"
$compileStatus = $LASTEXITCODE

Remove-Item $sourcesFile -ErrorAction SilentlyContinue

if ($compileStatus -ne 0) {
    Write-Host "[ERROR] Java compilation failed with exit code $compileStatus!" -ForegroundColor Red
    exit 1
}

Write-Host "[SUCCESS] PSOOP Java module compiled successfully to $binJava!" -ForegroundColor Green
exit 0
