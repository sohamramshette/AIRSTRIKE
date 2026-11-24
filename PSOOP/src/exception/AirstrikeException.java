// ==============================================================================
// File      : PSOOP/src/exception/AirstrikeException.java
// Subject   : PSOOP-CO3 | Custom Checked Exceptions
// Topics    : [TOPIC: Custom Exception] [TOPIC: Checked Exception] [TOPIC: Inheritance]
// Game Role : Defines the root checked exception type for recoverable AIRSTRIKE domain errors.
// ==============================================================================
package exception;

/**
 * Base checked exception for domain-specific errors in the AIRSTRIKE game system.
 */
public class AirstrikeException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /** [TOPIC: Custom Exception] Game Role: Wraps a domain error message for checked engine recovery. */
    public AirstrikeException(String message) {
        super(message);
    }

    /** [TOPIC: Exception Chaining] Game Role: Preserves root-cause context when wrapping lower-level failures. */
    public AirstrikeException(String message, Throwable cause) {
        super(message, cause);
    }
}

