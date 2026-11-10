import { supabase } from './SupabaseClient';
import { LeaderboardEntry } from '../types/supabase';

export interface LeaderboardResult {
  entries: LeaderboardEntry[];
  source: 'cloud';
  error?: boolean;
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
      // Guard against hanging network requests with a 6-second timeout
      const timeoutPromise = new Promise<{ data: null; error: { message: string; code: string }; status: number }>((resolve) => {
        setTimeout(() => {
          resolve({
            data: null,
            error: {
              message: 'Supabase cloud request timed out. Please check your connection or refresh the page.',
              code: 'TIMEOUT'
            },
            status: 408
          });
        }, 6000);
      });

      const queryPromise = supabase
        .from('leaderboard')
        .select('username,best_score')
        .order('best_score', { ascending: false })
        .limit(limit);

      const { data, error, status } = await Promise.race([queryPromise, timeoutPromise]);

      if (error) {
        console.warn(`Supabase leaderboard returned status ${status}:`, error.message);
        const errorCode = (error as { code?: string }).code;
        const isPermissionDenied = errorCode === '42501';
        const isAuthenticationError = (status === 401 || status === 403) && !isPermissionDenied;
        return {
          entries: [],
          source: 'cloud',
          error: true,
          message: isPermissionDenied
            ? 'Supabase is reachable, but the anon role cannot read public.leaderboard. Review and apply supabase/migrations/20261107_phase7_forward.sql in the Supabase SQL Editor.'
            : isAuthenticationError
              ? `Supabase rejected the API key or session (HTTP ${status}): ${error.message}`
              : `Supabase notice: ${error.message}`
        };
      }

      if (data && data.length > 0) {
        return {
          entries: data as LeaderboardEntry[],
          source: 'cloud',
          message: `⚡ Live Hall of Fame (${data.length} pilot${data.length === 1 ? '' : 's'} ranked)`
        };
      }

      return {
        entries: [],
        source: 'cloud',
        message: 'No flight combat records logged yet. Be the first Commander on the board!'
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return {
        entries: [],
        source: 'cloud',
        error: true,
        message: `Network error: ${msg}`
      };
    }
  }
}
