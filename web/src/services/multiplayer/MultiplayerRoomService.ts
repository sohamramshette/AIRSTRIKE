// ==============================================================================
// AIRSTRIKE Phase 12: High-Level Multiplayer Room Coordinator Service
// FILE: web/src/services/multiplayer/MultiplayerRoomService.ts
// ==============================================================================

import { supabase } from '../SupabaseClient';
import { AuthService } from '../AuthService';
import { MultiplayerPresenceService } from './MultiplayerPresenceService';
import { MultiplayerValidation } from './MultiplayerValidation';
import {
  MultiplayerRoomState,
  RoomOperationResult,
  RoomStateListener,
  ReadyChangeEventPayload,
  RoomCancelledEventPayload
} from './MultiplayerTypes';

export class MultiplayerRoomService {
  private static instance: MultiplayerRoomService;
  private authService: AuthService;
  private presenceService: MultiplayerPresenceService;
  private currentRoom: MultiplayerRoomState | null = null;
  private roomListeners: Set<RoomStateListener> = new Set();
  private broadcastCleanupFns: Array<() => void> = [];

  private constructor() {
    this.authService = AuthService.getInstance();
    this.presenceService = MultiplayerPresenceService.getInstance();
  }

  public static getInstance(): MultiplayerRoomService {
    if (!MultiplayerRoomService.instance) {
      MultiplayerRoomService.instance = new MultiplayerRoomService();
    }
    return MultiplayerRoomService.instance;
  }

  /**
   * Creates a private 1v1 multiplayer combat room.
   */
  public async createRoom(): Promise<RoomOperationResult<MultiplayerRoomState>> {
    const session = this.authService.getSession();
    if (!session) {
      return {
        success: false,
        error: 'Authentication required. Please log in as a registered Starfleet Pilot.',
        errorCode: 'UNAUTHENTICATED'
      };
    }

    try {
      const { data, error } = await supabase.rpc('create_multiplayer_room');

      if (error) {
        console.warn('create_multiplayer_room RPC error:', error);
        return {
          success: false,
          error: error.message || 'Failed to create room on Starfleet server.',
          errorCode: error.code
        };
      }

      const roomData = data as {
        room_id: string;
        room_code: string;
        host_id: string;
        host_username: string;
        match_seed: number;
        status: string;
        created_at: string;
        expires_at: string;
      };

      const roomState: MultiplayerRoomState = {
        roomId: roomData.room_id,
        roomCode: roomData.room_code,
        hostId: roomData.host_id,
        hostUsername: roomData.host_username || session.callsign,
        hostReady: false,
        guestId: null,
        guestUsername: null,
        guestReady: false,
        matchSeed: roomData.match_seed,
        status: 'WAITING',
        isHost: true,
        isGuest: false,
        createdAt: roomData.created_at,
        expiresAt: roomData.expires_at
      };

      this.currentRoom = roomState;
      this.notifyRoomListeners();

      // Connect to ephemeral Realtime channel
      await this.subscribeToRoom(roomState.roomId, roomState.roomCode);

      return {
        success: true,
        data: roomState
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return {
        success: false,
        error: `Network error creating room: ${msg}`,
        errorCode: 'NETWORK_ERROR'
      };
    }
  }

  /**
   * Joins an existing room using a 6-character room code.
   */
  public async joinRoom(roomCode: string): Promise<RoomOperationResult<MultiplayerRoomState>> {
    const session = this.authService.getSession();
    if (!session) {
      return {
        success: false,
        error: 'Authentication required. Please log in as a registered Starfleet Pilot.',
        errorCode: 'UNAUTHENTICATED'
      };
    }

    const sanitizedCode = MultiplayerValidation.sanitizeRoomCode(roomCode);
    if (!MultiplayerValidation.isValidRoomCode(sanitizedCode)) {
      return {
        success: false,
        error: 'Invalid room code format. Must be a 6-character alphanumeric code.',
        errorCode: 'INVALID_CODE_FORMAT'
      };
    }

    try {
      const { data, error } = await supabase.rpc('join_multiplayer_room', {
        p_room_code: sanitizedCode
      });

      if (error) {
        console.warn('join_multiplayer_room RPC error:', error);
        return {
          success: false,
          error: error.message || 'Failed to join combat room.',
          errorCode: error.code
        };
      }

      const roomData = data as {
        room_id: string;
        room_code: string;
        host_id: string;
        host_username: string;
        guest_id: string;
        guest_username: string;
        match_seed: number;
        status: string;
      };

      const roomState: MultiplayerRoomState = {
        roomId: roomData.room_id,
        roomCode: roomData.room_code,
        hostId: roomData.host_id,
        hostUsername: roomData.host_username,
        hostReady: false,
        guestId: roomData.guest_id,
        guestUsername: roomData.guest_username || session.callsign,
        guestReady: false,
        matchSeed: roomData.match_seed,
        status: 'READY',
        isHost: false,
        isGuest: true,
        createdAt: new Date().toISOString(),
        expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString()
      };

      this.currentRoom = roomState;
      this.notifyRoomListeners();

      // Connect to Realtime presence & broadcast channel
      await this.subscribeToRoom(roomState.roomId, roomState.roomCode);

      // Notify host via ephemeral broadcast that guest has joined
      await this.presenceService.broadcast('GUEST_JOINED', {
        roomId: roomState.roomId,
        guestId: session.userId,
        guestUsername: session.callsign
      });

      return {
        success: true,
        data: roomState
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return {
        success: false,
        error: `Network error joining room: ${msg}`,
        errorCode: 'NETWORK_ERROR'
      };
    }
  }

  /**
   * Fetches latest authoritative room state from PostgreSQL.
   */
  public async getRoom(roomId: string): Promise<RoomOperationResult<MultiplayerRoomState>> {
    const session = this.authService.getSession();
    if (!session) {
      return { success: false, error: 'Authentication required.', errorCode: 'UNAUTHENTICATED' };
    }

    try {
      // 1. Try get_multiplayer_room RPC first
      const { data, error } = await supabase.rpc('get_multiplayer_room', {
        p_room_id: roomId
      });

      if (!error && data) {
        const d = data as {
          room_id: string;
          room_code: string;
          host_id: string;
          host_username: string;
          host_ready: boolean;
          guest_id: string | null;
          guest_username: string | null;
          guest_ready: boolean;
          match_seed: number;
          status: string;
          is_host: boolean;
          is_guest: boolean;
          created_at: string;
          expires_at: string;
        };

        const state: MultiplayerRoomState = {
          roomId: d.room_id,
          roomCode: d.room_code,
          hostId: d.host_id,
          hostUsername: d.host_username,
          hostReady: d.host_ready,
          guestId: d.guest_id,
          guestUsername: d.guest_username,
          guestReady: d.guest_ready,
          matchSeed: d.match_seed,
          status: d.status as MultiplayerRoomState['status'],
          isHost: d.is_host,
          isGuest: d.is_guest,
          createdAt: d.created_at,
          expiresAt: d.expires_at
        };

        this.currentRoom = state;
        this.notifyRoomListeners();
        return { success: true, data: state };
      }

      // 2. Fallback to direct SELECT query against multiplayer_rooms (RLS allowed)
      const { data: rowData, error: selectError } = await supabase
        .from('multiplayer_rooms')
        .select('*')
        .eq('id', roomId)
        .maybeSingle();

      if (selectError || !rowData) {
        return {
          success: false,
          error: selectError?.message || 'Room not found.',
          errorCode: selectError?.code
        };
      }

      const state: MultiplayerRoomState = {
        roomId: rowData.id,
        roomCode: rowData.room_code,
        hostId: rowData.host_id,
        hostUsername: this.currentRoom?.hostUsername || 'Host Pilot',
        hostReady: rowData.host_ready,
        guestId: rowData.guest_id,
        guestUsername: this.currentRoom?.guestUsername || (rowData.guest_id ? 'Guest Pilot' : null),
        guestReady: rowData.guest_ready,
        matchSeed: rowData.match_seed,
        status: rowData.status,
        isHost: rowData.host_id === session.userId,
        isGuest: rowData.guest_id === session.userId,
        createdAt: rowData.created_at,
        expiresAt: rowData.expires_at
      };

      this.currentRoom = state;
      this.notifyRoomListeners();
      return { success: true, data: state };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return { success: false, error: msg, errorCode: 'QUERY_ERROR' };
    }
  }

  /**
   * Toggles caller ready state.
   */
  public async setReady(roomId: string): Promise<RoomOperationResult<MultiplayerRoomState>> {
    const session = this.authService.getSession();
    if (!session) {
      return { success: false, error: 'Authentication required.', errorCode: 'UNAUTHENTICATED' };
    }

    try {
      const { data, error } = await supabase.rpc('set_multiplayer_ready', {
        p_room_id: roomId
      });

      if (error) {
        return { success: false, error: error.message, errorCode: error.code };
      }

      const d = data as {
        host_ready: boolean;
        guest_ready: boolean;
        status: string;
      };

      if (this.currentRoom && this.currentRoom.roomId === roomId) {
        this.currentRoom = {
          ...this.currentRoom,
          hostReady: d.host_ready,
          guestReady: d.guest_ready,
          status: d.status as MultiplayerRoomState['status']
        };
        this.notifyRoomListeners();
      }

      // Update Presence with readiness
      await this.presenceService.updatePresence({ isReady: true });

      // Broadcast ready change to peer
      await this.presenceService.broadcast<ReadyChangeEventPayload>('READY_CHANGE', {
        roomId,
        userId: session.userId,
        isHost: this.currentRoom?.isHost ?? false,
        hostReady: d.host_ready,
        guestReady: d.guest_ready,
        status: d.status as MultiplayerRoomState['status']
      });

      return {
        success: true,
        data: this.currentRoom!
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return { success: false, error: msg, errorCode: 'RPC_ERROR' };
    }
  }

  /**
   * Leaves or cancels the current room.
   */
  public async leaveRoom(roomId: string): Promise<RoomOperationResult<void>> {
    const session = this.authService.getSession();
    if (!session) return { success: true };

    try {
      // Broadcast cancel notification to peer before leaving
      await this.presenceService.broadcast<RoomCancelledEventPayload>('ROOM_CANCELLED', {
        roomId,
        reason: 'Pilot departed the combat room.',
        cancelledBy: session.callsign
      });

      // Execute backend leave RPC if deployed, otherwise fallback to local teardown
      try {
        await supabase.rpc('leave_multiplayer_room', { p_room_id: roomId });
      } catch (rpcErr) {
        // RPC might not be deployed yet; local state teardown proceeds regardless
      }

      await this.disconnectPresence();
      this.currentRoom = null;
      this.notifyRoomListeners();

      return { success: true };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      await this.disconnectPresence();
      this.currentRoom = null;
      this.notifyRoomListeners();
      return { success: false, error: msg };
    }
  }

  /**
   * Sets up Realtime Presence and Broadcast listeners for the specified room.
   */
  public async subscribeToRoom(roomId: string, roomCode: string): Promise<void> {
    const session = this.authService.getSession();
    if (!session) return;

    // Clear any previous broadcast listeners
    for (const cleanup of this.broadcastCleanupFns) {
      cleanup();
    }
    this.broadcastCleanupFns = [];

    // Connect Presence
    await this.presenceService.connect(roomCode, {
      userId: session.userId,
      callsign: session.callsign,
      presenceStatus: 'online',
      isReady: this.currentRoom?.isHost ? this.currentRoom.hostReady : (this.currentRoom?.guestReady ?? false),
      connectedAt: Date.now()
    });

    // 1. Listen for GUEST_JOINED broadcast
    const unGuest = this.presenceService.onBroadcast<{ guestId: string; guestUsername: string }>(
      'GUEST_JOINED',
      (payload) => {
        if (this.currentRoom && this.currentRoom.roomId === roomId) {
          this.currentRoom = {
            ...this.currentRoom,
            guestId: payload.guestId,
            guestUsername: payload.guestUsername,
            status: 'READY'
          };
          this.notifyRoomListeners();
        }
      }
    );
    this.broadcastCleanupFns.push(unGuest);

    // 2. Listen for READY_CHANGE broadcast
    const unReady = this.presenceService.onBroadcast<ReadyChangeEventPayload>(
      'READY_CHANGE',
      (payload) => {
        if (this.currentRoom && this.currentRoom.roomId === roomId) {
          this.currentRoom = {
            ...this.currentRoom,
            hostReady: payload.hostReady,
            guestReady: payload.guestReady,
            status: payload.status
          };
          this.notifyRoomListeners();
        }
      }
    );
    this.broadcastCleanupFns.push(unReady);

    // 3. Listen for ROOM_CANCELLED broadcast
    const unCancel = this.presenceService.onBroadcast<RoomCancelledEventPayload>(
      'ROOM_CANCELLED',
      (_payload) => {
        if (this.currentRoom && this.currentRoom.roomId === roomId) {
          this.currentRoom = {
            ...this.currentRoom,
            status: 'CANCELLED'
          };
          this.notifyRoomListeners();
        }
      }
    );
    this.broadcastCleanupFns.push(unCancel);
  }

  /**
   * Disconnects Realtime Presence and removes broadcast listeners.
   */
  public async disconnectPresence(): Promise<void> {
    for (const cleanup of this.broadcastCleanupFns) {
      cleanup();
    }
    this.broadcastCleanupFns = [];
    await this.presenceService.disconnect();
  }

  public getCurrentRoom(): MultiplayerRoomState | null {
    return this.currentRoom ? { ...this.currentRoom } : null;
  }

  public addRoomListener(listener: RoomStateListener): () => void {
    this.roomListeners.add(listener);
    listener(this.currentRoom ? { ...this.currentRoom } : null);
    return () => this.roomListeners.delete(listener);
  }

  private notifyRoomListeners(): void {
    const copy = this.currentRoom ? { ...this.currentRoom } : null;
    for (const listener of this.roomListeners) {
      try {
        listener(copy);
      } catch (err) {
        console.warn('Error in room listener:', err);
      }
    }
  }
}
