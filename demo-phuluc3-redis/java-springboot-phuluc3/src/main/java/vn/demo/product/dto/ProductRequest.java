package vn.demo.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO — body tạo / cập nhật Product (syllabus §5).
 *
 * <p>Controller nhận DTO, không nhận Entity trực tiếp (convention module-3/4).</p>
 */
@Getter
@Setter
public class ProductRequest {

	@NotBlank
	@Size(min = 2, max = 120)
	private String name;

	@NotNull
	@DecimalMin(value = "0.0", inclusive = true)
	private BigDecimal price;

	@Size(max = 500)
	private String description;

}
