package model;

import java.util.List;

/**
 * Abstract class representing a spacecraft (Player or Enemy) equipped with shields and weapons.
 * Demonstrates Inheritance, Method Overloading, and Protected State Encapsulation.
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
     * Constructor for Ship initialization.
     */
    public Ship(double x, double y, int width, int height, int maxHealth, int maxShield, double speed, long shootCooldownMs) {
        super(x, y, width, height);
        this.maxHealth = Math.max(1, maxHealth);
        this.health = this.maxHealth;
        this.maxShield = Math.max(0, maxShield);
        this.shield = this.maxShield;
        this.speed = Math.max(0.0, speed);
        this.shootCooldownMs = Math.max(50, shootCooldownMs);
        this.lastShotTime = 0;
    }

    /**
     * Method Overloading #1: Standard damage absorption (Shields absorb damage first).
     * @param amount Damage amount to inflict.
     */
    public void takeDamage(int amount) {
        takeDamage(amount, false);
    }

    /**
     * Method Overloading #2: Damage with option to bypass shielding.
     * @param amount Damage amount to inflict.
     * @param bypassShield Whether shields are ignored.
     */
    public void takeDamage(int amount, boolean bypassShield) {
        if (amount <= 0 || !active) return;

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
            health = Math.max(0, health - amount);
            if (health == 0) {
                active = false;
            }
        }
    }

    /**
     * Restores health up to maximum limit.
     */
    public void heal(int amount) {
        if (amount > 0 && health > 0) {
            health = Math.min(maxHealth, health + amount);
        }
    }

    /**
     * Recharges shield up to maximum limit.
     */
    public void rechargeShield(int amount) {
        if (amount > 0 && health > 0) {
            shield = Math.min(maxShield, shield + amount);
        }
    }

    /**
     * Checks if weapon cooldown has expired.
     */
    public boolean canShoot() {
        return (System.currentTimeMillis() - lastShotTime) >= shootCooldownMs;
    }

    /**
     * Records the timestamp of weapon discharge.
     */
    protected void recordShot() {
        this.lastShotTime = System.currentTimeMillis();
    }

    /**
     * Abstract firing behavior implemented specifically by Player and individual Enemy subtypes.
     */
    public abstract void shoot(List<Projectile> projectileList);

    public boolean isDestroyed() { return health <= 0 || !active; }

    // Getters and Setters
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

