# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO3: Restoring Division Algorithm
# Implements Unsigned Binary Restoring Division from First Principles
# Does NOT use processor DIV or IDIV instructions for division logic.
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
div_hdr:
    .asciz "\n==============================================================================\n                 CO3: RESTORING BINARY DIVISION ALGORITHM                      \n==============================================================================\n"
div_inputs_fmt:
    .asciz "  Dividend (Q) = %u (0x%02X | Binary: "
div_inputs_m:
    .asciz ")\n  Divisor  (M) = %u (0x%02X | Binary: "
div_table_hdr:
    .asciz ")\n\n  +-------+--------------------+----------+----------+----------------------------+\n  | Cycle | Operation          |    A     |    Q     | Action / Decision          |\n  +-------+--------------------+----------+----------+----------------------------+\n"
div_row_fmt:
    .asciz "  |   %d   | %-18s | "
div_row_mid:
    .asciz " | "
div_row_end:
    .asciz " | %-26s |\n"
div_table_footer:
    .asciz "  +-------+--------------------+----------+----------+----------------------------+\n"
div_final_fmt:
    .asciz "  Final Quotient  (Q) = %u (0x%02X)\n  Final Remainder (A) = %u (0x%02X)\n  Verification Formula: %u = (%u * %u) + %u -> %s\n"
div_err_zero:
    .asciz "  [ERROR] Restoring Division by Zero is undefined!\n"
str_pass:
    .asciz "MATCH (PASS)"
str_fail:
    .asciz "MISMATCH (FAIL)"

str_div_init:
    .asciz "Initial State"
str_div_restore:
    .asciz "A < 0: Restore A, Q0=0"
str_div_norestore:
    .asciz "A >= 0: Keep A, Q0=1"
str_shift_sub:
    .asciz "SHL(A,Q), A=A-M"

prompt_div_q:
    .asciz "  Enter unsigned Dividend Q (0 to 255): "
prompt_div_m:
    .asciz "  Enter unsigned Divisor  M (1 to 255): "

.bss
.lcomm div_out_rem, 4

.text

# ------------------------------------------------------------------------------
# asm_restoring_divide: Executes 8-bit Restoring Division Algorithm
# Prototype: int asm_restoring_divide(int dividend, int divisor, int* out_rem, int verbose)
# Parameters:
#   [ebp + 8]  = Dividend Q (8-bit unsigned: 0..255)
#   [ebp + 12] = Divisor  M (8-bit unsigned: 1..255)
#   [ebp + 16] = pointer to output remainder (int*)
#   [ebp + 20] = verbose flag (1 = print table, 0 = test mode)
# Returns:
#   eax = Quotient (Q)
#   *out_rem = Remainder (A)
# ------------------------------------------------------------------------------
.global _asm_restoring_divide
_asm_restoring_divide:
    push ebp
    mov ebp, esp
    sub esp, 24                 # 24 bytes local stack storage
    push ebx
    push esi
    push edi

    # Stack layout:
    # [ebp - 4]  = Accumulator A (signed 32-bit, initially 0)
    # [ebp - 8]  = Quotient Q (32-bit, initially Dividend & 0xFF)
    # [ebp - 12] = Divisor M (32-bit, Divisor & 0xFF)
    # [ebp - 16] = Cycle index (1..8)
    # [ebp - 20] = action_str pointer

    mov eax, [ebp + 12]
    and eax, 0xFF
    test eax, eax
    jnz .L_div_valid

    # Division by zero error
    push offset div_err_zero
    call _print_str
    add esp, 4
    xor eax, eax
    jmp .L_rest_div_exit

.L_div_valid:
    mov dword ptr [ebp - 4], 0      # A = 0
    mov eax, [ebp + 8]
    and eax, 0xFF
    mov [ebp - 8], eax              # Q = Dividend & 0xFF
    mov eax, [ebp + 12]
    and eax, 0xFF
    mov [ebp - 12], eax             # M = Divisor & 0xFF
    mov dword ptr [ebp - 16], 1     # cycle = 1

    mov esi, [ebp + 20]             # verbose flag
    test esi, esi
    jz .L_div_loop

    # Print Table Header
    push offset div_hdr
    call _print_str
    add esp, 4

    push [ebp - 8]
    push [ebp - 8]
    push offset div_inputs_fmt
    call _printf
    add esp, 12

    push [ebp - 8]
    call _print_bin8
    add esp, 4

    push [ebp - 12]
    push [ebp - 12]
    push offset div_inputs_m
    call _printf
    add esp, 12

    push [ebp - 12]
    call _print_bin8
    add esp, 4

    push offset div_table_hdr
    call _print_str
    add esp, 4

    # Print Initial Row 0
    push offset str_div_init
    push 0
    push offset div_row_fmt
    call _printf
    add esp, 12

    push 0
    call _print_bin8
    add esp, 4

    push offset div_row_mid
    call _print_str
    add esp, 4

    push [ebp - 8]
    call _print_bin8
    add esp, 4

    push offset str_div_init
    push offset div_row_end
    call _printf
    add esp, 8

.L_div_loop:
    # 1. Shift Left [A:Q] by 1 bit
    # Extract MSB of Q (bit 7)
    mov eax, [ebp - 8]
    shr eax, 7
    and eax, 1                      # eax = MSB of Q

    # Shift Q left by 1, mask to 8 bits
    shl dword ptr [ebp - 8], 1
    and dword ptr [ebp - 8], 0xFF

    # Shift A left by 1 and insert MSB of Q into bit 0
    shl dword ptr [ebp - 4], 1
    or  [ebp - 4], eax

    # 2. Subtract Divisor: A = A - M
    mov eax, [ebp - 12]
    sub [ebp - 4], eax

    # 3. Check Sign of A
    cmp dword ptr [ebp - 4], 0
    jl .L_div_negative

    # A >= 0: Keep A, set Q0 = 1
    or  dword ptr [ebp - 8], 1
    mov dword ptr [ebp - 20], offset str_div_norestore
    jmp .L_div_print_row

.L_div_negative:
    # A < 0: Restore A = A + M, Q0 stays 0
    mov eax, [ebp - 12]
    add [ebp - 4], eax
    mov dword ptr [ebp - 20], offset str_div_restore

.L_div_print_row:
    test esi, esi
    jz .L_div_loop_next

    push offset str_shift_sub
    push [ebp - 16]                 # cycle number
    push offset div_row_fmt
    call _printf
    add esp, 12

    movzx eax, byte ptr [ebp - 4]
    push eax
    call _print_bin8
    add esp, 4

    push offset div_row_mid
    call _print_str
    add esp, 4

    movzx eax, byte ptr [ebp - 8]
    push eax
    call _print_bin8
    add esp, 4

    push [ebp - 20]                 # action string
    push offset div_row_end
    call _printf
    add esp, 8

.L_div_loop_next:
    inc dword ptr [ebp - 16]
    cmp dword ptr [ebp - 16], 8
    jle .L_div_loop

    # Store Remainder if pointer not null
    mov ecx, [ebp + 16]
    test ecx, ecx
    jz .L_div_skip_rem_store
    mov eax, [ebp - 4]
    mov [ecx], eax

.L_div_skip_rem_store:
    mov ebx, [ebp - 8]              # ebx = Quotient

    test esi, esi
    jz .L_div_ret

    push offset div_table_footer
    call _print_str
    add esp, 4

    # Verification: Dividend == (Quotient * Divisor) + Remainder
    mov eax, ebx
    imul eax, [ebp - 12]
    add eax, [ebp - 4]              # eax = (Q * M) + A

    mov edi, offset str_pass
    cmp eax, [ebp + 8]
    je .L_div_verif_pass
    mov edi, offset str_fail

.L_div_verif_pass:
    push edi                        # match/mismatch string
    push [ebp - 4]                  # Remainder
    push [ebp - 12]                 # Divisor
    push ebx                        # Quotient
    push [ebp + 8]                  # Dividend
    push [ebp - 4]                  # Remainder hex
    push [ebp - 4]                  # Remainder dec
    push ebx                        # Quotient hex
    push ebx                        # Quotient dec
    push offset div_final_fmt
    call _printf
    add esp, 40

.L_div_ret:
    mov eax, ebx

.L_rest_div_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co3_demo_restoring_div: Interactive runner for Restoring Division
# ------------------------------------------------------------------------------
.global _co3_demo_restoring_div
_co3_demo_restoring_div:
    push ebp
    mov ebp, esp
    push ebx
    push esi

    push offset prompt_div_q
    call _print_str
    add esp, 4
    call _read_int
    mov ebx, eax

    push offset prompt_div_m
    call _print_str
    add esp, 4
    call _read_int
    mov esi, eax

    # Run Restoring Division with verbose = 1
    push 1                      # verbose
    push offset div_out_rem     # out_rem
    push esi                    # Divisor M
    push ebx                    # Dividend Q
    call _asm_restoring_divide
    add esp, 16

    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
