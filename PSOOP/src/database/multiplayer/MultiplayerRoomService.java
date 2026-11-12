package database.multiplayer;

import database.SessionManager;
import database.SimpleJson;
import database.SupabaseConfig;
import database.SupabaseHttpClient;
import database.UserSession;
import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.PilotPresenceState;
import database.multiplayer.MultiplayerModels.RoomOperationResult;
import database.multiplayer.MultiplayerModels.RoomStateListener;
import database.multiplayer.MultiplayerModels.RoomStatus;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 4): High-Level Multiplayer Room Service Coordinator
 * PACKAGE: database.multiplayer
 *
 * Coordinates:
 * - Supabase PostgreSQL stored procedures (RPCs) over SupabaseHttpClient.
 * - Ephemeral Realtime Presence and Broadcast over MultiplayerPresenceService.
 * - Deterministic state machine transitions over MultiplayerStateManager.
 * ==============================================================================
 */
public class MultiplayerRoomService {

    private static volatile MultiplayerRoomService instance;

    private final SupabaseHttpClient httpClient;
    private final SessionManager sessionManager;
    private final MultiplayerPresenceService presenceService;
    private final MultiplayerStateManager stateManager;

    private final Set<RoomStateListener> roomListeners = new CopyOnWriteArraySet<>();

    public MultiplayerRoomService() {
        this(new SupabaseHttpClient(), SessionManager.getInstance(),
                MultiplayerPresenceService.getInstance(), new MultiplayerStateManager());
    }

    public MultiplayerRoomService(SupabaseHttpClient httpClient, SessionManager sessionManager,
                                  MultiplayerPresenceService presenceService, MultiplayerStateManager stateManager) {
        this.httpClient = httpClient;
        this.sessionManager = sessionManager;
        this.presenceService = presenceService;
        this.stateManager = stateManager;

        // Wire ephemeral broadcast listeners to keep state synchronized with peer events
        setupBroadcastHandlers();
    }

    public static MultiplayerRoomService getInstance() {
        if (instance == null) {
            synchronized (MultiplayerRoomService.class) {
                if (instance == null) {
                    instance = new MultiplayerRoomService();
                }
            }
        }
        return instance;
    }

    private void setupBroadcastHandlers() {
        // 1. Guest Joined broadcast from peer
        presenceService.addBroadcastListener("GUEST_JOINED", (event, payload) -> {
            MultiplayerRoomState current = stateManager.getCurrentRoom();
            if (current != null && current.isHost()) {
                String guestId = SimpleJson.getString(payload, "guestId");
                String guestUsername = SimpleJson.getString(payload, "guestUsername");
                MultiplayerRoomState updated = new MultiplayerRoomState(
                        current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                        current.isHostReady(), guestId, guestUsername, false,
                        current.getMatchSeed(), RoomStatus.READY, true, false,
                        current.getCreatedAt(), current.getExpiresAt()
                );
                stateManager.updateState(updated);
                notifyRoomListeners(updated);
            }
        });

        // 2. Ready change broadcast from peer
        presenceService.addBroadcastListener("READY_CHANGE", (event, payload) -> {
            MultiplayerRoomState current = stateManager.getCurrentRoom();
            if (current != null) {
                boolean hostReady = SimpleJson.getBoolean(payload, "hostReady", current.isHostReady());
                boolean guestReady = SimpleJson.getBoolean(payload, "guestReady", current.isGuestReady());
                String statusStr = SimpleJson.getString(payload, "status");
                RoomStatus status = statusStr != null ? RoomStatus.fromString(statusStr) : current.getStatus();

                MultiplayerRoomState updated = new MultiplayerRoomState(
                        current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                        hostReady, current.getGuestId(), current.getGuestUsername(), guestReady,
                        current.getMatchSeed(), status, current.isHost(), current.isGuest(),
                        current.getCreatedAt(), current.getExpiresAt()
                );
                stateManager.updateState(updated);
                notifyRoomListeners(updated);
            }
        });

        // 3. Room cancelled / peer departed broadcast
        presenceService.addBroadcastListener("ROOM_CANCELLED", (event, payload) -> {
            MultiplayerRoomState current = stateManager.getCurrentRoom();
            if (current != null) {
                if (current.isHost()) {
                    // Challenger left: reset host lobby to WAITING
                    MultiplayerRoomState updated = new MultiplayerRoomState(
                            current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                            false, null, null, false,
                            current.getMatchSeed(), RoomStatus.WAITING, true, false,
                            current.getCreatedAt(), current.getExpiresAt()
                    );
                    stateManager.updateState(updated);
                    notifyRoomListeners(updated);
                } else {
                    // Host cancelled room: guest must exit
                    MultiplayerRoomState updated = new MultiplayerRoomState(
                            current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                            false, current.getGuestId(), current.getGuestUsername(), false,
                            current.getMatchSeed(), RoomStatus.CANCELLED, false, true,
                            current.getCreatedAt(), current.getExpiresAt()
                    );
                    stateManager.updateState(updated);
                    notifyRoomListeners(updated);
                }
            }
        });

        // 4. Match Started broadcast from peer/host
        presenceService.addBroadcastListener("MATCH_STARTED", (event, payload) -> {
            MultiplayerRoomState current = stateManager.getCurrentRoom();
            if (current != null) {
                String startedAt = SimpleJson.getString(payload, "startedAt");
                long seed = SimpleJson.getLong(payload, "matchSeed", current.getMatchSeed());
                MultiplayerRoomState updated = new MultiplayerRoomState(
                        current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                        current.isHostReady(), current.getGuestId(), current.getGuestUsername(), current.isGuestReady(),
                        seed, RoomStatus.ACTIVE, current.isHost(), current.isGuest(),
                        current.getCreatedAt(), current.getExpiresAt(), startedAt, 90, null, null, null
                );
                stateManager.updateState(updated);
                notifyRoomListeners(updated);
            }
        });

        // 5. Match Finalized broadcast from peer
        presenceService.addBroadcastListener("MATCH_FINALIZED", (event, payload) -> {
            MultiplayerRoomState current = stateManager.getCurrentRoom();
            if (current != null) {
                String winnerId = SimpleJson.getString(payload, "winnerId");
                int hScore = SimpleJson.getInt(payload, "hostScore", 0);
                int gScore = SimpleJson.getInt(payload, "guestScore", 0);
                MultiplayerRoomState updated = new MultiplayerRoomState(
                        current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                        current.isHostReady(), current.getGuestId(), current.getGuestUsername(), current.isGuestReady(),
                        current.getMatchSeed(), RoomStatus.COMPLETED, current.isHost(), current.isGuest(),
                        current.getCreatedAt(), current.getExpiresAt(), current.getStartedAt(), current.getDurationSeconds(),
                        hScore, gScore, winnerId
                );
                stateManager.updateState(updated);
                notifyRoomListeners(updated);
            }
        });
    }

    /**
     * Creates a new private 1v1 multiplayer combat room.
     */
    public CompletableFuture<RoomOperationResult<MultiplayerRoomState>> createRoom() {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            return CompletableFuture.completedFuture(
                    RoomOperationResult.fail("Authentication required. Please log in as a registered Starfleet Pilot.", "UNAUTHENTICATED")
            );
        }

        return httpClient.post("/rest/v1/rpc/create_multiplayer_room", "{}", session.getAccessToken())
                .thenApply(response -> handleCreateRoomResponse(response, session))
                .exceptionally(ex -> RoomOperationResult.fail("Network error creating combat room: " + ex.getMessage(), "NETWORK_ERROR"));
    }

    private RoomOperationResult<MultiplayerRoomState> handleCreateRoomResponse(HttpResponse<String> response, UserSession session) {
        if (response.statusCode() >= 400) {
            Map<String, Object> errMap = SimpleJson.parseObject(response.body());
            String msg = SimpleJson.getString(errMap, "message");
            return RoomOperationResult.fail(msg != null ? msg : "Failed to create combat room (HTTP " + response.statusCode() + ")", "RPC_ERROR");
        }

        Map<String, Object> data = SimpleJson.parseObject(response.body());
        String roomId = SimpleJson.getString(data, "room_id");
        String roomCode = SimpleJson.getString(data, "room_code");
        String hostId = SimpleJson.getString(data, "host_id");
        String hostUsername = SimpleJson.getString(data, "host_username");
        long matchSeed = SimpleJson.getLong(data, "match_seed", System.currentTimeMillis());
        String statusStr = SimpleJson.getString(data, "status");
        String createdAt = SimpleJson.getString(data, "created_at");
        String expiresAt = SimpleJson.getString(data, "expires_at");

        MultiplayerRoomState state = new MultiplayerRoomState(
                roomId, roomCode, hostId, (hostUsername != null ? hostUsername : session.getUsername()),
                false, null, null, false,
                matchSeed, RoomStatus.fromString(statusStr), true, false,
                createdAt, expiresAt
        );

        stateManager.updateState(state);
        notifyRoomListeners(state);

        // Connect presence in background
        subscribeToRoom(roomId, roomCode);

        return RoomOperationResult.ok(state);
    }

    /**
     * Joins an existing room using a 6-character room code.
     */
    public CompletableFuture<RoomOperationResult<MultiplayerRoomState>> joinRoom(String roomCode) {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            return CompletableFuture.completedFuture(
                    RoomOperationResult.fail("Authentication required. Please log in as a registered Starfleet Pilot.", "UNAUTHENTICATED")
            );
        }

        final String sanitized = MultiplayerValidation.sanitizeRoomCode(roomCode);
        if (!MultiplayerValidation.isValidRoomCode(sanitized)) {
            return CompletableFuture.completedFuture(
                    RoomOperationResult.fail("Invalid room code format. Must be a 6-character alphanumeric code.", "INVALID_CODE_FORMAT")
            );
        }

        String payload = SimpleJson.toJson(Map.of("p_room_code", sanitized));
        return httpClient.post("/rest/v1/rpc/join_multiplayer_room", payload, session.getAccessToken())
                .thenApply(response -> handleJoinRoomResponse(response, session, sanitized))
                .exceptionally(ex -> RoomOperationResult.fail("Network error joining combat room: " + ex.getMessage(), "NETWORK_ERROR"));
    }

    private RoomOperationResult<MultiplayerRoomState> handleJoinRoomResponse(HttpResponse<String> response, UserSession session, String sanitizedCode) {
        if (response.statusCode() >= 400) {
            Map<String, Object> errMap = SimpleJson.parseObject(response.body());
            String msg = SimpleJson.getString(errMap, "message");
            return RoomOperationResult.fail(msg != null ? msg : "Failed to join combat room (HTTP " + response.statusCode() + ")", "RPC_ERROR");
        }

        Map<String, Object> data = SimpleJson.parseObject(response.body());
        String roomId = SimpleJson.getString(data, "room_id");
        String roomCode = SimpleJson.getString(data, "room_code");
        String hostId = SimpleJson.getString(data, "host_id");
        String hostUsername = SimpleJson.getString(data, "host_username");
        String guestId = SimpleJson.getString(data, "guest_id");
        String guestUsername = SimpleJson.getString(data, "guest_username");
        long matchSeed = SimpleJson.getLong(data, "match_seed", 0);
        String statusStr = SimpleJson.getString(data, "status");

        MultiplayerRoomState state = new MultiplayerRoomState(
                roomId, (roomCode != null ? roomCode : sanitizedCode), hostId, hostUsername,
                false, guestId, (guestUsername != null ? guestUsername : session.getUsername()), false,
                matchSeed, RoomStatus.fromString(statusStr), false, true,
                "", ""
        );

        stateManager.updateState(state);
        notifyRoomListeners(state);

        // Connect presence in background
        subscribeToRoom(roomId, state.getRoomCode()).thenRun(() -> {
            // Broadcast GUEST_JOINED
            presenceService.broadcast("GUEST_JOINED", Map.of(
                    "roomId", roomId,
                    "guestId", session.getUserId(),
                    "guestUsername", session.getUsername()
            ));
        });

        return RoomOperationResult.ok(state);
    }

    /**
     * Fetches current authoritative room state from PostgreSQL.
     */
    public CompletableFuture<RoomOperationResult<MultiplayerRoomState>> getRoom(String roomId) {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            return CompletableFuture.completedFuture(
                    RoomOperationResult.fail("Authentication required.", "UNAUTHENTICATED")
            );
        }

        String payload = SimpleJson.toJson(Map.of("p_room_id", roomId));
        return httpClient.post("/rest/v1/rpc/get_multiplayer_room", payload, session.getAccessToken())
                .thenApply(response -> handleGetRoomResponse(response, session))
                .exceptionally(ex -> RoomOperationResult.fail("Network error querying room: " + ex.getMessage(), "NETWORK_ERROR"));
    }

    private RoomOperationResult<MultiplayerRoomState> handleGetRoomResponse(HttpResponse<String> response, UserSession session) {
        if (response.statusCode() >= 400) {
            Map<String, Object> errMap = SimpleJson.parseObject(response.body());
            String msg = SimpleJson.getString(errMap, "message");
            return RoomOperationResult.fail(msg != null ? msg : "Failed to query combat room", "QUERY_ERROR");
        }

        Map<String, Object> data = SimpleJson.parseObject(response.body());
        String roomId = SimpleJson.getString(data, "room_id");
        String roomCode = SimpleJson.getString(data, "room_code");
        String hostId = SimpleJson.getString(data, "host_id");
        String hostUsername = SimpleJson.getString(data, "host_username");
        boolean hostReady = SimpleJson.getBoolean(data, "host_ready", false);
        String guestId = SimpleJson.getString(data, "guest_id");
        String guestUsername = SimpleJson.getString(data, "guest_username");
        boolean guestReady = SimpleJson.getBoolean(data, "guest_ready", false);
        long matchSeed = SimpleJson.getLong(data, "match_seed", 0);
        String statusStr = SimpleJson.getString(data, "status");
        boolean isHost = SimpleJson.getBoolean(data, "is_host", session.getUserId().equals(hostId));
        boolean isGuest = SimpleJson.getBoolean(data, "is_guest", session.getUserId().equals(guestId));
        String createdAt = SimpleJson.getString(data, "created_at");
        String expiresAt = SimpleJson.getString(data, "expires_at");

        MultiplayerRoomState state = new MultiplayerRoomState(
                roomId, roomCode, hostId, hostUsername, hostReady,
                guestId, guestUsername, guestReady,
                matchSeed, RoomStatus.fromString(statusStr), isHost, isGuest,
                createdAt, expiresAt
        );

        stateManager.updateState(state);
        notifyRoomListeners(state);
        return RoomOperationResult.ok(state);
    }

    /**
     * Toggles caller ready state.
     */
    public CompletableFuture<RoomOperationResult<MultiplayerRoomState>> setReady(String roomId) {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            return CompletableFuture.completedFuture(
                    RoomOperationResult.fail("Authentication required.", "UNAUTHENTICATED")
            );
        }

        String payload = SimpleJson.toJson(Map.of("p_room_id", roomId));
        return httpClient.post("/rest/v1/rpc/set_multiplayer_ready", payload, session.getAccessToken())
                .thenApply(response -> handleSetReadyResponse(response, roomId, session))
                .exceptionally(ex -> RoomOperationResult.fail("Network error setting readiness: " + ex.getMessage(), "NETWORK_ERROR"));
    }

    private RoomOperationResult<MultiplayerRoomState> handleSetReadyResponse(HttpResponse<String> response, String roomId, UserSession session) {
        if (response.statusCode() >= 400) {
            Map<String, Object> errMap = SimpleJson.parseObject(response.body());
            String msg = SimpleJson.getString(errMap, "message");
            return RoomOperationResult.fail(msg != null ? msg : "Failed to toggle ready status", "RPC_ERROR");
        }

        Map<String, Object> data = SimpleJson.parseObject(response.body());
        boolean hostReady = SimpleJson.getBoolean(data, "host_ready", false);
        boolean guestReady = SimpleJson.getBoolean(data, "guest_ready", false);
        String statusStr = SimpleJson.getString(data, "status");
        RoomStatus status = RoomStatus.fromString(statusStr);

        MultiplayerRoomState current = stateManager.getCurrentRoom();
        if (current != null && current.getRoomId().equals(roomId)) {
            MultiplayerRoomState updated = new MultiplayerRoomState(
                    current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                    hostReady, current.getGuestId(), current.getGuestUsername(), guestReady,
                    current.getMatchSeed(), status, current.isHost(), current.isGuest(),
                    current.getCreatedAt(), current.getExpiresAt()
            );
            stateManager.updateState(updated);
            notifyRoomListeners(updated);

            // Update Presence and broadcast
            presenceService.trackPresence(new PilotPresenceState(
                    session.getUserId(), session.getUsername(), "online", true, System.currentTimeMillis()
            ));

            presenceService.broadcast("READY_CHANGE", Map.of(
                    "roomId", roomId,
                    "userId", session.getUserId(),
                    "hostReady", hostReady,
                    "guestReady", guestReady,
                    "status", status.name()
            ));

            return RoomOperationResult.ok(updated);
        }

        return RoomOperationResult.fail("Room not active locally", "STATE_ERROR");
    }

    /**
     * Initiates the 1v1 multiplayer match.
     */
    public CompletableFuture<RoomOperationResult<MultiplayerRoomState>> startMatch(String roomId) {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            return CompletableFuture.completedFuture(
                    RoomOperationResult.fail("Authentication required.", "UNAUTHENTICATED")
            );
        }

        String payload = SimpleJson.toJson(Map.of("p_room_id", roomId));
        return httpClient.post("/rest/v1/rpc/start_multiplayer_match", payload, session.getAccessToken())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        Map<String, Object> errMap = SimpleJson.parseObject(response.body());
                        String msg = SimpleJson.getString(errMap, "message");
                        return RoomOperationResult.<MultiplayerRoomState>fail(msg != null ? msg : "Failed to start match", "RPC_ERROR");
                    }

                    Map<String, Object> data = SimpleJson.parseObject(response.body());
                    String startedAt = SimpleJson.getString(data, "started_at");
                    long seed = SimpleJson.getLong(data, "match_seed", 1337L);

                    MultiplayerRoomState current = stateManager.getCurrentRoom();
                    if (current != null) {
                        MultiplayerRoomState updated = new MultiplayerRoomState(
                                current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                                current.isHostReady(), current.getGuestId(), current.getGuestUsername(), current.isGuestReady(),
                                seed, RoomStatus.ACTIVE, current.isHost(), current.isGuest(),
                                current.getCreatedAt(), current.getExpiresAt(), startedAt != null ? startedAt : "", 90, null, null, null
                        );
                        stateManager.updateState(updated);
                        notifyRoomListeners(updated);

                        presenceService.broadcast("MATCH_STARTED", Map.of(
                                "roomId", roomId,
                                "startedAt", startedAt != null ? startedAt : "",
                                "matchSeed", seed,
                                "durationSeconds", 90
                        ));

                        return RoomOperationResult.ok(updated);
                    }
                    return RoomOperationResult.<MultiplayerRoomState>fail("Room not active", "STATE_ERROR");
                })
                .exceptionally(ex -> RoomOperationResult.fail("Network error starting match: " + ex.getMessage(), "NETWORK_ERROR"));
    }

    /**
     * Finalizes the match on PostgreSQL and resolves the winner.
     */
    public CompletableFuture<RoomOperationResult<Map<String, Object>>> finalizeMatch(String roomId, int submittedScore, int durationSeconds) {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            return CompletableFuture.completedFuture(
                    RoomOperationResult.fail("Authentication required.", "UNAUTHENTICATED")
            );
        }

        Map<String, Object> body = Map.of(
                "p_room_id", roomId,
                "p_submitted_score", Math.max(0, submittedScore),
                "p_duration_seconds", Math.max(1, durationSeconds)
        );
        String payload = SimpleJson.toJson(body);

        return httpClient.post("/rest/v1/rpc/finalize_multiplayer_match", payload, session.getAccessToken())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        Map<String, Object> errMap = SimpleJson.parseObject(response.body());
                        String msg = SimpleJson.getString(errMap, "message");
                        return RoomOperationResult.<Map<String, Object>>fail(msg != null ? msg : "Failed to finalize match", "RPC_ERROR");
                    }

                    Map<String, Object> data = SimpleJson.parseObject(response.body());
                    String status = SimpleJson.getString(data, "status");

                    if ("RESOLVED".equalsIgnoreCase(status)) {
                        String winnerId = SimpleJson.getString(data, "winner_id");
                        int p1Score = SimpleJson.getInt(data, "player1_score", 0);
                        int p2Score = SimpleJson.getInt(data, "player2_score", 0);
                        boolean draw = SimpleJson.getBoolean(data, "is_draw", false);

                        MultiplayerRoomState current = stateManager.getCurrentRoom();
                        if (current != null) {
                            MultiplayerRoomState updated = new MultiplayerRoomState(
                                    current.getRoomId(), current.getRoomCode(), current.getHostId(), current.getHostUsername(),
                                    current.isHostReady(), current.getGuestId(), current.getGuestUsername(), current.isGuestReady(),
                                    current.getMatchSeed(), RoomStatus.COMPLETED, current.isHost(), current.isGuest(),
                                    current.getCreatedAt(), current.getExpiresAt(), current.getStartedAt(), current.getDurationSeconds(),
                                    p1Score, p2Score, winnerId
                            );
                            stateManager.updateState(updated);
                            notifyRoomListeners(updated);
                        }

                        presenceService.broadcast("MATCH_FINALIZED", Map.of(
                                "roomId", roomId,
                                "winnerId", winnerId != null ? winnerId : "",
                                "isDraw", draw,
                                "hostScore", p1Score,
                                "guestScore", p2Score
                        ));
                    }

                    return RoomOperationResult.ok(data);
                })
                .exceptionally(ex -> RoomOperationResult.fail("Network error finalizing match: " + ex.getMessage(), "NETWORK_ERROR"));
    }

    /**
     * Leaves or cancels the current room.
     */
    public CompletableFuture<RoomOperationResult<Void>> leaveRoom(String roomId) {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            disconnectPresence();
            stateManager.clear();
            return CompletableFuture.completedFuture(RoomOperationResult.ok(null));
        }

        // Broadcast ROOM_CANCELLED before leaving
        presenceService.broadcast("ROOM_CANCELLED", Map.of(
                "roomId", roomId,
                "reason", "Pilot departed room",
                "cancelledBy", session.getUsername()
        ));

        String payload = SimpleJson.toJson(Map.of("p_room_id", roomId));
        return httpClient.post("/rest/v1/rpc/leave_multiplayer_room", payload, session.getAccessToken())
                .handle((response, ex) -> {
                    // Local state cleanup proceeds regardless of server ack
                    disconnectPresence();
                    stateManager.clear();
                    notifyRoomListeners(null);
                    return RoomOperationResult.ok(null);
                });
    }

    /**
     * Subscribes to Supabase Realtime Presence channel for the specified room.
     */
    public CompletableFuture<Boolean> subscribeToRoom(String roomId, String roomCode) {
        UserSession session = sessionManager.getCurrentSession();
        if (session == null || !session.isAuthenticated()) {
            return CompletableFuture.completedFuture(false);
        }

        PilotPresenceState selfState = new PilotPresenceState(
                session.getUserId(), session.getUsername(), "online", false, System.currentTimeMillis()
        );

        return presenceService.connect(roomCode, selfState, session.getAccessToken());
    }

    /**
     * Disconnects active Realtime Presence channel.
     */
    public void disconnectPresence() {
        presenceService.disconnect();
    }

    public MultiplayerRoomState getCurrentRoom() {
        return stateManager.getCurrentRoom();
    }

    public void addRoomListener(RoomStateListener listener) {
        if (listener != null) roomListeners.add(listener);
    }

    public void removeRoomListener(RoomStateListener listener) {
        if (listener != null) roomListeners.remove(listener);
    }

    private void notifyRoomListeners(MultiplayerRoomState room) {
        for (RoomStateListener l : roomListeners) {
            try {
                l.onRoomStateChanged(room);
            } catch (Exception ignored) {}
        }
    }
}

