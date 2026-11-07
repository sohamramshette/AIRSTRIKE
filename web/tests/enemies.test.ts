import { test, describe } from 'node:test';
import assert from 'node:assert';
import { Drone } from '../src/game/Drone';
import { Fighter } from '../src/game/Fighter';
import { Boss } from '../src/game/Boss';
import { Armoured } from '../src/game/Armoured';
import { Interceptor } from '../src/game/Interceptor';
import { Morphing } from '../src/game/Morphing';
import { FailSafe } from '../src/game/FailSafe';
import { Rammer } from '../src/game/Rammer';

describe('Enemy Forces Unit Tests', () => {
  test('Drone Scout properties and sine-wave trajectory', () => {
    const drone = new Drone(200, 100);
    assert.strictEqual(drone.width, 32);
    assert.strictEqual(drone.height, 32);
    assert.strictEqual(drone.health, 30);
    assert.strictEqual(drone.shield, 0);
    assert.strictEqual(drone.scoreValue, 100);
    assert.strictEqual(drone.enemyType, 'DRONE');

    // Update for 0.5 seconds
    drone.update(0.5);
    assert.strictEqual(drone.y, 100 + 200 * 0.5);
    const expectedX = 200 + 35 * Math.sin(0.5 * 4.5);
    assert.ok(Math.abs(drone.x - expectedX) < 0.001);

    // Drones do not fire projectiles
    assert.strictEqual(drone.fire().length, 0);
  });

  test('Combat Fighter properties, banking, and projectile attacks', () => {
    const fighter = new Fighter(300, 150);
    assert.strictEqual(fighter.width, 42);
    assert.strictEqual(fighter.height, 44);
    assert.strictEqual(fighter.health, 60);
    assert.strictEqual(fighter.shield, 20);
    assert.strictEqual(fighter.scoreValue, 250);
    assert.strictEqual(fighter.enemyType, 'FIGHTER');

    // Shield absorption on fighter
    fighter.takeDamage(15);
    assert.strictEqual(fighter.shield, 5);
    assert.strictEqual(fighter.health, 60);

    fighter.takeDamage(15);
    assert.strictEqual(fighter.shield, 0);
    assert.strictEqual(fighter.health, 50);

    // Weapons firing
    fighter.shootTimer = 0;
    const bullets = fighter.fire();
    assert.strictEqual(bullets.length, 2);
    assert.strictEqual(bullets[0].damage, 15);
    assert.strictEqual(bullets[1].damage, 15);
    assert.strictEqual(bullets[0].vy, 340.0);
    assert.strictEqual(bullets[0].isPlayerBullet, false);
  });

  test('Flagship Dreadnought Boss properties and cyclic attack phases', () => {
    const boss = new Boss(3);
    assert.strictEqual(boss.width, 116);
    assert.strictEqual(boss.height, 78);
    assert.strictEqual(boss.health, 550);
    assert.strictEqual(boss.shield, 150);
    assert.strictEqual(boss.scoreValue, 2500);
    assert.strictEqual(boss.enemyType, 'BOSS');

    // Finish entrance descent
    boss.isEntering = false;
    boss.y = 45;

    // Phase 0: 5-way spread fan
    boss.phase = 0;
    boss.shootTimer = 0;
    const fanBullets = boss.fire(400, 500);
    assert.strictEqual(fanBullets.length, 5);
    assert.deepStrictEqual(
      fanBullets.map(b => b.vx),
      [-160, -80, 0, 80, 160]
    );

    // Phase 1: Heavy dual plasma cannons
    boss.phase = 1;
    boss.shootTimer = 0;
    const heavyBullets = boss.fire(400, 500);
    assert.strictEqual(heavyBullets.length, 2);
    assert.strictEqual(heavyBullets[0].damage, 25);
    assert.strictEqual(heavyBullets[0].vy, 360.0);

    // Phase 2: Targeted barrage towards player
    boss.phase = 2;
    boss.shootTimer = 0;
    const targetBullets = boss.fire(400, 550);
    assert.strictEqual(targetBullets.length, 1);
    assert.strictEqual(targetBullets[0].damage, 20);
    assert.ok(targetBullets[0].vy > 0);
  });

  test('Flagship patrol sweep stays within 30 to 650 boundary', () => {
    const boss = new Boss(3);
    boss.isEntering = false;
    boss.y = 45;

    // Move to right edge
    boss.x = 651;
    boss.vx = 80;
    boss.update(0.016);
    assert.strictEqual(boss.x, 650);
    assert.strictEqual(boss.vx, -80);

    // Move to left edge
    boss.x = 29;
    boss.vx = -80;
    boss.update(0.016);
    assert.strictEqual(boss.x, 30);
    assert.strictEqual(boss.vx, 80);
  });

  test('Armoured Vanguard properties, encapsulation, and armor damage reduction (PSOOP-CO1)', () => {
    const armoured = new Armoured(250, 100);
    assert.strictEqual(armoured.width, 46);
    assert.strictEqual(armoured.height, 44);
    assert.strictEqual(armoured.health, 90);
    assert.strictEqual(armoured.shield, 30);
    assert.strictEqual(armoured.scoreValue, 350);
    assert.strictEqual(armoured.enemyType, 'ARMOURED');
    assert.strictEqual(armoured.getArmorRating(), 8);

    // 1. Shield absorbs damage first
    armoured.takeDamage(20);
    assert.strictEqual(armoured.shield, 10);
    assert.strictEqual(armoured.health, 90);

    // 2. Shield absorbs 10, remaining 20 hits armor (reduced by 8 to 12)
    armoured.takeDamage(30);
    assert.strictEqual(armoured.shield, 0);
    // 90 - (30 - 10 - 8) = 90 - 12 = 78
    assert.strictEqual(armoured.health, 78);

    // 3. Firing heavy prow slug
    armoured.shootTimer = 0;
    const slugs = armoured.fire();
    assert.strictEqual(slugs.length, 1);
    assert.strictEqual(slugs[0].damage, 20);
    assert.strictEqual(slugs[0].vy, 280.0);
  });

  test('Interceptor Fighter alignment detection, high-speed dash, and staggered twin darts (PSOOP-CO1)', () => {
    const interceptor = new Interceptor(200, 100);
    assert.strictEqual(interceptor.width, 38);
    assert.strictEqual(interceptor.height, 40);
    assert.strictEqual(interceptor.health, 45);
    assert.strictEqual(interceptor.shield, 10);
    assert.strictEqual(interceptor.scoreValue, 200);
    assert.strictEqual(interceptor.enemyType, 'INTERCEPTOR');

    // Initially cruising at normal speed
    assert.strictEqual(interceptor.vy, 110.0);
    assert.strictEqual(interceptor.dashing, false);

    // Player aligned horizontally (player X near interceptor X)
    interceptor.timeAlive = 1.0;
    interceptor.shootTimer = 0;
    const bullets = interceptor.fire(interceptor.x, 300);

    // Triggers dash
    assert.strictEqual(interceptor.dashing, true);
    interceptor.update(0.016);
    assert.strictEqual(interceptor.vy, 260.0);

    // First shot fired immediately
    assert.strictEqual(bullets.length, 1);
    assert.strictEqual(bullets[0].damage, 12);
  });

  test('Adaptive Morphing Drone state transitions and reduced complexity (PSOOP-CO2)', () => {
    const morphing = new Morphing(300, 100);
    assert.strictEqual(morphing.width, 36);
    assert.strictEqual(morphing.height, 36);
    assert.strictEqual(morphing.health, 50);
    assert.strictEqual(morphing.shield, 15);
    assert.strictEqual(morphing.scoreValue, 300);
    assert.strictEqual(morphing.enemyType, 'MORPHING');
    assert.strictEqual(morphing.currentState, 'PATROL');

    // Player far away -> stays in PATROL
    morphing.update(0.1, 800, 800);
    assert.strictEqual(morphing.currentState, 'PATROL');

    // Player approaches (dist < 260) -> transitions to CHASE
    morphing.update(0.1, 320, 200);
    assert.strictEqual(morphing.currentState, 'CHASE');

    // Player enters close quarters (dist < 150) -> transitions to OVERCHARGE
    morphing.update(0.1, 310, 120);
    assert.strictEqual(morphing.currentState, 'OVERCHARGE');

    // Health drops low (< 35%) -> transitions to EVADE
    // Health 50, Shield 15. Damage 48 -> absorbs 15 shield, 33 to health -> health becomes 17 (34%)
    morphing.takeDamage(48);
    morphing.update(0.1, 310, 120);
    assert.strictEqual(morphing.currentState, 'EVADE');
  });

  test('Fail-Safe Sentry defensive programming, boundary clamping, and single-use emergency barrier (PSOOP-CO3)', () => {
    const sentry = new FailSafe(100, 100);
    assert.strictEqual(sentry.width, 40);
    assert.strictEqual(sentry.height, 42);
    assert.strictEqual(sentry.health, 70);
    assert.strictEqual(sentry.shield, 15);
    assert.strictEqual(sentry.scoreValue, 280);
    assert.strictEqual(sentry.enemyType, 'FAIL_SAFE');
    assert.strictEqual(sentry.emergencyBarrierActive, false);
    assert.strictEqual(sentry.emergencyBarrierTriggered, false);

    // Non-positive damage rejected defensively
    sentry.takeDamage(-50);
    assert.strictEqual(sentry.health, 70);

    // Damage brings health below 30% threshold (<= 21 HP)
    // 70 HP, 15 Shield. Hit for 60 -> 15 shield absorbed, 45 to hull -> health becomes 25
    sentry.takeDamage(60);
    assert.strictEqual(sentry.health, 25);
    assert.strictEqual(sentry.emergencyBarrierActive, false);

    // Hit for 10 -> health becomes 15 (<= 21), activates emergency barrier!
    sentry.takeDamage(10);
    assert.strictEqual(sentry.health, 15);
    assert.strictEqual(sentry.emergencyBarrierActive, true);
    assert.strictEqual(sentry.emergencyBarrierTriggered, true);
    assert.ok(sentry.shield >= 35); // Shield recharged defensively

    // Subsequent damage while barrier is active gets mitigated 50%
    const prevShield = sentry.shield;
    sentry.takeDamage(20);
    assert.strictEqual(sentry.shield, prevShield - 10);
    assert.strictEqual(sentry.health, 15); // Health untouched while shield up
  });

  test('Kinetic Battering Ram non-uniform acceleration and heavy prow impact damage (PSOOP-CO4)', () => {
    const rammer = new Rammer(400, 100);
    assert.strictEqual(rammer.width, 38);
    assert.strictEqual(rammer.height, 46);
    assert.strictEqual(rammer.health, 85);
    assert.strictEqual(rammer.shield, 0);
    assert.strictEqual(rammer.scoreValue, 320);
    assert.strictEqual(rammer.enemyType, 'RAMMER');
    assert.strictEqual(rammer.prowImpactDamage, 45);
    assert.strictEqual(rammer.charging, false);
    assert.strictEqual(rammer.vy, 70.0);

    // Player far to the right -> no charge
    rammer.update(0.1, 700, 400);
    assert.strictEqual(rammer.charging, false);

    // Player enters forward corridor (dx <= 110, y below rammer)
    rammer.update(0.1, 410, 400);
    assert.strictEqual(rammer.charging, true);

    // Accelerated motion: vy increases
    const prevVy = rammer.vy;
    rammer.update(0.2, 410, 400);
    assert.ok(rammer.vy > prevVy);
    assert.ok(rammer.vy <= rammer.maxRamSpeed);
  });
});
