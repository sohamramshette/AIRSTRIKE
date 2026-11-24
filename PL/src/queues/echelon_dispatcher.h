// ==============================================================================
// File      : PL/src/queues/echelon_dispatcher.h
// Subject   : PL-CO3 | Stack and Queue Applications
// Topics    : [TOPIC: EchelonDispatcher] [TOPIC: Circular Queue] [TOPIC: FIFO] [TOPIC: Modulo Wrap-Around]
// Game Role : Dispatches PL-CO3 Queue Wave Echelon squadrons in FIFO order with wrap-around tracking.
// ==============================================================================

#ifndef ECHELON_DISPATCHER_H
#define ECHELON_DISPATCHER_H

#include <iostream>
#include <string>

// [TOPIC: EchelonWaveUnit Record]
// Purpose: Holds an enemy echelon squadron's ID, codename, size, speed, delay, and deployment flag.
// Game Role: Represents a single tactical echelon unit buffered inside QueueWaveEchelon.
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

// [TOPIC: QueueWaveEchelon] [TOPIC: EchelonDispatcher] [TOPIC: Circular Queue]
// Purpose: Circular array queue enforcing FIFO echelon dispatch with overflow/underflow/wraparound telemetry.
// Game Role: Controls sequential rank deployment for the PL-CO3 Queue Wave Echelon enemy mechanic.
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

    // [TOPIC: Circular Buffer Deep Copy]
    // Purpose: Copies queued echelon units in FIFO sequence into a normalized contiguous buffer.
    // Game Role: Duplicates pending echelon queues safely.
    QueueWaveEchelon(const QueueWaveEchelon& other);
    QueueWaveEchelon& operator=(const QueueWaveEchelon& other);

    // [TOPIC: FIFO Echelon Operations]
    // Purpose: Enqueues incoming echelons, deploys the front echelon, or peeks next launch in O(1).
    // Game Role: Releases attacking squadrons in deterministic FIFO order.
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
