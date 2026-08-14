package vn.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Document Entity
 * 
 * Represents products in MongoDB product_db.products collection.
 * Contains product information including pricing, categorization, and status
 * for the e-commerce catalog management system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    private BigDecimal price;

    @Indexed
    private String categoryId;

    @Builder.Default
    private String status = "ACTIVE";

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Check if product is active and available
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Check if product has valid price
     * @return true if price is greater than zero
     */
    public boolean hasValidPrice() {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }
}