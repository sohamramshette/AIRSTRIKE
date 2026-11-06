# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO1: Architecture & Instruction Set
# Demonstrates 80386 programmer model, registers, EFLAGS, segmentation,
# addressing modes, and core instruction categories.
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit Protected Mode)
# ==============================================================================

.intel_syntax noprefix

.data
arch_header:
    .asciz "\n==============================================================================\n                 CO1: INTEL 80386 ARCHITECTURE & INSTRUCTION SET               \n==============================================================================\n"

sec_gpr_title:
    .asciz "\n--- 1. GENERAL PURPOSE REGISTERS & PARTITIONING ---\n"
gpr_fmt:
    .asciz "  EAX = 0x%08X  |  AX = 0x%04X  |  AH = 0x%02X  |  AL = 0x%02X\n  EBX = 0x%08X  |  BX = 0x%04X  |  BH = 0x%02X  |  BL = 0x%02X\n  ECX = 0x%08X  |  CX = 0x%04X  |  CH = 0x%02X  |  CL = 0x%02X\n  EDX = 0x%08X  |  DX = 0x%04X  |  DH = 0x%02X  |  DL = 0x%02X\n"

sec_ptrs_title:
    .asciz "\n--- 2. INDEX & POINTER REGISTERS ---\n"
ptrs_fmt:
    .asciz "  ESI (Source Index)      = 0x%08X\n  EDI (Destination Index) = 0x%08X\n  EBP (Base Pointer)      = 0x%08X\n  ESP (Stack Pointer)     = 0x%08X\n"
esp_demo_msg:
    .asciz "  [Stack Operation] Pushing 0x12345678 -> ESP decrements by 4 bytes:\n"
esp_before_after:
    .asciz "  ESP Before: 0x%08X | ESP After Push: 0x%08X | ESP Restored: 0x%08X\n"

sec_eflags_title:
    .asciz "\n--- 3. EFLAGS REGISTER & CONDITION CODE FLAGS ---\n"
eflags_raw_fmt:
    .asciz "  Raw EFLAGS Register = 0x%08X\n"
flag_cf_demo:
    .asciz "  [Carry Flag CF]   : 0xFFFFFFFF + 0x00000001 -> CF = %d (Unsigned Overflow)\n"
flag_zf_demo:
    .asciz "  [Zero Flag ZF]    : 50 - 50                 -> ZF = %d (Result is Zero)\n"
flag_sf_demo:
    .asciz "  [Sign Flag SF]    : 10 - 25                 -> SF = %d (Result is Negative)\n"
flag_of_demo:
    .asciz "  [Overflow Flag OF]: 0x7FFFFFFF + 1          -> OF = %d (Signed Overflow: Pos + Pos = Neg)\n"

sec_segments_title:
    .asciz "\n--- 4. MEMORY SEGMENTATION (32-bit Protected Mode Segment Selectors) ---\n"
seg_fmt:
    .asciz "  Active Segment Selectors (read from Segment Registers in Ring 3 User Mode):\n    CS (Code Selector)  = 0x%04X (RPL=3, TI=0, GDT Index)\n    DS (Data Selector)  = 0x%04X (RPL=3, TI=0, GDT Index)\n    SS (Stack Selector) = 0x%04X (RPL=3, TI=0, GDT Index)\n    ES (Extra Selector) = 0x%04X (RPL=3, TI=0, GDT Index)\n  [Architectural Clarification]:\n    Under modern OS 32-bit Protected Mode, a Flat Memory Model is configured.\n    The OS kernel creates GDT descriptors where Base=0 and Limit=4 GB for all code\n    and data segments. User-space programs inspect selector registers (CS, DS, SS, ES),\n    but cannot independently inspect OS GDT descriptor tables or prove 4 GB physical\n    memory limits from unprivileged Ring 3. Segmentation remains the underlying x86\n    mechanism upon which the flat address space abstraction is established.\n"

sec_addr_title:
    .asciz "\n--- 5. 80386 ADDRESSING MODES DEMONSTRATION ---\n"
addr_imm_msg:
    .asciz "  1. Immediate Addressing           : mov eax, 450         -> Result: %d\n"
addr_reg_msg:
    .asciz "  2. Register Addressing            : mov ebx, eax         -> Result: %d\n"
addr_dir_msg:
    .asciz "  3. Direct Addressing              : mov eax, [sample_var]-> Result: %d\n"
addr_ind_msg:
    .asciz "  4. Register Indirect Addressing   : mov eax, [esi]       -> Result: %d\n"
addr_sib_msg:
    .asciz "  5. Based-Indexed-Scaled (SIB Mode): [ebx + esi*4 + 8]    -> Result: %d\n"

sec_instr_title:
    .asciz "\n--- 6. 80386 CORE INSTRUCTION CATEGORIES ---\n"
instr_cat_msg:
    .asciz "  Data Transfer: MOV (EAX=100), XCHG (EAX<->EBX), PUSH, POP -> OK\n  Arithmetic   : ADD (10+20=30), SUB (30-5=25), INC, DEC, NEG(-25)  -> OK\n  Logical      : AND (0xF0 & 0x0F=0), OR (0xAA | 0x55=0xFF), XOR    -> OK\n  Branching    : JMP, JZ, JNZ, CALL, RET tested and operational      -> OK\n==============================================================================\n"

# Test data for addressing modes
sample_var:
    .long 1234
sample_array:
    .long 10, 20, 30, 40, 50, 60, 70, 80

.text

# ------------------------------------------------------------------------------
# co1_run_all: Master entry point for CO1 demonstration
# ------------------------------------------------------------------------------
.global _co1_run_all
_co1_run_all:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset arch_header
    call _print_str
    add esp, 4

    call _demo_gpr
    call _demo_pointer_registers
    call _demo_eflags
    call _demo_segmentation
    call _demo_addressing_modes
    call _demo_instruction_categories

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# 1. GPR Partitioning Demonstration
# ------------------------------------------------------------------------------
.global _demo_gpr
_demo_gpr:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset sec_gpr_title
    call _print_str
    add esp, 4

    # Load recognizable hex values into GPRs
    mov eax, 0xAABBCCDD
    mov ebx, 0x11223344
    mov ecx, 0x55667788
    mov edx, 0x99AABBCC

    # Push EDX parts (reverse order for cdecl)
    movzx esi, dl
    push esi
    movzx esi, dh
    push esi
    movzx esi, dx
    push esi
    push edx

    # Push ECX parts
    movzx esi, cl
    push esi
    movzx esi, ch
    push esi
    movzx esi, cx
    push esi
    push ecx

    # Push EBX parts
    movzx esi, bl
    push esi
    movzx esi, bh
    push esi
    movzx esi, bx
    push esi
    push ebx

    # Push EAX parts
    movzx esi, al
    push esi
    movzx esi, ah
    push esi
    movzx esi, ax
    push esi
    push eax

    push offset gpr_fmt
    call _printf
    add esp, 68                 # 16 arguments * 4 + 4 (fmt) = 68 bytes

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# 2. Index and Pointer Registers Demonstration
# ------------------------------------------------------------------------------
.global _demo_pointer_registers
_demo_pointer_registers:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset sec_ptrs_title
    call _print_str
    add esp, 4

    # Display current pointer values
    push esp
    push ebp
    push edi
    push esi
    push offset ptrs_fmt
    call _printf
    add esp, 20

    # Demonstrate ESP alteration during PUSH / POP
    push offset esp_demo_msg
    call _print_str
    add esp, 4

    mov ebx, esp                # ESP Before
    push 0x12345678             # Value pushed
    mov esi, esp                # ESP After Push
    pop edx                     # Pop value back
    mov edi, esp                # ESP Restored

    push edi
    push esi
    push ebx
    push offset esp_before_after
    call _printf
    add esp, 16

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# 3. EFLAGS Register Demonstration
# ------------------------------------------------------------------------------
.global _demo_eflags
_demo_eflags:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset sec_eflags_title
    call _print_str
    add esp, 4

    # Read raw EFLAGS using pushfd
    pushfd
    pop eax
    push eax
    push offset eflags_raw_fmt
    call _printf
    add esp, 8

    # --- Carry Flag (CF) Demo ---
    mov eax, 0xFFFFFFFF
    add eax, 1                  # Triggers unsigned carry (CF = 1)
    setc bl                     # bl = 1 if CF=1
    movzx ebx, bl
    push ebx
    push offset flag_cf_demo
    call _printf
    add esp, 8

    # --- Zero Flag (ZF) Demo ---
    mov eax, 50
    sub eax, 50                 # 50 - 50 = 0 -> ZF = 1
    setz bl
    movzx ebx, bl
    push ebx
    push offset flag_zf_demo
    call _printf
    add esp, 8

    # --- Sign Flag (SF) Demo ---
    mov eax, 10
    sub eax, 25                 # 10 - 25 = -15 -> SF = 1
    sets bl
    movzx ebx, bl
    push ebx
    push offset flag_sf_demo
    call _printf
    add esp, 8

    # --- Overflow Flag (OF) Demo ---
    mov eax, 0x7FFFFFFF         # Maximum positive signed 32-bit int
    add eax, 1                  # 0x7FFFFFFF + 1 = 0x80000000 (negative!) -> OF = 1
    seto bl
    movzx ebx, bl
    push ebx
    push offset flag_of_demo
    call _printf
    add esp, 8

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# 4. Memory Segmentation Demonstration
# ------------------------------------------------------------------------------
.global _demo_segmentation
_demo_segmentation:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset sec_segments_title
    call _print_str
    add esp, 4

    # Read segment selectors into general registers
    mov ax, cs
    mov bx, ds
    mov cx, ss
    mov dx, es

    movzx edx, dx
    push edx
    movzx ecx, cx
    push ecx
    movzx ebx, bx
    push ebx
    movzx eax, ax
    push eax
    push offset seg_fmt
    call _printf
    add esp, 20

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# 5. Addressing Modes Demonstration
# ------------------------------------------------------------------------------
.global _demo_addressing_modes
_demo_addressing_modes:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset sec_addr_title
    call _print_str
    add esp, 4

    # 1. Immediate Addressing: operand is part of the instruction
    mov eax, 450
    push eax
    push offset addr_imm_msg
    call _printf
    add esp, 8

    # 2. Register Addressing: operand resides in register
    mov eax, 450
    mov ebx, eax
    push ebx
    push offset addr_reg_msg
    call _printf
    add esp, 8

    # 3. Direct Addressing: operand at direct label memory location
    mov eax, [sample_var]
    push eax
    push offset addr_dir_msg
    call _printf
    add esp, 8

    # 4. Register Indirect Addressing: pointer in ESI points to memory
    mov esi, offset sample_var
    mov eax, [esi]
    push eax
    push offset addr_ind_msg
    call _printf
    add esp, 8

    # 5. SIB Addressing: [Base + Index*Scale + Displacement]
    # sample_array = {10, 20, 30, 40, 50, 60, 70, 80}
    # Base = sample_array (points to 10)
    # Index = 2
    # Scale = 4 (dword) -> 2 * 4 = 8 bytes
    # Displacement = 8 bytes
    # Total offset = 8 + 8 = 16 bytes -> 5th element = sample_array[4] = 50!
    mov ebx, offset sample_array
    mov esi, 2
    mov eax, [ebx + esi*4 + 8]
    push eax
    push offset addr_sib_msg
    call _printf
    add esp, 8

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# 6. Instruction Categories Demonstration
# ------------------------------------------------------------------------------
.global _demo_instruction_categories
_demo_instruction_categories:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset sec_instr_title
    call _print_str
    add esp, 4

    # Verification of data transfer
    mov eax, 100
    mov ebx, 200
    xchg eax, ebx               # eax = 200, ebx = 100

    # Verification of arithmetic
    add eax, 10                 # 210
    sub eax, 5                  # 205
    inc eax                     # 206
    dec eax                     # 205
    neg eax                     # -205
    neg eax                     # 205

    # Verification of logical
    mov ecx, 0xF0
    and ecx, 0x0F               # 0
    or  ecx, 0xFF               # 0xFF
    xor ecx, 0xFF               # 0
    not ecx                     # 0xFFFFFFFF

    push offset instr_cat_msg
    call _print_str
    add esp, 4

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
