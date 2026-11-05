#ifndef HASH_TABLE_PROBING_H
#define HASH_TABLE_PROBING_H

#include <iostream>
#include <string>
#include "hash_table_chaining.h"

/**
 * Hash Table with Open Addressing (Linear Probing) (PL-CO5).
 * Application: Space Fighter Ship Asset & Upgrade Cache.
 *
 * Collision Resolution: Linear Probing with Tombstone marker deletion pattern.
 * Features:
 * - Polynomial rolling hash function
 * - Slot states: EMPTY, OCCUPIED, DELETED (Tombstone)
 * - Load factor calculation
 * - Dynamic rehashing when load factor > 0.70
 * - Probe distance metrics for clustering demonstration
 */
enum class SlotStatus {
    EMPTY,
    OCCUPIED,
    DELETED
};

struct ProbeEntry {
    SlotStatus status;
    std::string key;
    InventoryItem item;
    int initialHash;

    ProbeEntry() : status(SlotStatus::EMPTY), key(""), initialHash(-1) {}
};

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

    // Prevent inadvertent shallow copy
    HashTableProbing(const HashTableProbing& other);
    HashTableProbing& operator=(const HashTableProbing& other);

    // Operations
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
