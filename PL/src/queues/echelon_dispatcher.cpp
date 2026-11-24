// ==============================================================================
// File      : PL/src/queues/echelon_dispatcher.cpp
// Subject   : PL-CO3 | Stack and Queue Applications
// Topics    : [TOPIC: EchelonDispatcher] [TOPIC: Circular Queue] [TOPIC: Modulo Wrap-Around] [TOPIC: FIFO]
// Game Role : Implements circular FIFO echelon scheduling with wrap-around state detection.
// ==============================================================================

#include "echelon_dispatcher.h"
#include <iomanip>

QueueWaveEchelon::QueueWaveEchelon(int capacity)
    : frontIndex(0), rearIndex(-1), currentCount(0),
      maxCapacity(capacity > 0 ? capacity : DEFAULT_CAPACITY) {
    buffer = new EchelonWaveUnit[maxCapacity];
}

QueueWaveEchelon::~QueueWaveEchelon() {
    delete[] buffer;
}

// [TOPIC: Circular Buffer Copy]
// Purpose: Unrolls wrapped circular echelon slots in FIFO order into a fresh buffer.
// Game Role: Preserves exact echelon launch order across copied dispatchers.
QueueWaveEchelon::QueueWaveEchelon(const QueueWaveEchelon& other)
    : frontIndex(0), rearIndex(other.currentCount - 1), currentCount(other.currentCount),
      maxCapacity(other.maxCapacity) {
    buffer = new EchelonWaveUnit[maxCapacity];
    for (int i = 0; i < other.currentCount; ++i) {
        // WHY: Translate wrapped offset i relative to other.frontIndex using modulo capacity.
        int idx = (other.frontIndex + i) % other.maxCapacity;
        buffer[i] = other.buffer[idx];
    }
}

QueueWaveEchelon& QueueWaveEchelon::operator=(const QueueWaveEchelon& other) {
    if (this != &other) {
        delete[] buffer;
        frontIndex = 0;
        rearIndex = other.currentCount - 1;
        currentCount = other.currentCount;
        maxCapacity = other.maxCapacity;
        buffer = new EchelonWaveUnit[maxCapacity];
        for (int i = 0; i < other.currentCount; ++i) {
            // WHY: Normalize wrapped buffer indices starting from 0 in the destination copy.
            int idx = (other.frontIndex + i) % other.maxCapacity;
            buffer[i] = other.buffer[idx];
        }
    }
    return *this;
}

// [TOPIC: EchelonDispatcher] [TOPIC: Modulo Wrap-Around] [TOPIC: FIFO Enqueue]
// Purpose: Adds an echelon unit to rearIndex with modulo wrap-around in O(1).
// Game Role: Queues incoming enemy echelon formations up to maxCapacity.
bool QueueWaveEchelon::enqueue(const EchelonWaveUnit& unit) {
    if (isFull()) {
        std::cerr << "[Queue Overflow] Echelon queue full (" << maxCapacity << " max)!\n";
        return false;
    }
    // WHY: Modulo wrap-around lets rearIndex loop back to slot 0 after frontIndex advances -> Game: Zero array drift.
    rearIndex = (rearIndex + 1) % maxCapacity;
    buffer[rearIndex] = unit;
    currentCount++;
    return true;
}

// [TOPIC: EchelonDispatcher] [TOPIC: Modulo Wrap-Around] [TOPIC: FIFO Dequeue]
// Purpose: Marks the front echelon deployed and advances frontIndex circularly in O(1).
// Game Role: Launches the earliest queued enemy echelon into the combat arena.
bool QueueWaveEchelon::dequeue(EchelonWaveUnit& outUnit) {
    if (isEmpty()) {
        std::cerr << "[Queue Underflow] Echelon queue is empty!\n";
        return false;
    }
    outUnit = buffer[frontIndex];
    // WHY: Flag unit deployed upon launch while wrapping frontIndex in O(1).
    outUnit.deployed = true;
    frontIndex = (frontIndex + 1) % maxCapacity;
    currentCount--;
    return true;
}

bool QueueWaveEchelon::peek(EchelonWaveUnit& outUnit) const {
    if (isEmpty()) return false;
    outUnit = buffer[frontIndex];
    return true;
}

void QueueWaveEchelon::clear() {
    frontIndex = 0;
    rearIndex = -1;
    currentCount = 0;
}

void QueueWaveEchelon::printEchelonQueue() const {
    std::cout << "\n=== Queue Wave Echelon (Count: " << currentCount << "/" << maxCapacity
              << ", Front: " << frontIndex << ", Rear: " << rearIndex
              << (isWrappedAround() ? " [WRAPPED AROUND]" : "") << ") ===\n";
    if (isEmpty()) {
        std::cout << "  [Echelon Queue Idle - All Units Dispatched]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "Pos"
              << std::setw(8) << "ID"
              << std::setw(20) << "Codename"
              << std::setw(10) << "Craft"
              << std::setw(12) << "Speed"
              << "Role\n";
    std::cout << "--------------------------------------------------------------------\n";
    for (int i = 0; i < currentCount; ++i) {
        int idx = (frontIndex + i) % maxCapacity;
        std::cout << "[" << std::setw(4) << i << "] "
                  << std::setw(8) << buffer[idx].echelonId
                  << std::setw(20) << buffer[idx].codename
                  << std::setw(10) << buffer[idx].craftCount
                  << std::fixed << std::setprecision(1) << std::setw(12) << buffer[idx].flightSpeed
                  << (i == 0 ? "<-- FRONT (Next to launch)" : "")
                  << (i == currentCount - 1 ? "<-- REAR (Latest queued)" : "")
                  << "\n";
    }
}
