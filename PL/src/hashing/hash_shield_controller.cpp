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

size_t HashShieldController::hashFunction(const std::string& key) const {
    // Polynomial rolling hash: sum(key[i] * 31^(len - 1 - i)) % tableCapacity
    const size_t p = 31;
    size_t hashVal = 0;
    for (char c : key) {
        hashVal = (hashVal * p + static_cast<size_t>(static_cast<unsigned char>(c))) % tableCapacity;
    }
    return hashVal;
}

void HashShieldController::rehash(size_t newCapacity) {
    size_t oldCapacity = tableCapacity;
    MatrixShieldSlot* oldTable = table;

    tableCapacity = newCapacity;
    table = new MatrixShieldSlot[tableCapacity];
    activeCount = 0;
    tombstoneCount = 0;

    for (size_t i = 0; i < oldCapacity; ++i) {
        if (oldTable[i].status == MatrixSlotStatus::OCCUPIED) {
            activateHarmonic(oldTable[i].harmonicKey, oldTable[i].barrierStrength, oldTable[i].absorptionPercent);
        }
    }

    delete[] oldTable;
}

bool HashShieldController::activateHarmonic(const std::string& key, int barrierStrength, int absorptionPercent) {
    if (key.empty()) return false;

    // Check load factor (including tombstones)
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
                // Key already exists, update harmonic
                table[probeIdx].barrierStrength = barrierStrength;
                table[probeIdx].absorptionPercent = absorptionPercent;
                return true;
            }
        } else if (table[probeIdx].status == MatrixSlotStatus::TOMBSTONE) {
            if (firstTombstoneIdx == -1) {
                firstTombstoneIdx = static_cast<int>(probeIdx);
            }
        } else if (table[probeIdx].status == MatrixSlotStatus::EMPTY) {
            // Found empty slot
            break;
        }
        probeIdx = (probeIdx + 1) % tableCapacity;
    }

    // Determine target slot (prefer recycled tombstone if available)
    size_t insertSlot = (firstTombstoneIdx != -1) ? static_cast<size_t>(firstTombstoneIdx) : probeIdx;

    if (table[insertSlot].status == MatrixSlotStatus::TOMBSTONE) {
        tombstoneCount--;
    }

    table[insertSlot].status = MatrixSlotStatus::OCCUPIED;
    table[insertSlot].harmonicKey = key;
    table[insertSlot].barrierStrength = barrierStrength;
    table[insertSlot].absorptionPercent = absorptionPercent;
    table[insertSlot].initialHash = static_cast<int>(initialIdx);
    table[insertSlot].probeDistance = static_cast<int>((insertSlot + tableCapacity - initialIdx) % tableCapacity);

    activeCount++;
    totalProbeSteps += probes;
    return true;
}

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
            // Empty slot terminates search in open addressing
            return false;
        }
        // TOMBSTONE slots are intentionally skipped so subsequent keys remain reachable
        probeIdx = (probeIdx + 1) % tableCapacity;
    }

    return false;
}

bool HashShieldController::deactivateHarmonic(const std::string& key) {
    if (key.empty() || activeCount == 0) return false;

    size_t initialIdx = hashFunction(key);
    size_t probeIdx = initialIdx;

    for (size_t step = 0; step < tableCapacity; ++step) {
        if (table[probeIdx].status == MatrixSlotStatus::OCCUPIED) {
            if (table[probeIdx].harmonicKey == key) {
                // Apply Tombstone Marker
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
