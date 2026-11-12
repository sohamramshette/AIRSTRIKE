package database.multiplayer;

import database.SimpleJson;
import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.MultiplayerTelemetryPayload;

import javax.swing.SwingUtilities;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 6): Java Real-Time 1v1 Combat Telemetry & Scoreboard Sync
 * PACKAGE: database.multiplayer
 *
 * Demonstrates:
 * - PSOOP-CO1: Encapsulated concurrent telemetry modeling.
 * - PSOOP-CO3: Defensive packet sequence verification and rate-limiting.
 * - PSOOP-CO4: Kinematics and vital state replication over Realtime Broadcast.
 * ==============================================================================
 */
public class MultiplayerCombatSyncService {

    private static volatile MultiplayerCombatSyncService instance;

    private final MultiplayerPresenceService presenceService;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> broadcastTask;
    private ScheduledFuture<?> watchdogTask;

    private MultiplayerRoomState currentRoom;
    private String localPlayerId = "";
    private String localCallsign = "Pilot";

    private long localSequence = 0;
    private long lastOpponentSequence = -1;

    // Opponent State
    private String opponentPlayerId = "";
    private String opponentCallsign = "Opponent";
    private int opponentScore = 0;
    private int opponentHealth = 100;
    private int opponentShield = 100;
    private int opponentLives = 2;
    private int opponentWave = 1;
    private int opponentCombo = 1;
    private boolean opponentOverheated = false;
    private boolean opponentConnected = true;
    private long lastHeartbeatTime = System.currentTimeMillis();

    private long matchStartTimeEpoch = 0;
    public static final int MATCH_DURATION_SECONDS = 90;
    private boolean tenSecWarningFired = false;
    private String previousLeadState = "TIED";

    private Supplier<MultiplayerTelemetryPayload> vitalsSupplier;
    private Runnable onMatchExpiredCallback;

    private final Set<CombatSyncListener> syncListeners = new CopyOnWriteArraySet<>();
    private final Set<OvertakeListener> overtakeListeners = new CopyOnWriteArraySet<>();

    @FunctionalInterface
    public interface CombatSyncListener {
        void onCombatSync(MultiplayerTelemetryPayload opponentVitals, int remainingSeconds, int scoreDelta);
    }

    @FunctionalInterface
    public interface OvertakeListener {
        void onOvertake(boolean leading, int delta);
    }

    private MultiplayerCombatSyncService() {
        this.presenceService = MultiplayerPresenceService.getInstance();
    }

    public static MultiplayerCombatSyncService getInstance() {
        if (instance == null) {
            synchronized (MultiplayerCombatSyncService.class) {
                if (instance == null) {
                    instance = new MultiplayerCombatSyncService();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the combat sync session for an active room.
     */
    public synchronized void startCombatSync(
            MultiplayerRoomState room,
            String localPlayerId,
            String localCallsign,
            String startedAtIso,
            Supplier<MultiplayerTelemetryPayload> vitalsSupplier,
            Runnable onMatchExpired
    ) {
        stopCombatSync();

        this.currentRoom = Objects.requireNonNull(room, "Room cannot be null");
        this.localPlayerId = localPlayerId != null ? localPlayerId : "";
        this.localCallsign = localCallsign != null ? localCallsign : "Pilot";
        this.vitalsSupplier = vitalsSupplier;
        this.onMatchExpiredCallback = onMatchExpired;

        this.localSequence = 0;
        this.lastOpponentSequence = -1;
        this.tenSecWarningFired = false;
        this.previousLeadState = "TIED";
        this.lastHeartbeatTime = System.currentTimeMillis();
        this.matchStartTimeEpoch = parseIsoTimestamp(startedAtIso);

        this.opponentPlayerId = room.isHost() ? (room.getGuestId() != null ? room.getGuestId() : "") : room.getHostId();
        this.opponentCallsign = room.isHost() ? (room.getGuestUsername() != null ? room.getGuestUsername() : "Challenger") : room.getHostUsername();
        this.opponentScore = 0;
        this.opponentHealth = 100;
        this.opponentShield = 100;
        this.opponentLives = 2;
        this.opponentWave = 1;
        this.opponentCombo = 1;
        this.opponentOverheated = false;
        this.opponentConnected = true;

        // Register Realtime Broadcast listener for TELEMETRY
        presenceService.addBroadcastListener("TELEMETRY", (event, payload) -> {
            if ("TELEMETRY".equals(event) && payload != null) {
                handleInboundTelemetry(payload);
            }
        });

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MultiplayerCombatSyncThread");
            t.setDaemon(true);
            return t;
        });

        // 10 Hz Telemetry Broadcast (~100ms)
        this.broadcastTask = scheduler.scheduleAtFixedRate(this::sendLocalTelemetry, 0, 100, TimeUnit.MILLISECONDS);

        // Heartbeat Watchdog & Timer check (~200ms)
        this.watchdogTask = scheduler.scheduleAtFixedRate(this::checkWatchdogAndTimer, 200, 200, TimeUnit.MILLISECONDS);
    }

    private long parseIsoTimestamp(String iso) {
        if (iso == null || iso.trim().isEmpty()) return System.currentTimeMillis();
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    /**
     * Broadcasts local telemetry snapshot.
     */
    public void sendLocalTelemetry() {
        if (currentRoom == null || vitalsSupplier == null) return;

        try {
            localSequence++;
            MultiplayerTelemetryPayload vitals = vitalsSupplier.get();
            if (vitals == null) return;

            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("roomId", currentRoom.getRoomId());
            payloadMap.put("playerId", localPlayerId);
            payloadMap.put("callsign", localCallsign);
            payloadMap.put("score", vitals.getScore());
            payloadMap.put("health", vitals.getHealth());
            payloadMap.put("shield", vitals.getShield());
            payloadMap.put("lives", vitals.getLives());
            payloadMap.put("wave", vitals.getWave());
            payloadMap.put("combo", vitals.getCombo());
            payloadMap.put("overheated", vitals.isOverheated());
            payloadMap.put("sequence", localSequence);
            payloadMap.put("timestamp", System.currentTimeMillis());

            presenceService.broadcast("TELEMETRY", payloadMap);
        } catch (Exception ignored) {}
    }

    /**
     * Inbound telemetry packet handler with sequence filtering.
     */
    public void handleInboundTelemetry(Map<String, Object> payload) {
        if (payload == null || currentRoom == null) return;

        String roomId = SimpleJson.getString(payload, "roomId");
        if (!currentRoom.getRoomId().equals(roomId)) return;

        String senderId = SimpleJson.getString(payload, "playerId");
        if (localPlayerId.equals(senderId)) return; // Drop own reflection

        long seq = SimpleJson.getLong(payload, "sequence", 0);
        if (seq <= lastOpponentSequence) return; // Drop outdated packets
        lastOpponentSequence = seq;

        this.opponentPlayerId = senderId != null ? senderId : this.opponentPlayerId;
        String cs = SimpleJson.getString(payload, "callsign");
        if (cs != null && !cs.isEmpty()) this.opponentCallsign = cs;

        this.opponentScore = Math.max(0, SimpleJson.getInt(payload, "score", this.opponentScore));
        this.opponentHealth = Math.max(0, Math.min(100, SimpleJson.getInt(payload, "health", this.opponentHealth)));
        this.opponentShield = Math.max(0, Math.min(100, SimpleJson.getInt(payload, "shield", this.opponentShield)));
        this.opponentLives = Math.max(0, SimpleJson.getInt(payload, "lives", this.opponentLives));
        this.opponentWave = Math.max(1, SimpleJson.getInt(payload, "wave", this.opponentWave));
        this.opponentCombo = Math.max(1, SimpleJson.getInt(payload, "combo", this.opponentCombo));
        this.opponentOverheated = SimpleJson.getBoolean(payload, "overheated", false);
        this.opponentConnected = true;
        this.lastHeartbeatTime = System.currentTimeMillis();

        // Lead state detection
        int localScore = vitalsSupplier != null && vitalsSupplier.get() != null ? vitalsSupplier.get().getScore() : 0;
        int delta = localScore - opponentScore;
        String currentLead = delta > 0 ? "LEAD" : (delta < 0 ? "TRAIL" : "TIED");

        if (!previousLeadState.equals(currentLead)) {
            if ("LEAD".equals(currentLead)) {
                notifyOvertake(true, delta);
                announcer.AnnouncerManager.getInstance().showBanner(
                        "Arrey waah! Lead le liya, opponent ki hawa tight!",
                        announcer.DialogueCategory.DESI_MEME,
                        true
                );
            } else if ("TRAIL".equals(currentLead)) {
                notifyOvertake(false, Math.abs(delta));
                announcer.AnnouncerManager.getInstance().showBanner(
                        "Oye hoye! Piche chhoot gaya bhai, jaldi score bada!",
                        announcer.DialogueCategory.DESI_MEME,
                        true
                );
            }
            previousLeadState = currentLead;
        }

        notifySyncListeners();
    }

    private void checkWatchdogAndTimer() {
        long now = System.currentTimeMillis();

        if (now - lastHeartbeatTime > 3000) {
            if (opponentConnected) {
                opponentConnected = false;
                notifySyncListeners();
            }
        }

        int remaining = getRemainingSeconds();
        if (remaining <= 10 && remaining > 0 && !tenSecWarningFired) {
            tenSecWarningFired = true;
            announcer.AnnouncerManager.getInstance().showBanner(
                    "Aakhri 10 second! Sab kuch jhonk do!",
                    announcer.DialogueCategory.BOLLYWOOD,
                    true
            );
        }

        if (remaining <= 0) {
            if (onMatchExpiredCallback != null) {
                Runnable cb = onMatchExpiredCallback;
                onMatchExpiredCallback = null;
                SwingUtilities.invokeLater(cb);
            }
        }

        notifySyncListeners();
    }

    public int getRemainingSeconds() {
        if (matchStartTimeEpoch <= 0) return MATCH_DURATION_SECONDS;
        long elapsedSec = (System.currentTimeMillis() - matchStartTimeEpoch) / 1000L;
        return (int) Math.max(0, MATCH_DURATION_SECONDS - elapsedSec);
    }

    public int getOpponentScore() { return opponentScore; }
    public int getOpponentHealth() { return opponentHealth; }
    public int getOpponentShield() { return opponentShield; }
    public int getOpponentLives() { return opponentLives; }
    public int getOpponentWave() { return opponentWave; }
    public int getOpponentCombo() { return opponentCombo; }
    public boolean isOpponentOverheated() { return opponentOverheated; }
    public boolean isOpponentConnected() { return opponentConnected; }
    public String getOpponentCallsign() { return opponentCallsign; }

    public int getScoreDelta() {
        int localScore = vitalsSupplier != null && vitalsSupplier.get() != null ? vitalsSupplier.get().getScore() : 0;
        return localScore - opponentScore;
    }

    public void addSyncListener(CombatSyncListener listener) {
        if (listener != null) syncListeners.add(listener);
    }

    public void removeSyncListener(CombatSyncListener listener) {
        if (listener != null) syncListeners.remove(listener);
    }

    public void addOvertakeListener(OvertakeListener listener) {
        if (listener != null) overtakeListeners.add(listener);
    }

    public void removeOvertakeListener(OvertakeListener listener) {
        if (listener != null) overtakeListeners.remove(listener);
    }

    private void notifySyncListeners() {
        int remaining = getRemainingSeconds();
        int delta = getScoreDelta();
        MultiplayerTelemetryPayload payload = new MultiplayerTelemetryPayload(
                currentRoom != null ? currentRoom.getRoomId() : "",
                opponentPlayerId,
                opponentCallsign,
                opponentScore,
                opponentHealth,
                opponentShield,
                opponentLives,
                opponentWave,
                opponentCombo,
                opponentOverheated,
                lastOpponentSequence,
                lastHeartbeatTime
        );

        for (CombatSyncListener l : syncListeners) {
            try {
                l.onCombatSync(payload, remaining, delta);
            } catch (Exception ignored) {}
        }
    }

    private void notifyOvertake(boolean leading, int delta) {
        for (OvertakeListener l : overtakeListeners) {
            try {
                l.onOvertake(leading, delta);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Cleanly terminates active combat sync session.
     */
    public synchronized void stopCombatSync() {
        if (broadcastTask != null) {
            broadcastTask.cancel(true);
            broadcastTask = null;
        }
        if (watchdogTask != null) {
            watchdogTask.cancel(true);
            watchdogTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        vitalsSupplier = null;
        onMatchExpiredCallback = null;
    }
}
