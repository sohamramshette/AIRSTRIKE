#include "multiplayer_packet_queue.h"

namespace Airstrike::Multiplayer {

MultiplayerPacketQueue::MultiplayerPacketQueue(size_t capacity)
    : capacity_(capacity > 0 ? capacity : DEFAULT_CAPACITY),
      buffer_(new MatchEvent[capacity_]),
      front_(0),
      rear_(0),
      count_(0) {}

MultiplayerPacketQueue::~MultiplayerPacketQueue() {
    delete[] buffer_;
}

void MultiplayerPacketQueue::clear() {
    front_ = 0;
    rear_ = 0;
    count_ = 0;
}

bool MultiplayerPacketQueue::enqueue(const MatchEvent& packet) {
    if (isFull()) {
        return false; // Overflow rejection
    }

    buffer_[rear_] = packet;
    rear_ = (rear_ + 1) % capacity_;
    count_++;
    return true;
}

bool MultiplayerPacketQueue::dequeue(MatchEvent& outPacket) {
    if (isEmpty()) {
        return false; // Underflow rejection
    }

    outPacket = buffer_[front_];
    front_ = (front_ + 1) % capacity_;
    count_--;
    return true;
}

bool MultiplayerPacketQueue::peek(MatchEvent& outPacket) const {
    if (isEmpty()) {
        return false;
    }
    outPacket = buffer_[front_];
    return true;
}

} // namespace Airstrike::Multiplayer
