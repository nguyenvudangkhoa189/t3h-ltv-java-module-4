package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point — Demo Phụ lục 2: Spring Boot gửi email (Gmail SMTP).
 *
 * <p><b>{@code @EnableAsync}</b> bật gửi mail nền (syllabus §5.4). Thiếu annotation này
 * thì {@code @Async} trên {@code WelcomeMailService} <b>không có hiệu lực</b> — SMTP
 * sẽ chạy đồng bộ trên HTTP thread và làm request chậm.</p>
 *
 * <p>Demo cố ý <b>không dùng Mongo/JPA</b> — account in-memory — để tập trung SMTP,
 * {@code JavaMailSender}, text/HTML mail, và pattern Service mỏng (syllabus §2–§5).</p>
 *
 * @see vn.demo.mail.MailService
 * @see vn.demo.mail.WelcomeMailService
 * @see vn.demo.account.service.AccountService
 */
@SpringBootApplication
@EnableAsync
public class DemoPhuluc2EmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoPhuluc2EmailApplication.class, args);
	}

}
