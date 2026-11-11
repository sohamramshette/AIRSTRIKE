package database.multiplayer;

import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.RoomStatus;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 4): Multiplayer Room State Machine Manager
 * PACKAGE: database.multiplayer
 *
 * Demonstrates PSOOP-CO2:
 * - Deterministic finite state machine with guarded transitions and lifecycle invariants.
 * ==============================================================================
 */
public class MultiplayerStateManager {

    private final AtomicReference<MultiplayerRoomState> currentRoomRef = new AtomicReference<>(null);

    // Permissible state transitions
    private static final Map<RoomStatus, EnumSet<RoomStatus>> ALLOWED_TRANSITIONS = new ConcurrentHashMap<>();

    static {
        ALLOWED_TRANSITIONS.put(RoomStatus.WAITING, EnumSet.of(RoomStatus.READY, RoomStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(RoomStatus.READY, EnumSet.of(RoomStatus.WAITING, RoomStatus.COUNTDOWN, RoomStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(RoomStatus.COUNTDOWN, EnumSet.of(RoomStatus.WAITING, RoomStatus.ACTIVE, RoomStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(RoomStatus.ACTIVE, EnumSet.of(RoomStatus.COMPLETED, RoomStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(RoomStatus.COMPLETED, EnumSet.noneOf(RoomStatus.class));
        ALLOWED_TRANSITIONS.put(RoomStatus.CANCELLED, EnumSet.noneOf(RoomStatus.class));
    }

    public MultiplayerStateManager() {}

    /**
     * Inspects whether a state transition is logically permitted.
     */
    public static boolean isTransitionAllowed(RoomStatus from, RoomStatus to) {
        if (from == null || to == null) return false;
        if (from == to) return true; // Idempotent no-op
        EnumSet<RoomStatus> validTargets = ALLOWED_TRANSITIONS.get(from);
        return validTargets != null && validTargets.contains(to);
    }

    /**
     * Sets the active room state after verifying lifecycle invariants.
     */
    public synchronized boolean updateState(MultiplayerRoomState newState) {
        if (newState == null) {
            currentRoomRef.set(null);
            return true;
        }

        MultiplayerRoomState oldState = currentRoomRef.get();
        if (oldState != null && !isTransitionAllowed(oldState.getStatus(), newState.getStatus())) {
            return false;
        }

        currentRoomRef.set(newState);
        return true;
    }

    public MultiplayerRoomState getCurrentRoom() {
        return currentRoomRef.get();
    }

    public RoomStatus getCurrentStatus() {
        MultiplayerRoomState room = currentRoomRef.get();
        return room != null ? room.getStatus() : null;
    }

    public boolean isInRoom() {
        MultiplayerRoomState room = currentRoomRef.get();
        return room != null && room.getStatus() != RoomStatus.CANCELLED && room.getStatus() != RoomStatus.COMPLETED;
    }

    public void clear() {
        currentRoomRef.set(null);
    }
}
