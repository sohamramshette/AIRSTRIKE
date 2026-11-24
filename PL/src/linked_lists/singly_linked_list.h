// ==============================================================================
// File      : PL/src/linked_lists/singly_linked_list.h
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: Singly Linked List] [TOPIC: Dynamic Node Allocation] [TOPIC: Pointer Traversal]
// Game Role : Logs chronological AirStrike mission flight events via forward-linked nodes.
// ==============================================================================

#ifndef SINGLY_LINKED_LIST_H
#define SINGLY_LINKED_LIST_H

#include <iostream>
#include <string>

// [TOPIC: FlightLogNode] [TOPIC: Singly Linked Node]
// Purpose: Stores a timestamped flight event and a single forward next pointer.
// Game Role: Represents one entry in the AirStrike chronological mission flight log.
struct FlightLogNode {
    int eventId;
    double timestampSec;
    std::string description;
    FlightLogNode* next;

    FlightLogNode(int id, double time, const std::string& desc, FlightLogNode* nxt = nullptr)
        : eventId(id), timestampSec(time), description(desc), next(nxt) {}
};

// [TOPIC: SinglyLinkedList] [TOPIC: Singly Linked List]
// Purpose: Dynamic singly linked list supporting O(1) head insertion, arbitrary removal, and traversal.
// Game Role: Records combat flight events sequentially without fixed-capacity pre-allocation.
class SinglyLinkedList {
private:
    FlightLogNode* head;
    size_t count;

public:
    SinglyLinkedList();
    ~SinglyLinkedList();

    // [TOPIC: Deep Copy Semantics]
    // Purpose: Allocates fresh FlightLogNode copies in matching chronological order.
    // Game Role: Duplicates mission flight logs without sharing heap pointers.
    SinglyLinkedList(const SinglyLinkedList& other);
    SinglyLinkedList& operator=(const SinglyLinkedList& other);

    // [TOPIC: Singly Linked List Insertion]
    // Purpose: Inserts nodes at head in O(1), tail in O(N), or validated index position.
    // Game Role: Appends mission telemetry checkpoints as events occur.
    void insertAtHead(int id, double timestampSec, const std::string& desc);
    void insertAtTail(int id, double timestampSec, const std::string& desc);
    bool insertAt(size_t index, int id, double timestampSec, const std::string& desc);

    // [TOPIC: Singly Linked List Deletion]
    // Purpose: Unlinks and frees head, tail, indexed, or ID-matched nodes safely.
    // Game Role: Prunes expired or cleared flight-log records.
    bool deleteHead();
    bool deleteTail();
    bool deleteAt(size_t index);
    bool deleteById(int id);

    // Query & traversal
    const FlightLogNode* search(int id) const;
    FlightLogNode* searchMut(int id);
    void traverse() const;
    void clear();

    size_t size() const { return count; }
    bool isEmpty() const { return head == nullptr; }
    const FlightLogNode* getHead() const { return head; }
};

#endif // SINGLY_LINKED_LIST_H

