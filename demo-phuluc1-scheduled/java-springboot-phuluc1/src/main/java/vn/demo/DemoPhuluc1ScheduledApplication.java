package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point — Demo Phụ lục 1: Spring Boot Scheduled.
 *
 * <p><b>{@code @EnableScheduling}</b> bật bộ lập lịch của Spring. Thiếu annotation này
 * thì mọi {@code @Scheduled} <b>không chạy</b> (lỗi hay gặp nhất với HV mới).</p>
 *
 * <p>Demo cố ý <b>không dùng Mongo/JPA</b> — lưu in-memory — để tập trung vào
 * {@code @Scheduled}, cron, và pattern Job mỏng / Service dày (syllabus §2–§6).</p>
 *
 * @see vn.demo.schedule.HelloScheduleJob
 * @see vn.demo.account.schedule.AccountLockJob
 * @see vn.demo.report.schedule.DailyReportJob
 */
@SpringBootApplication
@EnableScheduling
public class DemoPhuluc1ScheduledApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoPhuluc1ScheduledApplication.class, args);
	}

}
