-- ==============================================================================
-- AIRSTRIKE Phase 12.3: Multiplayer Forward Corrections & Lobby RPC Extensions
-- FILE: supabase/migrations/20261111_phase12_multiplayer_corrections.sql
-- PURPOSE: Adds leave_multiplayer_room() and get_multiplayer_room() RPCs
-- STATUS: DORMANT / PENDING HUMAN APPROVAL — ZERO DIRECT MUTATION WITHOUT APPROVAL
-- ==============================================================================

BEGIN;

-- ------------------------------------------------------------------------------
-- 1. SECURE RPC: leave_multiplayer_room(p_room_id uuid)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.leave_multiplayer_room(p_room_id uuid)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_room public.multiplayer_rooms%ROWTYPE;
    v_new_status VARCHAR(20);
    v_role VARCHAR(10);
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required to leave a room.'
            USING ERRCODE = '42501';
    END IF;

    SELECT * INTO v_room
    FROM public.multiplayer_rooms
    WHERE id = p_room_id
    FOR UPDATE;

    IF v_room.id IS NULL THEN
        RAISE EXCEPTION 'ROOM_NOT_FOUND: Specified room does not exist.';
    END IF;

    IF v_room.host_id <> v_user_id AND (v_room.guest_id IS NULL OR v_room.guest_id <> v_user_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: You are not an enrolled pilot in this room.';
    END IF;

    -- If match is already completed or cancelled, return idempotent status
    IF v_room.status IN ('COMPLETED', 'CANCELLED') THEN
        RETURN jsonb_build_object(
            'success', true,
            'room_id', p_room_id,
            'status', v_room.status,
            'message', 'Room is already inactive.'
        );
    END IF;

    IF v_room.host_id = v_user_id THEN
        v_role := 'HOST';
        -- Host leaving cancels the room entirely
        v_new_status := 'CANCELLED';
        UPDATE public.multiplayer_rooms
        SET status = 'CANCELLED',
            completed_at = timezone('utc'::text, now())
        WHERE id = p_room_id;
    ELSIF v_room.guest_id = v_user_id THEN
        v_role := 'GUEST';
        -- Guest leaving reverts room back to WAITING so another guest can join
        IF v_room.status IN ('READY', 'COUNTDOWN') THEN
            v_new_status := 'WAITING';
            UPDATE public.multiplayer_rooms
            SET guest_id = NULL,
                guest_ready = false,
                host_ready = false,
                status = 'WAITING'
            WHERE id = p_room_id;
        ELSE
            -- If active or other state, mark cancelled
            v_new_status := 'CANCELLED';
            UPDATE public.multiplayer_rooms
            SET status = 'CANCELLED',
                completed_at = timezone('utc'::text, now())
            WHERE id = p_room_id;
        END IF;
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'room_id', p_room_id,
        'role', v_role,
        'status', v_new_status
    );
END;
$$;

REVOKE ALL ON FUNCTION public.leave_multiplayer_room(uuid) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.leave_multiplayer_room(uuid) TO authenticated;

-- ------------------------------------------------------------------------------
-- 2. SECURE RPC: get_multiplayer_room(p_room_id uuid)
-- ------------------------------------------------------------------------------
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

    -- Security: only participants or open waiting rooms can be inspected
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
