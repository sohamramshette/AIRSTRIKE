#include "singly_linked_list.h"
#include <iomanip>

SinglyLinkedList::SinglyLinkedList() : head(nullptr), count(0) {}

SinglyLinkedList::~SinglyLinkedList() {
    clear();
}

SinglyLinkedList::SinglyLinkedList(const SinglyLinkedList& other) : head(nullptr), count(0) {
    FlightLogNode* curr = other.head;
    while (curr != nullptr) {
        insertAtTail(curr->eventId, curr->timestampSec, curr->description);
        curr = curr->next;
    }
}

SinglyLinkedList& SinglyLinkedList::operator=(const SinglyLinkedList& other) {
    if (this != &other) {
        clear();
        FlightLogNode* curr = other.head;
        while (curr != nullptr) {
            insertAtTail(curr->eventId, curr->timestampSec, curr->description);
            curr = curr->next;
        }
    }
    return *this;
}

void SinglyLinkedList::insertAtHead(int id, double timestampSec, const std::string& desc) {
    head = new FlightLogNode(id, timestampSec, desc, head);
    count++;
}

void SinglyLinkedList::insertAtTail(int id, double timestampSec, const std::string& desc) {
    FlightLogNode* newNode = new FlightLogNode(id, timestampSec, desc, nullptr);
    if (!head) {
        head = newNode;
    } else {
        FlightLogNode* curr = head;
        while (curr->next != nullptr) {
            curr = curr->next;
        }
        curr->next = newNode;
    }
    count++;
}

bool SinglyLinkedList::insertAt(size_t index, int id, double timestampSec, const std::string& desc) {
    if (index > count) return false;
    if (index == 0) {
        insertAtHead(id, timestampSec, desc);
        return true;
    }
    FlightLogNode* curr = head;
    for (size_t i = 0; i < index - 1; ++i) {
        curr = curr->next;
    }
    curr->next = new FlightLogNode(id, timestampSec, desc, curr->next);
    count++;
    return true;
}

bool SinglyLinkedList::deleteHead() {
    if (!head) return false;
    FlightLogNode* temp = head;
    head = head->next;
    delete temp;
    count--;
    return true;
}

bool SinglyLinkedList::deleteTail() {
    if (!head) return false;
    if (!head->next) {
        delete head;
        head = nullptr;
        count--;
        return true;
    }
    FlightLogNode* curr = head;
    while (curr->next->next != nullptr) {
        curr = curr->next;
    }
    delete curr->next;
    curr->next = nullptr;
    count--;
    return true;
}

bool SinglyLinkedList::deleteAt(size_t index) {
    if (index >= count || !head) return false;
    if (index == 0) {
        return deleteHead();
    }
    FlightLogNode* curr = head;
    for (size_t i = 0; i < index - 1; ++i) {
        curr = curr->next;
    }
    FlightLogNode* target = curr->next;
    curr->next = target->next;
    delete target;
    count--;
    return true;
}

bool SinglyLinkedList::deleteById(int id) {
    if (!head) return false;
    if (head->eventId == id) {
        return deleteHead();
    }
    FlightLogNode* curr = head;
    while (curr->next != nullptr && curr->next->eventId != id) {
        curr = curr->next;
    }
    if (curr->next != nullptr) {
        FlightLogNode* target = curr->next;
        curr->next = target->next;
        delete target;
        count--;
        return true;
    }
    return false;
}

const FlightLogNode* SinglyLinkedList::search(int id) const {
    FlightLogNode* curr = head;
    while (curr != nullptr) {
        if (curr->eventId == id) {
            return curr;
        }
        curr = curr->next;
    }
    return nullptr;
}

FlightLogNode* SinglyLinkedList::searchMut(int id) {
    FlightLogNode* curr = head;
    while (curr != nullptr) {
        if (curr->eventId == id) {
            return curr;
        }
        curr = curr->next;
    }
    return nullptr;
}

void SinglyLinkedList::traverse() const {
    std::cout << "\n--- AirStrike Chronological Flight Log (" << count << " entries) ---\n";
    if (!head) {
        std::cout << "  [Empty Flight Log]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "ID"
              << std::setw(12) << "Time (s)"
              << "Event Description\n";
    std::cout << "--------------------------------------------------------\n";
    const FlightLogNode* curr = head;
    while (curr != nullptr) {
        std::cout << "[" << std::setw(4) << curr->eventId << "] "
                  << std::fixed << std::setprecision(2) << std::setw(12) << curr->timestampSec
                  << curr->description << "\n";
        curr = curr->next;
    }
}

void SinglyLinkedList::clear() {
    while (head != nullptr) {
        FlightLogNode* temp = head;
        head = head->next;
        delete temp;
    }
    count = 0;
}
