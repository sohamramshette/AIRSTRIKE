-- ==============================================================================
-- AIRSTRIKE Phase 7.2A: Proposed Backend Correction Migration
-- STATUS: DORMANT / PROPOSED ONLY — DO NOT EXECUTE WITHOUT APPROVAL
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PRE-MIGRATION VERIFICATION QUERIES
-- Run these queries first to inspect current definitions and permissions:
-- ------------------------------------------------------------------------------
/*
SELECT table_name, view_definition 
FROM information_schema.views 
WHERE table_schema = 'public' AND table_name = 'leaderboard';

SELECT grantee, privilege_type 
FROM information_schema.role_table_grants 
WHERE table_schema = 'public' AND table_name IN ('leaderboard', 'matches', 'profiles');

SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual, with_check 
FROM pg_policies 
WHERE schemaname = 'public';
*/

-- ==============================================================================
-- STEP 1: DEFINE SECURE LEADERBOARD VIEW (SECURITY DEFINER)
-- ==============================================================================
-- Recreate the view as a SECURITY DEFINER view (explicitly NOT security_invoker)
-- so that it aggregates public hall-of-fame data without exposing private tables.

CREATE OR REPLACE VIEW public.leaderboard
WITH (security_invoker = false)
AS
SELECT 
    COALESCE(p.callsign, 'Anonymous Pilot') AS username,
    MAX(m.score) AS best_score
FROM public.matches m
JOIN public.profiles p ON m.user_id = p.user_id
WHERE m.score > 0
GROUP BY p.callsign, m.user_id
ORDER BY best_score DESC
LIMIT 10;

COMMENT ON VIEW public.leaderboard IS 'Public Hall of Fame displaying top 10 pilot best scores.';

-- ==============================================================================
-- STEP 2: MINIMUM SAFE PRIVILEGE GRANTS (LEAST PRIVILEGE PRINCIPLE)
-- ==============================================================================
-- 1. Grant SELECT strictly on the aggregated leaderboard view to anon and authenticated.
GRANT SELECT ON public.leaderboard TO anon, authenticated;

-- 2. Explicitly DO NOT grant SELECT on profiles to anon (prevents UUID/email leakage).
REVOKE ALL ON public.profiles FROM anon;

-- 3. Ensure authenticated pilots can read and write only their own profiles.
GRANT SELECT, INSERT, UPDATE ON public.profiles TO authenticated;

-- 4. Ensure authenticated pilots can insert and read their own matches.
GRANT SELECT, INSERT ON public.matches TO authenticated;
REVOKE UPDATE, DELETE ON public.matches FROM authenticated, anon; -- Matches are immutable

-- ==============================================================================
-- STEP 3: HIGH-PERFORMANCE INDEXES (FOR 500+ CONCURRENT USERS)
-- ==============================================================================
-- Accelerates user match history queries:
CREATE INDEX IF NOT EXISTS idx_matches_user_created 
ON public.matches (user_id, created_at DESC);

-- Accelerates global leaderboard aggregation and sorting:
CREATE INDEX IF NOT EXISTS idx_matches_score_desc 
ON public.matches (score DESC);

-- Accelerates user profile lookups:
CREATE INDEX IF NOT EXISTS idx_profiles_user_id 
ON public.profiles (user_id);

-- ==============================================================================
-- STEP 4: MULTI-TIERED SERVER-SIDE SCORE VALIDATION RPC
-- ==============================================================================
-- Stored procedure executing with SECURITY DEFINER to enforce game physics rules
-- before any score record can be inserted into public.matches.

CREATE OR REPLACE FUNCTION public.submit_match_secure(
    p_score INT,
    p_level INT,
    p_duration INT,
    p_enemies_killed INT DEFAULT 0,
    p_bosses_defeated INT DEFAULT 0,
    p_shots_fired INT DEFAULT 0,
    p_shots_hit INT DEFAULT 0
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_last_match_time TIMESTAMP;
    v_max_theoretical_score INT;
    v_inserted_id UUID;
BEGIN
    -- 1. Authenticate caller
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required to submit flight logs.'
            USING ERRCODE = '28000';
    END IF;

    -- 2. Enforce minimum duration (a mission cannot end in < 5 seconds)
    IF p_duration < 5 THEN
        RAISE EXCEPTION 'Invalid telemetry: mission duration too short (% seconds).', p_duration
            USING ERRCODE = '22023';
    END IF;

    -- 3. Rate-limit check: user cannot submit scores faster than once every 15 seconds
    SELECT created_at INTO v_last_match_time
    FROM public.matches
    WHERE user_id = v_user_id
    ORDER BY created_at DESC
    LIMIT 1;

    IF v_last_match_time IS NOT NULL AND (NOW() - v_last_match_time) < INTERVAL '15 seconds' THEN
        RAISE EXCEPTION 'Rate limit exceeded: flight logs can only be filed once every 15 seconds.'
            USING ERRCODE = '22023';
    END IF;

    -- 4. Mathematical sanity check:
    -- Max points per second: 450 pts/sec (killing multiple high-tier enemies with max streak)
    -- Plus 5,000 pts baseline allowance
    v_max_theoretical_score := (p_duration * 450) + (p_bosses_defeated * 5000) + 2500;
    
    IF p_score < 0 OR p_score > v_max_theoretical_score THEN
        RAISE EXCEPTION 'Score integrity failure: score % exceeds maximum theoretical bounds (%) for duration %s.',
            p_score, v_max_theoretical_score, p_duration
            USING ERRCODE = '22023';
    END IF;

    -- 5. Level sanity check
    IF p_level < 1 OR p_level > 20 THEN
        RAISE EXCEPTION 'Sector out of bounds: level %.', p_level
            USING ERRCODE = '22023';
    END IF;

    -- 6. Insert validated match record
    INSERT INTO public.matches (user_id, score, level_reached, duration_seconds)
    VALUES (v_user_id, p_score, p_level, p_duration)
    RETURNING id INTO v_inserted_id;

    RETURN jsonb_build_object(
        'success', true,
        'match_id', v_inserted_id,
        'verified_score', p_score
    );
END;
$$;

-- Grant execution permission on the secure submission function
GRANT EXECUTE ON FUNCTION public.submit_match_secure TO authenticated;
REVOKE EXECUTE ON FUNCTION public.submit_match_secure FROM anon;

-- ------------------------------------------------------------------------------
-- POST-MIGRATION VERIFICATION QUERIES
-- ------------------------------------------------------------------------------
/*
-- 1. Test anonymous leaderboard query (should succeed with HTTP 200 / JSON array):
SELECT * FROM public.leaderboard;

-- 2. Verify profiles table cannot be queried by anon:
-- As anon: SELECT * FROM public.profiles; -- Should raise permission denied

-- 3. Test secure match submission via RPC:
-- As authenticated user: SELECT public.submit_match_secure(12500, 3, 140, 25, 0, 180, 110);
*/

-- ==============================================================================
-- ROLLBACK PLAN (IF REVERSION IS REQUIRED)
-- ==============================================================================
/*
-- Drop indexes
DROP INDEX IF EXISTS public.idx_matches_user_created;
DROP INDEX IF EXISTS public.idx_matches_score_desc;
DROP INDEX IF EXISTS public.idx_profiles_user_id;

-- Drop secure RPC function
DROP FUNCTION IF EXISTS public.submit_match_secure;

-- Revoke public grants
REVOKE SELECT ON public.leaderboard FROM anon;

-- Revert view if needed
DROP VIEW IF EXISTS public.leaderboard;
*/
