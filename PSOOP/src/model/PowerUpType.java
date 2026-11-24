// ==============================================================================
// File      : PSOOP/src/model/PowerUpType.java
// Subject   : PSOOP-CO1 | Typed Enumeration for Combat Upgrades
// Topics    : [TOPIC: Enum] [TOPIC: Encapsulation]
// Game Role : Pairs each collectible power-up variant with its HUD label and render color.
// ==============================================================================
package model;

import java.awt.Color;

/**
 * Enumeration of available combat power-up enhancements.
 */
public enum PowerUpType {
    HEALTH_PACK("Health Repair", new Color(50, 220, 80)),
    SHIELD_BOOST("Shield Recharge", new Color(60, 160, 255)),
    WEAPON_UPGRADE("Plasma Cannon", new Color(255, 180, 40)),
    SPEED_BOOST("Hyper Engine", new Color(220, 70, 255));

    private final String label;
    private final Color color;

    /** [TOPIC: Encapsulation] Game Role: Binds immutable HUD label and tint color to each enum constant. */
    PowerUpType(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public Color getColor() { return color; }
}

