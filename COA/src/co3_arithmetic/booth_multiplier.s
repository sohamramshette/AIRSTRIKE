# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO3: BOOTH'S MULTIPLICATION ALGORITHM
# Implements Signed Two's Complement Binary Multiplication from First Principles
# Does NOT use processor hardware MUL or IMUL instructions for multiplication logic.
# Satisfies Course Outcome: COA-CO3 (Computer Arithmetic - Signed Multiplication)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
booth_hdr:
    .asciz "\n==============================================================================\n        CO3: BOOTH'S SIGNED BINARY MULTIPLICATION ALGORITHM (64-BIT)          \n==============================================================================\n"
booth_inputs_fmt:
    .asciz "  Multiplicand (M) = %lld (0x%02llX | Binary: "
booth_inputs_q:
    .asciz ")\n  Multiplier   (Q) = %lld (0x%02llX | Binary: "
booth_table_hdr:
    .asciz ")\n\n  +-------+-------------------+----------+----------+-----+----------------+\n  | Cycle | Operation         |    A     |    Q     | Q-1 | Action         |\n  +-------+-------------------+----------+----------+-----+----------------+\n"
booth_row_fmt:
    .asciz "  |   %lld   | %-17s | "
booth_row_mid:
    .asciz " | "
booth_row_end:
    .asciz " |  %lld  | %-14s |\n"
booth_table_footer:
    .asciz "  +-------+-------------------+----------+----------+-----+----------------+\n"
booth_final_fmt:
    .asciz "  Final Combined Product (A:Q) = 0x%04llX (%lld in decimal)\n  Theoretical Verification     : %lld * %lld = %lld -> %s\n"
str_pass:
    .asciz "MATCH (PASS)"
str_fail:
    .asciz "MISMATCH (FAIL)"

str_init:
    .asciz "Initial State"
str_add_ashr:
    .asciz "A = A + M, ASHR"
str_sub_ashr:
    .asciz "A = A - M, ASHR"
str_ashr_only:
    .asciz "ASHR (No Add/Sub)"
str_action_add:
    .asciz "Q0,Q-1 = 01"
str_action_sub:
    .asciz "Q0,Q-1 = 10"
str_action_shift:
    .asciz "Q0,Q-1 = 00/11"

prompt_booth_m:
    .asciz "  Enter signed Multiplicand M (-128 to 127): "
prompt_booth_q:
    .asciz "  Enter signed Multiplier   Q (-128 to 127): "

.bss
.align 8
.lcomm booth_m_val, 8
.lcomm booth_q_val, 8

.text

# ==============================================================================
# Routine: asm_booth_multiply
# - Small Meaning: Executes 8-cycle Booth's signed binary multiplication algorithm.
# - Long Form    : Assembly 64-Bit Booth Signed Binary Multiplication
# - Why We Used It: Multiplies signed combat damage multipliers without IMUL hardware.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t M       (Signed Multiplicand, -128..127)
#   RDX = int64_t Q       (Signed Multiplier,   -128..127)
#   R8  = int64_t verbose (1 = print step-by-step table, 0 = silent evaluation)
# Returns:
#   RAX = 64-bit sign-extended 16-bit product (A:Q)
# ==============================================================================
.global asm_booth_multiply
.global _asm_booth_multiply
asm_booth_multiply:
_asm_booth_multiply:
    # --------------------------------------------------------------------------
    # Frame setup and non-volatile register preservation
    # --------------------------------------------------------------------------
    push rbp
    mov rbp, rsp
    sub rsp, 80                 # Local variable storage and shadow space
    push rbx
    push rsi
    push rdi
    push r12
    push r13
    push r14
    push r15

    # Register allocation:
    # R12 = Multiplicand M (sign-extended byte)
    # R13 = Multiplier Q   (low 8 bits in R13B)
    # R14 = Verbose flag
    # RBX = Accumulator A  (initially 0)
    # R15 = Q_prev (Q-1, initially 0)
    # [rbp - 8]  = Cycle counter (1..8)
    # [rbp - 16] = op_str pointer
    # [rbp - 24] = action_str pointer

    movsx r12, cl               # R12 = M (8-bit signed integer sign-extended to 64 bits)
    movzx r13, dl               # R13 = Q (8-bit value zero-extended)
    mov r14, r8                 # R14 = verbose flag
    xor rbx, rbx                # RBX = Accumulator A = 0
    xor r15, r15                # R15 = Q-1 = 0
    mov qword ptr [rbp - 8], 1  # Cycle = 1

    test r14, r14
    jz .L_booth_loop            # Silent mode skips table printing

    # Print Booth header and inputs
    lea rcx, [rip + booth_hdr]
    call print_str

    lea rcx, [rip + booth_inputs_fmt]
    mov rdx, r12
    movzx r8, r12b
    call printf

    movzx rcx, r12b
    call print_bin8

    lea rcx, [rip + booth_inputs_q]
    movsx rdx, r13b
    movzx r8, r13b
    call printf

    movzx rcx, r13b
    call print_bin8

    lea rcx, [rip + booth_table_hdr]
    call print_str

    # Print Cycle 0 (Initial State)
    lea rcx, [rip + booth_row_fmt]
    xor rdx, rdx                # Cycle 0
    lea r8,  [rip + str_init]
    call printf

    xor rcx, rcx                # A = 0
    call print_bin8

    lea rcx, [rip + booth_row_mid]
    call print_str

    movzx rcx, r13b             # Q
    call print_bin8

    lea rcx, [rip + booth_row_end]
    xor rdx, rdx                # Q-1 = 0
    lea r8,  [rip + str_init]
    call printf

.L_booth_loop:
    # --------------------------------------------------------------------------
    # Inspect lowest bit of Q (Q0) and previous bit (Q-1)
    # Pair = (Q0 << 1) | (Q-1)
    # --------------------------------------------------------------------------
    mov rax, r13
    and rax, 1                  # RAX = Q0
    shl rax, 1                  # RAX = Q0 << 1
    or  rax, r15                # RAX = (Q0 << 1) | Q-1

    cmp rax, 1                  # 01 -> A = A + M, then ASHR
    je .L_booth_op_add
    cmp rax, 2                  # 10 -> A = A - M, then ASHR
    je .L_booth_op_sub

    # 00 or 11 -> No arithmetic, ASHR only
    lea rax, [rip + str_ashr_only]
    mov [rbp - 16], rax
    lea rax, [rip + str_action_shift]
    mov [rbp - 24], rax
    jmp .L_booth_do_shift

.L_booth_op_add:
    # --------------------------------------------------------------------------
    # ADD RBX, R12:
    # - Small Meaning: Add Multiplicand M into Accumulator A
    # - Long Form    : Add Register R12 to Register Base (Accumulator)
    # - Why We Used It: Booth algorithm step when transitioning from 1 to 0 (Q0=0, Q-1=1).
    # --------------------------------------------------------------------------
    add rbx, r12
    movsx rbx, bl               # Keep signed 8-bit accumulator
    lea rax, [rip + str_add_ashr]
    mov [rbp - 16], rax
    lea rax, [rip + str_action_add]
    mov [rbp - 24], rax
    jmp .L_booth_do_shift

.L_booth_op_sub:
    # --------------------------------------------------------------------------
    # SUB RBX, R12:
    # - Small Meaning: Subtract Multiplicand M from Accumulator A
    # - Long Form    : Subtract Register R12 from Register Base (Accumulator)
    # - Why We Used It: Booth algorithm step when transitioning from 0 to 1 (Q0=1, Q-1=0).
    # --------------------------------------------------------------------------
    sub rbx, r12
    movsx rbx, bl               # Keep signed 8-bit accumulator
    lea rax, [rip + str_sub_ashr]
    mov [rbp - 16], rax
    lea rax, [rip + str_action_sub]
    mov [rbp - 24], rax

.L_booth_do_shift:
    # --------------------------------------------------------------------------
    # Arithmetic Right Shift of combined register pair [A : Q : Q-1]
    # --------------------------------------------------------------------------
    # 1. Update Q-1 = LSB of Q
    mov r15, r13
    and r15, 1

    # 2. Shift Q right by 1, and copy LSB of A into MSB of Q (bit 7)
    mov rax, rbx
    and rax, 1                  # LSB of A
    shl rax, 7                  # Position at bit 7
    shr r13, 1
    and r13, 0x7F
    or  r13, rax                # Combine into new Q

    # 3. Arithmetic shift right Accumulator A by 1 (preserving sign bit)
    sar bl, 1
    movsx rbx, bl

    # 4. Print row if verbose
    test r14, r14
    jz .L_booth_loop_next

    lea rcx, [rip + booth_row_fmt]
    mov rdx, [rbp - 8]          # Cycle index
    mov r8,  [rbp - 16]         # op_str
    call printf

    movzx rcx, bl               # A byte
    call print_bin8

    lea rcx, [rip + booth_row_mid]
    call print_str

    movzx rcx, r13b             # Q byte
    call print_bin8

    lea rcx, [rip + booth_row_end]
    mov rdx, r15                # Q-1
    mov r8,  [rbp - 24]         # action_str
    call printf

.L_booth_loop_next:
    inc qword ptr [rbp - 8]
    cmp qword ptr [rbp - 8], 8
    jle .L_booth_loop

    # --------------------------------------------------------------------------
    # Combine final 16-bit signed product: (A << 8) | (Q & 0xFF)
    # --------------------------------------------------------------------------
    mov rax, rbx
    and rax, 0xFF
    shl rax, 8
    mov rdx, r13
    and rdx, 0xFF
    or  rax, rdx
    movsx rax, ax               # RAX = signed 16-bit integer sign-extended to 64 bits

    test r14, r14
    jz .L_booth_exit            # If silent, return RAX immediately

    mov rsi, rax                # RSI = preserved computed product

    lea rcx, [rip + booth_table_footer]
    call print_str

    # Compute expected product = M * Q via standard arithmetic for verification
    movsx rdx, r13b
    mov rax, r12
    imul rax, rdx               # RAX = expected product

    lea rdi, [rip + str_pass]
    cmp rsi, rax
    je .L_booth_print_res
    lea rdi, [rip + str_fail]

.L_booth_print_res:
    lea rcx, [rip + booth_final_fmt]
    movzx rdx, si               # Hex product
    mov r8,  rsi                # Decimal product
    mov r9,  r12                # M
    mov [rsp + 32], rdx         # Q
    mov [rsp + 40], rax         # Expected product
    mov [rsp + 48], rdi         # Pass/Fail string
    call printf

    mov rax, rsi                # Return final computed product

.L_booth_exit:
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
# Routine: co3_demo_booth
# - Small Meaning: Interactive demonstration runner for Booth's algorithm.
# - Long Form    : CO3 Demonstration Driver for Signed Booth's Multiplication
# - Why We Used It: Accepts user multiplicand and multiplier and displays execution table.
# ==============================================================================
.global co3_demo_booth
.global _co3_demo_booth
co3_demo_booth:
_co3_demo_booth:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx
    push rsi

    lea rcx, [rip + prompt_booth_m]
    call print_str
    call read_int
    mov rbx, rax                # RBX = M

    lea rcx, [rip + prompt_booth_q]
    call print_str
    call read_int
    mov rsi, rax                # RSI = Q

    mov rcx, rbx                # 1st: M
    mov rdx, rsi                # 2nd: Q
    mov r8,  1                  # 3rd: verbose = 1 (Display table)
    call asm_booth_multiply

    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
