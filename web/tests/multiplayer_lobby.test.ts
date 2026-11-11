import { test, describe, beforeEach } from 'node:test';
import assert from 'node:assert';
import { MultiplayerValidation } from '../src/services/multiplayer/MultiplayerValidation';
import {
  MultiplayerRoomState,
  PilotPresenceState,
  RoomOperationResult
} from '../src/services/multiplayer/MultiplayerTypes';

describe('AIRSTRIKE Phase 12 Part 3: Multiplayer Lobby & Presence Unit Tests', () => {

  describe('1. Room Code Sanitization & Input Validation', () => {
    test('sanitizes lowercase, whitespace, and illegal characters into canonical 6-char room code', () => {
      assert.strictEqual(MultiplayerValidation.sanitizeRoomCode('  a3b9x2  '), 'A3B9X2');
      assert.strictEqual(MultiplayerValidation.sanitizeRoomCode('k9-4z!1'), 'K94Z1');
      assert.strictEqual(MultiplayerValidation.sanitizeRoomCode(''), '');
      assert.strictEqual(MultiplayerValidation.sanitizeRoomCode(null), '');
      assert.strictEqual(MultiplayerValidation.sanitizeRoomCode(undefined), '');
    });

    test('validates 6-character uppercase alphanumeric room codes correctly', () => {
      assert.strictEqual(MultiplayerValidation.isValidRoomCode('K7N2P9'), true);
      assert.strictEqual(MultiplayerValidation.isValidRoomCode('ABC123'), true);
      assert.strictEqual(MultiplayerValidation.isValidRoomCode('987654'), true);

      // Rejections:
      assert.strictEqual(MultiplayerValidation.isValidRoomCode('K7N2P'), false, 'Too short (5 chars)');
      assert.strictEqual(MultiplayerValidation.isValidRoomCode('K7N2P99'), false, 'Too long (7 chars)');
      assert.strictEqual(MultiplayerValidation.isValidRoomCode('K7-2P9'), false, 'Contains hyphen');
      assert.strictEqual(MultiplayerValidation.isValidRoomCode('K7 2P9'), false, 'Contains space');
      assert.strictEqual(MultiplayerValidation.isValidRoomCode(''), false, 'Empty code');
    });

    test('validates UUID v4 format strictly for rooms and users', () => {
      const validUUID = 'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d';
      assert.strictEqual(MultiplayerValidation.isValidUUID(validUUID), true);
      assert.strictEqual(MultiplayerValidation.isValidUUID('invalid-uuid-string'), false);
      assert.strictEqual(MultiplayerValidation.isValidUUID(''), false);
    });

    test('validates physical score bounds to reject impossible rate anomalies', () => {
      // 60 seconds of combat: max theoretical ~60 * 450 + 2000 = 29,000 pts
      assert.strictEqual(MultiplayerValidation.isScoreWithinPhysicalBounds(12000, 60), true);
      assert.strictEqual(MultiplayerValidation.isScoreWithinPhysicalBounds(28000, 60), true);
      assert.strictEqual(MultiplayerValidation.isScoreWithinPhysicalBounds(999999, 60), false, 'Impossible high score rejected');
      assert.strictEqual(MultiplayerValidation.isScoreWithinPhysicalBounds(-500, 60), false, 'Negative score rejected');
      assert.strictEqual(MultiplayerValidation.isScoreWithinPhysicalBounds(1000, 0), false, 'Zero duration rejected');
    });
  });

  describe('2. Multiplayer Room Lifecycle & Dual-Player Handshake State Machine', () => {
    interface SimulatedLobbyRoom {
      id: string;
      code: string;
      hostId: string;
      hostUsername: string;
      hostReady: boolean;
      guestId: string | null;
      guestUsername: string | null;
      guestReady: boolean;
      status: 'WAITING' | 'READY' | 'COUNTDOWN' | 'ACTIVE' | 'CANCELLED';
      expiresAt: Date;
    }

    class MockLobbyCoordinator {
      public rooms: Map<string, SimulatedLobbyRoom> = new Map();

      public createRoom(hostId: string, hostUsername: string): SimulatedLobbyRoom {
        const id = 'room-' + Math.random().toString(36).substring(2, 9);
        const code = 'R' + Math.floor(10000 + Math.random() * 90000).toString();
        const room: SimulatedLobbyRoom = {
          id,
          code,
          hostId,
          hostUsername,
          hostReady: false,
          guestId: null,
          guestUsername: null,
          guestReady: false,
          status: 'WAITING',
          expiresAt: new Date(Date.now() + 15 * 60 * 1000)
        };
        this.rooms.set(id, room);
        return room;
      }

      public joinRoom(roomCode: string, guestId: string, guestUsername: string): RoomOperationResult<SimulatedLobbyRoom> {
        let target: SimulatedLobbyRoom | null = null;
        for (const room of this.rooms.values()) {
          if (room.code === roomCode) {
            target = room;
            break;
          }
        }

        if (!target) {
          return { success: false, error: 'Room not found.', errorCode: 'ROOM_NOT_FOUND' };
        }
        if (target.hostId === guestId) {
          return { success: false, error: 'Host cannot join their own room as guest.', errorCode: 'SELF_JOIN_FORBIDDEN' };
        }
        if (target.guestId !== null) {
          return { success: false, error: 'Room is already full.', errorCode: 'ROOM_FULL' };
        }
        if (target.status !== 'WAITING') {
          return { success: false, error: `Cannot join room with status: ${target.status}`, errorCode: 'INVALID_STATUS' };
        }
        if (new Date() > target.expiresAt) {
          target.status = 'CANCELLED';
          return { success: false, error: 'Room invitation expired.', errorCode: 'ROOM_EXPIRED' };
        }

        target.guestId = guestId;
        target.guestUsername = guestUsername;
        target.status = 'READY';
        return { success: true, data: target };
      }

      public setReady(roomId: string, pilotId: string): RoomOperationResult<SimulatedLobbyRoom> {
        const room = this.rooms.get(roomId);
        if (!room) return { success: false, error: 'Room not found' };

        if (room.status !== 'READY' && room.status !== 'COUNTDOWN') {
          return { success: false, error: 'Room is not in a readyable state.' };
        }

        if (room.hostId === pilotId) {
          room.hostReady = true;
        } else if (room.guestId === pilotId) {
          room.guestReady = true;
        } else {
          return { success: false, error: 'Pilot not member of room.' };
        }

        if (room.hostReady && room.guestReady) {
          room.status = 'COUNTDOWN';
        }

        return { success: true, data: room };
      }

      public leaveRoom(roomId: string, pilotId: string): { status: string; guestDeparted: boolean } {
        const room = this.rooms.get(roomId);
        if (!room) return { status: 'NONE', guestDeparted: false };

        if (room.hostId === pilotId) {
          room.status = 'CANCELLED';
          return { status: 'CANCELLED', guestDeparted: false };
        }

        if (room.guestId === pilotId) {
          room.guestId = null;
          room.guestUsername = null;
          room.guestReady = false;
          room.hostReady = false;
          room.status = 'WAITING';
          return { status: 'WAITING', guestDeparted: true };
        }

        return { status: room.status, guestDeparted: false };
      }
    }

    let coordinator: MockLobbyCoordinator;

    beforeEach(() => {
      coordinator = new MockLobbyCoordinator();
    });

    test('creates room with status WAITING, unready host, and no guest', () => {
      const room = coordinator.createRoom('pilot-alpha', 'VIPER-1');
      assert.strictEqual(room.status, 'WAITING');
      assert.strictEqual(room.hostId, 'pilot-alpha');
      assert.strictEqual(room.hostUsername, 'VIPER-1');
      assert.strictEqual(room.guestId, null);
      assert.strictEqual(room.hostReady, false);
      assert.strictEqual(room.guestReady, false);
    });

    test('guest join advances status to READY and sets guest profile', () => {
      const room = coordinator.createRoom('pilot-alpha', 'VIPER-1');
      const joinResult = coordinator.joinRoom(room.code, 'pilot-bravo', 'GHOST-9');

      assert.strictEqual(joinResult.success, true);
      assert.strictEqual(joinResult.data?.status, 'READY');
      assert.strictEqual(joinResult.data?.guestId, 'pilot-bravo');
      assert.strictEqual(joinResult.data?.guestUsername, 'GHOST-9');
    });

    test('prevents host from joining own room (self-join guard)', () => {
      const room = coordinator.createRoom('pilot-alpha', 'VIPER-1');
      const joinResult = coordinator.joinRoom(room.code, 'pilot-alpha', 'VIPER-1');

      assert.strictEqual(joinResult.success, false);
      assert.strictEqual(joinResult.errorCode, 'SELF_JOIN_FORBIDDEN');
    });

    test('prevents third player from joining an already paired room', () => {
      const room = coordinator.createRoom('pilot-alpha', 'VIPER-1');
      coordinator.joinRoom(room.code, 'pilot-bravo', 'GHOST-9');

      const thirdResult = coordinator.joinRoom(room.code, 'pilot-charlie', 'ROGUE-3');
      assert.strictEqual(thirdResult.success, false);
      assert.strictEqual(thirdResult.errorCode, 'ROOM_FULL');
    });

    test('dual-player ready handshake advances room status to COUNTDOWN', () => {
      const room = coordinator.createRoom('pilot-alpha', 'VIPER-1');
      coordinator.joinRoom(room.code, 'pilot-bravo', 'GHOST-9');

      // Host signals READY
      const res1 = coordinator.setReady(room.id, 'pilot-alpha');
      assert.strictEqual(res1.success, true);
      assert.strictEqual(res1.data?.hostReady, true);
      assert.strictEqual(res1.data?.guestReady, false);
      assert.strictEqual(res1.data?.status, 'READY', 'Status remains READY until both confirm');

      // Guest signals READY
      const res2 = coordinator.setReady(room.id, 'pilot-bravo');
      assert.strictEqual(res2.success, true);
      assert.strictEqual(res2.data?.guestReady, true);
      assert.strictEqual(res2.data?.status, 'COUNTDOWN', 'Room advances to COUNTDOWN once both ready');
    });

    test('guest departure during READY or COUNTDOWN gracefully resets room to WAITING', () => {
      const room = coordinator.createRoom('pilot-alpha', 'VIPER-1');
      coordinator.joinRoom(room.code, 'pilot-bravo', 'GHOST-9');
      coordinator.setReady(room.id, 'pilot-alpha');

      // Guest leaves
      const leaveResult = coordinator.leaveRoom(room.id, 'pilot-bravo');
      assert.strictEqual(leaveResult.status, 'WAITING');
      assert.strictEqual(leaveResult.guestDeparted, true);
      assert.strictEqual(room.guestId, null);
      assert.strictEqual(room.hostReady, false, 'Host readiness reset to avoid instant ready on next guest');
    });

    test('host departure cancels room and invalidates lobby', () => {
      const room = coordinator.createRoom('pilot-alpha', 'VIPER-1');
      coordinator.joinRoom(room.code, 'pilot-bravo', 'GHOST-9');

      const leaveResult = coordinator.leaveRoom(room.id, 'pilot-alpha');
      assert.strictEqual(leaveResult.status, 'CANCELLED');
      assert.strictEqual(room.status, 'CANCELLED');
    });
  });

  describe('3. Realtime Presence Tracking & Ephemeral Signaling', () => {
    interface MockPresenceStore {
      presences: Record<string, PilotPresenceState>;
      listeners: Set<(presences: Record<string, PilotPresenceState>) => void>;
    }

    class MockPresenceManager {
      private state: MockPresenceStore = {
        presences: {},
        listeners: new Set()
      };

      public track(pilot: PilotPresenceState) {
        this.state.presences[pilot.userId] = pilot;
        this.emit();
      }

      public update(userId: string, partial: Partial<PilotPresenceState>) {
        if (this.state.presences[userId]) {
          this.state.presences[userId] = {
            ...this.state.presences[userId],
            ...partial
          };
          this.emit();
        }
      }

      public leave(userId: string) {
        if (this.state.presences[userId]) {
          this.state.presences[userId] = {
            ...this.state.presences[userId],
            presenceStatus: 'disconnected'
          };
          this.emit();
        }
      }

      public subscribe(listener: (p: Record<string, PilotPresenceState>) => void): () => void {
        this.state.listeners.add(listener);
        return () => this.state.listeners.delete(listener);
      }

      private emit() {
        for (const fn of this.state.listeners) {
          fn({ ...this.state.presences });
        }
      }

      public getPresences() {
        return this.state.presences;
      }
    }

    test('tracks host presence and emits updates to subscribed listeners', () => {
      const manager = new MockPresenceManager();
      let lastEmitted: Record<string, PilotPresenceState> | null = null;
      manager.subscribe((p) => { lastEmitted = p; });

      manager.track({
        userId: 'pilot-1',
        callsign: 'VIPER-1',
        presenceStatus: 'online',
        isReady: false,
        joinedAt: Date.now()
      });

      assert.ok(lastEmitted);
      assert.strictEqual(lastEmitted['pilot-1'].callsign, 'VIPER-1');
      assert.strictEqual(lastEmitted['pilot-1'].presenceStatus, 'online');
      assert.strictEqual(lastEmitted['pilot-1'].isReady, false);
    });

    test('deduplicates multiple presence entries for the same user key', () => {
      const manager = new MockPresenceManager();
      manager.track({
        userId: 'pilot-1',
        callsign: 'VIPER-1',
        presenceStatus: 'online',
        isReady: false,
        joinedAt: 1000
      });

      // Update to ready
      manager.update('pilot-1', { isReady: true });
      const presences = manager.getPresences();

      assert.strictEqual(Object.keys(presences).length, 1);
      assert.strictEqual(presences['pilot-1'].isReady, true);
    });

    test('marks departing pilot as disconnected so UI renders offline beacon', () => {
      const manager = new MockPresenceManager();
      manager.track({
        userId: 'pilot-2',
        callsign: 'GHOST-9',
        presenceStatus: 'online',
        isReady: true,
        joinedAt: 2000
      });

      manager.leave('pilot-2');
      const presences = manager.getPresences();

      assert.strictEqual(presences['pilot-2'].presenceStatus, 'disconnected');
      assert.strictEqual(presences['pilot-2'].callsign, 'GHOST-9');
    });

    test('unsubscribes listener cleanly without memory leaks', () => {
      const manager = new MockPresenceManager();
      let callCount = 0;
      const unsub = manager.subscribe(() => { callCount++; });

      manager.track({
        userId: 'pilot-1',
        callsign: 'VIPER-1',
        presenceStatus: 'online',
        isReady: false,
        joinedAt: Date.now()
      });
      assert.strictEqual(callCount, 1);

      unsub();
      manager.track({
        userId: 'pilot-2',
        callsign: 'GHOST-9',
        presenceStatus: 'online',
        isReady: false,
        joinedAt: Date.now()
      });
      assert.strictEqual(callCount, 1, 'Listener should not be invoked after unsubscription');
    });
  });

  describe('4. Forward Migration & SQL Integrity Checks for Part 3', () => {
    test('confirms leave_multiplayer_room and get_multiplayer_room forward migration exists', async () => {
      const fs = await import('node:fs');
      const path = await import('node:path');
      const migrationPath = path.resolve(process.cwd(), '../supabase/migrations/20261111_phase12_multiplayer_corrections.sql');
      const rollbackPath = path.resolve(process.cwd(), '../supabase/migrations/20261111_phase12_multiplayer_corrections_rollback.sql');

      assert.ok(fs.existsSync(migrationPath), 'Forward migration file must exist');
      assert.ok(fs.existsSync(rollbackPath), 'Rollback migration file must exist');

      const migrationSql = fs.readFileSync(migrationPath, 'utf8');
      assert.ok(migrationSql.includes('leave_multiplayer_room'), 'Contains leave_multiplayer_room function');
      assert.ok(migrationSql.includes('get_multiplayer_room'), 'Contains get_multiplayer_room function');
      assert.ok(migrationSql.includes('SECURITY DEFINER'), 'Enforces SECURITY DEFINER execution');
    });
  });
});

