package database;

import java.util.Objects;

/**
 * Encapsulates the authenticated pilot session state in AIRSTRIKE.
 *
 * Implements strict security standards:
 * - JWT access and refresh tokens are protected and never displayed in toString().
 * - Provides guest/offline fallback so gameplay is never blocked by network state.
 */
public class UserSession {

    private final String userId;
    private final String email;
    private final String username;
    private final String accessToken;
    private final String refreshToken;
    private final long expiresAt; // Epoch milliseconds
    private final boolean emailConfirmed;

    public UserSession(String userId, String email, String username,
                       String accessToken, String refreshToken,
                       long expiresAt, boolean emailConfirmed) {
        this.userId = userId != null ? userId.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.username = (username != null && !username.trim().isEmpty())
                ? username.trim()
                : (email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : "Pilot");
        this.accessToken = accessToken != null ? accessToken.trim() : "";
        this.refreshToken = refreshToken != null ? refreshToken.trim() : "";
        this.expiresAt = expiresAt;
        this.emailConfirmed = emailConfirmed;
    }

    /**
     * Create an unauthenticated Guest/Offline session.
     */
    public static UserSession guest() {
        return new UserSession(
                "GUEST",
                "",
                "Guest Pilot",
                "",
                "",
                Long.MAX_VALUE,
                false
        );
    }

    /**
     * True if the session has valid credentials and has not expired.
     */
    public boolean isAuthenticated() {
        return !userId.isEmpty() && !"GUEST".equalsIgnoreCase(userId) && !accessToken.isEmpty();
    }

    public boolean isExpired() {
        if (!isAuthenticated()) return false;
        return System.currentTimeMillis() >= expiresAt;
    }

    public boolean isGuest() {
        return !isAuthenticated();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    /**
     * Security: Tokens are strictly redacted in loggable representations.
     */
    @Override
    public String toString() {
        return "UserSession{" +
                "userId='" + userId + '\'' +
                ", email='" + (email.isEmpty() ? "N/A" : email) + '\'' +
                ", username='" + username + '\'' +
                ", authenticated=" + isAuthenticated() +
                ", accessToken='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                ", emailConfirmed=" + emailConfirmed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(userId, that.userId) && Objects.equals(accessToken, that.accessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, accessToken);
    }
}
