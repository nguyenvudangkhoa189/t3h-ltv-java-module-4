package vn.demo.account.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import vn.demo.account.model.UserAccount;

/**
 * Implement in-memory — đủ cho lab Scheduled (không cần DB).
 *
 * <p>{@link ConcurrentHashMap} an toàn khi job và REST cùng đọc/ghi
 * trên một JVM (demo 1 instance).</p>
 */
@Repository
public class InMemoryUserAccountRepository implements UserAccountRepository {

	private final Map<String, UserAccount> store = new ConcurrentHashMap<>();

	@Override
	public List<UserAccount> findAll() {
		return new ArrayList<>(store.values());
	}

	@Override
	public Optional<UserAccount> findById(String id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public UserAccount save(UserAccount account) {
		// Gán id nếu chưa có (tạo mới)
		if (account.getId() == null || account.getId().isBlank()) {
			account.setId(UUID.randomUUID().toString());
		}
		store.put(account.getId(), account);
		return account;
	}

	@Override
	public List<UserAccount> saveAll(List<UserAccount> accounts) {
		accounts.forEach(this::save);
		return accounts;
	}

	@Override
	public List<UserAccount> findByStatusAndCreatedAtBefore(String status, Instant before) {
		// Lọc theo đúng điều kiện job khóa account (syllabus §5)
		return store.values().stream()
				.filter(a -> status.equals(a.getStatus()))
				.filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isBefore(before))
				.toList();
	}

	@Override
	public long count() {
		return store.size();
	}

	@Override
	public void deleteAll() {
		store.clear();
	}

}
