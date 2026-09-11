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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Products", description = "§5 — GET @Cacheable; POST/PUT/DELETE @CacheEvict")
public class ProductController {

	private final ProductService productService;

	@GetMapping
	@Operation(summary = "Liệt kê products", description = "Cache key cố định 'all'.")
	public List<Product> findAll() {
		return productService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Lấy product theo id",
			description = "Lần 1 MISS (log Repository); lần 2 HIT. Sau PUT/DELETE phải thấy data mới.")
	@ApiResponse(responseCode = "404", description = "Không tìm thấy product")
	public Product getById(
			@Parameter(description = "ID product (seed: 1, 2, 3)", example = "1")
			@PathVariable Long id) {
		return productService.getById(id);
	}

	@PostMapping
	@Operation(summary = "Tạo product", description = "Evict toàn bộ cache products.")
	@ApiResponse(responseCode = "201", description = "Đã tạo")
	public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
		// 1) Tạo qua Service (có @CacheEvict)
		Product created = productService.create(request);
		// 2) 201 Created
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Cập nhật product", description = "Evict cache — GET sau đó phải MISS + giá mới.")
	@ApiResponse(responseCode = "404", description = "Không tìm thấy product")
	public Product update(
			@Parameter(description = "ID product", example = "1")
			@PathVariable Long id,
			@Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Xóa product", description = "Evict toàn bộ cache products.")
	@ApiResponse(responseCode = "204", description = "Đã xóa")
	@ApiResponse(responseCode = "404", description = "Không tìm thấy product")
	public ResponseEntity<Void> delete(
			@Parameter(description = "ID product", example = "1")
			@PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
