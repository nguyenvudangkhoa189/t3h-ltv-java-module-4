package vn.demo.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import vn.demo.service.JwtService;

/**
 * GATEWAY — JWT tại biên (pattern B / Phase 1).
 *
 * <p><b>Kiến thức mới:</b> Client chỉ gửi Bearer tới Gateway. Gateway verify chữ ký
 * (cùng secret Auth), gắn {@code X-User-*} tin cậy, <b>gỡ</b> Authorization trước khi
 * forward. Downstream (Order) <b>không</b> parse JWT — tránh duplicate code khi thêm service.</p>
 *
 * <p>Public: {@code /api/auth/**} (trừ khi cần), {@code GET /api/products/**}.
 * Protected: {@code /api/orders/**}; ghi Product cần {@code ROLE_ADMIN}.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

	public static final String HDR_USER_ID = "X-User-Id";
	public static final String HDR_USER_EMAIL = "X-User-Email";
	public static final String HDR_USER_ROLES = "X-User-Roles";

	private final JwtService jwtService;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();
		String method = request.getMethod() != null ? request.getMethod().name() : "GET";

		// 1) Bỏ qua health / public
		if (path.startsWith("/actuator")) {
			return chain.filter(exchange);
		}
		if (isPublic(path, method)) {
			return chain.filter(exchange);
		}

		// 2) Bắt buộc Bearer
		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
		String token = authHeader.substring(7);

		// 3) Verify JWT (không gọi Auth Service mỗi request)
		if (!jwtService.validateToken(token)) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		try {
			String userId = jwtService.extractUserId(token);
			String email = jwtService.extractEmail(token);
			List<String> roles = jwtService.extractRoles(token);

			// 4) ADMIN cho ghi Product
			if (isAdminRoute(path, method) && !roles.contains("ROLE_ADMIN")) {
				exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
				return exchange.getResponse().setComplete();
			}

			// 5) Gỡ Bearer + header giả mạo; gắn identity tin cậy
			ServerHttpRequest mutated = request.mutate()
					.headers(h -> {
						h.remove(HttpHeaders.AUTHORIZATION);
						h.remove(HDR_USER_ID);
						h.remove(HDR_USER_EMAIL);
						h.remove(HDR_USER_ROLES);
						h.set(HDR_USER_ID, userId != null ? userId : "");
						h.set(HDR_USER_EMAIL, email != null ? email : "");
						h.set(HDR_USER_ROLES, roles != null ? String.join(",", roles) : "");
					})
					.build();

			return chain.filter(exchange.mutate().request(mutated).build());
		} catch (Exception e) {
			log.warn("JWT process error: {}", e.getMessage());
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
	}

	private boolean isPublic(String path, String method) {
		if (path.startsWith("/api/auth/")) {
			return true;
		}
		return "GET".equals(method) && path.startsWith("/api/products");
	}

	private boolean isAdminRoute(String path, String method) {
		if (!path.startsWith("/api/products")) {
			return false;
		}
		return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10;
	}
}
