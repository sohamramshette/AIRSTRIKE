# ==============================================================================
# File    : COA/src/co3_arithmetic/co3_menu.s
# Subject : COA-CO3 | Computer Arithmetic Algorithms Submenu & Batch Runner
# Topics  : Booth's Signed Multiplication, Restoring Division, Carry (ADC),
#           Signed Overflow (OF), Booth Multiplier Gunship Enemy
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co3_menu_hdr:
    .asciz "\n==============================================================================\n               CO3: COMPUTER ARITHMETIC ALGORITHMS SUBMENU                    \n==============================================================================\n  [1] Booth's Signed Binary Multiplication (Cycle-by-Cycle Table)\n  [2] Restoring Binary Division (Cycle-by-Cycle Table)\n  [3] 128-Bit Addition with Carry (ADC) & Signed Overflow (OF)\n  [4] Booth Multiplier Gunship (COA-CO3 Enemy Demo)\n  [5] Run All CO3 Demonstrations\n  [6] Return to Main Menu\n==============================================================================\n  Enter choice [1-6]: "

.text

# ------------------------------------------------------------------------------
# [TOPIC: CO3 Interactive Submenu] co3_menu_loop
# Purpose   : Dispatches user selection across all CO3 arithmetic demonstrations.
# Game Role : Interactive testbench for Booth, Restoring Division, Carry/OF, and Gunship.
# ------------------------------------------------------------------------------
.global co3_menu_loop
.global _co3_menu_loop
co3_menu_loop:
_co3_menu_loop:
    push rbp
    mov  rbp, rsp
    push rbx
    sub  rsp, 40

.L_co3_loop:
    lea  rcx, [rip + co3_menu_hdr]
    call print_str

    call read_int
    mov  rbx, rax               # WHY: Store menu choice in callee-saved RBX before branching

    cmp  rbx, 1
    je   .L_co3_opt1
    cmp  rbx, 2
    je   .L_co3_opt2
    cmp  rbx, 3
    je   .L_co3_opt3
    cmp  rbx, 4
    je   .L_co3_opt4
    cmp  rbx, 5
    je   .L_co3_opt5
    cmp  rbx, 6
    je   .L_co3_exit
    cmp  rbx, -1                # WHY: Exit cleanly if stdin reaches EOF
    je   .L_co3_exit
    jmp  .L_co3_loop

.L_co3_opt1:
    call co3_demo_booth
    jmp  .L_co3_loop
.L_co3_opt2:
    call co3_demo_restoring_div
    jmp  .L_co3_loop
.L_co3_opt3:
    call co3_demo_carry_overflow
    jmp  .L_co3_loop
.L_co3_opt4:
    call coa_demo_booth_gunship
    jmp  .L_co3_loop
.L_co3_opt5:
    call co3_run_all
    jmp  .L_co3_loop

.L_co3_exit:
    add  rsp, 40
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO3 Batch Runner] co3_run_all
# Purpose   : Runs all CO3 demonstrations non-interactively in sequence.
# Game Role : Verifies signed Booth trace, Restoring Div trace, ADC/OF, and Gunship.
# ------------------------------------------------------------------------------
.global co3_run_all
.global _co3_run_all
co3_run_all:
_co3_run_all:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    mov  rcx, -7                # WHY: Run preset (-7 * 5) with verbose=1 so negative multiplicand handling is shown
    mov  rdx, 5
    mov  r8,  1
    call asm_booth_multiply

    mov  rcx, 14                # WHY: Run preset (14 / 3) with verbose=1 to show restoring steps & remainder
    mov  rdx, 3
    lea  r8,  [rip + div_out_rem]
    mov  r9,  1
    call asm_restoring_divide

    call co3_demo_carry_overflow
    call coa_demo_booth_gunship

    add  rsp, 32
    pop  rbp
    ret
