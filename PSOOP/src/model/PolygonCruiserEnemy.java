// ==============================================================================
// File      : PSOOP/src/model/PolygonCruiserEnemy.java
// Subject   : PSOOP-CO1 / CGL-CO1 | Multi-Primitive CCW Polygon Cruiser
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Graphics Primitives] [TOPIC: Kinematics]
// Game Role : Renders a counter-clockwise polygon fuselage and delta wing fans firing twin lasers.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Geometric Polygon Cruiser modeling CCW polygon winding and composite triangle-fan wings.
 */
public class PolygonCruiserEnemy extends Enemy {

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a shielded cruiser descending at 70% speed.
     */
    public PolygonCruiserEnemy(double x, double y) {
        // x, y, width 50, height 46, maxHealth 90, maxShield 30, speed 85.0, shootCooldown 1650ms, scoreValue 410
        super(x, y, 50, 46, 90, 30, 85.0, 1650, 410);
        this.vy = speed * 0.7;
    }

    /** [TOPIC: Method Overriding] [TOPIC: Kinematics] Game Role: Integrates steady descent and sine sway. */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.6;
        y += vy * delta;
        x += Math.sin(flightTimer) * 24.0 * delta;
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires twin wing bolts within active lane. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Emits slightly diverging bolts from left and right wings. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 285.0;
        int damage = 15;
        // Dual swept wing firing points
        projectileList.add(new Projectile(x + 4, y + height - 10, 6, 14, -12, bulletSpeed, false, damage));
        projectileList.add(new Projectile(x + width - 10, y + height - 10, 6, 14, 12, bulletSpeed, false, damage));
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Graphics Primitives]
     * Game Role: Draws CCW diamond fuselage, delta triangle wings, and GL_POLY:CCW indicator.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // WHY: Vertices follow counter-clockwise order matching OpenGL front-face culling conventions.
        int[] xs = { px + width / 2, px + width - 2, px + width / 2, px + 2 };
        int[] ys = { py + height, py + height / 2, py + 2, py + height / 2 };
        Polygon fuselage = new Polygon(xs, ys, 4);

        g.setColor(new Color(20, 35, 60));
        g.fillPolygon(fuselage);
        g.setColor(new Color(0, 255, 200));
        g.setStroke(new BasicStroke(1.8f));
        g.drawPolygon(fuselage);

        // Triangle Fan Delta Wings (Left & Right)
        int[] wingL_X = { px + width / 2, px + 2, px + 10 };
        int[] wingL_Y = { py + height / 2, py + height - 6, py + 8 };
        g.setColor(new Color(10, 80, 100, 160));
        g.fillPolygon(wingL_X, wingL_Y, 3);
        g.setColor(new Color(0, 200, 255));
        g.drawPolygon(wingL_X, wingL_Y, 3);

        int[] wingR_X = { px + width / 2, px + width - 2, px + width - 10 };
        int[] wingR_Y = { py + height / 2, py + height - 6, py + 8 };
        g.setColor(new Color(10, 80, 100, 160));
        g.fillPolygon(wingR_X, wingR_Y, 3);
        g.setColor(new Color(0, 200, 255));
        g.drawPolygon(wingR_X, wingR_Y, 3);

        // Primitive CCW indicator badge
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(0, 255, 220));
        g.drawString("GL_POLY:CCW", px - 2, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(0, 255, 200));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }
}
