package vn.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.external.ProductResponse;
import vn.demo.exception.ExternalServiceException;

/**
 * CLIENT — Order gọi Product bằng HTTP {@link RestClient} (sync).
 *
 * <h2>Kiến thức mới</h2>
 * <p>URL lấy từ {@code app.services.product.url} — không hard-code trong Java.
 * Forward {@code X-Correlation-Id} để lần cùng request trên log Product.</p>
 */
@Component
@Slf4j
public class ProductClient {

	private final RestClient restClient;

	public ProductClient(@Value("${app.services.product.url}") String productServiceUrl) {
		this.restClient = RestClient.builder().baseUrl(productServiceUrl).build();
	}

	/** GET /api/products/{id} — dùng cho snapshot. */
	public ProductResponse getProduct(String productId, String correlationId) {
		try {
			ProductResponse product = restClient.get()
					.uri("/api/products/{id}", productId)
					.header("X-Correlation-Id", correlationId != null ? correlationId : "")
					.retrieve()
					.body(ProductResponse.class);

			if (product == null) {
				throw new ExternalServiceException("Product service tra null: " + productId);
			}
			return product;
		} catch (ExternalServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Goi Product that bai id={}", productId, e);
			throw new ExternalServiceException("Failed to fetch product details: " + e.getMessage(), e);
		}
	}
}
