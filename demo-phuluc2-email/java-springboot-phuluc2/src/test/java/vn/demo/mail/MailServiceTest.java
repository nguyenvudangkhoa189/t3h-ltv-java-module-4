package vn.demo.mail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test MailService — mock {@link JavaMailSender}, không cần Gmail / mạng.
 *
 * <p>Giống hướng Module 3 Bài 8: test Service bằng Mockito, không spin SMTP.</p>
 */
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

	@Mock
	private JavaMailSender mailSender;

	@InjectMocks
	private MailService mailService;

	@BeforeEach
	void setFrom() {
		// Inject @Value app.mail.from (không load Spring context)
		ReflectionTestUtils.setField(mailService, "from", "lab@gmail.com");
	}

	@Test
	void sendText_setsFromToSubjectAndCallsSender() {
		// 1) Gọi API Service
		mailService.sendText("user@example.com", "Hello", "Body text");

		// 2) Bắt message đã gửi
		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());

		// 3) Kiểm tra From / To / Subject / Body
		SimpleMailMessage msg = captor.getValue();
		org.junit.jupiter.api.Assertions.assertEquals("lab@gmail.com", msg.getFrom());
		org.junit.jupiter.api.Assertions.assertArrayEquals(
				new String[] { "user@example.com" }, msg.getTo());
		org.junit.jupiter.api.Assertions.assertEquals("Hello", msg.getSubject());
		org.junit.jupiter.api.Assertions.assertEquals("Body text", msg.getText());
	}

	@Test
	void sendHtml_createsMimeAndCallsSender() throws Exception {
		// 1) Mock createMimeMessage — cần object thật tối thiểu
		jakarta.mail.internet.MimeMessage mime =
				new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
		when(mailSender.createMimeMessage()).thenReturn(mime);

		// 2) Gửi HTML
		mailService.sendHtml("user@example.com", "Hi HTML", "<b>xin chào</b>");

		// 3) Đảm bảo send(MimeMessage) được gọi
		verify(mailSender).send(any(jakarta.mail.internet.MimeMessage.class));
	}

}
