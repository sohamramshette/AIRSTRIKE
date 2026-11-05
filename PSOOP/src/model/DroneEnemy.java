package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Fast, lightweight scout craft.
 * Low health, high speed, performs rapid reconnaissance sweeps.
 */
public class DroneEnemy extends Enemy {

    private final double initialX;

    public DroneEnemy(double x, double y) {
        // width 32, height 32, health 30, shield 0, speed 200, cooldown 9999, score 100
        super(x, y, 32, 32, 30, 0, 200.0, 9999, 100);
        this.initialX = x;
        this.vy = speed;
    }

    @Override
    public void update(double delta) {
        flightTimer += delta * 4.5;
        y += vy * delta;
        x = initialX + Math.sin(flightTimer) * 35.0; // Rapid sine weave
    }

    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        // Drones do not shoot; they attack via ramming trajectory
    }

    @Override
    public void shoot(List<Projectile> projectileList) {
        // No projectile firing
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Angular dart shape pointing downwards
        Polygon dart = new Polygon();
        dart.addPoint(px + 16, py + 32); // Nose pointing down
        dart.addPoint(px + 32, py + 8);  // Right wing
        dart.addPoint(px + 24, py);      // Right aft
        dart.addPoint(px + 16, py + 6);  // Center engine
        dart.addPoint(px + 8, py);       // Left aft
        dart.addPoint(px, py + 8);       // Left wing

        // Red/Orange metallic body
        g.setColor(new Color(140, 25, 30));
        g.fillPolygon(dart);

        // Neon orange edge highlights
        g.setColor(new Color(255, 90, 40));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(dart);

        // Glowing red optic sensor
        g.setColor(new Color(255, 40, 40));
        g.fillOval(px + 13, py + 16, 6, 6);
        g.setColor(Color.WHITE);
        g.fillOval(px + 15, py + 18, 2, 2);
    }
}

