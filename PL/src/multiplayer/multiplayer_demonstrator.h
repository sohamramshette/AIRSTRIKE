#ifndef AIRSTRIKE_MULTIPLAYER_DEMONSTRATOR_H
#define AIRSTRIKE_MULTIPLAYER_DEMONSTRATOR_H

namespace Airstrike::Multiplayer {

/**
 * ==============================================================================
 * Master Demonstrator for PL Multiplayer Processing (PL-CO1 to PL-CO5)
 *
 * Demonstrates:
 * - PL-CO1: 2D Spatial Grid (Player 1, Player 2 & Fleet nearby spatial queries)
 * - PL-CO2: Doubly Linked List match event history with bi-directional traversal
 * - PL-CO3: Circular FIFO queue for multiplayer network packet stream
 * - PL-CO4: QuickSort and Binary Search for match score rankings
 * - PL-CO5: Open-addressing Hash Table for player session lookup with tombstones
 * ==============================================================================
 */
class MultiplayerDemonstrator {
public:
    static void runAllDemonstrations();
    static void demoSpatialGrid();
    static void demoEventHistory();
    static void demoPacketQueue();
    static void demoLeaderboardSearch();
    static void demoSessionTable();
};

} // namespace Airstrike::Multiplayer

#endif // AIRSTRIKE_MULTIPLAYER_DEMONSTRATOR_H
