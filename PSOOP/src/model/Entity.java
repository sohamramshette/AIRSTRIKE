// ==============================================================================
// File      : PSOOP/src/model/Entity.java
// Subject   : PSOOP-CO1 | Abstract Base Class & Encapsulation
// Topics    : [TOPIC: Abstraction] [TOPIC: Encapsulation] [TOPIC: Interfaces] [TOPIC: Kinematics]
// Game Role : Provides shared 2D coordinates, velocity, AABB hitboxes, and lifecycle flags for all entities.
// ==============================================================================
package model;

import java.awt.Rectangle;

/**
 * Abstract root class representing any physical entity in the 2D space shooter world.
 */
public abstract class Entity implements Renderable, Collidable {
    
    protected double x;
    protected double y;
    protected double vx;
    protected double vy;
    protected int width;
    protected int height;
    protected boolean active;

    /**
     * [TOPIC: Encapsulation] [TOPIC: Defensive Programming]
     * Game Role: Initializes entity coordinates and clamps dimensions to positive hitbox bounds.
     */
    public Entity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        // WHY: Enforces >= 1px bounds so zero/negative sizes never invalidate AABB collision checks.
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.vx = 0.0;
        this.vy = 0.0;
        this.active = true;
    }

    /**
     * [TOPIC: Abstraction] [TOPIC: Kinematics]
     * Game Role: Advances entity motion and internal timers over elapsed frame delta seconds.
     * @param delta Elapsed time in seconds.
     */
    public abstract void update(double delta);

    /**
     * [TOPIC: Method Overriding] [TOPIC: AABB Collision]
     * Game Role: Projects floating-point world coordinates onto an integer AABB hitbox.
     */
    @Override
    public Rectangle getBounds() {
        // WHY: Rounds continuous double coordinates to nearest pixel for accurate AWT Rectangle intersection.
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: Defensive Programming]
     * Game Role: Executes null-safe AABB collision detection against another Collidable object.
     */
    @Override
    public boolean intersects(Collidable other) {
        // WHY: Guards against null targets so collision loops never dereference an empty slot.
        if (other == null) return false;
        return this.getBounds().intersects(other.getBounds());
    }

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

