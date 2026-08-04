package vn.demo.exception;

/**
 * Ném khi không tìm thấy resource — map sang HTTP 404 qua {@link RestExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
