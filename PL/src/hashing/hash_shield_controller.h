#ifndef HASH_SHIELD_CONTROLLER_H
#define HASH_SHIELD_CONTROLLER_H

#include <iostream>
#include <string>

/**
 * Hash-Shielded Matrix Craft (PL-CO5).
 *
 * Demonstrates:
 * - Hash Table with Open Addressing & Linear Probing
 * - Polynomial Rolling Hash Function
 * - Collision Handling: When two harmonic keys hash to the same bucket,
 *   subsequent probe slots are checked sequentially.
 * - Tombstone Deletion Pattern: Disrupting/deactivating a harmonic marks the slot
 *   as TOMBSTONE so that linear probing chains for colliding keys remain unbroken.
 * - Dynamic Rehashing when load factor exceeds MAX_LOAD_FACTOR (0.70).
 */
enum class MatrixSlotStatus {
    EMPTY,
    OCCUPIED,
    TOMBSTONE
};

struct MatrixShieldSlot {
    MatrixSlotStatus status;
    std::string harmonicKey;
    int barrierStrength;
    int absorptionPercent;
    int initialHash;
    int probeDistance;

    MatrixShieldSlot()
        : status(MatrixSlotStatus::EMPTY), harmonicKey(""), barrierStrength(0),
          absorptionPercent(0), initialHash(-1), probeDistance(0) {}
};

class HashShieldController {
public:
    static constexpr size_t INITIAL_CAPACITY = 7;
    static constexpr double MAX_LOAD_FACTOR = 0.70;

private:
    MatrixShieldSlot* table;
    size_t tableCapacity;
    size_t activeCount;
    size_t tombstoneCount;
    int totalProbeSteps;

    size_t hashFunction(const std::string& key) const;
    void rehash(size_t newCapacity);

public:
    explicit HashShieldController(size_t initialCapacity = INITIAL_CAPACITY);
    ~HashShieldController();

    // Deep copy operations
    HashShieldController(const HashShieldController& other);
    HashShieldController& operator=(const HashShieldController& other);

    // Core Hash Operations (PL-CO5)
    bool activateHarmonic(const std::string& key, int barrierStrength, int absorptionPercent);
    bool lookupHarmonic(const std::string& key, int& outBarrier, int& outAbsorption, int& outProbes) const;
    bool deactivateHarmonic(const std::string& key); // Tombstone deletion

    // Collision & Probing verification
    int computeInitialIndex(const std::string& key) const { return static_cast<int>(hashFunction(key)); }
    bool isSlotTombstone(size_t slotIndex) const;
    bool isSlotOccupied(size_t slotIndex) const;
    MatrixSlotStatus getSlotStatus(size_t slotIndex) const;

    // Metrics
    double getLoadFactor() const { return static_cast<double>(activeCount) / tableCapacity; }
    size_t size() const { return activeCount; }
    size_t getTombstoneCount() const { return tombstoneCount; }
    size_t getCapacity() const { return tableCapacity; }
    int getTotalProbeSteps() const { return totalProbeSteps; }

    void clear();
    void printShieldMatrix() const;
};

#endif // HASH_SHIELD_CONTROLLER_H
