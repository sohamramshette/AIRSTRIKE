#include "multiplayer_spatial_grid.h"
#include <cmath>
#include <algorithm>

namespace Airstrike::Multiplayer {

template<typename T>
static inline T clampVal(T val, T low, T high) {
    return (val < low) ? low : (val > high) ? high : val;
}

MultiplayerSpatialGrid::MultiplayerSpatialGrid()
    : cellWidth_(WORLD_WIDTH / GRID_COLS),
      cellHeight_(WORLD_HEIGHT / GRID_ROWS) {
    clear();
}

void MultiplayerSpatialGrid::clear() {
    for (int r = 0; r < GRID_ROWS; ++r) {
        for (int c = 0; c < GRID_COLS; ++c) {
            cells_[r][c].clear();
        }
    }
}

int MultiplayerSpatialGrid::getCellIndex(float x, float y) const {
    int col = static_cast<int>(x / cellWidth_);
    int row = static_cast<int>(y / cellHeight_);
    col = clampVal(col, 0, GRID_COLS - 1);
    row = clampVal(row, 0, GRID_ROWS - 1);
    return row * GRID_COLS + col;
}

bool MultiplayerSpatialGrid::insertEntity(const CombatEntity& entity) {
    int col = static_cast<int>(entity.x / cellWidth_);
    int row = static_cast<int>(entity.y / cellHeight_);
    col = clampVal(col, 0, GRID_COLS - 1);
    row = clampVal(row, 0, GRID_ROWS - 1);

    cells_[row][col].push_back(entity);
    return true;
}

bool MultiplayerSpatialGrid::updateEntityPosition(int id, float newX, float newY) {
    // Find and remove from current cell
    CombatEntity foundEntity;
    bool found = false;

    for (int r = 0; r < GRID_ROWS && !found; ++r) {
        for (int c = 0; c < GRID_COLS && !found; ++c) {
            auto& vec = cells_[r][c];
            for (auto it = vec.begin(); it != vec.end(); ++it) {
                if (it->id == id) {
                    foundEntity = *it;
                    foundEntity.x = newX;
                    foundEntity.y = newY;
                    vec.erase(it);
                    found = true;
                    break;
                }
            }
        }
    }

    if (found) {
        return insertEntity(foundEntity);
    }
    return false;
}

std::vector<CombatEntity> MultiplayerSpatialGrid::queryNearby(float x, float y, float searchRadius) const {
    std::vector<CombatEntity> nearby;

    int minCol = clampVal(static_cast<int>((x - searchRadius) / cellWidth_), 0, GRID_COLS - 1);
    int maxCol = clampVal(static_cast<int>((x + searchRadius) / cellWidth_), 0, GRID_COLS - 1);
    int minRow = clampVal(static_cast<int>((y - searchRadius) / cellHeight_), 0, GRID_ROWS - 1);
    int maxRow = clampVal(static_cast<int>((y + searchRadius) / cellHeight_), 0, GRID_ROWS - 1);

    float radiusSq = searchRadius * searchRadius;

    for (int r = minRow; r <= maxRow; ++r) {
        for (int c = minCol; c <= maxCol; ++c) {
            for (const auto& entity : cells_[r][c]) {
                float dx = entity.x - x;
                float dy = entity.y - y;
                if ((dx * dx + dy * dy) <= radiusSq) {
                    nearby.push_back(entity);
                }
            }
        }
    }

    return nearby;
}

int MultiplayerSpatialGrid::getEntityCountAt(int col, int row) const {
    if (col < 0 || col >= GRID_COLS || row < 0 || row >= GRID_ROWS) return 0;
    return static_cast<int>(cells_[row][col].size());
}

int MultiplayerSpatialGrid::getTotalEntities() const {
    int total = 0;
    for (int r = 0; r < GRID_ROWS; ++r) {
        for (int c = 0; c < GRID_COLS; ++c) {
            total += static_cast<int>(cells_[r][c].size());
        }
    }
    return total;
}

MultiplayerSpatialGrid::BenchmarkResult MultiplayerSpatialGrid::runCollisionReductionBenchmark() const {
    BenchmarkResult res;
    res.totalEntities = getTotalEntities();

    // Naive pairwise checks: N * (N - 1) / 2
    res.naiveChecksRequired = (res.totalEntities * (res.totalEntities - 1)) / 2;

    // Grid checks: sum of cell-wise pairwise checks across populated cells and adjacent neighborhoods
    int gridChecks = 0;
    for (int r = 0; r < GRID_ROWS; ++r) {
        for (int c = 0; c < GRID_COLS; ++c) {
            int n = static_cast<int>(cells_[r][c].size());
            gridChecks += (n * (n - 1)) / 2;
            // Checks with right and bottom neighbors to avoid duplicates
            if (c + 1 < GRID_COLS) {
                gridChecks += n * static_cast<int>(cells_[r][c + 1].size());
            }
            if (r + 1 < GRID_ROWS) {
                gridChecks += n * static_cast<int>(cells_[r + 1][c].size());
            }
        }
    }

    res.gridChecksRequired = gridChecks;
    if (res.naiveChecksRequired > 0) {
        res.reductionPercentage = 100.0 * (1.0 - (static_cast<double>(res.gridChecksRequired) / res.naiveChecksRequired));
    } else {
        res.reductionPercentage = 0.0;
    }

    return res;
}

} // namespace Airstrike::Multiplayer
