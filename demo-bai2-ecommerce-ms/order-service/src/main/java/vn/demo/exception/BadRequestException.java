package vn.demo.exception;

/**
 * Bad Request Exception
 * 
 * Thrown when client sends invalid request data or violates business rules.
 * Results in HTTP 400 Bad Request response with descriptive error message.
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}