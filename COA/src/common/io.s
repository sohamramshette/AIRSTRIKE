# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — COMMON I/O UTILITIES
# Implements Formatted String, Integer, Hexadecimal, and Binary Console Display
# Satisfies Course Outcome: Cross-Module Common Support Library
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
fmt_str:
    .asciz "%s"
fmt_int64:
    .asciz "%lld"
fmt_uint64:
    .asciz "%llu"
fmt_hex8:
    .asciz "0x%02llX"
fmt_hex16:
    .asciz "0x%04llX"
fmt_hex32:
    .asciz "0x%08llX"
fmt_hex64:
    .asciz "0x%016llX"
fmt_nl:
    .asciz "\n"
fmt_prompt_pause:
    .asciz "\n[Press ENTER to continue...]"
bin_zero:
    .asciz "0"
bin_one:
    .asciz "1"

.bss
.align 8
.lcomm input_buffer, 128
.lcomm input_val_64, 8

.text

# ==============================================================================
# Routine: print_str / _print_str
# - Small Meaning: Prints a null-terminated string to stdout.
# - Long Form    : Assembly 64-Bit Print String
# - Why We Used It: Fast console string output conforming to Microsoft x64 ABI.
# Calling Convention (Microsoft x64 ABI):
#   RCX = const char* str (String pointer)
# ==============================================================================
.global print_str
.global _print_str
print_str:
_print_str:
    push rbp
    mov rbp, rsp
    sub rsp, 32                 # 32 bytes caller shadow space
    # --------------------------------------------------------------------------
    # MOV RDX, RCX / LEA RCX, [RIP + FMT_STR]:
    # - Small Meaning: Shift string pointer into 2nd argument (RDX) and load format string
    # - Long Form    : Move Register C to D / Load Effective Address of Format String
    # - Why We Used It: printf expects format in RCX, string argument in RDX.
    # --------------------------------------------------------------------------
    mov rdx, rcx
    lea rcx, [rip + fmt_str]
    call printf
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: print_int / _print_int
# - Small Meaning: Prints signed 64-bit quadword integer.
# - Long Form    : Assembly 64-Bit Print Signed Integer
# - Why We Used It: Formats telemetry coordinates, score tallies, and health points.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t val
# ==============================================================================
.global print_int
.global _print_int
print_int:
_print_int:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov rdx, rcx
    lea rcx, [rip + fmt_int64]
    call printf
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: print_uint / _print_uint
# - Small Meaning: Prints unsigned 64-bit integer.
# - Long Form    : Assembly 64-Bit Print Unsigned Integer
# - Why We Used It: Formats clock cycle counts, memory addresses, and bitmasks.
# ==============================================================================
.global print_uint
.global _print_uint
print_uint:
_print_uint:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov rdx, rcx
    lea rcx, [rip + fmt_uint64]
    call printf
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: print_hex64 / _print_hex64
# - Small Meaning: Prints 64-bit value in hexadecimal notation (0x%016llX).
# - Long Form    : Assembly 64-Bit Print Hexadecimal Quadword
# - Why We Used It: Displays 64-bit pointer registers (RAX, RSP, RIP) in clean hex.
# ==============================================================================
.global print_hex64
.global _print_hex64
print_hex64:
_print_hex64:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov rdx, rcx
    lea rcx, [rip + fmt_hex64]
    call printf
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: print_hex8 / _print_hex8
# - Small Meaning: Prints 8-bit low-byte in hex (0x%02X).
# - Long Form    : Assembly 64-Bit Print Hexadecimal Byte
# - Why We Used It: Inspects sub-register byte values (AL, BL, CL, DL, R8B).
# ==============================================================================
.global print_hex8
.global _print_hex8
print_hex8:
_print_hex8:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    movzx rdx, cl
    lea rcx, [rip + fmt_hex8]
    call printf
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: print_bin8 / _print_bin8
# - Small Meaning: Prints 8-bit binary string (e.g. 01011010).
# - Long Form    : Assembly 64-Bit Print Binary Byte
# - Why We Used It: Visualizes condition flags, Booth table columns, and opcode masks.
# Calling Convention (Microsoft x64 ABI):
#   RCX = uint64_t val (Low 8 bits printed)
# ==============================================================================
.global print_bin8
.global _print_bin8
print_bin8:
_print_bin8:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx
    push rsi

    mov bl, cl                  # BL holds the 8-bit value
    mov rsi, 8                  # 8-bit loop counter

.L_bin8_loop:
    # --------------------------------------------------------------------------
    # SHL BL, 1:
    # - Small Meaning: Shift Most Significant Bit into Carry Flag (CF)
    # - Long Form    : Shift Logical Left Byte by 1
    # - Why We Used It: Tests bits sequentially from MSB (bit 7) down to LSB (bit 0).
    # --------------------------------------------------------------------------
    shl bl, 1
    jc .L_bin8_one
    mov rcx, '0'
    call putchar
    jmp .L_bin8_next

.L_bin8_one:
    mov rcx, '1'
    call putchar

.L_bin8_next:
    dec rsi
    jnz .L_bin8_loop

    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: print_bin64 / _print_bin64
# - Small Meaning: Prints 64-bit binary string with space delimiters every 8 bits.
# - Long Form    : Assembly 64-Bit Print Binary Quadword
# - Why We Used It: Visualizes full 64-bit status registers and subsystem bitmasks.
# ==============================================================================
.global print_bin64
.global _print_bin64
print_bin64:
_print_bin64:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx
    push rsi

    mov rbx, rcx                # RBX holds the 64-bit value
    mov rsi, 64                 # 64-bit loop counter

.L_bin64_loop:
    shl rbx, 1                  # Shift MSB into Carry Flag
    jc .L_bin64_one
    mov rcx, '0'
    call putchar
    jmp .L_bin64_sep

.L_bin64_one:
    mov rcx, '1'
    call putchar

.L_bin64_sep:
    dec rsi
    jz .L_bin64_done

    # Print space separator every 8 bits
    mov rax, rsi
    test rax, 7
    jnz .L_bin64_loop

    mov rcx, ' '
    call putchar
    jmp .L_bin64_loop

.L_bin64_done:
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: print_nl / _print_nl
# - Small Meaning: Prints a single newline character to stdout.
# - Long Form    : Assembly 64-Bit Print Newline
# - Why We Used It: Flushes current line and advances cursor.
# ==============================================================================
.global print_nl
.global _print_nl
print_nl:
_print_nl:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    lea rcx, [rip + fmt_nl]
    call printf
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: read_int / _read_int
# - Small Meaning: Reads signed 64-bit integer from console stdin via scanf.
# - Long Form    : Assembly 64-Bit Read Console Integer
# - Why We Used It: Collects interactive menu choices and arithmetic operands.
# Returns:
#   RAX = 64-bit signed integer read, or -1 on EOF/error.
# ==============================================================================
.global read_int
.global _read_int
read_int:
_read_int:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx

    lea rcx, [rip + fmt_int64]
    lea rdx, [rip + input_val_64]
    call scanf

    cmp rax, 1
    je .L_read_valid

    # Failed or EOF reached
    mov qword ptr [rip + input_val_64], -1
    mov rax, -1
    jmp .L_read_exit

.L_read_valid:
    # Flush trailing characters in line buffer until newline or EOF
.L_flush_loop:
    call getchar
    cmp rax, 10                 # '\n'
    je .L_flush_done
    cmp rax, -1                 # EOF
    je .L_flush_done
    jmp .L_flush_loop

.L_flush_done:
    mov rax, [rip + input_val_64]

.L_read_exit:
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: pause_key / _pause_key
# - Small Meaning: Displays prompt and waits for user to press ENTER.
# - Long Form    : Assembly 64-Bit Console Pause Key Waiter
# - Why We Used It: Halts screen execution between demonstration modules.
# ==============================================================================
.global pause_key
.global _pause_key
pause_key:
_pause_key:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    lea rcx, [rip + fmt_prompt_pause]
    call printf
    call getchar
    mov rsp, rbp
    pop rbp
    ret
