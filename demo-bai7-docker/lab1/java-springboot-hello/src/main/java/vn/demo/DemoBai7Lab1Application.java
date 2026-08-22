package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ENTRY — Bài 7 Lab 1: API dữ liệu cố định (không DB) để luyện Docker.
 *
 * <p>Mục tiêu app: vài REST endpoint hard-code. Phần Docker (Dockerfile 1 stage,
 * {@code docker build}/{@code run}) nằm ngoài codebase — xem README demo.</p>
 */
@SpringBootApplication
public class DemoBai7Lab1Application {

	public static void main(String[] args) {
		// --- Khởi động Spring Boot (embedded Tomcat :8080) ---
		SpringApplication.run(DemoBai7Lab1Application.class, args);
	}

}
