package vn.demo.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.model.PermissionModel;
import vn.demo.model.RoleModel;
import vn.demo.repository.PermissionRepository;
import vn.demo.repository.RoleRepository;

/**
 * SERVICE — nạp Permission codes từ {@code roleIds} (Mongo).
 * Syllabus: <b>Phần 3 — Tính năng 3</b> / Phần 2.4.
 *
 * <p>Gọi trên <b>mỗi request</b> có Bearer hợp lệ — không đọc quyền từ JWT.</p>
 */
@Service
@RequiredArgsConstructor
public class PermissionLoader {

	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;

	/**
	 * @return tập {@code permission.code} (vd. USER_VIEW) — thứ tự ổn định để debug
	 */
	public Set<String> loadPermissionCodes(List<String> roleIds) {
		Set<String> codes = new LinkedHashSet<>();
		if (roleIds == null || roleIds.isEmpty()) {
			return codes;
		}

		// --- 1) Load roles theo id ---
		List<RoleModel> roles = roleRepository.findByIdIn(roleIds);
		Set<String> permissionIds = new LinkedHashSet<>();
		for (RoleModel role : roles) {
			if (role.getPermissionIds() != null) {
				permissionIds.addAll(role.getPermissionIds());
			}
		}
		if (permissionIds.isEmpty()) {
			return codes;
		}

		// --- 2) Load permissions → lấy code ---
		for (PermissionModel p : permissionRepository.findByIdIn(permissionIds)) {
			codes.add(p.getCode());
		}
		return codes;
	}

	/** Tên role (ADMIN, EDITOR, …) để trả /me — không dùng để @PreAuthorize. */
	public List<String> loadRoleCodes(List<String> roleIds) {
		if (roleIds == null || roleIds.isEmpty()) {
			return List.of();
		}
		return roleRepository.findByIdIn(roleIds).stream()
				.map(RoleModel::getCode)
				.toList();
	}

}
