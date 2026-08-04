package vn.demo.account.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.account.dto.RegisterRequest;
import vn.demo.account.dto.RegisterResponse;
import vn.demo.account.model.UserAccount;
import vn.demo.account.service.AccountService;

/**
 * CONTROLLER — đăng ký + liệt kê account (syllabus §5).
 *
 * <p>{@code POST /register} trả <b>202 Accepted</b>: account đã lưu, mail đang gửi nền.
 * Client không chờ SMTP xong (so sánh với {@code POST /api/mail/hello} đồng bộ).</p>
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
		RegisterResponse body = accountService.register(request);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
	}

	@GetMapping
	public List<RegisterResponse> list() {
		return accountService.findAll().stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	private RegisterResponse toResponse(UserAccount a) {
		return new RegisterResponse(
				a.getId(), a.getEmail(), a.getDisplayName(), a.getCreatedAt(), null);
	}

}
