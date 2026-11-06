import { supabase } from './SupabaseClient';
import { CurrentUserSession } from '../types/supabase';

export class AuthService {
  private static instance: AuthService;
  private currentSession: CurrentUserSession | null = null;
  private authListeners: Array<(session: CurrentUserSession | null) => void> = [];

  private constructor() {
    this.initSession();
    supabase.auth.onAuthStateChange(async (_event, session) => {
      if (session?.user) {
        const callsign = await this.resolveCallsign(session.user.id, session.user.email);
        this.currentSession = {
          userId: session.user.id,
          email: session.user.email || '',
          callsign,
          accessToken: session.access_token
        };
      } else {
        this.currentSession = null;
      }
      this.notifyListeners();
    });
  }

  public static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  private async initSession(): Promise<void> {
    try {
      const { data } = await supabase.auth.getSession();
      if (data?.session?.user) {
        const callsign = await this.resolveCallsign(data.session.user.id, data.session.user.email);
        this.currentSession = {
          userId: data.session.user.id,
          email: data.session.user.email || '',
          callsign,
          accessToken: data.session.access_token
        };
        this.notifyListeners();
      }
    } catch (err) {
      console.warn('Failed to restore Supabase session:', err);
    }
  }

  public async resolveCallsign(userId: string, email?: string): Promise<string> {
    try {
      const { data, error } = await supabase
        .from('profiles')
        .select('callsign')
        .eq('user_id', userId)
        .maybeSingle();

      if (!error && data?.callsign) {
        return data.callsign;
      }
    } catch (e) {
      // Profile table access might fail or user trigger still running
    }

    if (email) {
      return email.split('@')[0].toUpperCase();
    }
    return 'PILOT-' + userId.substring(0, 4).toUpperCase();
  }

  public async signUp(email: string, password: string, callsign: string): Promise<{ success: boolean; message: string }> {
    const trimmedEmail = email.trim();
    const trimmedCallsign = callsign.trim() || trimmedEmail.split('@')[0];

    if (!trimmedEmail || !password) {
      return { success: false, message: 'Email and password are required.' };
    }
    if (password.length < 6) {
      return { success: false, message: 'Password must be at least 6 characters.' };
    }

    try {
      const { data, error } = await supabase.auth.signUp({
        email: trimmedEmail,
        password,
        options: {
          data: {
            callsign: trimmedCallsign
          }
        }
      });

      if (error) {
        return { success: false, message: error.message };
      }

      if (data.user) {
        // Attempt to upsert/update profile callsign
        try {
          await supabase.from('profiles').upsert({
            user_id: data.user.id,
            callsign: trimmedCallsign
          });
        } catch {
          // Trigger may have already created it
        }

        if (data.session) {
          this.currentSession = {
            userId: data.user.id,
            email: data.user.email || trimmedEmail,
            callsign: trimmedCallsign,
            accessToken: data.session.access_token
          };
          this.notifyListeners();
          return { success: true, message: 'Account registered and logged in successfully!' };
        } else {
          return { success: true, message: 'Registration received! Please check your email for confirmation if required.' };
        }
      }

      return { success: false, message: 'Registration response was empty.' };
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      return { success: false, message: errorMsg };
    }
  }

  public async signIn(email: string, password: string): Promise<{ success: boolean; message: string }> {
    const trimmedEmail = email.trim();
    if (!trimmedEmail || !password) {
      return { success: false, message: 'Email and password are required.' };
    }

    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email: trimmedEmail,
        password
      });

      if (error) {
        return { success: false, message: error.message };
      }

      if (data.user && data.session) {
        const callsign = await this.resolveCallsign(data.user.id, data.user.email);
        this.currentSession = {
          userId: data.user.id,
          email: data.user.email || trimmedEmail,
          callsign,
          accessToken: data.session.access_token
        };
        this.notifyListeners();
        return { success: true, message: `Welcome back, Commander ${callsign}!` };
      }

      return { success: false, message: 'Authentication returned no session.' };
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      return { success: false, message: errorMsg };
    }
  }

  public async signOut(): Promise<void> {
    try {
      await supabase.auth.signOut();
    } catch (err) {
      console.warn('Sign out error:', err);
    }
    this.currentSession = null;
    this.notifyListeners();
  }

  public getSession(): CurrentUserSession | null {
    return this.currentSession;
  }

  public isAuthenticated(): boolean {
    return this.currentSession !== null;
  }

  public subscribe(listener: (session: CurrentUserSession | null) => void): () => void {
    this.authListeners.push(listener);
    listener(this.currentSession);
    return () => {
      this.authListeners = this.authListeners.filter(l => l !== listener);
    };
  }

  private notifyListeners(): void {
    for (const listener of this.authListeners) {
      listener(this.currentSession);
    }
  }
}

