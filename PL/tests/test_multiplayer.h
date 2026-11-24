// ==============================================================================
// File      : PL/tests/test_multiplayer.h
// Subject   : PL-CO1..CO5 | Multiplayer Unit Tests
// Topics    : [TOPIC: MultiplayerSpatialGrid] [TOPIC: MultiplayerEventHistory] [TOPIC: MultiplayerPacketQueue] [TOPIC: MultiplayerSessionTable]
// Game Role : Declares the automated multiplayer CO1..CO5 integration test suite.
// ==============================================================================

#ifndef TEST_MULTIPLAYER_H
#define TEST_MULTIPLAYER_H

#include "test_common.h"

// [TOPIC: PL Multiplayer Unit Tests]
// Purpose: Declares the runner for multiplayer grid, history, packet queue, ranking, and session tests.
// Game Role: Integrates multiplayer verification into PL/main.cpp.
TestSummary runMultiplayerTests();

#endif // TEST_MULTIPLAYER_H
