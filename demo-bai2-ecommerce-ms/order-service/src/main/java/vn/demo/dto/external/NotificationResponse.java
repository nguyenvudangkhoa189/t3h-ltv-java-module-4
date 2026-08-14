package vn.demo.dto.external;

import lombok.Data;

/**
 * External Notification Response DTO
 * 
 * Response format from Notification Service.
 * Indicates whether notification was sent successfully.
 */
@Data
public class NotificationResponse {

    private boolean success;
    
    private String message;
}