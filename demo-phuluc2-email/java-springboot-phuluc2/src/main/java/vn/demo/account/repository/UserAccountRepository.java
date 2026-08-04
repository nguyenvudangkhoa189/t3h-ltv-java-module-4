package vn.demo.account.repository;

import java.util.List;
import java.util.Optional;

import vn.demo.account.model.UserAccount;

/**
 * REPOSITORY — abstraction lưu account (syllabus §5).
 *
 * <p>Demo: in-memory. Production đổi sang JPA/Mongo mà không đổi Service signature.</p>
 */
public interface UserAccountRepository {

	UserAccount save(UserAccount account);

	Optional<UserAccount> findByEmail(String email);

	List<UserAccount> findAll();

}
