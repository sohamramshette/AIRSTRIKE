package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Model representing an authenticated gameplay match record stored in Supabase (public.matches).
 */
public class MatchRecord {

    private final String id;
    private final String userId;
    private final int score;
    private final int levelReached;
    private final int durationSeconds;
    private final String createdAt;

    public MatchRecord(String id, String userId, int score, int levelReached, int durationSeconds, String createdAt) {
        this.id = id != null ? id : "";
        this.userId = userId != null ? userId : "";
        this.score = score;
        this.levelReached = levelReached;
        this.durationSeconds = durationSeconds;
        this.createdAt = createdAt != null ? createdAt : "";
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public int getScore() {
        return score;
    }

    public int getLevelReached() {
        return levelReached;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Formats duration into MM:SS format.
     */
    public String getFormattedDuration() {
        int m = durationSeconds / 60;
        int s = durationSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    /**
     * Formats ISO 8601 timestamp into a clean human-readable date and time.
     */
    public String getFormattedDate() {
        if (createdAt.isEmpty()) return "N/A";
        try {
            Instant instant = Instant.parse(createdAt);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());
            return formatter.format(instant);
        } catch (Exception e) {
            // Fallback for custom or abbreviated timestamps
            if (createdAt.length() >= 16) {
                return createdAt.substring(0, 10) + " " + createdAt.substring(11, 16);
            }
            return createdAt;
        }
    }

    @Override
    public String toString() {
        return "MatchRecord{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", score=" + score +
                ", levelReached=" + levelReached +
                ", durationSeconds=" + durationSeconds +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
