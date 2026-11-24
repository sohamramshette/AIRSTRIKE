// ==============================================================================
// File      : PL/src/linked_lists/circular_linked_list.h
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: Circular Linked List] [TOPIC: Cyclic Traversal] [TOPIC: Weapon Selection Wheel]
// Game Role : Implements an endless cyclic weapon wheel for the AirStrike spacecraft HUD.
// ==============================================================================

#ifndef CIRCULAR_LINKED_LIST_H
#define CIRCULAR_LINKED_LIST_H

#include <iostream>
#include <string>

// [TOPIC: WeaponNode] [TOPIC: Circular Linked Node]
// Purpose: Encapsulates a spacecraft weapon's stats and a ring-closing next pointer.
// Game Role: Forms one selectable slot on the player's cyclic HUD weapon selector.
struct WeaponNode {
    int weaponId;
    std::string name;
    int energyCost;
    int damage;
    WeaponNode* next;

    WeaponNode(int id, const std::string& n, int cost, int dmg, WeaponNode* nxt = nullptr)
        : weaponId(id), name(n), energyCost(cost), damage(dmg), next(nxt) {}
};

// [TOPIC: CircularLinkedList] [TOPIC: Circular Linked List] [TOPIC: Weapon Cycling]
// Purpose: Singly circular list where tail->next points back to head for seamless cyclic traversal.
// Game Role: Cycles active spacecraft weapons endlessly without index wrap-around checks.
class CircularLinkedList {
private:
    WeaponNode* head;
    WeaponNode* activeWeapon;
    size_t count;

public:
    CircularLinkedList();
    ~CircularLinkedList();

    // [TOPIC: Circular Deep Copy]
    // Purpose: Walks one full ring around other.head to copy all weapons into a separate ring.
    // Game Role: Duplicates weapon wheel configurations safely.
    CircularLinkedList(const CircularLinkedList& other);
    CircularLinkedList& operator=(const CircularLinkedList& other);

    void insertHead(int id, const std::string& name, int energyCost, int damage);
    void insertTail(int id, const std::string& name, int energyCost, int damage);

    bool deleteHead();
    bool deleteTail();
    bool deleteById(int id);

    // [TOPIC: Cyclic Weapon Switching]
    // Purpose: Advances activeWeapon to activeWeapon->next in O(1) across the circular ring.
    // Game Role: Cycles the player's equipped weapon on HUD switch input.
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

