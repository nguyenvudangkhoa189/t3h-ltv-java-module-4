package vn.demo.service;

import java.util.List;

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

/**
 * SERVICE (Gateway) — <b>verify</b> JWT, không phát hành.
 *
 * <h2>Kiến thức mới</h2>
 * <p>Gateway không gọi Auth mỗi request. Cùng {@code app.jwt.secret} với Auth
 * là đủ để tin chữ ký. Fail → filter trả 401 tại biên.</p>
 */
@Service
@Slf4j
public class JwtService {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	public boolean validateToken(String token) {
		try {
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

	public String extractUserId(String token) {
		return extractClaims(token).getSubject();
	}

	public String extractEmail(String token) {
		return extractClaims(token).get("email", String.class);
	}

	@SuppressWarnings("unchecked")
	public List<String> extractRoles(String token) {
		Object roles = extractClaims(token).get("roles");
		if (roles instanceof List<?>) {
			return (List<String>) roles;
		}
		return List.of();
	}

	private Claims extractClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSignKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSignKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}
}
