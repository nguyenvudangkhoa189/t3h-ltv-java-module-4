package vn.demo.employee.repository;

import java.util.List;
import java.util.Optional;

import vn.demo.employee.model.Employee;

/**
 * Abstraction lưu trữ Employee — dễ đổi InMemory → JPA/Mongo sau này.
 */
public interface EmployeeRepository {

	List<Employee> findAll();

	Optional<Employee> findById(Long id);

	boolean existsByEmail(String email);

	/** true nếu email đã thuộc employee khác (dùng khi update). */
	boolean existsByEmailAndIdNot(String email, Long id);

	Employee save(Employee employee);

	void deleteById(Long id);

}
