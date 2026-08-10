package vn.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bind {@code app.jwt.*} từ {@code application.properties}.
 * Syllabus: <b>Phần 3 — Tính năng 1 &amp; 3</b>.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
		String secret,
		long expirationMs) {
}
