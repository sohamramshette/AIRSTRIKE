package database.multiplayer;

import java.util.regex.Pattern;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 4): Multiplayer Input Validation & Security Guards
 * PACKAGE: database.multiplayer
 *
 * Demonstrates PSOOP-CO3:
 * - Defensive programming, strict contract verification, and sanitization.
 * ==============================================================================
 */
public final class MultiplayerValidation {

    private static final Pattern ROOM_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6}$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

    private MultiplayerValidation() {}

    /**
     * Sanitizes raw room code input:
     * Trims leading/trailing whitespace, converts to uppercase, and removes any non-alphanumeric chars.
     */
    public static String sanitizeRoomCode(String rawCode) {
        if (rawCode == null) return "";
        return rawCode.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    /**
     * Validates whether candidate string strictly matches the 6-character room code specification.
     */
    public static boolean isValidRoomCode(String code) {
        String sanitized = sanitizeRoomCode(code);
        return ROOM_CODE_PATTERN.matcher(sanitized).matches();
    }

    /**
     * Validates standard RFC-4122 UUID v4 formatting.
     */
    public static boolean isValidUUID(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return UUID_PATTERN.matcher(id.trim()).matches();
    }

    /**
     * Anti-Cheat physical rate bound:
     * In AIRSTRIKE combat, the maximum theoretical score accumulation cannot exceed:
     * score <= (durationSeconds * 450) + 2000
     */
    public static boolean isScoreWithinPhysicalBounds(int score, int durationSeconds) {
        if (score < 0 || durationSeconds <= 0) return false;
        long maxTheoretical = (long) durationSeconds * 450 + 2000;
        return score <= maxTheoretical;
    }
}

