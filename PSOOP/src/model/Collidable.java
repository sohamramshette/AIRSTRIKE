// ==============================================================================
// File      : PSOOP/src/model/Collidable.java
// Subject   : PSOOP-CO1 | Interfaces & AABB Collision Contract
// Topics    : [TOPIC: Interfaces] [TOPIC: Abstraction] [TOPIC: AABB Collision]
// Game Role : Defines bounding-box overlap contracts consumed by CollisionManager across entity types.
// ==============================================================================
package model;

import java.awt.Rectangle;

/**
 * Interface representing any geometric game element capable of collision detection.
 */
public interface Collidable {
    /**
     * [TOPIC: AABB Collision]
     * Game Role: Supplies an Axis-Aligned Bounding Box for fast overlap checks.
     * @return Bounding rectangle.
     */
    Rectangle getBounds();

    /**
     * [TOPIC: AABB Collision] [TOPIC: Polymorphism]
     * Game Role: Tests geometric intersection with any other Collidable entity.
     * @param other Target collidable object.
     * @return True if bounding boxes overlap.
     */
    boolean intersects(Collidable other);
}

