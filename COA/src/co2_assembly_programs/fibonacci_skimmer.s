# ==============================================================================
# File    : COA/src/co2_assembly_programs/fibonacci_skimmer.s
# Subject : COA-CO2 Enemy | Fibonacci Skimmer
# Topics  : Fibonacci Sequence, Zigzag Flight Trajectory, Weapon Salvo Cadence,
#           Modulo Clamping (CQO + IDIV), Scaled-Index Addressing
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
skimmer_title:
    .asciz "\n==============================================================================\n        COA-CO2: FIBONACCI SKIMMER - MATHEMATICAL FLIGHT WEAVE                \n==============================================================================\n"

skimmer_hdr:
    .asciz "  Step  |  Fibonacci Term F(n)  |  Weave Offset  |  Target Screen X\n  ------+-----------------------+----------------+-----------------\n"

skimmer_row_fmt:
    .asciz "  [%02lld]  |  F(%lld) = %-12lld |  %+12lld  |  X = %lld px\n"

skimmer_fire_fmt:
    .asciz "\n  [WEAPON TIMING] Step %lld: 100 ms * F(%lld) [%lld] = %lld ms cooldown\n==============================================================================\n"

.bss
.align 8
.lcomm fib_temp_buf, 128        # 16 quadwords (16 * 8 bytes)

.text

# ------------------------------------------------------------------------------
# [TOPIC: Fibonacci Flight Trajectory] coa_skimmer_compute_trajectory
# Purpose   : Computes alternating left/right X positions around baseX:
#             baseX +/- (F(i) % maxWeave).
# Game Role : Steers the Fibonacci Skimmer enemy in an unpredictable zigzag dodge.
# Inputs    : RCX = outX[], RDX = count (1..16), R8 = baseX, R9 = maxWeave
# ------------------------------------------------------------------------------
.global coa_skimmer_compute_trajectory
.global _coa_skimmer_compute_trajectory
coa_skimmer_compute_trajectory:
_coa_skimmer_compute_trajectory:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push rdi
    push r12
    push r13
    sub  rsp, 40

    mov  rdi, rcx
    mov  r12, rdx
    mov  r13, r8
    mov  rbx, r9

    cmp  r12, 0
    jle  .L_traj_exit
    cmp  r12, 16                # WHY: Clamp step count to 16 so we never overflow fib_temp_buf
    jle  .L_traj_ok
    mov  r12, 16

.L_traj_ok:
    lea  rcx, [rip + fib_temp_buf]
    mov  rdx, r12
    call asm_fibonacci          # WHY: Reuse CO2 Fibonacci generator to fill sequence F(0)..F(count-1)

    xor  rsi, rsi
.L_traj_loop:
    cmp  rsi, r12
    jge  .L_traj_exit

    lea  r10, [rip + fib_temp_buf]
    mov  rax, [r10 + rsi*8]     # WHY: Scaled index [base + rsi*8] fetches 64-bit term F(i)

    cmp  rbx, 0
    jle  .L_no_mod
    cqo                         # WHY: Sign-extend RAX before IDIV for safe modulo arithmetic
    idiv rbx                    # WHY: Modulo F(i) % maxWeave keeps the enemy inside screen bounds!
    mov  rax, rdx

.L_no_mod:
    test rsi, 1                 # WHY: Bit-0 parity check alternates weave direction (even=right, odd=left)
    jnz  .L_odd_step
    lea  rdx, [r13 + rax]       # WHY: Even step weaves right (+offset)
    jmp  .L_store_x
.L_odd_step:
    mov  rdx, r13
    sub  rdx, rax               # WHY: Odd step weaves left (-offset)
.L_store_x:
    mov  [rdi + rsi*8], rdx     # WHY: Store final screen X coordinate for game engine
    inc  rsi
    jmp  .L_traj_loop

.L_traj_exit:
    add  rsp, 40
    pop  r13
    pop  r12
    pop  rdi
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Fibonacci Weapon Cooldown] coa_skimmer_fire_interval
# Purpose   : Computes weapon cooldown in ms as max(1, F(stepIndex)) * baseCooldownMs.
# Game Role : Drives Fibonacci Skimmer's dynamic laser burst timing in combat.
# Inputs    : RCX = stepIndex (0..15), RDX = baseCooldownMs
# Output    : RAX = cooldown in ms
# ------------------------------------------------------------------------------
.global coa_skimmer_fire_interval
.global _coa_skimmer_fire_interval
coa_skimmer_fire_interval:
_coa_skimmer_fire_interval:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 32

    mov  rsi, rcx
    mov  rbx, rdx

    test rsi, rsi               # WHY: Clamp negative step index to 0 safely
    jns  .L_step_nonneg
    xor  rsi, rsi
.L_step_nonneg:
    cmp  rsi, 15                # WHY: Clamp max index to 15 to match buffer size
    jle  .L_step_ok
    mov  rsi, 15
.L_step_ok:
    lea  rcx, [rip + fib_temp_buf]
    mov  rdx, 16
    call asm_fibonacci

    lea  r10, [rip + fib_temp_buf]
    mov  rax, [r10 + rsi*8]
    test rax, rax
    jnz  .L_fib_nz
    mov  rax, 1                 # WHY: Force multiplier >= 1 when F(0)=0 so gun doesn't fire at 0 ms infinitely
.L_fib_nz:
    imul rax, rbx               # WHY: Scale base cooldown by Fibonacci term F(step)

    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO2 Enemy Demo - Fibonacci Skimmer] coa_demo_fibonacci_skimmer
# Purpose   : Prints an 8-step flight weave table and weapon cooldown example.
# Game Role : Shows live Skimmer X positions and burst cooldown timing.
# ------------------------------------------------------------------------------
.global coa_demo_fibonacci_skimmer
.global _coa_demo_fibonacci_skimmer
coa_demo_fibonacci_skimmer:
_coa_demo_fibonacci_skimmer:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push r12
    push r13
    sub  rsp, 112

    lea  rcx, [rip + skimmer_title]
    call print_str
    lea  rcx, [rip + skimmer_hdr]
    call print_str

    lea  rcx, [rsp + 48]
    mov  rdx, 8
    mov  r8,  400
    mov  r9,  120
    call coa_skimmer_compute_trajectory

    xor  rsi, rsi
.L_demo_fib_loop:
    cmp  rsi, 8
    jge  .L_demo_fib_fire

    mov  r12, [rsp + 48 + rsi*8] # Screen X
    mov  r13, r12
    sub  r13, 400                # Weave offset around center X=400
    lea  r10, [rip + fib_temp_buf]
    mov  rbx, [r10 + rsi*8]      # F(rsi)

    lea  rcx, [rip + skimmer_row_fmt]
    mov  rdx, rsi
    mov  r8,  rsi
    mov  r9,  rbx
    mov  [rsp + 32], r13
    mov  [rsp + 40], r12
    call printf

    inc  rsi
    jmp  .L_demo_fib_loop

.L_demo_fib_fire:
    mov  rcx, 5
    mov  rdx, 100
    call coa_skimmer_fire_interval
    mov  rbx, rax

    lea  rcx, [rip + skimmer_fire_fmt]
    mov  rdx, 5
    mov  r8,  5
    lea  r10, [rip + fib_temp_buf]
    mov  r9,  [r10 + 5*8]
    mov  [rsp + 32], rbx
    call printf

    add  rsp, 112
    pop  r13
    pop  r12
    pop  rsi
    pop  rbx
    pop  rbp
    ret
