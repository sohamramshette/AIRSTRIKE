# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - Master Console Menu & Dispatcher
# Entry point for the COA Academic Verification System
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit Protected Mode, cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
main_banner:
    .asciz "\n==============================================================================\n        AIRSTRIKE - COMPUTER ORGANIZATION & ARCHITECTURE (COA) IN 80386       \n               Academic Demonstration & Architecture Verification              \n==============================================================================\n  [1] 80386 Architecture Demonstration (CO1: GPR, Flags, Segments, Addressing)\n  [2] Assembly Language Programs (CO2: 10 Core 80386 Programs)\n  [3] Computer Arithmetic Algorithms (CO3: Booth's Algorithm & Restoring Div)\n  [4] Control Unit & Instruction Pipelining (CO4: Hazards, Stalls, Flushes)\n  [5] Run All Demonstrations & Automated Verification Tests\n  [6] Exit Application\n==============================================================================\n  Enter selection [1-6]: "

farewell_msg:
    .asciz "\nExiting AIRSTRIKE 80386 Architecture module. Systems offline.\n"
invalid_msg:
    .asciz "\n[ERROR] Invalid option. Please select a number between 1 and 6.\n"

arg_test1:
    .asciz "--test"
arg_test2:
    .asciz "-t"
arg_test3:
    .asciz "--all"

.text

# ------------------------------------------------------------------------------
# main: Master C-runtime entry point
# Prototype: int main(int argc, char* argv[])
# ------------------------------------------------------------------------------
.global _main
_main:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    # Check for command line arguments: argc = [ebp+8], argv = [ebp+12]
    mov eax, [ebp + 8]          # argc
    cmp eax, 1
    jle .L_main_menu_loop       # no arguments, jump to interactive menu

    # Inspect argv[1]
    mov esi, [ebp + 12]         # argv
    mov ebx, [esi + 4]          # argv[1]
    test ebx, ebx
    jz .L_main_menu_loop

    # strcmp(argv[1], "--test")
    push offset arg_test1
    push ebx
    call _strcmp
    add esp, 8
    test eax, eax
    jz .L_headless_test

    # strcmp(argv[1], "-t")
    push offset arg_test2
    push ebx
    call _strcmp
    add esp, 8
    test eax, eax
    jz .L_headless_test

    # strcmp(argv[1], "--all")
    push offset arg_test3
    push ebx
    call _strcmp
    add esp, 8
    test eax, eax
    jz .L_headless_test

    jmp .L_main_menu_loop

.L_headless_test:
    call _run_all_coa_tests
    jmp .L_main_exit

.L_main_menu_loop:
    push offset main_banner
    call _print_str
    add esp, 4

    call _read_int
    mov ebx, eax

    cmp ebx, 1
    je .L_opt1_arch
    cmp ebx, 2
    je .L_opt2_programs
    cmp ebx, 3
    je .L_opt3_arithmetic
    cmp ebx, 4
    je .L_opt4_control_pipe
    cmp ebx, 5
    je .L_opt5_run_all
    cmp ebx, 6
    je .L_opt6_exit
    cmp ebx, -1
    je .L_opt6_exit

    push offset invalid_msg
    call _print_str
    add esp, 4
    jmp .L_main_menu_loop

.L_opt1_arch:
    call _co1_run_all
    call _pause_key
    jmp .L_main_menu_loop

.L_opt2_programs:
    call _co2_menu_loop
    jmp .L_main_menu_loop

.L_opt3_arithmetic:
    call _co3_menu_loop
    jmp .L_main_menu_loop

.L_opt4_control_pipe:
    call _co4_menu_loop
    jmp .L_main_menu_loop

.L_opt5_run_all:
    # Run all CO demonstrations, Enemy demonstrations + automated tests
    call _co1_run_all
    call _co2_run_all
    call _co3_run_all
    call _co4_run_all
    call _coa_demo_aegis_sentry
    call _coa_demo_fibonacci_skimmer
    call _coa_demo_booth_gunship
    call _coa_demo_pipelined_railgun
    call _run_all_coa_tests
    call _pause_key
    jmp .L_main_menu_loop

.L_opt6_exit:
    push offset farewell_msg
    call _print_str
    add esp, 4
    xor eax, eax

.L_main_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
