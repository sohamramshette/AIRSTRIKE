// ==============================================================================
// File      : PL/tests/test_hashing.h
// Subject   : PL-CO5 | Hashing Unit Tests
// Topics    : [TOPIC: HashTableChaining] [TOPIC: HashTableProbing] [TOPIC: Tombstone Deletion] [TOPIC: Rehashing]
// Game Role : Declares the automated PL-CO5 hashing verification suite.
// ==============================================================================

#ifndef TEST_HASHING_H
#define TEST_HASHING_H

#include "test_common.h"

// [TOPIC: PL-CO5 Hashing Unit Tests]
// Purpose: Declares the runner for Separate Chaining, Linear Probing, Tombstones, and Rehashing tests.
// Game Role: Integrates PL-CO5 hashing tests into PL/main.cpp.
TestSummary runHashingTests();

#endif // TEST_HASHING_H

