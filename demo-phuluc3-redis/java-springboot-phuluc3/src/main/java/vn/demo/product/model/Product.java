package vn.demo.product.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL — sản phẩm MongoDB collection {@code products} (syllabus §5).
 *
 * <p>Nguồn sự thật = Mongo. Redis chỉ cache bản đọc nhanh ({@code @Cacheable}).
 * Convention giống demo-bai4: {@code @Document} + {@code @Id} kiểu {@link String}.</p>
 *
 * <p>Cần {@code @NoArgsConstructor} để Jackson deserialize từ Redis JSON
 * và Spring Data Mongo map document.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

	/** Mongo {@code _id} — Spring Data tự gán khi insert. */
	@Id
	private String id;

	private String name;
	private BigDecimal price;
	private String description;
	private Instant updatedAt;

}
