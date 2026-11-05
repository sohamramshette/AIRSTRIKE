#include "test_arrays.h"
#include "../src/arrays/array_ops.h"
#include "../src/arrays/dynamic_array.h"
#include "../src/arrays/bullet_pool.h"
#include "../src/arrays/collision_grid.h"

TestSummary runArrayTests() {
    TestSummary summary;
    std::cout << "\n======================================================\n";
    std::cout << "[TEST SUITE] PL-CO1: Arrays & Dynamic Memory (10 Tests)\n";
    std::cout << "======================================================\n";

    // 1. Static 1D Array Insert & Bounds Check
    int arr[10] = {10, 20, 30};
    int size = 3;
    bool insOk = ArrayOps::insertAt(arr, size, 10, 1, 15);
    summary.record(insOk && size == 4 && arr[1] == 15, "1D Array: insertAt valid index");

    // 2. Static 1D Array Delete & Search
    bool delOk = ArrayOps::deleteAt(arr, size, 1);
    int searchIdx = ArrayOps::searchLinear(arr, size, 30);
    summary.record(delOk && size == 3 && searchIdx == 2, "1D Array: deleteAt and searchLinear");

    // 3. Static 2D Array Radar Grid
    int grid[ArrayOps::GRID_ROWS][ArrayOps::GRID_COLS] = {{0}};
    ArrayOps::updateGrid(grid, 2, 3, 99);
    int r = -1, c = -1;
    bool gridFound = ArrayOps::searchGrid(grid, 99, r, c);
    summary.record(gridFound && r == 2 && c == 3, "2D Array: Grid update and coordinate search");

    // 4. Dynamic Array Geometric Expansion
    DynamicArray dArr(2);
    dArr.pushBack(100);
    dArr.pushBack(200);
    dArr.pushBack(300); // Triggers resize
    summary.record(dArr.size() == 3 && dArr.getCapacity() >= 4 && dArr.get(2) == 300,
                   "DynamicArray: Push back and capacity doubling");

    // 5. Dynamic Array Insert & Remove
    dArr.insert(1, 150);
    bool rmOk = dArr.removeAt(1);
    summary.record(rmOk && dArr.size() == 3 && dArr.get(1) == 200, "DynamicArray: Insert and removeAt");

    // 6. Dynamic Array Deep Copy Rule of Five
    DynamicArray copyArr = dArr;
    copyArr.set(0, 999);
    summary.record(dArr.get(0) == 100 && copyArr.get(0) == 999, "DynamicArray: Rule of Five deep copy isolation");

    // 7. Bullet Pool Spawning & Active Count
    BulletPool pool;
    int b1 = pool.spawn(100.0, 500.0, 0.0, -400.0, 25);
    int b2 = pool.spawn(120.0, 500.0, 0.0, -400.0, 25);
    summary.record(b1 == 0 && b2 == 1 && pool.getActiveCount() == 2, "BulletPool: Spawning preallocated bullets");

    // 8. Bullet Pool Capacity Overflow Protection
    BulletPool smallPool;
    for (int i = 0; i < BulletPool::MAX_BULLETS; ++i) {
        smallPool.spawn(0, 0, 0, -100, 10);
    }
    int overflowSpawn = smallPool.spawn(50, 50, 0, -100, 10);
    summary.record(overflowSpawn == -1 && smallPool.getActiveCount() == BulletPool::MAX_BULLETS,
                   "BulletPool: Overflow gracefully rejected (-1)");

    // 9. Bullet Pool Update & Boundary Culling
    pool.update(2.0, 800.0, 600.0); // Bullet travels y = 500 + (-400)*2 = -300 (off screen top)
    summary.record(pool.getActiveCount() == 0, "BulletPool: Update and arena boundary culling");

    // 10. 2D Asteroid Collision Grid Broadphase Lookup
    CollisionGrid cGrid;
    cGrid.insertAsteroid(101, 250.0, 300.0, 20.0);
    int hitId = -1;
    bool hit = cGrid.testCollision(255.0, 300.0, 10.0, hitId);
    int missHitId = -1;
    bool miss = cGrid.testCollision(50.0, 50.0, 10.0, missHitId);
    summary.record(hit && hitId == 101 && !miss && missHitId == -1,
                   "CollisionGrid: Spatial partitioning collision hit and miss");

    return summary;
}
