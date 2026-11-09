package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for Supabase API connectivity in AIRSTRIKE.
 *
 * Adheres strictly to security best practices:
 * - Credentials are loaded from external configuration files or environment variables.
 * - Sensitive values (API keys, service-role tokens) are never hardcoded.
 * - Rejects any attempt to configure service-role or secret administrative keys.
 * - Redacts API keys when converted to string or logged.
 */
public class SupabaseConfig {

    private static final String DEFAULT_TIMEOUT_MS = "8000";
    private static volatile SupabaseConfig instance;

    private final String supabaseUrl;
    private final String supabaseKey;
    private final int timeoutMs;
    private final boolean configured;

    public SupabaseConfig(String url, String key, int timeoutMs) {
        this.supabaseUrl = sanitizeUrl(url);
        this.supabaseKey = (key != null) ? key.trim() : "";
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 8000;

        // Security check: Never permit service_role key
        if (this.supabaseKey.toLowerCase().contains("service_role")) {
            throw new SecurityException("CRITICAL: Attempted to configure Supabase service-role secret key! " +
                    "Only anonymous/publishable keys are permitted in client applications.");
        }

        this.configured = validateConfig(this.supabaseUrl, this.supabaseKey);
    }

    /**
     * Lazy-loaded singleton instance initialized from external sources.
     */
    public static SupabaseConfig getInstance() {
        if (instance == null) {
            synchronized (SupabaseConfig.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    /**
     * Reset cached singleton instance (useful for unit testing).
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Load configuration following precedence:
     * 1. System Properties (-Dsupabase.url=... -Dsupabase.key=...)
     * 2. Environment Variables (SUPABASE_URL, SUPABASE_KEY, SUPABASE_ANON_KEY)
     * 3. config/supabase.properties
     * 4. supabase.properties (project root)
     */
    public static SupabaseConfig load() {
        String url = null;
        String key = null;
        String timeoutStr = null;

        // 1. System properties
        url = System.getProperty("supabase.url");
        key = System.getProperty("supabase.key");
        timeoutStr = System.getProperty("supabase.timeout.ms");

        // 2. Environment variables
        if (isNullOrEmpty(url)) url = System.getenv("SUPABASE_URL");
        if (isNullOrEmpty(key)) {
            key = System.getenv("SUPABASE_KEY");
            if (isNullOrEmpty(key)) {
                key = System.getenv("SUPABASE_ANON_KEY");
            }
        }
        if (isNullOrEmpty(timeoutStr)) timeoutStr = System.getenv("SUPABASE_TIMEOUT_MS");

        // 3. File search: config/supabase.properties then supabase.properties (including parent dirs)
        File[] candidateFiles = new File[] {
                new File("config/supabase.properties"),
                new File("supabase.properties"),
                new File("../config/supabase.properties"),
                new File("../supabase.properties"),
                new File("web/.env"),
                new File("../web/.env")
        };

        for (File file : candidateFiles) {
            if ((isPlaceholder(url) || isPlaceholder(key)) && file.exists() && file.isFile()) {
                Properties props = new Properties();
                try (InputStream in = new FileInputStream(file)) {
                    props.load(in);
                    if (isPlaceholder(url)) {
                        url = props.getProperty("supabase.url", props.getProperty("SUPABASE_URL", props.getProperty("VITE_SUPABASE_URL")));
                    }
                    if (isPlaceholder(key)) {
                        key = props.getProperty("supabase.key", props.getProperty("SUPABASE_KEY"));
                        if (isPlaceholder(key)) {
                            key = props.getProperty("supabase.anon.key", props.getProperty("SUPABASE_ANON_KEY"));
                        }
                        if (isPlaceholder(key)) {
                            key = props.getProperty("supabase.publishable.key", props.getProperty("VITE_SUPABASE_PUBLISHABLE_KEY"));
                        }
                        if (isPlaceholder(key)) {
                            key = props.getProperty("VITE_SUPABASE_ANON_KEY");
                        }
                    }
                    if (isNullOrEmpty(timeoutStr)) {
                        timeoutStr = props.getProperty("supabase.timeout.ms", props.getProperty("SUPABASE_TIMEOUT_MS"));
                    }
                } catch (Exception ignored) {
                    // Fail silently and check next source
                }
            }
        }

        int timeout = 8000;
        if (timeoutStr != null) {
            try {
                timeout = Integer.parseInt(timeoutStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        return new SupabaseConfig(url, key, timeout);
    }

    private static String sanitizeUrl(String url) {
        if (url == null) return "";
        url = url.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static boolean validateConfig(String url, String key) {
        if (isNullOrEmpty(url) || isNullOrEmpty(key)) return false;
        if (url.contains("YOUR_PROJECT_ID") || key.contains("YOUR_SUPABASE_ANON_KEY")) return false;
        return url.startsWith("https://") || url.startsWith("http://localhost");
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static boolean isPlaceholder(String str) {
        if (isNullOrEmpty(str)) return true;
        return str.contains("YOUR_PROJECT_ID") || str.contains("YOUR_SUPABASE_ANON_KEY");
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getSupabaseKey() {
        return supabaseKey;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isConfigured() {
        return configured;
    }

    /**
     * Redacted representation safe for logging.
     */
    @Override
    public String toString() {
        String maskedKey;
        if (supabaseKey.isEmpty()) {
            maskedKey = "[NONE]";
        } else if (supabaseKey.length() <= 8) {
            maskedKey = "********";
        } else {
            maskedKey = supabaseKey.substring(0, 6) + "..." + supabaseKey.substring(supabaseKey.length() - 3);
        }

        return "SupabaseConfig{" +
                "supabaseUrl='" + supabaseUrl + '\'' +
                ", supabaseKey='" + maskedKey + '\'' +
                ", timeoutMs=" + timeoutMs +
                ", configured=" + configured +
                '}';
    }
}
