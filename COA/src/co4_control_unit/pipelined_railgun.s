# ==============================================================================
# File    : COA/src/co4_control_unit/pipelined_railgun.s
# Subject : COA-CO4 Enemy | Pipelined Railgun Dreadnought
# Topics  : 5-Stage Pipeline (IF, ID, EX, MEM, WB), RAW Data Hazard,
#           Stall Bubbles, Operand Forwarding Bypass, Railgun Dreadnought
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
railgun_title:
    .asciz "\n==============================================================================\n   COA-CO4: PIPELINED RAILGUN DREADNOUGHT - 5-STAGE WEAPON PIPELINE           \n==============================================================================\n"

railgun_firing_seq:
    .asciz "  Railgun Firing Sequence:\n    I1: CHARGE_CELL  RAX, RDX  (Writes energy to RAX in WB - Cycle 5)\n    I2: AIM_MAGNETIC RBX, RAX  (Needs RAX in ID - Cycle 3 -> RAW Hazard!)\n    I3: IONIZE_GAS   RCX, 100  (Independent plasma prep)\n    I4: DISCHARGE    RSI, RBX  (Fires beam using RBX)\n\n"

railgun_ideal_hdr:
    .asciz "  [CASE 1: IDEAL PIPELINE]        4 shots -> %lld cycles, %lld stalls (Speedup: 2.50x)\n"
railgun_raw_hdr:
    .asciz "  [CASE 2: RAW HAZARD (STALL)]    4 shots -> %lld cycles, %lld stalls (2 bubbles inserted)\n"
railgun_fwd_hdr:
    .asciz "  [CASE 3: OPERAND FORWARDING]    4 shots -> %lld cycles, %lld stalls (EX/MEM bypass)\n==============================================================================\n"

.text

# ------------------------------------------------------------------------------
# [TOPIC: Railgun Pipeline Hazard Evaluator] coa_railgun_pipeline_eval
# Purpose   : Computes total cycles and stall bubbles for N pipelined railgun steps.
# Game Role : Determines how fast Railgun Dreadnought charges & fires:
#             Ideal=8 cycles, Player disrupts via RAW Hazard=10 cycles (2 stalls),
#             Forwarding Bypass restores 8-cycle rapid fire!
# Inputs    : RCX=numShots (N), RDX=hazardMode (0=Ideal, 1=RAW Stall, 2=Forwarding),
#             R8=&outCycles, R9=&outStalls
# Output    : RAX = total execution cycles
# ------------------------------------------------------------------------------
.global coa_railgun_pipeline_eval
.global _coa_railgun_pipeline_eval
coa_railgun_pipeline_eval:
_coa_railgun_pipeline_eval:
    cmp  rcx, 1                 # WHY: Clamp minimum volley length to 1 so cycle count never drops below 5
    jge  .L_rg_n_ok
    mov  rcx, 1
.L_rg_n_ok:
    lea  rax, [rcx + 4]         # WHY: 5-stage pipeline overlaps CHARGE/AIM/IONIZE/DISCHARGE -> (5 + N - 1) = N + 4 cycles
    xor  r10, r10               # WHY: Default stall count = 0 (Ideal & Forwarding cases)

    cmp  rdx, 1                 # WHY: Mode 1 triggers RAW hazard (AIM reads RAX before CHARGE writes WB) -> inserts 2 stall bubbles!
    jne  .L_rg_store
    mov  r10, 2                 # WHY: Record 2 bubble cycles -> Game: delays Dreadnought beam discharge!
    add  rax, 2                 # WHY: Total volley latency grows from 8 -> 10 cycles

.L_rg_store:
    test r8, r8
    jz   .L_rg_skip_cyc
    mov  [r8], rax
.L_rg_skip_cyc:
    test r9, r9
    jz   .L_rg_done
    mov  [r9], r10
.L_rg_done:
    ret

# ------------------------------------------------------------------------------
# [TOPIC: CO4 Enemy Demo - Pipelined Railgun] coa_demo_pipelined_railgun
# Purpose   : Evaluates and prints Ideal, RAW Stall, and Forwarding bypass modes.
# Game Role : Demonstrates all 3 Railgun Dreadnought charge modes live.
# ------------------------------------------------------------------------------
.global coa_demo_pipelined_railgun
.global _coa_demo_pipelined_railgun
coa_demo_pipelined_railgun:
_coa_demo_pipelined_railgun:
    push rbp
    mov  rbp, rsp
    sub  rsp, 64

    lea  rcx, [rip + railgun_title]
    call print_str
    lea  rcx, [rip + railgun_firing_seq]
    call print_str

    # Mode 0: Ideal Pipeline (4 shots -> 8 cycles, 0 stalls)
    mov  rcx, 4
    xor  rdx, rdx
    lea  r8,  [rsp + 32]
    lea  r9,  [rsp + 40]
    call coa_railgun_pipeline_eval
    lea  rcx, [rip + railgun_ideal_hdr]
    mov  rdx, [rsp + 32]
    mov  r8,  [rsp + 40]
    call printf

    # Mode 1: RAW Data Hazard without Forwarding (4 shots -> 10 cycles, 2 stalls)
    mov  rcx, 4
    mov  rdx, 1
    lea  r8,  [rsp + 32]
    lea  r9,  [rsp + 40]
    call coa_railgun_pipeline_eval
    lea  rcx, [rip + railgun_raw_hdr]
    mov  rdx, [rsp + 32]
    mov  r8,  [rsp + 40]
    call printf

    # Mode 2: Operand Forwarding Bypass (4 shots -> 8 cycles, 0 stalls)
    mov  rcx, 4
    mov  rdx, 2
    lea  r8,  [rsp + 32]
    lea  r9,  [rsp + 40]
    call coa_railgun_pipeline_eval
    lea  rcx, [rip + railgun_fwd_hdr]
    mov  rdx, [rsp + 32]
    mov  r8,  [rsp + 40]
    call printf

    add  rsp, 64
    pop  rbp
    ret
