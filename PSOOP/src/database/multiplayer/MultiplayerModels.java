package database.multiplayer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 4): Multiplayer Domain Models & Contracts
 * PACKAGE: database.multiplayer
 *
 * Demonstrates PSOOP-CO1:
 * - Robust object-oriented modeling with complete data encapsulation.
 * - Defensive copy semantics, immutable value objects, and typed listeners.
 * ==============================================================================
 */
public final class MultiplayerModels {

    private MultiplayerModels() {}

    /**
     * Authoritative lifecycle states of a 1v1 multiplayer combat room.
     * Aligns 1:1 with PostgreSQL ENUM 'multiplayer_room_status' and TypeScript contracts.
     */
    public enum RoomStatus {
        WAITING,
        READY,
        COUNTDOWN,
        ACTIVE,
        COMPLETED,
        CANCELLED;

        public static RoomStatus fromString(String val) {
            if (val == null) return WAITING;
            try {
                return RoomStatus.valueOf(val.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return WAITING;
            }
        }
    }

    /**
     * Immutable snapshot representing authoritative room state.
     */
    public static class MultiplayerRoomState {
        private final String roomId;
        private final String roomCode;
        private final String hostId;
        private final String hostUsername;
        private final boolean hostReady;
        private final String guestId;
        private final String guestUsername;
        private final boolean guestReady;
        private final long matchSeed;
        private final RoomStatus status;
        private final boolean isHost;
        private final boolean isGuest;
        private final String createdAt;
        private final String expiresAt;

        public MultiplayerRoomState(String roomId, String roomCode, String hostId, String hostUsername,
                                    boolean hostReady, String guestId, String guestUsername, boolean guestReady,
                                    long matchSeed, RoomStatus status, boolean isHost, boolean isGuest,
                                    String createdAt, String expiresAt) {
            this.roomId = roomId != null ? roomId : "";
            this.roomCode = roomCode != null ? roomCode.toUpperCase() : "";
            this.hostId = hostId != null ? hostId : "";
            this.hostUsername = hostUsername != null ? hostUsername : "Host Pilot";
            this.hostReady = hostReady;
            this.guestId = (guestId != null && !guestId.trim().isEmpty()) ? guestId.trim() : null;
            this.guestUsername = (guestUsername != null && !guestUsername.trim().isEmpty()) ? guestUsername.trim() : null;
            this.guestReady = guestReady;
            this.matchSeed = matchSeed;
            this.status = status != null ? status : RoomStatus.WAITING;
            this.isHost = isHost;
            this.isGuest = isGuest;
            this.createdAt = createdAt != null ? createdAt : "";
            this.expiresAt = expiresAt != null ? expiresAt : "";
        }

        public String getRoomId() { return roomId; }
        public String getRoomCode() { return roomCode; }
        public String getHostId() { return hostId; }
        public String getHostUsername() { return hostUsername; }
        public boolean isHostReady() { return hostReady; }
        public String getGuestId() { return guestId; }
        public String getGuestUsername() { return guestUsername; }
        public boolean isGuestReady() { return guestReady; }
        public long getMatchSeed() { return matchSeed; }
        public RoomStatus getStatus() { return status; }
        public boolean isHost() { return isHost; }
        public boolean isGuest() { return isGuest; }
        public String getCreatedAt() { return createdAt; }
        public String getExpiresAt() { return expiresAt; }

        public boolean isBothReady() {
            return hostReady && guestReady;
        }

        public boolean hasGuest() {
            return guestId != null && !guestId.isEmpty();
        }

        @Override
        public String toString() {
            return "MultiplayerRoomState{" +
                    "code='" + roomCode + '\'' +
                    ", status=" + status +
                    ", host='" + hostUsername + '\'' + " (ready=" + hostReady + ")" +
                    ", guest='" + guestUsername + '\'' + " (ready=" + guestReady + ")" +
                    '}';
        }
    }

    /**
     * Ephemeral Realtime Presence snapshot for a connected pilot.
     */
    public static class PilotPresenceState {
        private final String userId;
        private final String callsign;
        private final String presenceStatus; // "online", "disconnected", "reconnecting"
        private final boolean isReady;
        private final long joinedAt;

        public PilotPresenceState(String userId, String callsign, String presenceStatus, boolean isReady, long joinedAt) {
            this.userId = userId != null ? userId : "";
            this.callsign = callsign != null ? callsign : "Cadet";
            this.presenceStatus = presenceStatus != null ? presenceStatus : "online";
            this.isReady = isReady;
            this.joinedAt = joinedAt > 0 ? joinedAt : System.currentTimeMillis();
        }

        public String getUserId() { return userId; }
        public String getCallsign() { return callsign; }
        public String getPresenceStatus() { return presenceStatus; }
        public boolean isReady() { return isReady; }
        public long getJoinedAt() { return joinedAt; }

        public boolean isOnline() {
            return "online".equalsIgnoreCase(presenceStatus);
        }

        public PilotPresenceState withReady(boolean newReady) {
            return new PilotPresenceState(userId, callsign, presenceStatus, newReady, joinedAt);
        }

        public PilotPresenceState withStatus(String newStatus) {
            return new PilotPresenceState(userId, callsign, newStatus, isReady, joinedAt);
        }

        @Override
        public String toString() {
            return "PilotPresenceState{" +
                    "callsign='" + callsign + '\'' +
                    ", status='" + presenceStatus + '\'' +
                    ", ready=" + isReady +
                    '}';
        }
    }

    /**
     * Result envelope for asynchronous room operations.
     */
    public static class RoomOperationResult<T> {
        private final boolean success;
        private final T data;
        private final String error;
        private final String errorCode;

        private RoomOperationResult(boolean success, T data, String error, String errorCode) {
            this.success = success;
            this.data = data;
            this.error = error;
            this.errorCode = errorCode;
        }

        public static <T> RoomOperationResult<T> ok(T data) {
            return new RoomOperationResult<>(true, data, null, null);
        }

        public static <T> RoomOperationResult<T> fail(String error, String errorCode) {
            return new RoomOperationResult<>(false, null, error, errorCode);
        }

        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getError() { return error; }
        public String getErrorCode() { return errorCode; }
    }

    /**
     * Remote Pilot representation (PSOOP-CO1: Object-oriented modeling).
     */
    public static class RemotePilotModel {
        private final String userId;
        private final String callsign;
        private final boolean host;
        private boolean ready;
        private boolean connected;
        private int currentScore;
        private int health;

        public RemotePilotModel(String userId, String callsign, boolean host) {
            this.userId = Objects.requireNonNull(userId, "userId cannot be null");
            this.callsign = Objects.requireNonNull(callsign, "callsign cannot be null");
            this.host = host;
            this.ready = false;
            this.connected = true;
            this.currentScore = 0;
            this.health = 100;
        }

        public String getUserId() { return userId; }
        public String getCallsign() { return callsign; }
        public boolean isHost() { return host; }
        public boolean isReady() { return ready; }
        public void setReady(boolean ready) { this.ready = ready; }
        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
        public int getCurrentScore() { return currentScore; }
        public void setCurrentScore(int score) { this.currentScore = Math.max(0, score); }
        public int getHealth() { return health; }
        public void setHealth(int health) { this.health = Math.max(0, Math.min(100, health)); }
    }

    /**
     * Observers for reactive UI updates on the Swing Event Dispatch Thread.
     */
    @FunctionalInterface
    public interface RoomStateListener {
        void onRoomStateChanged(MultiplayerRoomState room);
    }

    @FunctionalInterface
    public interface PresenceListener {
        void onPresenceChanged(Map<String, PilotPresenceState> presences);
    }

    @FunctionalInterface
    public interface BroadcastListener {
        void onBroadcastReceived(String event, Map<String, Object> payload);
    }
}

