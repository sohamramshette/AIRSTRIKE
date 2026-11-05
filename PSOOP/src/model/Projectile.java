package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

/**
 * Concrete entity representing laser bolts fired by either Player or Enemies.
 */
public class Projectile extends Entity {
    
    private final boolean isPlayerBullet;
    private final int damage;
    private final Color coreColor;
    private final Color glowColor;

    /**
     * Constructs a directed projectile.
     */
    public Projectile(double x, double y, int width, int height, double vx, double vy, boolean isPlayerBullet, int damage) {
        super(x, y, width, height);
        this.vx = vx;
        this.vy = vy;
        this.isPlayerBullet = isPlayerBullet;
        this.damage = Math.max(1, damage);
        
        if (isPlayerBullet) {
            this.coreColor = new Color(180, 255, 255);
            this.glowColor = new Color(0, 200, 255, 120);
        } else {
            this.coreColor = new Color(255, 220, 180);
            this.glowColor = new Color(255, 50, 50, 140);
        }
    }

    @Override
    public void update(double delta) {
        x += vx * delta;
        y += vy * delta;
    }

    /**
     * Checks whether the projectile has drifted outside screen boundaries.
     */
    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return (x + width < -20 || x > screenWidth + 20 || y + height < -30 || y > screenHeight + 30);
    }

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
