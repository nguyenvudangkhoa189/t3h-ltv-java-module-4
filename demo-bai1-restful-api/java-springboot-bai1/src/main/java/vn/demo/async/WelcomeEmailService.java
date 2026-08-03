package vn.demo.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Gửi email chào (giả lập) trên thread nền — lab {@code @Async} (§3).
 *
 * <p><b>Kiến thức mới:</b></p>
 * <ul>
 *   <li>{@code @Async} chỉ có hiệu lực khi gọi <b>qua Spring bean</b> (inject từ class khác).
 *       Gọi {@code this.sendWelcomeEmail()} trong cùng class → <b>không</b> async.</li>
 *   <li>Method nên {@code public}; kết quả không chờ trong HTTP response.</li>
 *   <li>Client thường nhận HTTP <b>202 Accepted</b> — email xong sau, xem log.</li>
 * </ul>
 */
@Service
public class WelcomeEmailService {

	private static final Logger log = LoggerFactory.getLogger(WelcomeEmailService.class);

	@Value("${app.email.delay-ms:3000}")
	private long delayMs;

	/**
	 * Giả lập SMTP chậm {@code delayMs} milli giây.
	 *
	 * <p>Quan sát log: tên thread bắt đầu bằng {@code async-} (khác {@code http-nio-...}).</p>
	 */
	@Async
	public void sendWelcomeEmail(String to) {
		// Đánh dấu bắt đầu trên thread pool
		log.info("[{}] Start sending email to {}", Thread.currentThread().getName(), to);
		try {
			// Giả lập I/O chậm — lab so sánh với welcome-sync
			Thread.sleep(delayMs);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Email interrupted for {}", to);
			return;
		}
		// Hoàn tất — thường xảy ra SAU khi client đã nhận 202
		log.info("[{}] Finished email to {}", Thread.currentThread().getName(), to);
	}

}
