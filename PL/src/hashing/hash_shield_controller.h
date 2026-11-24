// ==============================================================================
// File      : PL/src/hashing/hash_shield_controller.h
// Subject   : PL-CO5 | Hashing Techniques
// Topics    : [TOPIC: HashShieldController] [TOPIC: Linear Probing] [TOPIC: Tombstone Deletion] [TOPIC: Rehashing]
// Game Role : Drives the PL-CO5 Hash-Shielded Matrix Craft enemy's rotational harmonic defense table.
// ==============================================================================

#ifndef HASH_SHIELD_CONTROLLER_H
#define HASH_SHIELD_CONTROLLER_H

#include <iostream>
#include <string>

// [TOPIC: MatrixSlotStatus] [TOPIC: Tombstone Deletion]
// Purpose: Enumerates open-address slot states (EMPTY, OCCUPIED, TOMBSTONE) for harmonic barrier lookup.
// Game Role: Ensures broken shield frequencies do not sever colliding probe chains.
enum class MatrixSlotStatus {
    EMPTY,
    OCCUPIED,
    TOMBSTONE
};

// [TOPIC: MatrixShieldSlot]
// Purpose: Stores a harmonic shield key, strength, absorption, initial hash, and probe displacement.
// Game Role: Tracks one rotational shield frequency slot inside HashShieldController.
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

// [TOPIC: HashShieldController] [TOPIC: Open Addressing] [TOPIC: Linear Probing] [TOPIC: Rehashing]
// Purpose: Open-addressing table with rolling hash, linear probing, tombstone markers, and dynamic rehashing.
// Game Role: Manages active harmonic barriers on the PL-CO5 Hash-Shielded Matrix Craft enemy.
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

    HashShieldController(const HashShieldController& other);
    HashShieldController& operator=(const HashShieldController& other);

    // [TOPIC: Harmonic Shield Hash Operations]
    // Purpose: Activates harmonics with rehash/probing, queries barriers, and deactivates via tombstones.
    // Game Role: Rotates, verifies, and disrupts enemy shield harmonics during combat.
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
