package database.multiplayer;

import database.SimpleJson;
import database.SupabaseConfig;
import database.multiplayer.MultiplayerModels.BroadcastListener;
import database.multiplayer.MultiplayerModels.PilotPresenceState;
import database.multiplayer.MultiplayerModels.PresenceListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 4): Supabase Realtime WebSocket Presence Service
 * PACKAGE: database.multiplayer
 *
 * Implements Phoenix Channels Protocol v1.0.0 over native Java WebSocket (SE 11+):
 * - Phoenix channel join with auth token injection.
 * - 30-second periodic Phoenix heartbeat.
 * - Ephemeral Presence tracking ('presence_state' and 'presence_diff').
 * - Ephemeral Broadcast dispatcher ('GUEST_JOINED', 'READY_CHANGE', 'ROOM_CANCELLED').
 * - Graceful leave and network reconnect handling.
 * ==============================================================================
 */
public class MultiplayerPresenceService {

    private static volatile MultiplayerPresenceService instance;

    private final HttpClient httpClient;
    private final SupabaseConfig config;
    private final ScheduledExecutorService scheduler;
    private final AtomicLong refCounter = new AtomicLong(1);

    private WebSocket webSocket;
    private String currentRoomCode;
    private String currentTopic;
    private PilotPresenceState selfPresence;
    private ScheduledFuture<?> heartbeatTask;

    private final Map<String, PilotPresenceState> presences = new ConcurrentHashMap<>();
    private final Set<PresenceListener> presenceListeners = new CopyOnWriteArraySet<>();
    private final Map<String, Set<BroadcastListener>> broadcastListeners = new ConcurrentHashMap<>();

    private MultiplayerPresenceService() {
        this.config = SupabaseConfig.getInstance();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
                .build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AirStrike-Realtime-Heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    public static MultiplayerPresenceService getInstance() {
        if (instance == null) {
            synchronized (MultiplayerPresenceService.class) {
                if (instance == null) {
                    instance = new MultiplayerPresenceService();
                }
            }
        }
        return instance;
    }

    /**
     * Converts Supabase HTTPS URL to Phoenix Realtime WSS WebSocket URI.
     */
    public static String buildWebSocketUrl(String supabaseUrl, String anonKey) {
        if (supabaseUrl == null || supabaseUrl.trim().isEmpty()) return "";
        String cleanUrl = supabaseUrl.trim();
        String wsScheme = cleanUrl.startsWith("https") ? "wss" : "ws";
        String hostPart = cleanUrl.replaceFirst("^https?://", "").replaceAll("/+$", "");
        return wsScheme + "://" + hostPart + "/realtime/v1/websocket?apikey=" + anonKey + "&vsn=1.0.0";
    }

    /**
     * Connects to the Supabase Realtime channel for a specific room code and initiates Presence.
     */
    public synchronized CompletableFuture<Boolean> connect(String roomCode, PilotPresenceState selfState, String accessToken) {
        final String normalizedCode = MultiplayerValidation.sanitizeRoomCode(roomCode);
        this.selfPresence = selfState;

        // If already connected to this room, update presence state
        if (webSocket != null && !webSocket.isInputClosed() && normalizedCode.equalsIgnoreCase(this.currentRoomCode)) {
            trackPresence(selfState);
            return CompletableFuture.completedFuture(true);
        }

        // Disconnect previous channel if active
        disconnect();

        this.currentRoomCode = normalizedCode;
        this.currentTopic = "realtime:multiplayer-room:" + normalizedCode;

        String wsUrl = buildWebSocketUrl(config.getSupabaseUrl(), config.getSupabaseKey());
        if (wsUrl.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        CompletableFuture<Boolean> connectionPromise = new CompletableFuture<>();

        try {
            httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
                    .buildAsync(URI.create(wsUrl), new PhoenixWebSocketListener(connectionPromise, accessToken))
                    .thenAccept(ws -> {
                        synchronized (MultiplayerPresenceService.this) {
                            this.webSocket = ws;
                        }
                    })
                    .exceptionally(ex -> {
                        connectionPromise.complete(false);
                        return null;
                    });
        } catch (Exception e) {
            connectionPromise.complete(false);
        }

        return connectionPromise;
    }

    /**
     * Sends Phoenix Channel Join message.
     */
    private void sendChannelJoin(WebSocket ws, String accessToken) {
        String ref = String.valueOf(refCounter.getAndIncrement());

        Map<String, Object> presenceConfig = Map.of("key", selfPresence != null ? selfPresence.getUserId() : "");
        Map<String, Object> broadcastConfig = Map.of("ack", false, "self", false);
        Map<String, Object> configMap = Map.of(
                "broadcast", broadcastConfig,
                "presence", presenceConfig,
                "private", false
        );

        Map<String, Object> payload;
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            payload = Map.of("config", configMap, "access_token", accessToken.trim());
        } else {
            payload = Map.of("config", configMap);
        }

        Map<String, Object> joinMsg = Map.of(
                "topic", currentTopic,
                "event", "phx_join",
                "payload", payload,
                "ref", ref,
                "join_ref", ref
        );

        String json = SimpleJson.toJson(joinMsg);
        ws.sendText(json, true);

        // Start periodic heartbeat
        startHeartbeat(ws);
    }

    private synchronized void startHeartbeat(WebSocket ws) {
        if (heartbeatTask != null && !heartbeatTask.isDone()) {
            heartbeatTask.cancel(true);
        }
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (ws != null && !ws.isInputClosed()) {
                    String ref = String.valueOf(refCounter.getAndIncrement());
                    Map<String, Object> hb = Map.of(
                            "topic", "phoenix",
                            "event", "heartbeat",
                            "payload", Collections.emptyMap(),
                            "ref", ref
                    );
                    ws.sendText(SimpleJson.toJson(hb), true);
                }
            } catch (Exception ignored) {}
        }, 25, 25, TimeUnit.SECONDS);
    }

    /**
     * Publishes pilot presence state to the Phoenix channel.
     */
    public synchronized void trackPresence(PilotPresenceState state) {
        this.selfPresence = state;
        if (webSocket == null || webSocket.isInputClosed() || currentTopic == null) return;

        presences.put(state.getUserId(), state);
        notifyPresenceListeners();

        String ref = String.valueOf(refCounter.getAndIncrement());
        Map<String, Object> trackPayload = Map.of(
                "userId", state.getUserId(),
                "callsign", state.getCallsign(),
                "presenceStatus", state.getPresenceStatus(),
                "isReady", state.isReady(),
                "joinedAt", state.getJoinedAt()
        );

        Map<String, Object> msg = Map.of(
                "topic", currentTopic,
                "event", "presence",
                "payload", Map.of(
                        "type", "presence",
                        "event", "track",
                        "payload", trackPayload
                ),
                "ref", ref
        );

        webSocket.sendText(SimpleJson.toJson(msg), true);
    }

    /**
     * Broadcasts an ephemeral event to peers on this room topic.
     */
    public synchronized void broadcast(String eventName, Map<String, Object> eventPayload) {
        if (webSocket == null || webSocket.isInputClosed() || currentTopic == null) return;

        String ref = String.valueOf(refCounter.getAndIncrement());
        Map<String, Object> msg = Map.of(
                "topic", currentTopic,
                "event", "broadcast",
                "payload", Map.of(
                        "type", "broadcast",
                        "event", eventName,
                        "payload", eventPayload != null ? eventPayload : Collections.emptyMap()
                ),
                "ref", ref
        );

        webSocket.sendText(SimpleJson.toJson(msg), true);
    }

    /**
     * Gracefully leaves the active channel and closes WebSocket connection.
     */
    public synchronized void disconnect() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
            heartbeatTask = null;
        }

        if (webSocket != null && !webSocket.isInputClosed() && currentTopic != null) {
            try {
                String ref = String.valueOf(refCounter.getAndIncrement());
                Map<String, Object> leaveMsg = Map.of(
                        "topic", currentTopic,
                        "event", "phx_leave",
                        "payload", Collections.emptyMap(),
                        "ref", ref
                );
                webSocket.sendText(SimpleJson.toJson(leaveMsg), true);
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Pilot left room");
            } catch (Exception ignored) {}
        }

        webSocket = null;
        currentRoomCode = null;
        currentTopic = null;
        presences.clear();
        notifyPresenceListeners();
    }

    public void addPresenceListener(PresenceListener listener) {
        if (listener != null) presenceListeners.add(listener);
    }

    public void removePresenceListener(PresenceListener listener) {
        if (listener != null) presenceListeners.remove(listener);
    }

    public void addBroadcastListener(String event, BroadcastListener listener) {
        if (event == null || listener == null) return;
        broadcastListeners.computeIfAbsent(event, k -> new CopyOnWriteArraySet<>()).add(listener);
    }

    public void removeBroadcastListener(String event, BroadcastListener listener) {
        if (event == null || listener == null) return;
        Set<BroadcastListener> set = broadcastListeners.get(event);
        if (set != null) set.remove(listener);
    }

    public Map<String, PilotPresenceState> getPresences() {
        return Collections.unmodifiableMap(presences);
    }

    private void notifyPresenceListeners() {
        Map<String, PilotPresenceState> snapshot = Collections.unmodifiableMap(presences);
        for (PresenceListener l : presenceListeners) {
            try {
                l.onPresenceChanged(snapshot);
            } catch (Exception ignored) {}
        }
    }

    private void notifyBroadcastListeners(String event, Map<String, Object> payload) {
        Set<BroadcastListener> set = broadcastListeners.get(event);
        if (set != null) {
            for (BroadcastListener l : set) {
                try {
                    l.onBroadcastReceived(event, payload);
                } catch (Exception ignored) {}
            }
        }
    }

    // =========================================================================
    // Phoenix Protocol WebSocket Listener
    // =========================================================================

    private class PhoenixWebSocketListener implements WebSocket.Listener {
        private final CompletableFuture<Boolean> connectionPromise;
        private final String accessToken;
        private final StringBuilder textBuffer = new StringBuilder();

        PhoenixWebSocketListener(CompletableFuture<Boolean> connectionPromise, String accessToken) {
            this.connectionPromise = connectionPromise;
            this.accessToken = accessToken;
        }

        @Override
        public void onOpen(WebSocket ws) {
            sendChannelJoin(ws, accessToken);
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String completeMessage = textBuffer.toString();
                textBuffer.setLength(0);
                handlePhoenixMessage(completeMessage);
            }
            ws.request(1);
            return null;
        }

        @SuppressWarnings("unchecked")
        private void handlePhoenixMessage(String rawJson) {
            try {
                Map<String, Object> msg = SimpleJson.parseObject(rawJson);
                String event = SimpleJson.getString(msg, "event");
                Object payloadObj = msg.get("payload");

                if ("phx_reply".equals(event)) {
                    if (payloadObj instanceof Map) {
                        Map<String, Object> reply = (Map<String, Object>) payloadObj;
                        String status = SimpleJson.getString(reply, "status");
                        if ("ok".equalsIgnoreCase(status) && !connectionPromise.isDone()) {
                            connectionPromise.complete(true);
                            if (selfPresence != null) {
                                trackPresence(selfPresence);
                            }
                        }
                    }
                } else if ("presence_state".equals(event)) {
                    if (payloadObj instanceof Map) {
                        Map<String, Object> stateMap = (Map<String, Object>) payloadObj;
                        for (Map.Entry<String, Object> entry : stateMap.entrySet()) {
                            String key = entry.getKey();
                            if (entry.getValue() instanceof List) {
                                List<?> list = (List<?>) entry.getValue();
                                if (!list.isEmpty()) {
                                    Object latest = list.get(list.size() - 1);
                                    if (latest instanceof Map) {
                                        parsePresenceEntry(key, (Map<String, Object>) latest);
                                    }
                                }
                            }
                        }
                        notifyPresenceListeners();
                    }
                } else if ("presence_diff".equals(event)) {
                    if (payloadObj instanceof Map) {
                        Map<String, Object> diffMap = (Map<String, Object>) payloadObj;
                        Object leaves = diffMap.get("leaves");
                        if (leaves instanceof Map) {
                            for (String leftKey : ((Map<String, Object>) leaves).keySet()) {
                                PilotPresenceState existing = presences.get(leftKey);
                                if (existing != null) {
                                    presences.put(leftKey, existing.withStatus("disconnected"));
                                }
                            }
                        }
                        Object joins = diffMap.get("joins");
                        if (joins instanceof Map) {
                            for (Map.Entry<String, Object> e : ((Map<String, Object>) joins).entrySet()) {
                                if (e.getValue() instanceof List) {
                                    List<?> list = (List<?>) e.getValue();
                                    if (!list.isEmpty() && list.get(list.size() - 1) instanceof Map) {
                                        parsePresenceEntry(e.getKey(), (Map<String, Object>) list.get(list.size() - 1));
                                    }
                                }
                            }
                        }
                        notifyPresenceListeners();
                    }
                } else if ("broadcast".equals(event)) {
                    if (payloadObj instanceof Map) {
                        Map<String, Object> bMap = (Map<String, Object>) payloadObj;
                        String bEvent = SimpleJson.getString(bMap, "event");
                        Object bPayload = bMap.get("payload");
                        if (bPayload instanceof Map) {
                            notifyBroadcastListeners(bEvent, (Map<String, Object>) bPayload);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        private void parsePresenceEntry(String userId, Map<String, Object> entry) {
            String callsign = SimpleJson.getString(entry, "callsign");
            if (callsign == null) callsign = "Cadet";
            String status = SimpleJson.getString(entry, "presenceStatus");
            if (status == null) status = "online";
            boolean isReady = SimpleJson.getBoolean(entry, "isReady", false);
            long joinedAt = SimpleJson.getLong(entry, "joinedAt", System.currentTimeMillis());

            presences.put(userId, new PilotPresenceState(userId, callsign, status, isReady, joinedAt));
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            if (!connectionPromise.isDone()) {
                connectionPromise.complete(false);
            }
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            if (!connectionPromise.isDone()) {
                connectionPromise.complete(false);
            }
        }
    }
}
