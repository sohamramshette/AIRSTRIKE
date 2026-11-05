package service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedural asset manager generating multi-layered starfields and visual effects
 * without depending on external file assets.
 */
public class AssetManager {

    private static class Star {
        double x, y;
        double speed;
        int size;
        Color color;

        Star(double x, double y, double speed, int size, Color color) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.color = color;
        }
    }

    private final List<Star> stars;
    private final Random random;

    public AssetManager(int initialWidth, int initialHeight, int starCount) {
        this.stars = new ArrayList<>(starCount);
        this.random = new Random();
        initStarfield(initialWidth, initialHeight, starCount);
    }

    private void initStarfield(int width, int height, int count) {
        int w = Math.max(800, width);
        int h = Math.max(600, height);

        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * w;
            double y = random.nextDouble() * h;
            double layer = random.nextDouble();

            double speed;
            int size;
            Color color;

            if (layer < 0.6) {
                // Background distant stars: slow, small, dim
                speed = 25.0 + random.nextDouble() * 20.0;
                size = 1;
                int b = 140 + random.nextInt(60);
                color = new Color(b, b, b + 20, 160);
            } else if (layer < 0.9) {
                // Midground stars: medium speed, slight blue or cyan tint
                speed = 65.0 + random.nextDouble() * 30.0;
                size = 2;
                color = new Color(180, 220, 255, 210);
            } else {
                // Foreground stars: fast, bright white/yellow
                speed = 120.0 + random.nextDouble() * 40.0;
                size = 3;
                color = new Color(255, 250, 220, 255);
            }

            stars.add(new Star(x, y, speed, size, color));
        }
    }

    /**
     * Updates star positions and wraps stars around the screen vertically.
     */
    public void update(double delta, int width, int height) {
        int h = Math.max(600, height);
        int w = Math.max(800, width);

        for (Star s : stars) {
            s.y += s.speed * delta;
            if (s.y > h) {
                s.y = 0;
                s.x = random.nextDouble() * w;
            }
        }
    }

    /**
     * Renders space background and the parallax starfield.
     */
    public void renderBackground(Graphics2D g, int width, int height) {
        // Deep space void gradient
        g.setColor(new Color(8, 10, 18));
        g.fillRect(0, 0, width, height);

        // Distant subtle nebula clouds
        g.setColor(new Color(25, 15, 45, 60));
        g.fillOval(width / 4, height / 6, 400, 300);
        g.setColor(new Color(15, 35, 65, 50));
        g.fillOval(width / 2, height / 2, 450, 350);

        // Render stars
        for (Star s : stars) {
            g.setColor(s.color);
            g.fillRect((int) Math.round(s.x), (int) Math.round(s.y), s.size, s.size);
        }
    }
}
