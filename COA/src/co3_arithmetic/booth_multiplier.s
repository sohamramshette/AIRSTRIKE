# ==============================================================================
# File    : COA/src/co3_arithmetic/booth_multiplier.s
# Subject : COA-CO3 | Booth's Signed Binary Multiplication Algorithm
# Topics  : Booth's Algorithm, Signed Binary Multiplication, Two's Complement,
#           Pairwise Bit Recoding (Q0, Q-1), Arithmetic Right Shift (SAR)
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
booth_hdr:
    .asciz "\n==============================================================================\n        CO3: BOOTH'S SIGNED BINARY MULTIPLICATION ALGORITHM (8-CYCLE)         \n==============================================================================\n"
booth_inputs_fmt:   .asciz "  Multiplicand (M) = %lld (0x%02llX | Binary: "
booth_inputs_q:     .asciz ")\n  Multiplier   (Q) = %lld (0x%02llX | Binary: "
booth_table_hdr:
    .asciz ")\n\n  +-------+-------------------+----------+----------+-----+----------------+\n  | Cycle | Operation         |    A     |    Q     | Q-1 | Bit Pair       |\n  +-------+-------------------+----------+----------+-----+----------------+\n"
booth_row_fmt:      .asciz "  |   %lld   | %-17s | "
booth_row_mid:      .asciz " | "
booth_row_end:      .asciz " |  %lld  | %-14s |\n"
booth_table_footer: .asciz "  +-------+-------------------+----------+----------+-----+----------------+\n"
booth_final_fmt:
    .asciz "  Final Product (A:Q) = 0x%04llX (%lld decimal)\n  Verification        : %lld * %lld = %lld -> %s\n"

str_pass:       .asciz "MATCH (PASS)"
str_fail:       .asciz "MISMATCH (FAIL)"
str_init:       .asciz "Initial State"
str_add_ashr:   .asciz "A = A + M, SAR"
str_sub_ashr:   .asciz "A = A - M, SAR"
str_ashr_only:  .asciz "SAR (No Add/Sub)"
str_pair_01:    .asciz "01 -> Add M"
str_pair_10:    .asciz "10 -> Sub M"
str_pair_shift: .asciz "00/11 -> Shift"

prompt_booth_m: .asciz "  Enter signed Multiplicand M (-128 to 127): "
prompt_booth_q: .asciz "  Enter signed Multiplier   Q (-128 to 127): "

.text

# ------------------------------------------------------------------------------
# [TOPIC: Booth's Signed Multiplication Algorithm] asm_booth_multiply
# Purpose   : Multiplies two signed 8-bit numbers (-128..127) in 8 cycles using
#             Booth's bit-pair recoding and SAR—without hardware MUL or IMUL.
# Game Role : Core arithmetic engine for Booth Multiplier Gunship's blast damage.
# Inputs    : RCX = M (-128..127), RDX = Q (-128..127), R8 = verbose (1=print table)
# Output    : RAX = signed 16-bit product sign-extended to 64 bits
# ------------------------------------------------------------------------------
.global asm_booth_multiply
.global _asm_booth_multiply
asm_booth_multiply:
_asm_booth_multiply:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push rdi
    push r12
    push r13
    push r14
    push r15
    sub  rsp, 104               # WHY: 7 pushes (56) + 104 = 160 -> strictly 16-byte aligned for printf calls

    movsx r12, cl               # WHY: Sign-extend 8-bit M so negative values work in two's complement
    movsx rax, dl               # WHY: Save original signed Q at [rsp+88] for final verification line
    mov  [rsp + 88], rax
    movzx r13, dl               # WHY: Keep working Q as 8-bit unsigned slice while shifting [A:Q]
    mov  r14, r8                # WHY: Verbose flag (0 = fast silent mode for Gunship shots, 1 = print table)
    xor  rbx, rbx               # WHY: Accumulator A always starts at 0
    xor  r15, r15               # WHY: Auxiliary bit Q_-1 always starts at 0
    mov  qword ptr [rsp + 64], 1

    test r14, r14
    jz   .L_booth_loop

    lea  rcx, [rip + booth_hdr]
    call print_str

    lea  rcx, [rip + booth_inputs_fmt]
    mov  rdx, r12
    movzx r8, r12b
    call printf
    movzx rcx, r12b
    call print_bin8

    lea  rcx, [rip + booth_inputs_q]
    mov  rdx, [rsp + 88]
    movzx r8, r13b
    call printf
    movzx rcx, r13b
    call print_bin8

    lea  rcx, [rip + booth_table_hdr]
    call print_str

    lea  rcx, [rip + booth_row_fmt]
    xor  rdx, rdx
    lea  r8,  [rip + str_init]
    call printf
    xor  rcx, rcx
    call print_bin8
    lea  rcx, [rip + booth_row_mid]
    call print_str
    movzx rcx, r13b
    call print_bin8
    lea  rcx, [rip + booth_row_end]
    xor  rdx, rdx
    lea  r8,  [rip + str_init]
    call printf

.L_booth_loop:
    # Step 1: Build 2-bit pair (Q0, Q-1) to decide Add (+M), Sub (-M), or Shift only
    mov  rax, r13
    and  rax, 1                 # WHY: Isolate lowest bit Q0 of multiplier Q
    shl  rax, 1                 # WHY: Put Q0 in bit 1
    or   rax, r15               # WHY: Combine with Q-1 in bit 0 -> pair in {00=0, 01=1, 10=2, 11=3}

    cmp  rax, 1                 # WHY: Pair 01 marks end of a run of 1s -> A = A + M
    je   .L_booth_add
    cmp  rax, 2                 # WHY: Pair 10 marks start of a run of 1s -> A = A - M
    je   .L_booth_sub

    # Pair 00 or 11 -> skip arithmetic and shift directly (speeds up runs of 0s/1s)
    lea  rax, [rip + str_ashr_only]
    mov  [rsp + 72], rax
    lea  rax, [rip + str_pair_shift]
    mov  [rsp + 80], rax
    jmp  .L_booth_shift

.L_booth_add:
    add  rbx, r12               # WHY: Add signed multiplicand M into 8-bit accumulator A
    movsx rbx, bl               # WHY: Wrap to 8-bit two's complement and re-sign-extend
    lea  rax, [rip + str_add_ashr]
    mov  [rsp + 72], rax
    lea  rax, [rip + str_pair_01]
    mov  [rsp + 80], rax
    jmp  .L_booth_shift

.L_booth_sub:
    sub  rbx, r12               # WHY: Subtract signed multiplicand M from accumulator A
    movsx rbx, bl
    lea  rax, [rip + str_sub_ashr]
    mov  [rsp + 72], rax
    lea  rax, [rip + str_pair_10]
    mov  [rsp + 80], rax

.L_booth_shift:
    # Step 2: Arithmetic Right Shift across combined [A : Q : Q-1]
    mov  r15, r13
    and  r15, 1                 # WHY: Shift Q0 out into Q-1 for the next cycle

    mov  rax, rbx
    and  rax, 1                 # WHY: Shift lowest bit of A into bit 7 (MSB) of Q
    shl  rax, 7
    shr  r13, 1
    and  r13, 0x7F
    or   r13, rax

    sar  bl, 1                  # WHY: SAR (not SHR!) replicates A's sign bit so negative products stay negative
    movsx rbx, bl

    test r14, r14
    jz   .L_booth_next

    lea  rcx, [rip + booth_row_fmt]
    mov  rdx, [rsp + 64]
    mov  r8,  [rsp + 72]
    call printf
    movzx rcx, bl
    call print_bin8
    lea  rcx, [rip + booth_row_mid]
    call print_str
    movzx rcx, r13b
    call print_bin8
    lea  rcx, [rip + booth_row_end]
    mov  rdx, r15
    mov  r8,  [rsp + 80]
    call printf

.L_booth_next:
    inc  qword ptr [rsp + 64]
    cmp  qword ptr [rsp + 64], 8 # WHY: Exactly 8 cycles for 8-bit signed operands
    jle  .L_booth_loop

    # Step 3: Pack 8-bit A and 8-bit Q into signed 16-bit product (A:Q)
    mov  rax, rbx
    and  rax, 0xFF
    shl  rax, 8                 # WHY: A forms the high byte (bits 8..15)
    mov  rdx, r13
    and  rdx, 0xFF              # WHY: Q forms the low byte (bits 0..7)
    or   rax, rdx
    movsx rax, ax               # WHY: Sign-extend 16-bit product to 64-bit RAX for C/game callers

    test r14, r14
    jz   .L_booth_exit

    mov  rsi, rax
    lea  rcx, [rip + booth_table_footer]
    call print_str

    mov  r10, [rsp + 88]
    mov  rax, r12
    imul rax, r10               # WHY: Cross-check Booth output against CPU IMUL only to print PASS/FAIL

    lea  rdi, [rip + str_pass]
    cmp  rsi, rax
    je   .L_booth_print
    lea  rdi, [rip + str_fail]

.L_booth_print:
    lea  rcx, [rip + booth_final_fmt]
    movzx rdx, si
    mov  r8,  rsi
    mov  r9,  r12
    mov  [rsp + 32], r10
    mov  [rsp + 40], rax
    mov  [rsp + 48], rdi
    call printf

    mov  rax, rsi

.L_booth_exit:
    add  rsp, 104
    pop  r15
    pop  r14
    pop  r13
    pop  r12
    pop  rdi
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Interactive Booth Multiplication Demo] co3_demo_booth
# Purpose   : Reads M and Q from console and prints the 8-cycle Booth trace table.
# Game Role : Lets students enter Gunship Charge (M) & Velocity (Q) to trace bits.
# ------------------------------------------------------------------------------
.global co3_demo_booth
.global _co3_demo_booth
co3_demo_booth:
_co3_demo_booth:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 32

    lea  rcx, [rip + prompt_booth_m]
    call print_str
    call read_int
    mov  rbx, rax

    lea  rcx, [rip + prompt_booth_q]
    call print_str
    call read_int
    mov  rsi, rax

    mov  rcx, rbx
    mov  rdx, rsi
    mov  r8,  1                 # WHY: verbose=1 prints the full cycle-by-cycle binary trace
    call asm_booth_multiply

    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret
