# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO4: 5-STAGE INSTRUCTION PIPELINE SIMULATOR
# Models IF, ID, EX, MEM, WB Pipeline with RAW Data Hazard Stalls and Branch Flush
# Satisfies Course Outcome: COA-CO4 (Instruction Pipelining & Hazard Resolution)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
pipe_hdr:
    .asciz "\n==============================================================================\n        CO4: 5-STAGE INSTRUCTION PIPELINING SIMULATION & HAZARDS (64-BIT)     \n==============================================================================\n  [Educational Architecture Model: Classic 5-Stage RISC Pipelining (IF,ID,EX,MEM,WB)]\n  [Note: This educational model illustrates pipeline hazards, stalls, and speedup.\n         Modern 64-bit x86-64 processors use deep 14-to-19 stage out-of-order execution.]\n"

sec_ideal_title:
    .asciz "\n--- 1. IDEAL PIPELINE (NO HAZARDS) - CYCLE BY CYCLE ---\n"
ideal_matrix:
    .asciz "  Inst \\ Cycle | C1  | C2  | C3  | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  I1: ADD      | IF  | ID  | EX  | MEM | WB  |     |     |     |\n  I2: SUB      |     | IF  | ID  | EX  | MEM | WB  |     |     |\n  I3: AND      |     |     | IF  | ID  | EX  | MEM | WB  |     |\n  I4: OR       |     |     |     | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  Pipelined Latency: 8 Cycles | Sequential: 20 Cycles | Speedup: 2.50x\n"

sec_data_hazard_title:
    .asciz "\n--- 2. DATA HAZARD (RAW - READ AFTER WRITE) WITH STALL BUBBLES ---\n  Hazard Scenario:\n    I1: ADD R1, R2, R3   <- Writes R1 during WB (Cycle 5)\n    I2: SUB R4, R1, R5   <- Requires R1 during ID (Attempted Cycle 3!)\n\n  Pipeline Timeline with 2 Stalls (Bubbles):\n  Inst \\ Cycle | C1  | C2  | C3    | C4    | C5  | C6  | C7  | C8  | C9  | C10 |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  I1: ADD      | IF  | ID  | EX    | MEM   | WB  |     |     |     |     |     |\n  I2: SUB      |     | IF  | STALL | STALL | ID  | EX  | MEM | WB  |     |     |\n  I3: OR       |     |     | STALL | STALL | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  Total Execution: 10 Cycles (includes 2 bubble stalls to prevent stale data read)\n"

sec_branch_hazard_title:
    .asciz "\n--- 3. CONTROL HAZARD (BRANCH PENALTY) WITH PIPELINE FLUSH ---\n  Hazard Scenario:\n    I1: BEQ R1, R2, TARGET  <- Evaluates condition in EX (Cycle 3)\n    I2: ADD R4, R5, R6      <- Speculatively fetched in IF (Cycle 2)\n    Branch Condition is TAKEN -> I2 must be FLUSHED (turned into NOP)!\n\n  Pipeline Timeline with Flush:\n  Inst \\ Cycle | C1  | C2  | C3    | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-------+-----+-----+-----+-----+-----+\n  I1: BEQ      | IF  | ID  | EX    | MEM | WB  |     |     |     |\n  I2: ADD (old)|     | IF  | FLUSH |  -  |  -  |  -  |  -  |  -  |\n  TARGET: SUB  |     |     |   -   | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-------+-----+-----+-----+-----+-----+\n  Branch Penalty: 1 cycle flush penalty incurred upon branch resolution.\n"

sec_speedup_title:
    .asciz "\n--- 4. SEQUENTIAL VS PIPELINED PERFORMANCE METRICS ---\n"
speedup_fmt:
    .asciz "  Benchmark for N = %lld Instructions across 5-Stage Architecture:\n    Sequential Execution Time (T_seq)  : %lld cycles (N * 5)\n    Ideal Pipelined Time     (T_pipe) : %lld cycles (5 + N - 1)\n    Theoretical Ideal Speedup          : %lld.%02lldx\n    With 2 Stalls + 1 Flush Hazard     : %lld cycles -> Realized Speedup: %lld.%02lldx\n==============================================================================\n"

.text

# ==============================================================================
# Routine: co4_demo_pipeline
# - Small Meaning: Displays cycle-by-cycle pipeline simulation, hazards, and speedup math.
# - Long Form    : CO4 5-Stage Instruction Pipelining Demonstration Driver
# - Why We Used It: Mathematically proves pipelined speedup and evaluates stall penalties.
# ==============================================================================
.global co4_demo_pipeline
.global _co4_demo_pipeline
co4_demo_pipeline:
_co4_demo_pipeline:
    push rbp
    mov rbp, rsp
    sub rsp, 96                 # Stack allocation for parameters and shadow space
    push rbx
    push rsi
    push rdi
    push r12
    push r13

    lea rcx, [rip + pipe_hdr]
    call print_str

    # 1. Ideal Pipeline
    lea rcx, [rip + sec_ideal_title]
    call print_str

    lea rcx, [rip + ideal_matrix]
    call print_str

    # 2. Data Hazard (RAW)
    lea rcx, [rip + sec_data_hazard_title]
    call print_str

    # 3. Control Hazard (Branch)
    lea rcx, [rip + sec_branch_hazard_title]
    call print_str

    # 4. Performance Metrics (N = 100 instructions)
    lea rcx, [rip + sec_speedup_title]
    call print_str

    mov rcx, 100                # N = 100 instructions

    # --------------------------------------------------------------------------
    # T_seq = 100 * 5 = 500 cycles
    # --------------------------------------------------------------------------
    mov rax, rcx
    imul rax, 5
    mov rbx, rax                # RBX = T_seq = 500

    # --------------------------------------------------------------------------
    # T_pipe_ideal = 5 + N - 1 = 104 cycles
    # --------------------------------------------------------------------------
    lea rsi, [rcx + 4]          # RSI = T_pipe_ideal = 104

    # --------------------------------------------------------------------------
    # Ideal Speedup = (T_seq * 100) / T_pipe_ideal = 50000 / 104 = 480 (4.80x)
    # --------------------------------------------------------------------------
    mov rax, rbx
    imul rax, 100               # RAX = 50000
    cqo
    idiv rsi                    # RAX = 480
    mov rdi, rax                # RDI = 480

    # Format ideal speedup into integer and fractional parts
    mov rax, rdi
    cqo
    mov r10, 100
    idiv r10                    # RAX = 4 (int), RDX = 80 (frac)
    mov r12, rax                # R12 = 4
    mov r13, rdx                # R13 = 80

    # --------------------------------------------------------------------------
    # T_pipe_hazards = 104 + 3 (2 stalls + 1 flush) = 107 cycles
    # --------------------------------------------------------------------------
    lea r8, [rsi + 3]           # R8 = 107
    mov rax, rbx
    imul rax, 100               # RAX = 50000
    cqo
    idiv r8                     # RAX = realized speedup * 100 = 467

    # Format realized speedup (RAX / 100, RAX % 100)
    cqo
    mov r10, 100
    idiv r10                    # RAX = 4 (int), RDX = 67 (frac)

    # --------------------------------------------------------------------------
    # Invoke printf with 64-bit parameters:
    # 1st arg (RCX) = speedup_fmt
    # 2nd arg (RDX) = N (100)
    # 3rd arg (R8)  = T_seq (500)
    # 4th arg (R9)  = T_pipe_ideal (104)
    # 5th arg [rsp+32] = ideal int (4)
    # 6th arg [rsp+40] = ideal frac (80)
    # 7th arg [rsp+48] = T_pipe_hazards (107)
    # 8th arg [rsp+56] = realized int (4)
    # 9th arg [rsp+64] = realized frac (67)
    # --------------------------------------------------------------------------
    lea rcx, [rip + speedup_fmt]
    mov rdx, 100                # N
    mov r8,  rbx                # T_seq (500)
    mov r9,  rsi                # T_pipe ideal (104)
    mov [rsp + 32], r12         # Ideal int (4)
    mov [rsp + 40], r13         # Ideal frac (80)
    lea r10, [rsi + 3]
    mov [rsp + 48], r10         # T_pipe hazards (107)
    mov [rsp + 56], rax         # Realized int (4)
    mov [rsp + 64], rdx         # Realized frac (67)
    call printf

    pop r13
    pop r12
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
