# ==============================================================================
# File    : COA/src/co2_assembly_programs/co2_menu.s
# Subject : COA-CO2 | Assembly Language Programs Submenu & Batch Runner
# Topics  : Arithmetic, Arrays, Factorial, Fibonacci, Bitwise Logic, Skimmer
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co2_menu_hdr:
    .asciz "\n==============================================================================\n                 CO2: x86-64 ASSEMBLY LANGUAGE PROGRAMS SUBMENU               \n==============================================================================\n  [1] Addition & Subtraction (Prog 1)\n  [2] Multiplication & Division with Remainder (Prog 2)\n  [3] Array Operations (Progs 3, 4, 7, 8, 9: Min/Max, Sum, Search, Reverse, Count)\n  [4] Mathematical Series (Progs 5 & 6: Recursive Factorial & Fibonacci)\n  [5] Bitwise Logic Operations (Prog 10: AND, OR, XOR, NOT)\n  [6] Fibonacci Skimmer (COA-CO2 Enemy Demo)\n  [7] Run All 10 Programs Sequentially\n  [8] Return to Main Menu\n==============================================================================\n  Enter choice [1-8]: "

.text

# ------------------------------------------------------------------------------
# [TOPIC: CO2 Interactive Submenu] co2_menu_loop
# Purpose   : Dispatches user selection across all 10 CO2 programs and enemy demo.
# Game Role : Interactive testbench for CO2 math, arrays, bitmasks, and Skimmer.
# ------------------------------------------------------------------------------
.global co2_menu_loop
.global _co2_menu_loop
co2_menu_loop:
_co2_menu_loop:
    push rbp
    mov  rbp, rsp
    push rbx
    sub  rsp, 40                # WHY: 1 push (8) + 40 = 48 -> keeps stack 16-byte aligned

.L_co2_loop:
    lea  rcx, [rip + co2_menu_hdr]
    call print_str

    call read_int
    mov  rbx, rax               # WHY: Store selection in callee-saved RBX before branching

    cmp  rbx, 1
    je   .L_co2_opt1
    cmp  rbx, 2
    je   .L_co2_opt2
    cmp  rbx, 3
    je   .L_co2_opt3
    cmp  rbx, 4
    je   .L_co2_opt4
    cmp  rbx, 5
    je   .L_co2_opt5
    cmp  rbx, 6
    je   .L_co2_opt6
    cmp  rbx, 7
    je   .L_co2_opt7
    cmp  rbx, 8
    je   .L_co2_exit
    cmp  rbx, -1                # WHY: Exit loop cleanly on EOF
    je   .L_co2_exit
    jmp  .L_co2_loop

.L_co2_opt1:
    call co2_demo_add_sub
    jmp  .L_co2_loop
.L_co2_opt2:
    call co2_demo_mul_div
    jmp  .L_co2_loop
.L_co2_opt3:
    call co2_demo_arrays
    jmp  .L_co2_loop
.L_co2_opt4:
    call co2_demo_math
    jmp  .L_co2_loop
.L_co2_opt5:
    call co2_demo_logic
    jmp  .L_co2_loop
.L_co2_opt6:
    call coa_demo_fibonacci_skimmer
    jmp  .L_co2_loop
.L_co2_opt7:
    call co2_run_all
    jmp  .L_co2_loop

.L_co2_exit:
    add  rsp, 40
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO2 Batch Runner] co2_run_all
# Purpose   : Runs all 10 CO2 assembly programs non-interactively in sequence.
# Game Role : Executes batch demos without blocking on keyboard input.
# ------------------------------------------------------------------------------
.global co2_run_all
.global _co2_run_all
co2_run_all:
_co2_run_all:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    call co2_demo_arithmetic
    call co2_demo_arrays_batch  # WHY: Batch variant uses preset key (88) so automated runs don't stall
    call co2_demo_math_batch    # WHY: Batch variant runs preset 10! and 10 Fibonacci terms
    call co2_demo_logic
    call coa_demo_fibonacci_skimmer

    add  rsp, 32
    pop  rbp
    ret
