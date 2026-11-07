-- ==============================================================================
-- AIRSTRIKE Phase 7.2A.13: Safe Symmetrical Rollback Script
-- FILE: supabase/migrations/20261107_phase7_rollback.sql
-- TARGET ARCHITECTURE: Option B Rollback (Reverts to Exact Audited Baseline)
-- STATUS: DORMANT / CONTINGENCY ONLY — ZERO DIRECT EXECUTION WITHOUT APPROVAL
-- ==============================================================================
-- Safety Guarantees:
--   1. Validates migration policies still match expected definitions before dropping.
--   2. Will NOT drop policies if they were manually altered by an administrator.
--   3. Revokes ONLY privileges granted by the forward migration.
--   4. Valid PostgreSQL REVOKE ... FROM syntax (zero REVOKE ... TO).
--   5. Introduces ZERO unverified GRANT statements.
--   6. Restores the exact audited 0-privilege baseline for anon and authenticated.
-- ==============================================================================

BEGIN;

-- ------------------------------------------------------------------------------
-- PRE-ROLLBACK POLICY INTEGRITY VALIDATION
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_record record;
BEGIN
    -- 1. Validate anon_select_leaderboard if present
    SELECT * INTO v_record
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'leaderboard'
      AND policyname = 'anon_select_leaderboard';

    IF FOUND THEN
        IF NOT (
            v_record.cmd = 'SELECT'
            AND v_record.roles = '{anon}'
            AND v_record.permissive = 'PERMISSIVE'
            AND (v_record.qual = 'true' OR v_record.qual = '(true)')
            AND v_record.with_check IS NULL
        ) THEN
            RAISE EXCEPTION 'ROLLBACK HALT: Policy anon_select_leaderboard on public.leaderboard exists but its definition differs from the migration template. Aborting rollback to prevent dropping an unexpected policy.';
        END IF;
    END IF;

    -- 2. Validate authenticated_insert_own_profile if present
    SELECT * INTO v_record
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'profiles'
      AND policyname = 'authenticated_insert_own_profile';

    IF FOUND THEN
        IF NOT (
            v_record.cmd = 'INSERT'
            AND v_record.roles = '{authenticated}'
            AND v_record.permissive = 'PERMISSIVE'
            AND v_record.with_check LIKE '%auth.uid()%id%'
        ) THEN
            RAISE EXCEPTION 'ROLLBACK HALT: Policy authenticated_insert_own_profile on public.profiles exists but its definition differs from the migration template. Aborting rollback to prevent dropping an unexpected policy.';
        END IF;
    END IF;

    RAISE NOTICE 'AIRSTRIKE ROLLBACK PRE-CHECK PASSED: Migration policies verified for safe removal.';
END $$;

-- ------------------------------------------------------------------------------
-- STEP 1: DROP ONLY VERIFIED MIGRATION POLICIES
-- ------------------------------------------------------------------------------
DROP POLICY IF EXISTS "anon_select_leaderboard" ON public.leaderboard;
DROP POLICY IF EXISTS "authenticated_insert_own_profile" ON public.profiles;

-- ------------------------------------------------------------------------------
-- STEP 2: REVOKE ONLY PRIVILEGES GRANTED BY FORWARD MIGRATION
-- Using valid PostgreSQL REVOKE ... FROM syntax.
-- ------------------------------------------------------------------------------
-- Revoke anonymous grant
REVOKE SELECT ON public.leaderboard FROM anon;

-- Revoke authenticated grants
REVOKE SELECT ON public.leaderboard FROM authenticated;
REVOKE SELECT, INSERT ON public.matches FROM authenticated;
REVOKE SELECT, INSERT, UPDATE ON public.profiles FROM authenticated;

-- ------------------------------------------------------------------------------
-- STEP 3: POST-ROLLBACK VERIFICATION ASSERTIONS
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_count integer;
    v_record record;
BEGIN
    -- 1. Total policies must be restored to exactly 5
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename IN ('leaderboard', 'matches', 'profiles');

    IF v_count <> 5 THEN
        RAISE EXCEPTION 'ROLLBACK VERIFICATION HALT: Expected exactly 5 policies after rollback. Found %.', v_count;
    END IF;

    -- 2. Neither migration policy may exist
    IF EXISTS (
        SELECT 1 FROM pg_catalog.pg_policies
        WHERE schemaname = 'public'
          AND policyname IN ('anon_select_leaderboard', 'authenticated_insert_own_profile')
    ) THEN
        RAISE EXCEPTION 'ROLLBACK VERIFICATION HALT: One or more migration policies still exist.';
    END IF;

    -- 3. Both anon and authenticated must have 0 privileges across all 7 types
    FOR v_record IN
        SELECT t.tablename, r.rolname, p.privilege
        FROM (VALUES ('leaderboard'), ('matches'), ('profiles')) AS t(tablename)
        CROSS JOIN (VALUES ('anon'), ('authenticated')) AS r(rolname)
        CROSS JOIN (VALUES ('SELECT'), ('INSERT'), ('UPDATE'), ('DELETE'), ('TRUNCATE'), ('REFERENCES'), ('TRIGGER')) AS p(privilege)
    LOOP
        IF has_table_privilege(v_record.rolname, 'public.' || v_record.tablename, v_record.privilege) THEN
            RAISE EXCEPTION 'ROLLBACK VERIFICATION HALT: Role % still possesses % privilege on public.%. Baseline not restored.',
                v_record.rolname, v_record.privilege, v_record.tablename;
        END IF;
    END LOOP;

    RAISE NOTICE 'AIRSTRIKE ROLLBACK COMPLETE: Exact audited baseline restored successfully.';
END $$;

COMMIT;

