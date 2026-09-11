package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point — Demo Phụ lục 3: Spring Boot Redis Cache (Docker Redis).
 *
 * <p><b>{@code @EnableCaching}</b> bật abstraction cache của Spring (syllabus §3).
 * Thiếu annotation này thì {@code @Cacheable} / {@code @CacheEvict} <b>không có hiệu lực</b>
 * — mọi lần gọi đều chạy thẳng thân method (luôn MISS về mặt hành vi).</p>
 *
 * <p>Demo cố ý <b>không dùng Mongo/JPA</b> — Product in-memory — để tập trung Redis Docker,
 * {@code @Cacheable}/{@code @CacheEvict}, TTL, và pattern Service mỏng (syllabus §2–§6).</p>
 *
 * <p><b>Thứ tự chạy lab:</b> {@code docker compose up -d} (Redis :6379) → rồi
 * {@code ./mvnw spring-boot:run}.</p>
 *
 * @see vn.demo.hello.HelloCacheService
 * @see vn.demo.product.service.ProductService
 * @see vn.demo.config.RedisCacheConfig
 */
@SpringBootApplication
@EnableCaching
public class DemoPhuluc3RedisApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoPhuluc3RedisApplication.class, args);
	}

}
