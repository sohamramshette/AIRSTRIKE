# AIRSTRIKE: Computer Organization & Architecture (COA) Module
## 80386 32-bit Protected Mode Assembly Implementation

This module satisfies all four Course Outcomes (**COA-CO1 to COA-CO4**) for Computer Organization and Architecture using genuine 32-bit Intel 80386 Assembly language (GNU Assembler Intel syntax, MinGW GCC `-m32`).

---

### Course Outcome Structure

1. **CO1 – 80386 Architecture & Instruction Set (`COA/src/co1_architecture/arch_demo.s`)**
   - General-Purpose Registers: `EAX`, `EBX`, `ECX`, `EDX` and 16-bit/8-bit partitions (`AX`/`AL`/`AH`).
   - Pointer & Index Registers: `ESI`, `EDI`, `EBP`, `ESP` with stack push/pop displacement demonstration.
   - EFLAGS Register: Carry (`CF`), Zero (`ZF`), Sign (`SF`), and Overflow (`OF`) condition codes.
   - Memory Segmentation: `CS`, `DS`, `SS`, `ES` 32-bit protected mode selector demonstration.
   - 80386 Addressing Modes: Immediate, Register, Direct, Indirect, and SIB (`[Base + Index*Scale + Disp]`).
   - Core Instruction Categories: Data transfer, Arithmetic, Logic, Branching, and Stack manipulation.

2. **CO2 – Assembly Language Programs (`COA/src/co2_assembly_programs/`)**
   - `arithmetic_ops.s`: 1. Addition & Subtraction, 2. Multiplication & Division with Quotient/Remainder.
   - `array_ops.s`: 3. Largest & Smallest element, 4. Array Sum & Average, 7. Linear Search, 8. In-place Reverse Array, 9. Positive/Negative/Zero Counters.
   - `math_series.s`: 5. Factorial (Recursive via stack frames), 6. Fibonacci Series Generation.
   - `logic_ops.s`: 10. Bitwise `AND`, `OR`, `XOR`, `NOT` on spacecraft status bitmasks.
   - `co2_menu.s`: Submenu and batch runner.

3. **CO3 – Computer Arithmetic Algorithms (`COA/src/co3_arithmetic/`)**
   - `booth_multiplier.s`: Pure assembly implementation of **Booth's Signed Binary Multiplication Algorithm** ($M, Q, A, Q_{-1}$ registers, arithmetic right shift `SAR`, cycle-by-cycle step table, verified across all 4 sign quadrants).
   - `restoring_divider.s`: Pure assembly implementation of **Restoring Binary Division Algorithm** ($Q, M, A$ registers, restore subtraction on negative partial remainder, cycle-by-cycle step table, exact and non-exact divisions).
   - `carry_overflow.s`: Multi-precision 64-bit addition with Carry (`ADC`), signed overflow detection (`OF` flag and `JO`).
   - `co3_menu.s`: Submenu and batch runner.

4. **CO4 – Control Unit & Instruction Pipelining (`COA/src/co4_control_unit/`)**
   - `control_unit.s`: Control unit micro-operations and signal generation matrix (`RegDst`, `ALUSrc`, `MemToReg`, `RegWrite`, `MemRead`, `MemWrite`, `Branch`, `ALUOp`) for R-Type, LW, SW, BEQ, JMP.
   - `pipeline_sim.s`: 5-Stage Pipeline Simulator (`IF`, `ID`, `EX`, `MEM`, `WB`), Read-After-Write (RAW) data hazard with STALL bubbles, branch control hazard with pipeline FLUSH, and theoretical/realized speedup analysis.
   - `co4_menu.s`: Submenu and batch runner.

5. **Automated Verification Harness (`COA/tests/test_coa.s`)**
   - 34 automated unit test assertions covering all 4 COs with 100% pass validation.

---

### Building and Running

- **Build with Batch**:
  ```cmd
  COA\build_coa.bat
  ```
  *or project-wide:*
  ```cmd
  scripts\build_coa.bat
  ```
- **Build with PowerShell**:
  ```powershell
  .\scripts\build_coa.ps1
  ```
- **Run Interactive Menu**:
  ```cmd
  bin\coa\coa_runner.exe
  ```
  *or through launcher:*
  ```cmd
  run_airstrike.bat coa
  ```
- **Run Headless Automated Verification Tests**:
  ```cmd
  bin\coa\coa_runner.exe --test
  ```

