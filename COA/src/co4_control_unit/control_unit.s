# ==============================================================================
# File    : COA/src/co4_control_unit/control_unit.s
# Subject : COA-CO4 | Control Unit Organization & Control Signal Matrix
# Topics  : Control Unit, Hardwired vs Microprogrammed Control, Opcode Decoding,
#           Control Signals (RegDst, ALUSrc, MemToReg, RegWrite, MemRead,
#           MemWrite, Branch, ALUOp), Instruction Cycle
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
cu_header:
    .asciz "\n==============================================================================\n      CO4: CONTROL UNIT & CONTROL SIGNAL GENERATION MATRIX                    \n==============================================================================\n"
cu_desc:
    .asciz "  Datapath Control Signals (1=Asserted, 0=Deasserted, X=Don't Care):\n\n"
cu_table_hdr:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n  | Inst Type | RegDst | ALUSrc | MemToReg | RegWrite | MemRead | MemWrite | Branch | ALUOp  |\n  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_row_fmt:
    .asciz "  | %-9s |   %s    |   %s    |    %s     |    %s     |    %s    |    %s     |   %s    | %-6s |\n"
cu_table_footer:
    .asciz "  +-----------+--------+--------+----------+----------+---------+----------+--------+--------+\n"
cu_note:
    .asciz "  Control Unit Trade-offs:\n    - Hardwired      : Combinational logic gates (fastest, fixed ISA).\n    - Microprogrammed: Control ROM microinstructions (flexible, slower).\n    - Modern x86-64  : Decodes CISC instructions into RISC-like micro-ops (uOps).\n==============================================================================\n"

str_rtype:    .asciz "R-Type   "
str_lw:       .asciz "MOV/LD   "
str_sw:       .asciz "MOV/ST   "
str_beq:      .asciz "JE/CMP   "
str_jmp:      .asciz "JMP      "

str_0:        .asciz "0"
str_1:        .asciz "1"
str_x:        .asciz "X"
str_alu_r:    .asciz "FUNCT "
str_alu_add:  .asciz "ADD   "
str_alu_sub:  .asciz "SUB   "
str_alu_none: .asciz "NONE  "

# Packed Control Signal Bitmasks (Bit7=RegDst .. Bit0=ALUOpActive)
.align 8
cu_signal_masks:
    .quad 0x91                  # 0: R-Type -> RegDst, RegWrite, ALUOp
    .quad 0x79                  # 1: LW     -> ALUSrc, MemToReg, RegWrite, MemRead, ALUOp
    .quad 0x45                  # 2: SW     -> ALUSrc, MemWrite, ALUOp
    .quad 0x03                  # 3: BEQ    -> Branch, ALUOp
    .quad 0x00                  # 4: JMP    -> Control unit changes PC directly (no ALU/RegWrite)

.align 8
cu_table_rows:
    .quad str_rtype, str_1, str_0, str_0, str_1, str_0, str_0, str_0, str_alu_r
    .quad str_lw,    str_0, str_1, str_1, str_1, str_1, str_0, str_0, str_alu_add
    .quad str_sw,    str_x, str_1, str_x, str_0, str_0, str_1, str_0, str_alu_add
    .quad str_beq,   str_x, str_0, str_x, str_0, str_0, str_0, str_1, str_alu_sub
    .quad str_jmp,   str_x, str_x, str_x, str_0, str_0, str_0, str_0, str_alu_none

.text

# ------------------------------------------------------------------------------
# [TOPIC: Control Signal Decoder] coa_decode_control_signals
# Purpose   : Maps opcode ID (0=R-Type, 1=LW, 2=SW, 3=BEQ, 4=JMP) to control bits.
# Game Role : Models how the CPU's Control Unit routes signals for weapon & movement ops.
# Input     : RCX = opcode ID (0..4)
# Output    : RAX = packed 8-bit control word (or -1 if invalid opcode)
# ------------------------------------------------------------------------------
.global coa_decode_control_signals
.global _coa_decode_control_signals
coa_decode_control_signals:
_coa_decode_control_signals:
    cmp  rcx, 0                 # WHY: Bounds-check opcode ID before indexing ROM table
    jl   .L_cu_invalid
    cmp  rcx, 4
    jg   .L_cu_invalid

    lea  r10, [rip + cu_signal_masks] # WHY: Control ROM lookup converts opcode -> control bus word in 1 step
    mov  rax, [r10 + rcx*8]
    ret

.L_cu_invalid:
    mov  rax, -1                # WHY: Return -1 sentinel on illegal opcode
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Control Unit Demo] co4_demo_control_unit
# Purpose   : Prints the Control Signal Truth Table and Hardwired vs Microprogrammed notes.
# Game Role : Shows which CPU wires (RegWrite, MemRead, Branch) turn ON per instruction.
# ------------------------------------------------------------------------------
.global co4_demo_control_unit
.global _co4_demo_control_unit
co4_demo_control_unit:
_co4_demo_control_unit:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 96

    lea  rcx, [rip + cu_header]
    call print_str
    lea  rcx, [rip + cu_desc]
    call print_str
    lea  rcx, [rip + cu_table_hdr]
    call print_str

    xor  rsi, rsi
    lea  rbx, [rip + cu_table_rows]

.L_cu_print_loop:
    cmp  rsi, 5                 # WHY: Loop cleanly over 5 instruction types using table instead of 5 copy-pasted blocks
    jge  .L_cu_print_done

    imul r10, rsi, 72           # WHY: Each row stores 9 pointers * 8 bytes = 72 bytes
    lea  r11, [rbx + r10]

    lea  rcx, [rip + cu_row_fmt]
    mov  rdx, [r11 + 0]
    mov  r8,  [r11 + 8]
    mov  r9,  [r11 + 16]
    mov  rax, [r11 + 24]
    mov  [rsp + 32], rax
    mov  rax, [r11 + 32]
    mov  [rsp + 40], rax
    mov  rax, [r11 + 40]
    mov  [rsp + 48], rax
    mov  rax, [r11 + 48]
    mov  [rsp + 56], rax
    mov  rax, [r11 + 56]
    mov  [rsp + 64], rax
    mov  rax, [r11 + 64]
    mov  [rsp + 72], rax
    call printf

    inc  rsi
    jmp  .L_cu_print_loop

.L_cu_print_done:
    lea  rcx, [rip + cu_table_footer]
    call print_str
    lea  rcx, [rip + cu_note]
    call print_str

    add  rsp, 96
    pop  rsi
    pop  rbx
    pop  rbp
    ret
