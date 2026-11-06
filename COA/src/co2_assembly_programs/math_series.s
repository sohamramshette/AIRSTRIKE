# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO2: Mathematical Series Programs
# Implements:
#   5. Factorial Calculation (Recursive and Iterative)
#   6. Fibonacci Series Generation
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
math_demo_title:
    .asciz "\n--- CO2 Programs 5 & 6: Factorial & Fibonacci Demonstration ---\n"
fact_prompt:
    .asciz "  Enter positive integer for Factorial (0 to 12): "
fact_result_fmt:
    .asciz "  5. Factorial(%d) = %d (Computed recursively via 80386 stack frames)\n"
fib_prompt:
    .asciz "  Enter number of Fibonacci terms to generate (1 to 15): "
fib_result_hdr:
    .asciz "  6. Fibonacci Series (%d terms): [ "
fib_term_fmt:
    .asciz "%d "
fib_close_fmt:
    .asciz "]\n"

.bss
.lcomm fib_buffer, 64

.text

# ------------------------------------------------------------------------------
# asm_factorial: Computes n! recursively using standard stack frames
# Prototype: int asm_factorial(int n)
# Returns: eax = n!
# ------------------------------------------------------------------------------
.global _asm_factorial
_asm_factorial:
    push ebp
    mov ebp, esp
    push ebx

    mov ebx, [ebp + 8]          # ebx = n

    cmp ebx, 1
    jle .L_fact_base_case       # 0! = 1, 1! = 1

    dec ebx                     # n - 1
    push ebx
    call _asm_factorial         # recursive call: factorial(n - 1)
    add esp, 4

    mov ebx, [ebp + 8]          # restore original n
    imul eax, ebx               # eax = n * factorial(n - 1)
    jmp .L_fact_exit

.L_fact_base_case:
    mov eax, 1

.L_fact_exit:
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_fibonacci: Generates first n Fibonacci numbers into buffer
# Prototype: void asm_fibonacci(int* out_arr, int n)
# Series: F(0)=0, F(1)=1, F(2)=1, F(3)=2, F(4)=3, F(5)=5...
# ------------------------------------------------------------------------------
.global _asm_fibonacci
_asm_fibonacci:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov edi, [ebp + 8]          # edi = destination array pointer
    mov ecx, [ebp + 12]         # ecx = n terms

    cmp ecx, 0
    jle .L_fib_exit

    # First term: 0
    mov dword ptr [edi], 0
    cmp ecx, 1
    je .L_fib_exit

    # Second term: 1
    mov dword ptr [edi + 4], 1
    cmp ecx, 2
    je .L_fib_exit

    # Iterative calculation for term 3 onwards
    sub ecx, 2                  # remaining terms
    mov eax, 0                  # F(i-2)
    mov ebx, 1                  # F(i-1)
    add edi, 8                  # point to &out_arr[2]

.L_fib_loop:
    mov edx, eax
    add edx, ebx                # F(i) = F(i-1) + F(i-2)
    mov [edi], edx

    mov eax, ebx                # advance F(i-2) = F(i-1)
    mov ebx, edx                # advance F(i-1) = F(i)

    add edi, 4
    dec ecx
    jnz .L_fib_loop

.L_fib_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co2_demo_math: Interactive demonstration for Factorial and Fibonacci
# ------------------------------------------------------------------------------
.global _co2_demo_math
_co2_demo_math:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset math_demo_title
    call _print_str
    add esp, 4

    # 5. Factorial
    push offset fact_prompt
    call _print_str
    add esp, 4
    call _read_int
    mov ebx, eax                # ebx = n

    cmp ebx, 0
    jl .L_fact_default
    cmp ebx, 12
    jg .L_fact_default
    jmp .L_fact_run

.L_fact_default:
    mov ebx, 5                  # default to 5 if out of range

.L_fact_run:
    push ebx
    call _asm_factorial
    add esp, 4

    push eax
    push ebx
    push offset fact_result_fmt
    call _printf
    add esp, 12

    # 6. Fibonacci
    push offset fib_prompt
    call _print_str
    add esp, 4
    call _read_int
    mov esi, eax                # esi = terms

    cmp esi, 1
    jl .L_fib_default
    cmp esi, 15
    jg .L_fib_default
    jmp .L_fib_run

.L_fib_default:
    mov esi, 8                  # default to 8 terms

.L_fib_run:
    push esi
    push offset fib_buffer
    call _asm_fibonacci
    add esp, 8

    push esi
    push offset fib_result_hdr
    call _printf
    add esp, 8

    xor ebx, ebx
.L_print_fib_loop:
    cmp ebx, esi
    jge .L_print_fib_done
    push [fib_buffer + ebx*4]
    push offset fib_term_fmt
    call _printf
    add esp, 8
    inc ebx
    jmp .L_print_fib_loop

.L_print_fib_done:
    push offset fib_close_fmt
    call _print_str
    add esp, 4

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

