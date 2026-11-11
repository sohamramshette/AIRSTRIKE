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
    result.reserve(size_);
    EventNode* curr = tail_;
    while (curr) {
        result.push_back(curr->event);
        curr = curr->prev;
    }
    return result;
}

} // namespace Airstrike::Multiplayer
