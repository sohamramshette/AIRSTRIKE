package exception;

/**
 * Thrown when an illegal state transition is attempted within the game engine.
 */
public class InvalidGameStateException extends AirstrikeException {
    
    private final String attemptedTransition;

    public InvalidGameStateException(String message, String attemptedTransition) {
        super(message + " [Attempted: " + attemptedTransition + "]");
        this.attemptedTransition = attemptedTransition;
    }

    public String getAttemptedTransition() {
        return attemptedTransition;
    }
}

