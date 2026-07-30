package vn.demo.report.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import vn.demo.report.model.DailyReport;
import vn.demo.report.model.OrderRecord;

/**
 * In-memory: orders (data thô) + daily_reports (snapshot đã precompute).
 *
 * <p>Trong production, {@link #countOrdersOn} / {@link #sumRevenueOn} thường là
 * SQL/aggregation nặng — đúng lý do ta chạy trong Job chứ không trong mọi HTTP request.</p>
 */
@Repository
public class InMemoryReportDataRepository implements ReportDataRepository {

	private final Map<String, OrderRecord> orders = new ConcurrentHashMap<>();
	private final Map<LocalDate, DailyReport> reports = new ConcurrentHashMap<>();

	@Override
	public List<OrderRecord> findAllOrders() {
		return new ArrayList<>(orders.values());
	}

	@Override
	public void saveOrder(OrderRecord order) {
		if (order.getId() == null || order.getId().isBlank()) {
			order.setId(UUID.randomUUID().toString());
		}
		orders.put(order.getId(), order);
	}

	@Override
	public long countOrdersOn(LocalDate date) {
		// Giả lập “aggregation nặng” — lab chỉ đếm trong RAM
		return orders.values().stream()
				.filter(o -> date.equals(o.getOrderDate()))
				.count();
	}

	@Override
	public BigDecimal sumRevenueOn(LocalDate date) {
		return orders.values().stream()
				.filter(o -> date.equals(o.getOrderDate()))
				.map(OrderRecord::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	public Optional<DailyReport> findReportByDate(LocalDate reportDate) {
		return Optional.ofNullable(reports.get(reportDate));
	}

	@Override
	public DailyReport saveReport(DailyReport report) {
		if (report.getId() == null || report.getId().isBlank()) {
			report.setId(UUID.randomUUID().toString());
		}
		reports.put(report.getReportDate(), report);
		return report;
	}

	@Override
	public List<DailyReport> findAllReports() {
		return new ArrayList<>(reports.values());
	}

	@Override
	public void deleteAll() {
		orders.clear();
		reports.clear();
	}

}
