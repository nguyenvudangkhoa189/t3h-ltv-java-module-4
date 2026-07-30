package vn.demo.report.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.report.model.DailyReport;
import vn.demo.report.service.DailyReportService;

/**
 * REST ví dụ 2: đọc snapshot + (lab) kích hoạt generate thủ công.
 *
 * <p>GET chỉ đọc — minh họa “bưng món đã nấu”. POST generate chỉ để lab không
 * phải đợi cron; production thường chỉ để Job đêm chạy.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/reports/daily")
@RequiredArgsConstructor
public class DailyReportController {

	private final DailyReportService dailyReportService;

	/** GET /api/reports/daily — mọi snapshot đã có. */
	@GetMapping
	public List<DailyReport> list() {
		return dailyReportService.findAll();
	}

	/**
	 * GET /api/reports/daily/by-date?date=2026-07-29
	 *
	 * <p>Chỉ đọc snapshot — <b>không</b> chạy aggregation.</p>
	 */
	@GetMapping("/by-date")
	public DailyReport getByDate(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return dailyReportService.getByDate(date);
	}

	/**
	 * POST /api/reports/daily/generate?date=2026-07-29
	 *
	 * <p>Lab: chạy ngay logic giống Job (không đợi cron). Bỏ {@code date} → hôm qua.</p>
	 */
	@PostMapping("/generate")
	public ResponseEntity<DailyReport> generate(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		DailyReport report = (date == null)
				? dailyReportService.generateYesterday()
				: dailyReportService.generateFor(date);
		log.info("[API] generate báo cáo date={}", report.getReportDate());
		return ResponseEntity.ok(report);
	}

}
