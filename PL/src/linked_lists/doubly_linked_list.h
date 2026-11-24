// ==============================================================================
// File      : PL/src/linked_lists/doubly_linked_list.h
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: Doubly Linked List] [TOPIC: Bidirectional Traversal] [TOPIC: O(1) Node Deletion]
// Game Role : Maintains active AirStrike enemy squadron rosters with O(1) mid-chain node removal.
// ==============================================================================

#ifndef DOUBLY_LINKED_LIST_H
#define DOUBLY_LINKED_LIST_H

#include <iostream>
#include <string>

// [TOPIC: EnemyNode] [TOPIC: Doubly Linked Node]
// Purpose: Stores an enemy ship's ID, type, health, and bidirectional prev/next pointers.
// Game Role: Links individual enemy craft inside an active DoublyLinkedList squadron.
struct EnemyNode {
    int enemyId;
    std::string shipType;
    int health;
    EnemyNode* prev;
    EnemyNode* next;

    EnemyNode(int id, const std::string& type, int hp, EnemyNode* p = nullptr, EnemyNode* n = nullptr)
        : enemyId(id), shipType(type), health(hp), prev(p), next(n) {}
};

// [TOPIC: DoublyLinkedList] [TOPIC: Doubly Linked List] [TOPIC: Bidirectional Traversal]
// Purpose: Head/tail doubly linked list supporting O(1) endpoint ops, O(1) pointer deletion, and two-way scans.
// Game Role: Tracks live AirStrike enemy squadrons from vanguard to rearguard and splices out kills instantly.
class DoublyLinkedList {
private:
    EnemyNode* head;
    EnemyNode* tail;
    size_t count;

public:
    DoublyLinkedList();
    ~DoublyLinkedList();

    // [TOPIC: Deep Copy Semantics]
    // Purpose: Copies enemy nodes sequentially while rebuilding fresh prev/next links.
    // Game Role: Clones active enemy squadron rosters safely.
    DoublyLinkedList(const DoublyLinkedList& other);
    DoublyLinkedList& operator=(const DoublyLinkedList& other);

    // [TOPIC: Doubly Linked List Insertion]
    // Purpose: Inserts enemy nodes at head or tail in O(1) or at arbitrary index in O(N).
    // Game Role: Deploys new vanguard, rearguard, or mid-wing enemy craft.
    void insertHead(int id, const std::string& type, int health);
    void insertTail(int id, const std::string& type, int health);
    bool insertAt(size_t index, int id, const std::string& type, int health);

    // [TOPIC: O(1) Node Deletion]
    // Purpose: Deletes endpoints or splices out a known EnemyNode* directly in O(1) time.
    // Game Role: Removes destroyed enemy ships mid-formation without predecessor scanning.
    bool deleteHead();
    bool deleteTail();
    bool deleteById(int id);
    void deleteNode(EnemyNode* node);

    // Query & Traversals
    const EnemyNode* search(int id) const;
    EnemyNode* searchMut(int id);
    void traverseForward() const;
    void traverseBackward() const;
    void clear();

    size_t size() const { return count; }
    bool isEmpty() const { return count == 0; }
    const EnemyNode* getHead() const { return head; }
    const EnemyNode* getTail() const { return tail; }
};

#endif // DOUBLY_LINKED_LIST_H

