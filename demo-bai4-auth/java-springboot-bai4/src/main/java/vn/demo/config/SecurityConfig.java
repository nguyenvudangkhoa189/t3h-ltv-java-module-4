package vn.demo.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.demo.security.JwtAuthenticationFilter;

/**
 * CONFIG — Spring Security REST + JWT (stateless) + method security.
 * Syllabus: <b>Phần 3 — Tính năng 3</b> (Swagger public: tính năng 7).
 *
 * <h2>Khác Module 3 Bài 10</h2>
 * <ul>
 *   <li>M3: Stateful — cookie session, {@code formLogin}, {@code hasRole("ADMIN")}.</li>
 *   <li>Bài này: Stateless — Bearer JWT; quyền chi tiết bằng
 *       {@code @PreAuthorize("hasAuthority('USER_…')")}.</li>
 * </ul>
 *
 * <p>{@link EnableMethodSecurity} bật {@code @PreAuthorize} trên Controller.</p>
 *
 * <p><b>Swagger UI:</b> {@code /swagger-ui/**} và {@code /v3/api-docs/**} phải
 * {@code permitAll} — nếu không, mở UI bị 401 vì {@code anyRequest().authenticated()}.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// --- 1) API JWT không dùng cookie form → tắt CSRF ---
				.csrf(csrf -> csrf.disable())

				// --- 2) Stateless: không tạo HTTP session ---
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// --- 3) URL: login + Swagger/OpenAPI public; còn lại cần JWT (@PreAuthorize) ---
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						// springdoc: UI + spec JSON (lab — để Try it out không bị 401)
						.requestMatchers(
								"/swagger-ui.html",
								"/swagger-ui/**",
								"/v3/api-docs",
								"/v3/api-docs/**").permitAll()
						.anyRequest().authenticated())

				// --- 4) JSON 401 / 403 (không redirect HTML /login) ---
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(json401EntryPoint())
						.accessDeniedHandler(json403Handler()))

				// --- 5) JWT filter trước filter form-login mặc định ---
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private AuthenticationEntryPoint json401EntryPoint() {
		return (request, response, authException) -> writeJson(response, HttpStatus.UNAUTHORIZED, "Unauthorized");
	}

	private AccessDeniedHandler json403Handler() {
		return (request, response, accessDeniedException) -> writeJson(response, HttpStatus.FORBIDDEN, "Forbidden");
	}

	private static void writeJson(HttpServletResponse response, HttpStatus status, String error) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"error\":\"" + error + "\"}");
	}

}
