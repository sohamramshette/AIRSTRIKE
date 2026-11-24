# ==============================================================================
# File    : COA/src/co2_assembly_programs/logic_ops.s
# Subject : COA-CO2 | Program 10: Bitwise Logic Operations
# Topics  : Bitwise AND, OR, XOR, NOT, Spacecraft Subsystem Status Bitmasks
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
logic_demo_title:
    .asciz "\n--- CO2 Program 10: 64-Bit Bitwise AND, OR, XOR, NOT Operations ---\n"
logic_init_msg: .asciz "  Status Reg (A) : 0x%016llX ("
logic_mask_msg: .asciz ")\n  Mask       (B) : 0x%016llX ("
logic_and_msg:  .asciz ")\n  [AND]  A & B     : 0x%016llX ("
logic_or_msg:   .asciz ")\n  [OR]   A | B     : 0x%016llX ("
logic_xor_msg:  .asciz ")\n  [XOR]  A ^ B     : 0x%016llX ("
logic_not_msg:  .asciz ")\n  [NOT]  ~A        : 0x%016llX ("
close_paren_nl: .asciz ")\n"

.text

# ------------------------------------------------------------------------------
# [TOPIC: Bitwise AND (Query Bitmask)] asm_bitwise_and
# Purpose   : Filters active subsystem bits (a & b).
# Game Role : Checks whether specific ship systems (Shields, Cloak, Thrusters) are ON.
# Inputs    : RCX = a, RDX = b
# Output    : RAX = a & b
# ------------------------------------------------------------------------------
.global asm_bitwise_and
.global _asm_bitwise_and
asm_bitwise_and:
_asm_bitwise_and:
    mov  rax, rcx
    and  rax, rdx               # WHY: Bitwise AND clears unselected bits so we can test specific ship flags
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Bitwise OR (Enable Bits)] asm_bitwise_or
# Purpose   : Turns on designated subsystem flags (a | b).
# Game Role : Activates power-ups (e.g., Hyperdrive/Weapons) without turning off others.
# Inputs    : RCX = a, RDX = b
# Output    : RAX = a | b
# ------------------------------------------------------------------------------
.global asm_bitwise_or
.global _asm_bitwise_or
asm_bitwise_or:
_asm_bitwise_or:
    mov  rax, rcx
    or   rax, rdx               # WHY: Bitwise OR forces target bits to 1 while preserving all unrelated subsystems
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Bitwise XOR (Toggle Bits)] asm_bitwise_xor
# Purpose   : Flips specific subsystem flags (a ^ b).
# Game Role : Toggles stealth cloak or shield polarity ON/OFF in a single instruction.
# Inputs    : RCX = a, RDX = b
# Output    : RAX = a ^ b
# ------------------------------------------------------------------------------
.global asm_bitwise_xor
.global _asm_bitwise_xor
asm_bitwise_xor:
_asm_bitwise_xor:
    mov  rax, rcx
    xor  rax, rdx               # WHY: Bitwise XOR inverts bits where mask=1 (0->1, 1->0) for instant state toggling
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Bitwise NOT (Invert Bits)] asm_bitwise_not
# Purpose   : Computes one's complement bitwise inversion (~a).
# Game Role : Flips active system mask into an inverse disable/EMP jam mask.
# Input     : RCX = a
# Output    : RAX = ~a
# ------------------------------------------------------------------------------
.global asm_bitwise_not
.global _asm_bitwise_not
asm_bitwise_not:
_asm_bitwise_not:
    mov  rax, rcx
    not  rax                    # WHY: One's complement flips every bit across all 64 subsystem flags at once
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Bitwise Logic Demo] co2_demo_logic
# Purpose   : Displays hex and 64-bit binary results for AND, OR, XOR, NOT.
# Game Role : Visualizes how 64 ship system flags fit inside one CPU register.
# ------------------------------------------------------------------------------
.global co2_demo_logic
.global _co2_demo_logic
co2_demo_logic:
_co2_demo_logic:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push rdi
    sub  rsp, 40

    lea  rcx, [rip + logic_demo_title]
    call print_str

    mov  rbx, 0x8000000100000053  # Ship status register A (Uplink, Beacon, Shields, Cloak, Boost)
    mov  rsi, 0x800000000000003A  # Operation mask B

    lea  rcx, [rip + logic_init_msg]
    mov  rdx, rbx
    call printf
    mov  rcx, rbx
    call print_bin64

    lea  rcx, [rip + logic_mask_msg]
    mov  rdx, rsi
    call printf
    mov  rcx, rsi
    call print_bin64

    mov  rcx, rbx
    mov  rdx, rsi
    call asm_bitwise_and
    mov  rdi, rax
    lea  rcx, [rip + logic_and_msg]
    mov  rdx, rdi
    call printf
    mov  rcx, rdi
    call print_bin64

    mov  rcx, rbx
    mov  rdx, rsi
    call asm_bitwise_or
    mov  rdi, rax
    lea  rcx, [rip + logic_or_msg]
    mov  rdx, rdi
    call printf
    mov  rcx, rdi
    call print_bin64

    mov  rcx, rbx
    mov  rdx, rsi
    call asm_bitwise_xor
    mov  rdi, rax
    lea  rcx, [rip + logic_xor_msg]
    mov  rdx, rdi
    call printf
    mov  rcx, rdi
    call print_bin64

    mov  rcx, rbx
    call asm_bitwise_not
    mov  rdi, rax
    lea  rcx, [rip + logic_not_msg]
    mov  rdx, rdi
    call printf
    mov  rcx, rdi
    call print_bin64

    lea  rcx, [rip + close_paren_nl]
    call print_str

    add  rsp, 40
    pop  rdi
    pop  rsi
    pop  rbx
    pop  rbp
    ret
