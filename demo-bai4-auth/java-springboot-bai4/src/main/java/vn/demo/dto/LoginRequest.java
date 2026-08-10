package vn.demo.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO — đăng nhập. Syllabus tính năng 4. */
public record LoginRequest(
		@NotBlank String username,
		@NotBlank String password) {
}
