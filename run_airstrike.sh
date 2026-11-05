#!/usr/bin/env bash
# AIRSTRIKE – Master Launcher Script (POSIX / Bash)

show_menu() {
    clear
    echo "=============================================================================="
    echo "                 AIRSTRIKE – 2D SPACE SHOOTER LAUNCHER                        "
    echo "                 Academic Demonstration & Verification System                 "
    echo "=============================================================================="
    echo "  [1] Launch PSOOP Java Game Client (Swing / AWT)"
    echo "  [2] Launch PL C++ Data Structures & Algorithms Suite"
    echo "  [3] Launch COA 80386 32-bit Assembly Demonstrations"
    echo "  [4] Launch CGL OpenGL & GLUT Graphics Demonstrations"
    echo "  [5] Build & Verify All Modules"
    echo "  [6] View Environment Report"
    echo "  [7] Exit"
    echo "=============================================================================="
    read -p "Enter option [1-7]: " choice
    case $choice in
        1) run_java ;;
        2) run_pl ;;
        3) run_coa ;;
        4) run_cgl ;;
        5) run_build ;;
        6) view_env ;;
        7) exit 0 ;;
        *) echo "[ERROR] Invalid option."; sleep 1; show_menu ;;
    esac
}

run_java() {
    echo "[PSOOP] Launching Java Game Client..."
    if [ ! -f "bin/java/main/Main.class" ] && [ ! -f "bin/java/Main.class" ]; then
        echo "[NOTICE] Java binary not found in bin/java (Phase 1 Foundation state)."
    else
        java -cp bin/java main.Main
    fi
    read -p "Press Enter to return to menu..."
    show_menu
}

run_pl() {
    echo "[PL] Launching C++ Data Structures Runner..."
    if [ ! -f "bin/pl/pl_test_runner.exe" ] && [ ! -f "bin/pl/pl_test_runner" ]; then
        echo "[NOTICE] PL binary not found in bin/pl (Phase 1 Foundation state)."
    else
        ./bin/pl/pl_test_runner*
    fi
    read -p "Press Enter to return to menu..."
    show_menu
}

run_coa() {
    echo "[COA] Launching 80386 Assembly Demonstrations..."
    if [ ! -f "bin/coa/coa_runner.exe" ] && [ ! -f "bin/coa/coa_runner" ]; then
        echo "[NOTICE] COA binary not found in bin/coa (Phase 1 Foundation state)."
    else
        ./bin/coa/coa_runner*
    fi
    read -p "Press Enter to return to menu..."
    show_menu
}

run_cgl() {
    echo "[CGL] Launching OpenGL & GLUT Demonstrations..."
    if [ ! -f "bin/cgl/cgl_demo.exe" ] && [ ! -f "bin/cgl/cgl_demo" ]; then
        echo "[NOTICE] CGL binary not found in bin/cgl (Phase 1 Foundation state)."
    else
        ./bin/cgl/cgl_demo*
    fi
    read -p "Press Enter to return to menu..."
    show_menu
}

run_build() {
    echo "[BUILD] Running master build system..."
    if command -v powershell >/dev/null 2>&1; then
        powershell -ExecutionPolicy Bypass -File build_all.ps1
    elif [ -f "./build_all.bat" ]; then
        cmd.exe /c build_all.bat
    fi
    read -p "Press Enter to return to menu..."
    show_menu
}

view_env() {
    if [ -f "docs/setup/ENVIRONMENT_REPORT.md" ]; then
        cat docs/setup/ENVIRONMENT_REPORT.md
    else
        echo "[ERROR] docs/setup/ENVIRONMENT_REPORT.md not found."
    fi
    read -p "Press Enter to return to menu..."
    show_menu
}

# Parameter handling
case "$1" in
    java|psoop) run_java ;;
    pl) run_pl ;;
    coa) run_coa ;;
    cgl) run_cgl ;;
    build) run_build ;;
    *) show_menu ;;
esac

