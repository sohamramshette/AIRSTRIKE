# AIRSTRIKE: Computer Organization & Architecture (COA) Module
## x86-64 Assembly Implementation (GAS Intel Syntax)

This module satisfies all four Course Outcomes (**COA-CO1 to COA-CO4**) using x86-64 Assembly language (GNU Assembler Intel syntax, Windows x64 ABI) and maps each CO to an enemy system and multiplayer demonstration.

Every file includes concise `[TOPIC: ...]` tags so any syllabus concept can be searched and located immediately.

---

### Course Outcome Structure & Search Index

1. **CO1 – Processor Architecture & Instruction Set (`COA/src/co1_architecture/`)**
   - `arch_demo.s`: General Purpose Registers & Sub-Register Partitioning (`RAX -> EAX -> AX -> AH/AL`), Index/Pointer Registers (`RSI, RDI, RBP, RSP` stack push/pop), RFLAGS (`CF, ZF, SF, OF`), Segmentation (`CS, DS, SS, ES`), Addressing Modes & SIB (`[Base + Index*Scale + Disp]`).
   - `register_shield.s`: **Sub-Register Aegis Sentry** enemy — nested shields in `AL` / `AH`, RFLAGS break/breach detection, SIB layer lookup.
   - `multiplayer_coa.s`: Multiplayer `CMP` score comparison, RFLAGS leader evaluation, SIB telemetry lookup.

2. **CO2 – Assembly Language Programs (`COA/src/co2_assembly_programs/`)**
   - `arithmetic_ops.s`: Prog 1 (Add/Sub), Prog 2 (Mul/Div with `CQO` + `IDIV` quotient/remainder & divide-by-zero guard).
   - `array_ops.s`: Prog 3 (Min/Max), Prog 4 (Sum/Average), Prog 7 (Linear Search), Prog 8 (In-Place Reversal), Prog 9 (Positive/Negative/Zero Counters).
   - `math_series.s`: Prog 5 (Recursive Factorial via stack frames), Prog 6 (Iterative Fibonacci).
   - `logic_ops.s`: Prog 10 (Bitwise `AND`, `OR`, `XOR`, `NOT` status bitmasks).
   - `fibonacci_skimmer.s`: **Fibonacci Skimmer** enemy — zigzag weave coordinates & salvo cooldown scaling.
   - `co2_menu.s`: Submenu and batch runner.

3. **CO3 – Computer Arithmetic Algorithms (`COA/src/co3_arithmetic/`)**
   - `booth_multiplier.s`: **Booth's Signed Binary Multiplication** (8-cycle `(Q0, Q-1)` recoding & Arithmetic Right Shift `SAR`, no `MUL`/`IMUL`).
   - `restoring_divider.s`: **Restoring Binary Division** (8-cycle shift-left, trial subtraction, restore on `A < 0`, no `DIV`/`IDIV`).
   - `carry_overflow.s`: 128-bit multi-precision addition with Carry (`ADC`) and signed Overflow (`OF` flag).
   - `booth_combat_eval.s`: **Booth Multiplier Gunship** enemy — kinetic firepower, EMP phase inversion, dud shots, and saturation.
   - `co3_menu.s`: Submenu and batch runner.

4. **CO4 – Control Unit & Instruction Pipelining (`COA/src/co4_control_unit/`)**
   - `control_unit.s`: Control signal truth table & decoder (`RegDst, ALUSrc, MemToReg, RegWrite, MemRead, MemWrite, Branch, ALUOp`) + Hardwired vs. Microprogrammed trade-offs.
   - `pipeline_sim.s`: 5-Stage Pipeline (`IF, ID, EX, MEM, WB`), RAW Data Hazard stalls, Branch Control Hazard flushes, and `N=100` speedup benchmark.
   - `pipelined_railgun.s`: **Pipelined Railgun Dreadnought** enemy — Ideal, RAW Stall, and Operand Forwarding bypass simulation.
   - `co4_menu.s`: Submenu and batch runner.

5. **Automated Verification Harness (`COA/tests/test_coa.s`)**
   - **65 automated unit test assertions** across all 4 COs, 4 Academic Enemies, and Multiplayer modules.

---

### Building and Running

- **Build**:
  ```cmd
  COA\build_coa.bat
  ```
  *(or `scripts\build_coa.bat` / `.\scripts\build_coa.ps1`)*
- **Run Interactive Menu**:
  ```cmd
  bin\coa\coa_runner.exe
  ```
- **Run Automated Verification Tests (65 Assertions)**:
  ```cmd
  bin\coa\coa_runner.exe --test
  ```
