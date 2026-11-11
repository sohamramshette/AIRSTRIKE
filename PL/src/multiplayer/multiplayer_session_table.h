#ifndef AIRSTRIKE_MULTIPLAYER_SESSION_TABLE_H
#define AIRSTRIKE_MULTIPLAYER_SESSION_TABLE_H

#include <string>
#include <vector>

namespace Airstrike::Multiplayer {

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

/**
 * ==============================================================================
 * Demonstrator for PL-CO5: Hash Tables & Open Addressing
 *
 * Implements a genuine open-addressing hash table with:
 * - Linear probing for collision resolution: (hash + i) % capacity
 * - Tombstones for safe deletions without breaking probe chains
 * - Polynomial string hash function (djb2)
 * - Load factor calculation and stats
 * ==============================================================================
 */
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
