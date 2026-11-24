// ==============================================================================
// File      : PL/src/queues/wave_queue.h
// Subject   : PL-CO3 | Stack and Queue Applications
// Topics    : [TOPIC: WaveQueue] [TOPIC: Circular Queue] [TOPIC: FIFO] [TOPIC: Modulo Arithmetic]
// Game Role : Sequences upcoming AirStrike enemy waves in strict FIFO order without array drift.
// ==============================================================================

#ifndef WAVE_QUEUE_H
#define WAVE_QUEUE_H

#include <iostream>
#include <string>

// [TOPIC: EnemyWave Record]
// Purpose: Describes one scheduled enemy wave including enemy count, spawn rate, and difficulty scaling.
// Game Role: Serves as the FIFO payload queued in WaveQueue for level progression.
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

// [TOPIC: WaveQueue] [TOPIC: Circular Queue] [TOPIC: FIFO]
// Purpose: Circular array queue guaranteeing O(1) enqueue/dequeue via modulo wrap-around.
// Game Role: Schedules and dispatches enemy waves sequentially without O(N) element shifting.
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

    // [TOPIC: Circular Buffer Deep Copy]
    // Purpose: Copies active circular elements in FIFO order into a contiguous buffer.
    // Game Role: Clones queued wave schedules safely across game instances.
    WaveQueue(const WaveQueue& other);
    WaveQueue& operator=(const WaveQueue& other);

    // [TOPIC: FIFO Queue Operations]
    // Purpose: Enqueues at rear, dequeues from front, or peeks front wave in O(1) time.
    // Game Role: Feeds enemy waves to the AirStrike spawner in chronological order.
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

