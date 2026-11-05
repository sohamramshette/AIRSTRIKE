# AIRSTRIKE Phase 3: Programming Laboratory (PL) C++17 Implementation Report

## Executive Summary
This document provides an exhaustive academic and technical implementation report for **Phase 3: Programming Laboratory (PL) in Standard C++17**. The module is engineered completely independently of the Java game client (no JNI, sockets, or IPC), strictly utilizing custom-built data structures and manual memory management in compliance with college curriculum requirements.

All five Course Outcomes (**PL-CO1 to PL-CO5**) are fully implemented, verified via an interactive 9-option console demonstration suite (`bin/pl/pl_test_runner.exe`), and validated by **53/53 automated unit tests with 100% pass rate**.

---

## 1. Module Architecture & Source Layout

```
PL/
├── CMakeLists.txt
├── main.cpp                              # Master interactive CLI & headless test dispatcher
├── src/
│   ├── arrays/
│   │   ├── array_ops.h / .cpp           # 1D/2D static array operations & 4x4 radar grid
│   │   ├── dynamic_array.h / .cpp       # Custom dynamic array (Rule of 5, geometric doubling)
│   │   ├── bullet_pool.h / .cpp         # O(1) AirStrike pre-allocated bullet object pool
│   │   └── collision_grid.h / .cpp      # 8x8 spatial partitioning asteroid collision grid
│   ├── linked_lists/
│   │   ├── singly_linked_list.h / .cpp  # Singly linked list (AirStrike flight telemetry log)
│   │   ├── doubly_linked_list.h / .cpp  # Doubly linked list (Vanguard-Rearguard squadron roster)
│   │   └── circular_linked_list.h / .cpp# Circular linked list (Cyclic weapon selector ring)
│   ├── stacks/
│   │   └── action_stack.h / .cpp        # LIFO combat history & undo engine with overflow/underflow
│   ├── queues/
│   │   └── wave_queue.h / .cpp          # FIFO circular enemy wave spawner
│   ├── searching/
│   │   └── search_algorithms.h / .cpp   # Linear search vs O(log N) binary search with metrics
│   ├── sorting/
│   │   └── sort_algorithms.h / .cpp     # 5 canonical sorts (Bubble, Selection, Insertion, Merge, Quick)
│   └── hashing/
│       ├── hash_table_chaining.h / .cpp # Separate chaining hash table with DJB2 & rehash (> 0.75)
│       └── hash_table_probing.h / .cpp  # Open addressing (linear probing) with tombstones & rehash (> 0.70)
└── tests/
    ├── test_common.h                    # Custom lightweight testing assertion harness
    ├── test_arrays.h / .cpp             # 10 automated tests for PL-CO1
    ├── test_linked_lists.h / .cpp       # 10 automated tests for PL-CO2
    ├── test_stack.h / .cpp              # 6 automated tests for PL-CO3 (Stack)
    ├── test_queue.h / .cpp              # 6 automated tests for PL-CO3 (Queue)
    ├── test_searching.h / .cpp          # 6 automated tests for PL-CO4 (Searching)
    ├── test_sorting.h / .cpp            # 7 automated tests for PL-CO4 (Sorting)
    └── test_hashing.h / .cpp            # 8 automated tests for PL-CO5 (Hashing)
```

---

## 2. Course Outcome Implementation Details

### PL-CO1: Arrays & Dynamic Memory Management
- **Concepts Implemented**:
  1. 1D Static Arrays: In-place insertion, deletion, linear search, element updating, and traversal.
  2. 2D Static Matrix: $4 \times 4$ sector radar grid tracking entity coordinates and signatures.
  3. Custom `DynamicArray`: Heap-allocated array with manual `new[]`/`delete[]`, amortized $O(1)$ capacity doubling, and complete C++ Rule of Five (Destructor, Copy Constructor, Copy Assignment, Move Constructor, Move Assignment).
  4. `BulletPool`: Fixed 32-element projectile pool avoiding runtime allocations during combat loop; supports spawning, frame delta updates, boundary culling, and slot recycling.
  5. `CollisionGrid`: $8 \times 8$ spatial hash grid dividing an $800 \times 600$ combat zone into 64 sectors for $O(1)$ broadphase asteroid collision detection.
- **Complexity Analysis**:
  - 1D/2D Static Array Insertion/Deletion: $O(N)$ due to shifting elements.
  - Dynamic Array Push Back: Amortized $O(1)$, Worst case $O(N)$ when capacity doubles.
  - Bullet Pool Spawn/Recycle: $O(1)$ to $O(M)$ where $M = 32$ (constant upper bound).
  - Collision Grid Sector Query: $O(1)$ hash coordinate calculation; $3 \times 3$ neighbor window query avoids $O(N^2)$ all-pairs checks.

### PL-CO2: Linked Lists
- **Concepts Implemented**:
  1. **Singly Linked List (`SinglyLinkedList`)**:
     - *Application*: Chronological flight event log (`FlightLogNode`).
     - *Operations*: Head insertion, tail insertion, index insertion, head/tail/index deletion, ID search, traversal, deep copy.
     - *Why Suitable*: Flight events arrive sequentially; dynamic head/tail insertion requires zero memory re-allocation or capacity pre-sizing.
  2. **Doubly Linked List (`DoublyLinkedList`)**:
     - *Application*: Active enemy squadron formation roster (`EnemyNode`).
     - *Operations*: Vanguard to rearguard forward traversal, rearguard to vanguard backward traversal, head/tail insertion, deletion by ID, instant $O(1)$ node removal given node pointer.
     - *Why Suitable*: Combat flanking AI traverses bidirectional squadron lines; destroyed ships anywhere in formation are unlinked in $O(1)$ without predecessor traversal.
  3. **Circular Linked List (`CircularLinkedList`)**:
     - *Application*: Player cyclic weapon selector wheel (`WeaponNode`).
     - *Operations*: Continuous forward cycling (`cycleNext()`), head/tail insertion, node deletion, proper circular loop termination detection.
     - *Why Suitable*: Weapon switching is fundamentally cyclical (Plasma -> Laser -> Torpedo -> EMP -> Plasma); reaching the tail naturally wraps to head without modulo arithmetic.

### PL-CO3: Stack and Queue Applications
- **Concepts Implemented**:
  1. **Stack (`ActionStack`)**:
     - *Application*: Combat action history and undo engine (`CombatAction`).
     - *Semantics*: Strict LIFO (Last-In, First-Out).
     - *Operations*: `push()`, `pop()`, `peek()`, `isEmpty()`, `isFull()`, capacity boundaries, and explicit overflow/underflow detection.
  2. **Queue (`WaveQueue`)**:
     - *Application*: Sequential enemy wave spawner (`EnemyWave`).
     - *Semantics*: Strict FIFO (First-In, First-Out) using a circular array buffer.
     - *Operations*: `enqueue()`, `dequeue()`, `front()`, circular index wrapping (`(idx + 1) % capacity`), overflow/underflow handling without memory drift.

### PL-CO4: Searching and Sorting Algorithms
- **Searching Algorithms**:
  - `LinearSearch`: Exhaustive sequential scanning; tracks comparison counts and microsecond latency. $O(N)$ worst-case.
  - `BinarySearch`: Divide-and-conquer logarithmic search on pre-sorted arrays; tracks comparison counts. $O(\log_2 N)$ worst-case.
  - Real AirStrike leaderboard pilot score searching.
- **Sorting Algorithms (Empirical Benchmark Comparison)**:

| Algorithm | Best Time | Average Time | Worst Time | Space Complexity | Stable? | Practical AirStrike Role |
| :--- | :--- | :--- | :--- | :--- | :---: | :--- |
| **Bubble Sort** | $O(N)$ (Flag) | $O(N^2)$ | $O(N^2)$ | $O(1)$ | Yes | Small telemetry arrays already nearly sorted |
| **Selection Sort**| $O(N^2)$ | $O(N^2)$ | $O(N^2)$ | $O(1)$ | No | Memory write minimization (at most $N$ swaps) |
| **Insertion Sort**| $O(N)$ | $O(N^2)$ | $O(N^2)$ | $O(1)$ | Yes | Online stream insertion of incoming scores |
| **Merge Sort** | $O(N \log N)$ | $O(N \log N)$ | $O(N \log N)$ | $O(N)$ | Yes | Guaranteed predictable latency for tournament brackets |
| **Quick Sort** | $O(N \log N)$ | $O(N \log N)$ | $O(N^2)$ | $O(\log N)$ | No | High-speed descending pilot leaderboard ranking |

### PL-CO5: Hashing Techniques & Collision Resolution
- **Separate Chaining (`HashTableChaining`)**:
  - *Algorithm*: DJB2 polynomial hash function ($hash = ((hash \ll 5) + hash) + c$).
  - *Collision Resolution*: Linked bucket chains (`ChainNode`).
  - *Rehashing*: Dynamic array resizing and full element rehash when load factor $\lambda = \frac{\text{items}}{\text{buckets}} > 0.75$.
- **Open Addressing (`HashTableProbing`)**:
  - *Algorithm*: Polynomial rolling hash function.
  - *Collision Resolution*: Linear probing sequence: $h(k, i) = (h'(k) + i) \pmod M$.
  - *Tombstone Pattern*: Deleted slots marked with `SlotStatus::DELETED` to prevent premature search termination while allowing reuse on subsequent insertions.
  - *Rehashing*: Automatic expansion when load factor $\lambda \ge 0.70$.

---

## 3. Automated Test Suite Verification

Execution command: `bin/pl/pl_test_runner.exe --test`

```
==============================================================================
        AIRSTRIKE PROGRAMMING LABORATORY - AUTOMATED VERIFICATION SUITE       
        Evaluating Course Outcomes: PL-CO1 to PL-CO5 (Standard C++17)         
==============================================================================

[TEST SUITE] PL-CO1: Arrays & Dynamic Memory (10 Tests)
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

[TEST SUITE] PL-CO2: Linked Lists (10 Tests)
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

[TEST SUITE] PL-CO3: Stack Operations (6 Tests)
  [PASS] Stack: Push and Peek inspects top LIFO element
  [PASS] Stack: Pop removes and returns most recent action
  [PASS] Stack: Underflow protection prevents popping empty stack
  [PASS] Stack: Overflow protection prevents pushing beyond capacity
  [PASS] Stack: Deep copy maintains independent buffer state
  [PASS] Stack: Clear resets top index to empty

[TEST SUITE] PL-CO3: Queue Operations (6 Tests)
  [PASS] Queue: Enqueue and Front inspects earliest FIFO element
  [PASS] Queue: Dequeue removes earliest element in FIFO order
  [PASS] Queue: Circular buffer wraps around indices correctly
  [PASS] Queue: Overflow protection rejects enqueue on full queue
  [PASS] Queue: Underflow protection prevents dequeue on empty queue
  [PASS] Queue: Deep copy maintains independent circular buffer state

[TEST SUITE] PL-CO4: Searching Algorithms (6 Tests)
  [PASS] LinearSearch: Found target with exact linear comparison count
  [PASS] LinearSearch: Exhaustive traversal on search miss
  [PASS] BinarySearch: O(log N) logarithmic lookup hit
  [PASS] BinarySearch: Boundary extremes (First and Last)
  [PASS] BinarySearch: Correct miss detection without infinite looping
  [PASS] BinarySearchScores: AirStrike leaderboard binary search

[TEST SUITE] PL-CO4: Sorting Algorithms (7 Tests)
  [PASS] BubbleSort: Successfully sorts array in ascending order
  [PASS] SelectionSort: Minimal O(N) swaps verification
  [PASS] InsertionSort: Successfully sorts array in-place
  [PASS] MergeSort: O(N log N) divide-and-conquer sorting
  [PASS] QuickSort: In-place recursive partitioning sorting
  [PASS] QuickSortScores: AirStrike High Scores descending ordering
  [PASS] BubbleSort: Early termination flag achieves O(N) best case

[TEST SUITE] PL-CO5: Hashing Techniques (8 Tests)
  [PASS] HashTableChaining: Insert and Retrieve key-value pair
  [PASS] HashTableChaining: Collisions successfully resolved via linked chain
  [PASS] HashTableChaining: Key deletion updates bucket chain properly
  [PASS] HashTableChaining: Automatic rehashing expands bucket capacity
  [PASS] HashTableProbing: Insert and Retrieve key-value pair
  [PASS] HashTableProbing: Linear probe sequence resolves collisions
  [PASS] HashTableProbing: Tombstone marker permits continued probe search
  [PASS] HashTableProbing: Dynamic rehashing expands table array

==============================================================================
                          FINAL PL TEST RESULTS SUMMARY                       
==============================================================================
  Total Assertions Run : 53
  Passed Assertions    : 53 [OK]
  Failed Assertions    : 0
  Course Outcomes      : PL-CO1, PL-CO2, PL-CO3, PL-CO4, PL-CO5 (100% COVERED)
==============================================================================
[SUCCESS] ALL 53 AUTOMATED TESTS PASSED WITH ZERO ERRORS.
```

---

## 4. Viva Examination Preparation Guide

### Q1: Why not use `std::vector` or `std::list` in this laboratory implementation?
> **Answer**: The purpose of the Programming Laboratory course is to understand core foundational data structures and memory allocation mechanics from first principles. By manually creating dynamic arrays with pointers and handling deep copy semantics (Rule of Five), we demonstrate mastery over pointer arithmetic, heap allocation, and copy/move constructors that standard libraries abstract away.

### Q2: What is an Object Pool, and why is `BulletPool` used in AirStrike?
> **Answer**: In high-frequency 60 FPS action games, dynamically calling `new Bullet()` and `delete Bullet` every time a player fires triggers memory fragmentation and CPU allocation spikes. An Object Pool preallocates a contiguous array of objects upfront. Spawning marks an object as `active: true` in $O(1)$, and despawning resets `active: false` in $O(1)$, eliminating heap churn.

### Q3: Why is a Circular Linked List suited for the weapon selection wheel?
> **Answer**: Weapon selection is inherently cyclical. In a singly linked list, advancing past the final weapon requires checking `if (curr->next == nullptr) curr = head;`. In a Circular Linked List, the tail node's `next` pointer directly addresses the head node. Advancing to the next weapon is always an unconditional `active = active->next`, modeling an endless physical rotary switch with zero branching.

### Q4: Explain the difference between Separate Chaining and Linear Probing.
> **Answer**: 
> - **Separate Chaining**: An open-hashing scheme where each bucket points to a singly linked list. Collisions are accommodated by prepending nodes to the chain. The table can exceed 100% load factor, though search degrades to $O(N)$ without rehashing.
> - **Linear Probing**: A closed-hashing (open addressing) scheme where all key-value entries reside directly in a contiguous array. When collisions occur, subsequent adjacent slots are probed sequentially ($index = (hash + i) \pmod M$). It requires maintaining "Tombstone" markers upon deletion to preserve probe continuity for previously inserted collided keys.

### Q5: What makes Quick Sort faster than Merge Sort in practice despite worse worst-case complexity?
> **Answer**: Quick Sort has superior cache locality because it operates completely in-place within contiguous memory blocks, avoiding external auxiliary buffers. Merge Sort requires $O(N)$ extra memory allocation to merge sub-arrays, leading to heap allocation and cache misses on large data sets, even though its theoretical worst-case is strictly guaranteed at $O(N \log N)$.

---

## 5. Build and Execution Instructions

### Direct Compilation via Scripts:
- **Windows Command Prompt / Batch**:
  ```cmd
  scripts\build_pl.bat
  ```
- **PowerShell**:
  ```powershell
  .\scripts\build_pl.ps1
  ```
- **Master Project Build**:
  ```cmd
  build_all.bat
  ```

### Running the Application:
- **Interactive Menu Suite**:
  ```cmd
  bin\pl\pl_test_runner.exe
  ```
  *or through master launcher:*
  ```cmd
  run_airstrike.bat pl
  ```
- **Headless Automated Verification**:
  ```cmd
  bin\pl\pl_test_runner.exe --test
  ```
