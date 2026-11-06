package model;

/**
 * Model representing a high-score ranking entry from Supabase (public.leaderboard).
 */
public class LeaderboardEntry {

    private final int rank;
    private final String username;
    private final int bestScore;

    public LeaderboardEntry(int rank, String username, int bestScore) {
        this.rank = rank;
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

