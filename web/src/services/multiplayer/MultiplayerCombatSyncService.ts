// ==============================================================================
// AIRSTRIKE Phase 12 (Part 6): Real-Time 1v1 Combat Telemetry & Scoreboard Sync
// FILE: web/src/services/multiplayer/MultiplayerCombatSyncService.ts
//
// Coordinates:
// - Ephemeral Realtime Broadcast telemetry transmission (~10 Hz / 100ms).
// - Monotonic sequence filtering and packet drop protection.
// - Peer vital state replication (health, shield, lives, score, combo, overheat).
// - Real-time score delta calculations and overtake/lead transition detection.
// - Authoritative 90-second match duration tracking and 10s countdown alert.
// ==============================================================================

import { MultiplayerPresenceService } from './MultiplayerPresenceService';
import {
  MultiplayerRoomState,
  MultiplayerTelemetryPayload
} from './MultiplayerTypes';
import { AnnouncerManager } from '../../announcer/AnnouncerManager';

export interface OpponentVitals {
  playerId: string;
  callsign: string;
  score: number;
  health: number;
  shield: number;
  lives: number;
  wave: number;
  combo: number;
  overheated: boolean;
  connected: boolean;
  lastHeartbeat: number;
}

export type CombatSyncListener = (opponent: OpponentVitals, remainingSeconds: number, scoreDelta: number) => void;
export type OvertakeListener = (leading: boolean, delta: number) => void;

export class MultiplayerCombatSyncService {
  private static instance: MultiplayerCombatSyncService;
  private presenceService: MultiplayerPresenceService;

  private currentRoom: MultiplayerRoomState | null = null;
  private localPlayerId: string = '';
  private localCallsign: string = 'Pilot';

  private localSequence: number = 0;
  private lastOpponentSequence: number = -1;

  private opponentVitals: OpponentVitals = {
    playerId: '',
    callsign: 'Opponent',
    score: 0,
    health: 100,
    shield: 100,
    lives: 2,
    wave: 1,
    combo: 1,
    overheated: false,
    connected: true,
    lastHeartbeat: Date.now()
  };

  private broadcastIntervalId: any = null;
  private watchdogIntervalId: any = null;
  private broadcastCleanupFn: (() => void) | null = null;

  private matchStartTimeEpoch: number = 0;
  public readonly matchDurationSeconds: number = 90;
  private tenSecWarningFired: boolean = false;
  private previousLeadState: 'LEAD' | 'TRAIL' | 'TIED' = 'TIED';

  private syncListeners: Set<CombatSyncListener> = new Set();
  private overtakeListeners: Set<OvertakeListener> = new Set();
  private onMatchExpiredCallback: (() => void) | null = null;

  // Local vitals provider function
  private vitalsProvider: (() => {
    score: number;
    health: number;
    shield: number;
    lives: number;
    wave: number;
    combo: number;
    overheated: boolean;
  }) | null = null;

  private constructor() {
    this.presenceService = MultiplayerPresenceService.getInstance();
  }

  public static getInstance(): MultiplayerCombatSyncService {
    if (!MultiplayerCombatSyncService.instance) {
      MultiplayerCombatSyncService.instance = new MultiplayerCombatSyncService();
    }
    return MultiplayerCombatSyncService.instance;
  }

  /**
   * Initializes the combat sync session for an active room.
   */
  public startCombatSync(
    room: MultiplayerRoomState,
    localPlayerId: string,
    localCallsign: string,
    startedAtIso: string,
    vitalsProvider: () => {
      score: number;
      health: number;
      shield: number;
      lives: number;
      wave: number;
      combo: number;
      overheated: boolean;
    },
    onMatchExpired?: () => void
  ): void {
    this.stopCombatSync();

    this.currentRoom = room;
    this.localPlayerId = localPlayerId;
    this.localCallsign = localCallsign;
    this.vitalsProvider = vitalsProvider;
    this.onMatchExpiredCallback = onMatchExpired || null;

    this.localSequence = 0;
    this.lastOpponentSequence = -1;
    this.tenSecWarningFired = false;
    this.previousLeadState = 'TIED';

    const parsedEpoch = startedAtIso ? Date.parse(startedAtIso) : NaN;
    this.matchStartTimeEpoch = isNaN(parsedEpoch) ? Date.now() : parsedEpoch;

    const oppId = room.isHost ? (room.guestId || '') : room.hostId;
    const oppName = room.isHost ? (room.guestUsername || 'Challenger') : room.hostUsername;

    this.opponentVitals = {
      playerId: oppId,
      callsign: oppName,
      score: 0,
      health: 100,
      shield: 100,
      lives: 2,
      wave: 1,
      combo: 1,
      overheated: false,
      connected: true,
      lastHeartbeat: Date.now()
    };

    // 1. Subscribe to ephemeral TELEMETRY broadcasts
    this.broadcastCleanupFn = this.presenceService.onBroadcast<MultiplayerTelemetryPayload>(
      'TELEMETRY',
      (payload) => {
        if (payload) {
          this.handleInboundTelemetry(payload);
        }
      }
    );

    // 2. High-Frequency Broadcast Loop (~10 Hz = 100ms)
    this.broadcastIntervalId = setInterval(() => {
      this.sendLocalTelemetry();
    }, 100);

    // 3. Heartbeat Watchdog & Authoritative Match Timer (~200ms)
    this.watchdogIntervalId = setInterval(() => {
      this.checkWatchdogAndTimer();
    }, 200);
  }

  /**
   * Transmits local player vitals to peer.
   */
  public sendLocalTelemetry(): void {
    if (!this.currentRoom || !this.vitalsProvider) return;

    const vitals = this.vitalsProvider();
    this.localSequence++;

    const payload: MultiplayerTelemetryPayload = {
      roomId: this.currentRoom.roomId,
      playerId: this.localPlayerId,
      callsign: this.localCallsign,
      score: vitals.score,
      health: vitals.health,
      shield: vitals.shield,
      lives: vitals.lives,
      wave: vitals.wave,
      combo: vitals.combo,
      overheated: vitals.overheated,
      sequence: this.localSequence,
      timestamp: Date.now()
    };

    this.presenceService.broadcast<MultiplayerTelemetryPayload>('TELEMETRY', payload).catch(() => {
      // Ephemeral broadcast failures are silently ignored
    });
  }

  /**
   * Inbound telemetry packet handler with sequence filtering.
   */
  public handleInboundTelemetry(payload: MultiplayerTelemetryPayload): void {
    if (!payload || !this.currentRoom) return;
    if (payload.roomId !== this.currentRoom.roomId) return;
    if (payload.playerId === this.localPlayerId) return; // Drop own reflection

    // Monotonic sequence verification: drop outdated packets
    if (payload.sequence <= this.lastOpponentSequence) {
      return;
    }
    this.lastOpponentSequence = payload.sequence;

    this.opponentVitals = {
      playerId: payload.playerId,
      callsign: payload.callsign || this.opponentVitals.callsign,
      score: Math.max(0, payload.score),
      health: Math.max(0, Math.min(100, payload.health)),
      shield: Math.max(0, Math.min(100, payload.shield)),
      lives: Math.max(0, payload.lives),
      wave: Math.max(1, payload.wave),
      combo: Math.max(1, payload.combo),
      overheated: !!payload.overheated,
      connected: true,
      lastHeartbeat: Date.now()
    };

    // Lead state evaluation & commentary triggers
    const localScore = this.vitalsProvider ? this.vitalsProvider().score : 0;
    const delta = localScore - this.opponentVitals.score;
    const currentLeadState = delta > 0 ? 'LEAD' : (delta < 0 ? 'TRAIL' : 'TIED');

    if (this.previousLeadState !== currentLeadState) {
      if (currentLeadState === 'LEAD') {
        this.notifyOvertake(true, delta);
        AnnouncerManager.getInstance().showBanner(
          'Arrey waah! Lead le liya, opponent ki hawa tight!',
          'DESI_MEME',
          true
        );
      } else if (currentLeadState === 'TRAIL') {
        this.notifyOvertake(false, Math.abs(delta));
        AnnouncerManager.getInstance().showBanner(
          'Oye hoye! Piche chhoot gaya bhai, jaldi score bada!',
          'DESI_MEME',
          true
        );
      }
      this.previousLeadState = currentLeadState;
    }

    this.notifySyncListeners();
  }

  private checkWatchdogAndTimer(): void {
    const now = Date.now();

    // 1. Peer heartbeat check (>3s without telemetry indicates disconnection/lag)
    if (now - this.opponentVitals.lastHeartbeat > 3000) {
      if (this.opponentVitals.connected) {
        this.opponentVitals.connected = false;
        this.notifySyncListeners();
      }
    }

    // 2. Authoritative remaining match time check
    const remaining = this.getRemainingSeconds();

    if (remaining <= 10 && remaining > 0 && !this.tenSecWarningFired) {
      this.tenSecWarningFired = true;
      AnnouncerManager.getInstance().showBanner(
        'Aakhri 10 second! Sab kuch jhonk do!',
        'BOLLYWOOD',
        true
      );
    }

    if (remaining <= 0) {
      if (this.onMatchExpiredCallback) {
        const cb = this.onMatchExpiredCallback;
        this.onMatchExpiredCallback = null;
        cb();
      }
    }

    this.notifySyncListeners();
  }

  /**
   * Computes remaining seconds based on authoritative match start timestamp.
   */
  public getRemainingSeconds(): number {
    if (this.matchStartTimeEpoch <= 0) return this.matchDurationSeconds;
    const elapsedSeconds = (Date.now() - this.matchStartTimeEpoch) / 1000.0;
    return Math.max(0, Math.ceil(this.matchDurationSeconds - elapsedSeconds));
  }

  public getOpponentVitals(): Readonly<OpponentVitals> {
    return this.opponentVitals;
  }

  public getScoreDelta(): number {
    const localScore = this.vitalsProvider ? this.vitalsProvider().score : 0;
    return localScore - this.opponentVitals.score;
  }

  public addSyncListener(listener: CombatSyncListener): void {
    this.syncListeners.add(listener);
  }

  public removeSyncListener(listener: CombatSyncListener): void {
    this.syncListeners.delete(listener);
  }

  public addOvertakeListener(listener: OvertakeListener): void {
    this.overtakeListeners.add(listener);
  }

  public removeOvertakeListener(listener: OvertakeListener): void {
    this.overtakeListeners.delete(listener);
  }

  private notifySyncListeners(): void {
    const remaining = this.getRemainingSeconds();
    const delta = this.getScoreDelta();
    for (const listener of this.syncListeners) {
      try {
        listener(this.opponentVitals, remaining, delta);
      } catch (err) {
        // Listener safety
      }
    }
  }

  private notifyOvertake(leading: boolean, delta: number): void {
    for (const listener of this.overtakeListeners) {
      try {
        listener(leading, delta);
      } catch (err) {
        // Listener safety
      }
    }
  }

  /**
   * Cleanly terminates active combat sync session.
   */
  public stopCombatSync(): void {
    if (this.broadcastIntervalId !== null) {
      clearInterval(this.broadcastIntervalId);
      this.broadcastIntervalId = null;
    }
    if (this.watchdogIntervalId !== null) {
      clearInterval(this.watchdogIntervalId);
      this.watchdogIntervalId = null;
    }
    if (this.broadcastCleanupFn) {
      this.broadcastCleanupFn();
      this.broadcastCleanupFn = null;
    }
    this.vitalsProvider = null;
    this.onMatchExpiredCallback = null;
  }
}
