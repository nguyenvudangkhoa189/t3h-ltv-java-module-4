package vn.demo.dto.request;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Create Order Request DTO
 * 
 * Validates order creation input from clients.
 * Contains list of items with product IDs and quantities.
 * User identity is extracted from JWT by gateway, not from client input.
 */
@Data
public class CreateOrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<CreateOrderItemRequest> items;
}