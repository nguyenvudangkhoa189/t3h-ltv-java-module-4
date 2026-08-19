package vn.demo.filter;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * GATEWAY — luôn <b>tự sinh</b> {@code X-Correlation-Id} (không tin header client).
 *
 * <p>Client công khai có thể gửi id trùng / giả. Biên Gateway tạo UUID, gỡ header cũ,
 * forward xuống service và trả lại trên response để lần log {@code [cid=…]}.</p>
 *
 * <p>WebFlux: ghi MDC <b>trước</b> khi subscribe chain (không dùng {@code doOnEach}
 * sau khi inner filter đã log). Giá trị nằm trong Reactor Context để thread-hop
 * vẫn điền {@code [cid=]}.</p>
 */
@Slf4j
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

		ServerWebExchange mutated = exchange.mutate().request(request).build();
		String method = request.getMethod() != null ? request.getMethod().name() : "?";
		String path = request.getPath().value();

		// --- 3) Context + MDC trước khi filter sau (JWT) chạy ---
		return Mono.deferContextual(ctx -> {
					String cid = ctx.get(MDC_KEY);
					MDC.put(MDC_KEY, cid);
					log.info("Gateway {} {}", method, path);
					return chain.filter(mutated);
				})
				.doFinally(signalType -> MDC.remove(MDC_KEY))
				.contextWrite(Context.of(MDC_KEY, finalCid));
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
