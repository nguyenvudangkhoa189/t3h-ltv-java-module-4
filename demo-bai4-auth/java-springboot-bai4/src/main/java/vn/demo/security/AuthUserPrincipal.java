package vn.demo.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/**
 * Principal gắn vào SecurityContext sau khi JWT hợp lệ + đã load Permission từ Mongo.
 * Syllabus: <b>Phần 3 — Tính năng 3</b>.
 *
 * <p>Authorities = <b>permission codes</b> (USER_CREATE…), <b>không</b> phải {@code ROLE_ADMIN}.
 * Khớp {@code @PreAuthorize("hasAuthority('USER_CREATE')")}.</p>
 */
@Getter
public class AuthUserPrincipal implements UserDetails {

	private final String id;
	private final String username;
	private final String email;
	private final boolean enabled;
	private final List<String> roleCodes;
	private final List<String> permissionCodes;
	private final Collection<? extends GrantedAuthority> authorities;

	public AuthUserPrincipal(
			String id,
			String username,
			String email,
			boolean enabled,
			List<String> roleCodes,
			Collection<String> permissionCodes) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.enabled = enabled;
		this.roleCodes = List.copyOf(roleCodes);
		this.permissionCodes = List.copyOf(permissionCodes);
		// --- Map permission.code → GrantedAuthority (không thêm tiền tố ROLE_) ---
		this.authorities = permissionCodes.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
