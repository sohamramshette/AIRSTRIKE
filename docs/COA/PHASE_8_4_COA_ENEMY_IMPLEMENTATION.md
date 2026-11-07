# AIRSTRIKE — Phase 8.4 Implementation Report: COA Enemy Implementation

**Module**: Computer Organization & Architecture (COA)  
**Target Architecture**: Intel 80386 Protected Mode (32-bit x86, GNU Assembler - Intel Syntax)  
**Verification Target**: Course Outcomes COA-CO1 through COA-CO4  
**Test Suite Status**: **57 / 57 Assertions Passed (100% PASS)**  

---

## 1. Executive Summary

Phase 8.4 implements the four planned **Computer Organization & Architecture (COA)** enemy types directly in genuine 32-bit Intel 80386 GNU Assembly within the `COA/src/` architecture hierarchy. Every enemy model executes authentic low-level hardware routines adhering to the syllabus specifications:
1. **Sub-Register Aegis Sentry (`register_shield.s`)** — Demonstrates **COA-CO1** via GPR sub-register partitioning (`EAX` $\rightarrow$ `AX` $\rightarrow$ `AH`/`AL`), SIB addressing $[Base + Index \times 4 + Disp]$, and condition flags ($ZF, SF, CF, OF$).
2. **Fibonacci Skimmer (`fibonacci_skimmer.s`)** — Demonstrates **COA-CO2** by executing the genuine assembly `_asm_fibonacci` routine to compute sinusoidal-like weaving flight paths and dynamic salvo timing.
3. **Booth Multiplier Gunship (`booth_combat_eval.s`)** — Demonstrates **COA-CO3** by calling the authentic 8-cycle signed Booth's multiplication algorithm (`_asm_booth_multiply`) to calculate kinetic weapon yield, phase inversion (EMP drain), and saturation limits.
4. **Pipelined Railgun Dreadnought (`pipelined_railgun.s`)** — Demonstrates **COA-CO4** through an educational 5-stage RISC instruction pipeline simulation (IF, ID, EX, MEM, WB), Read-After-Write (RAW) data hazard stall bubble insertion, and operand forwarding bypass.

---

## 2. Files Created and Modified

### Newly Created Files
| File Path | Subject / CO | Component Description |
| :--- | :--- | :--- |
| `COA/src/co1_architecture/register_shield.s` | COA-CO1 | Pure 32-bit 80386 assembly implementation of Sub-Register Aegis Sentry: sub-register partitioning (`EAX`, `AX`, `AH`, `AL`), arithmetic flag evaluation (`ZF`, `SF`, `CF`, `OF`), and SIB layer access. |
| `COA/src/co2_assembly_programs/fibonacci_skimmer.s` | COA-CO2 | Pure 32-bit 80386 assembly implementation of Fibonacci Skimmer: trajectory weave coordinate generation and dynamic weapon salvo intervals based on assembly Fibonacci terms. |
| `COA/src/co3_arithmetic/booth_combat_eval.s` | COA-CO3 | Pure 32-bit 80386 assembly implementation of Booth Multiplier Gunship: kinetic impact damage calculation, phase inversion detection, and boundary saturation scaling via Booth multiplication. |
| `COA/src/co4_control_unit/pipelined_railgun.s` | COA-CO4 | Pure 32-bit 80386 assembly implementation of Pipelined Railgun Dreadnought: educational 5-stage pipeline simulation, RAW data hazard bubble insertion (2 stalls), and forwarding bypass. |
| `docs/COA/PHASE_8_4_COA_ENEMY_IMPLEMENTATION.md` | Doc | Complete academic mapping, register state traces, pipeline diagrams, viva Q&As, and verification report. |

### Modified Files
| File Path | Modification Summary |
| :--- | :--- |
| `COA/src/main.s` | Integrated `_coa_demo_aegis_sentry`, `_coa_demo_fibonacci_skimmer`, `_coa_demo_booth_gunship`, and `_coa_demo_pipelined_railgun` into the full demonstration runner. |
| `COA/tests/test_coa.s` | Added `[SUITE 5/5]` with 16 automated assertions verifying all 4 COA enemies across normal, boundary, flag, register, and pipeline conditions. Total COA assertions updated from 41 to 57. |

---

## 3. Academic CO Mapping & Low-Level Hardware Architecture

```
+----------------------------------------------------------------------------------------------------+
|                                 AIRSTRIKE COA ENEMY SYSTEM ARCHITECTURE                            |
+----------------------------------------------------------------------------------------------------+
|  [COA-CO1: Sub-Register Aegis Sentry]   --> EAX (32-bit) = [ Upper 16 | AH (8-bit) | AL (8-bit) ]  |
|                                             SIB Addressing: mov eax, [ebx + esi*4 + 8]             |
|  [COA-CO2: Fibonacci Skimmer]           --> _asm_fibonacci: F(i) = F(i-1) + F(i-2)                 |
|                                             Trajectory: X[i] = baseX +/- (F(i) % maxWeave)         |
|  [COA-CO3: Booth Multiplier Gunship]    --> _asm_booth_multiply: 8-cycle pairwise recoding (Q0,Q-1)|
|                                             Kinetic Yield: Product = M * Q (Signed 16-bit)         |
|  [COA-CO4: Pipelined Railgun]           --> 5-Stage Pipeline (IF, ID, EX, MEM, WB)                |
|                                             RAW Hazard: I1 writes WB (C5), I2 needs ID (C3) -> 2 Stalls |
+----------------------------------------------------------------------------------------------------+
```

### Detailed Mapping Table

| Enemy Name | Exact CO | Syllabus Concept | Assembly Routines Used | Core 80386 Instructions |
| :--- | :--- | :--- | :--- | :--- |
| **Sub-Register Aegis Sentry** | **COA-CO1** | 80386 Architecture, GPR Partitioning & Flags | `_coa_init_aegis_shield`<br>`_coa_damage_al_layer`<br>`_coa_recharge_ah_layer`<br>`_coa_evaluate_shield_flags`<br>`_coa_sib_shield_layer_access` | `mov`, `sub al, dl`, `add ah, dl`, `setc`, `setz`, `sets`, `seto`, `mov eax, [ebx + esi*4 + 8]` |
| **Fibonacci Skimmer** | **COA-CO2** | 80386 Assembly Loops & Series Programs | `_coa_skimmer_compute_trajectory`<br>`_coa_skimmer_fire_interval`<br>`_asm_fibonacci` | `mov`, `add`, `sub`, `imul`, `div`, `test esi, 1`, `jnz`, `dec`, `cmp` |
| **Booth Multiplier Gunship** | **COA-CO3** | Computer Arithmetic & Signed Multiplication | `_coa_booth_calculate_kinetic_damage`<br>`_asm_booth_multiply` | `sar`, `shl`, `shr`, `and`, `test eax, eax`, `js`, `neg`, `cmp` |
| **Pipelined Railgun Dreadnought** | **COA-CO4** | Control Unit & 5-Stage Instruction Pipeline | `_coa_railgun_pipeline_eval`<br>`_co4_demo_pipeline` | `mov`, `add`, `cmp`, `jne`, `lea`, `imul`, `div` |

---

## 4. Register-Level and Pipeline-Level Traces

### 1. Sub-Register Aegis Sentry (`register_shield.s` — COA-CO1)

#### Register Partitioning Model:
$$\text{EAX (32 bits)} = [\text{Bits 31..16: 0xA5B6}] \mid [\text{AX (Bits 15..0): 0xC7D8}]$$
$$\text{AX (16 bits)} = [\text{AH (Bits 15..8): 0xC7}] \mid [\text{AL (Bits 7..0): 0xD8}]$$

#### Concrete Combat Execution Trace:
1. **Initial Full Shield State**:
   - `EAX = 0xA5B6C7D8`
   - `AX  = 0xC7D8`
   - `AH  = 0xC7` (High-Byte Kinetic Damper)
   - `AL  = 0xD8` (Low-Byte Reactive Polarizer)
2. **Impact 1 — Laser strikes Low-Byte Polarizer (`sub al, 0x10`)**:
   - Executed instruction: `sub al, 0x10`
   - Before: `EAX = 0xA5B6C7D8`, `AL = 0xD8`
   - After:  `EAX = 0xA5B6C7C8`, `AL = 0xC8`
   - **Architectural Verification**: `AH` remains exactly `0xC7`; high word remains `0xA5B6`. Only the lowest byte is altered.
3. **Impact 2 — Recharge boost applied to High-Byte Damper (`add ah, 0x10`)**:
   - Executed instruction: `add ah, 0x10`
   - Before: `EAX = 0xA5B6C7C8`, `AH = 0xC7`
   - After:  `EAX = 0xA5B6D7C8`, `AH = 0xD7`
   - **Architectural Verification**: `AL` remains exactly `0xC8`. Modifying `AH` modifies `AX` (`0xD7C8`) and `EAX` without affecting `AL`.
4. **Condition Code Flags**:
   - Total Depletion (`sub al, al` on $50 - 50 = 0$):
     - Result is $0 \implies ZF = 1, SF = 0, CF = 0, OF = 0$.
   - Hull Over-penetration (`sub al, 50` on $20 - 50 = -30$):
     - Unsigned borrow occurs $\implies CF = 1$.
     - Sign bit is set $\implies SF = 1$.
5. **Scaled-Index-Base (SIB) Mode**:
   - Formula: $\text{Effective Address} = \text{Base} + (\text{Index} \times 4) + 8$
   - Instruction: `mov eax, [ebx + esi*4 + 8]`
   - When `EBX = &test_sib_layers` and `ESI = 2`:
     - Reads Layer 2 value `0xCCCC0003` in a single clock cycle.

---

### 2. Fibonacci Skimmer (`fibonacci_skimmer.s` — COA-CO2)

#### Flight Weave Calculation:
The skimmer invokes `_asm_fibonacci(buf, 6)` to generate $F(0)=0, F(1)=1, F(2)=1, F(3)=2, F(4)=3, F(5)=5$.
Coordinates oscillate around arena centerline $X_{\text{base}} = 400$:
- **Step 0** (even): $X_0 = 400 + (F(0) \pmod{100}) = 400 + 0 = 400\text{ px}$
- **Step 1** (odd):  $X_1 = 400 - (F(1) \pmod{100}) = 400 - 1 = 399\text{ px}$
- **Step 2** (even): $X_2 = 400 + (F(2) \pmod{100}) = 400 + 1 = 401\text{ px}$
- **Step 3** (odd):  $X_3 = 400 - (F(3) \pmod{100}) = 400 - 2 = 398\text{ px}$
- **Step 4** (even): $X_4 = 400 + (F(4) \pmod{100}) = 400 + 3 = 403\text{ px}$
- **Step 5** (odd):  $X_5 = 400 - (F(5) \pmod{100}) = 400 - 5 = 395\text{ px}$

#### Weapon Timing Scaling:
- Interval for step $n$ is calculated as $T_{\text{cooldown}} = 100\text{ ms} \times F(n)$.
- For step $5$: $F(5) = 5 \implies 5 \times 100\text{ ms} = 500\text{ ms}$.

---

### 3. Booth Multiplier Gunship (`booth_combat_eval.s` — COA-CO3)

#### Booth Recoding Dynamics:
Standard shift-and-add fails on two's complement negative multiplicands. Booth's algorithm inspects bit pairs $(Q_0, Q_{-1})$ across 8 cycles:
- $01 \implies A = A + M$, then $\text{SAR}(A:Q, 1)$
- $10 \implies A = A - M$, then $\text{SAR}(A:Q, 1)$
- $00 \text{ or } 11 \implies \text{SAR}(A:Q, 1)$ only

#### Combat Firepower Test Results:
| Shot # | Multiplicand $M$ (Base Charge) | Multiplier $Q$ (Velocity Modifier) | 16-bit Product | Flag Interpretation |
| :---: | :---: | :---: | :---: | :--- |
| **1** | $+7$ | $+5$ | $+35$ | Normal kinetic strike ($> 0$) |
| **2** | $+9$ | $-4$ | $-36$ | Phase-inverted EMP drain ($< 0$, Flag Bit 0 set) |
| **3** | $-8$ | $+6$ | $-48$ | Negative velocity vector strike ($< 0$) |
| **4** | $-11$ | $-3$ | $+33$ | Two negative vectors produce positive kinetic yield |
| **5** | $0$ | $+45$ | $0$ | Discharged capacitor dud (Flag Bit 1 set) |
| **6** | $-25$ | $0$ | $0$ | Zero velocity impact (Flag Bit 1 set) |
| **7** | $-64$ | $+2$ | $-128$ | 8-bit signed lower boundary saturated (Flag Bit 2 set) |

---

### 4. Pipelined Railgun Dreadnought (`pipelined_railgun.s` — COA-CO4)

> [!NOTE]
> **Educational Pipeline Model Clarification**:  
> The 5-stage RISC instruction pipelining model (Instruction Fetch, Instruction Decode, Execute, Memory Access, Write Back) implemented here is an academic teaching vehicle demonstrating data hazard stalls, branch penalties, and throughput speedup. It does not represent the internal prefetch and bus execution unit organization of the physical Intel 80386 processor.

#### 5-Stage Volley Simulation ($N=4$ instructions):
$$\text{Sequence}: I_1\text{ (CHARGE EAX)}, I_2\text{ (AIM EBX, EAX)}, I_3\text{ (IONIZE ECX)}, I_4\text{ (DISCHARGE)}$$

#### Case 1: Ideal Pipeline (No Hazards) — Latency = 8 Cycles
```
Inst \ Cycle | C1  | C2  | C3  | C4  | C5  | C6  | C7  | C8  |
-------------+-----+-----+-----+-----+-----+-----+-----+-----+
I1: CHARGE   | IF  | ID  | EX  | MEM | WB  |     |     |     |
I2: AIM      |     | IF  | ID  | EX  | MEM | WB  |     |     |
I3: IONIZE   |     |     | IF  | ID  | EX  | MEM | WB  |     |
I4: DISCHARGE|     |     |     | IF  | ID  | EX  | MEM | WB  |
-------------+-----+-----+-----+-----+-----+-----+-----+-----+
T_ideal = 5 + N - 1 = 5 + 4 - 1 = 8 Cycles | Sequential T_seq = 20 Cycles | Speedup = 2.50x
```

#### Case 2: Read-After-Write (RAW) Hazard Without Forwarding — Latency = 10 Cycles
$I_2$ requires `EAX` during its ID stage (Cycle 3), but $I_1$ does not write `EAX` to the register file until its WB stage (Cycle 5).
The hardware must insert **2 stall bubbles** in Cycles 3 and 4:
```
Inst \ Cycle | C1  | C2  | C3    | C4    | C5  | C6  | C7  | C8  | C9  | C10 |
-------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+
I1: CHARGE   | IF  | ID  | EX    | MEM   | WB  |     |     |     |     |     |
I2: AIM      |     | IF  | STALL | STALL | ID  | EX  | MEM | WB  |     |     |
I3: IONIZE   |     |     | STALL | STALL | IF  | ID  | EX  | MEM | WB  |     |
I4: DISCHARGE|     |     |   -   |   -   |     | IF  | ID  | EX  | MEM | WB  |
-------------+-----+-----+-------+-------+-----+-----+-----+-----+-----+-----+
Stall Penalty: 2 Bubble Cycles -> T_hazard = 8 + 2 = 10 Cycles
```

#### Case 3: Operand Forwarding (EX/MEM Bypass) — Latency = 8 Cycles
An internal hardware bypass bus routes the ALU output from the EX/MEM pipeline register directly back to the ALU input for $I_2$, eliminating the need to wait for writeback. The 2 stall bubbles are eliminated, restoring latency to 8 cycles.

---

## 5. Viva Voce Examination Questions & Answers

### Enemy 1: Sub-Register Aegis Sentry (COA-CO1)
> **Q**: *In the 80386 programmer's model, if an instruction modifies the 8-bit `AL` register, how does this affect `AX`, `AH`, and `EAX`?*  
> **A**: The `AL` register forms the lowest 8 bits (bits 0..7) of the 32-bit register `EAX`, while `AH` forms bits 8..15, and `AX` encompasses both as the lower 16 bits (bits 0..15). Modifying `AL` immediately alters the value of `AX` and the low byte of `EAX`. However, the contents of `AH` (bits 8..15) and the upper 16 bits of `EAX` (bits 16..31) remain completely unchanged unless an explicit carry or borrow is propagated by subsequent multi-precision arithmetic.

### Enemy 2: Fibonacci Skimmer (COA-CO2)
> **Q**: *How is parameter passing and register preservation managed when calling the assembly `_asm_fibonacci` routine under the standard cdecl convention?*  
> **A**: Under the 32-bit `cdecl` calling convention, arguments are pushed onto the hardware stack in right-to-left order (`push count`, `push outArrayPtr`). Inside the routine, a standard stack frame is established via `push ebp; mov ebp, esp`. The callee must preserve the callee-saved registers (`EBX`, `ESI`, `EDI`) by pushing them onto the stack before modifying them, and popping them before returning. The stack pointer is restored via `mov esp, ebp; pop ebp; ret`, and the caller cleans up the pushed parameters with `add esp, 8`.

### Enemy 3: Booth Multiplier Gunship (COA-CO3)
> **Q**: *Why does Booth's algorithm handle negative two's complement numbers correctly where standard unsigned shift-and-add algorithms fail?*  
> **A**: Standard shift-and-add assumes that a set bit represents a positive power of two ($+2^i$). In two's complement representation, the most significant bit (MSB) carries a negative weight ($-2^{N-1}$). Booth's algorithm evaluates adjacent bit pairs $(Q_i, Q_{i-1})$. When encountering a transition from 0 to 1 ($01$), it adds $+M$; when transitioning from 1 to 0 ($10$), it subtracts $-M$. Consecutive runs of 1s result in shifts only ($11 \implies 0$). By using Arithmetic Shift Right (`SAR`), the sign bit (MSB) is replicated, correctly preserving the signed magnitude across all negative inputs without requiring separate sign conversion steps.

### Enemy 4: Pipelined Railgun Dreadnought (COA-CO4)
> **Q**: *What causes a Read-After-Write (RAW) data hazard in a 5-stage pipeline, and how does operand forwarding prevent stall cycles?*  
> **A**: A Read-After-Write (RAW) data hazard occurs when an instruction $I_2$ requires a register value as an input during its Instruction Decode/Register Read (ID) stage, but a preceding instruction $I_1$ has not yet committed its result to the register file during its Write Back (WB) stage. Without intervention, the pipeline must insert stall bubbles (idle cycles) to delay $I_2$. Operand forwarding eliminates these bubbles by adding bypass data paths that feed the computed result directly from the EX/MEM or MEM/WB pipeline register to the ALU input of the execution stage, allowing $I_2$ to proceed without waiting for register writeback.

---

## 6. Build and Automated Test Verification

### Compilation Command
```powershell
powershell -ExecutionPolicy Bypass -File scripts\build_coa.ps1
```

**Build Output**:
```
========================================================
[COA] Building Computer Organization & Architecture Module...
Target: 32-bit 80386 Assembly Language (No x86-64 substitution)
========================================================
[INFO] Detected GCC/GAS Toolchain Target: mingw32 (32-bit i386 supported via -m32)
[INFO] Found 20 assembly source file(s). Assembling and linking...
[SUCCESS] COA 80386 module built successfully at bin\coa\coa_runner.exe!
```

### Test Runner Execution
```powershell
.\bin\coa\coa_runner.exe --test
```

**Full Test Output**:
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

[SUITE 5/5] COA Enemy Types Verification (Pure 80386 Assembly)...
  [PASS] COA-Enemy1: Aegis Sentry GPR Sub-register Partitioning (EAX -> AX -> AH/AL)
  [PASS] COA-Enemy1: Aegis Sentry AL Layer Damage preserves AH & Upper Word
  [PASS] COA-Enemy1: Aegis Sentry AH Layer Recharge preserves AL Layer
  [PASS] COA-Enemy1: Aegis Sentry Arithmetic Zero Flag (ZF=1 on Depletion)
  [PASS] COA-Enemy1: Aegis Sentry Sign & Carry Flags (SF=1, CF=1 on Over-penetration)
  [PASS] COA-Enemy1: Aegis Sentry SIB Addressing Mode [EBX + ESI*4 + 8]
  [PASS] COA-Enemy2: Fibonacci Skimmer Trajectory Weave Calculation
  [PASS] COA-Enemy2: Fibonacci Skimmer Weapon Timing Scaling F(n)*Cooldown
  [PASS] COA-Enemy3: Booth Gunship Firepower (+ * +): 7 * 5 = 35 Damage
  [PASS] COA-Enemy3: Booth Gunship Inverted Phase (+ * -): 9 * -4 = -36 (EMP Drain)
  [PASS] COA-Enemy3: Booth Gunship Dual Inversion (- * -): -11 * -3 = 33 Damage
  [PASS] COA-Enemy3: Booth Gunship Zero Vector Dud (0 * 45 = 0)
  [PASS] COA-Enemy3: Booth Gunship 8-bit Boundary Saturated (-64 * 2 = -128)
  [PASS] COA-Enemy4: Railgun Dreadnought Ideal 5-Stage Volley (8 Cycles, 0 Stalls)
  [PASS] COA-Enemy4: Railgun Dreadnought RAW Hazard Stall Bubbles (10 Cycles, 2 Stalls)
  [PASS] COA-Enemy4: Railgun Dreadnought Operand Forwarding Bypass (8 Cycles, 0 Stalls)

==============================================================================
                         FINAL COA TEST RESULTS SUMMARY                       
==============================================================================
  Total Assertions Run : 57
  Passed Assertions    : 57 [OK]
  Failed Assertions    : 0
  Target Architecture  : Intel 80386 Protected Mode (32-bit x86)
==============================================================================
[SUCCESS] ALL 80386 ASSEMBLY VERIFICATION TESTS PASSED WITH ZERO ERRORS!
```

---

## 7. Operational Limitations & Architectural Scope

In accordance with Phase 8.4 requirements, the architectural boundaries and integration scope are honestly documented:
1. **Autonomous 32-bit Assembly Execution**: The COA module operates as an independent 32-bit x86 Protected Mode console suite and automated test runner (`coa_runner.exe`). It compiles directly via MinGW GCC (`-m32 -O2`) into native x86 machine instructions.
2. **Absence of JNI / TCP Cross-Bridges**: Per mandatory rule #7, zero JNI, shared memory, or network socket bridges were introduced to communicate with Java or Web processes. All calculations execute authentically on the CPU registers.
3. **Educational Pipeline Scope**: The 5-stage pipeline model illustrates the canonical educational RISC execution cycle (IF, ID, EX, MEM, WB) and hazards. It does not model the physical prefetch queue or internal microcode sequencer of the historic 80386 microarchitecture.
4. **Course Outcome Integrity**: All COA enemy types map directly to the 4 verified syllabus outcomes (COA-CO1 through COA-CO4), maintaining the total project Course Outcome count at exactly 19.
