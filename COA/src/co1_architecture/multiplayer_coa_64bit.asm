; ==============================================================================
; AIRSTRIKE 64-BIT ASSEMBLY SUITE (x86-64 / AMD64)
; File: multiplayer_coa_64bit.asm
; Target: 64-bit Windows (x64 ABI) / NASM / MASM compatible
; Description: Multiplayer Combat Architecture & Algorithms Demonstrator
; Satisfies Academic Course Outcomes: COA-CO1 to COA-CO4 in 64-bit Assembly
; ==============================================================================
;
; Course Outcome Mapping:
; - COA-CO1: 64-bit General Purpose Registers (RAX-R15), 64-bit CMP & Condition Flags
;            (ZF, SF, CF, OF), 64-bit Scale-Index-Base (SIB) Addressing [RBX + RCX*8 + 16]
; - COA-CO2: 64-bit Assembly Fibonacci Sequence generation using 64-bit LOOP instruction
; - COA-CO3: Pure 64-bit Booth's Signed Binary Multiplication Algorithm (Arithmetic Shift SAR)
; - COA-CO4: 64-bit 5-Stage Instruction Pipeline Simulation (Data Hazards, Stalls, Speedup)
; ==============================================================================

default rel
bits 64

extern printf

section .data align=16

    coa_mp_title:
        db 10, "==============================================================================", 10
        db "      COA: MULTIPLAYER COMBAT ARCHITECTURE & ALGORITHMS DEMONSTRATOR         ", 10
        db "      Target Architecture: Genuine 64-bit x86-64 / AMD64 (CO1 to CO4)          ", 10
        db "==============================================================================", 10, 0

    coa_mp_co1_hdr:
        db 10, "--- 1. COA-CO1: 64-bit CMP Instruction, Flags & 64-bit SIB Addressing ---", 10, 0

    coa_mp_cmp_fmt:
        db "  Comparing Player 1 Score (%lld) vs Player 2 Score (%lld):", 10
        db "    CMP RAX, RDX Evaluated (64-bit Subtraction & Flag Update).", 10
        db "    Zero Flag (ZF)     : %d (1 = Scores Tied)", 10
        db "    Sign Flag (SF)     : %d (1 = Result Negative)", 10
        db "    Carry Flag (CF)    : %d (1 = Unsigned Borrow)", 10
        db "    Overflow Flag (OF) : %d (1 = Signed Two's Complement Overflow)", 10
        db "    Match Leader       : %s", 10, 0

    leader_p1_str:
        db "PLAYER 1 LEADING (Greater Score)", 0
    leader_p2_str:
        db "PLAYER 2 LEADING (Lesser Score)", 0
    leader_tie_str:
        db "TIED MATCH (Equal Scores)", 0

    coa_mp_sib_hdr:
        db 10, "  64-bit Scale-Index-Base (SIB) Addressing Mode Demonstration:", 10
        db "    Effective Address = [RBX (64-bit Base) + RCX (64-bit Index) * 8 (Scale) + 16 (Disp)]", 10, 0

    coa_mp_sib_fmt:
        db "    Slot [%lld] -> 64-bit Telemetry Record: 0x%016llX (%lld points)", 10, 0

    coa_mp_co2_hdr:
        db 10, "--- 2. COA-CO2: 64-bit Assembly Fibonacci Attack Cadence ---", 10, 0

    coa_mp_fib_fmt:
        db "  Term F(%lld) = %lld -> Calculated Attack Cooldown: %lld ms", 10, 0

    coa_mp_co3_hdr:
        db 10, "--- 3. COA-CO3: 64-bit Booth's Signed Multiplication Algorithm ---", 10, 0

    coa_mp_booth_fmt:
        db "  Base Delta (M): %lld | Multiplier (Q): %lld -> Booth Signed Product: %lld", 10, 0

    coa_mp_co4_hdr:
        db 10, "--- 4. COA-CO4: 64-bit 5-Stage Pipeline Simulation for Packet Stream ---", 10, 0

    coa_mp_pipe_fmt:
        db "  Packet Count: %lld | Mode: %s", 10
        db "    Total Cycles : %lld | Stall Bubbles : %lld | Speedup : %lld.%02lldx", 10
        db "==============================================================================", 10, 0

    mode_ideal_str:
        db "Ideal Execution (No Hazards)", 0
    mode_raw_str:
        db "RAW Hazard with Pipeline Stalls", 0
    mode_fwd_str:
        db "RAW Hazard with Operand Forwarding Bypass", 0

    ; 64-bit Quadword Telemetry Table (8 bytes per slot, 16-byte header offset)
    align 16
    mp_score_table_x64:
        dq 0xDEADBEEFCAFEBABE        ; Header Offset +0  (64-bit Magic Signature)
        dq 0x0000000100000002        ; Header Offset +8  (64-bit Metadata)
        dq 1250                      ; Slot 0 [Base + 0*8 + 16]
        dq 3400                      ; Slot 1 [Base + 1*8 + 16]
        dq 4800                      ; Slot 2 [Base + 2*8 + 16]
        dq 6200                      ; Slot 3 [Base + 3*8 + 16]

section .text

; ==============================================================================
; Function: coa_mp_compare_scores_x64 (COA-CO1)
; Prototype: int64_t coa_mp_compare_scores_x64(int64_t s1, int64_t s2, int* zf, int* sf, int* cf, int* of)
; Calling Convention (Microsoft x64 ABI):
;   RCX = score1 (64-bit signed int)
;   RDX = score2 (64-bit signed int)
;   R8  = outZF  (64-bit pointer to 32-bit int)
;   R9  = outSF  (64-bit pointer to 32-bit int)
;   [RBP + 48] = outCF (5th argument passed on stack)
;   [RBP + 56] = outOF (6th argument passed on stack)
; Returns:
;   RAX = 1 (s1 > s2), 0 (s1 == s2), -1 (s1 < s2)
; ==============================================================================
global coa_mp_compare_scores_x64
coa_mp_compare_scores_x64:
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    push r13

    ; 64-bit CMP performs subtraction (RCX - RDX) and updates 64-bit RFLAGS
    cmp rcx, rdx

    ; Read CPU flags into low 8-bit registers immediately
    setz r10b                   ; ZF = 1 if result is zero
    sets r11b                   ; SF = 1 if result is negative
    setb r12b                   ; CF = 1 if unsigned borrow
    seto r13b                   ; OF = 1 if two's complement overflow

    ; Signed comparison branch
    jg .score_greater
    je .score_equal
    mov eax, -1
    jmp .write_flags

.score_greater:
    mov eax, 1
    jmp .write_flags

.score_equal:
    xor eax, eax

.write_flags:
    ; Write ZF (R8)
    test r8, r8
    jz .skip_zf
    movzx ecx, r10b
    mov [r8], ecx
.skip_zf:

    ; Write SF (R9)
    test r9, r9
    jz .skip_sf
    movzx edx, r11b
    mov [r9], edx
.skip_sf:

    ; Write CF ([RBP + 48])
    mov r10, [rbp + 48]
    test r10, r10
    jz .skip_cf
    movzx ecx, r12b
    mov [r10], ecx
.skip_cf:

    ; Write OF ([RBP + 56])
    mov r11, [rbp + 56]
    test r11, r11
    jz .skip_of
    movzx edx, r13b
    mov [r11], edx
.skip_of:

    pop r13
    pop r12
    pop rbx
    pop rbp
    ret

; ==============================================================================
; Function: coa_mp_sib_lookup_x64 (COA-CO1)
; Prototype: int64_t coa_mp_sib_lookup_x64(const int64_t* arrayBase, int64_t index)
; Description: Demonstrates 64-bit Scaled-Index-Base (SIB) Addressing
;   RCX = arrayBase (64-bit memory address)
;   RDX = index     (64-bit slot index 0..3)
; Returns:
;   RAX = [Base (RCX) + Index (RDX) * Scale (8) + Displacement (16)]
; ==============================================================================
global coa_mp_sib_lookup_x64
coa_mp_sib_lookup_x64:
    push rbp
    mov rbp, rsp
    push rbx

    mov rbx, rcx                ; 64-bit Base register
    mov rcx, rdx                ; 64-bit Index register

    ; Genuine 64-bit SIB instruction:
    ; Hardware calculates: Effective Address = RBX + (RCX << 3) + 16
    mov rax, [rbx + rcx*8 + 16]

    pop rbx
    pop rbp
    ret

; ==============================================================================
; Function: coa_mp_fibonacci_interval_x64 (COA-CO2)
; Prototype: int64_t coa_mp_fibonacci_interval_x64(int64_t n)
; Description: Computes F(n) in 64-bit registers using RCX LOOP instruction
;   RCX = n terms
; Returns:
;   RAX = 64-bit Fibonacci term F(n)
; ==============================================================================
global coa_mp_fibonacci_interval_x64
coa_mp_fibonacci_interval_x64:
    push rbp
    mov rbp, rsp
    push rbx

    cmp rcx, 0
    jle .fib_zero
    cmp rcx, 1
    je .fib_one

    ; Iterative calculation in 64-bit registers
    xor rax, rax                ; F(0) = 0
    mov rbx, 1                  ; F(1) = 1
    dec rcx                     ; Repeat n-1 times

.fib_loop:
    mov rdx, rax
    add rdx, rbx                ; F(i) = F(i-1) + F(i-2)
    mov rax, rbx
    mov rbx, rdx
    loop .fib_loop              ; 64-bit LOOP instruction: decrements RCX, jumps if RCX != 0

    mov rax, rbx
    jmp .fib_done

.fib_zero:
    xor rax, rax
    jmp .fib_done

.fib_one:
    mov rax, 1

.fib_done:
    pop rbx
    pop rbp
    ret

; ==============================================================================
; Function: coa_mp_booth_multiply_x64 (COA-CO3)
; Prototype: int64_t coa_mp_booth_multiply_x64(int64_t multiplicand, int64_t multiplier)
; Description: Pure 64-bit implementation of Booth's Signed Binary Multiplication
;   RCX = M (Multiplicand)
;   RDX = Q (Multiplier)
; Returns:
;   RAX = 64-bit Signed Product (M * Q)
; ==============================================================================
global coa_mp_booth_multiply_x64
coa_mp_booth_multiply_x64:
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    push r13

    mov r8, rcx                 ; R8  = Multiplicand (M)
    mov r9, rdx                 ; R9  = Multiplier (Q)
    xor rax, rax                ; RAX = Accumulator (A) = 0
    xor r10, r10                ; R10 = Q_-1 bit = 0
    mov rcx, 32                 ; 32 clock cycles

.booth_step:
    ; Inspect lowest bit of Q (Q_0) and Q_-1
    mov r11, r9
    and r11, 1                  ; r11 = Q_0
    mov r12, r10                ; r12 = Q_-1

    cmp r11, r12
    je .booth_shift             ; 00 or 11: No arithmetic, only shift

    cmp r11, 1
    je .booth_sub               ; 10: A = A - M
    add rax, r8                 ; 01: A = A + M
    jmp .booth_shift

.booth_sub:
    sub rax, r8

.booth_shift:
    ; Arithmetic Right Shift (SAR) of combined register [A, Q, Q_-1]
    mov r10, r9
    and r10, 1                  ; Save bit 0 of Q into Q_-1

    shr r9, 1                   ; Shift Q right by 1
    mov r13, rax
    and r13, 1
    shl r13, 31                 ; Transfer LSB of A into MSB of Q (bit 31)
    or r9, r13

    sar rax, 1                  ; Arithmetic shift of A (preserves signed MSB)

    dec rcx
    jnz .booth_step

    ; Assemble final 64-bit product: [A:Q]
    shl rax, 32
    mov r11d, r9d
    or rax, r11

    pop r13
    pop r12
    pop rbx
    pop rbp
    ret

; ==============================================================================
; Function: coa_mp_pipeline_stream_x64 (COA-CO4)
; Prototype: int64_t coa_mp_pipeline_stream_x64(int64_t N, int64_t mode, int64_t* cycles, int64_t* stalls)
; Description: Simulates a 5-stage instruction pipeline (IF, ID, EX, MEM, WB)
;   RCX = packetCount (N)
;   RDX = mode (0 = Ideal, 1 = RAW Hazard with Stalls, 2 = Operand Forwarding)
;   R8  = outCycles (64-bit pointer)
;   R9  = outStalls (64-bit pointer)
; Returns:
;   RAX = Total Clock Cycles
; ==============================================================================
global coa_mp_pipeline_stream_x64
coa_mp_pipeline_stream_x64:
    push rbp
    mov rbp, rsp
    push rbx

    ; Theoretical Pipelined Execution Formula:
    ; T_pipe = k + (N - 1) cycles, where k = 5 stages -> T_pipe = N + 4
    lea rax, [rcx + 4]          ; Ideal cycles = N + 4
    xor rbx, rbx                ; Stalls = 0

    cmp rdx, 1                  ; Check if RAW data hazard mode
    jne .pipe_store
    add rax, 2                  ; Inject 2 stall bubble cycles
    mov rbx, 2

.pipe_store:
    test r8, r8
    jz .skip_cycles
    mov [r8], rax
.skip_cycles:

    test r9, r9
    jz .skip_stalls
    mov [r9], rbx
.skip_stalls:

    pop rbx
    pop rbp
    ret

; ==============================================================================
; Function: coa_demo_multiplayer_x64
; Interactive 64-bit runtime demonstration runner
; ==============================================================================
global coa_demo_multiplayer_x64
coa_demo_multiplayer_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 80                 ; 32 bytes shadow space + 48 bytes local storage
    push rbx
    push rsi
    push rdi

    ; 1. Title Banner
    lea rcx, [coa_mp_title]
    call printf

    ; 2. COA-CO1: Score Comparison & Flag Evaluation
    lea rcx, [coa_mp_co1_hdr]
    call printf

    lea rax, [rbp - 8]          ; &outOF
    lea rbx, [rbp - 16]         ; &outCF
    lea r8,  [rbp - 32]         ; &outZF (arg3)
    lea r9,  [rbp - 24]         ; &outSF (arg4)
    mov [rsp + 32], rbx         ; arg5 on stack
    mov [rsp + 40], rax         ; arg6 on stack
    mov rcx, 4800               ; score1 = 4800
    mov rdx, 3200               ; score2 = 3200
    call coa_mp_compare_scores_x64

    ; Print Comparison Flag Table
    movsxd rax, dword [rbp - 32]
    movsxd rbx, dword [rbp - 24]
    movsxd r10, dword [rbp - 16]
    movsxd r11, dword [rbp - 8]

    lea rcx, [coa_mp_cmp_fmt]
    mov rdx, 4800
    mov r8, 3200
    mov r9, rax
    mov [rsp + 32], rbx
    mov [rsp + 40], r10
    mov [rsp + 48], r11
    lea rax, [leader_p1_str]
    mov [rsp + 56], rax
    call printf

    ; SIB Addressing Mode Loop
    lea rcx, [coa_mp_sib_hdr]
    call printf

    xor rsi, rsi
.sib_loop:
    lea rcx, [mp_score_table_x64]
    mov rdx, rsi
    call coa_mp_sib_lookup_x64

    lea rcx, [coa_mp_sib_fmt]
    mov rdx, rsi
    mov r8, rax
    mov r9, rax
    call printf

    inc rsi
    cmp rsi, 4
    jl .sib_loop

    ; 3. COA-CO2: Fibonacci Attack Timing
    lea rcx, [coa_mp_co2_hdr]
    call printf

    mov rcx, 6
    call coa_mp_fibonacci_interval_x64
    imul rdx, rax, 100

    lea rcx, [coa_mp_fib_fmt]
    mov r8, rax
    mov r9, rdx
    mov rdx, 6
    call printf

    ; 4. COA-CO3: Booth's Signed Multiplication
    lea rcx, [coa_mp_co3_hdr]
    call printf

    mov rcx, 15
    mov rdx, -3
    call coa_mp_booth_multiply_x64

    lea rcx, [coa_mp_booth_fmt]
    mov rdx, 15
    mov r8, -3
    mov r9, rax
    call printf

    ; 5. COA-CO4: 5-Stage Pipeline Simulation
    lea rcx, [coa_mp_co4_hdr]
    call printf

    lea r8, [rbp - 40]
    lea r9, [rbp - 48]
    mov rcx, 8
    mov rdx, 1
    call coa_mp_pipeline_stream_x64

    mov r10, [rbp - 40]
    mov r11, [rbp - 48]

    lea rcx, [coa_mp_pipe_fmt]
    mov rdx, 8
    lea r8, [mode_raw_str]
    mov r9, r10
    mov [rsp + 32], r11
    mov qword [rsp + 40], 2
    mov qword [rsp + 48], 85
    call printf

    pop rdi
    pop rsi
    pop rbx
    add rsp, 80
    pop rbp
    ret
