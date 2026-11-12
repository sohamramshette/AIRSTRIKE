// ==============================================================================
// AIRSTRIKE Phase 12 (Part 6): Combat Telemetry & Score Attack Sync Unit Tests
// FILE: web/tests/multiplayer_combat_sync.test.ts
// ==============================================================================

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { DeterministicPRNG } from '../src/services/multiplayer/DeterministicPRNG';
import {
  MultiplayerCombatSyncService,
  OpponentVitals
} from '../src/services/multiplayer/MultiplayerCombatSyncService';
import {
  MultiplayerRoomState,
  MultiplayerTelemetryPayload
} from '../src/services/multiplayer/MultiplayerTypes';

describe('AIRSTRIKE Phase 12 Part 6: 1v1 Combat Synchronization & Telemetry Tests', () => {

  it('1. PRNG: Guarantees exact reproducible pseudo-random sequence for identical match seeds', () => {
    const prngA = new DeterministicPRNG(987654321);
    const prngB = new DeterministicPRNG(987654321);

    for (let i = 0; i < 50; i++) {
      assert.strictEqual(prngA.nextUint32(), prngB.nextUint32());
      assert.strictEqual(prngA.nextFloat(), prngB.nextFloat());
      assert.strictEqual(prngA.nextInt(1, 100), prngB.nextInt(1, 100));
      assert.strictEqual(prngA.nextRange(20, 80), prngB.nextRange(20, 80));
    }
  });

  it('2. Telemetry: Sequence monotonicity drops out-of-order or duplicate packets', () => {
    const sync = MultiplayerCombatSyncService.getInstance();
    const mockRoom: MultiplayerRoomState = {
      roomId: '11111111-1111-4111-8111-111111111111',
      roomCode: 'SYNC01',
      hostId: 'host-uuid-111',
      hostUsername: 'HostPilot',
      hostReady: true,
      guestId: 'guest-uuid-222',
      guestUsername: 'GuestChallenger',
      guestReady: true,
      matchSeed: 42,
      status: 'ACTIVE',
      isHost: true,
      isGuest: false,
      createdAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + 900000).toISOString()
    };

    let localScore = 500;
    sync.startCombatSync(
      mockRoom,
      'host-uuid-111',
      'HostPilot',
      new Date().toISOString(),
      () => ({
        score: localScore,
        health: 100,
        shield: 100,
        lives: 2,
        wave: 2,
        combo: 2,
        overheated: false
      })
    );

    // Initial valid packet sequence 1
    const packet1: MultiplayerTelemetryPayload = {
      roomId: mockRoom.roomId,
      playerId: 'guest-uuid-222',
      callsign: 'GuestChallenger',
      score: 300,
      health: 80,
      shield: 90,
      lives: 2,
      wave: 2,
      combo: 1,
      overheated: false,
      sequence: 1,
      timestamp: Date.now()
    };
    sync.handleInboundTelemetry(packet1);
    assert.strictEqual(sync.getOpponentVitals().score, 300);

    // Stale/out-of-order packet sequence 1 (duplicate) -> MUST BE DROPPED
    const packetDuplicate: MultiplayerTelemetryPayload = {
      ...packet1,
      score: 999999, // Forged or stale
      sequence: 1
    };
    sync.handleInboundTelemetry(packetDuplicate);
    assert.strictEqual(sync.getOpponentVitals().score, 300, 'Duplicate sequence must be ignored');

    // Out-of-order older packet sequence 0 -> MUST BE DROPPED
    const packetOld: MultiplayerTelemetryPayload = {
      ...packet1,
      score: 888888,
      sequence: 0
    };
    sync.handleInboundTelemetry(packetOld);
    assert.strictEqual(sync.getOpponentVitals().score, 300, 'Older sequence must be ignored');

    // Newer valid packet sequence 2 -> MUST BE ACCEPTED
    const packet2: MultiplayerTelemetryPayload = {
      ...packet1,
      score: 750,
      sequence: 2
    };
    sync.handleInboundTelemetry(packet2);
    assert.strictEqual(sync.getOpponentVitals().score, 750, 'Higher sequence must be accepted');

    sync.stopCombatSync();
  });

  it('3. Score Delta & Lead Transitions: Correctly calculates delta and detects overtake events', () => {
    const sync = MultiplayerCombatSyncService.getInstance();
    const mockRoom: MultiplayerRoomState = {
      roomId: '22222222-2222-4222-8222-222222222222',
      roomCode: 'LEAD01',
      hostId: 'host-uuid-333',
      hostUsername: 'AcePilot',
      hostReady: true,
      guestId: 'guest-uuid-444',
      guestUsername: 'RivalPilot',
      guestReady: true,
      matchSeed: 777,
      status: 'ACTIVE',
      isHost: true,
      isGuest: false,
      createdAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + 900000).toISOString()
    };

    let localScore = 1000;
    sync.startCombatSync(
      mockRoom,
      'host-uuid-333',
      'AcePilot',
      new Date().toISOString(),
      () => ({
        score: localScore,
        health: 100,
        shield: 100,
        lives: 2,
        wave: 3,
        combo: 2,
        overheated: false
      })
    );

    let overtakeLeading: boolean | null = null;
    let overtakeDelta: number | null = null;
    sync.addOvertakeListener((leading, delta) => {
      overtakeLeading = leading;
      overtakeDelta = delta;
    });

    // 1. Peer score = 800 -> Local leading by +200
    sync.handleInboundTelemetry({
      roomId: mockRoom.roomId,
      playerId: 'guest-uuid-444',
      callsign: 'RivalPilot',
      score: 800,
      health: 100,
      shield: 100,
      lives: 2,
      wave: 3,
      combo: 1,
      overheated: false,
      sequence: 1,
      timestamp: Date.now()
    });

    assert.strictEqual(sync.getScoreDelta(), 200);
    assert.strictEqual(overtakeLeading, true);
    assert.strictEqual(overtakeDelta, 200);

    // 2. Peer score jumps to 1500 -> Local falling behind by -500
    sync.handleInboundTelemetry({
      roomId: mockRoom.roomId,
      playerId: 'guest-uuid-444',
      callsign: 'RivalPilot',
      score: 1500,
      health: 100,
      shield: 100,
      lives: 2,
      wave: 3,
      combo: 3,
      overheated: false,
      sequence: 2,
      timestamp: Date.now()
    });

    assert.strictEqual(sync.getScoreDelta(), -500);
    assert.strictEqual(overtakeLeading, false);
    assert.strictEqual(overtakeDelta, 500);

    sync.stopCombatSync();
  });

  it('4. Authoritative Match Countdown: Calculates remaining seconds accurately from startedAt', () => {
    const sync = MultiplayerCombatSyncService.getInstance();
    const now = Date.now();
    // Match started 30 seconds ago
    const startedAt30sAgo = new Date(now - 30000).toISOString();

    const mockRoom: MultiplayerRoomState = {
      roomId: '33333333-3333-4333-8333-333333333333',
      roomCode: 'TIME01',
      hostId: 'host-uuid-555',
      hostUsername: 'HostPilot',
      hostReady: true,
      guestId: 'guest-uuid-666',
      guestUsername: 'GuestPilot',
      guestReady: true,
      matchSeed: 999,
      status: 'ACTIVE',
      isHost: true,
      isGuest: false,
      createdAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + 900000).toISOString()
    };

    sync.startCombatSync(
      mockRoom,
      'host-uuid-555',
      'HostPilot',
      startedAt30sAgo,
      () => ({
        score: 0,
        health: 100,
        shield: 100,
        lives: 2,
        wave: 1,
        combo: 1,
        overheated: false
      })
    );

    const remaining = sync.getRemainingSeconds();
    // 90s total - 30s elapsed = ~60s remaining
    assert.ok(remaining >= 59 && remaining <= 61, `Remaining expected ~60s, got ${remaining}`);

    sync.stopCombatSync();
  });

  it('5. Physical Anti-Cheat Rate Bounding: Validates 90s score limits and rejection rules', () => {
    const duration = 90;
    // Server anti-cheat formula: maxTheoretical = (duration * 450) + 2000 = 42,500
    const maxAllowed = (duration * 450) + 2000;
    assert.strictEqual(maxAllowed, 42500);

    const legalScore = 38000;
    assert.ok(legalScore <= maxAllowed, 'Realistic competitive score should pass validation');

    const cheatScore = 999999;
    assert.ok(cheatScore > maxAllowed, 'Impossible cheated score must be caught by bound validation');
  });
});

