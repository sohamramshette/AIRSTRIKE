package test;

import exception.InvalidGameStateException;
import game.CollisionManager;
import game.GameEngine;
import game.GameState;
import model.BossEnemy;
import model.DroneEnemy;
import model.Enemy;
import model.FighterEnemy;
import model.ArmouredEnemy;
import model.InterceptorEnemy;
import model.MorphingEnemy;
import model.DroneAIState;
import model.FailSafeEnemy;
import model.RammerEnemy;
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

        // Phase 8.2: Five New PSOOP Enemies & Multi-Tier Wave Verification
        testArmouredEnemyEncapsulation();
        testInterceptorEnemyPolymorphism();
        testMorphingEnemyStateTransitions();
        testFailSafeEnemyDefensiveProgramming();
        testRammerEnemyKinematicsAndCollision();
        testWaveProgressionMultiTier();

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

    // 17. Phase 8.2: Armoured Vanguard Encapsulation & Damage Mitigation (PSOOP-CO1)
    private static void testArmouredEnemyEncapsulation() {
        ArmouredEnemy armoured = new ArmouredEnemy(200, 100);
        assertTrue("Armoured Dimensions", armoured.getWidth() == 46 && armoured.getHeight() == 44,
                "Armoured Vanguard should have width 46, height 44");
        assertTrue("Armoured Vitals", armoured.getHealth() == 90 && armoured.getShield() == 30,
                "Armoured Vanguard should initialize with 90 health and 30 shield");
        assertTrue("Armoured Armor Rating", armoured.getArmorRating() == 8,
                "Armoured Vanguard should encapsulate an 8-point flat armor rating");

        // Shield absorbs first
        armoured.takeDamage(20);
        assertTrue("Armoured Shield Absorbs First", armoured.getShield() == 10 && armoured.getHealth() == 90,
                "Shield should absorb 20 damage without reducing health");

        // Remaining damage is reduced by armor (30 damage: 10 to shield, 20 to armor -> 20 - 8 = 12 damage to health)
        armoured.takeDamage(30);
        assertTrue("Armoured Armor Mitigation", armoured.getShield() == 0 && armoured.getHealth() == 78,
                "Armor should reduce 20 remaining damage by 8 down to 12 effective hull damage");

        // Firing weapon
        List<Projectile> projectiles = new ArrayList<>();
        armoured.shoot(projectiles);
        assertTrue("Armoured Heavy Plasma Slug", !projectiles.isEmpty() && projectiles.get(0).getDamage() == 20,
                "Armoured Vanguard should discharge heavy prow plasma slugs with 20 damage");
    }

    // 18. Phase 8.2: Interceptor Fighter Polymorphism & Dynamic Dispatch (PSOOP-CO1)
    private static void testInterceptorEnemyPolymorphism() {
        Enemy interceptor = new InterceptorEnemy(200, 100);
        Player player = new Player(210, 300); // Aligned horizontally
        List<Projectile> projectiles = new ArrayList<>();

        // Polymorphic invocation via Enemy reference
        interceptor.performAttack(1.2, projectiles, player);
        interceptor.update(0.016);

        assertTrue("Interceptor Dynamic Dispatch Dash", ((InterceptorEnemy) interceptor).isDashing(),
                "Polymorphic invocation should trigger high-speed dash upon player horizontal alignment");
        assertTrue("Interceptor Staggered Shot", !projectiles.isEmpty() && projectiles.get(0).getDamage() == 12,
                "Interceptor should discharge micro-darts with 12 damage");
    }

    // 19. Phase 8.2: Adaptive Morphing Drone State Pattern & Transitions (PSOOP-CO2)
    private static void testMorphingEnemyStateTransitions() {
        MorphingEnemy morphing = new MorphingEnemy(300, 100);
        assertTrue("Morphing Initial State", morphing.getCurrentState() == DroneAIState.PATROL,
                "Morphing Drone should initialize in PATROL state");

        // Far away player -> stays PATROL
        Player farPlayer = new Player(800, 800);
        morphing.performAttack(0.1, new ArrayList<>(), farPlayer);
        morphing.update(0.1);
        assertTrue("Morphing Patrol Sustained", morphing.getCurrentState() == DroneAIState.PATROL,
                "Morphing Drone should remain in PATROL when player is distant");

        // Near player (dist < 260) -> transitions to CHASE
        Player nearPlayer = new Player(310, 200);
        morphing.performAttack(0.1, new ArrayList<>(), nearPlayer);
        morphing.update(0.1);
        assertTrue("Morphing Transition CHASE", morphing.getCurrentState() == DroneAIState.CHASE,
                "Morphing Drone should transition to CHASE when player approaches within 260px");

        // Close quarters (dist < 150) -> transitions to OVERCHARGE
        Player closePlayer = new Player(310, 130);
        morphing.performAttack(0.1, new ArrayList<>(), closePlayer);
        morphing.update(0.1);
        assertTrue("Morphing Transition OVERCHARGE", morphing.getCurrentState() == DroneAIState.OVERCHARGE,
                "Morphing Drone should transition to OVERCHARGE when player is in close quarters");

        // Hull damaged critically -> transitions to EVADE
        // 50 HP, 15 Shield. Hit for 48 damage -> 15 shield, 33 to health -> 17 HP remaining (34% < 35%)
        morphing.takeDamage(48);
        morphing.update(0.1);
        assertTrue("Morphing Transition EVADE", morphing.getCurrentState() == DroneAIState.EVADE,
                "Morphing Drone should transition to EVADE when health drops critically low");
    }

    // 20. Phase 8.2: Fail-Safe Sentry Defensive Programming (PSOOP-CO3)
    private static void testFailSafeEnemyDefensiveProgramming() {
        FailSafeEnemy sentry = new FailSafeEnemy(150, 100);
        assertTrue("FailSafe Initial Properties", sentry.getHealth() == 70 && sentry.getShield() == 15,
                "FailSafe Sentry should initialize with 70 health and 15 shield");

        // Non-positive damage rejected defensively
        sentry.takeDamage(-25);
        assertTrue("FailSafe Negative Damage Rejection", sentry.getHealth() == 70,
                "Defensive invariant should reject non-positive damage");

        // Damage above threshold (70 HP, 15 Shield. Hit for 50 -> 15 to shield, 35 to health -> 35 HP > 21)
        sentry.takeDamage(50);
        assertTrue("FailSafe Normal Damage", sentry.getHealth() == 35 && !sentry.isEmergencyBarrierActive(),
                "Normal damage should resolve without activating emergency barrier");

        // Critical threshold hit (35 HP. Hit for 20 -> 15 HP <= 21 HP threshold)
        sentry.takeDamage(20);
        assertTrue("FailSafe Emergency Barrier Activated", sentry.isEmergencyBarrierActive() && sentry.isEmergencyBarrierTriggered(),
                "Critical health should trigger emergency barrier");
        assertTrue("FailSafe Shield Recharged Defensively", sentry.getShield() >= 35,
                "Emergency barrier should defensively recharge shield buffer");

        // Subsequent damage mitigated under active barrier
        int preShield = sentry.getShield();
        sentry.takeDamage(20);
        assertTrue("FailSafe 50% Barrier Damage Mitigation", sentry.getShield() == (preShield - 10),
                "Active barrier should mitigate incoming damage by 50%");
    }

    // 21. Phase 8.2: Kinetic Battering Ram Kinematics & Collision (PSOOP-CO4)
    private static void testRammerEnemyKinematicsAndCollision() {
        RammerEnemy rammer = new RammerEnemy(200, 100);
        Player player = new Player(210, 400); // Forward corridor
        CollisionManager cm = new CollisionManager();
        ScoreCalculator scoreCalc = new ScoreCalculator();
        WaveManager waveMgr = new WaveManager();
        List<Particle> particles = new ArrayList<>();
        List<PowerUp> powerUps = new ArrayList<>();

        assertTrue("Rammer Initial Speed", rammer.getSpeed() == 70.0 && !rammer.isCharging(),
                "Rammer should start uncharged at patrol speed");

        // Corridor acquisition
        rammer.performAttack(0.1, new ArrayList<>(), player);
        assertTrue("Rammer Locked and Charging", rammer.isCharging(),
                "Rammer should lock and charge when player is in forward corridor");

        // Non-uniform acceleration
        double prevVy = rammer.getVy();
        rammer.update(0.2);
        assertTrue("Rammer Non-Uniform Acceleration", rammer.getVy() > prevVy,
                "Kinematics integration should accelerate rammer velocity");

        // Kinetic prow impact on player
        List<Enemy> enemyList = new ArrayList<>();
        enemyList.add(rammer);
        player.setX(rammer.getX());
        player.setY(rammer.getY()); // Force collision overlap
        player.setShield(0); // Deplete shields to test direct kinetic prow impact
        int prePlayerHealth = player.getHealth();
        cm.checkPlayerVsEnemies(player, enemyList, particles, scoreCalc, waveMgr, powerUps);

        assertTrue("Rammer Prow Impact Inflicted", player.getHealth() == (prePlayerHealth - 45),
                "Rammer prow impact should inflict exactly 45 specialized damage to player hull");
    }

    // 22. Phase 8.2: Multi-Tier Fleet Spawning Progression
    private static void testWaveProgressionMultiTier() {
        WaveManager waveMgr = new WaveManager();

        // Level 1: Drones and Fighters only
        waveMgr.resetToLevel(1);
        List<Enemy> l1Enemies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            waveMgr.update(5.0, l1Enemies, 800);
        }
        boolean onlyDroneOrFighter = l1Enemies.stream()
                .allMatch(e -> e instanceof DroneEnemy || e instanceof FighterEnemy);
        assertTrue("Level 1 Baseline Spawning", onlyDroneOrFighter && !l1Enemies.isEmpty(),
                "Level 1 should strictly spawn baseline Drone and Fighter craft");

        // Level 2: Introduces Armoured and Interceptor
        waveMgr.resetToLevel(2);
        List<Enemy> l2Enemies = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            waveMgr.update(5.0, l2Enemies, 800);
        }
        boolean hasArmouredOrInterceptor = l2Enemies.stream()
                .anyMatch(e -> e instanceof ArmouredEnemy || e instanceof InterceptorEnemy);
        assertTrue("Level 2 Armoured/Interceptor Introduction", hasArmouredOrInterceptor,
                "Level 2 should introduce Armoured Vanguard and Interceptor Fighter");

        // Level 5: Full combined fleet
        waveMgr.resetToLevel(5);
        List<Enemy> l5Enemies = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            waveMgr.update(5.0, l5Enemies, 800);
        }
        long uniqueClasses = l5Enemies.stream().map(Object::getClass).distinct().count();
        assertTrue("Level 5 Full Fleet Deployment", uniqueClasses >= 4,
                "Level 5+ should deploy a rich multi-class hostile fleet");
    }
}

