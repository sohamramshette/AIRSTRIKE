package model;

import java.util.List;

/**
 * Abstract base class for all hostile combat craft.
 * Demonstrates Inheritance from Ship and Polymorphic attack behaviors.
 */
public abstract class Enemy extends Ship {
    
    protected int scoreValue;
    protected double attackTimer;
    protected double flightTimer;

    public Enemy(double x, double y, int width, int height, int maxHealth, int maxShield, double speed, long shootCooldownMs, int scoreValue) {
        super(x, y, width, height, maxHealth, maxShield, speed, shootCooldownMs);
        this.scoreValue = Math.max(10, scoreValue);
        this.attackTimer = 0.0;
        this.flightTimer = Math.random() * Math.PI * 2;
    }

    /**
     * Polymorphic attack logic executed on every game tick.
     */
    public abstract void performAttack(double delta, List<Projectile> projectileList, Player player);

    /**
     * Checks if the enemy has traversed below the screen viewport.
     */
    public boolean isOffScreen(int screenHeight) {
        return (y > screenHeight + 50);
    }

    public int getScoreValue() { return scoreValue; }
}

