# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO4: Menu & Master Dispatcher
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
co4_menu_hdr:
    .asciz "\n==============================================================================\n        CO4: CONTROL UNIT & INSTRUCTION PIPELINING SUBMENU                    \n==============================================================================\n  [1] Control Unit Architecture & Control Signal Generation\n  [2] 5-Stage Instruction Pipeline Simulation (Data & Branch Hazards)\n  [3] Pipelined Railgun Dreadnought (COA-CO4: 5-Stage Pipeline, RAW Hazard Stalls & Bypass)\n  [4] Run All CO4 Demonstrations Sequentially\n  [5] Return to Main Menu\n==============================================================================\n  Enter choice [1-5]: "

.text

# ------------------------------------------------------------------------------
# co4_menu_loop: Interactive submenu for CO4
# ------------------------------------------------------------------------------
.global _co4_menu_loop
_co4_menu_loop:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

.L_co4_loop:
    push offset co4_menu_hdr
    call _print_str
    add esp, 4

    call _read_int
    mov ebx, eax

    cmp ebx, 1
    je .L_co4_opt1
    cmp ebx, 2
    je .L_co4_opt2
    cmp ebx, 3
    je .L_co4_opt3
    cmp ebx, 4
    je .L_co4_opt4
    cmp ebx, 5
    je .L_co4_exit
    cmp ebx, -1
    je .L_co4_exit
    jmp .L_co4_loop

.L_co4_opt1:
    call _co4_demo_control_unit
    jmp .L_co4_loop

.L_co4_opt2:
    call _co4_demo_pipeline
    jmp .L_co4_loop

.L_co4_opt3:
    call _coa_demo_pipelined_railgun
    jmp .L_co4_loop

.L_co4_opt4:
    call _co4_run_all
    jmp .L_co4_loop

.L_co4_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co4_run_all: Runs both CO4 demonstrations
# ------------------------------------------------------------------------------
.global _co4_run_all
_co4_run_all:
    push ebp
    mov ebp, esp

    call _co4_demo_control_unit
    call _co4_demo_pipeline

    mov esp, ebp
    pop ebp
    ret
