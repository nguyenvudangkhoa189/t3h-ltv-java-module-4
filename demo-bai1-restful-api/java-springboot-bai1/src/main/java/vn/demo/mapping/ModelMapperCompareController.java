package vn.demo.mapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.employee.dto.EmployeeRequestDto;
import vn.demo.employee.dto.EmployeeResponseDto;

/**
 * Endpoint phụ — chỉ để lab §2.2 thử ModelMapper cạnh MapStruct.
 *
 * <p>Nộp bài / API chính vẫn dùng {@link vn.demo.employee.controller.EmployeeRestController} + MapStruct.</p>
 */
@RestController
@RequestMapping("/api/v1/employees/compare/model-mapper")
@RequiredArgsConstructor
@Tag(name = "Employees — ModelMapper compare", description = "Lab §2.2 — không dùng cho API chính")
public class ModelMapperCompareController {

	private final EmployeeModelMapperService modelMapperService;

	@Operation(summary = "GET by id bằng ModelMapper")
	@GetMapping("/{id}")
	public EmployeeResponseDto getById(@PathVariable Long id) {
		return modelMapperService.getById(id);
	}

	@Operation(summary = "POST create bằng ModelMapper")
	@PostMapping
	public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto request) {
		EmployeeResponseDto created = modelMapperService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

}
