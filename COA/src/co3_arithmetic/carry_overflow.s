# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO3: CARRY & OVERFLOW DEMONSTRATIONS
# Implements:
#   Prog 3. 128-Bit Multi-Precision Binary Addition with Carry (ADC)
#   Prog 4. Signed 64-Bit Arithmetic Overflow Detection (OF Flag & JO)
# Satisfies Course Outcome: COA-CO3 (Computer Arithmetic - Flags & Multi-Precision)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co3_arith_header:
    .asciz "\n==============================================================================\n   CO3: 128-BIT BINARY ADDITION WITH CARRY (ADC) & 64-BIT SIGNED OVERFLOW    \n==============================================================================\n"

sec_carry_title:
    .asciz "\n--- 1. 128-BIT MULTI-PRECISION BINARY ADDITION WITH CARRY (ADC) ---\n"
carry_demo_fmt:
    .asciz "  128-bit Addition on 64-bit Architecture:\n    Operand 1: 0x%016llX %016llX\n  + Operand 2: 0x%016llX %016llX\n  ---------------------------------------------------------\n    Sum      : 0x%016llX %016llX (Carry between 64-bit quadwords = %lld)\n"

sec_overflow_title:
    .asciz "\n--- 2. SIGNED 64-BIT ARITHMETIC OVERFLOW DEMONSTRATION (OF FLAG) ---\n"
overflow_case1_fmt:
    .asciz "  Case 1: Positive + Positive -> Negative Overflow\n    A = %lld (0x%016llX)\n  + B = %lld (0x%016llX)\n  ---------------------------------------------------------\n    Sum = %lld (0x%016llX) -> OF = %lld (SIGNED OVERFLOW!)\n"
overflow_case2_fmt:
    .asciz "  Case 2: Negative + Negative -> Positive Overflow\n    A = %lld (0x%016llX)\n  + B = %lld (0x%016llX)\n  ---------------------------------------------------------\n    Sum = %lld (0x%016llX) -> OF = %lld (SIGNED OVERFLOW!)\n"
no_overflow_fmt:
    .asciz "  Case 3: Valid 64-Bit Addition (No Overflow)\n    A = %lld (0x%016llX) + B = %lld (0x%016llX) -> Sum = %lld (0x%016llX), OF = %lld (Valid)\n==============================================================================\n"

.text

# ==============================================================================
# Routine: asm_add_with_carry_128
# - Small Meaning: Adds two 128-bit integers using 64-bit ADD and ADC instructions.
# - Long Form    : Assembly 128-Bit Multi-Precision Addition with Carry
# - Why We Used It: Accumulates orbital fleet coordinates and 128-bit combat timestamps.
# Calling Convention (Microsoft x64 ABI):
#   RCX = uint64_t a_lo
#   RDX = uint64_t a_hi
#   R8  = uint64_t b_lo
#   R9  = uint64_t b_hi
#   [RBP + 48] = uint64_t* out_lo (5th parameter)
#   [RBP + 56] = uint64_t* out_hi (6th parameter)
#   [RBP + 64] = int64_t*  out_cf (7th parameter)
# ==============================================================================
.global asm_add_with_carry_128
.global _asm_add_with_carry_128
asm_add_with_carry_128:
_asm_add_with_carry_128:
    push rbp
    mov rbp, rsp
    push rbx
    push rsi
    push rdi
    push r12
    push r13

    mov rax, rcx                # RAX = a_lo
    mov r10, rdx                # R10 = a_hi
    mov rbx, r8                 # RBX = b_lo
    mov r11, r9                 # R11 = b_hi

    # --------------------------------------------------------------------------
    # ADD RAX, RBX:
    # - Small Meaning: Add lower 64-bit quadwords and generate hardware Carry Flag (CF)
    # - Long Form    : Add Register Base to Register A Extended
    # - Why We Used It: Sets CF if unsigned sum exceeds 2^64 - 1.
    # --------------------------------------------------------------------------
    add rax, rbx                # RAX = a_lo + b_lo, sets CF
    setc sil                    # SIL = 1 if carry generated, else 0
    movzx rsi, sil              # RSI = carry between low and high quadwords

    # --------------------------------------------------------------------------
    # ADC R10, R11:
    # - Small Meaning: Add upper 64-bit quadwords incorporating low quadword Carry Flag
    # - Long Form    : Add with Carry Register R11 into Register R10
    # - Why We Used It: Propagates mathematical carry across 64-bit register boundary.
    # --------------------------------------------------------------------------
    shr sil, 1                  # Restore CF into RFLAGS from SIL
    adc r10, r11                # R10 = a_hi + b_hi + CF

    setc dil                    # DIL = carry out from upper 64 bits
    movzx rdi, dil

    # Store results via pointer arguments passed on stack
    mov r12, [rbp + 48]         # out_lo
    test r12, r12
    jz .L_skip_lo
    mov [r12], rax
.L_skip_lo:
    mov r12, [rbp + 56]         # out_hi
    test r12, r12
    jz .L_skip_hi
    mov [r12], r10
.L_skip_hi:
    mov r12, [rbp + 64]         # out_cf
    test r12, r12
    jz .L_add128_done
    mov [r12], rsi

.L_add128_done:
    pop r13
    pop r12
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co3_demo_carry_overflow
# - Small Meaning: Comprehensive demonstration of multi-precision carry and signed overflow.
# - Long Form    : CO3 Carry and Signed Overflow Demonstration Runner
# - Why We Used It: Validates processor condition flags CF and OF under boundary conditions.
# ==============================================================================
.global co3_demo_carry_overflow
.global _co3_demo_carry_overflow
co3_demo_carry_overflow:
_co3_demo_carry_overflow:
    push rbp
    mov rbp, rsp
    sub rsp, 96                 # Stack allocation for parameters and shadow space
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + co3_arith_header]
    call print_str

    # 1. 128-bit Addition with ADC
    # Op 1: 0x0000000000000001 FFFFFFFFFFFFFFFF
    # Op 2: 0x0000000000000002 0000000000000001
    # Expected Sum: 0x0000000000000004 0000000000000000 (Carry = 1)
    lea rcx, [rip + sec_carry_title]
    call print_str

    mov rax, 0xFFFFFFFFFFFFFFFF # a_lo
    mov r10, 0x0000000000000001 # a_hi
    mov rbx, 0x0000000000000001 # b_lo
    mov r11, 0x0000000000000002 # b_hi

    add rax, rbx                # Low quadword sum = 0, CF = 1
    setc sil
    movzx rsi, sil

    shr sil, 1                  # Restore CF
    adc r10, r11                # High quadword sum = 1 + 2 + 1 = 4

    lea rcx, [rip + carry_demo_fmt]
    mov rdx, 1                  # a_hi
    mov r8,  0xFFFFFFFFFFFFFFFF # a_lo
    mov r9,  2                  # b_hi
    mov qword ptr [rsp + 32], 1 # b_lo
    mov [rsp + 40], r10         # Sum hi
    mov [rsp + 48], rax         # Sum lo
    mov [rsp + 56], rsi         # Carry bit
    call printf

    # 2. Signed 64-Bit Overflow Demonstrations
    lea rcx, [rip + sec_overflow_title]
    call print_str

    # Case 1: Pos + Pos = Neg Overflow
    # A = 0x7FFFFFFFFFFFFFFF (+9,223,372,036,854,775,807)
    # B = 1
    # Sum = 0x8000000000000000 (-9,223,372,036,854,775,808) -> OF = 1
    mov rax, 0x7FFFFFFFFFFFFFFF
    mov rbx, 1
    mov r10, rax
    add r10, rbx
    seto sil                    # SIL = 1 if signed overflow occurred
    movzx rsi, sil

    lea rcx, [rip + overflow_case1_fmt]
    mov rdx, rax                # A dec
    mov r8,  rax                # A hex
    mov r9,  rbx                # B dec
    mov [rsp + 32], rbx         # B hex
    mov [rsp + 40], r10         # Sum dec
    mov [rsp + 48], r10         # Sum hex
    mov [rsp + 56], rsi         # OF flag
    call printf

    # Case 2: Neg + Neg = Pos Overflow
    # A = 0x8000000000000000 (-9,223,372,036,854,775,808)
    # B = 0xFFFFFFFFFFFFFFFF (-1)
    # Sum = 0x7FFFFFFFFFFFFFFF (+9,223,372,036,854,775,807) -> OF = 1
    mov rax, 0x8000000000000000
    mov rbx, 0xFFFFFFFFFFFFFFFF
    mov r10, rax
    add r10, rbx
    seto sil
    movzx rsi, sil

    lea rcx, [rip + overflow_case2_fmt]
    mov rdx, rax
    mov r8,  rax
    mov r9,  rbx
    mov [rsp + 32], rbx
    mov [rsp + 40], r10
    mov [rsp + 48], r10
    mov [rsp + 56], rsi
    call printf

    # Case 3: Valid Addition (No Overflow)
    # A = 5000000000, B = 3000000000 -> Sum = 8000000000, OF = 0
    mov rax, 5000000000
    mov rbx, 3000000000
    mov r10, rax
    add r10, rbx
    seto sil
    movzx rsi, sil

    lea rcx, [rip + no_overflow_fmt]
    mov rdx, rax
    mov r8,  rax
    mov r9,  rbx
    mov [rsp + 32], rbx
    mov [rsp + 40], r10
    mov [rsp + 48], r10
    mov [rsp + 56], rsi
    call printf

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
