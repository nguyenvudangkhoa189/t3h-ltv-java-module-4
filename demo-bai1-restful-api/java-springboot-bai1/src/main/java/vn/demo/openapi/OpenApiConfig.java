package vn.demo.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Metadata hiển thị đầu trang Swagger UI (§5.5).
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI employeeOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Module 4 — Bài 1 Employee API")
						.version("v1")
						.description("Demo chuẩn REST (/api/v1), MapStruct, @Async, logging, springdoc"));
	}

}
