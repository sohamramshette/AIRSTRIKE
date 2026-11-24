// ==============================================================================
// File      : PSOOP/src/model/LeaderboardEntry.java
// Subject   : PSOOP-CO1 | Immutable Leaderboard Value Object
// Topics    : [TOPIC: Encapsulation] [TOPIC: Value Object] [TOPIC: Defensive Programming]
// Game Role : Holds ranked player high-score rows retrieved from Supabase for UI display.
// ==============================================================================
package model;

/**
 * Model representing a high-score ranking entry from Supabase (public.leaderboard).
 */
public class LeaderboardEntry {

    private final int rank;
    private final String username;
    private final int bestScore;

    /**
     * [TOPIC: Encapsulation] [TOPIC: Defensive Programming]
     * Game Role: Constructs an immutable ranking row while normalizing blank pilot names.
     */
    public LeaderboardEntry(int rank, String username, int bestScore) {
        this.rank = rank;
        // WHY: Ensures missing or whitespace-only usernames display safely without UI blanks.
        this.username = (username != null && !username.trim().isEmpty()) ? username.trim() : "Anonymous Pilot";
        this.bestScore = bestScore;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getBestScore() {
        return bestScore;
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{" +
                "rank=" + rank +
                ", username='" + username + '\'' +
                ", bestScore=" + bestScore +
                '}';
    }
}

