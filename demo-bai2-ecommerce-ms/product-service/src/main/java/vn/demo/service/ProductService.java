package vn.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.demo.document.Category;
import vn.demo.document.Product;
import vn.demo.dto.request.ProductCreateRequest;
import vn.demo.dto.request.ProductUpdateRequest;
import vn.demo.dto.response.ProductResponse;
import vn.demo.exception.BadRequestException;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.repository.CategoryRepository;
import vn.demo.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE — catalog Product. GET public (qua Gateway); ghi do Gateway kiểm ROLE_ADMIN.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /** Danh sách SP ACTIVE — Order cũng dùng getById cho snapshot. */
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findByStatus("ACTIVE");
        return products.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return convertToResponse(product);
    }

    /** Tạo SP — category phải ACTIVE. */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Tao SP {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found"));
        if (!category.isActive()) {
            throw new BadRequestException("Category is not active");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .status("ACTIVE")
                .build();
        
        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        log.info("Cap nhat SP {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // --- Chỉ ghi field client gửi ---
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        
        if (StringUtils.hasText(request.getCategoryId())) {
            // Validate new category
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found"));
            if (!category.isActive()) {
                throw new BadRequestException("Category is not active");
            }
            product.setCategoryId(request.getCategoryId());
        }
        
        if (StringUtils.hasText(request.getStatus())) {
            product.setStatus(request.getStatus());
        }
        
        Product updatedProduct = productRepository.save(product);
        return convertToResponse(updatedProduct);
    }

    /** Soft delete: INACTIVE (Order cũ vẫn giữ snapshot). */
    @Transactional
    public void deleteProduct(String id) {
        log.info("An SP {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    public List<ProductResponse> searchProducts(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .filter(Product::isActive)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Document → DTO; gắn tên category (không lộ Mongo entity). */
    private ProductResponse convertToResponse(Product product) {
        String categoryName = null;
        if (StringUtils.hasText(product.getCategoryId())) {
            categoryName = categoryRepository.findById(product.getCategoryId())
                    .map(Category::getName)
                    .orElse("Unknown Category");
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}