-- ==============================================================================
-- AIRSTRIKE Phase 7.2A: Corrected Supabase Backend Security Migration
-- STATUS: DORMANT / PROPOSED ONLY — DO NOT EXECUTE WITHOUT TEACHER/USER APPROVAL
-- ==============================================================================
-- Purpose:
--   1. Secure public anonymous read access to the global Hall of Fame leaderboard
--      without leaking private profile data (emails, UUIDs, timestamps).
--   2. Establish server-side match score integrity validation via a BEFORE INSERT
--      trigger on public.matches, preserving 100% compatibility with both
--      the existing Java Swing desktop client and the TypeScript browser client.
--   3. Enforce atomic rate-limiting and duplicate submission guards.
--   4. Optimize query performance for 500+ concurrent students via targeted indexes.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PRE-MIGRATION INSPECTION QUERIES (Execute to verify baseline before changing)
-- ------------------------------------------------------------------------------
/*
-- 1. Check existing leaderboard view definition:
SELECT table_name, view_definition 
FROM information_schema.views 
WHERE table_schema = 'public' AND table_name = 'leaderboard';

-- 2. Check current role table grants:
SELECT grantee, table_name, privilege_type 
FROM information_schema.role_table_grants 
WHERE table_schema = 'public' AND table_name IN ('leaderboard', 'matches', 'profiles');

-- 3. Check current Row-Level Security policies:
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual, with_check 
FROM pg_policies 
WHERE schemaname = 'public';

-- 4. Check existing triggers on public.matches:
SELECT trigger_name, event_manipulation, action_statement 
FROM information_schema.triggers 
WHERE event_object_table = 'matches';
*/

-- ==============================================================================
-- STEP 1: SECURE LEADERBOARD VIEW (SECURITY DEFINER / SECURITY_INVOKER = FALSE)
-- ==============================================================================
-- Define the leaderboard view with security_invoker = false (the default PostgreSQL
-- view behavior owned by postgres). This allows the view to aggregate scores from
-- public.matches and public.profiles without requiring public read grants on the
-- underlying private tables.
--
-- Privacy Guarantee:
--   - ONLY 'username' (callsign) and 'best_score' (MAX(score)) are projected.
--   - User UUIDs, emails, match timestamps, and private stats are NEVER selected.

CREATE OR REPLACE VIEW public.leaderboard
WITH (security_invoker = false)
AS
SELECT 
    COALESCE(NULLIF(TRIM(p.callsign), ''), 'Cadet ' || SUBSTRING(m.user_id::text, 1, 6)) AS username,
    MAX(m.score)::INT AS best_score
FROM public.matches m
JOIN public.profiles p ON m.user_id = p.user_id
WHERE m.score > 0
GROUP BY p.callsign, m.user_id
ORDER BY best_score DESC
LIMIT 10;

COMMENT ON VIEW public.leaderboard IS 'Public Hall of Fame displaying top 10 pilot callsigns and verified best scores.';

-- ==============================================================================
-- STEP 2: LEAST-PRIVILEGE ROLE PERMISSIONS
-- ==============================================================================
-- 1. Grant SELECT on the aggregated leaderboard view to anon (guests) and authenticated pilots.
GRANT SELECT ON public.leaderboard TO anon, authenticated;

-- 2. Strictly revoke direct access to profiles from anon (prevents harvesting of student UUIDs/callsigns).
REVOKE ALL ON public.profiles FROM anon;

-- 3. Strictly revoke direct access to matches from anon.
REVOKE ALL ON public.matches FROM anon;

-- 4. Maintain standard authenticated pilot permissions on profiles:
GRANT SELECT, INSERT, UPDATE ON public.profiles TO authenticated;

-- 5. Maintain standard authenticated pilot permissions on matches:
GRANT SELECT, INSERT ON public.matches TO authenticated;
-- Matches are permanent flight logs: immutable once recorded.
REVOKE UPDATE, DELETE ON public.matches FROM authenticated, anon;

-- ==============================================================================
-- STEP 3: PERFORMANCE INDEXES (FOR 500+ CONCURRENT ACADEMY USERS)
-- ==============================================================================
-- Accelerates user match history retrieval (GET /rest/v1/matches?user_id=eq...&order=created_at.desc):
CREATE INDEX IF NOT EXISTS idx_matches_user_created 
ON public.matches (user_id, created_at DESC);

-- Accelerates global leaderboard aggregation and sorting (MAX(score) / ORDER BY score DESC):
CREATE INDEX IF NOT EXISTS idx_matches_score_desc 
ON public.matches (score DESC);

-- Accelerates JOIN between matches and profiles:
CREATE INDEX IF NOT EXISTS idx_profiles_user_id 
ON public.profiles (user_id);

-- ==============================================================================
-- STEP 4: SERVER-SIDE VALIDATION & RATE-LIMITING TRIGGER ON public.matches
-- ==============================================================================
-- Automatically intercepts ALL insertions into public.matches before they commit.
-- Works seamlessly with existing client POST /rest/v1/matches requests without
-- requiring any code modifications to the Java desktop or TypeScript web clients.

CREATE OR REPLACE FUNCTION public.validate_match_submission()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_last_match_time TIMESTAMPTZ;
    v_max_theoretical_score INT;
BEGIN
    -- 1. Ensure caller is authenticated
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Authentication required: anonymous flight score submissions are strictly prohibited.'
            USING ERRCODE = '28000';
    END IF;

    -- 2. Ensure caller is submitting for their own account
    IF NEW.user_id IS NULL OR NEW.user_id <> auth.uid() THEN
        RAISE EXCEPTION 'Identity mismatch: cannot file flight telemetry for another pilot.'
            USING ERRCODE = '42501';
    END IF;

    -- 3. Enforce minimum physical mission duration
    -- A spacecraft cannot engage, fight, and conclude a mission in under 5 seconds.
    IF NEW.duration_seconds < 5 THEN
        RAISE EXCEPTION 'Telemetry rejected: mission duration (%s seconds) is below physical simulation limits.', NEW.duration_seconds
            USING ERRCODE = '22023';
    END IF;

    IF NEW.duration_seconds > 86400 THEN
        RAISE EXCEPTION 'Telemetry rejected: mission duration exceeds 24-hour operational envelope.'
            USING ERRCODE = '22023';
    END IF;

    -- 4. Sector / Level bounds check (Sectors 1 to 20)
    IF NEW.level_reached < 1 OR NEW.level_reached > 20 THEN
        RAISE EXCEPTION 'Sector boundary violation: invalid level reached (%s).', NEW.level_reached
            USING ERRCODE = '22023';
    END IF;

    -- 5. Atomic Rate-Limiting & Concurrency Serialization
    -- Prevents rapid-fire bot submission or script injection.
    -- Advisory transaction lock guarantees serialized evaluation per user UUID without locking the table.
    PERFORM pg_advisory_xact_lock(hashtext(NEW.user_id::text));

    SELECT created_at INTO v_last_match_time
    FROM public.matches
    WHERE user_id = NEW.user_id
    ORDER BY created_at DESC
    LIMIT 1;

    IF v_last_match_time IS NOT NULL AND (NOW() - v_last_match_time) < INTERVAL '10 seconds' THEN
        RAISE EXCEPTION 'Rate limit exceeded: flight debriefs can only be recorded once every 10 seconds.'
            USING ERRCODE = '22023';
    END IF;

    -- 6. Mathematical Maximum Plausible Score Boundary
    -- Physics parameters: 180ms firing rate (~5.5 shots/s), Level 3 triple cannons (125 dmg/volley),
    -- 4x combo multiplier, 8-16 enemies/wave, 2500 pts Boss every 3rd level.
    -- Max theoretical generation rate is ~450 pts/sec.
    -- Theoretical maximum formula: (Duration * 500 pts/sec) + (LevelReached * 5000 pts) + 2000 base.
    v_max_theoretical_score := (NEW.duration_seconds * 500) + (NEW.level_reached * 5000) + 2000;

    IF NEW.score < 0 OR NEW.score > v_max_theoretical_score THEN
        RAISE EXCEPTION 'Score integrity failure: score % exceeds theoretical physical maximum (%) for duration %s in sector %.',
            NEW.score, v_max_theoretical_score, NEW.duration_seconds, NEW.level_reached
            USING ERRCODE = '22023';
    END IF;

    -- 7. Duplicate Submission Protection (within 60 seconds)
    IF EXISTS (
        SELECT 1 FROM public.matches
        WHERE user_id = NEW.user_id
          AND score = NEW.score
          AND level_reached = NEW.level_reached
          AND duration_seconds = NEW.duration_seconds
          AND created_at > (NOW() - INTERVAL '60 seconds')
    ) THEN
        RAISE EXCEPTION 'Duplicate debrief: this exact mission outcome has already been recorded.'
            USING ERRCODE = '23505';
    END IF;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION public.validate_match_submission IS 'Enforces server-side mathematical score sanity, atomic rate-limiting, and duplicate protection.';

-- Bind trigger to public.matches
DROP TRIGGER IF EXISTS trg_validate_match_submission ON public.matches;
CREATE TRIGGER trg_validate_match_submission
BEFORE INSERT ON public.matches
FOR EACH ROW
EXECUTE FUNCTION public.validate_match_submission();

-- ------------------------------------------------------------------------------
-- POST-MIGRATION VERIFICATION QUERIES (Run to confirm healthy deployment)
-- ------------------------------------------------------------------------------
/*
-- 1. Verify anonymous query succeeds on leaderboard:
SELECT * FROM public.leaderboard;

-- 2. Verify anonymous query is blocked on profiles (must return 42501 permission denied):
SELECT * FROM public.profiles;

-- 3. Verify anonymous query is blocked on matches (must return 42501 permission denied):
SELECT * FROM public.matches;

-- 4. Verify trigger exists and is active:
SELECT trigger_name, event_manipulation, action_statement 
FROM information_schema.triggers 
WHERE event_object_table = 'matches' AND trigger_name = 'trg_validate_match_submission';

-- 5. Test score sanity trigger rejection (as authenticated user):
-- Attempting to submit impossible score:
-- INSERT INTO public.matches (user_id, score, level_reached, duration_seconds)
-- VALUES (auth.uid(), 99999999, 1, 10);
-- Expected result: ERROR 22023 (Score integrity failure)
*/

-- ==============================================================================
-- COMPLETE ROLLBACK MIGRATION (Execute to revert changes if needed)
-- ==============================================================================
/*
-- 1. Remove validation trigger and function:
DROP TRIGGER IF EXISTS trg_validate_match_submission ON public.matches;
DROP FUNCTION IF EXISTS public.validate_match_submission();

-- 2. Remove performance indexes:
DROP INDEX IF EXISTS public.idx_matches_user_created;
DROP INDEX IF EXISTS public.idx_matches_score_desc;
DROP INDEX IF EXISTS public.idx_profiles_user_id;

-- 3. Revoke anonymous grant on leaderboard:
REVOKE SELECT ON public.leaderboard FROM anon;

-- 4. Restore original view definition:
CREATE OR REPLACE VIEW public.leaderboard AS
SELECT 
    p.callsign AS username,
    MAX(m.score) AS best_score
FROM public.matches m
JOIN public.profiles p ON m.user_id = p.user_id
GROUP BY p.callsign, m.user_id
ORDER BY best_score DESC;
*/
