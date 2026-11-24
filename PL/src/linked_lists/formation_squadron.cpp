// ==============================================================================
// File      : PL/src/linked_lists/formation_squadron.cpp
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: FormationSquadron] [TOPIC: Doubly Linked List] [TOPIC: O(1) List Splitting]
// Game Role : Implements doubly-linked squadron maneuvers, O(1) craft removal, and chain splitting.
// ==============================================================================

#include "formation_squadron.h"
#include <iomanip>

FormationSquadron::FormationSquadron(const std::string& name)
    : head(nullptr), tail(nullptr), craftCount(0), squadronName(name) {}

FormationSquadron::~FormationSquadron() {
    clear();
}

FormationSquadron::FormationSquadron(const FormationSquadron& other)
    : head(nullptr), tail(nullptr), craftCount(0), squadronName(other.squadronName + "-Clone") {
    SquadronCraft* curr = other.head;
    while (curr != nullptr) {
        addRearguard(curr->shipId, curr->callsign, curr->health, curr->posX, curr->posY);
        curr = curr->next;
    }
}

FormationSquadron& FormationSquadron::operator=(const FormationSquadron& other) {
    if (this != &other) {
        clear();
        squadronName = other.squadronName;
        SquadronCraft* curr = other.head;
        while (curr != nullptr) {
            addRearguard(curr->shipId, curr->callsign, curr->health, curr->posX, curr->posY);
            curr = curr->next;
        }
    }
    return *this;
}

void FormationSquadron::clear() {
    while (head != nullptr) {
        SquadronCraft* temp = head;
        head = head->next;
        delete temp;
    }
    tail = nullptr;
    craftCount = 0;
}

void FormationSquadron::addVanguard(int id, const std::string& callsign, int health, double x, double y) {
    SquadronCraft* newNode = new SquadronCraft(id, callsign, health, x, y, 0.0, 0.0, nullptr, head);
    if (!head) {
        head = tail = newNode;
    } else {
        head->prev = newNode;
        head = newNode;
    }
    craftCount++;
}

void FormationSquadron::addRearguard(int id, const std::string& callsign, int health, double x, double y) {
    SquadronCraft* newNode = new SquadronCraft(id, callsign, health, x, y, 0.0, 0.0, tail, nullptr);
    if (!tail) {
        head = tail = newNode;
    } else {
        tail->next = newNode;
        tail = newNode;
    }
    craftCount++;
}

bool FormationSquadron::insertAtRank(size_t rankIndex, int id, const std::string& callsign, int health, double x, double y) {
    if (rankIndex > craftCount) return false;
    if (rankIndex == 0) {
        addVanguard(id, callsign, health, x, y);
        return true;
    }
    if (rankIndex == craftCount) {
        addRearguard(id, callsign, health, x, y);
        return true;
    }

    SquadronCraft* curr = head;
    for (size_t i = 0; i < rankIndex; ++i) {
        curr = curr->next;
    }

    SquadronCraft* newNode = new SquadronCraft(id, callsign, health, x, y, 0.0, 0.0, curr->prev, curr);
    curr->prev->next = newNode;
    curr->prev = newNode;
    craftCount++;
    return true;
}

// [TOPIC: O(1) Node Deletion] [TOPIC: Doubly Linked List]
// Purpose: Splices node out of the doubly linked chain by updating neighbors in O(1).
// Game Role: Removes a destroyed wingman without reallocating or shifting remaining ships.
void FormationSquadron::removeNode(SquadronCraft* node) {
    if (!node) return;

    if (node == head) {
        head = head->next;
        if (head) {
            head->prev = nullptr;
        } else {
            tail = nullptr;
        }
    } else if (node == tail) {
        tail = tail->prev;
        if (tail) {
            tail->next = nullptr;
        } else {
            head = nullptr;
        }
    } else {
        // WHY: Direct prev/next relinking eliminates O(N) array shifting on mid-chain deletion.
        node->prev->next = node->next;
        node->next->prev = node->prev;
    }

    delete node;
    craftCount--;
}

bool FormationSquadron::removeShip(int shipId) {
    SquadronCraft* target = findShipMut(shipId);
    if (target) {
        removeNode(target);
        return true;
    }
    return false;
}

// [TOPIC: O(1) List Splitting] [TOPIC: Doubly Linked List]
// Purpose: Cuts the doubly linked chain after targetShipId and transfers trailing nodes to outSecondSquadron.
// Game Role: Splits a long enemy formation into two independent combat wings upon mid-chain break.
bool FormationSquadron::splitAt(int targetShipId, FormationSquadron& outSecondSquadron) {
    SquadronCraft* target = findShipMut(targetShipId);
    if (!target) return false;
    // WHY: Nothing trails the anchor craft, so splitting at tail produces no second squadron.
    if (target == tail) return false;

    SquadronCraft* secondHead = target->next;
    SquadronCraft* secondTail = tail;

    // Count how many nodes move to outSecondSquadron
    size_t trailingCount = 0;
    SquadronCraft* curr = secondHead;
    while (curr != nullptr) {
        trailingCount++;
        curr = curr->next;
    }

    // WHY: Sever target->next and retarget tail in O(1) without copying or reallocating craft nodes.
    target->next = nullptr;
    tail = target;
    craftCount -= trailingCount;

    // WHY: Clear destination squadron first to prevent leaks before adopting severed chain ownership.
    outSecondSquadron.clear();
    secondHead->prev = nullptr;
    outSecondSquadron.head = secondHead;
    outSecondSquadron.tail = secondTail;
    outSecondSquadron.craftCount = trailingCount;
    outSecondSquadron.squadronName = squadronName + "-Detachment";

    return true;
}

const SquadronCraft* FormationSquadron::findShip(int shipId) const {
    const SquadronCraft* curr = head;
    while (curr != nullptr) {
        if (curr->shipId == shipId) return curr;
        curr = curr->next;
    }
    return nullptr;
}

SquadronCraft* FormationSquadron::findShipMut(int shipId) {
    SquadronCraft* curr = head;
    while (curr != nullptr) {
        if (curr->shipId == shipId) return curr;
        curr = curr->next;
    }
    return nullptr;
}

void FormationSquadron::advanceFormation(double deltaX, double deltaY) {
    SquadronCraft* curr = head;
    while (curr != nullptr) {
        curr->posX += deltaX;
        curr->posY += deltaY;
        curr = curr->next;
    }
}

void FormationSquadron::traverseVanguardToRear() const {
    std::cout << "\n=== Formation [" << squadronName << "] Vanguard -> Rearguard (" << craftCount << " craft) ===\n";
    if (!head) {
        std::cout << "  [Squadron Disbanded / Empty]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "ID"
              << std::setw(18) << "Callsign"
              << std::setw(8) << "HP"
              << "Position (X, Y)\n";
    std::cout << "--------------------------------------------------------\n";
    const SquadronCraft* curr = head;
    while (curr != nullptr) {
        std::cout << "[" << std::setw(4) << curr->shipId << "] "
                  << std::setw(18) << curr->callsign
                  << std::setw(8) << curr->health
                  << "(" << curr->posX << ", " << curr->posY << ")"
                  << (curr == head ? " <-- VANGUARD" : "")
                  << (curr == tail ? " <-- REARGUARD" : "")
                  << "\n";
        curr = curr->next;
    }
}

void FormationSquadron::traverseRearToVanguard() const {
    std::cout << "\n=== Formation [" << squadronName << "] Rearguard -> Vanguard (" << craftCount << " craft) ===\n";
    if (!tail) {
        std::cout << "  [Squadron Disbanded / Empty]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "ID"
              << std::setw(18) << "Callsign"
              << std::setw(8) << "HP"
              << "Position (X, Y)\n";
    std::cout << "--------------------------------------------------------\n";
    const SquadronCraft* curr = tail;
    while (curr != nullptr) {
        std::cout << "[" << std::setw(4) << curr->shipId << "] "
                  << std::setw(18) << curr->callsign
                  << std::setw(8) << curr->health
                  << "(" << curr->posX << ", " << curr->posY << ")"
                  << (curr == tail ? " <-- REARGUARD" : "")
                  << (curr == head ? " <-- VANGUARD" : "")
                  << "\n";
        curr = curr->prev;
    }
}
