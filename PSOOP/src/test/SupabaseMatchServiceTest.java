package test;

import database.SessionManager;
import database.SimpleJson;
import database.SupabaseConfig;
import database.SupabaseHttpClient;
import database.UserSession;
import game.GameEngine;
import game.GameState;
import model.LeaderboardEntry;
import model.MatchRecord;
import service.MatchService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Automated test suite for Phase 6.2B: Supabase Gameplay Score Integration.
 *
 * Verifies all 8 mandatory specification tests:
 * 1. Authenticated match payload formatting and submission (POST /rest/v1/matches).
 * 2. Guest match local-only check (zero network calls, returns false).
 * 3. Duplicate submission prevention (game-over guard & restart reset).
 * 4. Match history PostgREST JSON array response parsing into List<MatchRecord>.
 * 5. Leaderboard JSON response parsing and ranking into List<LeaderboardEntry>.
 * 6. Network failure handling (returns false/empty, no unhandled exceptions).
 * 7. Expired session handling (auto-clears session, aborts network request).
 * 8. GameEngine game-over lifecycle duration and score calculation.
 */
public class SupabaseMatchServiceTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   AIRSTRIKE PHASE 6.2B: SUPABASE SCORE & MATCH TESTS   ");
        System.out.println("========================================================");

        try {
            // Test 1: Authenticated Match Submission & Payload Validation
            test1_AuthenticatedMatchSubmission();

            // Test 2: Guest Match Local-Only Guard
            test2_GuestMatchLocalOnly();

            // Test 3: Duplicate Submission Prevention
            test3_DuplicateSubmissionPrevention();

            // Test 4: Match History JSON Parsing & Helpers
            test4_MatchHistoryParsing();

            // Test 5: Leaderboard JSON Parsing & Rank Formatting
            test5_LeaderboardParsing();

            // Test 6: Network Failure Handling
            test6_NetworkFailureHandling();

            // Test 7: Expired Session Auto-Clear Guard
            test7_ExpiredSessionHandling();

            // Test 8: GameEngine Game-Over Lifecycle & Timing
            test8_GameOverLifecycleIntegration();

            // Additional Verification: MatchRecord Formatting & Validation
            test9_MatchRecordFormatting();

        } catch (Throwable t) {
            System.err.println("[CRITICAL TEST ERROR] " + t.getMessage());
            t.printStackTrace();
        }

        System.out.println("\n--------------------------------------------------------");
        System.out.printf("Test Execution Complete: %d / %d Passed (%.1f%%)\n",
                testsPassed, testsRun, (testsRun > 0 ? (testsPassed * 100.0 / testsRun) : 0.0));
        System.out.println("--------------------------------------------------------");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void assertTest(String description, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + description);
        } else {
            System.err.println("  [FAIL] " + description);
        }
    }

    /**
     * Test 1: Authenticated match payload formatting and submission (POST /rest/v1/matches).
     */
    private static void test1_AuthenticatedMatchSubmission() {
        System.out.println("\n[GROUP 1] Authenticated Match Submission (POST /rest/v1/matches)");

        AtomicReference<String> capturedPath = new AtomicReference<>();
        AtomicReference<String> capturedBody = new AtomicReference<>();
        AtomicReference<String> capturedToken = new AtomicReference<>();
        AtomicReference<Map<String, String>> capturedHeaders = new AtomicReference<>();

        MockSupabaseClient mockClient = new MockSupabaseClient() {
            @Override
            public CompletableFuture<HttpResponse<String>> post(String path, String jsonBody,
                                                               String bearerToken, Map<String, String> extraHeaders) {
                capturedPath.set(path);
                capturedBody.set(jsonBody);
                capturedToken.set(bearerToken);
                capturedHeaders.set(extraHeaders);
                return CompletableFuture.completedFuture(new MockHttpResponse(201, "[{\"id\":\"uuid-1\"}]"));
            }
        };

        SessionManager sm = SessionManager.getInstance();
        UserSession activeSession = new UserSession(
                "pilot-user-1234",
                "starpilot@galaxy.net",
                "PhoenixAce",
                "jwt.access.token.abc",
                "refresh-token",
                System.currentTimeMillis() + 3600000,
                true
        );
        sm.setSession(activeSession);

        MatchService matchService = new MatchService(mockClient, sm);
        boolean saved = matchService.saveMatch(15250, 4, 210).join();

        assertTest("1.1 Save match returns true on HTTP 201 Created", saved);
        assertTest("1.2 Correct endpoint path /rest/v1/matches targeted", "/rest/v1/matches".equals(capturedPath.get()));
        assertTest("1.3 Correct user Bearer token passed in request", "jwt.access.token.abc".equals(capturedToken.get()));
        assertTest("1.4 Prefer: return=representation header included",
                capturedHeaders.get() != null && "return=representation".equals(capturedHeaders.get().get("Prefer")));

        Map<String, Object> bodyMap = SimpleJson.parseObject(capturedBody.get());
        assertTest("1.5 Payload contains matching user_id", "pilot-user-1234".equals(bodyMap.get("user_id")));
        assertTest("1.6 Payload contains matching score (15250)", SimpleJson.getInt(bodyMap, "score", -1) == 15250);
        assertTest("1.7 Payload contains matching level_reached (4)", SimpleJson.getInt(bodyMap, "level_reached", -1) == 4);
        assertTest("1.8 Payload contains matching duration_seconds (210)", SimpleJson.getInt(bodyMap, "duration_seconds", -1) == 210);
    }

    /**
     * Test 2: Guest match local-only check (zero network calls, returns false).
     */
    private static void test2_GuestMatchLocalOnly() {
        System.out.println("\n[GROUP 2] Guest Match Local-Only Guard");

        AtomicInteger networkCalls = new AtomicInteger(0);
        MockSupabaseClient mockClient = new MockSupabaseClient() {
            @Override
            public CompletableFuture<HttpResponse<String>> post(String path, String body, String token, Map<String, String> headers) {
                networkCalls.incrementAndGet();
                return CompletableFuture.completedFuture(new MockHttpResponse(200, "{}"));
            }
        };

        SessionManager sm = SessionManager.getInstance();
        sm.clearSession(); // Ensure Guest mode

        assertTest("2.1 SessionManager correctly reports logged out / guest", !sm.isLoggedIn());

        MatchService matchService = new MatchService(mockClient, sm);
        boolean result = matchService.saveMatch(99999, 10, 500).join();

        assertTest("2.2 Guest match returns false from saveMatch()", !result);
        assertTest("2.3 Zero network calls dispatched for guest match", networkCalls.get() == 0);
    }

    /**
     * Test 3: Duplicate submission prevention (game-over guard & restart reset).
     */
    private static void test3_DuplicateSubmissionPrevention() {
        System.out.println("\n[GROUP 3] Duplicate Submission Prevention");

        AtomicInteger uploadCount = new AtomicInteger(0);
        MockSupabaseClient mockClient = new MockSupabaseClient() {
            @Override
            public CompletableFuture<HttpResponse<String>> post(String path, String body, String token, Map<String, String> headers) {
                uploadCount.incrementAndGet();
                return CompletableFuture.completedFuture(new MockHttpResponse(201, "[]"));
            }
        };

        SessionManager sm = SessionManager.getInstance();
        sm.setSession(new UserSession("user-dup-test", "test@test.com", "DupTest", "token", "ref", System.currentTimeMillis() + 60000, true));

        MatchService matchService = new MatchService(mockClient, sm);
        GameEngine engine = new GameEngine(800, 650, matchService);

        engine.startNewGame();
        assertTest("3.1 New game starts with matchUploaded=false", !engine.isMatchUploaded());

        try {
            // First transition to GAME_OVER
            engine.setState(GameState.GAME_OVER);
            assertTest("3.2 Transition to GAME_OVER marks matchUploaded=true", engine.isMatchUploaded());
            assertTest("3.3 First GAME_OVER triggers score save call", uploadCount.get() == 1);

            // Redundant trigger (e.g. repeated frame update or redundant key press)
            engine.setState(GameState.GAME_OVER);
            assertTest("3.4 Redundant GAME_OVER does not dispatch duplicate upload", uploadCount.get() == 1);

            // Starting a new game resets the upload guard
            engine.startNewGame();
            assertTest("3.5 Restarting game resets matchUploaded back to false", !engine.isMatchUploaded());

            // Next game over allows saving new match
            engine.setState(GameState.GAME_OVER);
            assertTest("3.6 Subsequent game over correctly uploads new match", uploadCount.get() == 2);
        } catch (Exception e) {
            assertTest("3.7 No exception during state transitions: " + e.getMessage(), false);
        }
    }

    /**
     * Test 4: Match history PostgREST JSON array response parsing into List<MatchRecord>.
     */
    private static void test4_MatchHistoryParsing() {
        System.out.println("\n[GROUP 4] Match History JSON Parsing & Data Mapping");

        String mockPostgrestJson = "[\n" +
                "  {\"id\": \"rec-001\", \"user_id\": \"u-1\", \"score\": 25400, \"level_reached\": 5, \"duration_seconds\": 285, \"created_at\": \"2026-11-06T14:30:00Z\"},\n" +
                "  {\"id\": \"rec-002\", \"user_id\": \"u-1\", \"score\": 8200,  \"level_reached\": 2, \"duration_seconds\": 65,  \"created_at\": \"2026-11-06T12:00:00Z\"}\n" +
                "]";

        MockHttpResponse response = new MockHttpResponse(200, mockPostgrestJson);
        MatchService matchService = new MatchService();

        List<MatchRecord> records = matchService.parseMatchHistoryResponse(response);

        assertTest("4.1 Parsed records list has size 2", records.size() == 2);

        MatchRecord r1 = records.get(0);
        assertTest("4.2 Record 1 id parsed correctly", "rec-001".equals(r1.getId()));
        assertTest("4.3 Record 1 score is 25,400", r1.getScore() == 25400);
        assertTest("4.4 Record 1 level is 5", r1.getLevelReached() == 5);
        assertTest("4.5 Record 1 duration is 285 seconds", r1.getDurationSeconds() == 285);
        assertTest("4.6 Record 1 formatted duration is 04:45", "04:45".equals(r1.getFormattedDuration()));

        MatchRecord r2 = records.get(1);
        assertTest("4.7 Record 2 formatted duration is 01:05", "01:05".equals(r2.getFormattedDuration()));

        // Empty response check
        List<MatchRecord> emptyList = matchService.parseMatchHistoryResponse(new MockHttpResponse(200, "[]"));
        assertTest("4.8 Empty JSON array produces empty list", emptyList.isEmpty());

        // Error status check
        List<MatchRecord> errList = matchService.parseMatchHistoryResponse(new MockHttpResponse(500, "Internal Server Error"));
        assertTest("4.9 HTTP 500 produces empty list without crashing", errList.isEmpty());
    }

    /**
     * Test 5: Leaderboard JSON response parsing and ranking into List<LeaderboardEntry>.
     */
    private static void test5_LeaderboardParsing() {
        System.out.println("\n[GROUP 5] Leaderboard JSON Parsing & Rank Formatting");

        String mockLeaderboardJson = "[\n" +
                "  {\"username\": \"ViperAce\", \"best_score\": 98500},\n" +
                "  {\"username\": \"CosmoStar\", \"best_score\": 84200},\n" +
                "  {\"username\": \"\", \"best_score\": 51000},\n" +
                "  {\"username\": \"NovaHunter\", \"best_score\": 32000}\n" +
                "]";

        MockHttpResponse response = new MockHttpResponse(200, mockLeaderboardJson);
        MatchService matchService = new MatchService();

        List<LeaderboardEntry> entries = matchService.parseLeaderboardResponse(response);

        assertTest("5.1 Leaderboard entries list has size 4", entries.size() == 4);

        LeaderboardEntry rank1 = entries.get(0);
        assertTest("5.2 Rank 1 is assigned rank 1", rank1.getRank() == 1);
        assertTest("5.3 Rank 1 username is ViperAce", "ViperAce".equals(rank1.getUsername()));
        assertTest("5.4 Rank 1 score is 98,500", rank1.getBestScore() == 98500);

        LeaderboardEntry rank2 = entries.get(1);
        assertTest("5.5 Rank 2 is assigned rank 2", rank2.getRank() == 2);

        LeaderboardEntry rank3 = entries.get(2);
        assertTest("5.6 Blank username falls back to Anonymous Pilot", "Anonymous Pilot".equals(rank3.getUsername()));

        LeaderboardEntry rank4 = entries.get(3);
        assertTest("5.7 Rank 4 is assigned rank 4", rank4.getRank() == 4);
    }

    /**
     * Test 6: Network failure handling (returns false/empty, no unhandled exceptions).
     */
    private static void test6_NetworkFailureHandling() {
        System.out.println("\n[GROUP 6] Network Failure Handling & Resilience");

        MockSupabaseClient failingClient = new MockSupabaseClient() {
            @Override
            public CompletableFuture<HttpResponse<String>> post(String path, String body, String token, Map<String, String> headers) {
                CompletableFuture<HttpResponse<String>> failed = new CompletableFuture<>();
                failed.completeExceptionally(new java.net.ConnectException("Starfleet Uplink Timeout"));
                return failed;
            }

            @Override
            public CompletableFuture<HttpResponse<String>> get(String path, String token, Map<String, String> headers) {
                CompletableFuture<HttpResponse<String>> failed = new CompletableFuture<>();
                failed.completeExceptionally(new java.io.IOException("Network down"));
                return failed;
            }
        };

        SessionManager sm = SessionManager.getInstance();
        sm.setSession(new UserSession("user-fail-test", "fail@galaxy.com", "FailSafe", "token", "ref", System.currentTimeMillis() + 60000, true));

        MatchService matchService = new MatchService(failingClient, sm);

        boolean saveResult = matchService.saveMatch(1000, 1, 30).join();
        assertTest("6.1 Network failure during saveMatch gracefully returns false", !saveResult);

        List<MatchRecord> history = matchService.fetchMyMatchHistory().join();
        assertTest("6.2 Network failure during fetchMyMatchHistory returns empty list", history != null && history.isEmpty());

        List<LeaderboardEntry> leaderboard = matchService.fetchGlobalLeaderboard().join();
        assertTest("6.3 Network failure during fetchGlobalLeaderboard returns empty list", leaderboard != null && leaderboard.isEmpty());
    }

    /**
     * Test 7: Expired session handling (auto-clears session, aborts network request).
     */
    private static void test7_ExpiredSessionHandling() {
        System.out.println("\n[GROUP 7] Expired Session Auto-Clear Guard");

        AtomicInteger networkCalls = new AtomicInteger(0);
        MockSupabaseClient mockClient = new MockSupabaseClient() {
            @Override
            public CompletableFuture<HttpResponse<String>> post(String path, String body, String token, Map<String, String> headers) {
                networkCalls.incrementAndGet();
                return CompletableFuture.completedFuture(new MockHttpResponse(200, "{}"));
            }

            @Override
            public CompletableFuture<HttpResponse<String>> get(String path, String token, Map<String, String> headers) {
                networkCalls.incrementAndGet();
                return CompletableFuture.completedFuture(new MockHttpResponse(200, "[]"));
            }
        };

        SessionManager sm = SessionManager.getInstance();
        // Session expired 5 minutes ago
        UserSession expired = new UserSession(
                "pilot-expired",
                "old@orbit.net",
                "OldPilot",
                "expired-token",
                "expired-refresh",
                System.currentTimeMillis() - 300000,
                true
        );
        sm.setSession(expired);

        assertTest("7.1 UserSession isExpired() correctly returns true", expired.isExpired());

        MatchService matchService = new MatchService(mockClient, sm);
        boolean saveResult = matchService.saveMatch(5000, 2, 45).join();

        assertTest("7.2 saveMatch returns false when session is expired", !saveResult);
        assertTest("7.3 Zero network calls made during expired saveMatch", networkCalls.get() == 0);
        assertTest("7.4 SessionManager automatically cleared expired session", !sm.isLoggedIn());

        // Re-arm expired session to test history
        sm.setSession(expired);
        List<MatchRecord> records = matchService.fetchMyMatchHistory().join();
        assertTest("7.5 fetchMyMatchHistory returns empty list when session is expired", records.isEmpty());
        assertTest("7.6 SessionManager automatically cleared expired session after history call", !sm.isLoggedIn());
    }

    /**
     * Test 8: GameEngine game-over lifecycle duration and score calculation.
     */
    private static void test8_GameOverLifecycleIntegration() {
        System.out.println("\n[GROUP 8] GameEngine Game-Over Lifecycle Integration");

        AtomicInteger capturedScore = new AtomicInteger(-1);
        AtomicInteger capturedLevel = new AtomicInteger(-1);
        AtomicInteger capturedDuration = new AtomicInteger(-1);

        MockSupabaseClient mockClient = new MockSupabaseClient() {
            @Override
            public CompletableFuture<HttpResponse<String>> post(String path, String jsonBody, String token, Map<String, String> headers) {
                Map<String, Object> map = SimpleJson.parseObject(jsonBody);
                capturedScore.set(SimpleJson.getInt(map, "score", -1));
                capturedLevel.set(SimpleJson.getInt(map, "level_reached", -1));
                capturedDuration.set(SimpleJson.getInt(map, "duration_seconds", -1));
                return CompletableFuture.completedFuture(new MockHttpResponse(201, "[]"));
            }
        };

        SessionManager sm = SessionManager.getInstance();
        sm.setSession(new UserSession("pilot-gameover", "go@space.com", "GoPilot", "tok", "ref", System.currentTimeMillis() + 60000, true));

        MatchService matchService = new MatchService(mockClient, sm);
        GameEngine engine = new GameEngine(800, 650, matchService);

        engine.startNewGame();
        engine.getScoreCalculator().addScore(3450);
        engine.getWaveManager().resetToLevel(3);

        try {
            // Trigger game over
            engine.setState(GameState.GAME_OVER);

            assertTest("8.1 Uploaded score matches ScoreCalculator (3450)", capturedScore.get() == 3450);
            assertTest("8.2 Uploaded level matches WaveManager level (3)", capturedLevel.get() == 3);
            assertTest("8.3 Uploaded duration is positive (>= 1 second)", capturedDuration.get() >= 1);
            assertTest("8.4 GameEngine status contains transmission acknowledgement",
                    engine.getMatchUploadStatus() != null && !engine.getMatchUploadStatus().isEmpty());
        } catch (Exception e) {
            assertTest("8.5 No exception in game over flow: " + e.getMessage(), false);
        }
    }

    /**
     * Test 9: MatchRecord Formatting & Validation.
     */
    private static void test9_MatchRecordFormatting() {
        System.out.println("\n[GROUP 9] MatchRecord Duration and Date Formatting");

        MatchRecord r1 = new MatchRecord("1", "u1", 100, 1, 0, "2026-11-06T10:15:30Z");
        assertTest("9.1 Duration 0s formats to 00:00", "00:00".equals(r1.getFormattedDuration()));

        MatchRecord r2 = new MatchRecord("2", "u1", 100, 1, 59, "2026-11-06T10:15:30Z");
        assertTest("9.2 Duration 59s formats to 00:59", "00:59".equals(r2.getFormattedDuration()));

        MatchRecord r3 = new MatchRecord("3", "u1", 100, 1, 3665, "2026-11-06T10:15:30Z");
        assertTest("9.3 Duration 3665s formats to 61:05", "61:05".equals(r3.getFormattedDuration()));

        assertTest("9.4 Formatted date contains date substring", r3.getFormattedDate().contains("2026-11-06"));
    }

    // =========================================================================
    // Mock Helpers
    // =========================================================================

    private static class MockSupabaseClient extends SupabaseHttpClient {
        public MockSupabaseClient() {
            super(SupabaseConfig.getInstance());
        }
    }

    private static class MockHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;

        public MockHttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override public int statusCode() { return statusCode; }
        @Override public String body() { return body; }
        @Override public HttpRequest request() { return null; }
        @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() { return HttpHeaders.of(Collections.emptyMap(), (k, v) -> true); }
        @Override public URI uri() { return URI.create("https://mock.supabase.co"); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_2; }
        @Override public Optional<javax.net.ssl.SSLSession> sslSession() { return Optional.empty(); }
    }
}
