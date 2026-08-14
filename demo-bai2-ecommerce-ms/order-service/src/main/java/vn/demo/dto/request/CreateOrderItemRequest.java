package vn.demo.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Create Order Item Request DTO
 * 
 * Validates individual item input for order creation.
 * Client only provides product ID and quantity; price and name
 * are fetched from product service to prevent price manipulation.
 */
@Data
public class CreateOrderItemRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}