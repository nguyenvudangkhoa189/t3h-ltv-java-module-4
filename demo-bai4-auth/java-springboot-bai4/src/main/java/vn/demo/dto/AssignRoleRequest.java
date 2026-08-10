package vn.demo.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO — gán role cho user. */
public record AssignRoleRequest(
		@NotBlank String roleCode) {
}
