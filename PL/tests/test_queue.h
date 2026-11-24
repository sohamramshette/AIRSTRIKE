// ==============================================================================
// File      : PL/tests/test_queue.h
// Subject   : PL-CO3 | Queue Unit Tests
// Topics    : [TOPIC: WaveQueue] [TOPIC: Circular Queue] [TOPIC: FIFO]
// Game Role : Declares the automated PL-CO3 enemy wave queue verification suite.
// ==============================================================================

#ifndef TEST_QUEUE_H
#define TEST_QUEUE_H

#include "test_common.h"

// [TOPIC: PL-CO3 Queue Unit Tests]
// Purpose: Declares the runner for WaveQueue FIFO, circular wrap-around, and boundary tests.
// Game Role: Integrates PL-CO3 queue tests into PL/main.cpp.
TestSummary runQueueTests();

#endif // TEST_QUEUE_H

