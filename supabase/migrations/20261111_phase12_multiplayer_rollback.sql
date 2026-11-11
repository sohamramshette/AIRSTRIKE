-- ==============================================================================
-- AIRSTRIKE Phase 12.2: Rollback Script for Multiplayer Schema
-- FILE: supabase/migrations/20261111_phase12_multiplayer_rollback.sql
-- PURPOSE: Safely removes multiplayer tables, policies, and RPCs without
--          affecting existing single-player tables (profiles, matches, leaderboard)
-- STATUS: DORMANT / CONTINGENCY ONLY
-- ==============================================================================

BEGIN;

-- 1. Drop Stored Procedures (RPCs)
DROP FUNCTION IF EXISTS public.finalize_multiplayer_match(uuid, int, int);
DROP FUNCTION IF EXISTS public.start_multiplayer_match(uuid);
DROP FUNCTION IF EXISTS public.set_multiplayer_ready(uuid);
DROP FUNCTION IF EXISTS public.join_multiplayer_room(text);
DROP FUNCTION IF EXISTS public.create_multiplayer_room();
DROP FUNCTION IF EXISTS public.generate_multiplayer_room_code();

-- 2. Drop Policies on Multiplayer Tables
DROP POLICY IF EXISTS "multiplayer_rooms_select" ON public.multiplayer_rooms;
DROP POLICY IF EXISTS "multiplayer_matches_select" ON public.multiplayer_matches;
DROP POLICY IF EXISTS "mp_leaderboard_select_auth" ON public.multiplayer_leaderboard;
DROP POLICY IF EXISTS "mp_leaderboard_select_anon" ON public.multiplayer_leaderboard;

-- 3. Drop Tables (in reverse dependency order)
DROP TABLE IF EXISTS public.multiplayer_matches;
DROP TABLE IF EXISTS public.multiplayer_rooms;
DROP TABLE IF EXISTS public.multiplayer_leaderboard;

COMMIT;

