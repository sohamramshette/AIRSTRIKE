-- ==============================================================================
-- AIRSTRIKE Phase 7.2A.13: Final Master Production Database Migration Omnibus
-- RECOMMENDED MIGRATION: OPTION B (PUBLIC READ-ONLY LEADERBOARD)
-- STATUS: DORMANT / PROPOSED ONLY — DO NOT EXECUTE WITHOUT TEACHER/USER APPROVAL
-- STRICT GOVERNANCE: READ-ONLY AUDIT FIRST, ZERO DIRECT MUTATIONS, ZERO KEY EXPOSURE
-- ==============================================================================
-- Source of Truth (Verified from Production Supabase Audit):
--   1. public.leaderboard is a physical TABLE (relkind = 'r'), NOT a view.
--      Has primary key leaderboard_pkey(user_id) referencing profiles(id).
--      Maintained by existing triggers and SECURITY DEFINER functions.
--   2. All 3 tables (profiles, matches, leaderboard) already have RLS enabled (relrowsecurity = true)
--      and are owned by role 'postgres'.
--   3. All 7 required indexes and 2 triggers already exist in production.
--   4. Root cause of HTTP 401 / 42501: 'anon' and 'authenticated' roles have 0 table SQL grants.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PART A: READ-ONLY PREFLIGHT VERIFICATION QUERIES (RUN IN SUPABASE SQL EDITOR)
-- NOTE: Prepared for administrative execution. Zero mutations.
-- ------------------------------------------------------------------------------
/*
-- 1. Verify table existence, ordinary table status ('r'), table owner ('postgres'), and RLS status:
SELECT 
    c.relname AS table_name,
    c.relkind AS table_kind,
    pg_roles.rolname AS owner,
    c.relrowsecurity AS rls_enabled
FROM pg_catalog.pg_class c
JOIN pg_catalog.pg_roles ON pg_roles.oid = c.relowner
WHERE c.relnamespace = 'public'::regnamespace 
  AND c.relname IN ('leaderboard', 'matches', 'profiles')
ORDER BY c.relname;

-- 2. Inspect all 7 effective table privileges for anon, authenticated, and service_role:
SELECT 
    t.tablename,
    r.rolname,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'SELECT') AS can_select,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'INSERT') AS can_insert,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'UPDATE') AS can_update,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'DELETE') AS can_delete,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'TRUNCATE') AS can_truncate,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'REFERENCES') AS can_references,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'TRIGGER') AS can_trigger
FROM (VALUES ('leaderboard'), ('matches'), ('profiles')) AS t(tablename)
CROSS JOIN (VALUES ('anon'), ('authenticated'), ('service_role')) AS r(rolname)
ORDER BY t.tablename, r.rolname;

-- 3. Inspect existing Row-Level Security policy definitions:
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check 
FROM pg_catalog.pg_policies 
WHERE schemaname = 'public' 
  AND tablename IN ('leaderboard', 'matches', 'profiles')
ORDER BY tablename, policyname;

-- 4. Inspect existing triggers on public tables:
SELECT 
    tg.tgname AS trigger_name,
    c.relname AS table_name,
    tg.tgenabled AS trigger_enabled,
    CASE tg.tgtype::integer & 66
        WHEN 2 THEN 'BEFORE'
        WHEN 64 THEN 'INSTEAD OF'
        ELSE 'AFTER'
    END AS trigger_timing,
    CASE tg.tgtype::integer & 28
        WHEN 4 THEN 'INSERT'
        WHEN 8 THEN 'DELETE'
        WHEN 16 THEN 'UPDATE'
        WHEN 20 THEN 'INSERT OR UPDATE'
        WHEN 28 THEN 'INSERT OR UPDATE OR DELETE'
        ELSE 'OTHER'
    END AS trigger_events,
    (tg.tgtype::integer & 1 = 1) AS is_row_level,
    p.proname AS called_function,
    p.prosecdef AS is_security_definer,
    r.rolname AS function_owner,
    pg_get_triggerdef(tg.oid, true) AS full_trigger_definition
FROM pg_catalog.pg_trigger tg
JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'public' 
  AND c.relname IN ('leaderboard', 'matches', 'profiles')
  AND NOT tg.tgisinternal
ORDER BY c.relname, tg.tgname;

-- 5. Inspect existing SECURITY DEFINER function signatures and definitions:
SELECT 
    p.proname AS function_name,
    pg_get_function_arguments(p.oid) AS argument_signature,
    pg_get_function_result(p.oid) AS return_type,
    r.rolname AS function_owner,
    p.prosecdef AS is_security_definer,
    p.proconfig AS runtime_config,
    pg_get_functiondef(p.oid) AS function_definition
FROM pg_catalog.pg_proc p
JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'public' 
  AND p.proname IN ('refresh_player_leaderboard', 'update_leaderboard_after_match', 'sync_leaderboard_username')
ORDER BY p.proname;

-- 6. Verify that proposed migration policy names do NOT already exist:
SELECT schemaname, tablename, policyname
FROM pg_catalog.pg_policies
WHERE schemaname = 'public'
  AND policyname IN ('authenticated_insert_own_profile', 'anon_select_leaderboard');

-- 7. Inspect auth.users triggers using tgfoid joined directly to pg_proc:
SELECT 
    tg.tgname AS trigger_name,
    c.relname AS table_name,
    tg.tgenabled AS trigger_enabled,
    p.proname AS called_function,
    p.prosecdef AS is_security_definer,
    r.rolname AS function_owner,
    pg_get_triggerdef(tg.oid, true) AS full_trigger_definition
FROM pg_catalog.pg_trigger tg
JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'auth' 
  AND c.relname = 'users'
  AND NOT tg.tgisinternal
ORDER BY tg.tgname;
*/

-- ==============================================================================
-- PART B: FINAL RECOMMENDED PRODUCTION FORWARD MIGRATION (OPTION B)
-- Allows unauthenticated guests/anon to view the global Hall of Fame leaderboard.
-- Allows authenticated cadets to view leaderboard, submit matches, and upsert profile.
-- Contains zero redundant REVOKE statements.
-- ==============================================================================
/*
BEGIN;

-- Complete Embedded Preflight Verification Assertions
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
        RAISE EXCEPTION 'PREFLIGHT HALT: Tables leaderboard, matches, profiles must exist as ordinary physical tables (relkind = r) owned by postgres with relrowsecurity = true. Found % matching.', v_count;
    END IF;

    -- 2. Total Policy Count on Target Tables (Audited baseline has exactly 5 policies)
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename IN ('leaderboard', 'matches', 'profiles');

    IF v_count <> 5 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Expected exactly 5 baseline policies across leaderboard, matches, profiles. Found % policies.', v_count;
    END IF;

    -- 3. Assert Exact Baseline Policy on public.leaderboard
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
        RAISE EXCEPTION 'PREFLIGHT HALT: Baseline policy on public.leaderboard must be authenticated SELECT with qual = true.';
    END IF;

    -- 4. Assert Exact Baseline Policies on public.matches
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

    -- 6. Assert Baseline Role Privileges (0 table privileges for anon or authenticated across all 7 privileges)
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

    -- 8. Assert Triggers on Target Tables
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

    -- 8a. Trigger update_leaderboard_after_match on public.matches
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_trigger tg
    JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
    JOIN pg_catalog.pg_namespace pn ON pn.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND c.relname = 'matches'
      AND tg.tgname = 'update_leaderboard_after_match'
      AND tg.tgenabled = 'O'
      AND (tg.tgtype::integer & 66 = 0)
      AND (tg.tgtype::integer & 28 = 28)
      AND (tg.tgtype::integer & 1 = 1)
      AND pn.nspname = 'public'
      AND p.proname = 'update_leaderboard_after_match';

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Trigger update_leaderboard_after_match on public.matches does not match audited baseline.';
    END IF;

    -- 8b. Trigger sync_leaderboard_username on public.profiles
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_trigger tg
    JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
    JOIN pg_catalog.pg_namespace pn ON pn.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND c.relname = 'profiles'
      AND tg.tgname = 'sync_leaderboard_username'
      AND tg.tgenabled = 'O'
      AND (tg.tgtype::integer & 66 = 0)
      AND (tg.tgtype::integer & 28 = 16)
      AND (tg.tgtype::integer & 1 = 1)
      AND pn.nspname = 'public'
      AND p.proname = 'sync_leaderboard_username';

    IF v_count <> 1 THEN
        RAISE EXCEPTION 'PREFLIGHT HALT: Trigger sync_leaderboard_username on public.profiles does not match audited baseline.';
    END IF;

    -- 9. Assert SECURITY DEFINER Functions Baseline
    -- 9a. Function refresh_player_leaderboard(uuid)
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
        RAISE EXCEPTION 'PREFLIGHT HALT: Function refresh_player_leaderboard(uuid) baseline assertion failed.';
    END IF;

    -- 9b. Function update_leaderboard_after_match()
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
        RAISE EXCEPTION 'PREFLIGHT HALT: Function update_leaderboard_after_match() baseline assertion failed.';
    END IF;

    -- 9c. Function sync_leaderboard_username()
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
        RAISE EXCEPTION 'PREFLIGHT HALT: Function sync_leaderboard_username() baseline assertion failed.';
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
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename IN ('leaderboard', 'matches', 'profiles');

    IF v_count <> 7 THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: Expected exactly 7 policies across leaderboard, matches, profiles. Found %.', v_count;
    END IF;

    IF NOT has_table_privilege('anon', 'public.leaderboard', 'SELECT') THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: anon missing SELECT on leaderboard.';
    END IF;

    IF has_table_privilege('anon', 'public.leaderboard', 'INSERT')
       OR has_table_privilege('anon', 'public.matches', 'SELECT')
       OR has_table_privilege('anon', 'public.profiles', 'SELECT') THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: anon granted excessive permissions.';
    END IF;

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

    IF has_table_privilege('authenticated', 'public.matches', 'DELETE')
       OR has_table_privilege('authenticated', 'public.profiles', 'DELETE') THEN
        RAISE EXCEPTION 'POST-MIGRATION HALT: authenticated possesses unexpected DELETE privilege.';
    END IF;

    RAISE NOTICE 'AIRSTRIKE FORWARD MIGRATION COMPLETE: All post-migration assertions satisfied.';
END $$;

COMMIT;
*/

-- ==============================================================================
-- PART C: COMPLETE SYMMETRICAL ROLLBACK (EXACT BASELINE RESTORATION)
-- NOTE: Uses valid PostgreSQL REVOKE ... FROM syntax.
-- Zero unverified GRANT statements. Restores exact 0-permission original state.
-- Removes ONLY policies created by this migration.
-- ==============================================================================
/*
BEGIN;

-- 1. Pre-Rollback Policy Validation (Ensures policies have not been modified)
DO $$
DECLARE
    v_record record;
BEGIN
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
            RAISE EXCEPTION 'ROLLBACK HALT: Policy anon_select_leaderboard has been modified. Aborting.';
        END IF;
    END IF;

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
            RAISE EXCEPTION 'ROLLBACK HALT: Policy authenticated_insert_own_profile has been modified. Aborting.';
        END IF;
    END IF;

    RAISE NOTICE 'AIRSTRIKE ROLLBACK PRE-CHECK PASSED: Policies verified for safe removal.';
END $$;

-- 2. Drop ONLY the specific policies created by this migration
DROP POLICY IF EXISTS "anon_select_leaderboard" ON public.leaderboard;
DROP POLICY IF EXISTS "authenticated_insert_own_profile" ON public.profiles;

-- 3. Revoke anonymous grant on leaderboard
REVOKE SELECT ON public.leaderboard FROM anon;

-- 4. Revoke authenticated grants on all 3 tables
REVOKE SELECT ON public.leaderboard FROM authenticated;
REVOKE SELECT, INSERT ON public.matches FROM authenticated;
REVOKE SELECT, INSERT, UPDATE ON public.profiles FROM authenticated;

-- 5. Post-Rollback Verification Assertions
DO $$
DECLARE
    v_count integer;
    v_record record;
BEGIN
    SELECT count(*) INTO v_count
    FROM pg_catalog.pg_policies
    WHERE schemaname = 'public'
      AND tablename IN ('leaderboard', 'matches', 'profiles');

    IF v_count <> 5 THEN
        RAISE EXCEPTION 'ROLLBACK VERIFICATION HALT: Expected exactly 5 policies after rollback. Found %.', v_count;
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_catalog.pg_policies
        WHERE schemaname = 'public'
          AND policyname IN ('anon_select_leaderboard', 'authenticated_insert_own_profile')
    ) THEN
        RAISE EXCEPTION 'ROLLBACK VERIFICATION HALT: Migration policies still exist.';
    END IF;

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
*/

-- ==============================================================================
-- PART D: POST-MIGRATION VERIFICATION QUERIES (RUN IN SQL EDITOR AS postgres)
-- ==============================================================================
/*
-- 1. Verify role table grants now match the intended specification:
SELECT table_name, grantee, privilege_type 
FROM information_schema.role_table_grants 
WHERE table_schema = 'public' 
  AND table_name IN ('leaderboard', 'matches', 'profiles')
  AND grantee IN ('anon', 'authenticated')
ORDER BY table_name, grantee, privilege_type;

-- 2. Test effective permissions via has_table_privilege across all 7 privileges:
SELECT 
    t.tablename,
    r.rolname,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'SELECT') AS can_select,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'INSERT') AS can_insert,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'UPDATE') AS can_update,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'DELETE') AS can_delete,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'TRUNCATE') AS can_truncate,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'REFERENCES') AS can_references,
    has_table_privilege(r.rolname, 'public.' || t.tablename, 'TRIGGER') AS can_trigger
FROM (VALUES ('leaderboard'), ('matches'), ('profiles')) AS t(tablename)
CROSS JOIN (VALUES ('anon'), ('authenticated'), ('service_role')) AS r(rolname)
ORDER BY t.tablename, r.rolname;

-- 3. Confirm policy status on all 3 target tables (expected 7 policies):
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check 
FROM pg_catalog.pg_policies 
WHERE schemaname = 'public' 
  AND tablename IN ('leaderboard', 'matches', 'profiles')
ORDER BY tablename, policyname;

-- 4. Confirm triggers remain intact and active:
SELECT 
    tg.tgname AS trigger_name,
    c.relname AS table_name,
    tg.tgenabled AS trigger_enabled,
    p.proname AS called_function,
    p.prosecdef AS is_security_definer,
    r.rolname AS function_owner
FROM pg_catalog.pg_trigger tg
JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'public' 
  AND c.relname IN ('leaderboard', 'matches', 'profiles')
  AND NOT tg.tgisinternal
ORDER BY c.relname, tg.tgname;

-- 5. Confirm functions remain intact, owned by postgres, SECURITY DEFINER:
SELECT 
    p.proname AS function_name,
    pg_get_function_arguments(p.oid) AS argument_signature,
    pg_get_function_result(p.oid) AS return_type,
    r.rolname AS function_owner,
    p.prosecdef AS is_security_definer,
    p.proconfig AS runtime_config
FROM pg_catalog.pg_proc p
JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'public' 
  AND p.proname IN ('refresh_player_leaderboard', 'update_leaderboard_after_match', 'sync_leaderboard_username')
ORDER BY p.proname;
*/
