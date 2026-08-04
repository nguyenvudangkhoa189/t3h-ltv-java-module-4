package vn.demo.account.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.account.dto.RegisterRequest;
import vn.demo.account.dto.RegisterResponse;
import vn.demo.account.model.UserAccount;
import vn.demo.account.repository.UserAccountRepository;
import vn.demo.exception.ConflictException;
import vn.demo.mail.WelcomeMailService;

/**
 * SERVICE — nghiệp vụ đăng ký (syllabus §5).
 *
 * <p>Sau khi lưu user thành công → gọi {@link WelcomeMailService#sendWelcome}
 * (async). Đăng ký không bị chặn bởi SMTP chậm.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

	private final UserAccountRepository repository;
	private final WelcomeMailService welcomeMailService;

	/**
	 * Đăng ký tài khoản mới + kích hoạt gửi mail chào (nền).
	 *
	 * @return response không chứa password; {@code mailNote} nhắc HV kiểm tra hộp thư
	 */
	public RegisterResponse register(RegisterRequest request) {
		String email = request.getEmail().trim().toLowerCase();

		// 1) Chống trùng email
		if (repository.findByEmail(email).isPresent()) {
			throw new ConflictException("Email đã tồn tại: " + email);
		}

		// 2) Tạo entity (hash password đơn giản — lab, không dùng cho production)
		UserAccount account = new UserAccount();
		account.setEmail(email);
		account.setDisplayName(request.getDisplayName().trim());
		account.setPasswordHash(simpleHash(request.getPassword()));
		account.setCreatedAt(Instant.now());

		// 3) Lưu in-memory
		UserAccount saved = repository.save(account);
		log.info("[AccountService] Đã đăng ký id={}, email={}", saved.getId(), saved.getEmail());

		// 4) Gửi mail chào — @Async: method trả về ngay, SMTP chạy trên async-*
		welcomeMailService.sendWelcome(saved.getEmail(), saved.getDisplayName());

		// 5) Trả response (HTTP 202 ở Controller — mail đang xử lý nền)
		return new RegisterResponse(
				saved.getId(),
				saved.getEmail(),
				saved.getDisplayName(),
				saved.getCreatedAt(),
				"Email chào đang gửi nền — kiểm tra Inbox/Spam");
	}

	/** Liệt kê account đã đăng ký (lab quan sát). */
	public List<UserAccount> findAll() {
		return repository.findAll();
	}

	/** Hash MD5 hex — chỉ để demo không lưu plain text. Production: BCrypt. */
	private static String simpleHash(String raw) {
		return DigestUtils.md5DigestAsHex(raw.getBytes());
	}

}
