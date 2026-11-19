-- ==============================================================================
-- ROLLBACK: Remove Player Name (username) from public.matches
-- FILE: supabase/migrations/20261120_add_username_to_matches_rollback.sql
-- ==============================================================================

DROP TRIGGER IF EXISTS trg_sync_matches_username ON public.profiles;
DROP FUNCTION IF EXISTS public.sync_matches_username();

DROP TRIGGER IF EXISTS trg_set_match_username ON public.matches;
DROP FUNCTION IF EXISTS public.set_match_username();

DROP INDEX IF EXISTS public.idx_matches_username;

ALTER TABLE public.matches DROP COLUMN IF EXISTS username;
