package vn.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response DTO
 * 
 * Returns complete order information in API responses.
 * Includes order details, customer info, and all order items
 * with proper data formatting for client consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;
    
    private String userId;
    
    private String customerEmail;
    
    private String customerName;
    
    private String status;
    
    private BigDecimal totalAmount;
    
    private List<OrderItemResponse> items;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}