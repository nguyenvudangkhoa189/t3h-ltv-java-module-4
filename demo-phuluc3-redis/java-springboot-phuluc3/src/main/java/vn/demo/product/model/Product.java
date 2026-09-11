package vn.demo.product.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL — sản phẩm in-memory (syllabus §5).
 *
 * <p>Demo không dùng DB: đủ để minh họa GET cache + PUT/DELETE evict.
 * Cần {@code @NoArgsConstructor} để Jackson deserialize từ Redis JSON.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Product {

	private Long id;
	private String name;
	private BigDecimal price;
	private String description;
	private Instant updatedAt;

}
