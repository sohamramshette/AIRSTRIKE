import { test, describe } from 'node:test';
import assert from 'node:assert';
import { Player } from '../src/game/Player';

describe('Player Spacecraft Unit Tests', () => {
  test('Initial kinematics and vital parameters match specification', () => {
    const player = new Player();
    assert.strictEqual(player.x, 376.0);
    assert.strictEqual(player.y, 540.0);
    assert.strictEqual(player.width, 48);
    assert.strictEqual(player.height, 56);
    assert.strictEqual(player.health, 100);
    assert.strictEqual(player.shield, 100);
    assert.strictEqual(player.lives, 2); // Hardcore 2-vessel limit
    assert.strictEqual(player.speed, 320.0);
    assert.strictEqual(player.weaponLevel, 1);
    assert.strictEqual(player.active, true);
  });

  test('Movement vector normalization prevents diagonal hyperspeed', () => {
    const player = new Player();
    // Diagonal movement: +1, +1
    player.updateMovement(1, 1);
    const expectedSpeed = 320.0 * (1.0 / Math.SQRT2);
    assert.ok(Math.abs(player.vx - expectedSpeed) < 0.001);
    assert.ok(Math.abs(player.vy - expectedSpeed) < 0.001);

    // Cardinal movement: only +1 in X
    player.updateMovement(1, 0);
    assert.strictEqual(player.vx, 320.0);
    assert.strictEqual(player.vy, 0);
  });

  test('Playfield boundaries clamp spacecraft within 800x650 viewport', () => {
    const player = new Player();

    // Push past left and top edges
    player.x = -100;
    player.y = -50;
    player.update(0.016);
    assert.strictEqual(player.x, 0);
    assert.strictEqual(player.y, 0);

    // Push past right and bottom edges
    player.x = 900;
    player.y = 700;
    player.update(0.016);
    assert.strictEqual(player.x, 800 - 48);
    assert.strictEqual(player.y, 650 - 56);
  });

  test('Weapon Level 1 spawns single cannon projectile with 30 damage', () => {
    const player = new Player();
    player.weaponLevel = 1;
    player.shootTimer = 0;

    const bullets = player.fire();
    assert.strictEqual(bullets.length, 1);
    assert.strictEqual(bullets[0].damage, 30);
    assert.strictEqual(bullets[0].vx, 0);
    assert.strictEqual(bullets[0].vy, -560.0);
    assert.strictEqual(bullets[0].isPlayerBullet, true);
    assert.strictEqual(bullets[0].x, player.x + 21);
    assert.strictEqual(bullets[0].y, player.y - 4);
  });

  test('Weapon Level 2 spawns dual wingtip cannons with 35 damage each', () => {
    const player = new Player();
    player.weaponLevel = 2;
    player.shootTimer = 0;

    const bullets = player.fire();
    assert.strictEqual(bullets.length, 2);
    assert.strictEqual(bullets[0].damage, 35);
    assert.strictEqual(bullets[1].damage, 35);
    assert.strictEqual(bullets[0].x, player.x + 6);
    assert.strictEqual(bullets[1].x, player.x + 37);
  });

  test('Weapon Level 3 spawns triple spread cannons (center 45, angled 40)', () => {
    const player = new Player();
    player.weaponLevel = 3;
    player.shootTimer = 0;

    const bullets = player.fire();
    assert.strictEqual(bullets.length, 3);
    // Center cannon
    assert.strictEqual(bullets[0].damage, 45);
    assert.strictEqual(bullets[0].vx, 0);
    assert.strictEqual(bullets[0].vy, -560.0);

    // Left angled cannon
    assert.strictEqual(bullets[1].damage, 40);
    assert.strictEqual(bullets[1].vx, -110.0);
    assert.strictEqual(bullets[1].vy, -560.0);

    // Right angled cannon
    assert.strictEqual(bullets[2].damage, 40);
    assert.strictEqual(bullets[2].vx, 110.0);
    assert.strictEqual(bullets[2].vy, -560.0);
  });

  test('Shield absorption absorbs damage completely when shield >= damage', () => {
    const player = new Player();
    player.invulnerableTimer = 0;
    player.shield = 100;
    player.health = 100;

    player.takeDamage(40);
    assert.strictEqual(player.shield, 60);
    assert.strictEqual(player.health, 100);
  });

  test('Shield absorption spills over into hull when damage > shield', () => {
    const player = new Player();
    player.invulnerableTimer = 0;
    player.shield = 30;
    player.health = 100;

    player.takeDamage(50);
    assert.strictEqual(player.shield, 0);
    assert.strictEqual(player.health, 80);
  });

  test('Lethal damage decrements lives and triggers respawn', () => {
    const player = new Player();
    player.invulnerableTimer = 0;
    player.shield = 0;
    player.health = 30;
    player.lives = 2;

    player.takeDamage(50);
    assert.strictEqual(player.lives, 1);
    assert.strictEqual(player.health, 100);
    assert.strictEqual(player.shield, 100);
    assert.strictEqual(player.x, 376.0);
    assert.strictEqual(player.y, 540.0);
    assert.strictEqual(player.active, true);
    assert.ok(player.invulnerableTimer > 0);
  });

  test('Power-up enhancements apply correctly to player state', () => {
    const player = new Player();
    player.health = 50;
    player.heal(40);
    assert.strictEqual(player.health, 90);

    player.shield = 40;
    player.rechargeShield(50);
    assert.strictEqual(player.shield, 90);

    player.weaponLevel = 1;
    player.upgradeWeapon();
    assert.strictEqual(player.weaponLevel, 2);

    const initialSpeed = player.speed;
    player.boostSpeed(25);
    assert.strictEqual(player.speed, initialSpeed + 25);
  });

  test('Weapon Overheat system enforces thermal cooldown and recovers (COA-CO4 / PSOOP-CO3)', () => {
    const player = new Player();
    assert.strictEqual(player.weaponHeat, 0);
    assert.strictEqual(player.isOverheated, false);

    // Fire 8 consecutive volleys -> accumulates heat
    for (let i = 0; i < 8; i++) {
      player.shootTimer = 0;
      const bullets = player.fire();
      assert.strictEqual(bullets.length > 0, true);
    }
    assert.strictEqual(player.weaponHeat, 88);
    assert.strictEqual(player.isOverheated, false);

    // Fire 9th and 10th volleys -> hits 100 maxHeat and triggers overheat lock
    player.shootTimer = 0;
    player.fire();
    player.shootTimer = 0;
    player.fire();
    assert.strictEqual(player.weaponHeat, 100);
    assert.strictEqual(player.isOverheated, true);

    // Cannot fire while overheated
    player.shootTimer = 0;
    const lockedBullets = player.fire();
    assert.strictEqual(lockedBullets.length, 0);

    // Cool down for 2.0s: 100 - (25 * 2) = 50
    player.update(2.0);
    assert.strictEqual(player.weaponHeat, 50);
    assert.strictEqual(player.isOverheated, true);

    // Cool down remaining 2.1s -> completely cool (heat reaches 0), overheat unlocked!
    player.update(2.1);
    assert.strictEqual(player.weaponHeat, 0);
    assert.strictEqual(player.isOverheated, false);

    // Can fire again
    player.shootTimer = 0;
    const restoredBullets = player.fire();
    assert.strictEqual(restoredBullets.length > 0, true);
  });

  test('Critical damage degrades thrusters by 30% and healing restores nominal velocity (PSOOP-CO4)', () => {
    const player = new Player();
    player.updateMovement(1, 0);
    assert.strictEqual(player.vx, 320.0);

    // Health drops to 30% (<= 35% threshold)
    player.health = 30;
    player.updateMovement(1, 0);
    assert.strictEqual(player.vx, 320.0 * 0.70); // 224 px/s

    // Healing above 35% restores nominal thruster speed
    player.heal(20); // 30 + 20 = 50 > 35
    player.updateMovement(1, 0);
    assert.strictEqual(player.vx, 320.0);
  });
});
