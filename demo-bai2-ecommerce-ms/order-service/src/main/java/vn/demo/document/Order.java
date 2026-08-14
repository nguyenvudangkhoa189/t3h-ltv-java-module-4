package vn.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Document Entity
 * 
 * Represents customer orders in MongoDB order_db.orders collection.
 * Contains order details, customer information, and embedded order items
 * with product snapshot data for maintaining transactional consistency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String customerEmail;

    private String customerName;

    @Builder.Default
    private String status = "PENDING";

    private BigDecimal totalAmount;

    private List<OrderItem> items;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Calculate total amount from all order items
     * @return total order amount
     */
    public BigDecimal calculateTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(OrderItem::calculateSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if order is confirmed
     * @return true if status is CONFIRMED
     */
    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    /**
     * Check if order is pending
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Confirm the order
     */
    public void confirm() {
        this.status = "CONFIRMED";
    }

    /**
     * Cancel the order
     */
    public void cancel() {
        this.status = "CANCELLED";
    }

    /**
     * Validate order data
     * @return true if order has valid data
     */
    public boolean isValid() {
        return userId != null &&
               customerEmail != null &&
               items != null && !items.isEmpty() &&
               items.stream().allMatch(OrderItem::isValid);
    }
}