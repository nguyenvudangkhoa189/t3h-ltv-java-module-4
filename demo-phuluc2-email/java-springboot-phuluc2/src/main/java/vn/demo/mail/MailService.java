package vn.demo.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE — tầng gửi email hạ tầng (syllabus §3–§4).
 *
 * <p><b>Vai trò:</b> bọc {@link JavaMailSender} — text ({@link SimpleMailMessage})
 * và HTML ({@link MimeMessageHelper}). Nghiệp vụ (chào mừng, quên mật khẩu…)
 * gọi class này, <b>không</b> gọi SMTP trực tiếp từ Controller.</p>
 *
 * <p><b>Kiến thức mới — {@code JavaMailSender}:</b> Spring Boot tự tạo bean này
 * khi có {@code spring-boot-starter-mail} + đủ {@code spring.mail.host}.
 * Lab dùng Gmail: {@code smtp.gmail.com:587} + <b>App Password</b>
 * (không phải mật khẩu đăng nhập — xem syllabus §2.1).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender mailSender;

	@Value("${app.mail.from}")
	private String from;

	/**
	 * Gửi email text thuần (syllabus §3).
	 *
	 * <p>Dùng {@link SimpleMailMessage} — đủ cho lab / thông báo ngắn.
	 * Không hỗ trợ HTML hay đính kèm.</p>
	 */
	public void sendText(String to, String subject, String body) {
		// 1) Tạo message text
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);

		// 2) Gửi qua SMTP (đồng bộ — có thể mất vài giây)
		mailSender.send(message);
		log.info("[MailService] Đã gửi text tới {}", to);
	}

	/**
	 * Gửi email HTML (syllabus §4).
	 *
	 * <p><b>Kiến thức mới — {@code MimeMessageHelper}:</b></p>
	 * <ul>
	 *   <li>{@code setText(html, true)} — tham số {@code true} = nội dung HTML
	 *       (không phải text thuần).</li>
	 *   <li>Charset {@code UTF-8} — bắt buộc để tiếng Việt không lỗi font.</li>
	 *   <li>{@code multipart = true} — sẵn sàng đính kèm sau này (nâng cao).</li>
	 * </ul>
	 */
	public void sendHtml(String to, String subject, String htmlBody) throws MessagingException {
		// 1) Tạo MimeMessage (hỗ trợ HTML / multipart)
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		// 2) Điền From / To / Subject / Body HTML
		helper.setFrom(from);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(htmlBody, true); // true = HTML

		// 3) Gửi qua SMTP
		mailSender.send(message);
		log.info("[MailService] Đã gửi HTML tới {}", to);
	}

}
