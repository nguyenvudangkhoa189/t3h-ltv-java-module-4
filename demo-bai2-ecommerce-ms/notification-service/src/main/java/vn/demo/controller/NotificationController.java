package vn.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.demo.dto.request.OrderSuccessNotificationRequest;
import vn.demo.dto.response.NotificationResponse;
import vn.demo.service.NotificationService;

import jakarta.validation.Valid;

/**
 * CONTROLLER nội bộ — chỉ Order gọi, không route Gateway.
 */
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@Validated
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send order success notification
     * POST /internal/notifications/order-success
     * 
     * This endpoint is called by the order service after successful order creation
     * to notify customers via email about their order confirmation.
     */
    @PostMapping("/order-success")
    public ResponseEntity<NotificationResponse> sendOrderSuccessNotification(
            @Valid @RequestBody OrderSuccessNotificationRequest request) {
        
        log.info("Order success notification request received for order: {} to: {}", 
                request.getOrderId(), request.getEmail());

        NotificationResponse response = notificationService.sendOrderSuccessNotification(request);
        // --- 200 dù mail fail: Order không rollback (Phase 1) ---
        return ResponseEntity.ok(response);
    }
}