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
        testPowerUpCollectionAndEffects();
        testBossAttacksAndPhases();
        testIllegalStateTransitions();
        testEntityPruning();
        testStreakResetOnDamage();

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

    // 12. Power-Up Collection & Effects Test
    private static void testPowerUpCollectionAndEffects() {
        Player player = new Player(300, 300);
        player.setHealth(50);
        player.setShield(20);
        double initialSpeed = player.getSpeed();

        CollisionManager cm = new CollisionManager();
        List<PowerUp> powerUps = new ArrayList<>();
        List<Particle> particles = new ArrayList<>();
        ScoreCalculator scoreCalc = new ScoreCalculator();

        // Health Pack
        PowerUp hp = new PowerUp(305, 305, PowerUpType.HEALTH_PACK);
        powerUps.add(hp);
        cm.checkPlayerVsPowerUps(player, powerUps, particles, scoreCalc);
        assertTrue("Health PowerUp Effect", player.getHealth() == 90 && !hp.isActive(), "Health pack should restore 40 health");

        // Shield Boost
        PowerUp sb = new PowerUp(305, 305, PowerUpType.SHIELD_BOOST);
        powerUps.add(sb);
        cm.checkPlayerVsPowerUps(player, powerUps, particles, scoreCalc);
        assertTrue("Shield PowerUp Effect", player.getShield() == 70 && !sb.isActive(), "Shield boost should recharge 50 shield");

        // Weapon Upgrade
        PowerUp wu = new PowerUp(305, 305, PowerUpType.WEAPON_UPGRADE);
        powerUps.add(wu);
        cm.checkPlayerVsPowerUps(player, powerUps, particles, scoreCalc);
        assertTrue("Weapon Upgrade Effect", player.getWeaponLevel() == 2 && !wu.isActive(), "Weapon level should advance to 2");

        // Speed Boost
        PowerUp spd = new PowerUp(305, 305, PowerUpType.SPEED_BOOST);
        powerUps.add(spd);
        cm.checkPlayerVsPowerUps(player, powerUps, particles, scoreCalc);
        assertTrue("Speed Boost Effect", player.getSpeed() > initialSpeed && !spd.isActive(), "Speed should increase after speed boost");
    }

    // 13. Boss Attacks & Phases Test
    private static void testBossAttacksAndPhases() {
        BossEnemy boss = new BossEnemy(300, 45, "Test Titan");
        List<Projectile> projectiles = new ArrayList<>();

        // Phase 0: 5-way spread fan
        boss.performAttack(0.016, projectiles, null);
        assertTrue("Boss Phase 0 Spread Shot", projectiles.size() == 5, "Boss phase 0 should fire 5-way spread fan");

        // Advance phase
        boss.update(4.5); // Advance past 4.0s phase timer
        projectiles.clear();
        boss.setShootCooldownMs(0); // Reset cooldown for test
        boss.performAttack(0.016, projectiles, null);
        assertTrue("Boss Phase 1 Dual Cannons", projectiles.size() == 2, "Boss phase 1 should fire 2 heavy plasma beams");
    }

    // 14. Illegal State Transitions Test
    private static void testIllegalStateTransitions() {
        GameEngine engine = new GameEngine(800, 600);
        boolean caughtException = false;
        try {
            engine.setState(GameState.PAUSED); // Illegal: Cannot pause from MENU
        } catch (InvalidGameStateException e) {
            caughtException = true;
        }
        assertTrue("Illegal State Exception", caughtException, "Engine should reject MENU -> PAUSED transition");
    }

    // 15. Entity Pruning (Memory Leak Prevention) Test
    private static void testEntityPruning() {
        GameEngine engine = new GameEngine(800, 600);
        engine.startNewGame();

        // Add an off-screen projectile and a dead enemy
        Projectile offscreenBullet = new Projectile(400, 900, 6, 16, 0, 500, false, 10);
        engine.getProjectiles().add(offscreenBullet);

        DroneEnemy deadDrone = new DroneEnemy(200, 200);
        deadDrone.setActive(false);
        engine.getEnemies().add(deadDrone);

        engine.update(0.016);

        assertTrue("Offscreen Bullet Pruned", !engine.getProjectiles().contains(offscreenBullet),
                "Offscreen projectiles should be automatically pruned");
        assertTrue("Dead Enemy Pruned", !engine.getEnemies().contains(deadDrone),
                "Inactive enemies should be automatically pruned");
    }

    // 16. Streak Reset on Damage Test
    private static void testStreakResetOnDamage() {
        CollisionManager cm = new CollisionManager();
        ScoreCalculator scoreCalc = new ScoreCalculator();
        Player player = new Player(200, 200);
        List<Particle> particles = new ArrayList<>();

        scoreCalc.addScore(100);
        scoreCalc.addScore(100);
        scoreCalc.addScore(100);
        int preStreak = scoreCalc.getKillStreak();

        // Enemy bullet hits player
        List<Projectile> enemyBullets = new ArrayList<>();
        enemyBullets.add(new Projectile(205, 205, 5, 14, 0, 300, false, 15));

        cm.checkEnemyBulletsVsPlayer(enemyBullets, player, particles, scoreCalc);

        assertTrue("Streak Reset on Hit", preStreak > 0 && scoreCalc.getKillStreak() == 0,
                "Combo streak should reset to 0 upon player taking damage");
    }
}

