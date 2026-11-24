// ==============================================================================
// File      : PSOOP/src/model/BoothGunshipEnemy.java
// Subject   : PSOOP-CO1 / COA-CO3 | Booth Multiplication Gunship
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Booth Algorithm] [TOPIC: Kinematics]
// Game Role : Steps an 8-cycle Booth multiplier state machine to gate high-damage plasma bolts.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Booth Multiplier Gunship stepping 8-cycle signed Booth arithmetic to arm plasma volleys.
 */
public class BoothGunshipEnemy extends Enemy {

    private int boothCycle = 0;
    private double arithmeticClockTimer = 0.0;
    private int accumulator = 0;
    private int multiplicand = 7;
    private int multiplier = 3;
    private int qPrev = 0;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a heavy gunship initialized at cycle 0 of Booth multiplication (7 * 3).
     */
    public BoothGunshipEnemy(double x, double y) {
        // x, y, width 52, height 48, maxHealth 115, maxShield 40, speed 70.0, shootCooldown 2200ms, scoreValue 480
        super(x, y, 52, 48, 115, 40, 70.0, 2200, 480);
        this.vy = speed * 0.65;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Booth Algorithm] [TOPIC: Kinematics]
     * Game Role: Advances gunship movement and clocks one Booth arithmetic step every 0.25s.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.3;
        y += vy * delta;
        x += Math.sin(flightTimer) * 16.0 * delta;

        // Step through 8 Booth's multiplication cycles
        arithmeticClockTimer += delta;
        if (arithmeticClockTimer >= 0.25) {
            arithmeticClockTimer = 0.0;
            stepBoothCycle();
        }
    }

    /**
     * [TOPIC: Booth Algorithm]
     * Game Role: Evaluates (Q0, Q-1) recoding rules and right-shifts registers across 8 cycles.
     */
    private void stepBoothCycle() {
        if (boothCycle < 8) {
            int q0 = multiplier & 1;
            // WHY: Booth pair recoding (10 -> -M, 01 -> +M, 00/11 -> shift only) handles signed runs uniformly.
            if (q0 == 1 && qPrev == 0) {
                accumulator -= multiplicand;
            } else if (q0 == 0 && qPrev == 1) {
                accumulator += multiplicand;
            }
            // WHY: Signed right shift (>>) preserves sign bit during register progression.
            qPrev = q0;
            multiplier >>= 1;
            accumulator >>= 1;
            boothCycle++;
        } else {
            // Reset for next arithmetic sequence
            boothCycle = 0;
            accumulator = 0;
            multiplicand = 5 + (int) (Math.random() * 5);
            multiplier = 2 + (int) (Math.random() * 4);
            qPrev = 0;
        }
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: Method Overriding]
     * Game Role: Fires only when Booth multiplication reaches final charge cycle (>= 7).
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        // WHY: Gates discharge on cycle >= 7 so firing synchronizes with completion of the 8-bit multiply.
        if (canShoot() && boothCycle >= 7 && y > 25 && y < 460) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Fires a high-damage Booth-charged plasma bolt. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 300.0;
        int damage = 24; // High Booth-amplified damage
        projectileList.add(new Projectile(x + width / 2.0 - 5, y + height, 10, 20, 0, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Renders armored prow, charging core ring, and BOOTH HUD. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Heavy Gunship Prow
        int[] xs = { px + width / 2, px + width - 2, px + width - 10, px + 10, px + 2 };
        int[] ys = { py + height, py + 12, py + 2, py + 2, py + 12 };
        g.setColor(new Color(60, 30, 10));
        g.fillPolygon(xs, ys, 5);
        g.setColor(new Color(255, 140, 0));
        g.setStroke(new BasicStroke(1.8f));
        g.drawPolygon(xs, ys, 5);

        // Arithmetic Core charging ring
        int coreRadius = (int) (boothCycle * 1.5);
        g.setColor(new Color(255, 140, 0, 80));
        g.fillOval(px + width / 2 - coreRadius, py + height / 2 - coreRadius, coreRadius * 2, coreRadius * 2);

        // Booth Register Telemetry
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(255, 170, 40));
        g.drawString("BOOTH[" + boothCycle + "/8] A:" + accumulator, px - 6, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(255, 140, 0));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);

        if (maxShield > 0) {
            float shRatio = (float) shield / maxShield;
            g.setColor(new Color(255, 200, 60));
            g.fillRect(px, barY - 4, (int) (barW * shRatio), barH);
        }
    }

    public int getBoothCycle() { return boothCycle; }
}
