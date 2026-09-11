package vn.demo.product.repository;

import java.util.List;
import java.util.Optional;

import vn.demo.product.model.Product;

/**
 * REPOSITORY — abstraction “DB giả” (syllabus §5).
 *
 * <p>Production có thể đổi sang JPA/Mongo mà không đổi chữ ký Service cache.</p>
 */
public interface ProductRepository {

	Product save(Product product);

	void saveAll(List<Product> products);

	Optional<Product> findById(Long id);

	List<Product> findAll();

	boolean existsById(Long id);

	void deleteById(Long id);

	long count();

}
