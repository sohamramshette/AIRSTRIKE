# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - Multiplayer Combat Architecture Demonstrator
# Genuine 32-bit Intel 80386 Assembly (Protected Mode, cdecl)
# Demonstrates:
# - COA-CO1: Compare scores (CMP), condition flags (ZF, SF, CF, OF), SIB addressing [EBX + ECX*4 + 8]
# - COA-CO2: Fibonacci attack cadence calculation via iterative 80386 assembly
# - COA-CO3: Booth's signed multiplication for score delta and penalty calculation
# - COA-CO4: 5-stage pipeline execution simulation for match-event packet streams
# ==============================================================================

.intel_syntax noprefix

.data
coa_mp_title:
    .asciz "\n==============================================================================\n      COA: MULTIPLAYER COMBAT ARCHITECTURE & ALGORITHMS DEMONSTRATOR         \n      Target Architecture: Genuine 32-bit Intel 80386 (CO1 to CO4)             \n==============================================================================\n"

coa_mp_co1_hdr:
    .asciz "\n--- 1. COA-CO1: CMP Instruction, Condition Flags & SIB Addressing ---\n"
coa_mp_cmp_fmt:
    .asciz "  Comparing Player 1 Score (%d) vs Player 2 Score (%d):\n    CMP EAX, EDX Evaluated.\n    Zero Flag (ZF)     : %d (1 = Scores Tied)\n    Sign Flag (SF)     : %d (1 = Result Negative)\n    Carry Flag (CF)    : %d (1 = Unsigned Borrow)\n    Overflow Flag (OF) : %d (1 = Signed Two's Complement Overflow)\n    Match Leader       : %s\n"

leader_p1_str:
    .asciz "PLAYER 1 LEADING (Greater Score)"
leader_p2_str:
    .asciz "PLAYER 2 LEADING (Lesser Score)"
leader_tie_str:
    .asciz "TIED MATCH (Equal Scores)"

coa_mp_sib_hdr:
    .asciz "\n  Scale-Index-Base (SIB) Addressing Mode Demonstration:\n    Effective Address = [EBX (Base) + ECX (Index) * 4 + 8 (Disp)]\n"
coa_mp_sib_fmt:
    .asciz "    Slot [%d] -> Telemetry Record: 0x%08X (%d points)\n"

coa_mp_co2_hdr:
    .asciz "\n--- 2. COA-CO2: 80386 Assembly Fibonacci Attack Cadence ---\n"
coa_mp_fib_fmt:
    .asciz "  Term F(%d) = %d -> Calculated Attack Cooldown: %d ms\n"

coa_mp_co3_hdr:
    .asciz "\n--- 3. COA-CO3: Booth's Signed Multiplication for Score Deltas ---\n"
coa_mp_booth_fmt:
    .asciz "  Base Delta: %d | Multiplier: %d -> Booth Signed Product: %d\n"

coa_mp_co4_hdr:
    .asciz "\n--- 4. COA-CO4: 5-Stage Pipeline Simulation for Packet Stream ---\n"
coa_mp_pipe_fmt:
    .asciz "  Packet Count: %d | Mode: %s\n    Total Cycles : %d | Stall Bubbles : %d | Speedup : %d.%02dx\n==============================================================================\n"

mode_ideal_str:
    .asciz "Ideal Execution (No Hazards)"
mode_raw_str:
    .asciz "RAW Hazard with Pipeline Stalls"
mode_fwd_str:
    .asciz "RAW Hazard with Operand Forwarding Bypass"

# Sample 32-bit score table with 8-byte header offset for SIB demo
.align 4
mp_score_table:
    .long 0xDEADBEEF        # Header Offset +0
    .long 0xCAFEBABE        # Header Offset +4
    .long 1250              # Slot 0 [Base + 0*4 + 8]
    .long 3400              # Slot 1 [Base + 1*4 + 8]
    .long 4800              # Slot 2 [Base + 2*4 + 8]
    .long 6200              # Slot 3 [Base + 3*4 + 8]

.text

# ------------------------------------------------------------------------------
# coa_mp_compare_scores: Compares two player scores and returns condition flags
# Prototype: int coa_mp_compare_scores(int score1, int score2, int* outZF, int* outSF, int* outCF, int* outOF)
# Returns: 1 if score1 > score2, 0 if equal, -1 if score1 < score2
# ------------------------------------------------------------------------------
.global _coa_mp_compare_scores
_coa_mp_compare_scores:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov eax, [ebp + 8]          # score1
    mov edx, [ebp + 12]         # score2

    cmp eax, edx

    # Capture flags immediately before any other instruction modifies them
    setz cl                     # cl = ZF
    sets ch                     # ch = SF
    setb bl                     # bl = CF
    seto bh                     # bh = OF

    # Compute signed comparison outcome
    jg .L_score_greater
    je .L_score_equal
    mov edi, -1
    jmp .L_cmp_write_flags

.L_score_greater:
    mov edi, 1
    jmp .L_cmp_write_flags

.L_score_equal:
    xor edi, edi

.L_cmp_write_flags:
    # Write ZF
    mov esi, [ebp + 16]         # outZF
    test esi, esi
    jz .L_skip_zf
    movzx eax, cl
    mov [esi], eax
.L_skip_zf:

    # Write SF
    mov esi, [ebp + 20]         # outSF
    test esi, esi
    jz .L_skip_sf
    movzx eax, ch
    mov [esi], eax
.L_skip_sf:

    # Write CF
    mov esi, [ebp + 24]         # outCF
    test esi, esi
    jz .L_skip_cf
    movzx eax, bl
    mov [esi], eax
.L_skip_cf:

    # Write OF
    mov esi, [ebp + 28]         # outOF
    test esi, esi
    jz .L_skip_of
    movzx eax, bh
    mov [esi], eax
.L_skip_of:

    mov eax, edi                # Return comparison outcome

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_mp_sib_lookup: Accesses element using genuine SIB [Base + Index*4 + 8]
# Prototype: int coa_mp_sib_lookup(const int* arrayBase, int index)
# ------------------------------------------------------------------------------
.global _coa_mp_sib_lookup
_coa_mp_sib_lookup:
    push ebp
    mov ebp, esp
    push ebx

    mov ebx, [ebp + 8]          # Base address
    mov ecx, [ebp + 12]         # Index

    # Genuine 80386 SIB instruction: [Base + Index*Scale + Disp]
    mov eax, [ebx + ecx*4 + 8]

    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_mp_fibonacci_interval: Computes F(n) using 80386 iterative loop
# Prototype: int coa_mp_fibonacci_interval(int n)
# ------------------------------------------------------------------------------
.global _coa_mp_fibonacci_interval
_coa_mp_fibonacci_interval:
    push ebp
    mov ebp, esp
    push ebx

    mov ecx, [ebp + 8]          # n
    cmp ecx, 0
    jle .L_fib_zero
    cmp ecx, 1
    je .L_fib_one

    # Iterative calculation in 32-bit registers
    mov eax, 0                  # F(0)
    mov ebx, 1                  # F(1)
    dec ecx                     # loop n-1 times

.L_fib_loop:
    mov edx, eax
    add edx, ebx                # F(i) = F(i-1) + F(i-2)
    mov eax, ebx
    mov ebx, edx
    loop .L_fib_loop

    mov eax, ebx
    jmp .L_fib_exit

.L_fib_zero:
    xor eax, eax
    jmp .L_fib_exit

.L_fib_one:
    mov eax, 1

.L_fib_exit:
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_mp_booth_score_delta: Calls 8-bit Booth multiplication for signed score deltas
# Prototype: int coa_mp_booth_score_delta(int baseDelta, int multiplier)
# ------------------------------------------------------------------------------
.global _coa_mp_booth_score_delta
_coa_mp_booth_score_delta:
    push ebp
    mov ebp, esp

    # _asm_booth_multiply(M, Q, verbose=0)
    push 0                      # verbose = 0
    push dword ptr [ebp + 12]   # Q (multiplier)
    push dword ptr [ebp + 8]    # M (baseDelta)
    call _asm_booth_multiply
    add esp, 12

    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_mp_pipeline_stream: Evaluates 5-stage pipeline for packet stream
# Prototype: int coa_mp_pipeline_stream(int packetCount, int mode, int* outCycles, int* outStalls)
# mode: 0 = Ideal (0 stalls), 1 = RAW Hazard (2 stalls), 2 = Operand Forwarding (0 stalls)
# ------------------------------------------------------------------------------
.global _coa_mp_pipeline_stream
_coa_mp_pipeline_stream:
    push ebp
    mov ebp, esp
    push ebx
    push esi

    mov eax, [ebp + 8]          # packetCount (N)
    mov edx, [ebp + 12]         # mode

    # Base pipelined cycles: T_pipe = 5 + (N - 1)
    lea ecx, [eax + 4]          # 5 + N - 1 = N + 4
    xor ebx, ebx                # stalls = 0

    cmp edx, 1                  # mode 1: RAW Hazard with stalls
    jne .L_check_fwd
    add ecx, 2                  # 2 stall cycles
    mov ebx, 2
    jmp .L_pipe_store

.L_check_fwd:
    # mode 0 or 2: 0 stalls
    xor ebx, ebx

.L_pipe_store:
    mov esi, [ebp + 16]         # outCycles
    test esi, esi
    jz .L_skip_cycles
    mov [esi], ecx
.L_skip_cycles:

    mov esi, [ebp + 20]         # outStalls
    test esi, esi
    jz .L_skip_stalls
    mov [esi], ebx
.L_skip_stalls:

    mov eax, ecx                # return total cycles

    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# coa_demo_multiplayer: Interactive runner for master menu
# ------------------------------------------------------------------------------
.global _coa_demo_multiplayer
_coa_demo_multiplayer:
    push ebp
    mov ebp, esp
    sub esp, 32                 # Local storage for flags & metrics
    push ebx
    push esi
    push edi

    push offset coa_mp_title
    call _printf
    add esp, 4

    # 1. COA-CO1: Score Comparison & Condition Flags
    push offset coa_mp_co1_hdr
    call _printf
    add esp, 4

    lea eax, [ebp - 16]         # outOF
    lea ebx, [ebp - 12]         # outCF
    lea ecx, [ebp - 8]          # outSF
    lea edx, [ebp - 4]          # outZF

    push eax
    push ebx
    push ecx
    push edx
    push 3200                   # score2
    push 4800                   # score1
    call _coa_mp_compare_scores
    add esp, 24

    push offset leader_p1_str
    push dword ptr [ebp - 16]   # OF
    push dword ptr [ebp - 12]   # CF
    push dword ptr [ebp - 8]    # SF
    push dword ptr [ebp - 4]    # ZF
    push 3200
    push 4800
    push offset coa_mp_cmp_fmt
    call _printf
    add esp, 28

    # SIB Demonstration
    push offset coa_mp_sib_hdr
    call _printf
    add esp, 4

    xor esi, esi
.L_demo_sib_loop:
    push esi
    push offset mp_score_table
    call _coa_mp_sib_lookup
    add esp, 8

    push eax
    push eax
    push esi
    push offset coa_mp_sib_fmt
    call _printf
    add esp, 16

    inc esi
    cmp esi, 4
    jl .L_demo_sib_loop

    # 2. COA-CO2: Fibonacci Attack Cadence
    push offset coa_mp_co2_hdr
    call _printf
    add esp, 4

    push 6                      # F(6)
    call _coa_mp_fibonacci_interval
    add esp, 4
    imul edx, eax, 100          # 100ms multiplier

    push edx
    push eax
    push 6
    push offset coa_mp_fib_fmt
    call _printf
    add esp, 16

    # 3. COA-CO3: Booth's Signed Multiplication
    push offset coa_mp_co3_hdr
    call _printf
    add esp, 4

    push -3                     # Multiplier
    push 15                     # Base delta
    call _coa_mp_booth_score_delta
    add esp, 8

    push eax
    push -3
    push 15
    push offset coa_mp_booth_fmt
    call _printf
    add esp, 16

    # 4. COA-CO4: 5-Stage Pipeline Simulation
    push offset coa_mp_co4_hdr
    call _printf
    add esp, 4

    lea eax, [ebp - 24]         # outStalls
    lea ebx, [ebp - 20]         # outCycles
    push eax
    push ebx
    push 1                      # Mode 1: RAW Hazard
    push 8                      # 8 packets
    call _coa_mp_pipeline_stream
    add esp, 16

    # T_seq = 8 * 5 = 40 cycles. T_pipe = 14 cycles. Speedup = 4000 / 14 = 285 -> 2.85x
    push 85
    push 2
    push dword ptr [ebp - 24]   # Stalls
    push dword ptr [ebp - 20]   # Cycles
    push offset mode_raw_str
    push 8
    push offset coa_mp_pipe_fmt
    call _printf
    add esp, 28

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
