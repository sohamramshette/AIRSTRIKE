// ==============================================================================
// File      : PSOOP/src/model/BoundarySkimmerEnemy.java
// Subject   : PSOOP-CO1 / CGL-CO5 | Cohen-Sutherland Outcode Skimmer
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Clipping Algorithms] [TOPIC: Kinematics]
// Game Role : Sweeps viewport edges while computing its live 4-bit TBRL Cohen-Sutherland outcode.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Boundary Phase Skimmer tracking 4-bit Cohen-Sutherland region outcodes during edge sweeps.
 */
public class BoundarySkimmerEnemy extends Enemy {

    private static final int INSIDE = 0; // 0000
    private static final int LEFT   = 1; // 0001
    private static final int RIGHT  = 2; // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP    = 8; // 1000

    private int currentOutcode = INSIDE;
    private double lateralDirection = 1.0;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a fast lateral skimmer initialized inside clipping bounds.
     */
    public BoundarySkimmerEnemy(double x, double y) {
        // x, y, width 44, height 40, maxHealth 80, maxShield 25, speed 100.0, shootCooldown 1600ms, scoreValue 390
        super(x, y, 44, 40, 80, 25, 100.0, 1600, 390);
        this.vy = speed * 0.65;
    }

    /**
     * [TOPIC: Clipping Algorithms]
     * Game Role: Encodes point (qx, qy) against [xmin..xmax, ymin..ymax] into a 4-bit TBRL outcode.
     */
    private int computeOutcode(double qx, double qy, double xmin, double ymin, double xmax, double ymax) {
        int code = INSIDE;
        // WHY: Independent bitwise OR flags (TBRL) let clip checks test trivial accept/reject in O(1).
        if (qx < xmin) code |= LEFT;
        else if (qx > xmax) code |= RIGHT;
        if (qy < ymin) code |= TOP;
        else if (qy > ymax) code |= BOTTOM;
        return code;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Clipping Algorithms] [TOPIC: Kinematics]
     * Game Role: Sweeps side-to-side across clipping limits and updates currentOutcode each tick.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.8;
        y += vy * delta;

        // WHY: Bounce range [40, 680] intentionally crosses the [80, 640] clip window to exercise outcode bits.
        x += lateralDirection * 90.0 * delta;
        if (x > 680) {
            lateralDirection = -1.0;
        } else if (x < 40) {
            lateralDirection = 1.0;
        }

        // Compute 4-bit Cohen-Sutherland Outcode
        currentOutcode = computeOutcode(x + width / 2.0, y + height / 2.0, 80, 20, 640, 550);
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires downward bolt inside active lane. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Launches a central downward plasma bolt. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 285.0;
        int damage = 14;
        projectileList.add(new Projectile(x + width / 2.0 - 3, y + height, 6, 14, 0, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Draws magenta chevron hull and binary CLIP outcode badge. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Skimmer chevron hull
        int[] xs = { px + width / 2, px + width - 2, px + width / 2, px + 2 };
        int[] ys = { py + height, py + 4, py + 12, py + 4 };
        g.setColor(new Color(45, 15, 45));
        g.fillPolygon(xs, ys, 4);
        g.setColor(new Color(255, 60, 220));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(xs, ys, 4);

        // Cohen-Sutherland 4-bit Outcode HUD display
        String outcodeStr = String.format("%4s", Integer.toBinaryString(currentOutcode)).replace(' ', '0');
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(255, 120, 240));
        g.drawString("CLIP:[" + outcodeStr + "]", px - 4, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(255, 60, 220));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }

    public int getCurrentOutcode() { return currentOutcode; }
}
