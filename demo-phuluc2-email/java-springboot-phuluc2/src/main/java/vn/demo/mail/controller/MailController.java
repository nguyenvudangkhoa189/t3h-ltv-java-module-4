package vn.demo.mail.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import vn.demo.mail.MailService;

/**
 * CONTROLLER — API lab thử gửi mail (syllabus §3–§4).
 *
 * <p>Chỉ dùng để học viên kiểm tra SMTP nhanh bằng Postman/curl.
 * Flow nghiệp vụ thật nằm ở {@code POST /api/accounts/register}.</p>
 */
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

	private final MailService mailService;

	/** §3 — gửi text; request sẽ chờ đến khi SMTP xong (đồng bộ). */
	@PostMapping("/hello")
	public ResponseEntity<Map<String, String>> hello(@RequestParam String to) {
		mailService.sendText(to, "Hello từ Spring Boot", "Đây là email text đầu tiên (Phụ lục 2).");
		return ResponseEntity.ok(Map.of(
				"message", "Đã gửi text — kiểm tra Inbox/Spam của " + to));
	}

	/** §4 — gửi HTML; cũng đồng bộ (lab so sánh với welcome @Async). */
	@PostMapping("/hello-html")
	public ResponseEntity<Map<String, String>> helloHtml(@RequestParam String to)
			throws MessagingException {
		String html = """
				<h2>Hello HTML</h2>
				<p>Đây là email <b>HTML</b> từ Spring Boot (UTF-8: Xin chào).</p>
				""";
		mailService.sendHtml(to, "Hello HTML từ Spring Boot", html);
		return ResponseEntity.ok(Map.of(
				"message", "Đã gửi HTML — kiểm tra Inbox/Spam của " + to));
	}

}
