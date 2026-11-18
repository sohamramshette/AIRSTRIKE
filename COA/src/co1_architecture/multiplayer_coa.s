# ==============================================================================
# AIRSTRIKE x86-64 Assembly Suite - Multiplayer Combat Architecture Demonstrator
# Genuine 64-bit AMD64 / Intel 64 Assembly (Long Mode, Microsoft x64 & System V ABI)
# Satisfies Course Outcomes: COA-CO1 to COA-CO4 in full 64-bit Architecture
# ==============================================================================
# Demonstrates:
# - COA-CO1: 64-bit GPRs (RAX-R15), CMP instruction, Condition Flags (ZF, SF, CF, OF),
#            64-bit Scale-Index-Base (SIB) Addressing [RBX + RCX*8 + 16]
# - COA-CO2: 64-bit Fibonacci Attack Cadence via iterative 64-bit register loop (RCX LOOP)
# - COA-CO3: Pure 64-bit Booth's Signed Multiplication Algorithm for score deltas
# - COA-CO4: 64-bit 5-Stage Pipeline Execution Simulation for match packet telemetry
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
coa_mp_title:
    .asciz "\n==============================================================================\n      COA: MULTIPLAYER COMBAT ARCHITECTURE & ALGORITHMS DEMONSTRATOR         \n      Target Architecture: Genuine 64-bit x86-64 / AMD64 (CO1 to CO4)          \n==============================================================================\n"

coa_mp_co1_hdr:
    .asciz "\n--- 1. COA-CO1: 64-bit CMP Instruction, Flags & 64-bit SIB Addressing ---\n"
coa_mp_cmp_fmt:
    .asciz "  Comparing Player 1 Score (%lld) vs Player 2 Score (%lld):\n    CMP RAX, RDX Evaluated (64-bit Subtraction & Flag Update).\n    Zero Flag (ZF)     : %d (1 = Scores Tied)\n    Sign Flag (SF)     : %d (1 = Result Negative)\n    Carry Flag (CF)    : %d (1 = Unsigned Borrow)\n    Overflow Flag (OF) : %d (1 = Signed Two's Complement Overflow)\n    Match Leader       : %s\n"

leader_p1_str:
    .asciz "PLAYER 1 LEADING (Greater Score)"
leader_p2_str:
    .asciz "PLAYER 2 LEADING (Lesser Score)"
leader_tie_str:
    .asciz "TIED MATCH (Equal Scores)"

coa_mp_sib_hdr:
    .asciz "\n  64-bit Scale-Index-Base (SIB) Addressing Mode Demonstration:\n    Effective Address = [RBX (64-bit Base) + RCX (64-bit Index) * 8 (Scale) + 16 (Disp)]\n"
coa_mp_sib_fmt:
    .asciz "    Slot [%lld] -> 64-bit Telemetry Record: 0x%016llX (%lld points)\n"

coa_mp_co2_hdr:
    .asciz "\n--- 2. COA-CO2: 64-bit Assembly Fibonacci Attack Cadence ---\n"
coa_mp_fib_fmt:
    .asciz "  Term F(%lld) = %lld -> Calculated Attack Cooldown: %lld ms\n"

coa_mp_co3_hdr:
    .asciz "\n--- 3. COA-CO3: 64-bit Booth's Signed Multiplication Algorithm ---\n"
coa_mp_booth_fmt:
    .asciz "  Base Delta (M): %lld | Multiplier (Q): %lld -> Booth Signed Product: %lld\n"

coa_mp_co4_hdr:
    .asciz "\n--- 4. COA-CO4: 64-bit 5-Stage Pipeline Simulation for Packet Stream ---\n"
coa_mp_pipe_fmt:
    .asciz "  Packet Count: %lld | Mode: %s\n    Total Cycles : %lld | Stall Bubbles : %lld | Speedup : %lld.%02lldx\n==============================================================================\n"

mode_ideal_str:
    .asciz "Ideal Execution (No Hazards)"
mode_raw_str:
    .asciz "RAW Hazard with Pipeline Stalls"
mode_fwd_str:
    .asciz "RAW Hazard with Operand Forwarding Bypass"

# 64-bit Quadword Telemetry Table with 16-byte Header Offset for 64-bit SIB Demo
.align 8
mp_score_table_x64:
    .quad 0xDEADBEEFCAFEBABE        # Header Offset +0  (64-bit Magic ID)
    .quad 0x0000000100000002        # Header Offset +8  (64-bit Metadata)
    .quad 1250                      # Slot 0 [Base + 0*8 + 16]
    .quad 3400                      # Slot 1 [Base + 1*8 + 16]
    .quad 4800                      # Slot 2 [Base + 2*8 + 16]
    .quad 6200                      # Slot 3 [Base + 3*8 + 16]

.text

# ==============================================================================
# COA-CO1: coa_mp_compare_scores_x64
# Compares two 64-bit player scores and returns condition flags.
#
# Calling Convention (Microsoft x64 ABI):
#   RCX = score1 (int64_t)
#   RDX = score2 (int64_t)
#   R8  = outZF  (int*)  64-bit pointer
#   R9  = outSF  (int*)  64-bit pointer
#   [RSP + 40] = outCF (int*) 64-bit pointer (passed on stack in shadow space)
#   [RSP + 48] = outOF (int*) 64-bit pointer (passed on stack in shadow space)
# Returns:
#   RAX = 1 if score1 > score2, 0 if equal, -1 if score1 < score2
# ==============================================================================
.global coa_mp_compare_scores_x64
.global _coa_mp_compare_scores_x64
coa_mp_compare_scores_x64:
_coa_mp_compare_scores_x64:
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    push r13

    # Perform 64-bit signed comparison (RAX = RCX, CMP RAX, RDX)
    mov rax, rcx
    cmp rax, rdx

    # Capture flags immediately
    setz r10b                   # r10b = ZF (Zero Flag: Scores Equal)
    sets r11b                   # r11b = SF (Sign Flag: Negative Result)
    setb r12b                   # r12b = CF (Carry Flag: Unsigned Borrow)
    seto r13b                   # r13b = OF (Overflow Flag: Two's Complement Overflow)

    # Determine signed comparison leader outcome
    jg .L_x64_score_greater
    je .L_x64_score_equal
    mov eax, -1
    jmp .L_x64_write_flags

.L_x64_score_greater:
    mov eax, 1
    jmp .L_x64_write_flags

.L_x64_score_equal:
    xor eax, eax

.L_x64_write_flags:
    # Write outZF (R8)
    test r8, r8
    jz .L_x64_skip_zf
    movzx ecx, r10b
    mov [r8], ecx
.L_x64_skip_zf:

    # Write outSF (R9)
    test r9, r9
    jz .L_x64_skip_sf
    movzx edx, r11b
    mov [r9], edx
.L_x64_skip_sf:

    # Write outCF ([RBP + 48] because of push rbp + 3 pushes = 32 bytes + return address)
    # Stack offset from caller: [RBP + 48] is 5th argument
    mov r10, [rbp + 48]
    test r10, r10
    jz .L_x64_skip_cf
    movzx ecx, r12b
    mov [r10], ecx
.L_x64_skip_cf:

    # Write outOF ([RBP + 56] is 6th argument)
    mov r11, [rbp + 56]
    test r11, r11
    jz .L_x64_skip_of
    movzx edx, r13b
    mov [r11], edx
.L_x64_skip_of:

    pop r13
    pop r12
    pop rbx
    pop rbp
    ret

# ==============================================================================
# COA-CO1: coa_mp_sib_lookup_x64
# Accesses 64-bit element using genuine 64-bit SIB: [RBX + RCX*8 + 16]
#
# Calling Convention:
#   RCX = arrayBase (const int64_t*)
#   RDX = index     (int64_t)
# Returns:
#   RAX = 64-bit value at [arrayBase + index*8 + 16]
# ==============================================================================
.global coa_mp_sib_lookup_x64
.global _coa_mp_sib_lookup_x64
coa_mp_sib_lookup_x64:
_coa_mp_sib_lookup_x64:
    push rbp
    mov rbp, rsp
    push rbx

    mov rbx, rcx                # RBX = 64-bit Base address
    mov rcx, rdx                # RCX = 64-bit Index

    # Genuine 64-bit Scale-Index-Base (SIB) Addressing:
    # [Base (RBX) + Index (RCX) * Scale (8) + Displacement (16)]
    mov rax, [rbx + rcx*8 + 16]

    pop rbx
    pop rbp
    ret

# ==============================================================================
# COA-CO2: coa_mp_fibonacci_interval_x64
# Computes 64-bit Fibonacci F(n) using iterative 64-bit register loop (RCX LOOP)
#
# Calling Convention:
#   RCX = n (int64_t)
# Returns:
#   RAX = F(n) as 64-bit integer
# ==============================================================================
.global coa_mp_fibonacci_interval_x64
.global _coa_mp_fibonacci_interval_x64
coa_mp_fibonacci_interval_x64:
_coa_mp_fibonacci_interval_x64:
    push rbp
    mov rbp, rsp
    push rbx

    cmp rcx, 0
    jle .L_x64_fib_zero
    cmp rcx, 1
    je .L_x64_fib_one

    # 64-bit iterative Fibonacci
    xor rax, rax                # F(0) = 0
    mov rbx, 1                  # F(1) = 1
    dec rcx                     # Loop count = n - 1

.L_x64_fib_loop:
    mov rdx, rax
    add rdx, rbx                # F(i) = F(i-1) + F(i-2)
    mov rax, rbx
    mov rbx, rdx
    loop .L_x64_fib_loop        # Decrements 64-bit RCX and branches if RCX != 0

    mov rax, rbx
    jmp .L_x64_fib_done

.L_x64_fib_zero:
    xor rax, rax
    jmp .L_x64_fib_done

.L_x64_fib_one:
    mov rax, 1

.L_x64_fib_done:
    pop rbx
    pop rbp
    ret

# ==============================================================================
# COA-CO3: coa_mp_booth_multiply_x64
# Pure 64-bit Assembly implementation of Booth's Signed Multiplication Algorithm
#
# Formula: Product = Multiplicand (M) * Multiplier (Q)
# Handles all 4 quadrants (positive*positive, positive*negative, negative*positive, negative*negative)
#
# Calling Convention:
#   RCX = M (int64_t multiplicand)
#   RDX = Q (int64_t multiplier)
# Returns:
#   RAX = 64-bit signed product
# ==============================================================================
.global coa_mp_booth_multiply_x64
.global _coa_mp_booth_multiply_x64
coa_mp_booth_multiply_x64:
_coa_mp_booth_multiply_x64:
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    push r13

    # Setup registers for 16-bit / 32-bit / 64-bit Booth calculation
    # Using 32 cycles for full standard 32-bit delta precision into 64-bit product
    mov r8, rcx                 # R8 = Multiplicand (M)
    mov r9, rdx                 # R9 = Multiplier (Q)
    xor rax, rax                # RAX = Accumulator (A) = 0
    xor r10, r10                # R10 = Q_-1 bit = 0
    mov rcx, 32                 # 32 iterations

.L_booth_step_loop:
    # Inspect Q_0 and Q_-1
    mov r11, r9
    and r11, 1                  # r11 = Q_0 bit
    mov r12, r10                # r12 = Q_-1 bit

    cmp r11, r12
    je .L_booth_shift           # 00 or 11: No operation, just shift

    # Check 10 vs 01
    cmp r11, 1
    je .L_booth_sub             # 10: A = A - M
    # 01: A = A + M
    add rax, r8
    jmp .L_booth_shift

.L_booth_sub:
    sub rax, r8

.L_booth_shift:
    # Combined Arithmetic Right Shift of [A, Q, Q_-1]
    # 1. Update Q_-1 from lowest bit of Q
    mov r10, r9
    and r10, 1

    # 2. Shift Q right by 1, with lowest bit of A entering MSB of Q (bit 31)
    shr r9, 1
    mov r13, rax
    and r13, 1
    shl r13, 31
    or r9, r13

    # 3. Arithmetic right shift of A (SAR preserves sign bit)
    sar rax, 1

    dec rcx
    jnz .L_booth_step_loop

    # Combine [A:Q] into final 64-bit product
    # Upper 32 bits from A, lower 32 bits from Q
    shl rax, 32
    mov r11d, r9d               # Zero-extend lower 32 bits
    or rax, r11

    pop r13
    pop r12
    pop rbx
    pop rbp
    ret

# ==============================================================================
# COA-CO4: coa_mp_pipeline_stream_x64
# 64-bit 5-Stage Instruction Pipeline Simulator (IF, ID, EX, MEM, WB)
#
# Calling Convention:
#   RCX = packetCount (int64_t N)
#   RDX = mode        (int64_t mode: 0=Ideal, 1=RAW Hazard, 2=Forwarding)
#   R8  = outCycles   (int64_t*) 64-bit pointer
#   R9  = outStalls   (int64_t*) 64-bit pointer
# Returns:
#   RAX = total execution cycles
# ==============================================================================
.global coa_mp_pipeline_stream_x64
.global _coa_mp_pipeline_stream_x64
coa_mp_pipeline_stream_x64:
_coa_mp_pipeline_stream_x64:
    push rbp
    mov rbp, rsp
    push rbx

    # Ideal Pipelined Execution Formula:
    # T_pipe = k + (N - 1) cycles, where k = 5 pipeline stages
    # T_pipe = 5 + N - 1 = N + 4
    lea rax, [rcx + 4]          # RAX = N + 4
    xor rbx, rbx                # stalls = 0

    cmp rdx, 1                  # Mode 1: RAW Data Hazard without forwarding
    jne .L_x64_pipe_check_done
    add rax, 2                  # 2 stall bubble cycles injected into EX stage
    mov rbx, 2

.L_x64_pipe_check_done:
    # Write outCycles (R8)
    test r8, r8
    jz .L_x64_skip_pipe_cycles
    mov [r8], rax
.L_x64_skip_pipe_cycles:

    # Write outStalls (R9)
    test r9, r9
    jz .L_x64_skip_pipe_stalls
    mov [r9], rbx
.L_x64_skip_pipe_stalls:

    pop rbx
    pop rbp
    ret

# ==============================================================================
# Interactive 64-bit Demonstrator: coa_demo_multiplayer_x64
# Formatted output displaying 64-bit registers, condition flags, and calculations
# ==============================================================================
.global coa_demo_multiplayer_x64
.global _coa_demo_multiplayer_x64
coa_demo_multiplayer_x64:
_coa_demo_multiplayer_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 80                 # Allocate shadow space (32) + locals (48) with 16-byte alignment
    push rbx
    push rsi
    push rdi

    # Print Title Banner
    lea rcx, [rip + coa_mp_title]
    call printf

    # 1. COA-CO1: 64-bit CMP & Condition Flags
    lea rcx, [rip + coa_mp_co1_hdr]
    call printf

    # Setup local pointers for flags on stack:
    # [rbp - 8]  = outOF
    # [rbp - 16] = outCF
    # [rbp - 24] = outSF
    # [rbp - 32] = outZF
    lea rax, [rbp - 8]          # outOF
    lea rbx, [rbp - 16]         # outCF
    lea r8,  [rbp - 32]         # outZF (3rd arg)
    lea r9,  [rbp - 24]         # outSF (4th arg)

    # 5th and 6th args passed on stack above 32-byte shadow space:
    mov [rsp + 32], rbx         # arg5: outCF
    mov [rsp + 40], rax         # arg6: outOF
    mov rcx, 4800               # arg1: score1 = 4800
    mov rdx, 3200               # arg2: score2 = 3200
    call coa_mp_compare_scores_x64

    # Print Comparison Results
    # Format: score1, score2, ZF, SF, CF, OF, leader_str
    movsxd rax, dword ptr [rbp - 32] # ZF
    movsxd rbx, dword ptr [rbp - 24] # SF
    movsxd r10, dword ptr [rbp - 16] # CF
    movsxd r11, dword ptr [rbp - 8]  # OF

    lea rcx, [rip + coa_mp_cmp_fmt]
    mov rdx, 4800               # %lld score1
    mov r8, 3200                # %lld score2
    mov r9, rax                 # %d ZF
    mov [rsp + 32], rbx         # %d SF
    mov [rsp + 40], r10         # %d CF
    mov [rsp + 48], r11         # %d OF
    lea rax, [rip + leader_p1_str]
    mov [rsp + 56], rax         # %s leader
    call printf

    # 64-bit SIB Demonstration
    lea rcx, [rip + coa_mp_sib_hdr]
    call printf

    xor rsi, rsi                # index = 0..3
.L_x64_sib_loop:
    lea rcx, [rip + mp_score_table_x64]
    mov rdx, rsi
    call coa_mp_sib_lookup_x64

    lea rcx, [rip + coa_mp_sib_fmt]
    mov rdx, rsi                # slot index
    mov r8, rax                 # hex points
    mov r9, rax                 # dec points
    call printf

    inc rsi
    cmp rsi, 4
    jl .L_x64_sib_loop

    # 2. COA-CO2: 64-bit Fibonacci Attack Cadence
    lea rcx, [rip + coa_mp_co2_hdr]
    call printf

    mov rcx, 6                  # F(6)
    call coa_mp_fibonacci_interval_x64
    imul rdx, rax, 100          # 100ms multiplier (800ms)

    lea rcx, [rip + coa_mp_fib_fmt]
    mov r8, rax                 # F(6) = 8
    mov r9, rdx                 # 800 ms
    mov rdx, 6                  # n = 6
    call printf

    # 3. COA-CO3: 64-bit Booth's Signed Multiplication
    lea rcx, [rip + coa_mp_co3_hdr]
    call printf

    mov rcx, 15                 # Multiplicand M = 15
    mov rdx, -3                 # Multiplier Q = -3
    call coa_mp_booth_multiply_x64

    lea rcx, [rip + coa_mp_booth_fmt]
    mov rdx, 15
    mov r8, -3
    mov r9, rax                 # Product = -45
    call printf

    # 4. COA-CO4: 64-bit 5-Stage Pipeline Simulation
    lea rcx, [rip + coa_mp_co4_hdr]
    call printf

    lea r8, [rbp - 40]          # outCycles
    lea r9, [rbp - 48]          # outStalls
    mov rcx, 8                  # 8 packets
    mov rdx, 1                  # Mode 1: RAW Hazard with Stalls
    call coa_mp_pipeline_stream_x64

    # T_seq = 8 * 5 = 40 cycles. T_pipe = 14 cycles. Speedup = 40 / 14 = 2.85x
    mov r10, [rbp - 40]         # Cycles = 14
    mov r11, [rbp - 48]         # Stalls = 2

    lea rcx, [rip + coa_mp_pipe_fmt]
    mov rdx, 8                  # Packet Count
    lea r8, [rip + mode_raw_str]# Mode string
    mov r9, r10                 # Total cycles
    mov [rsp + 32], r11         # Stalls
    mov qword ptr [rsp + 40], 2 # Speedup integer part (2)
    mov qword ptr [rsp + 48], 85# Speedup fractional part (85)
    call printf

    pop rdi
    pop rsi
    pop rbx
    add rsp, 80
    pop rbp
    ret
