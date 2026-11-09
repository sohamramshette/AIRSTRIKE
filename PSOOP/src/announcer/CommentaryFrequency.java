package announcer;

public enum CommentaryFrequency {
    LOW("Low (Relaxed)", 18.0, 30.0),
    NORMAL("Normal (Balanced)", 8.5, 18.0),
    HIGH("High (Non-stop Desi Banter)", 4.5, 10.0);

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
