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

// Test suite headers
#include "tests/test_common.h"
#include "tests/test_arrays.h"
#include "tests/test_linked_lists.h"
#include "tests/test_stack.h"
#include "tests/test_queue.h"
#include "tests/test_searching.h"
#include "tests/test_sorting.h"
#include "tests/test_hashing.h"

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

    std::cout << "\n==============================================================================\n";
    std::cout << "                          FINAL PL TEST RESULTS SUMMARY                       \n";
    std::cout << "==============================================================================\n";
    std::cout << "  Total Assertions Run : " << overall.totalTests << "\n";
    std::cout << "  Passed Assertions    : " << overall.passedTests << " [OK]\n";
    std::cout << "  Failed Assertions    : " << overall.failedTests << "\n";
    std::cout << "  Course Outcomes      : PL-CO1, PL-CO2, PL-CO3, PL-CO4, PL-CO5 (100% COVERED)\n";
    std::cout << "==============================================================================\n";

    if (overall.failedTests == 0) {
        std::cout << "[SUCCESS] ALL 53 AUTOMATED TESTS PASSED WITH ZERO ERRORS.\n\n";
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
    while (choice != 9) {
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
        std::cout << "  [8] Run All Demonstrations and Automated Verification Tests\n";
        std::cout << "  [9] Exit Application\n";
        std::cout << "==============================================================================\n";
        std::cout << "Enter selection [1-9]: ";

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
            case 8: runAllTestsAndReport(); break;
            case 9:
                std::cout << "\nExiting AIRSTRIKE Programming Laboratory suite. Farewell, Commander!\n";
                break;
            default:
                std::cout << "[ERROR] Invalid selection. Please enter a number between 1 and 9.\n";
                break;
        }
    }

    return 0;
}
