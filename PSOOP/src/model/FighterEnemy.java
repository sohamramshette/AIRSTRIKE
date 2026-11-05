package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Standard combat fighter ship.
 * Medium health, equipped with front-mounted plasma lasers targeting player position.
 */
public class FighterEnemy extends Enemy {

    public FighterEnemy(double x, double y) {
        // width 42, height 44, health 60, shield 20, speed 115, cooldown 1200ms, score 250
        super(x, y, 42, 44, 60, 20, 115.0, 1200, 250);
        this.vy = speed * 0.75;
    }

    @Override
    public void update(double delta) {
        flightTimer += delta * 2.0;
        y += vy * delta;
        x += Math.cos(flightTimer) * 45.0 * delta; // Lateral banking
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
        double bulletSpeed = 340.0; // Moving downwards
        int damage = 15;

        // Dual downward laser cannons
        projectileList.add(new Projectile(x + 8, y + height, 5, 14, 0, bulletSpeed, false, damage));
        projectileList.add(new Projectile(x + width - 13, y + height, 5, 14, 0, bulletSpeed, false, damage));
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Swept-forward wing fighter
        Polygon hull = new Polygon();
        hull.addPoint(px + 21, py + 44); // Nose pointing down
        hull.addPoint(px + 38, py + 32); // Right wing tip
        hull.addPoint(px + 42, py + 12); // Right wing flank
        hull.addPoint(px + 28, py);      // Right engine
        hull.addPoint(px + 21, py + 10); // Center aft
        hull.addPoint(px + 14, py);      // Left engine
        hull.addPoint(px, py + 12);      // Left wing flank
        hull.addPoint(px + 4, py + 32);  // Left wing tip

        // Dark purple / crimson hull
        g.setColor(new Color(65, 20, 75));
        g.fillPolygon(hull);

        // Violet-magenta edge glow
        g.setColor(new Color(230, 40, 180));
        g.setStroke(new BasicStroke(1.6f));
        g.drawPolygon(hull);

        // Weapon pods
        g.setColor(new Color(255, 60, 60));
        g.fillRect(px + 6, py + 26, 4, 10);
        g.fillRect(px + width - 10, py + 26, 4, 10);

        // Cockpit window
        g.setColor(new Color(255, 190, 50));
        g.fillOval(px + 17, py + 18, 8, 12);

        // Shield aura if active
        if (shield > 0) {
            g.setColor(new Color(200, 50, 255, 75));
            g.drawOval(px - 4, py - 4, width + 8, height + 8);
        }
    }
}

