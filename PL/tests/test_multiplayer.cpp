#include "test_multiplayer.h"
#include "../src/multiplayer/match_event.h"
#include "../src/multiplayer/multiplayer_spatial_grid.h"
#include "../src/multiplayer/multiplayer_event_history.h"
#include "../src/multiplayer/multiplayer_packet_queue.h"
#include "../src/multiplayer/multiplayer_leaderboard_search.h"
#include "../src/multiplayer/multiplayer_session_table.h"

#include <vector>
#include <string>

using namespace Airstrike::Multiplayer;

TestSummary runMultiplayerTests() {
    TestSummary summary;
    std::cout << "\n--- Running PL Multiplayer Integration & CO Tests ---\n";

    // 1. PL-CO1: Spatial Grid - Entity Insertion & Proximity
    {
        MultiplayerSpatialGrid grid;
        grid.insertEntity({1, "PLAYER_1", true, 100.0f, 100.0f, 15.0f});
        grid.insertEntity({10, "ENEMY_NEAR", false, 120.0f, 120.0f, 10.0f});
        grid.insertEntity({20, "ENEMY_FAR", false, 700.0f, 500.0f, 10.0f});

        std::vector<CombatEntity> nearby = grid.queryNearby(100.0f, 100.0f, 50.0f);
        bool foundNear = false;
        bool foundFar = false;
        for (size_t i = 0; i < nearby.size(); ++i) {
            if (nearby[i].id == 10) foundNear = true;
            if (nearby[i].id == 20) foundFar = true;
        }
        summary.record(foundNear && !foundFar, "PL-CO1: Spatial Grid filters out distant entities");
    }

    // 2. PL-CO1: Spatial Grid - Workload Reduction
    {
        MultiplayerSpatialGrid grid;
        // Populate 40 distributed entities across the 8x8 arena
        for (int i = 0; i < 40; ++i) {
            float x = static_cast<float>((i * 73) % 760 + 20);
            float y = static_cast<float>((i * 97) % 610 + 20);
            grid.insertEntity({i + 1, "ENTITY_" + std::to_string(i), false, x, y, 10.0f});
        }
        MultiplayerSpatialGrid::BenchmarkResult bench = grid.runCollisionReductionBenchmark();
        summary.record(bench.gridChecksRequired < bench.naiveChecksRequired, 
                       "PL-CO1: Spatial Partitioning reduces pairwise checks");
    }

    // 3. PL-CO2: Doubly Linked List - Append & Size
    {
        MultiplayerEventHistory history;
        std::vector<MatchEvent> sample = getSampleMatchEvents();
        for (size_t i = 0; i < sample.size(); ++i) {
            history.append(sample[i]);
        }
        summary.record(history.size() == sample.size(), "PL-CO2: Event History appends all sample events");
    }

    // 4. PL-CO2: Doubly Linked List - Bi-directional Traversal
    {
        MultiplayerEventHistory history;
        std::vector<MatchEvent> sample = getSampleMatchEvents();
        for (size_t i = 0; i < sample.size(); ++i) {
            history.append(sample[i]);
        }
        std::vector<MatchEvent> fwd = history.traverseForward();
        std::vector<MatchEvent> bwd = history.traverseBackward();
        summary.record(fwd.size() == bwd.size() && fwd.size() == sample.size(),
                       "PL-CO2: Bi-directional traversal yields identical element counts");
    }

    // 5. PL-CO2: Doubly Linked List - Remove Node by ID
    {
        MultiplayerEventHistory history;
        history.append(MatchEvent(1, "A", "p1", 100, 10, 0, 0));
        history.append(MatchEvent(2, "B", "p1", 200, 20, 0, 0));
        history.append(MatchEvent(3, "C", "p1", 300, 30, 0, 0));

        bool removed = history.removeById(2);
        std::vector<MatchEvent> fwd = history.traverseForward();
        bool found2 = false;
        for (size_t i = 0; i < fwd.size(); ++i) {
            if (fwd[i].eventId == 2) found2 = true;
        }
        summary.record(removed && history.size() == 2 && !found2,
                       "PL-CO2: Deletion removes target node and maintains node links");
    }

    // 6. PL-CO3: Circular Queue - FIFO Order
    {
        MultiplayerPacketQueue queue(3);
        queue.enqueue(MatchEvent(1, "P1", "u1", 10, 5, 0, 0));
        queue.enqueue(MatchEvent(2, "P2", "u1", 20, 10, 0, 0));

        MatchEvent out;
        bool deqSuccess = queue.dequeue(out);
        summary.record(deqSuccess && out.eventId == 1 && queue.size() == 1,
                       "PL-CO3: Circular Queue preserves strict FIFO ordering");
    }

    // 7. PL-CO3: Circular Queue - Capacity Overflow Protection
    {
        MultiplayerPacketQueue queue(2);
        queue.enqueue(MatchEvent(1, "P1", "u1", 10, 5, 0, 0));
        queue.enqueue(MatchEvent(2, "P2", "u1", 20, 10, 0, 0));
        bool overflowAttempt = queue.enqueue(MatchEvent(3, "P3", "u1", 30, 15, 0, 0));
        summary.record(!overflowAttempt && queue.isFull(),
                       "PL-CO3: Circular Queue rejects enqueues when buffer is at capacity");
    }

    // 8. PL-CO3: Circular Queue - Modulo Wraparound
    {
        MultiplayerPacketQueue queue(3);
        queue.enqueue(MatchEvent(1, "P1", "u1", 10, 5, 0, 0));
        queue.enqueue(MatchEvent(2, "P2", "u1", 20, 10, 0, 0));
        MatchEvent dummy;
        queue.dequeue(dummy); // Head advanced
        queue.enqueue(MatchEvent(3, "P3", "u1", 30, 15, 0, 0));
        queue.enqueue(MatchEvent(4, "P4", "u1", 40, 20, 0, 0)); // Wraps around index
        summary.record(queue.size() == 3 && queue.isFull(),
                       "PL-CO3: Modulo wraparound manages head and tail indices cleanly");
    }

    // 9. PL-CO4: QuickSort - Score Ordering
    {
        std::vector<MatchRecord> records = {
            {"m1", "p1", "p2", 300, 500, "p2"},
            {"m2", "p1", "p2", 100, 50, "p1"},
            {"m3", "p1", "p2", 800, 200, "p1"},
            {"m4", "p1", "p2", 400, 400, "DRAW"}
        };
        MultiplayerLeaderboardSearch::quickSortByScore(records, 0, static_cast<int>(records.size()) - 1);
        bool sorted = true;
        for (size_t i = 1; i < records.size(); ++i) {
            if (records[i - 1].highestScore > records[i].highestScore) {
                sorted = false;
                break;
            }
        }
        summary.record(sorted && records.back().highestScore == 800,
                       "PL-CO4: QuickSort accurately sorts match records in ascending order");
    }

    // 10. PL-CO4: Binary Search - Target Score Lookup
    {
        std::vector<MatchRecord> records = {
            {"m1", "p1", "p2", 100, 50, "p1"},
            {"m2", "p1", "p2", 300, 250, "p1"},
            {"m3", "p1", "p2", 400, 400, "DRAW"},
            {"m4", "p1", "p2", 800, 200, "p1"}
        };
        int idx = MultiplayerLeaderboardSearch::binarySearchByScore(records, 400);
        summary.record(idx == 2, "PL-CO4: Binary search finds correct index for target score");
    }

    // 11. PL-CO4: Binary Search - Duplicate Score Range
    {
        std::vector<MatchRecord> records = {
            {"m1", "p1", "p2", 100, 50, "p1"},
            {"m2", "p1", "p2", 500, 200, "p1"},
            {"m3", "p1", "p2", 500, 300, "p1"},
            {"m4", "p1", "p2", 500, 400, "p1"},
            {"m5", "p1", "p2", 900, 800, "p1"}
        };
        std::pair<int, int> range = MultiplayerLeaderboardSearch::findScoreRange(records, 500);
        int first = range.first;
        int last = range.second;
        summary.record(first == 1 && last == 3, "PL-CO4: Duplicate score range identifies contiguous bounds");
    }

    // 12. PL-CO5: Hash Table - Insert and Find
    {
        MultiplayerSessionTable table(8);
        PlayerSession s1("user_a", "room_1", "BROWSER", true, 25, 1000);
        table.insert("user_a", s1);
        const PlayerSession* found = table.find("user_a");
        summary.record(found != nullptr && found->roomId == "room_1",
                       "PL-CO5: Hash Table stores and retrieves session by string key");
    }

    // 13. PL-CO5: Hash Table - Tombstone Deletion & Probe Continuity
    {
        MultiplayerSessionTable table(4); // Small capacity to force collisions
        PlayerSession s1("u1", "r1", "BROWSER", true, 20, 100);
        PlayerSession s2("u2", "r1", "BROWSER", true, 20, 100);
        table.insert("u1", s1);
        table.insert("u2", s2);

        bool removed = table.remove("u1");
        const PlayerSession* foundU2 = table.find("u2");
        summary.record(removed && table.tombstoneCount() == 1 && foundU2 != nullptr,
                       "PL-CO5: Tombstone prevents broken probe sequences on subsequent keys");
    }

    // 14. PL-CO5: Hash Table - Automatic Dynamic Rehashing
    {
        MultiplayerSessionTable table(4);
        for (int i = 0; i < 10; ++i) {
            std::string id = "player_" + std::to_string(i);
            table.insert(id, PlayerSession(id, "r", "BROWSER", true, 10, 100));
        }
        summary.record(table.capacity() >= 16 && table.size() == 10 && table.contains("player_9"),
                       "PL-CO5: Hash Table dynamically rehashes and expands upon high load");
    }

    return summary;
}
