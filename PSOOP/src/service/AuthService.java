package service;

import database.SessionManager;
import database.SimpleJson;
import database.SupabaseConfig;
import database.SupabaseHttpClient;
import database.UserSession;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * High-level authentication service for AIRSTRIKE integrating Supabase Auth.
 *
 * Implements:
 * - Asynchronous pilot registration via /auth/v1/signup
 * - Asynchronous pilot authentication via /auth/v1/token?grant_type=password
 * - Asynchronous sign-out via /auth/v1/logout
 * - Profile synchronization with public.profiles via ProfileService
 * - Clean decoupling from Swing UI using CompletableFuture
 */
public class AuthService {

    private final SupabaseHttpClient httpClient;
    private final ProfileService profileService;
    private final SessionManager sessionManager;

    public AuthService() {
        this(new SupabaseHttpClient(), new ProfileService(), SessionManager.getInstance());
    }

    public AuthService(SupabaseHttpClient httpClient, ProfileService profileService, SessionManager sessionManager) {
        this.httpClient = httpClient;
        this.profileService = profileService;
        this.sessionManager = sessionManager;
    }

    public boolean isConfigured() {
        return httpClient.getConfig().isConfigured();
    }

    /**
     * Registers a new pilot account with email, password, and callsign.
     */
    public CompletableFuture<AuthResult> register(String email, String password, String callsign) {
        // Input validations first
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return CompletableFuture.completedFuture(
                    AuthResult.error(AuthResult.Status.INVALID_CREDENTIALS, "Please enter a valid pilot email address.", 400)
            );
        }

        if (password == null || password.length() < 6) {
            return CompletableFuture.completedFuture(
                    AuthResult.error(AuthResult.Status.WEAK_PASSWORD, "Password must be at least 6 characters.", 400)
            );
        }

        if (!isConfigured()) {
            return CompletableFuture.completedFuture(
                    AuthResult.configurationMissing("Supabase is not configured. Check config/supabase.properties.")
            );
        }

        final String pilotCallsign = (callsign != null && !callsign.trim().isEmpty())
                ? callsign.trim()
                : email.substring(0, email.indexOf('@'));

        Map<String, Object> body = new HashMap<>();
        body.put("email", email.trim().toLowerCase());
        body.put("password", password);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("username", pilotCallsign);
        metadata.put("callsign", pilotCallsign);
        body.put("data", metadata);

        String jsonPayload = SimpleJson.toJson(body);

        return httpClient.post("/auth/v1/signup", jsonPayload, null)
                .thenCompose(response -> handleSignupResponse(response, pilotCallsign))
                .exceptionally(ex -> {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    return AuthResult.networkError("Comm link failure: " + (msg != null ? msg : "Unable to reach Starfleet"));
                });
    }

    private CompletableFuture<AuthResult> handleSignupResponse(HttpResponse<String> response, String callsign) {
        int code = response.statusCode();
        String body = response.body();
        Map<String, Object> json = SimpleJson.parseObject(body);

        if (code == 200 || code == 201) {
            // Check if identities array is present and empty -> Supabase returns empty identities for duplicate user
            Object identitiesObj = json.get("identities");
            if (identitiesObj instanceof java.util.List && ((java.util.List<?>) identitiesObj).isEmpty()) {
                return CompletableFuture.completedFuture(
                        AuthResult.userAlreadyExists("A pilot with this email is already registered in the registry.")
                );
            }

            // Check if an access token was returned directly (when email confirm is disabled)
            String token = SimpleJson.getString(json, "access_token");
            if (token != null && !token.isEmpty()) {
                String userId = SimpleJson.getStringPath(json, "user.id");
                String email = SimpleJson.getStringPath(json, "user.email");
                long expiresIn = SimpleJson.getLong(json, "expires_in", 3600);
                String refreshToken = SimpleJson.getString(json, "refresh_token");

                UserSession session = new UserSession(
                        userId, email, callsign, token, refreshToken,
                        System.currentTimeMillis() + (expiresIn * 1000), true
                );
                sessionManager.setSession(session);

                // Attempt to create public profile in background
                profileService.upsertPilotProfile(userId, callsign, token);

                return CompletableFuture.completedFuture(
                        AuthResult.success("Registration successful! Clearance granted, Pilot " + callsign + ".", session)
                );
            }

            // Email confirmation is required by Supabase project settings
            return CompletableFuture.completedFuture(
                    AuthResult.confirmationPending("Registration accepted! A verification link was dispatched to your email.")
            );
        }

        // Handle error responses
        String errorMsg = extractErrorMessage(json, code);
        if (code == 422 || errorMsg.toLowerCase().contains("already registered") || errorMsg.toLowerCase().contains("already exists")) {
            return CompletableFuture.completedFuture(AuthResult.userAlreadyExists(errorMsg));
        }

        return CompletableFuture.completedFuture(AuthResult.error(AuthResult.Status.UNKNOWN_ERROR, errorMsg, code));
    }

    /**
     * Authenticates an existing pilot with email and password.
     */
    public CompletableFuture<AuthResult> login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return CompletableFuture.completedFuture(
                    AuthResult.invalidCredentials("Please enter both email and flight security clearance password.")
            );
        }

        if (!isConfigured()) {
            return CompletableFuture.completedFuture(
                    AuthResult.configurationMissing("Supabase is not configured. Check config/supabase.properties.")
            );
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", email.trim().toLowerCase());
        body.put("password", password);

        String jsonPayload = SimpleJson.toJson(body);

        return httpClient.post("/auth/v1/token?grant_type=password", jsonPayload, null)
                .thenCompose(this::handleLoginResponse)
                .exceptionally(ex -> {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    return AuthResult.networkError("Comm link failure: " + (msg != null ? msg : "Unable to reach Starfleet"));
                });
    }

    private CompletableFuture<AuthResult> handleLoginResponse(HttpResponse<String> response) {
        int code = response.statusCode();
        String body = response.body();
        Map<String, Object> json = SimpleJson.parseObject(body);

        if (code == 200) {
            String accessToken = SimpleJson.getString(json, "access_token");
            String refreshToken = SimpleJson.getString(json, "refresh_token");
            long expiresIn = SimpleJson.getLong(json, "expires_in", 3600);

            String userId = SimpleJson.getStringPath(json, "user.id");
            String email = SimpleJson.getStringPath(json, "user.email");
            String metaCallsign = SimpleJson.getStringPath(json, "user.user_metadata.callsign");
            if (metaCallsign == null || metaCallsign.isEmpty()) {
                metaCallsign = SimpleJson.getStringPath(json, "user.user_metadata.username");
            }
            if (metaCallsign == null || metaCallsign.isEmpty()) {
                metaCallsign = (email != null && email.contains("@")) ? email.substring(0, email.indexOf('@')) : "Pilot";
            }

            final String initialCallsign = metaCallsign;
            final long expiresAt = System.currentTimeMillis() + (expiresIn * 1000);

            // Synchronize with public.profiles table
            return profileService.fetchPilotCallsign(userId, accessToken, initialCallsign)
                    .thenApply(callsign -> {
                        UserSession session = new UserSession(
                                userId, email, callsign, accessToken, refreshToken, expiresAt, true
                        );
                        sessionManager.setSession(session);
                        return AuthResult.success("Clearance confirmed. Welcome back, Commander " + callsign + "!", session);
                    });
        }

        String errorMsg = extractErrorMessage(json, code);
        if (errorMsg.toLowerCase().contains("confirm")) {
            return CompletableFuture.completedFuture(
                    AuthResult.confirmationPending("Email confirmation pending. Please verify your email before logging in.")
            );
        }

        return CompletableFuture.completedFuture(AuthResult.invalidCredentials(errorMsg));
    }

    /**
     * Signs out the active pilot from Supabase and resets local session state.
     */
    public CompletableFuture<Boolean> logout() {
        UserSession active = sessionManager.getCurrentSession();
        String token = active.getAccessToken();

        // Local session is always cleared immediately to protect security
        sessionManager.clearSession();

        if (isConfigured() && token != null && !token.isEmpty()) {
            return httpClient.post("/auth/v1/logout", "{}", token)
                    .thenApply(resp -> true)
                    .exceptionally(ex -> true);
        }

        return CompletableFuture.completedFuture(true);
    }

    private String extractErrorMessage(Map<String, Object> json, int defaultCode) {
        String msg = SimpleJson.getString(json, "error_description");
        if (msg == null) msg = SimpleJson.getString(json, "msg");
        if (msg == null) msg = SimpleJson.getString(json, "message");
        if (msg == null) msg = SimpleJson.getString(json, "error");
        return (msg != null && !msg.trim().isEmpty()) ? msg.trim() : "Authentication error (Code " + defaultCode + ")";
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
