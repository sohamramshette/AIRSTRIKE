# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO3: MENU & MASTER DISPATCHER
# Interactive Submenu for Computer Arithmetic Algorithms
# Satisfies Course Outcome: COA-CO3
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co3_menu_hdr:
    .asciz "\n==============================================================================\n            CO3: COMPUTER ARITHMETIC ALGORITHMS SUBMENU (64-BIT)              \n==============================================================================\n  [1] Booth's Signed Binary Multiplication (Step-by-Step Table)\n  [2] Restoring Binary Division (Step-by-Step Table)\n  [3] 128-Bit Multi-Precision Carry (ADC) & Signed Overflow (OF Flag) Demo\n  [4] Booth Multiplier Gunship (COA-CO3: Signed Multiplication & Saturation)\n  [5] Run All CO3 Demonstrations Sequentially\n  [6] Return to Main Menu\n==============================================================================\n  Enter choice [1-6]: "

.text

# ==============================================================================
# Routine: co3_menu_loop
# - Small Meaning: Interactive dispatcher for CO3 arithmetic algorithm modules.
# - Long Form    : CO3 Interactive Submenu Loop
# - Why We Used It: Routes console selections to Booth, Restoring, and Overflow routines.
# ==============================================================================
.global co3_menu_loop
.global _co3_menu_loop
co3_menu_loop:
_co3_menu_loop:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx

.L_co3_loop:
    lea rcx, [rip + co3_menu_hdr]
    call print_str

    call read_int
    mov rbx, rax

    cmp rbx, 1
    je .L_co3_opt1
    cmp rbx, 2
    je .L_co3_opt2
    cmp rbx, 3
    je .L_co3_opt3
    cmp rbx, 4
    je .L_co3_opt4
    cmp rbx, 5
    je .L_co3_opt5
    cmp rbx, 6
    je .L_co3_exit
    cmp rbx, -1
    je .L_co3_exit
    jmp .L_co3_loop

.L_co3_opt1:
    call co3_demo_booth
    jmp .L_co3_loop

.L_co3_opt2:
    call co3_demo_restoring_div
    jmp .L_co3_loop

.L_co3_opt3:
    call co3_demo_carry_overflow
    jmp .L_co3_loop

.L_co3_opt4:
    call coa_demo_booth_gunship
    jmp .L_co3_loop

.L_co3_opt5:
    call co3_run_all
    jmp .L_co3_loop

.L_co3_exit:
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co3_run_all
# - Small Meaning: Sequentially executes all CO3 demonstrations.
# - Long Form    : CO3 Run All Arithmetic Demonstrations Batch Driver
# - Why We Used It: Verifies all arithmetic algorithm implementations in single test pass.
# ==============================================================================
.global co3_run_all
.global _co3_run_all
co3_run_all:
_co3_run_all:
    push rbp
    mov rbp, rsp
    sub rsp, 48

    # Run default Booth: (-7 * 5)
    mov rcx, -7
    mov rdx, 5
    mov r8,  1
    call asm_booth_multiply

    # Run default Restoring Division: (14 / 3)
    mov rcx, 14
    mov rdx, 3
    lea r8,  [rip + div_out_rem]
    mov r9,  1
    call asm_restoring_divide

    call co3_demo_carry_overflow
    call coa_demo_booth_gunship

    mov rsp, rbp
    pop rbp
    ret
