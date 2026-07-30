package vn.demo.account.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.account.model.UserAccount;
import vn.demo.account.repository.UserAccountRepository;
import vn.demo.exception.ResourceNotFoundException;

/**
 * SERVICE — nghiệp vụ tài khoản (syllabus §5).
 *
 * <p><b>Job chỉ gọi Service</b>; logic khóa / kích hoạt nằm đây để dễ unit test
 * (Mockito mock Repository — không cần Scheduler chạy thật).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountService {

	public static final String STATUS_PENDING = "PENDING_ACTIVATION";
	public static final String STATUS_ACTIVATED = "ACTIVATED";
	public static final String STATUS_LOCKED = "LOCKED";

	private final UserAccountRepository repository;

	/** Liệt kê mọi account — dùng cho API demo / Postman. */
	public List<UserAccount> findAll() {
		return repository.findAll();
	}

	/**
	 * Khóa các tài khoản còn {@code PENDING_ACTIVATION} lâu hơn {@code maxAge}.
	 *
	 * <p><b>Điểm kiến thức quan trọng:</b> không hẹn giờ riêng từng user.
	 * Job chạy định kỳ → tính {@code cutoff = now - maxAge} → quét mọi bản ghi
	 * thỏa điều kiện. 24h là {@link Duration}, không phải “qua ngày lịch”.</p>
	 *
	 * @param maxAge tuổi tối đa cho phép ở trạng thái PENDING (vd. 24 giờ)
	 * @return số tài khoản vừa chuyển sang {@code LOCKED}
	 */
	public int lockUnactivatedOlderThan(Duration maxAge) {
		// 1) Mốc thời gian: mọi PENDING tạo trước mốc này là “quá hạn”
		Instant cutoff = Instant.now().minus(maxAge);
		log.debug("[UserAccountService] lock — cutoff={}, maxAge={}", cutoff, maxAge);

		// 2) Query đúng nghiệp vụ
		List<UserAccount> stale = repository.findByStatusAndCreatedAtBefore(STATUS_PENDING, cutoff);
		if (stale.isEmpty()) {
			return 0;
		}

		// 3) Đổi status → LOCKED (giữ bản ghi để audit; không xóa cứng)
		Instant now = Instant.now();
		for (UserAccount user : stale) {
			user.setStatus(STATUS_LOCKED);
			user.setUpdatedAt(now);
		}
		repository.saveAll(stale);

		log.info("[UserAccountService] Đã khóa {} tài khoản PENDING quá {}", stale.size(), maxAge);
		return stale.size();
	}

	/**
	 * User bấm link kích hoạt trong hạn → ACTIVATED.
	 *
	 * <p>Chỉ cho phép khi đang {@code PENDING_ACTIVATION}. Account đã LOCKED
	 * phải đăng ký lại / liên hệ support (ngoài phạm vi demo).</p>
	 */
	public UserAccount activate(String id) {
		UserAccount user = repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy account id=" + id));

		if (!STATUS_PENDING.equals(user.getStatus())) {
			throw new IllegalStateException(
					"Chỉ kích hoạt được khi PENDING_ACTIVATION — hiện tại=" + user.getStatus());
		}

		Instant now = Instant.now();
		user.setStatus(STATUS_ACTIVATED);
		user.setActivatedAt(now);
		user.setUpdatedAt(now);
		UserAccount saved = repository.save(user);
		log.info("[UserAccountService] Đã kích hoạt account id={}, email={}", id, user.getEmail());
		return saved;
	}

}
