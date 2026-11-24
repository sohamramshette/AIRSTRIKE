# ==============================================================================
# File    : COA/src/co4_control_unit/pipeline_sim.s
# Subject : COA-CO4 | 5-Stage Instruction Pipeline Simulator & Hazard Analysis
# Topics  : 5-Stage Pipeline (IF, ID, EX, MEM, WB), Data Hazard (RAW),
#           Stall Bubbles, Control Hazard (Branch Flush), Pipeline Speedup
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
pipe_hdr:
    .asciz "\n==============================================================================\n        CO4: 5-STAGE INSTRUCTION PIPELINE SIMULATOR & HAZARDS                 \n==============================================================================\n  Stages: IF (Fetch) -> ID (Decode) -> EX (Execute) -> MEM (Memory) -> WB (Write)\n"

sec_ideal_title:
    .asciz "\n--- 1. IDEAL PIPELINE (NO HAZARDS) ---\n"
ideal_matrix:
    .asciz "  Inst \\ Cycle | C1  | C2  | C3  | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  I1: ADD      | IF  | ID  | EX  | MEM | WB  |     |     |     |\n  I2: SUB      |     | IF  | ID  | EX  | MEM | WB  |     |     |\n  I3: AND      |     |     | IF  | ID  | EX  | MEM | WB  |     |\n  I4: OR       |     |     |     | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  Pipelined: 8 Cycles | Sequential: 20 Cycles | Speedup: 2.50x\n"

sec_data_hazard_title:
    .asciz "\n--- 2. DATA HAZARD (RAW: READ-AFTER-WRITE) WITH 2 STALL BUBBLES ---\n  I1: ADD R1, R2, R3  (Writes R1 in WB at Cycle 5)\n  I2: SUB R4, R1, R5  (Reads R1 in ID -> stalls 2 cycles without forwarding)\n\n  Inst \\ Cycle | C1  | C2  | C3    | C4    | C5  | C6  | C7  | C8  | C9  | C10 |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  I1: ADD      | IF  | ID  | EX    | MEM   | WB  |     |     |     |     |     |\n  I2: SUB      |     | IF  | STALL | STALL | ID  | EX  | MEM | WB  |     |     |\n  I3: OR       |     |     | STALL | STALL | IF  | ID  | EX  | MEM | WB  |     |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  Total Execution: 10 Cycles (2 bubble cycles inserted)\n"

sec_branch_hazard_title:
    .asciz "\n--- 3. CONTROL HAZARD (BRANCH PENALTY) WITH PIPELINE FLUSH ---\n  I1: BEQ R1, R2, TARGET (Branch resolved in EX -> Cycle 3)\n  I2: ADD R4, R5, R6     (Speculatively fetched in IF -> FLUSHED to NOP)\n\n  Inst \\ Cycle | C1  | C2  | C3    | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-------+-----+-----+-----+-----+-----+\n  I1: BEQ      | IF  | ID  | EX    | MEM | WB  |     |     |     |\n  I2: ADD (old)|     | IF  | FLUSH |  -  |  -  |  -  |  -  |  -  |\n  TARGET: SUB  |     |     |   -   | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-------+-----+-----+-----+-----+-----+\n  Branch Penalty: 1 cycle flush\n"

sec_speedup_title:
    .asciz "\n--- 4. SEQUENTIAL VS PIPELINED SPEEDUP BENCHMARK ---\n"
speedup_fmt:
    .asciz "  Benchmark (N = %lld instructions, 5 stages):\n    Sequential Time (T_seq = N*5)         : %lld cycles\n    Ideal Pipelined (T_pipe = 5+N-1)      : %lld cycles -> Ideal Speedup   : %lld.%02lldx\n    With Hazards    (+2 stalls +1 flush)  : %lld cycles -> Realized Speedup: %lld.%02lldx\n==============================================================================\n"

.text

# ------------------------------------------------------------------------------
# [TOPIC: Pipeline Speedup Calculation] coa_calc_pipeline_metrics
# Purpose   : Computes T_seq = 5*N, T_ideal = N+4, and T_real = N+4+penalty.
# Game Role : Benchmarks frame-tick speedup & models packet/weapon pipeline delays.
# Inputs    : RCX=N, RDX=penaltyCycles, R8=&T_seq, R9=&T_ideal
# Output    : RAX = T_real (total cycles including stalls/flushes)
# ------------------------------------------------------------------------------
.global coa_calc_pipeline_metrics
.global _coa_calc_pipeline_metrics
coa_calc_pipeline_metrics:
_coa_calc_pipeline_metrics:
    imul rax, rcx, 5            # WHY: Without pipelining, N instructions take N * 5 sequential cycles
    lea  r10, [rcx + 4]         # WHY: 5-stage pipeline overlaps instructions: T_ideal = k + N - 1 = N + 4 cycles

    test r8, r8
    jz   .L_pm_skip_seq
    mov  [r8], rax
.L_pm_skip_seq:
    test r9, r9
    jz   .L_pm_skip_ideal
    mov  [r9], r10
.L_pm_skip_ideal:
    lea  rax, [r10 + rdx]       # WHY: Add hazard stalls/flush penalties to compute actual execution time
    ret

# ------------------------------------------------------------------------------
# [TOPIC: 5-Stage Pipeline Simulator Demo] co4_demo_pipeline
# Purpose   : Prints ideal, RAW stall, and branch flush timelines + speedup math.
# Game Role : Visualizes why stalls slow weapon/network tasks and how speedup scales.
# ------------------------------------------------------------------------------
.global co4_demo_pipeline
.global _co4_demo_pipeline
co4_demo_pipeline:
_co4_demo_pipeline:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push r12
    push r13
    sub  rsp, 96

    lea  rcx, [rip + pipe_hdr]
    call print_str
    lea  rcx, [rip + sec_ideal_title]
    call print_str
    lea  rcx, [rip + ideal_matrix]
    call print_str
    lea  rcx, [rip + sec_data_hazard_title]
    call print_str
    lea  rcx, [rip + sec_branch_hazard_title]
    call print_str
    lea  rcx, [rip + sec_speedup_title]
    call print_str

    mov  rcx, 100               # N = 100 instructions
    mov  rdx, 3                 # 2 RAW stalls + 1 branch flush penalty
    lea  r8,  [rsp + 72]        # T_seq = 500
    lea  r9,  [rsp + 80]        # T_ideal = 104
    call coa_calc_pipeline_metrics
    mov  [rsp + 88], rax        # T_real = 107

    mov  rbx, [rsp + 72]
    mov  rsi, [rsp + 80]

    imul rax, rbx, 100          # WHY: Fixed-point *100 computes 4.80x speedup in pure integer ALU without floats
    cqo
    idiv rsi
    cqo
    mov  r10, 100
    idiv r10                    # RAX = integer part (4), RDX = decimal fraction (80)
    mov  r12, rax
    mov  r13, rdx

    mov  r11, [rsp + 88]
    imul rax, rbx, 100          # WHY: Same integer fixed-point trick gives realized 4.67x speedup with hazards
    cqo
    idiv r11
    cqo
    mov  r10, 100
    idiv r10

    lea  rcx, [rip + speedup_fmt]
    mov  [rsp + 32], r12
    mov  [rsp + 40], r13
    mov  [rsp + 48], r11
    mov  [rsp + 56], rax
    mov  [rsp + 64], rdx
    mov  rdx, 100
    mov  r8,  rbx
    mov  r9,  rsi
    call printf

    add  rsp, 96
    pop  r13
    pop  r12
    pop  rsi
    pop  rbx
    pop  rbp
    ret
