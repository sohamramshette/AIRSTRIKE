# AIRSTRIKE: Build Script for COA (x86-64 Assembly Module)
$projectRoot = Split-Path -Parent $PSScriptRoot
if (Test-Path (Join-Path $projectRoot "COA")) {
    Set-Location $projectRoot
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "[COA] Building Computer Organization & Architecture Module..." -ForegroundColor Cyan
Write-Host "Target: 64-bit x86-64 Assembly Language (Windows x64 ABI)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$gccExe = $null
$extraArgs = @()

$x64Gcc = Get-Command "x86_64-w64-mingw32-gcc" -ErrorAction SilentlyContinue
if ($x64Gcc) {
    $gccExe = $x64Gcc.Source
} elseif (Test-Path "C:\msys64\ucrt64\bin\gcc.exe") {
    $gccExe = "C:\msys64\ucrt64\bin\gcc.exe"
} else {
    $gccCmd = Get-Command "gcc" -ErrorAction SilentlyContinue
    if ($gccCmd) {
        $gccExe = $gccCmd.Source
        $extraArgs = @("-m64")
    }
}

if (-not $gccExe) {
    Write-Host "[ERROR] No 64-bit GCC/GAS compiler found in PATH!" -ForegroundColor Red
    exit 1
}

$gccTarget = & $gccExe -dumpmachine 2>&1
Write-Host "[INFO] Using 64-bit GCC/GAS Toolchain: $gccExe ($gccTarget)" -ForegroundColor Green

$binCoa = "bin\coa"
if (-not (Test-Path $binCoa)) {
    New-Item -ItemType Directory -Path $binCoa -Force | Out-Null
}

$asmFiles = Get-ChildItem -Path "COA" -Include "*.s" -Recurse -ErrorAction SilentlyContinue
if (-not $asmFiles -or $asmFiles.Count -eq 0) {
    Write-Host "[WARN] No assembly source files found in COA." -ForegroundColor Yellow
    exit 0
}

Write-Host "[INFO] Found $($asmFiles.Count) assembly source file(s). Assembling and linking..." -ForegroundColor White
$outTarget = "$binCoa\coa_runner.exe"
$sourcePaths = $asmFiles.FullName
& $gccExe @extraArgs -Wall -O2 @sourcePaths -o $outTarget
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] COA x86-64 assembly compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "[SUCCESS] COA x86-64 module built successfully at $outTarget!" -ForegroundColor Green
exit 0
