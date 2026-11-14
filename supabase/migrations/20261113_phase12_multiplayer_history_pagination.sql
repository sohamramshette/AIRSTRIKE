BEGIN;

CREATE OR REPLACE FUNCTION public.get_my_multiplayer_match_history(
    p_limit integer DEFAULT 50,
    p_offset integer DEFAULT 0
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_user_id uuid;
    v_limit integer;
    v_offset integer;
    v_history jsonb;
    v_has_more boolean;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'UNAUTHORIZED: Authentication required.'
            USING ERRCODE = '42501';
    END IF;

    v_limit := LEAST(GREATEST(COALESCE(p_limit, 50), 1), 100);
    v_offset := GREATEST(COALESCE(p_offset, 0), 0);

    SELECT EXISTS (
        SELECT 1
        FROM public.multiplayer_matches m
        WHERE m.player1_id = v_user_id OR m.player2_id = v_user_id
        ORDER BY m.completed_at DESC, m.id DESC
        OFFSET v_offset + v_limit
        LIMIT 1
    ) INTO v_has_more;

    SELECT COALESCE(jsonb_agg(sub), '[]'::jsonb)
    INTO v_history
    FROM (
        SELECT
            m.id AS match_id,
            m.room_id,
            m.duration_seconds,
            m.match_status,
            m.is_verified,
            m.verification_status,
            m.completed_at,
            CASE WHEN m.player1_id = v_user_id THEN m.player1_score ELSE m.player2_score END AS player_score,
            CASE WHEN m.player1_id = v_user_id THEN m.player2_score ELSE m.player1_score END AS opponent_score,
            CASE
                WHEN m.winner_id = v_user_id THEN 'VICTORY'
                WHEN m.winner_id IS NULL THEN 'DRAW'
                ELSE 'DEFEAT'
            END AS result,
            p.username AS opponent_callsign
        FROM public.multiplayer_matches m
        JOIN public.profiles p
          ON p.id = CASE WHEN m.player1_id = v_user_id THEN m.player2_id ELSE m.player1_id END
        WHERE m.player1_id = v_user_id OR m.player2_id = v_user_id
        ORDER BY m.completed_at DESC, m.id DESC
        LIMIT v_limit
        OFFSET v_offset
    ) sub;

    RETURN jsonb_build_object(
        'success', true,
        'history', v_history,
        'has_more', v_has_more
    );
END;
$$;

REVOKE ALL ON FUNCTION public.get_my_multiplayer_match_history(integer, integer) FROM public, anon;
GRANT EXECUTE ON FUNCTION public.get_my_multiplayer_match_history(integer, integer) TO authenticated;

COMMIT;
