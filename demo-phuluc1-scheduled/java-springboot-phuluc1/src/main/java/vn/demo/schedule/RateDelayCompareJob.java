package vn.demo.schedule;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo so sánh {@code fixedRate} vs {@code fixedDelay} — syllabus §3.
 *
 * <p>Mặc định <b>tắt</b> ({@code app.rate-delay-demo.enabled=false}) để console sạch.
 * Khi giảng §3: bật property → quan sát log.</p>
 *
 * <p><b>Kiến thức:</b></p>
 * <ul>
 *   <li>{@code fixedRate}: nhịp theo giờ đồng hồ từ lúc <b>start</b> lần trước</li>
 *   <li>{@code fixedDelay}: chỉ bắt đầu đếm sau khi method <b>kết thúc</b></li>
 * </ul>
 * <p>Method cố ý {@code Thread.sleep(2000)} để thấy sự khác biệt rõ hơn.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.rate-delay-demo.enabled", havingValue = "true")
public class RateDelayCompareJob {

	@Scheduled(fixedRate = 5_000)
	public void fixedRateDemo() throws InterruptedException {
		log.info("[fixedRate ] START  {}", Instant.now());
		// Giả lập việc mất ~2 giây — fixedRate vẫn “hẹn” theo mốc start
		Thread.sleep(2_000);
		log.info("[fixedRate ] END    {}", Instant.now());
	}

	@Scheduled(fixedDelay = 5_000)
	public void fixedDelayDemo() throws InterruptedException {
		log.info("[fixedDelay] START  {}", Instant.now());
		Thread.sleep(2_000);
		log.info("[fixedDelay] END    {}", Instant.now());
	}

}
