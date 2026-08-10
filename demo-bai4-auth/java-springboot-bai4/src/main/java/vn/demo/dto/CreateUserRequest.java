package vn.demo.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO — tạo user. Syllabus tính năng 5. */
public record CreateUserRequest(
		@NotBlank @Size(min = 3, max = 50) String username,
		@NotBlank @Email String email,
		@NotBlank @Size(min = 6, max = 100) String password,
		/** Role codes tùy chọn (vd. USER). Mặc định USER nếu null/empty. */
		List<String> roleCodes) {
}
