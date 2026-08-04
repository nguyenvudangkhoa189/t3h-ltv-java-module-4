package vn.demo.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.demo.account.dto.RegisterRequest;
import vn.demo.account.dto.RegisterResponse;
import vn.demo.account.model.UserAccount;
import vn.demo.account.repository.UserAccountRepository;
import vn.demo.exception.ConflictException;
import vn.demo.mail.WelcomeMailService;

/**
 * Unit test AccountService — mock Repository + WelcomeMailService (không SMTP).
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private UserAccountRepository repository;

	@Mock
	private WelcomeMailService welcomeMailService;

	@InjectMocks
	private AccountService accountService;

	@Test
	void register_savesAndTriggersWelcomeMail() {
		// 1) Chưa có email
		when(repository.findByEmail("a@demo.vn")).thenReturn(Optional.empty());
		when(repository.save(any(UserAccount.class))).thenAnswer(inv -> {
			UserAccount a = inv.getArgument(0);
			a.setId("id-1");
			return a;
		});

		// 2) Đăng ký
		RegisterRequest req = new RegisterRequest();
		req.setEmail("a@demo.vn");
		req.setDisplayName("An");
		req.setPassword("secret1");

		RegisterResponse res = accountService.register(req);

		// 3) Response đúng + welcome được gọi
		assertEquals("a@demo.vn", res.getEmail());
		assertEquals("An", res.getDisplayName());
		verify(welcomeMailService).sendWelcome(eq("a@demo.vn"), eq("An"));

		ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
		verify(repository).save(saved.capture());
		org.junit.jupiter.api.Assertions.assertNotNull(saved.getValue().getPasswordHash());
	}

	@Test
	void register_duplicateEmail_throwsConflict() {
		when(repository.findByEmail("a@demo.vn")).thenReturn(Optional.of(new UserAccount()));

		RegisterRequest req = new RegisterRequest();
		req.setEmail("a@demo.vn");
		req.setDisplayName("An");
		req.setPassword("secret1");

		assertThrows(ConflictException.class, () -> accountService.register(req));
	}

}
