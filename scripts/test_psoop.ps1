# AIRSTRIKE: Test Runner for PSOOP (Java Module)
$projectRoot = Split-Path -Parent $PSScriptRoot
if (Test-Path (Join-Path $projectRoot "PSOOP")) {
    Set-Location $projectRoot
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "[PSOOP] Running Java Test Suites..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin\java\test\GameUnitTest.class")) {
    Write-Host "[INFO] Compiling Java module first..." -ForegroundColor White
    & "$PSScriptRoot\build_java.ps1"
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

Write-Host "`n[TEST 1/2] Running Core Game Unit Tests..." -ForegroundColor Yellow
& java -cp "bin\java" test.GameUnitTest
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] GameUnitTest failed!" -ForegroundColor Red
    exit 1
}

Write-Host "`n[TEST 2/2] Running Supabase Authentication Test Suite..." -ForegroundColor Yellow
& java -cp "bin\java" test.SupabaseAuthTest
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] SupabaseAuthTest failed!" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================================" -ForegroundColor Green
Write-Host "[SUCCESS] All PSOOP Java test suites passed!" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
exit 0

