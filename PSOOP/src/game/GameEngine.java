package game;

import exception.InvalidGameStateException;
import model.ArmouredEnemy;
import model.Enemy;
import model.FailSafeEnemy;
import model.InterceptorEnemy;
import model.MorphingEnemy;
import model.Particle;
import model.Player;
import model.PowerUp;
import model.Projectile;
import model.RammerEnemy;
import service.AssetManager;
import service.ScoreCalculator;
import service.WaveManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Central game coordinator orchestrating entities, physics, collision detection,
 * state transitions, and rendering pipelines.
 */
public class GameEngine {

    private GameState state;
    private final Player player;
    private final List<Enemy> enemies;
    private final List<Projectile> projectiles;
    private final List<PowerUp> powerUps;
    private final List<Particle> particles;

    private final CollisionManager collisionManager;
    private final ScoreCalculator scoreCalculator;
    private final WaveManager waveManager;
    private final AssetManager assetManager;

    private int screenWidth;
    private int screenHeight;
    private boolean shootingTriggered;

    // Match persistence & duration tracking (Phase 6.2B)
    private service.MatchService matchService;
    private long matchStartTimeMs;
    private boolean matchUploaded;
    private String matchUploadStatus;

    // Academic 5-Enemy Demonstration Mode (Phase 8.10)
    private boolean enemyDemoMode;
    private double demoRespawnTimer;

    // Combat Telemetry Statistics (Phase 9)
    private int enemiesDestroyed;
    private int shotsFired;
    private int shotsHit;
    private int mouseX;
    private int mouseY;

    public enum PauseButton { RESUME, RESTART, SETTINGS, MENU, EXIT, NONE }
    public enum GameOverButton { REDEPLOY, MENU, NONE }

    public GameEngine(int screenWidth, int screenHeight) {
        this(screenWidth, screenHeight, new service.MatchService());
    }

    public GameEngine(int screenWidth, int screenHeight, service.MatchService matchService) {
        this.screenWidth = Math.max(640, screenWidth);
        this.screenHeight = Math.max(480, screenHeight);
        this.state = GameState.MENU;
        this.matchService = matchService;
        this.matchStartTimeMs = 0;
        this.matchUploaded = false;
        this.matchUploadStatus = "";
        this.enemyDemoMode = false;
        this.demoRespawnTimer = 0.0;
        this.enemiesDestroyed = 0;
        this.shotsFired = 0;
        this.shotsHit = 0;
        this.mouseX = -1;
        this.mouseY = -1;

        this.player = new Player((this.screenWidth - 48) / 2.0, this.screenHeight - 110);
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.particles = new ArrayList<>();

        this.collisionManager = new CollisionManager();
        this.scoreCalculator = new ScoreCalculator();
        this.waveManager = new WaveManager();
        this.assetManager = new AssetManager(this.screenWidth, this.screenHeight, 140);
        this.shootingTriggered = false;
    }

    /**
     * Safely transitions between game states with validation against illegal paths.
     */
    public void setState(GameState newState) throws InvalidGameStateException {
        if (this.state == newState) return;

        // Validation: Cannot transition directly from MENU to PAUSED without starting game
        if (this.state == GameState.MENU && newState == GameState.PAUSED) {
            throw new InvalidGameStateException("Cannot pause while in Main Menu", "MENU -> PAUSED");
        }

        if (this.state != GameState.GAME_OVER && newState == GameState.GAME_OVER) {
            onGameOver();
        }

        this.state = newState;
    }

    private void onGameOver() {
        announcer.AnnouncerManager.getInstance().trigger(announcer.AnnouncerEvent.GAME_OVER, true);
        if (matchUploaded) return; // Prevent duplicate submission
        matchUploaded = true;

        int finalScore = scoreCalculator.getScore();
        int levelReached = waveManager.getCurrentLevel();
        long now = System.currentTimeMillis();
        int durationSeconds = (int) Math.max(1, (now - (matchStartTimeMs > 0 ? matchStartTimeMs : now)) / 1000);

        if (matchService != null && database.SessionManager.getInstance().isLoggedIn()) {
            matchUploadStatus = "TRANSMITTING FLIGHT LOG TO STARFLEET ARCHIVES...";
            matchService.saveMatch(finalScore, levelReached, durationSeconds).thenAccept(success -> {
                if (success) {
                    matchUploadStatus = "FLIGHT LOG SECURED IN STARFLEET ARCHIVES";
                } else {
                    matchUploadStatus = "STARFLEET UPLINK OFFLINE (LOG STORED LOCALLY)";
                }
            });
        } else {
            matchUploadStatus = "GUEST PILOT MISSION COMPLETE (LOCAL ONLY)";
        }
    }

    /**
     * Initiates a fresh game session.
     */
    public void startNewGame() {
        this.enemyDemoMode = false;
        enemies.clear();
        projectiles.clear();
        powerUps.clear();
        particles.clear();

        scoreCalculator.resetGame();
        waveManager.resetToLevel(1);
        player.resetFull((screenWidth - player.getWidth()) / 2, screenHeight - 110);

        this.matchStartTimeMs = System.currentTimeMillis();
        this.matchUploaded = false;
        this.matchUploadStatus = "";
        this.enemiesDestroyed = 0;
        this.shotsFired = 0;
        this.shotsHit = 0;

        announcer.AnnouncerManager.getInstance().reset();
        announcer.AnnouncerManager.getInstance().trigger(announcer.AnnouncerEvent.WAVE_START, false);

        try {
            setState(GameState.PLAYING);
        } catch (InvalidGameStateException ignored) {}
    }

    /**
     * Initiates the Academic 5-Enemy Demonstration arena (Phase 8.10).
     * Deploys all five PSOOP enemy types simultaneously for live interactive evaluation:
     * 1. Armoured Vanguard (PSOOP-CO1)
     * 2. Interceptor Fighter (PSOOP-CO1)
     * 3. Adaptive Morphing Drone (PSOOP-CO2)
     * 4. Fail-Safe Sentry (PSOOP-CO3)
     * 5. Kinetic Battering Ram (PSOOP-CO4)
     */
    public void startEnemyDemo() {
        enemies.clear();
        projectiles.clear();
        powerUps.clear();
        particles.clear();

        this.enemyDemoMode = true;
        this.demoRespawnTimer = 0.0;
        scoreCalculator.resetGame();
        player.resetFull((int)((screenWidth - player.getWidth()) / 2), screenHeight - 110);
        player.setHealth(player.getMaxHealth());
        player.rechargeShield(player.getMaxShield());

        this.matchStartTimeMs = System.currentTimeMillis();
        this.matchUploaded = false;
        this.matchUploadStatus = "";
        this.enemiesDestroyed = 0;
        this.shotsFired = 0;
        this.shotsHit = 0;

        populateDemoFleet();

        try {
            setState(GameState.PLAYING);
        } catch (InvalidGameStateException ignored) {}
    }

    public void populateDemoFleet() {
        enemies.clear();
        double spacing = screenWidth / 6.0;
        enemies.add(new ArmouredEnemy(spacing * 1 - 23, 75));     // CO1: Armoured Vanguard
        enemies.add(new InterceptorEnemy(spacing * 2 - 19, 120)); // CO1: Interceptor Fighter
        enemies.add(new MorphingEnemy(spacing * 3 - 18, 75));     // CO2: Adaptive Morphing Drone
        enemies.add(new FailSafeEnemy(spacing * 4 - 20, 120));    // CO3: Fail-Safe Sentry
        enemies.add(new RammerEnemy(spacing * 5 - 19, 75));       // CO4: Kinetic Battering Ram
    }

    public void spawnDemoEnemy(int type) {
        double spawnX = 60 + Math.random() * Math.max(10, screenWidth - 140);
        double spawnY = 50 + Math.random() * 80;
        switch (type) {
            case 1 -> enemies.add(new ArmouredEnemy(spawnX, spawnY));
            case 2 -> enemies.add(new InterceptorEnemy(spawnX, spawnY));
            case 3 -> enemies.add(new MorphingEnemy(spawnX, spawnY));
            case 4 -> enemies.add(new FailSafeEnemy(spawnX, spawnY));
            case 5 -> enemies.add(new RammerEnemy(spawnX, spawnY));
        }
    }

    public void clearDemoEnemies() {
        enemies.clear();
    }

    public void resetDemoFleet() {
        populateDemoFleet();
    }

    public boolean isEnemyDemoMode() {
        return enemyDemoMode;
    }

    /**
     * Pauses the active game session.
     */
    public void pauseGame() {
        if (state == GameState.PLAYING) {
            try {
                setState(GameState.PAUSED);
            } catch (InvalidGameStateException ignored) {}
        }
    }

    /**
     * Resumes the paused game session.
     */
    public void resumeGame() {
        if (state == GameState.PAUSED) {
            try {
                setState(GameState.PLAYING);
            } catch (InvalidGameStateException ignored) {}
        }
    }

    /**
     * Central update loop executed every frame delta.
     */
    public void update(double delta) {
        // Starfield background always scrolls smoothly in all states
        assetManager.update(delta, screenWidth, screenHeight);

        if (state != GameState.PLAYING) {
            return;
        }

        // Live announcer telemetry & in-game commentary
        announcer.AnnouncerManager.getInstance().update(delta, this);

        // 1. Update Player kinematics and boundaries
        player.update(delta);
        player.clampToBounds(0, screenWidth, 0, screenHeight);

        // Player weapon discharge
        if (shootingTriggered) {
            int pBefore = projectiles.size();
            player.shoot(projectiles);
            int fired = projectiles.size() - pBefore;
            if (fired > 0) shotsFired += fired;
        }

        // 2. Wave Manager updates (enemy spawning & difficulty)
        if (!enemyDemoMode) {
            waveManager.update(delta, enemies, screenWidth);
        } else {
            updateDemoFleet(delta);
        }

        // 3. Update active Enemies
        for (Enemy e : enemies) {
            if (e instanceof model.BossEnemy boss) {
                boss.setPatrolBounds(30, Math.max(300, screenWidth - boss.getWidth() - 30));
            }
            e.update(delta);
            e.performAttack(delta, projectiles, player);
            if (e.isOffScreen(screenHeight)) {
                if (enemyDemoMode) {
                    e.setY(-30);
                } else {
                    e.setActive(false);
                }
            }
        }

        // 4. Update Projectiles
        for (Projectile p : projectiles) {
            p.update(delta);
            if (p.isOffScreen(screenWidth, screenHeight)) {
                p.setActive(false);
            }
        }

        // 5. Update PowerUps
        for (PowerUp pu : powerUps) {
            pu.update(delta);
            if (pu.getY() > screenHeight + 40) {
                pu.setActive(false);
            }
        }

        // 6. Update Particles
        for (Particle pt : particles) {
            pt.update(delta);
        }

        // 7. Collision Detection & Resolution
        int activeEnemiesBefore = 0;
        for (Enemy e : enemies) if (e.isActive()) activeEnemiesBefore++;
        int pActiveBefore = 0;
        for (Projectile p : projectiles) if (p.isActive() && p.isPlayerBullet()) pActiveBefore++;

        collisionManager.checkPlayerBulletsVsEnemies(projectiles, enemies, particles, powerUps, scoreCalculator, waveManager);
        collisionManager.checkEnemyBulletsVsPlayer(projectiles, player, particles, scoreCalculator);
        collisionManager.checkPlayerVsEnemies(player, enemies, particles, scoreCalculator, waveManager, powerUps);
        collisionManager.checkPlayerVsPowerUps(player, powerUps, particles, scoreCalculator);

        int activeEnemiesAfter = 0;
        for (Enemy e : enemies) if (e.isActive()) activeEnemiesAfter++;
        int pActiveAfter = 0;
        for (Projectile p : projectiles) if (p.isActive() && p.isPlayerBullet()) pActiveAfter++;

        if (activeEnemiesBefore > activeEnemiesAfter) {
            enemiesDestroyed += (activeEnemiesBefore - activeEnemiesAfter);
        }
        if (pActiveBefore > pActiveAfter) {
            shotsHit += (pActiveBefore - pActiveAfter);
        }

        // 8. Player Destruction & Respawn Handling
        if (player.isDestroyed()) {
            if (enemyDemoMode) {
                player.setHealth(player.getMaxHealth());
                player.rechargeShield(player.getMaxShield());
                player.setActive(true);
            } else {
                boolean hasRemainingLives = player.loseLifeAndRespawn((screenWidth - player.getWidth()) / 2, screenHeight - 110);
                announcer.AnnouncerManager.getInstance().trigger(announcer.AnnouncerEvent.PLAYER_DEATH, true);
                if (!hasRemainingLives) {
                    try {
                        setState(GameState.GAME_OVER);
                    } catch (InvalidGameStateException ignored) {}
                }
            }
        }

        // 9. Clean up inactive dead entities from collections
        enemies.removeIf(e -> !e.isActive());
        projectiles.removeIf(p -> !p.isActive());
        powerUps.removeIf(pu -> !pu.isActive());
        particles.removeIf(pt -> !pt.isActive());
    }

    private void updateDemoFleet(double delta) {
        if (enemies.isEmpty()) {
            demoRespawnTimer += delta;
            if (demoRespawnTimer >= 1.5) {
                demoRespawnTimer = 0.0;
                populateDemoFleet();
            }
        }
    }

    /**
     * Renders all game entities, starfield, and HUD elements using Graphics2D.
     */
    public void render(Graphics2D g, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        // 1. Background starfield
        assetManager.renderBackground(g, width, height);

        // 2. Power-ups
        for (PowerUp pu : powerUps) pu.render(g);

        // 3. Enemies
        for (Enemy e : enemies) e.render(g);

        // Academic Demo Badges
        if (enemyDemoMode) {
            renderDemoBadges(g);
        }

        // 4. Projectiles
        for (Projectile p : projectiles) p.render(g);

        // 5. Player Ship
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            player.render(g);
        }

        // 6. Explosions and particle sparks
        for (Particle pt : particles) pt.render(g);

        // 7. HUD Overlay (Health, Shield, Score, Level, Lives)
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            renderHUD(g);
        }

        // Academic Demo Banner
        if (enemyDemoMode) {
            renderDemoBanner(g, width);
        }

        // 8. Wave Clear Notice Banner
        if (state == GameState.PLAYING && waveManager.isWaveClearNotice()) {
            renderBanner(g, "SECTOR SECURED", "HYPERDRIVE ENGAGED TO NEXT SECTOR", new Color(0, 220, 255));
        }

        // 9. Pause Overlay
        if (state == GameState.PAUSED) {
            renderPauseScreen(g);
        }

        // 10. Game Over Overlay
        if (state == GameState.GAME_OVER) {
            renderGameOverScreen(g);
        }
    }

    private void renderHUD(Graphics2D g) {
        // --- 1. Top-Left Vitals Pod ---
        int leftW = 280;
        int leftH = 76;
        int leftX = 20;
        int leftY = 20;

        g.setColor(new Color(13, 19, 33, 215));
        g.fillRoundRect(leftX, leftY, leftW, leftH, 12, 12);
        g.setColor(new Color(0, 229, 255, 90));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(leftX, leftY, leftW, leftH, 12, 12);
        g.setColor(new Color(0, 229, 255));
        g.drawLine(leftX + 20, leftY, leftX + leftW - 20, leftY);

        int barW = 150;
        int barH = 10;
        int barX = leftX + 115;

        // Health Bar
        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString("HULL INTEGRITY", leftX + 14, leftY + 22);

        g.setColor(new Color(22, 31, 56));
        g.fillRoundRect(barX, leftY + 12, barW, barH, 4, 4);
        float hRatio = Math.max(0f, Math.min(1f, (float) player.getHealth() / player.getMaxHealth()));
        Color hCol = hRatio > 0.35f ? new Color(52, 211, 153) : new Color(239, 68, 68);
        g.setColor(hCol);
        if (hRatio > 0) g.fillRoundRect(barX, leftY + 12, (int) (barW * hRatio), barH, 4, 4);
        g.setColor(new Color(hCol.getRed(), hCol.getGreen(), hCol.getBlue(), 120));
        g.drawRoundRect(barX, leftY + 12, barW, barH, 4, 4);

        // Shield Bar
        g.setColor(new Color(148, 163, 184));
        g.drawString("SHIELD CAPACITOR", leftX + 14, leftY + 42);

        g.setColor(new Color(22, 31, 56));
        g.fillRoundRect(barX, leftY + 32, barW, barH, 4, 4);
        float sRatio = Math.max(0f, Math.min(1f, (float) player.getShield() / player.getMaxShield()));
        g.setColor(new Color(0, 229, 255));
        if (sRatio > 0) g.fillRoundRect(barX, leftY + 32, (int) (barW * sRatio), barH, 4, 4);
        g.setColor(new Color(0, 229, 255, 120));
        g.drawRoundRect(barX, leftY + 32, barW, barH, 4, 4);

        // Vessels & Weapon Tier
        g.setColor(new Color(251, 191, 36));
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("VESSELS: " + player.getLives(), leftX + 14, leftY + 64);

        g.setColor(new Color(56, 189, 248));
        String wpnTier = switch (player.getWeaponLevel()) {
            case 2 -> "DUAL CANNONS";
            case 3 -> "TRIPLE SPREAD";
            default -> "SINGLE CANNON";
        };
        g.drawString("ORDNANCE: " + wpnTier, leftX + 115, leftY + 64);

        // --- 2. Top-Right Mission Pod ---
        int rightW = 230;
        int rightH = 92;
        int rightX = screenWidth - rightW - 20;
        int rightY = 20;

        g.setColor(new Color(13, 19, 33, 215));
        g.fillRoundRect(rightX, rightY, rightW, rightH, 12, 12);
        g.setColor(new Color(0, 229, 255, 90));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(rightX, rightY, rightW, rightH, 12, 12);
        g.setColor(new Color(0, 229, 255));
        g.drawLine(rightX + 20, rightY, rightX + rightW - 20, rightY);

        // Pilot Name
        g.setColor(new Color(52, 211, 153));
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        String pilotName = database.SessionManager.getInstance().getCurrentUsername().toUpperCase();
        if (pilotName.length() > 18) pilotName = pilotName.substring(0, 18) + "...";
        g.drawString("● PILOT: " + pilotName, rightX + 14, rightY + 20);

        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("SCORE: " + String.format("%,d", scoreCalculator.getScore()), rightX + 14, rightY + 42);

        // High Score
        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("RECORD: " + String.format("%,d", scoreCalculator.getHighScore()), rightX + 14, rightY + 60);

        // Sector / Wave & Streak Multiplier
        g.setColor(new Color(251, 191, 36));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("SECTOR: " + waveManager.getCurrentLevel(), rightX + 14, rightY + 80);

        if (scoreCalculator.getMultiplier() > 1) {
            g.setColor(new Color(239, 68, 68));
            g.drawString(scoreCalculator.getMultiplier() + "x STREAK!", rightX + 120, rightY + 80);
        }

        // --- 3. Center Pause Hint Pill ---
        int pillW = 86;
        int pillH = 22;
        int pillX = (screenWidth - pillW) / 2;
        int pillY = 16;
        g.setColor(new Color(13, 19, 33, 190));
        g.fillRoundRect(pillX, pillY, pillW, pillH, 10, 10);
        g.setColor(new Color(30, 41, 59, 200));
        g.drawRoundRect(pillX, pillY, pillW, pillH, 10, 10);
        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString("[P] PAUSE", pillX + 14, pillY + 15);

        // --- 4. Center Tactical Desi Announcer Banner ---
        announcer.AnnouncerManager.getInstance().renderHUD(g, screenWidth, screenHeight);
    }

    private void renderBanner(Graphics2D g, String mainText, String subText, Color color) {
        int bannerY = screenHeight / 3;
        g.setColor(new Color(10, 15, 25, 220));
        g.fillRect(0, bannerY - 35, screenWidth, 80);
        g.setColor(color);
        g.setStroke(new BasicStroke(2.0f));
        g.drawLine(0, bannerY - 35, screenWidth, bannerY - 35);
        g.drawLine(0, bannerY + 45, screenWidth, bannerY + 45);

        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics fm1 = g.getFontMetrics();
        g.drawString(mainText, (screenWidth - fm1.stringWidth(mainText)) / 2, bannerY);

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(subText, (screenWidth - fm2.stringWidth(subText)) / 2, bannerY + 24);
    }

    private void renderPauseScreen(Graphics2D g) {
        g.setColor(new Color(7, 10, 20, 215));
        g.fillRect(0, 0, screenWidth, screenHeight);

        int cardW = 440;
        int cardH = 350;
        int cardX = (screenWidth - cardW) / 2;
        int cardY = (screenHeight - cardH) / 2;

        // Card Container
        g.setColor(new Color(13, 19, 33, 240));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 16, 16);
        g.setColor(new Color(0, 229, 255, 100));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 16, 16);

        // Top hairline accent
        g.setColor(new Color(0, 229, 255));
        g.drawLine(cardX + 40, cardY + 1, cardX + cardW - 40, cardY + 1);

        // Title: TACTICAL PAUSE
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "TACTICAL PAUSE";
        int tx = (screenWidth - fmTitle.stringWidth(title)) / 2;
        g.setColor(new Color(0, 229, 255, 70));
        g.drawString(title, tx - 1, cardY + 45);
        g.drawString(title, tx + 1, cardY + 45);
        g.setColor(Color.WHITE);
        g.drawString(title, tx, cardY + 45);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fmSub = g.getFontMetrics();
        String sub = "COMBAT SUSPENDED  •  STARFLEET STANDBY";
        g.setColor(new Color(148, 163, 184));
        g.drawString(sub, (screenWidth - fmSub.stringWidth(sub)) / 2, cardY + 68);

        // Interactive Options
        int btnW = 360;
        int btnH = 38;
        int bx = cardX + 40;

        renderTacticalButton(g, bx, cardY + 86, btnW, btnH, "▶   RESUME MISSION", "[P / ESC]", new Color(0, 229, 255), new Color(0, 119, 182), isButtonHovered(bx, cardY + 86, btnW, btnH));
        renderTacticalButton(g, bx, cardY + 132, btnW, btnH, "↺   RESTART SECTOR", "[R]", new Color(251, 191, 36), new Color(40, 32, 16), isButtonHovered(bx, cardY + 132, btnW, btnH));
        renderTacticalButton(g, bx, cardY + 178, btnW, btnH, "⚙   MISSION SETTINGS", "", new Color(56, 189, 248), new Color(16, 32, 54), isButtonHovered(bx, cardY + 178, btnW, btnH));
        renderTacticalButton(g, bx, cardY + 224, btnW, btnH, "◈   RETURN TO MAIN MENU", "[M]", new Color(168, 85, 247), new Color(36, 18, 48), isButtonHovered(bx, cardY + 224, btnW, btnH));
        renderTacticalButton(g, bx, cardY + 270, btnW, btnH, "✕   ABORT TO DESKTOP", "", new Color(239, 68, 68), new Color(48, 18, 22), isButtonHovered(bx, cardY + 270, btnW, btnH));
    }

    private void renderGameOverScreen(Graphics2D g) {
        g.setColor(new Color(13, 5, 10, 225));
        g.fillRect(0, 0, screenWidth, screenHeight);

        int cardW = 480;
        int cardH = 370;
        int cardX = (screenWidth - cardW) / 2;
        int cardY = (screenHeight - cardH) / 2;

        g.setColor(new Color(13, 19, 33, 240));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 16, 16);
        g.setColor(new Color(239, 68, 68));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 16, 16);

        // Top red hairline
        g.setColor(new Color(239, 68, 68));
        g.drawLine(cardX + 40, cardY + 1, cardX + cardW - 40, cardY + 1);

        // Title: VESSEL DESTROYED
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "VESSEL DESTROYED";
        int tx = (screenWidth - fmTitle.stringWidth(title)) / 2;
        g.setColor(new Color(239, 68, 68, 90));
        g.drawString(title, tx - 1, cardY + 38);
        g.drawString(title, tx + 1, cardY + 38);
        g.setColor(Color.WHITE);
        g.drawString(title, tx, cardY + 38);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fmSub = g.getFontMetrics();
        String sub = "TACTICAL ENVELOPE BREACHED  •  MISSION TERMINATED";
        g.setColor(new Color(248, 113, 113));
        g.drawString(sub, (screenWidth - fmSub.stringWidth(sub)) / 2, cardY + 58);

        // Statistics Panel
        int statW = 400;
        int statH = 150;
        int statX = cardX + 40;
        int statY = cardY + 74;

        g.setColor(new Color(22, 31, 56));
        g.fillRoundRect(statX, statY, statW, statH, 10, 10);
        g.setColor(new Color(50, 65, 95));
        g.drawRoundRect(statX, statY, statW, statH, 10, 10);

        long now = System.currentTimeMillis();
        int durationSeconds = (int) Math.max(1, (now - (matchStartTimeMs > 0 ? matchStartTimeMs : now)) / 1000);
        int accuracy = getAccuracy();

        int rowY = statY + 24;
        renderStatRow(g, statX + 16, statW - 32, rowY, "FINAL SCORE:", String.format("%,d", scoreCalculator.getScore()), new Color(0, 229, 255), true);
        rowY += 21;
        renderStatRow(g, statX + 16, statW - 32, rowY, "ALL-TIME RECORD:", String.format("%,d", scoreCalculator.getHighScore()), new Color(251, 191, 36), false);
        rowY += 21;
        renderStatRow(g, statX + 16, statW - 32, rowY, "SECTOR REACHED:", "Sector " + waveManager.getCurrentLevel(), Color.WHITE, false);
        rowY += 21;
        renderStatRow(g, statX + 16, statW - 32, rowY, "HOSTILES DESTROYED:", String.valueOf(enemiesDestroyed), Color.WHITE, false);
        rowY += 21;
        renderStatRow(g, statX + 16, statW - 32, rowY, "FLIGHT TIME / ACCURACY:", durationSeconds + "s  /  " + accuracy + "%", new Color(52, 211, 153), false);
        rowY += 19;

        // Cloud sync status bar
        g.setColor(new Color(30, 42, 68));
        g.drawLine(statX + 16, rowY - 4, statX + statW - 16, rowY - 4);
        String syncText = (matchUploadStatus != null && !matchUploadStatus.isEmpty()) ? matchUploadStatus : "CLOUD SYNC: READY";
        Color syncCol = syncText.contains("SECURED") ? new Color(52, 211, 153) : new Color(56, 189, 248);
        g.setColor(syncCol);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString("● " + syncText, statX + 16, rowY + 10);

        // Buttons below statistics panel
        int btnW = 400;
        int bx = cardX + 40;
        renderTacticalButton(g, bx, cardY + 244, btnW, 40, "▶   REDEPLOY VESSEL", "[R]", new Color(0, 229, 255), new Color(0, 119, 182), isButtonHovered(bx, cardY + 244, btnW, 40));
        renderTacticalButton(g, bx, cardY + 294, btnW, 36, "◈   RETURN TO MAIN MENU", "[M]", new Color(168, 85, 247), new Color(36, 18, 48), isButtonHovered(bx, cardY + 294, btnW, 36));
    }

    private void renderStatRow(Graphics2D g, int x, int w, int y, String label, String value, Color valColor, boolean bold) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(148, 163, 184));
        g.drawString(label, x, y);

        g.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, 12));
        g.setColor(valColor);
        FontMetrics fm = g.getFontMetrics();
        int vx = x + w - fm.stringWidth(value);
        g.drawString(value, vx, y);
    }

    private void renderTacticalButton(Graphics2D g, int x, int y, int w, int h, String text, String shortcut, Color accent, Color bg, boolean hovered) {
        int arc = 10;
        if (hovered) {
            Color tintLeft = new Color(Math.min(255, bg.getRed() + 35), Math.min(255, bg.getGreen() + 35), Math.min(255, bg.getBlue() + 45), 235);
            g.setPaint(new java.awt.GradientPaint(x, y, tintLeft, x + w, y, new Color(17, 24, 39, 220)));
        } else {
            g.setPaint(new java.awt.GradientPaint(x, y, new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 175), x + w, y, new Color(13, 19, 33, 195)));
        }
        g.fillRoundRect(x, y, w, h, arc, arc);

        Color border = hovered ? accent : new Color(45, 60, 90, 150);
        g.setColor(border);
        g.setStroke(new BasicStroke(hovered ? 1.4f : 1.0f));
        g.drawRoundRect(x, y, w, h, arc, arc);

        // Left indicator bar
        g.setColor(accent);
        g.fillRoundRect(x + 2, y + 4, 4, h - 8, 3, 3);

        // Text
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(hovered ? Color.WHITE : new Color(226, 232, 240));
        g.drawString(text, x + 20, ty);

        if (shortcut != null && !shortcut.isEmpty()) {
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            FontMetrics fmSc = g.getFontMetrics();
            int scX = x + w - fmSc.stringWidth(shortcut) - 14;
            g.setColor(hovered ? accent : new Color(148, 163, 184));
            g.drawString(shortcut, scX, ty);
        }
    }

    private boolean isButtonHovered(int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public PauseButton getClickedPauseButton(int x, int y) {
        int cardW = 440;
        int cardH = 350;
        int cardX = (screenWidth - cardW) / 2;
        int cardY = (screenHeight - cardH) / 2;
        int btnW = 360;
        int btnH = 38;
        int bx = cardX + 40;

        if (x >= bx && x <= bx + btnW) {
            if (y >= cardY + 86 && y <= cardY + 86 + btnH) return PauseButton.RESUME;
            if (y >= cardY + 132 && y <= cardY + 132 + btnH) return PauseButton.RESTART;
            if (y >= cardY + 178 && y <= cardY + 178 + btnH) return PauseButton.SETTINGS;
            if (y >= cardY + 224 && y <= cardY + 224 + btnH) return PauseButton.MENU;
            if (y >= cardY + 270 && y <= cardY + 270 + btnH) return PauseButton.EXIT;
        }
        return PauseButton.NONE;
    }

    public GameOverButton getClickedGameOverButton(int x, int y) {
        int cardW = 480;
        int cardH = 370;
        int cardX = (screenWidth - cardW) / 2;
        int cardY = (screenHeight - cardH) / 2;
        int btnW = 400;
        int bx = cardX + 40;

        if (x >= bx && x <= bx + btnW) {
            if (y >= cardY + 244 && y <= cardY + 244 + 40) return GameOverButton.REDEPLOY;
            if (y >= cardY + 294 && y <= cardY + 294 + 36) return GameOverButton.MENU;
        }
        return GameOverButton.NONE;
    }

    private void renderDemoBadges(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        for (Enemy e : enemies) {
            if (!e.isActive()) continue;
            String label = "";
            Color col = Color.CYAN;
            if (e instanceof ArmouredEnemy arm) {
                label = "CO1: Armoured (Armor:" + arm.getArmorRating() + ")";
                col = new Color(0, 220, 230);
            } else if (e instanceof InterceptorEnemy inc) {
                label = "CO1: Interceptor" + (inc.isDashing() ? " [DASH]" : "");
                col = new Color(255, 180, 50);
            } else if (e instanceof MorphingEnemy morph) {
                label = "CO2: Morphing [" + morph.getCurrentState() + "]";
                col = new Color(180, 100, 255);
            } else if (e instanceof FailSafeEnemy fs) {
                label = "CO3: Fail-Safe" + (fs.isEmergencyBarrierActive() ? " [BARRIER]" : "");
                col = new Color(50, 220, 150);
            } else if (e instanceof RammerEnemy ram) {
                label = "CO4: Rammer" + (ram.isCharging() ? " [RAM]" : "");
                col = new Color(255, 90, 90);
            }
            if (!label.isEmpty()) {
                FontMetrics fm = g.getFontMetrics();
                int textW = fm.stringWidth(label);
                int lx = (int) (e.getX() + (e.getWidth() - textW) / 2.0);
                int ly = (int) Math.max(14, e.getY() - 6);
                g.setColor(new Color(10, 15, 25, 210));
                g.fillRoundRect(lx - 4, ly - 10, textW + 8, 13, 4, 4);
                g.setColor(col);
                g.drawString(label, lx, ly);
            }
        }
    }

    private void renderDemoBanner(Graphics2D g, int width) {
        g.setColor(new Color(7, 10, 19, 220));
        g.fillRect(0, 0, width, 46);
        g.setColor(new Color(0, 229, 255, 140));
        g.drawLine(0, 46, width, 46);

        g.setColor(new Color(0, 229, 255));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("PSOOP 5-ENEMY ACADEMIC DEMONSTRATION ARENA", 20, 18);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(200, 225, 245));
        g.drawString("[1] Armoured (CO1) | [2] Interceptor (CO1) | [3] Morphing (CO2) | [4] Fail-Safe (CO3) | [5] Rammer (CO4) | [C] Clear | [R] Reset | [M] Menu", 20, 36);
    }

    // Input hooks
    public void setShooting(boolean shooting) { this.shootingTriggered = shooting; }

    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }

    public int getEnemiesDestroyed() { return enemiesDestroyed; }
    public int getShotsFired() { return shotsFired; }
    public int getShotsHit() { return shotsHit; }
    public int getAccuracy() {
        return shotsFired > 0 ? (int) Math.min(100, Math.round((shotsHit * 100.0) / shotsFired)) : 0;
    }
    public void setMousePos(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public void setMatchService(service.MatchService matchService) { this.matchService = matchService; }
    public service.MatchService getMatchService() { return matchService; }
    public long getMatchStartTimeMs() { return matchStartTimeMs; }
    public boolean isMatchUploaded() { return matchUploaded; }
    public String getMatchUploadStatus() { return matchUploadStatus; }

    public boolean isBossActive() {
        for (Enemy e : enemies) {
            if (e.isActive() && e instanceof model.BossEnemy) {
                return true;
            }
        }
        return false;
    }

    public GameState getState() { return state; }
    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public List<Particle> getParticles() { return particles; }
    public ScoreCalculator getScoreCalculator() { return scoreCalculator; }
    public WaveManager getWaveManager() { return waveManager; }
}

