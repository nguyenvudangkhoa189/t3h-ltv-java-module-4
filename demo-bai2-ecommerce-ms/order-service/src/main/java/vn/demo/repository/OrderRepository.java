package vn.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.demo.document.Order;

import java.util.List;

/**
 * Order Repository Interface
 * 
 * Provides data access layer for Order entities in MongoDB.
 * Supports order management operations with user-based filtering
 * and status-based queries for order lifecycle management.
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    /**
     * Find all orders for a specific user
     * @param userId user ID
     * @return list of user's orders
     */
    List<Order> findByUserId(String userId);

    /**
     * Find orders by user ID and status
     * @param userId user ID
     * @param status order status
     * @return list of user's orders with specific status
     */
    List<Order> findByUserIdAndStatus(String userId, String status);

    /**
     * Find all orders by status
     * @param status order status
     * @return list of orders with specific status
     */
    List<Order> findByStatus(String status);

    /**
     * Count orders by user ID
     * @param userId user ID
     * @return number of orders for user
     */
    long countByUserId(String userId);
}