package vn.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * CONFIG — springdoc OpenAPI + nút <b>Authorize</b> (Bearer JWT) trên Swagger UI.
 * Syllabus: <b>Phần 3 — Tính năng 7</b>.
 *
 * <h2>Vì sao cần bean này?</h2>
 * <p>Chỉ thêm dependency springdoc thì UI mở được, nhưng chưa biết gửi JWT.
 * Khai báo {@link SecurityScheme} kiểu HTTP Bearer → Swagger UI hiện nút Authorize;
 * sau khi dán token, mọi request Try it out tự gắn {@code Authorization: Bearer &lt;token&gt;}.</p>
 *
 * <h2>Cách test trên UI</h2>
 * <ol>
 *   <li>Mở {@code /swagger-ui/index.html}</li>
 *   <li>{@code POST /api/auth/login} (không cần Authorize) → copy {@code token}</li>
 *   <li>Nút <b>Authorize</b> → dán token (không gõ chữ {@code Bearer })</li>
 *   <li>Gọi {@code GET /api/users}, {@code DELETE /api/users/{id}}… để thấy 200 / 403</li>
 * </ol>
 */
@Configuration
public class OpenApiConfig {

	public static final String BEARER_SCHEME = "bearerAuth";

	@Bean
	OpenAPI demoBai4OpenApi() {
		// --- 1) Metadata trang Swagger ---
		// --- 2) Khai báo scheme Bearer JWT (nút Authorize) ---
		// --- 3) Áp dụng scheme mặc định cho mọi operation (login tắt bằng @SecurityRequirements) ---
		return new OpenAPI()
				.info(new Info()
						.title("Module 4 — Bài 4 Auth API")
						.version("v1")
						.description("""
								JWT + RBAC Permission (hasAuthority).

								**Test token:** login → copy `token` → Authorize → dán token \
								(không cần chữ Bearer) → Try it out các API /api/users.
								"""))
				.components(new Components().addSecuritySchemes(BEARER_SCHEME,
						new SecurityScheme()
								.name(BEARER_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Dán access token lấy từ POST /api/auth/login")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
	}

}
