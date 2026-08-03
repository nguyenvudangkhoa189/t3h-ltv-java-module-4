package vn.demo.employee.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.employee.dto.EmployeeRequestDto;
import vn.demo.employee.dto.EmployeeResponseDto;
import vn.demo.employee.dto.PagedResponse;
import vn.demo.employee.service.EmployeeService;
import vn.demo.async.WelcomeEmailService;

/**
 * REST Employee dưới prefix {@code /api/v1} — chuẩn versioning §1 + OpenAPI §5.
 *
 * <p>CRUD / search / page dùng {@link EmployeeService} (MapStruct).
 * So sánh ModelMapper: {@link vn.demo.mapping.ModelMapperCompareController}.</p>
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Quản lý nhân viên — CRUD, tìm kiếm, phân trang, @Async welcome")
public class EmployeeRestController {

	private static final Logger log = LoggerFactory.getLogger(EmployeeRestController.class);

	private final EmployeeService employeeService;
	private final WelcomeEmailService welcomeEmailService;

	@Operation(
			summary = "Tìm kiếm & phân trang employee",
			description = "Lọc keyword (name/email), role (optional). page bắt đầu từ 0.")
	@ApiResponse(responseCode = "200", description = "Danh sách kèm metadata phân trang")
	@GetMapping
	public PagedResponse<EmployeeResponseDto> searchAndPage(
			@Parameter(description = "Từ khóa name/email", example = "nguyen")
			@RequestParam(required = false) String keyword,
			@Parameter(description = "Lọc role", example = "developer")
			@RequestParam(required = false) String role,
			@Parameter(description = "Trang (0-indexed)", example = "0")
			@RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Số phần tử mỗi trang", example = "10")
			@RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Sắp xếp, vd. name,asc", example = "name,asc")
			@RequestParam(defaultValue = "name,asc") String sort) {
		return employeeService.search(keyword, role, page, size, sort);
	}

	@Operation(summary = "Lấy employee theo id")
	@ApiResponse(responseCode = "200", description = "Tìm thấy")
	@ApiResponse(responseCode = "404", description = "Không tồn tại")
	@GetMapping("/{id}")
	public EmployeeResponseDto getById(
			@Parameter(description = "ID employee", example = "1")
			@PathVariable Long id) {
		return employeeService.getById(id);
	}

	@Operation(summary = "Tạo employee mới")
	@ApiResponse(responseCode = "201", description = "Tạo thành công")
	@ApiResponse(responseCode = "400", description = "Validation thất bại")
	@ApiResponse(responseCode = "409", description = "Email trùng")
	@PostMapping
	public ResponseEntity<EmployeeResponseDto> create(
			@Valid @RequestBody EmployeeRequestDto request) {
		EmployeeResponseDto created = employeeService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@Operation(summary = "Cập nhật toàn bộ (PUT)")
	@ApiResponse(responseCode = "200", description = "Cập nhật thành công")
	@ApiResponse(responseCode = "404", description = "Không tồn tại")
	@PutMapping("/{id}")
	public EmployeeResponseDto update(
			@PathVariable Long id,
			@Valid @RequestBody EmployeeRequestDto request) {
		return employeeService.update(id, request);
	}

	@Operation(summary = "Xóa employee")
	@ApiResponse(responseCode = "204", description = "Đã xóa")
	@ApiResponse(responseCode = "404", description = "Không tồn tại")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		employeeService.delete(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Lab §3 — bản sync (cố ý chậm): client phải chờ hết sleep mới nhận 200.
	 * Dùng để so với {@link #welcome(Long)}.
	 */
	@Operation(
			summary = "[Lab so sánh] Gửi email ĐỒNG BỘ (chậm)",
			description = "Thread HTTP bị block ~3s — Postman sẽ chờ. Không dùng production.")
	@ApiResponse(responseCode = "200", description = "Email giả lập đã xong (đã chờ)")
	@PostMapping("/{id}/welcome-sync")
	public ResponseEntity<Map<String, String>> welcomeSync(@PathVariable Long id)
			throws InterruptedException {
		EmployeeResponseDto emp = employeeService.getById(id);
		log.info("[{}] welcome-sync blocking for {}",
				Thread.currentThread().getName(), emp.getEmail());
		// Cố ý block thread HTTP — HV đo thời gian response
		Thread.sleep(3000);
		log.info("[{}] welcome-sync done for {}",
				Thread.currentThread().getName(), emp.getEmail());
		return ResponseEntity.ok(Map.of(
				"message", "Email sent (sync — you waited ~3s)",
				"employeeId", String.valueOf(id)));
	}

	/**
	 * Lab §3 — {@code @Async}: trả 202 ngay; email chạy trên pool {@code async-*}.
	 */
	@Operation(
			summary = "Gửi email chào (xử lý nền @Async)",
			description = "Trả 202 ngay; xem log server thread async-* sau vài giây")
	@ApiResponse(responseCode = "202", description = "Đã nhận, đang xử lý")
	@ApiResponse(responseCode = "404", description = "Không tồn tại")
	@PostMapping("/{id}/welcome")
	public ResponseEntity<Map<String, String>> welcome(@PathVariable Long id) {
		EmployeeResponseDto emp = employeeService.getById(id);
		// Chỉ schedule — không await kết quả email
		welcomeEmailService.sendWelcomeEmail(emp.getEmail());
		return ResponseEntity.accepted()
				.body(Map.of(
						"message", "Email is being processed",
						"employeeId", String.valueOf(id)));
	}

}
