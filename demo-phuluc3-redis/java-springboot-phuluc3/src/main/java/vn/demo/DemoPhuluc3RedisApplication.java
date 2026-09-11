package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point — Demo Phụ lục 3: Spring Boot Redis Cache + MongoDB Product.
 *
 * <p><b>{@code @EnableCaching}</b> bật abstraction cache của Spring (syllabus §3).
 * Thiếu annotation này thì {@code @Cacheable} / {@code @CacheEvict} <b>không có hiệu lực</b>
 * — mọi lần gọi đều chạy thẳng thân method (luôn MISS về mặt hành vi).</p>
 *
 * <p><b>Nguồn sự thật Product = MongoDB</b> (convention demo-bai4: {@code @Document},
 * {@code MongoRepository}). Redis chỉ cache bản đọc. Hello cache vẫn giả lập chậm in-process.</p>
 *
 * <p><b>Thứ tự chạy lab:</b> Mongo đang Up → {@code docker compose up -d} (Redis :6379)
 * → {@code ./mvnw spring-boot:run}.</p>
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
