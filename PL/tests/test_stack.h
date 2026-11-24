// ==============================================================================
// File      : PL/tests/test_stack.h
// Subject   : PL-CO3 | Stack Unit Tests
// Topics    : [TOPIC: ActionStack] [TOPIC: Stack] [TOPIC: LIFO]
// Game Role : Declares the automated PL-CO3 combat-action stack verification suite.
// ==============================================================================

#ifndef TEST_STACK_H
#define TEST_STACK_H

#include "test_common.h"

// [TOPIC: PL-CO3 Stack Unit Tests]
// Purpose: Declares the runner for ActionStack LIFO, overflow/underflow, and deep copy tests.
// Game Role: Integrates PL-CO3 stack tests into PL/main.cpp.
TestSummary runStackTests();

#endif // TEST_STACK_H

