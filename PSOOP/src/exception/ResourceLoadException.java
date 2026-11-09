package exception;

/**
 * Thrown when an internal or procedural game resource fails to generate or load properly.
 */
public class ResourceLoadException extends AirstrikeException {
    private static final long serialVersionUID = 1L;
    
    private final String resourceName;

    public ResourceLoadException(String message, String resourceName) {
        super(message + " [Resource: " + resourceName + "]");
        this.resourceName = resourceName;
    }

    public ResourceLoadException(String message, String resourceName, Throwable cause) {
        super(message + " [Resource: " + resourceName + "]", cause);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}

