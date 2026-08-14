package vn.demo.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * FILTER — gắn Correlation Id vào MDC để log {@code [cid=…]}.
 *
 * <h2>Kiến thức mới</h2>
 * <p>Một request xuyên nhiều process. Header {@code X-Correlation-Id} + MDC
 * giúp lần cùng chuỗi trên console Auth/Product/Order/Notify.</p>
 */
@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

	private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	private static final String MDC_KEY = "cid";

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		try {
			// --- 1) Nhận từ Gateway (hoặc RestClient); thiếu thì tự tạo ---
			String correlationId = request.getHeader(CORRELATION_ID_HEADER);
			if (correlationId == null || correlationId.isBlank()) {
				correlationId = UUID.randomUUID().toString();
			}

			// --- 2) MDC → logging.pattern.console [cid=%X{cid}] ---
			MDC.put(MDC_KEY, correlationId);
			response.setHeader(CORRELATION_ID_HEADER, correlationId);

			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}
