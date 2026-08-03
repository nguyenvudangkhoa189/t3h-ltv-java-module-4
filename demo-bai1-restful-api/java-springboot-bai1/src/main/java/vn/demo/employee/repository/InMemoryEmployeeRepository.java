package vn.demo.employee.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import vn.demo.employee.model.Employee;

/**
 * Lưu Employee trong bộ nhớ (ConcurrentHashMap) — không cần Mongo/DB cho lab Bài 1.
 */
@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {

	private final Map<Long, Employee> store = new ConcurrentHashMap<>();
	private final AtomicLong seq = new AtomicLong(0);

	@Override
	public List<Employee> findAll() {
		// Copy + sort theo id để list ổn định khi demo
		List<Employee> list = new ArrayList<>(store.values());
		list.sort(Comparator.comparing(Employee::getId));
		return list;
	}

	@Override
	public Optional<Employee> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public boolean existsByEmail(String email) {
		return store.values().stream()
				.anyMatch(e -> e.getEmail().equalsIgnoreCase(email));
	}

	@Override
	public boolean existsByEmailAndIdNot(String email, Long id) {
		return store.values().stream()
				.anyMatch(e -> e.getEmail().equalsIgnoreCase(email) && !e.getId().equals(id));
	}

	@Override
	public Employee save(Employee employee) {
		// Tạo mới: chưa có id → cấp số tăng dần
		if (employee.getId() == null) {
			employee.setId(seq.incrementAndGet());
		}
		store.put(employee.getId(), employee);
		return employee;
	}

	@Override
	public void deleteById(Long id) {
		store.remove(id);
	}

}
