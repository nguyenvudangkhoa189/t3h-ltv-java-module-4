package vn.demo.product.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import vn.demo.product.model.Product;

/**
 * In-memory store — ConcurrentHashMap (giống pattern demo-phuluc1/2).
 */
@Repository
public class InMemoryProductRepository implements ProductRepository {

	private final Map<Long, Product> byId = new ConcurrentHashMap<>();
	private final AtomicLong seq = new AtomicLong(0);

	@Override
	public Product save(Product product) {
		// 1) Gán id nếu mới
		if (product.getId() == null) {
			product.setId(seq.incrementAndGet());
		}
		// 2) Ghi đè theo id (create hoặc update)
		byId.put(product.getId(), product);
		return product;
	}

	@Override
	public void saveAll(List<Product> products) {
		for (Product p : products) {
			save(p);
		}
	}

	@Override
	public Optional<Product> findById(Long id) {
		return Optional.ofNullable(byId.get(id));
	}

	@Override
	public List<Product> findAll() {
		// Trả list mới, sort theo id — tránh caller sửa map gốc
		return byId.values().stream()
				.sorted(Comparator.comparing(Product::getId))
				.toList();
	}

	@Override
	public boolean existsById(Long id) {
		return byId.containsKey(id);
	}

	@Override
	public void deleteById(Long id) {
		byId.remove(id);
	}

	@Override
	public long count() {
		return byId.size();
	}

}
