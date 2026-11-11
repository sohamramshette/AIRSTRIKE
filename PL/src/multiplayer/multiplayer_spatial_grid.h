#ifndef AIRSTRIKE_MULTIPLAYER_SPATIAL_GRID_H
#define AIRSTRIKE_MULTIPLAYER_SPATIAL_GRID_H

#include <vector>
#include <string>
#include <cstdint>

namespace Airstrike::Multiplayer {

struct CombatEntity {
    int id;
    std::string name;
    bool isPlayer;
    float x;
    float y;
    float radius;
};

/**
 * ==============================================================================
 * Demonstrator for PL-CO1: 2D Arrays and Spatial Partitioning
 *
 * Implements an 8x8 uniform grid partitioning the 800x650 viewport into
 * 64 distinct spatial buckets. Demonstrates:
 * 1. O(1) cell coordinate calculation using row-major indexing: cell = row * COLS + col
 * 2. Independent location updates for Player 1 and Player 2.
 * 3. Fast localized nearby-entity querying.
 * 4. Reduction of collision checks from O(N * M) to O(K) where K << N.
 * ==============================================================================
 */
class MultiplayerSpatialGrid {
public:
    static constexpr int GRID_COLS = 8;
    static constexpr int GRID_ROWS = 8;
    static constexpr float WORLD_WIDTH = 800.0f;
    static constexpr float WORLD_HEIGHT = 650.0f;

    MultiplayerSpatialGrid();

    void clear();
    bool insertEntity(const CombatEntity& entity);
    bool updateEntityPosition(int id, float newX, float newY);
    
    // Spatial query: Returns entities residing within the same or adjacent 3x3 cells
    std::vector<CombatEntity> queryNearby(float x, float y, float searchRadius) const;

    // Benchmarking: Compares grid lookup count vs naive brute-force checks
    struct BenchmarkResult {
        int totalEntities;
        int naiveChecksRequired;
        int gridChecksRequired;
        double reductionPercentage;
    };

    BenchmarkResult runCollisionReductionBenchmark() const;

    int getCellIndex(float x, float y) const;
    int getEntityCountAt(int col, int row) const;
    int getTotalEntities() const;

private:
    float cellWidth_;
    float cellHeight_;
    // 2D Array represented in row-major layout
    std::vector<CombatEntity> cells_[GRID_ROWS][GRID_COLS];
};

} // namespace Airstrike::Multiplayer

#endif // AIRSTRIKE_MULTIPLAYER_SPATIAL_GRID_H
