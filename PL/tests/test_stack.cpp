#include "test_stack.h"
#include "../src/stacks/action_stack.h"

TestSummary runStackTests() {
    TestSummary summary;
    std::cout << "\n======================================================\n";
    std::cout << "[TEST SUITE] PL-CO3: Stack Operations (6 Tests)\n";
    std::cout << "======================================================\n";

    // 1. Push and Peek (LIFO Verification)
    ActionStack stack(4);
    stack.push(CombatAction(1, "KILL", 100, 0, "Destroyed Drone"));
    stack.push(CombatAction(2, "POWERUP", 50, 20, "Shield Pack Collected"));

    CombatAction topAct;
    bool peekOk = stack.peek(topAct);
    summary.record(peekOk && topAct.actionId == 2 && stack.size() == 2,
                   "Stack: Push and Peek inspects top LIFO element");

    // 2. Pop (LIFO Order)
    CombatAction popped;
    bool popOk = stack.pop(popped);
    summary.record(popOk && popped.actionId == 2 && stack.size() == 1,
                   "Stack: Pop removes and returns most recent action");

    // 3. Underflow Protection
    CombatAction temp;
    stack.pop(temp); // Now empty
    bool underflowOk = !stack.pop(temp);
    summary.record(underflowOk && stack.isEmpty(),
                   "Stack: Underflow protection prevents popping empty stack");

    // 4. Overflow Protection
    ActionStack boundedStack(2);
    boundedStack.push(CombatAction(10, "KILL", 100, 0, "D1"));
    boundedStack.push(CombatAction(20, "KILL", 100, 0, "D2"));
    bool overflowBlocked = !boundedStack.push(CombatAction(30, "KILL", 100, 0, "D3"));
    summary.record(overflowBlocked && boundedStack.isFull() && boundedStack.size() == 2,
                   "Stack: Overflow protection prevents pushing beyond capacity");

    // 5. Deep Copy Semantics
    ActionStack copyStack = boundedStack;
    CombatAction dummy;
    copyStack.pop(dummy);
    summary.record(boundedStack.size() == 2 && copyStack.size() == 1,
                   "Stack: Deep copy maintains independent buffer state");

    // 6. Clear Operation
    boundedStack.clear();
    summary.record(boundedStack.isEmpty() && boundedStack.size() == 0,
                   "Stack: Clear resets top index to empty");

    return summary;
}
