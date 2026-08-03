package vn.demo.exception;

import vn.demo.employee.controller.EmployeeRestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Bắt lỗi tập trung cho REST (ôn Module 3 Bài 3 §8).
 *
 * <p>Không viết try/catch lặp trong từng handler — map exception → status + JSON.
 * Package {@code exception} dùng chung; advice gắn với {@link EmployeeRestController}.</p>
 */
@RestControllerAdvice(basePackageClasses = EmployeeRestController.class)
public class RestExceptionHandler {

	/** Không tìm thấy → 404. */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/** Trùng email / xung đột → 409. */
	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	/** {@code @Valid} thất bại → 400 + chi tiết từng field. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");
		Map<String, String> fields = new HashMap<>();
		// Gom message theo tên field để frontend hiển thị
		ex.getBindingResult().getFieldErrors()
				.forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
		body.put("fields", fields);
		return ResponseEntity.badRequest().body(body);
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(baseBody(status, message));
	}

	/** Khung JSON lỗi chung: timestamp, status, error, message. */
	private Map<String, Object> baseBody(HttpStatus status, String message) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return body;
	}

}
