package vn.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.demo.dto.CourseDto;

/**
 * CONTROLLER — 3 API tĩnh để kiểm tra container Docker Lab 1.
 *
 * <p>Prefix {@code /api/v1} (ôn versioning Module 4 Bài 1). Không DB, không Service
 * — giữ app nhỏ để tập trung {@code docker build} / {@code docker run}.</p>
 */
@RestController
@RequestMapping("/api/v1")
public class HelloController {

	// --- Danh sách cố định trong code (thay DB) ---
	private static final List<CourseDto> COURSES = List.of(
			new CourseDto(1, "Java Spring Boot", "M3"),
			new CourseDto(2, "REST & Docker", "M4"),
			new CourseDto(3, "Mini Project Movie", "M3 Bài 10"));

	/**
	 * Smoke-check: container lên là endpoint này trả JSON ngay.
	 */
	@GetMapping("/hello")
	public Map<String, String> hello() {
		// --- Trả message cố định — dùng curl sau docker run ---
		return Map.of("message", "Docker Lab 1");
	}

	/**
	 * Danh sách khoá học hard-code (3 phần tử).
	 */
	@GetMapping("/courses")
	public List<CourseDto> courses() {
		// --- Không query DB — trả list tĩnh ---
		return COURSES;
	}

	/**
	 * Chi tiết theo id; không tìm thấy → HTTP 404 (không ném exception custom).
	 */
	@GetMapping("/courses/{id}")
	public ResponseEntity<CourseDto> course(@PathVariable int id) {
		// --- Tìm trong list tĩnh; Optional → ResponseEntity ---
		return COURSES.stream()
				.filter(c -> c.id() == id)
				.findFirst()
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

}
