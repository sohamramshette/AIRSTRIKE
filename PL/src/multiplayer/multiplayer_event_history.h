// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_event_history.h
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: MultiplayerEventHistory] [TOPIC: Doubly Linked List] [TOPIC: Bidirectional Traversal]
// Game Role : Stores two-player match events for forward replay and reverse audit traversal.
// ==============================================================================

#ifndef AIRSTRIKE_MULTIPLAYER_EVENT_HISTORY_H
#define AIRSTRIKE_MULTIPLAYER_EVENT_HISTORY_H

#include "match_event.h"
#include <vector>
#include <string>

namespace Airstrike::Multiplayer {

// [TOPIC: EventNode] [TOPIC: Doubly Linked Node]
// Purpose: Doubly linked node wrapping one MatchEvent with prev/next replay links.
// Game Role: Links sequential multiplayer events for forward and backward match inspection.
struct EventNode {
    MatchEvent event;
    EventNode* prev;
    EventNode* next;

    explicit EventNode(const MatchEvent& ev)
        : event(ev), prev(nullptr), next(nullptr) {}
};

// [TOPIC: MultiplayerEventHistory] [TOPIC: Doubly Linked List] [TOPIC: Bidirectional Traversal]
// Purpose: Bidirectional linked list supporting O(1) append/prepend, pointer-relink deletion, and two-way export.
// Game Role: Maintains chronological multiplayer combat logs and supports reverse rewind queries.
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
