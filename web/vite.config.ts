import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const supabaseTarget = env.VITE_SUPABASE_LOCAL_TARGET || 'http://localhost:3002';

  return {
    root: '.',
    server: {
      port: 3000,
      strictPort: true, // Guarantees the game stays locked to port 3000
      open: false,
      host: true,
      cors: true,
      proxy: {
        // Reverse-proxy Supabase Auth GoTrue API to port 3002
        '/auth': {
          target: supabaseTarget,
          changeOrigin: true,
          secure: false
        },
        // Reverse-proxy Supabase PostgREST REST API to port 3002
        '/rest': {
          target: supabaseTarget,
          changeOrigin: true,
          secure: false
        },
        // Reverse-proxy Supabase Realtime WebSocket to port 3002
        '/realtime': {
          target: supabaseTarget,
          ws: true,
          changeOrigin: true,
          secure: false
        },
        // Reverse-proxy Supabase Storage API to port 3002
        '/storage': {
          target: supabaseTarget,
          changeOrigin: true,
          secure: false
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

