# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO1: SUB-REGISTER AEGIS SENTRY
# Demonstrates 64-bit Register Partitioning (RAX -> EAX -> AX -> AH / AL, R8 -> R8D -> R8W -> R8B),
# 64-bit Scale-Index-Base (SIB) Addressing [RBX + RSI*8 + 16], and 64-bit RFLAGS
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
aegis_title:
    .asciz "\n==============================================================================\n      COA-CO1: SUB-REGISTER AEGIS SENTRY - 64-BIT REGISTER PARTITIONING        \n==============================================================================\n"

aegis_init_fmt:
    .asciz "  [INITIAL STATE] Full Aegis Shield Barrier Initialized:\n    RAX (64-bit Full Quadword) = 0x%016llX\n    EAX (32-bit Doubleword)    = 0x%08X\n    AX  (16-bit Mid-Layer)     = 0x%04X\n    AH  (8-bit High Damper)    = 0x%02X\n    AL  (8-bit Low Barrier)    = 0x%02X\n"

aegis_damage_al_fmt:
    .asciz "\n  [EVENT 1] Laser Damage Inflicted on Low-Byte Polarizer (AL -= 0x%02X):\n    RAX After Subtraction      = 0x%016llX (Bits 7..0 modified)\n    EAX After Subtraction      = 0x%08X\n    AX  After Subtraction      = 0x%04X\n    AH  Unchanged Damper       = 0x%02X (Bits 15..8 preserved)\n    AL  Remaining Low Barrier  = 0x%02X\n"

aegis_recharge_ah_fmt:
    .asciz "\n  [EVENT 2] Kinetic Capacitor Boost Applied to High-Byte Damper (AH += 0x%02X):\n    RAX After Boost            = 0x%016llX (Bits 15..8 modified)\n    EAX After Boost            = 0x%08X\n    AX  After Boost            = 0x%04X\n    AH  Recharged Damper Value = 0x%02X\n    AL  Preserved Low Barrier  = 0x%02X (Bits 7..0 preserved)\n"

aegis_flags_fmt:
    .asciz "\n  [EVENT 3] Condition Code Flags Under Combat Depletion:\n    Zero Flag (ZF)     = %d (Hull/Shield reduced to exactly 0)\n    Sign Flag (SF)     = %d (Hull over-penetrated, negative value)\n    Carry Flag (CF)    = %d (Unsigned underflow/borrow occurred)\n    Overflow Flag (OF) = %d (Signed two's complement boundary exceeded)\n"

aegis_sib_fmt:
    .asciz "\n  [EVENT 4] 64-bit Scaled-Index-Base (SIB) Shield Layer Access [RBX + RSI*8 + 16]:\n    Layer Index %lld -> Telemetry Value = 0x%016llX\n==============================================================================\n"

# 64-bit SIB Shield Barrier Telemetry Buffer
.align 8
aegis_layers_x64:
    .quad 0x1111111122222222    # Header offset (+0)
    .quad 0x3333333344444444    # Header offset (+8)
    .quad 0xAAAA000000000001    # Layer 0 (Offset +16)
    .quad 0xBBBB000000000002    # Layer 1 (Offset +24)
    .quad 0xCCCC000000000003    # Layer 2 (Offset +32)
    .quad 0xDDDD000000000004    # Layer 3 (Offset +40)

.text

# ==============================================================================
# Routine: coa_init_aegis_shield_x64
# - Small Meaning: Initialize 64-bit composite shield state
# - Long Form    : Computer Organization Architecture Initialize Aegis Shield
# - Why We Used It: Sets up a distinct 64-bit hex value to prove sub-register partitions.
# Returns: RAX = 0xFEDCBA98A5B6C7D8
# ==============================================================================
.global coa_init_aegis_shield_x64
.global _coa_init_aegis_shield_x64
coa_init_aegis_shield_x64:
_coa_init_aegis_shield_x64:
    # --------------------------------------------------------------------------
    # MOV RAX, 0xFEDCBA98A5B6C7D8:
    # - Small Meaning: Load 64-bit quadword immediate into RAX
    # - Long Form    : Move 64-bit Constant into Register A Extended
    # - Why We Used It: Higher 32 bits = 0xFEDCBA98, EAX = 0xA5B6C7D8,
    #                  AX = 0xC7D8, AH = 0xC7, AL = 0xD8
    # --------------------------------------------------------------------------
    mov rax, 0xFEDCBA98A5B6C7D8
    ret

# ==============================================================================
# Routine: coa_damage_al_layer_x64
# - Small Meaning: Inflict damage specifically on lowest 8 bits (AL)
# - Long Form    : Damage Accumulator Low Layer
# - Why We Used It: Proves that altering AL (bits 0..7) modifies RAX and AX
#                  while leaving AH (bits 8..15) completely untouched.
# Calling Convention:
#   RCX = currentShield (uint64_t)
#   RDX = damageAmount  (uint8_t)
# Returns: RAX = modified 64-bit shield state
# ==============================================================================
.global coa_damage_al_layer_x64
.global _coa_damage_al_layer_x64
coa_damage_al_layer_x64:
_coa_damage_al_layer_x64:
    mov rax, rcx                # RAX = current composite shield
    # SUB AL, DL:
    # - Small Meaning: Subtract 8-bit damage from AL
    # - Long Form    : Subtract byte from Accumulator Low
    # - Why We Used It: In-place byte subtraction modifying only bits 0..7.
    sub al, dl
    ret

# ==============================================================================
# Routine: coa_recharge_ah_layer_x64
# - Small Meaning: Add capacitor recharge specifically to bits 8..15 (AH)
# - Long Form    : Recharge Accumulator High Layer
# - Why We Used It: Proves that altering AH alters AX and RAX while preserving AL.
# Calling Convention:
#   RCX = currentShield (uint64_t)
#   RDX = boostAmount   (uint8_t)
# Returns: RAX = modified 64-bit shield state
# ==============================================================================
.global coa_recharge_ah_layer_x64
.global _coa_recharge_ah_layer_x64
coa_recharge_ah_layer_x64:
_coa_recharge_ah_layer_x64:
    mov rax, rcx                # RAX = current shield
    # ADD AH, DL:
    # - Small Meaning: Add 8-bit boost to AH
    # - Long Form    : Add byte to Accumulator High
    # - Why We Used It: Modifies only bits 8..15.
    add ah, dl
    ret

# ==============================================================================
# Routine: coa_eval_shield_flags_x64
# - Small Meaning: Compute combat damage and return condition flags
# - Long Form    : Evaluate Shield Flags
# - Why We Used It: Proves condition flags (ZF, SF, CF, OF) under 64-bit subtraction.
# Calling Convention:
#   RCX = currentShield (int64_t)
#   RDX = incomingDamage (int64_t)
#   R8  = outZF (int*)
#   R9  = outSF (int*)
#   [RSP + 40] = outCF (int*)
#   [RSP + 48] = outOF (int*)
# Returns: RAX = remaining shield value (RCX - RDX)
# ==============================================================================
.global coa_eval_shield_flags_x64
.global _coa_eval_shield_flags_x64
coa_eval_shield_flags_x64:
_coa_eval_shield_flags_x64:
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    push r13

    mov rax, rcx
    # CMP / SUB RAX, RDX:
    # - Small Meaning: Subtract incoming damage and update RFLAGS
    # - Long Form    : 64-bit Subtraction with Condition Flag generation
    # - Why We Used It: Updates Zero Flag (depletion), Sign Flag (over-penetration),
    #                  Carry Flag (borrow), and Overflow Flag (boundary flip).
    sub rax, rdx

    setz r10b                   # ZF: Shield depleted to exactly 0
    sets r11b                   # SF: Shield negative (hull damage)
    setb r12b                   # CF: Unsigned underflow
    seto r13b                   # OF: Signed boundary overflow

    # Store flags into output pointer parameters
    test r8, r8
    jz .L_skip_zf_shield
    movzx ecx, r10b
    mov [r8], ecx
.L_skip_zf_shield:

    test r9, r9
    jz .L_skip_sf_shield
    movzx edx, r11b
    mov [r9], edx
.L_skip_sf_shield:

    # 5th arg (outCF) at [RBP + 48]
    mov r10, [rbp + 48]
    test r10, r10
    jz .L_skip_cf_shield
    movzx ecx, r12b
    mov [r10], ecx
.L_skip_cf_shield:

    # 6th arg (outOF) at [RBP + 56]
    mov r11, [rbp + 56]
    test r11, r11
    jz .L_skip_of_shield
    movzx edx, r13b
    mov [r11], edx
.L_skip_of_shield:

    pop r13
    pop r12
    pop rbx
    pop rbp
    ret

# ==============================================================================
# Routine: coa_sib_shield_layer_x64
# - Small Meaning: Read shield layer using 64-bit SIB addressing
# - Long Form    : Scaled-Index-Base Shield Layer Access
# - Why We Used It: Hardware computes [Base + Index*8 + 16] in a single instruction.
# Calling Convention:
#   RCX = arrayBase (const int64_t*)
#   RDX = layerIndex (int64_t)
# Returns: RAX = 64-bit shield layer telemetry
# ==============================================================================
.global coa_sib_shield_layer_x64
.global _coa_sib_shield_layer_x64
coa_sib_shield_layer_x64:
_coa_sib_shield_layer_x64:
    push rbp
    mov rbp, rsp
    push rbx

    mov rbx, rcx                # RBX = Base Address
    mov rcx, rdx                # RCX = Layer Index (0..3)

    # --------------------------------------------------------------------------
    # MOV RAX, [RBX + RCX*8 + 16]:
    # - Small Meaning: 64-bit SIB memory read
    # - Long Form    : Scale-Index-Base with 8-byte scale and 16-byte displacement
    # - Why We Used It: Hardware shifts RCX by 3 bits (multiply by 8) and adds
    #                  16-byte header offset to read the quadword slot instantly.
    # --------------------------------------------------------------------------
    mov rax, [rbx + rcx*8 + 16]

    pop rbx
    pop rbp
    ret

# ==============================================================================
# Routine: coa_demo_aegis_sentry_x64 / coa_demo_aegis_sentry
# Interactive Aegis Sentry Demonstrator
# ==============================================================================
.global coa_demo_aegis_sentry_x64
.global _coa_demo_aegis_sentry_x64
.global coa_demo_aegis_sentry
.global _coa_demo_aegis_sentry
coa_demo_aegis_sentry:
_coa_demo_aegis_sentry:
coa_demo_aegis_sentry_x64:
_coa_demo_aegis_sentry_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 80
    push rbx
    push rsi

    lea rcx, [rip + aegis_title]
    call printf

    # Initial Shield
    call coa_init_aegis_shield_x64
    mov rbx, rax                # RBX = initial shield

    lea rcx, [rip + aegis_init_fmt]
    mov rdx, rbx                # RAX
    mov r8d, ebx                # EAX
    mov r9w, bx                 # AX
    movzx eax, bh
    mov [rsp + 32], rax         # AH
    movzx eax, bl
    mov [rsp + 40], rax         # AL
    call printf

    # Damage Event (AL -= 0x20)
    mov rcx, rbx
    mov rdx, 0x20
    call coa_damage_al_layer_x64
    mov rbx, rax

    lea rcx, [rip + aegis_damage_al_fmt]
    mov rdx, 0x20               # Dmg
    mov r8,  rbx                # RAX
    mov r9d, ebx                # EAX
    movzx eax, bx
    mov [rsp + 32], rax         # AX
    movzx eax, bh
    mov [rsp + 40], rax         # AH
    movzx eax, bl
    mov [rsp + 48], rax         # AL
    call printf

    # SIB Shield Layer Read
    lea rcx, [rip + aegis_layers_x64]
    mov rdx, 1                  # Layer 1
    call coa_sib_shield_layer_x64

    lea rcx, [rip + aegis_sib_fmt]
    mov rdx, 1                  # Layer Index
    mov r8,  rax                # Telemetry Value
    call printf

    pop rsi
    pop rbx
    add rsp, 80
    pop rbp
    ret
