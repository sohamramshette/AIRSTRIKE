#ifndef WAVE_QUEUE_H
#define WAVE_QUEUE_H

#include <iostream>
#include <string>

/**
 * Queue Implementation for AirStrike Enemy Wave Spawning (PL-CO3).
 * Implemented using a circular array to guarantee O(1) enqueue and dequeue
 * and prevent memory drift, demonstrating true FIFO (First-In, First-Out) semantics.
 */
struct EnemyWave {
    int waveNumber;
    std::string waveName;
    int enemyCount;
    double spawnIntervalSec;
    double difficultyMultiplier;

    EnemyWave() : waveNumber(0), waveName(""), enemyCount(0), spawnIntervalSec(0.0), difficultyMultiplier(1.0) {}
    EnemyWave(int num, const std::string& name, int count, double interval, double diff)
        : waveNumber(num), waveName(name), enemyCount(count), spawnIntervalSec(interval), difficultyMultiplier(diff) {}
};

class WaveQueue {
public:
    static constexpr int DEFAULT_CAPACITY = 8;

private:
    EnemyWave* elements;
    int frontIndex;
    int rearIndex;
    int currentSize;
    int maxCapacity;

public:
    explicit WaveQueue(int capacity = DEFAULT_CAPACITY);
    ~WaveQueue();

    // Deep copy operations
    WaveQueue(const WaveQueue& other);
    WaveQueue& operator=(const WaveQueue& other);

    // Core Queue Operations
    bool enqueue(const EnemyWave& wave);
    bool dequeue(EnemyWave& outWave);
    bool front(EnemyWave& outWave) const;

    // State Queries
    bool isEmpty() const { return currentSize == 0; }
    bool isFull() const { return currentSize == maxCapacity; }
    int size() const { return currentSize; }
    int capacity() const { return maxCapacity; }

    void clear();
    void printQueue() const;
};

#endif // WAVE_QUEUE_H
