package vn.demo.account.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.account.model.UserAccount;
import vn.demo.account.service.UserAccountService;

/**
 * REST hỗ trợ quan sát ví dụ 1 — không chứa logic khóa (logic ở Job + Service).
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class UserAccountController {

	private final UserAccountService userAccountService;

	/** GET /api/accounts — xem status trước/sau khi job chạy. */
	@GetMapping
	public List<UserAccount> list() {
		return userAccountService.findAll();
	}

	/**
	 * POST /api/accounts/{id}/activate — giả lập user bấm link kích hoạt trong hạn.
	 */
	@PostMapping("/{id}/activate")
	public ResponseEntity<?> activate(@PathVariable String id) {
		try {
			UserAccount activated = userAccountService.activate(id);
			return ResponseEntity.ok(activated);
		} catch (IllegalStateException ex) {
			log.warn("[API] activate REJECT — {}", ex.getMessage());
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

}
