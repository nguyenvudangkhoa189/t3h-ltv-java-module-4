package vn.demo.mapping;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean ModelMapper cho lab so sánh §2.2.
 *
 * <p>API chính dùng <b>MapStruct</b> ({@code EmployeeService}).
 * {@code EmployeeModelMapperService} chỉ để HV chạy thử cùng bài toán bằng ModelMapper.</p>
 */
@Configuration
public class MapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();
		// Bỏ qua null khi map — tránh ghi đè field đích bằng null ngoài ý muốn
		mapper.getConfiguration().setSkipNullEnabled(true);
		return mapper;
	}

}
