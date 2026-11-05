#include "hash_table_probing.h"
#include <iomanip>

HashTableProbing::HashTableProbing(size_t initialCapacity)
    : tableCapacity(initialCapacity > 0 ? initialCapacity : INITIAL_CAPACITY),
      activeCount(0), totalProbeSteps(0) {
    table = new ProbeEntry[tableCapacity];
}

HashTableProbing::~HashTableProbing() {
    delete[] table;
}

HashTableProbing::HashTableProbing(const HashTableProbing& other)
    : tableCapacity(other.tableCapacity), activeCount(0), totalProbeSteps(0) {
    table = new ProbeEntry[tableCapacity];
    for (size_t i = 0; i < other.tableCapacity; ++i) {
        if (other.table[i].status == SlotStatus::OCCUPIED) {
            insert(other.table[i].key, other.table[i].item);
        }
    }
}

HashTableProbing& HashTableProbing::operator=(const HashTableProbing& other) {
    if (this != &other) {
        delete[] table;
        tableCapacity = other.tableCapacity;
        activeCount = 0;
        totalProbeSteps = 0;
        table = new ProbeEntry[tableCapacity];
        for (size_t i = 0; i < other.tableCapacity; ++i) {
            if (other.table[i].status == SlotStatus::OCCUPIED) {
                insert(other.table[i].key, other.table[i].item);
            }
        }
    }
    return *this;
}

// Polynomial rolling hash: hash = (hash * 31 + c) % capacity
size_t HashTableProbing::hashFunction(const std::string& key) const {
    unsigned long hash = 0;
    const unsigned long p = 31;
    for (char c : key) {
        hash = (hash * p + static_cast<unsigned char>(c)) % tableCapacity;
    }
    return hash;
}

void HashTableProbing::rehash(size_t newCapacity) {
    size_t oldCapacity = tableCapacity;
    ProbeEntry* oldTable = table;

    tableCapacity = newCapacity;
    table = new ProbeEntry[tableCapacity];
    activeCount = 0;
    totalProbeSteps = 0;

    for (size_t i = 0; i < oldCapacity; ++i) {
        if (oldTable[i].status == SlotStatus::OCCUPIED) {
            insert(oldTable[i].key, oldTable[i].item);
        }
    }

    delete[] oldTable;
}

bool HashTableProbing::insert(const std::string& key, const InventoryItem& item) {
    if (getLoadFactor() >= MAX_LOAD_FACTOR) {
        rehash(tableCapacity * 2 + 1);
    }

    size_t baseHash = hashFunction(key);
    int firstTombstone = -1;

    for (size_t step = 0; step < tableCapacity; ++step) {
        size_t idx = (baseHash + step) % tableCapacity;
        totalProbeSteps++;

        if (table[idx].status == SlotStatus::OCCUPIED) {
            if (table[idx].key == key) {
                table[idx].item = item; // Update existing
                return true;
            }
        } else if (table[idx].status == SlotStatus::DELETED) {
            if (firstTombstone == -1) {
                firstTombstone = static_cast<int>(idx);
            }
        } else if (table[idx].status == SlotStatus::EMPTY) {
            // Use earlier tombstone if encountered
            size_t insertIdx = (firstTombstone != -1) ? static_cast<size_t>(firstTombstone) : idx;
            table[insertIdx].status = SlotStatus::OCCUPIED;
            table[insertIdx].key = key;
            table[insertIdx].item = item;
            table[insertIdx].initialHash = static_cast<int>(baseHash);
            activeCount++;
            return true;
        }
    }

    // Table was full of tombstones and occupied slots, use tombstone if available
    if (firstTombstone != -1) {
        size_t insertIdx = static_cast<size_t>(firstTombstone);
        table[insertIdx].status = SlotStatus::OCCUPIED;
        table[insertIdx].key = key;
        table[insertIdx].item = item;
        table[insertIdx].initialHash = static_cast<int>(baseHash);
        activeCount++;
        return true;
    }

    return false;
}

bool HashTableProbing::search(const std::string& key, InventoryItem& outItem, int& outProbes) const {
    size_t baseHash = hashFunction(key);
    outProbes = 0;

    for (size_t step = 0; step < tableCapacity; ++step) {
        size_t idx = (baseHash + step) % tableCapacity;
        outProbes++;

        if (table[idx].status == SlotStatus::EMPTY) {
            return false; // Terminate search on empty slot
        }
        if (table[idx].status == SlotStatus::OCCUPIED && table[idx].key == key) {
            outItem = table[idx].item;
            return true;
        }
    }
    return false;
}

bool HashTableProbing::search(const std::string& key, InventoryItem& outItem) const {
    int dummy = 0;
    return search(key, outItem, dummy);
}

bool HashTableProbing::remove(const std::string& key) {
    size_t baseHash = hashFunction(key);

    for (size_t step = 0; step < tableCapacity; ++step) {
        size_t idx = (baseHash + step) % tableCapacity;
        if (table[idx].status == SlotStatus::EMPTY) {
            return false;
        }
        if (table[idx].status == SlotStatus::OCCUPIED && table[idx].key == key) {
            table[idx].status = SlotStatus::DELETED; // Mark tombstone
            table[idx].key = "";
            activeCount--;
            return true;
        }
    }
    return false;
}

void HashTableProbing::clear() {
    for (size_t i = 0; i < tableCapacity; ++i) {
        table[i].status = SlotStatus::EMPTY;
        table[i].key = "";
        table[i].initialHash = -1;
    }
    activeCount = 0;
    totalProbeSteps = 0;
}

void HashTableProbing::printTable() const {
    std::cout << "\n=== Hash Table: Linear Probing (Items: " << activeCount
              << ", Capacity: " << tableCapacity
              << ", Load Factor: " << std::fixed << std::setprecision(2) << getLoadFactor()
              << ") ===\n";
    std::cout << std::left << std::setw(6) << "Slot"
              << std::setw(12) << "State"
              << std::setw(12) << "Base Hash"
              << std::setw(8) << "Offset"
              << std::setw(24) << "Key"
              << "Item Name [Bonus]\n";
    std::cout << "---------------------------------------------------------------------------------\n";

    for (size_t i = 0; i < tableCapacity; ++i) {
        std::cout << "[" << std::setw(4) << i << "] ";
        if (table[i].status == SlotStatus::EMPTY) {
            std::cout << std::setw(12) << "EMPTY"
                      << std::setw(12) << "-"
                      << std::setw(8) << "-"
                      << std::setw(24) << "-"
                      << "-\n";
        } else if (table[i].status == SlotStatus::DELETED) {
            std::cout << std::setw(12) << "TOMBSTONE"
                      << std::setw(12) << "-"
                      << std::setw(8) << "-"
                      << std::setw(24) << "<deleted>"
                      << "-\n";
        } else {
            int offset = (static_cast<int>(i) - table[i].initialHash + static_cast<int>(tableCapacity)) % static_cast<int>(tableCapacity);
            std::cout << std::setw(12) << "OCCUPIED"
                      << std::setw(12) << table[i].initialHash
                      << "+" << std::setw(7) << offset
                      << std::setw(24) << table[i].key
                      << table[i].item.name << " [Atk +" << table[i].item.attackBonus << "]\n";
        }
    }
}
