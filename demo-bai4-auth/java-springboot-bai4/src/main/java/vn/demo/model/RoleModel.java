package vn.demo.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * MODEL — Role nhóm Permission (collection {@code roles}).
 * Syllabus: <b>Phần 3 — Tính năng 2</b> / Phần 2 — Role chỉ là tập Permission.
 *
 * <p>{@code code}: ADMIN / EDITOR / USER — dùng khi gán role, <b>không</b> dùng
 * {@code hasRole(code)} trên Controller.</p>
 */
@Data
@Document(collection = "roles")
public class RoleModel {

	@Id
	private String id;

	@Indexed(unique = true)
	private String code;

	private String name;
	private String description = "";

	/** Reference → {@code permissions._id}. */
	private List<String> permissionIds = new ArrayList<>();

}
