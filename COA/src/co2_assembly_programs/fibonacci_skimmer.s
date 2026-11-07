# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO2: Fibonacci Skimmer Enemy
# Uses genuine 80386 assembly iterative Fibonacci series (_asm_fibonacci)
# to generate sinusoidal-like weaving trajectory coordinates and dynamic salvo timings.
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit Protected Mode, cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
skimmer_title:
    .asciz "\n==============================================================================\n        COA-CO2: FIBONACCI SKIMMER - ASSEMBLY MATHEMATICAL FLIGHT WEAVE        \n==============================================================================\n"

skimmer_hdr:
    .asciz "  Step  |  Fibonacci Term F(n)  |  Weave Offset  |  Target Screen X Coordinate\n  ------+-----------------------+---------------+----------------------------\n"

skimmer_row_fmt:
    .asciz "  [%02d]  |  F(%d) = %-12d |  %+11d  |  X = %d px\n"

skimmer_fire_fmt:
    .asciz "\n  [WEAPON TIMING] Kinetic Salvo Interval for Step %d:\n    Base Rate = 100 ms * F(%d) [%d] = %d ms cooldown\n==============================================================================\n"

.bss
.lcomm fib_temp_buf, 64         # Storage for up to 16 Fibonacci terms (16 * 4 bytes)

.text

# ------------------------------------------------------------------------------
# coa_skimmer_compute_trajectory: Computes screen X coordinates using Fibonacci
# Prototype: void coa_skimmer_compute_trajectory(int* outX, int count, int baseX, int maxWeave)
# Parameters:
#   [ebp + 8]  = outX array pointer
#   [ebp + 12] = count (number of steps, 1..16)
#   [ebp + 16] = baseX (e.g. 400)
#   [ebp + 20] = maxWeave (e.g. 150)
# ------------------------------------------------------------------------------
.global _coa_skimmer_compute_trajectory
_coa_skimmer_compute_trajectory:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov edi, [ebp + 8]          # edi = outX
    mov ecx, [ebp + 12]         # ecx = count
    cmp ecx, 0
    jle .L_traj_exit
    cmp ecx, 16
    jle .L_traj_count_ok
    mov ecx, 16                 # clamp to 16 max

.L_traj_count_ok:
    # 1. Compute raw Fibonacci terms using genuine 80386 assembly routine
    push ecx
    push offset fib_temp_buf
    call _asm_fibonacci
    add esp, 8

    # 2. Iterate through terms and generate alternating weave coordinates
    #    If step i is even: X[i] = baseX + (F[i] % maxWeave)
    #    If step i is odd : X[i] = baseX - (F[i] % maxWeave)
    xor esi, esi                # esi = step index (0 .. count-1)

.L_traj_loop:
    cmp esi, [ebp + 12]
    jge .L_traj_exit

    mov eax, [offset fib_temp_buf + esi * 4] # eax = F(i)
    mov ebx, [ebp + 20]         # ebx = maxWeave
    cmp ebx, 0
    jle .L_no_modulo

    # Modulo: edx = eax % ebx
    xor edx, edx
    div ebx                     # edx = remainder = F(i) % maxWeave
    mov eax, edx

.L_no_modulo:
    # Check parity of step index (test esi, 1)
    test esi, 1
    jnz .L_odd_step

.L_even_step:
    # Positive weave: baseX + offset
    mov edx, [ebp + 16]
    add edx, eax
    jmp .L_store_x

.L_odd_step:
    # Negative weave: baseX - offset
    mov edx, [ebp + 16]
    sub edx, eax

.L_store_x:
    mov [edi + esi * 4], edx
    inc esi
    jmp .L_traj_loop

.L_traj_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_skimmer_fire_interval: Computes weapon salvo cooldown based on F(step)
# Prototype: int coa_skimmer_fire_interval(int stepIndex, int baseCooldownMs)
# Parameters:
#   [ebp + 8]  = stepIndex (0..10)
#   [ebp + 12] = baseCooldownMs (e.g. 100)
# Returns:
#   EAX = cooldownMs
# ------------------------------------------------------------------------------
.global _coa_skimmer_fire_interval
_coa_skimmer_fire_interval:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov esi, [ebp + 8]          # stepIndex
    test esi, esi
    jns .L_step_valid
    xor esi, esi                # clamp negative to 0

.L_step_valid:
    cmp esi, 15
    jle .L_step_clamp
    mov esi, 15

.L_step_clamp:
    # Generate 16 Fibonacci terms
    push 16
    push offset fib_temp_buf
    call _asm_fibonacci
    add esp, 8

    mov eax, [offset fib_temp_buf + esi * 4] # F(stepIndex)
    test eax, eax
    jnz .L_fib_nonzero
    mov eax, 1                  # minimum multiplier 1 if F(0) = 0

.L_fib_nonzero:
    mov ebx, [ebp + 12]         # baseCooldownMs
    imul eax, ebx               # eax = F(step) * baseCooldownMs

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_demo_fibonacci_skimmer: Interactive demonstration of Fibonacci Skimmer
# ------------------------------------------------------------------------------
.global _coa_demo_fibonacci_skimmer
_coa_demo_fibonacci_skimmer:
    push ebp
    mov ebp, esp
    sub esp, 40                 # 8 coords * 4 bytes = 32 bytes + padding
    push ebx
    push esi
    push edi

    push offset skimmer_title
    call _print_str
    add esp, 4

    push offset skimmer_hdr
    call _print_str
    add esp, 4

    # Compute 8 trajectory points: baseX = 400, maxWeave = 120
    push 120
    push 400
    push 8
    lea eax, [ebp - 32]
    push eax
    call _coa_skimmer_compute_trajectory
    add esp, 16

    # Display table
    xor esi, esi
.L_demo_fib_loop:
    cmp esi, 8
    jge .L_demo_fib_fire

    mov edx, [ebp - 32 + esi * 4] # Screen X
    mov eax, edx
    sub eax, 400                # Offset relative to 400
    mov ebx, [offset fib_temp_buf + esi * 4] # F(esi)

    push edx                    # Screen X
    push eax                    # Weave offset
    push ebx                    # F(esi)
    push esi                    # n
    push esi                    # Step
    push offset skimmer_row_fmt
    call _printf
    add esp, 24

    inc esi
    jmp .L_demo_fib_loop

.L_demo_fib_fire:
    # Demonstrate salvo timing for step 5
    push 100                    # 100 ms base
    push 5                      # step 5 -> F(5) = 5
    call _coa_skimmer_fire_interval
    add esp, 8

    push eax                    # Result cooldown (500 ms)
    mov edx, offset fib_temp_buf
    mov ebx, [edx + 5 * 4]
    push ebx                    # F(5)
    push 5
    push 5
    push offset skimmer_fire_fmt
    call _printf
    add esp, 20

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
