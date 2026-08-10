package vn.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** DTO — cập nhật user (field null = giữ nguyên). */
public record UpdateUserRequest(
		@Email String email,
		Boolean enabled,
		@Size(min = 6, max = 100) String password) {
}
