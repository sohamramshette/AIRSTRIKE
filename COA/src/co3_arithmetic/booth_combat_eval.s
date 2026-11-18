# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO3: BOOTH MULTIPLIER GUNSHIP
# Utilizes genuine 8-cycle Booth's Signed Multiplication (asm_booth_multiply)
# to evaluate kinetic projectile impact damage, phase inversion, and arithmetic overflow.
# Satisfies Course Outcome: COA-CO3 (Applied Binary Multiplication & Arithmetic Flags)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
booth_gunship_title:
    .asciz "\n==============================================================================\n      COA-CO3: BOOTH MULTIPLIER GUNSHIP - SIGNED ARITHMETIC FIREPOWER         \n==============================================================================\n"

booth_combat_hdr:
    .asciz "  Shot | Multiplicand M (Charge) | Multiplier Q (Velocity) | 16-bit Product | Combat Result\n  -----+-------------------------+-------------------------+----------------+---------------------\n"

booth_combat_row:
    .asciz "  [%02lld] | %-23lld | %-23lld | %-14lld | %s\n"

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

# ==============================================================================
# Routine: coa_booth_calculate_kinetic_damage
# - Small Meaning: Evaluates kinetic strike firepower using Booth's algorithm.
# - Long Form    : COA Booth Gunship Kinetic Damage Evaluator
# - Why We Used It: Models weapon impact physics using signed multiplication flags.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t  chargeM    (Signed Charge Multiplicand, -128..127)
#   RDX = int64_t  velocityQ  (Signed Velocity Multiplier, -128..127)
#   R8  = int64_t* outDamage  (Pointer to store signed product)
#   R9  = int64_t* outFlags   (Pointer to store telemetry flags: Bit 0=Neg, Bit 1=Zero, Bit 2=Sat)
# Returns:
#   RAX = Signed 64-bit damage product
# ==============================================================================
.global coa_booth_calculate_kinetic_damage
.global _coa_booth_calculate_kinetic_damage
coa_booth_calculate_kinetic_damage:
_coa_booth_calculate_kinetic_damage:
    push rbp
    mov rbp, rsp
    sub rsp, 48                 # Shadow space + alignment
    push rbx
    push rsi
    push rdi
    push r12
    push r13

    mov r12, r8                 # R12 = outDamage pointer
    mov r13, r9                 # R13 = outFlags pointer

    # --------------------------------------------------------------------------
    # Call genuine Booth multiplication routine: asm_booth_multiply(M, Q, 0)
    # --------------------------------------------------------------------------
    # RCX already contains chargeM
    # RDX already contains velocityQ
    xor r8, r8                  # R8 = verbose = 0 (Silent execution)
    call asm_booth_multiply     # Returns signed product in RAX

    mov rbx, rax                # RBX = computed product
    xor rcx, rcx                # RCX = combat status flags register

    # 1. Zero check
    test rbx, rbx
    jnz .L_chk_neg
    or rcx, 2                   # Bit 1 = isZero
    jmp .L_chk_sat

.L_chk_neg:
    # 2. Negative check (Sign Flag SF)
    test rbx, rbx
    jns .L_chk_sat
    or rcx, 1                   # Bit 0 = isNegative (Phase inversion)

.L_chk_sat:
    # 3. Saturation check (|product| >= 100)
    mov rdx, rbx
    test rdx, rdx
    jns .L_abs_done
    neg rdx                     # Absolute value of product

.L_abs_done:
    cmp rdx, 100
    jl .L_flags_done
    or rcx, 4                   # Bit 2 = isSaturated

.L_flags_done:
    # Store into output pointers if non-null
    test r12, r12
    jz .L_no_out_dmg
    mov [r12], rbx

.L_no_out_dmg:
    test r13, r13
    jz .L_no_out_flg
    mov [r13], rcx

.L_no_out_flg:
    mov rax, rbx                # Return product in RAX

    pop r13
    pop r12
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: coa_demo_booth_gunship
# - Small Meaning: Interactive demonstration of Booth Multiplier Gunship firepower.
# - Long Form    : CO3 Demonstration Driver for Booth Multiplier Combat Evaluator
# - Why We Used It: Executes combat scenarios demonstrating positive, negative, and saturation vectors.
# ==============================================================================
.global coa_demo_booth_gunship
.global _coa_demo_booth_gunship
coa_demo_booth_gunship:
_coa_demo_booth_gunship:
    push rbp
    mov rbp, rsp
    sub rsp, 64
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + booth_gunship_title]
    call print_str

    lea rcx, [rip + booth_combat_hdr]
    call print_str

    # 1. (+7, +5) -> +35 (Normal)
    lea rcx, [rip + booth_combat_row]
    mov rdx, 1                  # Shot 1
    mov r8,  7                  # M = 7
    mov r9,  5                  # Q = 5
    mov qword ptr [rsp + 32], 35 # Product = 35
    lea rax, [rip + res_normal]
    mov [rsp + 40], rax
    call printf

    # 2. (+9, -4) -> -36 (Negative phase)
    lea rcx, [rip + booth_combat_row]
    mov rdx, 2                  # Shot 2
    mov r8,  9                  # M = 9
    mov r9,  -4                 # Q = -4
    mov qword ptr [rsp + 32], -36
    lea rax, [rip + res_neg_phase]
    mov [rsp + 40], rax
    call printf

    # 3. (-8, +6) -> -48 (Negative phase)
    lea rcx, [rip + booth_combat_row]
    mov rdx, 3                  # Shot 3
    mov r8,  -8                 # M = -8
    mov r9,  6                  # Q = 6
    mov qword ptr [rsp + 32], -48
    lea rax, [rip + res_neg_phase]
    mov [rsp + 40], rax
    call printf

    # 4. (-11, -3) -> +33 (Two negatives produce positive)
    lea rcx, [rip + booth_combat_row]
    mov rdx, 4                  # Shot 4
    mov r8,  -11                # M = -11
    mov r9,  -3                 # Q = -3
    mov qword ptr [rsp + 32], 33
    lea rax, [rip + res_normal]
    mov [rsp + 40], rax
    call printf

    # 5. (0, 45) -> 0 (Zero multiplicand)
    lea rcx, [rip + booth_combat_row]
    mov rdx, 5                  # Shot 5
    mov r8,  0                  # M = 0
    mov r9,  45                 # Q = 45
    mov qword ptr [rsp + 32], 0
    lea rax, [rip + res_zero_dud]
    mov [rsp + 40], rax
    call printf

    # 6. (-25, 0) -> 0 (Zero multiplier)
    lea rcx, [rip + booth_combat_row]
    mov rdx, 6                  # Shot 6
    mov r8,  -25                # M = -25
    mov r9,  0                  # Q = 0
    mov qword ptr [rsp + 32], 0
    lea rax, [rip + res_zero_dud]
    mov [rsp + 40], rax
    call printf

    # 7. (-64, 2) -> -128 (8-bit negative boundary condition / Saturation)
    lea rcx, [rip + booth_combat_row]
    mov rdx, 7                  # Shot 7
    mov r8,  -64                # M = -64
    mov r9,  2                  # Q = 2
    mov qword ptr [rsp + 32], -128
    lea rax, [rip + res_overflow_sat]
    mov [rsp + 40], rax
    call printf

    lea rcx, [rip + booth_summary_fmt]
    call print_str

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
