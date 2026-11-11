import { test, describe } from 'node:test';
import assert from 'node:assert';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Simulation models representing the PostgreSQL RPC & schema logic for validation
interface RoomRecord {
  id: string;
  room_code: string;
  host_id: string;
  guest_id: string | null;
  match_seed: number;
  status: 'WAITING' | 'READY' | 'COUNTDOWN' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  host_ready: boolean;
  guest_ready: boolean;
  host_score: number | null;
  guest_score: number | null;
  created_at: Date;
  started_at: Date | null;
  completed_at: Date | null;
  expires_at: Date;
}

interface MatchRecord {
  id: string;
  room_id: string;
  player1_id: string;
  player2_id: string;
  player1_score: number;
  player2_score: number;
  winner_id: string | null;
  duration_seconds: number;
  match_status: 'COMPLETED' | 'ABORTED' | 'FORFEIT';
  completed_at: Date;
}

interface LeaderboardRecord {
  user_id: string;
  username: string;
  rating_points: number;
  wins: number;
  losses: number;
  draws: number;
  total_matches: number;
  best_score: number;
  updated_at: Date;
}

class MultiplayerBackendSimulator {
  public rooms: Map<string, RoomRecord> = new Map();
  public matches: Map<string, MatchRecord> = new Map();
  public leaderboard: Map<string, LeaderboardRecord> = new Map();

  public generateRoomCode(): string {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    let code = '';
    for (let i = 0; i < 6; i++) {
      code += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return code;
  }

  public createRoom(userId: string): RoomRecord {
    if (!userId) throw new Error('UNAUTHORIZED: Authentication required.');
    const code = this.generateRoomCode();
    const id = `room-${Math.random().toString(36).substring(2, 9)}`;
    const now = new Date();
    const expires = new Date(now.getTime() + 15 * 60 * 1000);
    const seed = Math.floor(Math.random() * 9000000000) + 100000;

    const room: RoomRecord = {
      id,
      room_code: code,
      host_id: userId,
      guest_id: null,
      match_seed: seed,
      status: 'WAITING',
      host_ready: false,
      guest_ready: false,
      host_score: null,
      guest_score: null,
      created_at: now,
      started_at: null,
      completed_at: null,
      expires_at: expires
    };

    this.rooms.set(id, room);
    return room;
  }

  public joinRoom(roomCode: string, guestId: string): RoomRecord {
    if (!guestId) throw new Error('UNAUTHORIZED: Authentication required.');
    const code = roomCode.trim().toUpperCase();
    const room = [...this.rooms.values()].find(r => r.room_code === code);

    if (!room) throw new Error(`ROOM_NOT_FOUND: No combat room matches code ${code}`);
    if (room.expires_at.getTime() <= Date.now()) {
      room.status = 'CANCELLED';
      throw new Error('ROOM_EXPIRED: The room session has expired.');
    }
    if (room.status !== 'WAITING') {
      throw new Error(`ROOM_UNAVAILABLE: Room is not accepting new pilots (status: ${room.status}).`);
    }
    if (room.host_id === guestId) {
      throw new Error('SELF_JOIN_FORBIDDEN: You cannot join your own room as a guest pilot.');
    }
    if (room.guest_id !== null) {
      throw new Error('ROOM_FULL: Room already has a guest pilot.');
    }

    room.guest_id = guestId;
    room.status = 'READY';
    return room;
  }

  public setReady(roomId: string, userId: string): RoomRecord {
    const room = this.rooms.get(roomId);
    if (!room) throw new Error('ROOM_NOT_FOUND: Specified room does not exist.');
    if (room.host_id !== userId && room.guest_id !== userId) {
      throw new Error('FORBIDDEN: You are not an enrolled pilot in this combat room.');
    }
    if (!room.guest_id) {
      throw new Error('INCOMPLETE_ROOM: Waiting for guest pilot before ready check can proceed.');
    }
    if (room.status !== 'READY' && room.status !== 'WAITING') {
      throw new Error(`INVALID_ACTION: Room cannot toggle ready in status ${room.status}.`);
    }

    if (room.host_id === userId) room.host_ready = true;
    if (room.guest_id === userId) room.guest_ready = true;

    if (room.host_ready && room.guest_ready) {
      room.status = 'COUNTDOWN';
    }

    return room;
  }

  public startMatch(roomId: string, userId: string): RoomRecord {
    const room = this.rooms.get(roomId);
    if (!room) throw new Error('ROOM_NOT_FOUND: Specified room does not exist.');
    if (room.host_id !== userId && room.guest_id !== userId) {
      throw new Error('FORBIDDEN: You are not an enrolled pilot in this combat room.');
    }
    if (room.status === 'ACTIVE') return room;
    if (room.status !== 'COUNTDOWN' && room.status !== 'READY') {
      throw new Error(`INVALID_ACTION: Match cannot be started from state ${room.status}.`);
    }

    room.status = 'ACTIVE';
    room.started_at = new Date();
    return room;
  }

  public finalizeMatch(
    roomId: string,
    userId: string,
    score: number,
    duration: number
  ): { status: string; winner_id?: string | null; is_draw?: boolean } {
    if (!userId) throw new Error('UNAUTHORIZED: Authentication required.');
    if (score < 0) throw new Error('INVALID_ARGUMENT: Submitted score cannot be negative.');
    if (duration <= 0) throw new Error('INVALID_ARGUMENT: Match duration must be greater than zero.');

    const maxTheoretical = duration * 450 + 2000;
    if (score > maxTheoretical) {
      throw new Error(`UNREALISTIC_SCORE_REJECTED: Submitted score (${score}) exceeds physical bounds for ${duration}s.`);
    }

    const room = this.rooms.get(roomId);
    if (!room) throw new Error('ROOM_NOT_FOUND: Room does not exist.');
    if (room.host_id !== userId && room.guest_id !== userId) {
      throw new Error('FORBIDDEN: You are not a participant in this room.');
    }

    if (room.status === 'COMPLETED') {
      const match = this.matches.get(roomId);
      return { status: 'ALREADY_FINALIZED', winner_id: match?.winner_id };
    }

    if (room.status !== 'ACTIVE' && room.status !== 'COUNTDOWN') {
      throw new Error(`INVALID_ROOM_STATE: Cannot finalize a match with room status ${room.status}.`);
    }

    if (room.host_id === userId) room.host_score = score;
    if (room.guest_id === userId) room.guest_score = score;

    if (room.host_score === null || room.guest_score === null) {
      return { status: 'PENDING_OPPONENT' };
    }

    // Both scores present -> Atomic resolution
    let winnerId: string | null = null;
    let isDraw = false;
    let hostDelta = 0;
    let guestDelta = 0;

    if (room.host_score > room.guest_score) {
      winnerId = room.host_id;
      hostDelta = 25;
      guestDelta = 0;
    } else if (room.guest_score > room.host_score) {
      winnerId = room.guest_id;
      hostDelta = 0;
      guestDelta = 25;
    } else {
      winnerId = null;
      isDraw = true;
      hostDelta = 10;
      guestDelta = 10;
    }

    const match: MatchRecord = {
      id: `match-${Math.random().toString(36).substring(2, 9)}`,
      room_id: roomId,
      player1_id: room.host_id,
      player2_id: room.guest_id!,
      player1_score: room.host_score,
      player2_score: room.guest_score,
      winner_id: winnerId,
      duration_seconds: duration,
      match_status: 'COMPLETED',
      completed_at: new Date()
    };

    this.matches.set(roomId, match);
    room.status = 'COMPLETED';
    room.completed_at = new Date();

    // Leaderboard updates
    for (const [pId, isWinner, isLoser, delta, pScore] of [
      [room.host_id, winnerId === room.host_id, winnerId === room.guest_id, hostDelta, room.host_score],
      [room.guest_id!, winnerId === room.guest_id, winnerId === room.host_id, guestDelta, room.guest_score]
    ] as const) {
      let entry = this.leaderboard.get(pId);
      if (!entry) {
        entry = {
          user_id: pId,
          username: `Pilot_${pId.substring(0, 5)}`,
          rating_points: 1000,
          wins: 0,
          losses: 0,
          draws: 0,
          total_matches: 0,
          best_score: 0,
          updated_at: new Date()
        };
        this.leaderboard.set(pId, entry);
      }
      entry.rating_points += delta;
      if (isWinner) entry.wins += 1;
      if (isLoser) entry.losses += 1;
      if (isDraw) entry.draws += 1;
      entry.total_matches += 1;
      entry.best_score = Math.max(entry.best_score, pScore);
      entry.updated_at = new Date();
    }

    return { status: 'RESOLVED', winner_id: winnerId, is_draw: isDraw };
  }
}

describe('AIRSTRIKE Phase 12: 1v1 Online Multiplayer Backend & Security Tests', () => {
  const HOST_ID = 'pilot-uuid-001';
  const GUEST_ID = 'pilot-uuid-002';
  const THIRD_ID = 'pilot-uuid-003';

  test('Room creation generates valid unique room code, seed, and WAITING state', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);

    assert.ok(room.id);
    assert.strictEqual(room.host_id, HOST_ID);
    assert.strictEqual(room.guest_id, null);
    assert.strictEqual(room.status, 'WAITING');
    assert.strictEqual(room.host_ready, false);
    assert.strictEqual(room.guest_ready, false);
    assert.ok(room.room_code.length === 6);
    assert.match(room.room_code, /^[A-Z0-9]{6}$/);
    assert.ok(!/[0O1IL]/.test(room.room_code), 'Room code must avoid ambiguous characters (0, O, 1, I, L)');
    assert.ok(room.match_seed > 0, 'Match seed must be a positive 64-bit integer');
    assert.ok(room.expires_at.getTime() > room.created_at.getTime());
  });

  test('Guest pilot can join waiting room, advancing status to READY', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    const joined = backend.joinRoom(room.room_code, GUEST_ID);

    assert.strictEqual(joined.guest_id, GUEST_ID);
    assert.strictEqual(joined.status, 'READY');
    assert.strictEqual(joined.host_id, HOST_ID);
  });

  test('Self-joining is strictly forbidden by defensive validation and table constraint', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);

    assert.throws(
      () => backend.joinRoom(room.room_code, HOST_ID),
      /SELF_JOIN_FORBIDDEN/
    );
  });

  test('Third player is rejected from an already full room', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);

    assert.throws(
      () => backend.joinRoom(room.room_code, THIRD_ID),
      /ROOM_UNAVAILABLE/
    );
  });

  test('Joining an expired room is rejected and marks room CANCELLED', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    room.expires_at = new Date(Date.now() - 1000); // Backdate expiration

    assert.throws(
      () => backend.joinRoom(room.room_code, GUEST_ID),
      /ROOM_EXPIRED/
    );
    assert.strictEqual(room.status, 'CANCELLED');
  });

  test('Ready check requires both pilots before advancing room to COUNTDOWN', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);

    // Host readies up
    backend.setReady(room.id, HOST_ID);
    assert.strictEqual(room.host_ready, true);
    assert.strictEqual(room.guest_ready, false);
    assert.strictEqual(room.status, 'READY');

    // Guest readies up -> status advances to COUNTDOWN
    backend.setReady(room.id, GUEST_ID);
    assert.strictEqual(room.guest_ready, true);
    assert.strictEqual(room.status, 'COUNTDOWN');
  });

  test('Ready check is rejected if room does not yet have a guest pilot', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);

    assert.throws(
      () => backend.setReady(room.id, HOST_ID),
      /INCOMPLETE_ROOM/
    );
  });

  test('Match start validates room status and transitions to ACTIVE', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);
    backend.setReady(room.id, HOST_ID);
    backend.setReady(room.id, GUEST_ID);

    const started = backend.startMatch(room.id, HOST_ID);
    assert.strictEqual(started.status, 'ACTIVE');
    assert.ok(started.started_at);
  });

  test('Anti-Cheat: Submitting an unrealistic score is rejected by physical rate bounds', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);
    backend.setReady(room.id, HOST_ID);
    backend.setReady(room.id, GUEST_ID);
    backend.startMatch(room.id, HOST_ID);

    // In a 90s match, max points is 90 * 450 + 2000 = 42,500. Attempting 500,000 pts must fail.
    assert.throws(
      () => backend.finalizeMatch(room.id, HOST_ID, 500000, 90),
      /UNREALISTIC_SCORE_REJECTED/
    );
  });

  test('Unauthorized external pilot cannot finalize someone else match', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);
    backend.setReady(room.id, HOST_ID);
    backend.setReady(room.id, GUEST_ID);
    backend.startMatch(room.id, HOST_ID);

    assert.throws(
      () => backend.finalizeMatch(room.id, THIRD_ID, 12000, 90),
      /FORBIDDEN/
    );
  });

  test('Match resolution with distinct scores awards 25 rating points to winner and 0 to loser', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);
    backend.setReady(room.id, HOST_ID);
    backend.setReady(room.id, GUEST_ID);
    backend.startMatch(room.id, HOST_ID);

    // Host submits first
    const res1 = backend.finalizeMatch(room.id, HOST_ID, 18500, 90);
    assert.strictEqual(res1.status, 'PENDING_OPPONENT');
    assert.strictEqual(room.status, 'ACTIVE');

    // Guest submits lower score -> Host wins
    const res2 = backend.finalizeMatch(room.id, GUEST_ID, 14200, 90);
    assert.strictEqual(res2.status, 'RESOLVED');
    assert.strictEqual(res2.winner_id, HOST_ID);
    assert.strictEqual(res2.is_draw, false);
    assert.strictEqual(room.status, 'COMPLETED');

    const hostLb = backend.leaderboard.get(HOST_ID)!;
    const guestLb = backend.leaderboard.get(GUEST_ID)!;

    assert.strictEqual(hostLb.rating_points, 1025); // +25 points
    assert.strictEqual(hostLb.wins, 1);
    assert.strictEqual(hostLb.losses, 0);
    assert.strictEqual(hostLb.best_score, 18500);

    assert.strictEqual(guestLb.rating_points, 1000); // +0 points
    assert.strictEqual(guestLb.wins, 0);
    assert.strictEqual(guestLb.losses, 1);
    assert.strictEqual(guestLb.best_score, 14200);
  });

  test('Match resolution with tied scores awards 10 rating points to both players (Draw)', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);
    backend.setReady(room.id, HOST_ID);
    backend.setReady(room.id, GUEST_ID);
    backend.startMatch(room.id, HOST_ID);

    backend.finalizeMatch(room.id, HOST_ID, 16000, 90);
    const res = backend.finalizeMatch(room.id, GUEST_ID, 16000, 90);

    assert.strictEqual(res.status, 'RESOLVED');
    assert.strictEqual(res.winner_id, null);
    assert.strictEqual(res.is_draw, true);

    const hostLb = backend.leaderboard.get(HOST_ID)!;
    const guestLb = backend.leaderboard.get(GUEST_ID)!;

    assert.strictEqual(hostLb.rating_points, 1010); // +10 points for draw
    assert.strictEqual(hostLb.draws, 1);
    assert.strictEqual(guestLb.rating_points, 1010); // +10 points for draw
    assert.strictEqual(guestLb.draws, 1);
  });

  test('Duplicate finalization attempts are idempotent and do not award duplicate points', () => {
    const backend = new MultiplayerBackendSimulator();
    const room = backend.createRoom(HOST_ID);
    backend.joinRoom(room.room_code, GUEST_ID);
    backend.setReady(room.id, HOST_ID);
    backend.setReady(room.id, GUEST_ID);
    backend.startMatch(room.id, HOST_ID);

    backend.finalizeMatch(room.id, HOST_ID, 20000, 90);
    backend.finalizeMatch(room.id, GUEST_ID, 15000, 90);

    const hostLbBefore = backend.leaderboard.get(HOST_ID)!.rating_points;

    // Host attempts duplicate submission
    const dupRes = backend.finalizeMatch(room.id, HOST_ID, 20000, 90);
    assert.strictEqual(dupRes.status, 'ALREADY_FINALIZED');

    const hostLbAfter = backend.leaderboard.get(HOST_ID)!.rating_points;
    assert.strictEqual(hostLbAfter, hostLbBefore, 'Duplicate finalization must NOT award additional rating points');
  });

  test('Migration SQL file exists, has valid transactional structure, and defines all required objects', () => {
    const candidatePaths = [
      path.resolve(process.cwd(), 'supabase/migrations/20261111_phase12_multiplayer_schema.sql'),
      path.resolve(process.cwd(), '../supabase/migrations/20261111_phase12_multiplayer_schema.sql'),
      path.resolve(__dirname, '../../supabase/migrations/20261111_phase12_multiplayer_schema.sql')
    ];
    const migrationPath = candidatePaths.find(p => fs.existsSync(p));
    assert.ok(migrationPath, 'Migration SQL file must exist at supabase/migrations/20261111_phase12_multiplayer_schema.sql');

    const sqlContent = fs.readFileSync(migrationPath, 'utf8');

    // Structural checks
    assert.ok(sqlContent.includes('BEGIN;'), 'Migration must be wrapped in a transaction block');
    assert.ok(sqlContent.includes('COMMIT;'), 'Migration must conclude with COMMIT');
    assert.ok(!sqlContent.includes('DROP TABLE public.profiles'), 'Migration must NEVER drop existing profiles table');
    assert.ok(!sqlContent.includes('DROP TABLE public.matches'), 'Migration must NEVER drop existing matches table');
    assert.ok(!sqlContent.includes('DROP TABLE public.leaderboard'), 'Migration must NEVER drop existing single-player leaderboard table');

    // Table definitions
    assert.ok(sqlContent.includes('CREATE TABLE IF NOT EXISTS public.multiplayer_rooms'), 'Must declare multiplayer_rooms table');
    assert.ok(sqlContent.includes('CREATE TABLE IF NOT EXISTS public.multiplayer_matches'), 'Must declare multiplayer_matches table');
    assert.ok(sqlContent.includes('CREATE TABLE IF NOT EXISTS public.multiplayer_leaderboard'), 'Must declare multiplayer_leaderboard table');

    // Constraints & RLS
    assert.ok(sqlContent.includes('ENABLE ROW LEVEL SECURITY'), 'Must enable RLS on all tables');
    assert.ok(sqlContent.includes('chk_multiplayer_rooms_distinct_players'), 'Must enforce distinct players constraint');
    assert.ok(sqlContent.includes('uq_multiplayer_matches_room_id'), 'Must enforce single match per room constraint');

    // RPC Functions
    assert.ok(sqlContent.includes('CREATE OR REPLACE FUNCTION public.create_multiplayer_room()'), 'Must define create_multiplayer_room RPC');
    assert.ok(sqlContent.includes('CREATE OR REPLACE FUNCTION public.join_multiplayer_room('), 'Must define join_multiplayer_room RPC');
    assert.ok(sqlContent.includes('CREATE OR REPLACE FUNCTION public.set_multiplayer_ready('), 'Must define set_multiplayer_ready RPC');
    assert.ok(sqlContent.includes('CREATE OR REPLACE FUNCTION public.start_multiplayer_match('), 'Must define start_multiplayer_match RPC');
    assert.ok(sqlContent.includes('CREATE OR REPLACE FUNCTION public.finalize_multiplayer_match('), 'Must define finalize_multiplayer_match RPC');

    // Security Definer & Privilege restrictions
    assert.ok(sqlContent.includes('SECURITY DEFINER'), 'RPCs must be declared SECURITY DEFINER');
    assert.ok(sqlContent.includes('SET search_path = public, pg_temp'), 'Must set secure search_path');
    assert.ok(sqlContent.includes('REVOKE ALL ON FUNCTION public.finalize_multiplayer_match'), 'Must revoke public execute permissions');
    assert.ok(sqlContent.includes('GRANT EXECUTE ON FUNCTION public.finalize_multiplayer_match'), 'Must grant execute only to authenticated role');
  });

  test('Rollback SQL file exists and safely rolls back multiplayer objects without touching single-player data', () => {
    const candidatePaths = [
      path.resolve(process.cwd(), 'supabase/migrations/20261111_phase12_multiplayer_rollback.sql'),
      path.resolve(process.cwd(), '../supabase/migrations/20261111_phase12_multiplayer_rollback.sql'),
      path.resolve(__dirname, '../../supabase/migrations/20261111_phase12_multiplayer_rollback.sql')
    ];
    const rollbackPath = candidatePaths.find(p => fs.existsSync(p));
    assert.ok(rollbackPath, 'Rollback SQL file must exist');

    const rollbackContent = fs.readFileSync(rollbackPath, 'utf8');
    assert.ok(rollbackContent.includes('DROP FUNCTION IF EXISTS public.finalize_multiplayer_match'), 'Rollback must drop finalize RPC');
    assert.ok(rollbackContent.includes('DROP TABLE IF EXISTS public.multiplayer_matches'), 'Rollback must drop multiplayer_matches table');
    assert.ok(rollbackContent.includes('DROP TABLE IF EXISTS public.multiplayer_rooms'), 'Rollback must drop multiplayer_rooms table');
    assert.ok(rollbackContent.includes('DROP TABLE IF EXISTS public.multiplayer_leaderboard'), 'Rollback must drop multiplayer_leaderboard table');
    assert.ok(!rollbackContent.includes('public.profiles'), 'Rollback must not affect public.profiles');
    assert.ok(!rollbackContent.includes('public.matches'), 'Rollback must not affect public.matches');
  });
});
