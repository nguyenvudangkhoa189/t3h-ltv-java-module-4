package vn.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import vn.demo.config.JwtProperties;

/**
 * Unit test — JWT chỉ chứa identity (không roles/permissions).
 */
class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(new JwtProperties(
				"demo-bai4-lab-secret-change-me-32chars-min",
				3_600_000L));
	}

	@Test
	void generateAndParse_containsOnlyIdentityClaims() {
		// --- Arrange + Act ---
		String token = jwtService.generateToken("uid-1", "admin");
		Claims claims = jwtService.parseClaims(token);

		// --- Assert: sub + username; không có roles trong payload lab ---
		assertEquals("uid-1", jwtService.userId(claims));
		assertEquals("admin", jwtService.username(claims));
		assertTrue(jwtService.rolesIfAny(claims).isEmpty());
	}

}
