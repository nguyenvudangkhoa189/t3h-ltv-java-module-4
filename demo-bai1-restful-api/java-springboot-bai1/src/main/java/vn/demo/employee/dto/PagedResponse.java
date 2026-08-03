package vn.demo.employee.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Contract phân trang offset — giống Module 3 {@code PagedResponse}.
 *
 * <p>{@code page} bắt đầu từ <b>0</b>. Không trả thẳng {@code Page} của Spring Data
 * (demo này in-memory nên tự tính metadata).</p>
 *
 * @param <T> kiểu phần tử (thường là DTO)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PagedResponse")
public class PagedResponse<T> {

	private List<T> content;
	/** Chỉ số trang hiện tại, bắt đầu từ 0. */
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;

}
