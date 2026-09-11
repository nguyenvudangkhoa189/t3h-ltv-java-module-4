package vn.demo.hello;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test HelloCacheService — gọi thẳng instance (không Spring cache proxy).
 *
 * <p>Chỉ kiểm tra nội dung greet; hành vi HIT/MISS cần Redis + context Spring (lab tay).</p>
 */
class HelloCacheServiceTest {

	private final HelloCacheService helloCacheService = new HelloCacheService();

	@Test
	void greet_returnsHelloMessage() {
		// 1) Gọi thẳng — luôn chạy sleep (không có @Cacheable proxy)
		long start = System.currentTimeMillis();
		String msg = helloCacheService.greet("Khoa");
		long took = System.currentTimeMillis() - start;

		// 2) Nội dung đúng + chậm ~2s (giả lập)
		assertEquals("Xin chào, Khoa!", msg);
		assertTrue(took >= 1900, "expect ~2s sleep without cache proxy, took=" + took);
	}

}
