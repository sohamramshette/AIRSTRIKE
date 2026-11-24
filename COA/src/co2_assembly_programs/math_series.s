# ==============================================================================
# File    : COA/src/co2_assembly_programs/math_series.s
# Subject : COA-CO2 | Programs 5 & 6: Factorial & Fibonacci Series
# Topics  : Recursive Factorial, Stack Frame Unwinding, Iterative Fibonacci
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
math_demo_title: .asciz "\n--- CO2 Programs 5 & 6: 64-Bit Recursive Factorial & Fibonacci ---\n"
fact_prompt:     .asciz "  Enter integer for Factorial (0..20, default 10): "
fact_result_fmt: .asciz "  5. Factorial(%lld) = %lld\n"
fib_prompt:      .asciz "  Enter Fibonacci term count (1..20, default 10): "
fib_result_hdr:  .asciz "  6. Fibonacci (%lld terms): [ "
fib_term_fmt:    .asciz "%lld "
fib_close_fmt:   .asciz "]\n"

.bss
.align 8
.lcomm fib_buffer, 160          # Up to 20 quadwords (20 * 8 = 160 bytes)

.text

# ------------------------------------------------------------------------------
# [TOPIC: Recursive Factorial & Stack Frames] asm_factorial
# Purpose   : Computes n! recursively using per-call stack frames.
# Game Role : Models wave permutation scaling & proves call-stack unwinding in assembly.
# Input     : RCX = n (0..20)
# Output    : RAX = n!
# ------------------------------------------------------------------------------
.global asm_factorial
.global _asm_factorial
asm_factorial:
_asm_factorial:
    push rbp
    mov  rbp, rsp
    push rbx                    # WHY: Save each frame's `n` on stack before recursing into factorial(n-1)
    sub  rsp, 40                # WHY: 1 push (8) + 40 = 48 -> keeps stack 16-byte aligned across recursion

    mov  rbx, rcx               # WHY: Store `n` in RBX because RCX gets overwritten by the next call's argument
    cmp  rbx, 1                 # WHY: Stop recursion at base case (n <= 1 -> return 1) to avoid stack overflow
    jle  .L_fact_base

    lea  rcx, [rbx - 1]         # WHY: Compute (n - 1) into Arg1 without altering RBX or RFLAGS
    call asm_factorial          # WHY: Recurse; returns (n - 1)! in RAX
    imul rax, rbx               # WHY: Stack unwind step multiplies RAX = (n - 1)! * n
    jmp  .L_fact_exit

.L_fact_base:
    mov  rax, 1                 # WHY: Mathematical base case 0! = 1 and 1! = 1

.L_fact_exit:
    add  rsp, 40
    pop  rbx                    # WHY: Restore caller's `n` from this frame
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Iterative Fibonacci Sequence] asm_fibonacci
# Purpose   : Generates first n Fibonacci numbers F(0)..F(n-1) into out_arr.
# Game Role : Core math generator powering Fibonacci Skimmer enemy weaves & weapon rhythm.
# Inputs    : RCX = int64_t* out_arr, RDX = int64_t n
# ------------------------------------------------------------------------------
.global asm_fibonacci
.global _asm_fibonacci
asm_fibonacci:
_asm_fibonacci:
    push rbx
    push rdi

    mov  rdi, rcx
    mov  rcx, rdx
    cmp  rcx, 0                 # WHY: Guard against n <= 0 so nothing writes out of bounds
    jle  .L_fib_done

    mov  qword ptr [rdi], 0     # WHY: Seed F(0) = 0 directly in memory
    cmp  rcx, 1
    je   .L_fib_done

    mov  qword ptr [rdi + 8], 1 # WHY: Seed F(1) = 1 at +8 bytes
    cmp  rcx, 2
    je   .L_fib_done

    sub  rcx, 2                 # WHY: First 2 terms already written; compute remaining (n - 2) iteratively
    xor  rax, rax               # WHY: RAX = F(i-2) = 0 (iterative avoids exponential O(2^n) stack cost!)
    mov  rbx, 1                 # WHY: RBX = F(i-1) = 1
    add  rdi, 16                # WHY: Start writing at out_arr[2]

.L_fib_loop:
    lea  rdx, [rax + rbx]       # WHY: LEA computes F(i-2) + F(i-1) in 1 instruction without clobbering flags
    mov  [rdi], rdx             # WHY: Store F(i) into buffer for Skimmer trajectory lookup
    mov  rax, rbx               # WHY: Slide window: F(i-2) <- F(i-1)
    mov  rbx, rdx               # WHY: Slide window: F(i-1) <- F(i)
    add  rdi, 8
    dec  rcx
    jnz  .L_fib_loop

.L_fib_done:
    pop  rdi
    pop  rbx
    ret

# ------------------------------------------------------------------------------
# Internal helper: run_math_with_params(RCX = fact_n, RDX = fib_count)
# ------------------------------------------------------------------------------
run_math_with_params:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 32

    mov  rbx, rcx
    mov  rsi, rdx

    lea  rcx, [rip + math_demo_title]
    call print_str

    mov  rcx, rbx
    call asm_factorial
    lea  rcx, [rip + fact_result_fmt]
    mov  rdx, rbx
    mov  r8,  rax
    call printf

    lea  rcx, [rip + fib_buffer]
    mov  rdx, rsi
    call asm_fibonacci

    lea  rcx, [rip + fib_result_hdr]
    mov  rdx, rsi
    call printf

    xor  rbx, rbx
.L_pr_fib:
    cmp  rbx, rsi
    jge  .L_pr_fib_end
    lea  rcx, [rip + fib_term_fmt]
    lea  r10, [rip + fib_buffer]
    mov  rdx, [r10 + rbx*8]
    call printf
    inc  rbx
    jmp  .L_pr_fib
.L_pr_fib_end:
    lea  rcx, [rip + fib_close_fmt]
    call print_str

    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Interactive Math Series Demo] co2_demo_math
# Purpose   : Prompts user for Factorial n and Fibonacci count (defaults on out-of-range).
# Game Role : Interactive sandbox for testing recursion and Fibonacci generation.
# ------------------------------------------------------------------------------
.global co2_demo_math
.global _co2_demo_math
co2_demo_math:
_co2_demo_math:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 32

    lea  rcx, [rip + fact_prompt]
    call print_str
    call read_int
    mov  rbx, rax
    cmp  rbx, 0
    jl   .L_f_def
    cmp  rbx, 20                # WHY: 20! is the largest factorial fitting inside signed 64-bit integer
    jle  .L_f_ok
.L_f_def:
    mov  rbx, 10
.L_f_ok:

    lea  rcx, [rip + fib_prompt]
    call print_str
    call read_int
    mov  rsi, rax
    cmp  rsi, 1
    jl   .L_fib_def
    cmp  rsi, 20
    jle  .L_fib_ok
.L_fib_def:
    mov  rsi, 10
.L_fib_ok:

    mov  rcx, rbx
    mov  rdx, rsi
    call run_math_with_params

    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Automated Math Series Demo] co2_demo_math_batch
# Purpose   : Non-interactive demo computing 10! and 10 Fibonacci terms.
# Game Role : Runs Programs 5 & 6 cleanly during batch mode.
# ------------------------------------------------------------------------------
.global co2_demo_math_batch
.global _co2_demo_math_batch
co2_demo_math_batch:
_co2_demo_math_batch:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    mov  rcx, 10
    mov  rdx, 10
    call run_math_with_params

    add  rsp, 32
    pop  rbp
    ret
