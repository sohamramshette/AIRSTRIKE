# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO3: Menu & Master Dispatcher
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
co3_menu_hdr:
    .asciz "\n==============================================================================\n            CO3: COMPUTER ARITHMETIC ALGORITHMS SUBMENU                       \n==============================================================================\n  [1] Booth's Signed Binary Multiplication (Step-by-Step Table)\n  [2] Restoring Binary Division (Step-by-Step Table)\n  [3] Multi-Precision Carry (ADC) & Signed Overflow (OF Flag) Demo\n  [4] Run All CO3 Demonstrations Sequentially\n  [5] Return to Main Menu\n==============================================================================\n  Enter choice [1-5]: "

.text

# ------------------------------------------------------------------------------
# co3_menu_loop: Interactive submenu for CO3
# ------------------------------------------------------------------------------
.global _co3_menu_loop
_co3_menu_loop:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

.L_co3_loop:
    push offset co3_menu_hdr
    call _print_str
    add esp, 4

    call _read_int
    mov ebx, eax

    cmp ebx, 1
    je .L_co3_opt1
    cmp ebx, 2
    je .L_co3_opt2
    cmp ebx, 3
    je .L_co3_opt3
    cmp ebx, 4
    je .L_co3_opt4
    cmp ebx, 5
    je .L_co3_exit
    cmp ebx, -1
    je .L_co3_exit
    jmp .L_co3_loop

.L_co3_opt1:
    call _co3_demo_booth
    jmp .L_co3_loop

.L_co3_opt2:
    call _co3_demo_restoring_div
    jmp .L_co3_loop

.L_co3_opt3:
    call _co3_demo_carry_overflow
    jmp .L_co3_loop

.L_co3_opt4:
    call _co3_run_all
    jmp .L_co3_loop

.L_co3_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co3_run_all: Runs Booth, Restoring Div, and Carry/Overflow sequentially
# ------------------------------------------------------------------------------
.global _co3_run_all
_co3_run_all:
    push ebp
    mov ebp, esp

    # Preset Booth demo: 7 * -3 = -21
    push 1                      # verbose = 1
    push -3                     # Q
    push 7                      # M
    call _asm_booth_multiply
    add esp, 12

    # Preset Restoring Div demo: 23 / 5 = 4 rem 3
    push 0                      # dummy out_rem (not needed in demo)
    push 1                      # verbose = 1
    push 0
    push 5                      # Divisor
    push 23                     # Dividend
    call _asm_restoring_divide
    add esp, 20

    # Carry & Overflow demo
    call _co3_demo_carry_overflow

    mov esp, ebp
    pop ebp
    ret
