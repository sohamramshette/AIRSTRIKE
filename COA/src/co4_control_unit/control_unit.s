# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO4: CONTROL UNIT & SIGNALS
# Demonstrates Instruction Fetch, Decode, Execute, and Control Signal Generation
# Satisfies Course Outcome: COA-CO4 (Control Unit Architecture & Micro-operations)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
cu_header:
    .asciz "\n==============================================================================\n      CO4: CONTROL UNIT & INSTRUCTION DECODING (64-BIT SIMULATION)            \n==============================================================================\n"
cu_desc:
    .asciz "  [Educational Microarchitecture Datapath Model (Patterson & Hennessy)]\n  This model demonstrates how instruction opcodes decode into internal control wires\n  governing register writes, ALU source multiplexing, and 64-bit memory operations.\n\n  + Academic Datapath Control Signals Table:\n"

cu_table_hdr:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n  | Inst Type | RegDst | ALUSrc | MemToReg | RegWrite | MemRead | MemWrite | Branch | ALUOp  |\n  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_row_fmt:
    .asciz "  | %-9s |   %s    |   %s    |    %s     |    %s     |    %s    |    %s     |   %s    | %-6s |\n"
cu_table_footer:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_note:
    .asciz "  [Academic Note] 'X' represents Don't Care condition in control signal logic.\n\n--- ARCHITECTURAL CLARIFICATION: EDUCATIONAL MODEL VS 64-BIT x86-64 HARDWARE ---\n  1. Signals like RegDst, ALUSrc, MemToReg, RegWrite belong to an abstract academic\n     single-cycle datapath model. Genuine x86-64 microarchitectures (Intel Core / AMD Zen)\n     do NOT operate on monolithic single-cycle control buses.\n  2. Modern 64-bit x86-64 Processors Use Advanced Superscalar Microarchitectures:\n     - Instruction Fetch Unit (IFU): Fetches up to 32/64 bytes/cycle from L1 Instruction Cache.\n     - Macro-op Pre-decoders & Decoders: Translates complex CISC instructions into RISC-like Micro-ops (uOps).\n     - Micro-op Queue & Branch Predictor: Multi-level TAGE / Neural branch prediction.\n     - Out-of-Order (OoO) Engine: Allocates uOps to Reservation Stations and Reorder Buffer (ROB).\n     - Execution Ports: Multiple parallel ALUs, Vector/SIMD (AVX-512) pipelines, and Address Generation Units (AGUs).\n     - Load/Store Queues (LSQ): Dispatches speculative 64-bit memory reads and out-of-order writes safely.\n==============================================================================\n"

# Instruction Names
str_rtype:
    .asciz "R-Type   "
str_lw:
    .asciz "MOV/LD   "
str_sw:
    .asciz "MOV/ST   "
str_beq:
    .asciz "JE/CMP   "
str_jmp:
    .asciz "JMP      "

# Control signal values
str_0:
    .asciz "0"
str_1:
    .asciz "1"
str_x:
    .asciz "X"
str_alu_r:
    .asciz "FUNCT "
str_alu_add:
    .asciz "ADD   "
str_alu_sub:
    .asciz "SUB   "
str_alu_none:
    .asciz "NONE  "

.text

# ==============================================================================
# Routine: co4_demo_control_unit
# - Small Meaning: Displays Control Unit theory, signals table, and x86-64 microarchitecture analysis.
# - Long Form    : CO4 Control Unit Architecture and Instruction Decoding Driver
# - Why We Used It: Validates control signal matrix for academic curriculum and oral vivas.
# ==============================================================================
.global co4_demo_control_unit
.global _co4_demo_control_unit
co4_demo_control_unit:
_co4_demo_control_unit:
    # --------------------------------------------------------------------------
    # PUSH RBP / MOV RBP, RSP / SUB RSP, 112:
    # - Small Meaning: Set up 64-bit frame and allocate stack space for up to 10 printf arguments
    # - Long Form    : Base Pointer Setup and Multi-Argument Stack Shadow Allocation
    # - Why We Used It: Microsoft x64 ABI requires RCX, RDX, R8, R9 for 1st 4 args, remaining on stack.
    # --------------------------------------------------------------------------
    push rbp
    mov rbp, rsp
    sub rsp, 112
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + cu_header]
    call print_str

    lea rcx, [rip + cu_desc]
    call print_str

    lea rcx, [rip + cu_table_hdr]
    call print_str

    # --------------------------------------------------------------------------
    # 1. R-Type (ADD, SUB, AND, OR):
    # RegDst=1, ALUSrc=0, MemToReg=0, RegWrite=1, MemRead=0, MemWrite=0, Branch=0, ALUOp=FUNCT
    # --------------------------------------------------------------------------
    lea rcx, [rip + cu_row_fmt] # 1st arg in RCX
    lea rdx, [rip + str_rtype]   # 2nd arg in RDX: Inst Type
    lea r8,  [rip + str_1]       # 3rd arg in R8 : RegDst
    lea r9,  [rip + str_0]       # 4th arg in R9 : ALUSrc
    lea rax, [rip + str_0]
    mov [rsp + 32], rax         # 5th arg: MemToReg
    lea rax, [rip + str_1]
    mov [rsp + 40], rax         # 6th arg: RegWrite
    lea rax, [rip + str_0]
    mov [rsp + 48], rax         # 7th arg: MemRead
    lea rax, [rip + str_0]
    mov [rsp + 56], rax         # 8th arg: MemWrite
    lea rax, [rip + str_0]
    mov [rsp + 64], rax         # 9th arg: Branch
    lea rax, [rip + str_alu_r]
    mov [rsp + 72], rax         # 10th arg: ALUOp
    call printf

    # --------------------------------------------------------------------------
    # 2. MOV/LD (Load):
    # RegDst=0, ALUSrc=1, MemToReg=1, RegWrite=1, MemRead=1, MemWrite=0, Branch=0, ALUOp=ADD
    # --------------------------------------------------------------------------
    lea rcx, [rip + cu_row_fmt]
    lea rdx, [rip + str_lw]
    lea r8,  [rip + str_0]
    lea r9,  [rip + str_1]
    lea rax, [rip + str_1]
    mov [rsp + 32], rax
    lea rax, [rip + str_1]
    mov [rsp + 40], rax
    lea rax, [rip + str_1]
    mov [rsp + 48], rax
    lea rax, [rip + str_0]
    mov [rsp + 56], rax
    lea rax, [rip + str_0]
    mov [rsp + 64], rax
    lea rax, [rip + str_alu_add]
    mov [rsp + 72], rax
    call printf

    # --------------------------------------------------------------------------
    # 3. MOV/ST (Store):
    # RegDst=X, ALUSrc=1, MemToReg=X, RegWrite=0, MemRead=0, MemWrite=1, Branch=0, ALUOp=ADD
    # --------------------------------------------------------------------------
    lea rcx, [rip + cu_row_fmt]
    lea rdx, [rip + str_sw]
    lea r8,  [rip + str_x]
    lea r9,  [rip + str_1]
    lea rax, [rip + str_x]
    mov [rsp + 32], rax
    lea rax, [rip + str_0]
    mov [rsp + 40], rax
    lea rax, [rip + str_0]
    mov [rsp + 48], rax
    lea rax, [rip + str_1]
    mov [rsp + 56], rax
    lea rax, [rip + str_0]
    mov [rsp + 64], rax
    lea rax, [rip + str_alu_add]
    mov [rsp + 72], rax
    call printf

    # --------------------------------------------------------------------------
    # 4. JE/CMP (Branch):
    # RegDst=X, ALUSrc=0, MemToReg=X, RegWrite=0, MemRead=0, MemWrite=0, Branch=1, ALUOp=SUB
    # --------------------------------------------------------------------------
    lea rcx, [rip + cu_row_fmt]
    lea rdx, [rip + str_beq]
    lea r8,  [rip + str_x]
    lea r9,  [rip + str_0]
    lea rax, [rip + str_x]
    mov [rsp + 32], rax
    lea rax, [rip + str_0]
    mov [rsp + 40], rax
    lea rax, [rip + str_0]
    mov [rsp + 48], rax
    lea rax, [rip + str_0]
    mov [rsp + 56], rax
    lea rax, [rip + str_1]
    mov [rsp + 64], rax
    lea rax, [rip + str_alu_sub]
    mov [rsp + 72], rax
    call printf

    # --------------------------------------------------------------------------
    # 5. JMP (Unconditional Jump):
    # RegDst=X, ALUSrc=X, MemToReg=X, RegWrite=0, MemRead=0, MemWrite=0, Branch=0, ALUOp=NONE
    # --------------------------------------------------------------------------
    lea rcx, [rip + cu_row_fmt]
    lea rdx, [rip + str_jmp]
    lea r8,  [rip + str_x]
    lea r9,  [rip + str_x]
    lea rax, [rip + str_x]
    mov [rsp + 32], rax
    lea rax, [rip + str_0]
    mov [rsp + 40], rax
    lea rax, [rip + str_0]
    mov [rsp + 48], rax
    lea rax, [rip + str_0]
    mov [rsp + 56], rax
    lea rax, [rip + str_0]
    mov [rsp + 64], rax
    lea rax, [rip + str_alu_none]
    mov [rsp + 72], rax
    call printf

    lea rcx, [rip + cu_table_footer]
    call print_str

    lea rcx, [rip + cu_note]
    call print_str

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
