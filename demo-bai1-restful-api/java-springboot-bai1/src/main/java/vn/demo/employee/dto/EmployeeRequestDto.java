package vn.demo.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body tạo / cập nhật employee — nhận từ client.
 *
 * <p>{@code password} chỉ dùng khi ghi; Service hash rồi gắn {@code passwordHash} trên Model.
 * Không map password thẳng sang Response.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EmployeeRequest", description = "Body tạo / cập nhật employee")
public class EmployeeRequestDto {

	@Schema(description = "Họ tên", example = "Nguyen Van A")
	@NotBlank(message = "name không được trống")
	private String name;

	@Schema(example = "a@company.com")
	@NotBlank(message = "email không được trống")
	@Email(message = "email không đúng định dạng")
	private String email;

	@Schema(example = "developer", allowableValues = { "developer", "manager", "hr" })
	@NotBlank(message = "role không được trống")
	private String role;

	@Schema(description = "Mật khẩu dạng plain text — chỉ khi tạo/cập nhật", example = "Secret123!")
	@NotBlank(message = "password không được trống")
	private String password;

}
