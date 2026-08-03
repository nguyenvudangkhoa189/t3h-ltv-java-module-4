package vn.demo.mapping;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.employee.dto.EmployeeRequestDto;
import vn.demo.employee.dto.EmployeeResponseDto;
import vn.demo.employee.model.Employee;
import vn.demo.employee.repository.EmployeeRepository;
import vn.demo.exception.ConflictException;
import vn.demo.exception.ResourceNotFoundException;

/**
 * Bản map bằng <b>ModelMapper</b> (§2.2) — chỉ để so sánh với {@link vn.demo.employee.service.EmployeeService} (MapStruct).
 *
 * <p>API chính <b>không</b> dùng class này. Endpoint demo:
 * {@code GET/POST /api/v1/employees/compare/model-mapper/...}</p>
 *
 * <p><b>Khác MapStruct:</b> map bằng reflection lúc <b>runtime</b>; đổi tên field lệch
 * dễ ra null khi chạy, compile vẫn pass.</p>
 */
@Service
@RequiredArgsConstructor
public class EmployeeModelMapperService {

	private static final Logger log = LoggerFactory.getLogger(EmployeeModelMapperService.class);

	private final EmployeeRepository repository;
	private final ModelMapper modelMapper;

	public EmployeeResponseDto getById(Long id) {
		Employee emp = repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
		// Cùng tên field (id, name, email, role) → tự gán; passwordHash không có trên DTO
		return modelMapper.map(emp, EmployeeResponseDto.class);
	}

	public EmployeeResponseDto create(EmployeeRequestDto request) {
		if (repository.existsByEmail(request.getEmail())) {
			throw new ConflictException("Email already exists: " + request.getEmail());
		}

		Employee emp = modelMapper.map(request, Employee.class);
		// password trên request không phải passwordHash — set hash thủ công
		emp.setId(null);
		emp.setPasswordHash("demo-hash:" + request.getPassword());

		Employee saved = repository.save(emp);
		log.info("[ModelMapper] Employee created id={}", saved.getId());
		return modelMapper.map(saved, EmployeeResponseDto.class);
	}

}
