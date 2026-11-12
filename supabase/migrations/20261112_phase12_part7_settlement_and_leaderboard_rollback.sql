-- ==============================================================================
-- AIRSTRIKE Phase 12 (Part 7): Rollback Script for Settlement & Leaderboard
-- FILE: supabase/migrations/20261112_phase12_part7_settlement_and_leaderboard_rollback.sql
-- PURPOSE: Safely rolls back Part 7 migration functions and views
-- ==============================================================================

BEGIN;

DROP FUNCTION IF EXISTS public.get_player_multiplayer_history(uuid, int);
DROP FUNCTION IF EXISTS public.get_multiplayer_leaderboard(text, int);
DROP VIEW IF EXISTS public.v_multiplayer_leaderboard;
DROP FUNCTION IF EXISTS public.forfeit_multiplayer_match(uuid);

-- Revert finalize_multiplayer_match to Part 2 baseline signature
-- (Will restore from 20261111_phase12_multiplayer_schema.sql if rollback is applied)

COMMIT;

