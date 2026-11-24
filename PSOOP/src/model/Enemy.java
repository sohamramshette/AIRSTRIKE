// ==============================================================================
// File      : PSOOP/src/model/Enemy.java
// Subject   : PSOOP-CO1 | Enemy Base Hierarchy & Polymorphic Combat
// Topics    : [TOPIC: Inheritance] [TOPIC: Abstraction] [TOPIC: Polymorphism]
// Game Role : Specializes Ship for hostile AI craft with score rewards and polymorphic attack dispatch.
// ==============================================================================
package model;

import java.util.List;

/**
 * Abstract base class for all hostile combat craft.
 */
public abstract class Enemy extends Ship {
    
    protected int scoreValue;
    protected double attackTimer;
    protected double flightTimer;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Initializes hostile ship parameters, score reward floor, and randomized wave phase.
     */
    public Enemy(double x, double y, int width, int height, int maxHealth, int maxShield, double speed, long shootCooldownMs, int scoreValue) {
        super(x, y, width, height, maxHealth, maxShield, speed, shootCooldownMs);
        this.scoreValue = Math.max(10, scoreValue);
        this.attackTimer = 0.0;
        // WHY: Randomizes initial phase angle so simultaneously spawned enemies do not sway in lockstep.
        this.flightTimer = Math.random() * Math.PI * 2;
    }

    /**
     * [TOPIC: Abstraction] [TOPIC: Polymorphism]
     * Game Role: Invoked every game tick to execute subtype-specific targeting and firing behavior.
     */
    public abstract void performAttack(double delta, List<Projectile> projectileList, Player player);

    /**
     * [TOPIC: Kinematics]
     * Game Role: Flags enemies that have descended past the visible bottom arena edge for cleanup.
     */
    public boolean isOffScreen(int screenHeight) {
        // WHY: 50px grace buffer prevents despawning sprites while their tail is still partially visible.
        return (y > screenHeight + 50);
    }

    public int getScoreValue() { return scoreValue; }
}

