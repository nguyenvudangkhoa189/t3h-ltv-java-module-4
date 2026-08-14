package vn.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.demo.dto.request.CreateOrderRequest;
import vn.demo.dto.response.OrderResponse;
import vn.demo.service.OrderService;

import jakarta.validation.Valid;
import java.util.List;

/**
 * CONTROLLER — đặt hàng / xem đơn. Identity từ Gateway (không JWT tại đây).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Create order request received with {} items", request.getItems().size());
        
        // 1) Create order through service
        OrderResponse order = orderService.createOrder(request);
        
        // 2) Return created order
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get my orders (current user's orders)
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        log.info("Get my orders request received");
        
        // 1) Get user's orders
        List<OrderResponse> orders = orderService.getMyOrders();
        
        // 2) Return order list
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by ID (user can only access their own orders)
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        log.info("Get order request received for ID: {}", id);
        
        // 1) Get order details with ownership check
        OrderResponse order = orderService.getOrderById(id);
        
        // 2) Return order
        return ResponseEntity.ok(order);
    }
}