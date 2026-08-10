package vn.demo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import vn.demo.config.JwtProperties;

/**
 * SECURITY — phát hành / parse JWT (HS256).
 * Syllabus: <b>Phần 3 — Tính năng 3</b> / Phần 2.4.
 *
 * <h2>Payload lab — chỉ identity</h2>
 * <ul>
 *   <li>{@code sub} = user Mongo {@code _id}</li>
 *   <li>{@code username}</li>
 *   <li>{@code exp}</li>
 * </ul>
 * <p><b>Không</b> nhét Role / Permission vào JWT. Mỗi request filter nạp quyền từ Mongo
 * → đổi permission trên Role có hiệu lực ngay, không cần login lại.</p>
 */
@Service
public class JwtService {

	private final JwtProperties properties;
	private final SecretKey key;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		// --- Secret ≥ 32 bytes cho HS256 ---
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Phát hành access token sau login thành công.
	 *
	 * @param userId   Mongo {@code users._id} → claim {@code sub}
	 * @param username claim phụ (tiện debug /me)
	 */
	public String generateToken(String userId, String username) {
		// --- Build JWT: chỉ identity + thời điểm hết hạn + ký HS256 ---
		Date now = new Date();
		Date exp = new Date(now.getTime() + properties.expirationMs());
		return Jwts.builder()
				.subject(userId)
				.claim("username", username)
				.issuedAt(now)
				.expiration(exp)
				.signWith(key)
				.compact();
	}

	/**
	 * Verify chữ ký + exp; trả claims. Fail → JwtException (filter → không set context → 401).
	 */
	public Claims parseClaims(String token) {
		// --- Parser yêu cầu đúng secret ---
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String userId(Claims claims) {
		return claims.getSubject();
	}

	public String username(Claims claims) {
		Object v = claims.get("username");
		return v == null ? null : v.toString();
	}

	/** Helper test / debug — lab không dùng roles trong token. */
	public List<String> rolesIfAny(Claims claims) {
		Object v = claims.get("roles");
		if (v instanceof List<?> list) {
			return list.stream().map(Object::toString).toList();
		}
		return List.of();
	}

}
