# AIRSTRIKE — Phase 8.3 Implementation Report: PL Enemy Implementation

**Module**: Programming Laboratory (PL)  
**Standard**: ISO/IEC C++17  
**Verification Target**: Course Outcomes PL-CO1 through PL-CO5  
**Test Suite Status**: **99 / 99 Assertions Passed (100% PASS)**  

---

## 1. Executive Summary

Phase 8.3 implements the five planned **Programming Laboratory (PL)** enemy types directly in standard C++17 within the native `PL/` codebase. Every enemy model is built upon genuine data structures and algorithms from the approved academic syllabus:
1. **Spatial Grid Swarm (`GridSwarmEnemy`)** — Demonstrates **PL-CO1** via 2D row-major array indexing and spatial partitioning.
2. **Formation Squadron Chain (`FormationSquadron`)** — Demonstrates **PL-CO2** via Doubly Linked Lists with $O(1)$ bidirectional traversal, node removal, and formation splitting.
3. **Queue Wave Echelon (`QueueWaveEchelon`)** — Demonstrates **PL-CO3** via Circular FIFO Queue deployment with modulo wraparound arithmetic.
4. **Target-Seeking Hunter (`TargetSeekingHunter`)** — Demonstrates **PL-CO4** via QuickSort, MergeSort, precondition validation, and $O(\log N)$ Binary Search.
5. **Hash-Shielded Matrix Craft (`HashShieldController`)** — Demonstrates **PL-CO5** via Open Addressing Hash Table with Linear Probing and Tombstone deletion.

---

## 2. Files Created and Modified

### Newly Created Files
| File Path | Subject / CO | Component Description |
| :--- | :--- | :--- |
| `PL/src/arrays/grid_swarm_enemy.h` | PL-CO1 | Class definition for 2D spatial grid partitioning, sector binning, and scatter mine dispersion. |
| `PL/src/arrays/grid_swarm_enemy.cpp` | PL-CO1 | Implementation of row-major coordinate calculation, capacity clamping, and broadphase queries. |
| `PL/src/linked_lists/formation_squadron.h` | PL-CO2 | Class definition for Doubly Linked List enemy formation chain (`SquadronCraft`). |
| `PL/src/linked_lists/formation_squadron.cpp` | PL-CO2 | Implementation of $O(1)$ Vanguard/Rearguard insertion, node removal, and $O(1)$ formation splitting. |
| `PL/src/queues/echelon_dispatcher.h` | PL-CO3 | Class definition for circular array FIFO wave echelon queue. |
| `PL/src/queues/echelon_dispatcher.cpp` | PL-CO3 | Implementation of circular queue enqueue, dequeue, peek, and wraparound verification. |
| `PL/src/searching/hunter_targeting.h` | PL-CO4 | Class definition for QuickSort/MergeSort telemetry sorting and Binary Search targeting. |
| `PL/src/searching/hunter_targeting.cpp` | PL-CO4 | Implementation of Lomuto partitioning QuickSort, MergeSort, sorted precondition verification, and duplicate distance handling. |
| `PL/src/hashing/hash_shield_controller.h` | PL-CO5 | Class definition for Open Addressing Hash Table with linear probing and tombstone markers. |
| `PL/src/hashing/hash_shield_controller.cpp` | PL-CO5 | Implementation of polynomial rolling hash, linear probe sequencing, tombstone deletion, and dynamic rehashing. |
| `PL/tests/test_pl_enemies.h` | PL-CO1..5 | Test suite header exposing `runPLEnemyTests()`. |
| `PL/tests/test_pl_enemies.cpp` | PL-CO1..5 | 46 comprehensive unit assertions covering normal, boundary, collision, wraparound, and tombstone cases. |
| `docs/PL/PHASE_8_3_PL_ENEMY_IMPLEMENTATION.md` | Doc | Complete academic mapping, viva Q&As, and verification report. |

### Modified Files
| File Path | Modification Summary |
| :--- | :--- |
| `PL/main.cpp` | Included `test_pl_enemies.h` and registered `runPLEnemyTests()` into automated test runner (`--test`). Total PL assertions updated from 53 to 99. |

---

## 3. Academic CO Mapping & Data Structures Architecture

```
+----------------------------------------------------------------------------------------------------+
|                                  AIRSTRIKE PL ENEMY SYSTEM ARCHITECTURE                            |
+----------------------------------------------------------------------------------------------------+
|  [PL-CO1: 2D Spatial Grid Swarm]   -->  Row-Major Offset: Base + (r * COLS + c) * sizeof(Drone)    |
|  [PL-CO2: Formation Squadron Chain] -->  Doubly Linked List: O(1) Splitting (head/tail/count separation)  |
|  [PL-CO3: Queue Wave Echelon]      -->  Circular Buffer: rear = (rear + 1) % M, FIFO Wraparound    |
|  [PL-CO4: Target-Seeking Hunter]   -->  QuickSort / MergeSort -> Precondition Check -> Binary Search |
|  [PL-CO5: Hash-Shielded Matrix]    -->  Polynomial Hash -> Linear Probing -> Tombstone Deletion    |
+----------------------------------------------------------------------------------------------------+
```

### Detailed Mapping Table

| Enemy Name | Exact CO | Syllabus Concept | C++ Class / Struct | Key Methods & Algorithms |
| :--- | :--- | :--- | :--- | :--- |
| **Spatial Grid Swarm** | **PL-CO1** | Contiguous 2D Array & Spatial Partitioning | `GridSwarmEnemy`<br>`SwarmDrone` | `calculateRowMajorOffset()`, `getSector()`, `spawnDrone()`, `moveDrone()`, `findNearbyDrones()`, `disperseScatterMines()` |
| **Formation Squadron Chain** | **PL-CO2** | Doubly Linked Lists & Dynamic Splitting | `FormationSquadron`<br>`SquadronCraft` | `addVanguard()`, `addRearguard()`, `insertAtRank()`, `removeShip()`, `removeNode()`, `splitAt()`, `advanceFormation()` |
| **Queue Wave Echelon** | **PL-CO3** | Circular Array FIFO Queue | `QueueWaveEchelon`<br>`EchelonWaveUnit` | `enqueue()`, `dequeue()`, `peek()`, `isWrappedAround()`, `getFrontIndex()`, `getRearIndex()` |
| **Target-Seeking Hunter** | **PL-CO4** | Searching & Sorting Algorithms | `TargetSeekingHunter`<br>`TargetTelemetry` | `sortByDistanceQuickSort()`, `sortByDistanceMergeSort()`, `isTelemetrySorted()`, `binarySearchTarget()`, `findAllTargetsAtDistance()` |
| **Hash-Shielded Matrix Craft** | **PL-CO5** | Open Addressing Hash Table with Tombstones | `HashShieldController`<br>`MatrixShieldSlot` | `hashFunction()`, `activateHarmonic()`, `lookupHarmonic()`, `deactivateHarmonic()`, `rehash()`, `isSlotTombstone()` |

---

## 4. Technical Algorithm Explanations

### 1. Spatial Grid Swarm (`GridSwarmEnemy` — PL-CO1)
* **Contiguous 2D Memory Representation**: The arena ($800 \times 600$) is divided into an $8 \times 8$ grid of sectors. Memory offset calculation follows the canonical row-major formula:
  $$\text{Offset}(r, c) = (r \times \text{GRID\_COLS} + c) \times \text{sizeof}(\text{Element})$$
* **$O(1)$ Sector Binning & Local Queries**: Rather than performing an exhaustive $O(N)$ distance check across all active entities in the arena, `getSector(x, y)` computes cell coordinates in $O(1)$ time. When checking nearby entities, `findNearbyDrones()` only scans the local sector and its 8 adjacent neighboring sectors ($3 \times 3$ sub-matrix), achieving localized spatial partitioning.
* **Algorithmic Mine Dispersion**: Upon explosion, `disperseScatterMines()` calculates 8 radial coordinates using trigonometric dispersion and immediately registers sub-munitions into their respective 2D grid cells.

### 2. Formation Squadron Chain (`FormationSquadron` — PL-CO2)
* **Bidirectional Linkage**: Every `SquadronCraft` node maintains two explicit pointers: `prev` pointing toward Vanguard (head) and `next` pointing toward Rearguard (tail).
* **$O(1)$ Formation Splitting**: In an array-based structure, splitting a formation at index $k$ requires reallocating and copying elements ($O(N)$ time). In `FormationSquadron`, splitting at target ship node is achieved in $O(1)$ pointer operations:
  $$\text{target}\rightarrow\text{next}\rightarrow\text{prev} = \text{nullptr}$$
  $$\text{target}\rightarrow\text{next} = \text{nullptr}$$
  The current squadron updates its `tail` to `target`, while the detached squadron assigns its `head` to `secondHead` and `tail` to `secondTail`. Both squadrons remain fully operational, independently traversable doubly linked lists.
* **$O(1)$ Arbitrary Destruction**: Given a pointer to an enemy node destroyed by laser fire, pointer rewiring removes it in $O(1)$ time without searching from the head:
  $$\text{node}\rightarrow\text{prev}\rightarrow\text{next} = \text{node}\rightarrow\text{next}$$
  $$\text{node}\rightarrow\text{next}\rightarrow\text{prev} = \text{node}\rightarrow\text{prev}$$

### 3. Queue Wave Echelon (`QueueWaveEchelon` — PL-CO3)
* **Circular Buffer Arithmetic**: Avoids memory drift and element-shifting overhead ($O(N)$) inherent in standard linear arrays.
* **Index Wraparound**: Enqueue and dequeue advance using modulo arithmetic:
  $$\text{rearIndex} = (\text{rearIndex} + 1) \pmod{\text{maxCapacity}}$$
  $$\text{frontIndex} = (\text{frontIndex} + 1) \pmod{\text{maxCapacity}}$$
* **Wraparound Verification**: When multiple waves are dispatched and new echelons are buffered, the queue enters a state where $\text{rearIndex} < \text{frontIndex}$. The implementation explicitly exposes and validates this circular condition while guaranteeing strict FIFO (First-In, First-Out) deployment order.

### 4. Target-Seeking Hunter (`TargetSeekingHunter` — PL-CO4)
* **Precondition Enforcement**: Academic binary search requires that data be strictly sorted. `TargetSeekingHunter` enforces this via `isTelemetrySorted()` prior to binary search queries.
* **QuickSort & MergeSort**:
  - `sortByDistanceQuickSort()` applies in-place Lomuto partitioning with a pivot, recording empirical comparisons and swaps.
  - `sortByDistanceMergeSort()` applies divide-and-conquer recursion ($T(N) = 2T(N/2) + O(N)$), guaranteeing worst-case $O(N \log N)$ complexity.
* **Logarithmic Target Acquisition**: Binary search inspects the midpoint distance:
  $$\text{mid} = \text{left} + \frac{\text{right} - \text{left}}{2}$$
  Halving the search space each step achieves $O(\log_2 N)$ search time.
* **Duplicate & Miss Handling**: If multiple targets share identical threat distances, `findAllTargetsAtDistance()` uses binary search to locate one match, then expands to contiguous boundaries to extract all duplicates. Non-existent distances terminate gracefully with `found = false` and index `-1`.

### 5. Hash-Shielded Matrix Craft (`HashShieldController` — PL-CO5)
* **Polynomial Rolling Hash**: Converts harmonic string keys into an initial bucket index:
  $$h(k) = \left(\sum_{i=0}^{L-1} k[i] \cdot 31^{L-1-i}\right) \pmod{\text{tableCapacity}}$$
* **Open Addressing with Linear Probing**: Resolves collisions when two distinct harmonic keys map to the same bucket:
  $$h_i(k) = (h(k) + i) \pmod{\text{tableCapacity}} \quad \text{for } i = 0, 1, 2, \dots$$
* **Tombstone Deletion Pattern**: When a shield frequency is disrupted, marking the slot as `EMPTY` would prematurely terminate the linear probing sequence for subsequent keys that collided and probed past it. `HashShieldController` sets the slot state to `TOMBSTONE`. Probing lookups continue past tombstones, while new insertions can recycle tombstone slots, preserving chain continuity.
* **Dynamic Rehashing**: When the load factor $\alpha = \frac{N}{M}$ exceeds $0.70$, the capacity expands to $2M + 1$, rehashing all active entries into the enlarged table and purging tombstones.

---

## 5. Viva Voce Examination Questions & Answers

### Enemy 1: Spatial Grid Swarm (PL-CO1)
> **Q**: *Why is a 2D array preferred over a linear array for spatial partitioning of enemy swarms, and how is contiguous memory addressing computed?*  
> **A**: A 2D array provides natural two-dimensional spatial mapping where any $(x, y)$ world coordinate maps in $O(1)$ time to a row and column sector $(r, c)$. In C++ memory, 2D arrays are stored contiguously in row-major order using the address formula:
> $$\text{Address}(r, c) = \text{Base} + (r \times \text{COLS} + c) \times \text{sizeof}(\text{Element})$$
> This enables instant spatial binning and limits broadphase collision checks to local and adjacent neighboring cells rather than testing every entity in the arena ($O(N)$).

### Enemy 2: Formation Squadron Chain (PL-CO2)
> **Q**: *How does a Doubly Linked List enable $O(1)$ formation splitting when a middle ship is destroyed, compared to an array?*  
> **A**: In an array, splitting or removing an element at index $k$ requires shifting all trailing elements ($O(N)$ operations) or allocating two new arrays and copying memory. In a Doubly Linked List, splitting requires only severing the forward pointer of the target node and the backward pointer of its successor:
> $$\text{target}\rightarrow\text{next}\rightarrow\text{prev} = \text{nullptr}; \quad \text{target}\rightarrow\text{next} = \text{nullptr};$$
> Assigning the severed head to a new `FormationSquadron` creates two fully independent, bidirectional linked lists in $O(1)$ pointer operations without moving any data in memory.

### Enemy 3: Queue Wave Echelon (PL-CO3)
> **Q**: *Why must enemy wave dispatching use a Circular Queue rather than a simple linear queue array?*  
> **A**: In a linear array queue, dequeuing elements causes the front index to move toward the end of the array, leaving unused space at the beginning that cannot be reused without shifting all elements left ($O(N)$). A Circular Queue wraps around using modulo arithmetic:
> $$\text{rear} = (\text{rear} + 1) \pmod{\text{capacity}}$$
> This guarantees $O(1)$ enqueue and dequeue times, constant memory utilization, and prevents false queue-overflow conditions (memory drift).

### Enemy 4: Target-Seeking Hunter (PL-CO4)
> **Q**: *What is the prerequisite for executing Binary Search on enemy telemetry, and what happens if this condition is violated?*  
> **A**: Binary search requires the strict precondition that the data array is monotonically sorted according to the search key. If executed on an unsorted array, the divide-and-conquer assumption (that target is in left or right half based on midpoint comparison) fails, leading to false negatives (misses) or undefined results. `TargetSeekingHunter` guarantees sorted order by running QuickSort or MergeSort ($O(N \log N)$) and asserting `isTelemetrySorted()` before executing binary search in $O(\log N)$ time.

### Enemy 5: Hash-Shielded Matrix Craft (PL-CO5)
> **Q**: *What is the purpose of the "Tombstone" marker in an open-addressing hash table when a shield harmonic is deactivated?*  
> **A**: In linear probing, searching for a key terminates when an `EMPTY` slot is reached. If an intermediate key in a collision probe chain is deleted by simply marking its slot `EMPTY`, subsequent searches for colliding keys placed further down the probe sequence will stop at that empty slot and falsely report that the key does not exist. A `TOMBSTONE` marker indicates that the slot is vacant for new insertions but must not terminate probe chains for ongoing search queries.

---

## 6. Build and Automated Test Verification

### Compilation Command
```powershell
powershell -ExecutionPolicy Bypass -File scripts\build_pl.ps1
```

**Build Output**:
```
========================================================
[PL] Building Programming Laboratory (C++17) Module...
========================================================
[INFO] Detected C++ Compiler: g++.exe (MinGW.org GCC-6.3.0-1) 6.3.0
[INFO] CMake not detected in PATH. Using direct MinGW g++ compilation fallback...
[SUCCESS] PL C++ module built successfully!
```

### Test Runner Execution
```powershell
.\bin\pl\pl_test_runner.exe --test
```

**Full Test Output**:
```
==============================================================================
        AIRSTRIKE PROGRAMMING LABORATORY - AUTOMATED VERIFICATION SUITE       
        Evaluating Course Outcomes: PL-CO1 to PL-CO5 (Standard C++17)         
==============================================================================

======================================================
[TEST SUITE] PL-CO1: Arrays & Dynamic Memory (10 Tests)
======================================================
  [PASS] 1D Array: insertAt valid index
  [PASS] 1D Array: deleteAt and searchLinear
  [PASS] 2D Array: Grid update and coordinate search
  [PASS] DynamicArray: Push back and capacity doubling
  [PASS] DynamicArray: Insert and removeAt
  [PASS] DynamicArray: Rule of Five deep copy isolation
  [PASS] BulletPool: Spawning preallocated bullets
  [PASS] BulletPool: Overflow gracefully rejected (-1)
  [PASS] BulletPool: Update and arena boundary culling
  [PASS] CollisionGrid: Spatial partitioning collision hit and miss

======================================================
[TEST SUITE] PL-CO2: Linked Lists (10 Tests)
======================================================
  [PASS] SinglyLinkedList: Head, Tail, and Positional Insertion
  [PASS] SinglyLinkedList: Middle Node Deletion and Search Miss
  [PASS] SinglyLinkedList: Deep Copy Independent Node Lifetime
  [PASS] DoublyLinkedList: Bidirectional Pointer Linkage
  [PASS] DoublyLinkedList: Head Deletion updates Head and Prev pointer
  [PASS] DoublyLinkedList: Tail Deletion by Entity ID
  [PASS] CircularLinkedList: Seamless Endless Cyclic Traversal
  [PASS] CircularLinkedList: Proper Circular Termination Detection
  [PASS] CircularLinkedList: Middle Deletion preserves continuous ring
  [PASS] CircularLinkedList: Clear memory safely resets ring pointers

======================================================
[TEST SUITE] PL-CO3: Stack Operations (6 Tests)
======================================================
  [PASS] Stack: Push and Peek inspects top LIFO element
  [PASS] Stack: Pop removes and returns most recent action
[ERROR: ActionStack Underflow] Cannot pop from empty combat stack!
  [PASS] Stack: Underflow protection prevents popping empty stack
[ERROR: ActionStack Overflow] Cannot push action ID 30; stack reached capacity 2!
  [PASS] Stack: Overflow protection prevents pushing beyond capacity
  [PASS] Stack: Deep copy maintains independent buffer state
  [PASS] Stack: Clear resets top index to empty

======================================================
[TEST SUITE] PL-CO3: Queue Operations (6 Tests)
======================================================
  [PASS] Queue: Enqueue and Front inspects earliest FIFO element
  [PASS] Queue: Dequeue removes earliest element in FIFO order
  [PASS] Queue: Circular buffer wraps around indices correctly
[ERROR: WaveQueue Overflow] Cannot enqueue wave 6; queue at capacity 4!
  [PASS] Queue: Overflow protection rejects enqueue on full queue
[ERROR: WaveQueue Underflow] Cannot dequeue from empty wave queue!
  [PASS] Queue: Underflow protection prevents dequeue on empty queue
  [PASS] Queue: Deep copy maintains independent circular buffer state

======================================================
[TEST SUITE] PL-CO4: Searching Algorithms (6 Tests)
======================================================
  [PASS] LinearSearch: Found target with exact linear comparison count
  [PASS] LinearSearch: Exhaustive traversal on search miss
  [PASS] BinarySearch: O(log N) logarithmic lookup hit
  [PASS] BinarySearch: Boundary boundary extremes (First and Last)
  [PASS] BinarySearch: Correct miss detection without infinite looping
  [PASS] BinarySearchScores: AirStrike leaderboard binary search

======================================================
[TEST SUITE] PL-CO4: Sorting Algorithms (7 Tests)
======================================================
  [PASS] BubbleSort: Successfully sorts array in ascending order
  [PASS] SelectionSort: Minimal O(N) swaps verification
  [PASS] InsertionSort: Successfully sorts array in-place
  [PASS] MergeSort: O(N log N) divide-and-conquer sorting
  [PASS] QuickSort: In-place recursive partitioning sorting
  [PASS] QuickSortScores: AirStrike High Scores descending ordering
  [PASS] BubbleSort: Early termination flag achieves O(N) best case

======================================================
[TEST SUITE] PL-CO5: Hashing Techniques (8 Tests)
======================================================
  [PASS] HashTableChaining: Insert and Retrieve key-value pair
  [PASS] HashTableChaining: Collisions successfully resolved via linked chain
  [PASS] HashTableChaining: Key deletion updates bucket chain properly
  [PASS] HashTableChaining: Automatic rehashing expands bucket capacity
  [PASS] HashTableProbing: Insert and Retrieve key-value pair
  [PASS] HashTableProbing: Linear probe sequence resolves collisions
  [PASS] HashTableProbing: Tombstone marker permits continued probe search
  [PASS] HashTableProbing: Dynamic rehashing expands table array

--- Testing PL Enemy Implementations (PL-CO1 to PL-CO5) ---
  [PASS] SpatialGridSwarm: Initial active drone count is 0
  [PASS] SpatialGridSwarm: Row-major contiguous offset calculation (row * 8 + col)
  [PASS] SpatialGridSwarm: Coordinate (50, 50) maps to Sector [0, 0]
  [PASS] SpatialGridSwarm: Coordinate (750, 550) maps to Sector [7, 7]
  [PASS] SpatialGridSwarm: Out-of-bounds coords clamped to valid grid boundaries [7, 0]
  [PASS] SpatialGridSwarm: Spawning tracks 2D cell occupancy count (2 drones in [0, 0])
  [PASS] SpatialGridSwarm: Rejects duplicate drone ID correctly
  [PASS] SpatialGridSwarm: Drone movement updates spatial partitioning across cells
  [PASS] SpatialGridSwarm: Cell capacity saturation (8 max per cell) enforced
  [PASS] SpatialGridSwarm: 9-sector broadphase query finds all nearby swarm units
  [PASS] SpatialGridSwarm: Algorithmic scatter mine dispersion into neighboring cells
  [PASS] SpatialGridSwarm: Drone destruction frees cell occupancy and decrements count
  [PASS] FormationSquadron: Initial squadron state is empty
  [PASS] FormationSquadron: Add Vanguard and Rearguard constructs 4-ship chain
  [PASS] FormationSquadron: Rank insertion at interior index maintains chain length
  [PASS] FormationSquadron: Head points to Vanguard and Tail points to Rearguard
  [PASS] FormationSquadron: Coordinated maneuver shifts all craft coordinates
  [PASS] FormationSquadron: Mid-chain craft destruction unlinks node cleanly
  [PASS] FormationSquadron: O(1) Formation Splitting cuts chain into two valid squadrons
  [PASS] FormationSquadron: Split preserves head/tail integrity in both separated chains
  [PASS] FormationSquadron: Splitting at tail returns false with zero side effects
  [PASS] QueueWaveEchelon: Initial circular queue is empty with capacity 4
  [PASS] QueueWaveEchelon: Enqueue fills circular buffer to max capacity
[Queue Overflow] Echelon queue full (4 max)!
  [PASS] QueueWaveEchelon: Queue overflow blocked when capacity reached
  [PASS] QueueWaveEchelon: FIFO dequeue dispatches echelons in strict arrival order
  [PASS] QueueWaveEchelon: Circular index wraparound verified (rearIndex < frontIndex)
  [PASS] QueueWaveEchelon: Peek inspects next launching echelon without dequeuing
  [PASS] QueueWaveEchelon: FIFO order completely preserved across circular buffer wraparound
[Queue Underflow] Echelon queue is empty!
  [PASS] QueueWaveEchelon: Underflow gracefully blocked when dequeuing from empty queue
  [PASS] TargetSeekingHunter: Initial telemetry target count is 0
  [PASS] TargetSeekingHunter: Unsorted telemetry correctly recognized before search
  [PASS] TargetSeekingHunter: QuickSort sorts telemetry monotonically with verified swaps
  [PASS] TargetSeekingHunter: Binary Search finds target in O(log N) comparisons
  [PASS] TargetSeekingHunter: Binary Search gracefully reports non-existent target miss
  [PASS] TargetSeekingHunter: Binary Search expands to locate all duplicate threat distances
  [PASS] TargetSeekingHunter: Target acquisition engages nearest tactical threat
  [PASS] TargetSeekingHunter: MergeSort sorts targets ascending via divide-and-conquer
  [PASS] HashShieldMatrix: Initial matrix created with prime capacity 7
  [PASS] HashShieldMatrix: Harmonics activated into open addressing hash table
  [PASS] HashShieldMatrix: O(1) average lookup retrieves shield barrier strength
  [PASS] HashShieldMatrix: Linear probing successfully resolves hash collisions
  [PASS] HashShieldMatrix: Harmonic deactivation marks slot as TOMBSTONE
  [PASS] HashShieldMatrix: Tombstone marker prevents broken probing chains for colliding keys
  [PASS] HashShieldMatrix: Deactivated harmonic is no longer found in table
  [PASS] HashShieldMatrix: Subsequent insertion recycles existing tombstone slot
  [PASS] HashShieldMatrix: Dynamic rehashing expands table when load factor > 0.70

==============================================================================
                          FINAL PL TEST RESULTS SUMMARY                       
==============================================================================
  Total Assertions Run : 99
  Passed Assertions    : 99 [OK]
  Failed Assertions    : 0
  Course Outcomes      : PL-CO1, PL-CO2, PL-CO3, PL-CO4, PL-CO5 (100% COVERED)
==============================================================================
[SUCCESS] ALL 99 AUTOMATED TESTS PASSED WITH ZERO ERRORS.
```

---

## 7. Operational Limitations & Standalone Execution Scope

As required by the Phase 8.3 instructions, the following operational characteristics and boundaries are explicitly documented:
1. **Autonomous C++17 Module Execution**: The Programming Laboratory (PL) module operates as an independent, standalone command-line suite and automated test verification harness (`pl_test_runner.exe`). It does not render 2D/3D graphics; graphics rendering is strictly encapsulated within the CGL OpenGL module (`cgl_runner.exe`) and web canvas (`web/src/game/`).
2. **Absence of JNI / TCP Cross-Bridges**: Per mandatory rule #7, zero JNI, TCP, or shared-memory bridge layers were introduced between Java (PSOOP) and C++ (PL). The C++ algorithms execute genuinely within the native MinGW C++17 runtime.
3. **Strict Course Outcome Integrity**: All PL enemies and their algorithmic implementations map strictly to the verified syllabus Course Outcomes (PL-CO1 to PL-CO5). Total project Course Outcomes remain exactly 19.
