package vn.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.LoginRequest;
import vn.demo.dto.LoginResponse;
import vn.demo.exception.UnauthorizedException;
import vn.demo.model.UserModel;
import vn.demo.repository.UserRepository;
import vn.demo.security.JwtService;

/**
 * SERVICE — đăng nhập: BCrypt → JWT (chỉ identity).
 * Syllabus: <b>Phần 3 — Tính năng 4</b>.
 *
 * <p>Permission <b>không</b> đưa vào token; filter sẽ nạp từ Mongo mỗi request.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public LoginResponse login(LoginRequest request) {
		// --- 1) Tìm user theo username ---
		UserModel user = userRepository.findByUsername(request.username())
				.orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

		// --- 2) Tài khoản bị khóa ---
		if (!user.isEnabled()) {
			throw new UnauthorizedException("User disabled");
		}

		// --- 3) So khớp mật khẩu plain với BCrypt hash ---
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new UnauthorizedException("Invalid username or password");
		}

		// --- 4) Phát hành JWT (sub=id, username) — không nhét permissions ---
		String token = jwtService.generateToken(user.getId(), user.getUsername());
		return new LoginResponse(token);
	}

}
