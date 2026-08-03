package vn.demo.exception;

/**
 * Xung đột nghiệp vụ (vd. email trùng) — map thành HTTP 409.
 */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}

}
