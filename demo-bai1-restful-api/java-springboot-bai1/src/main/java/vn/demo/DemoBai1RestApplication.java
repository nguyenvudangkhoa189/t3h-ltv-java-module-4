package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point Bài 1 — REST nâng cao (MapStruct, @Async, Logging, springdoc).
 *
 * <p>{@code @EnableAsync} bật hỗ trợ {@code @Async} (§3). Cấu hình pool nằm ở
 * {@link vn.demo.async.AsyncConfig}.</p>
 */
@SpringBootApplication
@EnableAsync
public class DemoBai1RestApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoBai1RestApplication.class, args);
	}

}
