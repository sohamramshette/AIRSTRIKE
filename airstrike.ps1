# AIRSTRIKE: Master Unified Launcher (PowerShell Edition)
$projectRoot = $PSScriptRoot
if (Test-Path (Join-Path $projectRoot "bin")) {
    Set-Location $projectRoot
}

function Show-Menu {
    Clear-Host
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "                     AIRSTRIKE - UNIFIED MASTER LAUNCHER                      " -ForegroundColor Cyan
    Write-Host "                 Academic Demonstration & Verification System                 " -ForegroundColor Cyan
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "   Four Integrated Subjects - 19 Course Outcomes - Supabase Cloud Database    " -ForegroundColor White
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host "  [1] PSOOP - Java Space Shooter (Swing / AWT GUI)" -ForegroundColor Green
    Write-Host "  [2] PL    - Data Structures and Algorithms (C++17 CLI)" -ForegroundColor Green
    Write-Host "  [3] COA   - 80386 Assembly Programs (32-bit Assembly CLI)" -ForegroundColor Green
    Write-Host "  [4] CGL   - Computer Graphics Laboratory (OpenGL / GLUT)" -ForegroundColor Green
    Write-Host "  [5] Build All Modules (build_all.bat)" -ForegroundColor Yellow
    Write-Host "  [6] Run All Tests (test_all.bat)" -ForegroundColor Yellow
    Write-Host "  [7] Open Project Documentation" -ForegroundColor Magenta
    Write-Host "  [8] Exit" -ForegroundColor Red
    Write-Host "==============================================================================" -ForegroundColor Cyan
}

while ($true) {
    Show-Menu
    $choice = Read-Host "Select an option [1-8]"

    switch ($choice) {
        "1" {
            Write-Host "`n[PSOOP] Validating and launching Java Space Shooter..." -ForegroundColor Cyan
            if (-not (Test-Path "bin\java\main\Main.class")) {
                & ".\scripts\build_java.ps1"
            }
            Start-Process -FilePath "java" -ArgumentList "-cp", "bin\java", "main.Main"
        }
        "2" {
            Write-Host "`n[PL] Validating and launching C++17 Data Structures..." -ForegroundColor Cyan
            if (-not (Test-Path "bin\pl\pl_test_runner.exe")) {
                & ".\scripts\build_pl.ps1"
            }
            & "bin\pl\pl_test_runner.exe"
            Read-Host "`nPress Enter to return to menu"
        }
        "3" {
            Write-Host "`n[COA] Validating and launching 80386 Assembly Suite..." -ForegroundColor Cyan
            if (-not (Test-Path "bin\coa\coa_runner.exe")) {
                & ".\scripts\build_coa.ps1"
            }
            & "bin\coa\coa_runner.exe"
            Read-Host "`nPress Enter to return to menu"
        }
        "4" {
            Write-Host "`n[CGL] Validating and launching OpenGL Graphics..." -ForegroundColor Cyan
            if (-not (Test-Path "bin\cgl\cgl_runner.exe")) {
                & ".\scripts\build_cgl.ps1"
            }
            Start-Process -FilePath "bin\cgl\cgl_runner.exe"
        }
        "5" {
            Write-Host "`n[BUILD] Building all four modules..." -ForegroundColor Cyan
            & ".\build_all.ps1"
            Read-Host "`nPress Enter to return to menu"
        }
        "6" {
            Write-Host "`n[TEST] Running master automated test suite..." -ForegroundColor Cyan
            & ".\scripts\test_all.ps1"
            Read-Host "`nPress Enter to return to menu"
        }
        "7" {
            Write-Host "`nOpening documentation..." -ForegroundColor Cyan
            if (Test-Path "docs\FINAL_PROJECT_AUDIT.md") {
                Start-Process "docs\FINAL_PROJECT_AUDIT.md"
            } else {
                Start-Process "README.md"
            }
        }
        "8" {
            Write-Host "`nThank you for evaluating AIRSTRIKE. Systems safely powered down." -ForegroundColor Green
            exit 0
        }
        Default {
            Write-Host "`n[ERROR] Invalid option. Please enter a number between 1 and 8." -ForegroundColor Red
            Start-Sleep -Seconds 1
        }
    }
}

