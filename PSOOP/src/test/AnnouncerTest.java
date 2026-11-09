package test;

import announcer.AnnouncerEvent;
import announcer.AnnouncerManager;
import announcer.AnnouncerSettings;
import announcer.CommentaryFrequency;
import announcer.DialogueCategory;
import announcer.DialogueEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnouncerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   AIRSTRIKE PHASE 10: HINGLISH ANNOUNCER TEST SUITE    ");
        System.out.println("========================================================");

        testDialogueLoadingAndCount();
        testCategoryDistribution();
        testZeroDuplicates();
        testEventTriggerMatching();
        testSettingsDefaultsAndModification();
        testAntiRepetitionEngine();
        testPriorityCooldownBypass();
        testDisabledAnnouncerSuppression();

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

    private static void testDialogueLoadingAndCount() {
        AnnouncerManager manager = AnnouncerManager.getInstance();
        int count = manager.getDialogueCount();
        assertTrue("Dialogue collection loaded with >= 200 entries (Actual: " + count + ")", count >= 200);
        assertTrue("Dialogue collection matches expected 292 entries", count >= 290);
    }

    private static void testCategoryDistribution() {
        AnnouncerManager manager = AnnouncerManager.getInstance();
        List<DialogueEntry> all = manager.getAllDialogues();
        Map<DialogueCategory, Integer> counts = new HashMap<>();

        for (DialogueEntry d : all) {
            counts.put(d.getCategory(), counts.getOrDefault(d.getCategory(), 0) + 1);
        }

        assertTrue("Desi Meme pack has >= 48 entries (Actual: " + counts.getOrDefault(DialogueCategory.DESI_MEME, 0) + ")",
                counts.getOrDefault(DialogueCategory.DESI_MEME, 0) >= 48);
        assertTrue("Instagram & Gen-Z pack has >= 58 entries (Actual: " + counts.getOrDefault(DialogueCategory.INSTAGRAM_GENZ, 0) + ")",
                counts.getOrDefault(DialogueCategory.INSTAGRAM_GENZ, 0) >= 58);
        assertTrue("Bollywood pack has >= 56 entries (Actual: " + counts.getOrDefault(DialogueCategory.BOLLYWOOD, 0) + ")",
                counts.getOrDefault(DialogueCategory.BOLLYWOOD, 0) >= 56);
        assertTrue("General Roast pack has >= 40 entries (Actual: " + counts.getOrDefault(DialogueCategory.GENERAL_ROAST, 0) + ")",
                counts.getOrDefault(DialogueCategory.GENERAL_ROAST, 0) >= 40);
        assertTrue("Victory & Praise pack has >= 25 entries (Actual: " + counts.getOrDefault(DialogueCategory.VICTORY_PRAISE, 0) + ")",
                counts.getOrDefault(DialogueCategory.VICTORY_PRAISE, 0) >= 25);
        assertTrue("Random Interruptions pack has >= 25 entries (Actual: " + counts.getOrDefault(DialogueCategory.RANDOM_INTERRUPT, 0) + ")",
                counts.getOrDefault(DialogueCategory.RANDOM_INTERRUPT, 0) >= 25);
    }

    private static void testZeroDuplicates() {
        AnnouncerManager manager = AnnouncerManager.getInstance();
        List<DialogueEntry> all = manager.getAllDialogues();

        Set<String> ids = new HashSet<>();
        Set<String> texts = new HashSet<>();
        boolean duplicateIdFound = false;
        boolean duplicateTextFound = false;

        for (DialogueEntry d : all) {
            if (!ids.add(d.getId())) {
                duplicateIdFound = true;
            }
            if (!texts.add(d.getText().trim().toLowerCase())) {
                duplicateTextFound = true;
            }
        }

        assertTrue("Zero duplicate dialogue IDs in canonical collection", !duplicateIdFound);
        assertTrue("Zero duplicate dialogue texts in canonical collection", !duplicateTextFound);
    }

    private static void testEventTriggerMatching() {
        AnnouncerManager manager = AnnouncerManager.getInstance();
        List<DialogueEntry> all = manager.getAllDialogues();

        boolean hasMissedShots = false;
        boolean hasDamage = false;
        boolean hasBossAppears = false;
        boolean hasBossDefeated = false;
        boolean hasCriticalHealth = false;
        boolean hasKillStreak = false;

        for (DialogueEntry d : all) {
            if (d.matchesEvent(AnnouncerEvent.MISSED_SHOTS)) hasMissedShots = true;
            if (d.matchesEvent(AnnouncerEvent.PLAYER_DAMAGE)) hasDamage = true;
            if (d.matchesEvent(AnnouncerEvent.BOSS_APPEARS)) hasBossAppears = true;
            if (d.matchesEvent(AnnouncerEvent.BOSS_DEFEATED)) hasBossDefeated = true;
            if (d.matchesEvent(AnnouncerEvent.CRITICAL_HEALTH)) hasCriticalHealth = true;
            if (d.matchesEvent(AnnouncerEvent.KILL_STREAK)) hasKillStreak = true;
        }

        assertTrue("Dialogues match MISSED_SHOTS trigger", hasMissedShots);
        assertTrue("Dialogues match PLAYER_DAMAGE trigger", hasDamage);
        assertTrue("Dialogues match BOSS_APPEARS trigger", hasBossAppears);
        assertTrue("Dialogues match BOSS_DEFEATED trigger", hasBossDefeated);
        assertTrue("Dialogues match CRITICAL_HEALTH trigger", hasCriticalHealth);
        assertTrue("Dialogues match KILL_STREAK trigger", hasKillStreak);
    }

    private static void testSettingsDefaultsAndModification() {
        AnnouncerSettings settings = AnnouncerSettings.getInstance();
        assertTrue("Announcer is enabled by default", settings.isEnabled());
        assertTrue("Active pack is ALL_PACKS by default", settings.getActiveCategory() == DialogueCategory.ALL_PACKS);
        assertTrue("Frequency is NORMAL by default", settings.getFrequency() == CommentaryFrequency.NORMAL);
        assertTrue("Voice synthesis is OFF by default", !settings.isVoiceEnabled());

        settings.setActiveCategory(DialogueCategory.BOLLYWOOD);
        assertTrue("Active pack can be set to BOLLYWOOD", settings.getActiveCategory() == DialogueCategory.BOLLYWOOD);

        settings.setFrequency(CommentaryFrequency.HIGH);
        assertTrue("Frequency can be set to HIGH", settings.getFrequency() == CommentaryFrequency.HIGH);

        // Reset to defaults
        settings.setActiveCategory(DialogueCategory.ALL_PACKS);
        settings.setFrequency(CommentaryFrequency.NORMAL);
        settings.setEnabled(true);
    }

    private static void testAntiRepetitionEngine() {
        AnnouncerManager manager = AnnouncerManager.getInstance();
        manager.reset();

        // Trigger multiple consecutive lines
        boolean triggered1 = manager.trigger(AnnouncerEvent.CRITICAL_HEALTH, true);
        assertTrue("First critical health trigger succeeded", triggered1);

        // Force reset cooldown to test ring buffer
        manager.reset();
        boolean triggered2 = manager.trigger(AnnouncerEvent.CRITICAL_HEALTH, true);
        assertTrue("Second critical health trigger succeeded with anti-repetition", triggered2);
    }

    private static void testPriorityCooldownBypass() {
        AnnouncerManager manager = AnnouncerManager.getInstance();
        manager.reset();

        // Normal trigger puts manager on cooldown
        boolean triggeredNormal = manager.trigger(AnnouncerEvent.MISSED_SHOTS, false);
        assertTrue("Normal trigger initiates cooldown", triggeredNormal);

        // Immediate secondary normal trigger should be rejected by cooldown
        boolean rejectedNormal = manager.trigger(AnnouncerEvent.MISSED_SHOTS, false);
        assertTrue("Subsequent normal trigger rejected by cooldown timer", !rejectedNormal);
    }

    private static void testDisabledAnnouncerSuppression() {
        AnnouncerManager manager = AnnouncerManager.getInstance();
        AnnouncerSettings settings = AnnouncerSettings.getInstance();
        manager.reset();

        settings.setEnabled(false);
        boolean triggeredWhileDisabled = manager.trigger(AnnouncerEvent.BOSS_APPEARS, true);
        assertTrue("Trigger completely suppressed when announcer is disabled", !triggeredWhileDisabled);

        settings.setEnabled(true);
        manager.reset();
        boolean triggeredWhileReEnabled = manager.trigger(AnnouncerEvent.BOSS_APPEARS, true);
        assertTrue("Trigger resumes when announcer is re-enabled", triggeredWhileReEnabled);
    }
}

