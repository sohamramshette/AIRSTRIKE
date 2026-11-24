// ==============================================================================
// File      : PSOOP/src/model/Renderable.java
// Subject   : PSOOP-CO1 | Interfaces & Rendering Contract
// Topics    : [TOPIC: Interfaces] [TOPIC: Abstraction] [TOPIC: Polymorphism]
// Game Role : Establishes a uniform draw contract so GameCanvas renders all world objects polymorphically.
// ==============================================================================
package model;

import java.awt.Graphics2D;

/**
 * Interface representing any visual entity capable of being drawn onto an AWT Graphics2D surface.
 */
public interface Renderable {
    /**
     * [TOPIC: Abstraction] [TOPIC: Polymorphism]
     * Game Role: Renders this visual entity onto the active Graphics2D back-buffer.
     * @param g 2D graphics rendering context.
     */
    void render(Graphics2D g);
}

