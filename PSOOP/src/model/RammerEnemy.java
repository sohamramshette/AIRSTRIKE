package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Kinetic Battering Ram (PSOOP-CO4).
 * Demonstrates: Logic-Building for Real-Life Problems via Kinematics Simulation and OOP Collision Response.
 *
 * OOP & Problem-Solving Design:
 * - Encapsulates mass-inertia parameters, acceleration rates, and prow durability.
 * - Simulates real-time 60 FPS non-uniform accelerating motion: v(t + dt) = min(v_max, v(t) + a * dt).
 * - Implements controlled class methods (`lockAndCharge`, `applyKineticKinematics`, `resolveKineticImpact`)
 *   to decouple physics simulation and collision response from game loop coordinators.
 */
public class RammerEnemy extends Enemy {

    // Encapsulated kinematics & collision state
    private double currentAcceleration;
    private final double maxRamSpeed;
    private final int prowImpactDamage;
    private boolean charging;
    private double targetLockX;

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
     * Executes real-time kinematics integration over delta time (PSOOP-CO4).
     */
    @Override
    public void update(double delta) {
        if (delta <= 0.0 || Double.isNaN(delta)) return;

        applyKineticKinematics(delta);
    }

    /**
     * Controlled class method updating kinematics simulation.
     */
    public void applyKineticKinematics(double delta) {
        if (charging) {
            // Non-uniform acceleration: v_y += a * dt
            vy = Math.min(maxRamSpeed, vy + currentAcceleration * delta);

            // Subtle homing trim towards locked corridor
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
     * Detects player target alignment and triggers kinetic thrust ignition.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        if (player == null || !player.isActive()) return;

        // Detect player presence in forward ramming corridor
        double dx = Math.abs(player.getCenterX() - getCenterX());
        if (!charging && dx <= 110.0 && y < player.getCenterY() - 40.0) {
            lockAndCharge(player.getCenterX());
        }
    }

    /**
     * Controlled method to engage prow thrusters.
     */
    public void lockAndCharge(double targetX) {
        this.charging = true;
        this.targetLockX = targetX;
    }

    /**
     * Rammers do not fire projectiles; all reactor energy is channeled into kinetic acceleration.
     */
    @Override
    public void shoot(List<Projectile> projectileList) {
        // No projectile weapons
    }

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
