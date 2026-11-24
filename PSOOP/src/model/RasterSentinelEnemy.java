// ==============================================================================
// File      : PSOOP/src/model/RasterSentinelEnemy.java
// Subject   : PSOOP-CO1 / CGL-CO3 | Midpoint Circle & Raster Shield Sentinel
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Raster Algorithms] [TOPIC: Kinematics]
// Game Role : Renders a discrete 8-way symmetric Midpoint Circle shield lattice around a sentinel.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Bresenham Shield Sentinel plotting integer Midpoint Circle shield points with 8-way symmetry.
 */
public class RasterSentinelEnemy extends Enemy {

    private final int circleRadius = 26;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a shielded sentinel surrounded by a 26px Midpoint Circle barrier.
     */
    public RasterSentinelEnemy(double x, double y) {
        // x, y, width 46, height 46, maxHealth 95, maxShield 40, speed 75.0, shootCooldown 1700ms, scoreValue 430
        super(x, y, 46, 46, 95, 40, 75.0, 1700, 430);
        this.vy = speed * 0.7;
    }

    /** [TOPIC: Method Overriding] [TOPIC: Kinematics] Game Role: Integrates steady descent and sine weave. */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.4;
        y += vy * delta;
        x += Math.sin(flightTimer) * 18.0 * delta;
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires laser bolt inside active corridor. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Emits a central heavy downward beam. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 290.0;
        int damage = 16;
        // Central Bresenham laser beam
        projectileList.add(new Projectile(x + width / 2.0 - 4, y + height, 8, 18, 0, bulletSpeed, false, damage));
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Raster Algorithms]
     * Game Role: Draws Midpoint Circle shield lattice when shielded, plus diamond hull and vitals.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        int cx = px + width / 2;
        int cy = py + height / 2;

        // Render Midpoint Circle Shield using discrete pixel blocks (8-way octant symmetry)
        if (shield > 0) {
            g.setColor(new Color(0, 255, 120, 180));
            renderMidpointCirclePoints(g, cx, cy, circleRadius);
        }

        // Diamond sentinel hull
        int[] xs = { cx, px + width - 2, cx, px + 2 };
        int[] ys = { py + 2, cy, py + height - 2, cy };
        g.setColor(new Color(15, 40, 25));
        g.fillPolygon(xs, ys, 4);
        g.setColor(new Color(0, 255, 140));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(xs, ys, 4);

        // Bresenham label
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(0, 255, 140));
        g.drawString("BRESENHAM:LINE/CIRC", px - 8, py - 6);

        renderVitals(g, px, py);
    }

    /**
     * [TOPIC: Raster Algorithms]
     * Game Role: Plots discrete circle perimeter using integer decision updates and 8-way symmetry.
     */
    private void renderMidpointCirclePoints(Graphics2D g, int xc, int yc, int r) {
        int x = 0;
        int y = r;
        // WHY: Integer decision parameter d = 1 - r avoids trig/sqrt calls per pixel step.
        int d = 1 - r;
        drawCircleSymmetric(g, xc, yc, x, y);

        while (x < y) {
            x++;
            if (d < 0) {
                d += 2 * x + 1;
            } else {
                y--;
                d += 2 * (x - y) + 1;
            }
            drawCircleSymmetric(g, xc, yc, x, y);
        }
    }

    private void drawCircleSymmetric(Graphics2D g, int xc, int yc, int x, int y) {
        int s = 2; // Pixel block size
        g.fillRect(xc + x, yc + y, s, s);
        g.fillRect(xc - x, yc + y, s, s);
        g.fillRect(xc + x, yc - y, s, s);
        g.fillRect(xc - x, yc - y, s, s);
        g.fillRect(xc + y, yc + x, s, s);
        g.fillRect(xc - y, yc + x, s, s);
        g.fillRect(xc + y, yc - x, s, s);
        g.fillRect(xc - y, yc - x, s, s);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(0, 255, 140));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }
}
