// ==============================================================================
// File      : PSOOP/src/game/CollisionManager.java
// Subject   : PSOOP-CO4 | AABB Collision Detection & Kinetic Resolution
// Topics    : [TOPIC: AABB Collision] [TOPIC: Polymorphism] [TOPIC: Particle FX]
// Game Role : Resolves bullet, hull, and power-up collisions and awards score/drops.
// ==============================================================================
package game;

import model.Enemy;
import model.Particle;
import model.Player;
import model.PowerUp;
import model.Projectile;
import service.ScoreCalculator;
import service.WaveManager;

import java.awt.Color;
import java.util.List;
import java.util.Random;

/**
 * [TOPIC: AABB Collision]
 * High-performance collision detection and resolution subsystem across projectiles, ships, and power-ups.
 */
public class CollisionManager {

    private final Random random;

    public CollisionManager() {
        this.random = new Random();
    }

    /**
     * Resolves collisions between player laser bolts and hostile enemy craft.
     */
    public void checkPlayerBulletsVsEnemies(
            List<Projectile> projectiles,
            List<Enemy> enemies,
            List<Particle> particles,
            List<PowerUp> powerUps,
            ScoreCalculator scoreCalculator,
            WaveManager waveManager) {
        checkPlayerBulletsVsEnemies(projectiles, enemies, particles, powerUps, scoreCalculator, waveManager, null);
    }

    /**
     * [TOPIC: AABB Collision] [TOPIC: Polymorphism]
     * Resolves player projectile AABB hits against enemies and triggers drops/salvage on kill.
     */
    public void checkPlayerBulletsVsEnemies(
            List<Projectile> projectiles,
            List<Enemy> enemies,
            List<Particle> particles,
            List<PowerUp> powerUps,
            ScoreCalculator scoreCalculator,
            WaveManager waveManager,
            Player player) {

        for (Projectile p : projectiles) {
            if (!p.isActive() || !p.isPlayerBullet()) continue;

            for (Enemy e : enemies) {
                if (!e.isActive()) continue;

                // WHY: AABB bounding-box overlap test avoids multi-enemy penetration after a single bullet impact.
                if (p.intersects(e)) {
                    p.setActive(false);
                    e.takeDamage(p.getDamage());

                    // Visual sparks at impact point
                    spawnSparks(p.getX(), p.getY(), new Color(180, 240, 255), 4, particles);

                    if (e.isDestroyed()) {
                        scoreCalculator.addScore(e.getScoreValue());
                        spawnExplosion(e.getCenterX(), e.getCenterY(), new Color(255, 140, 30), 20, particles);
                        waveManager.evaluatePowerUpDrop(e.getCenterX(), e.getCenterY(), powerUps);

                        // Titan Core Salvage: Player Vitality & Shield Recovery (PSOOP-CO1)
                        if (e instanceof model.BossEnemy && player != null && player.isActive()) {
                            player.heal(65);
                            player.setShield(player.getMaxShield());
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * [TOPIC: AABB Collision]
     * Resolves enemy projectile hits on the player, granting brief i-frames and resetting streak.
     */
    public void checkEnemyBulletsVsPlayer(
            List<Projectile> projectiles,
            Player player,
            List<Particle> particles,
            ScoreCalculator scoreCalculator) {

        if (!player.isActive() || player.isInvulnerable()) return;

        for (Projectile p : projectiles) {
            if (!p.isActive() || p.isPlayerBullet()) continue;

            if (p.intersects(player)) {
                p.setActive(false);
                player.takeDamage(p.getDamage());
                // WHY: Brief 400 ms invulnerability prevents overlapping bolts from instakilling the player in one tick.
                player.triggerInvulnerability(400); // 400ms i-frame on bullet impact
                scoreCalculator.resetStreak();

                // Shield or armor impact sparks
                Color sparkColor = (player.getShield() > 0) ? new Color(0, 200, 255) : new Color(255, 80, 60);
                spawnSparks(p.getX(), p.getY(), sparkColor, 8, particles);

                if (player.isDestroyed()) {
                    spawnExplosion(player.getCenterX(), player.getCenterY(), new Color(255, 80, 30), 30, particles);
                }
            }
        }
    }

    /**
     * [TOPIC: AABB Collision] [TOPIC: Polymorphism]
     * Resolves ship-to-ship ramming collisions between the player and hostile craft.
     */
    public void checkPlayerVsEnemies(
            Player player,
            List<Enemy> enemies,
            List<Particle> particles,
            ScoreCalculator scoreCalculator,
            WaveManager waveManager,
            List<PowerUp> powerUps) {

        if (!player.isActive() || player.isInvulnerable()) return;

        for (Enemy e : enemies) {
            if (!e.isActive()) continue;

            if (player.intersects(e)) {
                // WHY: RammerEnemy applies specialized prow kinetic damage while other craft deal flat 35 hull impact.
                int kineticDamage = (e instanceof model.RammerEnemy rammer) ? rammer.getProwImpactDamage() : 35;
                player.takeDamage(kineticDamage);
                player.triggerInvulnerability(800); // i-frame after ramming
                e.takeDamage(60);
                scoreCalculator.resetStreak();

                spawnExplosion((player.getCenterX() + e.getCenterX()) / 2.0, (player.getCenterY() + e.getCenterY()) / 2.0, new Color(255, 180, 50), 16, particles);

                if (e.isDestroyed()) {
                    scoreCalculator.addScore(e.getScoreValue());
                    waveManager.evaluatePowerUpDrop(e.getCenterX(), e.getCenterY(), powerUps);
                }

                if (player.isDestroyed()) {
                    spawnExplosion(player.getCenterX(), player.getCenterY(), new Color(255, 50, 30), 32, particles);
                }
                break;
            }
        }
    }

    /**
     * [TOPIC: AABB Collision]
     * Resolves player collection of floating power-up enhancements.
     */
    public void checkPlayerVsPowerUps(Player player, List<PowerUp> powerUps, List<Particle> particles, ScoreCalculator scoreCalculator) {
        if (!player.isActive()) return;

        for (PowerUp pu : powerUps) {
            if (!pu.isActive()) continue;

            if (player.intersects(pu)) {
                pu.setActive(false);
                scoreCalculator.addScore(150, 1);

                switch (pu.getType()) {
                    case HEALTH_PACK -> player.heal(40);
                    case SHIELD_BOOST -> player.rechargeShield(50);
                    case WEAPON_UPGRADE -> player.upgradeWeapon();
                    case SPEED_BOOST -> player.setSpeed(player.getSpeed() + 25.0);
                }

                // Power-up absorption sparkle
                spawnSparks(pu.getCenterX(), pu.getCenterY(), pu.getType().getColor(), 12, particles);
            }
        }
    }

    public void spawnExplosion(double cx, double cy, Color baseColor, int count, List<Particle> particles) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 40.0 + random.nextDouble() * 180.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double life = 0.35 + random.nextDouble() * 0.45;
            double size = 4.0 + random.nextDouble() * 6.0;
            particles.add(new Particle(cx, cy, vx, vy, life, baseColor, size));
        }
    }

    public void spawnSparks(double cx, double cy, Color color, int count, List<Particle> particles) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 60.0 + random.nextDouble() * 120.0;
            particles.add(new Particle(cx, cy, Math.cos(angle) * speed, Math.sin(angle) * speed, 0.25, color, 3.0));
        }
    }
}
