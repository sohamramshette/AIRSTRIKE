# AIRSTRIKE Phase 4: Computer Organization & Architecture (COA)
## Genuine 32-bit Intel 80386 Protected-Mode Assembly Implementation

---

## 1. Executive Summary & Strict Technical Adherence

Phase 4 implements the entire **Computer Organization and Architecture (COA)** curriculum from first principles using pure **32-bit Intel 80386 Assembly Language**. 

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
| **COA-CO1** | 80386 Architecture & Instruction Set | `COA/src/co1_architecture/arch_demo.s` | 6 Assertions (`test_coa.s`) | **Completed (Phase 4)** |
| **COA-CO2** | 10 Assembly Language Programs | `COA/src/co2_assembly_programs/*.s` | 14 Assertions (`test_coa.s`) | **Completed (Phase 4)** |
| **COA-CO3** | Computer Arithmetic (Booth & Restoring) | `COA/src/co3_arithmetic/*.s` | 12 Assertions (`test_coa.s`) | **Completed (Phase 4)** |
| **COA-CO4** | Control Unit & 5-Stage Pipelining | `COA/src/co4_control_unit/*.s` | 2 Assertions (`test_coa.s`) | **Completed (Phase 4)** |

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

### 3.4 32-bit Protected Mode Memory Segmentation
In 32-bit Protected Mode under modern OS operating systems (Windows/Linux), segment registers (`CS`, `DS`, `SS`, `ES`) store 16-bit **Segment Selectors** pointing to entries in the Global Descriptor Table (GDT). Under the Flat Memory Model:
* `CS` (Code Segment) = `0x0023`
* `DS` (Data Segment), `SS` (Stack Segment), `ES` (Extra Segment) = `0x002B`
* All segments have base address `0x00000000` and limit `0xFFFFFFFF`, covering the entire contiguous 4 GB virtual address space.

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
├── arithmetic_ops.s   # Prog 1 (Add/Sub) & Prog 2 (Mul/Div)
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
  - Uses `cdq` (Convert Doubleword to Quadword) to sign-extend `EAX` into the 64-bit pair `EDX:EAX`.
  - Executes `idiv ebx` which divides `EDX:EAX` by `ebx`.
  - Quotient is left in `EAX`, and remainder is left in `EDX`.
  - Verifies division-by-zero protection.

### 4.3 Program 3: Largest and Smallest in an Array (`array_ops.s`)
* **`asm_find_min_max(arr, size, *out_min, *out_max)`**:
  - Initializes `EAX` (min) and `EDX` (max) with the first array element `[esi]`.
  - Iterates through the array using `EBX = [esi]`.
  - Executes signed comparison `cmp ebx, eax; jl .L_update_min`.
  - Executes signed comparison `cmp ebx, edx; jg .L_update_max`.

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
4. Loop 8 times (for 8-bit operands):
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

---

### 5.2 Restoring Binary Division Algorithm
Restoring division divides an 8-bit unsigned dividend $Q$ by an 8-bit divisor $M$:
1. Initialize Accumulator $A = 0$.
2. Loop 8 times:
   - Shift left $[A : Q]$ by 1 bit.
   - Subtract Divisor: $A \leftarrow A - M$.
   - Check sign of $A$:
     - If $A \ge 0$: Set $Q_0 = 1$ (Successful division step, keep $A$).
     - If $A < 0$: Set $Q_0 = 0$, and **Restore** $A \leftarrow A + M$.
3. Final Quotient is in $Q$, and Remainder is in $A$.

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

## 6. COA-CO4: Control Unit Organization and 5-Stage Pipelining

### 6.1 Control Unit Architecture
The Control Unit is the brain of the CPU responsible for instruction interpretation and control signal routing:
* **Hardwired Control Unit**: Built using combinational logic decoders and finite state machines. Offers high operating frequencies; common in RISC and critical execution units of CISC processors.
* **Microprogrammed Control Unit**: Employs a Control Store ROM containing micro-routines. Highly flexible and easier to update for complex CISC instruction sets (such as the x86 family).

#### Control Signal Truth Matrix (Implemented in `control_unit.s`)
| Instruction Type | RegDst | ALUSrc | MemToReg | RegWrite | MemRead | MemWrite | Branch | ALUOp |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **R-Type (ADD/SUB)** | `1` | `0` | `0` | `1` | `0` | `0` | `0` | `FUNCT` |
| **LW (Load Word)** | `0` | `1` | `1` | `1` | `1` | `0` | `0` | `ADD` |
| **SW (Store Word)** | `X` | `1` | `X` | `0` | `0` | `1` | `0` | `ADD` |
| **BEQ (Branch Eq)** | `X` | `0` | `X` | `0` | `0` | `0` | `1` | `SUB` |
| **JMP (Jump)** | `X` | `X` | `X` | `0` | `0` | `0` | `0` | `NONE` |

*Note: `'X'` represents Don't Care condition in control bus decoding.*

---

### 6.2 5-Stage Instruction Pipelining Simulation
The 5 standard RISC execution stages are:
1. **IF**: Instruction Fetch from Memory/Cache.
2. **ID**: Instruction Decode & Register File Read.
3. **EX**: Execution & ALU Operation.
4. **MEM**: Memory Data Read/Write.
5. **WB**: Register File Write Back.

#### Timeline 1: Ideal Pipeline (No Hazards)
For 4 instructions without dependencies:
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
Without data forwarding, the pipeline must insert **2 stall bubbles**:
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
I1: BEQ R1, R2, TARGET  <- Evaluates branch condition in EX stage (Cycle 3)
I2: ADD R4, R5, R6      <- Speculatively fetched in IF (Cycle 2)
Branch TAKEN            -> I2 is FLUSHED (converted to NOP / discarded)
```
```
Inst \ Cycle | C1  | C2  | C3    | C4  | C5  | C6  | C7  | C8  |
-------------+-----+-----+-------+-----+-----+-----+-----+-----+
I1: BEQ      | IF  | ID  | EX    | MEM | WB  |     |     |     |
I2: ADD (old)|     | IF  | FLUSH |  -  |  -  |  -  |  -  |  -  |
TARGET: SUB  |     |     |   -   | IF  | ID  | EX  | MEM | WB  |
-------------+-----+-----+-------+-----+-----+-----+-----+-----+
```

#### Performance Metrics & Quantitative Speedup Benchmark
For $N = 100$ instructions across a 5-stage pipeline:
* **Sequential Execution**: $T_{\text{seq}} = N \times k = 100 \times 5 = 500 \text{ cycles}$.
* **Ideal Pipelined Execution**: $T_{\text{pipe}} = k + N - 1 = 5 + 100 - 1 = 104 \text{ cycles}$.
* **Ideal Speedup**: $S_{\text{ideal}} = \frac{500}{104} = \mathbf{4.80\times}$.
* **With Real Hazards (2 Stalls + 1 Branch Flush)**: $T = 104 + 3 = 107 \text{ cycles} \implies S_{\text{realized}} = \frac{500}{107} = \mathbf{4.67\times}$.

---

## 7. Automated Test Verification Results

Executing `bin/coa/coa_runner.exe --test` runs all 34 assertions covering all 4 COs:

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
  [PASS] CO3: Booth's Algorithm Boundary (-64 * 2 = -128)
  [PASS] CO3: Restoring Division Exact (24 / 6 = 4, Rem = 0)
  [PASS] CO3: Restoring Division Non-Exact (29 / 5 = 5, Rem = 4)
  [PASS] CO3: Restoring Division Non-Exact (100 / 7 = 14, Rem = 2)
  [PASS] CO3: Restoring Division Dividend < Divisor (3 / 10 = 0, Rem = 3)
  [PASS] CO3: Multi-precision 64-bit Addition with Carry (ADC)
  [PASS] CO3: Signed Overflow Detection (Pos + Pos = Neg, OF=1)
  [PASS] CO3: Signed Overflow Detection (Neg + Neg = Pos, OF=1)

[SUITE 4/4] CO4: Control Unit & 5-Stage Pipeline Verification...
  [PASS] CO4: Control Unit Opcode Decode & Control Signal Matrix Generation
  [PASS] CO4: 5-Stage Pipeline Simulation (RAW Stalls & Branch Flush Verified)

==============================================================================
                         FINAL COA TEST RESULTS SUMMARY                       
==============================================================================
  Total Assertions Run : 34
  Passed Assertions    : 34 [OK]
  Failed Assertions    : 0
  Target Architecture  : Intel 80386 Protected Mode (32-bit x86)
==============================================================================
[SUCCESS] ALL 80386 ASSEMBLY VERIFICATION TESTS PASSED WITH ZERO ERRORS!
```

---

## 8. Academic Viva Voce Preparation (Q&A)

### Q1: What is the difference between Real Mode and 32-bit Protected Mode on the 80386?
**Answer**: Real Mode (default boot state for 8086 backward compatibility) operates with 16-bit registers and addresses up to 1 MB of memory using $\text{Segment} \times 16 + \text{Offset}$ without memory protection. Protected Mode enables 32-bit registers, 4 GB flat physical/virtual address spaces, hardware privilege rings (Ring 0 Kernel to Ring 3 User), virtual memory paging, and segment descriptor protection.

### Q2: How does the standard C calling convention (`cdecl`) function on the 80386?
**Answer**: In `cdecl`:
1. Parameters are pushed onto the stack from **right to left**.
2. Caller is responsible for cleaning the stack (`add esp, N` after `call`).
3. Return values are passed in `EAX`.
4. Registers `EAX`, `ECX`, and `EDX` are **caller-saved** (scratch registers).
5. Registers `EBX`, `ESI`, `EDI`, `EBP`, and `ESP` are **callee-saved** and must be preserved across procedure calls.

### Q3: Explain how SIB (Scaled-Index-Base) addressing calculates the effective memory address.
**Answer**: The SIB byte follows the ModR/M byte in 80386 machine code. The CPU hardware calculates:
$$\text{Effective Address} = \text{Base Register} + (\text{Index Register} \times \text{Scale}) + \text{Displacement}$$
Where Scale can be $1, 2, 4,$ or $8$. This enables access to array elements like `array[i]` in a single instruction (`[EBX + ESI*4 + displacement]`).

### Q4: Why does standard unsigned multiplication fail for signed two's complement numbers?
**Answer**: In two's complement, the MSB has a negative weight ($-2^{n-1}$). Unsigned multiplication treats the MSB with positive weight ($+2^{n-1}$), producing a vastly incorrect positive product. Booth's algorithm recodes the multiplier using pairwise subtraction to account for the negative weight of leading 1s.

### Q5: In Booth's algorithm, what do the $(Q_0, Q_{-1})$ bit pairs signify?
**Answer**:
* `01`: Transition from 0 to 1 $\implies$ Beginning of a run of 1s $\implies$ Add Multiplicand ($A \leftarrow A + M$).
* `10`: Transition from 1 to 0 $\implies$ End of a run of 1s $\implies$ Subtract Multiplicand ($A \leftarrow A - M$).
* `00` or `11`: Middle of a run of 0s or 1s $\implies$ No addition or subtraction, perform Arithmetic Shift Right only.

### Q6: What is the key difference between Restoring and Non-Restoring division?
**Answer**: In Restoring division, if $A - M$ yields a negative result, the subtraction is undone by adding $M$ back to $A$ (restoring $A$) and setting $Q_0 = 0$. Non-Restoring division skips the restore addition cycle and instead adds $M$ in the next cycle, saving cycles at the cost of slightly more complex control logic.

### Q7: How does the 80386 detect signed arithmetic overflow ($OF = 1$)?
**Answer**: The Overflow Flag is set if the carry into the sign bit ($C_{\text{in}}$) does not equal the carry out of the sign bit ($C_{\text{out}}$):
$$OF = C_{\text{in}} \oplus C_{\text{out}}$$
Equivalently, it triggers when two operands of identical sign produce a result with the opposite sign.

### Q8: Differentiate between a Hardwired Control Unit and a Microprogrammed Control Unit.
**Answer**:
* **Hardwired CU**: Constructed using fixed combinational logic gates, decoders, and counters. Fast execution speed ($O(1)$ gate delays), but rigid and difficult to modify or debug.
* **Microprogrammed CU**: Stores micro-instructions in a Control Store ROM. Slower due to ROM read latencies, but flexible and cost-effective for large CISC instruction sets.

### Q9: Name the 3 classic types of pipeline hazards and how they are resolved.
**Answer**:
1. **Structural Hazard**: Hardware resource conflict (resolved by duplicating hardware, e.g., separate I-Cache and D-Cache).
2. **Data Hazard (RAW, WAR, WAW)**: Instruction depends on a result not yet written (resolved by internal Operand Forwarding or inserting Stall bubbles).
3. **Control Hazard**: Branch instruction alters PC after speculatively fetching subsequent instructions (resolved by Branch Prediction, Branch Delay Slots, or Pipeline Flushes).

### Q10: What is the theoretical maximum speedup of a $k$-stage pipeline, and why is it never achieved in practice?
**Answer**: The theoretical maximum speedup is $k$ (the number of stages). In practice, speedup is reduced by:
1. Pipeline hazard stalls (RAW stalls and branch flushes).
2. Unbalanced stage delays (clock cycle is bounded by the slowest stage).
3. Pipeline register latch overhead between stages.
