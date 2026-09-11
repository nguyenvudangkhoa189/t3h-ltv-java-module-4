package vn.demo.hello.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import vn.demo.hello.HelloCacheService;

/**
 * CONTROLLER — API lab đo thời gian HIT/MISS (syllabus §4).
 *
 * <p>Trả thêm {@code tookMs} để HV so sánh lần 1 (~2000ms+) và lần 2 (vài ms).</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Hello Cache", description = "§4 — cảm nhận @Cacheable HIT/MISS (giả lập chậm 2s)")
public class HelloCacheController {

	private final HelloCacheService helloCacheService;

	@GetMapping("/hello-cache")
	@Operation(
			summary = "Greet có cache theo name",
			description = "Lần 1 cùng name ≈ 2s+ (MISS); lần 2 vài ms (HIT) nếu chưa hết TTL.")
	public Map<String, Object> hello(
			@Parameter(description = "Tên — thành một phần cache key", example = "Khoa")
			@RequestParam(defaultValue = "HV") String name) {
		// 1) Đo thời gian quanh lời gọi Service (có cache proxy)
		long start = System.currentTimeMillis();
		String message = helloCacheService.greet(name);
		long tookMs = System.currentTimeMillis() - start;

		// 2) Trả message + thời gian — client tự so sánh 2 lần gọi
		return Map.of("message", message, "tookMs", tookMs);
	}

}
