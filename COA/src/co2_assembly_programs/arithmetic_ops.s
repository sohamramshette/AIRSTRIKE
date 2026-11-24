# ==============================================================================
# File    : COA/src/co2_assembly_programs/arithmetic_ops.s
# Subject : COA-CO2 | Programs 1 & 2: Basic Integer Arithmetic
# Topics  : Addition (ADD), Subtraction (SUB), Signed Multiplication (IMUL),
#           Signed Division (IDIV), Sign Extension (CQO), Divide-by-Zero Guard
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
add_sub_title:
    .asciz "\n--- CO2 Program 1: 64-Bit Addition & Subtraction ---\n"
add_sub_fmt:
    .asciz "  Operands: A = %lld, B = %lld\n  Addition (A + B)    = %lld\n  Subtraction (A - B) = %lld\n"

mul_div_title:
    .asciz "\n--- CO2 Program 2: 64-Bit Multiplication & Division ---\n"
mul_div_fmt:
    .asciz "  Operands: Dividend A = %lld, Divisor B = %lld\n  Multiplication (A * B) = %lld\n  Division (A / B)       = %lld (Quotient: %lld, Remainder: %lld)\n"

.bss
.align 8
.lcomm rem_out_x64, 8

.text

# ------------------------------------------------------------------------------
# [TOPIC: Addition (ADD)] asm_add_x64
# Purpose   : Computes 64-bit signed sum (a + b).
# Game Role : Adds enemy kill points and power-up bonuses to player score.
# Inputs    : RCX = a, RDX = b
# Output    : RAX = a + b
# ------------------------------------------------------------------------------
.global asm_add_x64
.global _asm_add_x64
asm_add_x64:
_asm_add_x64:
    mov  rax, rcx               # WHY: Load 1st arg into accumulator RAX (where Windows x64 returns results)
    add  rax, rdx               # WHY: Single-cycle hardware two's complement addition -> Game: Accumulate score
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Subtraction (SUB)] asm_sub_x64
# Purpose   : Computes 64-bit signed difference (a - b).
# Game Role : Deducts incoming laser damage from ship HP/shield reserves.
# Inputs    : RCX = a, RDX = b
# Output    : RAX = a - b
# ------------------------------------------------------------------------------
.global asm_sub_x64
.global _asm_sub_x64
asm_sub_x64:
_asm_sub_x64:
    mov  rax, rcx
    sub  rax, rdx               # WHY: Hardware subtraction sets RFLAGS too -> Game: Remaining hull/shield HP
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Signed Multiplication (IMUL)] asm_mul_x64
# Purpose   : Computes 64-bit signed product (a * b).
# Game Role : Scales base kill points by combo multipliers and wave difficulty.
# Inputs    : RCX = a, RDX = b
# Output    : RAX = a * b
# ------------------------------------------------------------------------------
.global asm_mul_x64
.global _asm_mul_x64
asm_mul_x64:
_asm_mul_x64:
    mov  rax, rcx
    imul rax, rdx               # WHY: Use signed IMUL (not unsigned MUL) so negative deltas keep their sign
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Signed Division (IDIV) & Sign Extension (CQO)] asm_div_x64
# Purpose   : Computes quotient (RAX) and remainder (RDX) safely, guarding div/0.
# Game Role : Calculates accuracy ratios and screen grid wrapping (modulo) in 1 step.
# Inputs    : RCX = dividend A, RDX = divisor B, R8 = int64_t* outRemainder
# Output    : RAX = quotient (returns 0 and *outRemainder = 0 if divisor == 0)
# ------------------------------------------------------------------------------
.global asm_div_x64
.global _asm_div_x64
asm_div_x64:
_asm_div_x64:
    push rbx

    test rdx, rdx               # WHY: Guard against divisor == 0 because CPU throws fatal #DE exception on div/0
    jz   .L_div_by_zero

    mov  rax, rcx
    mov  rbx, rdx
    cqo                         # WHY: IDIV divides 128-bit RDX:RAX; CQO sign-extends RAX into RDX so stale RDX bits don't corrupt division
    idiv rbx                    # WHY: Produces BOTH quotient (RAX) and remainder (RDX) in one instruction

    test r8, r8                 # WHY: Only write remainder if caller provided a non-null pointer
    jz   .L_div_done
    mov  [r8], rdx
.L_div_done:
    pop  rbx
    ret

.L_div_by_zero:
    test r8, r8
    jz   .L_div_zero_ret
    mov  qword ptr [r8], 0
.L_div_zero_ret:
    xor  rax, rax               # WHY: Return safe 0 instead of crashing on 0 shots fired
    pop  rbx
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Addition & Subtraction Demo] co2_demo_add_sub
# Purpose   : Demonstrates Program 1 (Add & Sub).
# Game Role : Shows score accumulation and damage subtraction routines.
# ------------------------------------------------------------------------------
.global co2_demo_add_sub
.global _co2_demo_add_sub
co2_demo_add_sub:
_co2_demo_add_sub:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 48

    lea  rcx, [rip + add_sub_title]
    call printf

    mov  rcx, 5000
    mov  rdx, 3200
    call asm_add_x64
    mov  rbx, rax               # Sum = 8200

    mov  rcx, 5000
    mov  rdx, 3200
    call asm_sub_x64
    mov  rsi, rax               # Diff = 1800

    lea  rcx, [rip + add_sub_fmt]
    mov  rdx, 5000
    mov  r8,  3200
    mov  r9,  rbx
    mov  [rsp + 32], rsi
    call printf

    add  rsp, 48
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Multiplication & Division Demo] co2_demo_mul_div
# Purpose   : Demonstrates Program 2 (Mul & Div with Quotient/Remainder).
# Game Role : Shows multiplier scaling and quotient/remainder splitting.
# ------------------------------------------------------------------------------
.global co2_demo_mul_div
.global _co2_demo_mul_div
co2_demo_mul_div:
_co2_demo_mul_div:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 64

    lea  rcx, [rip + mul_div_title]
    call printf

    mov  rcx, 450
    mov  rdx, 12
    call asm_mul_x64
    mov  rbx, rax               # Product = 5400

    mov  rcx, 450
    mov  rdx, 12
    lea  r8,  [rip + rem_out_x64]
    call asm_div_x64
    mov  rsi, rax               # Quotient = 37
    mov  r11, [rip + rem_out_x64]

    lea  rcx, [rip + mul_div_fmt]
    mov  rdx, 450
    mov  r8,  12
    mov  r9,  rbx
    mov  [rsp + 32], rsi
    mov  [rsp + 40], rsi
    mov  [rsp + 48], r11
    call printf

    add  rsp, 64
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Combined Arithmetic Demo] co2_demo_arithmetic
# Purpose   : Runs both Program 1 and Program 2 sequentially.
# Game Role : Verifies all 4 core arithmetic operators.
# ------------------------------------------------------------------------------
.global co2_demo_arithmetic
.global _co2_demo_arithmetic
.global demo_arithmetic_ops_x64
.global _demo_arithmetic_ops_x64
co2_demo_arithmetic:
_co2_demo_arithmetic:
demo_arithmetic_ops_x64:
_demo_arithmetic_ops_x64:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    call co2_demo_add_sub
    call co2_demo_mul_div

    add  rsp, 32
    pop  rbp
    ret
