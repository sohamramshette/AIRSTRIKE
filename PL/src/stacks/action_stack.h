#ifndef ACTION_STACK_H
#define ACTION_STACK_H

#include <iostream>
#include <string>

/**
 * Stack Implementation for AirStrike Combat Action History / Undo (PL-CO3).
 * Demonstrates LIFO (Last-In, First-Out) behavior with explicit
 * overflow and underflow detection.
 */
struct CombatAction {
    int actionId;
    std::string type;       // "KILL", "POWERUP", "DAMAGE_TAKEN", "BOMB_DEPLOYED"
    int scoreDelta;
    int healthDelta;
    std::string detail;

    CombatAction() : actionId(0), type(""), scoreDelta(0), healthDelta(0), detail("") {}
    CombatAction(int id, const std::string& t, int sDelta, int hDelta, const std::string& d)
        : actionId(id), type(t), scoreDelta(sDelta), healthDelta(hDelta), detail(d) {}
};

class ActionStack {
public:
    static constexpr int DEFAULT_CAPACITY = 16;

private:
    CombatAction* elements;
    int topIndex;
    int maxCapacity;

public:
    explicit ActionStack(int capacity = DEFAULT_CAPACITY);
    ~ActionStack();

    // Deep copy operations
    ActionStack(const ActionStack& other);
    ActionStack& operator=(const ActionStack& other);

    // Core Stack Operations
    bool push(const CombatAction& action);
    bool pop(CombatAction& outAction);
    bool peek(CombatAction& outAction) const;

    // State Queries
    bool isEmpty() const { return topIndex == -1; }
    bool isFull() const { return topIndex == maxCapacity - 1; }
    int size() const { return topIndex + 1; }
    int capacity() const { return maxCapacity; }

    void clear();
    void printStack() const;
};

#endif // ACTION_STACK_H

