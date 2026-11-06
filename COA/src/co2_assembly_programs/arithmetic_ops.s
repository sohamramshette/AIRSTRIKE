# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO2: Arithmetic Programs
# Implements:
#   1. Addition and Subtraction
#   2. Multiplication and Division (with Quotient & Remainder)
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
add_sub_title:
    .asciz "\n--- CO2 Program 1: Addition & Subtraction ---\n"
add_sub_fmt:
    .asciz "  Operands: A = %d, B = %d\n  Addition (A + B)    = %d\n  Subtraction (A - B) = %d\n"

mul_div_title:
    .asciz "\n--- CO2 Program 2: Multiplication & Division ---\n"
mul_div_fmt:
    .asciz "  Operands: Dividend/Multiplicand A = %d, Divisor/Multiplier B = %d\n  Multiplication (A * B) = %d\n  Division (A / B)       = %d (Quotient: %d, Remainder: %d)\n"
div_by_zero_msg:
    .asciz "  [ERROR] Cannot divide by zero!\n"

prompt_enter_a:
    .asciz "  Enter first integer (A): "
prompt_enter_b:
    .asciz "  Enter second integer (B): "

.bss
.lcomm rem_out, 4

.text

# ------------------------------------------------------------------------------
# asm_add: Computes A + B
# Prototype: int asm_add(int a, int b)
# ------------------------------------------------------------------------------
.global _asm_add
_asm_add:
    push ebp
    mov ebp, esp

    mov eax, [ebp + 8]          # eax = a
    add eax, [ebp + 12]         # eax = a + b

    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_sub: Computes A - B
# Prototype: int asm_sub(int a, int b)
# ------------------------------------------------------------------------------
.global _asm_sub
_asm_sub:
    push ebp
    mov ebp, esp

    mov eax, [ebp + 8]          # eax = a
    sub eax, [ebp + 12]         # eax = a - b

    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_mul: Computes A * B (Signed 32-bit multiplication)
# Prototype: int asm_mul(int a, int b)
# ------------------------------------------------------------------------------
.global _asm_mul
_asm_mul:
    push ebp
    mov ebp, esp

    mov eax, [ebp + 8]          # eax = a
    imul eax, [ebp + 12]        # eax = a * b (80386 2-operand imul)

    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_div: Computes A / B and remainder
# Prototype: int asm_div(int dividend, int divisor, int* out_rem)
# Returns: eax = quotient, stores remainder in *out_rem
# ------------------------------------------------------------------------------
.global _asm_div
_asm_div:
    push ebp
    mov ebp, esp
    push ebx

    mov eax, [ebp + 8]          # eax = dividend
    mov ebx, [ebp + 12]         # ebx = divisor

    cmp ebx, 0
    je .L_div_zero

    cdq                         # Sign-extend EAX into EDX:EAX (80386 standard)
    idiv ebx                    # EAX = Quotient, EDX = Remainder

    mov ecx, [ebp + 16]         # ecx = out_rem pointer
    test ecx, ecx
    jz .L_div_done
    mov [ecx], edx              # *out_rem = remainder

    jmp .L_div_done

.L_div_zero:
    xor eax, eax
    xor edx, edx
    mov ecx, [ebp + 16]
    test ecx, ecx
    jz .L_div_done
    mov [ecx], edx

.L_div_done:
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co2_demo_arithmetic: Interactive/Preset demonstration for Add/Sub and Mul/Div
# ------------------------------------------------------------------------------
.global _co2_demo_arithmetic
_co2_demo_arithmetic:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    # Prompt user for inputs or use defaults if 0
    push offset add_sub_title
    call _print_str
    add esp, 4

    push offset prompt_enter_a
    call _print_str
    add esp, 4
    call _read_int
    mov ebx, eax                # ebx = A

    push offset prompt_enter_b
    call _print_str
    add esp, 4
    call _read_int
    mov esi, eax                # esi = B

    # 1. Addition and Subtraction
    push esi
    push ebx
    call _asm_sub
    add esp, 8
    mov edi, eax                # edi = A - B

    push esi
    push ebx
    call _asm_add
    add esp, 8                  # eax = A + B

    push edi
    push eax
    push esi
    push ebx
    push offset add_sub_fmt
    call _printf
    add esp, 20

    # 2. Multiplication and Division
    push offset mul_div_title
    call _print_str
    add esp, 4

    cmp esi, 0
    je .L_demo_div_zero

    push offset rem_out
    push esi
    push ebx
    call _asm_div
    add esp, 12
    mov edi, eax                # edi = Quotient

    push esi
    push ebx
    call _asm_mul
    add esp, 8                  # eax = Product

    mov edx, [rem_out]          # edx = Remainder
    push edx
    push edi
    push edi
    push eax
    push esi
    push ebx
    push offset mul_div_fmt
    call _printf
    add esp, 28
    jmp .L_demo_arith_exit

.L_demo_div_zero:
    push offset div_by_zero_msg
    call _print_str
    add esp, 4

.L_demo_arith_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

