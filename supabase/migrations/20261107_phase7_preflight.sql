-- ==============================================================================
-- AIRSTRIKE Phase 7.2A.13: Read-Only Database Preflight Inspection Script
-- FILE: supabase/migrations/20261107_phase7_preflight.sql
-- STATUS: READ-ONLY AUDIT — ZERO MUTATIONS, ZERO DATA MODIFICATIONS
-- ==============================================================================
-- Execute these queries in the Supabase Dashboard SQL Editor as role 'postgres'
-- to capture and verify the exact live production baseline before any migration.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. TABLE KINDS, RELOPTIONS, OWNER, AND ROW-LEVEL SECURITY STATUS
-- ------------------------------------------------------------------------------
-- Expected: relkind = 'r' (ordinary table), owner = 'postgres', relrowsecurity = true
SELECT 
    c.relname AS table_name,
    c.relkind AS table_kind,
    pg_roles.rolname AS table_owner,
    c.relrowsecurity AS rls_enabled,
    c.reloptions AS table_options
FROM pg_catalog.pg_class c
JOIN pg_catalog.pg_roles ON pg_roles.oid = c.relowner
WHERE c.relnamespace = 'public'::regnamespace 
  AND c.relname IN ('leaderboard', 'matches', 'profiles')
ORDER BY c.relname;

-- ------------------------------------------------------------------------------
-- 2. EFFECTIVE TABLE PRIVILEGES ACROSS ALL 7 POSTGRESQL PRIVILEGE TYPES
-- ------------------------------------------------------------------------------
-- Expected: anon and authenticated must have FALSE for all 7 privileges.
-- service_role has TRUE for all privileges.
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
-- 3. EXACT ROW-LEVEL SECURITY POLICY DEFINITIONS
-- ------------------------------------------------------------------------------
-- Expected: Exactly 5 baseline policies:
--   leaderboard: authenticated SELECT (USING: true)
--   matches: authenticated INSERT (WITH CHECK: (auth.uid() = user_id))
--   matches: authenticated SELECT (USING: (auth.uid() = user_id))
--   profiles: authenticated SELECT (USING: true)
--   profiles: authenticated UPDATE (USING / WITH CHECK: (auth.uid() = id))
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
-- 4. EXACT TRIGGER DEFINITIONS, TIMING, EVENTS, AND ENABLED STATUS
-- ------------------------------------------------------------------------------
-- Expected: 
--   update_leaderboard_after_match on public.matches: AFTER INSERT OR UPDATE OR DELETE, FOR EACH ROW, enabled 'O'
--   sync_leaderboard_username on public.profiles: AFTER UPDATE, FOR EACH ROW, enabled 'O'
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
    pn.nspname AS function_schema,
    p.prosecdef AS is_security_definer,
    r.rolname AS function_owner,
    pg_get_triggerdef(tg.oid, true) AS full_trigger_definition
FROM pg_catalog.pg_trigger tg
JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
JOIN pg_catalog.pg_namespace pn ON pn.oid = p.pronamespace
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'public' 
  AND c.relname IN ('leaderboard', 'matches', 'profiles')
  AND NOT tg.tgisinternal
ORDER BY c.relname, tg.tgname;

-- ------------------------------------------------------------------------------
-- 5. FUNCTION SIGNATURES, SECURITY DEFINER POSTURE, SEARCH PATH, AND DEFINITIONS
-- ------------------------------------------------------------------------------
-- Expected: All 3 functions owned by 'postgres', prosecdef = true, search_path = ''
SELECT 
    p.proname AS function_name,
    n.nspname AS function_schema,
    pg_get_function_arguments(p.oid) AS argument_signature,
    pg_get_function_result(p.oid) AS return_type,
    r.rolname AS function_owner,
    p.prosecdef AS is_security_definer,
    p.proconfig AS runtime_configuration,
    pg_get_functiondef(p.oid) AS full_function_definition
FROM pg_catalog.pg_proc p
JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'public' 
  AND p.proname IN ('refresh_player_leaderboard', 'update_leaderboard_after_match', 'sync_leaderboard_username')
ORDER BY p.proname;

-- ------------------------------------------------------------------------------
-- 6. POLICY NAME COLLISION CHECK (MUST RETURN 0 ROWS BEFORE MIGRATION)
-- ------------------------------------------------------------------------------
SELECT schemaname, tablename, policyname
FROM pg_catalog.pg_policies
WHERE schemaname = 'public'
  AND policyname IN ('authenticated_insert_own_profile', 'anon_select_leaderboard');

-- ------------------------------------------------------------------------------
-- 7. AUTH.USERS TRIGGER INSPECTION (READ-ONLY SAFETY AUDIT)
-- Uses pg_trigger joined directly to pg_proc via tgfoid (not information_schema).
-- Confirms whether auth.users triggers handle initial profile creation.
-- ------------------------------------------------------------------------------
SELECT 
    tg.tgname AS trigger_name,
    c.relname AS table_name,
    n.nspname AS table_schema,
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
    p.proname AS called_function,
    pn.nspname AS function_schema,
    p.prosecdef AS is_security_definer,
    r.rolname AS function_owner,
    pg_get_triggerdef(tg.oid, true) AS full_trigger_definition
FROM pg_catalog.pg_trigger tg
JOIN pg_catalog.pg_class c ON c.oid = tg.tgrelid
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
JOIN pg_catalog.pg_proc p ON p.oid = tg.tgfoid
JOIN pg_catalog.pg_namespace pn ON pn.oid = p.pronamespace
JOIN pg_catalog.pg_roles r ON r.oid = p.proowner
WHERE n.nspname = 'auth' 
  AND c.relname = 'users'
  AND NOT tg.tgisinternal
ORDER BY tg.tgname;

