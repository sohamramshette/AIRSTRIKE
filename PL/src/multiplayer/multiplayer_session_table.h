// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_session_table.h
// Subject   : PL-CO5 | Hashing Techniques
// Topics    : [TOPIC: MultiplayerSessionTable] [TOPIC: Open Addressing] [TOPIC: Linear Probing] [TOPIC: Tombstone Deletion]
// Game Role : Tracks active multiplayer player sessions by playerId with O(1) lookup and tombstones.
// ==============================================================================

#ifndef AIRSTRIKE_MULTIPLAYER_SESSION_TABLE_H
#define AIRSTRIKE_MULTIPLAYER_SESSION_TABLE_H

#include <string>
#include <vector>

namespace Airstrike::Multiplayer {

// [TOPIC: PlayerSession Record]
// Purpose: Holds a connected pilot's room assignment, client type, readiness, ping, and heartbeat.
// Game Role: Represents an active multiplayer lobby/match session entry.
struct PlayerSession {
    std::string playerId;
    std::string roomId;
    std::string clientType; // "BROWSER", "JAVA_SWING"
    bool isReady;
    int pingMs;
    long long lastHeartbeat;

    PlayerSession()
        : playerId(""), roomId(""), clientType(""), isReady(false), pingMs(0), lastHeartbeat(0) {}

    PlayerSession(const std::string& pId, const std::string& rId, const std::string& cType,
                  bool ready, int ping, long long heartbeat)
        : playerId(pId), roomId(rId), clientType(cType), isReady(ready),
          pingMs(ping), lastHeartbeat(heartbeat) {}
};

enum class EntryState {
    EMPTY,
    OCCUPIED,
    TOMBSTONE
};

struct HashEntry {
    std::string key;
    PlayerSession value;
    EntryState state;

    HashEntry() : key(""), value(), state(EntryState::EMPTY) {}
};

// [TOPIC: MultiplayerSessionTable] [TOPIC: Open Addressing] [TOPIC: Linear Probing] [TOPIC: Tombstone Deletion] [TOPIC: Rehashing]
// Purpose: Open-addressing hash map using DJB2 hashing, linear probing, tombstones, and 2x rehashing at 0.75 load.
// Game Role: Resolves live AirStrike multiplayer sessions by playerId without breaking probe chains on disconnect.
class MultiplayerSessionTable {
public:
    explicit MultiplayerSessionTable(size_t capacity = 16);

    // Core dictionary operations
    bool insert(const std::string& playerId, const PlayerSession& session);
    const PlayerSession* find(const std::string& playerId) const;
    PlayerSession* find(const std::string& playerId);
    bool remove(const std::string& playerId);
    bool contains(const std::string& playerId) const;

    // Table statistics
    size_t size() const { return m_size; }
    size_t capacity() const { return m_capacity; }
    size_t tombstoneCount() const { return m_tombstones; }
    double loadFactor() const { return static_cast<double>(m_size + m_tombstones) / static_cast<double>(m_capacity); }

    void clear();

private:
    size_t hashFunction(const std::string& key) const;

    size_t m_capacity;
    size_t m_size;
    size_t m_tombstones;
    std::vector<HashEntry> m_table;
};

} // namespace Airstrike::Multiplayer

#endif // AIRSTRIKE_MULTIPLAYER_SESSION_TABLE_H
