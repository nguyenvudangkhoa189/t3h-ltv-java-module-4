package vn.demo.dto;

import java.time.Instant;
import java.util.List;

/** DTO — response user (không lộ password). */
public record UserResponse(
		String id,
		String username,
		String email,
		boolean enabled,
		List<String> roles,
		Instant createdAt,
		Instant updatedAt) {
}
