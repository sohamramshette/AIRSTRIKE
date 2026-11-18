# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO2: ARITHMETIC PROGRAMS
# Implements:
#   1. 64-bit Addition and Subtraction
#   2. 64-bit Multiplication and Division (with 64-bit Quotient & Remainder)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
add_sub_title:
    .asciz "\n--- CO2 Program 1: 64-bit Addition & Subtraction ---\n"
add_sub_fmt:
    .asciz "  Operands: A = %lld, B = %lld\n  Addition (A + B)    = %lld\n  Subtraction (A - B) = %lld\n"

mul_div_title:
    .asciz "\n--- CO2 Program 2: 64-bit Multiplication & Division ---\n"
mul_div_fmt:
    .asciz "  Operands: Dividend A = %lld, Divisor B = %lld\n  Multiplication (A * B) = %lld\n  Division (A / B)       = %lld (Quotient: %lld, Remainder: %lld)\n"
div_by_zero_msg:
    .asciz "  [ERROR] Cannot divide by zero in 64-bit IDIV!\n"

.bss
.align 8
.lcomm rem_out_x64, 8

.text

# ==============================================================================
# Routine: asm_add_x64
# - Small Meaning: 64-bit integer addition
# - Long Form    : Assembly 64-bit Addition
# - Why We Used It: High-speed combat score accumulator combining player base and bonus.
# Calling Convention (Microsoft x64 ABI):
#   RCX = a (int64_t)
#   RDX = b (int64_t)
# Returns: RAX = a + b (64-bit signed sum)
# ==============================================================================
.global asm_add_x64
.global _asm_add_x64
asm_add_x64:
_asm_add_x64:
    # --------------------------------------------------------------------------
    # MOV RAX, RCX:
    # - Small Meaning: Load 1st parameter into accumulator
    # - Long Form    : Move Register C Extended to Register A Extended
    # - Why We Used It: Places operand A in RAX to prepare for arithmetic.
    # --------------------------------------------------------------------------
    mov rax, rcx

    # --------------------------------------------------------------------------
    # ADD RAX, RDX:
    # - Small Meaning: Add 2nd parameter to accumulator
    # - Long Form    : 64-bit Integer Add (RAX = RAX + RDX)
    # - Why We Used It: Performs two's complement 64-bit binary addition in hardware.
    # --------------------------------------------------------------------------
    add rax, rdx
    ret

# ==============================================================================
# Routine: asm_sub_x64
# - Small Meaning: 64-bit integer subtraction
# - Long Form    : Assembly 64-bit Subtraction
# - Why We Used It: Calculates damage deductions and shield energy consumption.
# Calling Convention:
#   RCX = a (int64_t)
#   RDX = b (int64_t)
# Returns: RAX = a - b (64-bit signed difference)
# ==============================================================================
.global asm_sub_x64
.global _asm_sub_x64
asm_sub_x64:
_asm_sub_x64:
    mov rax, rcx                # RAX = a
    # --------------------------------------------------------------------------
    # SUB RAX, RDX:
    # - Small Meaning: Subtract B from A
    # - Long Form    : 64-bit Integer Subtract (RAX = RAX - RDX)
    # - Why We Used It: Computes score difference and evaluates condition flags.
    # --------------------------------------------------------------------------
    sub rax, rdx
    ret

# ==============================================================================
# Routine: asm_mul_x64
# - Small Meaning: 64-bit signed integer multiplication
# - Long Form    : Assembly 64-bit Signed Multiply
# - Why We Used It: Computes score combo multipliers and explosion radius vectors.
# Calling Convention:
#   RCX = a (int64_t)
#   RDX = b (int64_t)
# Returns: RAX = a * b (64-bit signed product)
# ==============================================================================
.global asm_mul_x64
.global _asm_mul_x64
asm_mul_x64:
_asm_mul_x64:
    mov rax, rcx
    # --------------------------------------------------------------------------
    # IMUL RAX, RDX:
    # - Small Meaning: Signed integer multiplication
    # - Long Form    : 64-bit Integer Multiply
    # - Why We Used It: Multiplies two 64-bit signed integers in a single cycle.
    # --------------------------------------------------------------------------
    imul rax, rdx
    ret

# ==============================================================================
# Routine: asm_div_x64
# - Small Meaning: 64-bit signed integer division with remainder
# - Long Form    : Assembly 64-bit Signed Division
# - Why We Used It: Computes player accuracy percentages and spatial matrix cells.
# Calling Convention:
#   RCX = dividend A (int64_t)
#   RDX = divisor  B (int64_t)
#   R8  = outRemainder (int64_t* optional pointer)
# Returns: RAX = Quotient (A / B), Remainder stored at [R8]
# ==============================================================================
.global asm_div_x64
.global _asm_div_x64
asm_div_x64:
_asm_div_x64:
    push rbp
    mov rbp, rsp
    push rbx

    # Check for division by zero
    test rdx, rdx
    jz .L_div_zero_x64

    mov rax, rcx                # RAX = dividend A
    mov rbx, rdx                # RBX = divisor B

    # --------------------------------------------------------------------------
    # CQO:
    # - Small Meaning: Sign-extend RAX into RDX:RAX (128-bit)
    # - Long Form    : Convert Quadword to Octaword
    # - Why We Used It: 64-bit IDIV requires a 128-bit dividend in RDX:RAX.
    #                  If RAX is negative, RDX fills with 1s; if positive, 0s.
    # --------------------------------------------------------------------------
    cqo

    # --------------------------------------------------------------------------
    # IDIV RBX:
    # - Small Meaning: Signed 128-bit by 64-bit division
    # - Long Form    : Signed Integer Divide (RDX:RAX / RBX)
    # - Why We Used It: Computes Quotient -> RAX, and Remainder -> RDX.
    # --------------------------------------------------------------------------
    idiv rbx

    # Store remainder if output pointer (R8) is non-null
    test r8, r8
    jz .L_div_done_x64
    mov [r8], rdx

.L_div_done_x64:
    pop rbx
    pop rbp
    ret

.L_div_zero_x64:
    xor rax, rax                # Return 0 on division by zero
    pop rbx
    pop rbp
    ret

# ==============================================================================
# Routine: demo_arithmetic_ops_x64 / co2_demo_arithmetic
# Interactive demonstration of 64-bit addition, subtraction, mul & div
# ==============================================================================
.global demo_arithmetic_ops_x64
.global _demo_arithmetic_ops_x64
.global co2_demo_arithmetic
.global _co2_demo_arithmetic
co2_demo_arithmetic:
_co2_demo_arithmetic:
demo_arithmetic_ops_x64:
_demo_arithmetic_ops_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 64
    push rbx

    lea rcx, [rip + add_sub_title]
    call printf

    # Test Addition: 5000 + 3200
    mov rcx, 5000
    mov rdx, 3200
    call asm_add_x64
    mov rbx, rax                # Sum = 8200

    # Test Subtraction: 5000 - 3200
    mov rcx, 5000
    mov rdx, 3200
    call asm_sub_x64
    mov r8, rax                 # Diff = 1800

    lea rcx, [rip + add_sub_fmt]
    mov rdx, 5000
    mov r9, rbx                 # Sum
    mov [rsp + 32], r8          # Diff (passed on stack as 5th argument)
    mov r8, 3200
    call printf

    # Test Mul & Div: 450 * 12 and 450 / 12
    lea rcx, [rip + mul_div_title]
    call printf

    mov rcx, 450
    mov rdx, 12
    call asm_mul_x64
    mov rbx, rax                # Product = 5400

    lea r8, [rip + rem_out_x64]
    mov rcx, 450
    mov rdx, 12
    call asm_div_x64
    mov r10, rax                # Quotient = 37
    mov r11, [rip + rem_out_x64]# Remainder = 6

    lea rcx, [rip + mul_div_fmt]
    mov rdx, 450
    mov r8, 12
    mov r9, rbx                 # Product
    mov [rsp + 32], r10         # Quotient
    mov [rsp + 40], r10
    mov [rsp + 48], r11         # Remainder
    call printf

    pop rbx
    add rsp, 64
    pop rbp
    ret
