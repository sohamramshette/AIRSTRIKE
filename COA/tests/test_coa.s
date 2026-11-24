# ==============================================================================
# File    : COA/tests/test_coa.s
# Subject : COA Automated Verification Test Suite (65 Assertions)
# Topics  : Unit Tests for COA-CO1, COA-CO2, COA-CO3, COA-CO4, Enemies, Multiplayer
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
test_banner:
    .asciz "\n==============================================================================\n      AIRSTRIKE x86-64 ASSEMBLY (COA) - AUTOMATED VERIFICATION TEST SUITE     \n      Course Outcomes: CO1, CO2, CO3, CO4 (65 Automated Unit Assertions)        \n==============================================================================\n"

sec_co1_test:     .asciz "\n[SUITE 1/6] CO1: Architecture, Registers, Flags & SIB Verification...\n"
sec_co2_test:     .asciz "\n[SUITE 2/6] CO2: Assembly Language Programs Verification...\n"
sec_co3_test:     .asciz "\n[SUITE 3/6] CO3: Computer Arithmetic (Booth & Restoring Div) Verification...\n"
sec_co4_test:     .asciz "\n[SUITE 4/6] CO4: Control Unit & 5-Stage Pipeline Verification...\n"
sec_co5_test:     .asciz "\n[SUITE 5/6] COA Academic Enemy Modules Verification...\n"
sec_co6_test:     .asciz "\n[SUITE 6/6] COA Multiplayer Combat Architecture Verification...\n"

pass_fmt:         .asciz "  [PASS] %s\n"
fail_fmt:         .asciz "  [FAIL] %s\n"

summary_fmt:
    .asciz "\n==============================================================================\n                         FINAL COA TEST RESULTS SUMMARY                       \n==============================================================================\n  Total Assertions Run : %lld\n  Passed Assertions    : %lld [OK]\n  Failed Assertions    : %lld\n  Target Architecture  : x86-64 (64-bit Windows ABI)\n==============================================================================\n"
all_pass_msg:     .asciz "[SUCCESS] ALL 65 COA ASSEMBLY VERIFICATION TESTS PASSED!\n\n"
some_fail_msg:    .asciz "[FAILURE] ONE OR MORE COA TESTS FAILED.\n\n"

# Test descriptions
t_co1_gpr:        .asciz "CO1: GPR Sub-register Partitioning (RAX -> EAX -> AX -> AH/AL)"
t_co1_cf:         .asciz "CO1: RFLAGS Carry Flag (CF) on 0xFFFFFFFFFFFFFFFF + 1"
t_co1_zf:         .asciz "CO1: RFLAGS Zero Flag (ZF) on 100 - 100"
t_co1_sf:         .asciz "CO1: RFLAGS Sign Flag (SF) on 20 - 50"
t_co1_of:         .asciz "CO1: RFLAGS Overflow Flag (OF) on 0x7FFFFFFFFFFFFFFF + 1"
t_co1_sib:        .asciz "CO1: SIB Addressing Mode [RBX + RSI*8 + 16]"

t_co2_add:        .asciz "CO2: Addition (45 + 55 = 100)"
t_co2_sub:        .asciz "CO2: Subtraction (100 - 35 = 65)"
t_co2_mul:        .asciz "CO2: Signed Multiplication (-12 * 8 = -96)"
t_co2_div:        .asciz "CO2: Signed Division with Remainder (47 / 5 = 9 rem 2)"
t_co2_div_zero:   .asciz "CO2: Division by Zero Guard (50 / 0 = 0 rem 0)"
t_co2_minmax:     .asciz "CO2: Array Min & Max Extraction (-34, 102)"
t_co2_sum:        .asciz "CO2: Array Sum Accumulation (207)"
t_co2_fact0:      .asciz "CO2: Factorial Base Case (0! = 1)"
t_co2_fact5:      .asciz "CO2: Recursive Factorial (5! = 120)"
t_co2_fib:        .asciz "CO2: Fibonacci Series F(0..6) = [0,1,1,2,3,5,8]"
t_co2_search_hit: .asciz "CO2: Linear Search Target Found (88 -> index 2)"
t_co2_search_miss:.asciz "CO2: Linear Search Target Absent (9999 -> -1)"
t_co2_reverse:    .asciz "CO2: In-Place Array Reversal"
t_co2_counts:     .asciz "CO2: Array Pos/Neg/Zero Counts (4, 3, 1)"
t_co2_bitwise:    .asciz "CO2: Bitwise Logic AND/OR/XOR/NOT"

t_co3_b_pp:       .asciz "CO3: Booth (+ * +): 7 * 5 = 35"
t_co3_b_pn:       .asciz "CO3: Booth (+ * -): 9 * -4 = -36"
t_co3_b_np:       .asciz "CO3: Booth (- * +): -8 * 6 = -48"
t_co3_b_nn:       .asciz "CO3: Booth (- * -): -11 * -3 = 33"
t_co3_b_zm:       .asciz "CO3: Booth Zero Multiplicand (0 * 45 = 0)"
t_co3_b_zq:       .asciz "CO3: Booth Zero Multiplier (-25 * 0 = 0)"
t_co3_b_bnd:      .asciz "CO3: Booth Boundary (-64 * 2 = -128)"
t_co3_r_exact:    .asciz "CO3: Restoring Div Exact (24 / 6 = 4 rem 0)"
t_co3_r_ne1:      .asciz "CO3: Restoring Div (29 / 5 = 5 rem 4)"
t_co3_r_ne2:      .asciz "CO3: Restoring Div (100 / 7 = 14 rem 2)"
t_co3_r_small:    .asciz "CO3: Restoring Div Q < M (3 / 10 = 0 rem 3)"
t_co3_r_zdiv:     .asciz "CO3: Restoring Div Zero Dividend (0 / 15 = 0 rem 0)"
t_co3_r_d0:       .asciz "CO3: Restoring Div Zero Guard (45 / 0 = 0 rem 0)"
t_co3_r_maxq:     .asciz "CO3: Restoring Div Boundary (255 / 1 = 255 rem 0)"
t_co3_r_maxm:     .asciz "CO3: Restoring Div Boundary (255 / 255 = 1 rem 0)"
t_co3_adc:        .asciz "CO3: 128-Bit Addition with Carry (ADC)"
t_co3_ov_pos:     .asciz "CO3: Signed Overflow Pos + Pos = Neg (OF=1)"
t_co3_ov_neg:     .asciz "CO3: Signed Overflow Neg + Neg = Pos (OF=1)"

t_co4_cu:         .asciz "CO4: Control Unit Signal Matrix Decode (R-Type/LW/SW/BEQ/JMP)"
t_co4_pipe:       .asciz "CO4: 5-Stage Pipeline Speedup & Hazard Latency (N=100)"

t_en_aegis_part:  .asciz "Enemy-CO1: Aegis Sentry Sub-Register Partitioning"
t_en_aegis_al:    .asciz "Enemy-CO1: Aegis Sentry AL Damage Preserves AH"
t_en_aegis_ah:    .asciz "Enemy-CO1: Aegis Sentry AH Recharge Preserves AL"
t_en_aegis_zf:    .asciz "Enemy-CO1: Aegis Sentry Zero Flag (ZF=1 on 50-50)"
t_en_aegis_sfcf:  .asciz "Enemy-CO1: Aegis Sentry Sign & Carry Flags (SF=1, CF=1 on 20-50)"
t_en_aegis_sib:   .asciz "Enemy-CO1: Aegis Sentry SIB Layer Read [RBX + RCX*8 + 16]"
t_en_skim_traj:   .asciz "Enemy-CO2: Fibonacci Skimmer Trajectory Weave"
t_en_skim_fire:   .asciz "Enemy-CO2: Fibonacci Skimmer Salvo Interval (F(5)*100 = 500ms)"
t_en_b_pp:        .asciz "Enemy-CO3: Booth Gunship (+ * +): 7 * 5 = 35"
t_en_b_pn:        .asciz "Enemy-CO3: Booth Gunship EMP Drain (+ * -): 9 * -4 = -36"
t_en_b_nn:        .asciz "Enemy-CO3: Booth Gunship (- * -): -11 * -3 = 33"
t_en_b_zero:      .asciz "Enemy-CO3: Booth Gunship Zero Dud (0 * 45 = 0)"
t_en_b_sat:       .asciz "Enemy-CO3: Booth Gunship Saturation (-64 * 2 = -128)"
t_en_rg_ideal:    .asciz "Enemy-CO4: Railgun Dreadnought Ideal Pipeline (8 cycles, 0 stalls)"
t_en_rg_raw:      .asciz "Enemy-CO4: Railgun Dreadnought RAW Stall (10 cycles, 2 stalls)"
t_en_rg_fwd:      .asciz "Enemy-CO4: Railgun Dreadnought Forwarding (8 cycles, 0 stalls)"

t_mp_cmp:         .asciz "MP-CO1: CMP Score Compare (4800 > 3200 -> Leader=1, Flags=0)"
t_mp_eq:          .asciz "MP-CO1: CMP Tie Score (2500 == 2500 -> Leader=0, ZF=1)"
t_mp_sib:         .asciz "MP-CO1: SIB Telemetry Lookup [RBX + RCX*8 + 16]"
t_mp_fib:         .asciz "MP-CO2: Fibonacci Attack Interval F(6) = 8"
t_mp_b_neg:       .asciz "MP-CO3: Booth Score Delta (15 * -3 = -45)"
t_mp_b_pos:       .asciz "MP-CO3: Booth Dual Negative Bonus (-20 * -4 = 80)"
t_mp_pipe_raw:    .asciz "MP-CO4: Packet Stream RAW Stall (8 pkts -> 14 cyc, 2 stalls)"
t_mp_pipe_fwd:    .asciz "MP-CO4: Packet Stream Forwarding (8 pkts -> 12 cyc, 0 stalls)"

.align 8
test_array_coa:   .quad 45, -12, 88, 0, -34, 102, -5, 23
test_array_size:  .quad 8

.align 8
test_sib_layers:
    .quad 0x1111111122222222    # Header +0
    .quad 0x3333333344444444    # Header +8
    .quad 0xAAAA000000000001    # Layer 0 (+16)
    .quad 0xBBBB000000000002    # Layer 1 (+24)
    .quad 0xCCCC000000000003    # Layer 2 (+32)
    .quad 0xDDDD000000000004    # Layer 3 (+40)

.bss
.align 8
.lcomm total_cnt,    8
.lcomm pass_cnt,     8
.lcomm fail_cnt,     8
.lcomm test_min,     8
.lcomm test_max,     8
.lcomm test_pos,     8
.lcomm test_neg,     8
.lcomm test_zero,    8
.lcomm test_fib_buf, 64
.lcomm test_rem,     8
.lcomm test_lo,      8
.lcomm test_hi,      8
.lcomm test_cf,      8
.lcomm test_en_buf,  64
.lcomm test_en_dmg,  8
.lcomm test_en_flg,  8
.lcomm test_en_cyc,  8
.lcomm test_en_stl,  8
.lcomm test_pipe_seq,8
.lcomm test_pipe_id, 8
.lcomm test_mp_zf,   4
.lcomm test_mp_sf,   4
.lcomm test_mp_cf,   4
.lcomm test_mp_of,   4
.lcomm test_mp_cyc,  8
.lcomm test_mp_stl,  8

.text

# ------------------------------------------------------------------------------
# Helper: record_result(RCX = passed (0/1), RDX = description string)
# ------------------------------------------------------------------------------
record_result:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    inc  qword ptr [rip + total_cnt]
    test rcx, rcx
    jz   .L_rec_fail

    inc  qword ptr [rip + pass_cnt]
    lea  rcx, [rip + pass_fmt]
    call printf
    jmp  .L_rec_exit

.L_rec_fail:
    inc  qword ptr [rip + fail_cnt]
    lea  rcx, [rip + fail_fmt]
    call printf

.L_rec_exit:
    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Automated COA Verification Suite] run_all_coa_tests
# Purpose : Runs all 65 unit test assertions across CO1..CO4, Enemies, Multiplayer.
# Output  : RAX = 0 if all passed, 1 if any failed
# ------------------------------------------------------------------------------
.global run_all_coa_tests
.global _run_all_coa_tests
run_all_coa_tests:
_run_all_coa_tests:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push rdi
    push r12
    sub  rsp, 64                # 4 pushes (32) + 64 + rbp (8) -> 16-byte aligned

    mov  qword ptr [rip + total_cnt], 0
    mov  qword ptr [rip + pass_cnt], 0
    mov  qword ptr [rip + fail_cnt], 0

    lea  rcx, [rip + test_banner]
    call print_str

    # ==========================================================================
    # SUITE 1: CO1 Architecture & Registers (6 Tests)
    # ==========================================================================
    lea  rcx, [rip + sec_co1_test]
    call print_str

    # 1. GPR Partitioning
    mov  rax, 0x1122334412345678
    xor  ecx, ecx
    cmp  eax, 0x12345678
    jne  .L_t1
    cmp  ax, 0x5678
    jne  .L_t1
    cmp  ah, 0x56
    jne  .L_t1
    cmp  al, 0x78
    jne  .L_t1
    mov  ecx, 1
.L_t1:
    lea  rdx, [rip + t_co1_gpr]
    call record_result

    # 2. Carry Flag (CF)
    mov  rax, 0xFFFFFFFFFFFFFFFF
    add  rax, 1
    setc cl
    movzx rcx, cl
    lea  rdx, [rip + t_co1_cf]
    call record_result

    # 3. Zero Flag (ZF)
    mov  rax, 100
    sub  rax, 100
    setz cl
    movzx rcx, cl
    lea  rdx, [rip + t_co1_zf]
    call record_result

    # 4. Sign Flag (SF)
    mov  rax, 20
    sub  rax, 50
    sets cl
    movzx rcx, cl
    lea  rdx, [rip + t_co1_sf]
    call record_result

    # 5. Overflow Flag (OF)
    mov  rax, 0x7FFFFFFFFFFFFFFF
    add  rax, 1
    seto cl
    movzx rcx, cl
    lea  rdx, [rip + t_co1_of]
    call record_result

    # 6. SIB Addressing [RBX + RSI*8 + 16] -> index 2 at +32 = -34
    lea  rbx, [rip + test_array_coa]
    mov  rsi, 2
    mov  rax, [rbx + rsi*8 + 16] # Index 2*8 + 16 = +32 bytes -> element[4] = -34
    cmp  rax, -34
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co1_sib]
    call record_result

    # ==========================================================================
    # SUITE 2: CO2 Assembly Programs (15 Tests)
    # ==========================================================================
    lea  rcx, [rip + sec_co2_test]
    call print_str

    # 7. Addition
    mov  rcx, 45
    mov  rdx, 55
    call asm_add_x64
    cmp  rax, 100
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_add]
    call record_result

    # 8. Subtraction
    mov  rcx, 100
    mov  rdx, 35
    call asm_sub_x64
    cmp  rax, 65
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_sub]
    call record_result

    # 9. Multiplication
    mov  rcx, -12
    mov  rdx, 8
    call asm_mul_x64
    cmp  rax, -96
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_mul]
    call record_result

    # 10. Division with remainder
    mov  rcx, 47
    mov  rdx, 5
    lea  r8,  [rip + test_rem]
    call asm_div_x64
    xor  ecx, ecx
    cmp  rax, 9
    jne  .L_t10
    cmp  qword ptr [rip + test_rem], 2
    jne  .L_t10
    mov  ecx, 1
.L_t10:
    lea  rdx, [rip + t_co2_div]
    call record_result

    # 11. Division by zero protection
    mov  rcx, 50
    xor  rdx, rdx
    lea  r8,  [rip + test_rem]
    call asm_div_x64
    xor  ecx, ecx
    test rax, rax
    jnz  .L_t11
    cmp  qword ptr [rip + test_rem], 0
    jne  .L_t11
    mov  ecx, 1
.L_t11:
    lea  rdx, [rip + t_co2_div_zero]
    call record_result

    # 12. Array Min & Max
    lea  rcx, [rip + test_array_coa]
    mov  rdx, [rip + test_array_size]
    lea  r8,  [rip + test_min]
    lea  r9,  [rip + test_max]
    call asm_find_min_max
    xor  ecx, ecx
    cmp  qword ptr [rip + test_min], -34
    jne  .L_t12
    cmp  qword ptr [rip + test_max], 102
    jne  .L_t12
    mov  ecx, 1
.L_t12:
    lea  rdx, [rip + t_co2_minmax]
    call record_result

    # 13. Array Sum
    lea  rcx, [rip + test_array_coa]
    mov  rdx, [rip + test_array_size]
    call asm_array_sum
    cmp  rax, 207
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_sum]
    call record_result

    # 14. Factorial 0!
    xor  rcx, rcx
    call asm_factorial
    cmp  rax, 1
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_fact0]
    call record_result

    # 15. Factorial 5!
    mov  rcx, 5
    call asm_factorial
    cmp  rax, 120
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_fact5]
    call record_result

    # 16. Fibonacci F(0..6)
    lea  rcx, [rip + test_fib_buf]
    mov  rdx, 7
    call asm_fibonacci
    lea  rsi, [rip + test_fib_buf]
    xor  ecx, ecx
    cmp  qword ptr [rsi + 0],  0
    jne  .L_t16
    cmp  qword ptr [rsi + 8],  1
    jne  .L_t16
    cmp  qword ptr [rsi + 16], 1
    jne  .L_t16
    cmp  qword ptr [rsi + 24], 2
    jne  .L_t16
    cmp  qword ptr [rsi + 32], 3
    jne  .L_t16
    cmp  qword ptr [rsi + 40], 5
    jne  .L_t16
    cmp  qword ptr [rsi + 48], 8
    jne  .L_t16
    mov  ecx, 1
.L_t16:
    lea  rdx, [rip + t_co2_fib]
    call record_result

    # 17. Linear Search Hit
    lea  rcx, [rip + test_array_coa]
    mov  rdx, [rip + test_array_size]
    mov  r8,  88
    call asm_linear_search
    cmp  rax, 2
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_search_hit]
    call record_result

    # 18. Linear Search Miss
    lea  rcx, [rip + test_array_coa]
    mov  rdx, [rip + test_array_size]
    mov  r8,  9999
    call asm_linear_search
    cmp  rax, -1
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_co2_search_miss]
    call record_result

    # 19. In-Place Array Reversal (reverse, check, restore)
    lea  rcx, [rip + test_array_coa]
    mov  rdx, [rip + test_array_size]
    call asm_reverse_array
    lea  rsi, [rip + test_array_coa]
    xor  ebx, ebx
    cmp  qword ptr [rsi + 0],  23
    jne  .L_t19_restore
    cmp  qword ptr [rsi + 56], 45
    jne  .L_t19_restore
    mov  ebx, 1
.L_t19_restore:
    lea  rcx, [rip + test_array_coa]
    mov  rdx, [rip + test_array_size]
    call asm_reverse_array
    mov  rcx, rbx
    lea  rdx, [rip + t_co2_reverse]
    call record_result

    # 20. Pos/Neg/Zero Counts
    lea  rcx, [rip + test_array_coa]
    mov  rdx, [rip + test_array_size]
    lea  r8,  [rip + test_pos]
    lea  r9,  [rip + test_neg]
    lea  rax, [rip + test_zero]
    mov  [rsp + 32], rax
    call asm_count_pos_neg
    xor  ecx, ecx
    cmp  qword ptr [rip + test_pos],  4
    jne  .L_t20
    cmp  qword ptr [rip + test_neg],  3
    jne  .L_t20
    cmp  qword ptr [rip + test_zero], 1
    jne  .L_t20
    mov  ecx, 1
.L_t20:
    lea  rdx, [rip + t_co2_counts]
    call record_result

    # 21. Bitwise AND, OR, XOR, NOT
    mov  rcx, 0xFF
    mov  rdx, 0x0F
    call asm_bitwise_and
    cmp  rax, 0x0F
    jne  .L_t21_fail
    mov  rcx, 0xF0
    mov  rdx, 0x0F
    call asm_bitwise_or
    cmp  rax, 0xFF
    jne  .L_t21_fail
    mov  rcx, 0xFF
    mov  rdx, 0x0F
    call asm_bitwise_xor
    cmp  rax, 0xF0
    jne  .L_t21_fail
    xor  rcx, rcx
    call asm_bitwise_not
    cmp  rax, -1
    jne  .L_t21_fail
    mov  ecx, 1
    jmp  .L_t21_rec
.L_t21_fail:
    xor  ecx, ecx
.L_t21_rec:
    lea  rdx, [rip + t_co2_bitwise]
    call record_result

    # ==========================================================================
    # SUITE 3: CO3 Computer Arithmetic (18 Tests)
    # ==========================================================================
    lea  rcx, [rip + sec_co3_test]
    call print_str

    # 22..28 Booth Multiplication tests
    mov  rcx, 7;   mov rdx, 5;   xor r8, r8; call asm_booth_multiply; cmp rax, 35;   sete cl; movzx rcx, cl; lea rdx, [rip + t_co3_b_pp];  call record_result
    mov  rcx, 9;   mov rdx, -4;  xor r8, r8; call asm_booth_multiply; cmp rax, -36;  sete cl; movzx rcx, cl; lea rdx, [rip + t_co3_b_pn];  call record_result
    mov  rcx, -8;  mov rdx, 6;   xor r8, r8; call asm_booth_multiply; cmp rax, -48;  sete cl; movzx rcx, cl; lea rdx, [rip + t_co3_b_np];  call record_result
    mov  rcx, -11; mov rdx, -3;  xor r8, r8; call asm_booth_multiply; cmp rax, 33;   sete cl; movzx rcx, cl; lea rdx, [rip + t_co3_b_nn];  call record_result
    mov  rcx, 0;   mov rdx, 45;  xor r8, r8; call asm_booth_multiply; cmp rax, 0;    sete cl; movzx rcx, cl; lea rdx, [rip + t_co3_b_zm];  call record_result
    mov  rcx, -25; mov rdx, 0;   xor r8, r8; call asm_booth_multiply; cmp rax, 0;    sete cl; movzx rcx, cl; lea rdx, [rip + t_co3_b_zq];  call record_result
    mov  rcx, -64; mov rdx, 2;   xor r8, r8; call asm_booth_multiply; cmp rax, -128; sete cl; movzx rcx, cl; lea rdx, [rip + t_co3_b_bnd]; call record_result

    # 29..36 Restoring Division tests
    mov  rcx, 24;  mov rdx, 6;   lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 4;   jne 1f; cmp qword ptr [rip + test_rem], 0; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_exact]; call record_result

    mov  rcx, 29;  mov rdx, 5;   lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 5;   jne 1f; cmp qword ptr [rip + test_rem], 4; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_ne1]; call record_result

    mov  rcx, 100; mov rdx, 7;   lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 14;  jne 1f; cmp qword ptr [rip + test_rem], 2; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_ne2]; call record_result

    mov  rcx, 3;   mov rdx, 10;  lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 0;   jne 1f; cmp qword ptr [rip + test_rem], 3; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_small]; call record_result

    mov  rcx, 0;   mov rdx, 15;  lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 0;   jne 1f; cmp qword ptr [rip + test_rem], 0; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_zdiv]; call record_result

    mov  rcx, 45;  mov rdx, 0;   lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 0;   jne 1f; cmp qword ptr [rip + test_rem], 0; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_d0]; call record_result

    mov  rcx, 255; mov rdx, 1;   lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 255; jne 1f; cmp qword ptr [rip + test_rem], 0; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_maxq]; call record_result

    mov  rcx, 255; mov rdx, 255; lea r8, [rip + test_rem]; xor r9, r9; call asm_restoring_divide
    xor  ecx, ecx; cmp rax, 1;   jne 1f; cmp qword ptr [rip + test_rem], 0; jne 1f; mov ecx, 1
1:  lea  rdx, [rip + t_co3_r_maxm]; call record_result

    # 37. 128-bit Addition with Carry (ADC)
    lea  rax, [rip + test_lo]; mov [rsp + 32], rax
    lea  rax, [rip + test_hi]; mov [rsp + 40], rax
    lea  rax, [rip + test_cf]; mov [rsp + 48], rax
    mov  rcx, 0xFFFFFFFFFFFFFFFF
    mov  rdx, 1
    mov  r8,  1
    mov  r9,  2
    call asm_add_with_carry_128
    xor  ecx, ecx
    cmp  qword ptr [rip + test_lo], 0
    jne  1f
    cmp  qword ptr [rip + test_hi], 4
    jne  1f
    cmp  qword ptr [rip + test_cf], 1
    jne  1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_co3_adc]; call record_result

    # 38. Signed Overflow Pos + Pos = Neg
    mov  rax, 0x7FFFFFFFFFFFFFFF
    add  rax, 1
    seto cl
    movzx rcx, cl
    lea  rdx, [rip + t_co3_ov_pos]
    call record_result

    # 39. Signed Overflow Neg + Neg = Pos
    mov  rax, 0x8000000000000000
    add  rax, -1
    seto cl
    movzx rcx, cl
    lea  rdx, [rip + t_co3_ov_neg]
    call record_result

    # ==========================================================================
    # SUITE 4: CO4 Control Unit & Pipeline (2 Tests)
    # ==========================================================================
    lea  rcx, [rip + sec_co4_test]
    call print_str

    # 40. Control Unit opcode decode truth verification
    mov  rcx, 0; call coa_decode_control_signals; cmp rax, 0x91; jne 1f
    mov  rcx, 1; call coa_decode_control_signals; cmp rax, 0x79; jne 1f
    mov  rcx, 2; call coa_decode_control_signals; cmp rax, 0x45; jne 1f
    mov  rcx, 3; call coa_decode_control_signals; cmp rax, 0x03; jne 1f
    mov  rcx, 4; call coa_decode_control_signals; cmp rax, 0x00; jne 1f
    mov  ecx, 1; jmp 2f
1:  xor  ecx, ecx
2:  lea  rdx, [rip + t_co4_cu]; call record_result

    # 41. 5-Stage Pipeline Metrics (N=100, penalty=3 -> seq=500, ideal=104, real=107)
    mov  rcx, 100
    mov  rdx, 3
    lea  r8,  [rip + test_pipe_seq]
    lea  r9,  [rip + test_pipe_id]
    call coa_calc_pipeline_metrics
    xor  ecx, ecx
    cmp  qword ptr [rip + test_pipe_seq], 500; jne 1f
    cmp  qword ptr [rip + test_pipe_id],  104; jne 1f
    cmp  rax, 107;                             jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_co4_pipe]; call record_result

    # ==========================================================================
    # SUITE 5: Academic Enemy Modules (16 Tests)
    # ==========================================================================
    lea  rcx, [rip + sec_co5_test]
    call print_str

    # 42..47 Aegis Sentry (CO1 Enemy)
    call coa_init_aegis_shield_x64
    xor  ecx, ecx
    cmp  eax, 0xA5B6C7D8; jne 1f
    cmp  ax,  0xC7D8;     jne 1f
    cmp  ah,  0xC7;       jne 1f
    cmp  al,  0xD8;       jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_en_aegis_part]; call record_result

    mov  rcx, 0xFEDCBA98A5B6C7D8
    mov  rdx, 0x10
    call coa_damage_al_layer_x64
    xor  ecx, ecx
    cmp  al, 0xC8; jne 1f
    cmp  ah, 0xC7; jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_en_aegis_al]; call record_result

    mov  rcx, 0xFEDCBA98A5B6C7C8
    mov  rdx, 0x10
    call coa_recharge_ah_layer_x64
    xor  ecx, ecx
    cmp  ah, 0xD7; jne 1f
    cmp  al, 0xC8; jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_en_aegis_ah]; call record_result

    lea  r8,  [rip + test_mp_zf]
    lea  r9,  [rip + test_mp_sf]
    lea  rax, [rip + test_mp_cf]; mov [rsp + 32], rax
    lea  rax, [rip + test_mp_of]; mov [rsp + 40], rax
    mov  rcx, 50; mov rdx, 50
    call coa_eval_shield_flags_x64
    xor  ecx, ecx
    cmp  dword ptr [rip + test_mp_zf], 1; jne 1f
    cmp  dword ptr [rip + test_mp_sf], 0; jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_en_aegis_zf]; call record_result

    lea  r8,  [rip + test_mp_zf]
    lea  r9,  [rip + test_mp_sf]
    lea  rax, [rip + test_mp_cf]; mov [rsp + 32], rax
    lea  rax, [rip + test_mp_of]; mov [rsp + 40], rax
    mov  rcx, 20; mov rdx, 50
    call coa_eval_shield_flags_x64
    xor  ecx, ecx
    cmp  dword ptr [rip + test_mp_sf], 1; jne 1f
    cmp  dword ptr [rip + test_mp_cf], 1; jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_en_aegis_sfcf]; call record_result

    lea  rcx, [rip + test_sib_layers]
    mov  rdx, 2
    call coa_sib_shield_layer_x64
    mov  rbx, 0xCCCC000000000003
    cmp  rax, rbx
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_en_aegis_sib]
    call record_result

    # 48..49 Fibonacci Skimmer (CO2 Enemy)
    lea  rcx, [rip + test_en_buf]
    mov  rdx, 6
    mov  r8,  400
    mov  r9,  100
    call coa_skimmer_compute_trajectory
    lea  rsi, [rip + test_en_buf]
    xor  ecx, ecx
    cmp  qword ptr [rsi + 0],  400; jne 1f
    cmp  qword ptr [rsi + 8],  399; jne 1f
    cmp  qword ptr [rsi + 16], 401; jne 1f
    cmp  qword ptr [rsi + 24], 398; jne 1f
    cmp  qword ptr [rsi + 32], 403; jne 1f
    cmp  qword ptr [rsi + 40], 395; jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_en_skim_traj]; call record_result

    mov  rcx, 5
    mov  rdx, 100
    call coa_skimmer_fire_interval
    cmp  rax, 500
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_en_skim_fire]
    call record_result

    # 50..54 Booth Multiplier Gunship (CO3 Enemy)
    mov rcx, 7;   mov rdx, 5;  lea r8, [rip + test_en_dmg]; lea r9, [rip + test_en_flg]; call coa_booth_calculate_kinetic_damage
    xor ecx, ecx; cmp qword ptr [rip + test_en_dmg], 35;   jne 1f; test qword ptr [rip + test_en_flg], 1; jnz 1f; mov ecx, 1
1:  lea rdx, [rip + t_en_b_pp]; call record_result

    mov rcx, 9;   mov rdx, -4; lea r8, [rip + test_en_dmg]; lea r9, [rip + test_en_flg]; call coa_booth_calculate_kinetic_damage
    xor ecx, ecx; cmp qword ptr [rip + test_en_dmg], -36;  jne 1f; test qword ptr [rip + test_en_flg], 1; jz 1f;  mov ecx, 1
1:  lea rdx, [rip + t_en_b_pn]; call record_result

    mov rcx, -11; mov rdx, -3; lea r8, [rip + test_en_dmg]; lea r9, [rip + test_en_flg]; call coa_booth_calculate_kinetic_damage
    xor ecx, ecx; cmp qword ptr [rip + test_en_dmg], 33;   jne 1f; test qword ptr [rip + test_en_flg], 1; jnz 1f; mov ecx, 1
1:  lea rdx, [rip + t_en_b_nn]; call record_result

    mov rcx, 0;   mov rdx, 45; lea r8, [rip + test_en_dmg]; lea r9, [rip + test_en_flg]; call coa_booth_calculate_kinetic_damage
    xor ecx, ecx; cmp qword ptr [rip + test_en_dmg], 0;    jne 1f; test qword ptr [rip + test_en_flg], 2; jz 1f;  mov ecx, 1
1:  lea rdx, [rip + t_en_b_zero]; call record_result

    mov rcx, -64; mov rdx, 2;  lea r8, [rip + test_en_dmg]; lea r9, [rip + test_en_flg]; call coa_booth_calculate_kinetic_damage
    xor ecx, ecx; cmp qword ptr [rip + test_en_dmg], -128; jne 1f; test qword ptr [rip + test_en_flg], 4; jz 1f;  mov ecx, 1
1:  lea rdx, [rip + t_en_b_sat]; call record_result

    # 55..57 Pipelined Railgun Dreadnought (CO4 Enemy)
    mov rcx, 4; mov rdx, 0; lea r8, [rip + test_en_cyc]; lea r9, [rip + test_en_stl]; call coa_railgun_pipeline_eval
    xor ecx, ecx; cmp qword ptr [rip + test_en_cyc], 8;  jne 1f; cmp qword ptr [rip + test_en_stl], 0; jne 1f; mov ecx, 1
1:  lea rdx, [rip + t_en_rg_ideal]; call record_result

    mov rcx, 4; mov rdx, 1; lea r8, [rip + test_en_cyc]; lea r9, [rip + test_en_stl]; call coa_railgun_pipeline_eval
    xor ecx, ecx; cmp qword ptr [rip + test_en_cyc], 10; jne 1f; cmp qword ptr [rip + test_en_stl], 2; jne 1f; mov ecx, 1
1:  lea rdx, [rip + t_en_rg_raw]; call record_result

    mov rcx, 4; mov rdx, 2; lea r8, [rip + test_en_cyc]; lea r9, [rip + test_en_stl]; call coa_railgun_pipeline_eval
    xor ecx, ecx; cmp qword ptr [rip + test_en_cyc], 8;  jne 1f; cmp qword ptr [rip + test_en_stl], 0; jne 1f; mov ecx, 1
1:  lea rdx, [rip + t_en_rg_fwd]; call record_result

    # ==========================================================================
    # SUITE 6: Multiplayer COA Combat Architecture (8 Tests)
    # ==========================================================================
    lea  rcx, [rip + sec_co6_test]
    call print_str

    # 58. Score Compare (4800 > 3200)
    lea  r8,  [rip + test_mp_zf]
    lea  r9,  [rip + test_mp_sf]
    lea  rax, [rip + test_mp_cf]; mov [rsp + 32], rax
    lea  rax, [rip + test_mp_of]; mov [rsp + 40], rax
    mov  rcx, 4800; mov rdx, 3200
    call coa_mp_compare_scores_x64
    xor  ecx, ecx
    cmp  rax, 1;                          jne 1f
    cmp  dword ptr [rip + test_mp_zf], 0; jne 1f
    cmp  dword ptr [rip + test_mp_sf], 0; jne 1f
    cmp  dword ptr [rip + test_mp_cf], 0; jne 1f
    cmp  dword ptr [rip + test_mp_of], 0; jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_mp_cmp]; call record_result

    # 59. Equal Scores (2500 == 2500 -> ZF=1)
    lea  r8,  [rip + test_mp_zf]
    lea  r9,  [rip + test_mp_sf]
    lea  rax, [rip + test_mp_cf]; mov [rsp + 32], rax
    lea  rax, [rip + test_mp_of]; mov [rsp + 40], rax
    mov  rcx, 2500; mov rdx, 2500
    call coa_mp_compare_scores_x64
    xor  ecx, ecx
    cmp  rax, 0;                          jne 1f
    cmp  dword ptr [rip + test_mp_zf], 1; jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_mp_eq]; call record_result

    # 60. SIB Telemetry Lookup
    lea  rcx, [rip + test_sib_layers]
    mov  rdx, 2
    call coa_mp_sib_lookup_x64
    mov  rbx, 0xCCCC000000000003
    cmp  rax, rbx
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_mp_sib]
    call record_result

    # 61. Multiplayer Fibonacci Interval F(6) = 8
    mov  rcx, 6
    call coa_mp_fibonacci_interval_x64
    cmp  rax, 8
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_mp_fib]
    call record_result

    # 62. Multiplayer Booth Score Delta (15 * -3 = -45)
    mov  rcx, 15
    mov  rdx, -3
    call coa_mp_booth_multiply_x64
    cmp  rax, -45
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_mp_b_neg]
    call record_result

    # 63. Multiplayer Booth Dual Negative (-20 * -4 = 80)
    mov  rcx, -20
    mov  rdx, -4
    call coa_mp_booth_multiply_x64
    cmp  rax, 80
    sete cl
    movzx rcx, cl
    lea  rdx, [rip + t_mp_b_pos]
    call record_result

    # 64. Multiplayer Pipeline RAW Stall (8 packets, mode 1 -> 14 cycles, 2 stalls)
    mov  rcx, 8; mov rdx, 1; lea r8, [rip + test_mp_cyc]; lea r9, [rip + test_mp_stl]
    call coa_mp_pipeline_stream_x64
    xor  ecx, ecx
    cmp  qword ptr [rip + test_mp_cyc], 14; jne 1f
    cmp  qword ptr [rip + test_mp_stl], 2;  jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_mp_pipe_raw]; call record_result

    # 65. Multiplayer Pipeline Forwarding (8 packets, mode 2 -> 12 cycles, 0 stalls)
    mov  rcx, 8; mov rdx, 2; lea r8, [rip + test_mp_cyc]; lea r9, [rip + test_mp_stl]
    call coa_mp_pipeline_stream_x64
    xor  ecx, ecx
    cmp  qword ptr [rip + test_mp_cyc], 12; jne 1f
    cmp  qword ptr [rip + test_mp_stl], 0;  jne 1f
    mov  ecx, 1
1:  lea  rdx, [rip + t_mp_pipe_fwd]; call record_result

    # ==========================================================================
    # Summary Report
    # ==========================================================================
    lea  rcx, [rip + summary_fmt]
    mov  rdx, [rip + total_cnt]
    mov  r8,  [rip + pass_cnt]
    mov  r9,  [rip + fail_cnt]
    call printf

    cmp  qword ptr [rip + fail_cnt], 0
    jne  .L_tests_failed

    lea  rcx, [rip + all_pass_msg]
    call print_str
    xor  eax, eax               # Return 0 = all passed
    jmp  .L_tests_exit

.L_tests_failed:
    lea  rcx, [rip + some_fail_msg]
    call print_str
    mov  eax, 1                 # Return 1 = failure

.L_tests_exit:
    add  rsp, 64
    pop  r12
    pop  rdi
    pop  rsi
    pop  rbx
    pop  rbp
    ret
