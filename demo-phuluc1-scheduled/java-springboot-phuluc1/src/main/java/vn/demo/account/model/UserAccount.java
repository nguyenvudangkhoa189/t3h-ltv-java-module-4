package vn.demo.account.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL — tài khoản người dùng (anemic: field + getter/setter).
 *
 * <p>Status dùng trong demo (syllabus §5):</p>
 * <ul>
 *   <li>{@code PENDING_ACTIVATION} — vừa đăng ký, chờ bấm link kích hoạt</li>
 *   <li>{@code ACTIVATED} — đã kích hoạt thành công</li>
 *   <li>{@code LOCKED} — quá hạn 24h chưa kích hoạt → bị job khóa</li>
 * </ul>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

	private String id;
	private String email;
	/** PENDING_ACTIVATION | ACTIVATED | LOCKED */
	private String status;
	private Instant createdAt;
	private Instant activatedAt;
	private Instant updatedAt;

}
