# ==============================================================================
# File    : COA/src/co3_arithmetic/carry_overflow.s
# Subject : COA-CO3 | Multi-Precision Carry (ADC) & Signed Overflow (OF)
# Topics  : 128-Bit Addition, Add with Carry (ADC), Carry Flag (CF),
#           Signed Arithmetic Overflow, Overflow Flag (OF)
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co3_arith_header:
    .asciz "\n==============================================================================\n   CO3: 128-BIT ADDITION WITH CARRY (ADC) & SIGNED OVERFLOW (OF FLAG)         \n==============================================================================\n"
sec_carry_title:
    .asciz "\n--- 1. 128-BIT MULTI-PRECISION ADDITION WITH CARRY (ADC) ---\n"
carry_demo_fmt:
    .asciz "  0x%016llX %016llX + 0x%016llX %016llX\n  = 0x%016llX %016llX (Low Carry CF = %lld)\n"
sec_overflow_title:
    .asciz "\n--- 2. SIGNED OVERFLOW FLAG (OF) DETECTION ---\n"
overflow_case1_fmt:
    .asciz "  Case 1 (Pos + Pos -> Neg): %lld + %lld = %lld -> OF = %lld\n"
overflow_case2_fmt:
    .asciz "  Case 2 (Neg + Neg -> Pos): %lld + %lld = %lld -> OF = %lld\n"
no_overflow_fmt:
    .asciz "  Case 3 (Valid Signed Sum): %lld + %lld = %lld -> OF = %lld\n==============================================================================\n"

.text

# ------------------------------------------------------------------------------
# [TOPIC: 128-Bit Multi-Precision Addition (ADD + ADC)] asm_add_with_carry_128
# Purpose   : Adds two 128-bit integers (a_hi:a_lo + b_hi:b_lo) propagating CF.
# Game Role : Enables >64-bit star-sector coordinates & huge match timestamp sums.
# Inputs    : RCX=a_lo, RDX=a_hi, R8=b_lo, R9=b_hi,
#             [rbp+48]=&out_lo, [rbp+56]=&out_hi, [rbp+64]=&out_cf
# ------------------------------------------------------------------------------
.global asm_add_with_carry_128
.global _asm_add_with_carry_128
asm_add_with_carry_128:
_asm_add_with_carry_128:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi

    mov  rax, rcx               # a_lo
    mov  r10, rdx               # a_hi
    mov  rbx, r8                # b_lo
    mov  r11, r9                # b_hi

    add  rax, rbx               # WHY: Add lower 64 bits first; sets Carry Flag (CF=1) if sum spills past 2^64-1
    setc sil                    # WHY: Save low CF for reporting (SETcc/MOVZX do not disturb RFLAGS)
    movzx rsi, sil
    adc  r10, r11               # WHY: Add with Carry (a_hi + b_hi + CF) chains two 64-bit registers into 128-bit math

    mov  rcx, [rbp + 48]        # &out_lo
    test rcx, rcx
    jz   .L_skip_lo
    mov  [rcx], rax
.L_skip_lo:
    mov  rcx, [rbp + 56]        # &out_hi
    test rcx, rcx
    jz   .L_skip_hi
    mov  [rcx], r10
.L_skip_hi:
    mov  rcx, [rbp + 64]        # &out_cf
    test rcx, rcx
    jz   .L_add128_done
    mov  [rcx], rsi

.L_add128_done:
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Carry & Signed Overflow Demo] co3_demo_carry_overflow
# Purpose   : Prints 128-bit ADC carry propagation and 64-bit OF overflow examples.
# Game Role : Shows why games must check CF/OF to stop score/damage wrap-around bugs.
# ------------------------------------------------------------------------------
.global co3_demo_carry_overflow
.global _co3_demo_carry_overflow
co3_demo_carry_overflow:
_co3_demo_carry_overflow:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 96

    lea  rcx, [rip + co3_arith_header]
    call print_str

    lea  rcx, [rip + sec_carry_title]
    call print_str

    lea  rax, [rsp + 64]
    lea  rbx, [rsp + 72]
    lea  rsi, [rsp + 80]
    mov  [rsp + 32], rax
    mov  [rsp + 40], rbx
    mov  [rsp + 48], rsi

    mov  rcx, 0xFFFFFFFFFFFFFFFF
    mov  rdx, 1
    mov  r8,  1
    mov  r9,  2
    call asm_add_with_carry_128

    lea  rcx, [rip + carry_demo_fmt]
    mov  rdx, 1
    mov  r8,  0xFFFFFFFFFFFFFFFF
    mov  r9,  2
    mov  qword ptr [rsp + 32], 1
    mov  rax, [rsp + 72]
    mov  [rsp + 40], rax
    mov  rax, [rsp + 64]
    mov  [rsp + 48], rax
    mov  rax, [rsp + 80]
    mov  [rsp + 56], rax
    call printf

    lea  rcx, [rip + sec_overflow_title]
    call print_str

    # Case 1: Max Positive + 1 -> flips to Negative (OF = 1)
    mov  rax, 0x7FFFFFFFFFFFFFFF
    mov  rbx, 1
    mov  r10, rax
    add  r10, rbx
    seto sil                    # WHY: Detect illegal Pos+Pos=Neg wrap-around via Overflow Flag (OF)
    movzx rsi, sil

    lea  rcx, [rip + overflow_case1_fmt]
    mov  rdx, rax
    mov  r8,  rbx
    mov  r9,  r10
    mov  [rsp + 32], rsi
    call printf

    # Case 2: Min Negative + (-1) -> flips to Positive (OF = 1)
    mov  rax, 0x8000000000000000
    mov  rbx, -1
    mov  r10, rax
    add  r10, rbx
    seto sil                    # WHY: Detect illegal Neg+Neg=Pos wrap-around via OF
    movzx rsi, sil

    lea  rcx, [rip + overflow_case2_fmt]
    mov  rdx, rax
    mov  r8,  rbx
    mov  r9,  r10
    mov  [rsp + 32], rsi
    call printf

    # Case 3: Valid signed sum within bounds (OF = 0)
    mov  rax, 5000000000
    mov  rbx, 3000000000
    mov  r10, rax
    add  r10, rbx
    seto sil
    movzx rsi, sil

    lea  rcx, [rip + no_overflow_fmt]
    mov  rdx, rax
    mov  r8,  rbx
    mov  r9,  r10
    mov  [rsp + 32], rsi
    call printf

    add  rsp, 96
    pop  rsi
    pop  rbx
    pop  rbp
    ret
