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
    assert.strictEqual(player.lives, 3);
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
    player.lives = 3;

    player.takeDamage(50);
    assert.strictEqual(player.lives, 2);
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
});
