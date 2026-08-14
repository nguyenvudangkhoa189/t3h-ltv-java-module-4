package vn.demo.exception;

/**
 * External Service Exception
 * 
 * Thrown when communication with external services fails.
 * Used for product service and notification service integration errors.
 */
public class ExternalServiceException extends RuntimeException {
    
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}