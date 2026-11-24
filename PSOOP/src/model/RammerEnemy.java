// ==============================================================================
// File      : PSOOP/src/model/RammerEnemy.java
// Subject   : PSOOP-CO4 | Delta-Time Acceleration & Kinetic Ramming
// Topics    : [TOPIC: Kinematics] [TOPIC: Method Overriding] [TOPIC: Inheritance] [TOPIC: Defensive Programming]
// Game Role : Accelerates along a locked player corridor to inflict heavy kinetic collision damage.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Kinetic Battering Ram modeling non-uniform delta-time acceleration and ramming lock-on.
 */
public class RammerEnemy extends Enemy {

    // Encapsulated kinematics & collision state
    private double currentAcceleration;
    private final double maxRamSpeed;
    private final int prowImpactDamage;
    private boolean charging;
    private double targetLockX;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a heavy-prow rammer configured with acceleration and max-speed limits.
     */
    public RammerEnemy(double x, double y) {
        // x, y, width 38, height 46, maxHealth 85, maxShield 0, speed 70.0, shootCooldown 99999ms, scoreValue 320
        super(x, y, 38, 46, 85, 0, 70.0, 99999, 320);
        this.currentAcceleration = 260.0;
        this.maxRamSpeed = 380.0;
        this.prowImpactDamage = 45;
        this.charging = false;
        this.targetLockX = x;
        this.vy = speed;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Defensive Programming] [TOPIC: Kinematics]
     * Game Role: Validates frame delta before delegating Euler acceleration integration.
     */
    @Override
    public void update(double delta) {
        // WHY: Reject invalid/NaN time steps so acceleration never corrupts velocity or position.
        if (delta <= 0.0 || Double.isNaN(delta)) return;

        applyKineticKinematics(delta);
    }

    /**
     * [TOPIC: Kinematics]
     * Game Role: Integrates capped forward acceleration and corridor homing while charging, else gentle sweep.
     */
    public void applyKineticKinematics(double delta) {
        if (charging) {
            // WHY: Semi-implicit Euler velocity step capped at maxRamSpeed keeps ramming velocity bounded.
            vy = Math.min(maxRamSpeed, vy + currentAcceleration * delta);

            // WHY: Bounded lateral trim steers smoothly onto targetLockX without snapping.
            double dx = targetLockX - getCenterX();
            x += Math.signum(dx) * Math.min(Math.abs(dx), 80.0 * delta);
        } else {
            // Steady prow sweep
            flightTimer += delta * 2.0;
            x += Math.sin(flightTimer) * 30.0 * delta;
        }

        y += vy * delta;
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: Method Overriding]
     * Game Role: Locks onto player X and ignites ramming thrusters once player enters 110px forward lane.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        if (player == null || !player.isActive()) return;

        // WHY: Trigger only while Rammer is above the player (y < playerY - 40) so it never charges backward.
        double dx = Math.abs(player.getCenterX() - getCenterX());
        if (!charging && dx <= 110.0 && y < player.getCenterY() - 40.0) {
            lockAndCharge(player.getCenterX());
        }
    }

    /**
     * [TOPIC: Encapsulation]
     * Game Role: Latches charging mode and captures the horizontal ramming target coordinate.
     */
    public void lockAndCharge(double targetX) {
        this.charging = true;
        this.targetLockX = targetX;
    }

    /**
     * [TOPIC: Method Overriding]
     * Game Role: Intentional no-op because Rammers channel all output into hull-to-hull impact.
     */
    @Override
    public void shoot(List<Projectile> projectileList) {
        // WHY: No projectile weapons; threat comes strictly from prowImpactDamage on collision.
    }

    /** [TOPIC: Method Overriding] Game Role: Renders armored tungsten wedge, chevrons, and charge thrusters. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Heavy tungsten battering ram arrowhead
        Polygon ramHull = new Polygon();
        ramHull.addPoint(px + 19, py + 46); // Reinforced battering prow tip
        ramHull.addPoint(px + 38, py + 26); // Right wedge shoulder
        ramHull.addPoint(px + 32, py);      // Right heavy thruster
        ramHull.addPoint(px + 19, py + 8);  // Center spine notch
        ramHull.addPoint(px + 6, py);       // Left heavy thruster
        ramHull.addPoint(px, py + 26);      // Left wedge shoulder

        // Dark industrial iron body
        g.setColor(new Color(45, 45, 50));
        g.fillPolygon(ramHull);

        // Hazard warning yellow/orange prow trim
        g.setColor(charging ? new Color(255, 60, 20) : new Color(255, 170, 0));
        g.setStroke(new BasicStroke(2.2f));
        g.drawPolygon(ramHull);

        // Heavy chevron stripes on prow
        g.setColor(new Color(255, 200, 30));
        g.drawLine(px + 12, py + 22, px + 19, py + 30);
        g.drawLine(px + 19, py + 30, px + 26, py + 22);

        // Thruster exhaust plumes (blazing when charging)
        if (charging) {
            g.setColor(new Color(255, 90, 0, 220));
            g.fillOval(px + 13, py - 12, 12, 16);
            g.setColor(new Color(255, 240, 100));
            g.fillOval(px + 15, py - 8, 8, 10);
        } else {
            g.setColor(new Color(255, 120, 30, 160));
            g.fillOval(px + 15, py - 6, 8, 8);
        }
    }

    public boolean isCharging() {
        return charging;
    }

    public int getProwImpactDamage() {
        return prowImpactDamage;
    }

    public double getMaxRamSpeed() {
        return maxRamSpeed;
    }
}
