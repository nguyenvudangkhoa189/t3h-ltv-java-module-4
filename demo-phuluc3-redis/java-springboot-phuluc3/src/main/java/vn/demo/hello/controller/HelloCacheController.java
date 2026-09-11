package vn.demo.hello.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class HelloCacheController {

	private final HelloCacheService helloCacheService;

	@GetMapping("/hello-cache")
	public Map<String, Object> hello(@RequestParam(defaultValue = "HV") String name) {
		// 1) Đo thời gian quanh lời gọi Service (có cache proxy)
		long start = System.currentTimeMillis();
		String message = helloCacheService.greet(name);
		long tookMs = System.currentTimeMillis() - start;

		// 2) Trả message + thời gian — client tự so sánh 2 lần gọi
		return Map.of("message", message, "tookMs", tookMs);
	}

}
