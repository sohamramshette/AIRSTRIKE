import { supabase } from './SupabaseClient';
import { AuthService } from './AuthService';
import { SupabaseMatch } from '../types/supabase';

export class MatchService {
  private static instance: MatchService;
  private authService: AuthService;
  private isSaving: boolean = false;
  private lastSavedKey: string = '';

  private constructor() {
    this.authService = AuthService.getInstance();
  }

  public static getInstance(): MatchService {
    if (!MatchService.instance) {
      MatchService.instance = new MatchService();
    }
    return MatchService.instance;
  }

  public async saveMatch(
    score: number,
    levelReached: number,
    durationSeconds: number
  ): Promise<{ success: boolean; message: string; matchId?: string }> {
    const session = this.authService.getSession();

    // Generate unique key for this match to prevent duplicate submissions
    const matchKey = `${session?.userId || 'guest'}-${score}-${levelReached}-${durationSeconds}`;
    if (this.isSaving || this.lastSavedKey === matchKey) {
      return { success: false, message: 'Match score already submitted.' };
    }

    // Guest matches remain strictly local
    if (!session) {
      this.saveLocalGuestMatch(score, levelReached, durationSeconds);
      this.lastSavedKey = matchKey;
      return {
        success: true,
        message: '[COMM LINK ACTIVE] Guest flight logs recorded locally. Log in to sync to the Cloud Leaderboard!'
      };
    }

    this.isSaving = true;
    try {
      const payload: SupabaseMatch = {
        user_id: session.userId,
        score: Math.max(0, Math.floor(score)),
        level_reached: Math.max(1, Math.floor(levelReached)),
        duration_seconds: Math.max(0, Math.floor(durationSeconds))
      };

      const { data, error } = await supabase
        .from('matches')
        .insert(payload)
        .select('id,score,level_reached,duration_seconds,created_at')
        .single();

      if (error) {
        console.error('Supabase match insert error:', error);
        // Fallback to local storage so student doesn't lose score
        this.saveLocalGuestMatch(score, levelReached, durationSeconds);
        return { success: false, message: `Cloud upload error: ${error.message} (Saved locally)` };
      }

      this.lastSavedKey = matchKey;
      return {
        success: true,
        message: 'Match flight log uploaded to Supabase!',
        matchId: data?.id
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      this.saveLocalGuestMatch(score, levelReached, durationSeconds);
      return { success: false, message: `Save error: ${msg} (Saved locally)` };
    } finally {
      this.isSaving = false;
    }
  }

  public async fetchMyMatches(limit: number = 20): Promise<SupabaseMatch[]> {
    const session = this.authService.getSession();
    if (!session) {
      return this.getLocalGuestMatches();
    }

    try {
      const { data, error } = await supabase
        .from('matches')
        .select('id,user_id,score,level_reached,duration_seconds,created_at')
        .eq('user_id', session.userId)
        .order('created_at', { ascending: false })
        .limit(limit);

      if (error) {
        console.warn('Error fetching cloud match history:', error);
        return this.getLocalGuestMatches();
      }

      return (data as SupabaseMatch[]) || [];
    } catch (err) {
      console.warn('Network error fetching match history:', err);
      return this.getLocalGuestMatches();
    }
  }

  private inMemoryGuestMatches: SupabaseMatch[] = [];

  private saveLocalGuestMatch(score: number, levelReached: number, durationSeconds: number): void {
    const existing = this.getLocalGuestMatches();
    const newMatch: SupabaseMatch = {
      id: 'local-' + Date.now(),
      user_id: 'guest',
      score,
      level_reached: levelReached,
      duration_seconds: durationSeconds,
      created_at: new Date().toISOString()
    };
    existing.unshift(newMatch);

    if (typeof localStorage !== 'undefined') {
      try {
        localStorage.setItem('airstrike_guest_matches', JSON.stringify(existing.slice(0, 30)));
      } catch (e) {
        console.warn('localStorage access failed:', e);
      }
    } else {
      this.inMemoryGuestMatches = existing.slice(0, 30);
    }
  }

  public getLocalGuestMatches(): SupabaseMatch[] {
    if (typeof localStorage !== 'undefined') {
      try {
        const raw = localStorage.getItem('airstrike_guest_matches');
        if (raw) {
          return JSON.parse(raw);
        }
      } catch (e) {
        console.warn('localStorage read failed:', e);
      }
    }
    return this.inMemoryGuestMatches;
  }
}
