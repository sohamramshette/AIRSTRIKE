// ==============================================================================
// File      : PL/src/hashing/hash_table_probing.h
// Subject   : PL-CO5 | Hashing Techniques
// Topics    : [TOPIC: HashTableProbing] [TOPIC: Linear Probing] [TOPIC: Tombstone Deletion] [TOPIC: Rehashing]
// Game Role : Caches AirStrike ship upgrades using cache-friendly open addressing.
// ==============================================================================

#ifndef HASH_TABLE_PROBING_H
#define HASH_TABLE_PROBING_H

#include <iostream>
#include <string>
#include "hash_table_chaining.h"

// [TOPIC: Tombstone Deletion] [TOPIC: SlotStatus]
// Purpose: Distinguishes EMPTY, OCCUPIED, and DELETED (tombstone) slots so open-address probe chains stay intact.
// Game Role: Keeps colliding ship-upgrade keys searchable after earlier items are removed.
enum class SlotStatus {
    EMPTY,
    OCCUPIED,
    DELETED
};

// [TOPIC: ProbeEntry]
// Purpose: Flat table slot storing slot state, key, item payload, and initial base hash for offset tracking.
// Game Role: Packs upgrade cache entries contiguously in memory to minimize cache misses.
struct ProbeEntry {
    SlotStatus status;
    std::string key;
    InventoryItem item;
    int initialHash;

    ProbeEntry() : status(SlotStatus::EMPTY), key(""), initialHash(-1) {}
};

// [TOPIC: HashTableProbing] [TOPIC: Linear Probing] [TOPIC: Open Addressing] [TOPIC: Rehashing]
// Purpose: Open-addressing hash table resolving collisions via (h(k) + i) % M, tombstones, and alpha >= 0.70 rehash.
// Game Role: Implements the AirStrike Space Fighter Asset & Upgrade Cache with probe-step telemetry.
class HashTableProbing {
public:
    static constexpr size_t INITIAL_CAPACITY = 7;
    static constexpr double MAX_LOAD_FACTOR = 0.70;

private:
    ProbeEntry* table;
    size_t tableCapacity;
    size_t activeCount;
    int totalProbeSteps;

    size_t hashFunction(const std::string& key) const;
    void rehash(size_t newCapacity);

public:
    explicit HashTableProbing(size_t initialCapacity = INITIAL_CAPACITY);
    ~HashTableProbing();

    HashTableProbing(const HashTableProbing& other);
    HashTableProbing& operator=(const HashTableProbing& other);

    // [TOPIC: Open Addressing Operations]
    // Purpose: Inserts with tombstone reuse/rehash, probes linearly on lookup, or marks DELETED tombstones.
    // Game Role: Stores, retrieves, and retires ship upgrade assets while reporting probe cost.
    bool insert(const std::string& key, const InventoryItem& item);
    bool search(const std::string& key, InventoryItem& outItem, int& outProbes) const;
    bool search(const std::string& key, InventoryItem& outItem) const;
    bool remove(const std::string& key);

    // Metrics
    double getLoadFactor() const { return static_cast<double>(activeCount) / tableCapacity; }
    size_t size() const { return activeCount; }
    size_t getCapacity() const { return tableCapacity; }
    int getTotalProbeSteps() const { return totalProbeSteps; }

    void clear();
    void printTable() const;
};

#endif // HASH_TABLE_PROBING_H

