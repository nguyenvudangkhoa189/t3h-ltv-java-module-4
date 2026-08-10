package vn.demo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.UserModel;

public interface UserRepository extends MongoRepository<UserModel, String> {

	Optional<UserModel> findByUsername(String username);

	Optional<UserModel> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

}
