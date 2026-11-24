// ==============================================================================
// File      : PSOOP/src/model/HashShieldEnemy.java
// Subject   : PSOOP-CO1 / PL-CO5 | Hash-Bucket Shield Matrix Enemy
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Hashing] [TOPIC: Kinematics]
// Game Role : Cycles shield frequency slots via modulo linear probing across 3 hash buckets.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Hash-Shielded Matrix Craft cycling active shield buckets using modulo linear probing.
 */
public class HashShieldEnemy extends Enemy {

    private int activeHashBucket = 0;
    private double bucketCycleTimer = 0.0;
    private final int[] bucketDamage = new int[3];

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a high-shield matrix craft starting on hash slot 0.
     */
    public HashShieldEnemy(double x, double y) {
        // x, y, width 48, height 46, maxHealth 95, maxShield 45, speed 80.0, shootCooldown 1600ms, scoreValue 420
        super(x, y, 48, 46, 95, 45, 80.0, 1600, 420);
        this.vy = speed * 0.7;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Hashing] [TOPIC: Kinematics]
     * Game Role: Integrates descent and advances activeHashBucket every 1.5s via linear probing.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.5;
        y += vy * delta;
        x += Math.sin(flightTimer) * 22.0 * delta;

        // WHY: Modulo wrap (h + 1) % 3 steps through the 3-slot frequency table uniformly.
        bucketCycleTimer += delta;
        if (bucketCycleTimer >= 1.5) {
            bucketCycleTimer = 0.0;
            activeHashBucket = (activeHashBucket + 1) % 3;
        }
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Hashing]
     * Game Role: Records hit telemetry in the active hash bucket and resolves shield-before-hull damage.
     */
    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        if (amount <= 0 || !active) return;

        // WHY: Tracks cumulative impact per hash slot before applying standard shield-first absorption.
        int incoming = amount;
        bucketDamage[activeHashBucket] += incoming;

        if (!bypassShield && shield > 0) {
            if (shield >= incoming) {
                shield -= incoming;
                incoming = 0;
            } else {
                incoming -= shield;
                shield = 0;
            }
        }

        if (incoming > 0) {
            health = Math.max(0, health - incoming);
            if (health == 0) active = false;
        }
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires dual bolts inside active corridor. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Launches twin slightly diverging frequency bolts. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 275.0;
        int damage = 16;
        // Tri-frequency spread based on hash table slots
        projectileList.add(new Projectile(x + 6, y + height, 6, 14, -18, bulletSpeed, false, damage));
        projectileList.add(new Projectile(x + width - 12, y + height, 6, 14, 18, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Draws bucket-colored shield ring, chassis, and [H0..H2] HUD. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Hexagonal shield aura in active bucket color
        Color[] bucketColors = {
            new Color(0, 220, 255),  // Bucket 0: Cyan
            new Color(255, 180, 0),  // Bucket 1: Amber
            new Color(200, 80, 255)  // Bucket 2: Magenta
        };
        Color activeCol = bucketColors[activeHashBucket];

        g.setColor(new Color(activeCol.getRed(), activeCol.getGreen(), activeCol.getBlue(), 40));
        g.fillOval(px - 4, py - 4, width + 8, height + 8);
        g.setColor(activeCol);
        g.setStroke(new BasicStroke(1.8f));
        g.drawOval(px - 4, py - 4, width + 8, height + 8);

        // Solid central armored chassis
        g.setColor(new Color(30, 25, 45));
        g.fillRoundRect(px + 6, py + 6, width - 12, height - 12, 10, 10);
        g.setColor(new Color(180, 160, 220));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(px + 6, py + 6, width - 12, height - 12, 10, 10);

        // Mini Hash Table display above hull: [H0] [H1] [H2]
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        for (int i = 0; i < 3; i++) {
            if (i == activeHashBucket) {
                g.setColor(bucketColors[i]);
                g.drawString("[H" + i + "]*", px + i * 16, py - 6);
            } else {
                g.setColor(new Color(120, 120, 130));
                g.drawString(" H" + i, px + i * 16, py - 6);
            }
        }

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 4;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(180, 100, 255));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }

    public int getActiveHashBucket() { return activeHashBucket; }
}
