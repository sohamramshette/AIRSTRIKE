# AIRSTRIKE: Comprehensive Course Outcome (CO) Verification Matrix

**Project**: AIRSTRIKE – 2D Space Shooter & Academic Demonstration System  
**Academic Module Count**: 4 Autonomous Subjects (PSOOP, PL, COA, CGL)  
**Total Course Outcomes**: Exactly 19 Course Outcomes  
**Verification Level**: Code-Level Audit, Automated Mathematical Unit Testing & Runtime Visual Proof  

---

## 1. Executive Summary Table

| Module | Outcome ID | Official Topic | Primary Source Files | Verification Status | Tests Passed |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **PSOOP** | **PSOOP-CO1** | Object-Oriented Solutions & Inheritance | `PSOOP/src/model/*.java` | **VERIFIED** | 10 / 10 |
| **PSOOP** | **PSOOP-CO2** | Solutions to Reduce Complexity | `PSOOP/src/service/*.java`, `game/*.java` | **VERIFIED** | 12 / 12 |
| **PSOOP** | **PSOOP-CO3** | Critical Thinking & Problem Solving | `PSOOP/src/exception/*.java`, `database/*.java`| **VERIFIED** | 53 / 53 |
| **PSOOP** | **PSOOP-CO4** | Logic-Building for Real-Life Problems | `PSOOP/src/game/GameEngine.java`, `ui/*.java` | **VERIFIED** | 68 / 68 |
| **PL** | **PL-CO1** | Arrays | `PL/src/arrays/*.cpp` | **VERIFIED** | 10 / 10 |
| **PL** | **PL-CO2** | Suitable Linked Lists | `PL/src/linked_lists/*.cpp` | **VERIFIED** | 10 / 10 |
| **PL** | **PL-CO3** | Stack and Queue Applications | `PL/src/stacks/*.cpp`, `queues/*.cpp` | **VERIFIED** | 12 / 12 |
| **PL** | **PL-CO4** | Searching and Sorting Techniques | `PL/src/searching/*.cpp`, `sorting/*.cpp` | **VERIFIED** | 13 / 13 |
| **PL** | **PL-CO5** | Hashing Techniques | `PL/src/hashing/*.cpp` | **VERIFIED** | 8 / 8 |
| **COA** | **COA-CO1** | 80386 Architecture & Instruction Set | `COA/src/co1_architecture/arch_demo.s` | **VERIFIED** | 6 / 6 |
| **COA** | **COA-CO2** | Assembly Language Programs (80386) | `COA/src/co2_assembly_programs/*.s` | **VERIFIED** | 15 / 15 |
| **COA** | **COA-CO3** | Computer Arithmetic & Booth Algorithm | `COA/src/co3_arithmetic/*.s` | **VERIFIED** | 18 / 18 |
| **COA** | **COA-CO4** | Control Unit & Instruction Pipelining | `COA/src/co4_control_unit/*.s` | **VERIFIED (EDU MODEL)** | 2 / 2 |
| **CGL** | **CGL-CO1** | OpenGL Graphics Primitives | `CGL/src/primitives/space_primitives.cpp` | **VERIFIED** | 4 / 4 |
| **CGL** | **CGL-CO2** | Geometric Transformations | `CGL/src/transformations/transform_demo.cpp` | **VERIFIED** | 12 / 12 |
| **CGL** | **CGL-CO3** | 2D and 3D Algorithms | `CGL/src/algorithms/raster_algorithms.cpp` | **VERIFIED** | 21 / 21 |
| **CGL** | **CGL-CO4** | Projection and Viewing | `CGL/src/projection/viewing_demo.cpp` | **VERIFIED** | 7 / 7 |
| **CGL** | **CGL-CO5** | Clipping Algorithms | `CGL/src/clipping/clipping_algorithms.cpp` | **VERIFIED** | 10 / 10 |
| **CGL** | **CGL-CO6** | Texture, Lighting and Color | `CGL/src/appearance/lighting_texture_demo.cpp` | **VERIFIED** | 11 / 11 |
| **TOTAL** | **19 COs** | **All 4 Academic Modules** | **Repository-Wide Core Codebase** | **100% VERIFIED** | **302 / 302** |

---

## 2. Detailed Course Outcome Verifications

---

### Subject 1: Principles of Software Object-Oriented Programming (PSOOP)

#### PSOOP-CO1: Object-Oriented Solutions & Inheritance
- **Official Topic**: Analyze and apply OOP principles (Abstraction, Encapsulation, Inheritance, Polymorphism) to develop software systems.
- **Relevant Source Files**:
  - `PSOOP/src/model/Entity.java`
  - `PSOOP/src/model/Player.java`
  - `PSOOP/src/model/Enemy.java`
  - `PSOOP/src/model/DroneEnemy.java`
  - `PSOOP/src/model/FighterEnemy.java`
  - `PSOOP/src/model/BossEnemy.java`
  - `PSOOP/src/model/Projectile.java`
  - `PSOOP/src/model/PowerUp.java`
- **Actual Implementation**:
  - Abstract base class `Entity` provides common physical properties ($x, y$, bounding boxes, health, active flag).
  - Concrete inheritance hierarchy: `Entity` $\rightarrow$ `Player`, and `Entity` $\rightarrow$ `Enemy` $\rightarrow$ `DroneEnemy`, `FighterEnemy`, `BossEnemy`.
  - Dynamic runtime polymorphism: `update(double dt)` and `render(Graphics2D g)` overridden in subclasses for distinct behaviors (e.g., Drone swooping vs Fighter sinusoidal weaving vs Boss phased combat).
- **Execution**:
  - Run game client: `java -cp bin/java main.Main`
  - Or automated test suite: `java -cp bin/java test.GameUnitTest`
- **Expected Output**:
  - Active space combat with distinct rendering and flight patterns for player ship and 3 enemy archetypes.
- **Screenshot Evidence**: `docs/screenshots/PSOOP/active_gameplay.png`, `boss_battle.png`
- **Test Coverage**: Tested in `test.GameUnitTest` (Player movement, bounds, enemy inheritance properties, bullet absorption, damage). 10/10 assertions pass.

---

#### PSOOP-CO2: Solutions to Reduce Complexity (Services & Design Patterns)
- **Official Topic**: Formulate object-oriented solutions to reduce system complexity through modular service decoupling and standard design patterns.
- **Relevant Source Files**:
  - `PSOOP/src/service/CollisionManager.java`
  - `PSOOP/src/service/ScoreCalculator.java`
  - `PSOOP/src/service/WaveManager.java`
  - `PSOOP/src/service/AssetManager.java`
  - `PSOOP/src/database/SessionManager.java`
  - `PSOOP/src/service/AuthService.java`, `MatchService.java`
- **Actual Implementation**:
  - **Singleton Pattern**: `SessionManager.getInstance()` provides a thread-safe single source of truth for pilot credentials.
  - **Observer Pattern**: `SessionManager` notifies `MenuPanel` of login/logout changes via `SessionListener`.
  - **Separation of Concerns**: `CollisionManager` performs decoupled Axis-Aligned Bounding Box (AABB) checks; `ScoreCalculator` manages streak bonuses without UI entanglement; `WaveManager` controls spawn timers and milestone boss battles.
- **Execution**:
  - In `MainWindow`, trigger wave transitions or kill streaks.
- **Expected Output**:
  - Level advances automatically upon sector clear; streak multipliers (1x up to 4x) appear in HUD.
- **Screenshot Evidence**: `docs/screenshots/PSOOP/active_gameplay.png`
- **Test Coverage**: Tested in `GameUnitTest` (Streak multipliers, base scores, wave transitions, collision broadphase). 12/12 assertions pass.

---

#### PSOOP-CO3: Critical Thinking & Problem Solving (Exceptions & Network Resilience)
- **Official Topic**: Demonstrate critical thinking and problem-solving through robust error handling, domain-specific exceptions, and defensive networking.
- **Relevant Source Files**:
  - `PSOOP/src/exception/GameException.java`
  - `PSOOP/src/exception/InvalidGameStateException.java`
  - `PSOOP/src/database/SupabaseConfig.java`
  - `PSOOP/src/database/SimpleJson.java`
  - `PSOOP/src/database/SupabaseHttpClient.java`
- **Actual Implementation**:
  - Custom domain exception `InvalidGameStateException` rejects illegal state transitions (e.g., attempting to pause while in Main Menu).
  - Security validation in `SupabaseConfig.java` throws `SecurityException` if an administrative `service_role` key is provided.
  - Pure-Java `SimpleJson.java` recursive-descent parser safely handles malformed JSON without crashing the game engine.
  - Asynchronous HTTP failure recovery in `AuthService` and `MatchService`: network disconnects fallback gracefully to local Guest Pilot mode.
- **Execution**:
  - Run `java -cp bin/java test.SupabaseAuthTest`
- **Expected Output**:
  - Exception assertions confirmed: invalid states rejected, service keys blocked, expired sessions auto-cleared.
- **Screenshot Evidence**: `docs/screenshots/PSOOP/auth_dialog.png`
- **Test Coverage**: 53 assertions in `SupabaseAuthTest` verifying security exceptions, input validation, expired tokens, and network error resilience.

---

#### PSOOP-CO4: Logic-Building for Real-Life Problems (Game Loop, Swing EDT, Cloud Sync)
- **Official Topic**: Develop logical reasoning and algorithms to solve real-life interactive problems, multi-threading, and persistent cloud synchronization.
- **Relevant Source Files**:
  - `PSOOP/src/game/GameEngine.java`
  - `PSOOP/src/game/GameLoop.java`
  - `PSOOP/src/game/GameCanvas.java`
  - `PSOOP/src/ui/MainWindow.java`
  - `PSOOP/src/ui/MenuPanel.java`
  - `PSOOP/src/ui/MatchHistoryDialog.java`
  - `PSOOP/src/ui/LeaderboardDialog.java`
- **Actual Implementation**:
  - High-precision fixed-timestep game loop in `GameLoop.java` maintains steady 60 FPS updates using `System.nanoTime()` and dispatches paints to the Swing EDT.
  - Active keyboard input polling via bitmask states (`shootingTriggered`, key arrays).
  - Asynchronous REST synchronization on game over (`POST /rest/v1/matches`) guarded by `matchUploaded` boolean lock.
- **Execution**:
  - Run `airstrike.bat` $\rightarrow$ Option 1 (PSOOP). Play match until Game Over. Open Match History and Global Leaderboard.
- **Expected Output**:
  - Game Over screen updates with `"FLIGHT LOG SECURED IN STARFLEET ARCHIVES"`; Leaderboard displays top 10 pilots.
- **Screenshot Evidence**:
  - `docs/screenshots/PSOOP/main_menu.png`
  - `docs/screenshots/PSOOP/game_over_screen.png`
  - `docs/screenshots/PSOOP/match_history_dialog.png`
  - `docs/screenshots/PSOOP/leaderboard_dialog.png`
- **Test Coverage**: 50 assertions in `SupabaseMatchServiceTest` and 18 assertions in `GameUnitTest`.

---

### Subject 2: Programming Laboratory (PL – Standard C++17)

#### PL-CO1: Arrays (Contiguous Memory, Dynamic Resizing & Pooling)
- **Official Topic**: Design and analyze algorithms utilizing 1D and 2D arrays, contiguous memory allocation, dynamic resizing, and object pooling.
- **Relevant Source Files**:
  - `PL/src/arrays/array_ops.h`, `array_ops.cpp`
  - `PL/src/arrays/dynamic_array.h`, `dynamic_array.cpp`
  - `PL/src/arrays/bullet_pool.h`, `bullet_pool.cpp`
  - `PL/src/arrays/collision_grid.h`, `collision_grid.cpp`
- **Actual Implementation**:
  - Custom `DynamicArray` template implementing manual `new[]`/`delete[]`, geometric capacity doubling, and Rule of 5 copy/move semantics without `std::vector`.
  - Contiguous 2D spatial partitioning `CollisionGrid` using row-major index formula: $\text{Index}(r, c) = r \times \text{cols} + c$.
  - Fixed-capacity projectile memory pool `BulletPool` reusing preallocated bullet slots to eliminate runtime heap allocation churn.
- **Execution**:
  - `bin/pl/pl_test_runner.exe` $\rightarrow$ Option 1
  - Or headless: `bin/pl/pl_test_runner.exe --test`
- **Expected Output**:
  - Demonstration of array insertion, capacity growth from 4 to 8, bullet pooling lifecycle, and spatial collision queries.
- **Screenshot Evidence**: `docs/screenshots/FINAL/pl_arrays_demo.png`
- **Test Coverage**: 10 unit tests in `PL/tests/test_arrays.cpp` passing 100%.

---

#### PL-CO2: Suitable Linked Lists (Singly, Doubly, Circular)
- **Official Topic**: Formulate dynamic data models using appropriate singly, doubly, and circular linked lists with manual pointer management.
- **Relevant Source Files**:
  - `PL/src/linked_lists/singly_linked_list.h`, `singly_linked_list.cpp`
  - `PL/src/linked_lists/doubly_linked_list.h`, `doubly_linked_list.cpp`
  - `PL/src/linked_lists/circular_linked_list.h`, `circular_linked_list.cpp`
- **Actual Implementation**:
  - `SinglyLinkedList`: Dynamic node allocation (`struct Node { T data; Node* next; }`), head/tail insertion, positional deletion.
  - `DoublyLinkedList`: Bidirectional linkages (`next`, `prev`) allowing $O(1)$ node removal given an iterator reference, modeling active fighter squadron formations.
  - `CircularLinkedList`: Endless cyclic ring traversal modeling spacecraft weapon selection cycling.
- **Execution**:
  - `bin/pl/pl_test_runner.exe` $\rightarrow$ Option 2
- **Expected Output**:
  - Forward and backward traversal, arbitrary node deletion without shifting elements, cyclic ring stepping.
- **Screenshot Evidence**: `docs/screenshots/FINAL/pl_linked_lists_demo.png`
- **Test Coverage**: 10 unit tests in `PL/tests/test_linked_lists.cpp` passing 100%.

---

#### PL-CO3: Stack and Queue Applications (LIFO & FIFO)
- **Official Topic**: Implement and evaluate stack (LIFO) and queue (FIFO) data structures for application history and task scheduling.
- **Relevant Source Files**:
  - `PL/src/stacks/action_stack.h`, `action_stack.cpp`
  - `PL/src/queues/wave_queue.h`, `wave_queue.cpp`
- **Actual Implementation**:
  - `ActionStack`: Array-based LIFO stack with `push`, `pop`, `peek`, underflow detection, and overflow limits, implementing player combat command undo.
  - `WaveQueue`: Circular array FIFO queue with modulo arithmetic index wrap: $\text{rear} = (\text{rear} + 1) \pmod{\text{capacity}}$, scheduling incoming enemy waves.
- **Execution**:
  - `bin/pl/pl_test_runner.exe` $\rightarrow$ Option 3 (Stack) and Option 4 (Queue)
- **Expected Output**:
  - Step-by-step undo of recorded combat actions; FIFO dispatching of queued enemy squadrons.
- **Screenshot Evidence**: `docs/screenshots/FINAL/pl_stacks_queues_demo.png`
- **Test Coverage**: 12 unit tests across `PL/tests/test_stack.cpp` and `test_queue.cpp` passing 100%.

---

#### PL-CO4: Searching and Sorting Techniques
- **Official Topic**: Implement, compare, and benchmark searching (Linear, Binary) and sorting (Bubble, Selection, Insertion, Merge, QuickSort) algorithms.
- **Relevant Source Files**:
  - `PL/src/searching/search_algorithms.h`, `search_algorithms.cpp`
  - `PL/src/sorting/sort_algorithms.h`, `sort_algorithms.cpp`
- **Actual Implementation**:
  - Linear Search ($O(N)$) vs Binary Search ($O(\log N)$) with exact comparison step counters.
  - Canonical implementations of 5 sorting algorithms: Bubble Sort (with early exit flag for $O(N)$ best case), Selection Sort ($O(N)$ swaps), Insertion Sort, Merge Sort (stable $O(N \log N)$), and QuickSort (in-place Lomuto partitioning).
  - Leaderboard high score ranking algorithm in descending order.
- **Execution**:
  - `bin/pl/pl_test_runner.exe` $\rightarrow$ Option 5 (Searching) and Option 6 (Sorting)
- **Expected Output**:
  - Comparative step analysis showing Binary Search finding targets in 4 steps vs 100 steps in Linear Search; sorted leaderboard arrays.
- **Screenshot Evidence**: `docs/screenshots/FINAL/pl_sort_search_demo.png`
- **Test Coverage**: 13 unit tests across `PL/tests/test_searching.cpp` and `test_sorting.cpp` passing 100%.

---

#### PL-CO5: Hashing Techniques (Chaining & Probing)
- **Official Topic**: Implement hash table collision resolution techniques (separate chaining and open addressing with linear probing) with dynamic rehashing.
- **Relevant Source Files**:
  - `PL/src/hashing/hash_table_chaining.h`, `hash_table_chaining.cpp`
  - `PL/src/hashing/hash_table_probing.h`, `hash_table_probing.cpp`
- **Actual Implementation**:
  - Polynomial rolling hash function: $H(s) = \sum s[i] \cdot p^i \pmod M$.
  - `HashTableChaining`: Singly linked bucket chains resolving collisions; automatic rehashing when load factor $\alpha \ge 0.75$.
  - `HashTableProbing`: Open addressing with linear probing; tombstone markers (`DELETED`) preserving search continuity after element deletion.
- **Execution**:
  - `bin/pl/pl_test_runner.exe` $\rightarrow$ Option 7
- **Expected Output**:
  - Insertion of inventory keys, collision resolution display, tombstone step tracking, table capacity expansion.
- **Screenshot Evidence**: `docs/screenshots/FINAL/pl_hashing_demo.png`
- **Test Coverage**: 8 unit tests in `PL/tests/test_hashing.cpp` passing 100%.

---

### Subject 3: Computer Organization and Architecture (COA – 80386 Assembly)

#### COA-CO1: 80386 Architecture & Instruction Set
- **Official Topic**: Analyze the internal architecture, register set, addressing modes, and segmentation model of the Intel 80386 microprocessor in 32-bit protected mode.
- **Relevant Source Files**:
  - `COA/src/co1_architecture/arch_demo.s`
- **Actual Implementation**:
  - Demonstrates 32-bit General Purpose Registers (`EAX`, `EBX`, `ECX`, `EDX`) and their 16-bit (`AX`, `BX`) and 8-bit (`AH`, `AL`) subpartitions.
  - Demonstrates pointer/index registers (`ESI`, `EDI`, `EBP`, `ESP`).
  - Evaluates EFLAGS status flags: Carry (`CF`), Zero (`ZF`), Sign (`SF`), Overflow (`OF`) using native branch operations (`JC`, `JZ`, `JS`, `JO`).
  - Scaled-Index-Base (SIB) complex addressing mode: `[EBX + ESI*4 + 8]`.
  - Explains 32-bit flat memory model vs segmentation registers (`CS`, `DS`, `SS`, `ES`).
- **Execution**:
  - `bin/coa/coa_runner.exe` $\rightarrow$ Option 1
  - Or headless: `bin/coa/coa_runner.exe --test`
- **Expected Output**:
  - Formatted register dumps, flag status transitions, and SIB addressing calculations.
- **Screenshot Evidence**: `docs/screenshots/FINAL/coa_co1_arch_demo.png`
- **Test Coverage**: 6 assertions in `COA/tests/test_coa.s` passing 100%.

---

#### COA-CO2: Assembly Language Programs (80386)
- **Official Topic**: Develop and verify 32-bit 80386 assembly language programs demonstrating fundamental computing operations.
- **Relevant Source Files**:
  - `COA/src/co2_assembly_programs/arithmetic_ops.s`
  - `COA/src/co2_assembly_programs/array_ops.s`
  - `COA/src/co2_assembly_programs/math_series.s`
  - `COA/src/co2_assembly_programs/logic_ops.s`
- **Actual Implementation**:
  - 10 distinct 80386 assembly programs:
    1. 32-bit Signed Addition (`ADD`)
    2. 32-bit Signed Subtraction (`SUB`)
    3. Signed Multiplication (`IMUL`)
    4. Signed Division with Remainder (`IDIV`, with divide-by-zero protection)
    5. Array Min and Max Value Search
    6. Array Summation
    7. Recursive Factorial (managing `EBP` stack activation records)
    8. Iterative Fibonacci Series Generator
    9. Linear Search and In-Place Array Reversal
    10. Bitwise Logic Masks (`AND`, `OR`, `XOR`, `NOT`, `SHL`, `SHR`)
- **Execution**:
  - `bin/coa/coa_runner.exe` $\rightarrow$ Option 2
- **Expected Output**:
  - Interactive selection of all 10 programs with mathematical input/output verification.
- **Screenshot Evidence**: `docs/screenshots/FINAL/coa_co2_programs_demo.png`
- **Test Coverage**: 15 assertions in `COA/tests/test_coa.s` passing 100%.

---

#### COA-CO3: Computer Arithmetic & Booth Algorithm
- **Official Topic**: Formulate and verify binary multiplication (Booth's Algorithm) and division (Restoring Division) algorithms in 80386 assembly.
- **Relevant Source Files**:
  - `COA/src/co3_arithmetic/booth_multiplier.s`
  - `COA/src/co3_arithmetic/restoring_divider.s`
  - `COA/src/co3_arithmetic/carry_overflow.s`
- **Actual Implementation**:
  - **Booth's Multiplication**: Full signed two's complement multiplication evaluating pairs $Q_0, Q_{-1}$ ($01 \rightarrow +M, 10 \rightarrow -M$), arithmetic right shifts (`SAR`), and step-by-step register tracing across positive, negative, and zero operands.
  - **Restoring Division**: Unsigned integer division with left shift of $(A, Q)$, trial subtraction $A = A - M$, sign check, and restoring addition $A = A + M$ when negative.
  - **Multi-Precision Arithmetic**: 64-bit addition using `ADD` and `ADC` with signed overflow detection logic.
- **Execution**:
  - `bin/coa/coa_runner.exe` $\rightarrow$ Option 3
- **Expected Output**:
  - Step-by-step trace tables for Booth multiplication (e.g., $7 \times -5 = -35$) and restoring division (e.g., $29 / 5 = 5 \text{ rem } 4$).
- **Screenshot Evidence**: `docs/screenshots/FINAL/coa_co3_arithmetic_demo.png`
- **Test Coverage**: 18 assertions in `COA/tests/test_coa.s` passing 100%.

---

#### COA-CO4: Control Unit & Instruction Pipelining
- **Official Topic**: Analyze instruction pipelining, pipeline hazards (RAW, branch penalty), and control unit micro-operations.
- **Relevant Source Files**:
  - `COA/src/co4_control_unit/control_unit.s`
  - `COA/src/co4_control_unit/pipeline_sim.s`
- **Implementation Distinction**:
  - Implemented as an educational five-stage architectural simulation (IF, ID, EX, MEM, WB) written in 80386 assembly to demonstrate instruction hazard detection. This model clearly demonstrates control signal matrices and hazard stalls, and is explicitly documented as an educational architectural model rather than a claim of physical 80386 internal microcode exposure.
- **Actual Implementation**:
  - Instruction opcode decoding generating control signals (`RegDst`, `ALUSrc`, `MemToReg`, `RegWrite`, `MemRead`, `MemWrite`, `Branch`, `ALUOp`).
  - Read-After-Write (RAW) data hazard detection inserting pipeline stalls.
  - Branch misprediction flush penalty simulation discarding speculative stages.
- **Execution**:
  - `bin/coa/coa_runner.exe` $\rightarrow$ Option 4
- **Expected Output**:
  - Control signal matrix printout and cycle-by-cycle pipeline reservation table.
- **Screenshot Evidence**: `docs/screenshots/FINAL/coa_co4_pipeline_demo.png`
- **Test Coverage**: 2 assertions in `COA/tests/test_coa.s` passing 100%.

---

### Subject 4: Computer Graphics Laboratory (CGL – C++17, OpenGL, GLUT)

#### CGL-CO1: OpenGL Graphics Primitives
- **Official Topic**: Render geometric scenes using foundational OpenGL primitives and state attributes.
- **Relevant Source Files**:
  - `CGL/src/primitives/space_primitives.hpp`, `space_primitives.cpp`
- **Actual Implementation**:
  - Complete demonstration of 8 primitives: `GL_POINTS`, `GL_LINES`, `GL_LINE_STRIP`, `GL_LINE_LOOP`, `GL_TRIANGLES`, `GL_TRIANGLE_STRIP`, `GL_TRIANGLE_FAN`, and `GL_QUADS`.
  - Color interpolation, point smoothing, line width modulation.
- **Execution**:
  - `bin/cgl/cgl_runner.exe` $\rightarrow$ Press `[1]`
- **Expected Output**:
  - Interactive grid displaying all 8 primitives composing spacecraft shields, wings, hulls, and beacons.
- **Screenshot Evidence**: `docs/screenshots/CGL/cgl_co1_primitives.bmp`
- **Test Coverage**: 4 unit tests in `CGL/tests/test_cgl.cpp` passing 100%.

---

#### CGL-CO2: Geometric Transformations
- **Official Topic**: Formulate and apply 2D and 3D affine geometric transformations, arbitrary pivot rotations, and composite transformations.
- **Relevant Source Files**:
  - `CGL/src/transformations/transform_demo.hpp`, `transform_demo.cpp`
- **Actual Implementation**:
  - 2D/3D Translation (`glTranslatef`), Scaling (`glScalef`), and Euler Rotation (`glRotatef`).
  - Reflection across X-axis and Y-axis via matrix scaling ($S(1, -1)$ and $S(-1, 1)$).
  - Arbitrary pivot point rotation via composite sequence: $T(P) \cdot R(\theta) \cdot T(-P)$.
  - Non-commutative matrix order proof: visually proving $T \cdot R \ne R \cdot T$.
  - Hierarchical robotic arm / gimbal transformation stack (`glPushMatrix`, `glPopMatrix`).
- **Execution**:
  - `bin/cgl/cgl_runner.exe` $\rightarrow$ Press `[2]`. Use `[W/A/S/D]` to translate, `[Q/E]` to rotate, `[+/-]` to scale, `[P]` for pivot mode, `[O]` to swap order, `[F]` for reflection.
- **Expected Output**:
  - Transformed spacecraft demonstrating pivot rotation, reflection, and non-commutative transformation paths.
- **Screenshot Evidence**: `docs/screenshots/CGL/cgl_co2_transformations.bmp`
- **Test Coverage**: 12 mathematical coordinate verification tests in `CGL/tests/test_cgl.cpp` passing 100%.

---

#### CGL-CO3: 2D and 3D Algorithms
- **Official Topic**: Implement software rasterization algorithms (line drawing, curve generation, polygon fill) and 3D mesh representations.
- **Relevant Source Files**:
  - `CGL/src/algorithms/raster_algorithms.hpp`, `raster_algorithms.cpp`
- **Actual Implementation**:
  - **Bresenham's Integer Line Algorithm**: Implemented across all 8 octants, horizontal lines, vertical lines, and reverse directions with integer decision variables.
  - **DDA Line Drawing Algorithm**: Floating-point incremental line rasterization.
  - **Midpoint Circle Algorithm**: 8-way symmetry integer stepping satisfying $|x^2 + y^2 - r^2| \le 2r+1$.
  - **Midpoint Ellipse Algorithm**: 2-region decision variables for major/minor axes.
  - **Scanline Polygon Fill**: Edge table and active edge list sorting scanline intersections.
  - **3D Wireframe Mesh**: Vertices, edges, Euler rotation, and depth translation.
- **Execution**:
  - `bin/cgl/cgl_runner.exe` $\rightarrow$ Press `[3]`. Press `[M]` to cycle raster modes.
- **Expected Output**:
  - Pixel-grid rasterization of lines, circles, ellipses, filled polygon shields, and 3D mesh wireframe.
- **Screenshot Evidence**: `docs/screenshots/CGL/cgl_co3_algorithms.bmp`
- **Test Coverage**: 21 mathematical algorithm tests in `CGL/tests/test_cgl.cpp` passing 100%.

---

#### CGL-CO4: Projection and Viewing
- **Official Topic**: Implement camera viewing, orthographic parallel projection, and perspective projection with depth foreshortening.
- **Relevant Source Files**:
  - `CGL/src/projection/viewing_demo.hpp`, `viewing_demo.cpp`
- **Actual Implementation**:
  - **Orthographic Projection (`glOrtho`)**: Parallel projection where object size remains invariant with distance from camera.
  - **Perspective Projection (`gluPerspective`)**: Conical frustum projection producing depth foreshortening ($Z_2 / Z_1$ ratio verified).
  - **LookAt Camera**: `gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)` supporting interactive orbiting around space objects.
  - **Viewport Mapping**: NDC coordinates $[-1, 1]$ mapped to screen pixel boundaries.
- **Execution**:
  - `bin/cgl/cgl_runner.exe` $\rightarrow$ Press `[4]`. Press `[P]` to toggle Ortho/Perspective; Arrow keys to orbit camera.
- **Expected Output**:
  - Side-by-side or toggled view contrasting depth foreshortening against parallel orthographic view.
- **Screenshot Evidence**: `docs/screenshots/CGL/cgl_co4_projection.bmp`
- **Test Coverage**: 7 mathematical projection tests in `CGL/tests/test_cgl.cpp` passing 100%.

---

#### CGL-CO5: Clipping Algorithms
- **Official Topic**: Formulate and implement 2D line and polygon clipping algorithms against a rectangular clipping window.
- **Relevant Source Files**:
  - `CGL/src/clipping/clipping_algorithms.hpp`, `clipping_algorithms.cpp`
- **Actual Implementation**:
  - **Cohen-Sutherland Line Clipping**: 4-bit region outcodes (Top, Bottom, Right, Left) with bitwise trivial accept (`code1 | code2 == 0`) and trivial reject (`code1 & code2 != 0`), computing boundary line intersections.
  - **Liang-Barsky Line Clipping**: Parametric equation line clipping evaluating interval parameters $u_1, u_2$ against boundary inequalities.
  - **Sutherland-Hodgman Polygon Clipping**: Successive clipping of convex/concave polygon vertices against Left, Right, Bottom, and Top clipping window edges.
- **Execution**:
  - `bin/cgl/cgl_runner.exe` $\rightarrow$ Press `[5]`. Press `[M]` to switch between Cohen-Sutherland, Liang-Barsky, and Sutherland-Hodgman; `[Space]` to animate line through clip window.
- **Expected Output**:
  - Real-time display of unclipped vs clipped geometry outside the bounding box.
- **Screenshot Evidence**: `docs/screenshots/CGL/cgl_co5_clipping.bmp`
- **Test Coverage**: 10 mathematical boundary tests in `CGL/tests/test_cgl.cpp` passing 100%.

---

#### CGL-CO6: Texture, Lighting, and Color Models
- **Official Topic**: Implement local image texture mapping, Phong lighting model, shading interpolation, and color space representations.
- **Relevant Source Files**:
  - `CGL/src/appearance/lighting_texture_demo.hpp`, `lighting_texture_demo.cpp`
  - `CGL/src/common/image_loader.hpp`, `image_loader.cpp`
  - Assets: `CGL/assets/textures/space_hull.bmp`, `alien_surface.bmp`
- **Actual Implementation**:
  - **Genuine Local Image Texture Loading**: Custom uncompressed 24-bit RGB BMP loader reading bitmap headers from disk, binding to OpenGL texture handles with texture wrapping (`GL_REPEAT`, `GL_CLAMP`) and bilinear filtering (`GL_LINEAR`).
  - **Phong Reflection Model**: Ambient reflection ($I_a = k_a L_a$), Diffuse reflection ($I_d = k_d L_d (\mathbf{N} \cdot \mathbf{L})$), and Specular reflection with shininess ($I_s = k_s L_s (\mathbf{R} \cdot \mathbf{V})^n$).
  - **Shading Models**: Smooth Gouraud shading (bilinear vertex color interpolation) vs Flat shading.
  - **Color Models**: Real-time RGB $\leftrightarrow$ HSV color space conversion.
- **Execution**:
  - `bin/cgl/cgl_runner.exe` $\rightarrow$ Press `[6]`. Press `[L]` to toggle lighting, `[T]` for textures, `[U]` to toggle file vs procedural texture, `[7/8/9]` for ambient/diffuse/specular.
- **Expected Output**:
  - 3D textured, dynamically illuminated planetary sphere and fighter hull with specular highlights.
- **Screenshot Evidence**: `docs/screenshots/CGL/cgl_co6_appearance.bmp`
- **Test Coverage**: 11 assertions in `CGL/tests/test_cgl.cpp` passing 100%.

---

## 3. Verification Conclusion

Every single one of the 19 Course Outcomes has been audited, mapped to genuine source code files, exercised via interactive runtime demonstrations, and verified against 302 automated unit and regression tests.
