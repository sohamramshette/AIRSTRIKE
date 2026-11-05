package model;

import java.awt.Graphics2D;

/**
 * Interface representing any visual entity capable of being drawn onto an AWT Graphics2D surface.
 */
public interface Renderable {
    /**
     * Draws this object using the provided Graphics2D context.
     * @param g 2D graphics rendering context.
     */
    void render(Graphics2D g);
}

