package vn.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.dto.LoginRequest;
import vn.demo.dto.LoginResponse;
import vn.demo.dto.MeResponse;
import vn.demo.security.AuthUserPrincipal;
import vn.demo.service.AuthService;
import vn.demo.service.SecurityUtils;

/**
 * CONTROLLER — Authentication API.
 * Syllabus: <b>Phần 3 — Tính năng 4</b>.
 *
 * <ul>
 *   <li>{@code POST /api/auth/login} — public, trả JWT (identity)</li>
 *   <li>{@code GET /api/auth/me} — Bearer; roles + permissions từ principal (đã load DB)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

	private final AuthService authService;
	private final SecurityUtils securityUtils;

	/**
	 * Public — không yêu cầu Bearer trên Swagger (tắt security scheme toàn cục).
	 */
	@PostMapping("/login")
	@SecurityRequirements // empty = không bắt Authorize khi Try it out login
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		// --- Ủy thác AuthService; sai credentials → UnauthorizedException → 401 ---
		return authService.login(request);
	}

	@GetMapping("/me")
	public MeResponse me() {
		// --- Identity + quyền đã gắn bởi JWT filter (Permission từ Mongo) ---
		AuthUserPrincipal user = securityUtils.currentUser();
		return new MeResponse(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRoleCodes(),
				user.getPermissionCodes());
	}

}
