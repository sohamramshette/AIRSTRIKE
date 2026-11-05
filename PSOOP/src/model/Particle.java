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

    public Particle(double x, double y, double vx, double vy, double maxLife, Color baseColor, double size) {
        super(x, y, (int) Math.ceil(size), (int) Math.ceil(size));
        this.vx = vx;
        this.vy = vy;
        this.maxLife = Math.max(0.1, maxLife);
        this.life = this.maxLife;
        this.baseColor = baseColor;
        this.size = size;
    }

    @Override
    public void update(double delta) {
        x += vx * delta;
        y += vy * delta;
        vx *= 0.95; // Drag friction
        vy *= 0.95;
        life -= delta;
        if (life <= 0) {
            active = false;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!active || life <= 0) return;

        float alphaRatio = (float) Math.max(0.0, Math.min(1.0, life / maxLife));
        int alpha = (int) (255 * alphaRatio);
        Color renderColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);

        int currentSize = (int) Math.max(1, Math.round(size * alphaRatio));
        g.setColor(renderColor);
        g.fillOval((int) Math.round(x), (int) Math.round(y), currentSize, currentSize);
    }
}

