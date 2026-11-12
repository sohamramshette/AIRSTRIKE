-- ==============================================================================
-- AIRSTRIKE Phase 7.2A.13: Final Production Forward Migration (Option B)
-- FILE: supabase/migrations/20261107_phase7_forward.sql
-- TARGET ARCHITECTURE: Option B (Public Anonymous Read-Only Leaderboard)
-- STATUS: DORMANT / PENDING HUMAN APPROVAL — ZERO DIRECT EXECUTION WITHOUT APPROVAL
-- ==============================================================================
-- Scope of Changes:
--   1. Embedded transaction-level preflight assertions verifying exact baseline.
--   2. GRANT SELECT ON public.leaderboard TO authenticated, anon.
--   3. CREATE POLICY "anon_select_leaderboard" ON public.leaderboard FOR SELECT TO anon USING (true).
--   4. GRANT SELECT, INSERT ON public.matches TO authenticated.
--   5. GRANT SELECT, INSERT, UPDATE ON public.profiles TO authenticated.
--   6. CREATE POLICY "authenticated_insert_own_profile" ON public.profiles FOR INSERT TO authenticated WITH CHECK (auth.uid() = id).
--   7. Embedded post-migration sanity assertions before commit.
-- ==============================================================================

BEGIN;

-- ------------------------------------------------------------------------------
-- PREFLIGHT VERIFICATION ASSERTIONS (FAIL-FAST TRANSACTION GUARD)
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_count integer;
    v_record record;
BEGIN
    -- 1. Table Existence, Storage Kind, Owner, and RLS Status
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_class c
    JOIN pg_catalog.pg_roles r ON r.oid = c.relowner
    WHERE c.relnamespace = 'public'::regnamespace
      AND c.relname IN ('leaderboard', 'matches', 'profiles')
      AND c.relkind = 'r'
      AND r.rolname = 'postgres'
      AND c.relrowsecurity = true;

    IF v_count <> 3 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Tables leaderboard, matches, profiles must all exist as ordinary physical tables (relkind = r) owned by postgres with relrowsecurity = true. Found % matching tables.', v_count;
    END IF;

    -- 2. Total Policy Count on Target Tables (Audited baseline has exactly 5 policies)
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename IN ('leaderboard', 'matches', 'profiles');

    IF v_count <> 5 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Expected exactly 5 baseline policies across leaderboard, matches, profiles. Found % policies.', v_count;
    END IF;

    -- 3. Assert Exact Baseline Policy on public.leaderboard (authenticated SELECT, qual = true)
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'leaderboard'
      AND cmd = 'SELECT'
      AND roles = '{authenticated}'
      AND permissive = 'PERMISSIVE'
      AND (qual = 'true' OR qual = '(true)')
      AND with_check IS NULL;

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Baseline policy on public.leaderboard must be authenticated SELECT with qual = true and permissive status.';
    END IF;

    -- 4. Assert Exact Baseline Policies on public.matches
    -- 4a. authenticated INSERT with check (auth.uid() = user_id)
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'matches'
      AND cmd = 'INSERT'
      AND roles = '{authenticated}'
      AND permissive = 'PERMISSIVE'
      AND with_check LIKE '%auth.uid()%user_id%';

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Baseline policy on public.matches must be authenticated INSERT with check auth.uid() = user_id.';
    END IF;

    -- 4b. authenticated SELECT with qual (auth.uid() = user_id)
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'matches'
      AND cmd = 'SELECT'
      AND roles = '{authenticated}'
      AND permissive = 'PERMISSIVE'
      AND qual LIKE '%auth.uid()%user_id%';

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Baseline policy on public.matches must be authenticated SELECT with qual auth.uid() = user_id.';
    END IF;

    -- 5. Assert Exact Baseline Policies on public.profiles
    -- 5a. authenticated SELECT with qual = true
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'profiles'
      AND cmd = 'SELECT'
      AND roles = '{authenticated}'
      AND permissive = 'PERMISSIVE'
      AND (qual = 'true' OR qual = '(true)');

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Baseline policy on public.profiles must be authenticated SELECT with qual = true.';
    END IF;

    -- 5b. authenticated UPDATE with auth.uid() = id
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename = 'profiles'
      AND cmd = 'UPDATE'
      AND roles = '{authenticated}'
      AND permissive = 'PERMISSIVE'
      AND (qual LIKE '%auth.uid()%id%' OR with_check LIKE '%auth.uid()%id%');

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Baseline policy on public.profiles must be authenticated UPDATE with auth.uid() = id.';
    END IF;

    -- 6. Assert Baseline Role Privileges (anon and authenticated must have 0 privileges across all 7 privilege types)
    FOR v_record IN
        SELECT t.tablename, r.rolname, p.privilege
        FROM (VALUES ('leaderboard'), ('matches'), ('profiles')) AS t(tablename)
        CROSS JOIN (VALUES ('anon'), ('authenticated')) AS r(rolname)
        CROSS JOIN (VALUES ('SELECT'), ('INSERT'), ('UPDATE'), ('DELETE'), ('TRUNCATE'), ('REFERENCES'), ('TRIGGER')) AS p(privilege)
    LOOP
        IF has_table_privilege(v_record.rolname, 'public.' || v_record.tablename, v_record.privilege) THEN
            RAISE EXCEPTION 'PREFLIGHT HALT: Role % already possesses unexpected % privilege on public.%. Baseline differs from audit.',
                v_record.rolname, v_record.privilege, v_record.tablename;
        END IF;
    END LOOP;

    -- 7. Assert Absence of Conflicting Migration Policy Names
    IF EXISTS (
        SELECT 1 FROM pg_catalog.pg_policies
        WHERE schemaname = 'public'
          AND policyname IN ('authenticated_insert_own_profile', 'anon_select_leaderboard')
    ) THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Proposed migration policy names already exist in pg_policies. Aborting to prevent collision or overwriting.';
    END IF;

    -- 8. Assert Triggers on Target Tables (Audited baseline has exactly 2 non-internal triggers)
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_trigger tg
    JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public'
      AND c.relname IN ('leaderboard', 'matches', 'profiles')
      AND NOT tg.tgisinternal;

    IF v_count <> 2 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Expected exactly 2 non-internal triggers across leaderboard, matches, profiles. Found %.', v_count;
    END IF;

    -- 8a. Leaderboard refresh trigger on public.matches
    -- Timing: AFTER (tgtype & 66 = 0)
    -- Events: INSERT OR UPDATE OR DELETE (tgtype & 28 = 28)
    -- Row-level: true (tgtype & 1 = 1)
    -- Enabled: 'O'
    -- Called Function: update_leaderboard_after_match
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_trigger tg
    JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
    JOIN pg_catalog.pg_namespace pn ON pn.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND c.relname = 'matches'
      AND tg.tgenabled = 'O'
      AND (tg.tgtype::integer & 66 = 0)
      AND (tg.tgtype::integer & 28 = 28)
      AND (tg.tgtype::integer & 1 = 1)
      AND pn.nspname = 'public'
      AND p.proname = 'update_leaderboard_after_match';

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Leaderboard refresh trigger on public.matches does not match audited behavior (AFTER INSERT OR UPDATE OR DELETE FOR EACH ROW, enabled, calling public.update_leaderboard_after_match).';
    END IF;

    -- 8b. Username sync trigger on public.profiles
    -- Timing: AFTER (tgtype & 66 = 0)
    -- Events: UPDATE (tgtype & 28 = 16)
    -- Row-level: true (tgtype & 1 = 1)
    -- Enabled: 'O'
    -- Called Function: sync_leaderboard_username
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_trigger tg
    JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
    JOIN pg_catalog.pg_namespace pn ON pn.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND c.relname = 'profiles'
      AND tg.tgenabled = 'O'
      AND (tg.tgtype::integer & 66 = 0)
      AND (tg.tgtype::integer & 28 = 16)
      AND (tg.tgtype::integer & 1 = 1)
      AND pn.nspname = 'public'
      AND p.proname = 'sync_leaderboard_username';

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Username sync trigger on public.profiles does not match audited behavior (AFTER UPDATE FOR EACH ROW, enabled, calling public.sync_leaderboard_username).';
    END IF;

    -- 9. Assert SECURITY DEFINER Functions Baseline
    -- 9a. Function refresh_player_leaderboard(uuid) -> void, owned by postgres, prosecdef = true, search_path = ''
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_proc p
    JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
    JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
    WHERE n.nspname = 'public'
      AND p.proname = 'refresh_player_leaderboard'
      AND r.rolname = 'postgres'
      AND p.prosecdef = true
      AND pg_get_function_result(p.oid) = 'void'
      AND pg_get_function_arguments(p.oid) LIKE '%uuid%'
      AND ('search_path=' = ANY(p.proconfig) OR 'search_path=""' = ANY(p.proconfig));

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Function refresh_player_leaderboard(uuid) baseline verification failed (must be owned by postgres, SECURITY DEFINER, search_path = empty).';
    END IF;

    -- 9b. Function update_leaderboard_after_match() -> trigger, owned by postgres, prosecdef = true, search_path = ''
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_proc p
    JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
    JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
    WHERE n.nspname = 'public'
      AND p.proname = 'update_leaderboard_after_match'
      AND r.rolname = 'postgres'
      AND p.prosecdef = true
      AND pg_get_function_result(p.oid) = 'trigger'
      AND ('search_path=' = ANY(p.proconfig) OR 'search_path=""' = ANY(p.proconfig));

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Function update_leaderboard_after_match() baseline verification failed (must be owned by postgres, SECURITY DEFINER, search_path = empty).';
    END IF;

    -- 9c. Function sync_leaderboard_username() -> trigger, owned by postgres, prosecdef = true, search_path = ''
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_proc p
    JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
    JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
    WHERE n.nspname = 'public'
      AND p.proname = 'sync_leaderboard_username'
      AND r.rolname = 'postgres'
      AND p.prosecdef = true
      AND pg_get_function_result(p.oid) = 'trigger'
      AND ('search_path=' = ANY(p.proconfig) OR 'search_path=""' = ANY(p.proconfig));

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Function sync_leaderboard_username() baseline verification failed (must be owned by postgres, SECURITY DEFINER, search_path = empty).';
    END IF;

    RAISE NOTICE 'AIRSTRIKE PREFLIGHT PASSED: Database baseline verified successfully.';
END $$;

-- ------------------------------------------------------------------------------
-- MIGRATION STATEMENTS (LEAST PRIVILEGE — OPTION B)
-- Zero redundant REVOKE statements.
-- ------------------------------------------------------------------------------

-- Step 1: Leaderboard Table Grants & Public Read Policy
GRANT SELECT ON public.leaderboard TO authenticated;
GRANT SELECT ON public.leaderboard TO anon;

CREATE POLICY "anon_select_leaderboard"
ON public.leaderboard
FOR SELECT
TO anon
USING (true);

-- Step 2: Matches Table Grants (Authenticated cadets only)
GRANT SELECT, INSERT ON public.matches TO authenticated;

-- Step 3: Profiles Table Grants & Upsert Policy (Authenticated cadets only)
-- Supports Desktop Java ProfileService 'Prefer: resolution=merge-duplicates' (INSERT ... ON CONFLICT DO UPDATE)
-- and Web Client AuthService profile synchronization.
GRANT SELECT, INSERT, UPDATE ON public.profiles TO authenticated;

CREATE POLICY "authenticated_insert_own_profile"
ON public.profiles
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = id);

-- ------------------------------------------------------------------------------
-- POST-MIGRATION SANITY ASSERTIONS (IN-TRANSACTION INTEGRITY GATE)
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_count integer;
BEGIN
    -- 1. Total policies must now be exactly 7 (5 baseline + 2 new)
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename IN ('leaderboard', 'matches', 'profiles');

    IF v_count <> 7 THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: Expected exactly 7 policies across leaderboard, matches, profiles. Found %.', v_count;
    END IF;

    -- 2. Verify anon permissions: ONLY SELECT on leaderboard
    IF NOT has_table_privilege('anon', 'public.leaderboard', 'SELECT') THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: anon missing SELECT on leaderboard.';
    END IF;

    IF has_table_privilege('anon', 'public.leaderboard', 'INSERT')
       OR has_table_privilege('anon', 'public.matches', 'SELECT')
       OR has_table_privilege('anon', 'public.profiles', 'SELECT') THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: anon granted excessive permissions.';
    END IF;

    -- 3. Verify authenticated permissions
    IF NOT (
        has_table_privilege('authenticated', 'public.leaderboard', 'SELECT')
        AND has_table_privilege('authenticated', 'public.matches', 'SELECT')
        AND has_table_privilege('authenticated', 'public.matches', 'INSERT')
        AND has_table_privilege('authenticated', 'public.profiles', 'SELECT')
        AND has_table_privilege('authenticated', 'public.profiles', 'INSERT')
        AND has_table_privilege('authenticated', 'public.profiles', 'UPDATE')
    ) THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: authenticated missing required table privileges.';
    END IF;

    -- 4. Verify authenticated does NOT have ungranted privileges (e.g. DELETE on matches or profiles)
    IF has_table_privilege('authenticated', 'public.matches', 'DELETE')
       OR has_table_privilege('authenticated', 'public.profiles', 'DELETE') THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: authenticated possesses unexpected DELETE privilege.';
    END IF;

    RAISE NOTICE 'AIRSTRIKE FORWARD MIGRATION COMPLETE: All post-migration assertions satisfied.';
END $$;

COMMIT;
