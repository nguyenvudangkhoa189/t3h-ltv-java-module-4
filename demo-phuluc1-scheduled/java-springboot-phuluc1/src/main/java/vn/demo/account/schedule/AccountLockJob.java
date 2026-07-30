package vn.demo.account.schedule;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.account.service.UserAccountService;

/**
 * JOB mỏng — ví dụ 1 (syllabus §5 + §7).
 *
 * <p>Chỉ: đọc config → gọi {@link UserAccountService#lockUnactivatedOlderThan} → log.
 * <b>Không</b> viết query / vòng lặp nghiệp vụ trong Job.</p>
 *
 * <p>Cron lấy từ {@code app.account.lock-cron} (Spring cron 6 field, có giây).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLockJob {

	private final UserAccountService userAccountService;

	/** Số giờ chờ kích hoạt trước khi khóa — mặc định 24 (syllabus). */
	@Value("${app.account.lock-after-hours:24}")
	private long lockAfterHours;

	/**
	 * Quét định kỳ các account PENDING quá hạn.
	 *
	 * <p>{@code cron} hỗ trợ placeholder {@code ${...}} — đổi lịch không sửa code.</p>
	 */
	@Scheduled(cron = "${app.account.lock-cron:0 */15 * * * *}")
	public void lockStaleAccounts() {
		// Ủy thác toàn bộ nghiệp vụ xuống Service
		int locked = userAccountService.lockUnactivatedOlderThan(Duration.ofHours(lockAfterHours));
		if (locked > 0) {
			log.info("[AccountLockJob] Đã khóa {} tài khoản chưa kích hoạt quá {}h",
					locked, lockAfterHours);
		} else {
			log.debug("[AccountLockJob] Không có account nào cần khóa lần này");
		}
	}

}
