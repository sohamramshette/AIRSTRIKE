# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO1: Sub-Register Aegis Sentry
# Demonstrates 80386 Register Partitioning (EAX -> AX -> AH/AL),
# SIB Addressing Mode [Base + Index*4 + Disp], and Condition Code Flags (CF, ZF, SF, OF)
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit Protected Mode, cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
aegis_title:
    .asciz "\n==============================================================================\n        COA-CO1: SUB-REGISTER AEGIS SENTRY - 80386 REGISTER PARTITIONING      \n==============================================================================\n"

aegis_init_fmt:
    .asciz "  [INITIAL STATE] Full Aegis Shield Barrier Initialized:\n    EAX (32-bit Composite) = 0x%08X\n    AX  (16-bit Mid-Layer) = 0x%04X\n    AH  (8-bit High Damper)= 0x%02X\n    AL  (8-bit Low Barrier)= 0x%02X\n"

aegis_damage_al_fmt:
    .asciz "\n  [EVENT 1] Laser Damage Inflicted on Low-Byte Polarizer (AL -= 0x%02X):\n    EAX After Subtraction  = 0x%08X (Bits 7..0 modified)\n    AX  After Subtraction  = 0x%04X\n    AH  Unchanged Layer    = 0x%02X (Bits 15..8 preserved)\n    AL  Remaining Health   = 0x%02X\n"

aegis_recharge_ah_fmt:
    .asciz "\n  [EVENT 2] Kinetic Capacitor Boost Applied to High-Byte Damper (AH += 0x%02X):\n    EAX After Boost        = 0x%08X (Bits 15..8 modified)\n    AX  After Boost        = 0x%04X\n    AH  Recharged Value    = 0x%02X\n    AL  Preserved Value    = 0x%02X (Bits 7..0 preserved)\n"

aegis_flags_fmt:
    .asciz "\n  [EVENT 3] Condition Code Flags Under Combat Depletion:\n    Zero Flag (ZF)     = %d (Hull/Shield reduced to exactly 0)\n    Sign Flag (SF)     = %d (Hull over-penetrated, negative value)\n    Carry Flag (CF)    = %d (Unsigned underflow/borrow occurred)\n    Overflow Flag (OF) = %d (Signed two's complement boundary exceeded)\n"

aegis_sib_fmt:
    .asciz "\n  [EVENT 4] Scaled-Index-Base (SIB) Shield Layer Access [EBX + ESI*4 + 8]:\n    Layer Index %d -> Value = 0x%08X\n==============================================================================\n"

# Dummy SIB buffer for demo
aegis_layers:
    .long 0x11111111        # Header offset (+0)
    .long 0x22222222        # Header offset (+4)
    .long 0xAAAA0001        # Layer 0 (Offset +8)
    .long 0xBBBB0002        # Layer 1 (Offset +12)
    .long 0xCCCC0003        # Layer 2 (Offset +16)
    .long 0xDDDD0004        # Layer 3 (Offset +20)

.text

# ------------------------------------------------------------------------------
# coa_init_aegis_shield: Returns 32-bit initial composite shield in EAX
# Prototype: unsigned int coa_init_aegis_shield(void)
# ------------------------------------------------------------------------------
.global _coa_init_aegis_shield
_coa_init_aegis_shield:
    mov eax, 0xA5B6C7D8     # High Word: 0xA5B6, AX: 0xC7D8, AH: 0xC7, AL: 0xD8
    ret

# ------------------------------------------------------------------------------
# coa_damage_al_layer: Subtracts damage from AL and returns modified EAX
# Prototype: unsigned int coa_damage_al_layer(unsigned int currentShield, unsigned char dmg)
# ------------------------------------------------------------------------------
.global _coa_damage_al_layer
_coa_damage_al_layer:
    push ebp
    mov ebp, esp

    mov eax, [ebp + 8]      # Current shield in EAX
    mov dl, byte ptr [ebp + 12] # Damage amount
    sub al, dl              # Subtraction alters AL, AX, and EAX low byte!

    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_recharge_ah_layer: Adds recharge to AH and returns modified EAX
# Prototype: unsigned int coa_recharge_ah_layer(unsigned int currentShield, unsigned char boost)
# ------------------------------------------------------------------------------
.global _coa_recharge_ah_layer
_coa_recharge_ah_layer:
    push ebp
    mov ebp, esp

    mov eax, [ebp + 8]      # Current shield in EAX
    mov dl, byte ptr [ebp + 12] # Boost amount
    add ah, dl              # Addition alters AH, AX, and EAX bits 15..8!

    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_evaluate_shield_flags: Evaluates combat arithmetic flags on 8-bit layer
# Prototype: unsigned int coa_evaluate_shield_flags(unsigned char val1, unsigned char val2, int isSub)
# Returns:
#   EAX = (OF << 3) | (SF << 2) | (ZF << 1) | CF
# ------------------------------------------------------------------------------
.global _coa_evaluate_shield_flags
_coa_evaluate_shield_flags:
    push ebp
    mov ebp, esp
    push ebx

    mov al, byte ptr [ebp + 8]  # val1
    mov dl, byte ptr [ebp + 12] # val2
    mov ecx, [ebp + 16]         # isSub flag (1 = SUB, 0 = ADD)

    test ecx, ecx
    jz .L_eval_add

.L_eval_sub:
    sub al, dl
    jmp .L_collect_flags

.L_eval_add:
    add al, dl

.L_collect_flags:
    # Capture flags: CF, ZF, SF, OF
    setc bl                 # bl = CF (0 or 1)
    setz cl                 # cl = ZF (0 or 1)
    sets dl                 # dl = SF (0 or 1)
    seto al                 # al = OF (0 or 1)

    movzx eax, al           # eax = OF
    shl eax, 1
    movzx edx, dl
    or eax, edx             # eax = (OF << 1) | SF
    shl eax, 1
    movzx ecx, cl
    or eax, ecx             # eax = (OF << 2) | (SF << 1) | ZF
    shl eax, 1
    movzx ebx, bl
    or eax, ebx             # eax = (OF << 3) | (SF << 2) | (ZF << 1) | CF

    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_sib_shield_layer_access: Scaled-Index-Base addressing mode demonstration
# Prototype: unsigned int coa_sib_shield_layer_access(const unsigned int* basePtr, int layerIndex)
# Implements: [ebx + esi*4 + 8]
# ------------------------------------------------------------------------------
.global _coa_sib_shield_layer_access
_coa_sib_shield_layer_access:
    push ebp
    mov ebp, esp
    push ebx
    push esi

    mov ebx, [ebp + 8]      # Base pointer
    mov esi, [ebp + 12]     # Index (0..3)

    # Genuine 80386 SIB instruction: [Base + Index*4 + Disp8]
    mov eax, [ebx + esi * 4 + 8]

    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_demo_aegis_sentry: Full console demonstration of Sub-Register Sentry
# ------------------------------------------------------------------------------
.global _coa_demo_aegis_sentry
_coa_demo_aegis_sentry:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset aegis_title
    call _print_str
    add esp, 4

    # Initial composite shield
    call _coa_init_aegis_shield
    mov ebx, eax            # ebx = 0xA5B6C7D8

    movzx eax, bl           # AL = 0xD8
    push eax
    movzx eax, bh           # AH = 0xC7
    push eax
    movzx eax, bx           # AX = 0xC7D8
    push eax
    push ebx                # EAX = 0xA5B6C7D8
    push offset aegis_init_fmt
    call _printf
    add esp, 20

    # Event 1: Damage AL by 0x18
    push 0x18
    push ebx
    call _coa_damage_al_layer
    add esp, 8
    mov ebx, eax            # ebx = 0xA5B6C7C0

    movzx eax, bl           # AL = 0xC0
    push eax
    movzx eax, bh           # AH = 0xC7
    push eax
    movzx eax, bx           # AX = 0xC7C0
    push eax
    push ebx                # EAX = 0xA5B6C7C0
    push 0x18
    push offset aegis_damage_al_fmt
    call _printf
    add esp, 24

    # Event 2: Boost AH by 0x10
    push 0x10
    push ebx
    call _coa_recharge_ah_layer
    add esp, 8
    mov ebx, eax            # ebx = 0xA5B6D7C0

    movzx eax, bl           # AL = 0xC0
    push eax
    movzx eax, bh           # AH = 0xD7
    push eax
    movzx eax, bx           # AX = 0xD7C0
    push eax
    push ebx                # EAX = 0xA5B6D7C0
    push 0x10
    push offset aegis_recharge_ah_fmt
    call _printf
    add esp, 24

    # Event 3: Evaluate Flags on 50 - 50 -> ZF=1
    push 1                  # isSub
    push 50
    push 50
    call _coa_evaluate_shield_flags
    add esp, 12

    # Parse flags: eax = (OF << 3) | (SF << 2) | (ZF << 1) | CF
    mov ecx, eax
    shr ecx, 3
    and ecx, 1              # OF
    push ecx

    mov ecx, eax
    and ecx, 1              # CF
    push ecx

    mov ecx, eax
    shr ecx, 2
    and ecx, 1              # SF
    push ecx

    mov ecx, eax
    shr ecx, 1
    and ecx, 1              # ZF
    push ecx

    push offset aegis_flags_fmt
    call _printf
    add esp, 20

    # Event 4: SIB Access for Layer 2
    push 2
    push offset aegis_layers
    call _coa_sib_shield_layer_access
    add esp, 8

    push eax
    push 2
    push offset aegis_sib_fmt
    call _printf
    add esp, 12

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
