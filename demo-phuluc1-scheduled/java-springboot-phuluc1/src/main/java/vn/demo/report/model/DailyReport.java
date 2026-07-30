package vn.demo.report.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL — snapshot báo cáo ngày đã được job tính sẵn (syllabus §6).
 *
 * <p>API chỉ <b>đọc</b> document này — không chạy aggregation nặng mỗi request.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DailyReport {

	private String id;
	/** Ngày báo cáo (thường = hôm qua khi job chạy lúc 2:00). */
	private LocalDate reportDate;
	private long orderCount;
	private BigDecimal totalRevenue;
	private Instant generatedAt;

}
