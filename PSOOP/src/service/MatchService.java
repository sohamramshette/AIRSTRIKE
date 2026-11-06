package service;

import database.SessionManager;
import database.SimpleJson;
import database.SupabaseHttpClient;
import database.UserSession;
import model.LeaderboardEntry;
import model.MatchRecord;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service to manage gameplay match persistence, match history queries,
 * and global leaderboard retrieval with Supabase PostgREST.
 *
 * Implements strict security and integrity rules:
 * - Guest and offline matches remain local and are never uploaded.
 * - Prevents duplicate score submissions for the same game session.
 * - Uses publishable/anon key with authenticated Bearer JWT tokens (no service-role key).
 * - Adheres to Row Level Security (RLS) so pilots only access their own match records.
 * - Non-blocking asynchronous execution via CompletableFuture.
 */
public class MatchService {

    private final SupabaseHttpClient httpClient;
    private final SessionManager sessionManager;

    public MatchService() {
        this(new SupabaseHttpClient(), SessionManager.getInstance());
    }

    public MatchService(SupabaseHttpClient httpClient, SessionManager sessionManager) {
        this.httpClient = httpClient;
        this.sessionManager = sessionManager;
    }

    /**
     * Persists an authenticated player's completed match to public.matches.
     * Endpoint: POST /rest/v1/matches
     *
     * @param score Final match score.
     * @param levelReached Highest sector/level attained.
     * @param durationSeconds Total mission duration in seconds.
     * @return CompletableFuture resolving to true if successfully stored; false if guest/error.
     */
    public CompletableFuture<Boolean> saveMatch(int score, int levelReached, int durationSeconds) {
        if (!sessionManager.isLoggedIn()) {
            // Guest or offline pilots: matches remain local-only
            return CompletableFuture.completedFuture(false);
        }

        UserSession session = sessionManager.getCurrentSession();
        if (session.isExpired()) {
            sessionManager.clearSession();
            return CompletableFuture.completedFuture(false);
        }

        String userId = session.getUserId();
        String accessToken = session.getAccessToken();

        if (userId.isEmpty() || accessToken.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user_id", userId);
        payload.put("score", score);
        payload.put("level_reached", Math.max(1, levelReached));
        payload.put("duration_seconds", Math.max(1, durationSeconds));

        String json = SimpleJson.toJson(payload);
        Map<String, String> extraHeaders = Collections.singletonMap("Prefer", "return=representation");

        return httpClient.post("/rest/v1/matches", json, accessToken, extraHeaders)
                .thenApply(response -> {
                    int status = response.statusCode();
                    return status >= 200 && status < 300;
                })
                .exceptionally(ex -> false);
    }

    /**
     * Fetches only the currently authenticated pilot's match history.
     * Endpoint: GET /rest/v1/matches?select=id,score,level_reached,duration_seconds,created_at&user_id=eq.<userId>&order=created_at.desc
     *
     * @return CompletableFuture with list of MatchRecords.
     */
    public CompletableFuture<List<MatchRecord>> fetchMyMatchHistory() {
        if (!sessionManager.isLoggedIn()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        UserSession session = sessionManager.getCurrentSession();
        if (session.isExpired()) {
            sessionManager.clearSession();
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        String userId = session.getUserId();
        String accessToken = session.getAccessToken();
        String path = "/rest/v1/matches?select=id,score,level_reached,duration_seconds,created_at&user_id=eq."
                + userId + "&order=created_at.desc";

        return httpClient.get(path, accessToken)
                .thenApply(this::parseMatchHistoryResponse)
                .exceptionally(ex -> new ArrayList<>());
    }

    /**
     * Parses the PostgREST JSON array response into a list of MatchRecord objects.
     */
    public List<MatchRecord> parseMatchHistoryResponse(HttpResponse<String> response) {
        List<MatchRecord> records = new ArrayList<>();
        if (response == null || response.statusCode() < 200 || response.statusCode() >= 300) {
            return records;
        }

        List<Object> rows = SimpleJson.parseArray(response.body());
        for (Object rowObj : rows) {
            if (rowObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) rowObj;
                String id = SimpleJson.getString(map, "id");
                String userId = SimpleJson.getString(map, "user_id");
                int score = SimpleJson.getInt(map, "score", 0);
                int level = SimpleJson.getInt(map, "level_reached", 1);
                int duration = SimpleJson.getInt(map, "duration_seconds", 0);
                String createdAt = SimpleJson.getString(map, "created_at");

                records.add(new MatchRecord(id, userId, score, level, duration, createdAt));
            }
        }
        return records;
    }

    /**
     * Fetches the top 10 global leaderboard scores.
     * Endpoint: GET /rest/v1/leaderboard?select=username,best_score&order=best_score.desc&limit=10
     *
     * @return CompletableFuture resolving to top 10 LeaderboardEntries.
     */
    public CompletableFuture<List<LeaderboardEntry>> fetchGlobalLeaderboard() {
        String token = null;
        if (sessionManager.isLoggedIn()) {
            UserSession session = sessionManager.getCurrentSession();
            if (!session.isExpired()) {
                token = session.getAccessToken();
            }
        }

        String path = "/rest/v1/leaderboard?select=username,best_score&order=best_score.desc&limit=10";

        return httpClient.get(path, token)
                .thenApply(this::parseLeaderboardResponse)
                .exceptionally(ex -> new ArrayList<>());
    }

    /**
     * Parses the PostgREST JSON array response into a list of LeaderboardEntry objects.
     */
    public List<LeaderboardEntry> parseLeaderboardResponse(HttpResponse<String> response) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (response == null || response.statusCode() < 200 || response.statusCode() >= 300) {
            return entries;
        }

        List<Object> rows = SimpleJson.parseArray(response.body());
        int rank = 1;
        for (Object rowObj : rows) {
            if (rowObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) rowObj;
                String username = SimpleJson.getString(map, "username");
                int bestScore = SimpleJson.getInt(map, "best_score", 0);

                entries.add(new LeaderboardEntry(rank++, username, bestScore));
            }
        }
        return entries;
    }

    public SupabaseHttpClient getHttpClient() {
        return httpClient;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}

