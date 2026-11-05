#ifndef SINGLY_LINKED_LIST_H
#define SINGLY_LINKED_LIST_H

#include <iostream>
#include <string>

/**
 * Singly Linked List implementation for AirStrike Mission Flight Log (PL-CO2).
 * Application: Chronological flight log where log events are dynamically recorded.
 * SLL is ideal because appending new logs or inserting event checkpoints requires
 * O(1) head insertion or simple forward traversal without wasted array pre-allocation.
 */
struct FlightLogNode {
    int eventId;
    double timestampSec;
    std::string description;
    FlightLogNode* next;

    FlightLogNode(int id, double time, const std::string& desc, FlightLogNode* nxt = nullptr)
        : eventId(id), timestampSec(time), description(desc), next(nxt) {}
};

class SinglyLinkedList {
private:
    FlightLogNode* head;
    size_t count;

public:
    SinglyLinkedList();
    ~SinglyLinkedList();

    // Deep copy operations
    SinglyLinkedList(const SinglyLinkedList& other);
    SinglyLinkedList& operator=(const SinglyLinkedList& other);

    // Core insertion operations
    void insertAtHead(int id, double timestampSec, const std::string& desc);
    void insertAtTail(int id, double timestampSec, const std::string& desc);
    bool insertAt(size_t index, int id, double timestampSec, const std::string& desc);

    // Core deletion operations
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
