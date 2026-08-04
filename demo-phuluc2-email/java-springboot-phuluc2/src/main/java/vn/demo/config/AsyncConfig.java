package vn.demo.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Cấu hình thread pool cho {@code @Async} (syllabus §5.4 — khuyến khích).
 *
 * <p><b>Kiến thức mới:</b> Gửi SMTP thường mất 1–3 giây. Nếu chạy trên thread HTTP,
 * client phải chờ. {@code @Async} đẩy việc gửi sang pool riêng; HTTP trả
 * {@code 202 Accepted} ngay. Prefix thread {@code async-} giúp nhìn rõ trên log
 * (khác {@code http-nio-*}).</p>
 *
 * <p>Giống pattern Module 4 — Bài 1 ({@code AsyncConfig}).</p>
 */
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// Số thread tối thiểu luôn sẵn sàng
		executor.setCorePoolSize(2);
		// Trần khi tải cao
		executor.setMaxPoolSize(5);
		// Hàng đợi chờ nếu pool đầy
		executor.setQueueCapacity(100);
		// Tên thread hiện trong log — chứng minh mail không chạy trên HTTP thread
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
