package vn.demo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.CreateUserRequest;
import vn.demo.dto.UpdateUserRequest;
import vn.demo.dto.UserResponse;
import vn.demo.exception.ConflictException;
import vn.demo.exception.NotFoundException;
import vn.demo.model.RoleModel;
import vn.demo.model.UserModel;
import vn.demo.repository.RoleRepository;
import vn.demo.repository.UserRepository;

/**
 * SERVICE — CRUD User + gán / gỡ role.
 * Syllabus: <b>Phần 3 — Tính năng 5</b>.
 *
 * <p>Phân quyền API nằm ở {@code @PreAuthorize} trên Controller — service không
 * hardcode tên role để cho phép/từ chối gọi API.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PermissionLoader permissionLoader;
	private final PasswordEncoder passwordEncoder;

	public List<UserResponse> findAll() {
		// --- Map toàn bộ user → DTO (kèm role codes) ---
		return userRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	public UserResponse findById(String id) {
		return toResponse(requireUser(id));
	}

	public UserResponse create(CreateUserRequest request) {
		// --- 1) Trùng username / email → 409 ---
		if (userRepository.existsByUsername(request.username())) {
			throw new ConflictException("Username already exists");
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email already exists");
		}

		// --- 2) Resolve role codes (mặc định USER) ---
		List<String> roleCodes = (request.roleCodes() == null || request.roleCodes().isEmpty())
				? List.of("USER")
				: request.roleCodes();
		List<String> roleIds = resolveRoleIds(roleCodes);

		// --- 3) Lưu user (BCrypt) ---
		UserModel user = new UserModel();
		user.setUsername(request.username());
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setEnabled(true);
		user.setRoleIds(roleIds);
		Instant now = Instant.now();
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		return toResponse(userRepository.save(user));
	}

	public UserResponse update(String id, UpdateUserRequest request) {
		UserModel user = requireUser(id);

		// --- Cập nhật từng field nếu client gửi ---
		if (request.email() != null && !request.email().isBlank()) {
			userRepository.findByEmail(request.email())
					.filter(u -> !u.getId().equals(id))
					.ifPresent(u -> {
						throw new ConflictException("Email already exists");
					});
			user.setEmail(request.email());
		}
		if (request.enabled() != null) {
			user.setEnabled(request.enabled());
		}
		if (request.password() != null && !request.password().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.password()));
		}
		user.setUpdatedAt(Instant.now());
		return toResponse(userRepository.save(user));
	}

	public void delete(String id) {
		// --- Xóa; không thấy → 404 ---
		if (!userRepository.existsById(id)) {
			throw new NotFoundException("User not found: " + id);
		}
		userRepository.deleteById(id);
	}

	public UserResponse assignRole(String userId, String roleCode) {
		UserModel user = requireUser(userId);
		RoleModel role = roleRepository.findByCode(roleCode.toUpperCase())
				.orElseThrow(() -> new NotFoundException("Role not found: " + roleCode));

		// --- Idempotent: đã có role thì giữ nguyên ---
		List<String> roleIds = new ArrayList<>(user.getRoleIds());
		if (!roleIds.contains(role.getId())) {
			roleIds.add(role.getId());
			user.setRoleIds(roleIds);
			user.setUpdatedAt(Instant.now());
			user = userRepository.save(user);
		}
		return toResponse(user);
	}

	public UserResponse removeRole(String userId, String roleCode) {
		UserModel user = requireUser(userId);
		RoleModel role = roleRepository.findByCode(roleCode.toUpperCase())
				.orElseThrow(() -> new NotFoundException("Role not found: " + roleCode));

		List<String> roleIds = new ArrayList<>(user.getRoleIds());
		if (roleIds.remove(role.getId())) {
			user.setRoleIds(roleIds);
			user.setUpdatedAt(Instant.now());
			user = userRepository.save(user);
		}
		return toResponse(user);
	}

	private UserModel requireUser(String id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("User not found: " + id));
	}

	private List<String> resolveRoleIds(List<String> roleCodes) {
		List<String> ids = new ArrayList<>();
		for (String code : roleCodes) {
			RoleModel role = roleRepository.findByCode(code.toUpperCase())
					.orElseThrow(() -> new NotFoundException("Role not found: " + code));
			ids.add(role.getId());
		}
		return ids;
	}

	private UserResponse toResponse(UserModel user) {
		List<String> roles = permissionLoader.loadRoleCodes(user.getRoleIds());
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.isEnabled(),
				roles,
				user.getCreatedAt(),
				user.getUpdatedAt());
	}

}
