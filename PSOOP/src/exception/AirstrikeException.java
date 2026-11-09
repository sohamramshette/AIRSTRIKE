package exception;

/**
 * Base checked exception for domain-specific errors in the AIRSTRIKE game system.
 */
public class AirstrikeException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public AirstrikeException(String message) {
        super(message);
    }

    public AirstrikeException(String message, Throwable cause) {
        super(message, cause);
    }
}

