package vn.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.external.NotificationRequest;
import vn.demo.dto.external.NotificationResponse;

/**
 * CLIENT — Order gọi Notify nội bộ {@code POST /internal/notifications/order-success}.
 *
 * <p><b>Không ném exception ra ngoài</b> khi HTTP fail: OrderService đã lưu đơn;
 * Phase 1 chấp nhận mail có thể mất (cầu nối Kafka Phase 2).</p>
 */
@Component
@Slf4j
public class NotificationClient {

	private final RestClient restClient;

	public NotificationClient(@Value("${app.services.notification.url}") String notificationServiceUrl) {
		this.restClient = RestClient.builder().baseUrl(notificationServiceUrl).build();
	}

	public NotificationResponse sendOrderSuccessNotification(NotificationRequest request, String correlationId) {
		log.info("Gui notify order={} to={}", request.getOrderId(), request.getEmail());
		try {
			NotificationResponse response = restClient.post()
					.uri("/internal/notifications/order-success")
					.header("X-Correlation-Id", correlationId != null ? correlationId : "")
					.header("Content-Type", "application/json")
					.body(request)
					.retrieve()
					.body(NotificationResponse.class);

			if (response == null) {
				return failure("No response from notification service");
			}
			return response;
		} catch (Exception e) {
			log.error("Notify that bai order={}", request.getOrderId(), e);
			return failure("Failed to send notification: " + e.getMessage());
		}
	}

	private NotificationResponse failure(String message) {
		NotificationResponse response = new NotificationResponse();
		response.setSuccess(false);
		response.setMessage(message);
		return response;
	}
}
