# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — MASTER CONSOLE MENU & DISPATCHER
# Entry point for the COA Academic Verification System (CO1 through CO4)
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
main_banner:
    .asciz "\n==============================================================================\n        AIRSTRIKE - COMPUTER ORGANIZATION & ARCHITECTURE (COA) IN x86-64       \n               Academic Demonstration & Architecture Verification              \n==============================================================================\n  [1] 64-Bit Architecture Demonstration (CO1: RAX-R15, RFLAGS, SIB Addressing)\n  [2] Assembly Language Programs (CO2: 10 Core 64-Bit x86-64 Programs)\n  [3] Computer Arithmetic Algorithms (CO3: Booth's Algorithm & Restoring Div)\n  [4] Control Unit & Instruction Pipelining (CO4: Hazards, Stalls, Flushes)\n  [5] COA Multiplayer Combat Architecture Demonstrator (CO1 to CO4)\n  [6] Run All Demonstrations & Automated Verification Tests\n  [7] Exit Application\n==============================================================================\n  Enter selection [1-7]: "

farewell_msg:
    .asciz "\nExiting AIRSTRIKE 64-bit Architecture module. Systems offline.\n"
invalid_msg:
    .asciz "\n[ERROR] Invalid option. Please select a number between 1 and 7.\n"

arg_test1:
    .asciz "--test"
arg_test2:
    .asciz "-t"
arg_test3:
    .asciz "--all"

.text

# ==============================================================================
# Routine: main / _main
# - Small Meaning: Master 64-bit C-runtime entry point and interactive menu dispatcher.
# - Long Form    : Main Program Entry Point
# - Why We Used It: Initializes application, parses command line flags, and dispatches submenus.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t argc    (Argument count)
#   RDX = char**  argv    (Array of argument string pointers)
# Returns:
#   RAX = Process exit status code (0 = success)
# ==============================================================================
.global main
.global _main
main:
_main:
    # --------------------------------------------------------------------------
    # PUSH RBP / MOV RBP, RSP / SUB RSP, 48:
    # - Small Meaning: Set up 64-bit base pointer and shadow space
    # - Long Form    : Frame Setup and Caller Shadow Space Allocation
    # - Why We Used It: Conforms to Microsoft x64 ABI requirement for 16-byte stack alignment.
    # --------------------------------------------------------------------------
    push rbp
    mov rbp, rsp
    sub rsp, 48
    push rbx
    push rsi
    push rdi

    # Check for command line arguments: argc = RCX, argv = RDX
    cmp rcx, 1
    jle .L_main_menu_loop       # No arguments provided, jump to interactive menu

    # Inspect argv[1]
    mov rsi, rdx                # RSI = argv
    mov rbx, [rsi + 8]          # RBX = argv[1] (64-bit pointer at offset +8)
    test rbx, rbx
    jz .L_main_menu_loop

    # strcmp(argv[1], "--test")
    mov rcx, rbx
    lea rdx, [rip + arg_test1]
    call strcmp
    test eax, eax
    jz .L_headless_test

    # strcmp(argv[1], "-t")
    mov rcx, rbx
    lea rdx, [rip + arg_test2]
    call strcmp
    test eax, eax
    jz .L_headless_test

    # strcmp(argv[1], "--all")
    mov rcx, rbx
    lea rdx, [rip + arg_test3]
    call strcmp
    test eax, eax
    jz .L_headless_test

    jmp .L_main_menu_loop

.L_headless_test:
    # --------------------------------------------------------------------------
    # CALL CO1_RUN_ALL through CO4_RUN_ALL:
    # - Small Meaning: Automated non-interactive test execution
    # - Long Form    : Call Course Outcome Automated Test Suites
    # - Why We Used It: Executes complete CI/CD verification without terminal blocking.
    # --------------------------------------------------------------------------
    call co1_run_all
    call co2_run_all
    call co3_run_all
    call co4_run_all
    jmp .L_main_exit

.L_main_menu_loop:
    lea rcx, [rip + main_banner]
    call print_str

    call read_int               # Returns selection in RAX
    mov rbx, rax

    cmp rbx, 1
    je .L_opt1_arch
    cmp rbx, 2
    je .L_opt2_programs
    cmp rbx, 3
    je .L_opt3_arithmetic
    cmp rbx, 4
    je .L_opt4_control_pipe
    cmp rbx, 5
    je .L_opt5_multiplayer
    cmp rbx, 6
    je .L_opt6_run_all
    cmp rbx, 7
    je .L_opt7_exit
    cmp rbx, -1
    je .L_opt7_exit

    lea rcx, [rip + invalid_msg]
    call print_str
    jmp .L_main_menu_loop

.L_opt1_arch:
    call co1_menu_loop
    jmp .L_main_menu_loop

.L_opt2_programs:
    call co2_menu_loop
    jmp .L_main_menu_loop

.L_opt3_arithmetic:
    call co3_menu_loop
    jmp .L_main_menu_loop

.L_opt4_control_pipe:
    call co4_menu_loop
    jmp .L_main_menu_loop

.L_opt5_multiplayer:
    call coa_demo_multiplayer
    call pause_key
    jmp .L_main_menu_loop

.L_opt6_run_all:
    # Run all CO demonstrations, Enemy demonstrations, and test verification
    call co1_run_all
    call co2_run_all
    call co3_run_all
    call co4_run_all
    call coa_demo_aegis_sentry
    call coa_demo_fibonacci_skimmer
    call coa_demo_booth_gunship
    call coa_demo_pipelined_railgun
    call coa_demo_multiplayer
    call pause_key
    jmp .L_main_menu_loop

.L_opt7_exit:
    lea rcx, [rip + farewell_msg]
    call print_str
    xor eax, eax                # Return 0

.L_main_exit:
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
