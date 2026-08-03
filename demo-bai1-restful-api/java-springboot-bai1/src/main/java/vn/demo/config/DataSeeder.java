package vn.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.demo.employee.model.Employee;
import vn.demo.employee.repository.EmployeeRepository;

/**
 * Nạp vài employee mẫu khi app start — mở Swagger là có data để Try it out.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final EmployeeRepository repository;

	@Override
	public void run(String... args) {
		// Chỉ seed khi kho trống (tránh nhân đôi nếu restart trong cùng JVM test)
		if (!repository.findAll().isEmpty()) {
			return;
		}

		repository.save(Employee.builder()
				.name("Nguyen Van A")
				.email("a@company.com")
				.role("developer")
				.passwordHash("demo-hash:Secret123!")
				.build());
		repository.save(Employee.builder()
				.name("Tran Thi B")
				.email("b@company.com")
				.role("manager")
				.passwordHash("demo-hash:Secret123!")
				.build());
		repository.save(Employee.builder()
				.name("Le Van C")
				.email("c@company.com")
				.role("developer")
				.passwordHash("demo-hash:Secret123!")
				.build());

		log.info("Seeded {} sample employees", repository.findAll().size());
	}

}
