#ifndef DOUBLY_LINKED_LIST_H
#define DOUBLY_LINKED_LIST_H

#include <iostream>
#include <string>

/**
 * Doubly Linked List implementation for AirStrike Active Enemy Squadron (PL-CO2).
 * Application: Active squadron roster.
 * Why DLL is suitable:
 * 1. Bidirectional traversal allows tactical inspection from Vanguard (Head) to Rearguard (Tail)
 *    and vice versa.
 * 2. Instant O(1) node deletion given a pointer, essential when an enemy is destroyed
 *    in mid-screen without needing to find its predecessor from head.
 */
struct EnemyNode {
    int enemyId;
    std::string shipType;
    int health;
    EnemyNode* prev;
    EnemyNode* next;

    EnemyNode(int id, const std::string& type, int hp, EnemyNode* p = nullptr, EnemyNode* n = nullptr)
        : enemyId(id), shipType(type), health(hp), prev(p), next(n) {}
};

class DoublyLinkedList {
private:
    EnemyNode* head;
    EnemyNode* tail;
    size_t count;

public:
    DoublyLinkedList();
    ~DoublyLinkedList();

    // Deep copy operations
    DoublyLinkedList(const DoublyLinkedList& other);
    DoublyLinkedList& operator=(const DoublyLinkedList& other);

    // Insertion
    void insertHead(int id, const std::string& type, int health);
    void insertTail(int id, const std::string& type, int health);
    bool insertAt(size_t index, int id, const std::string& type, int health);

    // Deletion
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
