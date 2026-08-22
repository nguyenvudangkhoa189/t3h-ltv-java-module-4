package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ENTRY — Bài 7 Lab 2: Mini Project (copy Module 3 Bài 10) + Docker.
 *
 * <p><b>Nghiệp vụ không đổi</b> so với M3 Bài 10 (public Anime + Admin Security/CRUD/Dashboard).
 * Phần mới nằm ngoài Java: {@code Dockerfile} multi-stage, {@code docker-compose.yml}
 * (app + Mongo + volume + env URI).</p>
 *
 * <p>Flow khởi động: load context → {@code AdminUserSeeder} / {@code MovieNormalizeRunner}
 * → nhận request qua Spring Security filter → Controller.</p>
 */
@SpringBootApplication
public class DemoBai7DockerApplication {

	public static void main(String[] args) {
		// --- Khởi động Spring Boot (cùng app Mini Project Bài 10) ---
		SpringApplication.run(DemoBai7DockerApplication.class, args);
	}

}
