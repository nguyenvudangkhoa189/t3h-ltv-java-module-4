package vn.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.demo.dto.request.ProductCreateRequest;
import vn.demo.dto.request.ProductUpdateRequest;
import vn.demo.dto.response.ProductResponse;
import vn.demo.service.ProductService;

import jakarta.validation.Valid;
import java.util.List;

/**
 * CONTROLLER — catalog. GET public; POST/PUT/DELETE: Bearer ADMIN (Gateway).
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products (public access)
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Get all products request received");
        
        // 1) Get all active products
        List<ProductResponse> products = productService.getAllProducts();
        
        // 2) Return product list
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID (public access)
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        log.info("Get product request received for ID: {}", id);
        
        // 1) Get product details
        ProductResponse product = productService.getProductById(id);
        
        // 2) Return product
        return ResponseEntity.ok(product);
    }

    /**
     * Create new product (admin only - authorization handled at gateway)
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        log.info("Create product request received: {}", request.getName());
        
        // 1) Create product
        ProductResponse product = productService.createProduct(request);
        
        // 2) Return created product
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Update product (admin only - authorization handled at gateway)
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Update product request received for ID: {}", id);
        
        // 1) Update product
        ProductResponse product = productService.updateProduct(id, request);
        
        // 2) Return updated product
        return ResponseEntity.ok(product);
    }

    /**
     * Delete product (admin only - authorization handled at gateway)
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("Delete product request received for ID: {}", id);
        
        // 1) Delete product
        productService.deleteProduct(id);
        
        // 2) Return no content
        return ResponseEntity.noContent().build();
    }

    /**
     * Search products by name (public access)
     * GET /api/products/search?name=...
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String name) {
        log.info("Search products request received for name: {}", name);
        
        // 1) Search products
        List<ProductResponse> products = productService.searchProducts(name);
        
        // 2) Return search results
        return ResponseEntity.ok(products);
    }
}