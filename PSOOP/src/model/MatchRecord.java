// ==============================================================================
// File      : PSOOP/src/model/MatchRecord.java
// Subject   : PSOOP-CO1 | Match Telemetry Record & Formatting
// Topics    : [TOPIC: Encapsulation] [TOPIC: Method Overloading] [TOPIC: Defensive Programming]
// Game Role : Represents a persisted gameplay match record with safe duration and date formatting.
// ==============================================================================
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
    private final String username;
    private final int score;
    private final int levelReached;
    private final int durationSeconds;
    private final String createdAt;

    /** [TOPIC: Method Overloading] Game Role: Constructs a match record when username is omitted. */
    public MatchRecord(String id, String userId, int score, int levelReached, int durationSeconds, String createdAt) {
        this(id, userId, "", score, levelReached, durationSeconds, createdAt);
    }

    /**
     * [TOPIC: Encapsulation] [TOPIC: Defensive Programming]
     * Game Role: Initializes an immutable match record with null-safe string fields.
     */
    public MatchRecord(String id, String userId, String username, int score, int levelReached, int durationSeconds, String createdAt) {
        // WHY: Coerces null metadata strings to empty strings so UI renderers never throw NullPointerException.
        this.id = id != null ? id : "";
        this.userId = userId != null ? userId : "";
        this.username = username != null ? username : "";
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

    public String getUsername() {
        return username;
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
     * [TOPIC: Defensive Programming]
     * Game Role: Parses ISO-8601 timestamps into local display strings with safe fallback slicing.
     */
    public String getFormattedDate() {
        if (createdAt.isEmpty()) return "N/A";
        try {
            Instant instant = Instant.parse(createdAt);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());
            return formatter.format(instant);
        } catch (Exception e) {
            // WHY: Degrades gracefully to raw date/time substring when backend timestamp format varies.
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

