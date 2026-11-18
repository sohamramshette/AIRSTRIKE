; ==============================================================================
; AIRSTRIKE 64-BIT ASSEMBLY SUITE (x86-64 / AMD64) — CO1 ARCHITECTURE
; File: CO1_architecture_64bit.asm
; Target: 64-bit Windows (x64 ABI) / NASM / MASM compatible
; Description: 64-bit Registers, Condition Flags, SIB Addressing & Aegis Sentry
; ==============================================================================
;
; Course Outcome: COA-CO1
; - 64-bit General Purpose Registers (RAX-R15) & Sub-Register Partitioning
; - 64-bit Index & Pointer Registers (RSI, RDI, RBP, RSP Stack Operations)
; - 64-bit RFLAGS Register & Condition Code Flags (CF, ZF, SF, OF)
; - 64-bit Scaled-Index-Base (SIB) Addressing [RBX + RSI*8 + 16]
; ==============================================================================

default rel
bits 64

extern printf

section .data align=16

    arch_header:
        db 10, "==============================================================================", 10
        db "                 CO1: 64-BIT x86-64 ARCHITECTURE & INSTRUCTION SET              ", 10
        db "==============================================================================", 10, 0

    gpr_fmt:
        db "  RAX = 0x%016llX | EAX = 0x%08X | AX = 0x%04X | AH = 0x%02X | AL = 0x%02X", 10
        db "  RBX = 0x%016llX | EBX = 0x%08X | BX = 0x%04X | BH = 0x%02X | BL = 0x%02X", 10
        db "  RCX = 0x%016llX | ECX = 0x%08X | CX = 0x%04X | CH = 0x%02X | CL = 0x%02X", 10
        db "  RDX = 0x%016llX | EDX = 0x%08X | DX = 0x%04X | DH = 0x%02X | DL = 0x%02X", 10
        db "  R8  = 0x%016llX | R8D = 0x%08X | R8W= 0x%04X | R8B = 0x%02X", 10, 0

    flag_demo_fmt:
        db 10, "--- 64-bit RFLAGS Condition Code Evaluation ---", 10
        db "  Carry Flag    (CF) = %d (Unsigned Carry/Borrow across bit 63)", 10
        db "  Zero Flag     (ZF) = %d (Arithmetic result is exactly 0)", 10
        db "  Sign Flag     (SF) = %d (Result is negative, MSB sign bit = 1)", 10
        db "  Overflow Flag (OF) = %d (Signed two's complement boundary overflow)", 10, 0

    sib_fmt:
        db 10, "--- 64-bit Scale-Index-Base (SIB) Memory Access ---", 10
        db "  Formula: [Base (RBX) + Index (RSI) * Scale (8) + Displacement (16)]", 10
        db "  Slot [%lld] -> 64-bit Telemetry Value = 0x%016llX", 10, 0

    align 16
    sample_score_table_x64:
        dq 0xDEADBEEFCAFEBABE        ; Header Offset +0
        dq 0x0000000100000002        ; Header Offset +8
        dq 1250                      ; Slot 0 [Base + 0*8 + 16]
        dq 3400                      ; Slot 1 [Base + 1*8 + 16]
        dq 4800                      ; Slot 2 [Base + 2*8 + 16]
        dq 6200                      ; Slot 3 [Base + 3*8 + 16]

section .text

; ==============================================================================
; Function: coa_init_aegis_shield_x64
; - Small Meaning: Set up 64-bit initial composite shield
; - Long Form    : Computer Organization Architecture Initialize Aegis Shield
; - Why We Used It: Establishes a multi-byte hexadecimal pattern to prove partitioning.
; ==============================================================================
global coa_init_aegis_shield_x64
coa_init_aegis_shield_x64:
    ; MOV: Copy 64-bit constant into RAX
    ; Long Form: Move Data into Register A Extended
    ; Why: Sets full 64-bit quadword pattern across RAX, EAX, AX, AH, AL
    mov rax, 0xFEDCBA98A5B6C7D8
    ret

; ==============================================================================
; Function: coa_damage_al_layer_x64
; - Small Meaning: Damage lowest 8-bit polarizer
; - Long Form    : Damage Accumulator Low Layer
; - Why We Used It: Proves that modifying AL (bits 0..7) modifies RAX and AX while
;                  preserving AH (bits 8..15).
; ==============================================================================
global coa_damage_al_layer_x64
coa_damage_al_layer_x64:
    mov rax, rcx            ; RAX = current shield
    ; SUB AL, DL: Subtract byte from Accumulator Low
    ; Long Form: Subtract from Register AL
    ; Why: Modifies only bits 0..7
    sub al, dl
    ret

; ==============================================================================
; Function: coa_sib_lookup_x64
; - Small Meaning: Access 64-bit array element using SIB
; - Long Form    : Scale-Index-Base Memory Lookup
; - Why We Used It: Hardware performs [Base + Index*8 + Disp] in 1 clock cycle.
; ==============================================================================
global coa_sib_lookup_x64
coa_sib_lookup_x64:
    push rbp
    mov rbp, rsp
    push rbx

    mov rbx, rcx            ; Base Address (RBX)
    mov rsi, rdx            ; Index (RSI)

    ; SIB Instruction: [Base + Index*Scale + Disp]
    ; Scale = 8 for 64-bit quadwords, Displacement = 16
    mov rax, [rbx + rsi*8 + 16]

    pop rbx
    pop rbp
    ret
