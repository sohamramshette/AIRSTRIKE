package database.multiplayer;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 6): Cross-Platform Deterministic PRNG
 * PACKAGE: database.multiplayer
 *
 * Mulberry32 Pseudo-Random Number Generator.
 * Guarantees 100% bitwise parity with TypeScript DeterministicPRNG.ts across all platforms.
 * Used for synchronized procedural enemy wave spawning, power-up drops, and chaos events.
 * ==============================================================================
 */
public class DeterministicPRNG {

    private int state;

    public DeterministicPRNG(long seed) {
        reset(seed);
    }

    /**
     * Resets internal state to the specified 32-bit seed.
     */
    public final void reset(long seed) {
        int s = (int) (seed & 0xFFFFFFFFL);
        this.state = (s == 0) ? 1 : s;
    }

    /**
     * Generates next signed 32-bit integer.
     */
    public int nextInt32() {
        state += 0x6D2B79F5;
        int z = state;
        z = (z ^ (z >>> 15)) * (z | 1);
        z ^= z + ((z ^ (z >>> 7)) * (z | 61));
        return z ^ (z >>> 14);
    }

    /**
     * Generates next unsigned 32-bit integer [0, 4294967295L].
     */
    public long nextUint32() {
        return Integer.toUnsignedLong(nextInt32());
    }

    /**
     * Generates next double in half-open interval [0.0, 1.0).
     */
    public double nextFloat() {
        return nextUint32() / 4294967296.0;
    }

    /**
     * Generates next double in range [min, max).
     */
    public double nextRange(double min, double max) {
        return min + nextFloat() * (max - min);
    }

    /**
     * Generates next integer in range [min, max).
     */
    public int nextInt(int min, int max) {
        if (min >= max) return min;
        return min + (int) Math.floor(nextFloat() * (max - min));
    }
}
