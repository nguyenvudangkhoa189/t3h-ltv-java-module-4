package vn.demo.product.service;

import java.time.Instant;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.product.dto.ProductRequest;
import vn.demo.product.model.Product;
import vn.demo.product.repository.ProductRepository;

/**
 * SERVICE — nghiệp vụ Product + Redis cache (syllabus §5).
 *
 * <p><b>Quy tắc vàng:</b> đọc = {@code @Cacheable}; ghi/xoá = {@code @CacheEvict}.
 * Quên evict → client có thể thấy giá / mô tả cũ (stale) cho tới khi hết TTL.</p>
 *
 * <p><b>Kiến thức mới — {@code @CacheEvict(allEntries = true)}:</b> xoá toàn bộ
 * entry trong cache {@code products} (cả key {@code #id} lẫn {@code 'all'}).
 * Lab dùng cách này cho đơn giản; production tinh hơn: {@code @Caching} evict
 * từng key (syllabus §8).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	/**
	 * Đọc theo id — MISS thì đọc Repository rồi ghi Redis; HIT thì không vào thân method.
	 */
	@Cacheable(cacheNames = "products", key = "#id")
	public Product getById(Long id) {
		// 1) Log MISS — thấy dòng này = đang đọc “DB giả”, chưa có trong Redis
		log.info("[ProductService] MISS — đọc Repository id={}", id);

		// 2) Đọc nguồn sự thật; không có → 404
		return productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", id));
	}

	/**
	 * Danh sách ngắn — cache key cố định {@code 'all'} (SpEL chuỗi literal).
	 */
	@Cacheable(cacheNames = "products", key = "'all'")
	public List<Product> findAll() {
		log.info("[ProductService] MISS — findAll Repository");
		return productRepository.findAll();
	}

	/**
	 * Tạo mới — đổi nguồn sự thật → evict toàn bộ cache {@code products}.
	 */
	@CacheEvict(cacheNames = "products", allEntries = true)
	public Product create(ProductRequest request) {
		// 1) Map DTO → entity mới
		Product created = toNewProduct(request);

		// 2) Lưu in-memory
		Product saved = productRepository.save(created);
		log.info("[ProductService] create id={} — evict all products cache", saved.getId());

		// 3) Trả entity (cache đã được Spring xoá nhờ @CacheEvict)
		return saved;
	}

	/**
	 * Cập nhật — bắt buộc evict để GET sau đó không trả bản cũ.
	 */
	@CacheEvict(cacheNames = "products", allEntries = true)
	public Product update(Long id, ProductRequest request) {
		// 1) Đọc trực tiếp Repository (không qua getById — tránh phụ thuộc cache khi đang ghi)
		Product existing = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", id));

		// 2) Gán field từ DTO
		existing.setName(request.getName());
		existing.setPrice(request.getPrice());
		existing.setDescription(request.getDescription());
		existing.setUpdatedAt(Instant.now());

		// 3) Lưu + log evict
		Product saved = productRepository.save(existing);
		log.info("[ProductService] update id={} — evict all products cache", id);
		return saved;
	}

	/**
	 * Xoá — evict toàn bộ cache products.
	 */
	@CacheEvict(cacheNames = "products", allEntries = true)
	public void delete(Long id) {
		// 1) Kiểm tra tồn tại
		if (!productRepository.existsById(id)) {
			throw new ResourceNotFoundException("Product", id);
		}

		// 2) Xoá nguồn sự thật
		productRepository.deleteById(id);
		log.info("[ProductService] delete id={} — evict all products cache", id);
	}

	private static Product toNewProduct(ProductRequest request) {
		Product p = new Product();
		p.setName(request.getName());
		p.setPrice(request.getPrice());
		p.setDescription(request.getDescription());
		p.setUpdatedAt(Instant.now());
		return p;
	}

}
