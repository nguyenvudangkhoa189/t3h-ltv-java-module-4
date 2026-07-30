package vn.demo.account.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import vn.demo.account.model.UserAccount;

/**
 * Hợp đồng truy cập dữ liệu account.
 *
 * <p>Demo dùng implement in-memory ({@link InMemoryUserAccountRepository})
 * để không phụ thuộc Mongo/JPA — production có thể đổi sang Spring Data.</p>
 */
public interface UserAccountRepository {

	List<UserAccount> findAll();

	Optional<UserAccount> findById(String id);

	UserAccount save(UserAccount account);

	List<UserAccount> saveAll(List<UserAccount> accounts);

	/**
	 * Tìm account theo status và {@code createdAt} < cutoff.
	 *
	 * <p>Đây là query cốt lõi của ví dụ 1: “PENDING quá 24h” =
	 * {@code status=PENDING_ACTIVATION} + {@code createdAt < now - 24h}.</p>
	 */
	List<UserAccount> findByStatusAndCreatedAtBefore(String status, Instant before);

	long count();

	void deleteAll();

}
