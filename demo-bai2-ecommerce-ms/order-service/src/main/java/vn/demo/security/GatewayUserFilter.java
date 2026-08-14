package vn.demo.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * FILTER (Order) — tin header {@code X-User-*} do Gateway gắn sau khi verify JWT.
 *
 * <h2>Kiến thức mới</h2>
 * <p>Không jjwt tại Order. Lab giả định client không gọi thẳng :8083
 * (hoặc mạng nội bộ). Gỡ Authorization ở Gateway để HV không “tự gắn” Bearer giả.</p>
 */
@Component
@Order(2)
@Slf4j
public class GatewayUserFilter extends OncePerRequestFilter {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final String USER_EMAIL_HEADER = "X-User-Email";
	private static final String USER_ROLES_HEADER = "X-User-Roles";

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		try {
			String userId = request.getHeader(USER_ID_HEADER);
			String userEmail = request.getHeader(USER_EMAIL_HEADER);
			String rolesHeader = request.getHeader(USER_ROLES_HEADER);

			if (userId != null && userEmail != null) {
				// --- Parse roles CSV → GrantedAuthority ---
				List<SimpleGrantedAuthority> authorities = List.of();
				if (rolesHeader != null && !rolesHeader.isBlank()) {
					authorities = Arrays.stream(rolesHeader.split(","))
							.map(String::trim)
							.map(SimpleGrantedAuthority::new)
							.collect(Collectors.toList());
				}

				GatewayUserPrincipal principal = new GatewayUserPrincipal(userId, userEmail);
				var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

			filterChain.doFilter(request, response);
		} finally {
			// --- Tránh leak identity sang request sau (thread pool) ---
			SecurityContextHolder.clearContext();
		}
	}
}
