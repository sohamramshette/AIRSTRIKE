// ==============================================================================
// AIRSTRIKE Phase 12: Multiplayer System Types & Realtime Payloads
// FILE: web/src/services/multiplayer/MultiplayerTypes.ts
// ==============================================================================

export type MultiplayerRoomStatus =
  | 'WAITING'
  | 'READY'
  | 'COUNTDOWN'
  | 'ACTIVE'
  | 'COMPLETED'
  | 'CANCELLED';

export type PresenceConnectionStatus = 'online' | 'connecting' | 'away' | 'disconnected';

export interface PilotPresenceState {
  userId: string;
  callsign: string;
  presenceStatus: PresenceConnectionStatus;
  isReady: boolean;
  connectedAt: number;
}

export interface MultiplayerRoomState {
  roomId: string;
  roomCode: string;
  hostId: string;
  hostUsername: string;
  hostReady: boolean;
  guestId: string | null;
  guestUsername: string | null;
  guestReady: boolean;
  matchSeed: number;
  status: MultiplayerRoomStatus;
  isHost: boolean;
  isGuest: boolean;
  startedAt?: string | null;
  durationSeconds?: number;
  hostScore?: number | null;
  guestScore?: number | null;
  winnerId?: string | null;
  createdAt: string;
  expiresAt: string;
}

export interface RoomOperationResult<T = unknown> {
  success: boolean;
  data?: T;
  error?: string;
  errorCode?: string;
}

export interface RealtimeBroadcastMessage<T = unknown> {
  type: 'broadcast';
  event:
    | 'GUEST_JOINED'
    | 'READY_CHANGE'
    | 'ROOM_CANCELLED'
    | 'COUNTDOWN_TICK'
    | 'MATCH_STARTED'
    | 'TELEMETRY'
    | 'MATCH_FINALIZED'
    | 'PING';
  payload: T;
}

export interface MultiplayerTelemetryPayload {
  roomId: string;
  playerId: string;
  callsign: string;
  score: number;
  health: number;
  shield: number;
  lives: number;
  wave: number;
  combo: number;
  overheated: boolean;
  sequence: number;
  timestamp: number;
}

export interface MatchStartedPayload {
  roomId: string;
  startedAt: string;
  matchSeed: number;
  durationSeconds: number;
}

export interface MatchFinalizedPayload {
  roomId: string;
  winnerId: string | null;
  isDraw: boolean;
  hostScore: number;
  guestScore: number;
  completedAt: string;
}

export interface ReadyChangeEventPayload {
  roomId: string;
  userId: string;
  isHost: boolean;
  hostReady: boolean;
  guestReady: boolean;
  status: MultiplayerRoomStatus;
}

export interface RoomCancelledEventPayload {
  roomId: string;
  reason: string;
  cancelledBy: string;
}

export interface GuestJoinedEventPayload {
  roomId: string;
  guestId: string;
  guestUsername: string;
}

export type RoomStateListener = (room: MultiplayerRoomState | null) => void;
export type PresenceListener = (presences: Record<string, PilotPresenceState>) => void;

