package chaos;

import announcer.AnnouncerManager;
import game.GameEngine;
import model.ArmouredEnemy;
import model.Enemy;
import model.FighterEnemy;
import model.MorphingEnemy;
import model.Player;
import model.Projectile;
import model.DroneAIState;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Central event scheduler and simulation controller for AIRSTRIKE: CHAOS PROTOCOL.
 * Coordinates 3-second pre-event tactical warnings, randomized intervals,
 * multi-event compatibility, state restoration invariants, and HUD visualization.
 */
public class ChaosScheduler {

    private boolean enabled;
    private double firstEventDelay;
    private double minInterval;
    private double maxInterval;
    private double nextEventTimer;
    private final int maxActiveEvents;

    private final List<ActiveChaosEvent> activeEvents;
    private final LinkedList<ChaosEventType> history;
    private ChaosEventType lastTriggered;
    private final Random random;

    public ChaosScheduler() {
        this(45.0, 18.0, 35.0);
    }

    public ChaosScheduler(double firstDelay, double minInt, double maxInt) {
        this.enabled = true;
        this.firstEventDelay = Math.max(1.0, firstDelay);
        this.minInterval = Math.max(5.0, minInt);
        this.maxInterval = Math.max(minInterval, maxInt);
        this.nextEventTimer = this.firstEventDelay;
        this.maxActiveEvents = 4;
        this.activeEvents = new ArrayList<>();
        this.history = new LinkedList<>();
        this.lastTriggered = null;
        this.random = new Random();
    }

    /**
     * Resets scheduler state to pristine initial values, ensuring all entity invariants are restored.
     */
    public void reset(GameEngine engine) {
        if (engine != null && engine.getPlayer() != null) {
            restorePlayerState(engine.getPlayer());
        }
        activeEvents.clear();
        nextEventTimer = firstEventDelay;
        lastTriggered = null;
    }

    /**
     * Primary tick executed every frame delta from GameEngine.
     */
    public void update(double delta, GameEngine engine) {
        if (!enabled || engine == null) return;

        Player player = engine.getPlayer();
        if (player == null || !player.isActive()) return;

        int currentLevel = engine.getWaveManager() != null ? engine.getWaveManager().getCurrentLevel() : 1;

        // 1. Progress countdown to next event trigger
        if (nextEventTimer > 0.0) {
            nextEventTimer -= delta;
            if (nextEventTimer <= 0.0 && activeEvents.size() < maxActiveEvents) {
                ChaosEventType primary = pickEligibleEvent(currentLevel);
                if (primary != null) {
                    ChaosEventType secondary = pickCompatibleSecondary(primary, currentLevel);
                    if (secondary != null && (activeEvents.size() + 2 <= maxActiveEvents)) {
                        triggerDualChaos(primary, secondary, engine, 3.0);
                    } else {
                        triggerEvent(primary, engine);
                    }
                }
                // Schedule next interval
                nextEventTimer = minInterval + random.nextDouble() * (maxInterval - minInterval);
            }
        }

        // 2. Update all active events & apply ongoing continuous kinematic effects
        Iterator<ActiveChaosEvent> it = activeEvents.iterator();
        while (it.hasNext()) {
            ActiveChaosEvent event = it.next();
            boolean wasWarning = event.isWarning();

            // Progress warning state
            if (wasWarning && !event.isWarningAnnounced()) {
                event.setWarningAnnounced(true);
                if (event.getDualPartnerType() != null) {
                    ActiveChaosEvent partner = findActiveEvent(event.getDualPartnerType());
                    if (partner != null) partner.setWarningAnnounced(true);
                }
            }

            boolean justActivated = event.update(delta);

            // On transition from warning to active
            if (justActivated) {
                if (event.getDualPartnerType() != null) {
                    if (!event.isActivationAnnounced()) {
                        event.setActivationAnnounced(true);
                        ActiveChaosEvent partner = findActiveEvent(event.getDualPartnerType());
                        if (partner != null) partner.setActivationAnnounced(true);
                    }
                }
                applyInitialActivationEffect(event, engine);
            }

            // Apply per-frame continuous effects
            if (event.isActive()) {
                applyContinuousEffect(event, delta, engine);
            }

            // Clean expiration and state restoration
            if (event.isExpired()) {
                cleanupEventEffect(event, engine);
                it.remove();
            }
        }
    }

    /**
     * Triggered when the player clears a sector wave.
     * Schedules an advance warning identifying a DUAL CHAOS CONVERGENCE for the next sector,
     * announces it via the Announcer, and applies both anomalies simultaneously!
     */
    public ActiveChaosEvent onSectorCleared(int clearedSector, GameEngine engine) {
        if (!enabled || engine == null) return null;
        int nextSector = clearedSector + 1;
        ChaosEventType primary = pickEligibleEvent(nextSector);
        if (primary == null) return null;

        ChaosEventType secondary = pickCompatibleSecondary(primary, nextSector);
        double warningDuration = 4.0;
        ActiveChaosEvent event;
        if (secondary != null) {
            event = triggerDualChaos(primary, secondary, engine, warningDuration);
        } else {
            event = triggerEvent(primary, engine, warningDuration);
        }

        nextEventTimer = minInterval + random.nextDouble() * (maxInterval - minInterval);
        return event;
    }

    /**
     * Finds an active event of the given type among currently tracked active events.
     */
    private ActiveChaosEvent findActiveEvent(ChaosEventType type) {
        for (ActiveChaosEvent ev : activeEvents) {
            if (ev.getType() == type) return ev;
        }
        return null;
    }

    /**
     * Picks a compatible, non-conflicting secondary event for Dual Chaos Convergence.
     */
    public ChaosEventType pickCompatibleSecondary(ChaosEventType primary, int currentLevel) {
        List<ChaosEventType> candidates = new ArrayList<>();
        for (ChaosEventType type : ChaosEventType.values()) {
            if (type == primary) continue;
            if (type == lastTriggered) continue;
            if (currentLevel < type.getMinDifficulty()) continue;
            if (!primary.isCompatibleWith(type)) continue;

            boolean compatible = true;
            for (ActiveChaosEvent active : activeEvents) {
                if (!type.isCompatibleWith(active.getType())) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                candidates.add(type);
            }
        }

        if (candidates.isEmpty()) {
            for (ChaosEventType type : ChaosEventType.values()) {
                if (type == primary) continue;
                if (!primary.isCompatibleWith(type)) continue;
                boolean compatible = true;
                for (ActiveChaosEvent active : activeEvents) {
                    if (!type.isCompatibleWith(active.getType())) {
                        compatible = false;
                        break;
                    }
                }
                if (compatible) candidates.add(type);
            }
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * Stochastically selects a compatible, non-repeated event fitting current progression.
     */
    public ChaosEventType pickEligibleEvent(int currentLevel) {
        List<ChaosEventType> candidates = new ArrayList<>();
        for (ChaosEventType type : ChaosEventType.values()) {
            if (type == lastTriggered) continue; // Anti-repetition
            if (currentLevel < type.getMinDifficulty()) continue; // Difficulty threshold

            // Check compatibility against all currently active events
            boolean compatible = true;
            for (ActiveChaosEvent active : activeEvents) {
                if (!type.isCompatibleWith(active.getType())) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                candidates.add(type);
            }
        }

        if (candidates.isEmpty()) {
            // Fallback: If no candidate fits strict restrictions, try any compatible event
            for (ChaosEventType type : ChaosEventType.values()) {
                boolean compatible = true;
                for (ActiveChaosEvent active : activeEvents) {
                    if (!type.isCompatibleWith(active.getType())) {
                        compatible = false;
                        break;
                    }
                }
                if (compatible) candidates.add(type);
            }
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }

    public ActiveChaosEvent triggerDualChaos(ChaosEventType primary, ChaosEventType secondary, GameEngine engine) {
        return triggerDualChaos(primary, secondary, engine, 4.0);
    }

    public ActiveChaosEvent triggerDualChaos(ChaosEventType primary, ChaosEventType secondary, GameEngine engine, double warningDuration) {
        if (primary == null || secondary == null) return null;

        ActiveChaosEvent event1 = new ActiveChaosEvent(primary, primary.getDefaultDuration(), warningDuration);
        ActiveChaosEvent event2 = new ActiveChaosEvent(secondary, secondary.getDefaultDuration(), warningDuration);

        event1.setDualPartnerType(secondary);
        event2.setDualPartnerType(primary);

        activeEvents.add(event1);
        activeEvents.add(event2);

        lastTriggered = secondary;
        history.add(primary);
        history.add(secondary);
        while (history.size() > 20) history.removeFirst();

        return event1;
    }

    /**
     * Triggers a designated event with optional customizable warning duration.
     */
    public ActiveChaosEvent triggerEvent(ChaosEventType type, GameEngine engine) {
        return triggerEvent(type, engine, 3.0);
    }

    public ActiveChaosEvent triggerEvent(ChaosEventType type, double warningDuration) {
        return triggerEvent(type, null, warningDuration);
    }

    public ActiveChaosEvent triggerEvent(ChaosEventType type, GameEngine engine, double warningDuration) {
        if (type == null) return null;

        // If Total Chaos is triggered, also pair it with a random compatible secondary event
        if (type == ChaosEventType.TOTAL_CHAOS) {
            ActiveChaosEvent totalEvent = new ActiveChaosEvent(ChaosEventType.TOTAL_CHAOS, 12.0, warningDuration);
            activeEvents.add(totalEvent);
            lastTriggered = ChaosEventType.TOTAL_CHAOS;

            // Trigger secondary compatible sub-event
            ChaosEventType secondary = pickEligibleSubEventForTotalChaos();
            if (secondary != null) {
                ActiveChaosEvent subEvent = new ActiveChaosEvent(secondary, 12.0, warningDuration);
                activeEvents.add(subEvent);
            }
            return totalEvent;
        }

        ActiveChaosEvent event = new ActiveChaosEvent(type, type.getDefaultDuration(), warningDuration);
        activeEvents.add(event);
        lastTriggered = type;
        history.add(type);
        if (history.size() > 20) history.removeFirst();

        return event;
    }

    private ChaosEventType pickEligibleSubEventForTotalChaos() {
        ChaosEventType[] options = {
            ChaosEventType.REVERSE_CONTROLS,
            ChaosEventType.SPEED_DEMON,
            ChaosEventType.EMERGENCY_SHIELD,
            ChaosEventType.MAGNETIC_BULLETS,
            ChaosEventType.CRITICAL_OVERDRIVE,
            ChaosEventType.RAPID_FIRE
        };
        return options[random.nextInt(options.length)];
    }

    /**
     * Executes one-time discrete actions when the event transitions from warning to active.
     */
    private void applyInitialActivationEffect(ActiveChaosEvent event, GameEngine engine) {
        Player player = engine.getPlayer();
        List<Enemy> enemies = engine.getEnemies();
        List<Projectile> projectiles = engine.getProjectiles();

        switch (event.getType()) {
            case REVERSE_CONTROLS -> {
                if (player != null) player.setControlsInverted(true);
            }
            case SPEED_DEMON -> {
                if (player != null) {
                    event.setCachedPlayerSpeed(player.getSpeed());
                    player.setSpeed(player.getSpeed() * 1.7);
                }
            }
            case RAPID_FIRE -> {
                if (player != null) {
                    event.setCachedShootCooldown(player.getShootCooldownMs());
                    player.setShootCooldownMs(60); // 3x faster discharge
                }
            }
            case WEAPON_JAM -> {
                if (player != null) {
                    event.setCachedShootCooldown(player.getShootCooldownMs());
                    player.setShootCooldownMs((long) (player.getShootCooldownMs() * 3.5)); // Pipeline stall
                }
            }
            case CRITICAL_OVERDRIVE -> {
                if (player != null) {
                    event.setCachedDamageMultiplier(player.getDamageMultiplier());
                    player.setDamageMultiplier(2.0); // 2x Booth multiplier
                }
            }
            case EMERGENCY_SHIELD -> {
                if (player != null) {
                    player.setInvulnerable(true);
                }
            }
            case QUANTUM_COLLAPSE -> {
                // Zero Flag EMP: Purge 50% of active enemy projectiles
                int purged = 0;
                for (Projectile p : projectiles) {
                    if (p.isActive() && !p.isPlayerBullet()) {
                        if (purged % 2 == 0) p.setActive(false);
                        purged++;
                    }
                }
            }
            case DOUBLE_TROUBLE -> {
                // Clone up to 4 non-boss active enemies
                List<Enemy> clones = new ArrayList<>();
                int cloneCount = 0;
                for (Enemy e : enemies) {
                    if (e.isActive() && !(e instanceof model.BossEnemy) && cloneCount < 4) {
                        double nx = Math.max(40, Math.min(engine.getScreenWidth() - 60, e.getX() + 45));
                        clones.add(new FighterEnemy(nx, e.getY()));
                        cloneCount++;
                    }
                }
                enemies.addAll(clones);
            }
            case BOSS_RUSH -> {
                // Circular queue heavy echelon dispatch: spawn an Armoured Vanguard
                double spawnX = (engine.getScreenWidth() - 46) / 2.0;
                enemies.add(new ArmouredEnemy(spawnX, 40));
            }
            case STATE_CHAOS -> {
                // Force all morphing drones into random state transitions
                for (Enemy e : enemies) {
                    if (e instanceof MorphingEnemy morphing && e.isActive()) {
                        DroneAIState[] states = DroneAIState.values();
                        morphing.setCurrentState(states[random.nextInt(states.length)]);
                    }
                }
            }
            case GEOMETRIC_BARRAGE -> {
                // Spawn a 12-way regular dodecagonal projectile starburst from center top
                double cx = engine.getScreenWidth() / 2.0;
                double cy = 90.0;
                double speed = 220.0;
                for (int i = 0; i < 12; i++) {
                    double angle = (i * 2.0 * Math.PI / 12.0);
                    double vx = Math.cos(angle) * speed;
                    double vy = Math.sin(angle) * speed;
                    projectiles.add(new Projectile(cx, cy, 6, 6, vx, vy, false, 12));
                }
            }
            case BULLET_RAIN -> {
                // Spawn a grid of 8 downward Bresenham-spaced plasma bolts
                double step = engine.getScreenWidth() / 9.0;
                for (int i = 1; i <= 8; i++) {
                    projectiles.add(new Projectile(step * i, 20, 5, 14, 0, 260.0, false, 10));
                }
            }
            default -> {}
        }
    }

    /**
     * Applies continuous per-frame forces (e.g. gravity wells, magnetic bullet guidance).
     */
    private void applyContinuousEffect(ActiveChaosEvent event, double delta, GameEngine engine) {
        Player player = engine.getPlayer();
        List<Enemy> enemies = engine.getEnemies();
        List<Projectile> projectiles = engine.getProjectiles();

        switch (event.getType()) {
            case GRAVITY_WELL -> {
                // Micro-singularity located at center of screen
                double cx = engine.getScreenWidth() / 2.0;
                double cy = engine.getScreenHeight() / 2.0;
                double pullStrength = 130.0 * delta;

                if (player != null && player.isActive()) {
                    double dx = cx - player.getCenterX();
                    double dy = cy - player.getCenterY();
                    double dist = Math.max(30.0, Math.sqrt(dx * dx + dy * dy));
                    player.setX(player.getX() + (dx / dist) * pullStrength);
                    player.setY(player.getY() + (dy / dist) * pullStrength);
                }

                for (Enemy e : enemies) {
                    if (e.isActive() && !(e instanceof model.BossEnemy)) {
                        double dx = cx - (e.getX() + e.getWidth() / 2.0);
                        e.setX(e.getX() + Math.signum(dx) * Math.min(Math.abs(dx), pullStrength * 0.7));
                    }
                }
            }
            case SPEED_DEMON -> {
                for (Enemy e : enemies) {
                    if (e.isActive()) {
                        e.setY(e.getY() + (e.getSpeed() * 0.35 * delta));
                    }
                }
            }
            case MAGNETIC_BULLETS -> {
                // Steer player projectiles towards the nearest active enemy
                for (Projectile p : projectiles) {
                    if (p.isActive() && p.isPlayerBullet()) {
                        Enemy nearest = findNearestEnemy(p.getX(), p.getY(), enemies);
                        if (nearest != null) {
                            double targetX = nearest.getX() + nearest.getWidth() / 2.0;
                            double dx = targetX - p.getX();
                            p.setX(p.getX() + Math.signum(dx) * Math.min(Math.abs(dx), 110.0 * delta));
                        }
                    }
                }
            }
            case TIME_DISTORTION -> {
                // Slow down enemies by damping their downward speed
                for (Enemy e : enemies) {
                    if (e.isActive()) {
                        e.setY(e.getY() - (e.getSpeed() * 0.45 * delta));
                    }
                }
            }
            default -> {}
        }
    }

    private Enemy findNearestEnemy(double px, double py, List<Enemy> enemies) {
        Enemy nearest = null;
        double minD2 = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e.isActive() && e.getY() < py) {
                double dx = (e.getX() + e.getWidth() / 2.0) - px;
                double dy = (e.getY() + e.getHeight() / 2.0) - py;
                double d2 = dx * dx + dy * dy;
                if (d2 < minD2) {
                    minD2 = d2;
                    nearest = e;
                }
            }
        }
        return nearest;
    }

    /**
     * Cleanly restores entity variables upon event termination.
     */
    private void cleanupEventEffect(ActiveChaosEvent event, GameEngine engine) {
        Player player = engine.getPlayer();
        if (player == null) return;

        switch (event.getType()) {
            case REVERSE_CONTROLS -> player.setControlsInverted(false);
            case SPEED_DEMON -> {
                if (event.getCachedPlayerSpeed() > 0) {
                    player.setSpeed(event.getCachedPlayerSpeed());
                } else {
                    player.setSpeed(320.0);
                }
            }
            case RAPID_FIRE, WEAPON_JAM -> {
                if (event.getCachedShootCooldown() > 0) {
                    player.setShootCooldownMs(event.getCachedShootCooldown());
                } else {
                    player.setShootCooldownMs(180);
                }
            }
            case CRITICAL_OVERDRIVE -> {
                player.setDamageMultiplier(event.getCachedDamageMultiplier());
            }
            case EMERGENCY_SHIELD -> {
                player.setInvulnerable(false);
            }
            default -> {}
        }
    }

    private void restorePlayerState(Player player) {
        player.setControlsInverted(false);
        player.setSpeed(320.0);
        player.setShootCooldownMs(180);
        player.setDamageMultiplier(1.0);
        player.setInvulnerable(false);
    }

    /**
     * High-visibility glassmorphic HUD indicator rendering.
     */
    public void renderHUD(Graphics2D g, int screenWidth, int screenHeight) {
        if (!enabled || activeEvents.isEmpty()) return;

        int hudY = 16;
        for (ActiveChaosEvent event : activeEvents) {
            ChaosEventType type = event.getType();

            String badgeText;
            Color primaryCol;
            Color bgCol;

            if (event.isWarning()) {
                badgeText = String.format("WARNING: %s [%s] IN %04.1fs",
                        type.getDisplayName(), type.getCourseOutcome(), event.getWarningTimer());
                primaryCol = new Color(251, 191, 36); // Amber
                bgCol = new Color(30, 24, 12, 220);
            } else {
                badgeText = String.format("CHAOS: %s [%s] - %04.1fs",
                        type.getDisplayName(), type.getCourseOutcome(), event.getDurationRemaining());
                primaryCol = new Color(0, 229, 255); // Cyan
                bgCol = new Color(13, 25, 48, 225);
            }

            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            int txtW = fm.stringWidth(badgeText);
            int boxW = txtW + 36;
            int boxH = 24;
            int boxX = (screenWidth - boxW) / 2;

            // Glassmorphic pill container
            g.setColor(bgCol);
            g.fillRoundRect(boxX, hudY, boxW, boxH, 12, 12);
            g.setColor(primaryCol);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(boxX, hudY, boxW, boxH, 12, 12);

            // Progress bar line on bottom of pill
            if (event.isActive()) {
                int fillW = (int) (boxW * event.getProgress());
                g.setColor(new Color(primaryCol.getRed(), primaryCol.getGreen(), primaryCol.getBlue(), 160));
                g.drawLine(boxX + 6, hudY + boxH - 2, boxX + 6 + fillW, hudY + boxH - 2);
            }

            // Text string
            g.drawString(badgeText, boxX + 18, hudY + 16);

            hudY += boxH + 6;
        }
    }

    // Accessors
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public double getFirstEventDelay() { return firstEventDelay; }
    public void setFirstEventDelay(double delay) { this.firstEventDelay = delay; }
    public double getNextEventTimer() { return nextEventTimer; }
    public void setNextEventTimer(double timer) { this.nextEventTimer = timer; }
    public List<ActiveChaosEvent> getActiveEvents() { return activeEvents; }
    public ChaosEventType getLastTriggered() { return lastTriggered; }
}
