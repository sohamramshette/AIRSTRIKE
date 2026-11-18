# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO2: FIBONACCI SKIMMER ENEMY
# Utilizes genuine 64-bit assembly iterative Fibonacci series (asm_fibonacci)
# to generate sinusoidal flight trajectory coordinates and dynamic salvo timings.
# Satisfies Course Outcome: COA-CO2 (Mathematical Series & Applied Addressing)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
skimmer_title:
    .asciz "\n==============================================================================\n        COA-CO2: FIBONACCI SKIMMER - 64-BIT MATHEMATICAL FLIGHT WEAVE         \n==============================================================================\n"

skimmer_hdr:
    .asciz "  Step  |  Fibonacci Term F(n)  |  Weave Offset  |  Target Screen X Coordinate\n  ------+-----------------------+---------------+----------------------------\n"

skimmer_row_fmt:
    .asciz "  [%02lld]  |  F(%lld) = %-12lld |  %+11lld  |  X = %lld px\n"

skimmer_fire_fmt:
    .asciz "\n  [WEAPON TIMING] Kinetic Salvo Interval for Step %lld:\n    Base Rate = 100 ms * F(%lld) [%lld] = %lld ms cooldown\n==============================================================================\n"

.bss
.align 8
.lcomm fib_temp_buf, 128        # Storage for up to 16 Fibonacci quadwords (16 * 8 = 128 bytes)

.text

# ==============================================================================
# Routine: coa_skimmer_compute_trajectory
# - Small Meaning: Calculates enemy flight path coordinates using 64-bit Fibonacci.
# - Long Form    : COA Fibonacci Skimmer Trajectory Coordinate Evaluator
# - Why We Used It: Generates non-linear evasive maneuvers for AI interceptors.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t* outX     (Output array pointer for screen X coordinates)
#   RDX = int64_t  count    (Number of coordinate steps, 1..16)
#   R8  = int64_t  baseX    (Screen center X baseline, e.g. 400)
#   R9  = int64_t  maxWeave (Maximum lateral excursion amplitude, e.g. 150)
# ==============================================================================
.global coa_skimmer_compute_trajectory
.global _coa_skimmer_compute_trajectory
coa_skimmer_compute_trajectory:
_coa_skimmer_compute_trajectory:
    push rbp
    mov rbp, rsp
    sub rsp, 48                 # Shadow space (32) + alignment
    push rbx
    push rsi
    push rdi
    push r12
    push r13

    mov rdi, rcx                # RDI = outX pointer
    mov r12, rdx                # R12 = count
    mov r13, r8                 # R13 = baseX
    mov rbx, r9                 # RBX = maxWeave

    cmp r12, 0
    jle .L_traj_exit
    cmp r12, 16
    jle .L_traj_count_ok
    mov r12, 16                 # Clamp maximum trajectory count to 16

.L_traj_count_ok:
    # 1. Compute 64-bit Fibonacci sequence into fib_temp_buf
    lea rcx, [rip + fib_temp_buf]
    mov rdx, r12
    call asm_fibonacci

    # 2. Iterate through terms and generate alternating weave coordinates
    xor rsi, rsi                # RSI = step index i (0 .. count - 1)

.L_traj_loop:
    cmp rsi, r12
    jge .L_traj_exit

    lea r10, [rip + fib_temp_buf]
    mov rax, [r10 + rsi*8]      # RAX = F(i)

    cmp rbx, 0
    jle .L_no_modulo

    # --------------------------------------------------------------------------
    # CQO / IDIV RBX:
    # - Small Meaning: 64-bit signed modulo operation: RDX = RAX % RBX
    # - Long Form    : Convert Quadword to Octaword and Signed Divide by RBX
    # - Why We Used It: Bounds the unbounded Fibonacci growth within screen width.
    # --------------------------------------------------------------------------
    cqo
    idiv rbx                    # RDX = remainder = F(i) % maxWeave
    mov rax, rdx                # RAX = bounded lateral weave offset

.L_no_modulo:
    # Check parity of step index i to alternate left/right weave
    test rsi, 1
    jnz .L_odd_step

.L_even_step:
    # Positive weave: baseX + offset
    lea rdx, [r13 + rax]
    jmp .L_store_x

.L_odd_step:
    # Negative weave: baseX - offset
    mov rdx, r13
    sub rdx, rax

.L_store_x:
    mov [rdi + rsi*8], rdx      # Store 64-bit screen X into array
    inc rsi
    jmp .L_traj_loop

.L_traj_exit:
    pop r13
    pop r12
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: coa_skimmer_fire_interval
# - Small Meaning: Computes weapon salvo cooldown dynamically based on F(step).
# - Long Form    : COA Skimmer Dynamic Fire Interval Calculator
# - Why We Used It: Modulates enemy attack cadence based on Fibonacci progression.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t stepIndex      (Current step index, 0..15)
#   RDX = int64_t baseCooldownMs (Base cooldown unit, e.g. 100 ms)
# Returns:
#   RAX = 64-bit cooldown in milliseconds
# ==============================================================================
.global coa_skimmer_fire_interval
.global _coa_skimmer_fire_interval
coa_skimmer_fire_interval:
_coa_skimmer_fire_interval:
    push rbp
    mov rbp, rsp
    sub rsp, 48                 # Shadow space + alignment
    push rbx
    push rsi

    mov rsi, rcx                # RSI = stepIndex
    mov rbx, rdx                # RBX = baseCooldownMs

    test rsi, rsi
    jns .L_step_valid
    xor rsi, rsi                # Clamp negative step index to 0

.L_step_valid:
    cmp rsi, 15
    jle .L_step_clamp
    mov rsi, 15

.L_step_clamp:
    # Generate 16 Fibonacci terms
    lea rcx, [rip + fib_temp_buf]
    mov rdx, 16
    call asm_fibonacci

    lea r10, [rip + fib_temp_buf]
    mov rax, [r10 + rsi*8]      # RAX = F(stepIndex)
    test rax, rax
    jnz .L_fib_nonzero
    mov rax, 1                  # Minimum multiplier 1 if F(0) == 0

.L_fib_nonzero:
    # --------------------------------------------------------------------------
    # IMUL RAX, RBX:
    # - Small Meaning: Multiply Fibonacci factor by base cooldown
    # - Long Form    : Signed 64-Bit Integer Multiply
    # - Why We Used It: Scales cooldown proportionally to mathematical sequence.
    # --------------------------------------------------------------------------
    imul rax, rbx

    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: coa_demo_fibonacci_skimmer
# - Small Meaning: Interactive demonstration of the Fibonacci Skimmer enemy.
# - Long Form    : CO2 Demonstration Driver for Fibonacci Skimmer Flight
# - Why We Used It: Outputs formatted coordinate telemetry and firing timings.
# ==============================================================================
.global coa_demo_fibonacci_skimmer
.global _coa_demo_fibonacci_skimmer
coa_demo_fibonacci_skimmer:
_coa_demo_fibonacci_skimmer:
    push rbp
    mov rbp, rsp
    sub rsp, 128                # Stack buffer for 8 coordinates (64 bytes) + shadow
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + skimmer_title]
    call print_str

    lea rcx, [rip + skimmer_hdr]
    call print_str

    # Compute 8 trajectory points: baseX = 400, maxWeave = 120
    lea rcx, [rbp - 64]         # RCX = &local_coords[0]
    mov rdx, 8                  # RDX = 8 steps
    mov r8,  400                # R8  = baseX
    mov r9,  120                # R9  = maxWeave
    call coa_skimmer_compute_trajectory

    # Display trajectory table
    xor rsi, rsi
.L_demo_fib_loop:
    cmp rsi, 8
    jge .L_demo_fib_fire

    mov rdx, [rbp - 64 + rsi*8] # RDX = Screen X
    mov rax, rdx
    sub rax, 400                # RAX = Offset relative to 400
    lea r10, [rip + fib_temp_buf]
    mov rbx, [r10 + rsi*8]      # RBX = F(rsi)

    lea rcx, [rip + skimmer_row_fmt]
    mov rdx, rsi                # 1st: Step
    mov r8,  rsi                # 2nd: n
    mov r9,  rbx                # 3rd: F(n)
    mov [rsp + 32], rax         # 4th: Weave offset on stack
    mov [rsp + 40], rdx         # 5th: Screen X on stack
    call printf

    inc rsi
    jmp .L_demo_fib_loop

.L_demo_fib_fire:
    # Demonstrate salvo timing for step 5
    mov rcx, 5                  # Step 5
    mov rdx, 100                # 100 ms base
    call coa_skimmer_fire_interval
    mov rbx, rax                # RBX = Result cooldown

    lea rcx, [rip + skimmer_fire_fmt]
    mov rdx, 5
    mov r8,  5
    lea r10, [rip + fib_temp_buf]
    mov r9,  [r10 + 5*8]        # F(5)
    mov [rsp + 32], rbx         # Calculated cooldown ms on stack
    call printf

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
