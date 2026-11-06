#ifndef COLLISION_GRID_H
#define COLLISION_GRID_H

#include <iostream>

/**
 * 2D Asteroid Collision Grid (PL-CO1).
 * Spatial partitioning grid for efficient O(1) broadphase collision queries.
 * Divides an 800x600 arena into an 8x8 matrix of sectors.
 */
struct Asteroid {
    int id;
    double x;
    double y;
    double radius;
    bool active;

    Asteroid() : id(0), x(0.0), y(0.0), radius(0.0), active(false) {}
};

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

    // Coordinate to grid index mapping
    void getSector(double x, double y, int& outRow, int& outCol) const;

    // Insertion of asteroid entity
    bool insertAsteroid(int id, double x, double y, double radius);

    // Deletion of asteroid by ID
    bool removeAsteroid(int id);

    // Broadphase + Narrowphase collision test against player or projectile
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

