// ==============================================================================
// AIRSTRIKE Phase 12 (Part 6): Cross-Platform Deterministic PRNG
// FILE: web/src/services/multiplayer/DeterministicPRNG.ts
//
// Mulberry32 Pseudo-Random Number Generator.
// Guarantees 100% bitwise parity with Java DeterministicPRNG.java across all platforms.
// Used for synchronized procedural enemy wave spawning, power-up drops, and chaos events.
// ==============================================================================

export class DeterministicPRNG {
  private state: number;

  constructor(seed: number) {
    this.state = 0;
    this.reset(seed);
  }

  /**
   * Resets internal state to the specified 32-bit seed.
   */
  public reset(seed: number): void {
    const s = (seed >>> 0);
    this.state = s === 0 ? 1 : s;
  }

  /**
   * Generates next unsigned 32-bit integer [0, 4294967295].
   */
  public nextUint32(): number {
    let z = (this.state += 0x6D2B79F5) >>> 0;
    z = Math.imul(z ^ (z >>> 15), z | 1);
    z ^= z + Math.imul(z ^ (z >>> 7), z | 61);
    return (z ^ (z >>> 14)) >>> 0;
  }

  /**
   * Generates next float in half-open interval [0.0, 1.0).
   */
  public nextFloat(): number {
    return this.nextUint32() / 4294967296.0;
  }

  /**
   * Generates next float in range [min, max).
   */
  public nextRange(min: number, max: number): number {
    return min + this.nextFloat() * (max - min);
  }

  /**
   * Generates next integer in range [min, max).
   */
  public nextInt(min: number, max: number): number {
    if (min >= max) return min;
    return min + Math.floor(this.nextFloat() * (max - min));
  }
}

