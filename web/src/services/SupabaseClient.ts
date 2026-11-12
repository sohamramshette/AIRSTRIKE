import { createClient, SupabaseClient } from '@supabase/supabase-js';

const defaultUrl = 'https://qbhotsqwpxbmtpqbgpjy.supabase.co';
const defaultKey = 'sb_publishable_45YsUzEdi6crovilqrubwg_Z6QgVVsM';

// Read from Vite import.meta.env safely (supports Node test environments too)
const metaEnv = typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env : ({} as Record<string, string>);
const procEnv = typeof process !== 'undefined' && process.env ? process.env : ({} as Record<string, string>);

const envUrl = metaEnv.VITE_SUPABASE_URL || procEnv.VITE_SUPABASE_URL || procEnv.SUPABASE_URL;
const envKey = metaEnv.VITE_SUPABASE_PUBLISHABLE_KEY || metaEnv.VITE_SUPABASE_ANON_KEY || metaEnv.VITE_SUPABASE_KEY ||
               procEnv.VITE_SUPABASE_PUBLISHABLE_KEY || procEnv.VITE_SUPABASE_ANON_KEY || procEnv.SUPABASE_ANON_KEY || procEnv.SUPABASE_KEY;

// Check runtime environment: browser vs Node.js
const isBrowser = typeof window !== 'undefined' && Boolean(window.location);
const isLocalhost = isBrowser && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');

/**
 * Resolves the Supabase API endpoint.
 * In local browser mode on port 3000, routes through window.location.origin (port 3000)
 * so that Vite's reverse-proxy seamlessly forwards traffic to port 3002.
 */
function resolveSupabaseUrl(): string {
  if (isBrowser) {
    if (isLocalhost && (!envUrl || envUrl.includes('localhost') || envUrl.includes('127.0.0.1') || envUrl.includes('3000'))) {
      return window.location.origin;
    }
    return envUrl || defaultUrl;
  }
  // Node.js test environment: fallback to defaultUrl if env points to local dev
  if (envUrl && !envUrl.includes('localhost') && !envUrl.includes('127.0.0.1')) {
    return envUrl;
  }
  return defaultUrl;
}

export const SUPABASE_URL: string = resolveSupabaseUrl();
export const SUPABASE_ANON_KEY: string = envKey || defaultKey;

export const supabase: SupabaseClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true
  }
});

