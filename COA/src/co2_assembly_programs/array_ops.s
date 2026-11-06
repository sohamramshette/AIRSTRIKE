# ==============================================================================
# AIRSTRIKE 80386 Assembly Suite - CO2: Array Programs
# Implements:
#   3. Largest and Smallest in an array
#   4. Sum of array elements
#   7. Linear Search in an array
#   8. Reversing an array in-place
#   9. Counting Positive, Negative, and Zero elements
# GNU Assembler (GAS) - Intel Syntax (noprefix)
# Target: Intel 80386 (32-bit cdecl)
# ==============================================================================

.intel_syntax noprefix

.data
array_demo_title:
    .asciz "\n--- CO2 Programs 3, 4, 7, 8, 9: Array Operations Demonstration ---\n"
arr_initial_msg:
    .asciz "  Initial Array Elements (Size %d): [ "
arr_item_fmt:
    .asciz "%d "
arr_close_fmt:
    .asciz "]\n"

min_max_fmt:
    .asciz "  3. Largest and Smallest: Minimum = %d, Maximum = %d\n"
sum_fmt:
    .asciz "  4. Array Sum           : Total Sum = %d (Average = %d)\n"
search_prompt:
    .asciz "  Enter integer to linearly search in array: "
search_found_fmt:
    .asciz "  7. Linear Search Result: Target %d FOUND at index %d!\n"
search_notfound_fmt:
    .asciz "  7. Linear Search Result: Target %d NOT FOUND (Index -1).\n"
rev_msg:
    .asciz "  8. Reversed Array      : [ "
counts_fmt:
    .asciz "  9. Element Counts      : Positive = %d, Negative = %d, Zeros = %d\n"

# Preset test array for demonstration
demo_arr:
    .long 45, -12, 88, 0, -34, 102, -5, 23
demo_arr_size:
    .long 8

.bss
.lcomm min_val, 4
.lcomm max_val, 4
.lcomm pos_cnt, 4
.lcomm neg_cnt, 4
.lcomm zero_cnt, 4

.text

# ------------------------------------------------------------------------------
# asm_find_min_max: Finds smallest and largest 32-bit signed integer in array
# Prototype: void asm_find_min_max(const int* arr, int size, int* out_min, int* out_max)
# ------------------------------------------------------------------------------
.global _asm_find_min_max
_asm_find_min_max:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov esi, [ebp + 8]          # esi = array pointer
    mov ecx, [ebp + 12]         # ecx = size

    cmp ecx, 0
    jle .L_minmax_exit

    mov eax, [esi]              # eax = current min
    mov edx, [esi]              # edx = current max
    add esi, 4
    dec ecx
    jz .L_minmax_store

.L_minmax_loop:
    mov ebx, [esi]
    cmp ebx, eax
    jge .L_not_min
    mov eax, ebx                # update min
.L_not_min:
    cmp ebx, edx
    jle .L_not_max
    mov edx, ebx                # update max
.L_not_max:
    add esi, 4
    dec ecx
    jnz .L_minmax_loop

.L_minmax_store:
    mov edi, [ebp + 16]         # edi = out_min
    test edi, edi
    jz .L_skip_min
    mov [edi], eax
.L_skip_min:
    mov edi, [ebp + 20]         # edi = out_max
    test edi, edi
    jz .L_minmax_exit
    mov [edi], edx

.L_minmax_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_array_sum: Computes sum of 32-bit signed integers in array
# Prototype: int asm_array_sum(const int* arr, int size)
# Returns: eax = total sum
# ------------------------------------------------------------------------------
.global _asm_array_sum
_asm_array_sum:
    push ebp
    mov ebp, esp
    push ebx
    push esi

    mov esi, [ebp + 8]          # esi = array pointer
    mov ecx, [ebp + 12]         # ecx = count
    xor eax, eax                # eax = sum accumulator

    test ecx, ecx
    jle .L_sum_done

.L_sum_loop:
    add eax, [esi]
    add esi, 4
    dec ecx
    jnz .L_sum_loop

.L_sum_done:
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_linear_search: Searches for target integer in array
# Prototype: int asm_linear_search(const int* arr, int size, int target)
# Returns: eax = index if found, or -1 if not found
# ------------------------------------------------------------------------------
.global _asm_linear_search
_asm_linear_search:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov esi, [ebp + 8]          # esi = array pointer
    mov ecx, [ebp + 12]         # ecx = size
    mov edx, [ebp + 16]         # edx = target
    xor ebx, ebx                # ebx = current index

.L_search_loop:
    cmp ebx, ecx
    jge .L_search_not_found
    cmp [esi + ebx*4], edx
    je .L_search_found
    inc ebx
    jmp .L_search_loop

.L_search_found:
    mov eax, ebx
    jmp .L_search_exit

.L_search_not_found:
    mov eax, -1

.L_search_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_reverse_array: Reverses an array in-place using two-pointer swap
# Prototype: void asm_reverse_array(int* arr, int size)
# ------------------------------------------------------------------------------
.global _asm_reverse_array
_asm_reverse_array:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov esi, [ebp + 8]          # esi = left pointer (&arr[0])
    mov ecx, [ebp + 12]         # ecx = size
    cmp ecx, 1
    jle .L_rev_done

    lea edi, [esi + ecx*4 - 4]  # edi = right pointer (&arr[size-1])

.L_rev_loop:
    cmp esi, edi
    jge .L_rev_done

    mov eax, [esi]
    mov ebx, [edi]
    mov [esi], ebx              # swap
    mov [edi], eax

    add esi, 4
    sub edi, 4
    jmp .L_rev_loop

.L_rev_done:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# asm_count_pos_neg: Counts positive (>0), negative (<0), and zero elements
# Prototype: void asm_count_pos_neg(const int* arr, int size, int* pos, int* neg, int* zero)
# ------------------------------------------------------------------------------
.global _asm_count_pos_neg
_asm_count_pos_neg:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    mov esi, [ebp + 8]          # esi = arr
    mov ecx, [ebp + 12]         # ecx = size

    xor eax, eax                # eax = pos count
    xor ebx, ebx                # ebx = neg count
    xor edx, edx                # edx = zero count

    test ecx, ecx
    jle .L_cnt_store

.L_cnt_loop:
    mov edi, [esi]
    cmp edi, 0
    jg .L_cnt_pos
    jl .L_cnt_neg
    inc edx                     # zero
    jmp .L_cnt_next

.L_cnt_pos:
    inc eax
    jmp .L_cnt_next

.L_cnt_neg:
    inc ebx

.L_cnt_next:
    add esi, 4
    dec ecx
    jnz .L_cnt_loop

.L_cnt_store:
    mov edi, [ebp + 16]         # out pos
    test edi, edi
    jz .L_skip_p
    mov [edi], eax
.L_skip_p:
    mov edi, [ebp + 20]         # out neg
    test edi, edi
    jz .L_skip_n
    mov [edi], ebx
.L_skip_n:
    mov edi, [ebp + 24]         # out zero
    test edi, edi
    jz .L_cnt_exit
    mov [edi], edx

.L_cnt_exit:
    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret

# ------------------------------------------------------------------------------
# co2_demo_arrays: Master demonstration for array operations
# ------------------------------------------------------------------------------
.global _co2_demo_arrays
_co2_demo_arrays:
    push ebp
    mov ebp, esp
    push ebx
    push esi
    push edi

    push offset array_demo_title
    call _print_str
    add esp, 4

    # Print initial array
    push [demo_arr_size]
    push offset arr_initial_msg
    call _printf
    add esp, 8

    xor ebx, ebx
.L_print_init_loop:
    cmp ebx, [demo_arr_size]
    jge .L_print_init_done
    push [demo_arr + ebx*4]
    push offset arr_item_fmt
    call _printf
    add esp, 8
    inc ebx
    jmp .L_print_init_loop

.L_print_init_done:
    push offset arr_close_fmt
    call _print_str
    add esp, 4

    # 3. Find Min and Max
    push offset max_val
    push offset min_val
    push [demo_arr_size]
    push offset demo_arr
    call _asm_find_min_max
    add esp, 16

    push [max_val]
    push [min_val]
    push offset min_max_fmt
    call _printf
    add esp, 12

    # 4. Array Sum & Average
    push [demo_arr_size]
    push offset demo_arr
    call _asm_array_sum
    add esp, 8
    mov ebx, eax                # ebx = sum

    mov eax, ebx
    cdq
    idiv dword ptr [demo_arr_size] # eax = average

    push eax
    push ebx
    push offset sum_fmt
    call _printf
    add esp, 12

    # 7. Linear Search
    push offset search_prompt
    call _print_str
    add esp, 4
    call _read_int
    mov ebx, eax                # ebx = target to search

    push ebx
    push [demo_arr_size]
    push offset demo_arr
    call _asm_linear_search
    add esp, 12

    cmp eax, -1
    je .L_demo_search_miss
    push eax
    push ebx
    push offset search_found_fmt
    call _printf
    add esp, 12
    jmp .L_demo_do_rev

.L_demo_search_miss:
    push ebx
    push offset search_notfound_fmt
    call _printf
    add esp, 8

.L_demo_do_rev:
    # 8. Reversing Array
    push [demo_arr_size]
    push offset demo_arr
    call _asm_reverse_array
    add esp, 8

    push offset rev_msg
    call _print_str
    add esp, 4

    xor ebx, ebx
.L_print_rev_loop:
    cmp ebx, [demo_arr_size]
    jge .L_print_rev_done
    push [demo_arr + ebx*4]
    push offset arr_item_fmt
    call _printf
    add esp, 8
    inc ebx
    jmp .L_print_rev_loop

.L_print_rev_done:
    push offset arr_close_fmt
    call _print_str
    add esp, 4

    # Restore original array order
    push [demo_arr_size]
    push offset demo_arr
    call _asm_reverse_array
    add esp, 8

    # 9. Count Positive, Negative, Zero
    push offset zero_cnt
    push offset neg_cnt
    push offset pos_cnt
    push [demo_arr_size]
    push offset demo_arr
    call _asm_count_pos_neg
    add esp, 20

    push [zero_cnt]
    push [neg_cnt]
    push [pos_cnt]
    push offset counts_fmt
    call _printf
    add esp, 16

    pop edi
    pop esi
    pop ebx
    mov esp, ebp
    pop ebp
    ret
