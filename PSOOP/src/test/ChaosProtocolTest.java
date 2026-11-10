package test;

import chaos.ActiveChaosEvent;
import chaos.ChaosEventType;
import chaos.ChaosScheduler;
import game.GameEngine;
import model.Player;

import java.util.HashSet;
import java.util.Set;

public class ChaosProtocolTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   AIRSTRIKE PHASE 11: CHAOS PROTOCOL TEST SUITE        ");
        System.out.println("========================================================");

        testAll20EventsAnd19COs();
        testMutualExclusionRules();
        testWarningTransitionAndActiveCountdown();
        testStateRestoration();
        testSchedulerLifecycleAndAntiRepetition();
        testGameRestartReset();
        testDualChaosConvergence();

        System.out.println("========================================================");
        System.out.printf("TEST RESULTS: %d / %d PASSED (%.1f%%)%n", passed, (passed + failed), (passed * 100.0) / (passed + failed));
        System.out.println("========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + name);
            passed++;
        } else {
            System.err.println("  [FAIL] " + name);
            failed++;
        }
    }

    private static void testAll20EventsAnd19COs() {
        ChaosEventType[] types = ChaosEventType.values();
        assertTrue("Exactly 20 distinct Chaos Event types defined", types.length == 20);

        String[] expectedCOs = {
                "PSOOP-CO1", "PSOOP-CO2", "PSOOP-CO3", "PSOOP-CO4",
                "PL-CO1", "PL-CO2", "PL-CO3", "PL-CO4", "PL-CO5",
                "COA-CO1", "COA-CO2", "COA-CO3", "COA-CO4",
                "CGL-CO1", "CGL-CO2", "CGL-CO3", "CGL-CO4", "CGL-CO5", "CGL-CO6"
        };

        Set<String> coveredCOs = new HashSet<>();
        boolean allValidMetadata = true;

        for (ChaosEventType t : types) {
            coveredCOs.add(t.getCourseOutcome());
            if (t.getId() == null || !t.getId().startsWith("CHAOS_")) allValidMetadata = false;
            if (t.getDisplayName() == null || t.getDisplayName().isEmpty()) allValidMetadata = false;
            if (t.getCourseOutcome() == null || t.getCourseOutcome().isEmpty()) allValidMetadata = false;
            if (t.getDefaultDuration() < 8.0 || t.getDefaultDuration() > 15.0) allValidMetadata = false;
            if (t.getMinDifficulty() < 1 || t.getMinDifficulty() > 5) allValidMetadata = false;
            if (t.getCooldown() < 20.0) allValidMetadata = false;
            if (t.getWarningLine() == null || t.getWarningLine().isEmpty()) allValidMetadata = false;
            if (t.getActivateLine() == null || t.getActivateLine().isEmpty()) allValidMetadata = false;
            if (t.getCategory() == null) allValidMetadata = false;
        }

        assertTrue("All 20 events possess valid metadata and valid duration bounds (8-15s)", allValidMetadata);

        boolean allCOsCovered = true;
        for (String co : expectedCOs) {
            if (!coveredCOs.contains(co)) {
                System.err.println("    Missing CO mapping: " + co);
                allCOsCovered = false;
            }
        }
        assertTrue("All 19 Course Outcomes authentic mapping coverage satisfied", allCOsCovered);
    }

    private static void testMutualExclusionRules() {
        // Speed Demon vs Time Distortion
        assertTrue("SPEED_DEMON incompatible with TIME_DISTORTION",
                !ChaosEventType.SPEED_DEMON.isCompatibleWith(ChaosEventType.TIME_DISTORTION));

        // Rapid Fire vs Weapon Jam
        assertTrue("RAPID_FIRE incompatible with WEAPON_JAM",
                !ChaosEventType.RAPID_FIRE.isCompatibleWith(ChaosEventType.WEAPON_JAM));

        // Reverse Controls vs Gravity Well / Time Distortion
        assertTrue("REVERSE_CONTROLS incompatible with GRAVITY_WELL",
                !ChaosEventType.REVERSE_CONTROLS.isCompatibleWith(ChaosEventType.GRAVITY_WELL));
        assertTrue("REVERSE_CONTROLS incompatible with TIME_DISTORTION",
                !ChaosEventType.REVERSE_CONTROLS.isCompatibleWith(ChaosEventType.TIME_DISTORTION));

        // Total Chaos cannot stack
        assertTrue("TOTAL_CHAOS incompatible with another TOTAL_CHAOS",
                !ChaosEventType.TOTAL_CHAOS.isCompatibleWith(ChaosEventType.TOTAL_CHAOS));
        assertTrue("TOTAL_CHAOS incompatible with BULLET_RAIN",
                !ChaosEventType.TOTAL_CHAOS.isCompatibleWith(ChaosEventType.BULLET_RAIN));

        // Compatible pairs
        assertTrue("BULLET_RAIN compatible with MAGNETIC_BULLETS",
                ChaosEventType.BULLET_RAIN.isCompatibleWith(ChaosEventType.MAGNETIC_BULLETS));
    }

    private static void testWarningTransitionAndActiveCountdown() {
        ActiveChaosEvent event = new ActiveChaosEvent(ChaosEventType.BULLET_RAIN, 10.0, 3.0);
        assertTrue("Event begins in WARNING state", event.isWarning());
        assertTrue("Event not active during warning", !event.isActive());
        assertTrue("Warning timer matches 3.0s", Math.abs(event.getWarningTimer() - 3.0) < 0.001);

        // Tick 1.5s
        boolean trans1 = event.update(1.5);
        assertTrue("No active transition halfway through warning", !trans1 && event.isWarning());

        // Tick remaining 1.6s -> triggers active transition
        boolean trans2 = event.update(1.6);
        assertTrue("Transition to ACTIVE triggered exactly when warning expires", trans2);
        assertTrue("Event is now active", event.isActive());
        assertTrue("Event is no longer warning", !event.isWarning());

        // Tick 5.0s active duration
        event.update(5.0);
        assertTrue("Event remains active after 5s", event.isActive());
        assertTrue("Progress tracks correctly (~0.5)", Math.abs(event.getProgress() - 0.5) < 0.01);

        // Tick remaining duration to expiration
        event.update(6.0);
        assertTrue("Event expires after duration finishes", event.isExpired() && !event.isActive());
    }

    private static void testStateRestoration() {
        GameEngine engine = new GameEngine(800, 650);
        engine.startNewGame();
        ChaosScheduler scheduler = engine.getChaosScheduler();
        Player player = engine.getPlayer();
        double baseSpeed = player.getSpeed();
        long baseCooldown = player.getShootCooldownMs();
        double baseDamageMult = player.getDamageMultiplier();

        // 1. Test SPEED_DEMON
        scheduler.triggerEvent(ChaosEventType.SPEED_DEMON, engine, 0.1);
        scheduler.update(0.2, engine); // activates
        assertTrue("Player speed overclocked during SPEED_DEMON", player.getSpeed() > baseSpeed);
        scheduler.update(15.0, engine); // expires
        assertTrue("Player speed cleanly restored to baseline", Math.abs(player.getSpeed() - baseSpeed) < 0.001);

        // 2. Test RAPID_FIRE
        scheduler.triggerEvent(ChaosEventType.RAPID_FIRE, engine, 0.1);
        scheduler.update(0.2, engine);
        assertTrue("Weapon fire cooldown reduced during RAPID_FIRE", player.getShootCooldownMs() < baseCooldown);
        scheduler.update(15.0, engine);
        assertTrue("Weapon fire cooldown cleanly restored to baseline", player.getShootCooldownMs() == baseCooldown);

        // 3. Test CRITICAL_OVERDRIVE
        scheduler.triggerEvent(ChaosEventType.CRITICAL_OVERDRIVE, engine, 0.1);
        scheduler.update(0.2, engine);
        assertTrue("Damage multiplier doubled during CRITICAL_OVERDRIVE", player.getDamageMultiplier() > baseDamageMult);
        scheduler.update(15.0, engine);
        assertTrue("Damage multiplier cleanly restored to baseline", Math.abs(player.getDamageMultiplier() - baseDamageMult) < 0.001);
    }

    private static void testSchedulerLifecycleAndAntiRepetition() {
        GameEngine engine = new GameEngine(800, 650);
        engine.startNewGame();
        ChaosScheduler scheduler = engine.getChaosScheduler();
        Player player = engine.getPlayer();
        player.setControlsInverted(false);

        // Direct trigger
        ActiveChaosEvent ev = scheduler.triggerEvent(ChaosEventType.REVERSE_CONTROLS, engine, 3.0);
        assertTrue("Event triggered into activeEvents list", ev != null && scheduler.getActiveEvents().size() == 1);
        assertTrue("lastTriggered updated to REVERSE_CONTROLS", scheduler.getLastTriggered() == ChaosEventType.REVERSE_CONTROLS);

        // Anti-repetition check: pickEligibleEvent should NOT pick REVERSE_CONTROLS immediately
        ChaosEventType nextCandidate = scheduler.pickEligibleEvent(3);
        assertTrue("Anti-repetition ensures last triggered event is not immediately picked again",
                nextCandidate != ChaosEventType.REVERSE_CONTROLS);

        // Warning phase: controls NOT inverted yet
        scheduler.update(1.0, engine);
        assertTrue("Controls unaffected during 3.0s warning countdown", !player.isControlsInverted());

        // Activation phase: controls inverted
        scheduler.update(2.5, engine);
        assertTrue("Controls inverted upon activation transition", player.isControlsInverted());

        // Expiration: controls normalized
        scheduler.update(12.0, engine);
        assertTrue("Controls normalized upon event expiration", !player.isControlsInverted());
        assertTrue("Active events empty after expiration", scheduler.getActiveEvents().isEmpty());
    }

    private static void testGameRestartReset() {
        GameEngine engine = new GameEngine(800, 650);
        engine.startNewGame();
        ChaosScheduler scheduler = engine.getChaosScheduler();
        Player player = engine.getPlayer();
        player.setControlsInverted(true);
        player.setDamageMultiplier(2.0);

        scheduler.triggerEvent(ChaosEventType.REVERSE_CONTROLS, engine, 1.0);
        assertTrue("Event active prior to reset", scheduler.getActiveEvents().size() == 1);

        scheduler.reset(engine);
        assertTrue("Reset clears all active events", scheduler.getActiveEvents().isEmpty());
        assertTrue("Reset restores 45s first event delay", Math.abs(scheduler.getNextEventTimer() - 45.0) < 0.001);
        assertTrue("Reset restores controlsInverted to false", !player.isControlsInverted());
        assertTrue("Reset restores damageMultiplier to 1.0", Math.abs(player.getDamageMultiplier() - 1.0) < 0.001);
    }

    private static void testDualChaosConvergence() {
        GameEngine engine = new GameEngine(800, 650);
        engine.startNewGame();
        ChaosScheduler scheduler = engine.getChaosScheduler();
        Player player = engine.getPlayer();
        double baseSpeed = player.getSpeed();

        // 1. Test triggerDualChaos with compatible pair
        ActiveChaosEvent ev1 = scheduler.triggerDualChaos(
                ChaosEventType.REVERSE_CONTROLS,
                ChaosEventType.SPEED_DEMON,
                engine,
                1.0
        );
        assertTrue("triggerDualChaos instantiates primary event", ev1 != null);
        assertTrue("activeEvents contains both mixed events simultaneously", scheduler.getActiveEvents().size() == 2);
        assertTrue("Primary event has secondary as dual partner", ev1.getDualPartnerType() == ChaosEventType.SPEED_DEMON);
        assertTrue("Secondary event has primary as dual partner",
                scheduler.getActiveEvents().get(1).getDualPartnerType() == ChaosEventType.REVERSE_CONTROLS);

        // Warning state check
        assertTrue("Controls unaffected during warning", !player.isControlsInverted());
        assertTrue("Speed unaffected during warning", Math.abs(player.getSpeed() - baseSpeed) < 0.001);

        // Transition to active
        scheduler.update(1.2, engine);
        assertTrue("Controls inverted upon dual activation", player.isControlsInverted());
        assertTrue("Player speed overclocked upon dual activation", player.getSpeed() > baseSpeed);

        // Expiration and clean recovery without drift
        scheduler.update(20.0, engine);
        assertTrue("Both dual events expired cleanly", scheduler.getActiveEvents().isEmpty());
        assertTrue("Controls restored cleanly after dual chaos", !player.isControlsInverted());
        assertTrue("Speed cleanly restored without drift", Math.abs(player.getSpeed() - baseSpeed) < 0.001);

        // 2. Test onSectorCleared triggers advance dual warning
        ActiveChaosEvent sectorEv = scheduler.onSectorCleared(1, engine);
        assertTrue("onSectorCleared triggers dual event", sectorEv != null);
        assertTrue("onSectorCleared schedules exactly 2 simultaneous anomalies", scheduler.getActiveEvents().size() == 2);
        assertTrue("Sector clear anomaly starts with 4s advance warning", Math.abs(sectorEv.getWarningTimer() - 4.0) < 0.001);
    }
}
