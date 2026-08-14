package vn.demo.exception;

/**
 * Unauthorized Exception
 * 
 * Thrown when authentication fails or access token is invalid.
 * Results in HTTP 401 Unauthorized response for security violations.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}