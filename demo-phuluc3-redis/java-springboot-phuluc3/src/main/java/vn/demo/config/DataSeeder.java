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
 * Seed vài Product vào Mongo khi collection trống (syllabus §5).
 *
 * <p>Idempotent: {@code count() > 0} thì bỏ qua (giống tinh thần RbacDataSeeder demo-bai4).</p>
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
		// 1) Tắt seed khi test
		if (!seedOnStartup) {
			return;
		}

		// 2) Idempotent — đã có document trên Mongo thì bỏ qua
		long existing = productRepository.count();
		if (existing > 0) {
			log.info("[DataSeeder] products đã có {} document — skip seed", existing);
			return;
		}

		// 3) Tạo vài sản phẩm mẫu (Mongo tự gán _id)
		Instant now = Instant.now();
		Product laptop = product("Laptop Pro", new BigDecimal("22990000"), "Laptop học Spring Boot", now);
		Product mouse = product("Chuột không dây", new BigDecimal("350000"), "Phụ kiện lab", now);
		Product keyboard = product("Bàn phím cơ", new BigDecimal("890000"), "Gõ code", now);

		// 4) Lưu Mongo
		List<Product> saved = productRepository.saveAll(List.of(laptop, mouse, keyboard));
		log.info("[DataSeeder] Seed {} products vào Mongo — gọi GET /api/products để lấy id", saved.size());
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
