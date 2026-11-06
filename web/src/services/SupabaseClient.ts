import { createClient, SupabaseClient } from '@supabase/supabase-js';

const defaultUrl = 'https://qbhotsqwpxbmtpqbgpjy.supabase.co';
const defaultKey = 'sb_publishable_45YsUzEdi6crovilqrubwg_Z6QgVVsM';

// Read from Vite import.meta.env safely (supports Node test environments too)
const envUrl = typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env.VITE_SUPABASE_URL : undefined;
const envKey = typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY : undefined;

export const SUPABASE_URL: string = envUrl || (typeof process !== 'undefined' && process.env?.VITE_SUPABASE_URL) || defaultUrl;
export const SUPABASE_ANON_KEY: string = envKey || (typeof process !== 'undefined' && process.env?.VITE_SUPABASE_PUBLISHABLE_KEY) || defaultKey;

export const supabase: SupabaseClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true
  }
});
