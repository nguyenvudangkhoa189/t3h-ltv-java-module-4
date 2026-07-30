package vn.demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Nâng cao (syllabus §9.1) — pool > 1 thread cho nhiều {@code @Scheduled}.
 *
 * <p>Mặc định Spring dùng <b>1 thread</b>: job A chậm làm job B trễ.
 * Bật bằng {@code app.scheduling.pool.enabled=true} khi giảng phần nâng cao.</p>
 *
 * <p>Dùng {@link ThreadPoolTaskScheduler} (có lifecycle) thay vì
 * {@code Executors.newScheduledThreadPool} trần — app shutdown sẽ dừng pool gọn.</p>
 *
 * <p><b>Không</b> gắn thêm {@code @EnableScheduling} ở đây — đã có trên Application.</p>
 */
@Configuration
@ConditionalOnProperty(name = "app.scheduling.pool.enabled", havingValue = "true")
public class SchedulingConfig implements SchedulingConfigurer {

	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(3);
		scheduler.setThreadNamePrefix("demo-sched-");
		scheduler.initialize();
		registrar.setTaskScheduler(scheduler);
	}

}
