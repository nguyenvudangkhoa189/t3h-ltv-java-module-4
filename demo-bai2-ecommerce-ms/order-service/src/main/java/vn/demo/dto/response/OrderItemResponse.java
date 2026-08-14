package vn.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Order Item Response DTO
 * 
 * Returns order item details in API responses.
 * Contains product snapshot information at time of order placement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private String productId;
    
    private String productName;
    
    private BigDecimal price;
    
    private int quantity;
    
    private BigDecimal subTotal;
}