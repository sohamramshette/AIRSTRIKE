package game;

import exception.InvalidGameStateException;
import model.Enemy;
import model.Particle;
import model.Player;
import model.PowerUp;
import model.Projectile;
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

    public GameEngine(int screenWidth, int screenHeight) {
        this.screenWidth = Math.max(640, screenWidth);
        this.screenHeight = Math.max(480, screenHeight);
        this.state = GameState.MENU;

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

        this.state = newState;
    }

    /**
     * Initiates a fresh game session.
     */
    public void startNewGame() {
        enemies.clear();
        projectiles.clear();
        powerUps.clear();
        particles.clear();

        scoreCalculator.resetGame();
        waveManager.resetToLevel(1);
        player.resetFull((screenWidth - player.getWidth()) / 2, screenHeight - 110);

        try {
            setState(GameState.PLAYING);
        } catch (InvalidGameStateException ignored) {}
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

        // 1. Update Player kinematics and boundaries
        player.update(delta);
        player.clampToBounds(0, screenWidth, 0, screenHeight);

        // Player weapon discharge
        if (shootingTriggered) {
            player.shoot(projectiles);
        }

        // 2. Wave Manager updates (enemy spawning & difficulty)
        waveManager.update(delta, enemies, screenWidth);

        // 3. Update active Enemies
        for (Enemy e : enemies) {
            if (e instanceof model.BossEnemy boss) {
                boss.setPatrolBounds(30, Math.max(300, screenWidth - boss.getWidth() - 30));
            }
            e.update(delta);
            e.performAttack(delta, projectiles, player);
            if (e.isOffScreen(screenHeight)) {
                e.setActive(false);
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
        collisionManager.checkPlayerBulletsVsEnemies(projectiles, enemies, particles, powerUps, scoreCalculator, waveManager);
        collisionManager.checkEnemyBulletsVsPlayer(projectiles, player, particles, scoreCalculator);
        collisionManager.checkPlayerVsEnemies(player, enemies, particles, scoreCalculator, waveManager, powerUps);
        collisionManager.checkPlayerVsPowerUps(player, powerUps, particles, scoreCalculator);

        // 8. Player Destruction & Respawn Handling
        if (player.isDestroyed()) {
            boolean hasRemainingLives = player.loseLifeAndRespawn((screenWidth - player.getWidth()) / 2, screenHeight - 110);
            if (!hasRemainingLives) {
                try {
                    setState(GameState.GAME_OVER);
                } catch (InvalidGameStateException ignored) {}
            }
        }

        // 9. Clean up inactive dead entities from collections
        enemies.removeIf(e -> !e.isActive());
        projectiles.removeIf(p -> !p.isActive());
        powerUps.removeIf(pu -> !pu.isActive());
        particles.removeIf(pt -> !pt.isActive());
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
        int barW = 160;
        int barH = 12;
        int startX = 20;
        int startY = 24;

        // Container panel
        g.setColor(new Color(12, 18, 30, 200));
        g.fillRoundRect(startX - 10, startY - 14, 280, 72, 8, 8);
        g.setColor(new Color(40, 80, 130));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(startX - 10, startY - 14, 280, 72, 8, 8);

        // Health Bar
        g.setColor(new Color(180, 200, 220));
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString("HULL INTEGRITY", startX, startY);
        g.setColor(new Color(50, 50, 60));
        g.fillRect(startX + 95, startY - 10, barW, barH);
        float hRatio = (float) player.getHealth() / player.getMaxHealth();
        g.setColor(hRatio > 0.35 ? new Color(50, 220, 90) : new Color(240, 50, 50));
        g.fillRect(startX + 95, startY - 10, (int) (barW * hRatio), barH);

        // Shield Bar
        g.setColor(new Color(180, 200, 220));
        g.drawString("SHIELD CAPACITOR", startX, startY + 18);
        g.setColor(new Color(50, 50, 60));
        g.fillRect(startX + 95, startY + 8, barW, barH);
        float sRatio = (float) player.getShield() / player.getMaxShield();
        g.setColor(new Color(0, 190, 255));
        g.fillRect(startX + 95, startY + 8, (int) (barW * sRatio), barH);

        // Lives indicator
        g.setColor(new Color(255, 200, 50));
        g.drawString("VESSELS: " + player.getLives(), startX, startY + 38);

        // Weapon level
        g.setColor(new Color(255, 160, 50));
        g.drawString("WEAPON: LVL " + player.getWeaponLevel(), startX + 100, startY + 38);

        // Score & Level (Top Right)
        int rightX = screenWidth - 220;
        g.setColor(new Color(12, 18, 30, 200));
        g.fillRoundRect(rightX - 10, startY - 14, 210, 86, 8, 8);
        g.setColor(new Color(40, 80, 130));
        g.drawRoundRect(rightX - 10, startY - 14, 210, 86, 8, 8);

        g.setColor(new Color(80, 230, 160));
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        String pilotName = database.SessionManager.getInstance().getCurrentUsername().toUpperCase();
        if (pilotName.length() > 18) pilotName = pilotName.substring(0, 18) + "...";
        g.drawString("PILOT: " + pilotName, rightX, startY);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("SCORE: " + scoreCalculator.getScore(), rightX, startY + 18);

        g.setColor(new Color(160, 220, 255));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("HIGH SCORE: " + scoreCalculator.getHighScore(), rightX, startY + 36);

        g.setColor(new Color(255, 220, 80));
        g.drawString("SECTOR: " + waveManager.getCurrentLevel(), rightX, startY + 54);

        if (scoreCalculator.getMultiplier() > 1) {
            g.setColor(new Color(255, 90, 40));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString(scoreCalculator.getMultiplier() + "x STREAK!", rightX + 110, startY + 54);
        }
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
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, screenWidth, screenHeight);

        renderBanner(g, "TACTICAL PAUSE", "Press [P] or [ESC] to Resume Mission | Press [M] for Main Menu", new Color(255, 210, 50));
    }

    private void renderGameOverScreen(Graphics2D g) {
        g.setColor(new Color(20, 5, 10, 210));
        g.fillRect(0, 0, screenWidth, screenHeight);

        int midY = screenHeight / 2;
        g.setColor(new Color(255, 50, 50));
        g.setFont(new Font("SansSerif", Font.BOLD, 42));
        FontMetrics fm = g.getFontMetrics();
        String title = "VESSEL DESTROYED";
        g.drawString(title, (screenWidth - fm.stringWidth(title)) / 2, midY - 60);

        g.setColor(new Color(80, 230, 160));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fmPilot = g.getFontMetrics();
        String pilotStr = "PILOT: " + database.SessionManager.getInstance().getCurrentUsername().toUpperCase();
        g.drawString(pilotStr, (screenWidth - fmPilot.stringWidth(pilotStr)) / 2, midY - 30);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm2 = g.getFontMetrics();
        String scoreStr = "FINAL SCORE: " + scoreCalculator.getScore();
        g.drawString(scoreStr, (screenWidth - fm2.stringWidth(scoreStr)) / 2, midY - 10);

        String highStr = "ALL-TIME RECORD: " + scoreCalculator.getHighScore();
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(255, 200, 80));
        FontMetrics fm3 = g.getFontMetrics();
        g.drawString(highStr, (screenWidth - fm3.stringWidth(highStr)) / 2, midY + 18);

        g.setColor(new Color(180, 220, 255));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm4 = g.getFontMetrics();
        String prompt = "Press [R] to Redeploy Ship | Press [M] for Main Menu";
        g.drawString(prompt, (screenWidth - fm4.stringWidth(prompt)) / 2, midY + 65);
    }

    // Input hooks
    public void setShooting(boolean shooting) { this.shootingTriggered = shooting; }

    public GameState getState() { return state; }
    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public List<Particle> getParticles() { return particles; }
    public ScoreCalculator getScoreCalculator() { return scoreCalculator; }
    public WaveManager getWaveManager() { return waveManager; }
}

