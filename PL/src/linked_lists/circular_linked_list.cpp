// ==============================================================================
// File      : PL/src/linked_lists/circular_linked_list.cpp
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: Circular Linked List] [TOPIC: Ring Closure] [TOPIC: Cyclic Traversal]
// Game Role : Implements circular weapon-wheel insertion, deletion, cycling, and teardown.
// ==============================================================================

#include "circular_linked_list.h"
#include <iomanip>

CircularLinkedList::CircularLinkedList() : head(nullptr), activeWeapon(nullptr), count(0) {}

CircularLinkedList::~CircularLinkedList() {
    clear();
}

CircularLinkedList::CircularLinkedList(const CircularLinkedList& other)
    : head(nullptr), activeWeapon(nullptr), count(0) {
    if (!other.head) return;
    const WeaponNode* curr = other.head;
    // WHY: Use do-while so head is processed before checking return-to-head loop termination.
    do {
        insertTail(curr->weaponId, curr->name, curr->energyCost, curr->damage);
        curr = curr->next;
    } while (curr != other.head);
}

CircularLinkedList& CircularLinkedList::operator=(const CircularLinkedList& other) {
    if (this != &other) {
        clear();
        if (other.head) {
            const WeaponNode* curr = other.head;
            do {
                insertTail(curr->weaponId, curr->name, curr->energyCost, curr->damage);
                curr = curr->next;
            } while (curr != other.head);
        }
    }
    return *this;
}

void CircularLinkedList::insertHead(int id, const std::string& name, int energyCost, int damage) {
    WeaponNode* newNode = new WeaponNode(id, name, energyCost, damage, nullptr);
    if (!head) {
        // WHY: Single-node circular list closes onto itself so cycleNext() never hits nullptr.
        newNode->next = newNode;
        head = activeWeapon = newNode;
    } else {
        // WHY: Walk to tail (curr->next == head) so tail closes the loop onto the new head.
        WeaponNode* curr = head;
        while (curr->next != head) {
            curr = curr->next;
        }
        newNode->next = head;
        curr->next = newNode;
        head = newNode;
    }
    count++;
}

void CircularLinkedList::insertTail(int id, const std::string& name, int energyCost, int damage) {
    WeaponNode* newNode = new WeaponNode(id, name, energyCost, damage, nullptr);
    if (!head) {
        newNode->next = newNode;
        head = activeWeapon = newNode;
    } else {
        WeaponNode* curr = head;
        while (curr->next != head) {
            curr = curr->next;
        }
        curr->next = newNode;
        newNode->next = head;
    }
    count++;
}

bool CircularLinkedList::deleteHead() {
    if (!head) return false;
    // WHY: Self-looped single node must reset pointers to nullptr instead of relinking to freed self.
    if (head->next == head) {
        delete head;
        head = activeWeapon = nullptr;
        count = 0;
        return true;
    }
    WeaponNode* last = head;
    while (last->next != head) {
        last = last->next;
    }
    WeaponNode* temp = head;
    head = head->next;
    last->next = head;
    // WHY: Reassign activeWeapon before deleting temp so active cursor never dangles -> Game: Safe weapon switch.
    if (activeWeapon == temp) {
        activeWeapon = head;
    }
    delete temp;
    count--;
    return true;
}

bool CircularLinkedList::deleteTail() {
    if (!head) return false;
    if (head->next == head) {
        delete head;
        head = activeWeapon = nullptr;
        count = 0;
        return true;
    }
    WeaponNode* prev = nullptr;
    WeaponNode* curr = head;
    while (curr->next != head) {
        prev = curr;
        curr = curr->next;
    }
    prev->next = head;
    if (activeWeapon == curr) {
        activeWeapon = prev;
    }
    delete curr;
    count--;
    return true;
}

bool CircularLinkedList::deleteById(int id) {
    if (!head) return false;
    if (head->weaponId == id) {
        return deleteHead();
    }
    WeaponNode* prev = head;
    WeaponNode* curr = head->next;
    while (curr != head) {
        if (curr->weaponId == id) {
            prev->next = curr->next;
            if (activeWeapon == curr) {
                activeWeapon = prev;
            }
            delete curr;
            count--;
            return true;
        }
        prev = curr;
        curr = curr->next;
    }
    return false;
}

const WeaponNode* CircularLinkedList::cycleNext() {
    if (!activeWeapon) return nullptr;
    // WHY: Tail already links to head, giving wrap-around weapon cycling in O(1) without modulo checks.
    activeWeapon = activeWeapon->next;
    return activeWeapon;
}

const WeaponNode* CircularLinkedList::search(int id) const {
    if (!head) return nullptr;
    const WeaponNode* curr = head;
    do {
        if (curr->weaponId == id) return curr;
        curr = curr->next;
    } while (curr != head);
    return nullptr;
}

void CircularLinkedList::traverse() const {
    std::cout << "\n--- Weapon Selection Wheel [Circular Loop] (" << count << " weapons) ---\n";
    if (!head) {
        std::cout << "  [No Weapons Equipped]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "ID"
              << std::setw(18) << "Weapon Name"
              << std::setw(12) << "Energy Cost"
              << std::setw(10) << "Damage"
              << "Active?\n";
    std::cout << "--------------------------------------------------------\n";
    const WeaponNode* curr = head;
    do {
        std::cout << "[" << std::setw(4) << curr->weaponId << "] "
                  << std::setw(18) << curr->name
                  << std::setw(12) << curr->energyCost
                  << std::setw(10) << curr->damage
                  << (curr == activeWeapon ? " <== ACTIVE SELECTION" : "")
                  << "\n";
        curr = curr->next;
    } while (curr != head); // WHY: Loop terminates only when pointer completes full cycle back to head.
}

void CircularLinkedList::clear() {
    if (!head) return;
    WeaponNode* curr = head;
    WeaponNode* nextNode = nullptr;
    // WHY: Save curr->next before delete so circular cleanup stops cleanly upon reaching head.
    do {
        nextNode = curr->next;
        delete curr;
        curr = nextNode;
    } while (curr != head);
    head = activeWeapon = nullptr;
    count = 0;
}

