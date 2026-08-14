package vn.demo.dto.external;

import lombok.Data;

import java.math.BigDecimal;

/**
 * External Product Response DTO
 * 
 * Represents product information received from Product Service.
 * Used for fetching product details during order processing
 * to create product snapshots with current pricing.
 */
@Data
public class ProductResponse {

    private String id;
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private String categoryId;
    
    private String categoryName;
    
    private String status;
}