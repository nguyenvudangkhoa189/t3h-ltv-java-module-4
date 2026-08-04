package vn.demo.account.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL — tài khoản in-memory (syllabus §5).
 *
 * <p>Demo không dùng DB: đủ để minh họa đăng ký → gửi mail chào.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

	private String id;
	private String email;
	private String displayName;
	/** Đã hash đơn giản cho lab — không phải production security. */
	private String passwordHash;
	private Instant createdAt;

}
