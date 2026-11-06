# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - Common I/O Utilities
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: 32-bit Intel 80386 (cdecl calling convention)
# ==============================================================================

.intel_syntax noprefix

.data
fmt_str:
    .asciz "%s"
fmt_int:
    .asciz "%d"
fmt_uint:
    .asciz "%u"
fmt_hex8:
    .asciz "0x%02X"
fmt_hex16:
    .asciz "0x%04X"
fmt_hex32:
    .asciz "0x%08X"
fmt_nl:
    .asciz "\n"
fmt_prompt_pause:
    .asciz "\n[Press ENTER to continue...]"
bin_zero:
    .asciz "0"
bin_one:
    .asciz "1"

.bss
.lcomm input_buffer, 64
.lcomm input_val, 4

.text

# ------------------------------------------------------------------------------
# print_str: Prints null-terminated string
# Prototype: void print_str(const char* str)
# Parameter: [esp + 4] = pointer to string
# ------------------------------------------------------------------------------
.global _print_str
_print_str:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push [ebp + 8]
    push offset fmt_str
    call _printf
    add esp, 8

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# print_int: Prints signed 32-bit integer
# Prototype: void print_int(int val)
# Parameter: [esp + 4] = integer value
# ------------------------------------------------------------------------------
.global _print_int
_print_int:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push [ebp + 8]
    push offset fmt_int
    call _printf
    add esp, 8

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# print_uint: Prints unsigned 32-bit integer
# Prototype: void print_uint(unsigned int val)
# Parameter: [esp + 4] = unsigned integer value
# ------------------------------------------------------------------------------
.global _print_uint
_print_uint:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push [ebp + 8]
    push offset fmt_uint
    call _printf
    add esp, 8

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# print_hex32: Prints 32-bit hex value (0xXXXXXXXX)
# Prototype: void print_hex32(unsigned int val)
# Parameter: [esp + 4] = value
# ------------------------------------------------------------------------------
.global _print_hex32
_print_hex32:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push [ebp + 8]
    push offset fmt_hex32
    call _printf
    add esp, 8

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# print_hex8: Prints 8-bit hex value (0xXX)
# Prototype: void print_hex8(unsigned int val)
# Parameter: [esp + 4] = value
# ------------------------------------------------------------------------------
.global _print_hex8
_print_hex8:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov eax, [ebp + 8]
    and eax, 0xFF
    push eax
    push offset fmt_hex8
    call _printf
    add esp, 8

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# print_bin8: Prints 8-bit binary representation (e.g. 01011010)
# Prototype: void print_bin8(unsigned int val)
# Parameter: [esp + 4] = 8-bit value in low byte
# ------------------------------------------------------------------------------
.global _print_bin8
_print_bin8:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov bl, [ebp + 8]           # bl holds the 8-bit value
    mov esi, 8                  # 8 bit counter (using callee-saved esi)

.L_bin8_loop:
    shl bl, 1                   # shift MSB into Carry Flag (CF)
    jc .L_bin8_one
    push '0'
    call _putchar
    add esp, 4
    jmp .L_bin8_next

.L_bin8_one:
    push '1'
    call _putchar
    add esp, 4

.L_bin8_next:
    dec esi
    jnz .L_bin8_loop

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# print_bin16: Prints 16-bit binary representation with space between bytes
# Prototype: void print_bin16(unsigned int val)
# Parameter: [esp + 4] = 16-bit value in low word
# ------------------------------------------------------------------------------
.global _print_bin16
_print_bin16:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov bx, [ebp + 8]           # bx holds 16-bit value
    mov esi, 16                 # 16 bit counter (using callee-saved esi)

.L_bin16_loop:
    shl bx, 1                   # shift MSB into Carry Flag
    jc .L_bin16_one
    push '0'
    call _putchar
    add esp, 4
    jmp .L_bin16_next

.L_bin16_one:
    push '1'
    call _putchar
    add esp, 4

.L_bin16_next:
    cmp esi, 9                  # after printing high byte (8 bits, when esi was 9 going to 8), print space separator
    jne .L_bin16_skip_space
    push ' '
    call _putchar
    add esp, 4

.L_bin16_skip_space:
    dec esi
    jnz .L_bin16_loop

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# print_nl: Prints a single newline
# Prototype: void print_nl()
# ------------------------------------------------------------------------------
.global _print_nl
_print_nl:
    push ebp
    mov ebp, esp
    push offset fmt_nl
    call _printf
    add esp, 4
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# read_int: Reads a signed integer from stdin using scanf
# Prototype: int read_int()
# Returns: eax = integer value read
# ------------------------------------------------------------------------------
.global _read_int
_read_int:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset input_val
    push offset fmt_int
    call _scanf
    add esp, 8

    cmp eax, 1                  # Check if 1 integer was successfully matched
    je .L_read_valid

    # Failed or EOF reached
    mov dword ptr [input_val], -1
    mov eax, -1
    jmp .L_read_exit

.L_read_valid:
    # Consume trailing characters until newline or EOF
.L_flush_loop:
    call _getchar
    cmp eax, 10                 # '\n'
    je .L_flush_done
    cmp eax, -1                 # EOF
    je .L_flush_done
    jmp .L_flush_loop

.L_flush_done:
    mov eax, [input_val]

.L_read_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# pause_key: Displays prompt and waits for Enter key
# Prototype: void pause_key()
# ------------------------------------------------------------------------------
.global _pause_key
_pause_key:
    push ebp
    mov ebp, esp
    push offset fmt_prompt_pause
    call _printf
    add esp, 4

    call _getchar

    mov esp, ebp
    pop ebp
    ret
