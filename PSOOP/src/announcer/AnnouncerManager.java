package announcer;

import database.SimpleJson;
import game.GameEngine;
import model.Enemy;
import model.Player;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AIRSTRIKE AI — THE DESI TOXIC ANNOUNCER
 * Manages Hinglish memes, Bollywood quotes, roasts, and in-game tactical commentary.
 */
public class AnnouncerManager {
    private static final AnnouncerManager INSTANCE = new AnnouncerManager();

    private final List<DialogueEntry> dialogues = new ArrayList<>();
    private final LinkedList<String> recentHistory = new LinkedList<>();
    private static final int MAX_RECENT_HISTORY = 20;

    private final Random random = new Random();

    // Banner display state
    private String activeText = "";
    private DialogueCategory activeCategory = DialogueCategory.GENERAL_ROAST;
    private double bannerTimer = 0.0;
    private double bannerDuration = 4.8;
    private float bannerAlpha = 0.0f;

    // Cooldown management
    private double cooldownTimer = 0.0;
    private double randomInterruptTimer = 16.0;

    // Telemetry tracking
    private int lastHealth = -1;
    private int lastShotsFired = 0;
    private int lastShotsHit = 0;
    private int consecutiveMisses = 0;
    private double campingTimer = 0.0;
    private boolean criticalHealthTriggered = false;
    private int lastReportedKillStreak = 0;
    private boolean bossSpawnTriggered = false;

    private AnnouncerManager() {
        loadDialogues();
    }

    public static AnnouncerManager getInstance() {
        return INSTANCE;
    }

    /**
     * Loads dialogues from JSON file or resource stream with embedded fallback.
     */
    public void loadDialogues() {
        dialogues.clear();
        String jsonContent = null;

        // 1. Try relative filesystem path
        String[] possiblePaths = {
            "config/announcer_dialogues.json",
            "../config/announcer_dialogues.json",
            "PSOOP/src/announcer/announcer_dialogues.json"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                try {
                    jsonContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }

        // 2. Try ClassLoader resource stream
        if (jsonContent == null) {
            try (InputStream is = getClass().getResourceAsStream("/announcer/announcer_dialogues.json")) {
                if (is != null) {
                    jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {}
        }

        // 3. Parse JSON if loaded
        if (jsonContent != null) {
            try {
                Map<String, Object> root = SimpleJson.parseObject(jsonContent);
                Object diaListObj = root.get("dialogues");
                if (diaListObj instanceof List<?> diaList) {
                    for (Object entryObj : diaList) {
                        if (entryObj instanceof Map<?, ?> map) {
                            String id = String.valueOf(map.get("id"));
                            String catStr = String.valueOf(map.get("category"));
                            String text = String.valueOf(map.get("text"));
                            String priorityStr = String.valueOf(map.get("priority"));
                            boolean highPriority = "HIGH".equalsIgnoreCase(priorityStr);

                            DialogueCategory cat = DialogueCategory.fromString(catStr);

                            List<AnnouncerEvent> triggers = new ArrayList<>();
                            Object trigListObj = map.get("triggers");
                            if (trigListObj instanceof List<?> trigList) {
                                for (Object tObj : trigList) {
                                    try {
                                        triggers.add(AnnouncerEvent.valueOf(String.valueOf(tObj).trim().toUpperCase()));
                                    } catch (IllegalArgumentException ignored) {}
                                }
                            }

                            dialogues.add(new DialogueEntry(id, cat, triggers, text, highPriority));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[Announcer] Warning: Failed to parse dialogue JSON: " + e.getMessage());
            }
        }

        // 4. Fallback if empty
        if (dialogues.isEmpty()) {
            populateFallbackDialogues();
        }
    }

    private void populateFallbackDialogues() {
        dialogues.add(new DialogueEntry("fb_01", DialogueCategory.DESI_MEME, List.of(AnnouncerEvent.CRITICAL_HEALTH), "GHEE KHATAM! Ab teri spaceship kaise chalegi?", true));
        dialogues.add(new DialogueEntry("fb_02", DialogueCategory.DESI_MEME, List.of(AnnouncerEvent.PLAYER_DAMAGE), "AREY BHAI BHAI BHAI! Ye kya kar diya?", false));
        dialogues.add(new DialogueEntry("fb_03", DialogueCategory.BOLLYWOOD, List.of(AnnouncerEvent.MISSED_SHOTS), "Kitne bullets the? Aur sab miss kar diye?", false));
        dialogues.add(new DialogueEntry("fb_04", DialogueCategory.BOLLYWOOD, List.of(AnnouncerEvent.PLAYER_DEATH), "Utha le re baba! Is player ko koi utha le!", true));
        dialogues.add(new DialogueEntry("fb_05", DialogueCategory.INSTAGRAM_GENZ, List.of(AnnouncerEvent.LOW_ACCURACY), "Bro is NOT beating the bad aim allegations.", false));
        dialogues.add(new DialogueEntry("fb_06", DialogueCategory.GENERAL_ROAST, List.of(AnnouncerEvent.CAMPING), "Are you camping or did your navigation system freeze?", false));
        dialogues.add(new DialogueEntry("fb_07", DialogueCategory.VICTORY_PRAISE, List.of(AnnouncerEvent.BOSS_DEFEATED), "Boss knocked out cold! Now THAT is main character aura!", true));
        dialogues.add(new DialogueEntry("fb_08", DialogueCategory.RANDOM_INTERRUPT, List.of(AnnouncerEvent.RANDOM_INTERRUPT), "AIRSTRIKE AI status: Vibe-checking this entire combat run.", false));
    }

    public List<DialogueEntry> getAllDialogues() {
        return Collections.unmodifiableList(dialogues);
    }

    public int getDialogueCount() {
        return dialogues.size();
    }

    /**
     * Attempts to trigger an announcer comment for a given gameplay event.
     */
    public boolean trigger(AnnouncerEvent event, boolean forcePriority) {
        AnnouncerSettings settings = AnnouncerSettings.getInstance();
        if (!settings.isEnabled() || event == null) {
            return false;
        }

        // Cooldown checks
        if (!forcePriority && cooldownTimer > 0.0) {
            return false;
        }
        if (forcePriority && cooldownTimer > (settings.getFrequency().getMinCooldown() - 1.5)) {
            // Even priority events have a brief 1.5s separation
            return false;
        }

        DialogueCategory selectedCategory = settings.getActiveCategory();

        // Filter eligible dialogues
        List<DialogueEntry> candidates = new ArrayList<>();
        for (DialogueEntry d : dialogues) {
            if (selectedCategory != DialogueCategory.ALL_PACKS && d.getCategory() != selectedCategory) {
                continue;
            }
            if (d.matchesEvent(event)) {
                candidates.add(d);
            }
        }

        if (candidates.isEmpty()) {
            // Fallback to any dialogue matching the event regardless of pack filter if strictly requested
            for (DialogueEntry d : dialogues) {
                if (d.matchesEvent(event)) {
                    candidates.add(d);
                }
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        // Anti-repetition: exclude lines recently played
        List<DialogueEntry> freshCandidates = new ArrayList<>();
        for (DialogueEntry c : candidates) {
            if (!recentHistory.contains(c.getId())) {
                freshCandidates.add(c);
            }
        }

        if (freshCandidates.isEmpty()) {
            // Clear oldest history to free up pool
            while (recentHistory.size() > (MAX_RECENT_HISTORY / 2)) {
                recentHistory.removeFirst();
            }
            freshCandidates = candidates;
        }

        DialogueEntry chosen = freshCandidates.get(random.nextInt(freshCandidates.size()));

        // Display dialogue banner
        showBanner(chosen.getText(), chosen.getCategory(), chosen.isHighPriority());

        // Update history
        recentHistory.addLast(chosen.getId());
        if (recentHistory.size() > MAX_RECENT_HISTORY) {
            recentHistory.removeFirst();
        }

        // Reset cooldowns
        cooldownTimer = settings.getFrequency().getMinCooldown();
        randomInterruptTimer = settings.getFrequency().getRandomInterval() + (random.nextDouble() * 6.0 - 3.0);

        return true;
    }

    public void triggerChaosLine(String line, DialogueCategory category) {
        // Fleet Announcer is strictly reserved for funny memes, Hinglish banter, and roasts.
        // Chaos Protocol telemetry is rendered exclusively via the top Cyber HUD badges.
    }

    public void showBanner(String text, DialogueCategory category, boolean highPriority) {
        this.activeText = text;
        this.activeCategory = category != null ? category : DialogueCategory.GENERAL_ROAST;

        // Extended display duration between 8.0 and 12.0 seconds based on letters
        // ensuring players have ample time to read the funny dialogues and memes
        int letters = text != null ? text.length() : 0;
        double dynamicDuration = Math.min(12.0, Math.max(8.0, 8.0 + (letters - 30) * 0.06));
        this.bannerDuration = highPriority ? Math.max(dynamicDuration, 9.0) : dynamicDuration;
        this.bannerTimer = this.bannerDuration;
        this.bannerAlpha = 0.0f;
    }

    /**
     * Evaluates live game state and triggers context-aware announcer reactions.
     */
    public void update(double delta, GameEngine engine) {
        // Cooldown timer progression
        if (cooldownTimer > 0.0) {
            cooldownTimer -= delta;
        }

        // Banner presentation animation
        if (bannerTimer > 0.0) {
            bannerTimer -= delta;
            double elapsed = bannerDuration - bannerTimer;
            if (elapsed < 0.35) {
                bannerAlpha = (float) (elapsed / 0.35);
            } else if (bannerTimer < 0.6) {
                bannerAlpha = Math.max(0.0f, (float) (bannerTimer / 0.6));
            } else {
                bannerAlpha = 1.0f;
            }
        } else {
            bannerAlpha = 0.0f;
        }

        if (engine == null || engine.getPlayer() == null) {
            return;
        }

        Player player = engine.getPlayer();

        // 1. Critical Health Detection (< 25% hull)
        float healthPct = (float) player.getHealth() / Math.max(1, player.getMaxHealth());
        if (healthPct <= 0.25f && !criticalHealthTriggered && player.isActive()) {
            criticalHealthTriggered = true;
            trigger(AnnouncerEvent.CRITICAL_HEALTH, true);
        } else if (healthPct > 0.35f) {
            criticalHealthTriggered = false;
        }

        // 2. Significant Damage Taken Detection (shield depleted or substantial hull loss)
        if (lastHealth >= 0 && ((lastHealth - player.getHealth() >= 20) || (player.getShield() <= 0 && lastHealth - player.getHealth() >= 15))) {
            if (healthPct > 0.25f) {
                trigger(AnnouncerEvent.PLAYER_DAMAGE, false);
            }
        }
        lastHealth = player.getHealth();

        // 3. Shooting Accuracy & Missed Shots Detection (only when player is repeatedly spraying)
        int shotsFired = engine.getShotsFired();
        int shotsHit = engine.getShotsHit();
        int newFired = shotsFired - lastShotsFired;
        int newHits = shotsHit - lastShotsHit;

        if (newFired > 0) {
            if (newHits == 0) {
                consecutiveMisses += newFired;
                if (consecutiveMisses >= 28 && shotsFired >= 35) {
                    trigger(AnnouncerEvent.MISSED_SHOTS, false);
                    consecutiveMisses = 0;
                }
            } else {
                consecutiveMisses = 0;
            }
        }
        lastShotsFired = shotsFired;
        lastShotsHit = shotsHit;

        // 4. Low Accuracy Roast (evaluated infrequently)
        if (shotsFired >= 50 && shotsFired % 50 == 0) {
            double acc = (double) shotsHit / shotsFired;
            if (acc < 0.20) {
                trigger(AnnouncerEvent.LOW_ACCURACY, false);
            } else if (acc > 0.80) {
                trigger(AnnouncerEvent.HIGH_ACCURACY, false);
            }
        }

        // 5. Corner Camping Detection (only when squatting for extended duration)
        int screenW = engine.getScreenWidth();
        if (player.getX() < 70 || player.getX() > (screenW - 120)) {
            campingTimer += delta;
            if (campingTimer >= 8.5) {
                trigger(AnnouncerEvent.CAMPING, false);
                campingTimer = 0.0;
            }
        } else {
            campingTimer = Math.max(0.0, campingTimer - delta * 1.5);
        }

        // 6. Kill Streak / Multiple Enemies Destroyed (rewards consecutive eliminations)
        int currentStreak = engine.getScoreCalculator() != null ? engine.getScoreCalculator().getKillStreak() : 0;
        if (currentStreak >= 5 && currentStreak != lastReportedKillStreak && currentStreak % 5 == 0) {
            lastReportedKillStreak = currentStreak;
            trigger(AnnouncerEvent.KILL_STREAK, false);
        } else if (currentStreak == 0) {
            lastReportedKillStreak = 0;
        }

        // 7. Surrounded By Enemies Check
        checkSurrounded(player, engine.getEnemies());

        // 8. Boss Presence Detection
        boolean isBossNow = engine.isBossActive();
        if (isBossNow && !bossSpawnTriggered) {
            bossSpawnTriggered = true;
            trigger(AnnouncerEvent.BOSS_APPEARS, true);
        } else if (!isBossNow && bossSpawnTriggered) {
            bossSpawnTriggered = false;
            trigger(AnnouncerEvent.BOSS_DEFEATED, true);
        }

        // 9. Random Interruption Countdown
        randomInterruptTimer -= delta;
        if (randomInterruptTimer <= 0.0) {
            trigger(AnnouncerEvent.RANDOM_INTERRUPT, false);
        }
    }

    private void checkSurrounded(Player player, List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty() || !player.isActive()) return;

        int leftCount = 0;
        int rightCount = 0;
        int topCount = 0;
        double px = player.getX() + player.getWidth() / 2.0;
        double py = player.getY() + player.getHeight() / 2.0;

        for (Enemy e : enemies) {
            if (!e.isActive()) continue;
            double ex = e.getX() + e.getWidth() / 2.0;
            double ey = e.getY() + e.getHeight() / 2.0;
            double distY = py - ey;
            if (distY > 0 && distY < 240) {
                if (ex < px - 40 && ex > px - 220) leftCount++;
                if (ex > px + 40 && ex < px + 220) rightCount++;
                if (Math.abs(ex - px) <= 60) topCount++;
            }
        }

        if (leftCount >= 1 && rightCount >= 1 && topCount >= 1) {
            trigger(AnnouncerEvent.SURROUNDED, false);
        }
    }

    /**
     * Renders the glassmorphic futuristic announcer banner in the top center HUD.
     */
    public void renderHUD(Graphics2D g, int screenWidth, int screenHeight) {
        if (bannerAlpha <= 0.01f || activeText == null || activeText.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int bannerW = Math.min(500, Math.max(340, screenWidth - 480));
        if (screenWidth < 980) {
            bannerW = Math.min(520, screenWidth - 40);
        }
        int bannerH = 56;
        int x = (screenWidth - bannerW) / 2;
        int y = screenWidth >= 980 ? 44 : 114;

        int bgAlpha = Math.min(255, (int) (225 * bannerAlpha));
        int borderAlpha = Math.min(255, (int) (200 * bannerAlpha));

        // Dark glass background
        g2.setColor(new Color(10, 16, 32, bgAlpha));
        g2.fillRoundRect(x, y, bannerW, bannerH, 12, 12);

        // Neon border (Electric Cyan / Purple accent)
        Color borderColor = (activeCategory == DialogueCategory.BOLLYWOOD || activeCategory == DialogueCategory.DESI_MEME)
            ? new Color(245, 158, 11, borderAlpha)  // Warm Gold for Desi/Bollywood
            : new Color(0, 240, 255, borderAlpha);   // Electric Cyan for Cyber/Meme

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y, bannerW, bannerH, 12, 12);

        // Top glowing accent line
        GradientPaint grad = new GradientPaint(
            x + 20, y, new Color(0, 240, 255, (int) (230 * bannerAlpha)),
            x + bannerW - 20, y, new Color(168, 85, 247, (int) (230 * bannerAlpha))
        );
        g2.setPaint(grad);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine(x + 18, y, x + bannerW - 18, y);

        // Announcer Tag Badge
        String categoryTag = activeCategory != null ? activeCategory.name().replace('_', ' ') : "AIRSTRIKE AI";
        String badge = "⚡ AIRSTRIKE AI • " + categoryTag;
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(0, 240, 255, (int) (240 * bannerAlpha)));
        g2.drawString(badge, x + 14, y + 17);

        // Glowing dot beside badge
        g2.setColor(new Color(52, 211, 153, (int) (255 * bannerAlpha)));
        g2.fillOval(x + bannerW - 20, y + 10, 6, 6);

        // Main Dialogue Text (Crisp white with subtle text wrap if needed)
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(new Color(255, 255, 255, (int) (255 * bannerAlpha)));

        FontMetrics fm = g2.getFontMetrics();
        int maxTextWidth = bannerW - 28;

        if (fm.stringWidth(activeText) <= maxTextWidth) {
            g2.drawString(activeText, x + 14, y + 39);
        } else {
            // Two-line split for long dialogues
            String[] words = activeText.split(" ");
            StringBuilder line1 = new StringBuilder();
            StringBuilder line2 = new StringBuilder();
            boolean secondLine = false;

            for (String w : words) {
                if (!secondLine) {
                    if (fm.stringWidth(line1 + w + " ") < maxTextWidth) {
                        line1.append(w).append(" ");
                    } else {
                        secondLine = true;
                        line2.append(w).append(" ");
                    }
                } else {
                    line2.append(w).append(" ");
                }
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(line1.toString().trim(), x + 14, y + 33);
            if (line2.length() > 0) {
                g2.setColor(new Color(226, 232, 240, (int) (240 * bannerAlpha)));
                g2.drawString(line2.toString().trim(), x + 14, y + 48);
            }
        }

        g2.dispose();
    }

    public void testAnnounce() {
        AnnouncerSettings settings = AnnouncerSettings.getInstance();
        DialogueCategory cat = settings.getActiveCategory();
        List<DialogueEntry> pool = new ArrayList<>();
        for (DialogueEntry d : dialogues) {
            if (cat == DialogueCategory.ALL_PACKS || d.getCategory() == cat) {
                pool.add(d);
            }
        }
        if (pool.isEmpty()) pool = dialogues;
        if (!pool.isEmpty()) {
            DialogueEntry d = pool.get(random.nextInt(pool.size()));
            showBanner(d.getText(), d.getCategory(), true);
        }
    }

    public void reset() {
        cooldownTimer = 0.0;
        randomInterruptTimer = 35.0;
        lastHealth = -1;
        lastShotsFired = 0;
        lastShotsHit = 0;
        consecutiveMisses = 0;
        campingTimer = 0.0;
        criticalHealthTriggered = false;
        lastReportedKillStreak = 0;
        bossSpawnTriggered = false;
        bannerTimer = 0.0;
        bannerAlpha = 0.0f;
    }
}
