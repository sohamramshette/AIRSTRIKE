#include "collision_grid.h"
#include <cmath>
#include <iomanip>

CollisionGrid::CollisionGrid() {
    clear();
}

void CollisionGrid::clear() {
    for (int i = 0; i < MAX_ASTEROIDS; ++i) {
        asteroids[i].id = 0;
        asteroids[i].x = 0.0;
        asteroids[i].y = 0.0;
        asteroids[i].radius = 0.0;
        asteroids[i].active = false;
    }
    for (int r = 0; r < ROWS; ++r) {
        for (int c = 0; c < COLS; ++c) {
            cellCounts[r][c] = 0;
            for (int k = 0; k < MAX_PER_CELL; ++k) {
                cellOccupancy[r][c][k] = -1;
            }
        }
    }
}

void CollisionGrid::getSector(double x, double y, int& outRow, int& outCol) const {
    double cellW = ARENA_WIDTH / COLS;
    double cellH = ARENA_HEIGHT / ROWS;

    outCol = static_cast<int>(x / cellW);
    outRow = static_cast<int>(y / cellH);

    if (outCol < 0) outCol = 0;
    if (outCol >= COLS) outCol = COLS - 1;
    if (outRow < 0) outRow = 0;
    if (outRow >= ROWS) outRow = ROWS - 1;
}

bool CollisionGrid::insertAsteroid(int id, double x, double y, double radius) {
    // Find free slot in entity array
    int slot = -1;
    for (int i = 0; i < MAX_ASTEROIDS; ++i) {
        if (!asteroids[i].active) {
            slot = i;
            break;
        }
    }
    if (slot == -1) return false; // Max asteroids reached

    int r, c;
    getSector(x, y, r, c);
    if (cellCounts[r][c] >= MAX_PER_CELL) {
        return false; // Cell overflow
    }

    asteroids[slot].id = id;
    asteroids[slot].x = x;
    asteroids[slot].y = y;
    asteroids[slot].radius = radius;
    asteroids[slot].active = true;

    cellOccupancy[r][c][cellCounts[r][c]] = slot;
    cellCounts[r][c]++;
    return true;
}

bool CollisionGrid::removeAsteroid(int id) {
    int slot = -1;
    for (int i = 0; i < MAX_ASTEROIDS; ++i) {
        if (asteroids[i].active && asteroids[i].id == id) {
            slot = i;
            break;
        }
    }
    if (slot == -1) return false;

    // Find in sector grid and remove
    int r, c;
    getSector(asteroids[slot].x, asteroids[slot].y, r, c);
    for (int k = 0; k < cellCounts[r][c]; ++k) {
        if (cellOccupancy[r][c][k] == slot) {
            // Shift elements
            for (int j = k; j < cellCounts[r][c] - 1; ++j) {
                cellOccupancy[r][c][j] = cellOccupancy[r][c][j + 1];
            }
            cellCounts[r][c]--;
            break;
        }
    }

    asteroids[slot].active = false;
    return true;
}

bool CollisionGrid::testCollision(double testX, double testY, double testRadius, int& hitId) const {
    int rCenter, cCenter;
    getSector(testX, testY, rCenter, cCenter);

    // Check center sector and adjacent 8 neighbors (3x3 spatial query window)
    for (int r = rCenter - 1; r <= rCenter + 1; ++r) {
        if (r < 0 || r >= ROWS) continue;
        for (int c = cCenter - 1; c <= cCenter + 1; ++c) {
            if (c < 0 || c >= COLS) continue;

            for (int k = 0; k < cellCounts[r][c]; ++k) {
                int slot = cellOccupancy[r][c][k];
                if (slot >= 0 && asteroids[slot].active) {
                    double dx = testX - asteroids[slot].x;
                    double dy = testY - asteroids[slot].y;
                    double distSq = dx * dx + dy * dy;
                    double radSum = testRadius + asteroids[slot].radius;
                    if (distSq <= radSum * radSum) {
                        hitId = asteroids[slot].id;
                        return true;
                    }
                }
            }
        }
    }
    hitId = -1;
    return false;
}

int CollisionGrid::getCellCount(int row, int col) const {
    if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
        return cellCounts[row][col];
    }
    return -1;
}

int CollisionGrid::getTotalAsteroids() const {
    int total = 0;
    for (int i = 0; i < MAX_ASTEROIDS; ++i) {
        if (asteroids[i].active) total++;
    }
    return total;
}

const Asteroid* CollisionGrid::getAsteroidById(int id) const {
    for (int i = 0; i < MAX_ASTEROIDS; ++i) {
        if (asteroids[i].active && asteroids[i].id == id) {
            return &asteroids[i];
        }
    }
    return nullptr;
}

void CollisionGrid::printRadarGrid() const {
    std::cout << "\n=== Asteroid Sector Radar Grid [8x8] (Total: " << getTotalAsteroids() << ") ===\n";
    std::cout << "     ";
    for (int c = 0; c < COLS; ++c) std::cout << " C" << c << " ";
    std::cout << "\n    +";
    for (int c = 0; c < COLS; ++c) std::cout << "----";
    std::cout << "+\n";

    for (int r = 0; r < ROWS; ++r) {
        std::cout << " R" << r << " |";
        for (int c = 0; c < COLS; ++c) {
            int cnt = cellCounts[r][c];
            if (cnt == 0) {
                std::cout << "  . ";
            } else {
                std::cout << "  " << cnt << " ";
            }
        }
        std::cout << "|\n";
    }
    std::cout << "    +";
    for (int c = 0; c < COLS; ++c) std::cout << "----";
    std::cout << "+\n";
}
