// ==============================================================================
// File      : PL/src/stacks/action_stack.h
// Subject   : PL-CO3 | Stack and Queue Applications
// Topics    : [TOPIC: ActionStack] [TOPIC: Stack] [TOPIC: LIFO] [TOPIC: Overflow Underflow]
// Game Role : Tracks player combat history in LIFO order to support undo and tactical rollback.
// ==============================================================================

#ifndef ACTION_STACK_H
#define ACTION_STACK_H

#include <iostream>
#include <string>

// [TOPIC: CombatAction Record]
// Purpose: Encapsulates a reversible combat event with score and health deltas.
// Game Role: Forms the unit payload pushed onto and popped from ActionStack.
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

// [TOPIC: ActionStack] [TOPIC: Stack] [TOPIC: LIFO]
// Purpose: Array-backed stack offering O(1) push, pop, and peek with explicit overflow/underflow guards.
// Game Role: Enables deterministic LIFO undo/rollback of recent combat actions.
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

    // [TOPIC: Deep Copy Semantics]
    // Purpose: Allocates separate stack storage up to topIndex on copy construction/assignment.
    // Game Role: Preserves independent action-history snapshots.
    ActionStack(const ActionStack& other);
    ActionStack& operator=(const ActionStack& other);

    // [TOPIC: LIFO Stack Operations]
    // Purpose: Pushes, pops, and inspects the top combat action in O(1) time.
    // Game Role: Records live combat events and pops the latest one during undo.
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

