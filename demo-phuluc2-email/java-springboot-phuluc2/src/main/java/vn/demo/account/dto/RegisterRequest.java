package vn.demo.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO — body đăng ký (syllabus §5).
 *
 * <p>Controller nhận DTO, không nhận Entity trực tiếp (convention module-3/4).</p>
 */
@Getter
@Setter
public class RegisterRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 2, max = 80)
	private String displayName;

	@NotBlank
	@Size(min = 6, max = 100)
	private String password;

}
