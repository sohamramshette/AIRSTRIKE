// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_packet_queue.h
// Subject   : PL-CO3 | Stack and Queue Applications
// Topics    : [TOPIC: MultiplayerPacketQueue] [TOPIC: Circular Queue] [TOPIC: FIFO] [TOPIC: Modulo Wrap-Around]
// Game Role : Buffers incoming multiplayer network packets in a fixed-capacity FIFO ring buffer.
// ==============================================================================

#ifndef AIRSTRIKE_MULTIPLAYER_PACKET_QUEUE_H
#define AIRSTRIKE_MULTIPLAYER_PACKET_QUEUE_H

#include "match_event.h"
#include <vector>

namespace Airstrike::Multiplayer {

// [TOPIC: MultiplayerPacketQueue] [TOPIC: Circular Queue] [TOPIC: FIFO]
// Purpose: Ring-buffer FIFO queue with O(1) modulo wrap-around enqueue/dequeue and overflow/underflow guards.
// Game Role: Streams two-player MatchEvent network packets cleanly without runtime reallocation.
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
