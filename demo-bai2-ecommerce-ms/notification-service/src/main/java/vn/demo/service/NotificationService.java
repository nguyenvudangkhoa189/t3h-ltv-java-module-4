package vn.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.demo.dto.request.OrderSuccessNotificationRequest;
import vn.demo.dto.response.NotificationResponse;

/**
 * SERVICE — nhận lệnh nội bộ “order-success” rồi ủy quyền {@link EmailService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    /**
     * Send order success notification
     * @param request order notification details
     * @return notification result
     */
    public NotificationResponse sendOrderSuccessNotification(OrderSuccessNotificationRequest request) {
        log.info("Processing order success notification for order: {} to: {}", 
                request.getOrderId(), request.getEmail());

        try {
            // 1) Send order success email
            boolean emailSent = emailService.sendOrderSuccessEmail(request);

            // 2) Build response
            if (emailSent) {
                log.info("Order success notification sent successfully for order: {}", 
                        request.getOrderId());
                
                return NotificationResponse.builder()
                        .success(true)
                        .message("Order success notification sent successfully")
                        .build();
            } else {
                log.warn("Failed to send order success notification for order: {}", 
                        request.getOrderId());
                
                return NotificationResponse.builder()
                        .success(false)
                        .message("Failed to send order success notification")
                        .build();
            }

        } catch (Exception e) {
            log.error("Error processing order success notification for order: {}", 
                    request.getOrderId(), e);
            
            return NotificationResponse.builder()
                    .success(false)
                    .message("Error processing notification: " + e.getMessage())
                    .build();
        }
    }
}