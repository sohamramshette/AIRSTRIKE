// ==============================================================================
// File      : PSOOP/src/announcer/CommentaryFrequency.java
// Subject   : PSOOP-CO1 | Announcer Cadence Preset Enumeration
// Topics    : [TOPIC: Enumerations] [TOPIC: Rate Limiting]
// Game Role : Defines minimum cooldown and random banter interval presets.
// ==============================================================================
package announcer;

public enum CommentaryFrequency {
    LOW("Low (Relaxed)", 45.0, 90.0),
    NORMAL("Normal (Balanced)", 26.0, 55.0),
    HIGH("High (Active Banter)", 14.0, 28.0);

    private final String label;
    private final double minCooldown;
    private final double randomInterval;

    CommentaryFrequency(String label, double minCooldown, double randomInterval) {
        this.label = label;
        this.minCooldown = minCooldown;
        this.randomInterval = randomInterval;
    }

    public String getLabel() {
        return label;
    }

    public double getMinCooldown() {
        return minCooldown;
    }

    public double getRandomInterval() {
        return randomInterval;
    }
}

