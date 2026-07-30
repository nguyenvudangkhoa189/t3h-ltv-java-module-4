package vn.demo.report.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.report.model.DailyReport;
import vn.demo.report.service.DailyReportService;

/**
 * JOB mỏng — ví dụ 2 (syllabus §6).
 *
 * <p>{@code zone} trên {@code @Scheduled} quan trọng: cron “2:00” phải là 2:00
 * <b>Asia/Ho_Chi_Minh</b>, không phụ thuộc timezone JVM máy chủ cloud.</p>
 *
 * <p>Lab mặc định cron mỗi 2 phút (properties) để quan sát; production dùng
 * {@code 0 0 2 * * *}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportJob {

	private final DailyReportService dailyReportService;

	@Scheduled(
			cron = "${app.report.daily-cron:0 0 2 * * *}",
			zone = "Asia/Ho_Chi_Minh")
	public void generateDailyReport() {
		// Chỉ gọi Service — không aggregation trong Job
		DailyReport report = dailyReportService.generateYesterday();
		log.info("[DailyReportJob] Đã sinh báo cáo ngày {} — orders={}, revenue={}",
				report.getReportDate(), report.getOrderCount(), report.getTotalRevenue());
	}

}
