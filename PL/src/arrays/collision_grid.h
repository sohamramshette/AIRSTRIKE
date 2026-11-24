// ==============================================================================
// File      : PL/src/arrays/collision_grid.h
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: 2D Spatial Grid] [TOPIC: Spatial Partitioning] [TOPIC: Contiguous Memory] [TOPIC: Broadphase Collision]
// Game Role : Partitions the 800x600 AirStrike arena into an 8x8 grid for fast asteroid hit checks.
// ==============================================================================

#ifndef COLLISION_GRID_H
#define COLLISION_GRID_H

#include <iostream>

// [TOPIC: Spatial Entity]
// Purpose: Represents a circular asteroid hazard with world coordinates, radius, and active flag.
// Game Role: Stores asteroid geometry indexed by CollisionGrid sector buckets.
struct Asteroid {
    int id;
    double x;
    double y;
    double radius;
    bool active;

    Asteroid() : id(0), x(0.0), y(0.0), radius(0.0), active(false) {}
};

// [TOPIC: CollisionGrid] [TOPIC: 2D Spatial Partitioning] [TOPIC: 3D Static Array]
// Purpose: Bins asteroids into an 8x8 sector matrix using a fixed 3D occupancy array for O(1) local lookup.
// Game Role: Accelerates projectile-vs-asteroid and ship-vs-asteroid collision queries.
class CollisionGrid {
public:
    static constexpr int ROWS = 8;
    static constexpr int COLS = 8;
    static constexpr double ARENA_WIDTH = 800.0;
    static constexpr double ARENA_HEIGHT = 600.0;
    static constexpr int MAX_ASTEROIDS = 32;
    static constexpr int MAX_PER_CELL = 8;

private:
    Asteroid asteroids[MAX_ASTEROIDS];
    int cellOccupancy[ROWS][COLS][MAX_PER_CELL];
    int cellCounts[ROWS][COLS];

public:
    CollisionGrid();

    // [TOPIC: Coordinate Mapping]
    // Purpose: Maps continuous (x, y) arena coordinates to clamped (row, col) grid indices.
    // Game Role: Determines which spatial sector owns an asteroid or query point.
    void getSector(double x, double y, int& outRow, int& outCol) const;

    bool insertAsteroid(int id, double x, double y, double radius);
    bool removeAsteroid(int id);

    // [TOPIC: Broadphase + Narrowphase Collision]
    // Purpose: Checks only the target's 3x3 adjacent sectors before running circle-intersection math.
    // Game Role: Detects asteroid impacts against player ship or bullets in O(1) average time.
    bool testCollision(double testX, double testY, double testRadius, int& hitId) const;

    // Cell statistics
    int getCellCount(int row, int col) const;
    int getTotalAsteroids() const;
    const Asteroid* getAsteroidById(int id) const;

    // Grid reset
    void clear();

    // ASCII radar grid rendering
    void printRadarGrid() const;
};

#endif // COLLISION_GRID_H

