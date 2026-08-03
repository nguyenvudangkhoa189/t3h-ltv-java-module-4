package vn.demo.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vn.demo.employee.dto.EmployeeRequestDto;
import vn.demo.employee.dto.EmployeeResponseDto;
import vn.demo.employee.model.Employee;

/**
 * MapStruct mapper (§2.3) — Spring sinh bean {@code EmployeeMapperImpl} lúc compile.
 *
 * <p><b>Kiến thức mới:</b> khác ModelMapper (reflection runtime), MapStruct sinh code Java
 * thật trong {@code target/generated-sources}. Field lệch tên / thiếu {@code @Mapping}
 * → thường <b>fail compile</b>, an toàn hơn khi refactor.</p>
 *
 * <p>{@code componentModel = "spring"} → có thể {@code @Autowired} / constructor-inject.</p>
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

	/** Model → Response: cùng tên field tự map; passwordHash không có trên DTO → bị bỏ. */
	EmployeeResponseDto toDto(Employee employee);

	/**
	 * Request → Model khi tạo mới.
	 *
	 * <ul>
	 *   <li>{@code id} ignore — repository tự sinh</li>
	 *   <li>{@code passwordHash} ignore — Service hash {@code password} rồi set thủ công</li>
	 * </ul>
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	Employee toEntity(EmployeeRequestDto dto);

}
