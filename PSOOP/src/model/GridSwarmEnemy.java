// ==============================================================================
// File      : PSOOP/src/model/GridSwarmEnemy.java
// Subject   : PSOOP-CO1 / PL-CO1 | 2D Spatial Grid Matrix Enemy
// Topics    : [TOPIC: Inheritance] [TOPIC: Polymorphism] [TOPIC: Method Overriding] [TOPIC: 2D Spatial Grid]
// Game Role : Models an 8x8 sector grid on its hull that degrades visually as hits land.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Spatial Grid Swarm enemy pairing OOP inheritance with an 8x8 2D sector matrix.
 */
public class GridSwarmEnemy extends Enemy {

    private static final int GRID_SIZE = 8;
    private final boolean[][] localGrid = new boolean[GRID_SIZE][GRID_SIZE];
    private double scanAngle = 0.0;
    private int gridDeployCount = 0;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a shielded swarm unit and seeds its 8x8 checkerboard matrix.
     */
    public GridSwarmEnemy(double x, double y) {
        // x, y, width 44, height 42, maxHealth 80, maxShield 25, speed 85.0, shootCooldown 1800ms, scoreValue 380
        super(x, y, 44, 42, 80, 25, 85.0, 1800, 380);
        this.vy = speed * 0.75;
        initGrid();
    }

    private void initGrid() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                localGrid[r][c] = ((r + c) % 2 == 0);
            }
        }
    }

    /** [TOPIC: Method Overriding] [TOPIC: Kinematics] Game Role: Advances radar scan rotation and sine sway. */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.8;
        scanAngle += delta * 2.5;
        y += vy * delta;
        x += Math.sin(flightTimer) * 28.0 * delta;
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Triggers twin salvo inside firing corridor. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 30 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Fires angled dual bolts and increments deployment counter. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 260.0;
        int damage = 14;

        // Dual grid-aligned projectile release
        projectileList.add(new Projectile(x + 8, y + height, 6, 14, -30, bulletSpeed, false, damage));
        projectileList.add(new Projectile(x + width - 14, y + height, 6, 14, 30, bulletSpeed, false, damage));
        gridDeployCount++;
    }

    /**
     * [TOPIC: Method Overriding]
     * Game Role: Delegates damage handling and clears a sector cell in the 8x8 hull matrix.
     */
    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        super.takeDamage(amount, bypassShield);
        // WHY: Visualizes hull degradation by knocking out a random 2D sector cell upon impact.
        int r = (int) (Math.random() * GRID_SIZE);
        int c = (int) (Math.random() * GRID_SIZE);
        localGrid[r][c] = false;
    }

    /** [TOPIC: Method Overriding] Game Role: Draws cybernetic hull, 8x8 cell overlay, radar sweep, and vitals. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Cybernetic hexagonal base
        g.setColor(new Color(15, 45, 30));
        g.fillRoundRect(px + 4, py + 4, width - 8, height - 8, 8, 8);
        g.setColor(new Color(0, 255, 128));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(px + 4, py + 4, width - 8, height - 8, 8, 8);

        // 8x8 Spatial Matrix visualization on ship hull
        int cellW = Math.max(2, (width - 16) / GRID_SIZE);
        int cellH = Math.max(2, (height - 16) / GRID_SIZE);
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (localGrid[r][c]) {
                    g.setColor(new Color(0, 220, 100, 140));
                    g.fillRect(px + 8 + c * cellW, py + 8 + r * cellH, cellW - 1, cellH - 1);
                }
            }
        }

        // Radar sweep line
        int cx = px + width / 2;
        int cy = py + height / 2;
        int sx = cx + (int) (Math.cos(scanAngle) * 12);
        int sy = cy + (int) (Math.sin(scanAngle) * 12);
        g.setColor(new Color(150, 255, 180, 200));
        g.drawLine(cx, cy, sx, sy);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 7;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(0, 230, 100));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);

        if (maxShield > 0) {
            float shRatio = (float) shield / maxShield;
            g.setColor(new Color(0, 180, 255));
            g.fillRect(px, barY - 4, (int) (barW * shRatio), barH);
        }
    }

    public int getGridDeployCount() { return gridDeployCount; }
}
