package vn.demo.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.demo.exception.ResourceNotFoundException;
import vn.demo.product.dto.ProductRequest;
import vn.demo.product.model.Product;
import vn.demo.product.repository.ProductRepository;

/**
 * Unit test ProductService — mock Repository (không cần Redis).
 *
 * <p>Annotation {@code @Cacheable}/{@code @CacheEvict} chỉ có hiệu lực qua Spring AOP proxy;
 * unit test Mockito gọi thẳng instance → tập trung logic nghiệp vụ.</p>
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	void getById_found_returnsProduct() {
		// 1) Arrange
		Product p = new Product();
		p.setId(1L);
		p.setName("Laptop");
		p.setPrice(new BigDecimal("1000"));
		when(productRepository.findById(1L)).thenReturn(Optional.of(p));

		// 2) Act
		Product result = productService.getById(1L);

		// 3) Assert
		assertEquals("Laptop", result.getName());
		verify(productRepository).findById(1L);
	}

	@Test
	void getById_missing_throwsNotFound() {
		when(productRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> productService.getById(99L));
	}

	@Test
	void update_changesFields_andSaves() {
		// 1) Product hiện có
		Product existing = new Product();
		existing.setId(1L);
		existing.setName("Cũ");
		existing.setPrice(new BigDecimal("100"));
		when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

		// 2) Request mới
		ProductRequest req = new ProductRequest();
		req.setName("Laptop Pro");
		req.setPrice(new BigDecimal("24990000"));
		req.setDescription("Sau khi update");

		// 3) Update
		Product saved = productService.update(1L, req);

		assertEquals("Laptop Pro", saved.getName());
		assertEquals(new BigDecimal("24990000"), saved.getPrice());
		verify(productRepository).save(existing);
	}

	@Test
	void delete_missing_throwsNotFound() {
		when(productRepository.existsById(99L)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> productService.delete(99L));
	}

}
