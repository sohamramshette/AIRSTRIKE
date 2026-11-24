// ==============================================================================
// File      : PL/src/linked_lists/doubly_linked_list.cpp
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: Doubly Linked List] [TOPIC: O(1) Node Deletion] [TOPIC: Bidirectional Traversal]
// Game Role : Implements bidirectional enemy squadron management with O(1) node splicing.
// ==============================================================================

#include "doubly_linked_list.h"
#include <iomanip>

DoublyLinkedList::DoublyLinkedList() : head(nullptr), tail(nullptr), count(0) {}

DoublyLinkedList::~DoublyLinkedList() {
    clear();
}

DoublyLinkedList::DoublyLinkedList(const DoublyLinkedList& other)
    : head(nullptr), tail(nullptr), count(0) {
    EnemyNode* curr = other.head;
    while (curr != nullptr) {
        insertTail(curr->enemyId, curr->shipType, curr->health);
        curr = curr->next;
    }
}

DoublyLinkedList& DoublyLinkedList::operator=(const DoublyLinkedList& other) {
    if (this != &other) {
        clear();
        EnemyNode* curr = other.head;
        while (curr != nullptr) {
            insertTail(curr->enemyId, curr->shipType, curr->health);
            curr = curr->next;
        }
    }
    return *this;
}

void DoublyLinkedList::insertHead(int id, const std::string& type, int health) {
    EnemyNode* newNode = new EnemyNode(id, type, health, nullptr, head);
    if (!head) {
        // WHY: Empty list sets both head and tail to the sole node to keep endpoint invariants intact.
        head = tail = newNode;
    } else {
        head->prev = newNode;
        head = newNode;
    }
    count++;
}

void DoublyLinkedList::insertTail(int id, const std::string& type, int health) {
    EnemyNode* newNode = new EnemyNode(id, type, health, tail, nullptr);
    if (!tail) {
        head = tail = newNode;
    } else {
        tail->next = newNode;
        tail = newNode;
    }
    count++;
}

bool DoublyLinkedList::insertAt(size_t index, int id, const std::string& type, int health) {
    if (index > count) return false;
    if (index == 0) {
        insertHead(id, type, health);
        return true;
    }
    if (index == count) {
        insertTail(id, type, health);
        return true;
    }
    EnemyNode* curr = head;
    for (size_t i = 0; i < index; ++i) {
        curr = curr->next;
    }
    // WHY: Splice both curr->prev->next and curr->prev before curr so backward/forward links stay symmetric.
    EnemyNode* newNode = new EnemyNode(id, type, health, curr->prev, curr);
    curr->prev->next = newNode;
    curr->prev = newNode;
    count++;
    return true;
}

bool DoublyLinkedList::deleteHead() {
    if (!head) return false;
    EnemyNode* temp = head;
    head = head->next;
    if (head) {
        head->prev = nullptr;
    } else {
        tail = nullptr; // WHY: Null tail when removing the last remaining node to prevent dangling tail.
    }
    delete temp;
    count--;
    return true;
}

bool DoublyLinkedList::deleteTail() {
    if (!tail) return false;
    EnemyNode* temp = tail;
    tail = tail->prev;
    if (tail) {
        tail->next = nullptr;
    } else {
        head = nullptr;
    }
    delete temp;
    count--;
    return true;
}

// [TOPIC: O(1) Node Deletion] [TOPIC: Doubly Linked List]
// Purpose: Rewires neighbor prev/next pointers directly to unlink node in O(1) time.
// Game Role: Removes a destroyed enemy craft mid-screen without walking from head.
void DoublyLinkedList::deleteNode(EnemyNode* node) {
    if (!node) return;
    if (node == head) {
        deleteHead();
        return;
    }
    if (node == tail) {
        deleteTail();
        return;
    }
    // WHY: Bridge predecessor and successor directly via prev/next in O(1) -> Game: Instant mid-wing removal.
    node->prev->next = node->next;
    node->next->prev = node->prev;
    delete node;
    count--;
}

bool DoublyLinkedList::deleteById(int id) {
    EnemyNode* target = searchMut(id);
    if (target) {
        deleteNode(target);
        return true;
    }
    return false;
}

const EnemyNode* DoublyLinkedList::search(int id) const {
    EnemyNode* curr = head;
    while (curr != nullptr) {
        if (curr->enemyId == id) return curr;
        curr = curr->next;
    }
    return nullptr;
}

EnemyNode* DoublyLinkedList::searchMut(int id) {
    EnemyNode* curr = head;
    while (curr != nullptr) {
        if (curr->enemyId == id) return curr;
        curr = curr->next;
    }
    return nullptr;
}

void DoublyLinkedList::traverseForward() const {
    std::cout << "\n--- Enemy Squadron Forward Inspection [Vanguard -> Rear] (" << count << " active) ---\n";
    if (!head) {
        std::cout << "  [Squadron Cleared]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "ID"
              << std::setw(16) << "Ship Type"
              << std::setw(8) << "Health\n";
    std::cout << "-------------------------------------\n";
    const EnemyNode* curr = head;
    while (curr != nullptr) {
        std::cout << "[" << std::setw(4) << curr->enemyId << "] "
                  << std::setw(16) << curr->shipType
                  << std::setw(8) << curr->health << "\n";
        curr = curr->next;
    }
}

void DoublyLinkedList::traverseBackward() const {
    std::cout << "\n--- Enemy Squadron Backward Inspection [Rear -> Vanguard] (" << count << " active) ---\n";
    if (!tail) {
        std::cout << "  [Squadron Cleared]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "ID"
              << std::setw(16) << "Ship Type"
              << std::setw(8) << "Health\n";
    std::cout << "-------------------------------------\n";
    const EnemyNode* curr = tail;
    while (curr != nullptr) {
        std::cout << "[" << std::setw(4) << curr->enemyId << "] "
                  << std::setw(16) << curr->shipType
                  << std::setw(8) << curr->health << "\n";
        curr = curr->prev;
    }
}

void DoublyLinkedList::clear() {
    while (head != nullptr) {
        EnemyNode* temp = head;
        head = head->next;
        delete temp;
    }
    tail = nullptr;
    count = 0;
}

