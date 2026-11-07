#ifndef FORMATION_SQUADRON_H
#define FORMATION_SQUADRON_H

#include <iostream>
#include <string>

/**
 * Formation Squadron Chain (PL-CO2).
 *
 * Demonstrates:
 * - Doubly Linked List Structure: Each craft node maintains prev and next pointers.
 * - Bidirectional Traversal: Vanguard to Rearguard and Rearguard to Vanguard.
 * - O(1) Node Splitting: Severing a formation link cuts the squadron into two
 *   distinct, fully operational squadrons in O(1) pointer operations.
 * - O(1) Node Removal: Removing a destroyed node directly via pointer rewiring.
 * - Formation Movement & Coordinate Shifting: Updating positions along flight path.
 */
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

class FormationSquadron {
private:
    SquadronCraft* head; // Vanguard (Lead ship)
    SquadronCraft* tail; // Rearguard (Anchor ship)
    size_t craftCount;
    std::string squadronName;

public:
    explicit FormationSquadron(const std::string& name = "Alpha-Wing");
    ~FormationSquadron();

    // Deep copy operations
    FormationSquadron(const FormationSquadron& other);
    FormationSquadron& operator=(const FormationSquadron& other);

    // Dynamic insertion
    void addVanguard(int id, const std::string& callsign, int health, double x, double y);
    void addRearguard(int id, const std::string& callsign, int health, double x, double y);
    bool insertAtRank(size_t rankIndex, int id, const std::string& callsign, int health, double x, double y);

    // Tactical Deletion / Destruction (O(1) given node, O(N) by ID)
    bool removeShip(int shipId);
    void removeNode(SquadronCraft* node);

    // Splitting: Divides current formation at targetShipId.
    // The nodes after targetShipId become `outSecondSquadron` (with valid head, tail, count).
    // The current formation retains nodes up to and including targetShipId.
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
