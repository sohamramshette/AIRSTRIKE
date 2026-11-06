# AIRSTRIKE Phase 4 & 4.1: Computer Organization & Architecture (COA)
## Genuine 32-bit Intel 80386 Protected-Mode Assembly Implementation & Architectural Audit

---

## 1. Executive Summary & Technical Compliance

Phase 4 implements the entire **Computer Organization and Architecture (COA)** curriculum from first principles using pure **32-bit Intel 80386 Assembly Language**. Phase 4.1 audits the module for architectural precision, distinguishing physical 80386 hardware mechanisms from educational microarchitecture models.

### 1.1 Strict Compliance Verification
* **Target Architecture**: Genuine Intel 80386 in 32-bit Protected Mode (Flat Memory Model).
* **Instruction Set**: Exclusively standard 80386 instructions. Absolutely **no x86-64 instructions or registers** (no `RAX`, `RBX`, `RCX`, `RDX`, `R8`-`R15`, or 64-bit prefixes).
* **Assembler & Toolchain**: GNU Assembler (GAS) using Intel Syntax (`.intel_syntax noprefix`), compiled and linked via MinGW GCC (`gcc -m32`) into a standalone 32-bit PE binary: `bin/coa/coa_runner.exe`.
* **Algorithmic Authenticity**: Core computer arithmetic (Booth's signed multiplication, Restoring division, multi-precision addition, pipeline simulation) is implemented **100% in pure 80386 assembly instructions**. Standard C library functions (`_printf`, `_scanf`, `_putchar`) are restricted strictly to console string and number formatting.
* **Non-Interference**: All previous Java (`PSOOP`) and C++ (`PL`) modules remain completely intact and continue to pass 100% of their test suites.

---

## 2. Architecture & Course Outcome Mapping

| Outcome ID | Description | Source Files | Test Suite Coverage | Status |
| :--- | :--- | :--- | :--- | :--- |
| **COA-CO1** | 80386 Architecture & Instruction Set | `COA/src/co1_architecture/arch_demo.s` | 6 Assertions (`test_coa.s`) | **Completed (Phase 4.1 Audited)** |
| **COA-CO2** | 10 Assembly Language Programs | `COA/src/co2_assembly_programs/*.s` | 15 Assertions (`test_coa.s`) | **Completed (Phase 4.1 Audited)** |
| **COA-CO3** | Computer Arithmetic (Booth & Restoring) | `COA/src/co3_arithmetic/*.s` | 18 Assertions (`test_coa.s`) | **Completed (Phase 4.1 Audited)** |
| **COA-CO4** | Control Unit & 5-Stage Pipelining | `COA/src/co4_control_unit/*.s` | 2 Assertions (`test_coa.s`) | **Completed (Phase 4.1 Audited)** |

---

## 3. COA-CO1: 80386 Architecture and Instruction Set

### 3.1 Register Hierarchy and Sub-Register Partitioning
The Intel 80386 introduced full 32-bit general-purpose registers while maintaining backward compatibility with the 16-bit 8086/80286 architecture through register aliasing. In `COA/src/co1_architecture/arch_demo.s`, sub-register partitioning is verified:

```
+-----------------------------------+
|               EAX (32 bits)       |
+-----------------+-----------------+
                  |   AX (16 bits)  |
                  +--------+--------+
                  |AH (8b) |AL (8b) |
                  +--------+--------+
```

When `0xAABBCCDD` is loaded into `EAX`:
* `AX` references the lower 16 bits: `0xCCDD`.
* `AH` references the high byte of `AX`: `0xCC`.
* `AL` references the low byte of `AX`: `0xDD`.

### 3.2 Index and Pointer Registers
* **`ESI` (Source Index)** & **`EDI` (Destination Index)**: 32-bit registers utilized for high-speed string and block memory transfer operations (`MOVSB`, `MOVSD`).
* **`EBP` (Base Pointer)**: Establishes the stable base of the procedure activation record (stack frame) for referencing parameters (`[ebp + 8]`) and local stack variables (`[ebp - 4]`).
* **`ESP` (Stack Pointer)**: Dynamically points to the top of the stack. On the 80386, the stack grows downwards towards lower memory addresses. Pushing a 32-bit dword decrements `ESP` by 4 bytes; popping increments `ESP` by 4 bytes.

### 3.3 EFLAGS Register & Condition Codes
The 32-bit `EFLAGS` register contains single-bit status flags reflecting the outcome of ALU instructions:
* **Carry Flag (CF)**: Set when an unsigned arithmetic operation overflows the register width (e.g., `0xFFFFFFFF + 1` produces `CF = 1`).
* **Zero Flag (ZF)**: Set when the arithmetic or logical result is zero (e.g., `100 - 100` produces `ZF = 1`).
* **Sign Flag (SF)**: Equal to the most significant bit (MSB) of the result, indicating negative numbers in two's complement representation (`SF = 1`).
* **Overflow Flag (OF)**: Set when a signed arithmetic operation yields a result whose sign is mathematically impossible (e.g., `Pos + Pos = Neg` or `Neg + Neg = Pos`).

### 3.4 32-bit Protected Mode Memory Segmentation & Flat Model Distinction
In 32-bit Protected Mode, segment registers (`CS`, `DS`, `SS`, `ES`) store 16-bit **Segment Selectors**:
```
 15                               3   2   1  0
+-------------------------------+---+------+
|       Descriptor Index        | TI|  RPL |
+-------------------------------+---+------+
```
* **RPL (Requested Privilege Level)**: Bits 0–1 set to `3` in user space (Ring 3).
* **TI (Table Indicator)**: Bit 2 (`0 = GDT`, `1 = LDT`).
* **Descriptor Index**: Bits 3–15 index the Global Descriptor Table.

#### Crucial Architectural Distinction:
1. **Underlying Hardware Mechanism**: The 80386 hardware **always** evaluates memory accesses through segment descriptors (`Linear Address = Base + Offset`).
2. **Flat Model Abstraction**: Under modern 32-bit operating systems (Windows/Linux), the OS kernel configures GDT descriptors for code and data with `Base = 0x00000000` and `Limit = 0xFFFFFFFF` (4 GB). This causes the logical offset to map 1:1 to the 32-bit linear address.
3. **User-Space Audit Boundary**: In Ring 3 user mode, applications read selector registers (`mov ax, cs` yields `0x0023`, `mov bx, ds` yields `0x002B`). The application displays active selector values, but **cannot** independently inspect OS kernel GDT descriptor tables, modify descriptor limits, or prove physical 4 GB RAM boundaries without Ring 0 supervisor privileges.

### 3.5 Five Core 80386 Addressing Modes
The 80386 features the Scaled-Index-Base (SIB) addressing engine. Demonstrated in `arch_demo.s`:
1. **Immediate**: Operand is encoded directly within the instruction stream:
   `mov eax, 450`
2. **Register**: Operand is stored directly in a processor register:
   `mov ebx, eax`
3. **Direct (Displacement only)**: Accesses a fixed global memory location:
   `mov eax, [sample_var]`
4. **Register Indirect**: Memory address is held in a pointer register:
   `mov eax, [esi]`
5. **Based-Indexed-Scaled (SIB)**: Evaluates complex array and struct indexing in a single hardware cycle:
   $$\text{Effective Address} = \text{Base} + (\text{Index} \times \text{Scale}) + \text{Displacement}$$
   `mov eax, [ebx + esi*4 + 8]`

---

## 4. COA-CO2: 10 Core 80386 Assembly Programs

All 10 required assembly programs are implemented across modular source files in `COA/src/co2_assembly_programs/`:

```
COA/src/co2_assembly_programs/
├── arithmetic_ops.s   # Prog 1 (Add/Sub) & Prog 2 (Mul/Div with Remainder & Div-by-zero protection)
├── array_ops.s        # Progs 3, 4, 7, 8, 9 (Min/Max, Sum, Search, Reverse, Counters)
├── math_series.s      # Prog 5 (Recursive Factorial) & Prog 6 (Fibonacci Series)
├── logic_ops.s        # Prog 10 (Bitwise Telemetry Masks: AND, OR, XOR, NOT)
└── co2_menu.s         # Interactive submenu & batch runner
```

### 4.1 Program 1: Addition & Subtraction (`arithmetic_ops.s`)
* **`asm_add(a, b)`**: Executes `mov eax, [ebp+8]; add eax, [ebp+12]; ret`.
* **`asm_sub(a, b)`**: Executes `mov eax, [ebp+8]; sub eax, [ebp+12]; ret`.
* Demonstrates standard 32-bit two's complement integer operations.

### 4.2 Program 2: Signed Multiplication & Division (`arithmetic_ops.s`)
* **`asm_mul(a, b)`**: Executes standard two-operand signed multiplication `imul eax, [ebp+12]`.
* **`asm_div(dividend, divisor, *out_rem)`**:
  - Validates `divisor != 0`. If `divisor == 0`, zeroes `*out_rem` (if pointer is non-null), sets `eax = 0`, and returns safely without raising a CPU `#DE` (Divide Error) hardware exception.
  - For non-zero divisors, uses `cdq` (Convert Doubleword to Quadword) to sign-extend `EAX` into `EDX:EAX`.
  - Executes `idiv ebx`, storing Quotient in `EAX` and Remainder in `EDX`.

### 4.3 Program 3: Largest and Smallest in an Array (`array_ops.s`)
* **`asm_find_min_max(arr, size, *out_min, *out_max)`**:
  - Initializes `EAX` (min) and `EDX` (max) with the first array element `[esi]`.
  - Iterates through the array using `EBX = [esi]`.
  - Executes signed comparison `cmp ebx, eax; jl .L_update_min` and `cmp ebx, edx; jg .L_update_max`.

### 4.4 Program 4: Sum of Array Elements (`array_ops.s`)
* **`asm_array_sum(arr, size)`**:
  - Clears accumulator `xor eax, eax`.
  - Iterates through array adding `add eax, [esi]; add esi, 4`.
  - Returns 32-bit signed sum in `EAX`.

### 4.5 Program 5: Recursive Factorial (`math_series.s`)
* **`asm_factorial(n)`**:
  - Implements genuine recursive stack frames.
  - Base case: `cmp eax, 1; jle .L_base_case` returns 1.
  - Recursive step: decrements $N$, calls `_asm_factorial(n - 1)`.
  - Multiplies result on stack unwind: `imul eax, [ebp + 8]`.

### 4.6 Program 6: Fibonacci Series Generation (`math_series.s`)
* **`asm_fibonacci(out_arr, terms)`**:
  - Generates the classic Fibonacci sequence: $F(0) = 0, F(1) = 1, F(i) = F(i-1) + F(i-2)$.
  - Stores terms directly in memory using indexed addressing `[edi + 4]`.

### 4.7 Program 7: Linear Search in an Array (`array_ops.s`)
* **`asm_linear_search(arr, size, target)`**:
  - Scans array sequentially for `target`.
  - Returns 0-based index if found.
  - Returns `-1` if target is absent.

### 4.8 Program 8: In-place Array Reversal (`array_ops.s`)
* **`asm_reverse_array(arr, size)`**:
  - Implements two-pointer in-place reversal.
  - `ESI` points to array start (`arr[0]`), `EDI` points to array end (`arr[size-1]`).
  - Swaps values in registers: `mov eax, [esi]; mov edx, [edi]; mov [esi], edx; mov [edi], eax`.
  - Advances pointers: `add esi, 4; sub edi, 4` until `esi >= edi`.

### 4.9 Program 9: Positive, Negative, and Zero Counters (`array_ops.s`)
* **`asm_count_elements(arr, size, *pos, *neg, *zero)`**:
  - Inspects each element with `cmp dword ptr [esi], 0`.
  - Uses conditional jumps `jg` (positive), `jl` (negative), `je` (zero) to update dedicated counter registers.

### 4.10 Program 10: Bitwise Logic Operations (`logic_ops.s`)
* Demonstrates Spacecraft Telemetry and subsystem control:
  - `AND`: Query active subsystem bits using bitmasks.
  - `OR`: Enable hardware subsystems without altering existing state.
  - `XOR`: Toggle subsystem states (e.g., shields or weapons lock).
  - `NOT`: Invert entire telemetry status byte.

---

## 5. COA-CO3: Computer Arithmetic Algorithms

### 5.1 Booth's Signed Multiplication Algorithm
Standard shift-and-add binary multiplication fails for signed numbers represented in two's complement. Booth's Algorithm treats strings of consecutive 1s in the multiplier by replacing additions with a single subtraction at the start of the block and an addition at the end:
$$2^k + 2^{k-1} + \dots + 2^m = 2^{k+1} - 2^m$$

#### Algorithm Flow
1. Load 8-bit signed multiplicand into $M$.
2. Load 8-bit signed multiplier into $Q$.
3. Clear 8-bit accumulator $A = 0$, and previous bit $Q_{-1} = 0$.
4. Loop exactly 8 times (for 8-bit operands):
   - Inspect $(Q_0, Q_{-1})$:
     - `01`: $A \leftarrow A + M$
     - `10`: $A \leftarrow A - M$
     - `00` or `11`: No arithmetic operation
   - Arithmetic Shift Right the composite register $[A : Q : Q_{-1}]$ by 1 bit, preserving the sign bit of $A$.
5. The final 16-bit signed product resides in $[A : Q]$.

#### Worked Step-by-Step Example: Multiplicand $M = 7$ (`00000111`), Multiplier $Q = -3$ (`11111101`)
$$\text{Expected Product: } 7 \times (-3) = -21 \text{ (Two's complement 16-bit: } \texttt{0xFFEB}\text{)}$$

| Cycle | Operation | Accumulator $A$ | Multiplier $Q$ | $Q_{-1}$ | Action / Decision |
| :---: | :--- | :---: | :---: | :---: | :--- |
| **0** | Initial State | `00000000` | `11111101` | `0` | Initial State |
| **1** | $A = A - M$, ASHR | `11111100` | `11111110` | `1` | $Q_0, Q_{-1} = 10 \implies \text{Subtract } M$ |
| **2** | $A = A + M$, ASHR | `00000001` | `11111111` | `0` | $Q_0, Q_{-1} = 01 \implies \text{Add } M$ |
| **3** | $A = A - M$, ASHR | `11111101` | `01111111` | `1` | $Q_0, Q_{-1} = 10 \implies \text{Subtract } M$ |
| **4** | ASHR (No Add/Sub) | `11111110` | `10111111` | `1` | $Q_0, Q_{-1} = 11 \implies \text{Shift only}$ |
| **5** | ASHR (No Add/Sub) | `11111111` | `01011111` | `1` | $Q_0, Q_{-1} = 11 \implies \text{Shift only}$ |
| **6** | ASHR (No Add/Sub) | `11111111` | `10101111` | `1` | $Q_0, Q_{-1} = 11 \implies \text{Shift only}$ |
| **7** | ASHR (No Add/Sub) | `11111111` | `11010111` | `1` | $Q_0, Q_{-1} = 11 \implies \text{Shift only}$ |
| **8** | ASHR (No Add/Sub) | `11111111` | `11101011` | `1` | $Q_0, Q_{-1} = 11 \implies \text{Shift only}$ |

$$\text{Final Result: } [A:Q] = \texttt{11111111 11101011}_2 = \texttt{0xFFEB} = -21_{10} \implies \mathbf{MATCH\ (PASS)}$$

#### Boundary Asymmetry in Two's Complement:
In 8-bit two's complement, values span $[-128, +127]$. Multiplicand $M = -128$ has no positive 8-bit signed representation ($-(-128) = +128$, which overflows 8-bit range). The implementation preserves valid 8-bit signed boundaries (e.g., $-64 \times 2 = -128$, $0 \times 45 = 0$, $-25 \times 0 = 0$).

---

### 5.2 Restoring Binary Division Algorithm
Restoring division divides an 8-bit unsigned dividend $Q$ by an 8-bit divisor $M$:
1. Check divisor $M \ne 0$. If $M == 0$, zero remainder and return quotient 0 safely.
2. Initialize Accumulator $A = 0$.
3. Loop exactly 8 times:
   - Shift left $[A : Q]$ by 1 bit.
   - Subtract Divisor: $A \leftarrow A - M$.
   - Check sign of $A$:
     - If $A \ge 0$: Set $Q_0 = 1$ (Successful division step, keep $A$).
     - If $A < 0$: Set $Q_0 = 0$, and **Restore** $A \leftarrow A + M$.
4. Final Quotient is in $Q$, and Remainder is in $A$.

#### Worked Step-by-Step Example: Dividend $Q = 23$ (`00010111`), Divisor $M = 5$ (`00000101`)
$$\text{Expected: Quotient } Q = 4,\ \text{Remainder } A = 3 \quad (23 = 4 \times 5 + 3)$$

| Cycle | Operation | Accumulator $A$ | Quotient $Q$ | Action / Decision |
| :---: | :--- | :---: | :---: | :--- |
| **0** | Initial State | `00000000` | `00010111` | Initial State |
| **1** | SHL, $A = A - M$ | `00000000` | `00101110` | $A < 0 \implies \text{Restore } A,\ Q_0 = 0$ |
| **2** | SHL, $A = A - M$ | `00000000` | `01011100` | $A < 0 \implies \text{Restore } A,\ Q_0 = 0$ |
| **3** | SHL, $A = A - M$ | `00000000` | `10111000` | $A < 0 \implies \text{Restore } A,\ Q_0 = 0$ |
| **4** | SHL, $A = A - M$ | `00000001` | `01110000` | $A < 0 \implies \text{Restore } A,\ Q_0 = 0$ |
| **5** | SHL, $A = A - M$ | `00000010` | `11100000` | $A < 0 \implies \text{Restore } A,\ Q_0 = 0$ |
| **6** | SHL, $A = A - M$ | `00000000` | `11000001` | $A \ge 0 \implies \text{Keep } A,\ Q_0 = 1$ |
| **7** | SHL, $A = A - M$ | `00000001` | `10000010` | $A < 0 \implies \text{Restore } A,\ Q_0 = 0$ |
| **8** | SHL, $A = A - M$ | `00000011` | `00000100` | $A < 0 \implies \text{Restore } A,\ Q_0 = 0$ |

$$\text{Final Result: Quotient } Q = \texttt{00000100}_2 = 4,\ \text{Remainder } A = \texttt{00000011}_2 = 3 \implies 23 = (4 \times 5) + 3 \implies \mathbf{MATCH\ (PASS)}$$

---

### 5.3 Multi-Precision Addition & Signed Overflow Detection
* **Multi-Precision (`ADC`)**: On a 32-bit architecture, 64-bit numbers are summed across two registers using `ADD` on the lower 32 bits, followed by `ADC` (Add with Carry) on the upper 32 bits:
  $$\begin{aligned}
    \text{Operand 1: } & \texttt{0x00000001\ FFFFFFFF} \\
  + \text{Operand 2: } & \texttt{0x00000002\ 00000001} \\
  \hline
    \text{Sum: } & \texttt{0x00000004\ 00000000} \quad (\text{Carry propagated between words})
  \end{aligned}$$
* **Signed Overflow (`OF`)**: Hardware flag detection verifying:
  $$OF = (A_{31} \land B_{31} \land \neg S_{31}) \lor (\neg A_{31} \land \neg B_{31} \land S_{31})$$
  - Case 1: `0x7FFFFFFF + 0x00000001 = 0x80000000` (Pos + Pos = Neg) $\implies OF = 1$.
  - Case 2: `0x80000000 + 0xFFFFFFFF = 0x7FFFFFFF` (Neg + Neg = Pos) $\implies OF = 1$.

---

## 6. COA-CO4: Control Unit Organization & Instruction Pipelining

### 6.1 Educational Datapath Model vs Actual 80386 Hardware
In academic computer architecture curricula (e.g. Patterson & Hennessy), instruction decoding is taught using an abstract single-cycle or multi-cycle datapath with discrete control signals.

#### Educational Datapath Control Signal Decoding Table
| Instruction Type | RegDst | ALUSrc | MemToReg | RegWrite | MemRead | MemWrite | Branch | ALUOp |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **R-Type (ADD/SUB)** | `1` | `0` | `0` | `1` | `0` | `0` | `0` | `FUNCT` |
| **LW (Load Word)** | `0` | `1` | `1` | `1` | `1` | `0` | `0` | `ADD` |
| **SW (Store Word)** | `X` | `1` | `X` | `0` | `0` | `1` | `0` | `ADD` |
| **BEQ (Branch Eq)** | `X` | `0` | `X` | `0` | `0` | `0` | `1` | `SUB` |
| **JMP (Jump)** | `X` | `X` | `X` | `0` | `0` | `0` | `0` | `NONE` |

*Note: `'X'` represents Don't Care condition in combinational decode logic.*

#### Physical Intel 80386 Control Bus Interface:
The physical 80386 processor does not expose these internal micro-datapath wires on external pins. Instead, the 80386 Bus Interface Unit asserts external cycle definition pins:
* **`M/IO#`**: Distinguishes Memory cycle (`1`) from I/O port cycle (`0`).
* **`D/C#`**: Distinguishes Data operand transfer (`1`) from Control/Instruction fetch (`0`).
* **`W/R#`**: Distinguishes Write cycle (`1`) from Read cycle (`0`).
* **`ADS#`**: Address Status - indicates valid address and bus cycle definition pins.
* **`READY#`**: Input signal acknowledging completion of the active bus cycle.
* **`BE0#`–`BE3#`**: Byte Enable active-low lines selecting active byte lanes on the 32-bit data bus.

#### Actual Intel 80386 Functional Unit Organization:
The Intel 80386 is a CISC processor partitioned into 6 parallel functional units operating concurrently:
1. **Bus Interface Unit (BIU)**: Manages external 32-bit address/data buses, drives bus cycles, and samples `READY#`.
2. **Code Prefetch Unit**: Fetches instruction bytes from memory when the bus is idle into an internal **16-byte prefetch queue**.
3. **Instruction Decode Unit**: Translates variable-length (1 to 15 bytes) CISC instructions into decoded microcode and internal control words.
4. **Execution Unit (EU)**: Contains the 32-bit ALU, 8 GPRs, a 64-bit barrel shifter, and hardware multiply/divide logic.
5. **Segmentation Unit**: Translates 48-bit logical addresses (`Selector:Offset`) into 32-bit linear addresses via descriptor tables.
6. **Paging Unit**: Translates 32-bit linear addresses into physical addresses via two-tier page directories and page tables (when `CR0.PG = 1`).

---

### 6.2 5-Stage Educational Pipeline Simulator
To fulfill the academic CO4 requirement, an educational 5-stage RISC pipeline (IF, ID, EX, MEM, WB) simulation is modeled in assembly:

#### Timeline 1: Ideal Pipeline (No Hazards)
```
Inst \ Cycle | C1  | C2  | C3  | C4  | C5  | C6  | C7  | C8  |
-------------+-----+-----+-----+-----+-----+-----+-----+-----+
I1: ADD      | IF  | ID  | EX  | MEM | WB  |     |     |     |
I2: SUB      |     | IF  | ID  | EX  | MEM | WB  |     |     |
I3: AND      |     |     | IF  | ID  | EX  | MEM | WB  |     |
I4: OR       |     |     |     | IF  | ID  | EX  | MEM | WB  |
-------------+-----+-----+-----+-----+-----+-----+-----+-----+
Latency: 8 Cycles | Sequential: 20 Cycles | Ideal Speedup: 2.50x
```

#### Timeline 2: Data Hazard (RAW - Read After Write) with Stall Bubbles
```
I1: ADD R1, R2, R3   <- Writes R1 during WB stage (Cycle 5)
I2: SUB R4, R1, R5   <- Requires R1 during ID stage (Attempted Cycle 3!)
```
```
Inst \ Cycle | C1  | C2  | C3    | C4    | C5  | C6  | C7  | C8  | C9  | C10 |
-------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+
I1: ADD      | IF  | ID  | EX    | MEM   | WB  |     |     |     |     |     |
I2: SUB      |     | IF  | STALL | STALL | ID  | EX  | MEM | WB  |     |     |
I3: OR       |     |     | STALL | STALL | IF  | ID  | EX  | MEM | WB  |     |
-------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+
```

#### Timeline 3: Control Hazard (Branch Penalty) with Pipeline Flush
```
Inst \ Cycle | C1  | C2  | C3    | C4  | C5  | C6  | C7  | C8  |
-------------+-----+-----+-------+-----+-----+-----+-----+-----+
I1: BEQ      | IF  | ID  | EX    | MEM | WB  |     |     |     |
I2: ADD (old)|     | IF  | FLUSH |  -  |  -  |  -  |  -  |  -  |
TARGET: SUB  |     |     |   -   | IF  | ID  | EX  | MEM | WB  |
-------------+-----+-----+-------+-----+-----+-----+-----+-----+
```

#### Quantitative Benchmark Metrics:
For $N = 100$ instructions across a 5-stage pipeline:
* $T_{\text{seq}} = N \times 5 = 500 \text{ cycles}$.
* $T_{\text{pipe\_ideal}} = 5 + 100 - 1 = 104 \text{ cycles} \implies S_{\text{ideal}} = \mathbf{4.80\times}$.
* With hazards (2 stalls + 1 flush): $T = 107 \text{ cycles} \implies S_{\text{realized}} = \mathbf{4.67\times}$.

---

## 7. Architectural Accuracy and Simulation Limitations

This section documents the explicit technical boundaries and educational simplifications implemented across the COA module:

1. **User Mode (Ring 3) Privilege Boundary**:
   - The assembly code executes as a standard Win32 console process in Ring 3.
   - It reads segment selector registers (`CS`, `DS`, `SS`, `ES`) using unprivileged `mov ax, cs`.
   - It **does not** execute privileged Ring 0 instructions (`LGDT`, `SGDT`, `LLDT`, `LIDT`) or inspect kernel descriptor tables.
   - The 4 GB flat address space is an OS-level configuration; user-mode applications cannot independently verify kernel segment limit bytes or physical RAM capacities.

2. **Educational Datapath vs 80386 Bus Interface**:
   - The control signals table in `control_unit.s` reflects the canonical Patterson-Hennessy academic RISC model required by college COA syllabi.
   - The physical 80386 does not expose `RegDst`, `MemToReg`, etc.; it uses microcode ROM to orchestrate execution internally and communicates externally via physical bus definition pins (`M/IO#`, `D/C#`, `W/R#`, `ADS#`, `READY#`, `BE0#-BE3#`).

3. **5-Stage Pipeline Simulation Model**:
   - The classic 5-stage pipeline (IF, ID, EX, MEM, WB) models scalar RISC instruction flow to teach data/control hazard mechanics and speedup math.
   - The 80386 is a CISC processor with variable-length instructions (1–15 bytes) and concurrent functional stages (Bus Interface, 16-byte Prefetch Queue, Decode, Execution, Segmentation, Paging).

4. **Two's Complement Boundary Asymmetry in Booth's Multiplication**:
   - For an $n$-bit signed two's complement integer, valid values span $[-2^{n-1}, +2^{n-1} - 1]$. For 8 bits, this is $[-128, +127]$.
   - Negating $-128$ yields $+128$, which cannot be represented in 8-bit signed two's complement. Booth's algorithm assumes $M$ can be both added and subtracted within the operand width. Valid 8-bit multiplicand boundaries (e.g. $-64 \times 2 = -128$, $127 \times 1 = 127$) are verified.

5. **Division by Zero Protection**:
   - Both signed division (`asm_div`) and Restoring division (`asm_restoring_divide`) explicitly validate `divisor != 0`.
   - On zero divisor, they return quotient `0` and set remainder `0` safely, avoiding unhandled hardware `#DE` exceptions.

---

## 8. Automated Test Verification Results (41 Assertions)

Executing `bin/coa/coa_runner.exe --test` verifies all 41 unit tests across all 4 COs with a **100% pass rate**:

```
==============================================================================
      AIRSTRIKE 80386 ASSEMBLY (COA) - AUTOMATED VERIFICATION TEST SUITE       
      Course Outcomes: CO1, CO2, CO3, CO4 (Pure 32-bit 80386 Assembly)         
==============================================================================

[SUITE 1/4] CO1: 80386 Architecture & Registers Verification...
  [PASS] CO1: GPR Sub-register Partitioning (EAX -> AX -> AH/AL)
  [PASS] CO1: EFLAGS Carry Flag (CF) on Unsigned Overflow (0xFFFFFFFF + 1)
  [PASS] CO1: EFLAGS Zero Flag (ZF) on Zero Result (100 - 100)
  [PASS] CO1: EFLAGS Sign Flag (SF) on Negative Result (20 - 50)
  [PASS] CO1: EFLAGS Overflow Flag (OF) on Signed Overflow (0x7FFFFFFF + 1)
  [PASS] CO1: 80386 SIB Scaled-Index-Base Addressing Mode [EBX + ESI*4 + 8]

[SUITE 2/4] CO2: 80386 Assembly Language Programs Verification...
  [PASS] CO2: Addition (45 + 55 = 100)
  [PASS] CO2: Subtraction (100 - 35 = 65)
  [PASS] CO2: Signed Multiplication (-12 * 8 = -96)
  [PASS] CO2: Signed Division with Remainder (47 / 5 = 9 rem 2)
  [PASS] CO2: Signed Division by Zero Protection (50 / 0 returns 0, Rem = 0)
  [PASS] CO2: Array Minimum & Maximum Extraction (Min: -34, Max: 102)
  [PASS] CO2: Array Summation Accumulation
  [PASS] CO2: Factorial Base Case (0! = 1)
  [PASS] CO2: Recursive Factorial (5! = 120)
  [PASS] CO2: Fibonacci Series Generation (F(0..6): 0, 1, 1, 2, 3, 5, 8)
  [PASS] CO2: Linear Search Target Present (Found at Index)
  [PASS] CO2: Linear Search Target Absent (Returns -1)
  [PASS] CO2: In-place Array Reversal Two-Pointer Swap
  [PASS] CO2: Array Positive, Negative, and Zero Counting
  [PASS] CO2: Bitwise Logic AND, OR, XOR, NOT on Telemetry Masks

[SUITE 3/4] CO3: Computer Arithmetic (Booth & Restoring Div) Verification...
  [PASS] CO3: Booth's Algorithm (+ * +): 7 * 5 = 35
  [PASS] CO3: Booth's Algorithm (+ * -): 9 * -4 = -36
  [PASS] CO3: Booth's Algorithm (- * +): -8 * 6 = -48
  [PASS] CO3: Booth's Algorithm (- * -): -11 * -3 = 33
  [PASS] CO3: Booth's Algorithm Zero Multiplicand (0 * 45 = 0)
  [PASS] CO3: Booth's Algorithm Zero Multiplier (-25 * 0 = 0)
  [PASS] CO3: Booth's Algorithm Boundary (-64 * 2 = -128)
  [PASS] CO3: Restoring Division Exact (24 / 6 = 4, Rem = 0)
  [PASS] CO3: Restoring Division Non-Exact (29 / 5 = 5, Rem = 4)
  [PASS] CO3: Restoring Division Non-Exact (100 / 7 = 14, Rem = 2)
  [PASS] CO3: Restoring Division Dividend < Divisor (3 / 10 = 0, Rem = 3)
  [PASS] CO3: Restoring Division Zero Dividend (0 / 15 = 0, Rem = 0)
  [PASS] CO3: Restoring Division by Zero Protection (45 / 0 returns 0, Rem = 0)
  [PASS] CO3: Restoring Division 8-bit Maximum Boundary (255 / 1 = 255, Rem = 0)
  [PASS] CO3: Restoring Division Maximum Divisor Boundary (255 / 255 = 1, Rem = 0)
  [PASS] CO3: Multi-precision 64-bit Addition with Carry (ADC)
  [PASS] CO3: Signed Overflow Detection (Pos + Pos = Neg, OF=1)
  [PASS] CO3: Signed Overflow Detection (Neg + Neg = Pos, OF=1)

[SUITE 4/4] CO4: Control Unit & 5-Stage Pipeline Verification...
  [PASS] CO4: Control Unit Opcode Decode & Control Signal Matrix Generation
  [PASS] CO4: 5-Stage Pipeline Simulation (RAW Stalls & Branch Flush Verified)

==============================================================================
                         FINAL COA TEST RESULTS SUMMARY                       
==============================================================================
  Total Assertions Run : 41
  Passed Assertions    : 41 [OK]
  Failed Assertions    : 0
  Target Architecture  : Intel 80386 Protected Mode (32-bit x86)
==============================================================================
[SUCCESS] ALL 80386 ASSEMBLY VERIFICATION TESTS PASSED WITH ZERO ERRORS!
```

---

## 9. Academic Viva Voce Preparation (Q&A)

### Q1: What is the difference between Real Mode and 32-bit Protected Mode on the 80386?
**Answer**: Real Mode (default boot state for 8086 backward compatibility) operates with 16-bit registers and addresses up to 1 MB of memory using $\text{Segment} \times 16 + \text{Offset}$ without memory protection. Protected Mode enables 32-bit registers, 4 GB flat physical/virtual address spaces, hardware privilege rings (Ring 0 Kernel to Ring 3 User), virtual memory paging, and segment descriptor protection.

### Q2: How does the Flat Memory Model interact with the 80386 segmentation hardware?
**Answer**: On the 80386, all memory references pass through segmentation. The operating system creates a "Flat Memory Model" by configuring segment descriptors in the Global Descriptor Table (GDT) such that the segment Base is `0` and the Limit is `4 GB` for code and data. As a result, the logical offset equals the linear address. User-mode code reads the 16-bit segment selectors (`CS`, `DS`, `SS`, `ES`), but cannot directly inspect or modify the GDT descriptors.

### Q3: How does the standard C calling convention (`cdecl`) function on the 80386?
**Answer**: In `cdecl`:
1. Parameters are pushed onto the stack from **right to left**.
2. Caller is responsible for cleaning the stack (`add esp, N` after `call`).
3. Return values are passed in `EAX`.
4. Registers `EAX`, `ECX`, and `EDX` are **caller-saved** (scratch registers).
5. Registers `EBX`, `ESI`, `EDI`, `EBP`, and `ESP` are **callee-saved** and must be preserved across procedure calls.

### Q4: Explain how SIB (Scaled-Index-Base) addressing calculates the effective memory address.
**Answer**: The SIB byte follows the ModR/M byte in 80386 machine code. The CPU hardware calculates:
$$\text{Effective Address} = \text{Base Register} + (\text{Index Register} \times \text{Scale}) + \text{Displacement}$$
Where Scale can be $1, 2, 4,$ or $8$. This enables access to array elements like `array[i]` in a single instruction (`[EBX + ESI*4 + displacement]`).

### Q5: In Booth's algorithm, what do the $(Q_0, Q_{-1})$ bit pairs signify?
**Answer**:
* `01`: Transition from 0 to 1 $\implies$ Beginning of a run of 1s $\implies$ Add Multiplicand ($A \leftarrow A + M$).
* `10`: Transition from 1 to 0 $\implies$ End of a run of 1s $\implies$ Subtract Multiplicand ($A \leftarrow A - M$).
* `00` or `11`: Middle of a run of 0s or 1s $\implies$ No addition or subtraction, perform Arithmetic Shift Right only.

### Q6: Why can $-128$ not be negated in 8-bit two's complement arithmetic?
**Answer**: In 8-bit two's complement, values range from $-128$ (`0x80`) to $+127$ (`0x7F`). The value $-128$ has no positive counterpart because $+128$ requires 9 bits (`0 1000 0000`). Attempting to negate `0x80` by inverting bits (`0x7F`) and adding 1 produces `0x80` ($-128$) again, causing an arithmetic overflow.

### Q7: What is the key difference between Restoring and Non-Restoring division?
**Answer**: In Restoring division, if $A - M$ yields a negative result, the subtraction is undone by adding $M$ back to $A$ (restoring $A$) and setting $Q_0 = 0$. Non-Restoring division skips the restore addition cycle and instead adds $M$ in the next cycle, saving cycles at the cost of slightly more complex control logic.

### Q8: Differentiate between the educational 5-stage pipeline and actual 80386 organization.
**Answer**:
* **Educational 5-Stage Pipeline**: A conceptual RISC model (IF, ID, EX, MEM, WB) used in university courses to explain hazards (RAW stalls, branch flushes) and speedup formulas.
* **Intel 80386 Organization**: A CISC processor with variable-length instructions (1–15 bytes) divided into 6 concurrent functional units: Bus Interface Unit, 16-byte Code Prefetch Queue, Instruction Decode Unit, Execution Unit (ALU + barrel shifter), Segmentation Unit, and Paging Unit.

### Q9: Name the 3 classic types of pipeline hazards and how they are resolved.
**Answer**:
1. **Structural Hazard**: Hardware resource conflict (resolved by duplicating hardware, e.g., separate I-Cache and D-Cache).
2. **Data Hazard (RAW, WAR, WAW)**: Instruction depends on a result not yet written (resolved by internal Operand Forwarding or inserting Stall bubbles).
3. **Control Hazard**: Branch instruction alters PC after speculatively fetching subsequent instructions (resolved by Branch Prediction, Branch Delay Slots, or Pipeline Flushes).

### Q10: What are the physical bus control signals of the Intel 80386?
**Answer**: Unlike abstract datapath signals (`RegDst`, `MemToReg`), the physical 80386 asserts bus cycle definition pins: `M/IO#` (Memory vs I/O), `D/C#` (Data vs Control), `W/R#` (Write vs Read), `ADS#` (Address Status valid), `READY#` (transfer acknowledge input), and `BE0#`–`BE3#` (byte lane enables).
