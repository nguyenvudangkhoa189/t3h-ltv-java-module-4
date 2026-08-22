package vn.demo.dto;

/**
 * DTO khoá học — dữ liệu cố định trong memory (không Model / Repository).
 *
 * <p>Dùng {@code record} cho DTO bất biến, serialize JSON tự động với Jackson.</p>
 */
public record CourseDto(int id, String name, String level) {
}
