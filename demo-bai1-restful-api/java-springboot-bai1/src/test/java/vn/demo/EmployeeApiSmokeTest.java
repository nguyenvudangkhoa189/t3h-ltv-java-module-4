package vn.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Smoke test — CRUD + search page + welcome 202.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiSmokeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void searchAndCreateAndWelcome_flow() throws Exception {
		// Seed đã có developer — search trả content
		mockMvc.perform(get("/api/v1/employees")
						.param("role", "developer")
						.param("page", "0")
						.param("size", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.page").value(0));

		String body = """
				{
				  "name": "Test User",
				  "email": "test.user@company.com",
				  "role": "hr",
				  "password": "Secret123!"
				}
				""";

		MvcResult created = mockMvc.perform(post("/api/v1/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("test.user@company.com"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andReturn();

		String id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id")
				.toString();
		assertThat(id).isNotBlank();

		mockMvc.perform(post("/api/v1/employees/" + id + "/welcome"))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.message").value("Email is being processed"));
	}

}
