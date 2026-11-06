# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO2: Bitwise Logic Operations
# Implements:
#   10. Bitwise AND, OR, XOR, NOT on Spacecraft Status Bitmasks
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
logic_demo_title:
    .asciz "\n--- CO2 Program 10: Bitwise AND, OR, XOR, and NOT Operations ---\n"
logic_init_msg:
    .asciz "  Initial Spacecraft Status Register (A): 0x%02X ("
logic_mask_msg:
    .asciz ")\n  Operation Mask (B)                    : 0x%02X ("
logic_and_msg:
    .asciz ")\n  [Bitwise AND] A & B (Status Query)    : 0x%02X ("
logic_or_msg:
    .asciz ")\n  [Bitwise OR]  A | B (Enable Systems)   : 0x%02X ("
logic_xor_msg:
    .asciz ")\n  [Bitwise XOR] A ^ B (Toggle Systems)   : 0x%02X ("
logic_not_msg:
    .asciz ")\n  [Bitwise NOT] ~A    (Invert Status)    : 0x%02X ("
close_paren_nl:
    .asciz ")\n"

# Bit definitions for AirStrike Telemetry
# Bit 0 = Shields (0x01)
# Bit 1 = Engines Boost (0x02)
# Bit 2 = Weapons Armed (0x04)
# Bit 3 = Cloak Active (0x08)
# Bit 4 = Hyperdrive (0x10)
# Bit 5 = Targeting Radar (0x20)
# Bit 6 = EMP Charged (0x40)
# Bit 7 = Hull Alert (0x80)

.text

# ------------------------------------------------------------------------------
# asm_bitwise_and: Computes a & b
# Prototype: int asm_bitwise_and(int a, int b)
# ------------------------------------------------------------------------------
.global _asm_bitwise_and
_asm_bitwise_and:
    push ebp
    mov ebp, esp
    mov eax, [ebp + 8]
    and eax, [ebp + 12]
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_bitwise_or: Computes a | b
# Prototype: int asm_bitwise_or(int a, int b)
# ------------------------------------------------------------------------------
.global _asm_bitwise_or
_asm_bitwise_or:
    push ebp
    mov ebp, esp
    mov eax, [ebp + 8]
    or  eax, [ebp + 12]
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_bitwise_xor: Computes a ^ b
# Prototype: int asm_bitwise_xor(int a, int b)
# ------------------------------------------------------------------------------
.global _asm_bitwise_xor
_asm_bitwise_xor:
    push ebp
    mov ebp, esp
    mov eax, [ebp + 8]
    xor eax, [ebp + 12]
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_bitwise_not: Computes ~a
# Prototype: int asm_bitwise_not(int a)
# ------------------------------------------------------------------------------
.global _asm_bitwise_not
_asm_bitwise_not:
    push ebp
    mov ebp, esp
    mov eax, [ebp + 8]
    not eax
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co2_demo_logic: Visual bitwise demonstration with binary printing
# ------------------------------------------------------------------------------
.global _co2_demo_logic
_co2_demo_logic:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset logic_demo_title
    call _print_str
    add esp, 4

    # Status A = 0x53 (0101 0011: Shields, Boost, Cloak, Radar)
    # Mask   B = 0x3A (0011 1010)
    mov ebx, 0x53
    mov esi, 0x3A

    # Display Initial A
    push ebx
    push offset logic_init_msg
    call _printf
    add esp, 8
    push ebx
    call _print_bin8
    add esp, 4

    # Display Mask B
    push esi
    push offset logic_mask_msg
    call _printf
    add esp, 8
    push esi
    call _print_bin8
    add esp, 4

    # Bitwise AND
    mov eax, ebx
    and eax, esi
    mov edi, eax
    push edi
    push offset logic_and_msg
    call _printf
    add esp, 8
    push edi
    call _print_bin8
    add esp, 4

    # Bitwise OR
    mov eax, ebx
    or  eax, esi
    mov edi, eax
    push edi
    push offset logic_or_msg
    call _printf
    add esp, 8
    push edi
    call _print_bin8
    add esp, 4

    # Bitwise XOR
    mov eax, ebx
    xor eax, esi
    mov edi, eax
    push edi
    push offset logic_xor_msg
    call _printf
    add esp, 8
    push edi
    call _print_bin8
    add esp, 4

    # Bitwise NOT (mask to 8-bit)
    mov eax, ebx
    not eax
    and eax, 0xFF
    mov edi, eax
    push edi
    push offset logic_not_msg
    call _printf
    add esp, 8
    push edi
    call _print_bin8
    add esp, 4

    push offset close_paren_nl
    call _print_str
    add esp, 4

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

