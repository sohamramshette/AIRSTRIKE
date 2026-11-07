package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Armoured Vanguard Heavy Fighter (PSOOP-CO1).
 * Demonstrates: Encapsulation and Inheritance.
 *
 * Encapsulation:
 * - Maintains private armorRating and health fields.
 * - Provides controlled damage-handling methods ensuring state integrity.
 * - Armor absorbs a flat portion of incoming damage after shields deplete,
 *   guaranteeing at least 1 point of effective damage if incoming damage > 0.
 *
 * Inheritance:
 * - Inherits from Enemy -> Ship -> Entity hierarchy.
 * - Overrides takeDamage() to provide specialized defense absorption while preserving the superclass contract.
 */
public class ArmouredEnemy extends Enemy {

    // Encapsulated state: private armor rating
    private int armorRating;

    public ArmouredEnemy(double x, double y) {
        // x, y, width 46, height 44, maxHealth 90, maxShield 30, speed 75.0, shootCooldown 1600ms, scoreValue 350
        super(x, y, 46, 44, 90, 30, 75.0, 1600, 350);
        this.armorRating = 8;
        this.vy = speed * 0.7; // Slow steady forward crawl
    }

    /**
     * Controlled damage handling method demonstrating encapsulation.
     * Enforces combat rule: Shield absorbs first, then armour mitigates remaining damage
     * before hull health is reduced.
     *
     * @param amount Incoming raw damage amount.
     * @param bypassShield Whether shields are bypassed.
     */
    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        if (amount <= 0 || !active) return;

        int remaining = amount;

        // 1. Shield absorption layer (inherited from Ship mechanics)
        if (!bypassShield && shield > 0) {
            if (shield >= remaining) {
                shield -= remaining;
                remaining = 0;
            } else {
                remaining -= shield;
                shield = 0;
            }
        }

        // 2. Encapsulated Armour mitigation layer
        if (remaining > 0) {
            int effectiveDamage = Math.max(1, remaining - armorRating);
            health = Math.max(0, health - effectiveDamage);
            if (health == 0) {
                active = false;
            }
        }
    }

    @Override
    public void update(double delta) {
        flightTimer += delta * 1.5;
        y += vy * delta;
        x += Math.sin(flightTimer) * 20.0 * delta; // Heavy, stable dampening sway
    }

    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 20 && y < 460) {
            shoot(projectileList);
        }
    }

    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 280.0;
        int damage = 20;

        // Heavy central prow plasma slug
        projectileList.add(new Projectile(x + (width / 2.0) - 4, y + height, 8, 18, 0, bulletSpeed, false, damage));
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Heavy reinforced hexagonal chine
        Polygon hull = new Polygon();
        hull.addPoint(px + 23, py + 44); // Forward nose
        hull.addPoint(px + 44, py + 30); // Right forward chine
        hull.addPoint(px + 46, py + 10); // Right aft
        hull.addPoint(px + 34, py);      // Right thruster cowl
        hull.addPoint(px + 23, py + 8);  // Center aft
        hull.addPoint(px + 12, py);      // Left thruster cowl
        hull.addPoint(px, py + 10);      // Left aft
        hull.addPoint(px + 2, py + 30);  // Left forward chine

        // Gunmetal grey/blue armored plate fill
        g.setColor(new Color(40, 50, 65));
        g.fillPolygon(hull);

        // Cyan-reinforced armored edges
        g.setColor(new Color(0, 200, 220));
        g.setStroke(new BasicStroke(2.0f));
        g.drawPolygon(hull);

        // Armor plating rivets
        g.setColor(new Color(160, 210, 240));
        g.fillRect(px + 14, py + 16, 3, 3);
        g.fillRect(px + 29, py + 16, 3, 3);
        g.fillRect(px + 21, py + 26, 4, 4);

        // Cannon muzzle
        g.setColor(new Color(255, 120, 50));
        g.fillRect(px + 21, py + 38, 4, 6);

        // Shield aura
        if (shield > 0) {
            g.setColor(new Color(0, 220, 255, 70));
            g.drawOval(px - 4, py - 4, width + 8, height + 8);
        }
    }

    // Encapsulated accessors and mutators
    public int getArmorRating() {
        return armorRating;
    }

    public void setArmorRating(int armorRating) {
        this.armorRating = Math.max(0, armorRating);
    }
}
