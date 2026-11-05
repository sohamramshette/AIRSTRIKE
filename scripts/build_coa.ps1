# AIRSTRIKE: Build Script for COA (80386 Assembly Module)
$projectRoot = Split-Path -Parent $PSScriptRoot
if (Test-Path (Join-Path $projectRoot "COA")) {
    Set-Location $projectRoot
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "[COA] Building Computer Organization & Architecture Module..." -ForegroundColor Cyan
Write-Host "Target: 32-bit 80386 Assembly Language (No x86-64 substitution)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$assemblerFound = $false
$primaryAssembler = "none"

$nasmCmd = Get-Command "nasm" -ErrorAction SilentlyContinue
if ($nasmCmd) {
    $assemblerFound = $true
    $primaryAssembler = "nasm"
    $nasmVersion = & nasm -v 2>&1
    Write-Host "[INFO] Detected NASM Assembler: $nasmVersion" -ForegroundColor Green
}

$gccCmd = Get-Command "gcc" -ErrorAction SilentlyContinue
if ($gccCmd) {
    if (-not $assemblerFound) {
        $assemblerFound = $true
        $primaryAssembler = "gcc"
    }
    $gccTarget = & gcc -dumpmachine 2>&1
    Write-Host "[INFO] Detected GCC/GAS Toolchain Target: $gccTarget (32-bit i386 supported via -m32)" -ForegroundColor Green
}

if (-not $assemblerFound) {
    Write-Host "[ERROR] No suitable 32-bit 80386 assembler found in PATH!" -ForegroundColor Red
    Write-Host "Please install MinGW (32-bit as/gcc) or NASM (Netwide Assembler)." -ForegroundColor Yellow
    exit 1
}

$binCoa = "bin\coa"
if (-not (Test-Path $binCoa)) {
    New-Item -ItemType Directory -Path $binCoa -Force | Out-Null
}

$asmFiles = Get-ChildItem -Path "COA" -Include "*.s", "*.asm" -Recurse -ErrorAction SilentlyContinue
if (-not $asmFiles -or $asmFiles.Count -eq 0) {
    Write-Host "[WARN] No assembly source files found in COA yet (Phase 1 Foundation state)." -ForegroundColor Yellow
    Write-Host "[INFO] Prepared build output directory at $binCoa." -ForegroundColor Gray
    exit 0
}

Write-Host "[INFO] Found $($asmFiles.Count) assembly source file(s)." -ForegroundColor White
Write-Host "[SUCCESS] COA 80386 module build configuration ready!" -ForegroundColor Green
exit 0
