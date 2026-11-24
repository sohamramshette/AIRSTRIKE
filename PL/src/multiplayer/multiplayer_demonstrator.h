// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_demonstrator.h
// Subject   : PL-CO1..CO5 | Multiplayer Data Structures
// Topics    : [TOPIC: MultiplayerDemonstrator] [TOPIC: Spatial Grid] [TOPIC: Doubly Linked List] [TOPIC: Circular Queue] [TOPIC: QuickSort] [TOPIC: Binary Search] [TOPIC: Hash Table]
// Game Role : Orchestrates interactive console demonstrations for all 5 PL multiplayer structures.
// ==============================================================================

#ifndef AIRSTRIKE_MULTIPLAYER_DEMONSTRATOR_H
#define AIRSTRIKE_MULTIPLAYER_DEMONSTRATOR_H

namespace Airstrike::Multiplayer {

// [TOPIC: MultiplayerDemonstrator] [TOPIC: PL-CO1..CO5]
// Purpose: Groups standalone CLI walkthroughs for grid partitioning, event history, packet queue, ranking, and sessions.
// Game Role: Showcases how PL data structures power two-player AirStrike telemetry and matchmaking.
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
