// ==============================================================================
// File      : PSOOP/src/model/Ship.java
// Subject   : PSOOP-CO1 | Inheritance, Method Overloading & Combat State
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overloading] [TOPIC: Encapsulation] [TOPIC: Abstraction]
// Game Role : Adds hull health, energy shields, speed, and weapon-firing contracts to Player and Enemy.
// ==============================================================================
package model;

import java.util.List;

/**
 * Abstract spacecraft extending Entity with shields, hull integrity, and weapon cooldowns.
 */
public abstract class Ship extends Entity {
    
    protected int maxHealth;
    protected int health;
    protected int maxShield;
    protected int shield;
    protected double speed;
    protected long lastShotTime;
    protected long shootCooldownMs;

    /**
     * [TOPIC: Inheritance] [TOPIC: Defensive Programming]
     * Game Role: Chains Entity setup and enforces valid non-negative hull, shield, speed, and cooldown bounds.
     */
    public Ship(double x, double y, int width, int height, int maxHealth, int maxShield, double speed, long shootCooldownMs) {
        super(x, y, width, height);
        // WHY: Clamps vital combat invariants at construction to prevent instant-death or zero-cooldown bugs.
        this.maxHealth = Math.max(1, maxHealth);
        this.health = this.maxHealth;
        this.maxShield = Math.max(0, maxShield);
        this.shield = this.maxShield;
        this.speed = Math.max(0.0, speed);
        this.shootCooldownMs = Math.max(50, shootCooldownMs);
        this.lastShotTime = 0;
    }

    /**
     * [TOPIC: Method Overloading]
     * Game Role: Standard damage entry point routing hits through active shields first.
     */
    public void takeDamage(int amount) {
        takeDamage(amount, false);
    }

    /**
     * [TOPIC: Method Overloading] [TOPIC: Encapsulation]
     * Game Role: Resolves shield-before-hull damage absorption and deactivates destroyed ships.
     */
    public void takeDamage(int amount, boolean bypassShield) {
        // WHY: Ignore non-positive hits or inactive craft so health invariants cannot be corrupted.
        if (amount <= 0 || !active) return;

        // WHY: Shield capacitor absorbs incoming damage before hull integrity takes overflow.
        if (!bypassShield && shield > 0) {
            if (shield >= amount) {
                shield -= amount;
                amount = 0;
            } else {
                amount -= shield;
                shield = 0;
            }
        }

        if (amount > 0) {
            // WHY: Clamp hull at 0 and deactivate ship on lethal damage for cleanup.
            health = Math.max(0, health - amount);
            if (health == 0) {
                active = false;
            }
        }
    }

    /**
     * [TOPIC: Encapsulation]
     * Game Role: Repairs hull points on living ships without exceeding maxHealth.
     */
    public void heal(int amount) {
        if (amount > 0 && health > 0) {
            health = Math.min(maxHealth, health + amount);
        }
    }

    /**
     * [TOPIC: Encapsulation]
     * Game Role: Recharges energy shields on living ships without exceeding maxShield.
     */
    public void rechargeShield(int amount) {
        if (amount > 0 && health > 0) {
            shield = Math.min(maxShield, shield + amount);
        }
    }

    /** [TOPIC: Encapsulation] Game Role: Verifies weapon fire-rate cooldown has elapsed. */
    public boolean canShoot() {
        return (System.currentTimeMillis() - lastShotTime) >= shootCooldownMs;
    }

    /** Game Role: Records discharge timestamp to enforce per-ship weapon cadence. */
    protected void recordShot() {
        this.lastShotTime = System.currentTimeMillis();
    }

    /**
     * [TOPIC: Abstraction] [TOPIC: Polymorphism]
     * Game Role: Spawns subtype-specific projectiles into the active world projectile list.
     */
    public abstract void shoot(List<Projectile> projectileList);

    public boolean isDestroyed() { return health <= 0 || !active; }

    public int getMaxHealth() { return maxHealth; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(maxHealth, health)); }

    public int getMaxShield() { return maxShield; }
    public int getShield() { return shield; }
    public void setShield(int shield) { this.shield = Math.max(0, Math.min(maxShield, shield)); }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public long getShootCooldownMs() { return shootCooldownMs; }
    public void setShootCooldownMs(long ms) { this.shootCooldownMs = ms; }
}

