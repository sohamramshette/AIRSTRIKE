-- Keep multiplayer timestamp writes and elapsed-time validation in UTC.
-- start_multiplayer_match returns timezone('utc', now()) as a timestamp without
-- time zone, so its function session timezone must also be UTC when assigning
-- that value to started_at (TIMESTAMPTZ) and when comparing timestamps.

BEGIN;

ALTER FUNCTION public.start_multiplayer_match(uuid) SET timezone = 'UTC';
ALTER FUNCTION public.finalize_multiplayer_match(uuid, int, int) SET timezone = 'UTC';

COMMIT;
