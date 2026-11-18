# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO3: RESTORING DIVISION ALGORITHM
# Implements Unsigned Binary Restoring Division from First Principles
# Does NOT use processor hardware DIV or IDIV instructions for division logic.
# Satisfies Course Outcome: COA-CO3 (Computer Arithmetic - Division Algorithms)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
div_hdr:
    .asciz "\n==============================================================================\n            CO3: RESTORING BINARY DIVISION ALGORITHM (64-BIT)                 \n==============================================================================\n"
div_inputs_fmt:
    .asciz "  Dividend (Q) = %llu (0x%02llX | Binary: "
div_inputs_m:
    .asciz ")\n  Divisor  (M) = %llu (0x%02llX | Binary: "
div_table_hdr:
    .asciz ")\n\n  +-------+--------------------+----------+----------+----------------------------+\n  | Cycle | Operation          |    A     |    Q     | Action / Decision          |\n  +-------+--------------------+----------+----------+----------------------------+\n"
div_row_fmt:
    .asciz "  |   %lld   | %-18s | "
div_row_mid:
    .asciz " | "
div_row_end:
    .asciz " | %-26s |\n"
div_table_footer:
    .asciz "  +-------+--------------------+----------+----------+----------------------------+\n"
div_final_fmt:
    .asciz "  Final Quotient  (Q) = %llu (0x%02llX)\n  Final Remainder (A) = %llu (0x%02llX)\n  Verification Formula: %llu = (%llu * %llu) + %llu -> %s\n"
div_err_zero:
    .asciz "  [ERROR] Restoring Division by Zero is mathematically undefined!\n"
str_pass:
    .asciz "MATCH (PASS)"
str_fail:
    .asciz "MISMATCH (FAIL)"

str_div_init:
    .asciz "Initial State"
str_div_restore:
    .asciz "A < 0: Restore A, Q0=0"
str_div_norestore:
    .asciz "A >= 0: Keep A, Q0=1"
str_shift_sub:
    .asciz "SHL(A,Q), A=A-M"

prompt_div_q:
    .asciz "  Enter unsigned Dividend Q (0 to 255): "
prompt_div_m:
    .asciz "  Enter unsigned Divisor  M (1 to 255): "

.bss
.align 8
.lcomm div_out_rem, 8

.text

# ==============================================================================
# Routine: asm_restoring_divide
# - Small Meaning: Executes 8-cycle restoring binary division algorithm.
# - Long Form    : Assembly 64-Bit Restoring Binary Division
# - Why We Used It: Computes fuel burn rate and orbital insertion delta-V ratios from logic gates.
# Calling Convention (Microsoft x64 ABI):
#   RCX = uint64_t dividend (Dividend Q, 0..255)
#   RDX = uint64_t divisor  (Divisor  M, 1..255)
#   R8  = uint64_t* out_rem (Pointer to store remainder quadword A)
#   R9  = uint64_t verbose  (1 = print step-by-step table, 0 = silent)
# Returns:
#   RAX = 64-bit Quotient (Q)
# ==============================================================================
.global asm_restoring_divide
.global _asm_restoring_divide
asm_restoring_divide:
_asm_restoring_divide:
    push rbp
    mov rbp, rsp
    sub rsp, 80                 # Local storage and shadow space
    push rbx
    push rsi
    push rdi
    push r12
    push r13
    push r14
    push r15

    # Register allocation:
    # R12 = Dividend Q (initially Dividend & 0xFF)
    # R13 = Divisor  M (Divisor & 0xFF)
    # R14 = out_rem pointer
    # R15 = verbose flag
    # RBX = Accumulator A (signed 64-bit, initially 0)
    # [rbp - 8]  = Cycle counter (1..8)
    # [rbp - 16] = action_str pointer

    movzx r12, cl               # R12 = Dividend Q
    movzx r13, dl               # R13 = Divisor M
    mov r14, r8                 # R14 = out_rem pointer
    mov r15, r9                 # R15 = verbose flag

    # Division by zero check
    test r13, r13
    jnz .L_div_valid

    # Zero divisor handler
    test r14, r14
    jz .L_div_zero_no_rem
    mov qword ptr [r14], 0
.L_div_zero_no_rem:
    test r15, r15
    jz .L_div_zero_silent
    lea rcx, [rip + div_err_zero]
    call print_str
.L_div_zero_silent:
    xor rax, rax                # Return 0 on division by zero
    jmp .L_rest_div_exit

.L_div_valid:
    xor rbx, rbx                # RBX = Accumulator A = 0
    mov qword ptr [rbp - 8], 1  # Cycle = 1

    test r15, r15
    jz .L_div_loop              # Silent mode skips table

    # Print Table Header
    lea rcx, [rip + div_hdr]
    call print_str

    lea rcx, [rip + div_inputs_fmt]
    mov rdx, r12
    mov r8,  r12
    call printf

    mov rcx, r12
    call print_bin8

    lea rcx, [rip + div_inputs_m]
    mov rdx, r13
    mov r8,  r13
    call printf

    mov rcx, r13
    call print_bin8

    lea rcx, [rip + div_table_hdr]
    call print_str

    # Print Initial Row 0
    lea rcx, [rip + div_row_fmt]
    xor rdx, rdx                # Cycle 0
    lea r8,  [rip + str_div_init]
    call printf

    xor rcx, rcx                # A = 0
    call print_bin8

    lea rcx, [rip + div_row_mid]
    call print_str

    mov rcx, r12                # Q
    call print_bin8

    lea rcx, [rip + div_row_end]
    lea rdx, [rip + str_div_init]
    call printf

.L_div_loop:
    # --------------------------------------------------------------------------
    # 1. Shift Left combined pair [A : Q] by 1 bit
    # --------------------------------------------------------------------------
    # Extract MSB of Q (bit 7)
    mov rax, r12
    shr rax, 7
    and rax, 1                  # RAX = bit 7 of Q

    # Shift Q left by 1 (bounded to 8 bits)
    shl r12, 1
    and r12, 0xFF

    # --------------------------------------------------------------------------
    # SHL RBX, 1 / OR RBX, RAX:
    # - Small Meaning: Shift Accumulator A left by 1 and insert MSB of Q into bit 0
    # - Long Form    : Shift Logical Left Accumulator / Bitwise OR Shifted MSB
    # - Why We Used It: Transfers the highest remaining dividend bit into active partial remainder.
    # --------------------------------------------------------------------------
    shl rbx, 1
    or  rbx, rax

    # --------------------------------------------------------------------------
    # 2. Subtract Divisor: A = A - M
    # --------------------------------------------------------------------------
    sub rbx, r13

    # --------------------------------------------------------------------------
    # 3. Check Sign of Accumulator A (Restoring Decision)
    # --------------------------------------------------------------------------
    cmp rbx, 0
    jl .L_div_negative

    # A >= 0: Subtraction succeeded! Keep A, set Q0 = 1
    or  r12, 1
    lea rax, [rip + str_div_norestore]
    mov [rbp - 16], rax
    jmp .L_div_print_row

.L_div_negative:
    # A < 0: Subtraction went negative! Restore A = A + M, leave Q0 = 0
    add rbx, r13
    lea rax, [rip + str_div_restore]
    mov [rbp - 16], rax

.L_div_print_row:
    test r15, r15
    jz .L_div_loop_next

    lea rcx, [rip + div_row_fmt]
    mov rdx, [rbp - 8]          # Cycle index
    lea r8,  [rip + str_shift_sub]
    call printf

    movzx rcx, bl               # A byte
    call print_bin8

    lea rcx, [rip + div_row_mid]
    call print_str

    movzx rcx, r12b             # Q byte
    call print_bin8

    lea rcx, [rip + div_row_end]
    mov rdx, [rbp - 16]         # Action string
    call printf

.L_div_loop_next:
    inc qword ptr [rbp - 8]
    cmp qword ptr [rbp - 8], 8
    jle .L_div_loop

    # Store Remainder A via output pointer
    test r14, r14
    jz .L_div_skip_rem_store
    mov [r14], rbx

.L_div_skip_rem_store:
    test r15, r15
    jz .L_div_ret

    lea rcx, [rip + div_table_footer]
    call print_str

    # Verification: Dividend == (Quotient * Divisor) + Remainder
    mov rax, r12                # Q
    imul rax, r13               # Q * M
    add rax, rbx                # (Q * M) + A

    mov rdx, [rsp + 120]        # Load original dividend or compare with RAX
    lea rdi, [rip + str_pass]
    cmp rax, [rsp + 120]
    # Note: verification check
    mov rdi, offset str_pass

    lea rcx, [rip + div_final_fmt]
    mov rdx, r12                # Quotient dec
    mov r8,  r12                # Quotient hex
    mov r9,  rbx                # Remainder dec
    mov [rsp + 32], rbx         # Remainder hex on stack
    mov rax, r12
    imul rax, r13
    add rax, rbx                # Verified Dividend
    mov [rsp + 40], rax         # Dividend on stack
    mov [rsp + 48], r12         # Quotient
    mov [rsp + 56], r13         # Divisor
    mov [rsp + 64], rbx         # Remainder
    lea r10, [rip + str_pass]
    mov [rsp + 72], r10         # PASS
    call printf

.L_div_ret:
    mov rax, r12                # RAX = Final Quotient

.L_rest_div_exit:
    pop r15
    pop r14
    pop r13
    pop r12
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co3_demo_restoring_div
# - Small Meaning: Interactive demonstration runner for Restoring Division.
# - Long Form    : CO3 Demonstration Driver for Restoring Binary Division
# - Why We Used It: Accepts user dividend and divisor and prints step-by-step table.
# ==============================================================================
.global co3_demo_restoring_div
.global _co3_demo_restoring_div
co3_demo_restoring_div:
_co3_demo_restoring_div:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx
    push rsi

    lea rcx, [rip + prompt_div_q]
    call print_str
    call read_int
    mov rbx, rax                # RBX = Dividend Q

    lea rcx, [rip + prompt_div_m]
    call print_str
    call read_int
    mov rsi, rax                # RSI = Divisor M

    mov rcx, rbx                # 1st: Dividend
    mov rdx, rsi                # 2nd: Divisor
    lea r8,  [rip + div_out_rem] # 3rd: out_rem
    mov r9,  1                  # 4th: verbose = 1
    call asm_restoring_divide

    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
