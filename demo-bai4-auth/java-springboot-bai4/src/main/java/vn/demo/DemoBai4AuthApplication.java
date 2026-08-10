package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry — Demo Bài 4: JWT Authn + RBAC + Permission ({@code hasAuthority}).
 *
 * <p>Syllabus: {@code 4_java_m4_bai4_Authentication_Authorization.md} — Phần 3 lab
 * (tính năng 1→6).</p>
 *
 * <p>JWT chỉ mang identity; Permission nạp từ Mongo mỗi request (Phần 2).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class DemoBai4AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoBai4AuthApplication.class, args);
	}

}
