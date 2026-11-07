-- ==============================================================================
-- AIRSTRIKE Phase 7.2A.13: Post-Migration Database Verification Script
-- FILE: supabase/migrations/20261107_phase7_verification.sql
-- TARGET ARCHITECTURE: Option B (Public Read-Only Leaderboard)
-- STATUS: READ-ONLY AUDIT — ZERO MUTATIONS, ZERO DATA MODIFICATIONS
-- ==============================================================================
-- Execute these queries in the Supabase Dashboard SQL Editor as role 'postgres'
-- after applying 20261107_phase7_forward.sql to verify the migration.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. ROLE TABLE GRANTS VERIFICATION
-- ------------------------------------------------------------------------------
-- Expected Output:
--   leaderboard | anon          | SELECT
--   leaderboard | authenticated | SELECT
--   matches     | authenticated | INSERT
--   matches     | authenticated | SELECT
--   profiles    | authenticated | INSERT
--   profiles    | authenticated | SELECT
--   profiles    | authenticated | UPDATE
SELECT 
    table_name, 
    grantee, 
    privilege_type 
FROM information_schema.role_table_grants 
WHERE table_schema = 'public' 
  AND table_name IN ('leaderboard', 'matches', 'profiles')
  AND grantee IN ('anon', 'authenticated')
ORDER BY table_name, grantee, privilege_type;

-- ------------------------------------------------------------------------------
-- 2. EFFECTIVE TABLE PRIVILEGES ACROSS ALL 7 PRIVILEGE TYPES
-- ------------------------------------------------------------------------------
-- Expected:
--   anon: SELECT = true on leaderboard ONLY. All other 6 privileges FALSE.
--         All 7 privileges FALSE on matches and profiles.
--   authenticated: SELECT = true on leaderboard.
--                  SELECT = true, INSERT = true on matches (all others false).
--                  SELECT = true, INSERT = true, UPDATE = true on profiles (all others false).
--   service_role: true for all privileges.
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

-- ------------------------------------------------------------------------------
-- 3. ACTIVE ROW-LEVEL SECURITY POLICIES (EXPECTED: EXACTLY 7 POLICIES)
-- ------------------------------------------------------------------------------
-- Expected:
--   leaderboard (2):
--     1. [baseline] authenticated SELECT (USING: true)
--     2. anon_select_leaderboard: anon SELECT (USING: true)
--   matches (2):
--     1. [baseline] authenticated INSERT (WITH CHECK: (auth.uid() = user_id))
--     2. [baseline] authenticated SELECT (USING: (auth.uid() = user_id))
--   profiles (3):
--     1. [baseline] authenticated SELECT (USING: true)
--     2. [baseline] authenticated UPDATE (USING / WITH CHECK: (auth.uid() = id))
--     3. authenticated_insert_own_profile: authenticated INSERT (WITH CHECK: (auth.uid() = id))
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

-- ------------------------------------------------------------------------------
-- 4. CONFIRM TRIGGERS REMAIN UNCHANGED AND ACTIVE
-- ------------------------------------------------------------------------------
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

-- ------------------------------------------------------------------------------
-- 5. CONFIRM SECURITY DEFINER FUNCTIONS REMAIN UNTOUCHED
-- ------------------------------------------------------------------------------
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

