// ==============================================================================
// File      : PSOOP/src/model/SubRegSentryEnemy.java
// Subject   : PSOOP-CO1 / COA-CO1 | Sub-Register Shield Tier Sentry
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Enum] [TOPIC: Register Hierarchy]
// Game Role : Maps shield strength across 80386 register tiers (EAX -> AX -> AH -> AL -> DEPLETED).
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Sub-Register Aegis Sentry visualizing x86 register bit-width tiers as shield layers.
 */
public class SubRegSentryEnemy extends Enemy {

    public enum RegisterTier { EAX_DWORD, AX_WORD, AH_BYTE, AL_BYTE, DEPLETED }
    private RegisterTier currentTier = RegisterTier.EAX_DWORD;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a 60-shield sentry starting in the 32-bit EAX shield tier.
     */
    public SubRegSentryEnemy(double x, double y) {
        // x, y, width 50, height 46, maxHealth 100, maxShield 60, speed 75.0, shootCooldown 1700ms, scoreValue 450
        super(x, y, 50, 46, 100, 60, 75.0, 1700, 450);
        this.vy = speed * 0.7;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Register Hierarchy] [TOPIC: Kinematics]
     * Game Role: Moves the sentry and updates RegisterTier as shield drops across 15-point thresholds.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.4;
        y += vy * delta;
        x += Math.sin(flightTimer) * 20.0 * delta;

        // WHY: Partitions 60 maxShield into four 15-point bands mirroring 32b/16b/8b register collapse.
        if (shield > 45) {
            currentTier = RegisterTier.EAX_DWORD;
        } else if (shield > 30) {
            currentTier = RegisterTier.AX_WORD;
        } else if (shield > 15) {
            currentTier = RegisterTier.AH_BYTE;
        } else if (shield > 0) {
            currentTier = RegisterTier.AL_BYTE;
        } else {
            currentTier = RegisterTier.DEPLETED;
        }
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires heavy bolt inside active corridor. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 20 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Discharges a central downward energy bolt. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 280.0;
        int damage = 16;
        projectileList.add(new Projectile(x + width / 2.0 - 4, y + height, 8, 16, 0, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Draws golden hull, tier-scaled shield ring, and REG HUD tag. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Core Golden Sentry Hull
        int[] xs = { px + width / 2, px + width - 4, px + width - 12, px + 12, px + 4 };
        int[] ys = { py + height, py + 8, py + 2, py + 2, py + 8 };
        g.setColor(new Color(60, 45, 10));
        g.fillPolygon(xs, ys, 5);
        g.setColor(new Color(255, 190, 0));
        g.setStroke(new BasicStroke(1.6f));
        g.drawPolygon(xs, ys, 5);

        // Concentric Register Shield Rings
        if (shield > 0) {
            g.setColor(new Color(255, 215, 0, 60));
            int radiusPad = switch (currentTier) {
                case EAX_DWORD -> 8;
                case AX_WORD -> 6;
                case AH_BYTE -> 4;
                case AL_BYTE -> 2;
                case DEPLETED -> 0;
            };
            g.drawOval(px - radiusPad, py - radiusPad, width + radiusPad * 2, height + radiusPad * 2);
        }

        // Live CPU Register Telemetry
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(255, 220, 80));
        String regLabel = switch (currentTier) {
            case EAX_DWORD -> "REG:EAX[32b]";
            case AX_WORD -> "REG:AX[16b]";
            case AH_BYTE -> "REG:AH[8b]";
            case AL_BYTE -> "REG:AL[8b]";
            case DEPLETED -> "REG:OFFLINE";
        };
        g.drawString(regLabel, px - 2, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 4;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(255, 190, 0));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);

        if (maxShield > 0) {
            float shRatio = (float) shield / maxShield;
            g.setColor(new Color(255, 230, 100));
            g.fillRect(px, barY - 4, (int) (barW * shRatio), barH);
        }
    }

    public RegisterTier getCurrentTier() { return currentTier; }
}
