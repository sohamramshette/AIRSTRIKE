# ==============================================================================
# File    : COA/src/co3_arithmetic/restoring_divider.s
# Subject : COA-CO3 | Restoring Binary Division Algorithm
# Topics  : Restoring Division, Unsigned Binary Division, Partial Remainder,
#           Shift-Left & Restore Step, Divide-by-Zero Guard
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
div_hdr:
    .asciz "\n==============================================================================\n            CO3: RESTORING BINARY DIVISION ALGORITHM (8-CYCLE)                \n==============================================================================\n"
div_inputs_fmt:   .asciz "  Dividend (Q) = %llu (0x%02llX | Binary: "
div_inputs_m:     .asciz ")\n  Divisor  (M) = %llu (0x%02llX | Binary: "
div_table_hdr:
    .asciz ")\n\n  +-------+--------------------+----------+----------+----------------------------+\n  | Cycle | Operation          |    A     |    Q     | Decision                   |\n  +-------+--------------------+----------+----------+----------------------------+\n"
div_row_fmt:      .asciz "  |   %lld   | %-18s | "
div_row_mid:      .asciz " | "
div_row_end:      .asciz " | %-26s |\n"
div_table_footer: .asciz "  +-------+--------------------+----------+----------+----------------------------+\n"
div_final_fmt:
    .asciz "  Quotient (Q) = %llu (0x%02llX) | Remainder (A) = %llu (0x%02llX)\n  Verify: %llu = (%llu * %llu) + %llu -> %s\n"
div_err_zero:     .asciz "  [ERROR] Division by zero is undefined!\n"

str_pass:         .asciz "MATCH (PASS)"
str_fail:         .asciz "MISMATCH (FAIL)"
str_div_init:     .asciz "Initial State"
str_div_restore:  .asciz "A < 0: Restore A, Q0=0"
str_div_keep:     .asciz "A >= 0: Keep A, Q0=1"
str_shift_sub:    .asciz "SHL(A,Q), A=A-M"

prompt_div_q:     .asciz "  Enter unsigned Dividend Q (0..255): "
prompt_div_m:     .asciz "  Enter unsigned Divisor  M (1..255): "

.bss
.align 8
.global div_out_rem
div_out_rem: .space 8

.text

# ------------------------------------------------------------------------------
# [TOPIC: Restoring Binary Division Algorithm] asm_restoring_divide
# Purpose   : Divides unsigned 8-bit dividend by divisor in 8 cycles using
#             shift-left, trial subtract, and restore—without hardware DIV/IDIV.
# Game Role : Computes fuel burn ratios & delta-V splits at ALU gate level.
# Inputs    : RCX=dividend (0..255), RDX=divisor (1..255), R8=&out_rem, R9=verbose
# Output    : RAX = quotient Q (returns 0 and *out_rem=0 if divisor == 0)
# ------------------------------------------------------------------------------
.global asm_restoring_divide
.global _asm_restoring_divide
asm_restoring_divide:
_asm_restoring_divide:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push rdi
    push r12
    push r13
    push r14
    push r15
    sub  rsp, 120               # WHY: 7 pushes (56) + 120 = 176 -> strictly 16-byte aligned for printf

    movzx r12, cl               # WHY: Mask to 8-bit unsigned Dividend Q (also holds final Quotient)
    movzx r13, dl               # WHY: Mask to 8-bit unsigned Divisor M
    mov  r14, r8
    mov  r15, r9
    mov  [rsp + 96], r12        # WHY: Preserve initial dividend to verify Q*M + A == Dividend at end

    test r13, r13               # WHY: Guard against division by zero (undefined & infinite loop otherwise)
    jnz  .L_div_valid

    test r14, r14
    jz   .L_div_zero_msg
    mov  qword ptr [r14], 0
.L_div_zero_msg:
    test r15, r15
    jz   .L_div_zero_ret
    lea  rcx, [rip + div_err_zero]
    call print_str
.L_div_zero_ret:
    xor  rax, rax
    jmp  .L_div_exit

.L_div_valid:
    xor  rbx, rbx               # WHY: Partial remainder A starts at 0
    mov  qword ptr [rsp + 80], 1

    test r15, r15
    jz   .L_div_loop

    lea  rcx, [rip + div_hdr]
    call print_str
    lea  rcx, [rip + div_inputs_fmt]
    mov  rdx, r12
    mov  r8,  r12
    call printf
    mov  rcx, r12
    call print_bin8
    lea  rcx, [rip + div_inputs_m]
    mov  rdx, r13
    mov  r8,  r13
    call printf
    mov  rcx, r13
    call print_bin8
    lea  rcx, [rip + div_table_hdr]
    call print_str

    lea  rcx, [rip + div_row_fmt]
    xor  rdx, rdx
    lea  r8,  [rip + str_div_init]
    call printf
    xor  rcx, rcx
    call print_bin8
    lea  rcx, [rip + div_row_mid]
    call print_str
    mov  rcx, r12
    call print_bin8
    lea  rcx, [rip + div_row_end]
    lea  rdx, [rip + str_div_init]
    call printf

.L_div_loop:
    # Step 1: Shift [A : Q] left 1 bit so next dividend bit enters bottom of A
    mov  rax, r12
    shr  rax, 7
    and  rax, 1                 # WHY: Grab top bit (MSB) of Q before shifting Q left
    shl  r12, 1
    and  r12, 0xFF              # WHY: Keep Q within 8 bits (vacated bit 0 becomes next quotient bit)
    shl  rbx, 1
    or   rbx, rax               # WHY: Bring Q's old MSB into bit 0 of Accumulator A

    # Step 2: Trial subtraction A = A - M
    sub  rbx, r13               # WHY: Test whether Divisor M fits into partial remainder A

    # Step 3: Check sign of A -> Restore if negative, set Q0=1 if non-negative
    cmp  rbx, 0
    jl   .L_div_restore

    or   r12, 1                 # WHY: A >= 0 means M fit -> keep A and record quotient bit Q0 = 1
    lea  rax, [rip + str_div_keep]
    mov  [rsp + 88], rax
    jmp  .L_div_print_row

.L_div_restore:
    add  rbx, r13               # WHY: A < 0 means we subtracted too much -> RESTORE A = A + M (Q0 stays 0)
    lea  rax, [rip + str_div_restore]
    mov  [rsp + 88], rax

.L_div_print_row:
    test r15, r15
    jz   .L_div_next

    lea  rcx, [rip + div_row_fmt]
    mov  rdx, [rsp + 80]
    lea  r8,  [rip + str_shift_sub]
    call printf
    movzx rcx, bl
    call print_bin8
    lea  rcx, [rip + div_row_mid]
    call print_str
    movzx rcx, r12b
    call print_bin8
    lea  rcx, [rip + div_row_end]
    mov  rdx, [rsp + 88]
    call printf

.L_div_next:
    inc  qword ptr [rsp + 80]
    cmp  qword ptr [rsp + 80], 8 # WHY: Run 8 cycles for 8-bit unsigned dividend
    jle  .L_div_loop

    test r14, r14
    jz   .L_div_skip_rem
    mov  [r14], rbx             # WHY: Final Accumulator A holds the exact Remainder
.L_div_skip_rem:
    test r15, r15
    jz   .L_div_done

    lea  rcx, [rip + div_table_footer]
    call print_str

    mov  rax, r12
    imul rax, r13
    add  rax, rbx               # WHY: Verify (Quotient * Divisor + Remainder) == Original Dividend
    mov  rsi, [rsp + 96]
    lea  rdi, [rip + str_pass]
    cmp  rax, rsi
    je   .L_div_print_final
    lea  rdi, [rip + str_fail]

.L_div_print_final:
    lea  rcx, [rip + div_final_fmt]
    mov  rdx, r12
    mov  r8,  r12
    mov  r9,  rbx
    mov  [rsp + 32], rbx
    mov  [rsp + 40], rsi
    mov  [rsp + 48], r12
    mov  [rsp + 56], r13
    mov  [rsp + 64], rbx
    mov  [rsp + 72], rdi
    call printf

.L_div_done:
    mov  rax, r12               # WHY: Return Quotient Q in RAX

.L_div_exit:
    add  rsp, 120
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
# [TOPIC: Interactive Restoring Division Demo] co3_demo_restoring_div
# Purpose   : Reads Q and M (defaults 29 / 5) and displays step-by-step trace.
# Game Role : Interactive trace of Restoring Division steps (shift, sub, restore).
# ------------------------------------------------------------------------------
.global co3_demo_restoring_div
.global _co3_demo_restoring_div
co3_demo_restoring_div:
_co3_demo_restoring_div:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 32

    lea  rcx, [rip + prompt_div_q]
    call print_str
    call read_int
    cmp  rax, 0
    jge  .L_q_ok
    mov  rax, 29
.L_q_ok:
    mov  rbx, rax

    lea  rcx, [rip + prompt_div_m]
    call print_str
    call read_int
    cmp  rax, 1
    jge  .L_m_ok
    mov  rax, 5
.L_m_ok:
    mov  rsi, rax

    mov  rcx, rbx
    mov  rdx, rsi
    lea  r8,  [rip + div_out_rem]
    mov  r9,  1
    call asm_restoring_divide

    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret
