package vn.demo.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.UserModel;
import vn.demo.repository.UserRepository;
import vn.demo.service.PermissionLoader;

/**
 * SECURITY — filter đọc {@code Authorization: Bearer &lt;jwt&gt;} mỗi request.
 * Syllabus: <b>Phần 3 — Tính năng 3</b> / Phần 2.4.
 *
 * <h2>Khác lab JWT “nhét roles vào token”</h2>
 * <p>Token chỉ chứng minh <b>identity</b>. Quyền lấy từ Mongo mỗi lần:</p>
 * <ol>
 *   <li>Verify JWT</li>
 *   <li>Load User theo {@code sub}</li>
 *   <li>Load Roles → Permissions → {@link AuthUserPrincipal}</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final PermissionLoader permissionLoader;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		// --- 1) Đọc header Authorization; thiếu / không Bearer → đi tiếp chain ---
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7).trim();
		try {
			// --- 2) Verify chữ ký + exp ---
			Claims claims = jwtService.parseClaims(token);
			String userId = jwtService.userId(claims);

			// --- 3) Đã có Authentication rồi thì không ghi đè ---
			if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				// --- 4) Load User từ Mongo (không tin quyền trong JWT) ---
				UserModel user = userRepository.findById(userId).orElse(null);
				if (user == null || !user.isEnabled()) {
					log.debug("JWT ok nhưng user không tồn tại / disabled: {}", userId);
					filterChain.doFilter(request, response);
					return;
				}

				// --- 5) Nạp Permission codes từ Role → gắn SecurityContext ---
				Set<String> permissionCodes = permissionLoader.loadPermissionCodes(user.getRoleIds());
				List<String> roleCodes = permissionLoader.loadRoleCodes(user.getRoleIds());
				AuthUserPrincipal principal = new AuthUserPrincipal(
						user.getId(),
						user.getUsername(),
						user.getEmail(),
						user.isEnabled(),
						roleCodes,
						permissionCodes);

				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception ex) {
			// --- Token hỏng / hết hạn → không set context (→ 401) ---
			log.debug("JWT rejected: {}", ex.getMessage());
		}

		filterChain.doFilter(request, response);
	}

}
