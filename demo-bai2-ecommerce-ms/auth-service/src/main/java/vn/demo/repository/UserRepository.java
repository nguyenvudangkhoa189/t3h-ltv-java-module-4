package vn.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.demo.document.User;

import java.util.Optional;

/**
 * User Repository Interface
 * 
 * Provides data access layer for User entities in MongoDB.
 * Supports authentication queries by email and user management operations.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find user by email address (used for login)
     * @param email user's email
     * @return Optional user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists (for registration validation)
     * @param email email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);
}