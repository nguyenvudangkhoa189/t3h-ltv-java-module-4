package vn.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.dto.AssignRoleRequest;
import vn.demo.dto.CreateUserRequest;
import vn.demo.dto.UpdateUserRequest;
import vn.demo.dto.UserResponse;
import vn.demo.service.UserService;

/**
 * CONTROLLER — User management API.
 * Syllabus: <b>Phần 3 — Tính năng 5</b>.
 *
 * <p>Chỉ kiểm tra <b>Permission</b> qua {@code hasAuthority} — không {@code hasRole}.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasAuthority('USER_VIEW')")
	public List<UserResponse> list() {
		// --- Cần USER_VIEW (ADMIN / EDITOR / USER đều có) ---
		return userService.findAll();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('USER_VIEW')")
	public UserResponse get(@PathVariable String id) {
		return userService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('USER_CREATE')")
	public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
		// --- ADMIN + EDITOR ---
		return userService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('USER_UPDATE')")
	public UserResponse update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request) {
		// --- ADMIN + EDITOR ---
		return userService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('USER_DELETE')")
	public void delete(@PathVariable String id) {
		// --- Chỉ ADMIN (EDITOR / USER → 403) ---
		userService.delete(id);
	}

	@PostMapping("/{id}/roles")
	@PreAuthorize("hasAuthority('USER_ASSIGN_ROLE')")
	public UserResponse assignRole(
			@PathVariable String id,
			@Valid @RequestBody AssignRoleRequest request) {
		// --- Chỉ ADMIN ---
		return userService.assignRole(id, request.roleCode());
	}

	@DeleteMapping("/{id}/roles/{roleCode}")
	@PreAuthorize("hasAuthority('USER_ASSIGN_ROLE')")
	public UserResponse removeRole(@PathVariable String id, @PathVariable String roleCode) {
		// --- Chỉ ADMIN ---
		return userService.removeRole(id, roleCode);
	}

}
