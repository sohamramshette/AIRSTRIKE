# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO4: MENU & MASTER DISPATCHER
# Interactive Submenu for Control Unit & Instruction Pipelining
# Satisfies Course Outcome: COA-CO4
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co4_menu_hdr:
    .asciz "\n==============================================================================\n        CO4: CONTROL UNIT & INSTRUCTION PIPELINING SUBMENU (64-BIT)           \n==============================================================================\n  [1] Control Unit Architecture & Control Signal Generation\n  [2] 5-Stage Instruction Pipeline Simulation (Data & Branch Hazards)\n  [3] Pipelined Railgun Dreadnought (COA-CO4: 5-Stage Pipeline, RAW Hazard Stalls & Bypass)\n  [4] Run All CO4 Demonstrations Sequentially\n  [5] Return to Main Menu\n==============================================================================\n  Enter choice [1-5]: "

.text

# ==============================================================================
# Routine: co4_menu_loop
# - Small Meaning: Interactive dispatcher for CO4 control unit and pipelining modules.
# - Long Form    : CO4 Interactive Submenu Loop
# - Why We Used It: Routes console selections to Control Unit, Pipeline Sim, and Railgun.
# ==============================================================================
.global co4_menu_loop
.global _co4_menu_loop
co4_menu_loop:
_co4_menu_loop:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx

.L_co4_loop:
    lea rcx, [rip + co4_menu_hdr]
    call print_str

    call read_int
    mov rbx, rax

    cmp rbx, 1
    je .L_co4_opt1
    cmp rbx, 2
    je .L_co4_opt2
    cmp rbx, 3
    je .L_co4_opt3
    cmp rbx, 4
    je .L_co4_opt4
    cmp rbx, 5
    je .L_co4_exit
    cmp rbx, -1
    je .L_co4_exit
    jmp .L_co4_loop

.L_co4_opt1:
    call co4_demo_control_unit
    jmp .L_co4_loop

.L_co4_opt2:
    call co4_demo_pipeline
    jmp .L_co4_loop

.L_co4_opt3:
    call coa_demo_pipelined_railgun
    jmp .L_co4_loop

.L_co4_opt4:
    call co4_run_all
    jmp .L_co4_loop

.L_co4_exit:
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co4_run_all
# - Small Meaning: Sequentially executes all CO4 demonstrations.
# - Long Form    : CO4 Run All Pipeline Demonstrations Batch Driver
# - Why We Used It: Verifies control signals, pipeline hazard metrics, and combat railgun.
# ==============================================================================
.global co4_run_all
.global _co4_run_all
co4_run_all:
_co4_run_all:
    push rbp
    mov rbp, rsp
    sub rsp, 48

    call co4_demo_control_unit
    call co4_demo_pipeline
    call coa_demo_pipelined_railgun

    mov rsp, rbp
    pop rbp
    ret
