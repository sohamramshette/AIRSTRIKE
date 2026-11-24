// ==============================================================================
// File      : PSOOP/src/model/Projectile.java
// Subject   : PSOOP-CO1 | Concrete Projectile Entity & Kinematics
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Kinematics] [TOPIC: Defensive Programming]
// Game Role : Models player and enemy laser bolts with velocity integration and off-screen culling.
// ==============================================================================
package model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Concrete entity representing laser bolts fired by either Player or Enemies.
 */
public class Projectile extends Entity {
    
    private final boolean isPlayerBullet;
    private final int damage;
    private final Color coreColor;
    private final Color glowColor;

    /**
     * [TOPIC: Inheritance] [TOPIC: Defensive Programming]
     * Game Role: Spawns a directed bolt with >= 1 damage floor and faction-coded glow colors.
     */
    public Projectile(double x, double y, int width, int height, double vx, double vy, boolean isPlayerBullet, int damage) {
        super(x, y, width, height);
        this.vx = vx;
        this.vy = vy;
        this.isPlayerBullet = isPlayerBullet;
        // WHY: Guarantees every spawned projectile inflicts at least 1 hit point.
        this.damage = Math.max(1, damage);
        
        if (isPlayerBullet) {
            this.coreColor = new Color(180, 255, 255);
            this.glowColor = new Color(0, 200, 255, 120);
        } else {
            this.coreColor = new Color(255, 220, 180);
            this.glowColor = new Color(255, 50, 50, 140);
        }
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Kinematics]
     * Game Role: Advances bolt position linearly over elapsed delta seconds.
     */
    @Override
    public void update(double delta) {
        // WHY: Frame-rate independent Euler integration keeps projectile velocity consistent across FPS drops.
        x += vx * delta;
        y += vy * delta;
    }

    /**
     * [TOPIC: Kinematics]
     * Game Role: Identifies projectiles that have exited the arena bounds for list pruning.
     */
    public boolean isOffScreen(int screenWidth, int screenHeight) {
        // WHY: Extra margin prevents culling bolts while their outer glow aura is still visible on edge.
        return (x + width < -20 || x > screenWidth + 20 || y + height < -30 || y > screenHeight + 30);
    }

    /**
     * [TOPIC: Method Overriding]
     * Game Role: Draws layered glow aura and inner core onto the canvas.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Draw outer glow aura
        g.setColor(glowColor);
        g.fillRoundRect(px - 2, py - 2, width + 4, height + 4, 4, 4);

        // Draw intense core
        g.setColor(coreColor);
        g.fillRoundRect(px, py, width, height, 3, 3);
    }

    public boolean isPlayerBullet() { return isPlayerBullet; }
    public int getDamage() { return damage; }
}

