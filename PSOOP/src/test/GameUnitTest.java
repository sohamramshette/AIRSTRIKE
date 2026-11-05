package test;

import exception.InvalidGameStateException;
import game.CollisionManager;
import game.GameEngine;
import game.GameState;
import model.BossEnemy;
import model.DroneEnemy;
import model.Enemy;
import model.FighterEnemy;
import model.Particle;
import model.Player;
import model.PowerUp;
import model.PowerUpType;
import model.Projectile;
import service.ScoreCalculator;
import service.WaveManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Automated test suite verifying all 11 core functional gameplay requirements.
 */
public class GameUnitTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    private static void assertTrue(String testName, boolean condition, String message) {
        testsTotal++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + ": " + message);
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("        AIRSTRIKE PHASE 2 AUTOMATED TEST SUITE          ");
        System.out.println("========================================================");

        testPlayerMovement();
        testPlayerBoundaries();
        testShooting();
        testEnemySpawning();
        testBulletCollision();
        testEnemyHealthAndDestruction();
        testPlayerHealthAndShield();
        testScoreCalculation();
        testLevelProgression();
        testPauseAndResume();
        testGameOverCondition();

        System.out.println("========================================================");
        System.out.println("TEST RESULTS: " + testsPassed + " / " + testsTotal + " PASSED");
        System.out.println("========================================================");

        if (testsPassed != testsTotal) {
            System.exit(1);
        }
        System.exit(0);
    }

    // 1. Player Movement Test
    private static void testPlayerMovement() {
        Player player = new Player(400, 300);
        player.setMoveLeft(true);
        player.update(0.1); // 100 ms
        assertTrue("Player Move Left", player.getX() < 400, "X should have decreased after moving left");

        player.setMoveLeft(false);
        player.setMoveRight(true);
        double prevX = player.getX();
        player.update(0.1);
        assertTrue("Player Move Right", player.getX() > prevX, "X should have increased after moving right");

        player.setMoveRight(false);
        player.setMoveUp(true);
        player.update(0.1);
        assertTrue("Player Move Up", player.getY() < 300, "Y should have decreased after moving up");

        player.setMoveUp(false);
        player.setMoveDown(true);
        double prevY = player.getY();
        player.update(0.1);
        assertTrue("Player Move Down", player.getY() > prevY, "Y should have increased after moving down");
    }

    // 2. Player Boundaries Test
    private static void testPlayerBoundaries() {
        Player player = new Player(-100, -50);
        player.clampToBounds(0, 800, 0, 600);
        assertTrue("Player Boundary Left/Top", player.getX() == 0 && player.getY() == 0,
                "Player should be clamped to (0, 0) minimum boundary");

        player.setX(900);
        player.setY(700);
        player.clampToBounds(0, 800, 0, 600);
        assertTrue("Player Boundary Right/Bottom",
                player.getX() == (800 - player.getWidth()) && player.getY() == (600 - player.getHeight()),
                "Player should be clamped to (800 - width, 600 - height)");
    }

    // 3. Shooting Test
    private static void testShooting() {
        Player player = new Player(400, 500);
        List<Projectile> projectiles = new ArrayList<>();
        player.shoot(projectiles);

        assertTrue("Shooting Projectile Added", projectiles.size() == 1, "Exactly one projectile should be created at weapon level 1");
        Projectile p = projectiles.get(0);
        assertTrue("Projectile Properties", p.isPlayerBullet() && p.getVy() < 0, "Projectile should be a player bullet moving upwards");

        // Test shooting cooldown
        player.shoot(projectiles);
        assertTrue("Shooting Cooldown", projectiles.size() == 1, "Immediate consecutive shot should be blocked by cooldown timer");
    }

    // 4. Enemy Spawning Test
    private static void testEnemySpawning() {
        WaveManager waveManager = new WaveManager();
        List<Enemy> enemies = new ArrayList<>();

        // Advance simulation time to trigger enemy spawn
        for (int i = 0; i < 50; i++) {
            waveManager.update(0.1, enemies, 800);
            if (!enemies.isEmpty()) break;
        }

        assertTrue("Enemy Spawning", !enemies.isEmpty(), "WaveManager should spawn at least one enemy");
        Enemy e = enemies.get(0);
        assertTrue("Enemy Properties", e.isActive() && (e instanceof DroneEnemy || e instanceof FighterEnemy),
                "Spawned enemy should be active Drone or Fighter");
    }

    // 5. Bullet Collision Test
    private static void testBulletCollision() {
        CollisionManager cm = new CollisionManager();
        List<Projectile> projectiles = new ArrayList<>();
        List<Enemy> enemies = new ArrayList<>();
        List<Particle> particles = new ArrayList<>();
        List<PowerUp> powerUps = new ArrayList<>();
        ScoreCalculator scoreCalc = new ScoreCalculator();
        WaveManager waveMgr = new WaveManager();

        DroneEnemy enemy = new DroneEnemy(200, 200);
        enemies.add(enemy);
        int initialHealth = enemy.getHealth();

        // Direct hit projectile
        Projectile bullet = new Projectile(205, 205, 6, 16, 0, -400, true, 25);
        projectiles.add(bullet);

        cm.checkPlayerBulletsVsEnemies(projectiles, enemies, particles, powerUps, scoreCalc, waveMgr);

        assertTrue("Bullet Consumed on Hit", !bullet.isActive(), "Projectile should be consumed upon impact");
        assertTrue("Enemy Damaged on Hit", enemy.getHealth() < initialHealth, "Enemy health should decrease after impact");
    }

    // 6. Enemy Health & Destruction Test
    private static void testEnemyHealthAndDestruction() {
        FighterEnemy fighter = new FighterEnemy(300, 300);
        fighter.takeDamage(20); // Shield takes damage
        assertTrue("Enemy Shield Absorption", fighter.getShield() == 0 && fighter.getHealth() == 60, "Shield should absorb first 20 damage");

        fighter.takeDamage(60); // Hull damage
        assertTrue("Enemy Destruction", fighter.isDestroyed() && !fighter.isActive(), "Enemy should be destroyed and deactivated at 0 health");
    }

    // 7. Player Health & Shield Test
    private static void testPlayerHealthAndShield() {
        Player player = new Player(400, 400);
        // Player has 100 shield and 100 health
        player.takeDamage(40);
        assertTrue("Player Shield Absorption", player.getShield() == 60 && player.getHealth() == 100, "Shield should absorb damage first");

        player.takeDamage(80);
        assertTrue("Player Hull Damage", player.getShield() == 0 && player.getHealth() == 80, "Remaining damage should penetrate hull once shield is depleted");

        player.heal(15);
        assertTrue("Player Heal", player.getHealth() == 95, "Health should increase after healing");

        player.rechargeShield(30);
        assertTrue("Player Shield Recharge", player.getShield() == 30, "Shield should recharge properly");
    }

    // 8. Score Calculation Test
    private static void testScoreCalculation() {
        ScoreCalculator scoreCalc = new ScoreCalculator();
        scoreCalc.addScore(100);
        assertTrue("Base Score Calculation", scoreCalc.getScore() == 100, "Base score should be 100");

        // Simulate kill streak
        for (int i = 0; i < 4; i++) {
            scoreCalc.addScore(100);
        }
        assertTrue("Streak Multiplier", scoreCalc.getMultiplier() >= 2, "Multiplier should scale to at least 2x on streak");

        // Overloaded method test
        scoreCalc.addScore(500, 3);
        assertTrue("Overloaded AddScore", scoreCalc.getScore() >= 100 + 400 + 1500, "Custom multiplier should apply to base score");

        scoreCalc.resetStreak();
        assertTrue("Reset Streak", scoreCalc.getMultiplier() == 1 && scoreCalc.getKillStreak() == 0, "Streak should reset to 1x");
    }

    // 9. Level Progression Test
    private static void testLevelProgression() {
        WaveManager waveMgr = new WaveManager();
        assertTrue("Initial Level", waveMgr.getCurrentLevel() == 1, "Game should start at Level 1");

        waveMgr.resetToLevel(3);
        assertTrue("Level 3 Advance", waveMgr.getCurrentLevel() == 3, "Wave manager should advance to level 3");

        List<Enemy> enemies = new ArrayList<>();
        waveMgr.update(0.1, enemies, 800);
        assertTrue("Boss Spawning on Milestone", waveMgr.isBossSpawned() && enemies.get(0) instanceof BossEnemy,
                "Boss flagship should spawn on Level 3");
    }

    // 10. Pause & Resume Test
    private static void testPauseAndResume() {
        GameEngine engine = new GameEngine(800, 600);
        engine.startNewGame();
        assertTrue("Engine Start Playing", engine.getState() == GameState.PLAYING, "GameEngine should be in PLAYING state");

        engine.pauseGame();
        assertTrue("Engine Pause", engine.getState() == GameState.PAUSED, "GameEngine should be in PAUSED state");

        engine.resumeGame();
        assertTrue("Engine Resume", engine.getState() == GameState.PLAYING, "GameEngine should be in PLAYING state after resume");
    }

    // 11. Game-Over Condition Test
    private static void testGameOverCondition() {
        Player player = new Player(400, 500);
        player.setLives(1);
        player.takeDamage(200); // Fatal damage

        boolean hasLives = player.loseLifeAndRespawn(400, 500);
        assertTrue("Game Over Trigger", !hasLives && !player.isActive() && player.getLives() == 0,
                "Player should have 0 lives and be inactive after final life loss");
    }
}
