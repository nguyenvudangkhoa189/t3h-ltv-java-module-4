package vn.demo.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import vn.demo.document.User;

/**
 * SERVICE — <b>ký / phát</b> JWT (HS256). Chỉ Auth Service làm việc này.
 *
 * <h2>Kiến thức mới (khác Bài 4 một chút)</h2>
 * <p>Lab MS Phase 1 nhét {@code email} + {@code roles} vào access token để Gateway
 * verify rồi gắn {@code X-User-*} — Order <b>không</b> gọi Auth mỗi request.
 * Secret phải <b>giống</b> Gateway ({@code app.jwt.secret}).</p>
 *
 * <p>Access ~15 phút (không lưu Mongo). Refresh ~7 ngày (lưu Mongo, revoke khi logout).</p>
 */
@Service
@Slf4j
public class JwtService {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.access-token.expiration-minutes}")
	private int accessTokenExpirationMinutes;

	@Value("${app.jwt.refresh-token.expiration-days}")
	private int refreshTokenExpirationDays;

	/** Phát hành access token sau login — claims: sub, email, roles, exp. */
	public String generateAccessToken(User user) {
		// --- 1) TTL ngắn ---
		Date expiration = Date.from(
				LocalDateTime.now()
						.plusMinutes(accessTokenExpirationMinutes)
						.atZone(ZoneId.systemDefault())
						.toInstant());

		// --- 2) Ký HS256 — Gateway dùng cùng secret để verify ---
		return Jwts.builder()
				.subject(user.getId())
				.claim("email", user.getEmail())
				.claim("roles", user.getRoles())
				.issuedAt(new Date())
				.expiration(expiration)
				.signWith(getSignKey())
				.compact();
	}

	/** Refresh token: ít claim, TTL dài — lưu Mongo để revoke. */
	public String generateRefreshToken(User user) {
		Date expiration = Date.from(
				LocalDateTime.now()
						.plusDays(refreshTokenExpirationDays)
						.atZone(ZoneId.systemDefault())
						.toInstant());

		return Jwts.builder()
				.subject(user.getId())
				.issuedAt(new Date())
				.expiration(expiration)
				.signWith(getSignKey())
				.compact();
	}

	public String extractUserId(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public String extractEmail(String token) {
		return extractClaim(token, claims -> claims.get("email", String.class));
	}

	public boolean validateToken(String token) {
		try {
			// --- Parser bắt buộc đúng secret + chưa hết hạn ---
			Jwts.parser()
					.verifyWith(getSignKey())
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.warn("JWT het han: {}", e.getMessage());
			return false;
		} catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
			log.warn("JWT khong hop le: {}", e.getMessage());
			return false;
		} catch (Exception e) {
			log.error("JWT verify loi: {}", e.getMessage());
			return false;
		}
	}

	private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSignKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/** HS256: secret ≥ 32 bytes. */
	private SecretKey getSignKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}
}
