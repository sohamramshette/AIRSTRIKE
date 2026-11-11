#include <iostream>
#include <iomanip>
#include <string>
#include <cstring>

// Core PL headers
#include "src/arrays/array_ops.h"
#include "src/arrays/dynamic_array.h"
#include "src/arrays/bullet_pool.h"
#include "src/arrays/collision_grid.h"
#include "src/linked_lists/singly_linked_list.h"
#include "src/linked_lists/doubly_linked_list.h"
#include "src/linked_lists/circular_linked_list.h"
#include "src/stacks/action_stack.h"
#include "src/queues/wave_queue.h"
#include "src/searching/search_algorithms.h"
#include "src/sorting/sort_algorithms.h"
#include "src/hashing/hash_table_chaining.h"
#include "src/hashing/hash_table_probing.h"

// PL Enemy headers
#include "src/arrays/grid_swarm_enemy.h"
#include "src/linked_lists/formation_squadron.h"
#include "src/queues/echelon_dispatcher.h"
#include "src/searching/hunter_targeting.h"
#include "src/hashing/hash_shield_controller.h"

// PL Multiplayer headers
#include "src/multiplayer/multiplayer_demonstrator.h"

// Test suite headers
#include "tests/test_common.h"
#include "tests/test_arrays.h"
#include "tests/test_linked_lists.h"
#include "tests/test_stack.h"
#include "tests/test_queue.h"
#include "tests/test_searching.h"
#include "tests/test_sorting.h"
#include "tests/test_hashing.h"
#include "tests/test_pl_enemies.h"
#include "tests/test_multiplayer.h"

// ==========================================
// Automated Test Runner Function
// ==========================================
int runAllTestsAndReport() {
    std::cout << "\n==============================================================================\n";
    std::cout << "        AIRSTRIKE PROGRAMMING LABORATORY - AUTOMATED VERIFICATION SUITE       \n";
    std::cout << "        Evaluating Course Outcomes: PL-CO1 to PL-CO5 (Standard C++17)         \n";
    std::cout << "==============================================================================\n";

    TestSummary overall;
    overall.merge(runArrayTests());
    overall.merge(runLinkedListTests());
    overall.merge(runStackTests());
    overall.merge(runQueueTests());
    overall.merge(runSearchingTests());
    overall.merge(runSortingTests());
    overall.merge(runHashingTests());
    overall.merge(runPLEnemyTests());
    overall.merge(runMultiplayerTests());

    std::cout << "\n==============================================================================\n";
    std::cout << "                          FINAL PL TEST RESULTS SUMMARY                       \n";
    std::cout << "==============================================================================\n";
    std::cout << "  Total Assertions Run : " << overall.totalTests << "\n";
    std::cout << "  Passed Assertions    : " << overall.passedTests << " [OK]\n";
    std::cout << "  Failed Assertions    : " << overall.failedTests << "\n";
    std::cout << "  Course Outcomes      : PL-CO1, PL-CO2, PL-CO3, PL-CO4, PL-CO5 (100% COVERED)\n";
    std::cout << "==============================================================================\n";

    if (overall.failedTests == 0) {
        std::cout << "[SUCCESS] ALL " << overall.totalTests << " AUTOMATED TESTS PASSED WITH ZERO ERRORS.\n\n";
        return 0;
    } else {
        std::cerr << "[FAILURE] " << overall.failedTests << " TEST(S) FAILED.\n\n";
        return 1;
    }
}

// ==========================================
// Submenu Demonstrations
// ==========================================
void demoArrays() {
    int choice = 0;
    while (choice != 6) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "           PL-CO1: ARRAYS & MEMORY DEMONSTRATION SUBMENU      \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] 1D Array Static Operations (Insert, Delete, Search, Traverse)\n";
        std::cout << "  [2] 2D Radar Matrix (Spatial Coordinate Search & Update)\n";
        std::cout << "  [3] Custom DynamicArray (Heap Allocation & Rule of Five)\n";
        std::cout << "  [4] AirStrike Fixed BulletPool (Zero-allocation Object Pool)\n";
        std::cout << "  [5] 2D Asteroid Collision Grid (Broadphase Spatial Partitioning)\n";
        std::cout << "  [6] Return to Main Menu\n";
        std::cout << "Enter selection [1-6]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: {
                int arr[10] = {10, 25, 40, 55, 70};
                int size = 5;
                ArrayOps::printArray(arr, size, "Initial Telemetry Array");
                ArrayOps::insertAt(arr, size, 10, 2, 99);
                ArrayOps::printArray(arr, size, "After Inserting 99 at Index 2");
                ArrayOps::deleteAt(arr, size, 1);
                ArrayOps::printArray(arr, size, "After Deleting Element at Index 1");
                int idx = ArrayOps::searchLinear(arr, size, 70);
                std::cout << "Search target 70 found at index: " << idx << "\n";
                break;
            }
            case 2: {
                int grid[ArrayOps::GRID_ROWS][ArrayOps::GRID_COLS] = {{0}};
                grid[1][2] = 101; // Enemy fighter
                grid[3][0] = 202; // Asteroid
                grid[0][3] = 999; // Boss dreadnought
                ArrayOps::printGrid(grid, "AirStrike Sector Radar Matrix [4x4]");
                int r, c;
                if (ArrayOps::searchGrid(grid, 999, r, c)) {
                    std::cout << "Boss signature 999 located at Sector (Row " << r << ", Col " << c << ")\n";
                }
                break;
            }
            case 3: {
                DynamicArray dArr(3);
                std::cout << "Initial DynamicArray capacity: " << dArr.getCapacity() << "\n";
                dArr.pushBack(100);
                dArr.pushBack(200);
                dArr.pushBack(300);
                dArr.print("Filled Initial Capacity");
                std::cout << "Pushing 4th element to trigger geometric capacity doubling...\n";
                dArr.pushBack(400);
                dArr.print("After Capacity Expansion");
                std::cout << "Current capacity: " << dArr.getCapacity() << " (Doubled)\n";
                break;
            }
            case 4: {
                BulletPool pool;
                std::cout << "Spawning 4 combat projectiles...\n";
                pool.spawn(400.0, 550.0, 0.0, -500.0, 30);
                pool.spawn(420.0, 550.0, 50.0, -500.0, 30);
                pool.spawn(380.0, 550.0, -50.0, -500.0, 30);
                pool.spawn(400.0, 10.0, 0.0, -500.0, 30); // Near boundary
                pool.printStatus();
                std::cout << "\nAdvancing physics simulation by dt = 0.05 seconds...\n";
                pool.update(0.05);
                pool.printStatus();
                break;
            }
            case 5: {
                CollisionGrid cGrid;
                cGrid.insertAsteroid(1, 150.0, 100.0, 25.0);
                cGrid.insertAsteroid(2, 550.0, 450.0, 30.0);
                cGrid.insertAsteroid(3, 160.0, 110.0, 15.0);
                cGrid.insertAsteroid(4, 720.0, 50.0, 20.0);
                cGrid.printRadarGrid();

                int hitId = -1;
                bool hit = cGrid.testCollision(155.0, 105.0, 10.0, hitId);
                std::cout << "Broadphase Spatial Collision Query at (155, 105): "
                          << (hit ? "COLLISION DETECTED with Asteroid ID #" + std::to_string(hitId) : "CLEAR") << "\n";
                break;
            }
            case 6:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
    }
}

void demoLinkedLists() {
    int choice = 0;
    while (choice != 4) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "        PL-CO2: LINKED LISTS DEMONSTRATION SUBMENU            \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] Singly Linked List (Mission Flight Event Log)\n";
        std::cout << "  [2] Doubly Linked List (Enemy Squadron Bidirectional Formation)\n";
        std::cout << "  [3] Circular Linked List (Weapon Selection Ring)\n";
        std::cout << "  [4] Return to Main Menu\n";
        std::cout << "Enter selection [1-4]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: {
                SinglyLinkedList log;
                log.insertAtTail(1, 0.0, "Mission Launch - Engines Nominal");
                log.insertAtTail(2, 4.2, "Wave 1 Inbound - 5 Drone Interceptors");
                log.insertAtTail(3, 9.8, "Player Hull Hit - Shield Absorbed 40 Dmg");
                log.insertAtTail(4, 15.1, "Wave 1 Cleared - High Score +500");
                log.traverse();
                std::cout << "\nDeleting Event #2...\n";
                log.deleteById(2);
                log.traverse();
                break;
            }
            case 2: {
                DoublyLinkedList fleet;
                fleet.insertTail(101, "DRONE_SCOUT", 40);
                fleet.insertTail(102, "FIGHTER_INTERCEPTOR", 100);
                fleet.insertTail(103, "CRUISER_ESCORT", 250);
                fleet.insertTail(104, "BOSS_FLAGSHIP", 800);
                fleet.traverseForward();
                fleet.traverseBackward();
                std::cout << "\nCruiser #103 destroyed in combat. Removing node O(1)...\n";
                fleet.deleteById(103);
                fleet.traverseForward();
                break;
            }
            case 3: {
                CircularLinkedList wheel;
                wheel.insertTail(1, "Plasma Cannon", 5, 25);
                wheel.insertTail(2, "Laser Beam", 12, 60);
                wheel.insertTail(3, "Photon Torpedo", 25, 150);
                wheel.insertTail(4, "EMP Pulse Wave", 40, 220);
                wheel.traverse();

                std::cout << "\nCycling through weapons in circular loop:\n";
                for (int i = 0; i < 6; ++i) {
                    const WeaponNode* cur = wheel.cycleNext();
                    std::cout << "Cycle Step " << (i + 1) << " -> Active: " << cur->name
                              << " (Dmg: " << cur->damage << ", Energy: " << cur->energyCost << ")\n";
                }
                break;
            }
            case 4:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
    }
}

void demoStack() {
    int choice = 0;
    ActionStack stack(6);
    stack.push(CombatAction(1, "KILL", 100, 0, "Destroyed Drone #1"));
    stack.push(CombatAction(2, "DAMAGE", 0, -25, "Hit by Laser Fire"));
    stack.push(CombatAction(3, "POWERUP", 50, +25, "Shield Recharge"));

    while (choice != 6) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "        PL-CO3: STACK (COMBAT HISTORY & UNDO) SUBMENU         \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] Push New Combat Action\n";
        std::cout << "  [2] Pop / Undo Last Combat Action (LIFO)\n";
        std::cout << "  [3] Peek Top Action\n";
        std::cout << "  [4] Display Full Action Stack\n";
        std::cout << "  [5] Trigger Stack Overflow & Underflow Demonstration\n";
        std::cout << "  [6] Return to Main Menu\n";
        std::cout << "Enter selection [1-6]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: {
                int id = stack.size() + 1;
                std::cout << "Pushing Action ID " << id << "...\n";
                stack.push(CombatAction(id, "BOMB_FIRED", 250, 0, "Smart Bomb Cleared Screen"));
                stack.printStack();
                break;
            }
            case 2: {
                CombatAction act;
                if (stack.pop(act)) {
                    std::cout << "Undid Action #" << act.actionId << " [" << act.type
                              << "]: " << act.detail << " (Score Delta: " << act.scoreDelta << ")\n";
                }
                break;
            }
            case 3: {
                CombatAction act;
                if (stack.peek(act)) {
                    std::cout << "Current Top Action #" << act.actionId << ": " << act.detail << "\n";
                } else {
                    std::cout << "Stack is empty.\n";
                }
                break;
            }
            case 4:
                stack.printStack();
                break;
            case 5: {
                std::cout << "\nTesting Stack Overflow by filling capacity (" << stack.capacity() << ")...\n";
                while (!stack.isFull()) {
                    stack.push(CombatAction(99, "SIMULATED", 0, 0, "Fill Slot"));
                }
                stack.push(CombatAction(100, "OVERFLOW", 0, 0, "Should Trigger Error"));
                std::cout << "\nTesting Stack Underflow by emptying all elements...\n";
                CombatAction dummy;
                while (!stack.isEmpty()) {
                    stack.pop(dummy);
                }
                stack.pop(dummy); // Underflow
                break;
            }
            case 6:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
    }
}

void demoQueue() {
    int choice = 0;
    WaveQueue queue(5);
    queue.enqueue(EnemyWave(1, "Scout Incursion", 5, 2.0, 1.0));
    queue.enqueue(EnemyWave(2, "Fighter Vanguard", 8, 1.5, 1.25));
    queue.enqueue(EnemyWave(3, "Asteroid Swarm", 12, 1.0, 1.5));

    while (choice != 5) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "        PL-CO3: QUEUE (ENEMY WAVE SPAWNER) SUBMENU            \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] Enqueue New Enemy Wave\n";
        std::cout << "  [2] Dequeue / Dispatch Wave (FIFO)\n";
        std::cout << "  [3] Inspect Next Wave at Front\n";
        std::cout << "  [4] Display Full Enemy Wave Queue\n";
        std::cout << "  [5] Return to Main Menu\n";
        std::cout << "Enter selection [1-5]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: {
                int waveNum = queue.size() + 1;
                queue.enqueue(EnemyWave(waveNum, "Reinforcement Strike", 10, 1.2, 1.5));
                queue.printQueue();
                break;
            }
            case 2: {
                EnemyWave wave;
                if (queue.dequeue(wave)) {
                    std::cout << "Dispatched Wave #" << wave.waveNumber << " [" << wave.waveName
                              << "]: " << wave.enemyCount << " ships entering combat arena!\n";
                }
                break;
            }
            case 3: {
                EnemyWave wave;
                if (queue.front(wave)) {
                    std::cout << "Next Wave in Queue #" << wave.waveNumber << ": " << wave.waveName
                              << " (" << wave.enemyCount << " enemies)\n";
                } else {
                    std::cout << "Wave Queue is empty.\n";
                }
                break;
            }
            case 4:
                queue.printQueue();
                break;
            case 5:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
    }
}

void demoSearching() {
    int choice = 0;
    const int telemetryScores[] = {120, 340, 560, 780, 1050, 1420, 2100, 3500, 4800, 6200};
    const int n = sizeof(telemetryScores) / sizeof(telemetryScores[0]);

    while (choice != 5) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "        PL-CO4: SEARCHING ALGORITHMS SUBMENU                  \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] Linear Search Demo (Comparisons Tracking)\n";
        std::cout << "  [2] Binary Search Demo (Logarithmic Comparison Tracking)\n";
        std::cout << "  [3] Search AirStrike Pilot Leaderboard\n";
        std::cout << "  [4] Side-by-Side Performance Comparison (Linear vs Binary)\n";
        std::cout << "  [5] Return to Main Menu\n";
        std::cout << "Enter selection [1-5]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: {
                int target;
                std::cout << "Enter score to search linearly (e.g. 3500): ";
                if (std::cin >> target) {
                    SearchMetrics m = SearchAlgorithms::linearSearch(telemetryScores, n, target);
                    std::cout << "Result: " << (m.found ? "FOUND at index " + std::to_string(m.index) : "NOT FOUND") << "\n";
                    std::cout << "Comparisons performed: " << m.comparisons << " (O(N) Complexity)\n";
                    std::cout << "Time elapsed: " << m.elapsedMicroseconds << " microseconds\n";
                }
                break;
            }
            case 2: {
                int target;
                std::cout << "Enter score to search logarithmically with Binary Search (e.g. 3500): ";
                if (std::cin >> target) {
                    SearchMetrics m = SearchAlgorithms::binarySearch(telemetryScores, n, target);
                    std::cout << "Result: " << (m.found ? "FOUND at index " + std::to_string(m.index) : "NOT FOUND") << "\n";
                    std::cout << "Comparisons performed: " << m.comparisons << " (O(log N) Complexity)\n";
                    std::cout << "Time elapsed: " << m.elapsedMicroseconds << " microseconds\n";
                }
                break;
            }
            case 3: {
                PlayerScore lb[] = {
                    PlayerScore("NOVA_PILOT", 500, 2),
                    PlayerScore("VIPER_LEAD", 1200, 4),
                    PlayerScore("SOLAR_HAWK", 2400, 6),
                    PlayerScore("STAR_HUNTER", 4500, 9),
                    PlayerScore("COSMIC_ACE", 7800, 12)
                };
                SearchAlgorithms::printScores(lb, 5, "Official AirStrike Leaderboard");
                SearchMetrics m = SearchAlgorithms::binarySearchScores(lb, 5, 2400);
                std::cout << "Searching for Score 2400: Found Pilot '" << lb[m.index].pilotName
                          << "' in " << m.comparisons << " comparisons.\n";
                break;
            }
            case 4: {
                std::cout << "\nComparing Linear vs Binary Search on score 4800:\n";
                SearchMetrics mLin = SearchAlgorithms::linearSearch(telemetryScores, n, 4800);
                SearchMetrics mBin = SearchAlgorithms::binarySearch(telemetryScores, n, 4800);
                std::cout << "Linear Search: " << mLin.comparisons << " comparisons | "
                          << mLin.elapsedMicroseconds << " us\n";
                std::cout << "Binary Search: " << mBin.comparisons << " comparisons | "
                          << mBin.elapsedMicroseconds << " us\n";
                break;
            }
            case 5:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
    }
}

void demoSorting() {
    int choice = 0;
    while (choice != 4) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "        PL-CO4: SORTING ALGORITHMS SUBMENU                    \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] Benchmark All 5 Sorting Algorithms on Unsorted Combat Data\n";
        std::cout << "  [2] Sort AirStrike Pilot High Scores (Descending Leaderboard)\n";
        std::cout << "  [3] Theoretical Time & Space Complexity Summary\n";
        std::cout << "  [4] Return to Main Menu\n";
        std::cout << "Enter selection [1-4]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: {
                const int testData[] = {450, 120, 890, 230, 670, 310, 950, 150, 780, 520, 840, 290};
                const int sz = sizeof(testData) / sizeof(testData[0]);
                SortMetrics results[5];

                int bArr[sz], sArr[sz], iArr[sz], mArr[sz], qArr[sz];
                for (int i = 0; i < sz; ++i) {
                    bArr[i] = sArr[i] = iArr[i] = mArr[i] = qArr[i] = testData[i];
                }

                results[0] = SortAlgorithms::bubbleSort(bArr, sz, true);
                results[1] = SortAlgorithms::selectionSort(sArr, sz, true);
                results[2] = SortAlgorithms::insertionSort(iArr, sz, true);
                results[3] = SortAlgorithms::mergeSort(mArr, sz, true);
                results[4] = SortAlgorithms::quickSort(qArr, sz, true);

                SortAlgorithms::printMetricsTable(results, 5);
                break;
            }
            case 2: {
                PlayerScore lb[] = {
                    PlayerScore("ROOKIE_1", 450, 2),
                    PlayerScore("TITAN_COMMANDER", 6800, 11),
                    PlayerScore("ACE_STRIKER", 3200, 7),
                    PlayerScore("GHOST_PILOT", 1850, 5),
                    PlayerScore("OMEGA_DESTROYER", 8400, 14),
                    PlayerScore("WINGMAN_BETA", 920, 3)
                };
                int lbSize = 6;
                SearchAlgorithms::printScores(lb, lbSize, "Raw Unsorted High Scores");
                SortAlgorithms::quickSortScores(lb, lbSize, true); // Descending
                SearchAlgorithms::printScores(lb, lbSize, "Sorted Leaderboard (Descending via Quick Sort)");
                break;
            }
            case 3:
                SortAlgorithms::printComplexitySummary();
                break;
            case 4:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
    }
}

void demoHashing() {
    int choice = 0;
    while (choice != 4) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "        PL-CO5: HASHING & COLLISION RESOLUTION SUBMENU        \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] Hash Table: Separate Chaining (Linked Bucket Inspection)\n";
        std::cout << "  [2] Hash Table: Linear Probing (Open Addressing & Tombstones)\n";
        std::cout << "  [3] Dynamic Rehashing Demonstration (Load Factor > Threshold)\n";
        std::cout << "  [4] Return to Main Menu\n";
        std::cout << "Enter selection [1-4]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: {
                HashTableChaining table(5);
                table.insert("WEAPON_PLASMA",  InventoryItem("W01", "Plasma Cannon Mk-I", "COMMON", 25, 100));
                table.insert("WEAPON_LASER",   InventoryItem("W02", "Continuous Laser",    "RARE",   50, 300));
                table.insert("SHIELD_NANO",    InventoryItem("S01", "Nano Mesh Plating",   "RARE",   15, 250));
                table.insert("ENGINE_WARP",    InventoryItem("E01", "Sub-light Thrusters", "EPIC",   30, 600));
                table.insert("MODULE_OVERLOAD",InventoryItem("M01", "Reactor Supercharger","LEGENDARY", 80, 1200));

                table.printTable();
                InventoryItem found;
                if (table.search("WEAPON_LASER", found)) {
                    std::cout << "\nSearch Result for 'WEAPON_LASER': " << found.name
                              << " [Bonus +" << found.attackBonus << ", Val: " << found.creditValue << " credits]\n";
                }
                break;
            }
            case 2: {
                HashTableProbing probeTable(7);
                probeTable.insert("WEAPON_VULCAN", InventoryItem("W03", "Rotary Vulcan Gun", "COMMON", 20, 80));
                probeTable.insert("WEAPON_MISSILE",InventoryItem("W04", "Heat-Seeking Pod",  "RARE",   45, 220));
                probeTable.insert("SHIELD_DEFLECT",InventoryItem("S02", "Deflector Field",   "EPIC",   35, 450));
                probeTable.printTable();

                std::cout << "\nDeleting 'WEAPON_MISSILE' to create a Tombstone marker...\n";
                probeTable.remove("WEAPON_MISSILE");
                probeTable.printTable();

                std::cout << "Searching for 'SHIELD_DEFLECT' through the tombstone...\n";
                InventoryItem it;
                int steps = 0;
                if (probeTable.search("SHIELD_DEFLECT", it, steps)) {
                    std::cout << "Found '" << it.name << "' after " << steps << " probe steps.\n";
                }
                break;
            }
            case 3: {
                HashTableChaining rehashingTable(3);
                std::cout << "Initial bucket count: " << rehashingTable.getBucketCount() << "\n";
                std::cout << "Inserting 6 items to exceed load factor threshold 0.75...\n";
                rehashingTable.insert("ITEM_A", InventoryItem("A", "Alpha", "COMMON", 1, 10));
                rehashingTable.insert("ITEM_B", InventoryItem("B", "Beta", "COMMON", 2, 20));
                rehashingTable.insert("ITEM_C", InventoryItem("C", "Gamma", "RARE", 3, 30));
                rehashingTable.insert("ITEM_D", InventoryItem("D", "Delta", "EPIC", 4, 40));
                rehashingTable.insert("ITEM_E", InventoryItem("E", "Epsilon", "LEGENDARY", 5, 50));
                rehashingTable.printTable();
                break;
            }
            case 4:
                break;
            default:
                std::cout << "Invalid choice.\n";
        }
    }
}

// ==========================================
// PL Enemy Submenu Demonstrations (Phase 8.8)
// ==========================================
void demoSpatialGridSwarm() {
    std::cout << "\n==============================================================================\n";
    std::cout << "        PL-CO1: SPATIAL GRID SWARM DEMONSTRATION (2D SPATIAL PARTITIONING)    \n";
    std::cout << "==============================================================================\n";
    std::cout << "  Syllabus Concept: 2D Arrays, Row-Major Addressing, Broadphase Partitioning\n";
    std::cout << "  Row-Major Formula: Offset(i, j) = (i * COLS + j) * sizeof(Element)\n";
    std::cout << "  Arena Dimensions : " << GridSwarmEnemy::ARENA_WIDTH << " x " << GridSwarmEnemy::ARENA_HEIGHT
              << " px partitioned into [" << GridSwarmEnemy::GRID_ROWS << "x" << GridSwarmEnemy::GRID_COLS << "] Sectors\n";
    std::cout << "------------------------------------------------------------------------------\n";

    GridSwarmEnemy swarm;
    std::cout << "1. Deploying Swarm Interceptors into Sector Coordinates:\n";
    swarm.spawnDrone(101, 50.0, 40.0, 25);   // Sector [0, 0]
    swarm.spawnDrone(102, 80.0, 70.0, 25);   // Sector [0, 0]
    swarm.spawnDrone(103, 150.0, 50.0, 30);  // Sector [0, 1]
    swarm.spawnDrone(104, 420.0, 310.0, 50); // Sector [4, 4] (Center)
    swarm.spawnDrone(105, 750.0, 550.0, 80); // Sector [7, 7] (Perimeter)
    std::cout << "   Active Drones Spawned: " << swarm.getTotalActiveDrones() << "\n\n";

    std::cout << "2. Row-Major Contiguous Memory Offset Calculations:\n";
    std::cout << "   Sector [0, 0] -> Row-Major Offset = " << GridSwarmEnemy::calculateRowMajorOffset(0, 0) << " elements\n";
    std::cout << "   Sector [0, 1] -> Row-Major Offset = " << GridSwarmEnemy::calculateRowMajorOffset(0, 1) << " elements\n";
    std::cout << "   Sector [4, 4] -> Row-Major Offset = " << GridSwarmEnemy::calculateRowMajorOffset(4, 4) << " elements\n";
    std::cout << "   Sector [7, 7] -> Row-Major Offset = " << GridSwarmEnemy::calculateRowMajorOffset(7, 7) << " elements (Max Index)\n\n";

    std::cout << "3. 2D Swarm Sector Occupancy Matrix:\n";
    swarm.printSwarmGrid();

    std::cout << "4. Broadphase 9-Sector Proximity Query:\n";
    std::cout << "   Query Point: (70.0, 60.0), Search Radius: 120.0 px (Sectors [0,0], [0,1], [1,0], [1,1])\n";
    int nearbyIds[16];
    int count = swarm.findNearbyDrones(70.0, 60.0, 120.0, nearbyIds, 16);
    std::cout << "   Nearby Interceptors Located: " << count << " units [";
    for (int i = 0; i < count; ++i) {
        std::cout << "ID #" << nearbyIds[i] << (i + 1 < count ? ", " : "");
    }
    std::cout << "]\n\n";

    std::cout << "5. Dynamic Kinematics & Scatter Mine Fragmentation:\n";
    std::cout << "   Deploying 4 fragmentation scatter mines around Center (400, 300)...\n";
    int mineIds[8];
    int deployed = swarm.disperseScatterMines(400.0, 300.0, 4, mineIds);
    std::cout << "   Mines Deployed: " << deployed << " mine fragments into adjacent cells.\n";
    std::cout << "   Updated Swarm Active Units: " << swarm.getTotalActiveDrones() << "\n";
    swarm.printSwarmGrid();
}

void demoFormationSquadronChain() {
    std::cout << "\n==============================================================================\n";
    std::cout << "     PL-CO2: FORMATION SQUADRON CHAIN DEMONSTRATION (DOUBLY LINKED FLEET)     \n";
    std::cout << "==============================================================================\n";
    std::cout << "  Syllabus Concept: Doubly Linked Lists, Pointer Rewiring, O(1) Splitting\n";
    std::cout << "  Node Structure  : Prev pointer <-> [ Ship ID, Callsign, HP, Pos ] <-> Next pointer\n";
    std::cout << "------------------------------------------------------------------------------\n";

    FormationSquadron alphaSquad("Viper-Strike-Wing");
    std::cout << "1. Assembling Tactical Fleet via Head and Tail Insertions:\n";
    alphaSquad.addVanguard(1, "Vanguard-Prime", 250, 400.0, 500.0);
    alphaSquad.addRearguard(4, "Rear-Anchor", 350, 400.0, 200.0);
    alphaSquad.insertAtRank(1, 2, "Escort-Alpha", 120, 360.0, 400.0);
    alphaSquad.insertAtRank(2, 3, "Escort-Beta", 120, 440.0, 400.0);
    alphaSquad.insertAtRank(3, 5, "Tactical-Drone", 80, 400.0, 300.0);

    std::cout << "   Total ships in formation: " << alphaSquad.size() << "\n\n";

    std::cout << "2. Bidirectional Traversal Verification:\n";
    std::cout << "   [Forward: Vanguard -> Rearguard]:\n";
    alphaSquad.traverseVanguardToRear();
    std::cout << "   [Reverse: Rearguard -> Vanguard]:\n";
    alphaSquad.traverseRearToVanguard();

    std::cout << "\n3. Combat Hull Depletion & O(1) Node Removal:\n";
    std::cout << "   Cruiser #3 ('Escort-Beta') suffered critical hull breach. Unlinking node...\n";
    alphaSquad.removeShip(3);
    std::cout << "   Post-destruction fleet chain (Size: " << alphaSquad.size() << "):\n";
    alphaSquad.traverseVanguardToRear();

    std::cout << "\n4. O(1) Tactical Formation Splitting:\n";
    std::cout << "   Enemy EMP detected! Splitting formation after ship ID 2 ('Escort-Alpha')...\n";
    FormationSquadron betaSquad("Cobra-Flank-Wing");
    bool splitOk = alphaSquad.splitAt(2, betaSquad);
    std::cout << "   Split Execution: " << (splitOk ? "SUCCESSFUL (O(1) pointer cut)" : "FAILED") << "\n\n";

    std::cout << "   First Squadron  ['" << alphaSquad.getName() << "'] (Size " << alphaSquad.size() << "):\n";
    alphaSquad.traverseVanguardToRear();

    std::cout << "   Second Squadron ['" << betaSquad.getName() << "'] (Size " << betaSquad.size() << "):\n";
    betaSquad.traverseVanguardToRear();
}

void demoQueueWaveEchelon() {
    std::cout << "\n==============================================================================\n";
    std::cout << "        PL-CO3: QUEUE WAVE ECHELON DEMONSTRATION (CIRCULAR FIFO DISPATCHER)   \n";
    std::cout << "==============================================================================\n";
    std::cout << "  Syllabus Concept: Circular Queue Array, FIFO Dispatch, Index Wraparound\n";
    std::cout << "  Pointer Formula : rear = (rear + 1) % capacity | front = (front + 1) % capacity\n";
    std::cout << "  Boundary Checks : Overflow on count == capacity | Underflow on count == 0\n";
    std::cout << "------------------------------------------------------------------------------\n";

    QueueWaveEchelon queue(4); // Small capacity 4 for clear demonstration
    std::cout << "1. Initial Circular Echelon Queue State (Capacity: " << queue.capacity() << "):\n";
    queue.printEchelonQueue();

    std::cout << "2. Enqueueing 3 Tactical Echelons:\n";
    queue.enqueue(EchelonWaveUnit(1, "Scout-Incursion", 4, 2.5, 0.5));
    queue.enqueue(EchelonWaveUnit(2, "Heavy-Bombardment", 8, 1.8, 1.2));
    queue.enqueue(EchelonWaveUnit(3, "Stealth-Interceptors", 6, 3.0, 0.8));
    queue.printEchelonQueue();

    std::cout << "3. Dequeueing 2 Echelons in Strict FIFO Order:\n";
    EchelonWaveUnit dispatched;
    while (queue.size() > 1) {
        if (queue.dequeue(dispatched)) {
            std::cout << "   -> DISPATCHED: Echelon #" << dispatched.echelonId << " ["
                      << dispatched.codename << "] with " << dispatched.craftCount
                      << " craft at " << dispatched.flightSpeed << "x speed\n";
        }
    }
    queue.printEchelonQueue();

    std::cout << "4. Inducing Circular Index Wraparound (rear < front):\n";
    std::cout << "   Enqueueing 3 more echelons into capacity 4 buffer...\n";
    queue.enqueue(EchelonWaveUnit(4, "Dreadnought-Escorts", 5, 1.5, 2.0));
    queue.enqueue(EchelonWaveUnit(5, "Hyperspace-Strike", 10, 2.2, 1.5));
    queue.enqueue(EchelonWaveUnit(6, "Orbital-Siege", 12, 1.0, 2.5));
    queue.printEchelonQueue();

    std::cout << "   Wraparound Status: " << (queue.isWrappedAround() ? "ACTIVE (rear < front)" : "INACTIVE") << "\n";
    std::cout << "   Front Index = " << queue.getFrontIndex() << " | Rear Index = " << queue.getRearIndex() << "\n\n";

    std::cout << "5. Enforcing Queue Overflow Boundary Protection:\n";
    bool overflowAttempt = queue.enqueue(EchelonWaveUnit(7, "Illegal-Overfill", 99, 9.9, 0.0));
    std::cout << "   Enqueue attempt when full (Capacity " << queue.capacity() << "): "
              << (overflowAttempt ? "UNEXPECTED SUCCESS" : "BLOCKED BY OVERFLOW GUARD [OK]") << "\n";
}

void demoTargetSeekingHunter() {
    std::cout << "\n==============================================================================\n";
    std::cout << "      PL-CO4: TARGET-SEEKING HUNTER DEMONSTRATION (SEARCHING & SORTING)       \n";
    std::cout << "==============================================================================\n";
    std::cout << "  Syllabus Concept: QuickSort vs MergeSort, Monotonic Precondition, O(log N) Binary Search\n";
    std::cout << "  Search Complexities: Linear O(N) comparisons vs Binary O(log N) comparisons\n";
    std::cout << "------------------------------------------------------------------------------\n";

    TargetSeekingHunter hunter(707);
    std::cout << "1. Populating Unsorted Tactical Threat Telemetry:\n";
    hunter.addTarget(101, 620, 80, 45.0);
    hunter.addTarget(102, 150, 40, 12.0);
    hunter.addTarget(103, 430, 95, 180.0); // Duplicate distance target A
    hunter.addTarget(104, 890, 70, 270.0);
    hunter.addTarget(105, 280, 60, 90.0);
    hunter.addTarget(106, 430, 90, 185.0); // Duplicate distance target B
    hunter.addTarget(107, 95, 30, 5.0);
    hunter.addTarget(108, 510, 85, 315.0);

    hunter.printTelemetry();

    std::cout << "\n2. Precondition Validation Before Search:\n";
    std::cout << "   Is telemetry array sorted ascending? "
              << (hunter.isTelemetrySorted() ? "YES" : "NO [Binary Search precondition violated!]") << "\n";

    std::cout << "\n3. Executing QuickSort In-Place Partitioning:\n";
    long long comps = 0, swaps = 0;
    hunter.sortByDistanceQuickSort(comps, swaps);
    std::cout << "   QuickSort Completed: " << comps << " comparisons, " << swaps << " swaps performed.\n";
    std::cout << "   Is telemetry array sorted ascending now? "
              << (hunter.isTelemetrySorted() ? "YES [Precondition satisfied!]" : "NO") << "\n\n";
    hunter.printTelemetry();

    std::cout << "\n4. Performance Comparison: Linear Search vs Binary Search on Distance 430:\n";
    HunterSearchMetrics lin = hunter.linearSearchTarget(430);
    HunterSearchMetrics bin = hunter.binarySearchTarget(430);
    std::cout << "   Linear Search : Found=" << (lin.found ? "YES" : "NO") << " at index " << lin.targetIndex
              << " in " << lin.comparisons << " comparisons [O(N)]\n";
    std::cout << "   Binary Search : Found=" << (bin.found ? "YES" : "NO") << " at index " << bin.targetIndex
              << " in " << bin.comparisons << " comparisons [O(log N)]\n";

    std::cout << "\n5. Resolving Duplicate Threat Keys at Distance 430:\n";
    int dupIndices[8];
    int dupComps = 0;
    int dupCount = hunter.findAllTargetsAtDistance(430, dupIndices, 8, dupComps);
    std::cout << "   Total targets matching 430m: " << dupCount << " targets acquired in " << dupComps << " comparisons:\n";
    for (int i = 0; i < dupCount; ++i) {
        const TargetTelemetry* t = hunter.getTargetAt(dupIndices[i]);
        if (t) {
            std::cout << "   -> Target #" << t->targetId << " | Distance: " << t->threatDistance
                      << "m | Priority: " << t->priorityScore << " | Bearing: " << t->bearingDeg << " deg\n";
        }
    }

    std::cout << "\n6. Engaging Priority Target:\n";
    bool locked = hunter.engageTargetAtDistance(430);
    std::cout << "   Target lock established: " << (locked ? "LOCKED ON TARGET ID #" + std::to_string(hunter.getCurrentLockId()) : "FAILED") << "\n";
}

void demoHashShieldedMatrixCraft() {
    std::cout << "\n==============================================================================\n";
    std::cout << "    PL-CO5: HASH-SHIELDED MATRIX CRAFT (OPEN ADDRESSING & TOMBSTONE RECYCLING) \n";
    std::cout << "==============================================================================\n";
    std::cout << "  Syllabus Concept: Hash Table, Polynomial Rolling Hash, Linear Probing, Tombstones\n";
    std::cout << "  Hash Formula    : H(s) = sum(s[i] * p^i) % capacity (p = 31)\n";
    std::cout << "  Probe Sequence  : h_i(k) = (h(k) + i) % capacity\n";
    std::cout << "  Rehash Criterion: Load Factor > " << HashShieldController::MAX_LOAD_FACTOR << " (70%)\n";
    std::cout << "------------------------------------------------------------------------------\n";

    HashShieldController shield(7); // Prime capacity 7
    std::cout << "1. Initial Shield Matrix State (Capacity: " << shield.getCapacity() << "):\n";
    shield.printShieldMatrix();

    std::cout << "2. Activating Shield Harmonics (Simulating Collisions via Linear Probing):\n";
    shield.activateHarmonic("ALPHA_POLARIZER", 100, 80);
    shield.activateHarmonic("BETA_ABSORBER", 150, 70);
    shield.activateHarmonic("GAMMA_DEFLECTOR", 200, 90);
    shield.activateHarmonic("DELTA_REFLECTOR", 120, 60);
    shield.printShieldMatrix();

    std::cout << "3. O(1) Harmonic Frequency Lookup:\n";
    int barrier = 0, absorption = 0, probes = 0;
    if (shield.lookupHarmonic("GAMMA_DEFLECTOR", barrier, absorption, probes)) {
        std::cout << "   Lookup 'GAMMA_DEFLECTOR' -> Barrier: " << barrier << " HP | Absorption: "
                  << absorption << "% | Probing Steps: " << probes << "\n\n";
    }

    std::cout << "4. Tombstone Deletion Demonstration:\n";
    std::cout << "   Kinetic torpedo strikes 'BETA_ABSORBER'! Deactivating harmonic...\n";
    shield.deactivateHarmonic("BETA_ABSORBER");
    std::cout << "   Post-deactivation Matrix State (Tombstones: " << shield.getTombstoneCount() << "):\n";
    shield.printShieldMatrix();

    std::cout << "   Probing continuity verification through Tombstone:\n";
    if (shield.lookupHarmonic("DELTA_REFLECTOR", barrier, absorption, probes)) {
        std::cout << "   Lookup 'DELTA_REFLECTOR' through tombstone -> Barrier: " << barrier
                  << " HP | Probing Steps: " << probes << " [CHAIN PRESERVED]\n\n";
    }

    std::cout << "5. Dynamic Rehashing Demonstration (Load Factor > 0.70):\n";
    std::cout << "   Activating additional harmonic 'EPSILON_INVERTER' to exceed load factor...\n";
    shield.activateHarmonic("EPSILON_INVERTER", 250, 95);
    std::cout << "   Post-Rehash Matrix State:\n";
    shield.printShieldMatrix();
    std::cout << "   New Capacity = " << shield.getCapacity() << " (Prime Expansion)\n";
    std::cout << "   New Load Factor = " << std::fixed << std::setprecision(2) << shield.getLoadFactor() << "\n";
}

void demoPLEnemies() {
    int choice = 0;
    while (choice != 6) {
        std::cout << "\n--------------------------------------------------------------\n";
        std::cout << "       PL ENEMY SYSTEMS & TACTICAL FLEET DEMONSTRATOR         \n";
        std::cout << "       Evaluating Course Outcomes: PL-CO1 to PL-CO5 (C++17)   \n";
        std::cout << "--------------------------------------------------------------\n";
        std::cout << "  [1] Spatial Grid Swarm (PL-CO1: 2D Spatial Partitioning & Row-Major)\n";
        std::cout << "  [2] Formation Squadron Chain (PL-CO2: Doubly-Linked Fleet & O(1) Split)\n";
        std::cout << "  [3] Queue Wave Echelon (PL-CO3: Circular Queue & FIFO Dispatch)\n";
        std::cout << "  [4] Target-Seeking Hunter (PL-CO4: QuickSort & O(log N) Binary Search)\n";
        std::cout << "  [5] Hash-Shielded Matrix Craft (PL-CO5: Open Addressing & Tombstones)\n";
        std::cout << "  [6] Return to Main Menu\n";
        std::cout << "Enter selection [1-6]: ";
        if (!(std::cin >> choice)) { std::cin.clear(); std::string s; std::cin >> s; continue; }

        switch (choice) {
            case 1: demoSpatialGridSwarm(); break;
            case 2: demoFormationSquadronChain(); break;
            case 3: demoQueueWaveEchelon(); break;
            case 4: demoTargetSeekingHunter(); break;
            case 5: demoHashShieldedMatrixCraft(); break;
            case 6: break;
            default:
                std::cout << "Invalid choice. Please select [1-6].\n";
                break;
        }
    }
}

// ==========================================
// Main Function & CLI Dispatcher
// ==========================================
int main(int argc, char* argv[]) {
    // Check if headless test execution requested via command-line arguments
    if (argc > 1) {
        for (int i = 1; i < argc; ++i) {
            if (std::strcmp(argv[i], "--test") == 0 || std::strcmp(argv[i], "-t") == 0 ||
                std::strcmp(argv[i], "--all") == 0) {
                return runAllTestsAndReport();
            }
        }
    }

    int choice = 0;
    while (choice != 11) {
        std::cout << "\n==============================================================================\n";
        std::cout << "             AIRSTRIKE - PROGRAMMING LABORATORY (PL) IN C++17                 \n";
        std::cout << "             Data Structures & Algorithms Verification System                 \n";
        std::cout << "==============================================================================\n";
        std::cout << "  [1] PL-CO1: Arrays (1D/2D, DynamicArray, BulletPool, Collision Grid)\n";
        std::cout << "  [2] PL-CO2: Linked Lists (Singly, Doubly, Circular Linked Lists)\n";
        std::cout << "  [3] PL-CO3: Stack Applications (Combat Action History / Undo & LIFO)\n";
        std::cout << "  [4] PL-CO3: Queue Applications (Enemy Wave Spawner & FIFO)\n";
        std::cout << "  [5] PL-CO4: Searching Algorithms (Linear vs Binary Search & Metrics)\n";
        std::cout << "  [6] PL-CO4: Sorting Algorithms (5 Canonical Sorts & Leaderboard)\n";
        std::cout << "  [7] PL-CO5: Hashing Techniques (Chaining, Linear Probing, Rehashing)\n";
        std::cout << "  [8] PL Enemy Systems & Tactical Fleet Demonstrator (PL-CO1 to PL-CO5)\n";
        std::cout << "  [9] PL-CO1 to PL-CO5 Multiplayer Match Processing Demonstrator\n";
        std::cout << "  [10] Run All Demonstrations and Automated Verification Tests\n";
        std::cout << "  [11] Exit Application\n";
        std::cout << "==============================================================================\n";
        std::cout << "Enter selection [1-11]: ";

        if (!(std::cin >> choice)) {
            std::cin.clear();
            std::string discard;
            std::cin >> discard;
            continue;
        }

        switch (choice) {
            case 1: demoArrays(); break;
            case 2: demoLinkedLists(); break;
            case 3: demoStack(); break;
            case 4: demoQueue(); break;
            case 5: demoSearching(); break;
            case 6: demoSorting(); break;
            case 7: demoHashing(); break;
            case 8: demoPLEnemies(); break;
            case 9: Airstrike::Multiplayer::MultiplayerDemonstrator::runAllDemonstrations(); break;
            case 10: runAllTestsAndReport(); break;
            case 11:
                std::cout << "\nExiting AIRSTRIKE Programming Laboratory suite. Farewell, Commander!\n";
                break;
            default:
                std::cout << "[ERROR] Invalid selection. Please enter a number between 1 and 11.\n";
                break;
        }
    }

    return 0;
}

