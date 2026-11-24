// ==============================================================================
// File      : PSOOP/src/service/ScoreCalculator.java
// Subject   : PSOOP-CO1 | Score Accumulation & Streak Multiplier Service
// Topics    : [TOPIC: Method Overloading] [TOPIC: Encapsulation] [TOPIC: Combo Multiplier]
// Game Role : Tracks match score, high score, and timed kill-streak multipliers.
// ==============================================================================
package service;

/**
 * [TOPIC: Method Overloading] [TOPIC: Encapsulation]
 * Service managing score accumulation, kill-streak combos, and session high scores.
 */
public class ScoreCalculator {

    private int score;
    private int highScore;
    private int killStreak;
    private int multiplier;
    private long lastKillTime;

    public ScoreCalculator() {
        this.score = 0;
        this.highScore = 0;
        this.killStreak = 0;
        this.multiplier = 1;
        this.lastKillTime = 0;
    }

    /**
     * [TOPIC: Method Overloading] Adds base score scaled by the active streak multiplier.
     */
    public void addScore(int baseScore) {
        updateStreak();
        int points = baseScore * multiplier;
        this.score += points;
        if (this.score > this.highScore) {
            this.highScore = this.score;
        }
    }

    /**
     * [TOPIC: Method Overloading] Adds base score scaled by an explicit bonus multiplier.
     */
    public void addScore(int baseScore, int customMultiplier) {
        // WHY: Clamp customMultiplier to >= 1 so bonuses never zero out or invert earned score.
        int activeMult = Math.max(1, customMultiplier);
        int points = baseScore * activeMult;
        this.score += points;
        if (this.score > this.highScore) {
            this.highScore = this.score;
        }
    }

    /**
     * [TOPIC: Combo Multiplier] Extends streak within a 3.5-second window and updates tier.
     */
    private void updateStreak() {
        long now = System.currentTimeMillis();
        // WHY: 3500 ms kill window keeps combos active during normal wave pacing before decaying to 1.
        if (now - lastKillTime <= 3500) {
            killStreak++;
        } else {
            killStreak = 1;
        }
        lastKillTime = now;

        // Multiplier tiers: 1x, 2x (3+ kills), 3x (8+ kills), 4x (15+ kills)
        if (killStreak >= 15) multiplier = 4;
        else if (killStreak >= 8) multiplier = 3;
        else if (killStreak >= 3) multiplier = 2;
        else multiplier = 1;
    }

    /**
     * Resets combo streak (called when player takes damage).
     */
    public void resetStreak() {
        this.killStreak = 0;
        this.multiplier = 1;
    }

    /**
     * Full score reset for new game sessions while preserving session high score.
     */
    public void resetGame() {
        this.score = 0;
        this.killStreak = 0;
        this.multiplier = 1;
        this.lastKillTime = 0;
    }

    public int getScore() { return score; }
    public int getHighScore() { return highScore; }
    public int getKillStreak() { return killStreak; }
    public int getMultiplier() { return multiplier; }
}

