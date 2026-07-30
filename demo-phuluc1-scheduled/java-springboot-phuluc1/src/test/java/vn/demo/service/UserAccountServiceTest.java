package vn.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.demo.account.model.UserAccount;
import vn.demo.account.repository.InMemoryUserAccountRepository;
import vn.demo.account.service.UserAccountService;

/**
 * Unit test Service — <b>không</b> cần Scheduler chạy.
 *
 * <p>Dùng {@link InMemoryUserAccountRepository} thật (không Mockito) để HV
 * tập trung: seed data → gọi Service → assert status. Phù hợp phụ lục Scheduled
 * khi chưa bắt buộc lab Mockito.</p>
 */
class UserAccountServiceTest {

	private InMemoryUserAccountRepository repository;
	private UserAccountService userAccountService;

	@BeforeEach
	void setUp() {
		repository = new InMemoryUserAccountRepository();
		userAccountService = new UserAccountService(repository);
	}

	@Test
	void lockUnactivatedOlderThan_locksOnlyStalePending() {
		// Arrange: 1 quá hạn (25h) + 1 còn hạn (1h)
		UserAccount stale = pending("stale@demo.vn", Instant.now().minus(Duration.ofHours(25)));
		UserAccount fresh = pending("fresh@demo.vn", Instant.now().minus(Duration.ofHours(1)));
		repository.saveAll(List.of(stale, fresh));

		// Act — gọi thẳng Service (giống Job sẽ gọi)
		int locked = userAccountService.lockUnactivatedOlderThan(Duration.ofHours(24));

		// Assert
		assertThat(locked).isEqualTo(1);
		assertThat(repository.findById(stale.getId())).get()
				.extracting(UserAccount::getStatus)
				.isEqualTo(UserAccountService.STATUS_LOCKED);
		assertThat(repository.findById(fresh.getId())).get()
				.extracting(UserAccount::getStatus)
				.isEqualTo(UserAccountService.STATUS_PENDING);
	}

	@Test
	void lockUnactivatedOlderThan_returnsZero_whenNoStale() {
		repository.save(pending("fresh@demo.vn", Instant.now().minus(Duration.ofHours(1))));

		int locked = userAccountService.lockUnactivatedOlderThan(Duration.ofHours(24));

		assertThat(locked).isZero();
	}

	private static UserAccount pending(String email, Instant createdAt) {
		UserAccount u = new UserAccount();
		u.setEmail(email);
		u.setStatus(UserAccountService.STATUS_PENDING);
		u.setCreatedAt(createdAt);
		u.setUpdatedAt(createdAt);
		return u;
	}

}
