package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Heavy dreadnought command flagship.
 * High health, multi-layered shields, patrol movement, and cyclic attack patterns.
 */
public class BossEnemy extends Enemy {

    private int attackPhase;
    private double phaseTimer;
    private boolean movingRight;
    private final String bossName;
    private int patrolMinX;
    private int patrolMaxX;

    public BossEnemy(double x, double y, String bossName) {
        // width 116, height 78, health 550, shield 150, speed 80, cooldown 900ms, score 2500
        super(x, y, 116, 78, 550, 150, 80.0, 900, 2500);
        this.bossName = (bossName != null && !bossName.isEmpty()) ? bossName : "Dreadnought Titan";
        this.attackPhase = 0;
        this.phaseTimer = 0.0;
        this.movingRight = true;
        this.patrolMinX = 30;
        this.patrolMaxX = 650;
    }

    public void setPatrolBounds(int minX, int maxX) {
        this.patrolMinX = minX;
        this.patrolMaxX = maxX;
    }

    @Override
    public void update(double delta) {
        // Horizontal patrol across upper sector
        if (movingRight) {
            x += speed * delta;
            if (x >= patrolMaxX) {
                movingRight = false;
            }
        } else {
            x -= speed * delta;
            if (x <= patrolMinX) {
                movingRight = true;
            }
        }

        // Descend to combat ceiling if spawned above
        if (y < 45) {
            y += 40.0 * delta;
        }

        phaseTimer += delta;
        if (phaseTimer >= 4.0) {
            phaseTimer = 0.0;
            attackPhase = (attackPhase + 1) % 3; // Cycle 3 attack patterns
        }
    }

    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        if (!canShoot()) return;

        recordShot();
        double centerX = getCenterX();
        double noseY = y + height - 6;

        switch (attackPhase) {
            case 0 -> {
                // Pattern 1: 5-way spread fan
                double[] angles = {-160, -80, 0, 80, 160};
                for (double vxOffset : angles) {
                    projectileList.add(new Projectile(centerX - 3, noseY, 6, 16, vxOffset, 310.0, false, 15));
                }
            }
            case 1 -> {
                // Pattern 2: Heavy dual plasma cannons
                projectileList.add(new Projectile(x + 24, noseY, 8, 22, -20, 360.0, false, 25));
                projectileList.add(new Projectile(x + width - 32, noseY, 8, 22, 20, 360.0, false, 25));
            }
            case 2 -> {
                // Pattern 3: Targeted barrage towards player position
                double targetX = (player != null) ? player.getCenterX() : centerX;
                double dx = targetX - centerX;
                double dy = ((player != null) ? player.getCenterY() : y + 400) - noseY;
                double dist = Math.hypot(dx, dy);
                if (dist > 0) {
                    double speedMag = 330.0;
                    projectileList.add(new Projectile(centerX - 4, noseY, 8, 18, (dx / dist) * speedMag, (dy / dist) * speedMag, false, 20));
                }
            }
        }
    }

    @Override
    public void shoot(List<Projectile> projectileList) {
        performAttack(0.016, projectileList, null);
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Heavy flagship dreadnought silhouette
        Polygon dreadnought = new Polygon();
        dreadnought.addPoint(px + 58, py + 78);   // Heavy central beak
        dreadnought.addPoint(px + 78, py + 62);   // Right weapon sponson
        dreadnought.addPoint(px + 104, py + 48);  // Right wing tip
        dreadnought.addPoint(px + 116, py + 16);  // Right aft flank
        dreadnought.addPoint(px + 90, py);        // Right main thruster
        dreadnought.addPoint(px + 58, py + 18);   // Center bridge aft
        dreadnought.addPoint(px + 26, py);        // Left main thruster
        dreadnought.addPoint(px, py + 16);        // Left aft flank
        dreadnought.addPoint(px + 12, py + 48);   // Left wing tip
        dreadnought.addPoint(px + 38, py + 62);   // Left weapon sponson

        // Heavy dark obsidian/crimson hull
        g.setColor(new Color(40, 15, 20));
        g.fillPolygon(dreadnought);

        // Gold and crimson armor trim
        g.setColor(new Color(255, 60, 60));
        g.setStroke(new BasicStroke(2.2f));
        g.drawPolygon(dreadnought);

        // Core power reactor (pulsing red/yellow)
        float reactorPulse = (float) (0.7 + 0.3 * Math.sin(System.currentTimeMillis() * 0.008));
        g.setColor(new Color(255, (int)(180 * reactorPulse), 20));
        g.fillOval(px + 50, py + 34, 16, 16);
        g.setColor(Color.WHITE);
        g.drawOval(px + 50, py + 34, 16, 16);

        // Wing cannons
        g.setColor(new Color(220, 220, 240));
        g.fillRect(px + 20, py + 52, 6, 18);
        g.fillRect(px + width - 26, py + 52, 6, 18);

        // Shield bubble
        if (shield > 0) {
            float ratio = (float) shield / maxShield;
            g.setColor(new Color(255, 50, 50, (int) (60 + 60 * ratio)));
            g.fillOval(px - 10, py - 10, width + 20, height + 20);
            g.setColor(new Color(255, 120, 120, (int) (120 * ratio)));
            g.setStroke(new BasicStroke(1.8f));
            g.drawOval(px - 10, py - 10, width + 20, height + 20);
        }

        // On-screen Boss Boss Bar (HUD top)
        renderBossHealthBar(g);
    }

    private void renderBossHealthBar(Graphics2D g) {
        int screenW = (g.getClipBounds() != null) ? g.getClipBounds().width : 800;
        int barW = Math.min(480, Math.max(300, screenW - 200));
        int barH = 14;
        int barX = (screenW - barW) / 2;
        int barY = 32;

        // Background container
        g.setColor(new Color(15, 20, 30, 210));
        g.fillRoundRect(barX - 4, barY - 18, barW + 8, barH + 26, 8, 8);
        g.setColor(new Color(200, 50, 50));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(barX - 4, barY - 18, barW + 8, barH + 26, 8, 8);

        // Boss Title
        g.setColor(new Color(255, 220, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("BOSS: " + bossName.toUpperCase(), barX + 6, barY - 4);

        // Health fill
        float healthRatio = (float) Math.max(0, health) / maxHealth;
        g.setColor(new Color(60, 60, 70));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(new Color(230, 40, 40));
        g.fillRect(barX, barY, (int) (barW * healthRatio), barH);

        // Shield fill line
        if (shield > 0) {
            float shieldRatio = (float) shield / maxShield;
            g.setColor(new Color(0, 180, 255));
            g.fillRect(barX, barY + barH - 3, (int) (barW * shieldRatio), 3);
        }
    }

    public String getBossName() { return bossName; }
    public int getAttackPhase() { return attackPhase; }
}

