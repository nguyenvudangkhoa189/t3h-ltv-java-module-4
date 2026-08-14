package vn.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItem Embedded Document
 * 
 * Represents individual items within an order with product snapshot information.
 * Stores product details at time of purchase to maintain data consistency
 * even if product information changes after order placement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String productId;
    
    private String productName;
    
    private BigDecimal price;
    
    private int quantity;
    
    private BigDecimal subTotal;

    /**
     * Calculate subtotal for this order item
     * @return subtotal amount (price * quantity)
     */
    public BigDecimal calculateSubTotal() {
        if (price != null && quantity > 0) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Validate order item data
     * @return true if item has valid data
     */
    public boolean isValid() {
        return productId != null && 
               productName != null && 
               price != null && 
               price.compareTo(BigDecimal.ZERO) > 0 && 
               quantity > 0;
    }
}