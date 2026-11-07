# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO2: Menu & Master Dispatcher
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
co2_menu_hdr:
    .asciz "\n==============================================================================\n                 CO2: 80386 ASSEMBLY LANGUAGE PROGRAMS SUBMENU                 \n==============================================================================\n  [1] Addition & Subtraction (Prog 1)\n  [2] Multiplication & Division with Remainder (Prog 2)\n  [3] Array Operations (Progs 3, 4, 7, 8, 9: Min/Max, Sum, Search, Reverse, Counts)\n  [4] Mathematical Series (Progs 5 & 6: Factorial & Fibonacci)\n  [5] Bitwise Logic Operations (Prog 10: AND, OR, XOR, NOT)\n  [6] Fibonacci Skimmer (COA-CO2: Assembly Math Series & Trajectory Generator)\n  [7] Run All 10 Programs Sequentially\n  [8] Return to Main Menu\n==============================================================================\n  Enter choice [1-8]: "

.text

# ------------------------------------------------------------------------------
# co2_menu_loop: Interactive submenu for CO2
# ------------------------------------------------------------------------------
.global _co2_menu_loop
_co2_menu_loop:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

.L_co2_loop:
    push offset co2_menu_hdr
    call _print_str
    add esp, 4

    call _read_int
    mov ebx, eax

    cmp ebx, 1
    je .L_co2_opt1
    cmp ebx, 2
    je .L_co2_opt2
    cmp ebx, 3
    je .L_co2_opt3
    cmp ebx, 4
    je .L_co2_opt4
    cmp ebx, 5
    je .L_co2_opt5
    cmp ebx, 6
    je .L_co2_opt6
    cmp ebx, 7
    je .L_co2_opt7
    cmp ebx, 8
    je .L_co2_exit
    cmp ebx, -1
    je .L_co2_exit
    jmp .L_co2_loop

.L_co2_opt1:
    call _co2_demo_arithmetic
    jmp .L_co2_loop

.L_co2_opt2:
    call _co2_demo_arithmetic
    jmp .L_co2_loop

.L_co2_opt3:
    call _co2_demo_arrays
    jmp .L_co2_loop

.L_co2_opt4:
    call _co2_demo_math
    jmp .L_co2_loop

.L_co2_opt5:
    call _co2_demo_logic
    jmp .L_co2_loop

.L_co2_opt6:
    call _coa_demo_fibonacci_skimmer
    jmp .L_co2_loop

.L_co2_opt7:
    call _co2_run_all
    jmp .L_co2_loop

.L_co2_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co2_run_all: Runs all 10 CO2 programs sequentially
# ------------------------------------------------------------------------------
.global _co2_run_all
_co2_run_all:
    push ebp
    mov ebp, esp

    call _co2_demo_arithmetic
    call _co2_demo_arrays
    call _co2_demo_math
    call _co2_demo_logic

    mov esp, ebp
    pop ebp
    ret
