package model;

import java.awt.Rectangle;

/**
 * Interface representing any geometric game element capable of collision detection.
 */
public interface Collidable {
    /**
     * Returns the bounding rectangle for Axis-Aligned Bounding Box (AABB) intersection tests.
     * @return Bounding rectangle.
     */
    Rectangle getBounds();

    /**
     * Tests whether this entity's bounding box intersects with another collidable object.
     * @param other Target collidable object.
     * @return True if bounding boxes overlap.
     */
    boolean intersects(Collidable other);
}

