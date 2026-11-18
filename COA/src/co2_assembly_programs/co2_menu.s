# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO2: MENU & MASTER DISPATCHER
# Interactive Submenu for 10 Fundamental 64-bit Assembly Language Programs
# Satisfies Course Outcome: COA-CO2
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co2_menu_hdr:
    .asciz "\n==============================================================================\n                 CO2: 64-BIT ASSEMBLY LANGUAGE PROGRAMS SUBMENU                \n==============================================================================\n  [1] Addition & Subtraction (Prog 1)\n  [2] Multiplication & Division with Remainder (Prog 2)\n  [3] Array Operations (Progs 3, 4, 7, 8, 9: Min/Max, Sum, Search, Reverse, Counts)\n  [4] Mathematical Series (Progs 5 & 6: Factorial & Fibonacci)\n  [5] Bitwise Logic Operations (Prog 10: AND, OR, XOR, NOT)\n  [6] Fibonacci Skimmer (COA-CO2: Assembly Math Series & Trajectory Generator)\n  [7] Run All 10 Programs Sequentially\n  [8] Return to Main Menu\n==============================================================================\n  Enter choice [1-8]: "

.text

# ==============================================================================
# Routine: co2_menu_loop
# - Small Meaning: Main execution dispatcher for all CO2 assembly programs.
# - Long Form    : CO2 Interactive Submenu Loop
# - Why We Used It: Routes user input to specific 64-bit assembly demonstration modules.
# ==============================================================================
.global co2_menu_loop
.global _co2_menu_loop
co2_menu_loop:
_co2_menu_loop:
    # --------------------------------------------------------------------------
    # PUSH RBP / MOV RBP, RSP / SUB RSP, 48:
    # - Small Meaning: Set up 64-bit frame and allocate ABI shadow space
    # - Long Form    : Base Pointer Setup and Shadow Space Reservation
    # - Why We Used It: Guarantees 16-byte stack alignment prior to child calls.
    # --------------------------------------------------------------------------
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx

.L_co2_loop:
    lea rcx, [rip + co2_menu_hdr]
    call print_str

    call read_int               # Reads 64-bit integer into RAX
    mov rbx, rax                # Store selection in RBX

    cmp rbx, 1
    je .L_co2_opt1
    cmp rbx, 2
    je .L_co2_opt2
    cmp rbx, 3
    je .L_co2_opt3
    cmp rbx, 4
    je .L_co2_opt4
    cmp rbx, 5
    je .L_co2_opt5
    cmp rbx, 6
    je .L_co2_opt6
    cmp rbx, 7
    je .L_co2_opt7
    cmp rbx, 8
    je .L_co2_exit
    cmp rbx, -1
    je .L_co2_exit
    jmp .L_co2_loop

.L_co2_opt1:
    call co2_demo_arithmetic
    jmp .L_co2_loop

.L_co2_opt2:
    call co2_demo_arithmetic
    jmp .L_co2_loop

.L_co2_opt3:
    call co2_demo_arrays
    jmp .L_co2_loop

.L_co2_opt4:
    call co2_demo_math
    jmp .L_co2_loop

.L_co2_opt5:
    call co2_demo_logic
    jmp .L_co2_loop

.L_co2_opt6:
    call coa_demo_fibonacci_skimmer
    jmp .L_co2_loop

.L_co2_opt7:
    call co2_run_all
    jmp .L_co2_loop

.L_co2_exit:
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co2_run_all
# - Small Meaning: Runs all CO2 programs sequentially in batch automated mode.
# - Long Form    : CO2 Run All Programs Automated Verifier
# - Why We Used It: Powers automated test suites and complete course outcome viva demos.
# ==============================================================================
.global co2_run_all
.global _co2_run_all
co2_run_all:
_co2_run_all:
    push rbp
    mov rbp, rsp
    sub rsp, 48

    call co2_demo_arithmetic
    call co2_demo_arrays
    call co2_demo_math
    call co2_demo_logic
    call coa_demo_fibonacci_skimmer

    mov rsp, rbp
    pop rbp
    ret
