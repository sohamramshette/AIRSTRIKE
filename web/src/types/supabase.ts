export interface SupabaseProfile {
  id: string;
  username: string;
  created_at?: string;
  updated_at?: string;
}

export interface SupabaseMatch {
  id?: string;
  user_id?: string;
  score: number;
  level_reached: number;
  duration_seconds: number;
  created_at?: string;
}

export interface LeaderboardEntry {
  username: string;
  best_score: number;
}

export interface CurrentUserSession {
  userId: string;
  email: string;
  callsign: string;
  accessToken: string;
}

// ==============================================================================
// Phase 12: 1v1 Online Multiplayer Backend Types
// ==============================================================================

export type MultiplayerRoomStatus =
  | 'WAITING'
  | 'READY'
  | 'COUNTDOWN'
  | 'ACTIVE'
  | 'COMPLETED'
  | 'CANCELLED';

export interface MultiplayerRoom {
  id: string;
  room_code: string;
  host_id: string;
  guest_id: string | null;
  match_seed: number;
  status: MultiplayerRoomStatus;
  host_ready: boolean;
  guest_ready: boolean;
  host_score: number | null;
  guest_score: number | null;
  created_at: string;
  started_at: string | null;
  completed_at: string | null;
  expires_at: string;
}

export interface MultiplayerMatch {
  id: string;
  room_id: string;
  player1_id: string;
  player2_id: string;
  player1_score: number;
  player2_score: number;
  winner_id: string | null;
  duration_seconds: number;
  match_status: 'COMPLETED' | 'ABORTED' | 'FORFEIT';
  completed_at: string;
}

export interface MultiplayerLeaderboardEntry {
  user_id: string;
  username: string;
  rating_points: number;
  wins: number;
  losses: number;
  draws: number;
  total_matches: number;
  best_score: number;
  updated_at: string;
}

export interface CreateRoomResult {
  success: boolean;
  room_id: string;
  room_code: string;
  host_id: string;
  host_username: string;
  match_seed: number;
  status: MultiplayerRoomStatus;
  created_at: string;
  expires_at: string;
}

export interface JoinRoomResult {
  success: boolean;
  room_id: string;
  room_code: string;
  host_id: string;
  host_username: string;
  guest_id: string;
  guest_username: string;
  match_seed: number;
  status: MultiplayerRoomStatus;
}

export interface FinalizeMatchResult {
  success: boolean;
  status: 'RESOLVED' | 'PENDING_OPPONENT' | 'ALREADY_FINALIZED';
  room_id: string;
  match_id?: string;
  winner_id?: string | null;
  is_draw?: boolean;
  host_id?: string;
  host_score?: number;
  host_rating_delta?: number;
  guest_id?: string;
  guest_score?: number;
  guest_rating_delta?: number;
  duration_seconds?: number;
  message?: string;
}

