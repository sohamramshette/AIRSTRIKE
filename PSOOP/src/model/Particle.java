// ==============================================================================
// File      : PSOOP/src/model/Particle.java
// Subject   : PSOOP-CO1 | Visual Effect Particle & Decay Kinematics
// Topics    : [TOPIC: Inheritance] [TOPIC: Kinematics] [TOPIC: Method Overriding] [TOPIC: Defensive Programming]
// Game Role : Renders short-lived explosion sparks and impact particles with drag and alpha fade.
// ==============================================================================
package model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Animated particle for explosions, thruster trails, and combat impacts.
 */
public class Particle extends Entity {
    
    private double life;
    private final double maxLife;
    private final Color baseColor;
    private final double size;

    /**
     * [TOPIC: Inheritance] [TOPIC: Defensive Programming]
     * Game Role: Initializes a spark particle with clamped lifetime to guarantee safe fade ratios.
     */
    public Particle(double x, double y, double vx, double vy, double maxLife, Color baseColor, double size) {
        super(x, y, (int) Math.ceil(size), (int) Math.ceil(size));
        this.vx = vx;
        this.vy = vy;
        // WHY: Floor of 0.1s prevents division-by-zero when computing life / maxLife alpha ratio.
        this.maxLife = Math.max(0.1, maxLife);
        this.life = this.maxLife;
        this.baseColor = baseColor;
        this.size = size;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Kinematics]
     * Game Role: Integrates velocity with drag decay and deactivates expired sparks.
     */
    @Override
    public void update(double delta) {
        x += vx * delta;
        y += vy * delta;
        // WHY: Multiplicative damping simulates atmospheric drag so explosion bursts decelerate naturally.
        vx *= 0.95;
        vy *= 0.95;
        life -= delta;
        if (life <= 0) {
            active = false;
        }
    }

    /**
     * [TOPIC: Method Overriding]
     * Game Role: Draws size- and alpha-attenuated spark oval based on remaining lifetime.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active || life <= 0) return;

        // WHY: Clamps ratio strictly within [0, 1] so AWT Color alpha never exceeds [0, 255].
        float alphaRatio = (float) Math.max(0.0, Math.min(1.0, life / maxLife));
        int alpha = (int) (255 * alphaRatio);
        Color renderColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);

        int currentSize = (int) Math.max(1, Math.round(size * alphaRatio));
        g.setColor(renderColor);
        g.fillOval((int) Math.round(x), (int) Math.round(y), currentSize, currentSize);
    }
}

