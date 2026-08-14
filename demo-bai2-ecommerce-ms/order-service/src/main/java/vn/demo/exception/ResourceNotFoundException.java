package vn.demo.exception;

/**
 * Resource Not Found Exception
 * 
 * Thrown when requested order or other resource cannot be found.
 * Results in HTTP 404 Not Found response with descriptive error message.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id));
    }
}