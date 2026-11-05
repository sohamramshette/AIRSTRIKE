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

WaveQueue::WaveQueue(const WaveQueue& other)
    : frontIndex(0), rearIndex(other.currentSize - 1), currentSize(other.currentSize),
      maxCapacity(other.maxCapacity) {
    elements = new EnemyWave[maxCapacity];
    for (int i = 0; i < other.currentSize; ++i) {
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
            int idx = (other.frontIndex + i) % other.maxCapacity;
            elements[i] = other.elements[idx];
        }
    }
    return *this;
}

bool WaveQueue::enqueue(const EnemyWave& wave) {
    if (isFull()) {
        std::cerr << "[ERROR: WaveQueue Overflow] Cannot enqueue wave " << wave.waveNumber
                  << "; queue at capacity " << maxCapacity << "!\n";
        return false;
    }
    rearIndex = (rearIndex + 1) % maxCapacity;
    elements[rearIndex] = wave;
    currentSize++;
    return true;
}

bool WaveQueue::dequeue(EnemyWave& outWave) {
    if (isEmpty()) {
        std::cerr << "[ERROR: WaveQueue Underflow] Cannot dequeue from empty wave queue!\n";
        return false;
    }
    outWave = elements[frontIndex];
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
