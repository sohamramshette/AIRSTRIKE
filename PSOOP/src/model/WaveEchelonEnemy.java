// ==============================================================================
// File      : PSOOP/src/model/WaveEchelonEnemy.java
// Subject   : PSOOP-CO1 / PL-CO3 | FIFO Queue & LIFO Stack Echelon Craft
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Queue and Stack] [TOPIC: Kinematics]
// Game Role : Schedules cyclic lateral maneuvers via a FIFO queue and logs history on a LIFO stack.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Queue Wave Echelon craft scheduling maneuvers with a FIFO waypoint queue and LIFO stack.
 */
public class WaveEchelonEnemy extends Enemy {

    private final Deque<Double> waypointQueue = new ArrayDeque<>();
    private final Deque<Double> historyStack = new ArrayDeque<>();
    private double tacticalStepTimer = 0.0;
    private int currentEchelonPhase = 0;

    /**
     * [TOPIC: Inheritance] [TOPIC: Queue and Stack]
     * Game Role: Spawns an echelon craft and preloads its cyclic lateral waypoint queue.
     */
    public WaveEchelonEnemy(double x, double y) {
        // x, y, width 44, height 42, maxHealth 85, maxShield 25, speed 90.0, shootCooldown 1700ms, scoreValue 360
        super(x, y, 44, 42, 85, 25, 90.0, 1700, 360);
        this.vy = speed * 0.7;
        initQueue();
    }

    private void initQueue() {
        // Enqueue FIFO delta offsets
        waypointQueue.add(25.0);
        waypointQueue.add(-25.0);
        waypointQueue.add(40.0);
        waypointQueue.add(-40.0);
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Queue and Stack] [TOPIC: Kinematics]
     * Game Role: Advances descent and rotates FIFO lateral waypoint offsets every 0.8 seconds.
     */
    @Override
    public void update(double delta) {
        y += vy * delta;
        tacticalStepTimer += delta;

        if (tacticalStepTimer >= 0.8) {
            tacticalStepTimer = 0.0;
            if (!waypointQueue.isEmpty()) {
                double targetOffset = waypointQueue.poll();
                // Push onto LIFO history stack
                historyStack.push(targetOffset);
                x += targetOffset * 0.5;
                // WHY: Re-enqueue polled offset to keep the 4-step echelon patrol cycling indefinitely.
                waypointQueue.add(targetOffset);
                currentEchelonPhase = (currentEchelonPhase + 1) % 4;
            }
        }
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires angled salvo within active corridor. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 20 && y < 460) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Fires a bolt angled symmetrically by currentEchelonPhase. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 270.0;
        int damage = 15;
        // WHY: Centers phase index {0..3} around 1.5 so lateral firing angles sweep symmetrically left/right.
        double angle = (currentEchelonPhase - 1.5) * 15.0;
        projectileList.add(new Projectile(x + width / 2.0 - 3, y + height, 6, 14, angle, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Draws wedge hull, live queue size HUD tag, and vitals bar. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Wedge / Echelon Arrow Hull
        int[] xs = { px + width / 2, px + width - 2, px + width / 2 + 6, px + width / 2 - 6, px + 2 };
        int[] ys = { py + height - 2, py + 4, py + 12, py + 12, py + 4 };
        g.setColor(new Color(25, 45, 55));
        g.fillPolygon(xs, ys, 5);
        g.setColor(new Color(0, 240, 210));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(xs, ys, 5);

        // HUD Mini-Queue indicator
        g.setColor(new Color(0, 240, 210, 180));
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 8));
        g.drawString("Q[" + waypointQueue.size() + "]", px + 4, py - 8);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 4;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(0, 230, 190));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }
}
