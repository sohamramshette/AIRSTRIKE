# AIRSTRIKE: Master Build System (PowerShell)
# Builds and verifies all four independent subject modules.

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "                AIRSTRIKE - MASTER BUILD SYSTEM (PHASE 1)                     " -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "Target Modules:" -ForegroundColor White
Write-Host "  1. PSOOP : Java + Swing + AWT" -ForegroundColor Gray
Write-Host "  2. PL    : C++17 Data Structures and Algorithms" -ForegroundColor Gray
Write-Host "  3. COA   : 80386 32-bit Assembly Language" -ForegroundColor Gray
Write-Host "  4. CGL   : C++17 + OpenGL + GLUT Graphics" -ForegroundColor Gray
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

$binDirs = @("bin\java", "bin\pl", "bin\coa", "bin\cgl")
foreach ($dir in $binDirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}

$summary = [ordered]@{}
$totalFailures = 0

# 1. PSOOP
Write-Host "--- [1/4] Building PSOOP (Java) ---" -ForegroundColor Yellow
& .\scripts\build_java.ps1
if ($LASTEXITCODE -eq 0) {
    $summary["PSOOP"] = @{ Status = "SUCCESS"; Toolchain = "Java SE (javac)" }
} else {
    $summary["PSOOP"] = @{ Status = "FAILED"; Toolchain = "Java SE (javac)" }
    $totalFailures++
}
Write-Host ""

# 2. PL
Write-Host "--- [2/4] Building PL (C++17) ---" -ForegroundColor Yellow
& .\scripts\build_pl.ps1
if ($LASTEXITCODE -eq 0) {
    $summary["PL"] = @{ Status = "SUCCESS"; Toolchain = "MinGW C++17 (g++)" }
} else {
    $summary["PL"] = @{ Status = "FAILED"; Toolchain = "MinGW C++17 (g++)" }
    $totalFailures++
}
Write-Host ""

# 3. COA
Write-Host "--- [3/4] Building COA (80386 ASM) ---" -ForegroundColor Yellow
& .\scripts\build_coa.ps1
if ($LASTEXITCODE -eq 0) {
    $summary["COA"] = @{ Status = "SUCCESS"; Toolchain = "32-bit 80386 (as / gcc -m32)" }
} else {
    $summary["COA"] = @{ Status = "FAILED"; Toolchain = "32-bit 80386 (as / gcc -m32)" }
    $totalFailures++
}
Write-Host ""

# 4. CGL
Write-Host "--- [4/4] Building CGL (OpenGL/GLUT) ---" -ForegroundColor Yellow
& .\scripts\build_cgl.ps1
if ($LASTEXITCODE -eq 0) {
    $summary["CGL"] = @{ Status = "SUCCESS"; Toolchain = "MinGW g++ + OpenGL/GLUT" }
} else {
    $summary["CGL"] = @{ Status = "FAILED"; Toolchain = "MinGW g++ + OpenGL/GLUT" }
    $totalFailures++
}
Write-Host ""

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "                          BUILD SUMMARY MATRIX                                " -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
foreach ($key in $summary.Keys) {
    $st = $summary[$key].Status
    $tc = $summary[$key].Toolchain
    $color = if ($st -eq "SUCCESS") { "Green" } else { "Red" }
    Write-Host ("  {0,-8} : {1,-10} [{2}]" -f $key, $st, $tc) -ForegroundColor $color
}
Write-Host "==============================================================================" -ForegroundColor Cyan

if ($totalFailures -gt 0) {
    Write-Host "[ERROR] Build completed with $totalFailures failure(s). Check messages above." -ForegroundColor Red
    exit 1
}

Write-Host "[SUCCESS] All Airstrike modules verified and ready!" -ForegroundColor Green
exit 0
