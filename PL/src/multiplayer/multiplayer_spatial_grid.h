// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_spatial_grid.h
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: MultiplayerSpatialGrid] [TOPIC: 2D Spatial Grid] [TOPIC: Row-Major Indexing] [TOPIC: Broadphase Collision]
// Game Role : Partitions Player 1, Player 2, and enemy fleets into an 8x8 grid for fast proximity queries.
// ==============================================================================

#ifndef AIRSTRIKE_MULTIPLAYER_SPATIAL_GRID_H
#define AIRSTRIKE_MULTIPLAYER_SPATIAL_GRID_H

#include <vector>
#include <string>
#include <cstdint>

namespace Airstrike::Multiplayer {

// [TOPIC: CombatEntity]
// Purpose: Represents a multiplayer ship or enemy position and bounding radius in arena space.
// Game Role: Stored inside MultiplayerSpatialGrid sectors for Player 1 / Player 2 proximity checks.
struct CombatEntity {
    int id;
    std::string name;
    bool isPlayer;
    float x;
    float y;
    float radius;
};

// [TOPIC: MultiplayerSpatialGrid] [TOPIC: 2D Spatial Grid] [TOPIC: Spatial Partitioning]
// Purpose: 8x8 uniform spatial grid partitioning an 800x650 arena with row-major indexing and broadphase benchmarks.
// Game Role: Reduces two-player collision checks from O(N^2) pairwise scans to localized sector lookups.
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
