// ==============================================================================
// File      : PSOOP/src/database/multiplayer/DeterministicPRNG.java
// Subject   : PSOOP-CO4 | Cross-Platform Mulberry32 Deterministic PRNG
// Topics    : [TOPIC: Deterministic PRNG] [TOPIC: Bitwise Arithmetic] [TOPIC: Cross-Platform Parity]
// Game Role : Ensures identical wave spawns, drops, and chaos events across 1v1 clients.
// ==============================================================================
package database.multiplayer;

/**
 * [TOPIC: Deterministic PRNG]
 * Mulberry32 32-bit PRNG guaranteeing bitwise parity with TypeScript DeterministicPRNG.ts.
 */
public class DeterministicPRNG {

    private int state;

    public DeterministicPRNG(long seed) {
        reset(seed);
    }

    /**
     * [TOPIC: Deterministic PRNG] Resets internal state to the specified 32-bit seed.
     */
    public final void reset(long seed) {
        // WHY: Mask to 32 bits and substitute 0 -> 1 because Mulberry32 relies on non-zero Weyl stepping.
        int s = (int) (seed & 0xFFFFFFFFL);
        this.state = (s == 0) ? 1 : s;
    }

    /**
     * [TOPIC: Bitwise Arithmetic] Generates next signed 32-bit integer using Mulberry32 mixing.
     */
    public int nextInt32() {
        // WHY: Unsigned right shift (>>>) mirrors JS `>>> 0` bit behavior for exact cross-language parity.
        state += 0x6D2B79F5;
        int z = state;
        z = (z ^ (z >>> 15)) * (z | 1);
        z ^= z + ((z ^ (z >>> 7)) * (z | 61));
        return z ^ (z >>> 14);
    }

    /**
     * [TOPIC: Bitwise Arithmetic] Generates next unsigned 32-bit integer [0, 4294967295L].
     */
    public long nextUint32() {
        return Integer.toUnsignedLong(nextInt32());
    }

    /**
     * [TOPIC: Cross-Platform Parity] Generates next double in [0.0, 1.0) by dividing by 2^32.
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

