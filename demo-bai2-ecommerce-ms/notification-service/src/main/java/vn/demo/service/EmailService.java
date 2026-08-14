package vn.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.demo.dto.request.OrderSuccessNotificationRequest;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * SERVICE — gửi mail xác nhận đơn (SMTP) hoặc log khi {@code app.mail.enabled=false}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled}")
    private boolean mailEnabled;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Send order success notification email
     * @param request order notification details
     * @return true if email was sent successfully
     */
    public boolean sendOrderSuccessEmail(OrderSuccessNotificationRequest request) {
        log.info("Sending order success email to: {} for order: {}", 
                request.getEmail(), request.getOrderId());

        try {
            // --- Lab: tắt SMTP → chỉ log, coi như OK ---
            if (!mailEnabled) {
                log.warn("Email sending is disabled. Would send email to: {} for order: {}", 
                        request.getEmail(), request.getOrderId());
                logEmailContent(request);
                return true; // Return success for lab purposes
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getEmail());
            message.setSubject("Order Confirmation - " + request.getOrderId());
            message.setText(buildOrderEmailContent(request));

            mailSender.send(message);
            return true;

        } catch (Exception e) {
            log.error("Failed to send order success email to: {} for order: {}", 
                    request.getEmail(), request.getOrderId(), e);
            return false;
        }
    }

    /**
     * Build email content for order success notification
     */
    private String buildOrderEmailContent(OrderSuccessNotificationRequest request) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormat.format(request.getTotalAmount());

        return String.format("""
            Dear %s,
            
            Thank you for your order! Your order has been confirmed and is being processed.
            
            Order Details:
            - Order ID: %s
            - Total Amount: %s
            
            We will notify you once your order is shipped.
            
            Thank you for choosing our e-commerce platform!
            
            Best regards,
            E-Commerce Team
            """, 
            request.getCustomerName(),
            request.getOrderId(),
            formattedAmount
        );
    }

    /**
     * Log email content when email sending is disabled (for development)
     */
    private void logEmailContent(OrderSuccessNotificationRequest request) {
        log.info("=== EMAIL CONTENT (Mail disabled) ===");
        log.info("To: {}", request.getEmail());
        log.info("Subject: Order Confirmation - {}", request.getOrderId());
        log.info("Content:\n{}", buildOrderEmailContent(request));
        log.info("=== END EMAIL CONTENT ===");
    }
}