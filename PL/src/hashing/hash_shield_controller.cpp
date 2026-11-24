// ==============================================================================
// File      : PL/src/hashing/hash_shield_controller.cpp
// Subject   : PL-CO5 | Hashing Techniques
// Topics    : [TOPIC: HashShieldController] [TOPIC: Linear Probing] [TOPIC: Tombstone Deletion] [TOPIC: Rehashing]
// Game Role : Implements harmonic shield hashing, linear probing, tombstone removal, and rehashing.
// ==============================================================================

#include "hash_shield_controller.h"
#include <iomanip>

HashShieldController::HashShieldController(size_t initialCapacity)
    : tableCapacity(initialCapacity > 0 ? initialCapacity : INITIAL_CAPACITY),
      activeCount(0), tombstoneCount(0), totalProbeSteps(0) {
    table = new MatrixShieldSlot[tableCapacity];
}

HashShieldController::~HashShieldController() {
    delete[] table;
}

HashShieldController::HashShieldController(const HashShieldController& other)
    : tableCapacity(other.tableCapacity), activeCount(other.activeCount),
      tombstoneCount(other.tombstoneCount), totalProbeSteps(other.totalProbeSteps) {
    table = new MatrixShieldSlot[tableCapacity];
    for (size_t i = 0; i < tableCapacity; ++i) {
        table[i] = other.table[i];
    }
}

HashShieldController& HashShieldController::operator=(const HashShieldController& other) {
    if (this != &other) {
        delete[] table;
        tableCapacity = other.tableCapacity;
        activeCount = other.activeCount;
        tombstoneCount = other.tombstoneCount;
        totalProbeSteps = other.totalProbeSteps;
        table = new MatrixShieldSlot[tableCapacity];
        for (size_t i = 0; i < tableCapacity; ++i) {
            table[i] = other.table[i];
        }
    }
    return *this;
}

// [TOPIC: Polynomial Rolling Hash]
// Purpose: Hashes harmonic keys using base p=31 modulo tableCapacity per character.
// Game Role: Computes the initial shield matrix bucket for a harmonic frequency key.
size_t HashShieldController::hashFunction(const std::string& key) const {
    // WHY: Apply modulo tableCapacity at every character step to keep hashVal overflow-free.
    const size_t p = 31;
    size_t hashVal = 0;
    for (char c : key) {
        hashVal = (hashVal * p + static_cast<size_t>(static_cast<unsigned char>(c))) % tableCapacity;
    }
    return hashVal;
}

// [TOPIC: Rehashing]
// Purpose: Expands table capacity and re-inserts active harmonics while purging tombstones.
// Game Role: Rebuilds the harmonic shield matrix when active load exceeds MAX_LOAD_FACTOR.
void HashShieldController::rehash(size_t newCapacity) {
    size_t oldCapacity = tableCapacity;
    MatrixShieldSlot* oldTable = table;

    tableCapacity = newCapacity;
    table = new MatrixShieldSlot[tableCapacity];
    activeCount = 0;
    tombstoneCount = 0;

    for (size_t i = 0; i < oldCapacity; ++i) {
        // WHY: Re-insert only OCCUPIED slots so stale TOMBSTONE entries are cleared during resize.
        if (oldTable[i].status == MatrixSlotStatus::OCCUPIED) {
            activateHarmonic(oldTable[i].harmonicKey, oldTable[i].barrierStrength, oldTable[i].absorptionPercent);
        }
    }

    delete[] oldTable;
}

// [TOPIC: HashShieldController] [TOPIC: Linear Probing] [TOPIC: Load Factor]
// Purpose: Rehashes past 0.70 load, probes linearly with modulo wrap-around, and recycles tombstones.
// Game Role: Activates or updates a harmonic defense layer on the Matrix Craft enemy.
bool HashShieldController::activateHarmonic(const std::string& key, int barrierStrength, int absorptionPercent) {
    if (key.empty()) return false;

    // WHY: Trigger rehash to odd capacity (2*M + 1) when projected load exceeds 0.70 to limit clustering.
    if (static_cast<double>(activeCount + 1) / tableCapacity > MAX_LOAD_FACTOR) {
        rehash(tableCapacity * 2 + 1);
    }

    size_t initialIdx = hashFunction(key);
    size_t probeIdx = initialIdx;
    int firstTombstoneIdx = -1;
    int probes = 0;

    for (size_t step = 0; step < tableCapacity; ++step) {
        probes++;
        if (table[probeIdx].status == MatrixSlotStatus::OCCUPIED) {
            if (table[probeIdx].harmonicKey == key) {
                table[probeIdx].barrierStrength = barrierStrength;
                table[probeIdx].absorptionPercent = absorptionPercent;
                return true;
            }
        } else if (table[probeIdx].status == MatrixSlotStatus::TOMBSTONE) {
            // WHY: Record earliest tombstone for insertion only after confirming key isn't later in chain.
            if (firstTombstoneIdx == -1) {
                firstTombstoneIdx = static_cast<int>(probeIdx);
            }
        } else if (table[probeIdx].status == MatrixSlotStatus::EMPTY) {
            break;
        }
        probeIdx = (probeIdx + 1) % tableCapacity;
    }

    size_t insertSlot = (firstTombstoneIdx != -1) ? static_cast<size_t>(firstTombstoneIdx) : probeIdx;

    if (table[insertSlot].status == MatrixSlotStatus::TOMBSTONE) {
        tombstoneCount--;
    }

    table[insertSlot].status = MatrixSlotStatus::OCCUPIED;
    table[insertSlot].harmonicKey = key;
    table[insertSlot].barrierStrength = barrierStrength;
    table[insertSlot].absorptionPercent = absorptionPercent;
    table[insertSlot].initialHash = static_cast<int>(initialIdx);
    // WHY: Add tableCapacity before subtracting initialIdx to measure modular wrap-around displacement cleanly.
    table[insertSlot].probeDistance = static_cast<int>((insertSlot + tableCapacity - initialIdx) % tableCapacity);

    activeCount++;
    totalProbeSteps += probes;
    return true;
}

// [TOPIC: Linear Probing Search] [TOPIC: Tombstone Deletion]
// Purpose: Probes linearly across OCCUPIED/TOMBSTONE slots until harmonic match or EMPTY sentinel.
// Game Role: Checks whether an incoming weapon frequency triggers an active shield barrier.
bool HashShieldController::lookupHarmonic(const std::string& key, int& outBarrier, int& outAbsorption, int& outProbes) const {
    outProbes = 0;
    if (key.empty() || activeCount == 0) return false;

    size_t initialIdx = hashFunction(key);
    size_t probeIdx = initialIdx;

    for (size_t step = 0; step < tableCapacity; ++step) {
        outProbes++;
        if (table[probeIdx].status == MatrixSlotStatus::OCCUPIED) {
            if (table[probeIdx].harmonicKey == key) {
                outBarrier = table[probeIdx].barrierStrength;
                outAbsorption = table[probeIdx].absorptionPercent;
                return true;
            }
        } else if (table[probeIdx].status == MatrixSlotStatus::EMPTY) {
            // WHY: Only EMPTY terminates linear probing; TOMBSTONE slots must be stepped past.
            return false;
        }
        probeIdx = (probeIdx + 1) % tableCapacity;
    }

    return false;
}

// [TOPIC: Tombstone Deletion]
// Purpose: Marks a deactivated harmonic slot as TOMBSTONE rather than EMPTY to preserve probe chains.
// Game Role: Disables a shield frequency without hiding downstream colliding shield frequencies.
bool HashShieldController::deactivateHarmonic(const std::string& key) {
    if (key.empty() || activeCount == 0) return false;

    size_t initialIdx = hashFunction(key);
    size_t probeIdx = initialIdx;

    for (size_t step = 0; step < tableCapacity; ++step) {
        if (table[probeIdx].status == MatrixSlotStatus::OCCUPIED) {
            if (table[probeIdx].harmonicKey == key) {
                // WHY: Use TOMBSTONE instead of EMPTY so keys displaced past this slot remain searchable.
                table[probeIdx].status = MatrixSlotStatus::TOMBSTONE;
                table[probeIdx].harmonicKey = "";
                table[probeIdx].barrierStrength = 0;
                table[probeIdx].absorptionPercent = 0;
                activeCount--;
                tombstoneCount++;
                return true;
            }
        } else if (table[probeIdx].status == MatrixSlotStatus::EMPTY) {
            return false;
        }
        probeIdx = (probeIdx + 1) % tableCapacity;
    }

    return false;
}

bool HashShieldController::isSlotTombstone(size_t slotIndex) const {
    if (slotIndex >= tableCapacity) return false;
    return table[slotIndex].status == MatrixSlotStatus::TOMBSTONE;
}

bool HashShieldController::isSlotOccupied(size_t slotIndex) const {
    if (slotIndex >= tableCapacity) return false;
    return table[slotIndex].status == MatrixSlotStatus::OCCUPIED;
}

MatrixSlotStatus HashShieldController::getSlotStatus(size_t slotIndex) const {
    if (slotIndex >= tableCapacity) return MatrixSlotStatus::EMPTY;
    return table[slotIndex].status;
}

void HashShieldController::clear() {
    for (size_t i = 0; i < tableCapacity; ++i) {
        table[i] = MatrixShieldSlot();
    }
    activeCount = 0;
    tombstoneCount = 0;
    totalProbeSteps = 0;
}

void HashShieldController::printShieldMatrix() const {
    std::cout << "\n=== Hash Shield Matrix (" << activeCount << " active, "
              << tombstoneCount << " tombstones, cap: " << tableCapacity
              << ", load: " << std::fixed << std::setprecision(2) << getLoadFactor() << ") ===\n";
    std::cout << std::left << std::setw(6) << "Slot"
              << std::setw(12) << "Status"
              << std::setw(16) << "Harmonic Key"
              << std::setw(10) << "Barrier"
              << std::setw(12) << "Absorption"
              << std::setw(10) << "InitHash"
              << "ProbeDist\n";
    std::cout << "----------------------------------------------------------------------\n";
    for (size_t i = 0; i < tableCapacity; ++i) {
        std::string statusStr = "EMPTY";
        if (table[i].status == MatrixSlotStatus::OCCUPIED) statusStr = "OCCUPIED";
        else if (table[i].status == MatrixSlotStatus::TOMBSTONE) statusStr = "TOMBSTONE";

        std::cout << "[" << std::setw(4) << i << "] "
                  << std::setw(12) << statusStr;

        if (table[i].status == MatrixSlotStatus::OCCUPIED) {
            std::cout << std::setw(16) << table[i].harmonicKey
                      << std::setw(10) << table[i].barrierStrength
                      << std::setw(12) << (std::to_string(table[i].absorptionPercent) + "%")
                      << std::setw(10) << table[i].initialHash
                      << table[i].probeDistance;
        } else if (table[i].status == MatrixSlotStatus::TOMBSTONE) {
            std::cout << std::setw(16) << "[DELETED]"
                      << std::setw(10) << "--"
                      << std::setw(12) << "--"
                      << std::setw(10) << "--"
                      << "--";
        } else {
            std::cout << std::setw(16) << "--"
                      << std::setw(10) << "--"
                      << std::setw(12) << "--"
                      << std::setw(10) << "--"
                      << "--";
        }
        std::cout << "\n";
    }
}
