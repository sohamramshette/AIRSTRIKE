# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO4: PIPELINED RAILGUN DREADNOUGHT
# Demonstrates 5-Stage Instruction Pipelining (IF, ID, EX, MEM, WB),
# Read-After-Write (RAW) Data Hazards, Stall Bubbles, and Forwarding Dynamics.
# Satisfies Course Outcome: COA-CO4 (Applied Pipeline Combat Execution)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
railgun_title:
    .asciz "\n==============================================================================\n   COA-CO4: PIPELINED RAILGUN DREADNOUGHT - 5-STAGE INSTRUCTION PIPELINE (x64) \n==============================================================================\n  [Educational Model: Classic 5-Stage Execution (IF, ID, EX, MEM, WB)]\n  [Notice: Educational model illustrating hazards; not actual microarchitecture]\n"

railgun_firing_seq:
    .asciz "\n--- RAILGUN VOLLEY INSTRUCTION SEQUENCE ---\n  I1: CHARGE_CELL  RAX, RDX      (Writes capacitor energy to RAX in WB stage)\n  I2: AIM_MAGNETIC RBX, RAX      (Requires energy reading RAX in ID stage)\n  I3: IONIZE_GAS   RCX, 100      (Independent plasma ionization)\n  I4: DISCHARGE    RSI, RBX      (Requires magnetic vector in ID stage)\n"

railgun_ideal_hdr:
    .asciz "\n[CASE 1: IDEAL PIPELINE EXECUTION - NO HAZARDS (8 CYCLES)]\n  Inst \\ Cycle | C1  | C2  | C3  | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  I1: CHARGE   | IF  | ID  | EX  | MEM | WB  |     |     |     |\n  I2: AIM      |     | IF  | ID  | EX  | MEM | WB  |     |     |\n  I3: IONIZE   |     |     | IF  | ID  | EX  | MEM | WB  |     |\n  I4: DISCHARGE|     |     |     | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  Total Volley Latency: 8 Cycles (T_seq = 20 Cycles -> Speedup: 2.50x)\n"

railgun_raw_hdr:
    .asciz "\n[CASE 2: RAW DATA HAZARD WITH STALL BUBBLES - NO FORWARDING (10 CYCLES)]\n  Hazard: I2 requires RAX at Cycle 3 (ID), but I1 does not write RAX until Cycle 5 (WB)!\n\n  Inst \\ Cycle | C1  | C2  | C3    | C4    | C5  | C6  | C7  | C8  | C9  | C10 |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  I1: CHARGE   | IF  | ID  | EX    | MEM   | WB  |     |     |     |     |     |\n  I2: AIM      |     | IF  | STALL | STALL | ID  | EX  | MEM | WB  |     |     |\n  I3: IONIZE   |     |     | STALL | STALL | IF  | ID  | EX  | MEM | WB  |     |\n  I4: DISCHARGE|     |     |   -   |   -   |     | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  Stall Penalty: 2 Bubble Cycles inserted -> Realized Volley Latency: 10 Cycles\n"

railgun_fwd_hdr:
    .asciz "\n[CASE 3: OPERAND FORWARDING (EX/MEM -> ID/EX BYPASS - 8 CYCLES)]\n  Forwarding: Internal bypass wire routes ALU output directly from EX/MEM to EX.\n  I2 receives forwarded data without pipeline stalling!\n  Volley Latency Restored: 8 Cycles (Zero Stall Bubbles)\n==============================================================================\n"

.text

# ==============================================================================
# Routine: coa_railgun_pipeline_eval
# - Small Meaning: Computes cycle metrics and stall bubbles for Railgun salvo.
# - Long Form    : COA Railgun Pipeline Latency and Hazard Evaluator
# - Why We Used It: Simulates weapon charging throughput with and without forwarding hardware.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t  numShots   (Number of projectile instructions, N)
#   RDX = int64_t  hazardMode (0 = Ideal, 1 = RAW with Stalls, 2 = RAW with Forwarding)
#   R8  = int64_t* outCycles  (Pointer to store total latency cycles)
#   R9  = int64_t* outStalls  (Pointer to store stall bubbles count)
# Returns:
#   RAX = Total Execution Cycles
# ==============================================================================
.global coa_railgun_pipeline_eval
.global _coa_railgun_pipeline_eval
coa_railgun_pipeline_eval:
_coa_railgun_pipeline_eval:
    push rbp
    mov rbp, rsp
    push rbx
    push rsi
    push rdi
    push r12
    push r13

    mov r12, r8                 # R12 = outCycles pointer
    mov r13, r9                 # R13 = outStalls pointer

    cmp rcx, 1
    jge .L_n_valid
    mov rcx, 1                  # Clamp minimum shot count to 1

.L_n_valid:
    # --------------------------------------------------------------------------
    # Base Ideal Latency Formula: T_ideal = 5 + N - 1 = N + 4
    # --------------------------------------------------------------------------
    lea rax, [rcx + 4]          # RAX = 5 + N - 1

    mov rbx, rdx                # RBX = hazardMode
    xor rdx, rdx                # RDX = stall bubbles count = 0

    cmp rbx, 1
    jne .L_check_fwd

.L_mode_stall:
    # --------------------------------------------------------------------------
    # Mode 1: RAW Data Hazard requires 2 stall bubbles
    # --------------------------------------------------------------------------
    mov rdx, 2                  # 2 stall bubble cycles
    add rax, 2                  # Total latency += 2
    jmp .L_calc_done

.L_check_fwd:
    cmp rbx, 2
    jne .L_calc_done

.L_mode_fwd:
    # --------------------------------------------------------------------------
    # Mode 2: Forwarding hardware bypasses stalls (0 bubbles)
    # --------------------------------------------------------------------------
    xor rdx, rdx

.L_calc_done:
    # Store results via output pointers
    test r12, r12
    jz .L_no_cycles
    mov [r12], rax

.L_no_cycles:
    test r13, r13
    jz .L_no_stalls
    mov [r13], rdx

.L_no_stalls:
    pop r13
    pop r12
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: coa_demo_pipelined_railgun
# - Small Meaning: Interactive demonstration of Railgun Dreadnought pipeline.
# - Long Form    : CO4 Demonstration Driver for Pipelined Railgun
# - Why We Used It: Displays visual ASCII timing diagrams for ideal, stall, and bypass cases.
# ==============================================================================
.global coa_demo_pipelined_railgun
.global _coa_demo_pipelined_railgun
coa_demo_pipelined_railgun:
_coa_demo_pipelined_railgun:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + railgun_title]
    call print_str

    lea rcx, [rip + railgun_firing_seq]
    call print_str

    lea rcx, [rip + railgun_ideal_hdr]
    call print_str

    lea rcx, [rip + railgun_raw_hdr]
    call print_str

    lea rcx, [rip + railgun_fwd_hdr]
    call print_str

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
