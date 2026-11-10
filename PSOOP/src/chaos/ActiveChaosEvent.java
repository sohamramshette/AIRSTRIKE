package chaos;

/**
 * Tracks the live lifecycle of a single triggered Chaos Protocol event.
 * Manages the 3-second warning phase, active duration progression,
 * and encapsulates state restoration parameters.
 */
public class ActiveChaosEvent {

    private final ChaosEventType type;
    private final double totalDuration;
    private double warningTimer;
    private double durationRemaining;
    private boolean warningAnnounced;
    private boolean activated;
    private boolean activationAnnounced;
    private ChaosEventType dualPartnerType;

    // Encapsulated restore cache to guarantee state safety
    private double cachedPlayerSpeed = -1.0;
    private long cachedShootCooldown = -1;
    private double cachedDamageMultiplier = 1.0;

    public ActiveChaosEvent(ChaosEventType type) {
        this(type, type.getDefaultDuration(), 3.0);
    }

    public ActiveChaosEvent(ChaosEventType type, double duration, double warningDuration) {
        this.type = type;
        this.totalDuration = Math.max(2.0, duration);
        this.durationRemaining = this.totalDuration;
        this.warningTimer = Math.max(0.0, warningDuration);
        this.warningAnnounced = false;
        this.activated = false;
    }

    public boolean update(double delta) {
        if (warningTimer > 0.0) {
            warningTimer -= delta;
            if (warningTimer <= 0.0) {
                warningTimer = 0.0;
                activated = true;
                return true;
            }
            return false;
        } else if (durationRemaining > 0.0) {
            durationRemaining -= delta;
            if (durationRemaining <= 0.0) {
                durationRemaining = 0.0;
            }
        }
        return false;
    }

    public boolean isWarning() {
        return warningTimer > 0.0;
    }

    public boolean isActive() {
        return warningTimer <= 0.0 && durationRemaining > 0.0;
    }

    public boolean isExpired() {
        return warningTimer <= 0.0 && durationRemaining <= 0.0;
    }

    public ChaosEventType getType() { return type; }
    public double getTotalDuration() { return totalDuration; }
    public double getWarningTimer() { return warningTimer; }
    public double getDurationRemaining() { return durationRemaining; }
    public float getProgress() {
        if (totalDuration <= 0.0) return 0f;
        return (float) Math.max(0.0, Math.min(1.0, durationRemaining / totalDuration));
    }

    public boolean isWarningAnnounced() { return warningAnnounced; }
    public void setWarningAnnounced(boolean announced) { this.warningAnnounced = announced; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public boolean isActivationAnnounced() { return activationAnnounced; }
    public void setActivationAnnounced(boolean announced) { this.activationAnnounced = announced; }

    public ChaosEventType getDualPartnerType() { return dualPartnerType; }
    public void setDualPartnerType(ChaosEventType partner) { this.dualPartnerType = partner; }

    // State restoration accessors
    public double getCachedPlayerSpeed() { return cachedPlayerSpeed; }
    public void setCachedPlayerSpeed(double speed) { this.cachedPlayerSpeed = speed; }

    public long getCachedShootCooldown() { return cachedShootCooldown; }
    public void setCachedShootCooldown(long cooldown) { this.cachedShootCooldown = cooldown; }

    public double getCachedDamageMultiplier() { return cachedDamageMultiplier; }
    public void setCachedDamageMultiplier(double multiplier) { this.cachedDamageMultiplier = multiplier; }
}
