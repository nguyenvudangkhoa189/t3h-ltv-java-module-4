package vn.demo.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.product.model.Product;
import vn.demo.product.repository.ProductRepository;

/**
 * Seed vài Product khi app start — để gọi {@code GET /api/products/1} ngay (syllabus §5).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

	private final ProductRepository productRepository;

	@Value("${app.product.seed-on-startup:true}")
	private boolean seedOnStartup;

	@Override
	public void run(ApplicationArguments args) {
		// 1) Tắt seed khi test / khi đã có data
		if (!seedOnStartup || productRepository.count() > 0) {
			return;
		}

		// 2) Tạo vài sản phẩm mẫu (id sẽ được repository gán)
		Instant now = Instant.now();
		Product laptop = product("Laptop Pro", new BigDecimal("22990000"), "Laptop học Spring Boot", now);
		Product mouse = product("Chuột không dây", new BigDecimal("350000"), "Phụ kiện lab", now);
		Product keyboard = product("Bàn phím cơ", new BigDecimal("890000"), "Gõ code", now);

		// 3) Lưu in-memory
		productRepository.saveAll(List.of(laptop, mouse, keyboard));
		log.info("[DataSeeder] Seed 3 products — id thường là 1, 2, 3");
	}

	private static Product product(String name, BigDecimal price, String description, Instant at) {
		Product p = new Product();
		p.setName(name);
		p.setPrice(price);
		p.setDescription(description);
		p.setUpdatedAt(at);
		return p;
	}

}
