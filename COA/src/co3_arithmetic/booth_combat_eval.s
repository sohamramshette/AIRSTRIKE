# ==============================================================================
# File    : COA/src/co3_arithmetic/booth_combat_eval.s
# Subject : COA-CO3 Enemy | Booth Multiplier Gunship
# Topics  : Booth's Signed Multiplication, Kinetic Firepower, EMP Phase Inversion,
#           High-Energy Saturation, Sign Flag (SF) Combat Evaluation
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
booth_gunship_title:
    .asciz "\n==============================================================================\n      COA-CO3: BOOTH MULTIPLIER GUNSHIP - SIGNED ARITHMETIC FIREPOWER         \n==============================================================================\n"
booth_combat_hdr:
    .asciz "  Shot | Charge (M) | Velocity (Q) | Booth Product | Combat Effect\n  -----+------------+--------------+---------------+---------------------------\n"
booth_combat_row:
    .asciz "  [%02lld] | %-10lld | %-12lld | %-13lld | %s\n"
booth_summary_fmt:
    .asciz "==============================================================================\n"

res_normal:       .asciz "Normal Kinetic Strike [OK]"
res_neg_phase:    .asciz "Inverted Phase (EMP Drain)"
res_zero_dud:     .asciz "Neutral/Discharged Dud"
res_overflow_sat: .asciz "High-Energy Saturation"

.align 8
gunship_shots_m:  .quad 7,  9, -8, -11,  0, -25, -64
gunship_shots_q:  .quad 5, -4,  6,  -3, 45,   0,   2

.text

# ------------------------------------------------------------------------------
# [TOPIC: Booth Combat Firepower] coa_booth_calculate_kinetic_damage
# Purpose   : Computes signed damage (chargeM * velocityQ) via Booth's algorithm
#             and flags Negative (Bit 0), Zero (Bit 1), and Saturation (Bit 2).
# Game Role : Governs Booth Multiplier Gunship shots (Normal, EMP Drain, Dud, Saturated).
# Inputs    : RCX=chargeM, RDX=velocityQ, R8=&outDamage, R9=&outFlags
# Output    : RAX = signed damage product
# ------------------------------------------------------------------------------
.global coa_booth_calculate_kinetic_damage
.global _coa_booth_calculate_kinetic_damage
coa_booth_calculate_kinetic_damage:
_coa_booth_calculate_kinetic_damage:
    push rbp
    mov  rbp, rsp
    push rbx
    push r12
    push r13
    sub  rsp, 40

    mov  r12, r8
    mov  r13, r9

    xor  r8,  r8                # WHY: Call Booth multiplier silently (verbose=0) during fast combat tick
    call asm_booth_multiply     # WHY: Computes Charge * Velocity cleanly for both + and - values without IMUL
    mov  rbx, rax
    xor  rcx, rcx

    test rbx, rbx               # WHY: Sets ZF=1 if product == 0 -> Game: Neutral/Discharged Dud shot
    jnz  .L_chk_neg
    or   rcx, 2                 # Bit 1 = isZero (Dud)
    jmp  .L_chk_sat

.L_chk_neg:
    jns  .L_chk_sat             # WHY: Check Sign Flag (SF=1 if negative) -> Game: Inverted Phase EMP Drain shot!
    or   rcx, 1                 # Bit 0 = isNegative (EMP Drain)

.L_chk_sat:
    mov  rdx, rbx
    test rdx, rdx
    jns  .L_abs_ok
    neg  rdx                    # WHY: Take |product| via two's complement NEG to test threshold regardless of sign
.L_abs_ok:
    cmp  rdx, 100               # WHY: Shots >= 100 power trigger High-Energy Saturation overheat alert
    jl   .L_flags_ready
    or   rcx, 4                 # Bit 2 = isSaturated

.L_flags_ready:
    test r12, r12
    jz   .L_no_dmg
    mov  [r12], rbx
.L_no_dmg:
    test r13, r13
    jz   .L_no_flg
    mov  [r13], rcx
.L_no_flg:
    mov  rax, rbx

    add  rsp, 40
    pop  r13
    pop  r12
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO3 Enemy Demo - Booth Multiplier Gunship] coa_demo_booth_gunship
# Purpose   : Runs all 7 gunship shots through Booth's algorithm and prints table.
# Game Role : Demonstrates positive, EMP negative, dud, and saturated Gunship shots.
# ------------------------------------------------------------------------------
.global coa_demo_booth_gunship
.global _coa_demo_booth_gunship
coa_demo_booth_gunship:
_coa_demo_booth_gunship:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push r12
    push r13
    sub  rsp, 80

    lea  rcx, [rip + booth_gunship_title]
    call print_str
    lea  rcx, [rip + booth_combat_hdr]
    call print_str

    xor  rsi, rsi
.L_gunship_loop:
    cmp  rsi, 7                 # WHY: Evaluate all 7 shot vectors in table via loop (no duplicate code)
    jge  .L_gunship_done

    lea  r10, [rip + gunship_shots_m]
    mov  r12, [r10 + rsi*8]
    lea  r10, [rip + gunship_shots_q]
    mov  r13, [r10 + rsi*8]

    mov  rcx, r12
    mov  rdx, r13
    lea  r8,  [rsp + 48]
    lea  r9,  [rsp + 56]
    call coa_booth_calculate_kinetic_damage
    mov  rbx, rax

    mov  rax, [rsp + 56]        # WHY: Choose combat effect label from flag bits (Sat > Zero > EMP > Normal)
    test rax, 4
    jnz  .L_lbl_sat
    test rax, 2
    jnz  .L_lbl_zero
    test rax, 1
    jnz  .L_lbl_neg
    lea  rax, [rip + res_normal]
    jmp  .L_print_shot
.L_lbl_sat:
    lea  rax, [rip + res_overflow_sat]
    jmp  .L_print_shot
.L_lbl_zero:
    lea  rax, [rip + res_zero_dud]
    jmp  .L_print_shot
.L_lbl_neg:
    lea  rax, [rip + res_neg_phase]

.L_print_shot:
    lea  rcx, [rip + booth_combat_row]
    lea  rdx, [rsi + 1]
    mov  r8,  r12
    mov  r9,  r13
    mov  [rsp + 32], rbx
    mov  [rsp + 40], rax
    call printf

    inc  rsi
    jmp  .L_gunship_loop

.L_gunship_done:
    lea  rcx, [rip + booth_summary_fmt]
    call print_str

    add  rsp, 80
    pop  r13
    pop  r12
    pop  rsi
    pop  rbx
    pop  rbp
    ret
