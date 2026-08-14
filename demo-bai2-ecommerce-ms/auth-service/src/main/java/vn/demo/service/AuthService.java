package vn.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.demo.document.RefreshToken;
import vn.demo.document.User;
import vn.demo.dto.request.LoginRequest;
import vn.demo.dto.request.RefreshTokenRequest;
import vn.demo.dto.request.RegisterRequest;
import vn.demo.dto.response.LoginResponse;
import vn.demo.dto.response.RegisterResponse;
import vn.demo.exception.BadRequestException;
import vn.demo.exception.UnauthorizedException;
import vn.demo.repository.RefreshTokenRepository;
import vn.demo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * SERVICE — đăng ký / login / refresh / logout.
 * Password BCrypt; access JWT không lưu DB; refresh lưu Mongo để thu hồi.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Tạo tài khoản ROLE_USER. Không trả JWT — HV phải login.
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Dang ky email: {}", request.getEmail());

        // --- 1) Email unique ---
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // --- 2) Hash BCrypt — không lưu plain text ---
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(List.of("ROLE_USER"))
                .status("ACTIVE")
                .build();

        // --- 3) Lưu users ---
        User savedUser = userRepository.save(user);

        log.info("Dang ky OK id={}", savedUser.getId());

        // --- 4) Response không kèm token ---
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .roles(savedUser.getRoles())
                .message("Registration successful. Please login.")
                .build();
    }

    /**
     * Xác thực → access + refresh token.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login email: {}", request.getEmail());

        // --- 1) Tìm user ---
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // --- 2) So khớp BCrypt ---
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // --- 3) Tài khoản ACTIVE ---
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is inactive");
        }

        // --- 4) Ký JWT ---
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // --- 5) Lưu refresh (rotation / logout) ---
        saveRefreshToken(user.getId(), refreshToken);

        log.info("Login OK user={}", user.getId());

        // --- 6) Trả token + identity ---
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(LoginResponse.UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .roles(user.getRoles())
                        .build())
                .build();
    }

    /**
     * Đổi cặp token: xóa refresh cũ, phát access + refresh mới.
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token");

        // --- 1) Refresh phải còn trong Mongo ---
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        // --- 2) Hết hạn / đã revoke → xóa ---
        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        // --- 3) Load user ---
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // --- 4) Phát token mới ---
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // --- 5) Rotation: xóa cũ, lưu mới ---
        refreshTokenRepository.delete(refreshToken);
        saveRefreshToken(user.getId(), newRefreshToken);

        log.info("Refresh OK user={}", user.getId());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .user(LoginResponse.UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .roles(user.getRoles())
                        .build())
                .build();
    }

    /** Logout: xóa refresh trong Mongo (access hết hạn theo TTL). */
    @Transactional
    public void logout(String refreshTokenValue) {
        log.info("Logout");
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    log.info("Da thu hoi refresh user={}", token.getUserId());
                });
    }

    private void saveRefreshToken(String userId, String tokenValue) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);
    }
}