# ==============================================================================
# File    : COA/src/co1_architecture/register_shield.s
# Subject : COA-CO1 Enemy | Sub-Register Aegis Sentry
# Topics  : Sub-Register Partitioning (RAX/EAX/AX/AH/AL), RFLAGS Condition
#           Flags (ZF, SF, CF, OF), Scaled-Index-Base (SIB) Addressing
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
aegis_title:
    .asciz "\n==============================================================================\n      COA-CO1: SUB-REGISTER AEGIS SENTRY (RAX -> EAX -> AX -> AH / AL)        \n==============================================================================\n"

aegis_init_fmt:
    .asciz "  [INITIAL SHIELD] RAX = 0x%016llX | EAX = 0x%08X | AX = 0x%04X | AH = 0x%02X | AL = 0x%02X\n"
aegis_damage_al_fmt:
    .asciz "  [AL LASER HIT -0x%02X] RAX = 0x%016llX | AX = 0x%04X | AH = 0x%02X (untouched) | AL = 0x%02X\n"
aegis_recharge_ah_fmt:
    .asciz "  [AH BOOST +0x%02X]     RAX = 0x%016llX | AX = 0x%04X | AH = 0x%02X (updated)   | AL = 0x%02X (untouched)\n"
aegis_flags_fmt:
    .asciz "  [FLAG CHECK (50-50)] Remaining = %lld | ZF=%d (Broken) SF=%d CF=%d OF=%d\n"
aegis_breach_fmt:
    .asciz "  [HULL BREACH (20-50)] Remaining = %lld | ZF=%d SF=%d (Penetrated) CF=%d OF=%d\n"
aegis_sib_fmt:
    .asciz "  [SIB SHIELD ACCESS] [RBX + RSI*8 + 16] Layer %lld = 0x%016llX\n==============================================================================\n"

# 16-byte header + 4 quadword shield layers for SIB lookup
.align 8
aegis_layers_x64:
    .quad 0x1111111122222222    # Header +0
    .quad 0x3333333344444444    # Header +8
    .quad 0xAAAA000000000001    # Layer 0 (+16)
    .quad 0xBBBB000000000002    # Layer 1 (+24)
    .quad 0xCCCC000000000003    # Layer 2 (+32)
    .quad 0xDDDD000000000004    # Layer 3 (+40)

.text

# ------------------------------------------------------------------------------
# [TOPIC: Sub-Register Initialization] coa_init_aegis_shield_x64
# Purpose   : Loads composite hex value so RAX/EAX/AX/AH/AL slices are distinct.
# Game Role : Spawns the Aegis Sentry enemy with 4 nested shield layers in 1 register.
# Output    : RAX = 0xFEDCBA98A5B6C7D8 (AH = 0xC7, AL = 0xD8)
# ------------------------------------------------------------------------------
.global coa_init_aegis_shield_x64
.global _coa_init_aegis_shield_x64
coa_init_aegis_shield_x64:
_coa_init_aegis_shield_x64:
    mov  rax, 0xFEDCBA98A5B6C7D8 # WHY: Packs all 4 shield tiers into one 64-bit register (AH=0xC7, AL=0xD8)
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Sub-Register AL Damage] coa_damage_al_layer_x64
# Purpose   : Subtracts 8-bit damage from AL only; AH and upper 48 bits stay intact.
# Game Role : Applies player laser hits directly to Aegis Sentry's inner AL shield.
# Inputs    : RCX = currentShield (uint64_t), DL = damageAmount (uint8_t)
# Output    : RAX = updated 64-bit shield state
# ------------------------------------------------------------------------------
.global coa_damage_al_layer_x64
.global _coa_damage_al_layer_x64
coa_damage_al_layer_x64:
_coa_damage_al_layer_x64:
    mov  rax, rcx               # WHY: Copy composite shield into accumulator RAX
    sub  al, dl                 # WHY: 8-bit SUB on AL hits bits 0..7 only; AH stays untouched & total RAX updates in hardware
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Sub-Register AH Recharge] coa_recharge_ah_layer_x64
# Purpose   : Adds 8-bit boost to AH only; AL and upper 48 bits stay intact.
# Game Role : Recharges Aegis Sentry's outer AH damper independently of AL damage.
# Inputs    : RCX = currentShield (uint64_t), DL = boostAmount (uint8_t)
# Output    : RAX = updated 64-bit shield state
# ------------------------------------------------------------------------------
.global coa_recharge_ah_layer_x64
.global _coa_recharge_ah_layer_x64
coa_recharge_ah_layer_x64:
_coa_recharge_ah_layer_x64:
    mov  rax, rcx
    add  ah, dl                 # WHY: 8-bit ADD on AH updates bits 8..15 while leaving core AL layer untouched
    ret

# ------------------------------------------------------------------------------
# [TOPIC: RFLAGS Shield Evaluation (ZF, SF, CF, OF)] coa_eval_shield_flags_x64
# Purpose   : Subtracts damage from shield and captures ZF, SF, CF, OF flags.
# Game Role : Detects shield break (ZF=1) and hull penetration (SF=1, CF=1) in combat.
# Inputs    : RCX=shield, RDX=damage, R8=&zf, R9=&sf, [rbp+48]=&cf, [rbp+56]=&of
# Output    : RAX = remaining shield (shield - damage)
# ------------------------------------------------------------------------------
.global coa_eval_shield_flags_x64
.global _coa_eval_shield_flags_x64
coa_eval_shield_flags_x64:
_coa_eval_shield_flags_x64:
    push rbp
    mov  rbp, rsp
    push r12
    push r13

    mov  rax, rcx
    sub  rax, rdx               # WHY: Single subtraction computes remaining shield AND sets RFLAGS automatically

    setz r10b                   # WHY: ZF=1 -> Shield reduced to exactly 0 (Shield Broken!)
    sets r11b                   # WHY: SF=1 -> Result negative (Laser pierced shield into enemy hull!)
    setb r12b                   # WHY: CF=1 -> Unsigned borrow (Damage exceeded shield capacity)
    seto r13b                   # WHY: OF=1 -> Signed integer limit overflowed

    test r8, r8
    jz   .L_skip_zf
    movzx ecx, r10b
    mov  [r8], ecx
.L_skip_zf:
    test r9, r9
    jz   .L_skip_sf
    movzx edx, r11b
    mov  [r9], edx
.L_skip_sf:
    mov  r10, [rbp + 48]        # WHY: Arg5 (&cf) sits at [RBP+48] past saved RBP, return address, and 32-byte shadow
    test r10, r10
    jz   .L_skip_cf
    movzx ecx, r12b
    mov  [r10], ecx
.L_skip_cf:
    mov  r11, [rbp + 56]        # WHY: Arg6 (&of) sits at [RBP+56]
    test r11, r11
    jz   .L_skip_of
    movzx edx, r13b
    mov  [r11], edx
.L_skip_of:
    pop  r13
    pop  r12
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: SIB Addressing Shield Lookup] coa_sib_shield_layer_x64
# Purpose   : Reads shield layer telemetry via [Base + Index*8 + 16] in one cycle.
# Game Role : Lets Aegis Sentry switch/query any shield tier instantly without loops.
# Inputs    : RCX = basePtr, RDX = layerIndex
# Output    : RAX = 64-bit telemetry value
# ------------------------------------------------------------------------------
.global coa_sib_shield_layer_x64
.global _coa_sib_shield_layer_x64
coa_sib_shield_layer_x64:
_coa_sib_shield_layer_x64:
    mov  rax, [rcx + rdx*8 + 16] # WHY: SIB skips 16-byte header and scales index by 8 bytes in a single hardware cycle
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO1 Enemy Demo - Aegis Sentry] coa_demo_aegis_sentry
# Purpose   : Console walkthrough of Aegis Sentry sub-registers, flags, and SIB.
# Game Role : Demonstrates CO1 enemy combat events live from assembly.
# ------------------------------------------------------------------------------
.global coa_demo_aegis_sentry
.global _coa_demo_aegis_sentry
.global coa_demo_aegis_sentry_x64
.global _coa_demo_aegis_sentry_x64
coa_demo_aegis_sentry:
_coa_demo_aegis_sentry:
coa_demo_aegis_sentry_x64:
_coa_demo_aegis_sentry_x64:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 80

    lea  rcx, [rip + aegis_title]
    call printf

    # Step 1: Spawn enemy shield
    call coa_init_aegis_shield_x64
    mov  rbx, rax

    lea  rcx, [rip + aegis_init_fmt]
    mov  rdx, rbx
    mov  r8d, ebx
    movzx r9d, bx
    movzx eax, bh               # WHY: Read AH without REX prefix before storing onto stack
    mov  [rsp + 32], eax
    movzx eax, bl
    mov  [rsp + 40], eax
    call printf

    # Step 2: Laser damages core AL layer (-0x20), keeping AH unchanged
    mov  rcx, rbx
    mov  rdx, 0x20
    call coa_damage_al_layer_x64
    mov  rbx, rax

    lea  rcx, [rip + aegis_damage_al_fmt]
    mov  rdx, 0x20
    mov  r8,  rbx
    movzx r9d, bx
    movzx eax, bh
    mov  [rsp + 32], eax
    movzx eax, bl
    mov  [rsp + 40], eax
    call printf

    # Step 3: Capacitor boosts outer AH damper (+0x10), keeping AL unchanged
    mov  rcx, rbx
    mov  rdx, 0x10
    call coa_recharge_ah_layer_x64
    mov  rbx, rax

    lea  rcx, [rip + aegis_recharge_ah_fmt]
    mov  rdx, 0x10
    mov  r8,  rbx
    movzx r9d, bx
    movzx eax, bh
    mov  [rsp + 32], eax
    movzx eax, bl
    mov  [rsp + 40], eax
    call printf

    # Step 4: Flag evaluations (50-50 = shield broken; 20-50 = hull breached)
    lea  rax, [rsp + 64]
    lea  rsi, [rsp + 68]
    lea  r10, [rsp + 72]
    lea  r11, [rsp + 76]
    mov  rcx, 50
    mov  rdx, 50
    mov  r8,  rax
    mov  r9,  rsi
    mov  [rsp + 32], r10
    mov  [rsp + 40], r11
    call coa_eval_shield_flags_x64

    lea  rcx, [rip + aegis_flags_fmt]
    mov  rdx, rax
    mov  r8d, [rsp + 64]
    mov  r9d, [rsp + 68]
    mov  eax, [rsp + 72]
    mov  [rsp + 32], eax
    mov  eax, [rsp + 76]
    mov  [rsp + 40], eax
    call printf

    lea  rax, [rsp + 64]
    lea  rsi, [rsp + 68]
    lea  r10, [rsp + 72]
    lea  r11, [rsp + 76]
    mov  rcx, 20
    mov  rdx, 50
    mov  r8,  rax
    mov  r9,  rsi
    mov  [rsp + 32], r10
    mov  [rsp + 40], r11
    call coa_eval_shield_flags_x64

    lea  rcx, [rip + aegis_breach_fmt]
    mov  rdx, rax
    mov  r8d, [rsp + 64]
    mov  r9d, [rsp + 68]
    mov  eax, [rsp + 72]
    mov  [rsp + 32], eax
    mov  eax, [rsp + 76]
    mov  [rsp + 40], eax
    call printf

    # Step 5: SIB Shield Layer Lookup
    lea  rcx, [rip + aegis_layers_x64]
    mov  rdx, 1
    call coa_sib_shield_layer_x64

    lea  rcx, [rip + aegis_sib_fmt]
    mov  rdx, 1
    mov  r8,  rax
    call printf

    add  rsp, 80
    pop  rsi
    pop  rbx
    pop  rbp
    ret
