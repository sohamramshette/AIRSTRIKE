#ifndef GRID_SWARM_ENEMY_H
#define GRID_SWARM_ENEMY_H

#include <iostream>
#include <cmath>

/**
 * Spatial Grid Swarm Enemy (PL-CO1).
 *
 * Demonstrates:
 * - 2D Array Contiguous Memory Representation & Row-Major Addressing:
 *   Offset(i, j) = (i * COLS + j) * sizeof(Element)
 * - Spatial Partitioning for O(1) Sector Binning & Local Entity Queries
 * - Boundary Condition Handling & Swarm Dispersion into Adjacent Cells
 */
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

    // Coordinate mapping & row-major indexing
    void getSector(double x, double y, int& outRow, int& outCol) const;
    static size_t calculateRowMajorOffset(int row, int col);

    // Drone lifecycle & kinematics
    bool spawnDrone(int droneId, double x, double y, int health = 20);
    bool removeDrone(int droneId);
    bool moveDrone(int droneId, double newX, double newY);

    // Nearby entity lookup using adjacent cell scanning
    int findNearbyDrones(double targetX, double targetY, double searchRadius,
                         int outIds[], int maxResults) const;

    // Dispersion & Debris fragmentation
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
