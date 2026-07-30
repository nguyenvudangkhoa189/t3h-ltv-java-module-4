package vn.demo.report.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Đơn hàng mẫu — nguồn “data thô” để aggregation (giả lập bảng orders lớn).
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderRecord {

	private String id;
	private LocalDate orderDate;
	private BigDecimal amount;

}
