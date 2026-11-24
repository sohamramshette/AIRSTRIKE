// ==============================================================================
// File      : PSOOP/src/model/RailgunDreadnoughtEnemy.java
// Subject   : PSOOP-CO1 / COA-CO4 | 5-Stage Pipelined Railgun Dreadnought
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: CPU Pipeline] [TOPIC: Hazard Detection]
// Game Role : Advances a 5-stage instruction pipeline to fire railgun salvos unless stalled by hits.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Pipelined Railgun Dreadnought cycling IF -> ID -> EX -> MEM -> WB with RAW stall hazards.
 */
public class RailgunDreadnoughtEnemy extends Enemy {

    public enum PipelineStage { IF, ID, EX, MEM, WB }
    private static final PipelineStage[] STAGES = PipelineStage.values();

    private int currentStageIndex = 0;
    private double pipelineClockTimer = 0.0;
    private boolean stallBubble = false;
    private double stallDuration = 0.0;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a heavy dreadnought initialized at Fetch (IF) stage.
     */
    public RailgunDreadnoughtEnemy(double x, double y) {
        // x, y, width 56, height 52, maxHealth 140, maxShield 50, speed 65.0, shootCooldown 2400ms, scoreValue 520
        super(x, y, 56, 52, 140, 50, 65.0, 2400, 520);
        this.vy = speed * 0.6;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: CPU Pipeline] [TOPIC: Kinematics]
     * Game Role: Clocks the 5-stage pipeline every 0.4s or counts down active RAW stall bubbles.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.2;
        y += vy * delta;
        x += Math.sin(flightTimer) * 14.0 * delta;

        // WHY: Stall bubble freezes stage progression for stallDuration before resuming pipeline clock.
        if (stallBubble) {
            stallDuration -= delta;
            if (stallDuration <= 0) {
                stallBubble = false;
            }
        } else {
            pipelineClockTimer += delta;
            // WHY: While-loop drains all 0.4s clock slices so frame spikes do not drop pipeline ticks.
            while (pipelineClockTimer >= 0.4) {
                pipelineClockTimer -= 0.4;
                currentStageIndex = (currentStageIndex + 1) % STAGES.length;
            }
        }
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Hazard Detection]
     * Game Role: Resolves damage and injects a 1s stall bubble if struck during EX or MEM stages.
     */
    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        super.takeDamage(amount, bypassShield);
        // WHY: Hitting EX/MEM simulates a Read-After-Write dependency hazard that stalls Write-Back firing.
        if (STAGES[currentStageIndex] == PipelineStage.EX || STAGES[currentStageIndex] == PipelineStage.MEM) {
            stallBubble = true;
            stallDuration = 1.0; // 1 second stall penalty
        }
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: CPU Pipeline]
     * Game Role: Fires only when pipeline reaches Write-Back (WB) with no active stall hazard.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && STAGES[currentStageIndex] == PipelineStage.WB && !stallBubble && y > 30 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Discharges central heavy railgun slug and twin stabilizers. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 340.0;
        int damage = 28; // Massive railgun payload
        // Central railgun slug + lateral stabilizers
        projectileList.add(new Projectile(x + width / 2.0 - 5, y + height, 10, 26, 0, bulletSpeed, false, damage));
        projectileList.add(new Projectile(x + 6, y + height - 8, 5, 12, -15, bulletSpeed * 0.8, false, 10));
        projectileList.add(new Projectile(x + width - 11, y + height - 8, 5, 12, 15, bulletSpeed * 0.8, false, 10));
    }

    /** [TOPIC: Method Overriding] Game Role: Renders hull, 5-cell [IF..WB] stage bar, and stall alert. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Heavy Dreadnought Hull
        int[] xs = { px + width / 2, px + width - 2, px + width - 8, px + width / 2 + 10, px + width / 2 - 10, px + 8, px + 2 };
        int[] ys = { py + height, py + 14, py + 2, py + 6, py + 6, py + 2, py + 14 };
        g.setColor(new Color(40, 20, 25));
        g.fillPolygon(xs, ys, 7);
        g.setColor(new Color(255, 50, 70));
        g.setStroke(new BasicStroke(2.0f));
        g.drawPolygon(xs, ys, 7);

        // 5-Stage CPU Pipeline HUD indicator: [IF][ID][EX][MEM][WB]
        int cellW = 10;
        int cellH = 7;
        int startX = px + width / 2 - (5 * (cellW + 1)) / 2;
        int startY = py - 10;

        for (int i = 0; i < STAGES.length; i++) {
            int cx = startX + i * (cellW + 1);
            if (stallBubble && i == currentStageIndex) {
                g.setColor(new Color(255, 0, 0)); // Red stall bubble
                g.fillRect(cx, startY, cellW, cellH);
            } else if (i == currentStageIndex) {
                g.setColor(new Color(0, 255, 200)); // Active stage
                g.fillRect(cx, startY, cellW, cellH);
            } else if (i < currentStageIndex) {
                g.setColor(new Color(0, 140, 100)); // Completed stage
                g.fillRect(cx, startY, cellW, cellH);
            } else {
                g.setColor(new Color(50, 50, 60)); // Pending stage
                g.fillRect(cx, startY, cellW, cellH);
            }
            g.setColor(Color.WHITE);
            g.drawRect(cx, startY, cellW, cellH);
        }

        // Stall alert
        if (stallBubble) {
            g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
            g.setColor(Color.RED);
            g.drawString("HAZARD:STALL", px - 6, startY - 3);
        }

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 2;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(255, 50, 70));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);

        if (maxShield > 0) {
            float shRatio = (float) shield / maxShield;
            g.setColor(new Color(0, 220, 255));
            g.fillRect(px, barY - 4, (int) (barW * shRatio), barH);
        }
    }

    public PipelineStage getCurrentStage() { return STAGES[currentStageIndex]; }
    public boolean isStallBubble() { return stallBubble; }
}
