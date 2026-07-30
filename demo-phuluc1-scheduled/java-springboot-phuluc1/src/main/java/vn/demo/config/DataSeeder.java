package vn.demo.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.account.model.UserAccount;
import vn.demo.account.repository.UserAccountRepository;
import vn.demo.account.service.UserAccountService;
import vn.demo.report.model.OrderRecord;
import vn.demo.report.repository.ReportDataRepository;

/**
 * Seed dữ liệu lab khi app start — để thấy Job khóa account / báo cáo ngay.
 *
 * <p>Account:</p>
 * <ul>
 *   <li>{@code stale@demo.vn} — PENDING, createdAt = now - 25h → <b>sẽ bị khóa</b></li>
 *   <li>{@code fresh@demo.vn} — PENDING, createdAt = now - 1h → còn hạn</li>
 *   <li>{@code active@demo.vn} — đã ACTIVATED → job bỏ qua</li>
 * </ul>
 *
 * <p>Orders: vài đơn của <b>hôm qua</b> (zone VN) → DailyReportJob có số liệu để tổng hợp.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

	private final UserAccountRepository userAccountRepository;
	private final ReportDataRepository reportDataRepository;

	@Value("${app.account.seed-on-startup:true}")
	private boolean seedAccounts;

	@Value("${app.report.seed-on-startup:true}")
	private boolean seedOrders;

	@Value("${app.report.zone:Asia/Ho_Chi_Minh}")
	private String zoneId;

	@Override
	public void run(ApplicationArguments args) {
		if (seedAccounts && userAccountRepository.count() == 0) {
			seedAccounts();
		}
		if (seedOrders && reportDataRepository.findAllOrders().isEmpty()) {
			seedOrders();
		}
	}

	private void seedAccounts() {
		Instant now = Instant.now();

		UserAccount stale = account("stale@demo.vn", UserAccountService.STATUS_PENDING,
				now.minusSeconds(25 * 3600L), null);
		UserAccount fresh = account("fresh@demo.vn", UserAccountService.STATUS_PENDING,
				now.minusSeconds(3600L), null);
		UserAccount active = account("active@demo.vn", UserAccountService.STATUS_ACTIVATED,
				now.minusSeconds(48 * 3600L), now.minusSeconds(47 * 3600L));

		userAccountRepository.saveAll(List.of(stale, fresh, active));
		log.info("[DataSeeder] Seed 3 accounts — stale(PENDING 25h), fresh(PENDING 1h), active(ACTIVATED)");
	}

	private void seedOrders() {
		LocalDate yesterday = LocalDate.now(ZoneId.of(zoneId)).minusDays(1);
		reportDataRepository.saveOrder(order(yesterday, "150000"));
		reportDataRepository.saveOrder(order(yesterday, "220000"));
		reportDataRepository.saveOrder(order(yesterday, "99000"));
		log.info("[DataSeeder] Seed 3 orders cho ngày {} (hôm qua theo {})", yesterday, zoneId);
	}

	private static UserAccount account(String email, String status, Instant createdAt, Instant activatedAt) {
		UserAccount u = new UserAccount();
		u.setEmail(email);
		u.setStatus(status);
		u.setCreatedAt(createdAt);
		u.setActivatedAt(activatedAt);
		u.setUpdatedAt(createdAt);
		return u;
	}

	private static OrderRecord order(LocalDate date, String amount) {
		OrderRecord o = new OrderRecord();
		o.setOrderDate(date);
		o.setAmount(new BigDecimal(amount));
		return o;
	}

}
