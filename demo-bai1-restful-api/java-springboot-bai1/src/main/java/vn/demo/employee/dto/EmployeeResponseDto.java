package vn.demo.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về client — chỉ field an toàn (không có password / passwordHash).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EmployeeResponse")
public class EmployeeResponseDto {

	@Schema(example = "1")
	private Long id;
	private String name;
	private String email;
	private String role;

}
