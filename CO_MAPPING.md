# AIRSTRIKE: Course Outcome (CO) Mapping Specification

**Project Title**: AIRSTRIKE – 2D Space Shooter & Academic Demonstration System  
**Document Version**: 1.0.0 (Phase 1 Foundation)  
**Total Course Outcomes**: 19 (Exhaustively Mapped)  
**Baseline Completion Status**: Planned (Not Started)  

---

## Overview Table of Course Outcomes

| Subject Code | Subject Name | Outcome ID | Outcome Description | Target Module | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PL** | Programming Laboratory (C++) | **PL-CO1** | Arrays | `PL/src/arrays/` | Completed (Phase 3) |
| **PL** | Programming Laboratory (C++) | **PL-CO2** | Suitable Linked Lists | `PL/src/linked_lists/` | Completed (Phase 3) |
| **PL** | Programming Laboratory (C++) | **PL-CO3** | Stack and Queue Applications | `PL/src/stacks/`, `queues/` | Completed (Phase 3) |
| **PL** | Programming Laboratory (C++) | **PL-CO4** | Searching and Sorting Techniques | `PL/src/searching/`, `sorting/` | Completed (Phase 3) |
| **PL** | Programming Laboratory (C++) | **PL-CO5** | Hashing Techniques | `PL/src/hashing/` | Completed (Phase 3) |
| **COA** | Comp. Org. & Architecture (80386) | **COA-CO1** | 80386 Architecture & Instruction Set | `COA/src/co1_architecture/` | Completed (Phase 4) |
| **COA** | Comp. Org. & Architecture (80386) | **COA-CO2** | Assembly Language Programs (80386) | `COA/src/co2_assembly_programs/` | Completed (Phase 4) |
| **COA** | Comp. Org. & Architecture (80386) | **COA-CO3** | Computer Arithmetic & Booth Algorithm | `COA/src/co3_arithmetic/` | Completed (Phase 4) |
| **COA** | Comp. Org. & Architecture (80386) | **COA-CO4** | Control Unit & Instruction Pipelining | `COA/src/co4_control_unit/` | Completed (Phase 4) |
| **CGL** | Computer Graphics Lab (OpenGL) | **CGL-CO1** | OpenGL Graphics Primitives | `CGL/src/primitives/` | Planned |
| **CGL** | Computer Graphics Lab (OpenGL) | **CGL-CO2** | Geometric Transformations | `CGL/src/transformations/` | Planned |
| **CGL** | Computer Graphics Lab (OpenGL) | **CGL-CO3** | 2D and 3D Algorithms | `CGL/src/algorithms/` | Planned |
| **CGL** | Computer Graphics Lab (OpenGL) | **CGL-CO4** | Projection and Viewing | `CGL/src/projection/` | Planned |
| **CGL** | Computer Graphics Lab (OpenGL) | **CGL-CO5** | Clipping Algorithms | `CGL/src/clipping/` | Planned |
| **CGL** | Computer Graphics Lab (OpenGL) | **CGL-CO6** | Texture, Lighting and Color | `CGL/src/appearance/` | Planned |
| **PSOOP** | Principles of OOP (Java) | **PSOOP-CO1** | Object-Oriented Solutions | `PSOOP/src/model/` | Completed (Phase 2) |
| **PSOOP** | Principles of OOP (Java) | **PSOOP-CO2** | Solutions to Reduce Complexity | `PSOOP/src/service/`, `game/` | Completed (Phase 2) |
| **PSOOP** | Principles of OOP (Java) | **PSOOP-CO3** | Critical Thinking & Problem Solving | `PSOOP/src/exception/`, `service/` | Completed (Phase 2) |
| **PSOOP** | Principles of OOP (Java) | **PSOOP-CO4** | Logic-Building for Real-Life Problems | `PSOOP/src/game/`, `ui/` | Completed (Phase 2) |

---

# 1. Programming Laboratory (PL) – C++17

### PL-CO1: Arrays
- **Required concepts**:
  - Memory representation of 1D and 2D arrays in contiguous memory (row-major addressing: $\text{Address}(A[i][j]) = \text{Base} + (i \times C + j) \times \text{sizeof}(T)$).
  - Dynamic heap allocation and memory deallocation (`new[]` and `delete[]`).
  - Amortized geometric capacity resizing (1.5x / 2.0x factor).
  - Bounds checking, raw pointer iteration, and circular buffer pooling for game projectiles.
- **Planned implementation**:
  - Custom dynamic array template class `CustomArray<T>` without using `std::vector`.
  - Fixed-size 2D spatial grid `GridMap<T>` for space sector entity partitioning.
  - Projectile memory pool (`ProjectilePool`) built on contiguous array memory to avoid frequent heap allocations.
- **Source file location**:
  - `PL/src/arrays/array_ops.h` / `array_ops.cpp`
  - `PL/src/arrays/dynamic_array.h` / `dynamic_array.cpp`
  - `PL/src/arrays/bullet_pool.h` / `bullet_pool.cpp`
  - `PL/src/arrays/collision_grid.h` / `collision_grid.cpp`
- **Demonstration method**:
  - Interactive CLI test suite demonstrating array instantiation, element insertion, capacity doubling upon saturation, out-of-bounds boundary detection, bullet pool reuse, and 2D asteroid collision grid broadphase lookup.
- **Test cases**:
  - 10 automated unit tests in `PL/tests/test_arrays.cpp` verified with 100% pass rate.
- **Viva explanation**:
  - Why arrays offer $O(1)$ random access due to direct base-offset pointer arithmetic.
  - Comparison between contiguous array cache spatial locality and non-contiguous memory access.
  - Mathematical proof of amortized $O(1)$ push-back time complexity despite occasional $O(N)$ reallocation.
- **Completion status**: Completed (Phase 3)

---

### PL-CO2: Suitable Linked Lists
- **Required concepts**:
  - Dynamic node memory allocation via pointers (`struct Node { T data; Node* next; Node* prev; }`).
  - Singly Linked List, Doubly Linked List, and Circular Linked List structures.
  - Node insertion and deletion at head, tail, and arbitrary node references in $O(1)$ time.
  - Safe traversal, reverse traversal, and robust destructor traversal to prevent memory leaks.
- **Planned implementation**:
  - Custom Doubly Linked List (`DoublyLinkedList<T>`) for managing active combat particles and live enemy squadron formations.
  - Circular Singly Linked List (`CircularList<T>`) for weapon selection cycling in the spacecraft HUD.
- **Source file location**:
  - `PL/src/linked_lists/singly_linked_list.h` / `singly_linked_list.cpp`
  - `PL/src/linked_lists/doubly_linked_list.h` / `doubly_linked_list.cpp`
  - `PL/src/linked_lists/circular_linked_list.h` / `circular_linked_list.cpp`
- **Demonstration method**:
  - CLI harness displaying forward and backward traversal of squadron nodes, deletion of arbitrary active nodes without index shifting, and infinite cycle stepping for weapon selection.
- **Test cases**:
  - 10 automated unit tests in `PL/tests/test_linked_lists.cpp` verified with 100% pass rate.
- **Viva explanation**:
  - Trade-offs between arrays ($O(1)$ access, expensive $O(N)$ deletion) and linked lists ($O(1)$ deletion with known pointer, $O(N)$ sequential traversal).
  - Internal overhead of two pointer fields (`next`, `prev`) per node on 32-bit (8 bytes) vs 64-bit (16 bytes) systems.
  - Cache misses caused by heap node dispersion across non-contiguous memory pages.
- **Completion status**: Completed (Phase 3)

---

### PL-CO3: Stack and Queue Applications
- **Required concepts**:
  - LIFO (Last-In, First-Out) stack operations (`push`, `pop`, `peek`, `isEmpty`).
  - FIFO (First-In, First-Out) queue operations (`enqueue`, `dequeue`, `front`, `isEmpty`).
  - Circular array queue implementation using modulo arithmetic: $\text{tail} = (\text{tail} + 1) \pmod{\text{capacity}}$.
  - Applications: Undo/Redo command pattern stack for player navigation; game event buffer queue for deterministic frame simulation.
- **Planned implementation**:
  - Array-based stack template `ActionStack<T>` implementing player command history.
  - Circular array queue `EventQueue<T>` for sequencing incoming game inputs and network packets.
- **Source file location**:
  - `PL/src/stacks/action_stack.h` / `action_stack.cpp`
  - `PL/src/queues/wave_queue.h` / `wave_queue.cpp`
- **Demonstration method**:
  - CLI demonstration recording player combat action history, undoing actions via LIFO stack popping, and dispatching incoming enemy waves sequentially via FIFO circular queue.
- **Test cases**:
  - 12 automated unit tests across `PL/tests/test_stack.cpp` and `PL/tests/test_queue.cpp` verified with 100% pass rate.
- **Viva explanation**:
  - Call stack operation in program execution (activation records, return addresses, local variables).
  - Why circular queues prevent array drift without requiring $O(N)$ element shifts.
  - Time complexities of stack and queue operations ($O(1)$ constant time guarantee).
- **Completion status**: Completed (Phase 3)

---

### PL-CO4: Searching and Sorting Techniques
- **Required concepts**:
  - Linear Search ($O(N)$) vs Binary Search ($O(\log N)$ on ordered datasets).
  - Quadratic sorting algorithms: Insertion Sort ($O(N^2)$ worst-case, $O(N)$ best-case for nearly sorted), Bubble Sort (with early exit flag), Selection Sort ($O(N)$ swaps).
  - Divide-and-Conquer sorting algorithms: QuickSort ($O(N \log N)$ average, partition in-place) and MergeSort ($O(N \log N)$ guaranteed, stable).
  - Algorithm stability, space complexity (in-place vs auxiliary buffer), recursion call-stack depth.
- **Planned implementation**:
  - High-score leaderboard manager implementing custom `QuickSort` and `MergeSort`.
  - Iterative `BinarySearch` and `LinearSearch` for player query and rank lookup.
- **Source file location**:
  - `PL/src/searching/search_algorithms.h` / `search_algorithms.cpp`
  - `PL/src/sorting/sort_algorithms.h` / `sort_algorithms.cpp`
- **Demonstration method**:
  - CLI benchmarking module executing 5 sorts across unsorted datasets; displaying comparison counts, swaps, and execution time; followed by binary search queries on AirStrike leaderboard scores.
- **Test cases**:
  - 13 automated unit tests across `PL/tests/test_searching.cpp` and `PL/tests/test_sorting.cpp` verified with 100% pass rate.
- **Viva explanation**:
  - Mathematical derivation of MergeSort recurrence relation $T(N) = 2T(N/2) + O(N) \implies O(N \log N)$ via Master Theorem.
  - Worst-case QuickSort degradation to $O(N^2)$ when pivot selection consistently produces unbalanced partitions ($0$ and $N-1$).
  - Why binary search requires a monotonic/sorted precondition and its halving logarithmic progression.
- **Completion status**: Completed (Phase 3)

---

### PL-CO5: Hashing Techniques
- **Required concepts**:
  - Hash function properties (deterministic, uniform distribution, minimal collisions).
  - Common hash algorithms (Modulo division, polynomial string hashing such as djb2 or rolling polynomial).
  - Open Addressing collision resolution: Linear Probing ($h(k, i) = (h'(k) + i) \pmod M$) with Tombstone deletion marker.
  - Closed Addressing: Separate Chaining with bucket linked lists.
  - Load factor $\alpha = N / M$, clustering phenomena (primary clustering), automatic dynamic rehashing.
- **Planned implementation**:
  - Key-value hash tables supporting both Linear Probing (`HashTableProbing`) and Separate Chaining (`HashTableChaining`).
  - Game asset registry and player achievement lookup dictionary using string keys.
- **Source file location**:
  - `PL/src/hashing/hash_table_chaining.h` / `hash_table_chaining.cpp`
  - `PL/src/hashing/hash_table_probing.h` / `hash_table_probing.cpp`
- **Demonstration method**:
  - CLI test module displaying hash bucket distribution, deliberately injecting colliding keys to demonstrate linear probing and separate chaining, and showcasing automatic rehashing upon exceeding $\alpha > 0.70$ or $0.75$.
- **Test cases**:
  - 8 automated unit tests in `PL/tests/test_hashing.cpp` verified with 100% pass rate.
- **Viva explanation**:
  - Average-case $O(1)$ vs worst-case $O(N)$ lookup complexity in hash tables.
  - Comparison between open addressing (cache friendly, prone to clustering) and separate chaining (extra pointer overhead, resilient to high load factors).
  - Why deleted entries in open addressing require a special "tombstone" marker rather than `nullptr`.
- **Completion status**: Completed (Phase 3)

---

# 2. Computer Organization and Architecture (COA) – 80386 ASM

### COA-CO1: 80386 Architecture and Instruction Set
- **Required concepts**:
  - 80386 programmer's model: 32-bit General Purpose Registers (`EAX`, `EBX`, `ECX`, `EDX`, `ESI`, `EDI`, `ESP`, `EBP`).
  - Sub-register partitioning (`AX`, `AH`, `AL` within `EAX`).
  - Segment registers (`CS`, `DS`, `SS`, `ES`, `FS`, `GS`) and flat memory model under 32-bit protected mode.
  - EFLAGS register flags: Zero Flag (ZF), Carry Flag (CF), Sign Flag (SF), Overflow Flag (OF), Parity Flag (PF).
  - Addressing modes: Immediate, Register, Direct, Register Indirect, and Based-Indexed-Scaled (SIB: `[Base + Index*Scale + Disp]`).
  - Instruction set basics: Data transfer (`MOV`, `XCHG`, `PUSH`, `POP`), Arithmetic (`ADD`, `SUB`, `INC`, `DEC`), Logic (`AND`, `OR`, `XOR`, `NOT`, `TEST`).
- **Implemented solution**:
  - Pure 32-bit 80386 assembly module (`arch_demo.s`) displaying and testing all 8 general-purpose registers, sub-register partitioning (`EAX` -> `AX` -> `AH`/`AL`), 4 pointer/index registers, hardware stack push/pop dynamics, 4 condition code flags (`CF`, `ZF`, `SF`, `OF`), segment selectors (`CS`, `DS`, `SS`, `ES`), and 5 distinct addressing modes including 80386 SIB `[EBX + ESI*4 + 8]`.
- **Source file location**:
  - `COA/src/co1_architecture/arch_demo.s`
  - `COA/src/common/io.s`
- **Demonstration method**:
  - Console demonstration via menu option `[1]` in `bin/coa/coa_runner.exe` and automated test assertions via `--test`.
- **Test cases**:
  - 6 automated unit tests in `COA/tests/test_coa.s` verifying register partitioning, EFLAGS updates, and SIB addressing with 100% pass rate.
- **Viva explanation**:
  - Difference between real mode (16-bit segmented, 1 MB limit) and protected mode (32-bit flat memory model, 4 GB limit, privilege rings).
  - Detailed definition of each EFLAGS bit and how conditional branches evaluate flag conditions.
  - SIB effective address calculation formula: $\text{EA} = \text{Base} + (\text{Index} \times \text{Scale}) + \text{Disp}$.
- **Completion status**: Completed (Phase 4)

---

### COA-CO2: Assembly Language Programs Using 80386 Instructions
- **Required concepts**:
  - Control flow: Unconditional jumps (`JMP`) and conditional jumps (`JE`, `JNE`, `JL`, `JLE`, `JG`, `JGE`, `JB`, `JAE`).
  - Arithmetic operations: Addition, subtraction, signed multiplication (`IMUL`), and division with remainder (`IDIV` with `CDQ`).
  - Array operations: Min/Max finding, array summation & average, linear search with index return, two-pointer in-place reversal, and counting positive, negative, and zero values.
  - Mathematical series: Recursive factorial via 80386 stack frames, and iterative Fibonacci series generation.
  - Bitwise logic: `AND`, `OR`, `XOR`, `NOT` on spacecraft subsystem status registers.
  - Standard C calling convention (cdecl): Stack parameter passing (`[ebp + 8]`), local stack frames (`[ebp - 4]`), register preservation (`EBX`, `ESI`, `EDI`), and return value in `EAX`.
- **Implemented solution**:
  - Complete suite of 10 genuine 80386 assembly programs organized across `arithmetic_ops.s`, `array_ops.s`, `math_series.s`, and `logic_ops.s`.
- **Source file location**:
  - `COA/src/co2_assembly_programs/arithmetic_ops.s`
  - `COA/src/co2_assembly_programs/array_ops.s`
  - `COA/src/co2_assembly_programs/math_series.s`
  - `COA/src/co2_assembly_programs/logic_ops.s`
  - `COA/src/co2_assembly_programs/co2_menu.s`
- **Demonstration method**:
  - Interactive submenu option `[2]` in `bin/coa/coa_runner.exe` allowing user input or batch automated test execution.
- **Test cases**:
  - 14 automated unit tests in `COA/tests/test_coa.s` verifying all 10 programs across positive, negative, edge, and recursive conditions with 100% pass rate.
- **Viva explanation**:
  - Differences between caller-saved (`EAX`, `ECX`, `EDX`) and callee-saved (`EBX`, `ESI`, `EDI`, `EBP`, `ESP`) registers under cdecl.
  - Stack frame organization with negative local offsets and positive parameter offsets.
  - Importance of `CDQ` sign-extending `EAX` into `EDX:EAX` before signed `IDIV`.
- **Completion status**: Completed (Phase 4)

---

### COA-CO3: Computer Arithmetic Algorithms, Including Booth Multiplication
- **Required concepts**:
  - Two's complement signed integer representation, sign extension, arithmetic right shifts preserving MSB (`SAR`).
  - Booth's Multiplication Algorithm for signed binary integers using pairwise recoding: $(Q_0, Q_{-1}) \in \{01 \implies +M,\ 10 \implies -M,\ 00/11 \implies \text{Shift only}\}$.
  - Restoring Binary Division Algorithm for unsigned integers using shift-left, subtract, sign check, and conditional restore.
  - Multi-precision binary addition using `ADC` (Add with Carry) across multiple 32-bit words.
  - Hardware signed arithmetic overflow detection ($OF$ flag condition).
- **Implemented solution**:
  - Pure 80386 assembly implementation of Booth's algorithm (`booth_multiplier.s`) generating a complete cycle-by-cycle trace table with binary output.
  - Pure 80386 assembly implementation of Restoring Division (`restoring_divider.s`) generating a complete cycle-by-cycle trace table with binary output and remainder verification.
  - Multi-precision 64-bit addition (`carry_overflow.s`) propagating carry between words, and signed overflow tests on $OF$ flag.
- **Source file location**:
  - `COA/src/co3_arithmetic/booth_multiplier.s`
  - `COA/src/co3_arithmetic/restoring_divider.s`
  - `COA/src/co3_arithmetic/carry_overflow.s`
  - `COA/src/co3_arithmetic/co3_menu.s`
- **Demonstration method**:
  - Interactive submenu option `[3]` in `bin/coa/coa_runner.exe` showing full binary shift tables and verification formulas ($M \times Q$ and $\text{Dividend} = \text{Quotient} \times \text{Divisor} + \text{Remainder}$).
- **Test cases**:
  - 12 automated unit tests in `COA/tests/test_coa.s` covering Booth ($+ \times +$, $+ \times -$, $- \times +$, $- \times -$, boundary $-64 \times 2 = -128$), Restoring Division (exact, non-exact, $Q < M$), multi-precision `ADC`, and positive/negative overflow with 100% pass rate.
- **Viva explanation**:
  - Why standard shift-and-add fails for negative two's complement numbers and how Booth recoding resolves consecutive runs of 1s ($2^{k+1} - 2^m$).
  - Difference between Arithmetic Shift Right (`SAR`) and Logical Shift Right (`SHR`).
  - Restoring vs Non-Restoring division cycle trade-offs.
- **Completion status**: Completed (Phase 4)

---

### COA-CO4: Control Signal Generation, Control Unit Organization and Instruction Pipelining
- **Required concepts**:
  - Control Unit (CU) architectures: Hardwired Control Unit (combinational logic gates, high speed) vs Microprogrammed Control Unit (control memory ROM, microinstructions, flexibility).
  - Instruction Cycle stages: IF (Fetch), ID (Decode), EX (Execute), MEM (Memory Access), WB (Write Back).
  - 80386 Control signal truth table for R-type, LW, SW, BEQ, JMP instructions (`RegDst`, `ALUSrc`, `MemToReg`, `RegWrite`, `MemRead`, `MemWrite`, `Branch`, `ALUOp`).
  - 5-Stage Instruction Pipeline Simulation and Hazard Handling:
    - Ideal pipeline matrix throughput.
    - Data Hazards: Read-After-Write (RAW) requiring 2 stall bubbles.
    - Control Hazards: Branch evaluation requiring pipeline flush (branch penalty).
    - Performance speedup metrics ($T_{\text{seq}}$ vs $T_{\text{pipe}}$).
- **Implemented solution**:
  - Pure 80386 assembly Control Signal generator (`control_unit.s`) decoding opcodes into the standard control bus matrix.
  - Pure 80386 assembly 5-stage pipeline simulator (`pipeline_sim.s`) visualizing stage matrices for ideal execution, RAW data hazard stall bubbles, branch flush penalty, and quantitative benchmark speedup metrics ($4.80\times$ ideal, $4.67\times$ with hazards for $N=100$).
- **Source file location**:
  - `COA/src/co4_control_unit/control_unit.s`
  - `COA/src/co4_control_unit/pipeline_sim.s`
  - `COA/src/co4_control_unit/co4_menu.s`
- **Demonstration method**:
  - Interactive submenu option `[4]` in `bin/coa/coa_runner.exe` displaying formatted pipeline stage timelines and speedup calculations.
- **Test cases**:
  - 2 automated unit tests in `COA/tests/test_coa.s` validating control signal truth matrix generation and 5-stage pipeline hazard simulation metrics.
- **Viva explanation**:
  - Trade-offs between hardwired and microprogrammed control units.
  - Types of pipeline hazards: Structural, Data (RAW, WAR, WAW), and Control (Branch).
  - Theoretical vs realized speedup formulas ($S = k / (1 + \text{hazard stalls})$).
- **Completion status**: Completed (Phase 4)

---

# 3. Computer Graphics Laboratory (CGL) – C++ & OpenGL / GLUT

### CGL-CO1: OpenGL Graphics Primitives
- **Required concepts**:
  - OpenGL Client-Server architecture and State Machine model.
  - Normalized Device Coordinates (NDC) space $[-1.0, 1.0]^3$.
  - Graphics primitives enclosed in `glBegin()` and `glEnd()`:
    - Points: `GL_POINTS` (`glPointSize`).
    - Lines: `GL_LINES`, `GL_LINE_STRIP`, `GL_LINE_LOOP` (`glLineWidth`, `glLineStipple`).
    - Polygons: `GL_TRIANGLES`, `GL_TRIANGLE_STRIP`, `GL_TRIANGLE_FAN`, `GL_QUADS`, `GL_POLYGON`.
  - Vertex attributes: Position (`glVertex2f`, `glVertex3f`), Color (`glColor3f`, `glColor4f`).
  - Double buffering mechanism: Front buffer vs Back buffer, `glutSwapBuffers()`.
- **Planned implementation**:
  - Vector rendering module for space combat elements: player fighter hull (polygons), laser beams (lines), starfield background (points), and thruster particle fans.
- **Source file location**:
  - `CGL/src/primitives/space_primitives.hpp`
  - `CGL/src/primitives/space_primitives.cpp`
- **Demonstration method**:
  - Standalone OpenGL/GLUT window rendering a structured showcase of all primitive types, clearly annotated with on-screen raster text labels and toggleable wireframe modes (`glPolygonMode`).
- **Test cases**:
  - `TC_CGL_01A`: Render starfield using `GL_POINTS` with varying point sizes and color intensities.
  - `TC_CGL_01B`: Render laser beams using `GL_LINES` with modified line widths.
  - `TC_CGL_01C`: Render space fighter geometry using `GL_TRIANGLES` and `GL_TRIANGLE_FAN` verifying vertex winding order.
  - `TC_CGL_01D`: Smooth Gouraud vertex color interpolation across primitive surfaces.
- **Viva explanation**:
  - OpenGL execution pipeline: Vertex Processing $\rightarrow$ Primitive Assembly $\rightarrow$ Rasterization $\rightarrow$ Fragment Processing $\rightarrow$ Framebuffer.
  - Why double buffering eliminates screen flickering and tearing during real-time rendering.
  - Vertex winding rule (Counter-Clockwise - CCW for front faces, Clockwise - CW for back faces).
- **Completion status**: Planned (Not Started)

---

### CGL-CO2: Geometric Transformations
- **Required concepts**:
  - Homogeneous coordinates: 2D points represented as $[x, y, 1]^T$, 3D points as $[x, y, z, 1]^T$.
  - Affine transformation matrices ($4 \times 4$):
    - Translation Matrix $T(t_x, t_y, t_z)$ (`glTranslatef`).
    - Rotation Matrix $R(\theta, u_x, u_y, u_z)$ (`glRotatef`).
    - Scaling Matrix $S(s_x, s_y, s_z)$ (`glScalef`).
  - Matrix stack architecture: `glPushMatrix()` and `glPopMatrix()`, matrix stack depth limits.
  - Composite transformations and the non-commutative nature of matrix multiplication ($T \times R \neq R \times T$).
  - Transformation order: Translate-to-origin $\rightarrow$ Rotate/Scale $\rightarrow$ Translate-back for arbitrary pivot transformations.
- **Planned implementation**:
  - Spacecraft dynamic transformation controller: fighter rotation about center axis, orbital revolution around parent space station, thruster pulsation scaling, and turret attachment hierarchical transforms.
- **Source file location**:
  - `CGL/src/transformations/transform_demo.hpp`
  - `CGL/src/transformations/transform_demo.cpp`
- **Demonstration method**:
  - Interactive GLUT window allowing keyboard control of object translation, rotation, and scaling; visual demonstration of pivot-point transformations vs origin transformations; on-screen matrix values.
- **Test cases**:
  - `TC_CGL_02A`: Pure translation verifying correct vertex offset in screen space.
  - `TC_CGL_02B`: Rotation about spacecraft center point verifying lack of orbital drift.
  - `TC_CGL_02C`: Composite transformation order demonstration showing differences between $(T \times R)$ and $(R \times T)$.
  - `TC_CGL_02D`: Hierarchical transformations using `glPushMatrix`/`glPopMatrix` for rotating weapon pods mounted on moving ship.
- **Viva explanation**:
  - Why homogeneous coordinates are necessary to represent translation as a matrix multiplication rather than vector addition.
  - Why matrix multiplication order in OpenGL is written in reverse of execution order (post-multiplication column-vector convention: $v' = M_1 \cdot M_2 \cdot M_3 \cdot v$).
  - Purpose of the OpenGL matrix stack (`MODELVIEW`, `PROJECTION`) and stack depth overflow hazards.
- **Completion status**: Planned (Not Started)

---

### CGL-CO3: 2D and 3D Algorithms
- **Required concepts**:
  - Line drawing algorithms:
    - Digital Differential Analyzer (DDA): Floating-point incremental approach.
    - Bresenham's Line Algorithm: Pure integer arithmetic, decision parameter $p_k$, error term tracking for $|m| < 1$ and $|m| \ge 1$.
  - Circle drawing algorithms:
    - Midpoint Circle Algorithm / Bresenham's Circle Algorithm: 8-way octant symmetry, integer decision parameter $p_k = 1 - r$.
  - Polygon filling algorithms:
    - Boundary-Fill Algorithm (4-connected and 8-connected recursion).
    - Flood-Fill Algorithm.
    - Scanline Polygon Fill Algorithm: Edge Table (ET), Active Edge Table (AET), parity rule, horizontal span filling.
- **Planned implementation**:
  - Software rasterizer module implementing Bresenham's Line Algorithm (laser trajectory / radar lines), Midpoint Circle Algorithm (blast shockwaves and shield bubbles), and Scanline Polygon Fill (asteroid rendering) by plotting individual screen pixels (`glDrawPixels` / `GL_POINTS`).
- **Source file location**:
  - `CGL/src/algorithms/raster_algorithms.hpp`
  - `CGL/src/algorithms/raster_algorithms.cpp`
- **Demonstration method**:
  - Dual-pane OpenGL window: Left side displays custom software-rasterized algorithms; Right side displays hardware OpenGL rasterization for visual accuracy comparison and performance benchmarking.
- **Test cases**:
  - `TC_CGL_03A`: Bresenham line drawing across all 8 octants (slopes $0 \le m \le 1$, $m > 1$, negative slopes, vertical and horizontal lines).
  - `TC_CGL_03B`: Midpoint circle generation with odd and even radii verifying 8-way symmetry plotting.
  - `TC_CGL_03C`: Scanline polygon filling on convex and concave asteroid polygons.
  - `TC_CGL_03D`: Boundary-fill recursion depth verification and stack overflow prevention.
- **Viva explanation**:
  - Why Bresenham's algorithm is faster than DDA (eliminates floating-point multiplication/rounding, uses only fast integer addition and bit shifts).
  - Derivation of the Midpoint Circle decision parameter update: $p_{k+1} = p_k + 2x_{k+1} + 1$ (if $p_k < 0$) vs $p_k + 2x_{k+1} + 1 - 2y_{k+1}$ (if $p_k \ge 0$).
  - How the Scanline algorithm avoids pixel re-drawing by sorting edges by minimum $y$ and updating active $x$-intersections.
- **Completion status**: Planned (Not Started)

---

### CGL-CO4: Projection and Viewing
- **Required concepts**:
  - Graphics viewing pipeline: World Coordinates $\rightarrow$ Viewing Coordinates (Camera space) $\rightarrow$ Projection Coordinates (Clip space) $\rightarrow$ Normalized Device Coordinates (NDC) $\rightarrow$ Screen Coordinates (Viewport).
  - Parallel / Orthographic Projection:
    - Affine mapping, preservation of parallel lines, no perspective foreshortening.
    - OpenGL `glOrtho(left, right, bottom, top, near, far)`.
  - Perspective Projection:
    - Center of Projection (COP), vanishing points, realistic distance foreshortening ($1/z$ scaling).
    - OpenGL `glFrustum()`, `gluPerspective(fovy, aspect, zNear, zFar)`.
  - Camera positioning: Look-at transformation using `gluLookAt(eye, center, up)`.
  - Viewport mapping: `glViewport(x, y, width, height)`.
- **Planned implementation**:
  - Multi-camera viewing system for Airstrike:
    - Tactical 2D Top-Down Orthographic View for radar HUD and classic 2D shooter gameplay.
    - Cinematic 3D Chase-Camera Perspective View for asteroid field depth visualization.
- **Source file location**:
  - `CGL/src/projection/viewing_demo.hpp`
  - `CGL/src/projection/viewing_demo.cpp`
- **Demonstration method**:
  - Interactive split-screen OpenGL window rendering the same space combat scene simultaneously through an Orthographic camera and a Perspective camera; dynamic aspect ratio preservation on window resize.
- **Test cases**:
  - `TC_CGL_04A`: Orthographic projection maintaining constant object size regardless of $Z$ distance.
  - `TC_CGL_04B`: Perspective projection exhibiting natural size reduction as distance $Z$ increases toward $zFar$.
  - `TC_CGL_04C`: Window resize callback (`reshape`) correctly updating aspect ratio without geometric distortion.
  - `TC_CGL_04D`: Viewport clipping removing objects behind the camera or beyond the far plane.
- **Viva explanation**:
  - Differences between parallel and perspective projection matrices in homogeneous coordinates.
  - Perspective division: why coordinates $(x, y, z, w)$ must be divided by $w$ ($x_{ndc} = x/w$) to produce non-linear depth foreshortening.
  - Role of near and far clipping planes in preventing depth-buffer (Z-buffer) precision loss (Z-fighting).
- **Completion status**: Planned (Not Started)

---

### CGL-CO5: Clipping Algorithms
- **Required concepts**:
  - 2D Point clipping: Point $P(x, y)$ inside if $x_{min} \le x \le x_{max}$ and $y_{min} \le y \le y_{max}$.
  - Line Clipping:
    - Cohen-Sutherland Algorithm: 4-bit region outcodes ($\text{Top}, \text{Bottom}, \text{Right}, \text{Left} \implies \text{TBRL}$), trivial accept ($\text{code}_1 \mid \text{code}_2 == 0$), trivial reject ($\text{code}_1 \ \& \ \text{code}_2 \neq 0$), intersection calculation with boundary edges.
    - Liang-Barsky Algorithm: Parametric line equation $P(u) = P_1 + u(P_2 - P_1)$ with $0 \le u \le 1$, solving inequalities $u \cdot p_k \le q_k$.
  - Polygon Clipping:
    - Sutherland-Hodgman Polygon Clipping Algorithm: Successive clipping against 4 infinite boundary lines (Left, Right, Bottom, Top), vertex transition classification (Inside-to-Inside, Inside-to-Outside, Outside-to-Outside, Outside-to-Inside).
- **Planned implementation**:
  - Standalone clipping engine clipping projectile trajectory lines (Cohen-Sutherland) and spacecraft polygon silhouettes (Sutherland-Hodgman) against the boundaries of the gameplay viewport.
- **Source file location**:
  - `CGL/src/clipping/clipping_algorithms.hpp`
  - `CGL/src/clipping/clipping_algorithms.cpp`
- **Demonstration method**:
  - Visual demonstration canvas rendering a rectangular clipping window; interactive lines and polygons crossing boundaries; color-coded clipped segments, discarded line fragments, and updated vertex lists.
- **Test cases**:
  - `TC_CGL_05A`: Cohen-Sutherland trivial accept of line completely inside window.
  - `TC_CGL_05B`: Cohen-Sutherland trivial reject of line completely outside in same half-plane.
  - `TC_CGL_05C`: Cohen-Sutherland partial intersection calculating correct boundary edge clip points.
  - `TC_CGL_05D`: Sutherland-Hodgman polygon clipping against all 4 boundaries producing correct vertex sequence.
- **Viva explanation**:
  - Meaning of the 4-bit Cohen-Sutherland outcodes and how bitwise operators enable fast trivial acceptance/rejection.
  - Why Liang-Barsky is computationally more efficient than Cohen-Sutherland (replaces floating-point line intersection calculations with parameter comparisons).
  - Limitation of the standard Sutherland-Hodgman algorithm when clipping concave polygons (produces connecting extraneous bridge lines).
- **Completion status**: Planned (Not Started)

---

### CGL-CO6: Texture, Lighting and Color
- **Required concepts**:
  - Color models: RGB, RGBA (alpha channel for transparency), HSV / HSL color spaces.
  - Shading models: Flat Shading (`GL_FLAT` - single normal per polygon) vs Gouraud Shading (`GL_SMOOTH` - normal per vertex, bilinear color interpolation across fragments).
  - Phong Reflection Model components:
    - Ambient Reflection: $I_a = k_a \cdot L_a$.
    - Diffuse Reflection (Lambert's Cosine Law): $I_d = k_d \cdot L_d \cdot (\vec{N} \cdot \vec{L})$.
    - Specular Reflection: $I_s = k_s \cdot L_s \cdot (\vec{R} \cdot \vec{V})^n$ or Blinn-Phong half-angle vector $\vec{H}$.
  - Light sources: Directional light ($w = 0$) vs Positional point light ($w = 1$), attenuation.
  - 2D Texture mapping: UV texture coordinates $[0.0, 1.0]^2$ (`glTexCoord2f`), texture filtering (`GL_NEAREST` vs `GL_LINEAR`), texture wrapping (`GL_REPEAT`, `GL_CLAMP`).
- **Planned implementation**:
  - Illuminated 3D celestial body (planet/moon) and spacecraft hull rendering with directional sun illumination, specular highlights, ambient space glow, and procedural metallic texture mapping.
- **Source file location**:
  - `CGL/src/appearance/lighting_texture_demo.hpp`
  - `CGL/src/appearance/lighting_texture_demo.cpp`
- **Demonstration method**:
  - Interactive OpenGL window featuring an illuminated 3D spacecraft and asteroid; real-time toggles for individual lighting components (Ambient, Diffuse, Specular), material shininess adjustments, and texture mapping on/off.
- **Test cases**:
  - `TC_CGL_06A`: Toggle flat shading vs smooth Gouraud shading on curved spacecraft hull.
  - `TC_CGL_06B`: Verify diffuse lighting intensity variation as surface normal angles rotate relative to light source.
  - `TC_CGL_06C`: Specular highlight variation across material shininess exponents (e.g., $n = 5$ broad vs $n = 100$ sharp).
  - `TC_CGL_06D`: Texture coordinate wrapping and linear vs nearest filtering visual inspection.
- **Viva explanation**:
  - Detailed formula of the Phong Illumination Model: $I = I_a k_a + I_d k_d (\vec{L} \cdot \vec{N}) + I_s k_s (\vec{R} \cdot \vec{V})^n$.
  - Difference between Gouraud shading (calculates lighting at vertices, interpolates color across polygon) and Phong shading (interpolates normals across polygon, calculates lighting per pixel).
  - Texture sampling concepts: texels, UV parameterization, and why bilinear filtering prevents pixelation artifacts.
- **Completion status**: Planned (Not Started)

---

# 4. Java Object-Oriented Programming (PSOOP) – Java SE / Swing / AWT

### PSOOP-CO1: Object-Oriented Solutions
- **Required concepts**:
  - Core Pillars of OOP:
    - Abstraction (hiding implementation details, exposing clean interfaces).
    - Encapsulation (private fields, getters, setters, invariant validation).
    - Inheritance (base classes, `extends`, code reuse, `super` constructor chaining).
    - Polymorphism (method overriding `@Override`, runtime dynamic method dispatch, method overloading).
  - Abstract classes vs Interfaces (`default` and `static` methods).
  - Access specifiers (`private`, `protected`, `public`, package-private).
  - Composition over inheritance ("is-a" vs "has-a" relationships).
- **Planned implementation**:
  - Core game entity object hierarchy:
    - Abstract base class `Entity` with position, velocity, bounding box, and abstract methods `update()` and `render()`.
    - Abstract class `Ship extends Entity` adding health, shields, and weapon hardpoints.
    - Concrete classes `Player`, `Enemy` (`DroneEnemy`, `FighterEnemy`, `BossEnemy`), `Projectile`, `PowerUp`, `Particle`.
    - Capability interfaces: `Renderable`, `Collidable`.
- **Source file location**:
  - `PSOOP/src/model/Entity.java`
  - `PSOOP/src/model/Ship.java`
  - `PSOOP/src/model/Player.java`
  - `PSOOP/src/model/Enemy.java`
  - `PSOOP/src/model/DroneEnemy.java`
  - `PSOOP/src/model/FighterEnemy.java`
  - `PSOOP/src/model/BossEnemy.java`
  - `PSOOP/src/model/Projectile.java`
  - `PSOOP/src/model/Renderable.java`
  - `PSOOP/src/model/Collidable.java`
- **Demonstration method**:
  - Standalone Java test suite instantiating a heterogeneous collection of `List<Entity>`, executing polymorphic updates and renders, validating encapsulation constraints (e.g. preventing negative health), and demonstrating interface polymorphic dispatch.
- **Test cases**:
  - `TC_PSOOP_01A`: Polymorphic invocation of `update()` across mixed list containing Player, Enemy, and Projectile.
  - `TC_PSOOP_01B`: Encapsulation validation checking that negative damage or invalid coordinate assignment clamps gracefully.
  - `TC_PSOOP_01C`: Interface capability testing: verify `Collidable` contracts on colliding instances.
  - `TC_PSOOP_01D`: Subclass constructor chaining correctly initializing superclass physics state.
- **Viva explanation**:
  - Differences between abstract classes and interfaces in Java (state inheritance vs contract implementation).
  - How Java Virtual Machine (JVM) implements runtime polymorphism via virtual method tables (`vtable`).
  - The Liskov Substitution Principle (LSP) and why derived classes must be substitutable for base class instances without breaking program correctness.
- **Completion status**: Completed (Phase 2)

---

### PSOOP-CO2: Appropriate Programming Solutions to Reduce Complexity
- **Required concepts**:
  - Software Design Patterns:
    - State Machine (`GameState`: `MENU`, `PLAYING`, `PAUSED`, `GAME_OVER`).
    - Decoupled subsystem design (WaveManager, CollisionManager, ScoreCalculator).
    - Model-View-Controller (MVC) architectural separation:
      - Model: Game entities (`model.*`)
      - View: Swing components (`GameCanvas`, `MenuPanel`, `MainWindow`)
      - Controller: Keyboard event dispatching on EDT
    - Clean code principles: Single Responsibility Principle (SRP), low coupling, high cohesion.
- **Planned implementation**:
  - Complete decoupling of simulation logic from UI presentation via MVC.
  - State machine in `GameEngine` controlling menu navigation, active gameplay, pause, and game over.
  - `WaveManager` scaling difficulty, spawn intervals, and Boss encounters.
  - `ScoreCalculator` managing combo streaks and multipliers.
- **Source file location**:
  - `PSOOP/src/game/GameEngine.java`
  - `PSOOP/src/game/GameState.java`
  - `PSOOP/src/service/WaveManager.java`
  - `PSOOP/src/service/ScoreCalculator.java`
  - `PSOOP/src/ui/MenuPanel.java`
- **Demonstration method**:
  - Verification program demonstrating factory generation of enemy varieties without modifying client code, score streak multipliers, and state machine transitions between game states.
- **Test cases**:
  - `TC_PSOOP_02A`: State transition validation ensuring illegal transitions (e.g. `MENU` directly to `PAUSED`) throw `InvalidGameStateException`.
  - `TC_PSOOP_02B`: Wave progression spawning varied enemy subclasses dynamically.
  - `TC_PSOOP_02C`: ScoreCalculator kill-streak multiplier scaling from 1x to 4x.
  - `TC_PSOOP_02D`: Pause and resume state transitions preserve entity vectors.
- **Viva explanation**:
  - How design patterns reduce cyclomatic complexity and prevent code smells (e.g. replacing sprawling `switch-case` statements with State patterns and subsystems).
  - Thread safety in Swing timer execution on the Event Dispatch Thread (EDT).
  - Benefits of MVC separation in eliminating direct dependencies between simulation models and UI presentation.
- **Completion status**: Completed (Phase 2)

---

### PSOOP-CO3: Critical Thinking and Programming-Based Problem Solving
- **Required concepts**:
  - Exception Handling architecture in Java:
    - `Throwable` hierarchy: `Error` vs `Exception` (Checked exceptions) vs `RuntimeException` (Unchecked exceptions).
    - `try-catch-finally` blocks, multiple catch clauses, rethrowing exceptions.
    - Custom domain-specific checked exception classes (`AirstrikeException`, `InvalidGameStateException`, `ResourceLoadException`).
    - Defensive programming and boundary validations.
    - Graceful degradation: procedural asset generation (`AssetManager`) ensuring 100% playable game without external asset files.
- **Planned implementation**:
  - Custom domain checked exception hierarchy.
  - Defensive coordinate and boundary clamping in `Entity` and `Player`.
  - Procedural asset generation in `AssetManager` with zero external image dependencies.
- **Source file location**:
  - `PSOOP/src/exception/AirstrikeException.java`
  - `PSOOP/src/exception/InvalidGameStateException.java`
  - `PSOOP/src/exception/ResourceLoadException.java`
  - `PSOOP/src/service/AssetManager.java`
  - `PSOOP/src/game/GameEngine.java`
- **Demonstration method**:
  - Test suite executing deliberate failure scenarios: invalid state transitions throwing domain exceptions, boundary overflow clamping, and validating procedural fallback rendering without crashes.
- **Test cases**:
  - `TC_PSOOP_03A`: Illegal state transition throws checked `InvalidGameStateException`.
  - `TC_PSOOP_03B`: Boundary clamping prevents negative or out-of-screen coordinates.
  - `TC_PSOOP_03C`: Procedural starfield generates stars cleanly without asset file dependencies.
  - `TC_PSOOP_03D`: Damage overflow clamp prevents health from becoming negative.
- **Viva explanation**:
  - Distinction between Checked exceptions (compile-time enforcement for recoverable conditions) and Unchecked exceptions (programming bugs, logic errors).
  - Why suppressing exceptions with empty `catch` blocks is dangerous in production systems.
  - How defensive programming prevents runtime crashes in real-time desktop systems.
- **Completion status**: Completed (Phase 2)

---

### PSOOP-CO4: Logic-Building for Real-Life Problems
- **Required concepts**:
  - Real-time simulation game loop architectures:
    - 60 FPS / UPS loop using `javax.swing.Timer` executing on the Swing EDT.
    - Delta time ($\Delta t$) kinematics integration for smooth frame-rate independent physics.
  - 2D Collision Detection algorithms:
    - Axis-Aligned Bounding Box (AABB) intersection: `Rectangle.intersects()`.
    - Multi-entity collision resolution (bullet vs enemy, enemy bullet vs player, ship ramming, power-up collection).
  - Graphical User Interface (GUI) architecture with Swing:
    - Event Dispatch Thread (EDT) safety (`SwingUtilities.invokeLater()`).
    - Custom rendering in Swing: overriding `paintComponent(Graphics g)`, double buffering, `Graphics2D` rendering hints (antialiasing, quality rendering).
    - Event-driven programming (`KeyListener`).
- **Planned implementation**:
  - 60 FPS `GameLoop` with delta time clamping.
  - Mathematical `CollisionManager` handling 4 distinct interaction channels.
  - Double-buffered `GameCanvas` and `MainWindow` card layout.
- **Source file location**:
  - `PSOOP/src/game/GameLoop.java`
  - `PSOOP/src/game/CollisionManager.java`
  - `PSOOP/src/game/GameCanvas.java`
  - `PSOOP/src/ui/MainWindow.java`
  - `PSOOP/src/test/GameUnitTest.java`
  - `PSOOP/src/test/DesktopWindowVerification.java`
- **Demonstration method**:
  - Benchmark module measuring game loop delta-time stability under fluctuating CPU workloads, accompanied by mathematical collision detection verification reporting intersection boundaries and response vectors.
- **Test cases**:
  - `TC_PSOOP_04A`: AABB collision true positive when bounding boxes overlap and true negative when disjoint.
  - `TC_PSOOP_04B`: Shield capacitor absorbs damage prior to hull integrity reduction.
  - `TC_PSOOP_04C`: Game loop delta-time accumulator stability test.
  - `TC_PSOOP_04D`: Desktop window launch verification confirming `isShowing() == true` on EDT.
- **Viva explanation**:
  - The "Spiral of Death" problem in naive game loops and how delta time clamping prevents simulation freezes.
  - Why Axis-Aligned Bounding Boxes (AABB) offer fast $O(1)$ overlap rejection before detailed checks.
  - Difference between game simulation ticks and Swing Event Dispatch Thread (EDT) rendering.
- **Completion status**: Completed (Phase 2)

---

## 5. Summary Matrix & Academic Verification

All 19 Course Outcomes across the 4 core subjects are mapped with complete academic and engineering specifications:
- **PSOOP**: 4 Outcomes (CO1 to CO4) -> **Completed (Phase 2)**
- **PL**: 5 Outcomes (CO1 to CO5) -> **Planned (Not Started)**
- **COA**: 4 Outcomes (CO1 to CO4) -> **Planned (Not Started)**
- **CGL**: 6 Outcomes (CO1 to CO6) -> **Planned (Not Started)**
- **Total Outcomes**: **19**

Phase 2 completed and verified the 4 PSOOP outcomes with a full playable Swing/AWT space shooter, automated testing, and desktop verification. Subsequent phases will address the remaining subject modules.

