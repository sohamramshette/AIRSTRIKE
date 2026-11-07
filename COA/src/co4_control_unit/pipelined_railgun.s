# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO4: Pipelined Railgun Dreadnought
# Demonstrates 5-Stage Instruction Pipelining (IF, ID, EX, MEM, WB),
# Read-After-Write (RAW) Data Hazards, Stall Bubbles, and Forwarding Dynamics.
#
# [ACADEMIC ARCHITECTURAL NOTICE]:
# This is an educational pipeline simulation designed to teach classic 5-stage RISC
# execution dynamics, hazards, and speedup metrics. It is an educational instructional
# model and does not claim to represent the complex microcode-driven, non-RISC prefetch
# queue and bus unit execution architecture of the physical Intel 80386 processor.
#
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit Protected Mode, cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
railgun_title:
    .asciz "\n==============================================================================\n      COA-CO4: PIPELINED RAILGUN DREADNOUGHT - 5-STAGE INSTRUCTION PIPELINE   \n==============================================================================\n  [Educational Model: Classic 5-Stage Execution (IF, ID, EX, MEM, WB)]\n  [Notice: Educational model illustrating hazards; not actual 80386 microarchitecture]\n"

railgun_firing_seq:
    .asciz "\n--- RAILGUN VOLLEY INSTRUCTION SEQUENCE ---\n  I1: CHARGE_CELL  EAX, EDX      (Writes capacitor energy to EAX in WB stage)\n  I2: AIM_MAGNETIC EBX, EAX      (Requires energy reading EAX in ID stage)\n  I3: IONIZE_GAS   ECX, 100      (Independent plasma ionization)\n  I4: DISCHARGE    ESI, EBX      (Requires magnetic vector in ID stage)\n"

railgun_ideal_hdr:
    .asciz "\n[CASE 1: IDEAL PIPELINE EXECUTION - NO HAZARDS (8 CYCLES)]\n  Inst \\ Cycle | C1  | C2  | C3  | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  I1: CHARGE   | IF  | ID  | EX  | MEM | WB  |     |     |     |\n  I2: AIM      |     | IF  | ID  | EX  | MEM | WB  |     |     |\n  I3: IONIZE   |     |     | IF  | ID  | EX  | MEM | WB  |     |\n  I4: DISCHARGE|     |     |     | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  Total Volley Latency: 8 Cycles (T_seq = 20 Cycles -> Speedup: 2.50x)\n"

railgun_raw_hdr:
    .asciz "\n[CASE 2: RAW DATA HAZARD WITH STALL BUBBLES - NO FORWARDING (10 CYCLES)]\n  Hazard: I2 requires EAX at Cycle 3 (ID), but I1 does not write EAX until Cycle 5 (WB)!\n\n  Inst \\ Cycle | C1  | C2  | C3    | C4    | C5  | C6  | C7  | C8  | C9  | C10 |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  I1: CHARGE   | IF  | ID  | EX    | MEM   | WB  |     |     |     |     |     |\n  I2: AIM      |     | IF  | STALL | STALL | ID  | EX  | MEM | WB  |     |     |\n  I3: IONIZE   |     |     | STALL | STALL | IF  | ID  | EX  | MEM | WB  |     |\n  I4: DISCHARGE|     |     |   -   |   -   |     | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  Stall Penalty: 2 Bubble Cycles inserted -> Realized Volley Latency: 10 Cycles\n"

railgun_fwd_hdr:
    .asciz "\n[CASE 3: OPERAND FORWARDING (EX/MEM -> ID/EX BYPASS - 8 CYCLES)]\n  Forwarding: Internal bypass wire routes ALU output directly from EX/MEM to EX.\n  I2 receives forward data without pipeline stalling!\n  Volley Latency Restored: 8 Cycles (Zero Stall Bubbles)\n==============================================================================\n"

.text

# ------------------------------------------------------------------------------
# coa_railgun_pipeline_eval: Computes cycle metrics for Railgun firing sequence
# Prototype: int coa_railgun_pipeline_eval(int numShots, int hazardMode, int* outCycles, int* outStalls)
# Parameters:
#   [ebp + 8]  = numShots (e.g. 4)
#   [ebp + 12] = hazardMode (0 = Ideal, 1 = RAW with Stalls, 2 = RAW with Forwarding)
#   [ebp + 16] = outCycles (pointer to int)
#   [ebp + 20] = outStalls (pointer to int)
# Returns:
#   EAX = Total Execution Cycles
# ------------------------------------------------------------------------------
.global _coa_railgun_pipeline_eval
_coa_railgun_pipeline_eval:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov ecx, [ebp + 8]          # ecx = numShots (N)
    cmp ecx, 1
    jge .L_n_valid
    mov ecx, 1

.L_n_valid:
    # Base Ideal Latency: 5 + N - 1
    mov eax, ecx
    add eax, 4                  # eax = 5 + N - 1

    mov ebx, [ebp + 12]         # ebx = hazardMode
    xor edx, edx                # edx = stall bubbles count

    cmp ebx, 1
    jne .L_check_fwd

.L_mode_stall:
    # Mode 1: RAW Data Hazard requires 2 stall bubbles
    mov edx, 2
    add eax, 2
    jmp .L_calc_done

.L_check_fwd:
    cmp ebx, 2
    jne .L_calc_done

.L_mode_fwd:
    # Mode 2: Forwarding eliminates stalls (0 stalls)
    mov edx, 0

.L_calc_done:
    # Store into output pointers
    mov edi, [ebp + 16]
    test edi, edi
    jz .L_no_cycles
    mov [edi], eax

.L_no_cycles:
    mov edi, [ebp + 20]
    test edi, edi
    jz .L_no_stalls
    mov [edi], edx

.L_no_stalls:
    # Return total cycles in EAX
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_demo_pipelined_railgun: Interactive demonstration of Railgun Pipeline
# ------------------------------------------------------------------------------
.global _coa_demo_pipelined_railgun
_coa_demo_pipelined_railgun:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset railgun_title
    call _print_str
    add esp, 4

    push offset railgun_firing_seq
    call _print_str
    add esp, 4

    push offset railgun_ideal_hdr
    call _print_str
    add esp, 4

    push offset railgun_raw_hdr
    call _print_str
    add esp, 4

    push offset railgun_fwd_hdr
    call _print_str
    add esp, 4

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
