package vn.demo.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * External Notification Request DTO
 * 
 * Request format for sending notifications to Notification Service.
 * Contains order success information needed for customer email notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String orderId;
    
    private String email;
    
    private String customerName;
    
    private BigDecimal totalAmount;
}