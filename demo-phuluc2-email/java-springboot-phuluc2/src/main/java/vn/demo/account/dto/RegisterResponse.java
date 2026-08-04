package vn.demo.account.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO — response sau đăng ký (không trả password).
 */
@Getter
@AllArgsConstructor
public class RegisterResponse {

	private String id;
	private String email;
	private String displayName;
	private Instant createdAt;
	/** Gợi ý HV: mail đang gửi nền — kiểm tra Inbox/Spam. */
	private String mailNote;

}
