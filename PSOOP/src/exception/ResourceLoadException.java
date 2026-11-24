// ==============================================================================
// File      : PSOOP/src/exception/ResourceLoadException.java
// Subject   : PSOOP-CO3 | Resource Failure Exception
// Topics    : [TOPIC: Custom Exception] [TOPIC: Exception Chaining] [TOPIC: Defensive Programming]
// Game Role : Reports asset initialization or lookup failures so procedural fallbacks can recover.
// ==============================================================================
package exception;

/**
 * Thrown when an internal or procedural game resource fails to generate or load properly.
 */
public class ResourceLoadException extends AirstrikeException {
    private static final long serialVersionUID = 1L;
    
    private final String resourceName;

    /** [TOPIC: Custom Exception] Game Role: Identifies the failing asset key while allowing graceful fallback. */
    public ResourceLoadException(String message, String resourceName) {
        // WHY: Tags the failing asset identifier into the base message for rapid diagnostics.
        super(message + " [Resource: " + resourceName + "]");
        this.resourceName = resourceName;
    }

    /** [TOPIC: Exception Chaining] Game Role: Preserves the underlying cause alongside the failing asset name. */
    public ResourceLoadException(String message, String resourceName, Throwable cause) {
        super(message + " [Resource: " + resourceName + "]", cause);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}

