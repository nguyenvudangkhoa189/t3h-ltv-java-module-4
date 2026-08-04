package vn.demo.mail;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE — email chào mừng sau đăng ký (syllabus §5).
 *
 * <p>Tách khỏi {@link MailService}: class này chứa <b>nội dung nghiệp vụ</b>
 * (tiêu đề, HTML template ngắn); {@code MailService} chỉ biết cách gửi SMTP.</p>
 *
 * <p><b>Kiến thức mới — {@code @Async}:</b> method chạy trên thread pool
 * ({@code async-*}), không chặn HTTP. Chỉ có hiệu lực khi gọi <b>qua Spring bean</b>
 * (inject từ {@code AccountService}). Gọi {@code this.sendWelcome(...)} trong
 * cùng class → <b>không</b> async.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeMailService {

	private final MailService mailService;

	/**
	 * Gửi HTML chào mừng — chạy nền ({@code @Async}).
	 *
	 * <p>Lỗi SMTP được bắt + log: đăng ký vẫn thành công dù mail thất bại
	 * (lab). Production có thể retry / đưa vào hàng đợi.</p>
	 */
	@Async
	public void sendWelcome(String to, String displayName) {
		// 1) Log thread — phải khác http-nio-* nếu @Async hoạt động
		log.info("[{}] Bắt đầu gửi welcome tới {}", Thread.currentThread().getName(), to);

		// 2) Ghép HTML ngắn (lab — production thường dùng Thymeleaf template)
		String html = """
				<h2>Xin chào %s!</h2>
				<p>Cảm ơn bạn đã đăng ký. Chúc bạn học vui với Spring Boot Email.</p>
				<p><a href="https://demo.vn/activate">Kích hoạt tài khoản</a></p>
				""".formatted(displayName);

		// 3) Gửi qua MailService; lỗi chỉ log (không ném ra HTTP vì đã async)
		try {
			mailService.sendHtml(to, "Chào mừng đến Demo Phụ lục 2", html);
			log.info("[{}] Đã gửi welcome tới {}", Thread.currentThread().getName(), to);
		} catch (Exception ex) {
			log.error("[WelcomeMail] Gửi thất bại tới {}: {}", to, ex.getMessage());
		}
	}

}
