// ==============================================================================
// File      : PSOOP/src/model/WarpCruiserEnemy.java
// Subject   : PSOOP-CO1 / CGL-CO4 | 1/Z Perspective Warp Cruiser
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Perspective Projection] [TOPIC: Kinematics]
// Game Role : Warps from Z=50 via 1/Z perspective scaling before locking into the 2D plane to fire.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Depth-Diving Warp Cruiser demonstrating 1/Z perspective foreshortening into orthographic combat.
 */
public class WarpCruiserEnemy extends Enemy {

    private double depthZ = 50.0; // Deep 3D space
    private double perspectiveScale = 0.2;
    private boolean warpComplete = false;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a cruiser in deep Z=50 space at 0.2 initial perspective scale.
     */
    public WarpCruiserEnemy(double x, double y) {
        // x, y, width 50, height 46, maxHealth 90, maxShield 25, speed 80.0, shootCooldown 1800ms, scoreValue 440
        super(x, y, 50, 46, 90, 25, 80.0, 1800, 440);
        this.vy = speed * 0.7;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Perspective Projection] [TOPIC: Kinematics]
     * Game Role: Approaches near plane Z=10 with 1/Z scaling while descending and swaying.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.5;

        // WHY: 10.0 / depthZ implements 1/Z perspective division until locking at near plane Z=10.
        if (!warpComplete) {
            depthZ = Math.max(10.0, depthZ - delta * 25.0);
            perspectiveScale = 10.0 / depthZ; // 1/Z scaling
            if (depthZ <= 10.0) {
                warpComplete = true;
                perspectiveScale = 1.0;
            }
        }

        y += vy * delta;
        x += Math.sin(flightTimer) * 22.0 * delta;
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: Method Overriding]
     * Game Role: Suppresses fire until the cruiser finishes warping into the 2D combat plane.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        // WHY: Prevents unseeable deep-Z ships from firing before reaching full foreground scale.
        if (canShoot() && warpComplete && y > 30 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Fires a heavy central forward bolt. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 295.0;
        int damage = 17;
        projectileList.add(new Projectile(x + width / 2.0 - 4, y + height, 8, 16, 0, bulletSpeed, false, damage));
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Perspective Projection]
     * Game Role: Draws hull scaled by perspectiveScale around entity center plus PERSP HUD tag.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Calculate perspective scaled bounds
        int w = (int) (width * perspectiveScale);
        int h = (int) (height * perspectiveScale);
        int ox = px + (width - w) / 2;
        int oy = py + (height - h) / 2;

        // Hull polygon scaled by 1/Z
        int[] xs = { ox + w / 2, ox + w - 2, ox + w / 2, ox + 2 };
        int[] ys = { oy + h, oy + h / 2, oy + 2, oy + h / 2 };
        g.setColor(new Color(25, 30, 65));
        g.fillPolygon(xs, ys, 4);
        g.setColor(new Color(100, 140, 255));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(xs, ys, 4);

        // Perspective warp trail
        if (!warpComplete) {
            g.setColor(new Color(100, 180, 255, 100));
            g.drawLine(px + width / 2, py + height / 2, px + width / 2, py + height / 2 - 20);
        }

        // HUD Perspective tag
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(130, 170, 255));
        g.drawString("PERSP:1/Z=" + String.format("%.2f", perspectiveScale), px - 4, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(100, 140, 255));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }

    public double getDepthZ() { return depthZ; }
}
