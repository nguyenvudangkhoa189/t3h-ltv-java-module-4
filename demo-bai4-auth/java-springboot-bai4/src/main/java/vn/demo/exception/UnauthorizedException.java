package vn.demo.exception;

/** Login sai / chưa xác thực ở tầng Service → 401. */
public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException(String message) {
		super(message);
	}

}
