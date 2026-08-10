package vn.demo.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.PermissionModel;
import vn.demo.model.RoleModel;
import vn.demo.model.UserModel;
import vn.demo.repository.PermissionRepository;
import vn.demo.repository.RoleRepository;
import vn.demo.repository.UserRepository;

/**
 * CONFIG — seed Permission → Role → 3 user mẫu (lần đầu chạy app).
 * Syllabus: <b>Phần 3 — Tính năng 2</b>.
 *
 * <p>Idempotent: đã có username {@code admin} thì bỏ qua.</p>
 *
 * <h2>Ma trận (syllabus §3.0.3)</h2>
 * <ul>
 *   <li>ADMIN — USER_VIEW/CREATE/UPDATE/DELETE + USER_ASSIGN_ROLE</li>
 *   <li>EDITOR — VIEW + CREATE + UPDATE</li>
 *   <li>USER — chỉ VIEW</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacDataSeeder implements ApplicationRunner {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(ApplicationArguments args) {
		// --- Đã seed rồi → bỏ qua (idempotent) ---
		if (userRepository.existsByUsername("admin")) {
			log.info("RbacDataSeeder: data already present — skip");
			return;
		}

		log.info("RbacDataSeeder: seeding permissions, roles, users…");

		// --- 1) Permissions ---
		Map<String, PermissionModel> perms = new LinkedHashMap<>();
		perms.put("USER_VIEW", savePermission("USER_VIEW", "View users"));
		perms.put("USER_CREATE", savePermission("USER_CREATE", "Create user"));
		perms.put("USER_UPDATE", savePermission("USER_UPDATE", "Update user"));
		perms.put("USER_DELETE", savePermission("USER_DELETE", "Delete user"));
		perms.put("USER_ASSIGN_ROLE", savePermission("USER_ASSIGN_ROLE", "Assign or remove role"));

		// --- 2) Roles (permissionIds) ---
		RoleModel roleAdmin = saveRole("ADMIN", "Administrator", List.of(
				perms.get("USER_VIEW"),
				perms.get("USER_CREATE"),
				perms.get("USER_UPDATE"),
				perms.get("USER_DELETE"),
				perms.get("USER_ASSIGN_ROLE")));
		RoleModel roleEditor = saveRole("EDITOR", "Editor", List.of(
				perms.get("USER_VIEW"),
				perms.get("USER_CREATE"),
				perms.get("USER_UPDATE")));
		RoleModel roleUser = saveRole("USER", "User", List.of(
				perms.get("USER_VIEW")));

		// --- 3) Users (BCrypt) ---
		saveUser("admin", "admin@demo.local", "admin123", List.of(roleAdmin.getId()));
		saveUser("editor", "editor@demo.local", "editor123", List.of(roleEditor.getId()));
		saveUser("alice", "alice@demo.local", "user123", List.of(roleUser.getId()));

		log.info("RbacDataSeeder: done. Logins: admin/admin123, editor/editor123, alice/user123");
	}

	private PermissionModel savePermission(String code, String name) {
		PermissionModel p = new PermissionModel();
		p.setCode(code);
		p.setName(name);
		p.setDescription("");
		return permissionRepository.save(p);
	}

	private RoleModel saveRole(String code, String name, List<PermissionModel> permissions) {
		RoleModel r = new RoleModel();
		r.setCode(code);
		r.setName(name);
		r.setDescription("");
		List<String> ids = new ArrayList<>();
		for (PermissionModel p : permissions) {
			ids.add(p.getId());
		}
		r.setPermissionIds(ids);
		return roleRepository.save(r);
	}

	private void saveUser(String username, String email, String rawPassword, List<String> roleIds) {
		UserModel u = new UserModel();
		u.setUsername(username);
		u.setEmail(email);
		// --- Chỉ encode một lần; login dùng matches(raw, hashTrongDb) ---
		u.setPassword(passwordEncoder.encode(rawPassword));
		u.setEnabled(true);
		u.setRoleIds(roleIds);
		Instant now = Instant.now();
		u.setCreatedAt(now);
		u.setUpdatedAt(now);
		userRepository.save(u);
	}

}
