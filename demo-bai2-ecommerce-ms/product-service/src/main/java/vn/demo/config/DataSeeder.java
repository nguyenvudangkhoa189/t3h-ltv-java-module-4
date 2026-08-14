package vn.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.demo.document.Category;
import vn.demo.document.Product;
import vn.demo.repository.CategoryRepository;
import vn.demo.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * CONFIG — seed category + sản phẩm nếu DB trống. Id Mongo random — test lấy từ GET /api/products.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Seed initial category and product data on application startup
     */
    @Bean
    public ApplicationRunner seedData() {
        return args -> {
            // 1) Check if data already exists
            if (categoryRepository.count() > 0) {
                log.info("Data already exists, skipping seeding");
                return;
            }

            log.info("Seeding initial data...");

            // 2) Create categories
            Category electronics = Category.builder()
                    .name("Electronics")
                    .description("Electronic devices and gadgets")
                    .status("ACTIVE")
                    .build();

            Category clothing = Category.builder()
                    .name("Clothing")
                    .description("Fashion and apparel")
                    .status("ACTIVE")
                    .build();

            Category books = Category.builder()
                    .name("Books")
                    .description("Books and educational materials")
                    .status("ACTIVE")
                    .build();

            // 3) Save categories
            List<Category> savedCategories = categoryRepository.saveAll(
                    List.of(electronics, clothing, books));

            // 4) Get category IDs for products
            String electronicsId = savedCategories.get(0).getId();
            String clothingId = savedCategories.get(1).getId();
            String booksId = savedCategories.get(2).getId();

            // 5) Create products with stable names for testing
            List<Product> products = List.of(
                    Product.builder()
                            .name("iPhone 17 Pro")
                            .description("Latest iPhone with advanced features")
                            .price(new BigDecimal("29990000"))
                            .categoryId(electronicsId)
                            .status("ACTIVE")
                            .build(),

                    Product.builder()
                            .name("Samsung Galaxy S26")
                            .description("Premium Android smartphone")
                            .price(new BigDecimal("24990000"))
                            .categoryId(electronicsId)
                            .status("ACTIVE")
                            .build(),

                    Product.builder()
                            .name("MacBook Pro M5")
                            .description("Professional laptop for developers")
                            .price(new BigDecimal("49990000"))
                            .categoryId(electronicsId)
                            .status("ACTIVE")
                            .build(),

                    Product.builder()
                            .name("Premium T-Shirt")
                            .description("Comfortable cotton t-shirt")
                            .price(new BigDecimal("299000"))
                            .categoryId(clothingId)
                            .status("ACTIVE")
                            .build(),

                    Product.builder()
                            .name("Java Programming Guide")
                            .description("Comprehensive Java programming book")
                            .price(new BigDecimal("599000"))
                            .categoryId(booksId)
                            .status("ACTIVE")
                            .build()
            );

            // 6) Save products
            productRepository.saveAll(products);

            log.info("Data seeding completed successfully");
            log.info("Created {} categories and {} products", 
                    savedCategories.size(), products.size());
        };
    }
}