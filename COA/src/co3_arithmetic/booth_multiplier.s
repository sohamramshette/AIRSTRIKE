# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO3: Booth's Multiplication Algorithm
# Implements Signed 2's Complement Binary Multiplication from First Principles
# Does NOT use processor MUL or IMUL instructions for the multiplication logic.
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
booth_hdr:
    .asciz "\n==============================================================================\n            CO3: BOOTH'S SIGNED BINARY MULTIPLICATION ALGORITHM                \n==============================================================================\n"
booth_inputs_fmt:
    .asciz "  Multiplicand (M) = %d (0x%02X | Binary: "
booth_inputs_q:
    .asciz ")\n  Multiplier   (Q) = %d (0x%02X | Binary: "
booth_table_hdr:
    .asciz ")\n\n  +-------+-------------------+----------+----------+-----+----------------+\n  | Cycle | Operation         |    A     |    Q     | Q-1 | Action         |\n  +-------+-------------------+----------+----------+-----+----------------+\n"
booth_row_fmt:
    .asciz "  |   %d   | %-17s | "
booth_row_mid:
    .asciz " | "
booth_row_end:
    .asciz " |  %d  | %-14s |\n"
booth_table_footer:
    .asciz "  +-------+-------------------+----------+----------+-----+----------------+\n"
booth_final_fmt:
    .asciz "  Final Combined Product (A:Q) = 0x%04X (%d in decimal)\n  Theoretical Verification     : %d * %d = %d -> %s\n"
str_pass:
    .asciz "MATCH (PASS)"
str_fail:
    .asciz "MISMATCH (FAIL)"

str_init:
    .asciz "Initial State"
str_add_ashr:
    .asciz "A = A + M, ASHR"
str_sub_ashr:
    .asciz "A = A - M, ASHR"
str_ashr_only:
    .asciz "ASHR (No Add/Sub)"
str_action_add:
    .asciz "Q0,Q-1 = 01"
str_action_sub:
    .asciz "Q0,Q-1 = 10"
str_action_shift:
    .asciz "Q0,Q-1 = 00/11"

prompt_booth_m:
    .asciz "  Enter signed Multiplicand M (-128 to 127): "
prompt_booth_q:
    .asciz "  Enter signed Multiplier   Q (-128 to 127): "

.bss
.lcomm booth_m_val, 4
.lcomm booth_q_val, 4

.text

# ------------------------------------------------------------------------------
# asm_booth_multiply: Executes 8-bit Booth's Multiplication Algorithm
# Prototype: int asm_booth_multiply(int M, int Q, int verbose)
# Parameters:
#   [ebp + 8]  = Multiplicand M (8-bit signed)
#   [ebp + 12] = Multiplier Q (8-bit signed)
#   [ebp + 16] = verbose flag (1 = print table, 0 = silent/test mode)
# Returns:
#   eax = 16-bit signed product (A:Q)
# ------------------------------------------------------------------------------
.global _asm_booth_multiply
_asm_booth_multiply:
    push ebp
    mov ebp, esp
    sub esp, 24                 # 24 bytes local stack storage
    push ebx
    push esi
    push edi

    # Stack layout:
    # [ebp - 4]  = Accumulator A (signed 32-bit, initially 0)
    # [ebp - 8]  = Multiplier Q (32-bit, low 8 bits valid)
    # [ebp - 12] = Q_prev (Q-1, 0 or 1)
    # [ebp - 16] = Cycle index (1..8)
    # [ebp - 20] = op_str pointer
    # [ebp - 24] = action_str pointer

    mov dword ptr [ebp - 4], 0      # A = 0
    mov eax, [ebp + 12]
    and eax, 0xFF
    mov [ebp - 8], eax              # Q = Q & 0xFF
    mov dword ptr [ebp - 12], 0     # Q-1 = 0
    mov dword ptr [ebp - 16], 1     # cycle = 1

    mov esi, [ebp + 16]             # verbose flag
    test esi, esi
    jz .L_booth_loop

    # Verbose header & initial row
    push offset booth_hdr
    call _print_str
    add esp, 4

    movsx eax, byte ptr [ebp + 8]
    push eax
    push eax
    push offset booth_inputs_fmt
    call _printf
    add esp, 12

    movzx eax, byte ptr [ebp + 8]
    push eax
    call _print_bin8
    add esp, 4

    movsx eax, byte ptr [ebp + 12]
    push eax
    push eax
    push offset booth_inputs_q
    call _printf
    add esp, 12

    movzx eax, byte ptr [ebp + 12]
    push eax
    call _print_bin8
    add esp, 4

    push offset booth_table_hdr
    call _print_str
    add esp, 4

    # Print Cycle 0 Initial row
    push offset str_init
    push 0
    push offset booth_row_fmt
    call _printf
    add esp, 12

    push 0
    call _print_bin8
    add esp, 4

    push offset booth_row_mid
    call _print_str
    add esp, 4

    push [ebp - 8]
    call _print_bin8
    add esp, 4

    push offset str_init
    push 0
    push offset booth_row_end
    call _printf
    add esp, 12

.L_booth_loop:
    # 1. Examine Q0 and Q-1
    mov eax, [ebp - 8]
    and eax, 1                      # eax = Q0
    shl eax, 1                      # Q0 << 1
    or  eax, [ebp - 12]             # (Q0 << 1) | Q-1

    cmp eax, 1                      # 01 -> A = A + M
    je .L_booth_op_add
    cmp eax, 2                      # 10 -> A = A - M
    je .L_booth_op_sub

    # 00 or 11 -> No arithmetic, ASHR only
    mov dword ptr [ebp - 20], offset str_ashr_only
    mov dword ptr [ebp - 24], offset str_action_shift
    jmp .L_booth_do_shift

.L_booth_op_add:
    movsx edx, byte ptr [ebp + 8]   # edx = M
    mov eax, [ebp - 4]
    add eax, edx
    movsx eax, al                   # keep 8-bit signed
    mov [ebp - 4], eax
    mov dword ptr [ebp - 20], offset str_add_ashr
    mov dword ptr [ebp - 24], offset str_action_add
    jmp .L_booth_do_shift

.L_booth_op_sub:
    movsx edx, byte ptr [ebp + 8]   # edx = M
    mov eax, [ebp - 4]
    sub eax, edx
    movsx eax, al                   # keep 8-bit signed
    mov [ebp - 4], eax
    mov dword ptr [ebp - 20], offset str_sub_ashr
    mov dword ptr [ebp - 24], offset str_action_sub

.L_booth_do_shift:
    # 2. Arithmetic Right Shift of [A : Q : Q-1]
    # Update Q-1 = LSB of Q
    mov eax, [ebp - 8]
    and eax, 1
    mov [ebp - 12], eax

    # Shift Q right by 1, MSB takes LSB of A
    mov eax, [ebp - 4]
    and eax, 1                      # LSB of A
    shl eax, 7                      # into bit 7
    mov edx, [ebp - 8]
    shr edx, 1
    and edx, 0x7F
    or  edx, eax
    mov [ebp - 8], edx

    # Arithmetic shift right A by 1 (preserving sign)
    mov al, byte ptr [ebp - 4]
    sar al, 1
    movsx eax, al
    mov [ebp - 4], eax

    # 3. Print row if verbose
    test esi, esi
    jz .L_booth_loop_next

    push [ebp - 20]                 # op_str
    push [ebp - 16]                 # cycle
    push offset booth_row_fmt
    call _printf
    add esp, 12

    movzx eax, byte ptr [ebp - 4]
    push eax
    call _print_bin8
    add esp, 4

    push offset booth_row_mid
    call _print_str
    add esp, 4

    movzx eax, byte ptr [ebp - 8]
    push eax
    call _print_bin8
    add esp, 4

    push [ebp - 24]                 # action_str
    push [ebp - 12]                 # Q-1
    push offset booth_row_end
    call _printf
    add esp, 12

.L_booth_loop_next:
    inc dword ptr [ebp - 16]
    cmp dword ptr [ebp - 16], 8
    jle .L_booth_loop

    # Final combined 16-bit signed product in (A << 8) | (Q & 0xFF)
    mov eax, [ebp - 4]
    and eax, 0xFF
    shl eax, 8
    mov edx, [ebp - 8]
    and edx, 0xFF
    or  eax, edx
    movsx eax, ax                   # eax = signed 16-bit integer

    test esi, esi
    jz .L_booth_exit

    mov ebx, eax                    # preserve product in ebx

    push offset booth_table_footer
    call _print_str
    add esp, 4

    # Calculate expected product = M * Q
    movsx ecx, byte ptr [ebp + 8]
    movsx edx, byte ptr [ebp + 12]
    imul ecx, edx                   # ecx = expected

    mov edi, offset str_pass
    cmp ebx, ecx
    je .L_booth_print_res
    mov edi, offset str_fail

.L_booth_print_res:
    push edi                        # pass/fail string
    push ecx                        # expected product
    movsx edx, byte ptr [ebp + 12]
    push edx                        # Q
    movsx edx, byte ptr [ebp + 8]
    push edx                        # M
    push ebx                        # computed product
    movzx edx, bx
    push edx                        # hex product
    push offset booth_final_fmt
    call _printf
    add esp, 28

    mov eax, ebx

.L_booth_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co3_demo_booth: Interactive runner for Booth's Algorithm
# ------------------------------------------------------------------------------
.global _co3_demo_booth
_co3_demo_booth:
    push ebp
    mov ebp, esp
    push ebx
    push esi

    push offset prompt_booth_m
    call _print_str
    add esp, 4
    call _read_int
    mov ebx, eax

    push offset prompt_booth_q
    call _print_str
    add esp, 4
    call _read_int
    mov esi, eax

    # Run Booth with verbose = 1
    push 1                      # verbose
    push esi                    # Q
    push ebx                    # M
    call _asm_booth_multiply
    add esp, 12

    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
