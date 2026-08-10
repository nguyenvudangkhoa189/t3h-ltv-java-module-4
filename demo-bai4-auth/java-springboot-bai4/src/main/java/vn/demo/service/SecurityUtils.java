package vn.demo.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import vn.demo.exception.UnauthorizedException;
import vn.demo.security.AuthUserPrincipal;

/**
 * Helper — lấy {@link AuthUserPrincipal} từ SecurityContext (JWT filter đã gắn).
 */
@Component
public class SecurityUtils {

	public AuthUserPrincipal currentUser() {
		// --- 1) Đọc Authentication đã gắn bởi JWT filter ---
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// --- 2) Chưa login / principal không đúng kiểu → 401 ---
		if (auth == null || !(auth.getPrincipal() instanceof AuthUserPrincipal principal)) {
			throw new UnauthorizedException("Unauthorized");
		}
		return principal;
	}

}
