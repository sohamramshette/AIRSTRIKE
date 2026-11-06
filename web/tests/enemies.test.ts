import { test, describe } from 'node:test';
import assert from 'node:assert';
import { Drone } from '../src/game/Drone';
import { Fighter } from '../src/game/Fighter';
import { Boss } from '../src/game/Boss';

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
});
