package vn.demo.report.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.report.model.DailyReport;
import vn.demo.report.repository.ReportDataRepository;

/**
 * SERVICE — sinh / đọc báo cáo ngày (syllabus §6).
 *
 * <p><b>Pattern precompute:</b> Job gọi {@link #generateYesterday()} (hoặc {@link #generateFor})
 * một lần → lưu snapshot. HTTP chỉ gọi {@link #getByDate} — <b>không</b> aggregation lại.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

	private final ReportDataRepository reportDataRepository;

	/** Múi giờ chốt “hôm qua” — khớp annotation zone trên Job. */
	@Value("${app.report.zone:Asia/Ho_Chi_Minh}")
	private String zoneId;

	/**
	 * Tính báo cáo cho một ngày rồi lưu snapshot (idempotent: ghi đè nếu đã có).
	 *
	 * <p>Đây là chỗ “nặng” — trong hệ thống thật có thể mất vài phút trên triệu bản ghi.
	 * Vì vậy ta chạy theo lịch (đêm), không gắn vào mỗi lần user mở dashboard.</p>
	 */
	public DailyReport generateFor(LocalDate reportDate) {
		log.info("[DailyReportService] generateFor START — reportDate={}", reportDate);

		// 1) Aggregation từ data thô (giả lập query nặng)
		long orderCount = reportDataRepository.countOrdersOn(reportDate);
		BigDecimal revenue = reportDataRepository.sumRevenueOn(reportDate);

		// 2) Lấy snapshot cũ nếu có — giữ id; không thì tạo mới
		DailyReport report = reportDataRepository.findReportByDate(reportDate)
				.orElseGet(DailyReport::new);

		// 3) Ghi kết quả đã tính sẵn
		report.setReportDate(reportDate);
		report.setOrderCount(orderCount);
		report.setTotalRevenue(revenue);
		report.setGeneratedAt(Instant.now());

		DailyReport saved = reportDataRepository.saveReport(report);
		log.info("[DailyReportService] generateFor DONE — date={}, orders={}, revenue={}",
				reportDate, orderCount, revenue);
		return saved;
	}

	/** Job gọi: chốt báo cáo <b>ngày hôm qua</b> theo zone cấu hình. */
	public DailyReport generateYesterday() {
		ZoneId zone = ZoneId.of(zoneId);
		LocalDate yesterday = LocalDate.now(zone).minusDays(1);
		return generateFor(yesterday);
	}

	/**
	 * Đọc snapshot — dùng cho API.
	 *
	 * <p><b>Không</b> gọi {@code countOrdersOn} / {@code sumRevenueOn} tại đây.</p>
	 */
	public DailyReport getByDate(LocalDate reportDate) {
		return reportDataRepository.findReportByDate(reportDate)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Chưa có báo cáo cho ngày " + reportDate + " — đợi job chạy hoặc gọi generate"));
	}

	public List<DailyReport> findAll() {
		return reportDataRepository.findAllReports();
	}

}
