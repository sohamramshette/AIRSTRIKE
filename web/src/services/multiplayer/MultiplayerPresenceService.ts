// ==============================================================================
// AIRSTRIKE Phase 12: Supabase Realtime Presence & Ephemeral Signaling Service
// FILE: web/src/services/multiplayer/MultiplayerPresenceService.ts
// ==============================================================================

import { RealtimeChannel } from '@supabase/supabase-js';
import { supabase } from '../SupabaseClient';
import {
  PilotPresenceState,
  PresenceListener
} from './MultiplayerTypes';

export class MultiplayerPresenceService {
  private static instance: MultiplayerPresenceService;
  private currentChannel: RealtimeChannel | null = null;
  private currentRoomCode: string | null = null;
  private selfState: PilotPresenceState | null = null;
  private presences: Record<string, PilotPresenceState> = {};
  private presenceListeners: Set<PresenceListener> = new Set();
  private broadcastListeners: Map<string, Set<(payload: unknown) => void>> = new Map();
  private isConnecting: boolean = false;

  private constructor() {}

  public static getInstance(): MultiplayerPresenceService {
    if (!MultiplayerPresenceService.instance) {
      MultiplayerPresenceService.instance = new MultiplayerPresenceService();
    }
    return MultiplayerPresenceService.instance;
  }

  /**
   * Connects to the Supabase Realtime channel for a specific room code and tracks pilot presence.
   */
  public async connect(roomCode: string, selfState: PilotPresenceState): Promise<boolean> {
    const normalizedCode = roomCode.trim().toUpperCase();

    // If already connected to this channel, update presence tracking
    if (this.currentChannel && this.currentRoomCode === normalizedCode) {
      this.selfState = selfState;
      await this.currentChannel.track(selfState);
      return true;
    }

    // Disconnect any previous room channel before joining a new one
    await this.disconnect();

    this.currentRoomCode = normalizedCode;
    this.selfState = selfState;
    this.isConnecting = true;

    try {
      const channelTopic = `multiplayer-room:${normalizedCode}`;
      const channel = supabase.channel(channelTopic, {
        config: {
          broadcast: { ack: false, self: false },
          presence: { key: selfState.userId }
        }
      });

      // 1. Presence Sync handler (full state snapshot)
      channel.on('presence', { event: 'sync' }, () => {
        const state = channel.presenceState<PilotPresenceState>();
        const formatted: Record<string, PilotPresenceState> = {};

        for (const [key, presList] of Object.entries(state)) {
          if (presList && presList.length > 0) {
            // Deduplicate: select most recent presence event for key
            const latest = presList[presList.length - 1];
            formatted[key] = latest;
          }
        }

        this.presences = formatted;
        this.notifyPresenceListeners();
      });

      // 2. Presence Join handler
      channel.on('presence', { event: 'join' }, ({ key, newPresences }) => {
        if (newPresences && newPresences.length > 0) {
          this.presences[key] = newPresences[newPresences.length - 1] as unknown as PilotPresenceState;
          this.notifyPresenceListeners();
        }
      });

      // 3. Presence Leave handler (graceful disconnect detection)
      channel.on('presence', { event: 'leave' }, ({ key }) => {
        if (this.presences[key]) {
          // Mark as disconnected rather than immediately deleting so UI can show disconnect badge
          this.presences[key] = {
            ...this.presences[key],
            presenceStatus: 'disconnected'
          };
          this.notifyPresenceListeners();
        }
      });

      // 4. Wildcard Broadcast dispatcher
      channel.on('broadcast', { event: '*' }, ({ event, payload }) => {
        const listeners = this.broadcastListeners.get(event);
        if (listeners) {
          for (const listener of listeners) {
            try {
              listener(payload);
            } catch (err) {
              console.warn(`Error in broadcast listener for '${event}':`, err);
            }
          }
        }
      });

      // Subscribe and await channel readiness
      await new Promise<void>((resolve, reject) => {
        const timeout = setTimeout(() => {
          resolve(); // Resolve anyway on slow connection, will sync in background
        }, 5000);

        channel.subscribe(async (status) => {
          if (status === 'SUBSCRIBED') {
            clearTimeout(timeout);
            await channel.track(this.selfState!);
            resolve();
          } else if (status === 'CHANNEL_ERROR') {
            clearTimeout(timeout);
            reject(new Error(`Failed to subscribe to realtime channel ${channelTopic}`));
          }
        });
      });

      this.currentChannel = channel;
      this.isConnecting = false;
      return true;
    } catch (err) {
      this.isConnecting = false;
      console.warn('Realtime presence connection error:', err);
      return false;
    }
  }

  /**
   * Updates current pilot's presence payload (e.g. ready state, ping).
   */
  public async updatePresence(partial: Partial<PilotPresenceState>): Promise<void> {
    if (!this.currentChannel || !this.selfState) return;

    this.selfState = {
      ...this.selfState,
      ...partial
    };

    try {
      await this.currentChannel.track(this.selfState);
    } catch (err) {
      console.warn('Failed to update presence state:', err);
    }
  }

  /**
   * Sends an ephemeral broadcast event to the other participant in the room.
   */
  public async broadcast<T>(event: string, payload: T): Promise<boolean> {
    if (!this.currentChannel) return false;

    try {
      await this.currentChannel.send({
        type: 'broadcast',
        event,
        payload
      });
      return true;
    } catch (err) {
      console.warn(`Failed to send broadcast '${event}':`, err);
      return false;
    }
  }

  /**
   * Subscribes to presence changes.
   */
  public onPresenceChange(listener: PresenceListener): () => void {
    this.presenceListeners.add(listener);
    // Fire immediately with current state
    listener(this.presences);
    return () => this.presenceListeners.delete(listener);
  }

  /**
   * Subscribes to broadcast messages for a specific event name.
   */
  public onBroadcast<T>(event: string, listener: (payload: T) => void): () => void {
    if (!this.broadcastListeners.has(event)) {
      this.broadcastListeners.set(event, new Set());
    }
    const set = this.broadcastListeners.get(event)!;
    const typedListener = listener as (payload: unknown) => void;
    set.add(typedListener);

    return () => {
      set.delete(typedListener);
      if (set.size === 0) {
        this.broadcastListeners.delete(event);
      }
    };
  }

  /**
   * Disconnects and cleans up channel subscriptions.
   */
  public async disconnect(): Promise<void> {
    if (this.currentChannel) {
      try {
        await this.currentChannel.untrack();
        await supabase.removeChannel(this.currentChannel);
      } catch (e) {
        // Channel already closed or network disconnected
      }
      this.currentChannel = null;
    }
    this.currentRoomCode = null;
    this.selfState = null;
    this.presences = {};
    this.isConnecting = false;
    this.notifyPresenceListeners();
  }

  public isConnected(): boolean {
    return this.currentChannel !== null && !this.isConnecting;
  }

  public getPresences(): Record<string, PilotPresenceState> {
    return { ...this.presences };
  }

  public getOpponentPresence(myUserId: string): PilotPresenceState | null {
    for (const [key, state] of Object.entries(this.presences)) {
      if (key !== myUserId && state.presenceStatus !== 'disconnected') {
        return state;
      }
    }
    return null;
  }

  private notifyPresenceListeners(): void {
    const copy = { ...this.presences };
    for (const listener of this.presenceListeners) {
      try {
        listener(copy);
      } catch (err) {
        console.warn('Error in presence listener:', err);
      }
    }
  }
}
