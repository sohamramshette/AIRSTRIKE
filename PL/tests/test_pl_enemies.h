// ==============================================================================
// File      : PL/tests/test_pl_enemies.h
// Subject   : PL-CO1..CO5 | PL Enemy Systems Unit Tests
// Topics    : [TOPIC: GridSwarmEnemy] [TOPIC: FormationSquadron] [TOPIC: EchelonDispatcher] [TOPIC: HunterTargeting] [TOPIC: HashShieldController]
// Game Role : Declares the automated PL-CO1..CO5 enemy systems verification suite.
// ==============================================================================

#ifndef TEST_PL_ENEMIES_H
#define TEST_PL_ENEMIES_H

#include "test_common.h"

// [TOPIC: PL Enemy Systems Tests]
// Purpose: Declares the runner verifying all five CO-aligned PL enemy subsystems.
// Game Role: Integrates PL enemy tests into PL/main.cpp.
TestSummary runPLEnemyTests();

#endif // TEST_PL_ENEMIES_H
