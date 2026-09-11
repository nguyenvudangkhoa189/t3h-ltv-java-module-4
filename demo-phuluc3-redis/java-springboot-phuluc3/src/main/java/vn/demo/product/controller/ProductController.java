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
 * CONTROLLER — CRUD Product (Mongo); cache nằm ở Service (syllabus §5).
 *
 * <p>Controller mỏng: nhận request / trả response — không gọi Redis/Mongo trực tiếp.</p>
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "§5 — Mongo nguồn sự thật; GET @Cacheable; POST/PUT/DELETE @CacheEvict")
public class ProductController {

	private final ProductService productService;

	@GetMapping
	@Operation(summary = "Liệt kê products", description = "Cache key cố định 'all'. Seed lần đầu tạo 3 product.")
	public List<Product> findAll() {
		return productService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Lấy product theo id (Mongo _id)",
			description = "Lần 1 MISS (log Mongo); lần 2 HIT. Sau PUT/DELETE phải thấy data mới. "
					+ "Lấy id từ GET /api/products hoặc response seed.")
	@ApiResponse(responseCode = "404", description = "Không tìm thấy product")
	public Product getById(
			@Parameter(description = "Mongo ObjectId của product", example = "66f000000000000000000001")
			@PathVariable String id) {
		return productService.getById(id);
	}

	@PostMapping
	@Operation(summary = "Tạo product (lưu Mongo)", description = "Evict toàn bộ cache products.")
	@ApiResponse(responseCode = "201", description = "Đã tạo")
	public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
		// 1) Tạo qua Service (Mongo + @CacheEvict)
		Product created = productService.create(request);
		// 2) 201 Created
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Cập nhật product (Mongo)", description = "Evict cache — GET sau đó phải MISS + giá mới.")
	@ApiResponse(responseCode = "404", description = "Không tìm thấy product")
	public Product update(
			@Parameter(description = "Mongo ObjectId", example = "66f000000000000000000001")
			@PathVariable String id,
			@Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Xóa product (Mongo)", description = "Evict toàn bộ cache products.")
	@ApiResponse(responseCode = "204", description = "Đã xóa")
	@ApiResponse(responseCode = "404", description = "Không tìm thấy product")
	public ResponseEntity<Void> delete(
			@Parameter(description = "Mongo ObjectId", example = "66f000000000000000000001")
			@PathVariable String id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
