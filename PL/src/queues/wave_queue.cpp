// ==============================================================================
// File      : PL/src/queues/wave_queue.cpp
// Subject   : PL-CO3 | Stack and Queue Applications
// Topics    : [TOPIC: WaveQueue] [TOPIC: Circular Queue] [TOPIC: Modulo Wrap-Around] [TOPIC: FIFO]
// Game Role : Implements O(1) circular-array FIFO wave queuing for AIRSTRIKE.
// ==============================================================================

#include "wave_queue.h"
#include <iomanip>

WaveQueue::WaveQueue(int capacity)
    : frontIndex(0), rearIndex(-1), currentSize(0),
      maxCapacity(capacity > 0 ? capacity : DEFAULT_CAPACITY) {
    elements = new EnemyWave[maxCapacity];
}

WaveQueue::~WaveQueue() {
    delete[] elements;
}

// [TOPIC: Circular Buffer Copy]
// Purpose: Unrolls wrapped circular entries into indices [0..currentSize-1] during deep copy.
// Game Role: Preserves exact FIFO wave dispatch order when cloning WaveQueue.
WaveQueue::WaveQueue(const WaveQueue& other)
    : frontIndex(0), rearIndex(other.currentSize - 1), currentSize(other.currentSize),
      maxCapacity(other.maxCapacity) {
    elements = new EnemyWave[maxCapacity];
    for (int i = 0; i < other.currentSize; ++i) {
        // WHY: Map logical FIFO offset i from wrapped source frontIndex via modulo capacity.
        int idx = (other.frontIndex + i) % other.maxCapacity;
        elements[i] = other.elements[idx];
    }
}

WaveQueue& WaveQueue::operator=(const WaveQueue& other) {
    if (this != &other) {
        delete[] elements;
        frontIndex = 0;
        rearIndex = other.currentSize - 1;
        currentSize = other.currentSize;
        maxCapacity = other.maxCapacity;
        elements = new EnemyWave[maxCapacity];
        for (int i = 0; i < other.currentSize; ++i) {
            // WHY: Linearize wrapped circular slots into normalized [0..currentSize-1] layout.
            int idx = (other.frontIndex + i) % other.maxCapacity;
            elements[i] = other.elements[idx];
        }
    }
    return *this;
}

// [TOPIC: WaveQueue] [TOPIC: Modulo Wrap-Around] [TOPIC: FIFO Enqueue]
// Purpose: Appends a wave at the circular rear index in O(1) if buffer capacity permits.
// Game Role: Registers the next enemy wave without shifting existing queue elements.
bool WaveQueue::enqueue(const EnemyWave& wave) {
    if (isFull()) {
        std::cerr << "[ERROR: WaveQueue Overflow] Cannot enqueue wave " << wave.waveNumber
                  << "; queue at capacity " << maxCapacity << "!\n";
        return false;
    }
    // WHY: Modulo wrap-around reuses freed slots at array start -> Game: Prevents linear array drift.
    rearIndex = (rearIndex + 1) % maxCapacity;
    elements[rearIndex] = wave;
    currentSize++;
    return true;
}

// [TOPIC: WaveQueue] [TOPIC: Modulo Wrap-Around] [TOPIC: FIFO Dequeue]
// Purpose: Removes and returns the oldest queued wave from frontIndex in O(1).
// Game Role: Launches the next scheduled enemy wave in strict FIFO order.
bool WaveQueue::dequeue(EnemyWave& outWave) {
    if (isEmpty()) {
        std::cerr << "[ERROR: WaveQueue Underflow] Cannot dequeue from empty wave queue!\n";
        return false;
    }
    outWave = elements[frontIndex];
    // WHY: Advance frontIndex circularly in O(1) instead of shifting trailing waves left.
    frontIndex = (frontIndex + 1) % maxCapacity;
    currentSize--;
    return true;
}

bool WaveQueue::front(EnemyWave& outWave) const {
    if (isEmpty()) {
        return false;
    }
    outWave = elements[frontIndex];
    return true;
}

void WaveQueue::clear() {
    frontIndex = 0;
    rearIndex = -1;
    currentSize = 0;
}

void WaveQueue::printQueue() const {
    std::cout << "\n=== Enemy Wave Queue (FIFO: Front to Rear, " << currentSize
              << "/" << maxCapacity << ") ===\n";
    if (isEmpty()) {
        std::cout << "  [Wave Queue is Empty - All Waves Dispatched]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "Pos"
              << std::setw(8) << "Wave#"
              << std::setw(20) << "Wave Codename"
              << std::setw(10) << "Enemies"
              << std::setw(12) << "Spawn Rate"
              << "Multiplier\n";
    std::cout << "----------------------------------------------------------------------\n";
    for (int i = 0; i < currentSize; ++i) {
        int idx = (frontIndex + i) % maxCapacity;
        std::cout << "[" << std::setw(4) << i << "] "
                  << std::setw(8) << elements[idx].waveNumber
                  << std::setw(20) << elements[idx].waveName
                  << std::setw(10) << elements[idx].enemyCount
                  << std::fixed << std::setprecision(2) << std::setw(12) << elements[idx].spawnIntervalSec
                  << elements[idx].difficultyMultiplier << "x"
                  << (i == 0 ? " <-- FRONT (Spawning Now)" : "")
                  << (i == currentSize - 1 ? " <-- REAR (Latest Added)" : "")
                  << "\n";
    }
}

