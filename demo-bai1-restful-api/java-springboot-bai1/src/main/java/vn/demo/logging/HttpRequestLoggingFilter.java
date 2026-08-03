package vn.demo.logging;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * §4 — ví dụ logging thực tế: ghi mỗi HTTP request (method, URI, status, thời gian).
 *
 * <p><b>DEBUG</b>: luôn in chi tiết. <b>INFO</b>: chỉ in khi chậm hoặc status &gt;= 400
 * (tránh spam production). Đổi {@code logging.level.vn.demo.logging} để quan sát.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		long start = System.currentTimeMillis();
		try {
			// Cho request đi tiếp xuống DispatcherServlet / Controller
			filterChain.doFilter(request, response);
		} finally {
			long tookMs = System.currentTimeMillis() - start;
			String msg = "{} {} -> {} ({} ms)";
			Object[] args = {
					request.getMethod(),
					request.getRequestURI(),
					response.getStatus(),
					tookMs
			};
			// Chi tiết khi học / debug
			if (log.isDebugEnabled()) {
				log.debug(msg, args);
			} else if (response.getStatus() >= 400 || tookMs >= 1000) {
				// Production-friendly: chỉ WARN-ish qua INFO khi lỗi hoặc chậm
				log.info(msg, args);
			}
		}
	}

}
