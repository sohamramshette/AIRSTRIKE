package service;

/**
 * Service managing player score accumulation, kill-streak combos, and high score calculation.
 * Demonstrates Method Overloading and Encapsulation.
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
     * Method Overloading #1: Adds base score with automatic combo multiplier calculation.
     * @param baseScore Points awarded for destroyed unit.
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
     * Method Overloading #2: Adds score with an explicit custom bonus multiplier.
     * @param baseScore Base points.
     * @param customMultiplier Explicit multiplier (e.g. boss defeat or precision bonus).
     */
    public void addScore(int baseScore, int customMultiplier) {
        int activeMult = Math.max(1, customMultiplier);
        int points = baseScore * activeMult;
        this.score += points;
        if (this.score > this.highScore) {
            this.highScore = this.score;
        }
    }

    private void updateStreak() {
        long now = System.currentTimeMillis();
        // If kill happens within 3.5 seconds of previous kill, increment streak
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

