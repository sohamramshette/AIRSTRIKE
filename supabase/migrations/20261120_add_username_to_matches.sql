-- ==============================================================================
-- AIRSTRIKE: Add Player Name (username) to public.matches
-- FILE: supabase/migrations/20261120_add_username_to_matches.sql
-- PURPOSE:
--   1. Adds 'username' column to public.matches table.
--   2. Backfills existing match rows with username from public.profiles.
--   3. Adds BEFORE INSERT trigger to auto-populate username on new matches.
--   4. Adds AFTER UPDATE trigger on public.profiles to sync name changes.
--   5. Creates index on public.matches(username) for fast filtering.
-- ==============================================================================

-- 1. Add username column to public.matches
ALTER TABLE public.matches 
ADD COLUMN IF NOT EXISTS username TEXT;

-- 2. Backfill existing matches with player callsigns/usernames from public.profiles
UPDATE public.matches m
SET username = COALESCE(p.username, 'Cadet_' || SUBSTR(m.user_id::text, 1, 6))
FROM public.profiles p
WHERE m.user_id = p.id
  AND (m.username IS NULL OR m.username = '');

-- 2b. Fallback for any match rows where a profile record doesn't exist
UPDATE public.matches
SET username = 'Cadet_' || SUBSTR(user_id::text, 1, 6)
WHERE username IS NULL OR username = '';

-- 3. Create Trigger Function to auto-populate username on new match insertions
CREATE OR REPLACE FUNCTION public.set_match_username()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
BEGIN
    -- If username was not provided in the insert payload, lookup from public.profiles
    IF NEW.username IS NULL OR NEW.username = '' THEN
        SELECT username INTO NEW.username
        FROM public.profiles
        WHERE id = NEW.user_id;

        -- Fallback if profile row is not yet created
        IF NEW.username IS NULL THEN
            NEW.username := 'Cadet_' || SUBSTR(NEW.user_id::text, 1, 6);
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

-- 4. Attach BEFORE INSERT trigger on public.matches
DROP TRIGGER IF EXISTS trg_set_match_username ON public.matches;
CREATE TRIGGER trg_set_match_username
BEFORE INSERT ON public.matches
FOR EACH ROW
EXECUTE FUNCTION public.set_match_username();

-- 5. Trigger Function to sync matches username when a player changes callsign in profiles
CREATE OR REPLACE FUNCTION public.sync_matches_username()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
BEGIN
    IF NEW.username IS DISTINCT FROM OLD.username THEN
        UPDATE public.matches
        SET username = NEW.username
        WHERE user_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_matches_username ON public.profiles;
CREATE TRIGGER trg_sync_matches_username
AFTER UPDATE ON public.profiles
FOR EACH ROW
EXECUTE FUNCTION public.sync_matches_username();

-- 6. Index for fast search & filtering in Supabase Table Editor
CREATE INDEX IF NOT EXISTS idx_matches_username ON public.matches (username);
