-- ==============================================================================
-- AIRSTRIKE Phase 12 (Part 6): Rollback Migration for Multiplayer Combat Sync
-- FILE: supabase/migrations/20261112_phase12_part6_gameplay_sync_rollback.sql
-- PURPOSE: Restores get_multiplayer_room() to Phase 12 Part 3 signature
-- STATUS: DORMANT / PENDING HUMAN APPROVAL — ZERO DIRECT MUTATION WITHOUT APPROVAL
-- ==============================================================================

BEGIN;

CREATE OR REPLACE FUNCTION public.get_multiplayer_room(p_room_id uuid)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_room public.multiplayer_rooms%ROWTYPE;
    v_host_name TEXT;
    v_guest_name TEXT;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required.' USING ERRCODE = '42501';
    END IF;

    SELECT * INTO v_room
    FROM public.multiplayer_rooms
    WHERE id = p_room_id;

    IF v_room.id IS NULL THEN
        RAISE EXCEPTION 'ROOM_NOT_FOUND: Specified room does not exist.';
    END IF;

    IF v_room.host_id <> v_user_id AND (v_room.guest_id IS NULL OR v_room.guest_id <> v_user_id) AND v_room.status <> 'WAITING' THEN
        RAISE EXCEPTION 'FORBIDDEN: You do not have access to this room.' USING ERRCODE = '42501';
    END IF;

    SELECT username INTO v_host_name FROM public.profiles WHERE id = v_room.host_id;
    IF v_host_name IS NULL THEN v_host_name := 'Pilot_' || substr(v_room.host_id::text, 1, 6); END IF;

    IF v_room.guest_id IS NOT NULL THEN
        SELECT username INTO v_guest_name FROM public.profiles WHERE id = v_room.guest_id;
        IF v_guest_name IS NULL THEN v_guest_name := 'Pilot_' || substr(v_room.guest_id::text, 1, 6); END IF;
    ELSE
        v_guest_name := NULL;
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'room_id', v_room.id,
        'room_code', v_room.room_code,
        'host_id', v_room.host_id,
        'host_username', v_host_name,
        'host_ready', v_room.host_ready,
        'guest_id', v_room.guest_id,
        'guest_username', v_guest_name,
        'guest_ready', v_room.guest_ready,
        'match_seed', v_room.match_seed,
        'status', v_room.status,
        'is_host', (v_room.host_id = v_user_id),
        'is_guest', (v_room.guest_id = v_user_id),
        'created_at', v_room.created_at,
        'expires_at', v_room.expires_at
    );
END;
$$;

REVOKE ALL ON FUNCTION public.get_multiplayer_room(uuid) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.get_multiplayer_room(uuid) TO authenticated;

COMMIT;
