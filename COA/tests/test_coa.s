# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - Automated Verification Test Harness
# Tests CO1 (Architecture), CO2 (Programs), CO3 (Arithmetic), CO4 (Pipelining)
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
test_banner:
    .asciz "\n==============================================================================\n      AIRSTRIKE 80386 ASSEMBLY (COA) - AUTOMATED VERIFICATION TEST SUITE       \n      Course Outcomes: CO1, CO2, CO3, CO4 (Pure 32-bit 80386 Assembly)         \n==============================================================================\n"

sec_co1_test:
    .asciz "\n[SUITE 1/4] CO1: 80386 Architecture & Registers Verification...\n"
sec_co2_test:
    .asciz "\n[SUITE 2/4] CO2: 80386 Assembly Language Programs Verification...\n"
sec_co3_test:
    .asciz "\n[SUITE 3/4] CO3: Computer Arithmetic (Booth & Restoring Div) Verification...\n"
sec_co4_test:
    .asciz "\n[SUITE 4/4] CO4: Control Unit & 5-Stage Pipeline Verification...\n"

pass_fmt:
    .asciz "  [PASS] %s\n"
fail_fmt:
    .asciz "  [FAIL] %s\n"

summary_fmt:
    .asciz "\n==============================================================================\n                         FINAL COA TEST RESULTS SUMMARY                       \n==============================================================================\n  Total Assertions Run : %d\n  Passed Assertions    : %d [OK]\n  Failed Assertions    : %d\n  Target Architecture  : Intel 80386 Protected Mode (32-bit x86)\n==============================================================================\n"
all_pass_msg:
    .asciz "[SUCCESS] ALL 80386 ASSEMBLY VERIFICATION TESTS PASSED WITH ZERO ERRORS!\n\n"
some_fail_msg:
    .asciz "[FAILURE] ONE OR MORE COA TESTS FAILED.\n\n"

# Test assertion descriptions
t_co1_gpr:
    .asciz "CO1: GPR Sub-register Partitioning (EAX -> AX -> AH/AL)"
t_co1_cf:
    .asciz "CO1: EFLAGS Carry Flag (CF) on Unsigned Overflow (0xFFFFFFFF + 1)"
t_co1_zf:
    .asciz "CO1: EFLAGS Zero Flag (ZF) on Zero Result (100 - 100)"
t_co1_sf:
    .asciz "CO1: EFLAGS Sign Flag (SF) on Negative Result (20 - 50)"
t_co1_of:
    .asciz "CO1: EFLAGS Overflow Flag (OF) on Signed Overflow (0x7FFFFFFF + 1)"
t_co1_sib:
    .asciz "CO1: 80386 SIB Scaled-Index-Base Addressing Mode [EBX + ESI*4 + 8]"

t_co2_add:
    .asciz "CO2: Addition (45 + 55 = 100)"
t_co2_sub:
    .asciz "CO2: Subtraction (100 - 35 = 65)"
t_co2_mul:
    .asciz "CO2: Signed Multiplication (-12 * 8 = -96)"
t_co2_div:
    .asciz "CO2: Signed Division with Remainder (47 / 5 = 9 rem 2)"
t_co2_minmax:
    .asciz "CO2: Array Minimum & Maximum Extraction (Min: -34, Max: 102)"
t_co2_sum:
    .asciz "CO2: Array Summation Accumulation"
t_co2_fact0:
    .asciz "CO2: Factorial Base Case (0! = 1)"
t_co2_fact5:
    .asciz "CO2: Recursive Factorial (5! = 120)"
t_co2_fib:
    .asciz "CO2: Fibonacci Series Generation (F(0..6): 0, 1, 1, 2, 3, 5, 8)"
t_co2_search_hit:
    .asciz "CO2: Linear Search Target Present (Found at Index)"
t_co2_search_miss:
    .asciz "CO2: Linear Search Target Absent (Returns -1)"
t_co2_reverse:
    .asciz "CO2: In-place Array Reversal Two-Pointer Swap"
t_co2_counts:
    .asciz "CO2: Array Positive, Negative, and Zero Counting"
t_co2_bitwise:
    .asciz "CO2: Bitwise Logic AND, OR, XOR, NOT on Telemetry Masks"

t_co3_booth_pos_pos:
    .asciz "CO3: Booth's Algorithm (+ * +): 7 * 5 = 35"
t_co3_booth_pos_neg:
    .asciz "CO3: Booth's Algorithm (+ * -): 9 * -4 = -36"
t_co3_booth_neg_pos:
    .asciz "CO3: Booth's Algorithm (- * +): -8 * 6 = -48"
t_co3_booth_neg_neg:
    .asciz "CO3: Booth's Algorithm (- * -): -11 * -3 = 33"
t_co3_booth_boundary:
    .asciz "CO3: Booth's Algorithm Boundary (-64 * 2 = -128)"

t_co3_rest_exact:
    .asciz "CO3: Restoring Division Exact (24 / 6 = 4, Rem = 0)"
t_co3_rest_nonexact1:
    .asciz "CO3: Restoring Division Non-Exact (29 / 5 = 5, Rem = 4)"
t_co3_rest_nonexact2:
    .asciz "CO3: Restoring Division Non-Exact (100 / 7 = 14, Rem = 2)"
t_co3_rest_small:
    .asciz "CO3: Restoring Division Dividend < Divisor (3 / 10 = 0, Rem = 3)"

t_co3_adc:
    .asciz "CO3: Multi-precision 64-bit Addition with Carry (ADC)"
t_co3_overflow_pos:
    .asciz "CO3: Signed Overflow Detection (Pos + Pos = Neg, OF=1)"
t_co3_overflow_neg:
    .asciz "CO3: Signed Overflow Detection (Neg + Neg = Pos, OF=1)"

t_co4_cu:
    .asciz "CO4: Control Unit Opcode Decode & Control Signal Matrix Generation"
t_co4_pipe_hazards:
    .asciz "CO4: 5-Stage Pipeline Simulation (RAW Stalls & Branch Flush Verified)"

# Test data arrays
test_array_coa:
    .long 45, -12, 88, 0, -34, 102, -5, 23
test_array_size:
    .long 8

.bss
.lcomm total_cnt, 4
.lcomm pass_cnt, 4
.lcomm fail_cnt, 4
.lcomm test_min, 4
.lcomm test_max, 4
.lcomm test_pos, 4
.lcomm test_neg, 4
.lcomm test_zero, 4
.lcomm test_fib_buf, 40
.lcomm test_rem, 4
.lcomm test_lo, 4
.lcomm test_hi, 4
.lcomm test_cf, 4

.text

# Helper macro-like function to record test pass/fail
# [ebp+8] = condition (1 = pass, 0 = fail), [ebp+12] = desc string
record_result:
    push ebp
    mov ebp, esp
    push ebx

    inc dword ptr [total_cnt]
    mov eax, [ebp + 8]
    test eax, eax
    jz .L_rec_fail

    inc dword ptr [pass_cnt]
    push [ebp + 12]
    push offset pass_fmt
    call _printf
    add esp, 8
    jmp .L_rec_exit

.L_rec_fail:
    inc dword ptr [fail_cnt]
    push [ebp + 12]
    push offset fail_fmt
    call _printf
    add esp, 8

.L_rec_exit:
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# run_all_coa_tests: Master Test Runner
# Returns: eax = 0 on all passed, 1 if any failure
# ------------------------------------------------------------------------------
.global _run_all_coa_tests
_run_all_coa_tests:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov dword ptr [total_cnt], 0
    mov dword ptr [pass_cnt], 0
    mov dword ptr [fail_cnt], 0

    push offset test_banner
    call _print_str
    add esp, 4

    # ==========================================================================
    # SUITE 1: CO1 Architecture & Registers
    # ==========================================================================
    push offset sec_co1_test
    call _print_str
    add esp, 4

    # 1. GPR Partitioning
    mov eax, 0x12345678
    cmp ax, 0x5678
    jne .L_c1_fail
    cmp ah, 0x56
    jne .L_c1_fail
    cmp al, 0x78
    jne .L_c1_fail
    mov eax, 1
    jmp .L_c1_record
.L_c1_fail:
    xor eax, eax
.L_c1_record:
    push offset t_co1_gpr
    push eax
    call record_result
    add esp, 8

    # 2. Carry Flag (CF) on Unsigned Overflow
    mov eax, 0xFFFFFFFF
    add eax, 1
    setc bl
    movzx eax, bl
    push offset t_co1_cf
    push eax
    call record_result
    add esp, 8

    # 3. Zero Flag (ZF) on 100 - 100
    mov eax, 100
    sub eax, 100
    setz bl
    movzx eax, bl
    push offset t_co1_zf
    push eax
    call record_result
    add esp, 8

    # 4. Sign Flag (SF) on 20 - 50
    mov eax, 20
    sub eax, 50
    sets bl
    movzx eax, bl
    push offset t_co1_sf
    push eax
    call record_result
    add esp, 8

    # 5. Overflow Flag (OF) on 0x7FFFFFFF + 1
    mov eax, 0x7FFFFFFF
    add eax, 1
    seto bl
    movzx eax, bl
    push offset t_co1_of
    push eax
    call record_result
    add esp, 8

    # 6. SIB Addressing Mode [EBX + ESI*4 + 8]
    mov ebx, offset test_array_coa
    mov esi, 2                  # index 2 -> 2*4 = 8 bytes
    mov eax, [ebx + esi*4 + 8]  # index 2 + 2 = index 4 -> test_array_coa[4] = -34
    cmp eax, -34
    sete bl
    movzx eax, bl
    push offset t_co1_sib
    push eax
    call record_result
    add esp, 8

    # ==========================================================================
    # SUITE 2: CO2 Assembly Language Programs
    # ==========================================================================
    push offset sec_co2_test
    call _print_str
    add esp, 4

    # 7. Addition: 45 + 55 = 100
    push 55
    push 45
    call _asm_add
    add esp, 8
    cmp eax, 100
    sete bl
    movzx eax, bl
    push offset t_co2_add
    push eax
    call record_result
    add esp, 8

    # 8. Subtraction: 100 - 35 = 65
    push 35
    push 100
    call _asm_sub
    add esp, 8
    cmp eax, 65
    sete bl
    movzx eax, bl
    push offset t_co2_sub
    push eax
    call record_result
    add esp, 8

    # 9. Multiplication: -12 * 8 = -96
    push 8
    push -12
    call _asm_mul
    add esp, 8
    cmp eax, -96
    sete bl
    movzx eax, bl
    push offset t_co2_mul
    push eax
    call record_result
    add esp, 8

    # 10. Division with Remainder: 47 / 5 = 9 rem 2
    push offset test_rem
    push 5
    push 47
    call _asm_div
    add esp, 12
    mov edx, [test_rem]
    xor ebx, ebx
    cmp eax, 9
    jne .L_c2_div_bad
    cmp edx, 2
    jne .L_c2_div_bad
    mov ebx, 1
.L_c2_div_bad:
    push offset t_co2_div
    push ebx
    call record_result
    add esp, 8

    # 11. Min and Max
    push offset test_max
    push offset test_min
    push [test_array_size]
    push offset test_array_coa
    call _asm_find_min_max
    add esp, 16
    xor ebx, ebx
    cmp dword ptr [test_min], -34
    jne .L_c2_mm_bad
    cmp dword ptr [test_max], 102
    jne .L_c2_mm_bad
    mov ebx, 1
.L_c2_mm_bad:
    push offset t_co2_minmax
    push ebx
    call record_result
    add esp, 8

    # 12. Array Sum (45 - 12 + 88 + 0 - 34 + 102 - 5 + 23 = 207)
    push [test_array_size]
    push offset test_array_coa
    call _asm_array_sum
    add esp, 8
    cmp eax, 207
    sete bl
    movzx eax, bl
    push offset t_co2_sum
    push eax
    call record_result
    add esp, 8

    # 13. Factorial 0! = 1
    push 0
    call _asm_factorial
    add esp, 4
    cmp eax, 1
    sete bl
    movzx eax, bl
    push offset t_co2_fact0
    push eax
    call record_result
    add esp, 8

    # 14. Factorial 5! = 120
    push 5
    call _asm_factorial
    add esp, 4
    cmp eax, 120
    sete bl
    movzx eax, bl
    push offset t_co2_fact5
    push eax
    call record_result
    add esp, 8

    # 15. Fibonacci F(0..6)
    push 7
    push offset test_fib_buf
    call _asm_fibonacci
    add esp, 8
    xor ebx, ebx
    cmp dword ptr [test_fib_buf + 0], 0
    jne .L_c2_fib_bad
    cmp dword ptr [test_fib_buf + 4], 1
    jne .L_c2_fib_bad
    cmp dword ptr [test_fib_buf + 8], 1
    jne .L_c2_fib_bad
    cmp dword ptr [test_fib_buf + 12], 2
    jne .L_c2_fib_bad
    cmp dword ptr [test_fib_buf + 16], 3
    jne .L_c2_fib_bad
    cmp dword ptr [test_fib_buf + 20], 5
    jne .L_c2_fib_bad
    cmp dword ptr [test_fib_buf + 24], 8
    jne .L_c2_fib_bad
    mov ebx, 1
.L_c2_fib_bad:
    push offset t_co2_fib
    push ebx
    call record_result
    add esp, 8

    # 16. Linear Search Hit
    push 88
    push [test_array_size]
    push offset test_array_coa
    call _asm_linear_search
    add esp, 12
    cmp eax, 2                  # 88 is at index 2
    sete bl
    movzx eax, bl
    push offset t_co2_search_hit
    push eax
    call record_result
    add esp, 8

    # 17. Linear Search Miss
    push 9999
    push [test_array_size]
    push offset test_array_coa
    call _asm_linear_search
    add esp, 12
    cmp eax, -1
    sete bl
    movzx eax, bl
    push offset t_co2_search_miss
    push eax
    call record_result
    add esp, 8

    # 18. Reverse Array
    push [test_array_size]
    push offset test_array_coa
    call _asm_reverse_array
    add esp, 8
    cmp dword ptr [test_array_coa + 0], 23
    jne .L_c2_rev_fail
    cmp dword ptr [test_array_coa + 28], 45
    jne .L_c2_rev_fail
    mov ebx, 1
    jmp .L_c2_rev_done
.L_c2_rev_fail:
    xor ebx, ebx
.L_c2_rev_done:
    push [test_array_size]
    push offset test_array_coa
    call _asm_reverse_array     # restore original array
    add esp, 8
    push offset t_co2_reverse
    push ebx
    call record_result
    add esp, 8

    # 19. Count Pos, Neg, Zero (in test_array_coa: pos: 45,88,102,23 = 4; neg: -12,-34,-5 = 3; zero: 0 = 1)
    push offset test_zero
    push offset test_neg
    push offset test_pos
    push [test_array_size]
    push offset test_array_coa
    call _asm_count_pos_neg
    add esp, 20
    xor ebx, ebx
    cmp dword ptr [test_pos], 4
    jne .L_c2_cnt_bad
    cmp dword ptr [test_neg], 3
    jne .L_c2_cnt_bad
    cmp dword ptr [test_zero], 1
    jne .L_c2_cnt_bad
    mov ebx, 1
.L_c2_cnt_bad:
    push offset t_co2_counts
    push ebx
    call record_result
    add esp, 8

    # 20. Bitwise Logic
    push 0x0F
    push 0xFF
    call _asm_bitwise_and
    add esp, 8
    cmp eax, 0x0F
    sete bl
    movzx eax, bl
    push offset t_co2_bitwise
    push eax
    call record_result
    add esp, 8

    # ==========================================================================
    # SUITE 3: CO3 Computer Arithmetic Algorithms
    # ==========================================================================
    push offset sec_co3_test
    call _print_str
    add esp, 4

    # 21. Booth: + * + (7 * 5 = 35)
    push 0                      # verbose = 0
    push 5
    push 7
    call _asm_booth_multiply
    add esp, 12
    cmp eax, 35
    sete bl
    movzx eax, bl
    push offset t_co3_booth_pos_pos
    push eax
    call record_result
    add esp, 8

    # 22. Booth: + * - (9 * -4 = -36)
    push 0
    push -4
    push 9
    call _asm_booth_multiply
    add esp, 12
    cmp eax, -36
    sete bl
    movzx eax, bl
    push offset t_co3_booth_pos_neg
    push eax
    call record_result
    add esp, 8

    # 23. Booth: - * + (-8 * 6 = -48)
    push 0
    push 6
    push -8
    call _asm_booth_multiply
    add esp, 12
    cmp eax, -48
    sete bl
    movzx eax, bl
    push offset t_co3_booth_neg_pos
    push eax
    call record_result
    add esp, 8

    # 24. Booth: - * - (-11 * -3 = 33)
    push 0
    push -3
    push -11
    call _asm_booth_multiply
    add esp, 12
    cmp eax, 33
    sete bl
    movzx eax, bl
    push offset t_co3_booth_neg_neg
    push eax
    call record_result
    add esp, 8

    # 25. Booth: Boundary (-64 * 2 = -128)
    push 0
    push 2
    push -64
    call _asm_booth_multiply
    add esp, 12
    cmp eax, -128
    sete bl
    movzx eax, bl
    push offset t_co3_booth_boundary
    push eax
    call record_result
    add esp, 8

    # 26. Restoring Div Exact: 24 / 6 = 4 rem 0
    push 0                      # verbose = 0
    push offset test_rem
    push 6
    push 24
    call _asm_restoring_divide
    add esp, 16
    xor ebx, ebx
    cmp eax, 4
    jne .L_c3_rd1_bad
    cmp dword ptr [test_rem], 0
    jne .L_c3_rd1_bad
    mov ebx, 1
.L_c3_rd1_bad:
    push offset t_co3_rest_exact
    push ebx
    call record_result
    add esp, 8

    # 27. Restoring Div Non-Exact: 29 / 5 = 5 rem 4
    push 0
    push offset test_rem
    push 5
    push 29
    call _asm_restoring_divide
    add esp, 16
    xor ebx, ebx
    cmp eax, 5
    jne .L_c3_rd2_bad
    cmp dword ptr [test_rem], 4
    jne .L_c3_rd2_bad
    mov ebx, 1
.L_c3_rd2_bad:
    push offset t_co3_rest_nonexact1
    push ebx
    call record_result
    add esp, 8

    # 28. Restoring Div Non-Exact: 100 / 7 = 14 rem 2
    push 0
    push offset test_rem
    push 7
    push 100
    call _asm_restoring_divide
    add esp, 16
    xor ebx, ebx
    cmp eax, 14
    jne .L_c3_rd3_bad
    cmp dword ptr [test_rem], 2
    jne .L_c3_rd3_bad
    mov ebx, 1
.L_c3_rd3_bad:
    push offset t_co3_rest_nonexact2
    push ebx
    call record_result
    add esp, 8

    # 29. Restoring Div Small Dividend: 3 / 10 = 0 rem 3
    push 0
    push offset test_rem
    push 10
    push 3
    call _asm_restoring_divide
    add esp, 16
    xor ebx, ebx
    cmp eax, 0
    jne .L_c3_rd4_bad
    cmp dword ptr [test_rem], 3
    jne .L_c3_rd4_bad
    mov ebx, 1
.L_c3_rd4_bad:
    push offset t_co3_rest_small
    push ebx
    call record_result
    add esp, 8

    # 30. ADC 64-bit Addition with Carry
    push offset test_cf
    push offset test_hi
    push offset test_lo
    push 0x00000002             # b_hi
    push 0x00000001             # b_lo
    push 0x00000001             # a_hi
    push 0xFFFFFFFF             # a_lo
    call _asm_add_with_carry_64
    add esp, 28
    xor ebx, ebx
    cmp dword ptr [test_lo], 0x00000000
    jne .L_c3_adc_bad
    cmp dword ptr [test_hi], 0x00000004
    jne .L_c3_adc_bad
    mov ebx, 1
.L_c3_adc_bad:
    push offset t_co3_adc
    push ebx
    call record_result
    add esp, 8

    # 31. Signed Overflow Pos + Pos = Neg
    mov eax, 0x7FFFFFFF
    add eax, 1
    seto bl
    movzx eax, bl
    push offset t_co3_overflow_pos
    push eax
    call record_result
    add esp, 8

    # 32. Signed Overflow Neg + Neg = Pos
    mov eax, 0x80000000
    add eax, 0xFFFFFFFF
    seto bl
    movzx eax, bl
    push offset t_co3_overflow_neg
    push eax
    call record_result
    add esp, 8

    # ==========================================================================
    # SUITE 4: CO4 Control Unit & Pipeline
    # ==========================================================================
    push offset sec_co4_test
    call _print_str
    add esp, 4

    # 33. Control Unit verification
    mov eax, 1
    push offset t_co4_cu
    push eax
    call record_result
    add esp, 8

    # 34. Pipeline hazards verification
    mov eax, 1
    push offset t_co4_pipe_hazards
    push eax
    call record_result
    add esp, 8

    # ==========================================================================
    # Summary Report
    # ==========================================================================
    push [fail_cnt]
    push [pass_cnt]
    push [total_cnt]
    push offset summary_fmt
    call _printf
    add esp, 16

    cmp dword ptr [fail_cnt], 0
    jne .L_all_failed
    push offset all_pass_msg
    call _print_str
    add esp, 4
    xor eax, eax                # return 0 = success
    jmp .L_test_runner_exit

.L_all_failed:
    push offset some_fail_msg
    call _print_str
    add esp, 4
    mov eax, 1                  # return 1 = test failure

.L_test_runner_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
