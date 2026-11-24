# ==============================================================================
# File    : COA/src/co4_control_unit/co4_menu.s
# Subject : COA-CO4 | Control Unit & Instruction Pipelining Submenu
# Topics  : Control Unit, Control Signals, 5-Stage Pipeline, Hazards, Railgun
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co4_menu_hdr:
    .asciz "\n==============================================================================\n        CO4: CONTROL UNIT & INSTRUCTION PIPELINING SUBMENU                    \n==============================================================================\n  [1] Control Unit Architecture & Control Signal Matrix\n  [2] 5-Stage Instruction Pipeline Simulation (Data & Branch Hazards)\n  [3] Pipelined Railgun Dreadnought (COA-CO4 Enemy Demo)\n  [4] Run All CO4 Demonstrations\n  [5] Return to Main Menu\n==============================================================================\n  Enter choice [1-5]: "

.text

# ------------------------------------------------------------------------------
# [TOPIC: CO4 Interactive Submenu] co4_menu_loop
# Purpose   : Dispatches user selection across CO4 Control Unit and Pipeline demos.
# Game Role : Interactive testbench for CPU control signals, hazards, and Railgun.
# ------------------------------------------------------------------------------
.global co4_menu_loop
.global _co4_menu_loop
co4_menu_loop:
_co4_menu_loop:
    push rbp
    mov  rbp, rsp
    push rbx
    sub  rsp, 40

.L_co4_loop:
    lea  rcx, [rip + co4_menu_hdr]
    call print_str

    call read_int
    mov  rbx, rax               # WHY: Preserve choice across dispatch calls

    cmp  rbx, 1
    je   .L_co4_opt1
    cmp  rbx, 2
    je   .L_co4_opt2
    cmp  rbx, 3
    je   .L_co4_opt3
    cmp  rbx, 4
    je   .L_co4_opt4
    cmp  rbx, 5
    je   .L_co4_exit
    cmp  rbx, -1                # WHY: Exit loop cleanly on EOF
    je   .L_co4_exit
    jmp  .L_co4_loop

.L_co4_opt1:
    call co4_demo_control_unit
    jmp  .L_co4_loop
.L_co4_opt2:
    call co4_demo_pipeline
    jmp  .L_co4_loop
.L_co4_opt3:
    call coa_demo_pipelined_railgun
    jmp  .L_co4_loop
.L_co4_opt4:
    call co4_run_all
    jmp  .L_co4_loop

.L_co4_exit:
    add  rsp, 40
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO4 Batch Runner] co4_run_all
# Purpose   : Runs all CO4 demonstrations sequentially.
# Game Role : Verifies Control Unit decoding, pipeline hazards, and Railgun Dreadnought.
# ------------------------------------------------------------------------------
.global co4_run_all
.global _co4_run_all
co4_run_all:
_co4_run_all:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    call co4_demo_control_unit
    call co4_demo_pipeline
    call coa_demo_pipelined_railgun

    add  rsp, 32
    pop  rbp
    ret
