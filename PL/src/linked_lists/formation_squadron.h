// ==============================================================================
// File      : PL/src/linked_lists/formation_squadron.h
// Subject   : PL-CO2 | Suitable Linked Lists
// Topics    : [TOPIC: FormationSquadron] [TOPIC: Doubly Linked List] [TOPIC: O(1) List Splitting] [TOPIC: O(1) Node Removal]
// Game Role : Models the PL-CO2 Formation Squadron Chain enemy that splits into two wings on mid-chain hits.
// ==============================================================================

#ifndef FORMATION_SQUADRON_H
#define FORMATION_SQUADRON_H

#include <iostream>
#include <string>

// [TOPIC: SquadronCraft] [TOPIC: Doubly Linked Node]
// Purpose: Stores a linked enemy craft's telemetry, coordinates, formation offsets, and prev/next links.
// Game Role: Forms an individual segment of the FormationSquadron chain enemy.
struct SquadronCraft {
    int shipId;
    std::string callsign;
    int health;
    int maxHealth;
    double posX;
    double posY;
    double offsetX;
    double offsetY;
    SquadronCraft* prev;
    SquadronCraft* next;

    SquadronCraft(int id, const std::string& name, int hp, double x, double y,
                  double offX = 0.0, double offY = 0.0,
                  SquadronCraft* p = nullptr, SquadronCraft* n = nullptr)
        : shipId(id), callsign(name), health(hp), maxHealth(hp),
          posX(x), posY(y), offsetX(offX), offsetY(offY), prev(p), next(n) {}
};

// [TOPIC: FormationSquadron] [TOPIC: Doubly Linked List] [TOPIC: O(1) List Splitting]
// Purpose: Doubly linked enemy formation supporting O(1) craft unlinking and O(1) pointer-severing splits.
// Game Role: Drives the PL-CO2 splitting centipede squadron that divides when a middle craft is destroyed.
class FormationSquadron {
private:
    SquadronCraft* head; // Vanguard (Lead ship)
    SquadronCraft* tail; // Rearguard (Anchor ship)
    size_t craftCount;
    std::string squadronName;

public:
    explicit FormationSquadron(const std::string& name = "Alpha-Wing");
    ~FormationSquadron();

    FormationSquadron(const FormationSquadron& other);
    FormationSquadron& operator=(const FormationSquadron& other);

    void addVanguard(int id, const std::string& callsign, int health, double x, double y);
    void addRearguard(int id, const std::string& callsign, int health, double x, double y);
    bool insertAtRank(size_t rankIndex, int id, const std::string& callsign, int health, double x, double y);

    // [TOPIC: O(1) Node Deletion]
    // Purpose: Unlinks a destroyed craft node via direct prev/next pointer rewiring without array shifts.
    // Game Role: Eliminates shot-down squadron members cleanly.
    bool removeShip(int shipId);
    void removeNode(SquadronCraft* node);

    // [TOPIC: O(1) List Splitting] [TOPIC: Doubly Linked List]
    // Purpose: Severs the formation after targetShipId, transferring trailing nodes into outSecondSquadron.
    // Game Role: Splits a damaged enemy formation into two autonomous combat detachments.
    bool splitAt(int targetShipId, FormationSquadron& outSecondSquadron);

    // Search
    const SquadronCraft* findShip(int shipId) const;
    SquadronCraft* findShipMut(int shipId);

    // Maneuvers
    void advanceFormation(double deltaX, double deltaY);

    // Traversals
    void traverseVanguardToRear() const;
    void traverseRearToVanguard() const;

    // Queries
    size_t size() const { return craftCount; }
    bool isEmpty() const { return craftCount == 0; }
    const SquadronCraft* getVanguard() const { return head; }
    const SquadronCraft* getRearguard() const { return tail; }
    const std::string& getName() const { return squadronName; }
    void setName(const std::string& name) { squadronName = name; }

    void clear();
};

#endif // FORMATION_SQUADRON_H
