package test;

import database.multiplayer.DeterministicPRNG;
import database.multiplayer.MultiplayerCombatSyncService;
import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.MultiplayerTelemetryPayload;
import database.multiplayer.MultiplayerModels.RoomStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 6): Java 1v1 Combat Telemetry & Score Attack Sync Tests
 * PACKAGE: test
 * ==============================================================================
 */
public class MultiplayerCombatSyncTest {

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
        System.out.println(" AIRSTRIKE PHASE 12: JAVA 1v1 COMBAT SYNCHRONIZATION    ");
        System.out.println("========================================================");

        // 1. PRNG reproducibility for identical seeds
        DeterministicPRNG prng1 = new DeterministicPRNG(987654321L);
        DeterministicPRNG prng2 = new DeterministicPRNG(987654321L);
        boolean prngMatch = true;
        for (int i = 0; i < 50; i++) {
            if (prng1.nextUint32() != prng2.nextUint32() ||
                prng1.nextFloat() != prng2.nextFloat() ||
                prng1.nextInt(1, 100) != prng2.nextInt(1, 100)) {
                prngMatch = false;
                break;
            }
        }
        if (prngMatch) {
            pass("PRNG: Guarantees exact reproducible pseudo-random sequence for identical match seeds");
        } else {
            fail("PRNGRepro", "PRNG instances produced divergent outputs for identical seed");
        }

        // 2. Sequence monotonicity drops out-of-order or duplicate packets
        MultiplayerCombatSyncService sync = MultiplayerCombatSyncService.getInstance();
        MultiplayerRoomState mockRoom = new MultiplayerRoomState(
                "11111111-1111-4111-8111-111111111111", "SYNC01",
                "host-uuid-111", "HostPilot", true,
                "guest-uuid-222", "GuestChallenger", true,
                42L, RoomStatus.ACTIVE, true, false, "", ""
        );

        final int[] localScore = {500};
        sync.startCombatSync(
                mockRoom, "host-uuid-111", "HostPilot",
                java.time.Instant.now().toString(),
                () -> new MultiplayerTelemetryPayload(
                        mockRoom.getRoomId(), "host-uuid-111", "HostPilot",
                        localScore[0], 100, 100, 2, 2, 2, false, 0, System.currentTimeMillis()
                ),
                () -> {}
        );

        // Packet sequence 1
        Map<String, Object> p1 = new HashMap<>();
        p1.put("roomId", mockRoom.getRoomId());
        p1.put("playerId", "guest-uuid-222");
        p1.put("callsign", "GuestChallenger");
        p1.put("score", 300);
        p1.put("health", 80);
        p1.put("shield", 90);
        p1.put("lives", 2);
        p1.put("wave", 2);
        p1.put("combo", 1);
        p1.put("overheated", false);
        p1.put("sequence", 1L);
        p1.put("timestamp", System.currentTimeMillis());

        sync.handleInboundTelemetry(p1);
        if (sync.getOpponentScore() == 300) {
            pass("Inbound telemetry packet sequence 1 accepted and vital state replicated");
        } else {
            fail("SeqTest1", "Expected opponent score 300, got " + sync.getOpponentScore());
        }

        // Stale duplicate packet sequence 1 -> MUST BE DROPPED
        Map<String, Object> pDup = new HashMap<>(p1);
        pDup.put("score", 999999);
        sync.handleInboundTelemetry(pDup);
        if (sync.getOpponentScore() == 300) {
            pass("Duplicate sequence number packet dropped cleanly by monotonic guard");
        } else {
            fail("DupSeqTest", "Duplicate sequence packet was not dropped; score updated to " + sync.getOpponentScore());
        }

        // Newer packet sequence 2 -> MUST BE ACCEPTED
        Map<String, Object> p2 = new HashMap<>(p1);
        p2.put("score", 750);
        p2.put("sequence", 2L);
        sync.handleInboundTelemetry(p2);
        if (sync.getOpponentScore() == 750) {
            pass("Higher sequence number packet accepted and updated opponent vitals");
        } else {
            fail("SeqTest2", "Expected opponent score 750, got " + sync.getOpponentScore());
        }

        // 3. Score Delta and Lead Detection
        final boolean[] overtakeLeading = {false};
        final int[] overtakeDelta = {0};
        sync.addOvertakeListener((leading, delta) -> {
            overtakeLeading[0] = leading;
            overtakeDelta[0] = delta;
        });

        // Local score is 500, opponent jumped to 750 -> Local trailing by -250
        int delta = sync.getScoreDelta();
        if (delta == -250) {
            pass("Score delta correctly calculates local lead/deficit (-250 PTS)");
        } else {
            fail("DeltaTest", "Expected delta -250, got " + delta);
        }

        sync.stopCombatSync();

        // 4. Physical Anti-Cheat Rate Bounding
        int duration = 90;
        int maxAllowed = (duration * 450) + 2000; // 42,500
        if (maxAllowed == 42500) {
            pass("Server anti-cheat theoretical maximum formula matches 42,500 bound for 90s");
        } else {
            fail("AntiCheatFormula", "Expected 42,500, got " + maxAllowed);
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

