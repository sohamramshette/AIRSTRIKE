package service;

import database.SimpleJson;
import database.SupabaseHttpClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service to query and synchronize pilot profile records with the Supabase PostgREST table (public.profiles).
 */
public class ProfileService {

    private final SupabaseHttpClient httpClient;

    public ProfileService() {
        this(new SupabaseHttpClient());
    }

    public ProfileService(SupabaseHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Queries the public.profiles table for the pilot record corresponding to the authenticated UUID.
     * Endpoint: GET /rest/v1/profiles?id=eq.<userId>&select=*
     *
     * @param userId The UUID of the authenticated pilot.
     * @param accessToken The JWT access token for authorization.
     * @param defaultCallsign Fallback name if no record exists yet.
     * @return CompletableFuture yielding the pilot callsign/username.
     */
    public CompletableFuture<String> fetchPilotCallsign(String userId, String accessToken, String defaultCallsign) {
        if (userId == null || userId.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            return CompletableFuture.completedFuture(defaultCallsign);
        }

        String path = "/rest/v1/profiles?id=eq." + userId + "&select=*";

        return httpClient.get(path, accessToken).thenApply(response -> {
            if (response.statusCode() == 200) {
                List<Object> rows = SimpleJson.parseArray(response.body());
                if (!rows.isEmpty() && rows.get(0) instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> profile = (Map<String, Object>) rows.get(0);
                    String callsign = SimpleJson.getString(profile, "username");
                    if (callsign == null || callsign.trim().isEmpty()) {
                        callsign = SimpleJson.getString(profile, "callsign");
                    }
                    if (callsign != null && !callsign.trim().isEmpty()) {
                        return callsign.trim();
                    }
                }
            }
            return defaultCallsign;
        }).exceptionally(ex -> defaultCallsign);
    }

    /**
     * Upserts a profile record in public.profiles.
     */
    public CompletableFuture<Boolean> upsertPilotProfile(String userId, String callsign, String accessToken) {
        if (userId == null || userId.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        String path = "/rest/v1/profiles";
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", userId);
        payload.put("username", callsign != null ? callsign.trim() : "Pilot");

        Map<String, String> extraHeaders = Collections.singletonMap("Prefer", "resolution=merge-duplicates");

        return httpClient.post(path, SimpleJson.toJson(payload), accessToken, extraHeaders)
                .thenApply(response -> response.statusCode() >= 200 && response.statusCode() < 300)
                .exceptionally(ex -> false);
    }
}

