package vn.demo.employee.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.employee.dto.EmployeeRequestDto;
import vn.demo.employee.dto.EmployeeResponseDto;
import vn.demo.employee.dto.PagedResponse;
import vn.demo.mapping.EmployeeMapper;
import vn.demo.employee.model.Employee;
import vn.demo.employee.repository.EmployeeRepository;
import vn.demo.exception.ConflictException;
import vn.demo.exception.ResourceNotFoundException;

/**
 * Nghiệp vụ Employee — dùng <b>MapStruct</b> ở biên API (chuẩn nộp bài §2.3).
 *
 * <p>So sánh ModelMapper: xem {@link vn.demo.mapping.EmployeeModelMapperService}.</p>
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

	private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

	private final EmployeeRepository repository;
	private final EmployeeMapper mapper;

	/**
	 * Tìm kiếm + phân trang offset (ôn M3 {@code PagedResponse}).
	 *
	 * @param keyword lọc name/email chứa chuỗi (không phân biệt hoa thường); null = bỏ qua
	 * @param role    lọc đúng role; null/blank = bỏ qua
	 * @param page    trang 0-indexed
	 * @param size    kích thước trang
	 * @param sort    dạng {@code field,dir} — demo hỗ trợ {@code name,asc|desc}
	 */
	public PagedResponse<EmployeeResponseDto> search(
			String keyword, String role, int page, int size, String sort) {
		log.debug("Search keyword={} role={} page={} size={} sort={}", keyword, role, page, size, sort);

		// 1) Lấy toàn bộ rồi lọc in-memory (đủ cho lab; production filter ở DB)
		List<Employee> filtered = repository.findAll().stream()
				.filter(e -> matchKeyword(e, keyword))
				.filter(e -> matchRole(e, role))
				.collect(Collectors.toList());

		// 2) Sắp xếp theo tham số sort
		sortInPlace(filtered, sort);

		// 3) Cắt trang + metadata
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		int total = filtered.size();
		int from = Math.min(safePage * safeSize, total);
		int to = Math.min(from + safeSize, total);
		List<EmployeeResponseDto> content = filtered.subList(from, to).stream()
				.map(mapper::toDto)
				.toList();

		int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
		return new PagedResponse<>(
				content,
				safePage,
				safeSize,
				total,
				totalPages,
				safePage == 0,
				safePage >= totalPages - 1 || totalPages == 0);
	}

	public EmployeeResponseDto getById(Long id) {
		Employee emp = require(id);
		return mapper.toDto(emp);
	}

	public EmployeeResponseDto create(EmployeeRequestDto request) {
		log.debug("Creating employee with email={}", request.getEmail());

		// Cảnh báo nghiệp vụ: email trùng → 409 (log WARN)
		if (repository.existsByEmail(request.getEmail())) {
			log.warn("Duplicate email rejected: {}", request.getEmail());
			throw new ConflictException("Email already exists: " + request.getEmail());
		}

		try {
			// MapStruct: name/email/role; id + passwordHash ignore
			Employee emp = mapper.toEntity(request);
			// Hash demo — production dùng BCrypt (Module Auth)
			emp.setPasswordHash(demoHash(request.getPassword()));
			Employee saved = repository.save(emp);
			log.info("Employee created id={} email={}", saved.getId(), saved.getEmail());
			return mapper.toDto(saved);
		} catch (RuntimeException ex) {
			log.error("Failed to create employee email={}", request.getEmail(), ex);
			throw ex;
		}
	}

	public EmployeeResponseDto update(Long id, EmployeeRequestDto request) {
		Employee existing = require(id);

		if (repository.existsByEmailAndIdNot(request.getEmail(), id)) {
			log.warn("Duplicate email on update rejected: {}", request.getEmail());
			throw new ConflictException("Email already exists: " + request.getEmail());
		}

		// Ghi đè field cho phép; giữ nguyên id
		existing.setName(request.getName());
		existing.setEmail(request.getEmail());
		existing.setRole(request.getRole());
		existing.setPasswordHash(demoHash(request.getPassword()));

		Employee saved = repository.save(existing);
		log.info("Employee updated id={}", saved.getId());
		return mapper.toDto(saved);
	}

	public void delete(Long id) {
		require(id);
		repository.deleteById(id);
		log.info("Employee deleted id={}", id);
	}

	/** Demo hash — không dùng cho production. */
	private String demoHash(String rawPassword) {
		return "demo-hash:" + rawPassword;
	}

	private Employee require(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
	}

	private boolean matchKeyword(Employee e, String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return true;
		}
		String k = keyword.toLowerCase(Locale.ROOT);
		return e.getName().toLowerCase(Locale.ROOT).contains(k)
				|| e.getEmail().toLowerCase(Locale.ROOT).contains(k);
	}

	private boolean matchRole(Employee e, String role) {
		if (role == null || role.isBlank()) {
			return true;
		}
		return e.getRole().equalsIgnoreCase(role);
	}

	private void sortInPlace(List<Employee> list, String sort) {
		String safe = (sort == null || sort.isBlank()) ? "name,asc" : sort;
		String[] parts = safe.split(",", 2);
		String field = parts[0].trim();
		boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

		Comparator<Employee> cmp = switch (field) {
			case "email" -> Comparator.comparing(Employee::getEmail, String.CASE_INSENSITIVE_ORDER);
			case "role" -> Comparator.comparing(Employee::getRole, String.CASE_INSENSITIVE_ORDER);
			case "id" -> Comparator.comparing(Employee::getId);
			default -> Comparator.comparing(Employee::getName, String.CASE_INSENSITIVE_ORDER);
		};
		if (desc) {
			cmp = cmp.reversed();
		}
		list.sort(cmp);
	}

}
