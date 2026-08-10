package vn.demo.dto;

import java.util.List;

/**
 * DTO — identity + quyền hiện tại (load từ DB qua filter).
 * Syllabus: tính năng 4 — tiện đối chiếu {@code @PreAuthorize}.
 */
public record MeResponse(
		String id,
		String username,
		String email,
		List<String> roles,
		List<String> permissions) {
}
