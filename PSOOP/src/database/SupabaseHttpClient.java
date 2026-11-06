package database;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous HTTPS client for communicating with Supabase Auth and PostgREST endpoints.
 *
 * Implements security requirements:
 * - Built on standard Java HttpClient (Java 26 / SE 11+).
 * - Enforces HTTPS.
 * - Injects standard Supabase apikey and optional Authorization Bearer headers.
 * - Never logs raw Bearer tokens or plaintext credentials.
 */
public class SupabaseHttpClient {

    private final SupabaseConfig config;
    private final HttpClient httpClient;

    public SupabaseHttpClient() {
        this(SupabaseConfig.getInstance());
    }

    public SupabaseHttpClient(SupabaseConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
                .build();
    }

    /**
     * Executes an asynchronous POST request against the Supabase endpoint.
     */
    public CompletableFuture<HttpResponse<String>> post(String endpointPath, String jsonBody, String bearerToken) {
        return post(endpointPath, jsonBody, bearerToken, null);
    }

    /**
     * Executes an asynchronous POST request with optional extra headers.
     */
    public CompletableFuture<HttpResponse<String>> post(String endpointPath, String jsonBody,
                                                       String bearerToken, Map<String, String> extraHeaders) {
        if (!config.isConfigured()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Supabase configuration is missing or incomplete.")
            );
        }

        try {
            String fullUrl = buildFullUrl(endpointPath);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofMillis(config.getTimeoutMs()))
                    .header("apikey", config.getSupabaseKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            if (bearerToken != null && !bearerToken.trim().isEmpty()) {
                builder.header("Authorization", "Bearer " + bearerToken.trim());
            }

            if (extraHeaders != null) {
                for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }

            String payload = (jsonBody != null) ? jsonBody : "{}";
            builder.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));

            return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Executes an asynchronous GET request against the Supabase endpoint.
     */
    public CompletableFuture<HttpResponse<String>> get(String endpointPath, String bearerToken) {
        return get(endpointPath, bearerToken, null);
    }

    /**
     * Executes an asynchronous GET request with optional extra headers.
     */
    public CompletableFuture<HttpResponse<String>> get(String endpointPath, String bearerToken,
                                                      Map<String, String> extraHeaders) {
        if (!config.isConfigured()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Supabase configuration is missing or incomplete.")
            );
        }

        try {
            String fullUrl = buildFullUrl(endpointPath);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofMillis(config.getTimeoutMs()))
                    .header("apikey", config.getSupabaseKey())
                    .header("Accept", "application/json");

            if (bearerToken != null && !bearerToken.trim().isEmpty()) {
                builder.header("Authorization", "Bearer " + bearerToken.trim());
            }

            if (extraHeaders != null) {
                for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }

            builder.GET();
            return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private String buildFullUrl(String path) {
        if (path == null) path = "";
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return config.getSupabaseUrl() + path;
    }

    public SupabaseConfig getConfig() {
        return config;
    }
}

