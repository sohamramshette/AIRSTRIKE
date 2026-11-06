# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO4: Control Unit & Signals
# Demonstrates Instruction Fetch, Decode, Execute, and Control Signal Generation
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
cu_header:
    .asciz "\n==============================================================================\n        CO4: CONTROL UNIT ARCHITECTURE & CONTROL SIGNAL GENERATION            \n==============================================================================\n"
cu_desc:
    .asciz "  Control Unit Functions:\n  1. Instruction Fetch (IF): Reads instruction from memory into IR using PC.\n  2. Instruction Decode (ID): Decodes opcode and asserts appropriate control signals.\n  3. Execute (EX): Steers ALU operation, register routing, and memory accesses.\n\n"

cu_table_hdr:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n  | Inst Type | RegDst | ALUSrc | MemToReg | RegWrite | MemRead | MemWrite | Branch | ALUOp  |\n  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_row_fmt:
    .asciz "  | %-9s |   %s    |   %s    |    %s     |    %s     |    %s    |    %s     |   %s    | %-6s |\n"
cu_table_footer:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_note:
    .asciz "  [Academic Note] 'X' represents Don't Care condition in control signal logic.\n==============================================================================\n"

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
