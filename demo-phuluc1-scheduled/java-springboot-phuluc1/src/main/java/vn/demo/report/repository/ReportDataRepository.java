package vn.demo.report.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import vn.demo.report.model.DailyReport;
import vn.demo.report.model.OrderRecord;

/** Truy cập snapshot báo cáo + đơn hàng thô (in-memory). */
public interface ReportDataRepository {

	List<OrderRecord> findAllOrders();

	void saveOrder(OrderRecord order);

	long countOrdersOn(LocalDate date);

	BigDecimal sumRevenueOn(LocalDate date);

	Optional<DailyReport> findReportByDate(LocalDate reportDate);

	DailyReport saveReport(DailyReport report);

	List<DailyReport> findAllReports();

	void deleteAll();

}
