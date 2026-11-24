// ==============================================================================
// File      : PSOOP/src/exception/InvalidGameStateException.java
// Subject   : PSOOP-CO3 | State Transition Guard Exceptions
// Topics    : [TOPIC: Custom Exception] [TOPIC: FSM State Pattern] [TOPIC: Defensive Programming]
// Game Role : Signals illegal game-state transitions before they corrupt active loop state.
// ==============================================================================
package exception;

/**
 * Thrown when an illegal state transition is attempted within the game engine.
 */
public class InvalidGameStateException extends AirstrikeException {
    private static final long serialVersionUID = 1L;
    
    private final String attemptedTransition;

    /** [TOPIC: Defensive Programming] Game Role: Captures the disallowed transition name for diagnostics and safe recovery. */
    public InvalidGameStateException(String message, String attemptedTransition) {
        // WHY: Embeds attempted transition directly in the exception message so caller logs pinpoint the illegal edge.
        super(message + " [Attempted: " + attemptedTransition + "]");
        this.attemptedTransition = attemptedTransition;
    }

    public String getAttemptedTransition() {
        return attemptedTransition;
    }
}

