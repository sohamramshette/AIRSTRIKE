#include "test_queue.h"
#include "../src/queues/wave_queue.h"

TestSummary runQueueTests() {
    TestSummary summary;
    std::cout << "\n======================================================\n";
    std::cout << "[TEST SUITE] PL-CO3: Queue Operations (6 Tests)\n";
    std::cout << "======================================================\n";

    // 1. Enqueue and Front (FIFO Verification)
    WaveQueue queue(4);
    queue.enqueue(EnemyWave(1, "Scout Incursion", 5, 2.0, 1.0));
    queue.enqueue(EnemyWave(2, "Fighter Vanguard", 8, 1.5, 1.2));

    EnemyWave frontWave;
    bool frontOk = queue.front(frontWave);
    summary.record(frontOk && frontWave.waveNumber == 1 && queue.size() == 2,
                   "Queue: Enqueue and Front inspects earliest FIFO element");

    // 2. Dequeue (FIFO Order)
    EnemyWave dequeued;
    bool deqOk = queue.dequeue(dequeued);
    summary.record(deqOk && dequeued.waveNumber == 1 && queue.size() == 1,
                   "Queue: Dequeue removes earliest element in FIFO order");

    // 3. Circular Wrapping without Pointer Drift
    queue.enqueue(EnemyWave(3, "Asteroid Swarm", 12, 1.0, 1.5));
    queue.enqueue(EnemyWave(4, "Cruiser Escort", 6, 2.5, 1.8));
    queue.enqueue(EnemyWave(5, "Boss Dreadnought", 1, 0.0, 2.5)); // Fills circular capacity (4 total)
    summary.record(queue.isFull() && queue.size() == 4,
                   "Queue: Circular buffer wraps around indices correctly");

    // 4. Overflow Protection
    bool overflowBlocked = !queue.enqueue(EnemyWave(6, "Extra Wave", 5, 1.0, 3.0));
    summary.record(overflowBlocked && queue.isFull(),
                   "Queue: Overflow protection rejects enqueue on full queue");

    // 5. Underflow Protection
    EnemyWave temp;
    while (!queue.isEmpty()) {
        queue.dequeue(temp);
    }
    bool underflowBlocked = !queue.dequeue(temp);
    summary.record(underflowBlocked && queue.isEmpty(),
                   "Queue: Underflow protection prevents dequeue on empty queue");

    // 6. Deep Copy Independence
    queue.enqueue(EnemyWave(10, "Reinforcements", 4, 1.0, 1.0));
    WaveQueue copyQueue = queue;
    copyQueue.dequeue(temp);
    summary.record(queue.size() == 1 && copyQueue.isEmpty(),
                   "Queue: Deep copy maintains independent circular buffer state");

    return summary;
}

