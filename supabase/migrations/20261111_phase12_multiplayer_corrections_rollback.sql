-- ==============================================================================
-- AIRSTRIKE Phase 12.3: Rollback Script for Multiplayer Corrections
-- FILE: supabase/migrations/20261111_phase12_multiplayer_corrections_rollback.sql
-- PURPOSE: Safely drops leave_multiplayer_room() and get_multiplayer_room() RPCs
-- STATUS: DORMANT / CONTINGENCY ONLY
-- ==============================================================================

BEGIN;

DROP FUNCTION IF EXISTS public.get_multiplayer_room(uuid);
DROP FUNCTION IF EXISTS public.leave_multiplayer_room(uuid);

COMMIT;

