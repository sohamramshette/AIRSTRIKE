package service;

import database.UserSession;

/**
 * Encapsulates the outcome of Supabase authentication operations in AIRSTRIKE.
 */
public class AuthResult {

    public enum Status {
        SUCCESS,
        EMAIL_CONFIRMATION_PENDING,
        INVALID_CREDENTIALS,
        USER_ALREADY_EXISTS,
        WEAK_PASSWORD,
        CONFIGURATION_MISSING,
        NETWORK_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final String message;
    private final UserSession session;
    private final int httpStatusCode;

    public AuthResult(Status status, String message, UserSession session, int httpStatusCode) {
        this.status = status;
        this.message = message != null ? message : "";
        this.session = session;
        this.httpStatusCode = httpStatusCode;
    }

    public static AuthResult success(String message, UserSession session) {
        return new AuthResult(Status.SUCCESS, message, session, 200);
    }

    public static AuthResult confirmationPending(String message) {
        return new AuthResult(Status.EMAIL_CONFIRMATION_PENDING, message, null, 200);
    }

    public static AuthResult invalidCredentials(String message) {
        return new AuthResult(Status.INVALID_CREDENTIALS, message, null, 400);
    }

    public static AuthResult userAlreadyExists(String message) {
        return new AuthResult(Status.USER_ALREADY_EXISTS, message, null, 422);
    }

    public static AuthResult configurationMissing(String message) {
        return new AuthResult(Status.CONFIGURATION_MISSING, message, null, 0);
    }

    public static AuthResult networkError(String message) {
        return new AuthResult(Status.NETWORK_ERROR, message, null, 0);
    }

    public static AuthResult error(Status status, String message, int httpStatusCode) {
        return new AuthResult(status, message, null, httpStatusCode);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UserSession getSession() {
        return session;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isConfirmationPending() {
        return status == Status.EMAIL_CONFIRMATION_PENDING;
    }

    @Override
    public String toString() {
        return "AuthResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", session=" + session +
                '}';
    }
}

