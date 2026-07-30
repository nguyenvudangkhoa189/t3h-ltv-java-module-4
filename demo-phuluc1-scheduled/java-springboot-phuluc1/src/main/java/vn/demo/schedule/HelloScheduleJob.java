package vn.demo.schedule;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * JOB Hello — syllabus §2.
 *
 * <p>{@code fixedRate} = khoảng cách tính từ lúc <b>bắt đầu</b> lần chạy trước
 * (đơn vị milliseconds). Tắt bằng {@code app.hello.enabled=false} khi demo ví dụ 1–2.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.hello.enabled", havingValue = "true", matchIfMissing = true)
public class HelloScheduleJob {

	/**
	 * In log định kỳ — chứng minh Scheduler đang sống (không cần Postman).
	 *
	 * <p>{@code fixedRateString} đọc từ properties để đổi nhịp mà không sửa code.</p>
	 */
	@Scheduled(fixedRateString = "${app.hello.fixed-rate-ms:10000}")
	public void tick() {
		log.info("[HelloScheduleJob] Scheduler đang chạy lúc {}", Instant.now());
	}

}
