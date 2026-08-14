package vn.demo.filter;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * GATEWAY — luôn <b>tự sinh</b> {@code X-Correlation-Id} (không tin header client).
 *
 * <p>Client công khai có thể gửi id trùng / giả. Biên Gateway tạo UUID, gỡ header cũ,
 * forward xuống service và trả lại trên response để lần log {@code [cid=…]}.</p>
 *
 * <p>WebFlux: gắn MDC từ Reactor Context vì thread có thể đổi.</p>
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

	public static final String HEADER = "X-Correlation-Id";
	public static final String MDC_KEY = "cid";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// --- 1) Luôn sinh UUID — không dùng giá trị client gửi ---
		String finalCid = UUID.randomUUID().toString();

		// --- 2) Gỡ header cũ, forward xuống service + trả client ---
		ServerHttpRequest request = exchange.getRequest().mutate()
				.headers(h -> {
					h.remove(HEADER);
					h.set(HEADER, finalCid);
				})
				.build();
		exchange.getResponse().getHeaders().set(HEADER, finalCid);

		// --- 3) WebFlux: MDC theo Reactor Context (thread có thể đổi) ---
		return chain.filter(exchange.mutate().request(request).build())
				.doOnEach(signal -> {
					if (signal.getContextView().hasKey(MDC_KEY)) {
						MDC.put(MDC_KEY, signal.getContextView().get(MDC_KEY));
					}
				})
				.contextWrite(Context.of(MDC_KEY, finalCid))
				.doFinally(signalType -> MDC.remove(MDC_KEY));
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
