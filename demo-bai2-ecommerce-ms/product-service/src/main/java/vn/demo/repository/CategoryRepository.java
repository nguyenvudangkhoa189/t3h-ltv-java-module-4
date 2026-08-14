package vn.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.demo.document.Category;

import java.util.List;
import java.util.Optional;

/**
 * Category Repository Interface
 * 
 * Provides data access layer for Category entities in MongoDB.
 * Supports category management operations and status-based queries.
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    /**
     * Find category by name
     * @param name category name
     * @return Optional category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Find all active categories
     * @return list of active categories
     */
    List<Category> findByStatus(String status);

    /**
     * Check if category name already exists
     * @param name category name to check
     * @return true if name exists
     */
    boolean existsByName(String name);
}