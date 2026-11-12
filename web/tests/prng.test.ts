import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { DeterministicPRNG } from '../src/services/multiplayer/DeterministicPRNG';

describe('AIRSTRIKE Phase 12 Part 6: Deterministic PRNG Parity Tests', () => {
  it('generates reproducible unsigned 32-bit integer sequences from seed 1337', () => {
    const prng = new DeterministicPRNG(1337);
    const expected = [
      792042790,
      815997621,
      3480950701,
      2764880138,
      1850162886,
      1636579666,
      2261569356,
      2356160527,
      2336483547,
      2334845290
    ];
    const actual: number[] = [];
    for (let i = 0; i < 10; i++) {
      actual.push(prng.nextUint32());
    }
    assert.deepStrictEqual(actual, expected);
  });

  it('resets state correctly and reproduces sequence identically', () => {
    const prng = new DeterministicPRNG(42);
    const seq1 = [prng.nextFloat(), prng.nextFloat(), prng.nextFloat()];
    prng.reset(42);
    const seq2 = [prng.nextFloat(), prng.nextFloat(), prng.nextFloat()];
    assert.deepStrictEqual(seq1, seq2);
  });

  it('generates ranges and integers within specified bounds', () => {
    const prng = new DeterministicPRNG(99999);
    for (let i = 0; i < 100; i++) {
      const flt = prng.nextFloat();
      assert.ok(flt >= 0.0 && flt < 1.0, `Float out of range: ${flt}`);

      const rng = prng.nextRange(50, 150);
      assert.ok(rng >= 50 && rng < 150, `Range out of range: ${rng}`);

      const intVal = prng.nextInt(1, 7);
      assert.ok(intVal >= 1 && intVal < 7, `Int out of range: ${intVal}`);
      assert.strictEqual(Math.floor(intVal), intVal);
    }
  });
});
