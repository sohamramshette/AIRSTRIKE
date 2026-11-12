-- ==============================================================================
-- AIRSTRIKE Phase 12 (Part 7): Final Multiplayer Security, Settlement & Leaderboard
-- FILE: supabase/migrations/20261112_phase12_part7_settlement_and_leaderboard.sql
-- PURPOSE: 
--   1. Hardens finalize_multiplayer_match with server timestamp elapsed validation.
--   2. Explicitly tags peer-reported match results as PROVISIONAL (unverified peer telemetry).
--   3. Freezes official verified rating points (+0) while maintaining provisional MMR.
--   4. Adds forfeit_multiplayer_match RPC for abandonment/disconnect resolution.
--   5. Creates public.v_multiplayer_leaderboard with win rates and provisional ranking.
--   6. Creates get_multiplayer_leaderboard() and get_player_multiplayer_history() RPCs.
-- STATUS: DORMANT / PENDING HUMAN APPROVAL — ZERO DIRECT MUTATION WITHOUT APPROVAL
-- ==============================================================================

BEGIN;

-- ------------------------------------------------------------------------------
-- 1. EXTEND SCHEMA: PROVISIONAL AUDIT & RANKING COLUMNS
-- ------------------------------------------------------------------------------

-- Add provisional verification audit columns to multiplayer_matches
ALTER TABLE public.multiplayer_matches
    ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) NOT NULL DEFAULT 'PROVISIONAL',
    ADD COLUMN IF NOT EXISTS verification_notes TEXT DEFAULT 'Peer-reported client telemetry; serverless architecture without authoritative physics simulation.';

-- Add provisional ranking columns to multiplayer_leaderboard
ALTER TABLE public.multiplayer_leaderboard
    ADD COLUMN IF NOT EXISTS provisional_rating INTEGER NOT NULL DEFAULT 1000,
    ADD COLUMN IF NOT EXISTS provisional_wins INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS provisional_losses INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS provisional_draws INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS provisional_matches INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS best_verified_score INTEGER NOT NULL DEFAULT 0;

-- ------------------------------------------------------------------------------
-- 2. HARDENED RPC: finalize_multiplayer_match()
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
    v_elapsed_seconds NUMERIC;
    v_host_prov_delta INTEGER := 0;
    v_guest_prov_delta INTEGER := 0;
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

    -- Lock room row for atomic update
    SELECT * INTO v_room
    FROM public.multiplayer_rooms
    WHERE id = p_room_id
    FOR UPDATE;

    IF v_room.id IS NULL THEN
        RAISE EXCEPTION 'ROOM_NOT_FOUND: Room does not exist.';
    END IF;

    IF v_room.host_id <> v_user_id AND (v_room.guest_id IS NULL OR v_room.guest_id <> v_user_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: You are not an enrolled pilot in this room.'
            USING ERRCODE = '42501';
    END IF;

    -- If room is already COMPLETED, return idempotent result without duplicate reward
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
                'is_verified', v_match.is_verified,
                'verification_status', v_match.verification_status,
                'player1_score', v_match.player1_score,
                'player2_score', v_match.player2_score,
                'completed_at', v_match.completed_at
            );
        END IF;
    END IF;

    IF v_room.status <> 'ACTIVE' THEN
        RAISE EXCEPTION 'INVALID_ROOM_STATE: Cannot finalize a match when room status is %.', v_room.status;
    END IF;

    -- Server Timestamp Verification: Ensure match has actually run
    IF v_room.started_at IS NOT NULL THEN
        v_elapsed_seconds := EXTRACT(EPOCH FROM (timezone('utc'::text, now()) - v_room.started_at));
        -- If player claims 90s full duration, wall-clock must have elapsed at least 80s (accounting for slight clock skew)
        IF p_duration_seconds >= 80 AND v_elapsed_seconds < 75 THEN
            RAISE EXCEPTION 'PREMATURE_FINALIZATION_REJECTED: Wall-clock elapsed time (%) is less than claimed match duration (%).',
                ROUND(v_elapsed_seconds), p_duration_seconds;
        END IF;
    END IF;

    -- Upper Sanity Bound Check: Max theoretical physics bound
    v_max_theoretical := (p_duration_seconds * 450) + 2000;
    IF p_submitted_score > v_max_theoretical THEN
        RAISE EXCEPTION 'UNREALISTIC_SCORE_REJECTED: Submitted score (%) exceeds physical mathematical bounds for % seconds.',
            p_submitted_score, p_duration_seconds;
    END IF;

    -- Record the caller's score in the room record
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

    -- If opponent has not submitted yet, return PENDING_OPPONENT
    IF v_host_score IS NULL OR v_guest_score IS NULL THEN
        RETURN jsonb_build_object(
            'success', true,
            'status', 'PENDING_OPPONENT',
            'room_id', p_room_id,
            'submitted_score', p_submitted_score,
            'is_verified', false,
            'verification_status', 'PROVISIONAL',
            'message', 'Telemetry recorded. Waiting for challenger match telemetry.'
        );
    END IF;

    -- =========================================================================
    -- BOTH SCORES SUBMITTED: ATOMIC WINNER DETERMINATION & PROVISIONAL SETTLEMENT
    -- =========================================================================

    IF v_host_score > v_guest_score THEN
        v_winner_id := v_room.host_id;
        v_is_draw := false;
        v_host_prov_delta := 25;
        v_guest_prov_delta := 0;
    ELSIF v_guest_score > v_host_score THEN
        v_winner_id := v_room.guest_id;
        v_is_draw := false;
        v_host_prov_delta := 0;
        v_guest_prov_delta := 25;
    ELSE
        v_winner_id := NULL; -- Draw
        v_is_draw := true;
        v_host_prov_delta := 10;
        v_guest_prov_delta := 10;
    END IF;

    -- Insert completed match record tagged as PROVISIONAL
    INSERT INTO public.multiplayer_matches (
        room_id,
        player1_id,
        player2_id,
        player1_score,
        player2_score,
        winner_id,
        duration_seconds,
        match_status,
        is_verified,
        verification_status,
        verification_notes,
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
        false, -- Strictly unverified client submission
        'PROVISIONAL',
        'Peer-reported client telemetry; verified rankings frozen pending authoritative simulation.',
        timezone('utc'::text, now())
    )
    ON CONFLICT (room_id) DO NOTHING
    RETURNING * INTO v_match;

    -- Mark room COMPLETED
    UPDATE public.multiplayer_rooms
    SET status = 'COMPLETED',
        completed_at = timezone('utc'::text, now())
    WHERE id = p_room_id;

    -- Fetch pilot usernames
    SELECT username INTO v_host_name FROM public.profiles WHERE id = v_room.host_id;
    SELECT username INTO v_guest_name FROM public.profiles WHERE id = v_room.guest_id;
    IF v_host_name IS NULL THEN v_host_name := 'Pilot_' || substr(v_room.host_id::text, 1, 6); END IF;
    IF v_guest_name IS NULL THEN v_guest_name := 'Pilot_' || substr(v_room.guest_id::text, 1, 6); END IF;

    -- Ensure host leaderboard entry exists
    INSERT INTO public.multiplayer_leaderboard (user_id, username, rating_points, wins, losses, draws, total_matches, best_score, provisional_rating)
    VALUES (v_room.host_id, v_host_name, 1000, 0, 0, 0, 0, 0, 1000)
    ON CONFLICT (user_id) DO UPDATE SET username = EXCLUDED.username;

    -- Ensure guest leaderboard entry exists
    INSERT INTO public.multiplayer_leaderboard (user_id, username, rating_points, wins, losses, draws, total_matches, best_score, provisional_rating)
    VALUES (v_room.guest_id, v_guest_name, 1000, 0, 0, 0, 0, 0, 1000)
    ON CONFLICT (user_id) DO UPDATE SET username = EXCLUDED.username;

    -- Update Host: Verified rating points receives +0 (FROZEN), Provisional rating receives delta
    UPDATE public.multiplayer_leaderboard
    SET provisional_rating = provisional_rating + v_host_prov_delta,
        provisional_wins = provisional_wins + (CASE WHEN v_winner_id = v_room.host_id THEN 1 ELSE 0 END),
        provisional_losses = provisional_losses + (CASE WHEN v_winner_id = v_room.guest_id THEN 1 ELSE 0 END),
        provisional_draws = provisional_draws + (CASE WHEN v_is_draw THEN 1 ELSE 0 END),
        provisional_matches = provisional_matches + 1,
        best_score = GREATEST(best_score, v_host_score),
        updated_at = timezone('utc'::text, now())
    WHERE user_id = v_room.host_id;

    -- Update Guest: Verified rating points receives +0 (FROZEN), Provisional rating receives delta
    UPDATE public.multiplayer_leaderboard
    SET provisional_rating = provisional_rating + v_guest_prov_delta,
        provisional_wins = provisional_wins + (CASE WHEN v_winner_id = v_room.guest_id THEN 1 ELSE 0 END),
        provisional_losses = provisional_losses + (CASE WHEN v_winner_id = v_room.host_id THEN 1 ELSE 0 END),
        provisional_draws = provisional_draws + (CASE WHEN v_is_draw THEN 1 ELSE 0 END),
        provisional_matches = provisional_matches + 1,
        best_score = GREATEST(best_score, v_guest_score),
        updated_at = timezone('utc'::text, now())
    WHERE user_id = v_room.guest_id;

    RETURN jsonb_build_object(
        'success', true,
        'status', 'RESOLVED',
        'room_id', p_room_id,
        'match_id', v_match.id,
        'winner_id', v_winner_id,
        'is_draw', v_is_draw,
        'is_verified', false,
        'verification_status', 'PROVISIONAL',
        'host_id', v_room.host_id,
        'host_score', v_host_score,
        'host_prov_delta', v_host_prov_delta,
        'host_verified_delta', 0,
        'guest_id', v_room.guest_id,
        'guest_score', v_guest_score,
        'guest_prov_delta', v_guest_prov_delta,
        'guest_verified_delta', 0,
        'duration_seconds', p_duration_seconds
    );
END;
$$;

REVOKE ALL ON FUNCTION public.finalize_multiplayer_match(uuid, int, int) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.finalize_multiplayer_match(uuid, int, int) TO authenticated;

-- ------------------------------------------------------------------------------
-- 3. SECURE RPC: forfeit_multiplayer_match(p_room_id uuid)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.forfeit_multiplayer_match(p_room_id uuid)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id UUID;
    v_room public.multiplayer_rooms%ROWTYPE;
    v_winner_id UUID;
    v_loser_id UUID;
    v_match public.multiplayer_matches%ROWTYPE;
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
        RAISE EXCEPTION 'ROOM_NOT_FOUND: Room does not exist.';
    END IF;

    IF v_room.host_id <> v_user_id AND (v_room.guest_id IS NULL OR v_room.guest_id <> v_user_id) THEN
        RAISE EXCEPTION 'FORBIDDEN: Not a participant in this room.' USING ERRCODE = '42501';
    END IF;

    IF v_room.status = 'COMPLETED' THEN
        RETURN jsonb_build_object('success', true, 'status', 'ALREADY_COMPLETED');
    END IF;

    -- The forfeiting caller concedes the match to their opponent
    IF v_room.host_id = v_user_id THEN
        v_loser_id := v_room.host_id;
        v_winner_id := v_room.guest_id;
    ELSE
        v_loser_id := v_room.guest_id;
        v_winner_id := v_room.host_id;
    END IF;

    -- Update room to COMPLETED
    UPDATE public.multiplayer_rooms
    SET status = 'COMPLETED',
        completed_at = timezone('utc'::text, now())
    WHERE id = p_room_id;

    -- Insert forfeit match record
    IF v_winner_id IS NOT NULL THEN
        INSERT INTO public.multiplayer_matches (
            room_id, player1_id, player2_id, player1_score, player2_score, winner_id,
            duration_seconds, match_status, is_verified, verification_status, verification_notes, completed_at
        ) VALUES (
            p_room_id, v_room.host_id, v_room.guest_id,
            COALESCE(v_room.host_score, 0), COALESCE(v_room.guest_score, 0),
            v_winner_id, 90, 'FORFEIT', false, 'PROVISIONAL', 'Match concluded via pilot forfeit / concession.',
            timezone('utc'::text, now())
        )
        ON CONFLICT (room_id) DO NOTHING
        RETURNING * INTO v_match;
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'status', 'FORFEIT_RESOLVED',
        'room_id', p_room_id,
        'winner_id', v_winner_id,
        'loser_id', v_loser_id
    );
END;
$$;

REVOKE ALL ON FUNCTION public.forfeit_multiplayer_match(uuid) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.forfeit_multiplayer_match(uuid) TO authenticated;

-- ------------------------------------------------------------------------------
-- 4. VIEW: public.v_multiplayer_leaderboard
-- ------------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.v_multiplayer_leaderboard AS
SELECT 
    ROW_NUMBER() OVER (ORDER BY provisional_rating DESC, provisional_wins DESC, best_score DESC) AS rank,
    user_id,
    username,
    rating_points AS verified_rating,
    wins AS verified_wins,
    losses AS verified_losses,
    draws AS verified_draws,
    total_matches AS verified_matches,
    best_verified_score,
    ROUND((wins::numeric / NULLIF(total_matches, 0)) * 100, 1) AS verified_win_rate,
    provisional_rating,
    provisional_wins,
    provisional_losses,
    provisional_draws,
    provisional_matches,
    ROUND((provisional_wins::numeric / NULLIF(provisional_matches, 0)) * 100, 1) AS provisional_win_rate,
    best_score,
    updated_at
FROM public.multiplayer_leaderboard;

GRANT SELECT ON public.v_multiplayer_leaderboard TO authenticated, anon;

-- ------------------------------------------------------------------------------
-- 5. SECURE RPC: get_multiplayer_leaderboard(p_mode text, p_limit int)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.get_multiplayer_leaderboard(
    p_mode text DEFAULT 'provisional',
    p_limit int DEFAULT 50
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_result jsonb;
    v_clamped_limit integer;
BEGIN
    v_clamped_limit := LEAST(GREATEST(p_limit, 1), 100);

    IF p_mode = 'verified' THEN
        SELECT jsonb_agg(sub) INTO v_result
        FROM (
            SELECT 
                ROW_NUMBER() OVER (ORDER BY rating_points DESC, wins DESC, best_verified_score DESC) AS rank,
                user_id,
                username,
                rating_points,
                wins,
                losses,
                draws,
                total_matches,
                ROUND((wins::numeric / NULLIF(total_matches, 0)) * 100, 1) AS win_rate,
                best_verified_score AS best_score,
                'VERIFIED' AS ranking_tier
            FROM public.multiplayer_leaderboard
            WHERE total_matches > 0
            ORDER BY rating_points DESC, wins DESC
            LIMIT v_clamped_limit
        ) sub;
    ELSE
        -- Default to provisional rankings
        SELECT jsonb_agg(sub) INTO v_result
        FROM (
            SELECT 
                ROW_NUMBER() OVER (ORDER BY provisional_rating DESC, provisional_wins DESC, best_score DESC) AS rank,
                user_id,
                username,
                provisional_rating AS rating_points,
                provisional_wins AS wins,
                provisional_losses AS losses,
                provisional_draws AS draws,
                provisional_matches AS total_matches,
                ROUND((provisional_wins::numeric / NULLIF(provisional_matches, 0)) * 100, 1) AS win_rate,
                best_score,
                'PROVISIONAL' AS ranking_tier
            FROM public.multiplayer_leaderboard
            WHERE provisional_matches > 0
            ORDER BY provisional_rating DESC, provisional_wins DESC, best_score DESC
            LIMIT v_clamped_limit
        ) sub;
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'mode', p_mode,
        'leaderboard', COALESCE(v_result, '[]'::jsonb)
    );
END;
$$;

REVOKE ALL ON FUNCTION public.get_multiplayer_leaderboard(text, int) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.get_multiplayer_leaderboard(text, int) TO authenticated, anon;

-- ------------------------------------------------------------------------------
-- 6. SECURE RPC: get_player_multiplayer_history(p_user_id uuid, p_limit int)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.get_player_multiplayer_history(
    p_user_id uuid DEFAULT NULL,
    p_limit int DEFAULT 20
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_target_id UUID;
    v_clamped_limit integer;
    v_result jsonb;
BEGIN
    v_target_id := COALESCE(p_user_id, auth.uid());
    IF v_target_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: User ID required.' USING ERRCODE = '42501';
    END IF;

    v_clamped_limit := LEAST(GREATEST(p_limit, 1), 50);

    SELECT jsonb_agg(sub) INTO v_result
    FROM (
        SELECT 
            m.id AS match_id,
            m.room_id,
            m.duration_seconds,
            m.match_status,
            m.is_verified,
            m.verification_status,
            m.completed_at,
            CASE WHEN m.player1_id = v_target_id THEN m.player1_score ELSE m.player2_score END AS player_score,
            CASE WHEN m.player1_id = v_target_id THEN m.player2_score ELSE m.player1_score END AS opponent_score,
            CASE 
                WHEN m.winner_id = v_target_id THEN 'VICTORY'
                WHEN m.winner_id IS NULL THEN 'DRAW'
                ELSE 'DEFEAT'
            END AS result,
            p.username AS opponent_callsign
        FROM public.multiplayer_matches m
        JOIN public.profiles p ON p.id = (CASE WHEN m.player1_id = v_target_id THEN m.player2_id ELSE m.player1_id END)
        WHERE m.player1_id = v_target_id OR m.player2_id = v_target_id
        ORDER BY m.completed_at DESC
        LIMIT v_clamped_limit
    ) sub;

    RETURN jsonb_build_object(
        'success', true,
        'user_id', v_target_id,
        'history', COALESCE(v_result, '[]'::jsonb)
    );
END;
$$;

REVOKE ALL ON FUNCTION public.get_player_multiplayer_history(uuid, int) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.get_player_multiplayer_history(uuid, int) TO authenticated, anon;

COMMIT;
