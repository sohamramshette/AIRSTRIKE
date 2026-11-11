#ifndef AIRSTRIKE_MULTIPLAYER_PACKET_QUEUE_H
#define AIRSTRIKE_MULTIPLAYER_PACKET_QUEUE_H

#include "match_event.h"
#include <vector>

namespace Airstrike::Multiplayer {

/**
 * ==============================================================================
 * Demonstrator for PL-CO3: Circular Queue (FIFO Dispatching)
 *
 * Implements a fixed-capacity ring buffer for multiplayer packet ingestion:
 * - O(1) Enqueue with tail wraparound: rear = (rear + 1) % CAPACITY
 * - O(1) Dequeue with head advance:   front = (front + 1) % CAPACITY
 * - Overflow guard: rejects packets when count == CAPACITY
 * - Underflow guard: returns false when count == 0
 * - Demonstrates zero-allocation streaming event processing
 * ==============================================================================
 */
class MultiplayerPacketQueue {
public:
    static constexpr size_t DEFAULT_CAPACITY = 8;

    explicit MultiplayerPacketQueue(size_t capacity = DEFAULT_CAPACITY);
    ~MultiplayerPacketQueue();

    MultiplayerPacketQueue(const MultiplayerPacketQueue&) = delete;
    MultiplayerPacketQueue& operator=(const MultiplayerPacketQueue&) = delete;

    bool enqueue(const MatchEvent& packet);
    bool dequeue(MatchEvent& outPacket);
    bool peek(MatchEvent& outPacket) const;

    bool isEmpty() const { return count_ == 0; }
    bool isFull() const { return count_ == capacity_; }
    size_t size() const { return count_; }
    size_t capacity() const { return capacity_; }

    size_t getFrontIndex() const { return front_; }
    size_t getRearIndex() const { return rear_; }

    void clear();

private:
    size_t capacity_;
    MatchEvent* buffer_;
    size_t front_;
    size_t rear_;
    size_t count_;
};

} // namespace Airstrike::Multiplayer

#endif // AIRSTRIKE_MULTIPLAYER_PACKET_QUEUE_H
