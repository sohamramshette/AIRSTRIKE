# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO3: Carry & Overflow Demonstrations
# Implements:
#   3. Binary Addition with Carry Detection & ADC Multi-Precision
#   4. Signed Arithmetic Overflow Detection (OF Flag & JO)
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
co3_arith_header:
    .asciz "\n==============================================================================\n        CO3: BINARY ADDITION WITH CARRY & SIGNED OVERFLOW DETECTION           \n==============================================================================\n"

sec_carry_title:
    .asciz "\n--- 1. MULTI-PRECISION BINARY ADDITION WITH CARRY (ADC) ---\n"
carry_demo_fmt:
    .asciz "  64-bit Addition on 32-bit 80386 Architecture:\n    Operand 1: 0x%08X %08X\n  + Operand 2: 0x%08X %08X\n  ---------------------------------------\n    Sum      : 0x%08X %08X (Carry between 32-bit words = %d)\n"

sec_overflow_title:
    .asciz "\n--- 2. SIGNED ARITHMETIC OVERFLOW DEMONSTRATION (OF FLAG) ---\n"
overflow_case1_fmt:
    .asciz "  Case 1: Positive + Positive -> Negative Overflow\n    A = %d (0x%08X)\n  + B = %d (0x%08X)\n  ---------------------------------------\n    Sum = %d (0x%08X) -> OF = %d (SIGNED OVERFLOW!)\n"
overflow_case2_fmt:
    .asciz "  Case 2: Negative + Negative -> Positive Overflow\n    A = %d (0x%08X)\n  + B = %d (0x%08X)\n  ---------------------------------------\n    Sum = %d (0x%08X) -> OF = %d (SIGNED OVERFLOW!)\n"
no_overflow_fmt:
    .asciz "  Case 3: Valid Addition (No Overflow)\n    A = %d (0x%08X) + B = %d (0x%08X) -> Sum = %d (0x%08X), OF = %d (Valid)\n==============================================================================\n"

.text

# ------------------------------------------------------------------------------
# asm_add_with_carry_64: Adds two 64-bit integers on 32-bit registers
# Prototype: void asm_add_with_carry_64(uint32_t a_lo, uint32_t a_hi,
#                                       uint32_t b_lo, uint32_t b_hi,
#                                       uint32_t* out_lo, uint32_t* out_hi, int* out_cf)
# ------------------------------------------------------------------------------
.global _asm_add_with_carry_64
_asm_add_with_carry_64:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov eax, [ebp + 8]          # a_lo
    mov edx, [ebp + 12]         # a_hi
    mov ebx, [ebp + 16]         # b_lo
    mov ecx, [ebp + 20]         # b_hi

    # Add lower 32-bit words
    add eax, ebx                # EAX = a_lo + b_lo, generates Carry Flag (CF)
    setc bl                     # bl = CF (1 if carry generated from low word)

    # Add upper 32-bit words with Carry (ADC)
    # Restore CF for ADC
    shr bl, 1                   # sets CF if bl was 1
    adc edx, ecx                # EDX = a_hi + b_hi + CF

    setc cl                     # cl = carry out from high word

    # Store results
    mov esi, [ebp + 24]         # out_lo
    test esi, esi
    jz .L_skip_lo
    mov [esi], eax
.L_skip_lo:
    mov esi, [ebp + 28]         # out_hi
    test esi, esi
    jz .L_skip_hi
    mov [esi], edx
.L_skip_hi:
    mov esi, [ebp + 32]         # out_cf
    test esi, esi
    jz .L_add64_done
    movzx ecx, cl
    mov [esi], ecx

.L_add64_done:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co3_demo_carry_overflow: Complete demonstration of Carry and Overflow
# ------------------------------------------------------------------------------
.global _co3_demo_carry_overflow
_co3_demo_carry_overflow:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset co3_arith_header
    call _print_str
    add esp, 4

    # 1. 64-bit Addition with ADC
    # Op 1: 0x00000001 FFFFFFFF
    # Op 2: 0x00000002 00000001
    # Expected Sum: 0x00000004 00000000 (Low sum produces Carry, high sum is 1 + 2 + 1 = 4)
    push offset sec_carry_title
    call _print_str
    add esp, 4

    mov eax, 0xFFFFFFFF         # a_lo
    mov edx, 0x00000001         # a_hi
    mov ebx, 0x00000001         # b_lo
    mov ecx, 0x00000002         # b_hi

    add eax, ebx                # 0xFFFFFFFF + 1 = 0, CF = 1
    setc bl
    movzx esi, bl               # esi = CF

    shr bl, 1                   # restore CF
    adc edx, ecx                # 1 + 2 + 1 = 4

    push esi                    # Carry bit
    push eax                    # Sum lo
    push edx                    # Sum hi
    push 1                      # b_lo
    push 2                      # b_hi
    push 0xFFFFFFFF             # a_lo
    push 1                      # a_hi
    push offset carry_demo_fmt
    call _printf
    add esp, 32

    # 2. Signed Overflow Demonstrations
    push offset sec_overflow_title
    call _print_str
    add esp, 4

    # Case 1: Pos + Pos = Neg
    mov eax, 0x7FFFFFFF         # +2,147,483,647
    mov ebx, 1                  # +1
    mov ecx, eax
    add ecx, ebx                # 0x80000000 (-2,147,483,648)
    seto dl
    movzx edx, dl

    push edx                    # OF
    push ecx                    # Sum hex
    push ecx                    # Sum dec
    push ebx                    # B hex
    push ebx                    # B dec
    push eax                    # A hex
    push eax                    # A dec
    push offset overflow_case1_fmt
    call _printf
    add esp, 32

    # Case 2: Neg + Neg = Pos
    mov eax, 0x80000000         # -2,147,483,648
    mov ebx, 0xFFFFFFFF         # -1
    mov ecx, eax
    add ecx, ebx                # 0x7FFFFFFF (+2,147,483,647)
    seto dl
    movzx edx, dl

    push edx                    # OF
    push ecx                    # Sum hex
    push ecx                    # Sum dec
    push ebx                    # B hex
    push ebx                    # B dec
    push eax                    # A hex
    push eax                    # A dec
    push offset overflow_case2_fmt
    call _printf
    add esp, 32

    # Case 3: Normal Valid Addition
    mov eax, 500
    mov ebx, 300
    mov ecx, eax
    add ecx, ebx
    seto dl
    movzx edx, dl

    push edx
    push ecx
    push ecx
    push ebx
    push ebx
    push eax
    push eax
    push offset no_overflow_fmt
    call _printf
    add esp, 32

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
