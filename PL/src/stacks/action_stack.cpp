#include "action_stack.h"
#include <iomanip>

ActionStack::ActionStack(int capacity)
    : topIndex(-1), maxCapacity(capacity > 0 ? capacity : DEFAULT_CAPACITY) {
    elements = new CombatAction[maxCapacity];
}

ActionStack::~ActionStack() {
    delete[] elements;
}

ActionStack::ActionStack(const ActionStack& other)
    : topIndex(other.topIndex), maxCapacity(other.maxCapacity) {
    elements = new CombatAction[maxCapacity];
    for (int i = 0; i <= topIndex; ++i) {
        elements[i] = other.elements[i];
    }
}

ActionStack& ActionStack::operator=(const ActionStack& other) {
    if (this != &other) {
        delete[] elements;
        topIndex = other.topIndex;
        maxCapacity = other.maxCapacity;
        elements = new CombatAction[maxCapacity];
        for (int i = 0; i <= topIndex; ++i) {
            elements[i] = other.elements[i];
        }
    }
    return *this;
}

bool ActionStack::push(const CombatAction& action) {
    if (isFull()) {
        std::cerr << "[ERROR: ActionStack Overflow] Cannot push action ID " << action.actionId
                  << "; stack reached capacity " << maxCapacity << "!\n";
        return false;
    }
    elements[++topIndex] = action;
    return true;
}

bool ActionStack::pop(CombatAction& outAction) {
    if (isEmpty()) {
        std::cerr << "[ERROR: ActionStack Underflow] Cannot pop from empty combat stack!\n";
        return false;
    }
    outAction = elements[topIndex--];
    return true;
}

bool ActionStack::peek(CombatAction& outAction) const {
    if (isEmpty()) {
        return false;
    }
    outAction = elements[topIndex];
    return true;
}

void ActionStack::clear() {
    topIndex = -1;
}

void ActionStack::printStack() const {
    std::cout << "\n=== Combat Action Stack (LIFO: Top to Bottom, " << size()
              << "/" << maxCapacity << ") ===\n";
    if (isEmpty()) {
        std::cout << "  [Stack is Empty]\n";
        return;
    }
    std::cout << std::left << std::setw(6) << "Pos"
              << std::setw(6) << "ID"
              << std::setw(16) << "Type"
              << std::setw(10) << "Score"
              << std::setw(10) << "Health"
              << "Details\n";
    std::cout << "------------------------------------------------------------------\n";
    for (int i = topIndex; i >= 0; --i) {
        std::cout << "[" << std::setw(4) << i << "] "
                  << std::setw(6) << elements[i].actionId
                  << std::setw(16) << elements[i].type
                  << std::setw(10) << elements[i].scoreDelta
                  << std::setw(10) << elements[i].healthDelta
                  << elements[i].detail
                  << (i == topIndex ? " <-- TOP (Next Pop)" : "") << "\n";
    }
}

