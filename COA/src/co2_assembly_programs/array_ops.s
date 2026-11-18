# ==============================================================================
# AIRSTRIKE 64-BIT ASSEMBLY SUITE — CO2: ARRAY OPERATIONS
# Implements:
#   Prog 3. Largest and Smallest Quadword in an Array
#   Prog 4. Sum and Average of 64-bit Array Elements
#   Prog 7. Linear Search in 64-bit Scaled-Index Array
#   Prog 8. Reversing a 64-bit Quadword Array In-Place
#   Prog 9. Counting Positive, Negative, and Zero Quadwords
# Target Architecture: Genuine 64-bit x86-64 / AMD64 (Long Mode)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
array_demo_title:
    .asciz "\n--- CO2 Programs 3, 4, 7, 8, 9: 64-Bit Array Operations Demonstration ---\n"
arr_initial_msg:
    .asciz "  Initial Array Elements (Size %lld): [ "
arr_item_fmt:
    .asciz "%lld "
arr_close_fmt:
    .asciz "]\n"

min_max_fmt:
    .asciz "  3. Largest and Smallest: Minimum = %lld, Maximum = %lld\n"
sum_fmt:
    .asciz "  4. Array Sum           : Total Sum = %lld (Average = %lld)\n"
search_prompt:
    .asciz "  Enter 64-bit integer to linearly search in array: "
search_found_fmt:
    .asciz "  7. Linear Search Result: Target %lld FOUND at index %lld!\n"
search_notfound_fmt:
    .asciz "  7. Linear Search Result: Target %lld NOT FOUND (Index -1).\n"
rev_msg:
    .asciz "  8. Reversed Array      : [ "
counts_fmt:
    .asciz "  9. Element Counts      : Positive = %lld, Negative = %lld, Zeros = %lld\n"

# Preset 64-bit test array for telemetry and combat coordinate simulation
demo_arr:
    .quad 45, -12, 88, 0, -34, 102, -5, 23
demo_arr_size:
    .quad 8

.bss
.align 8
.lcomm min_val, 8
.lcomm max_val, 8
.lcomm pos_cnt, 8
.lcomm neg_cnt, 8
.lcomm zero_cnt, 8

.text

# ==============================================================================
# Routine: asm_find_min_max
# - Small Meaning: Locates the minimum and maximum 64-bit quadwords in an array.
# - Long Form    : Assembly Find Minimum and Maximum (64-Bit Mode)
# - Why We Used It: Evaluates mission telemetry extreme boundaries (e.g., peak shield vs critical hull).
# Calling Convention (Microsoft x64 ABI):
#   RCX = const int64_t* arr (Base pointer to 64-bit quadword array)
#   RDX = int64_t size        (Number of elements)
#   R8  = int64_t* out_min    (Pointer to store minimum quadword)
#   R9  = int64_t* out_max    (Pointer to store maximum quadword)
# ==============================================================================
.global asm_find_min_max
.global _asm_find_min_max
asm_find_min_max:
_asm_find_min_max:
    # --------------------------------------------------------------------------
    # PUSH RBP / MOV RBP, RSP:
    # - Small Meaning: Set up 64-bit stack frame base pointer
    # - Long Form    : Push Register Base Pointer / Move Stack Pointer to Base Pointer
    # - Why We Used It: Establishes standard ABI frame record for debugging and unwinding.
    # --------------------------------------------------------------------------
    push rbp
    mov rbp, rsp
    push rbx
    push rsi
    push rdi

    # Validate size > 0
    cmp rdx, 0
    jle .L_minmax_exit

    mov rsi, rcx                # RSI = array pointer (Source Index)
    mov rcx, rdx                # RCX = element loop counter
    mov rax, [rsi]              # RAX = current minimum candidate (first element)
    mov rbx, [rsi]              # RBX = current maximum candidate (first element)
    add rsi, 8                  # Advance pointer to next 64-bit quadword
    dec rcx                     # Decrement count
    jz .L_minmax_store

.L_minmax_loop:
    mov r10, [rsi]              # R10 = load next 64-bit quadword from memory
    cmp r10, rax
    jge .L_not_min
    mov rax, r10                # Update minimum
.L_not_min:
    cmp r10, rbx
    jle .L_not_max
    mov rbx, r10                # Update maximum
.L_not_max:
    add rsi, 8                  # Advance pointer by 8 bytes (64-bit quadword step)
    dec rcx
    jnz .L_minmax_loop

.L_minmax_store:
    # --------------------------------------------------------------------------
    # Store outputs via 64-bit output pointer registers R8 and R9
    # --------------------------------------------------------------------------
    test r8, r8
    jz .L_skip_min
    mov [r8], rax               # *out_min = RAX
.L_skip_min:
    test r9, r9
    jz .L_minmax_exit
    mov [r9], rbx               # *out_max = RBX

.L_minmax_exit:
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_array_sum
# - Small Meaning: Computes the 64-bit cumulative sum of array elements.
# - Long Form    : Assembly 64-Bit Array Sum Accumulator
# - Why We Used It: Aggregates total combat squadron damage and orbital battery outputs.
# Calling Convention (Microsoft x64 ABI):
#   RCX = const int64_t* arr (Pointer to array)
#   RDX = int64_t size       (Count of elements)
# Returns:
#   RAX = 64-bit signed total sum
# ==============================================================================
.global asm_array_sum
.global _asm_array_sum
asm_array_sum:
_asm_array_sum:
    push rbp
    mov rbp, rsp
    push rsi

    mov rsi, rcx                # RSI = array pointer
    mov rcx, rdx                # RCX = loop counter
    xor rax, rax                # RAX = 0 (Clear 64-bit sum accumulator)

    test rcx, rcx
    jle .L_sum_done

.L_sum_loop:
    # --------------------------------------------------------------------------
    # ADD RAX, [RSI]:
    # - Small Meaning: Add 64-bit memory quadword into accumulator
    # - Long Form    : Add memory content to Register A Extended
    # - Why We Used It: High-throughput 64-bit accumulation without register spill.
    # --------------------------------------------------------------------------
    add rax, [rsi]
    add rsi, 8                  # Advance pointer by 8 bytes (64 bits)
    dec rcx
    jnz .L_sum_loop

.L_sum_done:
    pop rsi
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_linear_search
# - Small Meaning: Performs linear scan for a 64-bit target key in array.
# - Long Form    : Assembly 64-Bit Linear Search
# - Why We Used It: Scans active squadron radar targets to match enemy craft UID.
# Calling Convention (Microsoft x64 ABI):
#   RCX = const int64_t* arr (Pointer to array)
#   RDX = int64_t size       (Number of elements)
#   R8  = int64_t target     (Target value to locate)
# Returns:
#   RAX = Index of target (0-based) if found, or -1 if absent.
# ==============================================================================
.global asm_linear_search
.global _asm_linear_search
asm_linear_search:
_asm_linear_search:
    push rbp
    mov rbp, rsp

    xor rax, rax                # RAX = 0 (Loop index counter i)

.L_search_loop:
    cmp rax, rdx
    jge .L_search_not_found     # Reached end of array without match

    # --------------------------------------------------------------------------
    # CMP [RCX + RAX*8], R8:
    # - Small Meaning: Compare target with element using 64-bit SIB addressing
    # - Long Form    : Compare Scale-Index-Base [Base:RCX + Index:RAX * Scale:8]
    # - Why We Used It: Single-cycle memory operand fetch scaled by 8 bytes per quadword.
    # --------------------------------------------------------------------------
    cmp [rcx + rax*8], r8
    je .L_search_found          # Match found! RAX contains index.

    inc rax
    jmp .L_search_loop

.L_search_found:
    mov rsp, rbp
    pop rbp
    ret

.L_search_not_found:
    mov rax, -1                 # Sentinel return -1 (Not found)
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_reverse_array
# - Small Meaning: Inverts array element order in-place using two-pointer swap.
# - Long Form    : Assembly 64-Bit In-Place Array Reversal
# - Why We Used It: Reverses flight trajectory history and time-series replay frames.
# Calling Convention (Microsoft x64 ABI):
#   RCX = int64_t* arr (Pointer to start of array)
#   RDX = int64_t size (Number of elements)
# ==============================================================================
.global asm_reverse_array
.global _asm_reverse_array
asm_reverse_array:
_asm_reverse_array:
    push rbp
    mov rbp, rsp
    push rbx
    push rsi
    push rdi

    cmp rdx, 1
    jle .L_rev_done             # Arrays of size 0 or 1 are already reversed

    mov rsi, rcx                # RSI = Left pointer (&arr[0])
    # --------------------------------------------------------------------------
    # LEA RDI, [RCX + RDX*8 - 8]:
    # - Small Meaning: Calculate memory address of last quadword in array
    # - Long Form    : Load Effective Address of right boundary pointer
    # - Why We Used It: Calculates &arr[size - 1] in one clock cycle without multiplication.
    # --------------------------------------------------------------------------
    lea rdi, [rcx + rdx*8 - 8]  # RDI = Right pointer (&arr[size - 1])

.L_rev_loop:
    cmp rsi, rdi
    jge .L_rev_done             # Pointers met or crossed in middle

    # Swap 64-bit elements using RAX and R10
    mov rax, [rsi]              # RAX = *left
    mov r10, [rdi]              # R10 = *right
    mov [rsi], r10              # *left = *right
    mov [rdi], rax              # *right = *left

    add rsi, 8                  # Advance left pointer rightward (+8 bytes)
    sub rdi, 8                  # Advance right pointer leftward (-8 bytes)
    jmp .L_rev_loop

.L_rev_done:
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: asm_count_pos_neg
# - Small Meaning: Classifies and counts positive, negative, and zero quadwords.
# - Long Form    : Assembly 64-Bit Positive, Negative, and Zero Partition Counter
# - Why We Used It: Evaluates thruster vectors (forward positive, reverse negative, stationary zero).
# Calling Convention (Microsoft x64 ABI):
#   RCX = const int64_t* arr (Array pointer)
#   RDX = int64_t size       (Array size)
#   R8  = int64_t* pos       (Pointer to positive counter)
#   R9  = int64_t* neg       (Pointer to negative counter)
#   [RBP + 48] = int64_t* zero (5th parameter passed on stack)
# ==============================================================================
.global asm_count_pos_neg
.global _asm_count_pos_neg
asm_count_pos_neg:
_asm_count_pos_neg:
    push rbp
    mov rbp, rsp
    push rbx
    push rsi
    push rdi

    mov rsi, rcx                # RSI = array pointer
    mov rcx, rdx                # RCX = element counter
    xor rax, rax                # RAX = positive counter = 0
    xor rbx, rbx                # RBX = negative counter = 0
    xor r10, r10                # R10 = zero counter = 0

    test rcx, rcx
    jle .L_cnt_store

.L_cnt_loop:
    mov r11, [rsi]              # Load 64-bit quadword
    cmp r11, 0
    jg .L_cnt_pos
    jl .L_cnt_neg
    inc r10                     # Is zero
    jmp .L_cnt_next

.L_cnt_pos:
    inc rax                     # Increment positive count
    jmp .L_cnt_next

.L_cnt_neg:
    inc rbx                     # Increment negative count

.L_cnt_next:
    add rsi, 8                  # Advance by 8 bytes
    dec rcx
    jnz .L_cnt_loop

.L_cnt_store:
    test r8, r8
    jz .L_skip_p
    mov [r8], rax               # Store positive count
.L_skip_p:
    test r9, r9
    jz .L_skip_n
    mov [r9], rbx               # Store negative count
.L_skip_n:
    mov rdi, [rbp + 48]         # Load 5th parameter from stack shadow frame
    test rdi, rdi
    jz .L_cnt_exit
    mov [rdi], r10              # Store zero count

.L_cnt_exit:
    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

# ==============================================================================
# Routine: co2_demo_arrays
# - Small Meaning: Interactive master runner demonstrating all 5 array routines.
# - Long Form    : CO2 Demonstration Driver for 64-Bit Array Operations
# - Why We Used It: Executes comprehensive educational vivas and test suites.
# ==============================================================================
.global co2_demo_arrays
.global _co2_demo_arrays
co2_demo_arrays:
_co2_demo_arrays:
    push rbp
    mov rbp, rsp
    sub rsp, 64                 # Shadow space (32) + parameter alignment (32)
    push rbx
    push rsi
    push rdi

    # Print title
    lea rcx, [rip + array_demo_title]
    call print_str

    # Print initial array
    lea rcx, [rip + arr_initial_msg]
    mov rdx, [rip + demo_arr_size]
    call printf

    xor rbx, rbx
.L_print_init_loop:
    cmp rbx, [rip + demo_arr_size]
    jge .L_print_init_done
    lea rcx, [rip + arr_item_fmt]
    lea r10, [rip + demo_arr]
    mov rdx, [r10 + rbx*8]
    call printf
    inc rbx
    jmp .L_print_init_loop

.L_print_init_done:
    lea rcx, [rip + arr_close_fmt]
    call print_str

    # 3. Find Min and Max
    lea rcx, [rip + demo_arr]
    mov rdx, [rip + demo_arr_size]
    lea r8, [rip + min_val]
    lea r9, [rip + max_val]
    call asm_find_min_max

    lea rcx, [rip + min_max_fmt]
    mov rdx, [rip + min_val]
    mov r8,  [rip + max_val]
    call printf

    # 4. Array Sum & Average
    lea rcx, [rip + demo_arr]
    mov rdx, [rip + demo_arr_size]
    call asm_array_sum
    mov rbx, rax                # RBX = sum

    mov rax, rbx
    cqo                         # Sign-extend RAX into RDX:RAX for 64-bit IDIV
    idiv qword ptr [rip + demo_arr_size] # RAX = average, RDX = remainder

    lea rcx, [rip + sum_fmt]
    mov rdx, rbx
    mov r8,  rax
    call printf

    # 7. Linear Search
    lea rcx, [rip + search_prompt]
    call print_str
    call read_int               # Returns 64-bit int in RAX
    mov rbx, rax                # RBX = target to search

    lea rcx, [rip + demo_arr]
    mov rdx, [rip + demo_arr_size]
    mov r8,  rbx
    call asm_linear_search

    cmp rax, -1
    je .L_demo_search_miss
    lea rcx, [rip + search_found_fmt]
    mov rdx, rbx
    mov r8,  rax
    call printf
    jmp .L_demo_do_rev

.L_demo_search_miss:
    lea rcx, [rip + search_notfound_fmt]
    mov rdx, rbx
    call printf

.L_demo_do_rev:
    # 8. Reversing Array
    lea rcx, [rip + demo_arr]
    mov rdx, [rip + demo_arr_size]
    call asm_reverse_array

    lea rcx, [rip + rev_msg]
    call print_str

    xor rbx, rbx
.L_print_rev_loop:
    cmp rbx, [rip + demo_arr_size]
    jge .L_print_rev_done
    lea rcx, [rip + arr_item_fmt]
    lea r10, [rip + demo_arr]
    mov rdx, [r10 + rbx*8]
    call printf
    inc rbx
    jmp .L_print_rev_loop

.L_print_rev_done:
    lea rcx, [rip + arr_close_fmt]
    call print_str

    # Restore array order
    lea rcx, [rip + demo_arr]
    mov rdx, [rip + demo_arr_size]
    call asm_reverse_array

    # 9. Count Positive, Negative, Zero
    lea rcx, [rip + demo_arr]
    mov rdx, [rip + demo_arr_size]
    lea r8,  [rip + pos_cnt]
    lea r9,  [rip + neg_cnt]
    lea r10, [rip + zero_cnt]
    mov [rsp + 32], r10         # 5th parameter on stack
    call asm_count_pos_neg

    lea rcx, [rip + counts_fmt]
    mov rdx, [rip + pos_cnt]
    mov r8,  [rip + neg_cnt]
    mov r9,  [rip + zero_cnt]
    call printf

    pop rdi
    pop rsi
    pop rbx
    mov rsp, rbp
    pop rbp
    ret
