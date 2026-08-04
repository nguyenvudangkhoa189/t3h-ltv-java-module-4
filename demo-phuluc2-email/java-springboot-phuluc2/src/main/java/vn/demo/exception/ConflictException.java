package vn.demo.exception;

/**
 * Ném khi xung đột nghiệp vụ (vd. trùng email) — map sang HTTP 409.
 */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}

}
