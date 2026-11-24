// ==============================================================================
// File      : PSOOP/src/model/PowerUp.java
// Subject   : PSOOP-CO1 | Collectible Power-Up Capsule
// Topics    : [TOPIC: Inheritance] [TOPIC: Composition] [TOPIC: Kinematics] [TOPIC: Method Overriding]
// Game Role : Descends with a pulsing sway to grant health, shield, weapon, or speed upgrades.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Collectible power-up capsule spawned upon destroying enemy units.
 */
public class PowerUp extends Entity {
    
    private final PowerUpType type;
    private double pulseTimer;
    private final double initialX;

    /**
     * [TOPIC: Inheritance] [TOPIC: Composition]
     * Game Role: Spawns a 26x26 typed upgrade capsule descending at constant drift speed.
     */
    public PowerUp(double x, double y, PowerUpType type) {
        super(x, y, 26, 26);
        this.type = type;
        this.vy = 85.0; // Slow descent
        this.initialX = x;
        this.pulseTimer = Math.random() * Math.PI * 2;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Kinematics]
     * Game Role: Advances downward drift and sinusoidal lateral oscillation over delta seconds.
     */
    @Override
    public void update(double delta) {
        pulseTimer += delta * 4.0;
        y += vy * delta;
        // WHY: Computes position directly around initialX so lateral sway never drifts across the arena.
        x = initialX + Math.sin(pulseTimer) * 12.0;
    }

    /**
     * [TOPIC: Method Overriding]
     * Game Role: Renders pulsing aura, capsule frame, and PowerUpType glyph.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Pulsing glow aura
        float pulse = (float) (0.6 + 0.4 * Math.sin(pulseTimer));
        Color c = type.getColor();
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (100 * pulse)));
        g.fillOval(px - 4, py - 4, width + 8, height + 8);

        // Hexagonal / Diamond capsule background
        g.setColor(new Color(20, 25, 40, 220));
        g.fillRoundRect(px, py, width, height, 8, 8);

        // Border outline
        g.setColor(c);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(px, py, width, height, 8, 8);

        // Icon symbol
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        String symbol = switch (type) {
            case HEALTH_PACK -> "+";
            case SHIELD_BOOST -> "S";
            case WEAPON_UPGRADE -> "W";
            case SPEED_BOOST -> "^";
        };
        int textW = g.getFontMetrics().stringWidth(symbol);
        g.drawString(symbol, px + (width - textW) / 2, py + height - 7);
    }

    public PowerUpType getType() { return type; }
}

