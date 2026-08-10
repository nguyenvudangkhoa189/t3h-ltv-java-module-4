package vn.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * MODEL — đơn vị kiểm tra quyền (collection {@code permissions}).
 * Syllabus: <b>Phần 3 — Tính năng 2</b> / Phần 2.
 *
 * <p>{@code code} (vd. {@code USER_CREATE}) → {@code SimpleGrantedAuthority(code)}
 * → {@code @PreAuthorize("hasAuthority('USER_CREATE')")}.</p>
 */
@Data
@Document(collection = "permissions")
public class PermissionModel {

	@Id
	private String id;

	@Indexed(unique = true)
	private String code;

	private String name;
	private String description = "";

}
