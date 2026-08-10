package vn.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.RoleModel;

public interface RoleRepository extends MongoRepository<RoleModel, String> {

	Optional<RoleModel> findByCode(String code);

	List<RoleModel> findByIdIn(Collection<String> ids);

}
