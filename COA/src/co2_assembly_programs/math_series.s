# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO2: MATHEMATICAL SERIES PROGRAMS
# Implements:
#   Prog 5. Recursive 64-bit Factorial Calculation (Stack Frame Dynamics)
#   Prog 6. Iterative 64-bit Fibonacci Series Generation
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
math_demo_title:
    .asciz "\n--- CO2 Programs 5 & 6: 64-Bit Factorial & Fibonacci Demonstration ---\n"
fact_prompt:
    .asciz "  Enter positive 64-bit integer for Factorial (0 to 20): "
fact_result_fmt:
    .asciz "  5. Factorial(%lld) = %lld (Computed recursively via 64-bit stack frames)\n"
fib_prompt:
    .asciz "  Enter number of Fibonacci terms to generate (1 to 20): "
fib_result_hdr:
    .asciz "  6. Fibonacci Series (%lld terms): [ "
fib_term_fmt:
    .asciz "%lld "
fib_close_fmt:
    .asciz "]\n"

.bss
.align 8
.lcomm fib_buffer, 160         # Storage for up to 20 terms (20 * 8 bytes = 160 bytes)

.text

# ==============================================================================
# Routine: asm_factorial
# - Small Meaning: Computes n! recursively using genuine 64-bit stack frames.
# - Long Form    : Assembly 64-Bit Recursive Factorial Calculator
# - Why We Used It: Models exponential projectile trajectory combinatorial expansion.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t n (Non-negative integer, 0..20)
# Returns:
#   RAX = 64-bit n! (Up to 20! = 2,432,902,008,176,640,000 fits in 64-bit signed int)
# ==============================================================================
.global asm_factorial
.global _asm_factorial
asm_factorial:
_asm_factorial:
    # --------------------------------------------------------------------------
    # PUSH RBP / MOV RBP, RSP / SUB RSP, 32:
    # - Small Meaning: Establish 64-bit stack frame and reserve shadow space
    # - Long Form    : Base Pointer Setup and Shadow Stack Allocation
    # - Why We Used It: Preserves stack alignment and provides storage across recursive calls.
    # --------------------------------------------------------------------------
    push rbp
    mov rbp, rsp
    sub rsp, 32                 # 32-byte caller shadow space
    push rbx                    # Preserve non-volatile RBX

    mov rbx, rcx                # RBX = n (Preserve current n across recursive call)

    # Base case check: n <= 1 -> return 1
    cmp rbx, 1
    jle .L_fact_base_case

    # Recursive step: factorial(n - 1)
    lea rcx, [rbx - 1]          # RCX = n - 1 (1st parameter in RCX)
    call asm_factorial          # Recursive invocation; RAX = factorial(n - 1)

    # --------------------------------------------------------------------------
    # IMUL RAX, RBX:
    # - Small Meaning: Multiply recursive result by n (64-bit signed multiplication)
    # - Long Form    : Signed Integer Multiply Accumulator by Base Register
    # - Why We Used It: Computes n * factorial(n - 1) directly in 64-bit precision.
    # --------------------------------------------------------------------------
    imul rax, rbx
    jmp .L_fact_exit

.L_fact_base_case:
    mov rax, 1                  # Base case: 0! = 1, 1! = 1

.L_fact_exit:
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_fibonacci
# - Small Meaning: Generates first n terms of Fibonacci sequence into buffer.
# - Long Form    : Assembly 64-Bit Iterative Fibonacci Sequence Generator
# - Why We Used It: Drives rhythmic pulse timings and evasive spiral wave patterns.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t* out_arr (Pointer to output buffer)
#   RDX = int64_t n        (Number of terms to generate)
# ==============================================================================
.global asm_fibonacci
.global _asm_fibonacci
asm_fibonacci:
_asm_fibonacci:
    push rbp
    mov rbp, rsp
    push rbx
    push rsi
    push rdi

    mov rdi, rcx                # RDI = destination buffer pointer
    mov rcx, rdx                # RCX = number of terms n

    cmp rcx, 0
    jle .L_fib_exit

    # First term: F(0) = 0
    mov qword ptr [rdi], 0
    cmp rcx, 1
    je .L_fib_exit

    # Second term: F(1) = 1
    mov qword ptr [rdi + 8], 1
    cmp rcx, 2
    je .L_fib_exit

    # Iterative calculation for term 3 onwards
    sub rcx, 2                  # Remaining terms count
    xor rax, rax                # RAX = F(i-2) = 0
    mov rbx, 1                  # RBX = F(i-1) = 1
    add rdi, 16                 # Point to &out_arr[2] (each element is 8 bytes)

.L_fib_loop:
    # --------------------------------------------------------------------------
    # LEA RDX, [RAX + RBX]:
    # - Small Meaning: Compute F(i) = F(i-2) + F(i-1) in one instruction
    # - Long Form    : Load Effective Address of Sum
    # - Why We Used It: Fast addition without modifying condition flags.
    # --------------------------------------------------------------------------
    lea rdx, [rax + rbx]        # RDX = next Fibonacci term
    mov [rdi], rdx              # Store into 64-bit array buffer

    mov rax, rbx                # Slide window: F(i-2) = old F(i-1)
    mov rbx, rdx                # Slide window: F(i-1) = new F(i)

    add rdi, 8                  # Advance buffer pointer by 8 bytes
    dec rcx
    jnz .L_fib_loop

.L_fib_exit:
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co2_demo_math
# - Small Meaning: Interactive demonstration runner for Factorial and Fibonacci.
# - Long Form    : CO2 Demonstration Driver for 64-Bit Mathematical Series
# - Why We Used It: Provides interactive CLI demonstration with verified edge cases.
# ==============================================================================
.global co2_demo_math
.global _co2_demo_math
co2_demo_math:
_co2_demo_math:
    push rbp
    mov rbp, rsp
    sub rsp, 48                 # Shadow space + stack alignment
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + math_demo_title]
    call print_str

    # 5. Factorial
    lea rcx, [rip + fact_prompt]
    call print_str
    call read_int               # Returns input in RAX
    mov rbx, rax                # RBX = n

    cmp rbx, 0
    jl .L_fact_default
    cmp rbx, 20
    jg .L_fact_default
    jmp .L_fact_run

.L_fact_default:
    mov rbx, 10                 # Safe default to 10 if out of range

.L_fact_run:
    mov rcx, rbx
    call asm_factorial          # RAX = 10! = 3628800

    lea rcx, [rip + fact_result_fmt]
    mov rdx, rbx
    mov r8,  rax
    call printf

    # 6. Fibonacci
    lea rcx, [rip + fib_prompt]
    call print_str
    call read_int
    mov rsi, rax                # RSI = terms count

    cmp rsi, 1
    jl .L_fib_default
    cmp rsi, 20
    jg .L_fib_default
    jmp .L_fib_run

.L_fib_default:
    mov rsi, 10                 # Default to 10 terms

.L_fib_run:
    lea rcx, [rip + fib_buffer]
    mov rdx, rsi
    call asm_fibonacci

    lea rcx, [rip + fib_result_hdr]
    mov rdx, rsi
    call printf

    xor rbx, rbx
.L_print_fib_loop:
    cmp rbx, rsi
    jge .L_print_fib_done
    lea rcx, [rip + fib_term_fmt]
    lea r10, [rip + fib_buffer]
    mov rdx, [r10 + rbx*8]
    call printf
    inc rbx
    jmp .L_print_fib_loop

.L_print_fib_done:
    lea rcx, [rip + fib_close_fmt]
    call print_str

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
