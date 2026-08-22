package vn.demo.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import vn.demo.filter.CorrelationIdGatewayFilter;

/**
 * WebFlux đổi thread theo event-loop. MDC là ThreadLocal nên phải đăng ký
 * accessor: Reactor Context {@code cid} ↔ {@code MDC["cid"]} khi
 * {@code spring.reactor.context-propagation=auto}.
 */
@Configuration
public class MdcContextConfig {

	@PostConstruct
	void registerCidAccessor() {
		String key = CorrelationIdGatewayFilter.MDC_KEY;
		ContextRegistry.getInstance().registerThreadLocalAccessor(
				key,
				() -> MDC.get(key),
				value -> {
					if (value == null) {
						MDC.remove(key);
					} else {
						MDC.put(key, value);
					}
				},
				() -> MDC.remove(key));
	}
}
