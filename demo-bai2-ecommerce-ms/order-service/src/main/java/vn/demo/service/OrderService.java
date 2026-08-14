package vn.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.demo.client.NotificationClient;
import vn.demo.client.ProductClient;
import vn.demo.document.Order;
import vn.demo.document.OrderItem;
import vn.demo.dto.external.NotificationRequest;
import vn.demo.dto.external.ProductResponse;
import vn.demo.dto.request.CreateOrderItemRequest;
import vn.demo.dto.request.CreateOrderRequest;
import vn.demo.dto.response.OrderItemResponse;
import vn.demo.dto.response.OrderResponse;
import vn.demo.exception.BadRequestException;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.repository.OrderRepository;
import vn.demo.security.GatewayUserPrincipal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE — tạo / xem đơn.
 *
 * <h2>Kiến thức mới</h2>
 * <ul>
 *   <li>Identity lấy từ Gateway ({@code X-User-*}), không parse JWT.</li>
 *   <li>Giá/tên: HTTP {@link ProductClient} — snapshot vào document, không tin client.</li>
 *   <li>Notify sync HTTP: lỗi mail <b>không</b> rollback đơn (Phase 1, chưa Saga).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final NotificationClient notificationClient;

    /**
     * Tạo đơn: snapshot Product → lưu order_db → gọi Notify.
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        // --- 1) User từ SecurityContext (GatewayUserFilter) ---
        GatewayUserPrincipal user = getCurrentUser();
        String correlationId = MDC.get("cid");

        log.info("Tao don user={} items={}", user.getEmail(), request.getItems().size());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            // --- 2) RestClient → Product (không đọc product_db) ---
            ProductResponse product = productClient.getProduct(itemRequest.getProductId(), correlationId);

            // --- 3) Chỉ ACTIVE ---
            if (!"ACTIVE".equals(product.getStatus())) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }

            // --- 4) Snapshot giá/tên lúc mua ---
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .subTotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubTotal());
        }

        // --- 5) Lưu PENDING ---
        Order order = Order.builder()
                .userId(user.getUserId())
                .customerEmail(user.getEmail())
                .customerName(user.getEmail())
                .status("PENDING")
                .items(orderItems)
                .totalAmount(totalAmount)
                .build();

        Order savedOrder = orderRepository.save(order);

        // --- 6) CONFIRMED sau khi persist ---
        savedOrder.confirm();
        savedOrder = orderRepository.save(savedOrder);
        log.info("Don da luu id={}", savedOrder.getId());

        // --- 7) Notify sync — catch: don da thanh cong van giu ---
        try {
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .orderId(savedOrder.getId())
                    .email(savedOrder.getCustomerEmail())
                    .customerName(savedOrder.getCustomerName())
                    .totalAmount(savedOrder.getTotalAmount())
                    .build();
            notificationClient.sendOrderSuccessNotification(notificationRequest, correlationId);
        } catch (Exception e) {
            log.warn("Failed to send order notification for order: {} - continuing anyway",
                    savedOrder.getId(), e);
        }

        // --- 8) DTO ra API ---
        return convertToResponse(savedOrder);
    }

    /** Danh sách đơn của user đang login. */
    public List<OrderResponse> getMyOrders() {
        GatewayUserPrincipal user = getCurrentUser();
        log.debug("Lay don cua {}", user.getEmail());
        return orderRepository.findByUserId(user.getUserId()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Chi tiết đơn — chỉ chủ đơn. */
    public OrderResponse getOrderById(String orderId) {
        GatewayUserPrincipal user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!user.getUserId().equals(order.getUserId())) {
            throw new AccessDeniedException("Access denied to order: " + orderId);
        }
        return convertToResponse(order);
    }

    /** Principal do GatewayUserFilter gắn từ header tin cậy. */
    private GatewayUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof GatewayUserPrincipal)) {
            throw new RuntimeException("Invalid user principal type");
        }

        return (GatewayUserPrincipal) principal;
    }

    /** Document Mongo → Response DTO (không lộ entity). */
    private OrderResponse convertToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subTotal(item.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .customerEmail(order.getCustomerEmail())
                .customerName(order.getCustomerName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}