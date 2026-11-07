package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Interceptor Fighter (PSOOP-CO1).
 * Demonstrates: Polymorphism and Dynamic Dispatch.
 *
 * Polymorphism & Dynamic Dispatch:
 * - Extends the abstract Enemy superclass and overrides update() and performAttack().
 * - When GameEngine invokes `e.performAttack(delta, projectiles, player)` and `e.update(delta)`
 *   via the superclass reference `Enemy e`, dynamic method dispatch at runtime resolves and
 *   executes Interceptor's specialized alignment-detection, dash kinematics, and staggered twin attacks.
 *
 * Distinct Behaviour:
 * - Actively scans for player horizontal alignment (|player.x - x| <= 75px).
 * - On alignment, triggers a high-speed forward dash with accelerated velocity.
 * - Discharges staggered twin micro-darts (left cannon fires, followed by right cannon with a stagger delay).
 */
public class InterceptorEnemy extends Enemy {

    private boolean dashing;
    private double dashTimer;
    private static final double DASH_DURATION = 0.85;
    private static final double NORMAL_VY = 110.0;
    private static final double DASH_VY = 260.0;

    // Staggered twin projectile state
    private boolean pendingStaggerShot;
    private double staggerTimer;
    private static final double STAGGER_DELAY = 0.14; // 140ms between left and right shots

    public InterceptorEnemy(double x, double y) {
        // x, y, width 38, height 40, maxHealth 45, maxShield 10, speed 170.0, shootCooldown 1100ms, scoreValue 200
        super(x, y, 38, 40, 45, 10, 170.0, 1100, 200);
        this.vy = NORMAL_VY;
        this.dashing = false;
        this.dashTimer = 0.0;
        this.pendingStaggerShot = false;
        this.staggerTimer = 0.0;
    }

    /**
     * Polymorphic kinematic update overridden from Entity/Enemy.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 3.0;

        // Dynamic dash state resolution
        if (dashing) {
            dashTimer -= delta;
            vy = DASH_VY;
            if (dashTimer <= 0.0) {
                dashing = false;
                vy = NORMAL_VY;
            }
        } else {
            vy = NORMAL_VY;
            // Agile serpentine banking
            x += Math.cos(flightTimer) * 55.0 * delta;
        }

        y += vy * delta;
    }

    /**
     * Polymorphic attack logic dynamically dispatched via Enemy superclass reference.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;

        // Process pending staggered twin shot
        if (pendingStaggerShot) {
            staggerTimer -= delta;
            if (staggerTimer <= 0.0) {
                pendingStaggerShot = false;
                // Fire second staggered micro-dart from right wing
                projectileList.add(new Projectile(x + width - 8, y + height - 6, 4, 12, 0, 360.0, false, 12));
            }
        }

        if (player == null || !player.isActive()) return;

        // Detect player alignment across horizontal combat plane
        double dx = Math.abs(player.getCenterX() - getCenterX());
        boolean isAligned = (dx <= 75.0);

        if (isAligned && y > 30 && y < 450) {
            // Initiate high-speed dash towards player corridor
            if (!dashing && attackTimer >= 1.0) {
                dashing = true;
                dashTimer = DASH_DURATION;
            }

            // Discard staggered volley when cooldown expires
            if (canShoot()) {
                recordShot();
                // 1. First shot discharged from left wing immediately
                projectileList.add(new Projectile(x + 4, y + height - 6, 4, 12, 0, 360.0, false, 12));
                // 2. Queue second shot for staggered delay
                pendingStaggerShot = true;
                staggerTimer = STAGGER_DELAY;
            }
        }
    }

    @Override
    public void shoot(List<Projectile> projectileList) {
        // Standard fallback firing implementation satisfying Ship contract
        projectileList.add(new Projectile(x + 4, y + height - 6, 4, 12, 0, 360.0, false, 12));
        projectileList.add(new Projectile(x + width - 8, y + height - 6, 4, 12, 0, 360.0, false, 12));
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Sleek forward-swept delta needle
        Polygon deltaHull = new Polygon();
        deltaHull.addPoint(px + 19, py + 40); // Forward needle nose
        deltaHull.addPoint(px + 38, py + 22); // Right razor wing
        deltaHull.addPoint(px + 32, py + 4);  // Right canard
        deltaHull.addPoint(px + 23, py);      // Right engine
        deltaHull.addPoint(px + 19, py + 10); // Center aft
        deltaHull.addPoint(px + 15, py);      // Left engine
        deltaHull.addPoint(px + 6, py + 4);   // Left canard
        deltaHull.addPoint(px, py + 22);      // Left razor wing

        // Dark teal hull body
        g.setColor(new Color(20, 60, 75));
        g.fillPolygon(deltaHull);

        // Luminous electric cyan trim (or blazing yellow during dash)
        if (dashing) {
            g.setColor(new Color(255, 230, 80));
            g.setStroke(new BasicStroke(2.2f));
        } else {
            g.setColor(new Color(50, 220, 255));
            g.setStroke(new BasicStroke(1.5f));
        }
        g.drawPolygon(deltaHull);

        // Thruster afterburner trail when dashing
        if (dashing) {
            g.setColor(new Color(255, 120, 30, 200));
            g.fillOval(px + 14, py - 8, 10, 12);
        }

        // Optic tracking sensor
        g.setColor(new Color(0, 255, 200));
        g.fillOval(px + 16, py + 18, 6, 6);

        // Shield aura
        if (shield > 0) {
            g.setColor(new Color(0, 255, 255, 60));
            g.drawOval(px - 3, py - 3, width + 6, height + 6);
        }
    }

    public boolean isDashing() {
        return dashing;
    }
}
