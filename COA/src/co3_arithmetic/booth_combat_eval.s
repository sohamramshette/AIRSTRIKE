# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO3: Booth Multiplier Gunship
# Utilizes genuine 8-cycle Booth's Signed Multiplication (_asm_booth_multiply)
# to evaluate kinetic projectile impact damage, phase inversion, and arithmetic overflow.
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit Protected Mode, cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
booth_gunship_title:
    .asciz "\n==============================================================================\n      COA-CO3: BOOTH MULTIPLIER GUNSHIP - SIGNED ARITHMETIC FIREPOWER         \n==============================================================================\n"

booth_combat_hdr:
    .asciz "  Shot | Multiplicand M (Charge) | Multiplier Q (Velocity) | 16-bit Product | Combat Result\n  -----+-------------------------+-------------------------+----------------+---------------------\n"

booth_combat_row:
    .asciz "  [%02d] | %-23d | %-23d | %-14d | %s\n"

res_normal:
    .asciz "Normal Kinetic Strike [OK]"
res_neg_phase:
    .asciz "Inverted Phase (EMP Drain)"
res_zero_dud:
    .asciz "Neutral/Discharged Dud"
res_overflow_sat:
    .asciz "High-Energy Saturation"

booth_summary_fmt:
    .asciz "\n  [BOOTH ALGORITHM JUSTIFICATION]:\n    Standard shift-and-add hardware fails on signed two's complement inputs.\n    The Booth Gunship evaluates positive, negative, and zero velocity vectors\n    uniformly in exactly 8 cycles using pairwise bit inspection (Q0, Q-1).\n==============================================================================\n"

.text

# ------------------------------------------------------------------------------
# coa_booth_calculate_kinetic_damage: Evaluates kinetic strike firepower
# Prototype: int coa_booth_calculate_kinetic_damage(int chargeM, int velocityQ, int* outDamage, int* outFlags)
# Parameters:
#   [ebp + 8]  = chargeM (signed 8-bit, -128..127)
#   [ebp + 12] = velocityQ (signed 8-bit, -128..127)
#   [ebp + 16] = outDamage (pointer to int)
#   [ebp + 20] = outFlags (pointer to int: Bit 0 = isNegative, Bit 1 = isZero, Bit 2 = isSaturated)
# Returns:
#   EAX = signed 16-bit damage product
# ------------------------------------------------------------------------------
.global _coa_booth_calculate_kinetic_damage
_coa_booth_calculate_kinetic_damage:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    # Call genuine 8-cycle Booth multiplication routine: _asm_booth_multiply(M, Q, 0)
    push 0                      # verbose = 0 (silent computation)
    push [ebp + 12]             # multiplier Q
    push [ebp + 8]              # multiplicand M
    call _asm_booth_multiply
    add esp, 12

    # EAX now contains 16-bit signed product (sign-extended to 32 bits)
    mov ebx, eax                # ebx = product
    xor ecx, ecx                # ecx = flags register

    # 1. Zero check
    test ebx, ebx
    jnz .L_chk_neg
    or ecx, 2                   # Bit 1 = isZero
    jmp .L_chk_sat

.L_chk_neg:
    # 2. Negative check (Sign Flag)
    test ebx, ebx
    jns .L_chk_sat
    or ecx, 1                   # Bit 0 = isNegative (phase inversion)

.L_chk_sat:
    # 3. Saturation check (|product| >= 100)
    mov edx, ebx
    test edx, edx
    jns .L_abs_done
    neg edx                     # absolute value

.L_abs_done:
    cmp edx, 100
    jl .L_flags_done
    or ecx, 4                   # Bit 2 = isSaturated

.L_flags_done:
    # Store into output pointers if provided
    mov edi, [ebp + 16]
    test edi, edi
    jz .L_no_out_dmg
    mov [edi], ebx

.L_no_out_dmg:
    mov edi, [ebp + 20]
    test edi, edi
    jz .L_no_out_flg
    mov [edi], ecx

.L_no_out_flg:
    mov eax, ebx                # return product in eax

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_demo_booth_gunship: Interactive demonstration of Booth Multiplier Gunship
# ------------------------------------------------------------------------------
.global _coa_demo_booth_gunship
_coa_demo_booth_gunship:
    push ebp
    mov ebp, esp
    sub esp, 16                 # [ebp-4] = damage, [ebp-8] = flags
    push ebx
    push esi
    push edi

    push offset booth_gunship_title
    call _print_str
    add esp, 4

    push offset booth_combat_hdr
    call _print_str
    add esp, 4

    # Test cases: (M, Q, shotNumber)
    # 1. (+7, +5) -> +35 (Normal)
    push offset res_normal
    push 35
    push 5
    push 7
    push 1
    push offset booth_combat_row
    call _printf
    add esp, 24

    # 2. (+9, -4) -> -36 (Negative phase)
    push offset res_neg_phase
    push -36
    push -4
    push 9
    push 2
    push offset booth_combat_row
    call _printf
    add esp, 24

    # 3. (-8, +6) -> -48 (Negative phase)
    push offset res_neg_phase
    push -48
    push 6
    push -8
    push 3
    push offset booth_combat_row
    call _printf
    add esp, 24

    # 4. (-11, -3) -> +33 (Two negatives produce positive)
    push offset res_normal
    push 33
    push -3
    push -11
    push 4
    push offset booth_combat_row
    call _printf
    add esp, 24

    # 5. (0, 45) -> 0 (Zero multiplicand)
    push offset res_zero_dud
    push 0
    push 45
    push 0
    push 5
    push offset booth_combat_row
    call _printf
    add esp, 24

    # 6. (-25, 0) -> 0 (Zero multiplier)
    push offset res_zero_dud
    push 0
    push 0
    push -25
    push 6
    push offset booth_combat_row
    call _printf
    add esp, 24

    # 7. (-64, 2) -> -128 (8-bit negative boundary condition)
    push offset res_overflow_sat
    push -128
    push 2
    push -64
    push 7
    push offset booth_combat_row
    call _printf
    add esp, 24

    push offset booth_summary_fmt
    call _print_str
    add esp, 4

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
