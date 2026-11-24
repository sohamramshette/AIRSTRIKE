// ==============================================================================
// File      : PSOOP/src/model/TargetHunterEnemy.java
// Subject   : PSOOP-CO1 / PL-CO4 | Telemetry Sort & Binary Search Sniper
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Sorting and Searching] [TOPIC: Kinematics]
// Game Role : Samples player X history, applies sort + binary search, and fires angled sniper bolts.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

/**
 * Target-Seeking Hunter modeling QuickSort telemetry ordering and Binary Search target lock.
 */
public class TargetHunterEnemy extends Enemy {

    private final double[] playerTrajectoryHistory = new double[8];
    private int sampleCount = 0;
    private double calculatedLeadAngle = 0.0;
    private boolean trackingLocked = false;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a shielded sniper enemy with an 8-slot trajectory history buffer.
     */
    public TargetHunterEnemy(double x, double y) {
        // x, y, width 42, height 46, maxHealth 75, maxShield 35, speed 105.0, shootCooldown 1400ms, scoreValue 390
        super(x, y, 42, 46, 75, 35, 105.0, 1400, 390);
        this.vy = speed * 0.7;
    }

    /** [TOPIC: Method Overriding] [TOPIC: Kinematics] Game Role: Integrates steady descent and cosine weave. */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.6;
        y += vy * delta;
        x += Math.cos(flightTimer) * 32.0 * delta;
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: Sorting and Searching]
     * Game Role: Records player X samples, sorts + binary-searches telemetry, and fires when cooled down.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;

        if (player != null && player.isActive()) {
            // WHY: Ring-buffer modulo indexing overwrites oldest sample cleanly in O(1) space.
            playerTrajectoryHistory[sampleCount % playerTrajectoryHistory.length] = player.getX();
            sampleCount++;

            if (sampleCount >= 4) {
                // WHY: Arrays.sort establishes the monotonic precondition required by binarySearch.
                double[] sortedSamples = Arrays.copyOf(playerTrajectoryHistory, Math.min(sampleCount, playerTrajectoryHistory.length));
                Arrays.sort(sortedSamples);

                double targetX = player.getX();
                int idx = Arrays.binarySearch(sortedSamples, targetX);
                // WHY: Convert negative binarySearch insertion encodes -(insertionPoint) - 1 back to array index.
                if (idx < 0) idx = -idx - 1;

                double diffX = targetX - (x + width / 2.0);
                // WHY: Clamp horizontal velocity offset to [-45, 45] so sniper bolts never fire nearly sideways.
                calculatedLeadAngle = Math.max(-45.0, Math.min(45.0, diffX * 0.12));
                trackingLocked = Math.abs(diffX) < 80;
            }
        }

        if (canShoot() && y > 30 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Launches a high-velocity bolt steered by calculatedLeadAngle. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 310.0;
        int damage = 18;
        // Targeted high-velocity sniper beam aimed with calculated lead angle
        projectileList.add(new Projectile(x + width / 2.0 - 3, y + height, 6, 16, calculatedLeadAngle, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Renders hunter hull, dashed targeting laser, and lock badge. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Sleek triangular interceptor hull
        int[] xs = { px + width / 2, px + width - 2, px + width / 2 + 5, px + width / 2 - 5, px + 2 };
        int[] ys = { py + height, py + 4, py + 14, py + 14, py + 4 };
        g.setColor(new Color(60, 15, 25));
        g.fillPolygon(xs, ys, 5);
        g.setColor(new Color(255, 60, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(xs, ys, 5);

        // Sniper laser targeting line
        if (trackingLocked) {
            g.setColor(new Color(255, 30, 60, 160));
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 5}, 0));
            double rad = Math.toRadians(calculatedLeadAngle + 90);
            int tx = px + width / 2 + (int) (Math.cos(rad) * 120);
            int ty = py + height + (int) (Math.sin(rad) * 120);
            g.drawLine(px + width / 2, py + height, tx, ty);
        }

        // HUD search lock icon
        g.setColor(new Color(255, 120, 140));
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.drawString("BSearch:" + (trackingLocked ? "LOCK" : "SCAN"), px - 4, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(255, 60, 80));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }
}
