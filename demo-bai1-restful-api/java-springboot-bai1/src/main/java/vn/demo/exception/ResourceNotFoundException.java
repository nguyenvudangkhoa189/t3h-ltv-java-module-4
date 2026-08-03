package vn.demo.exception;

/**
 * Ném khi không tìm thấy resource — {@code RestExceptionHandler} map thành HTTP 404.
 * (Pattern giống Module 3 Bài 3.)
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
