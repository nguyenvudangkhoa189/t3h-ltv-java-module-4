package vn.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.demo.document.Product;

import java.util.List;

/**
 * Product Repository Interface
 * 
 * Provides data access layer for Product entities in MongoDB.
 * Supports product catalog operations with filtering by status and category.
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * Find all products by status
     * @param status product status (ACTIVE/INACTIVE)
     * @return list of products with specified status
     */
    List<Product> findByStatus(String status);

    /**
     * Find all products in a category
     * @param categoryId category ID
     * @return list of products in category
     */
    List<Product> findByCategoryId(String categoryId);

    /**
     * Find active products in a category
     * @param categoryId category ID
     * @param status product status
     * @return list of active products in category
     */
    List<Product> findByCategoryIdAndStatus(String categoryId, String status);

    /**
     * Find products by name containing (case-insensitive search)
     * @param name partial product name
     * @return list of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);
}