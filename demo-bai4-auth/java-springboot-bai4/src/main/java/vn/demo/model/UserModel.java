package vn.demo.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * MODEL — tài khoản đăng nhập (collection {@code users}).
 * Syllabus: <b>Phần 3 — Tính năng 2</b>.
 *
 * <p>{@code roleIds} reference → {@code roles._id}. Permission không embed vào user —
 * nạp qua Role mỗi request (Phần 2).</p>
 *
 * <p>{@code password} luôn là BCrypt hash (ôn M3 Bài 10 §4.4).</p>
 */
@Data
@Document(collection = "users")
public class UserModel {

	@Id
	private String id;

	@Indexed(unique = true)
	private String username;

	@Indexed(unique = true)
	private String email;

	/** BCrypt hash — không lưu plain text. */
	private String password;

	private boolean enabled = true;

	/** Reference → {@code roles._id}. */
	private List<String> roleIds = new ArrayList<>();

	private Instant createdAt;
	private Instant updatedAt;

}
