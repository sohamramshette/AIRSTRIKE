-- ==============================================================================
-- AIRSTRIKE Phase 12.2: Competitive 1v1 Online Multiplayer Database Schema
-- FILE: supabase/migrations/20261111_phase12_multiplayer_schema.sql
-- PURPOSE: Establishes rooms, matches, leaderboard, RLS, and secure RPCs
-- STATUS: DORMANT / PENDING HUMAN APPROVAL — ZERO DIRECT MUTATION WITHOUT APPROVAL
-- ==============================================================================

BEGIN;

-- ------------------------------------------------------------------------------
-- 1. EXTENSIONS & PREREQUISITES
-- ------------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------------------------
-- 2. CREATE TABLE: public.multiplayer_rooms
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.multiplayer_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_code VARCHAR(8) NOT NULL,
    host_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    guest_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    match_seed BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    host_ready BOOLEAN NOT NULL DEFAULT false,
    guest_ready BOOLEAN NOT NULL DEFAULT false,
    host_score INTEGER DEFAULT NULL,
    guest_score INTEGER DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    started_at TIMESTAMPTZ DEFAULT NULL,
    completed_at TIMESTAMPTZ DEFAULT NULL,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (timezone('utc'::text, now()) + interval '15 minutes'),
    CONSTRAINT uq_multiplayer_rooms_code UNIQUE (room_code),
    CONSTRAINT chk_multiplayer_rooms_distinct_players CHECK (guest_id IS NULL OR host_id <> guest_id),
    CONSTRAINT chk_multiplayer_rooms_status CHECK (
        status IN ('WAITING', 'READY', 'COUNTDOWN', 'ACTIVE', 'COMPLETED', 'CANCELLED')
    ),
    CONSTRAINT chk_multiplayer_rooms_host_score CHECK (host_score IS NULL OR host_score >= 0),
    CONSTRAINT chk_multiplayer_rooms_guest_score CHECK (guest_score IS NULL OR guest_score >= 0)
);

-- Indexes for multiplayer_rooms
CREATE INDEX IF NOT EXISTS idx_multiplayer_rooms_code ON public.multiplayer_rooms (room_code);
CREATE INDEX IF NOT EXISTS idx_multiplayer_rooms_host ON public.multiplayer_rooms (host_id);
CREATE INDEX IF NOT EXISTS idx_multiplayer_rooms_guest ON public.multiplayer_rooms (guest_id);
CREATE INDEX IF NOT EXISTS idx_multiplayer_rooms_status ON public.multiplayer_rooms (status);
CREATE INDEX IF NOT EXISTS idx_multiplayer_rooms_lookup ON public.multiplayer_rooms (status, expires_at);

-- ------------------------------------------------------------------------------
-- 3. CREATE TABLE: public.multiplayer_matches
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.multiplayer_matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL REFERENCES public.multiplayer_rooms(id) ON DELETE CASCADE,
    player1_id UUID NOT NULL REFERENCES public.profiles(id),
    player2_id UUID NOT NULL REFERENCES public.profiles(id),
    player1_score INTEGER NOT NULL DEFAULT 0,
    player2_score INTEGER NOT NULL DEFAULT 0,
    winner_id UUID REFERENCES public.profiles(id),
    duration_seconds INTEGER NOT NULL DEFAULT 90,
    match_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    completed_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_multiplayer_matches_room_id UNIQUE (room_id),
    CONSTRAINT chk_multiplayer_matches_distinct_players CHECK (player1_id <> player2_id),
    CONSTRAINT chk_multiplayer_matches_scores CHECK (player1_score >= 0 AND player2_score >= 0),
    CONSTRAINT chk_multiplayer_matches_duration CHECK (duration_seconds > 0),
    CONSTRAINT chk_multiplayer_matches_winner CHECK (
        winner_id IS NULL OR winner_id = player1_id OR winner_id = player2_id
    ),
    CONSTRAINT chk_multiplayer_matches_status CHECK (
        match_status IN ('COMPLETED', 'ABORTED', 'FORFEIT')
    )
);

-- Indexes for multiplayer_matches
CREATE INDEX IF NOT EXISTS idx_multiplayer_matches_room ON public.multiplayer_matches (room_id);
CREATE INDEX IF NOT EXISTS idx_multiplayer_matches_p1 ON public.multiplayer_matches (player1_id);
CREATE INDEX IF NOT EXISTS idx_multiplayer_matches_p2 ON public.multiplayer_matches (player2_id);
CREATE INDEX IF NOT EXISTS idx_multiplayer_matches_winner ON public.multiplayer_matches (winner_id);
CREATE INDEX IF NOT EXISTS idx_multiplayer_matches_completed ON public.multiplayer_matches (completed_at DESC);

-- ------------------------------------------------------------------------------
-- 4. CREATE TABLE: public.multiplayer_leaderboard
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.multiplayer_leaderboard (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    username TEXT NOT NULL,
    rating_points INTEGER NOT NULL DEFAULT 1000,
    wins INTEGER NOT NULL DEFAULT 0,
    losses INTEGER NOT NULL DEFAULT 0,
    draws INTEGER NOT NULL DEFAULT 0,
    total_matches INTEGER NOT NULL DEFAULT 0,
    best_score INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT chk_mp_leaderboard_rating CHECK (rating_points >= 0),
    CONSTRAINT chk_mp_leaderboard_counts CHECK (wins >= 0 AND losses >= 0 AND draws >= 0),
    CONSTRAINT chk_mp_leaderboard_total CHECK (total_matches = wins + losses + draws),
    CONSTRAINT chk_mp_leaderboard_best_score CHECK (best_score >= 0)
);

-- Indexes for multiplayer_leaderboard
CREATE INDEX IF NOT EXISTS idx_mp_leaderboard_rating ON public.multiplayer_leaderboard (rating_points DESC, wins DESC);
CREATE INDEX IF NOT EXISTS idx_mp_leaderboard_user ON public.multiplayer_leaderboard (user_id);

-- ------------------------------------------------------------------------------
-- 5. ROW-LEVEL SECURITY (RLS) POLICIES
-- ------------------------------------------------------------------------------
ALTER TABLE public.multiplayer_rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.multiplayer_matches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.multiplayer_leaderboard ENABLE ROW LEVEL SECURITY;

-- multiplayer_rooms: Authenticated users can view rooms they belong to or rooms waiting for a guest
DROP POLICY IF EXISTS "multiplayer_rooms_select" ON public.multiplayer_rooms;
CREATE POLICY "multiplayer_rooms_select" ON public.multiplayer_rooms
    FOR SELECT TO authenticated
    USING (auth.uid() = host_id OR auth.uid() = guest_id OR status = 'WAITING');

-- multiplayer_matches: Authenticated users can view matches they participated in
DROP POLICY IF EXISTS "multiplayer_matches_select" ON public.multiplayer_matches;
CREATE POLICY "multiplayer_matches_select" ON public.multiplayer_matches
    FOR SELECT TO authenticated
    USING (auth.uid() = player1_id OR auth.uid() = player2_id);

-- multiplayer_leaderboard: Public readable by authenticated users and anon cadets
DROP POLICY IF EXISTS "mp_leaderboard_select_auth" ON public.multiplayer_leaderboard;
CREATE POLICY "mp_leaderboard_select_auth" ON public.multiplayer_leaderboard
    FOR SELECT TO authenticated
    USING (true);

DROP POLICY IF EXISTS "mp_leaderboard_select_anon" ON public.multiplayer_leaderboard;
CREATE POLICY "mp_leaderboard_select_anon" ON public.multiplayer_leaderboard
    FOR SELECT TO anon
    USING (true);

-- Explicitly withhold direct client mutations (INSERT/UPDATE/DELETE) on all three tables
-- All state mutations MUST traverse the security-definer RPC functions below.
REVOKE INSERT, UPDATE, DELETE ON public.multiplayer_rooms FROM authenticated, anon, public;
REVOKE INSERT, UPDATE, DELETE ON public.multiplayer_matches FROM authenticated, anon, public;
REVOKE INSERT, UPDATE, DELETE ON public.multiplayer_leaderboard FROM authenticated, anon, public;

GRANT SELECT ON public.multiplayer_rooms TO authenticated;
GRANT SELECT ON public.multiplayer_matches TO authenticated;
GRANT SELECT ON public.multiplayer_leaderboard TO authenticated, anon;

-- ------------------------------------------------------------------------------
-- 6. HELPER FUNCTION: generate_room_code()
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.generate_multiplayer_room_code()
RETURNS VARCHAR(8)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_chars CONSTANT text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; -- 32 characters, no ambiguous 0/O, 1/I
    v_code VARCHAR(8);
    v_idx integer;
    v_exists boolean;
    v_attempts integer := 0;
BEGIN
    LOOP
        v_code := '';
        FOR v_idx IN 1..6 LOOP
            v_code := v_code || substr(v_chars, floor(random() * length(v_chars) + 1)::integer, 1);
        END LOOP;

        SELECT EXISTS (
            SELECT 1 FROM public.multiplayer_rooms
            WHERE room_code = v_code AND status NOT IN ('COMPLETED', 'CANCELLED')
        ) INTO v_exists;

        IF NOT v_exists THEN
            RETURN v_code;
        END IF;

        v_attempts := v_attempts + 1;
        IF v_attempts > 100 THEN
            RAISE EXCEPTION 'INTERNAL_ERROR: Unable to generate unique room code after 100 attempts.';
        END IF;
    END LOOP;
END;
$$;

REVOKE ALL ON FUNCTION public.generate_multiplayer_room_code() FROM public, anon;
GRANT EXECUTE ON FUNCTION public.generate_multiplayer_room_code() TO authenticated;

-- ------------------------------------------------------------------------------
-- 7. SECURE RPC 1: create_multiplayer_room()
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.create_multiplayer_room()
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_username TEXT;
    v_room_code VARCHAR(8);
    v_match_seed BIGINT;
    v_room_id UUID;
    v_created_at TIMESTAMPTZ;
    v_expires_at TIMESTAMPTZ;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required to create a multiplayer room.'
            USING ERRCODE = '42501';
    END IF;

    -- Verify profile exists and fetch username
    SELECT username INTO v_username
    FROM public.profiles
    WHERE id = v_user_id;

    IF v_username IS NULL THEN
        -- If profile record was delayed, use fallback derived from auth
        v_username := 'Cadet_' || substr(v_user_id::text, 1, 6);
        INSERT INTO public.profiles (id, username)
        VALUES (v_user_id, v_username)
        ON CONFLICT (id) DO NOTHING;
    END IF;

    -- Cancel any abandoned WAITING rooms created by this user that haven't expired
    UPDATE public.multiplayer_rooms
    SET status = 'CANCELLED'
    WHERE host_id = v_user_id
      AND status = 'WAITING'
      AND guest_id IS NULL;

    -- Generate cryptographically safe room code and seed
    v_room_code := public.generate_multiplayer_room_code();
    
    -- Generate non-zero 63-bit positive seed for PRNG
    v_match_seed := (abs(('x' || substr(md5(gen_random_uuid()::text), 1, 15))::bit(60)::bigint) % 9000000000000000000) + 100000;

    INSERT INTO public.multiplayer_rooms (
        room_code,
        host_id,
        match_seed,
        status,
        expires_at
    ) VALUES (
        v_room_code,
        v_user_id,
        v_match_seed,
        'WAITING',
        timezone('utc'::text, now()) + interval '15 minutes'
    )
    RETURNING id, created_at, expires_at INTO v_room_id, v_created_at, v_expires_at;

    RETURN jsonb_build_object(
        'success', true,
        'room_id', v_room_id,
        'room_code', v_room_code,
        'host_id', v_user_id,
        'host_username', v_username,
        'match_seed', v_match_seed,
        'status', 'WAITING',
        'created_at', v_created_at,
        'expires_at', v_expires_at
    );
END;
$$;

REVOKE ALL ON FUNCTION public.create_multiplayer_room() FROM public, anon;
GRANT EXECUTE ON FUNCTION public.create_multiplayer_room() TO authenticated;

-- ------------------------------------------------------------------------------
-- 8. SECURE RPC 2: join_multiplayer_room(p_room_code text)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.join_multiplayer_room(p_room_code text)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_guest_username TEXT;
    v_host_username TEXT;
    v_code VARCHAR(8);
    v_room public.multiplayer_rooms%ROWTYPE;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required to join a multiplayer room.'
            USING ERRCODE = '42501';
    END IF;

    IF p_room_code IS NULL OR trim(p_room_code) = '' THEN
        RAISE EXCEPTION 'INVALID_ARGUMENT: Room code must be specified.';
    END IF;

    v_code := upper(trim(p_room_code));

    -- Acquire row lock to prevent race conditions during join
    SELECT * INTO v_room
    FROM public.multiplayer_rooms
    WHERE room_code = v_code
    FOR UPDATE;

    IF v_room.id IS NULL THEN
        RAISE EXCEPTION 'ROOM_NOT_FOUND: No combat room matches code %', v_code;
    END IF;

    IF v_room.expires_at <= timezone('utc'::text, now()) THEN
        UPDATE public.multiplayer_rooms SET status = 'CANCELLED' WHERE id = v_room.id;
        RAISE EXCEPTION 'ROOM_EXPIRED: The room session has expired.';
    END IF;

    IF v_room.status <> 'WAITING' THEN
        RAISE EXCEPTION 'ROOM_UNAVAILABLE: Room is not accepting new pilots (current state: %).', v_room.status;
    END IF;

    IF v_room.host_id = v_user_id THEN
        RAISE EXCEPTION 'SELF_JOIN_FORBIDDEN: You cannot join your own room as a guest pilot.';
    END IF;

    IF v_room.guest_id IS NOT NULL THEN
        RAISE EXCEPTION 'ROOM_FULL: Room already has a guest pilot.';
    END IF;

    -- Fetch guest username
    SELECT username INTO v_guest_username
    FROM public.profiles
    WHERE id = v_user_id;

    IF v_guest_username IS NULL THEN
        v_guest_username := 'Cadet_' || substr(v_user_id::text, 1, 6);
        INSERT INTO public.profiles (id, username)
        VALUES (v_user_id, v_guest_username)
        ON CONFLICT (id) DO NOTHING;
    END IF;

    -- Fetch host username
    SELECT username INTO v_host_username
    FROM public.profiles
    WHERE id = v_room.host_id;

    -- Atomically assign guest and advance room state to READY
    UPDATE public.multiplayer_rooms
    SET guest_id = v_user_id,
        status = 'READY'
    WHERE id = v_room.id;

    RETURN jsonb_build_object(
        'success', true,
        'room_id', v_room.id,
        'room_code', v_room.room_code,
        'host_id', v_room.host_id,
        'host_username', v_host_username,
        'guest_id', v_user_id,
        'guest_username', v_guest_username,
        'match_seed', v_room.match_seed,
        'status', 'READY'
    );
END;
$$;

REVOKE ALL ON FUNCTION public.join_multiplayer_room(text) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.join_multiplayer_room(text) TO authenticated;

-- ------------------------------------------------------------------------------
-- 9. SECURE RPC 3: set_multiplayer_ready(p_room_id uuid)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.set_multiplayer_ready(p_room_id uuid)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_room public.multiplayer_rooms%ROWTYPE;
    v_new_status VARCHAR(20);
    v_host_ready BOOLEAN;
    v_guest_ready BOOLEAN;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required.' USING ERRCODE = '42501';
    END IF;

    SELECT * INTO v_room
    FROM public.multiplayer_rooms
    WHERE id = p_room_id
    FOR UPDATE;

    IF v_room.id IS NULL THEN
        RAISE EXCEPTION 'ROOM_NOT_FOUND: Specified room does not exist.';
    END IF;

    IF v_room.host_id <> v_user_id AND v_room.guest_id <> v_user_id THEN
        RAISE EXCEPTION 'FORBIDDEN: You are not an enrolled pilot in this combat room.';
    END IF;

    IF v_room.guest_id IS NULL THEN
        RAISE EXCEPTION 'INCOMPLETE_ROOM: Waiting for guest pilot before ready check can proceed.';
    END IF;

    IF v_room.status NOT IN ('READY', 'WAITING') THEN
        RAISE EXCEPTION 'INVALID_ACTION: Room cannot toggle ready in status %.', v_room.status;
    END IF;

    v_host_ready := v_room.host_ready;
    v_guest_ready := v_room.guest_ready;

    IF v_room.host_id = v_user_id THEN
        v_host_ready := true;
    ELSIF v_room.guest_id = v_user_id THEN
        v_guest_ready := true;
    END IF;

    v_new_status := v_room.status;
    IF v_host_ready AND v_guest_ready THEN
        v_new_status := 'COUNTDOWN';
    END IF;

    UPDATE public.multiplayer_rooms
    SET host_ready = v_host_ready,
        guest_ready = v_guest_ready,
        status = v_new_status
    WHERE id = p_room_id;

    RETURN jsonb_build_object(
        'success', true,
        'room_id', p_room_id,
        'host_ready', v_host_ready,
        'guest_ready', v_guest_ready,
        'status', v_new_status
    );
END;
$$;

REVOKE ALL ON FUNCTION public.set_multiplayer_ready(uuid) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.set_multiplayer_ready(uuid) TO authenticated;

-- ------------------------------------------------------------------------------
-- 10. SECURE RPC 4: start_multiplayer_match(p_room_id uuid)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.start_multiplayer_match(p_room_id uuid)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_room public.multiplayer_rooms%ROWTYPE;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required.' USING ERRCODE = '42501';
    END IF;

    SELECT * INTO v_room
    FROM public.multiplayer_rooms
    WHERE id = p_room_id
    FOR UPDATE;

    IF v_room.id IS NULL THEN
        RAISE EXCEPTION 'ROOM_NOT_FOUND: Specified room does not exist.';
    END IF;

    IF v_room.host_id <> v_user_id AND v_room.guest_id <> v_user_id THEN
        RAISE EXCEPTION 'FORBIDDEN: You are not an enrolled pilot in this combat room.';
    END IF;

    -- Idempotent return if already active
    IF v_room.status = 'ACTIVE' THEN
        RETURN jsonb_build_object(
            'success', true,
            'room_id', p_room_id,
            'status', 'ACTIVE',
            'started_at', v_room.started_at,
            'match_seed', v_room.match_seed
        );
    END IF;

    IF v_room.status NOT IN ('COUNTDOWN', 'READY') THEN
        RAISE EXCEPTION 'INVALID_ACTION: Match cannot be started from state %.', v_room.status;
    END IF;

    UPDATE public.multiplayer_rooms
    SET status = 'ACTIVE',
        started_at = timezone('utc'::text, now())
    WHERE id = p_room_id;

    RETURN jsonb_build_object(
        'success', true,
        'room_id', p_room_id,
        'status', 'ACTIVE',
        'started_at', timezone('utc'::text, now()),
        'match_seed', v_room.match_seed
    );
END;
$$;

REVOKE ALL ON FUNCTION public.start_multiplayer_match(uuid) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.start_multiplayer_match(uuid) TO authenticated;

-- ------------------------------------------------------------------------------
-- 11. SECURE RPC 5: finalize_multiplayer_match(p_room_id uuid, p_submitted_score int, p_duration_seconds int)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.finalize_multiplayer_match(
    p_room_id uuid,
    p_submitted_score int,
    p_duration_seconds int
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_room public.multiplayer_rooms%ROWTYPE;
    v_match public.multiplayer_matches%ROWTYPE;
    v_host_score INTEGER;
    v_guest_score INTEGER;
    v_winner_id UUID;
    v_host_name TEXT;
    v_guest_name TEXT;
    v_max_theoretical INTEGER;
    v_is_draw BOOLEAN := false;
    v_host_rating_delta INTEGER := 0;
    v_guest_rating_delta INTEGER := 0;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required to finalize match.'
            USING ERRCODE = '42501';
    END IF;

    -- Basic argument validation
    IF p_submitted_score < 0 THEN
        RAISE EXCEPTION 'INVALID_ARGUMENT: Submitted score cannot be negative.';
    END IF;

    IF p_duration_seconds <= 0 THEN
        RAISE EXCEPTION 'INVALID_ARGUMENT: Match duration must be greater than zero.';
    END IF;

    -- Anti-Cheat Baseline Sanity Check:
    -- Max theoretical score = 450 pts/sec (based on max wave density * 4x combo limit) + 2000 initial buffer
    v_max_theoretical := (p_duration_seconds * 450) + 2000;
    IF p_submitted_score > v_max_theoretical THEN
        RAISE EXCEPTION 'UNREALISTIC_SCORE_REJECTED: Submitted score (%) exceeds physical mathematical bounds for % seconds.',
            p_submitted_score, p_duration_seconds;
    END IF;

    -- Lock room row for update
    SELECT * INTO v_room
    FROM public.multiplayer_rooms
    WHERE id = p_room_id
    FOR UPDATE;

    IF v_room.id IS NULL THEN
        RAISE EXCEPTION 'ROOM_NOT_FOUND: Room does not exist.';
    END IF;

    IF v_room.host_id <> v_user_id AND v_room.guest_id <> v_user_id THEN
        RAISE EXCEPTION 'FORBIDDEN: You are not a participant in this room.';
    END IF;

    -- If room is already COMPLETED, check if match is already recorded
    IF v_room.status = 'COMPLETED' THEN
        SELECT * INTO v_match
        FROM public.multiplayer_matches
        WHERE room_id = p_room_id;

        IF v_match.id IS NOT NULL THEN
            RETURN jsonb_build_object(
                'success', true,
                'status', 'ALREADY_FINALIZED',
                'room_id', p_room_id,
                'match_id', v_match.id,
                'winner_id', v_match.winner_id,
                'player1_score', v_match.player1_score,
                'player2_score', v_match.player2_score,
                'completed_at', v_match.completed_at
            );
        END IF;
    END IF;

    IF v_room.status NOT IN ('ACTIVE', 'COUNTDOWN') THEN
        RAISE EXCEPTION 'INVALID_ROOM_STATE: Cannot finalize a match with room status %.', v_room.status;
    END IF;

    -- Record the caller's submitted score in the room record
    IF v_room.host_id = v_user_id THEN
        UPDATE public.multiplayer_rooms
        SET host_score = p_submitted_score
        WHERE id = p_room_id;
        v_host_score := p_submitted_score;
        v_guest_score := v_room.guest_score;
    ELSIF v_room.guest_id = v_user_id THEN
        UPDATE public.multiplayer_rooms
        SET guest_score = p_submitted_score
        WHERE id = p_room_id;
        v_guest_score := p_submitted_score;
        v_host_score := v_room.host_score;
    END IF;

    -- If the other player has NOT submitted their score yet, return pending status
    IF v_host_score IS NULL OR v_guest_score IS NULL THEN
        RETURN jsonb_build_object(
            'success', true,
            'status', 'PENDING_OPPONENT',
            'room_id', p_room_id,
            'submitted_score', p_submitted_score,
            'message', 'Telemetry recorded. Waiting for opponent final match telemetry.'
        );
    END IF;

    -- =========================================================================
    -- BOTH SCORES SUBMITTED: ATOMIC WINNER DETERMINATION & RATING DISTRIBUTION
    -- =========================================================================

    IF v_host_score > v_guest_score THEN
        v_winner_id := v_room.host_id;
        v_is_draw := false;
        v_host_rating_delta := 25;
        v_guest_rating_delta := 0;
    ELSIF v_guest_score > v_host_score THEN
        v_winner_id := v_room.guest_id;
        v_is_draw := false;
        v_host_rating_delta := 0;
        v_guest_rating_delta := 25;
    ELSE
        v_winner_id := NULL; -- Draw
        v_is_draw := true;
        v_host_rating_delta := 10;
        v_guest_rating_delta := 10;
    END IF;

    -- Record completed match
    INSERT INTO public.multiplayer_matches (
        room_id,
        player1_id,
        player2_id,
        player1_score,
        player2_score,
        winner_id,
        duration_seconds,
        match_status,
        completed_at
    ) VALUES (
        p_room_id,
        v_room.host_id,
        v_room.guest_id,
        v_host_score,
        v_guest_score,
        v_winner_id,
        p_duration_seconds,
        'COMPLETED',
        timezone('utc'::text, now())
    )
    ON CONFLICT (room_id) DO NOTHING
    RETURNING * INTO v_match;

    -- Transition room to COMPLETED
    UPDATE public.multiplayer_rooms
    SET status = 'COMPLETED',
        completed_at = timezone('utc'::text, now())
    WHERE id = p_room_id;

    -- Fetch usernames for leaderboard records
    SELECT username INTO v_host_name FROM public.profiles WHERE id = v_room.host_id;
    SELECT username INTO v_guest_name FROM public.profiles WHERE id = v_room.guest_id;
    IF v_host_name IS NULL THEN v_host_name := 'Pilot_' || substr(v_room.host_id::text, 1, 6); END IF;
    IF v_guest_name IS NULL THEN v_guest_name := 'Pilot_' || substr(v_room.guest_id::text, 1, 6); END IF;

    -- Ensure host leaderboard entry exists
    INSERT INTO public.multiplayer_leaderboard (user_id, username, rating_points, wins, losses, draws, total_matches, best_score)
    VALUES (v_room.host_id, v_host_name, 1000, 0, 0, 0, 0, 0)
    ON CONFLICT (user_id) DO UPDATE SET username = EXCLUDED.username;

    -- Ensure guest leaderboard entry exists
    INSERT INTO public.multiplayer_leaderboard (user_id, username, rating_points, wins, losses, draws, total_matches, best_score)
    VALUES (v_room.guest_id, v_guest_name, 1000, 0, 0, 0, 0, 0)
    ON CONFLICT (user_id) DO UPDATE SET username = EXCLUDED.username;

    -- Update Host Leaderboard Record
    UPDATE public.multiplayer_leaderboard
    SET rating_points = rating_points + v_host_rating_delta,
        wins = wins + (CASE WHEN v_winner_id = v_room.host_id THEN 1 ELSE 0 END),
        losses = losses + (CASE WHEN v_winner_id = v_room.guest_id THEN 1 ELSE 0 END),
        draws = draws + (CASE WHEN v_is_draw THEN 1 ELSE 0 END),
        total_matches = total_matches + 1,
        best_score = GREATEST(best_score, v_host_score),
        updated_at = timezone('utc'::text, now())
    WHERE user_id = v_room.host_id;

    -- Update Guest Leaderboard Record
    UPDATE public.multiplayer_leaderboard
    SET rating_points = rating_points + v_guest_rating_delta,
        wins = wins + (CASE WHEN v_winner_id = v_room.guest_id THEN 1 ELSE 0 END),
        losses = losses + (CASE WHEN v_winner_id = v_room.host_id THEN 1 ELSE 0 END),
        draws = draws + (CASE WHEN v_is_draw THEN 1 ELSE 0 END),
        total_matches = total_matches + 1,
        best_score = GREATEST(best_score, v_guest_score),
        updated_at = timezone('utc'::text, now())
    WHERE user_id = v_room.guest_id;

    RETURN jsonb_build_object(
        'success', true,
        'status', 'RESOLVED',
        'room_id', p_room_id,
        'winner_id', v_winner_id,
        'is_draw', v_is_draw,
        'host_id', v_room.host_id,
        'host_score', v_host_score,
        'host_rating_delta', v_host_rating_delta,
        'guest_id', v_room.guest_id,
        'guest_score', v_guest_score,
        'guest_rating_delta', v_guest_rating_delta,
        'duration_seconds', p_duration_seconds
    );
END;
$$;

REVOKE ALL ON FUNCTION public.finalize_multiplayer_match(uuid, int, int) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.finalize_multiplayer_match(uuid, int, int) TO authenticated;

COMMIT;

