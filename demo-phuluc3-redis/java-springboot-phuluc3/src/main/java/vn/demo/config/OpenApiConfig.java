package vn.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Metadata hiển thị đầu trang Swagger UI.
 *
 * <p>Mở {@code http://localhost:8080/swagger-ui/index.html} sau khi
 * {@code docker compose up -d} + {@code ./mvnw spring-boot:run}.</p>
 *
 * <p>Ôn springdoc: Module 4 Bài 1 §5.</p>
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI phuluc3RedisOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Phụ lục 3 — Redis Cache API")
						.version("v1")
						.description("""
								Demo Spring Cache + Redis (Docker).
								Gợi ý lab: gọi GET hello-cache / products/{id} hai lần để thấy HIT;
								PUT/DELETE products để thấy @CacheEvict.
								"""));
	}

}
