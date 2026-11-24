// ==============================================================================
// File      : PSOOP/src/model/FailSafeEnemy.java
// Subject   : PSOOP-CO3 | Defensive Programming & Fail-Safe Barrier
// Topics    : [TOPIC: Defensive Programming] [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Encapsulation]
// Game Role : Guards health/delta invariants and activates a one-time barrier below 30% HP.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Fail-Safe Sentry demonstrating invariant checks, boundary clamping, and one-shot recovery.
 */
public class FailSafeEnemy extends Enemy {

    private static final double CRITICAL_HEALTH_RATIO = 0.30;
    private boolean emergencyBarrierTriggered;
    private boolean emergencyBarrierActive;
    private double emergencyBarrierTimer;
    private static final double BARRIER_DURATION = 3.0; // 3.0 second emergency safety barrier

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a bastion sentry with unlatched emergency barrier state.
     */
    public FailSafeEnemy(double x, double y) {
        // x, y, width 40, height 42, maxHealth 70, maxShield 15, speed 85.0, shootCooldown 1400ms, scoreValue 280
        super(x, y, 40, 42, 70, 15, 85.0, 1400, 280);
        this.emergencyBarrierTriggered = false;
        this.emergencyBarrierActive = false;
        this.emergencyBarrierTimer = 0.0;
        this.vy = speed * 0.9;
    }

    /**
     * [TOPIC: Defensive Programming] [TOPIC: Method Overriding]
     * Game Role: Protects health invariants, halves barrier damage, and triggers emergency shield at <= 30% HP.
     */
    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        // WHY: Reject invalid negative/zero damage or hits after destruction to protect invariants.
        if (amount <= 0 || !active) return;

        // WHY: Active emergency barrier absorbs hits directly at 50% reduced shield drain while protecting hull.
        if (emergencyBarrierActive && !bypassShield) {
            if (shield > 0) {
                shield = Math.max(0, shield - (amount / 2));
            }
            return;
        }

        // Standard damage resolution with defensive clamping
        int remaining = amount;
        if (!bypassShield && shield > 0) {
            if (shield >= remaining) {
                shield -= remaining;
                remaining = 0;
            } else {
                remaining -= shield;
                shield = 0;
            }
        }

        if (remaining > 0) {
            health = Math.max(0, health - remaining);
            if (health == 0) {
                active = false;
                emergencyBarrierActive = false;
                return;
            }

            // WHY: Latched boolean guard guarantees the emergency barrier triggers at most once per craft.
            int threshold = (int) Math.ceil(maxHealth * CRITICAL_HEALTH_RATIO);
            if (health <= threshold && !emergencyBarrierTriggered) {
                activateEmergencyBarrier();
            }
        }
    }

    /**
     * [TOPIC: Defensive Programming]
     * Game Role: Latches the single-use emergency barrier and raises shield to at least 35.
     */
    private void activateEmergencyBarrier() {
        emergencyBarrierTriggered = true;
        emergencyBarrierActive = true;
        emergencyBarrierTimer = BARRIER_DURATION;

        // WHY: Guarantees a minimum 35-point barrier buffer without lowering any higher existing shield.
        this.shield = Math.max(this.shield, 35);
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Defensive Programming] [TOPIC: Kinematics]
     * Game Role: Validates frame delta, clamps horizontal bounds, and ticks down barrier duration.
     */
    @Override
    public void update(double delta) {
        // WHY: Rejects NaN or non-positive delta ticks so coordinates never corrupt into NaN.
        if (delta <= 0.0 || Double.isNaN(delta)) return;

        flightTimer += delta * 1.8;
        y += vy * delta;

        // Smooth defensive weave
        x += Math.sin(flightTimer) * 35.0 * delta;

        // WHY: Defensive boundary clamp prevents lateral weave from pushing the sentry off-screen.
        if (x < 10) x = 10;
        if (x > 750) x = 750;

        // Update emergency barrier timer
        if (emergencyBarrierActive) {
            emergencyBarrierTimer -= delta;
            if (emergencyBarrierTimer <= 0.0) {
                emergencyBarrierActive = false;
            }
        }
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires twin pulses within active vertical lane. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 20 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Launches twin slightly diverging containment pulses. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 300.0;
        int damage = 16;

        // Heavy twin defensive containment pulses
        projectileList.add(new Projectile(x + 6, y + height - 4, 6, 14, -15.0, bulletSpeed, false, damage));
        projectileList.add(new Projectile(x + width - 12, y + height - 4, 6, 14, 15.0, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Renders fortified emerald bastion and pulsing emergency barrier. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Fortified diamond bastion polygon
        Polygon bastion = new Polygon();
        bastion.addPoint(px + 20, py + 42); // Bottom apex
        bastion.addPoint(px + 40, py + 26); // Right flank
        bastion.addPoint(px + 34, py + 6);  // Right shoulder
        bastion.addPoint(px + 20, py);      // Top apex
        bastion.addPoint(px + 6, py + 6);   // Left shoulder
        bastion.addPoint(px, py + 26);      // Left flank

        // Dark emerald / obsidian armor
        g.setColor(new Color(20, 50, 40));
        g.fillPolygon(bastion);

        // Bright emerald structural edge
        g.setColor(new Color(0, 230, 140));
        g.setStroke(new BasicStroke(1.8f));
        g.drawPolygon(bastion);

        // Fail-safe core sensor
        g.setColor(emergencyBarrierActive ? new Color(255, 240, 80) : new Color(0, 255, 180));
        g.fillOval(px + 15, py + 16, 10, 10);

        // Emergency Barrier visual projection
        if (emergencyBarrierActive) {
            float pulse = (float) (0.6 + 0.4 * Math.sin(System.currentTimeMillis() * 0.015));
            g.setColor(new Color(0, 255, 200, (int) (100 * pulse)));
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval(px - 8, py - 8, width + 16, height + 16);
            g.setColor(new Color(0, 255, 180, (int) (40 * pulse)));
            g.fillOval(px - 8, py - 8, width + 16, height + 16);
        } else if (shield > 0) {
            g.setColor(new Color(0, 220, 180, 60));
            g.drawOval(px - 3, py - 3, width + 6, height + 6);
        }
    }

    public boolean isEmergencyBarrierActive() {
        return emergencyBarrierActive;
    }

    public boolean isEmergencyBarrierTriggered() {
        return emergencyBarrierTriggered;
    }
}
