// ==============================================================================
// File      : PSOOP/src/model/FibonacciSkimmerEnemy.java
// Subject   : PSOOP-CO1 / COA-CO2 | Fibonacci Sequence Skimmer
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Mathematical Series] [TOPIC: Kinematics]
// Game Role : Drives lateral wave amplitude and burst shot counts using Fibonacci terms.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Fibonacci Trajectory Skimmer driving sway amplitude and burst count from {1,1,2,3,5,8,13}.
 */
public class FibonacciSkimmerEnemy extends Enemy {

    private static final int[] FIB = { 1, 1, 2, 3, 5, 8, 13 };
    private int fibIndex = 0;
    private double fibStepTimer = 0.0;
    private double currentWaveAmplitude = 18.0;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a golden skimmer starting at index 0 of the Fibonacci table.
     */
    public FibonacciSkimmerEnemy(double x, double y) {
        // x, y, width 40, height 40, maxHealth 75, maxShield 20, speed 110.0, shootCooldown 1500ms, scoreValue 370
        super(x, y, 40, 40, 75, 20, 110.0, 1500, 370);
        this.vy = speed * 0.75;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Mathematical Series] [TOPIC: Kinematics]
     * Game Role: Steps through FIB[] every 0.7s to expand lateral sine amplitude smoothly.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 2.2;
        y += vy * delta;

        // WHY: Cycling FIB[] every 0.7s varies weave width unpredictably without unbounded growth.
        fibStepTimer += delta;
        if (fibStepTimer >= 0.7) {
            fibStepTimer = 0.0;
            fibIndex = (fibIndex + 1) % FIB.length;
            currentWaveAmplitude = 10.0 + (FIB[fibIndex] * 3.5);
        }

        x += Math.sin(flightTimer) * currentWaveAmplitude * delta;
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Triggers Fibonacci bursts in lane. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Mathematical Series]
     * Game Role: Fires 1-3 fanned rounds matching the current Fibonacci tier.
     */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 295.0;
        int damage = 13;

        // WHY: Caps burst count at 3 and centers spread indices symmetrically around 0.
        int burstCount = Math.min(3, FIB[fibIndex % 4]);
        for (int i = 0; i < burstCount; i++) {
            double angle = (i - (burstCount - 1) / 2.0) * 14.0;
            projectileList.add(new Projectile(x + width / 2.0 - 3, y + height, 6, 14, angle, bulletSpeed, false, damage));
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Renders golden needle hull and FIB(i) HUD readout. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Sleek golden needle geometry
        int[] xs = { px + width / 2, px + width - 3, px + width / 2 + 4, px + width / 2 - 4, px + 3 };
        int[] ys = { py + height, py + 6, py + 14, py + 14, py + 6 };
        g.setColor(new Color(55, 45, 10));
        g.fillPolygon(xs, ys, 5);
        g.setColor(new Color(255, 215, 0));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(xs, ys, 5);

        // Fibonacci HUD badge
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(255, 220, 100));
        g.drawString("FIB(" + fibIndex + ")=" + FIB[fibIndex], px - 2, py - 5);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(255, 215, 0));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }

    public int getFibValue() { return FIB[fibIndex]; }
}
