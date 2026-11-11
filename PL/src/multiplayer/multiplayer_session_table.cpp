#include "multiplayer_session_table.h"

namespace Airstrike::Multiplayer {

MultiplayerSessionTable::MultiplayerSessionTable(size_t capacity)
    : m_capacity(capacity < 8 ? 8 : capacity), m_size(0), m_tombstones(0) {
    m_table.resize(m_capacity);
}

size_t MultiplayerSessionTable::hashFunction(const std::string& key) const {
    // DJB2 hash algorithm
    unsigned long hash = 5381;
    for (char c : key) {
        hash = ((hash << 5) + hash) + static_cast<unsigned char>(c); // hash * 33 + c
    }
    return static_cast<size_t>(hash % m_capacity);
}

bool MultiplayerSessionTable::insert(const std::string& playerId, const PlayerSession& session) {
    if (playerId.empty()) return false;

    // Check if table is too full (load factor >= 0.75)
    if (m_size + m_tombstones >= m_capacity * 3 / 4) {
        // Rehash with double capacity
        size_t oldCap = m_capacity;
        std::vector<HashEntry> oldTable = std::move(m_table);
        m_capacity = oldCap * 2;
        m_table.assign(m_capacity, HashEntry());
        m_size = 0;
        m_tombstones = 0;

        for (size_t i = 0; i < oldCap; ++i) {
            if (oldTable[i].state == EntryState::OCCUPIED) {
                insert(oldTable[i].key, oldTable[i].value);
            }
        }
    }

    size_t index = hashFunction(playerId);
    int firstTombstoneIndex = -1;

    for (size_t i = 0; i < m_capacity; ++i) {
        size_t probeIndex = (index + i) % m_capacity;

        if (m_table[probeIndex].state == EntryState::OCCUPIED) {
            if (m_table[probeIndex].key == playerId) {
                // Update existing
                m_table[probeIndex].value = session;
                return true;
            }
        } else if (m_table[probeIndex].state == EntryState::TOMBSTONE) {
            if (firstTombstoneIndex == -1) {
                firstTombstoneIndex = static_cast<int>(probeIndex);
            }
        } else if (m_table[probeIndex].state == EntryState::EMPTY) {
            // Found empty slot. Insert into tombstone if found earlier, otherwise probeIndex.
            size_t insertIndex = (firstTombstoneIndex != -1) ? static_cast<size_t>(firstTombstoneIndex) : probeIndex;
            if (firstTombstoneIndex != -1) {
                --m_tombstones;
            }
            m_table[insertIndex].key = playerId;
            m_table[insertIndex].value = session;
            m_table[insertIndex].state = EntryState::OCCUPIED;
            ++m_size;
            return true;
        }
    }

    // If loop ended and tombstone was found
    if (firstTombstoneIndex != -1) {
        size_t insertIndex = static_cast<size_t>(firstTombstoneIndex);
        --m_tombstones;
        m_table[insertIndex].key = playerId;
        m_table[insertIndex].value = session;
        m_table[insertIndex].state = EntryState::OCCUPIED;
        ++m_size;
        return true;
    }

    return false;
}

const PlayerSession* MultiplayerSessionTable::find(const std::string& playerId) const {
    if (playerId.empty() || m_size == 0) return nullptr;

    size_t index = hashFunction(playerId);

    for (size_t i = 0; i < m_capacity; ++i) {
        size_t probeIndex = (index + i) % m_capacity;

        if (m_table[probeIndex].state == EntryState::EMPTY) {
            return nullptr; // Not found
        }
        if (m_table[probeIndex].state == EntryState::OCCUPIED && m_table[probeIndex].key == playerId) {
            return &m_table[probeIndex].value;
        }
    }
    return nullptr;
}

PlayerSession* MultiplayerSessionTable::find(const std::string& playerId) {
    if (playerId.empty() || m_size == 0) return nullptr;

    size_t index = hashFunction(playerId);

    for (size_t i = 0; i < m_capacity; ++i) {
        size_t probeIndex = (index + i) % m_capacity;

        if (m_table[probeIndex].state == EntryState::EMPTY) {
            return nullptr;
        }
        if (m_table[probeIndex].state == EntryState::OCCUPIED && m_table[probeIndex].key == playerId) {
            return &m_table[probeIndex].value;
        }
    }
    return nullptr;
}

bool MultiplayerSessionTable::remove(const std::string& playerId) {
    if (playerId.empty() || m_size == 0) return false;

    size_t index = hashFunction(playerId);

    for (size_t i = 0; i < m_capacity; ++i) {
        size_t probeIndex = (index + i) % m_capacity;

        if (m_table[probeIndex].state == EntryState::EMPTY) {
            return false;
        }
        if (m_table[probeIndex].state == EntryState::OCCUPIED && m_table[probeIndex].key == playerId) {
            m_table[probeIndex].state = EntryState::TOMBSTONE;
            m_table[probeIndex].key = "";
            --m_size;
            ++m_tombstones;
            return true;
        }
    }
    return false;
}

bool MultiplayerSessionTable::contains(const std::string& playerId) const {
    return find(playerId) != nullptr;
}

void MultiplayerSessionTable::clear() {
    for (auto& entry : m_table) {
        entry.state = EntryState::EMPTY;
        entry.key = "";
    }
    m_size = 0;
    m_tombstones = 0;
}

} // namespace Airstrike::Multiplayer
