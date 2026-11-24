// ==============================================================================
// File      : PL/src/arrays/grid_swarm_enemy.h
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: GridSwarmEnemy] [TOPIC: 2D Array] [TOPIC: Row-Major Addressing] [TOPIC: Spatial Partitioning]
// Game Role : Manages swarm drones and scatter mines across an 8x8 spatial sector grid.
// ==============================================================================

#ifndef GRID_SWARM_ENEMY_H
#define GRID_SWARM_ENEMY_H

#include <iostream>
#include <cmath>

// [TOPIC: SwarmDrone Entity]
// Purpose: Stores a swarm drone's world position, cached sector coordinates, health, and active flag.
// Game Role: Represents one drone or dispersed sub-munition tracked by GridSwarmEnemy.
struct SwarmDrone {
    int droneId;
    double x;
    double y;
    int sectorRow;
    int sectorCol;
    int health;
    bool active;

    SwarmDrone() : droneId(0), x(0.0), y(0.0), sectorRow(0), sectorCol(0), health(0), active(false) {}
    SwarmDrone(int id, double posX, double posY, int r, int c, int hp)
        : droneId(id), x(posX), y(posY), sectorRow(r), sectorCol(c), health(hp), active(true) {}
};

// [TOPIC: GridSwarmEnemy] [TOPIC: Row-Major Addressing] [TOPIC: Spatial Partitioning]
// Purpose: Partitions up to 32 swarm drones into an 8x8 cell array for O(1) sector binning and local queries.
// Game Role: Drives the PL-CO1 Spatial Grid Swarm enemy and its debris scatter mechanic.
class GridSwarmEnemy {
public:
    static constexpr int GRID_ROWS = 8;
    static constexpr int GRID_COLS = 8;
    static constexpr double ARENA_WIDTH = 800.0;
    static constexpr double ARENA_HEIGHT = 600.0;
    static constexpr int MAX_SWARM_DRONES = 32;
    static constexpr int MAX_PER_CELL = 8;

private:
    SwarmDrone drones[MAX_SWARM_DRONES];
    int cellOccupancy[GRID_ROWS][GRID_COLS][MAX_PER_CELL];
    int cellCounts[GRID_ROWS][GRID_COLS];
    int activeDroneCount;

public:
    GridSwarmEnemy();
    ~GridSwarmEnemy() = default;

    // [TOPIC: Row-Major Addressing]
    // Purpose: Maps coordinates to grid indices and computes linear row-major offset (i * COLS + j).
    // Game Role: Demonstrates contiguous 2D memory layout for sector lookups.
    void getSector(double x, double y, int& outRow, int& outCol) const;
    static size_t calculateRowMajorOffset(int row, int col);

    // [TOPIC: Swarm Lifecycle & Re-Binning]
    // Purpose: Spawns, removes, and moves drones while maintaining accurate cell occupancy buckets.
    // Game Role: Keeps swarm spatial indices synchronized as drones fly across sector boundaries.
    bool spawnDrone(int droneId, double x, double y, int health = 20);
    bool removeDrone(int droneId);
    bool moveDrone(int droneId, double newX, double newY);

    // [TOPIC: Local Proximity Query]
    // Purpose: Scans the 3x3 adjacent sector window to collect drones within searchRadius.
    // Game Role: Enables localized cluster targeting without full-array traversal.
    int findNearbyDrones(double targetX, double targetY, double searchRadius,
                         int outIds[], int maxResults) const;

    // [TOPIC: Swarm Dispersion]
    // Purpose: Spawns radial scatter sub-mines around an origin into surrounding grid cells.
    // Game Role: Implements the Spatial Grid Swarm's hull-breach debris scatter attack.
    int disperseScatterMines(double originX, double originY, int count, int outMineIds[]);

    // State queries
    int getCellCount(int row, int col) const;
    int getTotalActiveDrones() const { return activeDroneCount; }
    const SwarmDrone* getDroneById(int droneId) const;
    bool isCellFull(int row, int col) const;

    void clear();
    void printSwarmGrid() const;
};

#endif // GRID_SWARM_ENEMY_H
