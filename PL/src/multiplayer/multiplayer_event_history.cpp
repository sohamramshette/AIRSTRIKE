// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_event_history.cpp
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: MultiplayerEventHistory] [TOPIC: Doubly Linked List] [TOPIC: Pointer Relinking]
// Game Role : Implements O(1) append/prepend, relink deletion, and forward/reverse log traversal.
// ==============================================================================

#include "multiplayer_event_history.h"

namespace Airstrike::Multiplayer {

MultiplayerEventHistory::MultiplayerEventHistory()
    : head_(nullptr), tail_(nullptr), size_(0) {}

MultiplayerEventHistory::~MultiplayerEventHistory() {
    clear();
}

void MultiplayerEventHistory::clear() {
    EventNode* curr = head_;
    while (curr) {
        EventNode* nxt = curr->next;
        delete curr;
        curr = nxt;
    }
    head_ = nullptr;
    tail_ = nullptr;
    size_ = 0;
}

void MultiplayerEventHistory::append(const MatchEvent& ev) {
    EventNode* node = new EventNode(ev);
    if (!head_) {
        head_ = tail_ = node;
    } else {
        tail_->next = node;
        node->prev = tail_;
        tail_ = node;
    }
    size_++;
}

void MultiplayerEventHistory::prepend(const MatchEvent& ev) {
    EventNode* node = new EventNode(ev);
    if (!head_) {
        head_ = tail_ = node;
    } else {
        node->next = head_;
        head_->prev = node;
        head_ = node;
    }
    size_++;
}

bool MultiplayerEventHistory::removeById(uint32_t eventId) {
    EventNode* curr = head_;
    while (curr) {
        if (curr->event.eventId == eventId) {
            // WHY: Patch predecessor/head_ and successor/tail_ independently to handle all boundary cases cleanly.
            if (curr->prev) {
                curr->prev->next = curr->next;
            } else {
                head_ = curr->next;
            }

            if (curr->next) {
                curr->next->prev = curr->prev;
            } else {
                tail_ = curr->prev;
            }

            delete curr;
            size_--;
            return true;
        }
        curr = curr->next;
    }
    return false;
}

std::vector<MatchEvent> MultiplayerEventHistory::traverseForward() const {
    std::vector<MatchEvent> result;
    // WHY: Pre-reserve size_ to avoid vector reallocations while copying the linked chain.
    result.reserve(size_);
    EventNode* curr = head_;
    while (curr) {
        result.push_back(curr->event);
        curr = curr->next;
    }
    return result;
}

std::vector<MatchEvent> MultiplayerEventHistory::traverseBackward() const {
    std::vector<MatchEvent> result;
    // WHY: Pre-reserve size_ for single-allocation reverse replay export from tail_ to head_.
    result.reserve(size_);
    EventNode* curr = tail_;
    while (curr) {
        result.push_back(curr->event);
        curr = curr->prev;
    }
    return result;
}

} // namespace Airstrike::Multiplayer
