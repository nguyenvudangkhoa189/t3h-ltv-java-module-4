package vn.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.PermissionModel;

public interface PermissionRepository extends MongoRepository<PermissionModel, String> {

	Optional<PermissionModel> findByCode(String code);

	List<PermissionModel> findByIdIn(Collection<String> ids);

}
