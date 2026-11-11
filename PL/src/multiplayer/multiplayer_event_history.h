#ifndef AIRSTRIKE_MULTIPLAYER_EVENT_HISTORY_H
#define AIRSTRIKE_MULTIPLAYER_EVENT_HISTORY_H

#include "match_event.h"
#include <vector>
#include <string>

namespace Airstrike::Multiplayer {

/**
 * Doubly Linked List Node for MatchEvent records.
 */
struct EventNode {
    MatchEvent event;
    EventNode* prev;
    EventNode* next;

    explicit EventNode(const MatchEvent& ev)
        : event(ev), prev(nullptr), next(nullptr) {}
};

/**
 * ==============================================================================
 * Demonstrator for PL-CO2: Doubly Linked Lists
 *
 * Implements a genuine bidirectional linked list for match event logging:
 * - O(1) push_back (tail append) and push_front (head prepend)
 * - Forward traversal (chronological history order)
 * - Backward traversal (reverse chronological replay / rewind)
 * - Arbitrary deletion by eventId with pointer relinking
 * ==============================================================================
 */
class MultiplayerEventHistory {
public:
    MultiplayerEventHistory();
    ~MultiplayerEventHistory();

    // Disable copy to enforce strict memory ownership
    MultiplayerEventHistory(const MultiplayerEventHistory&) = delete;
    MultiplayerEventHistory& operator=(const MultiplayerEventHistory&) = delete;

    void append(const MatchEvent& ev);
    void prepend(const MatchEvent& ev);
    bool removeById(uint32_t eventId);
    void clear();

    size_t size() const { return size_; }
    bool isEmpty() const { return size_ == 0; }

    std::vector<MatchEvent> traverseForward() const;
    std::vector<MatchEvent> traverseBackward() const;

    const EventNode* getHead() const { return head_; }
    const EventNode* getTail() const { return tail_; }

private:
    EventNode* head_;
    EventNode* tail_;
    size_t size_;
};

} // namespace Airstrike::Multiplayer

#endif // AIRSTRIKE_MULTIPLAYER_EVENT_HISTORY_H
