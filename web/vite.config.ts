import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const supabaseTarget = env.VITE_SUPABASE_URL || 'https://qbhotsqwpxbmtpqbgpjy.supabase.co';

  return {
    root: '.',
    server: {
      port: 3000,
      open: false,
      host: true,
      cors: true,
      proxy: {
        // Reverse-proxy Supabase Auth GoTrue API to live Supabase backend
        '/auth': {
          target: supabaseTarget,
          changeOrigin: true,
          secure: true
        },
        // Reverse-proxy Supabase PostgREST REST API to live Supabase backend
        '/rest': {
          target: supabaseTarget,
          changeOrigin: true,
          secure: true
        },
        // Reverse-proxy Supabase Realtime WebSocket to live Supabase backend
        '/realtime': {
          target: supabaseTarget,
          ws: true,
          changeOrigin: true,
          secure: true
        },
        // Reverse-proxy Supabase Storage API to live Supabase backend
        '/storage': {
          target: supabaseTarget,
          changeOrigin: true,
          secure: true
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: false
    }
  };
});

