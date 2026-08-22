package vn.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Smoke test 3 API Lab 1 — đảm bảo app chạy trước khi đóng gói Docker.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelloApiSmokeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void hello_returnsMessage() throws Exception {
		mockMvc.perform(get("/api/v1/hello"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Docker Lab 1"));
	}

	@Test
	void courses_returnsList() throws Exception {
		mockMvc.perform(get("/api/v1/courses"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	void courseById_notFound() throws Exception {
		mockMvc.perform(get("/api/v1/courses/99"))
				.andExpect(status().isNotFound());
	}

}
