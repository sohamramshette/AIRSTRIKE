#ifndef CIRCULAR_LINKED_LIST_H
#define CIRCULAR_LINKED_LIST_H

#include <iostream>
#include <string>

/**
 * Circular Linked List implementation for AirStrike Weapon Selection Wheel (PL-CO2).
 * Application: Cyclic weapon selector (Plasma, Laser, Missile, EMP).
 * Why CLL is suitable:
 * 1. Infinite cycling: Cycling past the last weapon seamlessly wraps to the first
 *    without modulo or boundary check overhead.
 * 2. Proper circular termination: Traversal stops reliably when current pointer returns
 *    to head node.
 */
struct WeaponNode {
    int weaponId;
    std::string name;
    int energyCost;
    int damage;
    WeaponNode* next;

    WeaponNode(int id, const std::string& n, int cost, int dmg, WeaponNode* nxt = nullptr)
        : weaponId(id), name(n), energyCost(cost), damage(dmg), next(nxt) {}
};

class CircularLinkedList {
private:
    WeaponNode* head;
    WeaponNode* activeWeapon;
    size_t count;

public:
    CircularLinkedList();
    ~CircularLinkedList();

    // Deep copy operations
    CircularLinkedList(const CircularLinkedList& other);
    CircularLinkedList& operator=(const CircularLinkedList& other);

    // Insertion
    void insertHead(int id, const std::string& name, int energyCost, int damage);
    void insertTail(int id, const std::string& name, int energyCost, int damage);

    // Deletion
    bool deleteHead();
    bool deleteTail();
    bool deleteById(int id);

    // Cycling mechanism
    const WeaponNode* cycleNext();
    const WeaponNode* getActiveWeapon() const { return activeWeapon; }

    // Traversal and query
    const WeaponNode* search(int id) const;
    void traverse() const;
    void clear();

    size_t size() const { return count; }
    bool isEmpty() const { return count == 0; }
    const WeaponNode* getHead() const { return head; }
};

#endif // CIRCULAR_LINKED_LIST_H

