package vn.demo.employee.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model nội bộ (in-memory) — <b>không</b> trả thẳng ra API.
 *
 * <p>{@code passwordHash} là field nhạy cảm: chỉ tồn tại ở tầng persistence/domain,
 * Response DTO không có field này.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

	private Long id;
	private String name;
	private String email;
	private String role;
	/** Đã "hash" (demo) — không được lộ JSON ra client. */
	private String passwordHash;

}
