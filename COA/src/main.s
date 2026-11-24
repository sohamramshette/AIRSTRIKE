# ==============================================================================
# File    : COA/src/main.s
# Subject : COA Master Console Menu & Automated Test Dispatcher
# Topics  : COA-CO1, COA-CO2, COA-CO3, COA-CO4, CLI Test Runner (--test)
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
main_banner:
    .asciz "\n==============================================================================\n        AIRSTRIKE - COMPUTER ORGANIZATION & ARCHITECTURE (COA) IN x86-64       \n               Academic Demonstration & Architecture Verification              \n==============================================================================\n  [1] Architecture & Registers (CO1: RAX-AL, RFLAGS, SIB, Aegis Sentry)\n  [2] Assembly Language Programs (CO2: 10 Core Programs & Fibonacci Skimmer)\n  [3] Computer Arithmetic (CO3: Booth Multiply, Restoring Div & Booth Gunship)\n  [4] Control Unit & Pipelining (CO4: Signals, Hazards & Pipelined Railgun)\n  [5] COA Multiplayer Combat Architecture Demonstrator (CO1 to CO4)\n  [6] Run All Demonstrations & Automated Verification Tests\n  [7] Exit Application\n==============================================================================\n  Enter selection [1-7]: "

farewell_msg: .asciz "\nExiting AIRSTRIKE COA module. Systems offline.\n"
invalid_msg:  .asciz "\n[ERROR] Invalid option. Please select a number between 1 and 7.\n"

arg_test1:    .asciz "--test"
arg_test2:    .asciz "-t"
arg_test3:    .asciz "--all"

.text

# ------------------------------------------------------------------------------
# [TOPIC: Program Entry Point & CLI Dispatch] main
# Purpose   : Parses --test/-t/--all for automated verification or opens menu.
# Game Role : Main launcher connecting Java UI/CLI (`coa_runner.exe --test`) to all 4 COs.
# Inputs    : ECX = argc, RDX = argv
# Output    : RAX = 0 on success, 1 if any unit test fails
# ------------------------------------------------------------------------------
.global main
.global _main
main:
_main:
    push rbp
    mov  rbp, rsp
    push rbx                    # WHY: Save RBX to hold argv[1] and menu choice across calls
    push rsi
    sub  rsp, 32                # WHY: Reserve 32-byte shadow space for C calls (strcmp, printf)

    cmp  ecx, 1                 # WHY: Use 32-bit ECX because C `int argc` may leave upper bits of RCX dirty
    jle  .L_main_menu_loop      # WHY: No CLI args -> enter interactive user menu

    mov  rsi, rdx               # WHY: RSI = char** argv base pointer
    mov  rbx, [rsi + 8]         # WHY: Offset +8 skips argv[0] (program path) to grab 64-bit pointer argv[1]
    test rbx, rbx               # WHY: Guard against null argv[1] pointer
    jz   .L_main_menu_loop

    mov  rcx, rbx
    lea  rdx, [rip + arg_test1] # WHY: Compare argv[1] against "--test" (used by Java NativeBridge & CI)
    call strcmp
    test eax, eax               # WHY: strcmp returns 0 on exact match -> sets ZF=1
    jz   .L_headless_test

    mov  rcx, rbx
    lea  rdx, [rip + arg_test2] # WHY: Also support short flag "-t"
    call strcmp
    test eax, eax
    jz   .L_headless_test

    mov  rcx, rbx
    lea  rdx, [rip + arg_test3] # WHY: Also support "--all" flag
    call strcmp
    test eax, eax
    jz   .L_headless_test

    jmp  .L_main_menu_loop

.L_headless_test:
    call run_all_coa_tests      # WHY: Run all 65 PASS/FAIL assertions headlessly and propagate exit code in RAX
    jmp  .L_main_exit

.L_main_menu_loop:
    lea  rcx, [rip + main_banner]
    call print_str

    call read_int               # WHY: Read user choice [1-7] into RAX
    mov  rbx, rax

    cmp  rbx, 1                 # WHY: Route [1..4] to CO1..CO4, [5] to Multiplayer, [6] to Full Suite, [7] Exit
    je   .L_opt1_co1
    cmp  rbx, 2
    je   .L_opt2_co2
    cmp  rbx, 3
    je   .L_opt3_co3
    cmp  rbx, 4
    je   .L_opt4_co4
    cmp  rbx, 5
    je   .L_opt5_multiplayer
    cmp  rbx, 6
    je   .L_opt6_run_all
    cmp  rbx, 7
    je   .L_opt7_exit
    cmp  rbx, -1                # WHY: Exit gracefully if stdin stream hits EOF
    je   .L_opt7_exit

    lea  rcx, [rip + invalid_msg]
    call print_str
    jmp  .L_main_menu_loop

.L_opt1_co1:
    call co1_menu_loop
    jmp  .L_main_menu_loop

.L_opt2_co2:
    call co2_menu_loop
    jmp  .L_main_menu_loop

.L_opt3_co3:
    call co3_menu_loop
    jmp  .L_main_menu_loop

.L_opt4_co4:
    call co4_menu_loop
    jmp  .L_main_menu_loop

.L_opt5_multiplayer:
    call coa_demo_multiplayer
    call pause_key
    jmp  .L_main_menu_loop

.L_opt6_run_all:
    # WHY: Run every CO demo, every academic enemy, multiplayer, and 65 tests in one pass for live demos
    call co1_run_all
    call co2_run_all
    call co3_run_all
    call co4_run_all
    call coa_demo_multiplayer
    call run_all_coa_tests
    call pause_key
    jmp  .L_main_menu_loop

.L_opt7_exit:
    lea  rcx, [rip + farewell_msg]
    call print_str
    xor  eax, eax               # WHY: Zeroing EAX returns exit code 0 (success) to OS

.L_main_exit:
    add  rsp, 32
    pop  rsi
    pop  rbx
    pop  rbp
    ret
