# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO4: Control Unit & Signals
# Demonstrates Instruction Fetch, Decode, Execute, and Control Signal Generation
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
cu_header:
    .asciz "\n==============================================================================\n      CO4: CONTROL UNIT & INSTRUCTION DECODING (EDUCATIONAL SIMULATION)       \n==============================================================================\n"
cu_desc:
    .asciz "  [Educational Microarchitecture Datapath Model (Patterson & Hennessy)]\n  This model demonstrates how instruction opcodes decode into internal control wires\n  governing register writes, ALU source multiplexing, and memory operations.\n\n  + Academic Datapath Control Signals Table:\n"

cu_table_hdr:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n  | Inst Type | RegDst | ALUSrc | MemToReg | RegWrite | MemRead | MemWrite | Branch | ALUOp  |\n  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_row_fmt:
    .asciz "  | %-9s |   %s    |   %s    |    %s     |    %s     |    %s    |    %s     |   %s    | %-6s |\n"
cu_table_footer:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_note:
    .asciz "  [Academic Note] 'X' represents Don't Care condition in control signal logic.\n\n--- ARCHITECTURAL CLARIFICATION: EDUCATIONAL MODEL VS INTEL 80386 HARDWARE ---\n  1. Signals like RegDst, ALUSrc, MemToReg, RegWrite belong to an abstract academic\n     datapath model used in university curricula. The physical Intel 80386 does NOT\n     expose these internal control wires on its external pins.\n  2. The physical 80386 bus interface instead asserts hardware bus cycle pins:\n     - M/IO#  : Memory space access (1) vs I/O port access (0)\n     - D/C#   : Data operand transfer (1) vs Control/Code fetch (0)\n     - W/R#   : Write cycle (1) vs Read cycle (0)\n     - ADS#   : Address Status - asserts when address & bus definitions are valid\n     - READY# : Transfer Acknowledge input from external memory or bus controller\n     - BE0#-3#: Active-low byte enable lines selecting active byte lanes on 32-bit bus\n  3. Actual Intel 80386 Instruction Processing Organization (6 Parallel Units):\n     - Bus Interface Unit (BIU) : Coordinates external address/data bus transfers.\n     - Code Prefetch Unit       : Continuously fetches up to 16 bytes into prefetch queue.\n     - Instruction Decode Unit  : Translates 1-to-15 byte CISC instructions to microcode.\n     - Execution Unit (EU)      : Executes operations with 32-bit ALU, 8 GPRs & barrel shifter.\n     - Segmentation Unit        : Translates logical address (seg:offset) to 32-bit linear address.\n     - Paging Unit              : Translates linear address to physical address via page tables.\n==============================================================================\n"

# Instruction Names
str_rtype:
    .asciz "R-Type   "
str_lw:
    .asciz "LW       "
str_sw:
    .asciz "SW       "
str_beq:
    .asciz "BEQ      "
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

# ------------------------------------------------------------------------------
# co4_demo_control_unit: Displays control unit theory and signal generation table
# ------------------------------------------------------------------------------
.global _co4_demo_control_unit
_co4_demo_control_unit:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset cu_header
    call _print_str
    add esp, 4

    push offset cu_desc
    call _print_str
    add esp, 4

    push offset cu_table_hdr
    call _print_str
    add esp, 4

    # 1. R-Type (ADD, SUB, AND, OR): RegDst=1, ALUSrc=0, MemToReg=0, RegWrite=1, MemRead=0, MemWrite=0, Branch=0, ALUOp=FUNCT
    push offset str_alu_r
    push offset str_0
    push offset str_0
    push offset str_0
    push offset str_1
    push offset str_0
    push offset str_0
    push offset str_1
    push offset str_rtype
    push offset cu_row_fmt
    call _printf
    add esp, 40

    # 2. LW (Load Word): RegDst=0, ALUSrc=1, MemToReg=1, RegWrite=1, MemRead=1, MemWrite=0, Branch=0, ALUOp=ADD
    push offset str_alu_add
    push offset str_0
    push offset str_0
    push offset str_1
    push offset str_1
    push offset str_1
    push offset str_1
    push offset str_0
    push offset str_lw
    push offset cu_row_fmt
    call _printf
    add esp, 40

    # 3. SW (Store Word): RegDst=X, ALUSrc=1, MemToReg=X, RegWrite=0, MemRead=0, MemWrite=1, Branch=0, ALUOp=ADD
    push offset str_alu_add
    push offset str_0
    push offset str_1
    push offset str_0
    push offset str_0
    push offset str_x
    push offset str_1
    push offset str_x
    push offset str_sw
    push offset cu_row_fmt
    call _printf
    add esp, 40

    # 4. BEQ (Branch on Equal): RegDst=X, ALUSrc=0, MemToReg=X, RegWrite=0, MemRead=0, MemWrite=0, Branch=1, ALUOp=SUB
    push offset str_alu_sub
    push offset str_1
    push offset str_0
    push offset str_0
    push offset str_0
    push offset str_x
    push offset str_0
    push offset str_x
    push offset str_beq
    push offset cu_row_fmt
    call _printf
    add esp, 40

    # 5. JMP (Unconditional Jump): RegDst=X, ALUSrc=X, MemToReg=X, RegWrite=0, MemRead=0, MemWrite=0, Branch=0, ALUOp=NONE
    push offset str_alu_none
    push offset str_0
    push offset str_0
    push offset str_0
    push offset str_0
    push offset str_x
    push offset str_x
    push offset str_x
    push offset str_jmp
    push offset cu_row_fmt
    call _printf
    add esp, 40

    push offset cu_table_footer
    call _print_str
    add esp, 4

    push offset cu_note
    call _print_str
    add esp, 4

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

