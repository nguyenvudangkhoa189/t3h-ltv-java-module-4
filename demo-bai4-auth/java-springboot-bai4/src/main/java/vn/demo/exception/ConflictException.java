package vn.demo.exception;

/** Trùng username / email → 409. */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}

}
