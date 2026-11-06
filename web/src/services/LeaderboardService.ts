import { supabase } from './SupabaseClient';
import { LeaderboardEntry } from '../types/supabase';

export interface LeaderboardResult {
  entries: LeaderboardEntry[];
  source: 'cloud' | 'offline_fallback';
  message?: string;
}

export class LeaderboardService {
  private static instance: LeaderboardService;

  private constructor() {}

  public static getInstance(): LeaderboardService {
    if (!LeaderboardService.instance) {
      LeaderboardService.instance = new LeaderboardService();
    }
    return LeaderboardService.instance;
  }

  public async fetchLeaderboard(limit: number = 10): Promise<LeaderboardResult> {
    try {
      const { data, error, status } = await supabase
        .from('leaderboard')
        .select('username,best_score')
        .order('best_score', { ascending: false })
        .limit(limit);

      if (error) {
        // HTTP 401 / 42501 occurs when anonymous RLS permission is not yet applied
        console.warn(`Supabase leaderboard returned status ${status}:`, error.message);
        return {
          entries: this.getOfflineLeaderboard(),
          source: 'offline_fallback',
          message: status === 401 || (error as { code?: string }).code === '42501'
            ? 'Cloud Leaderboard awaiting Phase 7.2A backend RLS activation. Displaying local academy records.'
            : `Supabase notice: ${error.message}`
        };
      }

      if (data && data.length > 0) {
        return {
          entries: data as LeaderboardEntry[],
          source: 'cloud'
        };
      }

      return {
        entries: this.getOfflineLeaderboard(),
        source: 'offline_fallback',
        message: 'No cloud scores recorded yet. Be the first Commander on the board!'
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return {
        entries: this.getOfflineLeaderboard(),
        source: 'offline_fallback',
        message: `Offline mode: ${msg}`
      };
    }
  }

  private getOfflineLeaderboard(): LeaderboardEntry[] {
    return [
      { username: 'COMMANDER_APEX', best_score: 48500 },
      { username: 'STAR_VIPER', best_score: 36200 },
      { username: 'NOVA_STRIKER', best_score: 29400 },
      { username: 'CYBER_ACE', best_score: 22800 },
      { username: 'PHANTOM_9', best_score: 18500 },
      { username: 'SOLAR_WING', best_score: 14200 },
      { username: 'VORTEX_LEADER', best_score: 11000 },
      { username: 'NEBULA_RUNNER', best_score: 8750 },
      { username: 'ASTRO_CADET', best_score: 6200 },
      { username: 'ROOKIE_ONE', best_score: 3500 }
    ];
  }
}
