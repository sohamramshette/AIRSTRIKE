import { supabase } from './SupabaseClient';
import { CurrentUserSession } from '../types/supabase';

export class AuthService {
  private static instance: AuthService;
  private currentSession: CurrentUserSession | null = null;
  private authListeners: Array<(session: CurrentUserSession | null) => void> = [];

  private constructor() {
    this.initSession();
    supabase.auth.onAuthStateChange((_event, session) => {
      if (session?.user) {
        const userId = session.user.id;
        const existingSession = this.currentSession?.userId === userId
          ? this.currentSession
          : null;
        const rawCallsign = session.user.user_metadata?.callsign || session.user.user_metadata?.username;
        const metadataCallsign = typeof rawCallsign === 'string' ? rawCallsign.trim() : '';
        const email = session.user.email || '';
        const fallbackCallsign = metadataCallsign ||
          (email ? email.split('@')[0].toUpperCase() : `PILOT-${userId.substring(0, 4).toUpperCase()}`);
        const shouldResolveCallsign = !existingSession;

        this.currentSession = {
          userId,
          email,
          callsign: existingSession?.callsign || fallbackCallsign,
          accessToken: session.access_token
        };

        if (shouldResolveCallsign) {
          setTimeout(() => {
            void this.resolveCallsign(userId, email, session.user.user_metadata)
              .then((callsign) => {
                if (!this.currentSession || this.currentSession.userId !== userId ||
                    this.currentSession.callsign === callsign) {
                  return;
                }
                this.currentSession = { ...this.currentSession, callsign };
                this.notifyListeners();
              })
              .catch((err: unknown) => {
                console.warn('Failed to resolve pilot callsign after auth state change:', err);
              });
          }, 0);
        }
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
        const callsign = await this.resolveCallsign(
          data.session.user.id,
          data.session.user.email,
          data.session.user.user_metadata
        );
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

  /**
   * Resolves the pilot callsign from public.profiles using the audited schema:
   * Table: public.profiles, Primary Key: id, Unique Column: username.
   */
  public async resolveCallsign(
    userId: string,
    email?: string,
    userMetadata?: Record<string, unknown>
  ): Promise<string> {
    const rawMetadataCallsign = userMetadata?.callsign || userMetadata?.username;
    const metadataCallsign = typeof rawMetadataCallsign === 'string' ? rawMetadataCallsign.trim() : undefined;

    try {
      const { data, error } = await supabase
        .from('profiles')
        .select('username')
        .eq('id', userId)
        .maybeSingle();

      if (!error && data?.username) {
        const dbName = (data.username as string).trim();
        // If the database has an automated default name (e.g. player_xxxx) and user metadata has a custom name:
        if (metadataCallsign && dbName.startsWith('player_') && dbName !== metadataCallsign) {
          const { error: updateErr } = await supabase
            .from('profiles')
            .update({ username: metadataCallsign })
            .eq('id', userId);
          if (!updateErr) {
            return metadataCallsign;
          }
        }
        return dbName;
      }
    } catch (e) {
      // Profile table access might fail or user trigger still running
    }

    if (metadataCallsign) {
      return metadataCallsign;
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
      const redirectUrl = (typeof window !== 'undefined' && window.location) ? `${window.location.origin}/` : 'http://localhost:3000/';
      const { data, error } = await supabase.auth.signUp({
        email: trimmedEmail,
        password,
        options: {
          emailRedirectTo: redirectUrl,
          data: {
            callsign: trimmedCallsign,
            username: trimmedCallsign
          }
        }
      });

      if (error) {
        return { success: false, message: error.message };
      }

      if (data.user) {
        // An automated trigger on auth.users automatically inserts the initial row into public.profiles.
        // Update the profile row with the user's chosen callsign:
        try {
          const { error: updateErr } = await supabase
            .from('profiles')
            .update({ username: trimmedCallsign })
            .eq('id', data.user.id);

          if (updateErr) {
            // Fallback to upsert if trigger was not present
            await supabase.from('profiles').upsert({
              id: data.user.id,
              username: trimmedCallsign
            });
          }
        } catch {
          // Non-blocking fallback
        }

        if (data.session) {
          this.currentSession = {
            userId: data.user.id,
            email: data.user.email || trimmedEmail,
            callsign: trimmedCallsign,
            accessToken: data.session.access_token
          };
          this.notifyListeners();
          return { success: true, message: `Account registered! Welcome Commander ${trimmedCallsign}!` };
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

  /**
   * Updates the pilot username/callsign in public.profiles.
   * Adheres to audited schema: table public.profiles, key 'id', column 'username'.
   */
  public async updateCallsign(newCallsign: string): Promise<{ success: boolean; message: string }> {
    if (!this.currentSession) {
      return { success: false, message: 'Pilot not authenticated.' };
    }
    const trimmed = newCallsign.trim();
    if (!trimmed) {
      return { success: false, message: 'Callsign cannot be empty.' };
    }
    try {
      const { error } = await supabase
        .from('profiles')
        .update({ username: trimmed })
        .eq('id', this.currentSession.userId);

      if (error) {
        return { success: false, message: error.message };
      }
      this.currentSession.callsign = trimmed;
      this.notifyListeners();
      return { success: true, message: 'Callsign updated successfully.' };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return { success: false, message: msg };
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
        const callsign = await this.resolveCallsign(
          data.user.id,
          data.user.email,
          data.user.user_metadata
        );
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
