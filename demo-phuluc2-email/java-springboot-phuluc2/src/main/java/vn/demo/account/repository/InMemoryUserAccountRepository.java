package vn.demo.account.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import vn.demo.account.model.UserAccount;

/**
 * In-memory store — ConcurrentHashMap (giống pattern demo-phuluc1).
 */
@Repository
public class InMemoryUserAccountRepository implements UserAccountRepository {

	private final Map<String, UserAccount> byId = new ConcurrentHashMap<>();
	private final Map<String, String> emailToId = new ConcurrentHashMap<>();

	@Override
	public UserAccount save(UserAccount account) {
		// Gán id nếu mới
		if (account.getId() == null || account.getId().isBlank()) {
			account.setId(UUID.randomUUID().toString());
		}
		byId.put(account.getId(), account);
		emailToId.put(account.getEmail().toLowerCase(), account.getId());
		return account;
	}

	@Override
	public Optional<UserAccount> findByEmail(String email) {
		String id = emailToId.get(email.toLowerCase());
		if (id == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(byId.get(id));
	}

	@Override
	public List<UserAccount> findAll() {
		return new ArrayList<>(byId.values());
	}

}
