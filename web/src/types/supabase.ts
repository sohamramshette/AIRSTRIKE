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
