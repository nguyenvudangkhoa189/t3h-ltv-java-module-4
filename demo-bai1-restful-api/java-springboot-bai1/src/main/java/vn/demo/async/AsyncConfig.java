package vn.demo.async;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Cấu hình thread pool cho {@code @Async} (§3).
 *
 * <p><b>Vì sao cần?</b> Mặc định Spring dùng executor đơn giản, không tái sử dụng pool tốt.
 * Lab cấu hình {@link ThreadPoolTaskExecutor} với prefix {@code async-} để nhìn rõ trên log
 * khác thread {@code http-nio-*}.</p>
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

	private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// Số thread tối thiểu luôn sẵn sàng
		executor.setCorePoolSize(2);
		// Trần khi tải cao
		executor.setMaxPoolSize(5);
		// Hàng đợi chờ nếu pool đầy
		executor.setQueueCapacity(100);
		// Tên thread hiện trong log — chứng minh @Async không chạy trên HTTP thread
		executor.setThreadNamePrefix("async-");
		executor.initialize();
		return executor;
	}

	/** Exception trên method {@code @Async} kiểu void — log ERROR (không đẩy về HTTP). */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) ->
				log.error("Async error in {}: {}", method.getName(), ex.getMessage(), ex);
	}

}
