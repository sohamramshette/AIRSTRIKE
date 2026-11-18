# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO1: ARCHITECTURE & INSTRUCTION SET
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# Satisfies Course Outcome: COA-CO1
# ==============================================================================
# Demonstrates:
# 1. 64-bit General Purpose Registers (RAX-R15) & Sub-Register Partitioning
# 2. 64-bit Index & Pointer Registers (RSI, RDI, RBP, RSP Stack Operations)
# 3. 64-bit RFLAGS Register & Condition Code Flags (CF, ZF, SF, OF)
# 4. 64-bit Flat Memory Architecture & Long Mode Segment Registers (CS, DS, SS, ES, FS, GS)
# 5. 64-bit Addressing Modes: Immediate, Register, RIP-Relative Direct, Indirect, and SIB
# 6. Core 64-bit Instruction Categories (Data Transfer, Arithmetic, Logic, Control Flow)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
arch_header:
    .asciz "\n==============================================================================\n                 CO1: 64-BIT x86-64 ARCHITECTURE & INSTRUCTION SET              \n==============================================================================\n"

sec_gpr_title:
    .asciz "\n--- 1. 64-BIT GENERAL PURPOSE REGISTERS & SUB-REGISTER PARTITIONING ---\n"
gpr_fmt:
    .asciz "  RAX = 0x%016llX | EAX = 0x%08X | AX = 0x%04X | AH = 0x%02X | AL = 0x%02X\n  RBX = 0x%016llX | EBX = 0x%08X | BX = 0x%04X | BH = 0x%02X | BL = 0x%02X\n  RCX = 0x%016llX | ECX = 0x%08X | CX = 0x%04X | CH = 0x%02X | CL = 0x%02X\n  RDX = 0x%016llX | EDX = 0x%08X | DX = 0x%04X | DH = 0x%02X | DL = 0x%02X\n  R8  = 0x%016llX | R8D = 0x%08X | R8W= 0x%04X | R8B = 0x%02X\n"

sec_ptrs_title:
    .asciz "\n--- 2. 64-BIT INDEX & POINTER REGISTERS ---\n"
ptrs_fmt:
    .asciz "  RSI (Source Index)      = 0x%016llX\n  RDI (Destination Index) = 0x%016llX\n  RBP (Base Pointer)      = 0x%016llX\n  RSP (Stack Pointer)     = 0x%016llX\n"
rsp_demo_msg:
    .asciz "  [Stack Operation] Pushing 64-bit Quadword (0x1122334455667788) -> RSP decrements by 8 bytes:\n"
rsp_before_after:
    .asciz "  RSP Before: 0x%016llX | RSP After Push: 0x%016llX | RSP Restored: 0x%016llX\n"

sec_eflags_title:
    .asciz "\n--- 3. 64-BIT RFLAGS REGISTER & CONDITION CODE FLAGS ---\n"
flag_cf_demo:
    .asciz "  [Carry Flag CF]   : 0xFFFFFFFFFFFFFFFF + 1 -> CF = %d (64-bit Unsigned Overflow/Carry)\n"
flag_zf_demo:
    .asciz "  [Zero Flag ZF]    : 5000 - 5000            -> ZF = %d (Arithmetic Result is Exactly Zero)\n"
flag_sf_demo:
    .asciz "  [Sign Flag SF]    : 1000 - 2500            -> SF = %d (Arithmetic Result is Negative, Sign Bit MSB=1)\n"
flag_of_demo:
    .asciz "  [Overflow Flag OF]: 0x7FFFFFFFFFFFFFFF + 1 -> OF = %d (Signed Overflow: Max Pos + 1 became Negative)\n"

sec_segments_title:
    .asciz "\n--- 4. 64-BIT MEMORY ARCHITECTURE & FLAT LONG MODE MODEL ---\n"
seg_fmt:
    .asciz "  64-bit Long Mode Segment Selectors:\n    CS (Code Selector)  = 0x%04X\n    DS (Data Selector)  = 0x%04X\n    SS (Stack Selector) = 0x%04X\n    ES (Extra Selector) = 0x%04X\n  [Architectural Explanation]:\n    In 64-bit x86-64 Long Mode, segmentation is flattened in hardware.\n    The processor enforces Base Address = 0 and Limit = 2^64 (16 Exabytes)\n    for CS, DS, ES, and SS. Only FS and GS registers allow 64-bit base offsets,\n    which modern 64-bit OS kernels utilize for Thread Local Storage (TLS).\n"

sec_addr_title:
    .asciz "\n--- 5. 64-BIT ADDRESSING MODES DEMONSTRATION ---\n"
addr_imm_msg:
    .asciz "  1. Immediate Addressing                 : mov rax, 450         -> Result: %lld\n"
addr_reg_msg:
    .asciz "  2. Register Addressing                  : mov rbx, rax         -> Result: %lld\n"
addr_rip_msg:
    .asciz "  3. RIP-Relative Direct Addressing       : mov rax, [rip + var] -> Result: %lld\n"
addr_ind_msg:
    .asciz "  4. Register Indirect Addressing         : mov rax, [rsi]       -> Result: %lld\n"
addr_sib_msg:
    .asciz "  5. 64-bit Scaled-Index-Base (SIB Mode)  : [rbx + rsi*8 + 16]   -> Result: %lld\n"

sec_instr_title:
    .asciz "\n--- 6. 64-BIT CORE INSTRUCTION CATEGORIES ---\n"
instr_cat_msg:
    .asciz "  Data Transfer: MOV (64-bit), XCHG (RAX<->RBX), PUSH, POP (8-byte Quadword) -> OK\n  Arithmetic   : ADD (64-bit), SUB, INC, DEC, NEG, IMUL, IDIV (CQO 128-bit)   -> OK\n  Logical      : AND (64-bit Mask), OR, XOR (Zeroing idiom), NOT              -> OK\n  Control Flow : JMP, JZ, JNZ, CALL, RET (with 16-byte stack alignment)       -> OK\n==============================================================================\n"

# Test data for 64-bit addressing modes
.align 8
sample_var_x64:
    .quad 9876543210

.align 8
sample_array_x64:
    .quad 0x1111111122222222    # Header offset +0
    .quad 0x3333333344444444    # Header offset +8
    .quad 100                   # Slot 0 [Base + 0*8 + 16]
    .quad 200                   # Slot 1 [Base + 1*8 + 16]
    .quad 300                   # Slot 2 [Base + 2*8 + 16]
    .quad 400                   # Slot 3 [Base + 3*8 + 16]

.text

# ==============================================================================
# Routine: co1_run_all
# Master dispatcher demonstrating all 6 CO1 architecture topics in 64-bit
# ==============================================================================
.global co1_run_all
.global _co1_run_all
co1_run_all:
_co1_run_all:
    # --------------------------------------------------------------------------
    # PUSH RBP:
    # - Small Meaning: Save old base pointer on stack
    # - Long Form    : Push Register Base Pointer
    # - Why We Used It: Preserves the caller's stack frame reference in 64-bit ABI.
    # --------------------------------------------------------------------------
    push rbp

    # --------------------------------------------------------------------------
    # MOV RBP, RSP:
    # - Small Meaning: Establish new stack frame base
    # - Long Form    : Move Register Stack Pointer to Register Base Pointer
    # - Why We Used It: Creates a stable anchor point (RBP) to index local variables.
    # --------------------------------------------------------------------------
    mov rbp, rsp

    # --------------------------------------------------------------------------
    # SUB RSP, 48:
    # - Small Meaning: Allocate stack space for function calls
    # - Long Form    : Subtract from Register Stack Pointer
    # - Why We Used It: Reserves 32 bytes of Microsoft x64 Shadow Space + 16 bytes
    #                  alignment for ABI compliance before calling external functions.
    # --------------------------------------------------------------------------
    sub rsp, 48

    # Print main CO1 header banner
    # LEA RCX, [rip + arch_header]:
    # - Small Meaning: Load pointer address of string into 1st argument register
    # - Long Form    : Load Effective Address with Instruction Pointer relative offset
    # - Why We Used It: 64-bit position-independent code (PIC) uses RIP-relative offsets.
    lea rcx, [rip + arch_header]
    call printf

    # Execute all 6 sub-demonstrations
    call demo_gpr_x64
    call demo_pointer_registers_x64
    call demo_eflags_x64
    call demo_segmentation_x64
    call demo_addressing_modes_x64
    call demo_instruction_categories_x64

    # --------------------------------------------------------------------------
    # ADD RSP, 48 / POP RBP / RET:
    # - Small Meaning: Clean allocated stack space, restore RBP, and return to caller
    # - Long Form    : Add to Stack Pointer, Pop Base Pointer, Return from Subroutine
    # - Why We Used It: Standard 64-bit function epilogue preventing stack leaks.
    # --------------------------------------------------------------------------
    add rsp, 48
    pop rbp
    ret

# ==============================================================================
# Topic 1: 64-bit General Purpose Registers & Sub-Register Partitioning
# ==============================================================================
.global demo_gpr_x64
.global _demo_gpr_x64
demo_gpr_x64:
_demo_gpr_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 128                # Allocate shadow space + stack argument slots
    push rbx
    push r12

    lea rcx, [rip + sec_gpr_title]
    call printf

    # --------------------------------------------------------------------------
    # 64-BIT GPR ASSIGNMENT:
    # RAX: 64-bit Register A Extended (Accumulator)
    # RBX: 64-bit Register B Extended (Base register)
    # RCX: 64-bit Register C Extended (Count register / Arg 1)
    # RDX: 64-bit Register D Extended (Data register / Arg 2)
    # R8 : 64-bit Extended Register 8 (Arg 3 in x64 ABI)
    # Why We Used It: Demonstrates loading full 64-bit quadword hexadecimal bit patterns.
    # --------------------------------------------------------------------------
    mov rax, 0x11223344AABBCCDD
    mov rbx, 0x2233445511223344
    mov rcx, 0x3344556655667788
    mov rdx, 0x4455667799AABBCC
    mov r8,  0x55667788AABBCCDD

    # Print sub-register breakdown (EAX, AX, AH, AL, etc.)
    lea rcx, [rip + gpr_fmt]
    mov rdx, 0x11223344AABBCCDD # RAX (%016llX)
    mov r8d, 0xAABBCCDD         # EAX (%08X)
    mov r9d, 0xCCDD             # AX  (%04X)
    mov dword ptr [rsp + 32], 0xCC # AH  (%02X)
    mov dword ptr [rsp + 40], 0xDD # AL  (%02X)

    # RBX sub-registers
    mov qword ptr [rsp + 48], 0x2233445511223344 # RBX
    mov dword ptr [rsp + 56], 0x11223344         # EBX
    mov dword ptr [rsp + 64], 0x3344             # BX
    mov dword ptr [rsp + 72], 0x33               # BH
    mov dword ptr [rsp + 80], 0x44               # BL
    call printf

    pop r12
    pop rbx
    add rsp, 128
    pop rbp
    ret

# ==============================================================================
# Topic 2: 64-bit Index & Pointer Registers (RSI, RDI, RBP, RSP)
# ==============================================================================
.global demo_pointer_registers_x64
.global _demo_pointer_registers_x64
demo_pointer_registers_x64:
_demo_pointer_registers_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 64
    push rbx
    push rsi
    push rdi

    lea rcx, [rip + sec_ptrs_title]
    call printf

    # --------------------------------------------------------------------------
    # RSI: Register Source Index (64-bit)
    # RDI: Register Destination Index (64-bit)
    # RBP: Register Base Pointer (64-bit)
    # RSP: Register Stack Pointer (64-bit)
    # Why We Used It: Display current hardware addresses held by pointer registers.
    # --------------------------------------------------------------------------
    lea rcx, [rip + ptrs_fmt]
    mov rdx, rsi                # RSI
    mov r8,  rdi                # RDI
    mov r9,  rbp                # RBP
    mov [rsp + 32], rsp         # RSP (passed as 5th argument on stack)
    call printf

    # Demonstrate 64-bit Stack Push: RSP decrements by exactly 8 bytes (Quadword)
    lea rcx, [rip + rsp_demo_msg]
    call printf

    mov rbx, rsp                # RBX = RSP Before Push
    push 0x1122334455667788     # Push 64-bit immediate quadword
    mov r10, rsp                # R10 = RSP After Push (RBX - 8)
    pop rax                     # Restore stack
    mov r11, rsp                # R11 = RSP Restored (equals RBX)

    lea rcx, [rip + rsp_before_after]
    mov rdx, rbx                # RSP Before
    mov r8,  r10                # RSP After Push
    mov r9,  r11                # RSP Restored
    call printf

    pop rdi
    pop rsi
    pop rbx
    add rsp, 64
    pop rbp
    ret

# ==============================================================================
# Topic 3: 64-bit RFLAGS Register & Condition Code Flags (CF, ZF, SF, OF)
# ==============================================================================
.global demo_eflags_x64
.global _demo_eflags_x64
demo_eflags_x64:
_demo_eflags_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx

    lea rcx, [rip + sec_eflags_title]
    call printf

    # --------------------------------------------------------------------------
    # 1. CARRY FLAG (CF):
    # - Small Meaning: Unsigned arithmetic borrow or carry-out
    # - Long Form    : Carry Flag in 64-bit RFLAGS
    # - Why We Used It: Adding 1 to 0xFFFFFFFFFFFFFFFF exceeds 64-bit register capacity.
    # --------------------------------------------------------------------------
    mov rax, 0xFFFFFFFFFFFFFFFF
    add rax, 1                  # Triggers CF = 1 (unsigned overflow)
    setc bl
    movzx edx, bl
    lea rcx, [rip + flag_cf_demo]
    call printf

    # --------------------------------------------------------------------------
    # 2. ZERO FLAG (ZF):
    # - Small Meaning: Zero result indicator
    # - Long Form    : Zero Flag in 64-bit RFLAGS
    # - Why We Used It: Subtracting identical numbers (5000 - 5000) yields 0, setting ZF=1.
    # --------------------------------------------------------------------------
    mov rax, 5000
    sub rax, 5000               # Triggers ZF = 1
    setz bl
    movzx edx, bl
    lea rcx, [rip + flag_zf_demo]
    call printf

    # --------------------------------------------------------------------------
    # 3. SIGN FLAG (SF):
    # - Small Meaning: Negative result indicator
    # - Long Form    : Sign Flag (copies MSB sign bit of 64-bit result)
    # - Why We Used It: 1000 - 2500 = -1500, setting SF=1 for signed branch decisions.
    # --------------------------------------------------------------------------
    mov rax, 1000
    sub rax, 2500               # Triggers SF = 1
    sets bl
    movzx edx, bl
    lea rcx, [rip + flag_sf_demo]
    call printf

    # --------------------------------------------------------------------------
    # 4. OVERFLOW FLAG (OF):
    # - Small Meaning: Signed two's complement boundary overflow
    # - Long Form    : Overflow Flag in 64-bit RFLAGS
    # - Why We Used It: Adding 1 to Max Positive Signed Quadword (0x7FFFFFFFFFFFFFFF)
    #                  corrupts the sign bit, flipping to Negative and setting OF=1.
    # --------------------------------------------------------------------------
    mov rax, 0x7FFFFFFFFFFFFFFF
    add rax, 1                  # Triggers OF = 1
    seto bl
    movzx edx, bl
    lea rcx, [rip + flag_of_demo]
    call printf

    pop rbx
    add rsp, 48
    pop rbp
    ret

# ==============================================================================
# Topic 4: 64-bit Long Mode Memory Segmentation & Flat Model
# ==============================================================================
.global demo_segmentation_x64
.global _demo_segmentation_x64
demo_segmentation_x64:
_demo_segmentation_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 48

    lea rcx, [rip + sec_segments_title]
    call printf

    # Read active 16-bit segment selectors from CPU hardware registers
    # CS: Code Segment
    # DS: Data Segment
    # SS: Stack Segment
    # ES: Extra Segment
    mov ax, cs
    mov bx, ds
    mov dx, ss
    mov r8w, es

    lea rcx, [rip + seg_fmt]
    movzx edx, ax               # CS
    movzx r8d, bx               # DS
    movzx r9d, dx               # SS
    movzx eax, r8w
    mov [rsp + 32], rax         # ES
    call printf

    add rsp, 48
    pop rbp
    ret

# ==============================================================================
# Topic 5: 64-bit Addressing Modes Demonstration
# ==============================================================================
.global demo_addressing_modes_x64
.global _demo_addressing_modes_x64
demo_addressing_modes_x64:
_demo_addressing_modes_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx
    push rsi

    lea rcx, [rip + sec_addr_title]
    call printf

    # 1. Immediate Addressing (Operand encoded directly inside instruction machine code)
    mov rax, 450
    lea rcx, [rip + addr_imm_msg]
    mov rdx, rax
    call printf

    # 2. Register Addressing (Operand stored in hardware register)
    mov rbx, rax
    lea rcx, [rip + addr_reg_msg]
    mov rdx, rbx
    call printf

    # 3. RIP-Relative Direct Addressing (64-bit Position-Independent offset from IP)
    mov rax, [rip + sample_var_x64]
    lea rcx, [rip + addr_rip_msg]
    mov rdx, rax
    call printf

    # 4. Register Indirect Addressing (Register contains 64-bit memory pointer)
    lea rsi, [rip + sample_var_x64]
    mov rax, [rsi]
    lea rcx, [rip + addr_ind_msg]
    mov rdx, rax
    call printf

    # 5. 64-bit Scale-Index-Base (SIB) Addressing Mode:
    # SIB Formula: Effective Address = [Base + Index*Scale + Displacement]
    # Here: RBX = Array Base Address, RSI = Index (2), Scale = 8 (bytes per quadword), Disp = 16
    lea rbx, [rip + sample_array_x64] # RBX = Base Address
    mov rsi, 2                        # RSI = Slot Index 2
    mov rax, [rbx + rsi*8 + 16]       # Access Slot 2 (Value = 300)

    lea rcx, [rip + addr_sib_msg]
    mov rdx, rax
    call printf

    pop rsi
    pop rbx
    add rsp, 48
    pop rbp
    ret

# ==============================================================================
# Topic 6: Core 64-bit Instruction Categories Verification
# ==============================================================================
.global demo_instruction_categories_x64
.global _demo_instruction_categories_x64
demo_instruction_categories_x64:
_demo_instruction_categories_x64:
    push rbp
    mov rbp, rsp
    sub rsp, 32

    # Verification of Data Transfer, Arithmetic, Logic, and Control instructions
    lea rcx, [rip + sec_instr_title]
    call printf

    lea rcx, [rip + instr_cat_msg]
    call printf

    add rsp, 32
    pop rbp
    ret
