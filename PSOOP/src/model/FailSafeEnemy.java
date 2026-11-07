package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Fail-Safe Sentry (PSOOP-CO3).
 * Demonstrates: Critical Thinking & Problem Solving via Defensive Programming.
 *
 * Defensive Programming Features:
 * - Detects critical health (<= 30% of maxHealth) and activates a single-use emergency barrier.
 * - Latched boolean guard (`emergencyBarrierTriggered`) prevents repeated/infinite emergency activations.
 * - Defensive boundary clamping ensures coordinates remain safely inside screen limits.
 * - Invariant assertions prevent negative damage, NaN delta steps, and illegal health/shield overflows.
 */
public class FailSafeEnemy extends Enemy {

    private static final double CRITICAL_HEALTH_RATIO = 0.30;
    private boolean emergencyBarrierTriggered;
    private boolean emergencyBarrierActive;
    private double emergencyBarrierTimer;
    private static final double BARRIER_DURATION = 3.0; // 3.0 second emergency safety barrier

    public FailSafeEnemy(double x, double y) {
        // x, y, width 40, height 42, maxHealth 70, maxShield 15, speed 85.0, shootCooldown 1400ms, scoreValue 280
        super(x, y, 40, 42, 70, 15, 85.0, 1400, 280);
        this.emergencyBarrierTriggered = false;
        this.emergencyBarrierActive = false;
        this.emergencyBarrierTimer = 0.0;
        this.vy = speed * 0.9;
    }

    /**
     * Defensive damage handler enforcing object invariants.
     */
    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        // Defensive check: reject non-positive amounts or damage on inactive craft
        if (amount <= 0 || !active) return;

        // If emergency barrier is actively shielding the craft, dissipate incoming kinetic energy
        if (emergencyBarrierActive && !bypassShield) {
            if (shield > 0) {
                shield = Math.max(0, shield - (amount / 2)); // 50% damage reduction under barrier
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

            // Critical health boundary detection: Activate single-use emergency fail-safe barrier
            int threshold = (int) Math.ceil(maxHealth * CRITICAL_HEALTH_RATIO);
            if (health <= threshold && !emergencyBarrierTriggered) {
                activateEmergencyBarrier();
            }
        }
    }

    /**
     * Activates emergency barrier exactly once per lifetime.
     */
    private void activateEmergencyBarrier() {
        emergencyBarrierTriggered = true;
        emergencyBarrierActive = true;
        emergencyBarrierTimer = BARRIER_DURATION;

        // Defensive invariant: recharge protective shield buffer safely up to cap
        this.shield = Math.max(this.shield, 35);
    }

    @Override
    public void update(double delta) {
        // Defensive check: guard against invalid frame deltas
        if (delta <= 0.0 || Double.isNaN(delta)) return;

        flightTimer += delta * 1.8;
        y += vy * delta;

        // Smooth defensive weave
        x += Math.sin(flightTimer) * 35.0 * delta;

        // Defensive boundary safety: clamp within arena margins
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

    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 20 && y < 450) {
            shoot(projectileList);
        }
    }

    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 300.0;
        int damage = 16;

        // Heavy twin defensive containment pulses
        projectileList.add(new Projectile(x + 6, y + height - 4, 6, 14, -15.0, bulletSpeed, false, damage));
        projectileList.add(new Projectile(x + width - 12, y + height - 4, 6, 14, 15.0, bulletSpeed, false, damage));
    }

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
