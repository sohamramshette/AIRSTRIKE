// ==============================================================================
// File      : PSOOP/src/model/OrbitalRotorEnemy.java
// Subject   : PSOOP-CO1 / CGL-CO2 | Affine Transform Orbital Drone
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Affine Transformations] [TOPIC: Kinematics]
// Game Role : Revolves two orbital weapon pods around a pulsing core and fires from live pod coords.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Orbital Rotor Drone demonstrating hierarchical translation, rotation, and scale transforms.
 */
public class OrbitalRotorEnemy extends Enemy {

    private double rotorAngle = 0.0;
    private static final double ORBIT_RADIUS = 28.0;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a rotor drone whose weapon pods orbit at ORBIT_RADIUS=28px.
     */
    public OrbitalRotorEnemy(double x, double y) {
        // x, y, width 52, height 52, maxHealth 85, maxShield 25, speed 80.0, shootCooldown 1750ms, scoreValue 400
        super(x, y, 52, 52, 85, 25, 80.0, 1750, 400);
        this.vy = speed * 0.7;
    }

    /** [TOPIC: Method Overriding] [TOPIC: Kinematics] Game Role: Integrates descent and pod angular velocity. */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.5;
        rotorAngle += delta * 3.2; // Rapid pod orbit
        y += vy * delta;
        x += Math.sin(flightTimer) * 20.0 * delta;
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires from orbital pods inside active lane. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Affine Transformations]
     * Game Role: Spawns bolts directly at the two rotated pod coordinates (rotorAngle and +PI).
     */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 280.0;
        int damage = 14;

        // WHY: Polar rotation about ship center (cx, cy) ensures bolts originate from actual pod locations.
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double p1x = cx + Math.cos(rotorAngle) * ORBIT_RADIUS;
        double p1y = cy + Math.sin(rotorAngle) * ORBIT_RADIUS;
        double p2x = cx + Math.cos(rotorAngle + Math.PI) * ORBIT_RADIUS;
        double p2y = cy + Math.sin(rotorAngle + Math.PI) * ORBIT_RADIUS;

        projectileList.add(new Projectile(p1x - 3, p1y, 6, 12, 0, bulletSpeed, false, damage));
        projectileList.add(new Projectile(p2x - 3, p2y, 6, 12, 0, bulletSpeed, false, damage));
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Affine Transformations]
     * Game Role: Draws pulsing core scale S(t), orbit guide, and twin orbiting pods.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        int cx = px + width / 2;
        int cy = py + height / 2;

        // Core pulsating chassis
        double scale = 1.0 + 0.12 * Math.sin(flightTimer * 3.0);
        int coreR = (int) (14 * scale);
        g.setColor(new Color(20, 50, 60));
        g.fillOval(cx - coreR, cy - coreR, coreR * 2, coreR * 2);
        g.setColor(new Color(0, 240, 255));
        g.setStroke(new BasicStroke(1.6f));
        g.drawOval(cx - coreR, cy - coreR, coreR * 2, coreR * 2);

        // Orbit ring guide
        g.setColor(new Color(0, 200, 255, 60));
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{2, 4}, 0));
        g.drawOval(cx - (int) ORBIT_RADIUS, cy - (int) ORBIT_RADIUS, (int) ORBIT_RADIUS * 2, (int) ORBIT_RADIUS * 2);

        // Render Detached Orbital Weapon Pods (Affine matrix transform: T * R)
        int pod1X = cx + (int) (Math.cos(rotorAngle) * ORBIT_RADIUS);
        int pod1Y = cy + (int) (Math.sin(rotorAngle) * ORBIT_RADIUS);
        int pod2X = cx + (int) (Math.cos(rotorAngle + Math.PI) * ORBIT_RADIUS);
        int pod2Y = cy + (int) (Math.sin(rotorAngle + Math.PI) * ORBIT_RADIUS);

        g.setColor(new Color(255, 100, 0));
        g.fillOval(pod1X - 5, pod1Y - 5, 10, 10);
        g.fillOval(pod2X - 5, pod2Y - 5, 10, 10);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(pod1X - 5, pod1Y - 5, 10, 10);
        g.drawOval(pod2X - 5, pod2Y - 5, 10, 10);

        // HUD Transform label
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(0, 240, 255));
        g.drawString("MATRIX:T*R", px - 2, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(0, 240, 255));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }
}
