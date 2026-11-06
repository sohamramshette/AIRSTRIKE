# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO4: 5-Stage Instruction Pipeline Simulator
# Models IF, ID, EX, MEM, WB pipeline with RAW Data Hazard & Branch Hazard
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
pipe_hdr:
    .asciz "\n==============================================================================\n        CO4: 5-STAGE INSTRUCTION PIPELINING SIMULATION & HAZARDS              \n==============================================================================\n  5-Stage Pipeline Model: IF (Fetch), ID (Decode), EX (Execute), MEM, WB (WriteBack)\n"

sec_ideal_title:
    .asciz "\n--- 1. IDEAL PIPELINE (NO HAZARDS) - CYCLE BY CYCLE ---\n"
ideal_matrix:
    .asciz "  Inst \\ Cycle | C1  | C2  | C3  | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  I1: ADD      | IF  | ID  | EX  | MEM | WB  |     |     |     |\n  I2: SUB      |     | IF  | ID  | EX  | MEM | WB  |     |     |\n  I3: AND      |     |     | IF  | ID  | EX  | MEM | WB  |     |\n  I4: OR       |     |     |     | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-----+-----+-----+-----+-----+-----+\n  Pipelined Latency: 8 Cycles | Sequential: 20 Cycles | Speedup: 2.50x\n"

sec_data_hazard_title:
    .asciz "\n--- 2. DATA HAZARD (RAW - READ AFTER WRITE) WITH STALL BUBBLES ---\n  Hazard Scenario:\n    I1: ADD R1, R2, R3   <- Writes R1 during WB (Cycle 5)\n    I2: SUB R4, R1, R5   <- Requires R1 during ID (Attempted Cycle 3!)\n\n  Pipeline Timeline with 2 Stalls (Bubbles):\n  Inst \\ Cycle | C1  | C2  | C3    | C4    | C5  | C6  | C7  | C8  | C9  | C10 |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  I1: ADD      | IF  | ID  | EX    | MEM   | WB  |     |     |     |     |     |\n  I2: SUB      |     | IF  | STALL | STALL | ID  | EX  | MEM | WB  |     |     |\n  I3: OR       |     |     | STALL | STALL | IF  | ID  | EX  | MEM | WB  |     |\n  -------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+\n  Total Execution: 10 Cycles (includes 2 bubble stalls to prevent stale data read)\n"

sec_branch_hazard_title:
    .asciz "\n--- 3. CONTROL HAZARD (BRANCH PENALTY) WITH PIPELINE FLUSH ---\n  Hazard Scenario:\n    I1: BEQ R1, R2, TARGET  <- Evaluates condition in EX (Cycle 3)\n    I2: ADD R4, R5, R6      <- Speculatively fetched in IF (Cycle 2)\n    Branch Condition is TAKEN -> I2 must be FLUSHED (turned into NOP)!\n\n  Pipeline Timeline with Flush:\n  Inst \\ Cycle | C1  | C2  | C3    | C4  | C5  | C6  | C7  | C8  |\n  -------------+-----+-----+-------+-----+-----+-----+-----+-----+\n  I1: BEQ      | IF  | ID  | EX    | MEM | WB  |     |     |     |\n  I2: ADD (old)|     | IF  | FLUSH |  -  |  -  |  -  |  -  |  -  |\n  TARGET: SUB  |     |     |   -   | IF  | ID  | EX  | MEM | WB  |\n  -------------+-----+-----+-------+-----+-----+-----+-----+-----+\n  Branch Penalty: 1 cycle flush penalty incurred upon branch resolution.\n"

sec_speedup_title:
    .asciz "\n--- 4. SEQUENTIAL VS PIPELINED PERFORMANCE METRICS ---\n"
speedup_fmt:
    .asciz "  Benchmark for N = %d Instructions across 5-Stage Architecture:\n    Sequential Execution Time (T_seq)  : %d cycles (N * 5)\n    Ideal Pipelined Time     (T_pipe) : %d cycles (5 + N - 1)\n    Theoretical Ideal Speedup          : %d.%02dx\n    With 2 Stalls + 1 Flush Hazard     : %d cycles -> Realized Speedup: %d.%02dx\n==============================================================================\n"

.text

# ------------------------------------------------------------------------------
# co4_demo_pipeline: Displays cycle-by-cycle pipeline simulation & hazards
# ------------------------------------------------------------------------------
.global _co4_demo_pipeline
_co4_demo_pipeline:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset pipe_hdr
    call _print_str
    add esp, 4

    # 1. Ideal Pipeline
    push offset sec_ideal_title
    call _print_str
    add esp, 4
    push offset ideal_matrix
    call _print_str
    add esp, 4

    # 2. Data Hazard (RAW)
    push offset sec_data_hazard_title
    call _print_str
    add esp, 4

    # 3. Control Hazard (Branch)
    push offset sec_branch_hazard_title
    call _print_str
    add esp, 4

    # 4. Performance Metrics (N = 100 instructions)
    push offset sec_speedup_title
    call _print_str
    add esp, 4

    mov ecx, 100                # N = 100 instructions

    # T_seq = 100 * 5 = 500
    mov eax, ecx
    imul eax, 5
    mov ebx, eax                # ebx = T_seq = 500

    # T_pipe_ideal = 5 + 100 - 1 = 104
    mov edx, ecx
    add edx, 4
    mov esi, edx                # esi = T_pipe_ideal = 104

    # Ideal Speedup = T_seq / T_pipe_ideal = 500 / 104 = 4.80x
    mov eax, ebx
    imul eax, 100               # 500 * 100 = 50000
    xor edx, edx
    div esi                     # eax = 480 (4.80)
    mov edi, eax                # edi = 480

    # T_pipe_hazards = 104 + 3 (2 stalls + 1 flush) = 107
    lea ecx, [esi + 3]          # ecx = 107
    mov eax, ebx
    imul eax, 100               # 50000
    xor edx, edx
    div ecx                     # eax = realized speedup * 100

    # Format realized speedup (eax / 100, eax % 100)
    xor edx, edx
    mov ecx, 100
    div ecx                     # eax = int part, edx = frac part
    push edx                    # realized frac
    push eax                    # realized int

    mov ecx, esi
    add ecx, 3
    push ecx                    # T_pipe with hazards (107)

    # Format ideal speedup (edi / 100, edi % 100)
    mov eax, edi
    xor edx, edx
    mov ecx, 100
    div ecx
    push edx                    # ideal frac
    push eax                    # ideal int

    push esi                    # T_pipe ideal (104)
    push ebx                    # T_seq (500)
    push 100                    # N
    push offset speedup_fmt
    call _printf
    add esp, 36

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
