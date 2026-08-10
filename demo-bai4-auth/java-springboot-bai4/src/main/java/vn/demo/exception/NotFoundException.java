package vn.demo.exception;

/** Không tìm thấy resource → 404. */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}

}
