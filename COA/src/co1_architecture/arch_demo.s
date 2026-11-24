# ==============================================================================
# File    : COA/src/co1_architecture/arch_demo.s
# Subject : COA-CO1 | Processor Architecture & Instruction Set
# Topics  : General Purpose Registers (GPR), Sub-Register Partitioning
#           (RAX -> EAX -> AX -> AH/AL), Pointer & Index Registers (RSI, RDI,
#           RBP, RSP), Stack Push/Pop, RFLAGS/EFLAGS (CF, ZF, SF, OF),
#           Segmentation (CS, DS, SS, ES), Addressing Modes, SIB [Base+Idx*8+16]
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
co1_menu_hdr:
    .asciz "\n==============================================================================\n               CO1: x86-64 ARCHITECTURE & INSTRUCTION SET SUBMENU             \n==============================================================================\n  [1] General Purpose Registers & Sub-Register Partitioning (RAX/EAX/AX/AH/AL)\n  [2] Index & Pointer Registers (RSI, RDI, RBP, RSP Stack Push/Pop)\n  [3] RFLAGS / Condition Code Flags (CF, ZF, SF, OF)\n  [4] Memory Segmentation & Flat Long Mode (CS, DS, SS, ES)\n  [5] Addressing Modes (Immediate, Register, RIP-Rel, Indirect, SIB)\n  [6] Sub-Register Aegis Sentry (COA-CO1 Enemy Demo)\n  [7] Run All CO1 Demonstrations\n  [8] Return to Main Menu\n==============================================================================\n  Enter choice [1-8]: "

arch_header:
    .asciz "\n==============================================================================\n                 CO1: x86-64 ARCHITECTURE & INSTRUCTION SET                   \n==============================================================================\n"

sec_gpr_title:
    .asciz "\n--- 1. GENERAL PURPOSE REGISTERS & SUB-REGISTER PARTITIONING ---\n"
gpr_row_fmt:
    .asciz "  %-3s = 0x%016llX | %-3s = 0x%08X | %-2s = 0x%04X | %-2s = 0x%02X | %-2s = 0x%02X\n"
gpr_alias_msg:
    .asciz "  [Live Aliasing Test] After `mov al, 0xFF`:\n    RAX = 0x%016llX | AH = 0x%02X (unchanged!) | AL = 0x%02X (updated!)\n"

s_rax: .asciz "RAX"
s_eax: .asciz "EAX"
s_ax:  .asciz "AX"
s_ah:  .asciz "AH"
s_al:  .asciz "AL"

s_rbx: .asciz "RBX"
s_ebx: .asciz "EBX"
s_bx:  .asciz "BX"
s_bh:  .asciz "BH"
s_bl:  .asciz "BL"

sec_ptrs_title:
    .asciz "\n--- 2. INDEX & POINTER REGISTERS (RSI, RDI, RBP, RSP) ---\n"
ptrs_fmt:
    .asciz "  RSI (Source Index)      = 0x%016llX\n  RDI (Destination Index) = 0x%016llX\n  RBP (Base Pointer)      = 0x%016llX\n  RSP (Stack Pointer)     = 0x%016llX\n"
rsp_demo_msg:
    .asciz "  [Stack Push/Pop] Pushing 64-bit value decrements RSP by 8 bytes:\n"
rsp_before_after:
    .asciz "  RSP Before: 0x%016llX | RSP After Push: 0x%016llX | RSP After Pop: 0x%016llX\n"

sec_eflags_title:
    .asciz "\n--- 3. RFLAGS / EFLAGS CONDITION CODE FLAGS ---\n"
flag_cf_demo:
    .asciz "  [Carry Flag CF]   : 0xFFFFFFFFFFFFFFFF + 1 -> CF = %d (Unsigned Carry-Out)\n"
flag_zf_demo:
    .asciz "  [Zero Flag ZF]    : 5000 - 5000            -> ZF = %d (Result is Zero)\n"
flag_sf_demo:
    .asciz "  [Sign Flag SF]    : 1000 - 2500            -> SF = %d (Negative MSB = 1)\n"
flag_of_demo:
    .asciz "  [Overflow Flag OF]: 0x7FFFFFFFFFFFFFFF + 1 -> OF = %d (Signed Sign-Flip)\n"

sec_segments_title:
    .asciz "\n--- 4. MEMORY SEGMENTATION & FLAT 64-BIT MODEL ---\n"
seg_fmt:
    .asciz "  Segment Selectors: CS=0x%04X | DS=0x%04X | SS=0x%04X | ES=0x%04X\n  Note: In 64-bit Long Mode, CS/DS/SS/ES share a flat 0-base address space.\n"

sec_addr_title:
    .asciz "\n--- 5. ADDRESSING MODES (INCLUDING SIB) ---\n"
addr_imm_msg:
    .asciz "  1. Immediate Addressing       : mov rax, 450           -> %lld\n"
addr_reg_msg:
    .asciz "  2. Register Addressing        : mov rbx, rax           -> %lld\n"
addr_rip_msg:
    .asciz "  3. RIP-Relative Direct        : mov rax, [rip + var]   -> %lld\n"
addr_ind_msg:
    .asciz "  4. Register Indirect          : mov rax, [rsi]         -> %lld\n"
addr_sib_msg:
    .asciz "  5. Scaled-Index-Base (SIB)    : mov rax, [rbx+rsi*8+16]-> %lld\n"

sec_instr_title:
    .asciz "\n--- 6. CORE INSTRUCTION CATEGORIES ---\n"
instr_cat_msg:
    .asciz "  Data Transfer : MOV, LEA, XCHG, PUSH, POP\n  Arithmetic    : ADD, SUB, INC, DEC, NEG, IMUL, IDIV (CQO)\n  Bitwise Logic : AND, OR, XOR, NOT, SHL, SHR, SAR\n  Control Flow  : CMP, TEST, JMP, JE/JNE/JL/JG, CALL, RET\n==============================================================================\n"

.align 8
sample_var_x64:
    .quad 9876543210

# SIB Test Array: 16-byte header followed by 4 quadwords (8 bytes each)
.align 8
sample_array_x64:
    .quad 0x1111111122222222    # Header +0
    .quad 0x3333333344444444    # Header +8
    .quad 100                   # Slot 0: [Base + 0*8 + 16]
    .quad 200                   # Slot 1: [Base + 1*8 + 16]
    .quad 300                   # Slot 2: [Base + 2*8 + 16]
    .quad 400                   # Slot 3: [Base + 3*8 + 16]

.text

# ------------------------------------------------------------------------------
# [TOPIC: CO1 Interactive Submenu] co1_menu_loop
# Purpose   : Menu dispatcher for CO1 architecture demos and Aegis Sentry enemy.
# Game Role : Lets users test CPU register layers, flags, SIB, and Aegis Sentry.
# ------------------------------------------------------------------------------
.global co1_menu_loop
.global _co1_menu_loop
co1_menu_loop:
_co1_menu_loop:
    push rbp
    mov  rbp, rsp
    push rbx
    sub  rsp, 40                # WHY: 1 push (8) + 40 = 48 bytes -> maintains 16-byte stack alignment

.L_co1_loop:
    lea  rcx, [rip + co1_menu_hdr]
    call print_str

    call read_int
    mov  rbx, rax

    cmp  rbx, 1
    je   .L_co1_opt1
    cmp  rbx, 2
    je   .L_co1_opt2
    cmp  rbx, 3
    je   .L_co1_opt3
    cmp  rbx, 4
    je   .L_co1_opt4
    cmp  rbx, 5
    je   .L_co1_opt5
    cmp  rbx, 6
    je   .L_co1_opt6
    cmp  rbx, 7
    je   .L_co1_opt7
    cmp  rbx, 8
    je   .L_co1_exit
    cmp  rbx, -1
    je   .L_co1_exit
    jmp  .L_co1_loop

.L_co1_opt1:
    call demo_gpr_x64
    jmp  .L_co1_loop
.L_co1_opt2:
    call demo_pointer_registers_x64
    jmp  .L_co1_loop
.L_co1_opt3:
    call demo_eflags_x64
    jmp  .L_co1_loop
.L_co1_opt4:
    call demo_segmentation_x64
    jmp  .L_co1_loop
.L_co1_opt5:
    call demo_addressing_modes_x64
    call demo_instruction_categories_x64
    jmp  .L_co1_loop
.L_co1_opt6:
    call coa_demo_aegis_sentry
    jmp  .L_co1_loop
.L_co1_opt7:
    call co1_run_all
    jmp  .L_co1_loop

.L_co1_exit:
    add  rsp, 40
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO1 Batch Runner] co1_run_all
# Purpose   : Runs all CO1 demonstrations and the Aegis Sentry demo sequentially.
# Game Role : Verifies complete CO1 hardware foundation before gameplay demos.
# ------------------------------------------------------------------------------
.global co1_run_all
.global _co1_run_all
co1_run_all:
_co1_run_all:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    lea  rcx, [rip + arch_header]
    call print_str

    call demo_gpr_x64
    call demo_pointer_registers_x64
    call demo_eflags_x64
    call demo_segmentation_x64
    call demo_addressing_modes_x64
    call demo_instruction_categories_x64
    call coa_demo_aegis_sentry

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: General Purpose Registers & Sub-Register Partitioning] demo_gpr_x64
# Purpose   : Reads 64/32/16/8-bit register slices and proves AL/AH hardware aliasing.
# Game Role : Proves the hardware basis behind Aegis Sentry's 4-layer nested shields.
# ------------------------------------------------------------------------------
.global demo_gpr_x64
.global _demo_gpr_x64
demo_gpr_x64:
_demo_gpr_x64:
    push rbp
    mov  rbp, rsp
    push rbx
    push r12
    sub  rsp, 96                # WHY: Reserve 32-byte shadow + 7 stack argument slots for 11-arg printf

    lea  rcx, [rip + sec_gpr_title]
    call printf

    # Extract RAX -> EAX -> AX -> AH / AL slices directly from one 64-bit register
    mov  rbx, 0x11223344AABBCCDD # WHY: Distinct byte pattern makes every sub-register slice easy to spot
    mov  r10d, ebx               # WHY: Reading 32-bit EBX isolates bits 0..31 (EAX slice)
    movzx r11d, bx               # WHY: Zero-extending 16-bit BX isolates bits 0..15 (AX slice)
    mov  [rsp + 32], r10d        # WHY: Pass Arg5+ on stack per Windows x64 ABI
    lea  rcx, [rip + s_ax]
    mov  [rsp + 40], rcx
    mov  [rsp + 48], r11d
    lea  rcx, [rip + s_ah]
    mov  [rsp + 56], rcx
    movzx ecx, bh                # WHY: BH holds bits 8..15; must load into ECX (no REX) because high-byte regs forbid REX
    mov  [rsp + 64], ecx
    lea  rcx, [rip + s_al]
    mov  [rsp + 72], rcx
    movzx ecx, bl                # WHY: BL holds lowest 8 bits (bits 0..7)
    mov  [rsp + 80], ecx

    lea  rcx, [rip + gpr_row_fmt]
    lea  rdx, [rip + s_rax]
    mov  r8,  rbx
    lea  r9,  [rip + s_eax]
    call printf

    # Repeat live slice extraction for RBX -> EBX -> BX -> BH / BL
    mov  rbx, 0x2233445511223344
    mov  r10d, ebx
    movzx r11d, bx
    mov  [rsp + 32], r10d
    lea  rcx, [rip + s_bx]
    mov  [rsp + 40], rcx
    mov  [rsp + 48], r11d
    lea  rcx, [rip + s_bh]
    mov  [rsp + 56], rcx
    movzx ecx, bh
    mov  [rsp + 64], ecx
    lea  rcx, [rip + s_bl]
    mov  [rsp + 72], rcx
    movzx ecx, bl
    mov  [rsp + 80], ecx

    lea  rcx, [rip + gpr_row_fmt]
    lea  rdx, [rip + s_rbx]
    mov  r8,  rbx
    lea  r9,  [rip + s_ebx]
    call printf

    # Live sub-register aliasing test: write low byte only and confirm high byte stays intact
    mov  rbx, 0x11223344AABBCCDD
    mov  bl,  0xFF              # WHY: Updating low byte (AL/BL) mutates bits 0..7 without touching bits 8..63 -> Game: AL shield hits preserve AH damper

    lea  rcx, [rip + gpr_alias_msg]
    mov  rdx, rbx
    movzx eax, bh               # WHY: Read BH via EAX first because `movzx r8d, bh` is illegal in x86-64 (REX + high-byte reg)
    mov  r8d, eax
    movzx r9d, bl
    call printf

    add  rsp, 96
    pop  r12
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Index & Pointer Registers & Stack Operations] demo_pointer_registers_x64
# Purpose   : Prints RSI, RDI, RBP, RSP and proves PUSH shifts RSP by -8 bytes.
# Game Role : Explains how call stacks and pointer scanning work in game routines.
# ------------------------------------------------------------------------------
.global demo_pointer_registers_x64
.global _demo_pointer_registers_x64
demo_pointer_registers_x64:
_demo_pointer_registers_x64:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push rdi
    sub  rsp, 40                # WHY: 3 pushes (24) + 40 = 64 -> keeps stack 16-byte aligned

    lea  rcx, [rip + sec_ptrs_title]
    call printf

    lea  rcx, [rip + ptrs_fmt]
    mov  rdx, rsi
    mov  r8,  rdi
    mov  r9,  rbp
    mov  [rsp + 32], rsp
    call printf

    lea  rcx, [rip + rsp_demo_msg]
    call printf

    mov  rax, 0x1122334455667788 # WHY: Load 64-bit value into RAX first since x86-64 cannot `push` a 64-bit immediate directly
    mov  rbx, rsp               # WHY: Snapshot RSP before push
    push rax                    # WHY: Hardware stack grows downward, decrementing RSP by 8 bytes
    mov  r10, rsp               # WHY: Snapshot RSP after push (before - 8)
    pop  rax                    # WHY: Hardware pop restores RSP (+8 bytes) so stack frame stays balanced
    mov  r11, rsp

    lea  rcx, [rip + rsp_before_after]
    mov  rdx, rbx
    mov  r8,  r10
    mov  r9,  r11
    call printf

    add  rsp, 40
    pop  rdi
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: RFLAGS Condition Code Flags (CF, ZF, SF, OF)] demo_eflags_x64
# Purpose   : Triggers and extracts hardware Carry, Zero, Sign, and Overflow flags.
# Game Role : These exact flags drive shield break (ZF), hull breach (SF), and leader checks (CMP).
# ------------------------------------------------------------------------------
.global demo_eflags_x64
.global _demo_eflags_x64
demo_eflags_x64:
_demo_eflags_x64:
    push rbp
    mov  rbp, rsp
    push rbx
    sub  rsp, 40

    lea  rcx, [rip + sec_eflags_title]
    call printf

    mov  rax, 0xFFFFFFFFFFFFFFFF
    add  rax, 1                 # WHY: Exceeds max 64-bit unsigned value -> sets Carry Flag (CF=1)
    setc bl                     # WHY: Extract CF directly into BL without conditional branching
    movzx edx, bl
    lea  rcx, [rip + flag_cf_demo]
    call printf

    mov  rax, 5000
    sub  rax, 5000              # WHY: Exact zero result sets Zero Flag (ZF=1) -> Game: Shield depleted / Match tied
    setz bl
    movzx edx, bl
    lea  rcx, [rip + flag_zf_demo]
    call printf

    mov  rax, 1000
    sub  rax, 2500              # WHY: Negative result sets MSB=1 -> sets Sign Flag (SF=1) -> Game: Hull breached
    sets bl
    movzx edx, bl
    lea  rcx, [rip + flag_sf_demo]
    call printf

    mov  rax, 0x7FFFFFFFFFFFFFFF
    add  rax, 1                 # WHY: Pos + Pos wrapping to Negative corrupts sign -> sets Overflow Flag (OF=1)
    seto bl
    movzx edx, bl
    lea  rcx, [rip + flag_of_demo]
    call printf

    add  rsp, 40
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Memory Segmentation & Flat Model] demo_segmentation_x64
# Purpose   : Reads active 16-bit CS, DS, SS, ES segment selector registers.
# Game Role : Shows why game code/data/stack share one flat virtual memory space.
# ------------------------------------------------------------------------------
.global demo_segmentation_x64
.global _demo_segmentation_x64
demo_segmentation_x64:
_demo_segmentation_x64:
    push rbp
    mov  rbp, rsp
    sub  rsp, 48

    lea  rcx, [rip + sec_segments_title]
    call printf

    mov  ax, cs                 # WHY: Read Code, Data, Stack, Extra hardware segment selectors
    mov  bx, ds
    mov  dx, ss
    mov  r8w, es

    movzx ecx, r8w
    mov  [rsp + 32], ecx
    movzx r9d, dx
    movzx r8d, bx
    movzx edx, ax
    lea  rcx, [rip + seg_fmt]
    call printf

    add  rsp, 48
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Addressing Modes & SIB] demo_addressing_modes_x64
# Purpose   : Demonstrates Immediate, Register, RIP-Relative, Indirect, and SIB.
# Game Role : SIB lets the engine index enemy shield arrays & score tables in 1 cycle.
# ------------------------------------------------------------------------------
.global demo_addressing_modes_x64
.global _demo_addressing_modes_x64
demo_addressing_modes_x64:
_demo_addressing_modes_x64:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 32

    lea  rcx, [rip + sec_addr_title]
    call printf

    mov  rbx, 450               # WHY: Immediate Addressing encodes constant 450 inside opcode (zero memory read)
    lea  rcx, [rip + addr_imm_msg]
    mov  rdx, rbx
    call printf

    mov  rax, rbx               # WHY: Register Addressing transfers CPU-to-CPU in 1 cycle
    lea  rcx, [rip + addr_reg_msg]
    mov  rdx, rax
    call printf

    mov  rax, [rip + sample_var_x64] # WHY: RIP-Relative Direct Addressing enables position-independent 64-bit code
    lea  rcx, [rip + addr_rip_msg]
    mov  rdx, rax
    call printf

    lea  rsi, [rip + sample_var_x64] # WHY: Register Indirect dereferences memory pointer stored inside RSI
    mov  rax, [rsi]
    lea  rcx, [rip + addr_ind_msg]
    mov  rdx, rax
    call printf

    lea  rbx, [rip + sample_array_x64] # WHY: Base address of telemetry array
    mov  rsi, 2                        # WHY: Element index = 2
    mov  rax, [rbx + rsi*8 + 16]       # WHY: Hardware SIB computes Base + Index*8 + 16-byte header in ONE instruction -> Game: Instant array slot lookup
    lea  rcx, [rip + addr_sib_msg]
    mov  rdx, rax
    call printf

    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Instruction Categories] demo_instruction_categories_x64
# Purpose   : Summarizes the four instruction groups used across all COA modules.
# Game Role : Quick reference for how assembly drives combat, loops, and branches.
# ------------------------------------------------------------------------------
.global demo_instruction_categories_x64
.global _demo_instruction_categories_x64
demo_instruction_categories_x64:
_demo_instruction_categories_x64:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    lea  rcx, [rip + sec_instr_title]
    call printf
    lea  rcx, [rip + instr_cat_msg]
    call printf

    add  rsp, 32
    pop  rbp
    ret
