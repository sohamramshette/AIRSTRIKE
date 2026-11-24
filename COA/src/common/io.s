# ==============================================================================
# File    : COA/src/common/io.s
# Subject : COA Common I/O Support Library
# Topics  : Console Print, Hexadecimal, Binary Output, Integer Input (x64 ABI)
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
fmt_str:          .asciz "%s"
fmt_int64:        .asciz "%lld"
fmt_uint64:       .asciz "%llu"
fmt_hex8:         .asciz "0x%02llX"
fmt_hex64:        .asciz "0x%016llX"
fmt_nl:           .asciz "\n"
fmt_prompt_pause: .asciz "\n[Press ENTER to continue...]"

.bss
.align 8
.lcomm input_val_64, 8

.text

# ------------------------------------------------------------------------------
# [TOPIC: Console String Output] print_str
# Purpose   : Prints a null-terminated string to stdout.
# Game Role : Displays COA mission menus, battle logs, and enemy status banners.
# Input     : RCX = const char* str
# ------------------------------------------------------------------------------
.global print_str
.global _print_str
print_str:
_print_str:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32                # WHY: Reserve 32-byte Windows x64 shadow space required before calling C functions

    mov  rdx, rcx               # WHY: Shift string pointer to Arg2 (RDX) because printf needs format string in Arg1
    lea  rcx, [rip + fmt_str]   # WHY: Load "%s" via RIP-relative addressing to prevent format-string bugs & stay PIC
    call printf

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Signed Integer Output] print_int
# Purpose   : Prints a 64-bit signed integer (%lld).
# Game Role : Outputs player score deltas, negative damage values, and HP balance.
# Input     : RCX = int64_t val
# ------------------------------------------------------------------------------
.global print_int
.global _print_int
print_int:
_print_int:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32                # WHY: Reserve Windows x64 shadow space & keep stack 16-byte aligned

    mov  rdx, rcx               # WHY: Move integer into Arg2 (RDX) for printf("%lld", val)
    lea  rcx, [rip + fmt_int64] # WHY: Load 64-bit signed format specifier
    call printf

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Unsigned Integer Output] print_uint
# Purpose   : Prints a 64-bit unsigned integer (%llu).
# Game Role : Outputs cycle counters, memory addresses, and unsigned shield totals.
# Input     : RCX = uint64_t val
# ------------------------------------------------------------------------------
.global print_uint
.global _print_uint
print_uint:
_print_uint:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    mov  rdx, rcx
    lea  rcx, [rip + fmt_uint64] # WHY: Unsigned %llu prevents large values from printing as negative
    call printf

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Hexadecimal Output] print_hex64
# Purpose   : Prints a 64-bit value in 16-digit hexadecimal (0x...).
# Game Role : Shows 64-bit ship status masks and CPU register snapshots.
# Input     : RCX = uint64_t val
# ------------------------------------------------------------------------------
.global print_hex64
.global _print_hex64
print_hex64:
_print_hex64:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    mov  rdx, rcx
    lea  rcx, [rip + fmt_hex64] # WHY: %016llX pads with leading zeros so all 64 bits align visually
    call printf

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Hexadecimal Byte Output] print_hex8
# Purpose   : Prints the lowest 8 bits in 2-digit hexadecimal (0x..).
# Game Role : Inspects Aegis Sentry 8-bit shield bytes (AH / AL).
# Input     : CL = uint8_t val
# ------------------------------------------------------------------------------
.global print_hex8
.global _print_hex8
print_hex8:
_print_hex8:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    movzx rdx, cl               # WHY: Zero-extend 8-bit CL -> 64-bit RDX so upper garbage bits don't corrupt hex output
    lea  rcx, [rip + fmt_hex8]
    call printf

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Binary Output] print_bin8
# Purpose   : Prints an 8-bit value in binary (MSB to LSB) using SHL + Carry Flag.
# Game Role : Draws live 8-bit binary registers (A, Q) in Booth & Division tables.
# Input     : CL = uint8_t val
# ------------------------------------------------------------------------------
.global print_bin8
.global _print_bin8
print_bin8:
_print_bin8:
    push rbp
    mov  rbp, rsp
    push rbx                    # WHY: Preserve non-volatile RBX across putchar calls
    push rsi                    # WHY: Preserve RSI (2 pushes = 16 bytes -> keeps RSP aligned)
    sub  rsp, 32

    mov  bl, cl                 # WHY: Save 8-bit value in callee-saved BL because putchar overwrites RCX
    mov  rsi, 8                 # WHY: Loop 8 times to print bit 7 down to bit 0

.L_bin8_loop:
    shl  bl, 1                  # WHY: Shifts top bit into Carry Flag (CF) so we can test it with jc (no mask needed)
    jc   .L_bin8_one            # WHY: Branch to print '1' if CF=1, else fall through to print '0'
    mov  ecx, '0'
    call putchar
    jmp  .L_bin8_next

.L_bin8_one:
    mov  ecx, '1'
    call putchar

.L_bin8_next:
    dec  rsi                    # WHY: Count down remaining bits and set ZF=1 when all 8 bits are printed
    jnz  .L_bin8_loop

    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Binary Output] print_bin64
# Purpose   : Prints a 64-bit value in binary with a space every 8 bits.
# Game Role : Visualizes all 64 spacecraft subsystem status flags bit-by-bit.
# Input     : RCX = uint64_t val
# ------------------------------------------------------------------------------
.global print_bin64
.global _print_bin64
print_bin64:
_print_bin64:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 32

    mov  rbx, rcx               # WHY: Store 64-bit mask in callee-saved RBX
    mov  rsi, 64                # WHY: Print all 64 bits from MSB (bit 63) to LSB (bit 0)

.L_bin64_loop:
    shl  rbx, 1                 # WHY: Shift MSB into CF to inspect one bit per cycle
    jc   .L_bin64_one
    mov  ecx, '0'
    call putchar
    jmp  .L_bin64_sep

.L_bin64_one:
    mov  ecx, '1'
    call putchar

.L_bin64_sep:
    dec  rsi
    jz   .L_bin64_done          # WHY: Stop after bit 0 without printing a trailing space

    test rsi, 7                 # WHY: Fast bitwise check for (rsi % 8 == 0) to separate bytes visually
    jnz  .L_bin64_loop
    mov  ecx, ' '
    call putchar
    jmp  .L_bin64_loop

.L_bin64_done:
    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Newline Output] print_nl
# Purpose   : Prints a single newline character.
# Game Role : Formats console telemetry tables cleanly.
# ------------------------------------------------------------------------------
.global print_nl
.global _print_nl
print_nl:
_print_nl:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    lea  rcx, [rip + fmt_nl]
    call printf

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Integer Input] read_int
# Purpose   : Reads a 64-bit signed integer from stdin and flushes trailing chars.
# Game Role : Captures player menu choices and custom math inputs safely.
# Output    : RAX = integer read, or -1 on invalid input / EOF
# ------------------------------------------------------------------------------
.global read_int
.global _read_int
read_int:
_read_int:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    lea  rcx, [rip + fmt_int64]    # WHY: Arg1 = "%lld" format
    lea  rdx, [rip + input_val_64] # WHY: Arg2 = address in .bss where scanf stores user's number
    call scanf

    cmp  eax, 1                 # WHY: scanf returns 1 only when a valid number was matched
    je   .L_read_flush

    cmp  eax, -1                # WHY: Check for EOF (-1) so piped/batch runs exit instead of looping forever
    je   .L_read_eof
.L_bad_flush:
    call getchar                # WHY: Drain invalid non-numeric input up to newline so menu won't infinite-loop
    cmp  eax, 10
    je   .L_read_eof
    cmp  eax, -1
    je   .L_read_eof
    jmp  .L_bad_flush

.L_read_eof:
    mov  rax, -1                # WHY: Signal invalid/EOF to caller
    jmp  .L_read_exit

.L_read_flush:
    call getchar                # WHY: Consume leftover '\n' so later pause_key() calls actually wait
    cmp  eax, 10
    je   .L_read_ok
    cmp  eax, -1
    je   .L_read_ok
    jmp  .L_read_flush

.L_read_ok:
    mov  rax, [rip + input_val_64] # WHY: Load scanned 64-bit integer into RAX return register

.L_read_exit:
    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Console Pause] pause_key
# Purpose   : Prompts user to press ENTER before returning to menu.
# Game Role : Lets players inspect combat telemetry before screen clears/scrolls.
# ------------------------------------------------------------------------------
.global pause_key
.global _pause_key
pause_key:
_pause_key:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    lea  rcx, [rip + fmt_prompt_pause]
    call printf
    call getchar                # WHY: Block until user presses ENTER

    add  rsp, 32
    pop  rbp
    ret
