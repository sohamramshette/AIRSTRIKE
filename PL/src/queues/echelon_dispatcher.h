#ifndef ECHELON_DISPATCHER_H
#define ECHELON_DISPATCHER_H

#include <iostream>
#include <string>

/**
 * Queue Wave Echelon (PL-CO3).
 *
 * Demonstrates:
 * - Circular Queue implementation using static/dynamic array
 * - True FIFO (First-In, First-Out) dispatch semantics
 * - Pointer / Index Wraparound arithmetic: (index + 1) % capacity
 * - Underflow and Overflow boundary enforcement
 * - Explicit wraparound state verification (rearIndex < frontIndex)
 */
struct EchelonWaveUnit {
    int echelonId;
    std::string codename;
    int craftCount;
    double flightSpeed;
    double spawnDelaySec;
    bool deployed;

    EchelonWaveUnit()
        : echelonId(0), codename(""), craftCount(0), flightSpeed(0.0), spawnDelaySec(0.0), deployed(false) {}
    EchelonWaveUnit(int id, const std::string& name, int count, double spd, double delay)
        : echelonId(id), codename(name), craftCount(count), flightSpeed(spd), spawnDelaySec(delay), deployed(false) {}
};

class QueueWaveEchelon {
public:
    static constexpr int DEFAULT_CAPACITY = 6;

private:
    EchelonWaveUnit* buffer;
    int frontIndex;
    int rearIndex;
    int currentCount;
    int maxCapacity;

public:
    explicit QueueWaveEchelon(int capacity = DEFAULT_CAPACITY);
    ~QueueWaveEchelon();

    // Deep copy operations
    QueueWaveEchelon(const QueueWaveEchelon& other);
    QueueWaveEchelon& operator=(const QueueWaveEchelon& other);

    // Core Queue Operations
    bool enqueue(const EchelonWaveUnit& unit);
    bool dequeue(EchelonWaveUnit& outUnit);
    bool peek(EchelonWaveUnit& outUnit) const;

    // State & Boundary Queries
    bool isEmpty() const { return currentCount == 0; }
    bool isFull() const { return currentCount == maxCapacity; }
    int size() const { return currentCount; }
    int capacity() const { return maxCapacity; }

    int getFrontIndex() const { return frontIndex; }
    int getRearIndex() const { return rearIndex; }
    bool isWrappedAround() const { return currentCount > 0 && rearIndex < frontIndex; }

    void clear();
    void printEchelonQueue() const;
};

#endif // ECHELON_DISPATCHER_H
