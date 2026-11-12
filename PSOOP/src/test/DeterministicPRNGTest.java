package test;

import database.multiplayer.DeterministicPRNG;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 Part 6: Java Deterministic PRNG Parity Test Suite
 * PACKAGE: test
 * ==============================================================================
 */
public class DeterministicPRNGTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    private static void pass(String name) {
        testsRun++;
        testsPassed++;
        System.out.println("  [PASS] " + name);
    }

    private static void fail(String name, String reason) {
        testsRun++;
        System.err.println("  [FAIL] " + name + ": " + reason);
    }

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   AIRSTRIKE PHASE 12: DETERMINISTIC PRNG PARITY TESTS  ");
        System.out.println("========================================================");

        // 1. Bitwise unsigned 32-bit integer parity check against TypeScript Mulberry32
        DeterministicPRNG prng = new DeterministicPRNG(1337);
        long[] expected = {
            792042790L,
            815997621L,
            3480950701L,
            2764880138L,
            1850162886L,
            1636579666L,
            2261569356L,
            2356160527L,
            2336483547L,
            2334845290L
        };

        boolean parityPassed = true;
        for (int i = 0; i < expected.length; i++) {
            long val = prng.nextUint32();
            if (val != expected[i]) {
                fail("ParityTest", "Index " + i + " expected " + expected[i] + " but got " + val);
                parityPassed = false;
                break;
            }
        }
        if (parityPassed) {
            pass("Mulberry32 PRNG achieves 100% bitwise parity with TypeScript on seed 1337");
        }

        // 2. Reset reproducibility
        prng.reset(42);
        double f1 = prng.nextFloat();
        double f2 = prng.nextFloat();
        double f3 = prng.nextFloat();
        prng.reset(42);
        if (prng.nextFloat() == f1 && prng.nextFloat() == f2 && prng.nextFloat() == f3) {
            pass("Reset state correctly reproduces sequence identically");
        } else {
            fail("ResetTest", "Reset failed to reproduce exact float sequence");
        }

        // 3. Range and int bounds
        prng.reset(99999);
        boolean boundsPassed = true;
        for (int i = 0; i < 100; i++) {
            double flt = prng.nextFloat();
            if (flt < 0.0 || flt >= 1.0) {
                boundsPassed = false;
                fail("BoundsTest", "Float out of bounds: " + flt);
                break;
            }
            double rng = prng.nextRange(50.0, 150.0);
            if (rng < 50.0 || rng >= 150.0) {
                boundsPassed = false;
                fail("BoundsTest", "Range out of bounds: " + rng);
                break;
            }
            int intVal = prng.nextInt(1, 7);
            if (intVal < 1 || intVal >= 7) {
                boundsPassed = false;
                fail("BoundsTest", "Int out of bounds: " + intVal);
                break;
            }
        }
        if (boundsPassed) {
            pass("Generates floating point and integer values within strictly clamped bounds");
        }

        System.out.println("========================================================");
        System.out.printf("TEST RESULTS: %d / %d PASSED (%.1f%%)\n",
                testsPassed, testsRun, (testsPassed * 100.0) / Math.max(1, testsRun));
        System.out.println("========================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }
}
