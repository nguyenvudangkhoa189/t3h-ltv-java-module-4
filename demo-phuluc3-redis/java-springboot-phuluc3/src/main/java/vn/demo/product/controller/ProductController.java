package vn.demo.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.product.dto.ProductRequest;
import vn.demo.product.model.Product;
import vn.demo.product.service.ProductService;

/**
 * CONTROLLER — CRUD Product; cache nằm ở Service (syllabus §5).
 *
 * <p>Controller mỏng: nhận request / trả response — không gọi Redis trực tiếp.</p>
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public List<Product> findAll() {
		return productService.findAll();
	}

	@GetMapping("/{id}")
	public Product getById(@PathVariable Long id) {
		return productService.getById(id);
	}

	@PostMapping
	public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
		// 1) Tạo qua Service (có @CacheEvict)
		Product created = productService.create(request);
		// 2) 201 Created
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
