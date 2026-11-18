# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO2: BITWISE LOGIC OPERATIONS
# Implements:
#   Prog 10. Bitwise AND, OR, XOR, NOT on 64-bit Spacecraft Status Bitmasks
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
logic_demo_title:
    .asciz "\n--- CO2 Program 10: 64-Bit Bitwise AND, OR, XOR, and NOT Operations ---\n"
logic_init_msg:
    .asciz "  Initial Spacecraft Status Register (A): 0x%016llX ("
logic_mask_msg:
    .asciz ")\n  Operation Mask (B)                    : 0x%016llX ("
logic_and_msg:
    .asciz ")\n  [Bitwise AND] A & B (Status Query)    : 0x%016llX ("
logic_or_msg:
    .asciz ")\n  [Bitwise OR]  A | B (Enable Systems)   : 0x%016llX ("
logic_xor_msg:
    .asciz ")\n  [Bitwise XOR] A ^ B (Toggle Systems)   : 0x%016llX ("
logic_not_msg:
    .asciz ")\n  [Bitwise NOT] ~A    (Invert Status)    : 0x%016llX ("
close_paren_nl:
    .asciz ")\n"

# 64-bit AirStrike Spacecraft Subsystem Bitmask Layout:
# Bit 0  = Kinetic Shields        (0x0000000000000001)
# Bit 1  = Ion Thrusters Boost    (0x0000000000000002)
# Bit 2  = Plasma Cannons Armed   (0x0000000000000004)
# Bit 3  = Optical Cloak Active   (0x0000000000000008)
# Bit 4  = Hyperdrive Core Online (0x0000000000000010)
# Bit 5  = Long-Range Targeting   (0x0000000000000020)
# Bit 6  = EMP Capacitor Charged  (0x0000000000000040)
# Bit 7  = Hull Integrity Alert   (0x0000000000000080)
# Bit 32 = Sub-space Radio Beacon (0x0000000100000000)
# Bit 63 = Orbital Defense Uplink (0x8000000000000000)

.text

# ==============================================================================
# Routine: asm_bitwise_and
# - Small Meaning: Computes bitwise conjunction of two 64-bit operands (A & B).
# - Long Form    : Assembly 64-Bit Bitwise Logical AND
# - Why We Used It: Filters and isolates individual subsystem status bits (Query Mask).
# Calling Convention (Microsoft x64 ABI):
#   RCX = uint64_t a (Status bitmask)
#   RDX = uint64_t b (Filter mask)
# Returns:
#   RAX = a & b
# ==============================================================================
.global asm_bitwise_and
.global _asm_bitwise_and
asm_bitwise_and:
_asm_bitwise_and:
    push rbp
    mov rbp, rsp

    # --------------------------------------------------------------------------
    # MOV RAX, RCX / AND RAX, RDX:
    # - Small Meaning: Copy first mask and perform bitwise AND with second mask
    # - Long Form    : Move Register C to A / Bitwise Logical AND Register D with A
    # - Why We Used It: Evaluates whether designated spacecraft systems are active.
    # --------------------------------------------------------------------------
    mov rax, rcx
    and rax, rdx

    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_bitwise_or
# - Small Meaning: Computes bitwise disjunction of two 64-bit operands (A | B).
# - Long Form    : Assembly 64-Bit Bitwise Logical OR
# - Why We Used It: Sets specific subsystem flags to active without modifying others.
# Calling Convention (Microsoft x64 ABI):
#   RCX = uint64_t a
#   RDX = uint64_t b
# Returns:
#   RAX = a | b
# ==============================================================================
.global asm_bitwise_or
.global _asm_bitwise_or
asm_bitwise_or:
_asm_bitwise_or:
    push rbp
    mov rbp, rsp

    # --------------------------------------------------------------------------
    # MOV RAX, RCX / OR RAX, RDX:
    # - Small Meaning: Copy first mask and perform bitwise OR with second mask
    # - Long Form    : Move Register C to A / Bitwise Logical OR Register D with A
    # - Why We Used It: Power-on sequence asserting flags like Weapons and Shields.
    # --------------------------------------------------------------------------
    mov rax, rcx
    or  rax, rdx

    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_bitwise_xor
# - Small Meaning: Computes bitwise exclusive-OR of two 64-bit operands (A ^ B).
# - Long Form    : Assembly 64-Bit Bitwise Logical eXclusive-OR
# - Why We Used It: Toggles ship states (e.g., flipping Cloak or Shield polarity).
# Calling Convention (Microsoft x64 ABI):
#   RCX = uint64_t a
#   RDX = uint64_t b
# Returns:
#   RAX = a ^ b
# ==============================================================================
.global asm_bitwise_xor
.global _asm_bitwise_xor
asm_bitwise_xor:
_asm_bitwise_xor:
    push rbp
    mov rbp, rsp

    # --------------------------------------------------------------------------
    # MOV RAX, RCX / XOR RAX, RDX:
    # - Small Meaning: Copy first mask and perform bitwise XOR with second mask
    # - Long Form    : Move Register C to A / Bitwise Logical eXclusive-OR
    # - Why We Used It: State toggling and cryptographic checksum calculation.
    # --------------------------------------------------------------------------
    mov rax, rcx
    xor rax, rdx

    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_bitwise_not
# - Small Meaning: Computes bitwise one's complement inversion of 64-bit operand (~A).
# - Long Form    : Assembly 64-Bit Bitwise Logical NOT (One's Complement)
# - Why We Used It: Inverts operational telemetry masks to construct disable masks.
# Calling Convention (Microsoft x64 ABI):
#   RCX = uint64_t a
# Returns:
#   RAX = ~a
# ==============================================================================
.global asm_bitwise_not
.global _asm_bitwise_not
asm_bitwise_not:
_asm_bitwise_not:
    push rbp
    mov rbp, rsp

    # --------------------------------------------------------------------------
    # MOV RAX, RCX / NOT RAX:
    # - Small Meaning: Invert all 64 bits (0 -> 1, 1 -> 0)
    # - Long Form    : Move Register C to A / One's Complement Negation of Register A
    # - Why We Used It: Reverses all bit states across all 64 subsystem positions.
    # --------------------------------------------------------------------------
    mov rax, rcx
    not rax

    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co2_demo_logic
# - Small Meaning: Demonstrates all 4 bitwise logic operations with 64-bit output.
# - Long Form    : CO2 Demonstration Driver for 64-Bit Logical Operations
# - Why We Used It: Validates Boolean logic truth tables under 64-bit register width.
# ==============================================================================
.global co2_demo_logic
.global _co2_demo_logic
co2_demo_logic:
_co2_demo_logic:
    push rbp
    mov rbp, rsp
    sub rsp, 48                 # Shadow space (32) + 16 bytes local/alignment
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + logic_demo_title]
    call print_str

    # 64-bit Test Bitmasks:
    # A = 0x8000000100000053 (Uplink, Beacon, Shields, Boost, Cloak, Radar)
    # B = 0x800000000000003A (Uplink, Hyperdrive, Cloak, Weapons, Boost)
    mov rbx, 0x8000000100000053
    mov rsi, 0x800000000000003A

    # Display Initial A
    lea rcx, [rip + logic_init_msg]
    mov rdx, rbx
    call printf
    mov rcx, rbx
    call print_bin64

    # Display Mask B
    lea rcx, [rip + logic_mask_msg]
    mov rdx, rsi
    call printf
    mov rcx, rsi
    call print_bin64

    # Bitwise AND
    mov rcx, rbx
    mov rdx, rsi
    call asm_bitwise_and
    mov rdi, rax
    lea rcx, [rip + logic_and_msg]
    mov rdx, rdi
    call printf
    mov rcx, rdi
    call print_bin64

    # Bitwise OR
    mov rcx, rbx
    mov rdx, rsi
    call asm_bitwise_or
    mov rdi, rax
    lea rcx, [rip + logic_or_msg]
    mov rdx, rdi
    call printf
    mov rcx, rdi
    call print_bin64

    # Bitwise XOR
    mov rcx, rbx
    mov rdx, rsi
    call asm_bitwise_xor
    mov rdi, rax
    lea rcx, [rip + logic_xor_msg]
    mov rdx, rdi
    call printf
    mov rcx, rdi
    call print_bin64

    # Bitwise NOT
    mov rcx, rbx
    call asm_bitwise_not
    mov rdi, rax
    lea rcx, [rip + logic_not_msg]
    mov rdx, rdi
    call printf
    mov rcx, rdi
    call print_bin64

    lea rcx, [rip + close_paren_nl]
    call print_str

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
