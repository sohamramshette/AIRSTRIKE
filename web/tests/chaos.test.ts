import { describe, it } from 'node:test';
import assert from 'node:assert';
import { ChaosEventType, CHAOS_DEFINITIONS, isCompatibleChaosEvents } from '../src/chaos/ChaosEventType';
import { ActiveChaosEvent } from '../src/chaos/ActiveChaosEvent';
import { ChaosScheduler } from '../src/chaos/ChaosScheduler';
import { Player } from '../src/game/Player';

describe('AIRSTRIKE Phase 11: Chaos Protocol Web Unit Tests', () => {

  it('defines exactly 20 distinct Chaos Event types with authentic Course Outcome mappings', () => {
    const keys = Object.values(ChaosEventType);
    assert.strictEqual(keys.length, 20, 'Must have exactly 20 distinct Chaos Event types');

    // Verify all 20 definitions exist
    for (const type of keys) {
      const def = CHAOS_DEFINITIONS[type];
      assert.ok(def, `Definition must exist for ${type}`);
      assert.ok(def.id.startsWith('CHAOS_'), `Event ID must start with CHAOS_ for ${type}`);
      assert.ok(def.displayName.length > 0, `DisplayName must not be empty for ${type}`);
      assert.ok(def.courseOutcome.length > 0, `CO mapping must not be empty for ${type}`);
      assert.ok(def.defaultDuration >= 8.0 && def.defaultDuration <= 15.0, `Duration must be 8-15s for ${type}`);
      assert.ok(def.minDifficulty >= 1 && def.minDifficulty <= 5, `Difficulty must be 1-5 for ${type}`);
      assert.ok(def.cooldown >= 20.0, `Cooldown must be at least 20s for ${type}`);
      assert.ok(def.warningLine.length > 0, `Warning line must not be empty for ${type}`);
      assert.ok(def.activateLine.length > 0, `Activation line must not be empty for ${type}`);
    }

    // Verify all 19 Course Outcomes are covered
    const expectedCOs = [
      'PSOOP-CO1', 'PSOOP-CO2', 'PSOOP-CO3', 'PSOOP-CO4',
      'PL-CO1', 'PL-CO2', 'PL-CO3', 'PL-CO4', 'PL-CO5',
      'COA-CO1', 'COA-CO2', 'COA-CO3', 'COA-CO4',
      'CGL-CO1', 'CGL-CO2', 'CGL-CO3', 'CGL-CO4', 'CGL-CO5', 'CGL-CO6'
    ];

    const coveredCOs = new Set<string>();
    for (const type of keys) {
      coveredCOs.add(CHAOS_DEFINITIONS[type].courseOutcome);
    }

    for (const co of expectedCOs) {
      assert.ok(coveredCOs.has(co), `Course Outcome ${co} must be covered by Chaos Protocol definitions`);
    }
  });

  it('enforces strict mutual exclusion and incompatibility rules', () => {
    // Speed Demon vs Time Distortion
    assert.strictEqual(
      isCompatibleChaosEvents(ChaosEventType.SPEED_DEMON, ChaosEventType.TIME_DISTORTION),
      false,
      'SPEED_DEMON and TIME_DISTORTION cannot stack'
    );

    // Rapid Fire vs Weapon Jam
    assert.strictEqual(
      isCompatibleChaosEvents(ChaosEventType.RAPID_FIRE, ChaosEventType.WEAPON_JAM),
      false,
      'RAPID_FIRE and WEAPON_JAM cannot stack'
    );

    // Reverse Controls vs Gravity Well / Time Distortion
    assert.strictEqual(
      isCompatibleChaosEvents(ChaosEventType.REVERSE_CONTROLS, ChaosEventType.GRAVITY_WELL),
      false,
      'REVERSE_CONTROLS and GRAVITY_WELL cannot stack'
    );
    assert.strictEqual(
      isCompatibleChaosEvents(ChaosEventType.REVERSE_CONTROLS, ChaosEventType.TIME_DISTORTION),
      false,
      'REVERSE_CONTROLS and TIME_DISTORTION cannot stack'
    );

    // Total Chaos cannot stack with itself or anything else
    assert.strictEqual(
      isCompatibleChaosEvents(ChaosEventType.TOTAL_CHAOS, ChaosEventType.BULLET_RAIN),
      false,
      'TOTAL_CHAOS cannot stack'
    );

    // Compatible pairs
    assert.strictEqual(
      isCompatibleChaosEvents(ChaosEventType.BULLET_RAIN, ChaosEventType.MAGNETIC_BULLETS),
      true,
      'BULLET_RAIN and MAGNETIC_BULLETS can stack'
    );
  });

  it('manages 3.0s warning transition and active duration countdown', () => {
    const event = new ActiveChaosEvent(ChaosEventType.BULLET_RAIN, 10.0, 3.0);
    assert.strictEqual(event.isWarning(), true, 'Event must initially be in WARNING state');
    assert.strictEqual(event.isActive(), false, 'Event must not be active during warning');
    assert.strictEqual(event.warningTimer, 3.0);

    // Tick warning 1.5s
    const trans1 = event.update(1.5);
    assert.strictEqual(trans1, false, 'No transition yet');
    assert.strictEqual(event.isWarning(), true);
    assert.ok(Math.abs(event.warningTimer - 1.5) < 0.001);

    // Tick warning 1.6s -> transitions to active
    const trans2 = event.update(1.6);
    assert.strictEqual(trans2, true, 'Transition to ACTIVE must be reported');
    assert.strictEqual(event.isWarning(), false);
    assert.strictEqual(event.isActive(), true);
    assert.strictEqual(event.warningTimer, 0);

    // Tick active duration 5.0s
    event.update(5.0);
    assert.strictEqual(event.isActive(), true);
    assert.ok(Math.abs(event.durationRemaining - 5.0) < 0.001);
    assert.ok(Math.abs(event.getProgress() - 0.5) < 0.001);

    // Tick active duration 5.5s -> expires
    const trans3 = event.update(5.5);
    assert.strictEqual(trans3, false);
    assert.strictEqual(event.isExpired(), true);
    assert.strictEqual(event.isActive(), false);
  });

  it('manages ChaosScheduler scheduling, dispatching, and state restoration', () => {
    const scheduler = new ChaosScheduler(45.0, 30.0, 60.0);
    assert.strictEqual(scheduler.nextEventTimer, 45.0, 'First event delay must be 45s');

    const mockPlayer = new Player();
    mockPlayer.speed = 360;
    mockPlayer.shootCooldown = 0.22;
    mockPlayer.damageMultiplier = 1.0;
    mockPlayer.controlsInverted = false;

    const mockEngine = {
      player: mockPlayer,
      enemies: [],
      projectiles: [],
      waveManager: { currentLevel: 3 }
    };

    // Trigger REVERSE_CONTROLS directly
    const ev = scheduler.triggerEvent(ChaosEventType.REVERSE_CONTROLS, mockEngine, 3.0);
    assert.ok(ev !== null, 'Event should be triggered');
    assert.strictEqual(scheduler.activeEvents.length, 1);
    assert.strictEqual(scheduler.lastTriggered, ChaosEventType.REVERSE_CONTROLS);

    // Before warning ends: player controls not inverted yet
    scheduler.update(1.0, mockEngine);
    assert.strictEqual(mockPlayer.controlsInverted, false, 'Controls not inverted during warning');

    // Warning finishes -> activates
    scheduler.update(2.5, mockEngine);
    assert.strictEqual(mockPlayer.controlsInverted, true, 'Controls inverted upon activation');

    // Fast-forward past duration -> state restored
    scheduler.update(10.0, mockEngine);
    assert.strictEqual(scheduler.activeEvents.length, 0, 'Active events should be cleared');
    assert.strictEqual(mockPlayer.controlsInverted, false, 'Controls must be restored after expiration');
  });

  it('restores speed, weapon cooldown, and damage multipliers without stat drift', () => {
    const scheduler = new ChaosScheduler(10.0, 10.0, 20.0);
    const mockPlayer = new Player();
    const baseSpeed = mockPlayer.speed;
    const baseCooldown = mockPlayer.shootCooldown;
    const baseDamageMult = mockPlayer.damageMultiplier;

    const mockEngine = {
      player: mockPlayer,
      enemies: [],
      projectiles: [],
      waveManager: { currentLevel: 2 }
    };

    // Test SPEED_DEMON
    scheduler.triggerEvent(ChaosEventType.SPEED_DEMON, mockEngine, 0.1);
    scheduler.update(0.2, mockEngine); // triggers activation
    assert.ok(mockPlayer.speed > baseSpeed, 'Player speed should be increased');
    scheduler.update(15.0, mockEngine); // expires
    assert.strictEqual(mockPlayer.speed, baseSpeed, 'Player speed must be cleanly restored');

    // Test RAPID_FIRE
    scheduler.triggerEvent(ChaosEventType.RAPID_FIRE, mockEngine, 0.1);
    scheduler.update(0.2, mockEngine); // activates
    assert.ok(mockPlayer.shootCooldown < baseCooldown, 'Shoot cooldown should be decreased');
    scheduler.update(15.0, mockEngine); // expires
    assert.strictEqual(mockPlayer.shootCooldown, baseCooldown, 'Shoot cooldown must be cleanly restored');

    // Test CRITICAL_OVERDRIVE
    scheduler.triggerEvent(ChaosEventType.CRITICAL_OVERDRIVE, mockEngine, 0.1);
    scheduler.update(0.2, mockEngine); // activates
    assert.ok(mockPlayer.damageMultiplier > baseDamageMult, 'Damage multiplier should be doubled');
    scheduler.update(15.0, mockEngine); // expires
    assert.strictEqual(mockPlayer.damageMultiplier, baseDamageMult, 'Damage multiplier must be cleanly restored');
  });

  it('resets scheduler and clears all effects on game restart', () => {
    const scheduler = new ChaosScheduler(45.0, 30.0, 60.0);
    const mockPlayer = new Player();
    mockPlayer.controlsInverted = true;
    mockPlayer.damageMultiplier = 2.0;

    const mockEngine = {
      player: mockPlayer,
      enemies: [],
      projectiles: [],
      waveManager: { currentLevel: 1 }
    };

    scheduler.triggerEvent(ChaosEventType.REVERSE_CONTROLS, mockEngine, 1.0);
    assert.strictEqual(scheduler.activeEvents.length, 1);

    scheduler.reset(mockEngine);
    assert.strictEqual(scheduler.activeEvents.length, 0, 'Reset must clear active events');
    assert.strictEqual(scheduler.nextEventTimer, 45.0, 'Timer must reset to initial delay');
    assert.strictEqual(mockPlayer.controlsInverted, false, 'Controls inverted flag must reset');
    assert.strictEqual(mockPlayer.damageMultiplier, 1.0, 'Damage multiplier must reset');
  });

  it('triggers advance warning on sector clear and applies Dual Chaos Convergence for next sector', () => {
    const scheduler = new ChaosScheduler(45.0, 30.0, 60.0);
    const mockPlayer = new Player();
    const mockEngine = {
      player: mockPlayer,
      enemies: [],
      projectiles: [],
      waveManager: { currentLevel: 1 }
    };

    // Clearing Sector 1 schedules a Dual Chaos Convergence for Sector 2 with a 4s advance warning
    const event = scheduler.onSectorCleared(1, mockEngine);
    assert.ok(event, 'An anomaly must be scheduled for Sector 2');
    assert.strictEqual(event.isWarning(), true, 'Event must start in WARNING state');
    assert.strictEqual(event.warningTimer, 4.0, 'Warning timer must be 4.0 seconds');
    assert.strictEqual(scheduler.activeEvents.length, 2, 'Must schedule 2 mixed chaos events simultaneously');
    assert.ok(scheduler.activeEvents[0].dualPartnerType !== null, 'Primary event must be linked to partner');
    assert.ok(scheduler.activeEvents[1].dualPartnerType !== null, 'Secondary event must be linked to primary');

    // After 4.0s of combat transition, both anomalies activate
    scheduler.update(4.1, mockEngine);
    assert.strictEqual(event.isActive(), true, 'Primary anomaly must activate after warning countdown finishes');
    assert.strictEqual(scheduler.activeEvents[1].isActive(), true, 'Secondary anomaly must activate simultaneously');
  });

  it('mixes two compatible chaos events into a Dual Chaos Convergence without state drift', () => {
    const scheduler = new ChaosScheduler(45.0, 30.0, 60.0);
    const mockPlayer = new Player();
    const baseSpeed = mockPlayer.speed;
    const baseCooldown = mockPlayer.shootCooldown;

    const mockEngine = {
      player: mockPlayer,
      enemies: [],
      projectiles: [],
      waveManager: { currentLevel: 3 }
    };

    // Trigger dual compatible events: REVERSE_CONTROLS + SPEED_DEMON
    const ev1 = scheduler.triggerDualChaos(
      ChaosEventType.REVERSE_CONTROLS,
      ChaosEventType.SPEED_DEMON,
      mockEngine,
      1.0
    );
    assert.ok(ev1, 'Dual chaos must instantiate primary event');
    assert.strictEqual(scheduler.activeEvents.length, 2, 'Both events must be in activeEvents');

    // In warning phase
    assert.strictEqual(mockPlayer.controlsInverted, false);
    assert.strictEqual(mockPlayer.speed, baseSpeed);

    // Transition to active
    scheduler.update(1.2, mockEngine);
    assert.strictEqual(mockPlayer.controlsInverted, true, 'Controls inverted should activate');
    assert.ok(mockPlayer.speed > baseSpeed, 'Speed demon should boost speed');

    // Fast-forward until both expire
    scheduler.update(20.0, mockEngine);
    assert.strictEqual(scheduler.activeEvents.length, 0, 'Both events must expire cleanly');
    assert.strictEqual(mockPlayer.controlsInverted, false, 'Controls must be restored to normal');
    assert.strictEqual(mockPlayer.speed, baseSpeed, 'Speed must be cleanly restored to base without drift');
  });
});

