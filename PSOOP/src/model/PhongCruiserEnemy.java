// ==============================================================================
// File      : PSOOP/src/model/PhongCruiserEnemy.java
// Subject   : PSOOP-CO1 / CGL-CO6 | Blinn-Phong Specular Lighting Cruiser
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Lighting Model] [TOPIC: Kinematics]
// Game Role : Modulates metallic hull specular highlights from the normalized player light vector.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Specular Phong Cruiser computing dynamic Blinn-Phong hull glint from player position.
 */
public class PhongCruiserEnemy extends Enemy {

    private double specularIntensity = 0.5;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a shielded metallic cruiser with default 0.5 specular intensity.
     */
    public PhongCruiserEnemy(double x, double y) {
        // x, y, width 52, height 48, maxHealth 105, maxShield 45, speed 75.0, shootCooldown 1750ms, scoreValue 460
        super(x, y, 52, 48, 105, 45, 75.0, 1750, 460);
        this.vy = speed * 0.7;
    }

    /** [TOPIC: Method Overriding] [TOPIC: Kinematics] Game Role: Integrates steady descent and sine sway. */
    @Override
    public void update(double delta) {
        flightTimer += delta * 1.5;
        y += vy * delta;
        x += Math.sin(flightTimer) * 18.0 * delta;
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: Lighting Model]
     * Game Role: Updates specularIntensity from player direction (N . L)^4 and fires on cooldown.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;

        if (player != null && player.isActive()) {
            double lx = player.getX() - (x + width / 2.0);
            double ly = player.getY() - (y + height / 2.0);
            double len = Math.sqrt(lx * lx + ly * ly);
            // WHY: Normalize light vector L (guarded against zero length) before evaluating N . L.
            if (len > 0.001) {
                lx /= len;
                ly /= len;
            }
            // WHY: Clamps dot product with downward normal N=(0,1) at 0 so ships behind the cruiser stay unlit.
            double nDotL = Math.max(0.0, ly);
            specularIntensity = Math.pow(nDotL, 4.0);
        }

        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Fires a central downward thermal bolt. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 290.0;
        int damage = 17;
        projectileList.add(new Projectile(x + width / 2.0 - 4, y + height, 8, 16, 0, bulletSpeed, false, damage));
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Lighting Model]
     * Game Role: Draws ambient hull fill, specular edge/hotspot alpha, and PHONG HUD readout.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Armored hull polygon
        int[] xs = { px + width / 2, px + width - 2, px + width - 8, px + 8, px + 2 };
        int[] ys = { py + height, py + 14, py + 2, py + 2, py + 14 };

        // Ambient base
        g.setColor(new Color(30, 40, 55));
        g.fillPolygon(xs, ys, 5);

        // WHY: Maps specularIntensity into [20, 220] alpha so hull outline keeps visible baseline contrast.
        int specAlpha = (int) Math.min(220, Math.max(20, specularIntensity * 255));
        g.setColor(new Color(255, 255, 255, specAlpha));
        g.setStroke(new BasicStroke(2.0f));
        g.drawPolygon(xs, ys, 5);

        // Specular hotspot
        int spotX = px + width / 2 - 4;
        int spotY = py + 8;
        g.setColor(new Color(255, 255, 255, specAlpha));
        g.fillOval(spotX, spotY, 8, 8);

        // HUD Phong label
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 8));
        g.setColor(new Color(200, 230, 255));
        g.drawString("PHONG:I_spec=" + String.format("%.2f", specularIntensity), px - 4, py - 6);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 3;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(120, 180, 255));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);

        if (maxShield > 0) {
            float shRatio = (float) shield / maxShield;
            g.setColor(new Color(200, 240, 255));
            g.fillRect(px, barY - 4, (int) (barW * shRatio), barH);
        }
    }

    public double getSpecularIntensity() { return specularIntensity; }
}
