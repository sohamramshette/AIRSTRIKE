package model;

import java.awt.Rectangle;

/**
 * Abstract root class representing any physical entity in the 2D space shooter world.
 * Demonstrates OOP Abstraction, Encapsulation, and Interface Implementation.
 */
public abstract class Entity implements Renderable, Collidable {
    
    // Encapsulated coordinates and dimensions
    protected double x;
    protected double y;
    protected double vx;
    protected double vy;
    protected int width;
    protected int height;
    protected boolean active;

    /**
     * Primary constructor with encapsulation boundary checks.
     */
    public Entity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.vx = 0.0;
        this.vy = 0.0;
        this.active = true;
    }

    /**
     * Updates physics, kinematics, and internal timers over elapsed delta seconds.
     * @param delta Elapsed time in seconds.
     */
    public abstract void update(double delta);

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    @Override
    public boolean intersects(Collidable other) {
        if (other == null) return false;
        return this.getBounds().intersects(other.getBounds());
    }

    // Encapsulated Getters and Setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }

    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = Math.max(1, width); }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = Math.max(1, height); }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }
}

