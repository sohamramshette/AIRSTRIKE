import { test, describe } from 'node:test';
import assert from 'node:assert';
import { ScoreCalculator } from '../src/game/ScoreCalculator';

describe('Score and Combo Multiplier Unit Tests', () => {
  test('Initial state has score 0 and multiplier 1x', () => {
    const calc = new ScoreCalculator();
    calc.reset();
    assert.strictEqual(calc.getScore(), 0);
    assert.strictEqual(calc.getStreak(), 0);
    assert.strictEqual(calc.getMultiplier(), 1);
  });

  test('Kill streak multipliers scale: 1-2 (1x), 3-7 (2x), 8-14 (3x), 15+ (4x)', () => {
    const calc = new ScoreCalculator();
    calc.reset();

    // Kill 1 & 2 -> 1x multiplier
    let res = calc.addKill(100);
    assert.strictEqual(res.multiplier, 1);
    assert.strictEqual(res.addedPoints, 100);

    res = calc.addKill(100);
    assert.strictEqual(res.multiplier, 1);
    assert.strictEqual(res.addedPoints, 100);

    // Kill 3 -> reaches 2x multiplier
    res = calc.addKill(100);
    assert.strictEqual(res.multiplier, 2);
    assert.strictEqual(res.addedPoints, 200);

    // Advance to 7 kills -> still 2x
    for (let i = 4; i <= 7; i++) {
      res = calc.addKill(100);
      assert.strictEqual(res.multiplier, 2);
    }

    // Kill 8 -> reaches 3x multiplier
    res = calc.addKill(100);
    assert.strictEqual(res.multiplier, 3);
    assert.strictEqual(res.addedPoints, 300);

    // Advance to 14 kills -> still 3x
    for (let i = 9; i <= 14; i++) {
      res = calc.addKill(100);
      assert.strictEqual(res.multiplier, 3);
    }

    // Kill 15 -> reaches 4x multiplier
    res = calc.addKill(100);
    assert.strictEqual(res.multiplier, 4);
    assert.strictEqual(res.addedPoints, 400);
  });

  test('Combo timer window expires after 3.5 seconds of inactivity', () => {
    const calc = new ScoreCalculator();
    calc.reset();

    calc.addKill(100);
    calc.addKill(100);
    calc.addKill(100);
    assert.strictEqual(calc.getMultiplier(), 2);
    assert.strictEqual(calc.getStreak(), 3);

    // Advance time by 2.0s -> timer active
    calc.update(2.0);
    assert.strictEqual(calc.getMultiplier(), 2);

    // Advance by another 1.6s -> total 3.6s > 3.5s window -> combo drops
    calc.update(1.6);
    assert.strictEqual(calc.getStreak(), 0);
    assert.strictEqual(calc.getMultiplier(), 1);
  });

  test('Player taking damage immediately breaks kill-streak combo', () => {
    const calc = new ScoreCalculator();
    calc.reset();

    for (let i = 0; i < 10; i++) {
      calc.addKill(100);
    }
    assert.strictEqual(calc.getMultiplier(), 3);

    // Player takes damage
    calc.onPlayerDamaged();
    assert.strictEqual(calc.getStreak(), 0);
    assert.strictEqual(calc.getMultiplier(), 1);
  });

  test('Bonus points add directly to active score without affecting combo streak', () => {
    const calc = new ScoreCalculator();
    calc.reset();

    calc.addKill(100);
    const scoreBefore = calc.getScore();
    const streakBefore = calc.getStreak();

    calc.addBonus(500);
    assert.strictEqual(calc.getScore(), scoreBefore + 500);
    assert.strictEqual(calc.getStreak(), streakBefore);
  });
});
