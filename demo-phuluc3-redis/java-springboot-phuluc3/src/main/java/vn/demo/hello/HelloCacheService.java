package vn.demo.hello;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE — Hello cache để cảm nhận HIT / MISS (syllabus §4).
 *
 * <p><b>Kiến thức mới — {@code @Cacheable}:</b></p>
 * <ul>
 *   <li>{@code cacheNames = "hello"} — tên “ngăn” cache (TTL cấu hình ở
 *       {@link vn.demo.config.RedisCacheConfig}).</li>
 *   <li>{@code key = "#name"} — SpEL: tham số {@code name} thành một phần key Redis.</li>
 *   <li>Method <b>chỉ chạy</b> khi MISS; HIT → trả giá trị đã cache,
 *       <b>không</b> vào thân method (không {@code sleep}).</li>
 * </ul>
 *
 * <p><b>Lưu ý proxy:</b> phải gọi qua Spring bean (Controller inject Service).
 * Gọi {@code this.greet(...)} trong cùng class → bỏ qua AOP → không cache.</p>
 */
@Slf4j
@Service
public class HelloCacheService {

	/**
	 * Lần đầu MISS (chậm ~2s); lần sau cùng {@code name} là HIT (nhanh) nếu chưa hết TTL.
	 */
	@Cacheable(cacheNames = "hello", key = "#name")
	public String greet(String name) {
		// 1) Log MISS — nếu thấy dòng này thì thân method đang chạy (chưa có trong Redis)
		log.info("[HelloCache] MISS — đang giả lập xử lý chậm cho name={}", name);

		// 2) Giả lập I/O / DB chậm — HIT sẽ bỏ qua đoạn này
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// 3) Kết quả sẽ được Spring Cache ghi vào Redis sau khi method return
		return "Xin chào, " + name + "!";
	}

}
