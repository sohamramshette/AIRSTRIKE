import { test, describe } from 'node:test';
import assert from 'node:assert';
import { WaveManager } from '../src/game/WaveManager';
import { Enemy } from '../src/game/Enemy';

describe('Wave Progression & Difficulty Scaling Tests', () => {
  test('Sector 1 initializes with 12 wave enemies (8 + 1*4)', () => {
    const wm = new WaveManager();
    wm.startLevel(1);
    assert.strictEqual(wm.currentLevel, 1);
    assert.strictEqual(wm.isBossWave, false);
    assert.strictEqual(wm.waveEnemiesRemaining, 12);
  });

  test('Sector 2 initializes with 16 wave enemies (8 + 2*4)', () => {
    const wm = new WaveManager();
    wm.startLevel(2);
    assert.strictEqual(wm.currentLevel, 2);
    assert.strictEqual(wm.isBossWave, false);
    assert.strictEqual(wm.waveEnemiesRemaining, 16);
  });

  test('Sector 3 triggers Flagship Boss Encounter', () => {
    const wm = new WaveManager();
    wm.startLevel(3);
    assert.strictEqual(wm.currentLevel, 3);
    assert.strictEqual(wm.isBossWave, true);
    assert.strictEqual(wm.waveEnemiesRemaining, 1);
  });

  test('Sector 6 and 9 also trigger Boss Encounters', () => {
    const wm = new WaveManager();
    wm.startLevel(6);
    assert.strictEqual(wm.isBossWave, true);

    wm.startLevel(9);
    assert.strictEqual(wm.isBossWave, true);

    wm.startLevel(4);
    assert.strictEqual(wm.isBossWave, false);
  });

  test('Enemy spawning and wave clear transitions', () => {
    const wm = new WaveManager();
    wm.startLevel(1);

    const spawned: Enemy[] = [];
    // Advance timer to trigger first spawn
    wm.update(0.6, 0, (e) => spawned.push(e), () => {});
    assert.ok(spawned.length > 0);
    assert.strictEqual(wm.waveEnemiesRemaining, 11);

    // Simulate all enemies defeated
    wm.waveEnemiesRemaining = 0;
    let cleared = false;
    wm.update(0.1, 0, () => {}, () => { cleared = true; });
    assert.ok(wm.waveClearTimer > 0);

    // Complete wave clear delay (2.5s)
    wm.update(2.5, 0, () => {}, () => { cleared = true; });
    assert.strictEqual(cleared, true);
    assert.strictEqual(wm.currentLevel, 2);
  });

  test('Controlled multi-tier fleet enemy spawning across levels', () => {
    const wm = new WaveManager();

    // Level 1: Drone & Fighter only
    wm.startLevel(1);
    const l1Spawned: Enemy[] = [];
    for (let i = 0; i < 20; i++) {
      wm.spawnTimer = 0;
      wm.waveEnemiesRemaining = 10;
      wm.update(0.1, 0, (e) => l1Spawned.push(e), () => {});
    }
    assert.ok(l1Spawned.every(e => e.enemyType === 'DRONE' || e.enemyType === 'FIGHTER'));

    // Level 2: Introduces Armoured and Interceptor
    wm.startLevel(2);
    const l2Types = new Set<string>();
    for (let i = 0; i < 60; i++) {
      wm.spawnTimer = 0;
      wm.waveEnemiesRemaining = 30;
      wm.update(0.1, 0, (e) => l2Types.add(e.enemyType), () => {});
    }
    assert.ok(l2Types.has('ARMOURED') || l2Types.has('INTERCEPTOR'));

    // Level 5: Full combined fleet
    wm.startLevel(5);
    const l5Types = new Set<string>();
    for (let i = 0; i < 100; i++) {
      wm.spawnTimer = 0;
      wm.waveEnemiesRemaining = 50;
      wm.update(0.1, 0, (e) => l5Types.add(e.enemyType), () => {});
    }
    assert.ok(l5Types.size >= 4);
  });
});
