import { createClient, SupabaseClient } from '@supabase/supabase-js';

const defaultUrl = 'https://qbhotsqwpxbmtpqbgpjy.supabase.co';
const defaultKey = 'sb_publishable_45YsUzEdi6crovilqrubwg_Z6QgVVsM';

// Read from Vite import.meta.env safely (supports Node test environments too)
const metaEnv = typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env : ({} as Record<string, string>);
const procEnv = typeof process !== 'undefined' && process.env ? process.env : ({} as Record<string, string>);

const envUrl = metaEnv.VITE_SUPABASE_URL || procEnv.VITE_SUPABASE_URL || procEnv.SUPABASE_URL;
const envKey = metaEnv.VITE_SUPABASE_PUBLISHABLE_KEY || metaEnv.VITE_SUPABASE_ANON_KEY || metaEnv.VITE_SUPABASE_KEY ||
               procEnv.VITE_SUPABASE_PUBLISHABLE_KEY || procEnv.VITE_SUPABASE_ANON_KEY || procEnv.SUPABASE_ANON_KEY || procEnv.SUPABASE_KEY;

export const SUPABASE_URL: string = envUrl || defaultUrl;
export const SUPABASE_ANON_KEY: string = envKey || defaultKey;

export const supabase: SupabaseClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true
  }
});

