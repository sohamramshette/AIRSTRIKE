# ==============================================================================
# File    : COA/src/co2_assembly_programs/array_ops.s
# Subject : COA-CO2 | Programs 3, 4, 7, 8, 9: Array Operations
# Topics  : Array Min/Max, Array Sum & Average, Linear Search, In-Place Array
#           Reversal (Two-Pointer Swap), Positive/Negative/Zero Element Count
# Target  : x86-64 (64-bit Windows ABI, GAS Intel Syntax)
# ==============================================================================

.intel_syntax noprefix

.data
.align 8
array_demo_title: .asciz "\n--- CO2 Programs 3, 4, 7, 8, 9: 64-Bit Array Operations ---\n"
arr_initial_msg:  .asciz "  Initial Array (Size %lld): [ "
arr_item_fmt:     .asciz "%lld "
arr_close_fmt:    .asciz "]\n"
min_max_fmt:      .asciz "  3. Min & Max        : Min = %lld, Max = %lld\n"
sum_fmt:          .asciz "  4. Sum & Average    : Sum = %lld, Avg = %lld\n"
search_prompt:    .asciz "  Enter target value to search (default 88): "
search_found_fmt: .asciz "  7. Linear Search    : Target %lld FOUND at index %lld\n"
search_miss_fmt:  .asciz "  7. Linear Search    : Target %lld NOT FOUND (-1)\n"
rev_msg:          .asciz "  8. Reversed Array   : [ "
counts_fmt:       .asciz "  9. Sign Counters    : Pos = %lld, Neg = %lld, Zero = %lld\n"

demo_arr:
    .quad 45, -12, 88, 0, -34, 102, -5, 23
demo_arr_size:
    .quad 8

.bss
.align 8
.lcomm min_val,  8
.lcomm max_val,  8
.lcomm pos_cnt,  8
.lcomm neg_cnt,  8
.lcomm zero_cnt, 8

.text

# ------------------------------------------------------------------------------
# [TOPIC: Array Minimum & Maximum] asm_find_min_max
# Purpose   : Scans array in one pass to extract minimum and maximum quadwords.
# Game Role : Finds critical (lowest) ship HP and peak score/shield in telemetry.
# Inputs    : RCX = arr, RDX = size, R8 = &out_min, R9 = &out_max
# ------------------------------------------------------------------------------
.global asm_find_min_max
.global _asm_find_min_max
asm_find_min_max:
_asm_find_min_max:
    push rbx
    push rsi

    cmp  rdx, 0                 # WHY: Guard against empty array to avoid out-of-bounds memory read
    jle  .L_mm_exit

    mov  rsi, rcx
    mov  rcx, rdx
    mov  rax, [rsi]             # WHY: Seed both min (RAX) and max (RBX) with arr[0] so negative arrays work accurately
    mov  rbx, [rsi]
    add  rsi, 8                 # WHY: Advance pointer by 8 bytes (sizeof 64-bit quadword)
    dec  rcx
    jz   .L_mm_store            # WHY: Size-1 array is already finished

.L_mm_loop:
    mov  r10, [rsi]
    cmp  r10, rax               # WHY: Signed JGE skips min update unless element < current min
    jge  .L_not_min
    mov  rax, r10
.L_not_min:
    cmp  r10, rbx               # WHY: Signed JLE skips max update unless element > current max
    jle  .L_not_max
    mov  rbx, r10
.L_not_max:
    add  rsi, 8
    dec  rcx
    jnz  .L_mm_loop

.L_mm_store:
    test r8, r8
    jz   .L_skip_min
    mov  [r8], rax
.L_skip_min:
    test r9, r9
    jz   .L_mm_exit
    mov  [r9], rbx

.L_mm_exit:
    pop  rsi
    pop  rbx
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Array Sum] asm_array_sum
# Purpose   : Computes cumulative sum of 64-bit array elements.
# Game Role : Totals wave damage dealt or combined fleet shield strength.
# Inputs    : RCX = arr, RDX = size
# Output    : RAX = total sum
# ------------------------------------------------------------------------------
.global asm_array_sum
.global _asm_array_sum
asm_array_sum:
_asm_array_sum:
    push rsi

    mov  rsi, rcx
    mov  rcx, rdx
    xor  rax, rax               # WHY: Clear accumulator RAX=0 using XOR (faster than mov rax, 0)
    test rcx, rcx
    jle  .L_sum_exit

.L_sum_loop:
    add  rax, [rsi]             # WHY: Accumulate directly from memory operand into RAX without temp register
    add  rsi, 8                 # WHY: Step +8 bytes to next 64-bit score/damage record
    dec  rcx
    jnz  .L_sum_loop

.L_sum_exit:
    pop  rsi
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Linear Search] asm_linear_search
# Purpose   : Searches array using scaled index addressing [rcx + rax*8].
# Game Role : Locates an active enemy UID or target score in radar array.
# Inputs    : RCX = arr, RDX = size, R8 = target
# Output    : RAX = 0-based index if found, or -1
# ------------------------------------------------------------------------------
.global asm_linear_search
.global _asm_linear_search
asm_linear_search:
_asm_linear_search:
    xor  rax, rax               # WHY: RAX acts as loop index i=0 AND return register on hit

.L_search_loop:
    cmp  rax, rdx
    jge  .L_search_miss
    cmp  [rcx + rax*8], r8      # WHY: Scaled-index [base + i*8] checks arr[i] == target without advancing extra pointers
    je   .L_search_hit
    inc  rax
    jmp  .L_search_loop

.L_search_hit:
    ret
.L_search_miss:
    mov  rax, -1                # WHY: Sentinel -1 tells game engine target was not found on radar
    ret

# ------------------------------------------------------------------------------
# [TOPIC: In-Place Array Reversal (Two-Pointer Swap)] asm_reverse_array
# Purpose   : Reverses array in-place by swapping left (RSI) and right (RDI).
# Game Role : Reverses stored flight trajectory paths & replay frame buffers in O(1) space.
# Inputs    : RCX = arr, RDX = size
# ------------------------------------------------------------------------------
.global asm_reverse_array
.global _asm_reverse_array
asm_reverse_array:
_asm_reverse_array:
    push rsi
    push rdi

    cmp  rdx, 1                 # WHY: Arrays of size 0 or 1 need no swap
    jle  .L_rev_done

    mov  rsi, rcx                   # Left pointer = &arr[0]
    lea  rdi, [rcx + rdx*8 - 8]     # WHY: LEA computes &arr[size-1] in 1 cycle with zero multiplication overhead

.L_rev_loop:
    cmp  rsi, rdi               # WHY: Stop once left pointer meets or crosses right pointer
    jge  .L_rev_done

    mov  rax, [rsi]             # WHY: Swap *left and *right in-place using registers (no extra heap array)
    mov  r10, [rdi]
    mov  [rsi], r10
    mov  [rdi], rax

    add  rsi, 8                 # WHY: Move left pointer forward (+1 quadword)
    sub  rdi, 8                 # WHY: Move right pointer backward (-1 quadword)
    jmp  .L_rev_loop

.L_rev_done:
    pop  rdi
    pop  rsi
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Positive / Negative / Zero Counters] asm_count_pos_neg
# Purpose   : Counts positive (>0), negative (<0), and zero (==0) elements.
# Game Role : Classifies thruster vector telemetry (forward +, reverse -, idle 0).
# Inputs    : RCX = arr, RDX = size, R8 = &pos, R9 = &neg, [rbp+48] = &zero
# ------------------------------------------------------------------------------
.global asm_count_pos_neg
.global _asm_count_pos_neg
asm_count_pos_neg:
_asm_count_pos_neg:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    push rdi

    mov  rsi, rcx
    mov  rcx, rdx
    xor  rax, rax               # pos count
    xor  rbx, rbx               # neg count
    xor  r10, r10               # zero count

    test rcx, rcx
    jle  .L_cnt_store

.L_cnt_loop:
    mov  r11, [rsi]
    cmp  r11, 0                 # WHY: Single comparison sets flags for both JG (>0) and JL (<0); fall-through is 0
    jg   .L_is_pos
    jl   .L_is_neg
    inc  r10
    jmp  .L_cnt_next
.L_is_pos:
    inc  rax
    jmp  .L_cnt_next
.L_is_neg:
    inc  rbx
.L_cnt_next:
    add  rsi, 8
    dec  rcx
    jnz  .L_cnt_loop

.L_cnt_store:
    test r8, r8
    jz   .L_skip_pos
    mov  [r8], rax
.L_skip_pos:
    test r9, r9
    jz   .L_skip_neg
    mov  [r9], rbx
.L_skip_neg:
    mov  rdi, [rbp + 48]        # WHY: 5th argument (&zero) arrives on stack per Windows x64 ABI
    test rdi, rdi
    jz   .L_cnt_done
    mov  [rdi], r10

.L_cnt_done:
    pop  rdi
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# Internal helper: run_arrays_with_target(RCX = searchTarget)
# ------------------------------------------------------------------------------
run_arrays_with_target:
    push rbp
    mov  rbp, rsp
    push rbx
    push rsi
    sub  rsp, 48

    mov  rsi, rcx

    lea  rcx, [rip + array_demo_title]
    call print_str

    lea  rcx, [rip + arr_initial_msg]
    mov  rdx, [rip + demo_arr_size]
    call printf

    xor  rbx, rbx
.L_pr_init:
    cmp  rbx, [rip + demo_arr_size]
    jge  .L_pr_init_end
    lea  rcx, [rip + arr_item_fmt]
    lea  r10, [rip + demo_arr]
    mov  rdx, [r10 + rbx*8]
    call printf
    inc  rbx
    jmp  .L_pr_init
.L_pr_init_end:
    lea  rcx, [rip + arr_close_fmt]
    call print_str

    lea  rcx, [rip + demo_arr]
    mov  rdx, [rip + demo_arr_size]
    lea  r8,  [rip + min_val]
    lea  r9,  [rip + max_val]
    call asm_find_min_max

    lea  rcx, [rip + min_max_fmt]
    mov  rdx, [rip + min_val]
    mov  r8,  [rip + max_val]
    call printf

    lea  rcx, [rip + demo_arr]
    mov  rdx, [rip + demo_arr_size]
    call asm_array_sum
    mov  rbx, rax
    cqo                         # WHY: Sign-extend sum before dividing by element count for average
    idiv qword ptr [rip + demo_arr_size]

    lea  rcx, [rip + sum_fmt]
    mov  rdx, rbx
    mov  r8,  rax
    call printf

    lea  rcx, [rip + demo_arr]
    mov  rdx, [rip + demo_arr_size]
    mov  r8,  rsi
    call asm_linear_search
    cmp  rax, -1
    je   .L_arr_miss
    lea  rcx, [rip + search_found_fmt]
    mov  rdx, rsi
    mov  r8,  rax
    call printf
    jmp  .L_arr_rev
.L_arr_miss:
    lea  rcx, [rip + search_miss_fmt]
    mov  rdx, rsi
    call printf

.L_arr_rev:
    lea  rcx, [rip + demo_arr]
    mov  rdx, [rip + demo_arr_size]
    call asm_reverse_array

    lea  rcx, [rip + rev_msg]
    call print_str
    xor  rbx, rbx
.L_pr_rev:
    cmp  rbx, [rip + demo_arr_size]
    jge  .L_pr_rev_end
    lea  rcx, [rip + arr_item_fmt]
    lea  r10, [rip + demo_arr]
    mov  rdx, [r10 + rbx*8]
    call printf
    inc  rbx
    jmp  .L_pr_rev
.L_pr_rev_end:
    lea  rcx, [rip + arr_close_fmt]
    call print_str

    lea  rcx, [rip + demo_arr]
    mov  rdx, [rip + demo_arr_size]
    call asm_reverse_array      # WHY: Restore original order so repeated runs stay consistent

    lea  rcx, [rip + demo_arr]
    mov  rdx, [rip + demo_arr_size]
    lea  r8,  [rip + pos_cnt]
    lea  r9,  [rip + neg_cnt]
    lea  r10, [rip + zero_cnt]
    mov  [rsp + 32], r10
    call asm_count_pos_neg

    lea  rcx, [rip + counts_fmt]
    mov  rdx, [rip + pos_cnt]
    mov  r8,  [rip + neg_cnt]
    mov  r9,  [rip + zero_cnt]
    call printf

    add  rsp, 48
    pop  rsi
    pop  rbx
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Interactive Array Demo] co2_demo_arrays
# Purpose   : Prompts user for a search key (defaults to 88 on EOF) and runs demos.
# Game Role : Interactive test of array telemetry programs 3, 4, 7, 8, and 9.
# ------------------------------------------------------------------------------
.global co2_demo_arrays
.global _co2_demo_arrays
co2_demo_arrays:
_co2_demo_arrays:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    lea  rcx, [rip + search_prompt]
    call print_str
    call read_int
    cmp  rax, -1
    jne  .L_arr_use_input
    mov  rax, 88
.L_arr_use_input:
    mov  rcx, rax
    call run_arrays_with_target

    add  rsp, 32
    pop  rbp
    ret

# ------------------------------------------------------------------------------
# [TOPIC: Automated Array Demo] co2_demo_arrays_batch
# Purpose   : Non-interactive array demo searching for key 88.
# Game Role : Powers batch execution without pausing for stdin input.
# ------------------------------------------------------------------------------
.global co2_demo_arrays_batch
.global _co2_demo_arrays_batch
co2_demo_arrays_batch:
_co2_demo_arrays_batch:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32

    mov  rcx, 88
    call run_arrays_with_target

    add  rsp, 32
    pop  rbp
    ret
