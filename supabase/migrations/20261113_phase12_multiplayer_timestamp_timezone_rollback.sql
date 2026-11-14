BEGIN;

ALTER FUNCTION public.start_multiplayer_match(uuid) RESET timezone;
ALTER FUNCTION public.finalize_multiplayer_match(uuid, int, int) RESET timezone;

COMMIT;
