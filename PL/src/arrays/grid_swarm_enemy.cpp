// ==============================================================================
// File      : PL/src/arrays/grid_swarm_enemy.cpp
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: GridSwarmEnemy] [TOPIC: Row-Major Addressing] [TOPIC: Spatial Partitioning]
// Game Role : Implements 2D spatial sector binning, movement re-bucketing, and mine dispersion.
// ==============================================================================

#include "grid_swarm_enemy.h"
#include <iomanip>
#include <algorithm>

GridSwarmEnemy::GridSwarmEnemy() : activeDroneCount(0) {
    clear();
}

void GridSwarmEnemy::clear() {
    activeDroneCount = 0;
    for (int i = 0; i < MAX_SWARM_DRONES; ++i) {
        drones[i] = SwarmDrone();
    }
    for (int r = 0; r < GRID_ROWS; ++r) {
        for (int c = 0; c < GRID_COLS; ++c) {
            cellCounts[r][c] = 0;
            for (int k = 0; k < MAX_PER_CELL; ++k) {
                cellOccupancy[r][c][k] = -1;
            }
        }
    }
}

// [TOPIC: Row-Major Addressing]
// Purpose: Computes linear element offset (row * GRID_COLS + col) for 2D row-major layout.
// Game Role: Exposes the underlying memory index formula used by the 8x8 swarm sector table.
size_t GridSwarmEnemy::calculateRowMajorOffset(int row, int col) {
    // WHY: C++ stores 2D arrays row-by-row so element (row, col) sits at Base + (row * COLS + col) * sizeof(T).
    return static_cast<size_t>(row * GRID_COLS + col);
}

void GridSwarmEnemy::getSector(double x, double y, int& outRow, int& outCol) const {
    double cellW = ARENA_WIDTH / GRID_COLS;
    double cellH = ARENA_HEIGHT / GRID_ROWS;

    // WHY: Clamp just below arena bounds so x / cellW cannot produce index GRID_COLS on exact edge hits.
    if (x < 0.0) x = 0.0;
    if (x >= ARENA_WIDTH) x = ARENA_WIDTH - 0.001;
    if (y < 0.0) y = 0.0;
    if (y >= ARENA_HEIGHT) y = ARENA_HEIGHT - 0.001;

    outCol = static_cast<int>(x / cellW);
    outRow = static_cast<int>(y / cellH);

    outCol = std::max(0, std::min(GRID_COLS - 1, outCol));
    outRow = std::max(0, std::min(GRID_ROWS - 1, outRow));
}

bool GridSwarmEnemy::isCellFull(int row, int col) const {
    if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) return true;
    return cellCounts[row][col] >= MAX_PER_CELL;
}

int GridSwarmEnemy::getCellCount(int row, int col) const {
    if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) return 0;
    return cellCounts[row][col];
}

const SwarmDrone* GridSwarmEnemy::getDroneById(int droneId) const {
    for (int i = 0; i < MAX_SWARM_DRONES; ++i) {
        if (drones[i].active && drones[i].droneId == droneId) {
            return &drones[i];
        }
    }
    return nullptr;
}

bool GridSwarmEnemy::spawnDrone(int droneId, double x, double y, int health) {
    if (activeDroneCount >= MAX_SWARM_DRONES) return false;
    if (getDroneById(droneId) != nullptr) return false; // Duplicate ID

    int r = 0, c = 0;
    getSector(x, y, r, c);

    if (cellCounts[r][c] >= MAX_PER_CELL) {
        return false; // Cell saturated
    }

    // Find free drone slot
    int slot = -1;
    for (int i = 0; i < MAX_SWARM_DRONES; ++i) {
        if (!drones[i].active) {
            slot = i;
            break;
        }
    }
    if (slot == -1) return false;

    drones[slot] = SwarmDrone(droneId, x, y, r, c, health);
    cellOccupancy[r][c][cellCounts[r][c]] = droneId;
    cellCounts[r][c]++;
    activeDroneCount++;
    return true;
}

bool GridSwarmEnemy::removeDrone(int droneId) {
    for (int i = 0; i < MAX_SWARM_DRONES; ++i) {
        if (drones[i].active && drones[i].droneId == droneId) {
            int r = drones[i].sectorRow;
            int c = drones[i].sectorCol;

            // Remove from cell occupancy list
            int idxInCell = -1;
            for (int k = 0; k < cellCounts[r][c]; ++k) {
                if (cellOccupancy[r][c][k] == droneId) {
                    idxInCell = k;
                    break;
                }
            }
            if (idxInCell != -1) {
                // Shift remaining cell elements
                for (int k = idxInCell; k < cellCounts[r][c] - 1; ++k) {
                    cellOccupancy[r][c][k] = cellOccupancy[r][c][k + 1];
                }
                cellOccupancy[r][c][cellCounts[r][c] - 1] = -1;
                cellCounts[r][c]--;
            }

            drones[i].active = false;
            activeDroneCount--;
            return true;
        }
    }
    return false;
}

bool GridSwarmEnemy::moveDrone(int droneId, double newX, double newY) {
    for (int i = 0; i < MAX_SWARM_DRONES; ++i) {
        if (drones[i].active && drones[i].droneId == droneId) {
            int oldR = drones[i].sectorRow;
            int oldC = drones[i].sectorCol;

            int newR = 0, newC = 0;
            getSector(newX, newY, newR, newC);

            // WHY: Only update 3D occupancy array when drone crosses a sector boundary -> Game: Avoids needless re-binning each tick.
            if (oldR != newR || oldC != newC) {
                if (cellCounts[newR][newC] >= MAX_PER_CELL) {
                    return false; // Target cell saturated
                }

                // Remove from old cell
                int idxInCell = -1;
                for (int k = 0; k < cellCounts[oldR][oldC]; ++k) {
                    if (cellOccupancy[oldR][oldC][k] == droneId) {
                        idxInCell = k;
                        break;
                    }
                }
                if (idxInCell != -1) {
                    // WHY: Left-shift bucket entries to keep cellOccupancy[oldR][oldC] densely packed.
                    for (int k = idxInCell; k < cellCounts[oldR][oldC] - 1; ++k) {
                        cellOccupancy[oldR][oldC][k] = cellOccupancy[oldR][oldC][k + 1];
                    }
                    cellOccupancy[oldR][oldC][cellCounts[oldR][oldC] - 1] = -1;
                    cellCounts[oldR][oldC]--;
                }

                // Insert into new cell
                cellOccupancy[newR][newC][cellCounts[newR][newC]] = droneId;
                cellCounts[newR][newC]++;
                drones[i].sectorRow = newR;
                drones[i].sectorCol = newC;
            }

            drones[i].x = newX;
            drones[i].y = newY;
            return true;
        }
    }
    return false;
}

// [TOPIC: Spatial Partitioning] [TOPIC: Local Proximity Search]
// Purpose: Inspects the 3x3 surrounding sectors and collects drone IDs within searchRadius.
// Game Role: Speeds up local swarm queries for AirStrike radial targeting.
int GridSwarmEnemy::findNearbyDrones(double targetX, double targetY, double searchRadius,
                                     int outIds[], int maxResults) const {
    int centerR = 0, centerC = 0;
    getSector(targetX, targetY, centerR, centerC);

    int foundCount = 0;
    double radiusSq = searchRadius * searchRadius;

    // WHY: Scan only the 3x3 sector neighborhood instead of all MAX_SWARM_DRONES -> Game: O(1) localized query.
    for (int dr = -1; dr <= 1; ++dr) {
        int r = centerR + dr;
        if (r < 0 || r >= GRID_ROWS) continue;

        for (int dc = -1; dc <= 1; ++dc) {
            int c = centerC + dc;
            if (c < 0 || c >= GRID_COLS) continue;

            for (int k = 0; k < cellCounts[r][c]; ++k) {
                int id = cellOccupancy[r][c][k];
                const SwarmDrone* d = getDroneById(id);
                if (d && d->active) {
                    double distSq = (d->x - targetX) * (d->x - targetX) + (d->y - targetY) * (d->y - targetY);
                    // WHY: Compare squared distances to avoid costly sqrt() per candidate.
                    if (distSq <= radiusSq) {
                        if (foundCount < maxResults) {
                            outIds[foundCount] = id;
                        }
                        foundCount++;
                    }
                }
            }
        }
    }

    return std::min(foundCount, maxResults);
}

// [TOPIC: GridSwarmEnemy] [TOPIC: Radial Dispersion]
// Purpose: Places up to 8 scatter mines around (originX, originY) at 45-degree intervals into grid sectors.
// Game Role: Populates neighboring spatial cells with debris hazards when a Swarm Carrier splits.
int GridSwarmEnemy::disperseScatterMines(double originX, double originY, int count, int outMineIds[]) {
    int spawnedCount = 0;
    // WHY: Precomputed radian offsets every 45 deg disperse mines evenly across neighboring cells.
    double angles[] = {0.0, 1.5708, 3.14159, 4.71239, 0.7854, 2.3562, 3.9270, 5.4978};
    double scatterDistance = 45.0;

    for (int i = 0; i < count && i < 8; ++i) {
        double mx = originX + std::cos(angles[i]) * scatterDistance;
        double my = originY + std::sin(angles[i]) * scatterDistance;
        int newId = 9000 + i;

        if (spawnDrone(newId, mx, my, 10)) {
            outMineIds[spawnedCount] = newId;
            spawnedCount++;
        }
    }
    return spawnedCount;
}

void GridSwarmEnemy::printSwarmGrid() const {
    std::cout << "\n=== Spatial Grid Swarm Status (Active: " << activeDroneCount << "/" << MAX_SWARM_DRONES << ") ===\n";
    std::cout << "   ";
    for (int c = 0; c < GRID_COLS; ++c) std::cout << "  C" << c;
    std::cout << "\n";
    for (int r = 0; r < GRID_ROWS; ++r) {
        std::cout << "R" << r << ":";
        for (int c = 0; c < GRID_COLS; ++c) {
            std::cout << "  [" << cellCounts[r][c] << "]";
        }
        std::cout << "\n";
    }
}
