# ==============================================================================
# File    : COA/src/co1_architecture/multiplayer_coa.s
# Subject : COA-CO1..CO4 | Multiplayer Combat Architecture Demonstrator
# Topics  : CMP Score Comparison, RFLAGS (ZF/SF/CF/OF), SIB Telemetry Lookup,
#           Fibonacci Attack Cadence, Booth Signed Score Delta, 5-Stage Pipeline
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
coa_mp_title:
    .asciz "\n==============================================================================\n      COA: MULTIPLAYER COMBAT ARCHITECTURE & ALGORITHMS DEMONSTRATOR         \n==============================================================================\n"

coa_mp_co1_hdr:  .asciz "\n--- 1. COA-CO1: CMP Score Comparison, RFLAGS & SIB Telemetry ---\n"
coa_mp_cmp_fmt:  .asciz "  Player 1 (%lld) vs Player 2 (%lld): ZF=%d SF=%d CF=%d OF=%d -> %s\n"
leader_p1_str:   .asciz "PLAYER 1 LEADING"
leader_p2_str:   .asciz "PLAYER 2 LEADING"
leader_tie_str:  .asciz "TIED MATCH"

coa_mp_sib_hdr:  .asciz "  SIB Telemetry Read [RBX + RCX*8 + 16]:\n"
coa_mp_sib_fmt:  .asciz "    Slot [%lld] = 0x%016llX (%lld pts)\n"
coa_mp_co2_hdr:  .asciz "\n--- 2. COA-CO2: Fibonacci Attack Cooldown ---\n"
coa_mp_fib_fmt:  .asciz "  F(%lld) = %lld -> Cooldown = %lld ms\n"
coa_mp_co3_hdr:  .asciz "\n--- 3. COA-CO3: Booth's Signed Score Delta ---\n"
coa_mp_booth_fmt:.asciz "  Base (%lld) * Multiplier (%lld) = %lld\n"
coa_mp_co4_hdr:  .asciz "\n--- 4. COA-CO4: 5-Stage Pipeline Packet Stream ---\n"
coa_mp_pipe_fmt: .asciz "  Packets=%lld (%s) -> Cycles=%lld | Stalls=%lld | Speedup=%lld.%02lldx\n==============================================================================\n"
mode_raw_str:    .asciz "RAW Hazard with Stalls"

.align 8
mp_score_table_x64:
    .quad 0xDEADBEEFCAFEBABE    # Header +0
    .quad 0x0000000100000002    # Header +8
    .quad 1250                  # Slot 0 (+16)
    .quad 3400                  # Slot 1 (+24)
    .quad 4800                  # Slot 2 (+32)
    .quad 6200                  # Slot 3 (+40)

.text

# ------------------------------------------------------------------------------
# [TOPIC: CMP & RFLAGS Score Comparison] coa_mp_compare_scores_x64
# Purpose   : Compares two scores using CMP and captures ZF, SF, CF, OF flags.
# Game Role : Determines the live 1v1 Score Attack leader (P1, Tie, or P2) in HUD.
# Inputs    : RCX=score1, RDX=score2, R8=&zf, R9=&sf, [rbp+48]=&cf, [rbp+56]=&of
# Output    : RAX = 1 (P1 > P2), 0 (Tie), -1 (P1 < P2)
# ------------------------------------------------------------------------------
.global coa_mp_compare_scores_x64
.global _coa_mp_compare_scores_x64
coa_mp_compare_scores_x64:
_coa_mp_compare_scores_x64:
    push rbp
    mov  rbp, rsp
    push r12
    push r13

    cmp  rcx, rdx               # WHY: Subtracts score2 from score1 purely to update RFLAGS without altering RCX
    setz r10b                   # WHY: ZF=1 -> Scores are tied
    sets r11b                   # WHY: SF=1 -> Difference is negative
    setb r12b                   # WHY: CF=1 -> Unsigned borrow (score1 < score2 unsigned)
    seto r13b                   # WHY: OF=1 -> Signed comparison overflowed

    jg   .L_mp_p1_wins          # WHY: Signed Jump-if-Greater checks ZF=0 and SF==OF -> P1 leads
    je   .L_mp_tie              # WHY: Jump-if-Equal checks ZF=1 -> Match tied
    mov  rax, -1
    jmp  .L_mp_store_flags
.L_mp_p1_wins:
    mov  rax, 1
    jmp  .L_mp_store_flags
.L_mp_tie:
    xor  rax, rax               # WHY: Faster/smaller than `mov rax, 0` for returning tie status

.L_mp_store_flags:
    test r8, r8
    jz   .L_mp_no_zf
    movzx ecx, r10b
    mov  [r8], ecx
.L_mp_no_zf:
    test r9, r9
    jz   .L_mp_no_sf
    movzx edx, r11b
    mov  [r9], edx
.L_mp_no_sf:
    mov  r10, [rbp + 48]        # WHY: 5th argument (&cf)
    test r10, r10
    jz   .L_mp_no_cf
    movzx ecx, r12b
    mov  [r10], ecx
.L_mp_no_cf:
    mov  r11, [rbp + 56]        # WHY: 6th argument (&of)
    test r11, r11
    jz   .L_mp_no_of
    movzx edx, r13b
    mov  [r11], edx
.L_mp_no_of:
    pop  r13
    pop  r12
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: SIB Telemetry Lookup] coa_mp_sib_lookup_x64
# Purpose   : Reads multiplayer score slot using [Base + Index*8 + 16].
# Game Role : Fetches live player scoreboard records past a 16-byte packet header.
# Inputs    : RCX = basePtr, RDX = slotIndex
# Output    : RAX = score value
# ------------------------------------------------------------------------------
.global coa_mp_sib_lookup_x64
.global _coa_mp_sib_lookup_x64
coa_mp_sib_lookup_x64:
_coa_mp_sib_lookup_x64:
    mov  rax, [rcx + rdx*8 + 16] # WHY: Hardware SIB skips 16-byte packet magic/metadata and indexes 8-byte score slots
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Fibonacci Attack Cadence] coa_mp_fibonacci_interval_x64
# Purpose   : Computes F(n) iteratively using the hardware LOOP instruction.
# Game Role : Synchronizes dynamic enemy attack cooldowns across 1v1 match clients.
# Input     : RCX = n
# Output    : RAX = F(n)
# ------------------------------------------------------------------------------
.global coa_mp_fibonacci_interval_x64
.global _coa_mp_fibonacci_interval_x64
coa_mp_fibonacci_interval_x64:
_coa_mp_fibonacci_interval_x64:
    push rbx

    cmp  rcx, 0
    jle  .L_mp_fib_0
    cmp  rcx, 1
    je   .L_mp_fib_1

    xor  rax, rax               # F(0) = 0
    mov  rbx, 1                 # F(1) = 1
    dec  rcx                    # WHY: Already handled F(1), so loop n-1 times

.L_mp_fib_loop:
    lea  rdx, [rax + rbx]       # WHY: LEA adds F(i-2) + F(i-1) in 1 cycle without touching RFLAGS
    mov  rax, rbx
    mov  rbx, rdx
    loop .L_mp_fib_loop         # WHY: Decrements RCX and jumps until RCX == 0 (classic COA loop instruction)

    mov  rax, rbx
    jmp  .L_mp_fib_done
.L_mp_fib_0:
    xor  rax, rax
    jmp  .L_mp_fib_done
.L_mp_fib_1:
    mov  rax, 1
.L_mp_fib_done:
    pop  rbx
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Booth's Signed Multiplication (32->64 bit)] coa_mp_booth_multiply_x64
# Purpose   : Multiplies signed score delta M * Q using 32-cycle Booth's loop.
# Game Role : Calculates deterministic 1v1 combo bonuses and negative penalties.
# Inputs    : RCX = M, RDX = Q
# Output    : RAX = 64-bit signed product
# ------------------------------------------------------------------------------
.global coa_mp_booth_multiply_x64
.global _coa_mp_booth_multiply_x64
coa_mp_booth_multiply_x64:
_coa_mp_booth_multiply_x64:
    push rbx
    push r12
    push r13

    mov  r8d, ecx
    movsxd r8, r8d              # WHY: Sign-extend 32-bit M -> 64-bit so negative multiplicands work uniformly
    mov  r9d, edx               # WHY: Hold 32-bit Q in R9D (lower half of [A:Q])
    xor  rax, rax               # WHY: Accumulator A = 0
    xor  r10, r10               # WHY: Q_-1 = 0
    mov  rcx, 32                # WHY: 32 cycles for full 32-bit signed multiplication

.L_mp_booth_loop:
    mov  r11, r9
    and  r11, 1                 # WHY: Isolate Q0 (LSB of multiplier)
    mov  r12, r10               # WHY: Load Q_-1
    cmp  r11, r12
    je   .L_mp_booth_shift      # WHY: 00 or 11 -> no add/sub, just shift
    cmp  r11, 1
    je   .L_mp_booth_sub        # WHY: Q0=1, Q_-1=0 (pair 10) -> A = A - M
    add  rax, r8                # WHY: Q0=0, Q_-1=1 (pair 01) -> A = A + M
    jmp  .L_mp_booth_shift
.L_mp_booth_sub:
    sub  rax, r8

.L_mp_booth_shift:
    mov  r10, r9
    and  r10, 1                 # WHY: Save current Q0 into Q_-1 before shifting Q

    shr  r9d, 1                 # WHY: Shift Q right 1 bit and feed LSB of A into bit 31 of Q
    mov  r13, rax
    and  r13, 1
    shl  r13, 31
    or   r9d, r13d

    sar  rax, 1                 # WHY: Arithmetic Right Shift (SAR) keeps A's sign bit intact
    dec  rcx
    jnz  .L_mp_booth_loop

    shl  rax, 32                # WHY: Pack upper 32 bits (A) and lower 32 bits (Q) into 64-bit signed RAX
    mov  r11d, r9d
    or   rax, r11

    pop  r13
    pop  r12
    pop  rbx
    ret

# ------------------------------------------------------------------------------
# [TOPIC: 5-Stage Pipeline Packet Stream] coa_mp_pipeline_stream_x64
# Purpose   : Computes latency and stall count for N packets across 5 stages.
# Game Role : Models 10 Hz multiplayer network telemetry packet throughput & lag.
# Inputs    : RCX=packets, RDX=mode (0=Ideal,1=RAW Stall,2=Forwarding), R8=&cyc, R9=&stl
# Output    : RAX = total cycles
# ------------------------------------------------------------------------------
.global coa_mp_pipeline_stream_x64
.global _coa_mp_pipeline_stream_x64
coa_mp_pipeline_stream_x64:
_coa_mp_pipeline_stream_x64:
    push rbx

    lea  rax, [rcx + 4]         # WHY: 5-stage pipeline takes (k + N - 1) = (N + 4) ideal cycles
    xor  rbx, rbx
    cmp  rdx, 1                 # WHY: Mode 1 simulates a RAW dependency without forwarding (+2 stall bubbles)
    jne  .L_mp_pipe_done
    add  rax, 2
    mov  rbx, 2

.L_mp_pipe_done:
    test r8, r8
    jz   .L_mp_no_cyc
    mov  [r8], rax
.L_mp_no_cyc:
    test r9, r9
    jz   .L_mp_no_stl
    mov  [r9], rbx
.L_mp_no_stl:
    pop  rbx
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Multiplayer COA Demo] coa_demo_multiplayer
# Purpose   : Runs all 4 CO demonstrations in a multiplayer combat scenario.
# Game Role : Shows how CO1..CO4 combine to power 1v1 live match architecture.
# ------------------------------------------------------------------------------
.global coa_demo_multiplayer
.global _coa_demo_multiplayer
.global coa_demo_multiplayer_x64
.global _coa_demo_multiplayer_x64
coa_demo_multiplayer:
_coa_demo_multiplayer:
coa_demo_multiplayer_x64:
_coa_demo_multiplayer_x64:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 96

    lea  rcx, [rip + coa_mp_title]
    call printf

    # 1. CO1: Compare scores & inspect SIB telemetry table
    lea  rcx, [rip + coa_mp_co1_hdr]
    call printf

    lea  r8,  [rsp + 64]
    lea  r9,  [rsp + 68]
    lea  rax, [rsp + 72]
    lea  rbx, [rsp + 76]
    mov  [rsp + 32], rax
    mov  [rsp + 40], rbx
    mov  rcx, 4800
    mov  rdx, 3200
    call coa_mp_compare_scores_x64

    lea  rcx, [rip + coa_mp_cmp_fmt]
    mov  rdx, 4800
    mov  r8,  3200
    mov  r9d, [rsp + 64]
    mov  eax, [rsp + 68]
    mov  [rsp + 32], eax
    mov  eax, [rsp + 72]
    mov  [rsp + 40], eax
    mov  eax, [rsp + 76]
    mov  [rsp + 48], eax
    lea  rax, [rip + leader_p1_str]
    mov  [rsp + 56], rax
    call printf

    lea  rcx, [rip + coa_mp_sib_hdr]
    call printf

    xor  rsi, rsi
.L_mp_sib_loop:
    lea  rcx, [rip + mp_score_table_x64]
    mov  rdx, rsi
    call coa_mp_sib_lookup_x64

    lea  rcx, [rip + coa_mp_sib_fmt]
    mov  rdx, rsi
    mov  r8,  rax
    mov  r9,  rax
    call printf

    inc  rsi
    cmp  rsi, 4
    jl   .L_mp_sib_loop

    # 2. CO2: Fibonacci Attack Cooldown
    lea  rcx, [rip + coa_mp_co2_hdr]
    call printf

    mov  rcx, 6
    call coa_mp_fibonacci_interval_x64
    imul r9, rax, 100           # WHY: Scale Fibonacci term F(6)=8 by 100ms base -> 800ms attack cooldown
    lea  rcx, [rip + coa_mp_fib_fmt]
    mov  rdx, 6
    mov  r8,  rax
    call printf

    # 3. CO3: Booth Score Delta
    lea  rcx, [rip + coa_mp_co3_hdr]
    call printf

    mov  rcx, 15
    mov  rdx, -3
    call coa_mp_booth_multiply_x64
    lea  rcx, [rip + coa_mp_booth_fmt]
    mov  rdx, 15
    mov  r8,  -3
    mov  r9,  rax
    call printf

    # 4. CO4: 5-Stage Pipeline Packet Stream
    lea  rcx, [rip + coa_mp_co4_hdr]
    call printf

    lea  r8,  [rsp + 80]
    lea  r9,  [rsp + 88]
    mov  rcx, 8
    mov  rdx, 1
    call coa_mp_pipeline_stream_x64

    lea  rcx, [rip + coa_mp_pipe_fmt]
    mov  rdx, 8
    lea  r8,  [rip + mode_raw_str]
    mov  r9,  [rsp + 80]
    mov  rax, [rsp + 88]
    mov  [rsp + 32], rax
    mov  qword ptr [rsp + 40], 2
    mov  qword ptr [rsp + 48], 85
    call printf

    add  rsp, 96
    pop  rsi
    pop  rbx
    pop  rbp
    ret
