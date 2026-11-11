#include "multiplayer_demonstrator.h"
#include "multiplayer_spatial_grid.h"
#include "multiplayer_event_history.h"
#include "multiplayer_packet_queue.h"
#include "multiplayer_leaderboard_search.h"
#include "multiplayer_session_table.h"
#include "match_event.h"

#include <iostream>
#include <iomanip>
#include <vector>

namespace Airstrike::Multiplayer {

void MultiplayerDemonstrator::demoSpatialGrid() {
    std::cout << "\n==============================================================================\n";
    std::cout << "  PL-CO1: 2D Spatial Grid & Partitioning Demonstrator                         \n";
    std::cout << "==============================================================================\n";
    MultiplayerSpatialGrid grid;

    // Place two players
    grid.insertEntity({1, "PLAYER_1", true, 150.0f, 250.0f, 15.0f});
    grid.insertEntity({2, "PLAYER_2", true, 650.0f, 350.0f, 15.0f});

    // Place enemy craft
    grid.insertEntity({101, "ENEMY_SCOUT_1", false, 180.0f, 270.0f, 10.0f});
    grid.insertEntity({102, "ENEMY_SCOUT_2", false, 220.0f, 290.0f, 10.0f});
    grid.insertEntity({103, "ENEMY_CRUISER_1", false, 620.0f, 320.0f, 20.0f});
    grid.insertEntity({104, "ENEMY_BOSS", false, 400.0f, 100.0f, 35.0f});

    std::cout << "  [Grid Initialized] 800x650 arena with 8x8 cells\n";
    std::cout << "  Registered Entities: Player 1 (150, 250), Player 2 (650, 350), 4 Enemies\n\n";

    // Query nearby entities for Player 1 (radius 120.0)
    std::cout << "  Querying entities within radius 120.0 around Player 1 (150, 250):\n";
    std::vector<CombatEntity> nearbyP1 = grid.queryNearby(150.0f, 250.0f, 120.0f);
    for (size_t i = 0; i < nearbyP1.size(); ++i) {
        const CombatEntity& e = nearbyP1[i];
        std::cout << "    -> Found ID: " << std::left << std::setw(6) << e.id 
                  << " Name: " << std::setw(18) << e.name 
                  << " at (" << e.x << ", " << e.y << ")\n";
    }

    // Benchmark collision reduction
    MultiplayerSpatialGrid::BenchmarkResult bench = grid.runCollisionReductionBenchmark();
    std::cout << "\n  [Collision Broadphase Benchmark - " << bench.totalEntities << " Entities]:\n";
    std::cout << "    Brute-force pairwise checks : " << bench.naiveChecksRequired << "\n";
    std::cout << "    Spatial-grid cell checks    : " << bench.gridChecksRequired << "\n";
    std::cout << "    Workload Reduction          : " << std::fixed << std::setprecision(1) << bench.reductionPercentage << "%\n";
}

void MultiplayerDemonstrator::demoEventHistory() {
    std::cout << "\n==============================================================================\n";
    std::cout << "  PL-CO2: Doubly Linked List Match Event History Demonstrator                 \n";
    std::cout << "==============================================================================\n";
    MultiplayerEventHistory history;

    std::vector<MatchEvent> events = getSampleMatchEvents();
    for (size_t i = 0; i < events.size(); ++i) {
        history.append(events[i]);
    }

    std::cout << "  [Event History Initialized] Appended " << history.size() << " match events.\n\n";

    std::cout << "  -- Forward Chronological Traversal (Head -> Tail) --\n";
    std::vector<MatchEvent> forwardList = history.traverseForward();
    for (size_t i = 0; i < forwardList.size(); ++i) {
        const MatchEvent& ev = forwardList[i];
        std::cout << "    [ID " << ev.eventId << "] " << std::setw(16) << std::left << ev.eventType
                  << " Player: " << std::setw(12) << ev.playerId
                  << " Score: " << std::setw(6) << ev.score
                  << " Pos: (" << ev.positionX << ", " << ev.positionY << ")\n";
    }

    std::cout << "\n  -- Reverse Chronological Traversal (Tail -> Head) --\n";
    std::vector<MatchEvent> backwardList = history.traverseBackward();
    for (size_t i = 0; i < backwardList.size(); ++i) {
        const MatchEvent& ev = backwardList[i];
        std::cout << "    [ID " << ev.eventId << "] " << std::setw(16) << std::left << ev.eventType
                  << " Player: " << std::setw(12) << ev.playerId
                  << " Score: " << std::setw(6) << ev.score << "\n";
    }

    std::cout << "\n  Removing event ID 3 (SCORE_UPDATED)...\n";
    bool removed = history.removeById(3);
    std::cout << "  Removal result: " << (removed ? "SUCCESS" : "FAILED")
              << " | New size: " << history.size() << "\n";
}

void MultiplayerDemonstrator::demoPacketQueue() {
    std::cout << "\n==============================================================================\n";
    std::cout << "  PL-CO3: Circular FIFO Packet Queue Demonstrator                             \n";
    std::cout << "==============================================================================\n";
    MultiplayerPacketQueue queue(4); // Capacity 4

    std::cout << "  [Queue Created] Fixed capacity = 4 packets\n";

    // Enqueue 4 packets
    for (int i = 1; i <= 4; ++i) {
        MatchEvent ev(i, "PACKET_" + std::to_string(i), "p1", 1000 + i * 10, i * 50, 0.0f, 0.0f);
        queue.enqueue(ev);
        std::cout << "  Enqueued Packet " << i << " | Queue size: " << queue.size() << "\n";
    }

    std::cout << "  Is Full: " << (queue.isFull() ? "YES" : "NO") << "\n";

    // Attempt overflow
    MatchEvent overflowEv(99, "OVERFLOW", "p1", 2000, 999, 0.0f, 0.0f);
    bool pushed = queue.enqueue(overflowEv);
    std::cout << "  Attempt enqueue into full queue: " << (pushed ? "SUCCESS" : "REJECTED (Buffer Full Protection)") << "\n";

    // Dequeue 2 packets
    MatchEvent out1, out2;
    queue.dequeue(out1);
    queue.dequeue(out2);
    std::cout << "  Dequeued Packet: " << out1.eventType << "\n";
    std::cout << "  Dequeued Packet: " << out2.eventType << "\n";
    std::cout << "  Queue size after 2 dequeues: " << queue.size() << "\n";

    // Enqueue 2 new packets (Demonstrating circular modulo wraparound)
    MatchEvent ev5(5, "PACKET_5_WRAP", "p2", 1050, 250, 0.0f, 0.0f);
    MatchEvent ev6(6, "PACKET_6_WRAP", "p2", 1060, 300, 0.0f, 0.0f);
    queue.enqueue(ev5);
    queue.enqueue(ev6);
    std::cout << "  Enqueued 2 new packets with circular index wraparound.\n";
    std::cout << "  Queue final size: " << queue.size() << " | Is Full: " << (queue.isFull() ? "YES" : "NO") << "\n";
}

void MultiplayerDemonstrator::demoLeaderboardSearch() {
    std::cout << "\n==============================================================================\n";
    std::cout << "  PL-CO4: QuickSort & Binary Search Demonstrator                              \n";
    std::cout << "==============================================================================\n";

    std::vector<MatchRecord> records = {
        {"m1", "alice", "bob", 3200, 2100, "alice"},
        {"m2", "charlie", "david", 1500, 1800, "david"},
        {"m3", "eve", "frank", 4800, 4200, "eve"},
        {"m4", "grace", "heidi", 2100, 950, "grace"},
        {"m5", "ivan", "judy", 4800, 4800, "DRAW"},
        {"m6", "mallory", "oscar", 6200, 5900, "mallory"},
        {"m7", "trent", "walter", 950, 800, "trent"}
    };

    std::cout << "  Original Unsorted Match Records (" << records.size() << " records):\n";
    for (size_t i = 0; i < records.size(); ++i) {
        const MatchRecord& r = records[i];
        std::cout << "    [" << r.matchId << "] " << r.player1Id << " vs " << r.player2Id 
                  << " | Top Score: " << r.highestScore << " (Winner: " << r.winnerId << ")\n";
    }

    MultiplayerLeaderboardSearch::quickSortByScore(records, 0, static_cast<int>(records.size()) - 1);

    std::cout << "\n  QuickSorted Records (Ascending by Top Score):\n";
    for (size_t i = 0; i < records.size(); ++i) {
        std::cout << "    Rank #" << i << " | Top Score: " << std::setw(5) << records[i].highestScore
                  << " | " << records[i].matchId << " (" << records[i].player1Id << " vs " << records[i].player2Id << ")\n";
    }

    // Binary search for exact score
    int target = 4800;
    int foundIdx = MultiplayerLeaderboardSearch::binarySearchByScore(records, target);
    std::cout << "\n  Binary Search for score " << target << ": " 
              << (foundIdx != -1 ? "FOUND at index " + std::to_string(foundIdx) : "NOT FOUND") << "\n";

    // Duplicate score range
    std::pair<int, int> range = MultiplayerLeaderboardSearch::findScoreRange(records, target);
    int first = range.first;
    int last = range.second;
    std::cout << "  Duplicate Score Range for " << target << ": indices [" << first << " to " << last 
              << "] (Count: " << (last - first + 1) << " matches)\n";
}

void MultiplayerDemonstrator::demoSessionTable() {
    std::cout << "\n==============================================================================\n";
    std::cout << "  PL-CO5: Open-Addressing Hash Table & Tombstone Lookup Demonstrator           \n";
    std::cout << "==============================================================================\n";
    MultiplayerSessionTable table(8);

    PlayerSession s1("student_01", "room_alpha", "BROWSER", true, 42, 100001);
    PlayerSession s2("student_02", "room_alpha", "JAVA_SWING", true, 38, 100002);
    PlayerSession s3("student_03", "room_beta", "BROWSER", false, 65, 100003);
    PlayerSession s4("student_04", "room_beta", "JAVA_SWING", true, 29, 100004);

    table.insert(s1.playerId, s1);
    table.insert(s2.playerId, s2);
    table.insert(s3.playerId, s3);
    table.insert(s4.playerId, s4);

    std::cout << "  [Hash Table Initialized] Inserted 4 player sessions into capacity " << table.capacity() << "\n";
    std::cout << "  Table Size: " << table.size() << " | Load Factor: " 
              << std::fixed << std::setprecision(2) << table.loadFactor() << "\n\n";

    // Lookup
    const PlayerSession* found = table.find("student_02");
    if (found) {
        std::cout << "  Lookup 'student_02': FOUND in " << found->roomId 
                  << " via " << found->clientType << " (Ping: " << found->pingMs << "ms)\n";
    }

    // Tombstone deletion
    std::cout << "\n  Deleting 'student_02' (marks slot as TOMBSTONE)...\n";
    bool removed = table.remove("student_02");
    std::cout << "  Deletion result: " << (removed ? "SUCCESS" : "FAILED") << "\n";
    std::cout << "  Active Items: " << table.size() << " | Tombstones: " << table.tombstoneCount() << "\n";

    // Verify lookup of subsequent key in probe sequence still succeeds across tombstone
    const PlayerSession* foundAfter = table.find("student_04");
    std::cout << "  Lookup 'student_04' across tombstone: " 
              << (foundAfter ? "SUCCESS (Probe Chain Intact)" : "BROKEN CHAIN") << "\n";
}

void MultiplayerDemonstrator::runAllDemonstrations() {
    std::cout << "\n==============================================================================\n";
    std::cout << "       AIRSTRIKE MULTIPLAYER NATIVE DATA STRUCTURE DEMONSTRATIONS             \n";
    std::cout << "       Course Outcomes Covered: PL-CO1, PL-CO2, PL-CO3, PL-CO4, PL-CO5        \n";
    std::cout << "==============================================================================\n";

    demoSpatialGrid();
    demoEventHistory();
    demoPacketQueue();
    demoLeaderboardSearch();
    demoSessionTable();

    std::cout << "\n==============================================================================\n";
    std::cout << "       ALL PL MULTIPLAYER DEMONSTRATIONS COMPLETED SUCCESSFULLY               \n";
    std::cout << "==============================================================================\n";
}

} // namespace Airstrike::Multiplayer
